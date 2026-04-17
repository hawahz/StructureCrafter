package io.github.hawah.structure_crafter.client.render;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unchecked")
public abstract class DoublePointElement<Self extends DoublePointElement<Self>> extends ColoredElement<Self>  {
    protected Vec3 oPos0 = Vec3.ZERO;
    protected Vec3 oPos1 = Vec3.ZERO;
    protected Vec3 visualPos0 = Vec3.ZERO;
    protected Vec3 actualPos0 =  Vec3.ZERO;
    protected Vec3 visualPos1 = Vec3.ZERO;
    protected Vec3 actualPos1 = Vec3.ZERO;

    public Self setPositions(Vec3 p0, Vec3 p1) {
        actualPos0 = p0;
        actualPos1 = p1;
        return (Self) this;
    }

    @Override
    public void tick() {
        super.tick();
        oPos0 = visualPos0;
        oPos1 = visualPos1;
        visualPos0 = visualPos0.lerp(actualPos0, deltaTicks.orElse(StructureCrafterClient.ANI_DELTAF));
        visualPos1 = visualPos1.lerp(actualPos1, deltaTicks.orElse(StructureCrafterClient.ANI_DELTAF));
    }

    @Override
    public boolean finish() {
        if (!super.finish())
            return false;
        visualPos0 = actualPos0;
        visualPos1 = actualPos1;
        oPos0 = visualPos0;
        oPos1 = visualPos1;
        return true;
    }
}
