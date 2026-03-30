package io.github.hawah.structure_crafter.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.util.files.FileHelper;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.blackboard.Blackboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@SuppressWarnings({"ConstantValue", "DataFlowIssue"})
public class BlackboardHandler {

    private Object outlineSlot = new Object();
    private Object centerSlot = new Object();

    private BlockPos firstPos;
    private BlockPos secondPos;
    private BlockPos centerPos;
    private BlockPos selectedPos;
    private int reach = 4;
    private final int MAX_REACH = 100;

    public boolean onMouseScroll(double delta) {
        if (firstPos == null || !canReachAir()) {
            return false;
        }
        if (!isActive()) {
            return false;
        }
        int intDelta = (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));
        reach = Mth.clamp(reach + intDelta, 0, MAX_REACH);

        return true;
    }

    /**
     * Handle MouseInput when local player holds a {@link Blackboard} item
     * @param button Use {@link GLFW} patterns
     * */
    public boolean onMouseInput(int button, boolean pressed) {
        boolean isRight;
        if ((!(isRight = (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) && button != GLFW.GLFW_MOUSE_BUTTON_LEFT) || !pressed) {
            return false;
        }
        if (!isActive()) {
            return false;
        }



        if (!isRight & selectedPos != null) {
            if (Minecraft.getInstance().player.isShiftKeyDown()) {
                deleteCenter();
                return true;
            }
            centerPos = selectedPos;
            return true;
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
            secondPos = selectedPos;
            return true;
        }
        firstPos = selectedPos;
        secondPos = null;

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
                false
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

    private static ListTag newIntegerList(int... pValues) {
        ListTag listtag = new ListTag();
        for (int i : pValues)
            listtag.add(IntTag.valueOf(i));
        return listtag;
    }

    private boolean canReachAir() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);
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
        if (trace != null && trace.getType() == HitResult.Type.BLOCK) {

            selectedPos = trace.getBlockPos();
        } else
            selectedPos = null;

        if (canReachAir()) {
            Vec3 targetVec = player.getEyePosition(0)
                    .add(player.getLookAngle()
                            .scale(reach));
            selectedPos = BlockPos.containing(targetVec);
            System.out.println(1);
        }

        if (firstPos != null && !player.isShiftKeyDown()) {
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
                    .setRGBA(1, 1, 1, 1)
                    .setPriority(0)
                    .finish();
        }

        if (centerPos != null) {
            Outliner.getInstance()
                    .chaseThickBox(
                            centerSlot,
                            centerPos,
                            centerPos
                    )
                    .setRGBA(1, 216F/255, 0, 1)
                    .setPriority(1)
                    .finish();
        }
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
        float f = player.getXRot();
        float f1 = player.getYRot();
        float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        return origin.add((double) f6 * range, (double) f5 * range, (double) f7 * range);
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
}
