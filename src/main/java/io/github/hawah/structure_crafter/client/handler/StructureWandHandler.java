package io.github.hawah.structure_crafter.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.client.StructureWandModifier;
import io.github.hawah.structure_crafter.client.utils.StructureData;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.client.gui.StructureWandHUD;
import io.github.hawah.structure_crafter.client.gui.StructureWandScreen;
import io.github.hawah.structure_crafter.client.render.structure.StructureRenderer;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.IModifierItem;
import io.github.hawah.structure_crafter.item.structure_wand.AbstractStructureWand;
import io.github.hawah.structure_crafter.networking.HandholdItemChangePacket;
import io.github.hawah.structure_crafter.networking.PlaceStructurePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
import io.github.hawah.structure_crafter.util.StructureHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StructureWandHandler implements LayeredDraw.Layer, IHandler {

    private final Object slot = new Object();

    private ItemStack activeSchematicItem;
    private final StructureRenderer structureRenderer = new StructureRenderer();
    private StructureData structureData = null;

    private BlockPos selectedPos;
    private BlockPos oSelectedPos;
    private Direction playerDirection;
    private Direction oPlayerDirection;
    private Direction rawDirection;

    private boolean dirty = true;

    private boolean lock = false;

    private boolean rotateLock = false;
    private boolean renderBoundingBox = false;

    private StructureWandModifier modifier = StructureWandModifier.create(StructureWandModifier.Type.NONE);

    private int rotated;
    private final StructureWandHUD hud = new StructureWandHUD();

    public StructureWandHandler() {
        bindKeys();
    }

    private void bindKeys() {
        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                () -> isActive() && selectedPos != null,
                () ->{
                    lock = false;
                    Networking.sendToServer(new PlaceStructurePacket(activeSchematicItem.copy(), selectedPos, playerDirection));
                    modifier.onPlace(selectedPos, playerDirection);
                },
                LangData.HUD_TIP_STRUCTURE_WAND_PLACE.get()
        ));
        KeyBinding.SHIFT_R.bind(KeyBinding.Action.of(
                this::isActive,
                () -> ScreenOpener.open(new StructureWandScreen()),
                LangData.HUD_TIP_STRUCTURE_WAND_OPENC_ONFIG.get()
        ));
        KeyBinding.LEFT.bind(KeyBinding.Action.of(
                this::isActive,
                () -> lock = !lock && selectedPos != null,
                () -> lock?
                        LangData.HUD_TIP_STRUCTURE_WAND_UNLOCK.get() :
                        LangData.HUD_TIP_STRUCTURE_WAND_LOCK.get()
        ));
        KeyBinding.ALT_S.bind(KeyBinding.Action.of(
                this::isActive,
                () -> {

                },
                LangData.HUD_TIP_STRUCTURE_WAND_SWITCH.get()
        ));
        KeyBinding.CTRL_S.bind(KeyBinding.Action.of(
                this::isActive,
                () -> { },
                LangData.HUD_TIP_STRUCTURE_WAND_ROTATE.get()
        ));
        KeyBinding.SHIFT_S.bind(KeyBinding.Action.of(
                () -> isActive() && lock,
                () -> {
                    LocalPlayer player;
                    if ((player = Minecraft.getInstance().player) == null || !modifier.getType().equals(StructureWandModifier.Type.NONE))
                        return;
                    player.getDirection();
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    oSelectedPos = selectedPos;
                    selectedPos = selectedPos.offset(player.getNearestViewDirection().getNormal().multiply(intDelta));
                },
                LangData.HUD_TIP_STRUCTURE_WAND_MOVE_LOCK.get()
        ));
    }

    public void setCurrentStructure(String structure) {
        hud.setCurrentStructure(structure);
    }

    @Override
    public void tick() {
        if (!isVisible()) {
            Outliner.getInstance().thickBox(slot)
                    .fade()
                    .finish();
            modifier.clear();
            modifier = StructureWandModifier.create(StructureWandModifier.Type.NONE);
        }
        if (!isActive()) {
            activeSchematicItem = null;
            modifier.clear();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        ItemStack wandStack = player.getMainHandItem();
        if (!(player.getOffhandItem().getItem() instanceof IModifierItem modifierItem)) {
            if (!modifier.getType().equals(StructureWandModifier.Type.NONE)) {
                modifier.clear();
                modifier = StructureWandModifier.create(StructureWandModifier.Type.NONE);
            }
        } else if (!modifierItem.getType().equals(modifier.getType())) {
            modifier.clear();
            modifier = StructureWandModifier.create(modifierItem.getType());
        }

        hud.tick();

        handleChanged(wandStack, player);

        // 变换预处理
        BlockHitResult trace = RaycastHelper.rayTraceRange(
                player.level(),
                player,
                player.isCreative()? 75 : player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) * Config.CommonConfig.STRUCTURE_PLACE_DISTANCE.getAsInt()
        );
        if (!rotateLock && !lock) {
            // 当没有旋转锁定和锁定时，将玩家朝向缓存到rawDirection当中
            // 若未来发生锁定，则直接通过此渠道获取原始方向，并作用rotated
            rawDirection = player.getDirection();
        }

        // 位置处理
        handlePosition(trace, player);
        // 旋转处理
        handleRotation(player);

        // 强制解锁条件
        if (lock && player.blockPosition().distManhattan(selectedPos) > Config.CommonConfig.PREVIEW_UNLOCK_DISTANCE.getAsInt()) {
            lock = false;
        }

        // 提交渲染
        submitRenderer();
    }

    @Override
    public boolean isActive() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        return player.getMainHandItem().getItem() instanceof AbstractStructureWand;
    }

    private void handlePosition(BlockHitResult hitResult, LocalPlayer player) {
        if (lock) {
            // 锁定的时候，tick前的选择点更新，如果当前冻结选择点为null(保护性)，则维持，否则更新为选择点
            oSelectedPos = selectedPos==null? oSelectedPos : selectedPos;
            return;
        }

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            selectedPos = null;
            modifier.clear();
            return;
        }

        BlockPos hit = hitResult.getBlockPos();
        boolean lookingAtReplaceable = player.level().getBlockState(hit)
                .canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult)));
        if (!lookingAtReplaceable) {
            hit = hit.relative(hitResult.getDirection());
        }
        oSelectedPos = selectedPos==null? hit : selectedPos;

        if (modifier != null) {
            selectedPos = modifier.applyModify(hit);
        }
        //setupRenderer();
    }

    private void handleRotation(LocalPlayer player) {
        if (lock) {
            oPlayerDirection = playerDirection==null?
                    player.getDirection() :
                    playerDirection;
            playerDirection = rawDirection;
        } else if (selectedPos != null) {
            oPlayerDirection = playerDirection==null?
                    player.getDirection() :
                    playerDirection;
            playerDirection = rawDirection;
        }
        if (rotated > 0) {
            for (int i = 0; i < Math.abs(rotated); i++) {
                playerDirection = playerDirection.getClockWise();
            }
        } else if (rotated < 0) {
            for (int i = 0; i < Math.abs(rotated); i++) {
                playerDirection = playerDirection.getCounterClockWise();
            }
        }
        playerDirection = modifier.applyModify(playerDirection);
    }

    private void submitRenderer() {
        if (
                this.renderBoundingBox &&
                        selectedPos != null &&
                        structureData != null &&
                        structureData.structureTemplate() != null &&
                        structureData.center() != null
        ) {
            StructurePlaceSettings settings = new StructurePlaceSettings();
            Rotation rotation = StructureWandHandler.transferDirectionToRotation(this.playerDirection);
            settings.setRotation(rotation);
            BoundingBox boundingBox = structureData.structureTemplate()
                    .getBoundingBox(settings, selectedPos.subtract(structureData.center().rotate(rotation)));
            Outliner.getInstance()
                    .chaseThickBox(slot,
                            new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ()),
                            new BlockPos(boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ())
                    ).setRGBA(1, 1, 1, 1)
                    .setPriority(2)
                    .smooth(1)
                    .finish();
        } else {
            Outliner.getInstance()
                    .thickBox(slot)
                    .fade()
                    .finish();
        }
        modifier.submit(selectedPos, playerDirection, structureData);
    }

    /**
     * 处理结构文件改变。
     * 若当前物品与缓存不一致，或是已标记为脏（标记为脏意味着当前选择的结构发生了变化），
     * 则重新加载结构数据，也就是结构发生变化，需要告知渲染器结构数据已改变，重新建立顶点缓存
     * @param stack: 玩家主手的物品
     * @param player： 玩家
     */
    private void handleChanged(ItemStack stack, LocalPlayer player) {
        if (activeSchematicItem == stack && !dirty) {
            return;
        }
        hud.loadStructures();
        structureRenderer.clearCache();

        // 若是因为物品发生改变，则重新设置结构文件并修改hud
        if (activeSchematicItem != stack) {
            activeSchematicItem = stack;
            hud.setCurrentStructure(activeSchematicItem.get(DataComponentTypeRegistries.STRUCTURE_FILE));
        }
        this.renderBoundingBox = AbstractStructureWand.isBoundsVisible(activeSchematicItem);
        setupRenderer();
        structureRenderer.setDirty();
        String currentFile = hud.getCurrentStructure();
        // 如果hud的当前文件不为空，则
        if (!currentFile.isEmpty()) {
            //lock = false;
            AbstractStructureWand.selectStructure(activeSchematicItem, currentFile);
            AbstractStructureWand.setOwnerName(activeSchematicItem, player.getName().getString());
            Networking.sendToServer(new HandholdItemChangePacket(activeSchematicItem));
        }
        dirty = false;
    }

    public boolean onMouseScroll(double delta) {
        if (!isActive()) {
            return false;
        }

        if (Screen.hasAltDown()) {
            if (delta > 0) {
                String currentFile = hud.scrollUp();
                if (currentFile.isEmpty()) {
                    return true;
                }
                setDirty();
                return true;
            } else if (delta < 0) {
                String currentFile = hud.scrollDown();
                if (currentFile.isEmpty()) {
                    return true;
                }
                setDirty();
                return true;
            }
        }
        if (selectedPos == null) {
            return false;
        }
        if (Screen.hasControlDown()) {
            int intDelta = (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));
            rotated = rotated + intDelta;
            rotated %= 4;
            return true;
        }
        return false;
    }

    private void setupRenderer() {
        Level clientWorld = Minecraft.getInstance().level;
        structureData = StructureHandler.loadSchematic(clientWorld, activeSchematicItem);
    }

    public void render(PoseStack ms, MultiBufferSource.BufferSource buffer, Vec3 camera) {
        if (!isActive()) {
            return;
        }
        if (structureData == null || selectedPos == null) {
            return;
        }

        structureRenderer.render(
                ms,
                buffer,
                camera,
                structureData.structureTemplate(),
                selectedPos,
                oSelectedPos,
                structureData.center(),
                playerDirection,
                oPlayerDirection,
                Minecraft.getInstance().level
        );

        buffer.endBatch();
    }

    public static Rotation transferDirectionToRotation(Direction direction) {
        return switch (direction) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || !isActive())
            return;
        hud.render(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true));
    }

    public boolean isRotateLock() {
        return rotateLock;
    }

    public void setRotateLock(boolean rotateLock) {
        this.rotateLock = rotateLock;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void setDirty() {
        this.dirty = true;
    }
}