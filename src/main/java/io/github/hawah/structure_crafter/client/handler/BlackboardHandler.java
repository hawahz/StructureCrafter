package io.github.hawah.structure_crafter.client.handler;

import com.mojang.logging.LogUtils;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.compat.sable.SableLogicTransformCompat;
import io.github.hawah.structure_crafter.networking.structure_sync.ServerboundSaveWorldStructurePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
import io.github.hawah.structure_crafter.util.StructureHandler;
import io.github.hawah.structure_crafter.util.files.FileHelper;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"ConstantValue", "DataFlowIssue", "SameParameterValue"})
public class BlackboardHandler implements IHandler{

    private Object outlineSlot = new Object();
    private Object centerSlot = new Object();

    private BlockPos firstPos;
    private BlockPos secondPos;
    private BlockPos centerPos;

    private BlockPos selectedPos;
    private AABB cachedBoundingBox;
    private Direction selectedFace;
    private int reach = 4;
    private int scrolling = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MAX_REACH = 100;

    public BlackboardHandler() {
        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                ()-> this.isActive() && selectedPos != null,
                () -> {
                    if (firstPos == null) {
                        firstPos = selectedPos;
                        return;
                    }
                    if (secondPos == null) {
                        setSecondPos(selectedPos);
                        return;
                    }
                    firstPos = selectedPos;
                    setSecondPos(null);
                },
                () -> selectedPos == null?
                        firstPos == null?
                                LangData.HUD_TIP_BLACKBOARD_SELECT_FIRST_POINT.get():
                                LangData.HUD_TIP_BLACKBOARD_SELECT_SECOND_POINT.get():
                        LangData.HUD_TIP_BLACKBOARD_CLEAR_AND_SELECT_FIRST.get()
        ));
        KeyBinding.LEFT.bind(KeyBinding.Action.of(
                ()-> this.isActive() && selectedPos != null,
                () -> centerPos = transform(selectedPos),
                LangData.HUD_TIP_BLACKBOARD_SELECT_ANCHOR.get()
        ));
        KeyBinding.SHIFT_R.bind(KeyBinding.Action.of(
                () -> this.isActive() && firstPos != null,
                this::delete,
                LangData.HUD_TIP_BLACKBOARD_DELETE_ALL.get()
        ));
        KeyBinding.SHIFT_L.bind(KeyBinding.Action.of(
                () -> this.isActive() && centerPos != null,
                this::deleteCenter,
                LangData.HUD_TIP_BLACKBOARD_DELETE_ANCHOR.get()
        ));
        KeyBinding.CTRL.bind(KeyBinding.Action.of(
                () -> this.isActive() && firstPos != null && secondPos != null,
                KeyBinding.Action.EMPTY,
                LangData.HUD_TIP_BLACKBOARD_SHOW_ALL_FACES.get()
        ));
        KeyBinding.CTRL_L.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null || centerPos == null),
                KeyBinding.Action.EMPTY,
                LangData.HUD_TIP_BLACKBOARD_PICK_AIR_CENTER.get()
        ));
        KeyBinding.CTRL_R.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null || centerPos == null),
                KeyBinding.Action.EMPTY,
                LangData.HUD_TIP_BLACKBOARD_PICK_AIR_POINT.get()
        ));
        KeyBinding.CTRL_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null || centerPos == null),
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    reach = Mth.clamp(reach + intDelta, 0, MAX_REACH);
                },
                LangData.HUD_TIP_BLACKBOARD_CHANGE_DISTANCE.get()
        ));
        // TODO Take over scrolling
        KeyBinding.CTRL_ALT_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && selectedFace != null,
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    pushOrPullFace(intDelta, true);
                },
                LangData.HUD_TIP_BLACKBOARD_SELECT_OPPOSITE_FACE.get(),
                () -> {
//                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
//                    pushOrPullFace(intDelta, true);
                }
        ));
        KeyBinding.ALT_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && selectedFace != null,
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    pushOrPullFace(intDelta, false);
                },
                LangData.HUD_TIP_BLACKBOARD_PUSH_OR_PULL_FACE.get()
        ));
    }

    /**
     * Save Structure to disk if selected box is valid <br>
     * Use {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate} to save details <br>
     * TODO
     * */
    public void saveStructure(String fileName, boolean overwrite) {
        if (firstPos == null) {
            return;
        }

        Path dir = Paths.STRUCTURE_DIR;

        BoundingBox bb = BoundingBox.fromCorners(firstPos, secondPos);
        BlockPos origin = new BlockPos(bb.minX(), bb.minY(), bb.minZ());
        BlockPos bounds = new BlockPos(bb.getXSpan(), bb.getYSpan(), bb.getZSpan());

        StructureTemplate structure = new StructureTemplate();
        Level level = Minecraft.getInstance().level;
        structure.fillFromWorld(level, origin, bounds, true, Blocks.AIR);
        CompoundTag data = structure.save(new CompoundTag());

        data.put("center", StructureHandler.newIntegerList(
                centerPos.getX() - origin.getX(),
                centerPos.getY() - origin.getY(),
                centerPos.getZ() - origin.getZ()
        ));

        if (fileName.isEmpty())
            fileName = "new_structure";
        if (!overwrite)
            fileName = FileHelper.getValidFileName(fileName, dir, "nbt");
        if (!fileName.endsWith(".nbt"))
            fileName += ".nbt";
        Path file = dir.resolve(fileName).toAbsolutePath();

        try {
            Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
                NbtIo.writeCompressed(data, out);
            }
            Networking.sendToServer(new ServerboundSaveWorldStructurePacket(fileName, firstPos, secondPos, centerPos, overwrite));
            Minecraft.getInstance().player.displayClientMessage(
                    LangData.INFO_CREATE_FILE_SUCCESS.get(fileName),
                    true
            );
        } catch (IOException e) {
            LogUtils.getLogger().error("Occurred Error when saving structure.", e);
        } finally {
            Outliner.getInstance().thickBox(outlineSlot)
                    .setRGBA(0, 1, 0, 1)
                    .lazyFade(20)
                    .discard()
                    .finish();
            Outliner.getInstance().thickBox(centerSlot)
                    .setRGBA(0, 1, 0, 1)
                    .lazyFade(20)
                    .discard()
                    .finish();
            discard();
        }
    }

    @Override
    public void tick() {
        if (!isVisible()) {
            Outliner.getInstance().thickBox(outlineSlot)
                    .fade()
                    .finish();
            Outliner.getInstance().thickBox(centerSlot)
                    .fade()
                    .finish();
        }

        if (!isActive()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        BlockHitResult trace = RaycastHelper.rayTraceRange(player.level(), player, 75);

        // buffered selected pos
        if (trace != null && trace.getType() == HitResult.Type.BLOCK) {
            setSelectedPos(trace.getBlockPos());
        } else
            setSelectedPos(null);

        // select face
        if (firstPos != null && secondPos != null && Screen.hasAltDown()) {
            selectedFace = scrolling <= 0? intersectRayWithBox(
                    player.getEyePosition(),
                    RaycastHelper.getTraceTarget(player, 300, player.getEyePosition())
            ) : selectedFace;
            if (canSelectOpposite()) {
                selectedFace = selectedFace.getOpposite();
            }
            scrolling = Math.max(0, scrolling - 1);
        } else{
            selectedFace = null;
            scrolling = 0;
        }

        // select in the air
        if (canReachAir()) {
            Vec3 targetVec = transform(player.getEyePosition(0)
                    .add(player.getLookAngle()
                            .scale(reach)));
            setSelectedPos(BlockPos.containing(targetVec));
        }

        if (firstPos != null && (selectedPos != null || secondPos != null) && cachedBoundingBox != null) {
            Vec3i size = (secondPos == null? selectedPos : secondPos).subtract(firstPos);

            int volume = (Math.abs(size.getX()) + 1) * (Math.abs(size.getY()) + 1) * (Math.abs(size.getZ()) + 1);
            player.displayClientMessage(
                    LangData.HUD_BLACKBOARD_SELECTION.get(
                            (isOversizeX()?"§c" : "") + (Math.abs(size.getX()) + 1) + "§r",
                            (isOversizeY()?"§c" : "") + (Math.abs(size.getY()) + 1) + "§r",
                            (isOversizeZ()?"§c" : "") + (Math.abs(size.getZ()) + 1) + "§r",
                            (volume > Config.CommonConfig.MAX_VOLUME.get()? "§c" : "") + volume
                    ),
                    true
            );
        }

        // call outline renderer above
        if (firstPos != null) {
            int gb = 1;
            if (cachedBoundingBox != null) {
                gb = isValidSize() && (isValidCenter() || selectedFace != null)? gb: 0;
            }
            Outliner.getInstance()
                    .chaseThickBox(
                            outlineSlot,
                            firstPos,
                            secondPos==null?
                                    selectedPos==null?
                                            firstPos:
                                            selectedPos:
                                    secondPos
                    )
                    .face(selectedFace)
                    .faces(Minecraft.getInstance().screen == null && Screen.hasControlDown() && !Screen.hasAltDown() && secondPos != null? Direction.values(): null)
                    .setRGBA(1, gb, gb, 1)
                    .setPriority(0)
                    .finish();
        }

        if (centerPos != null || (firstPos != null && secondPos != null && selectedPos != null && selectedFace == null)) {
            BlockPos renderedCenterPos = centerPos==null? selectedPos: centerPos;
            Outliner.getInstance()
                    .chaseThickBox(
                            centerSlot,
                            renderedCenterPos,
                            renderedCenterPos
                    )
                    .setRGBA(1, 216F/255, 0, 1)
                    .setPriority(1)
                    .finish();
        } else {
            Outliner.getInstance().thickBox(centerSlot)
                    .fade()
                    .finish();
        }
    }

    public boolean isValidSize() {
        double size = cachedBoundingBox.getMaxPosition().subtract(cachedBoundingBox.getMinPosition()).lengthSqr();
        return !(size > Config.CommonConfig.MAX_VOLUME.get() ||
                isOversizeX() ||
                isOversizeY() ||
                isOversizeZ());
    }

    private boolean isOversizeZ() {
        return cachedBoundingBox.getZsize() > Config.CommonConfig.MAX_SIZE_Z.get() && Config.CommonConfig.MAX_SIZE_Z.get() != -1;
    }

    private boolean isOversizeY() {
        return cachedBoundingBox.getYsize() > Config.CommonConfig.MAX_SIZE_Y.get() && Config.CommonConfig.MAX_SIZE_Y.get() != -1;
    }

    private boolean isOversizeX() {
        return cachedBoundingBox.getXsize() > Config.CommonConfig.MAX_SIZE_X.get() && Config.CommonConfig.MAX_SIZE_X.get() != -1;
    }

    public void setSelectedPos(BlockPos selectedPos) {
        if (Objects.equals(this.selectedPos, selectedPos)) {
            return;
        }
        this.selectedPos = transform(selectedPos);

        if (secondPos != null) {
            return;
        }

        updateBoundingBox();
    }

    public void setSecondPos(BlockPos secondPos) {
        if (Objects.equals(this.secondPos, secondPos)) {
            return;
        }

        List<BlockPos> resultHolder = Arrays.asList(firstPos, secondPos);

        SableLogicTransformCompat.instance().applyReverseAreaTotalTransform(secondPos, resultHolder);

        this.firstPos = resultHolder.getFirst();
        this.secondPos = resultHolder.getLast();

        updateBoundingBox();
    }

    public void updateBoundingBox() {

        if (firstPos == null || (secondPos == null && selectedPos == null)) {
            return;
        }

        BlockPos first = firstPos;
        BlockPos second = secondPos == null? (selectedPos == null? firstPos: selectedPos) : secondPos;
        cachedBoundingBox = new AABB(
                new Vec3(
                        Math.min(first.getX(), second.getX()),
                        Math.min(first.getY(), second.getY()),
                        Math.min(first.getZ(), second.getZ())
                ),
                new Vec3(
                        Math.max(first.getX(), second.getX()) + 1.0,
                        Math.max(first.getY(), second.getY()) + 1.0,
                        Math.max(first.getZ(), second.getZ()) + 1.0
                )
        );
    }


    private void pushOrPullFace(int intDelta, boolean opposite) {
        Vec3i normal = selectedFace.getNormal();
        AABB box = cachedBoundingBox;
        AABB aabb = (intDelta < 0) ^ opposite?
                box.expandTowards(normal.getX(), normal.getY(), normal.getZ()) :
                box.contract(normal.getX(), normal.getY(), normal.getZ());
        firstPos = BlockPos.containing(aabb.getMinPosition());
        setSecondPos(BlockPos.containing(aabb.getMaxPosition().add(new Vec3(-1, -1, -1))));
        updateBoundingBox();
        scrolling += Math.abs(intDelta * 2);
        scrolling = Math.min(scrolling, 6);
    }

    private boolean canPushOrPullFace() {
        return selectedFace != null && Screen.hasAltDown();
    }

    private boolean canSelectOpposite() {
        return Screen.hasControlDown() && selectedFace != null && scrolling <= 0;
    }

    private boolean canReachAir() {
        return Screen.hasControlDown() && (firstPos == null || secondPos == null || centerPos == null);
    }


    /**
     * Whether the Handler is active, only when player holds a blackboard item and no screen present
     * */
    @Override
    public boolean isActive() {
        return isPresent() && Minecraft.getInstance().player.getMainHandItem().is(ItemRegistries.BLACKBOARD);
    }

    @Override
    public boolean isVisible() {
        return isActive() || (isPresent() && Minecraft.getInstance().player.getOffhandItem().is(ItemRegistries.BLACKBOARD));
    }

    /**
     * Discard present handler data and outline data. Usually cause a fade and discard on the current binded outline
     * */
    public void discard() {
        outlineSlot = new Object();
        firstPos = null;
        selectedPos = null;
        secondPos = null;
        centerSlot = new Object();
        centerPos = null;
    }

    public boolean hasSelection() {
        return firstPos != null;
    }

    public boolean hasCenter() {
        return centerPos != null;
    }

    public void delete() {
        Outliner.getInstance().thickBox(outlineSlot)
                .setRGBA(1, 0, 0, 1)
                .lazyFade(40)
                .discard()
                .finish();
        Outliner.getInstance().thickBox(centerSlot)
                .setRGBA(1, 0, 0, 1)
                .lazyFade(40)
                .discard()
                .finish();
        discard();

    }

    public void deleteCenter() {
        Outliner.getInstance().thickBox(centerSlot)
                .setRGBA(1, 0, 0, 1)
                .lazyFade(40)
                .discard()
                .finish();
        centerSlot = new Object();
        centerPos = null;
    }

    private Direction intersectRayWithBox(Vec3 from, Vec3 direction) {

        List<Vec3> dataHolder = new ArrayList<>(List.of(from, direction));

        SableLogicTransformCompat.instance().transformRayIntersectData(from, direction, dataHolder, cachedBoundingBox.getCenter());

        BlockHitResult clip = AABB.clip(List.of(cachedBoundingBox), dataHolder.get(0), dataHolder.get(1), BlockPos.ZERO);
        return clip==null? null : clip.getDirection();
    }


    public boolean isValidCenter() {
        return (centerPos != null && cachedBoundingBox.contains(centerPos.getCenter())) ||
                (centerPos == null && selectedPos != null && cachedBoundingBox.contains(selectedPos.getCenter()));
    }

    private boolean isPhysicalSide() {
        return SableLogicTransformCompat.instance().isPhysical(firstPos);
    }

    private BlockPos transform(BlockPos pos) {
        SableLogicTransformCompat transformer = SableLogicTransformCompat.instance();
        if (isPhysicalSide()) {
            return transformer.isSameSide(pos, firstPos)? pos: transformer.applyTransformInverse(pos, firstPos);
        } else {
            return transformer.isSameSide(pos, firstPos)? pos: transformer.applyTransform(pos);
        }
    }

    private Vec3 transform(Vec3 pos) {
        SableLogicTransformCompat transformer = SableLogicTransformCompat.instance();
        if (isPhysicalSide()) {
            return transformer.isPhysical(pos)? pos: transformer.applyTransformInverse(pos, firstPos.getCenter());
        } else {
            return transformer.isPhysical(pos)? transformer.applyTransform(pos, firstPos.getCenter()): pos;
        }
    }
}
