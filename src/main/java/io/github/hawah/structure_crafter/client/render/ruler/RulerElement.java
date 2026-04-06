package io.github.hawah.structure_crafter.client.render.ruler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.client.render.ColoredElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;

public abstract class RulerElement<T extends RulerElement<T>> extends ColoredElement<T> {

    private Vec3 begin, end;

    public T createManhattan(Vec3 start, Vec3 end) {
        return (T) this;
    }
    public T createStrait(Vec3 start, Vec3 end) {
        return (T) this;
    }
    @Override
    public void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick) {

    }
}
