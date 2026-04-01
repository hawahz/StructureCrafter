package io.github.hawah.structure_crafter.client.render.outliner;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.render.ColoredElement;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class OutlineElement extends ColoredElement {
    protected Vec3 oPos0 = Vec3.ZERO;
    protected Vec3 oPos1 = Vec3.ZERO;
    protected Vec3 visualPos0 = Vec3.ZERO;
    protected Vec3 actualPos0 =  Vec3.ZERO;
    protected Vec3 visualPos1 = Vec3.ZERO;
    protected Vec3 actualPos1 = Vec3.ZERO;
    protected AABB boundingBox = AABB.ofSize(Vec3.ZERO, 0, 0, 0);
    protected Set<Direction> renderedFaces = new HashSet<>();

    public void setPositions(Vec3 p0, Vec3 p1) {
        actualPos0 = p0;
        actualPos1 = p1;
    }

    public void tick() {
        super.tick();
        oPos0 = visualPos0;
        oPos1 = visualPos1;
        visualPos0 = visualPos0.lerp(actualPos0, StructureCrafterClient.ANI_DELTAF);
        visualPos1 = visualPos1.lerp(actualPos1, StructureCrafterClient.ANI_DELTAF);
    }

    public <T extends OutlineElement> T face(Direction direction) {
        renderedFaces.clear();
        if (direction != null) {
            renderedFaces.add(direction);
        }
        return (T) this;
    }
    public <T extends OutlineElement> T clearFaces() {
        renderedFaces.clear();
        return (T) this;
    }
    public <T extends OutlineElement> T faces(Direction... directions) {
        if (directions != null) {
            renderedFaces.addAll(Arrays.stream(directions).filter(Objects::nonNull).toList());
        }
        return (T) this;
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
