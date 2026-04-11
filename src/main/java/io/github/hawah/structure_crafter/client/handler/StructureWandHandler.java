package io.github.hawah.structure_crafter.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.client.utils.StructureData;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.client.gui.StructureWandHUD;
import io.github.hawah.structure_crafter.client.gui.StructureWandScreen;
import io.github.hawah.structure_crafter.client.render.StructureRenderer;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.structure_wand.AbstractStructureWand;
import io.github.hawah.structure_crafter.networking.ClientboundContainerSlotChangedPacket;
import io.github.hawah.structure_crafter.networking.HandholdItemChangePacket;
import io.github.hawah.structure_crafter.networking.PlaceStructurePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class StructureWandHandler implements LayeredDraw.Layer {

    private final Object slot = new Object();

    private ItemStack activeSchematicItem;
    private BlockPos selectedPos;
    private BlockPos oSelectedPos;
    private final StructureRenderer structureRenderer = new StructureRenderer();
    private StructureData structureData = null;
    private Direction playerDirection;
    private Direction oPlayerDirection;
    private Direction rawDirection;
    private boolean active;
    private boolean dirty = true;

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    private boolean lock = false;
    private boolean rotateLock = false;
    private boolean renderBoundingBox = false;
    private int rotated;
    private final StructureWandHUD hud = new StructureWandHUD();
    @Deprecated
    public ItemStackData data = new ItemStackData();

    public StructureWandHandler() {
        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                () -> active && selectedPos != null,
                () ->{
                    lock = false;
                    Networking.sendToServer(new PlaceStructurePacket(activeSchematicItem.copy(), selectedPos, playerDirection));
                },
                LangData.HUD_TIP_STRUCTURE_WAND_PLACE.get()
        ));
        KeyBinding.SHIFT_R.bind(KeyBinding.Action.of(
                () -> active,
                () -> ScreenOpener.open(new StructureWandScreen()),
                LangData.HUD_TIP_STRUCTURE_WAND_OPENC_ONFIG.get()
        ));
        KeyBinding.LEFT.bind(KeyBinding.Action.of(
                () -> active,
                () -> lock = !lock && selectedPos != null,
                LangData.HUD_TIP_STRUCTURE_WAND_LOCK_UNLOCK.get()
        ));
//        KeyBinding.ALT_S.bind(KeyBinding.Action.of(
//                () -> active,
//                () -> {
//
//                }
//        ));
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setCurrentStructure(String structure) {
        hud.setCurrentStructure(structure);
    }

    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack stack;
        if (!((stack = player.getMainHandItem()).getItem() instanceof AbstractStructureWand)) {
            active = false;
            activeSchematicItem = null;
            Outliner.getInstance().thickBox(slot)
                    .fade()
                    .finish();
            return;
        }

        hud.tick();

        active = true;
        if (activeSchematicItem != stack || dirty) {
            hud.loadStructures();
            structureRenderer.clearCache();
            if (activeSchematicItem != stack) {
                activeSchematicItem = stack;
                hud.setCurrentStructure(activeSchematicItem.get(DataComponentTypeRegistries.STRUCTURE_FILE));
            }
            this.renderBoundingBox = AbstractStructureWand.isBoundsVisible(activeSchematicItem);
            setupRenderer();
            String currentFile = hud.getCurrentStructure();
            if (!currentFile.isEmpty()) {
                lock = false;
                AbstractStructureWand.selectStructure(activeSchematicItem, currentFile);
                Networking.sendToServer(new HandholdItemChangePacket(activeSchematicItem));
            }
            dirty = false;
        }

        BlockHitResult trace = RaycastHelper.rayTraceRange(
                player.level(),
                player,
                player.isCreative()? 75 : player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) * Config.CommonConfig.STRUCTURE_PLACE_DISTANCE.getAsInt()
        );
        if (!rotateLock) {
            rawDirection = player.getDirection();
        }
        if (trace.getType() == HitResult.Type.BLOCK && !lock) {

            BlockPos hit = trace.getBlockPos();
            boolean replaceable = player.level().getBlockState(hit)
                    .canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, trace)));
            if (!replaceable)
                hit = hit.relative(trace.getDirection());
            oSelectedPos = selectedPos==null? hit : selectedPos;
            selectedPos = hit;
            oPlayerDirection = playerDirection==null?
                    player.getDirection() :
                    playerDirection;
            playerDirection = rawDirection;
            setupRenderer();
        } else if (!lock) {
            selectedPos = null;
        } else {
            oSelectedPos = selectedPos==null? oSelectedPos : selectedPos;
            oPlayerDirection = playerDirection==null? player.getDirection() : playerDirection;
            playerDirection = playerDirection==null? player.getDirection() : playerDirection;
        }
        if (lock) {

        } else if (rotated > 0) {
            for (int i = 0; i < Math.abs(rotated); i++) {
                playerDirection = playerDirection.getClockWise();
            }
        } else if (rotated < 0) {
            for (int i = 0; i < Math.abs(rotated); i++) {
                playerDirection = playerDirection.getCounterClockWise();
            }
        }

        if (lock && player.blockPosition().distManhattan(selectedPos) > Config.CommonConfig.PREVIEW_UNLOCK_DISTANCE.getAsInt()) {
            lock = false;
        }

        if (this.renderBoundingBox && selectedPos != null && structureData != null && structureData.structureTemplate() != null && structureData.center() != null) {
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

    }

    @Deprecated
    public boolean onMouseInput(int button, boolean pressed) {
        if (!active) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && pressed) {
            lock = !lock && selectedPos != null;
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return false;
        }
        if (!pressed) {
            return false;
        }
        if (Screen.hasShiftDown()) {
            ScreenOpener.open(new StructureWandScreen());
            return true;
        }
        if (selectedPos == null) {
            return false;
        }

