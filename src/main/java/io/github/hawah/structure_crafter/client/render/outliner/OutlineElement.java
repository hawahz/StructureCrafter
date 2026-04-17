package io.github.hawah.structure_crafter.client.render.outliner;

import io.github.hawah.structure_crafter.client.render.DoublePointElement;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@SuppressWarnings("unchecked")
public abstract class OutlineElement<Self extends OutlineElement<Self>> extends DoublePointElement<Self> {
    protected AABB boundingBox = AABB.ofSize(Vec3.ZERO, 0, 0, 0);
    protected Set<Direction> renderedFaces = new HashSet<>();

    public Self face(Direction direction) {
        renderedFaces.clear();
        if (direction != null) {
            renderedFaces.add(direction);
        }
        return (Self) this;
    }
    public Self clearFaces() {
        renderedFaces.clear();
        return (Self) this;
    }
    public Self faces(Direction... directions) {
        if (directions != null) {
            renderedFaces.addAll(Arrays.stream(directions).filter(Objects::nonNull).toList());
        }
        return (Self) this;
    }
}
