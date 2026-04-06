package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
import io.github.hawah.structure_crafter.util.files.FileHelper;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.blackboard.Blackboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"ConstantValue", "DataFlowIssue", "SameParameterValue"})
public class BlackboardHandler {

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
                                Component.literal("Select First Point"):
                                Component.literal("Select Second Point"):
                        Component.literal("Clear and Select First")
        ));
        KeyBinding.LEFT.bind(KeyBinding.Action.of(
                ()-> this.isActive() && selectedPos != null,
                () -> centerPos = selectedPos,
                Component.literal("Select Center Point")
        ));
        KeyBinding.SHIFT_R.bind(KeyBinding.Action.of(
                () -> this.isActive() && firstPos != null,
                this::delete,
                Component.literal("Delete All")
        ));
        KeyBinding.SHIFT_L.bind(KeyBinding.Action.of(
                () -> this.isActive() && centerPos != null,
                this::deleteCenter,
                Component.literal("Delete Anchor")
        ));
        KeyBinding.CTRL.bind(KeyBinding.Action.of(
                () -> this.isActive() && firstPos != null && secondPos != null,
                KeyBinding.Action.EMPTY,
                Component.literal("Show All Faces")
        ));
        KeyBinding.CTRL_L.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null || centerPos == null),
                KeyBinding.Action.EMPTY,
                Component.literal("Pick Air as Center")
        ));
        KeyBinding.CTRL_R.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null || centerPos == null),
                KeyBinding.Action.EMPTY,
                Component.literal("Pick Air as Point")
        ));
        KeyBinding.CTRL_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null || centerPos == null),
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    reach = Mth.clamp(reach + intDelta, 0, MAX_REACH);
                },
                Component.literal("Change Reach Distance")
        ));
        // TODO Take over scrolling
        KeyBinding.CTRL_ALT_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && selectedFace != null,
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    pushOrPullFace(intDelta, true);
                },
                Component.literal("Select Opposite"),
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
                Component.literal("Push/Pull Face")
        ));
    }

    @Deprecated
    public boolean onMouseScroll(double delta) {
        if (firstPos == null) {
            return false;
        }
        if (!isActive()) {
            return false;
        }
        int intDelta = (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));
        if (canPushOrPullFace()) {
            pushOrPullFace(intDelta, Screen.hasControlDown());
            return true;
        }

        return false;
    }

    /**
     * Handle MouseInput when local player holds a {@link Blackboard} item
     * @param button Use {@link GLFW} patterns
     * */
    @Deprecated
    public boolean onMouseInput(int button, boolean pressed) {
        boolean isRight;
        if ((!(isRight = (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) && button != GLFW.GLFW_MOUSE_BUTTON_LEFT) || !pressed) {
            return false;
        }
        if (!isActive()) {
            return false;
        }



        if (!isRight) {
            if (Minecraft.getInstance().player.isShiftKeyDown()) {
                deleteCenter();
                return true;
            }
            if (selectedPos != null) {
                centerPos = selectedPos;
                return true;
            }
        }

        if (Minecraft.getInstance().player.isShiftKeyDown()) {
            delete();
            return true;
        }

        if (selectedPos == null) {
            return false;
        }

        if (firstPos == null) {
            firstPos = selectedPos;
            return true;
        }
        if (secondPos == null) {
            setSecondPos(selectedPos);
            return true;
        }
        firstPos = selectedPos;
        setSecondPos(null);

        return true;
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

        data.put("center", newIntegerList(
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
        } catch (IOException e) {
            StructureCrafter.LOGGER.error("Occurred Error when saving structure.", e);
        }

        Minecraft.getInstance().player.displayClientMessage(
                LangData.INFO_CREATE_FILE_SUCCESS.get(fileName),
                true
        );


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
        BlockHitResult trace = rayTraceRange(player.level(), player, 75);

        // buffered selected pos
        if (trace != null && trace.getType() == HitResult.Type.BLOCK) {
            setSelectedPos(trace.getBlockPos());
        } else
            setSelectedPos(null);

        // select face
        if (firstPos != null && secondPos != null && Screen.hasAltDown()) {
            selectedFace = scrolling <= 0? intersectRayWithBox(
                    player.getEyePosition(),
                    getTraceTarget(player, 300, player.getEyePosition())
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
            Vec3 targetVec = player.getEyePosition(0)
                    .add(player.getLookAngle()
                            .scale(reach));
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
                            (volume > Config.MAX_VOLUME.get()? "§c" : "") + volume
                    ),
                    true
            );
        }

        // call outline renderer above
        if (firstPos != null) {
            int gb = 1;
            if (cachedBoundingBox != null) {
                gb = isValidSize()? gb: 0;
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

        if (centerPos != null || (firstPos != null && secondPos != null && selectedPos != null)) {
            Outliner.getInstance()
                    .chaseThickBox(
                            centerSlot,
                            centerPos==null? selectedPos: centerPos,
                            centerPos==null? selectedPos: centerPos
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
        return !(size > Config.MAX_VOLUME.get() ||
                isOversizeX() ||
                isOversizeY() ||
                isOversizeZ());
    }

    private boolean isOversizeZ() {
        return cachedBoundingBox.getZsize() > Config.MAX_SIZE_Z.get() && Config.MAX_SIZE_Z.get() != -1;
    }

    private boolean isOversizeY() {
        return cachedBoundingBox.getYsize() > Config.MAX_SIZE_Y.get() && Config.MAX_SIZE_Y.get() != -1;
    }

    private boolean isOversizeX() {
        return cachedBoundingBox.getXsize() > Config.MAX_SIZE_X.get() && Config.MAX_SIZE_X.get() != -1;
    }

    public void setSelectedPos(BlockPos selectedPos) {
        if (Objects.equals(this.selectedPos, selectedPos)) {
            return;
        }
        this.selectedPos = selectedPos;

        if (secondPos != null) {
            return;
        }

        updateBoundingBox();
    }

    public void setSecondPos(BlockPos secondPos) {
        if (Objects.equals(this.secondPos, secondPos)) {
            return;
        }

        this.secondPos = secondPos;

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
     * Cast a Ray Trace in Level, from Player's Camera to blocks max to range
     * @param player Ray Trace Source
     * @param range How long would this ray keep detecting
     * */
    private static BlockHitResult rayTraceRange(Level level, Player player, double range) {
        Vec3 origin = player.getEyePosition();
        Vec3 target = getTraceTarget(player, range, origin);
        ClipContext context = new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        return level.clip(context);
    }


    static Vec3 getTraceTarget(Player player, double range, Vec3 origin) {
        return RaycastHelper.getTraceTarget(player, range, origin);
    }

    /**
     * Whether the Handler is active, only when player holds a blackboard item and no screen present
     * */
    public boolean isActive() {
        return isPresent() && Minecraft.getInstance().player.getMainHandItem().is(ItemRegistries.BLACKBOARD);
    }

    public boolean isVisible() {
        return isActive() || (isPresent() && Minecraft.getInstance().player.getOffhandItem().is(ItemRegistries.BLACKBOARD));
    }

    private boolean isPresent() {
        return Minecraft.getInstance() != null && Minecraft.getInstance().level != null
                /*&& Minecraft.getInstance().screen == null*/ && Minecraft.getInstance().player != null;
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
        BlockHitResult clip = AABB.clip(List.of(cachedBoundingBox), from, direction, BlockPos.ZERO);
        return clip==null? null : clip.getDirection();

    }

    private static ListTag newIntegerList(int... pValues) {
        ListTag listtag = new ListTag();
        for (int i : pValues)
            listtag.add(IntTag.valueOf(i));
        return listtag;
    }
}