//        if (lock) {
//            Minecraft.getInstance().player.displayClientMessage(
//                    LangData, true
//            );
//            return false;
//        }

        lock = false;
        Networking.sendToServer(new PlaceStructurePacket(activeSchematicItem.copy(), selectedPos, playerDirection));
        return true;
    }

    public boolean onMouseScroll(double delta) {
        if (!active) {
            return false;
        }

        if (Screen.hasAltDown()) {
            if (delta > 0) {
                String currentFile = hud.scrollUp();
                if (currentFile.isEmpty()) {
                    return true;
                }
//                lock = false;
//                activeSchematicItem.set(DataComponentTypeRegistries.STRUCTURE_FILE, currentFile);
//                Networking.sendToServer(new HandholdItemChangePacket(activeSchematicItem));
                dirty = true;
                return true;
            } else if (delta < 0) {
                String currentFile = hud.scrollDown();
                if (currentFile.isEmpty()) {
                    return true;
                }
//                lock = false;
//                activeSchematicItem.set(DataComponentTypeRegistries.STRUCTURE_FILE, currentFile);
//                Networking.sendToServer(new HandholdItemChangePacket(activeSchematicItem));
                dirty = true;
                return true;
            }
        }
        if (selectedPos == null) {
            return false;
        }
        if (Screen.hasControlDown()) {
            if (lock) {
                return true;
            }
            int intDelta = (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));
            rotated = rotated + intDelta;
            rotated %= 4;
            return true;
        }
        return false;
    }

    private void setupRenderer() {
        Level clientWorld = Minecraft.getInstance().level;
        structureData = AbstractStructureWand.loadSchematic(clientWorld, activeSchematicItem);
//        hud.setCurrentStructure(activeSchematicItem.get(DataComponentTypeRegistries.STRUCTURE_FILE));
    }

    public void render(PoseStack ms, MultiBufferSource.BufferSource buffer, Vec3 camera) {
        if (!active) {
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
                selectedPos.subtract(structureData.center().rotate(transferDirectionToRotation(playerDirection))),
                oSelectedPos.subtract(structureData.center().rotate(transferDirectionToRotation(oPlayerDirection))),
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
        if (mc.options.hideGui || !active)
            return;
        hud.render(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true));
    }

    public boolean isRotateLock() {
        return rotateLock;
    }

    public void setRotateLock(boolean rotateLock) {
        this.rotateLock = rotateLock;
    }

    @SuppressWarnings("unused")
    @Deprecated(forRemoval = true)
    public static class ItemStackData {

        public int slotId;
        public ItemStack itemStack;
        public Configuration currentConfiguration = Configuration.UPDATE_ALL;
        public boolean isUpdateAll = false;
        public boolean isRenderBoundingBox = false;
        public boolean isReplaceAir = false;

        public void init(ItemStack itemStack, int slotId) {
            if (this.itemStack == itemStack)
                return;
            this.itemStack = itemStack;
            this.slotId = slotId;
            this.isUpdateAll = AbstractStructureWand.getUpdateFlags(itemStack) == Block.UPDATE_ALL;
            this.isRenderBoundingBox = AbstractStructureWand.isBoundsVisible(itemStack);
            this.isReplaceAir = AbstractStructureWand.isReplaceAir(itemStack);
            this.currentConfiguration = Configuration.UPDATE_ALL;
        }

        public void config() {
            if (Minecraft.getInstance().screen == null || isInValid())
                return;
            this.currentConfiguration.apply(this);
            Networking.sendToServer(new ClientboundContainerSlotChangedPacket(slotId, itemStack));

        }
        public boolean onMouseScroll(double delta) {
            if (this.isInValid())
                return false;
            if (Screen.hasShiftDown()) {
                delta *= -1;
                int intDelta = (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));
                currentConfiguration = Configuration.values()[Math.floorMod(currentConfiguration.ordinal() + intDelta, Configuration.values().length)];
                return true;
            }
            currentConfiguration = Configuration.UPDATE_ALL;
            return false;
        }

        public boolean isInValid() {
            return this.itemStack == null;
        }

        public boolean onMouseInput(int button, boolean pressed) {
            if (!pressed || isInValid()) {
                return false;
            }

            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && Screen.hasShiftDown()) {
                config();
                return true;
            }
            return false;
        }
        public void clear() {
            this.itemStack = null;
            this.currentConfiguration = Configuration.UPDATE_ALL;
        }
        public enum Configuration {
            UPDATE_ALL(itemStackData -> {
                itemStackData.isUpdateAll = !itemStackData.isUpdateAll;
                AbstractStructureWand.setUpdateFlags(itemStackData.itemStack, itemStackData.isUpdateAll ? Block.UPDATE_ALL : 0);
            }),
            REPLACE_AIR(itemStackData -> {
                itemStackData.isReplaceAir = !itemStackData.isReplaceAir;
                AbstractStructureWand.setReplaceAir(itemStackData.itemStack, itemStackData.isReplaceAir);
            }),
            RENDER_BOUNDING_BOX(itemStackData -> {
                itemStackData.isRenderBoundingBox = !itemStackData.isRenderBoundingBox;
                AbstractStructureWand.setBoundsVisible(itemStackData.itemStack, itemStackData.isRenderBoundingBox);
            }),
            ;
            private final Consumer<ItemStackData> config;

            Configuration(Consumer<ItemStackData> config) {
                this.config = config;
            }

            public void apply(ItemStackData itemStackData) {
                this.config.accept(itemStackData);
            }
        }
    }
}