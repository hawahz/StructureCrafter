package io.github.hawah.structure_crafter.client.render.ruler;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ruler {
    private final HashMap<Object, RulerElement<?>> rulers = new HashMap<>();
    private static Ruler INSTANCE = null;

    public Ruler() {
    }

    public static Ruler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Ruler();
        }
        return INSTANCE;
    }

    public RulerElement<?> chase(Object slot, BlockPos pos0, BlockPos pos1) {

        boolean flagX = pos0.getX() <= pos1.getX();
        boolean flagY = pos0.getY() <= pos1.getY();
        boolean flagZ = pos0.getZ() <= pos1.getZ();
        float x0 = flagX ? pos0.getX() + 1 : pos0.getX();
        float y0 = flagY ? pos0.getY() + 1 : pos0.getY();
        float z0 = flagZ ? pos0.getZ() + 1 : pos0.getZ();
        float x1 = !flagX ? pos1.getX() + 1 : pos1.getX();
        float y1 = !flagY ? pos1.getY() + 1 : pos1.getY();
        float z1 = !flagZ ? pos1.getZ() + 1 : pos1.getZ();

        return rulers.compute(slot, (object, outlineElement) ->
                outlineElement == null?
                        new ThickRuler().createManhattan(new Vec3(x0, y0, z0), new Vec3(x1, y1, z1)):
                        outlineElement.setPositions(new Vec3(x0, y0, z0), new Vec3(x1, y1, z1))
        );
    }

    public static boolean hasInstance() {
        return INSTANCE != null;
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
        rulers.forEach((object, rulerElement) ->
                rulerElement.render(poseStack, bufferSource.getBuffer(
                        rulerElement instanceof ThickRuler ?
                                RenderType.debugQuads():
                                RenderType.lines()
                ), cameraPos, partialTick)
        );
    }

    public static void tick() {
        if (!hasInstance()) {
            return;
        }
        List<Object> slotsToRemove = new ArrayList<>();
        INSTANCE.rulers.forEach((object, outlineElement) -> {
            outlineElement.tick();
            if (Math.abs(outlineElement.oa) < 0.01 && outlineElement.discarded) {
                slotsToRemove.add(object);
            }
        });
        slotsToRemove.forEach(object -> INSTANCE.clearSlot(object));
    }

    public void clearSlot(Object slot) {
        rulers.computeIfPresent(slot, (object, outlineElement) -> outlineElement.discard());
        rulers.remove(slot);
    }

}
