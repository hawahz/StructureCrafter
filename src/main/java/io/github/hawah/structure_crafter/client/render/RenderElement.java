package io.github.hawah.structure_crafter.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.client.utils.LazySet;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class RenderElement<Self extends RenderElement<Self>> {

    protected List<LazySet> lazySets = new ArrayList<>();

    protected boolean dirty = true;
    public boolean discarded = false;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected Optional<Float> deltaTicks = Optional.empty();
    public void lazySet(int delayTicks, Runnable setter) {
        lazySets.add(LazySet.create(delayTicks, setter));
    }

    public void tick() {
        lazySets.forEach(LazySet::tick);
        lazySets.removeIf(LazySet::isDiscarded);
    }

    public abstract void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick);

    public Self discard() {
        discarded = true;
        return (Self) this;
    }

    public Self lazyDiscard(int delayTicks) {
        lazySet(delayTicks, this::discard);
        return (Self) this;
    }

    public Self setPriority(int priority) {
        this.priority = priority;
        return (Self) this;
    }

    public Self smooth(float smooth) {
        this.deltaTicks = Optional.of(smooth);
        return (Self) this;
    }

    protected int priority = 0;

    public abstract boolean finish();
}
