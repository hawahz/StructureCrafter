package io.github.hawah.structure_crafter.client.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Outliner {
    private static Outliner INSTANCE = null;

    private final HashMap<Object, OutlineElement<?>> outlines = new HashMap<>();

    public static boolean hasInstance() {
        return INSTANCE != null;
    }

    public static Outliner getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Outliner();
        }
        return INSTANCE;
    }

    public OutlineElement<?> thickBox(Object slot) {
        if (outlines.containsKey(slot)) {
            OutlineElement<?> outlineElement = outlines.get(slot);
            if (!(outlineElement instanceof ThickOutline)) {
                StructureCrafter.LOGGER.warn("Outline element is not a ThickOutline at thickBox()");
            }
            return outlineElement;
        }
        ThickOutline outline = new ThickOutline();
        outlines.put(slot, outline);
        return outline;
    }

    public OutlineElement<?> chaseThickBox(Object slot, @NonNull BlockPos first, @NonNull BlockPos second) {
        if (outlines.containsKey(slot)) {
            OutlineElement<?> outline = outlines.get(slot);
            if (!(outline instanceof ThickOutline)) {
                StructureCrafter.LOGGER.warn("Outline element is not a ThickOutline at chaseThickBox()   ");
            }
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
        ThickOutline outline = new ThickOutline();
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
                )
        );
        outlines.put(slot, outline);
        return outline;
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick) {
        outlines.forEach((object, outlineElement) -> {
            outlineElement.render(poseStack, buffer, cameraPos, partialTick);
        });
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
        slotsToRemove.forEach(object -> INSTANCE.clearSlot(object));
    }

    public void clearSlot(Object slot) {
        outlines.computeIfPresent(slot, (object, outlineElement) -> outlineElement.discard());
        outlines.remove(slot);
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