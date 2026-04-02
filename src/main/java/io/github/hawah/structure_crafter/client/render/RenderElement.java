package io.github.hawah.structure_crafter.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.client.LazySet;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class RenderElement {

    protected List<LazySet> lazySets = new ArrayList<>();

    protected boolean dirty = true;
    public boolean discarded = false;
    protected Optional<Float> deltaTicks = Optional.empty();
    public void lazySet(int delayTicks, Runnable setter) {
        lazySets.add(LazySet.create(delayTicks, setter));
    }

    public void tick() {
        lazySets.forEach(LazySet::tick);
        lazySets.removeIf(LazySet::isDiscarded);
    }

    public abstract void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick);

    public <T extends RenderElement> T discard() {
        discarded = true;
        return (T) this;
    }

    public <T extends RenderElement> T setPriority(int priority) {
        this.priority = priority;
        return (T) this;
    }

    public <T extends RenderElement> T smooth(float smooth) {
        this.deltaTicks = Optional.of(smooth);
        return (T) this;
    }

    protected int priority = 0;

    public abstract boolean finish();
}
