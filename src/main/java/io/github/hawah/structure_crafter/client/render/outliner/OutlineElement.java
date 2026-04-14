package io.github.hawah.structure_crafter.client.render.outliner;

import io.github.hawah.structure_crafter.client.render.DoublePointElement;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class OutlineElement<T extends OutlineElement<T>> extends DoublePointElement<T> {
    protected AABB boundingBox = AABB.ofSize(Vec3.ZERO, 0, 0, 0);
    protected Set<Direction> renderedFaces = new HashSet<>();

    public T face(Direction direction) {
        renderedFaces.clear();
        if (direction != null) {
            renderedFaces.add(direction);
        }
        return (T) this;
    }
    public T clearFaces() {
        renderedFaces.clear();
        return (T) this;
    }
    public T faces(Direction... directions) {
        if (directions != null) {
            renderedFaces.addAll(Arrays.stream(directions).filter(Objects::nonNull).toList());
        }
        return (T) this;
    }
}
