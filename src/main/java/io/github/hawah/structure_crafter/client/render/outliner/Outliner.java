package io.github.hawah.structure_crafter.client.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.client.render.OverRenderType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Outliner {
    private static Outliner INSTANCE = null;

    private final HashMap<Object, OutlineElement<?>> outlines = new HashMap<>();
    private final HashMap<Object, OutlineElement<?>> overOutlines = new HashMap<>();

    public static boolean hasInstance() {
        return INSTANCE != null;
    }

    public static Outliner getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Outliner();
        }
        return INSTANCE;
    }

    public static void renderInto(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
        if (INSTANCE == null) {
            return;
        }
        INSTANCE.render(poseStack, bufferSource, cameraPos, partialTick);
        INSTANCE.renderOverlay(poseStack, bufferSource, cameraPos, partialTick);
    }

    public OutlineElement<?> thickBox(Object slot) {
        var slotHolder = outlines.containsKey(slot)?
                outlines :
                overOutlines.containsKey(slot)?
                        overOutlines :
                        null;
        if (slotHolder == null) {
            return new ThickOutline();
        }
        OutlineElement<?> outlineElement = slotHolder.get(slot);
        if (!(outlineElement instanceof ThickOutline)) {
            StructureCrafter.LOGGER.warn("Outline element is not a ThickOutline at thickBox()");
        }
        return outlineElement;
    }

    public OutlineElement<?> chaseThickBox(Object slot, @NonNull BlockPos first, @NonNull BlockPos second) {
        return chaseThickBox(slot, first, second, false);
    }

    public OutlineElement<?> chaseThickBox(Object slot, @NonNull BlockPos first, @NonNull BlockPos second, boolean overlay) {
        var slotHolder = overlay? overOutlines: outlines;
        if (slotHolder.containsKey(slot)) {
            OutlineElement<?> outline = slotHolder.get(slot);
            if (!(outline instanceof ThickOutline)) {
                StructureCrafter.LOGGER.warn("Outline element is not a ThickOutline at chaseThickBox()   ");
            }
            return mulPose(first, second, outline);
        }
        ThickOutline outline = new ThickOutline();
        slotHolder.put(slot, mulPose(first, second, outline));
        return outline;
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
        outlines.forEach((object, outlineElement) ->
                outlineElement.render(poseStack, bufferSource.getBuffer(
                        outlineElement instanceof ThickOutline?
                                RenderType.debugQuads():
                                RenderType.lines()
                ), cameraPos, partialTick)
        );
    }

    public void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
        overOutlines.forEach((object, outlineElement) ->
                outlineElement.render(poseStack, bufferSource.getBuffer(
                        outlineElement instanceof ThickOutline?
                                OverRenderType.OVERLAY_QUADS :
                                OverRenderType.OVERLAY_LINES
                ), cameraPos, partialTick)
        );
    }

    public OutlineElement<?> chaseBox(Object slot, @NonNull BlockPos first, @NonNull BlockPos second) {
        return chaseBox(slot, first, second, false);
    }

    public OutlineElement<?> chaseBox(Object slot, @NonNull BlockPos first, @NonNull BlockPos second, boolean overlay) {
        var slotHolder = overlay? overOutlines: outlines;
        if (slotHolder.containsKey(slot)) {
            OutlineElement<?> outline = slotHolder.get(slot);
            if (!(outline instanceof FineOutline)) {
                StructureCrafter.LOGGER.warn("Outline element is not a FineOutline at chaseBox()");
            }
            return mulPose(first, second, outline);
        }
        FineOutline outline = new FineOutline();
        slotHolder.put(slot, mulPose(first, second, outline));
        return outline;
    }

    public OutlineElement<?> box(Object slot) {
        var slotHolder = outlines.containsKey(slot)?
                outlines :
                overOutlines.containsKey(slot)?
                        overOutlines :
                        null;
        if (slotHolder == null) {
            return new FineOutline();
        }
        OutlineElement<?> outlineElement = slotHolder.get(slot);
        if (!(outlineElement instanceof FineOutline)) {
            StructureCrafter.LOGGER.warn("Outline element is not a FineOutline at box()");
        }
        return outlineElement;
    }

    @NotNull
    private OutlineElement<?> mulPose(@NonNull BlockPos first, @NonNull BlockPos second, OutlineElement<?> outline) {
        outline.setPositions(
                new Vec3(
                        Math.min(first.getX(), second.getX()),
                        Math.min(first.getY(), second.getY()),
                        Math.min(first.getZ(), second.getZ())
                ),
                new Vec3(
                        Math.max(first.getX(), second.getX()) + 1.0,
                        Math.max(first.getY(), second.getY()) + 1.0,
                        Math.max(first.getZ(), second.getZ()) + 1.0
                ));
        return outline;
    }

    public static void tick() {
        if (!hasInstance()) {
            return;
        }
        List<Object> slotsToRemove = new ArrayList<>();
        INSTANCE.outlines.forEach((object, outlineElement) -> {
            outlineElement.tick();
            if (Math.abs(outlineElement.oa) < 0.01 && outlineElement.discarded) {
                slotsToRemove.add(object);
            }
        });
        List<Object> outlineSlotsToRemove = new ArrayList<>();
        INSTANCE.overOutlines.forEach((object, outlineElement) -> {
            outlineElement.tick();
            if (Math.abs(outlineElement.oa) < 0.01 && outlineElement.discarded) {
                outlineSlotsToRemove.add(object);
            }
        });
        slotsToRemove.forEach(object -> INSTANCE.clearSlot(object));
        outlineSlotsToRemove.forEach(object -> INSTANCE.clearSlot(object));
    }

    public void clearSlot(Object slot) {
        outlines.computeIfPresent(slot, (object, outlineElement) -> outlineElement.discard());
        outlines.remove(slot);
    }

    public void clear() {
        outlines.clear();
        overOutlines.clear();
    }

    public void updateOutlinePosition(Object slot, Vec3 p0, Vec3 p1) {
        if (outlines.containsKey(slot)) {
            outlines.get(slot).setPositions(p0, p1);
        } else {
            ThickOutline outline = new ThickOutline();
            outline.setPositions(p0, p1);
            outlines.put(slot, outline);
        }
    }

    public void updateOutlineColor(Object slot, float r, float g, float b, float a) {
        if (outlines.containsKey(slot)) {
            outlines.get(slot).setRGBA(r, g, b, a);
        } else {
            ThickOutline outline = new ThickOutline();
            outline.setRGBA(r, g, b, a);
            outlines.put(slot, outline);
        }
    }
}