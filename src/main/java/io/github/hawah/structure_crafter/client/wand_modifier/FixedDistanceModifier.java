package io.github.hawah.structure_crafter.client.wand_modifier;

import io.github.hawah.structure_crafter.lib.util.MutablePair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

public class FixedDistanceModifier implements Modifiers.Pos{

    public BlockPos anchor;
    public int distance;
    protected final Vector2i size = new Vector2i();
    protected final Vector2i offset = new Vector2i();

    public FixedDistanceModifier(int distance) {
        this.distance = distance;
    }

    @Override
    public BlockPos modify(BlockPos pos) {
        if (anchor == null) {
            return pos;
        }

        Vec3 direction = Vec3.atCenterOf(pos).subtract(Vec3.atCenterOf(anchor)).multiply(1, 0, 1);
        double currentDist = direction.length();

        if (currentDist < 0.001) {
            return anchor;
        }

        Vec3 normalized = direction.normalize();
        boolean axisZ = pos.getX() == anchor().getX();
        boolean axisX = pos.getZ() == anchor().getZ();
        Vec3 result;
        if ((axisZ || axisX)) {
            int axis = axisZ ? size().y() : size().x();
            int multiplier = Math.max(0, (int) Math.round((currentDist - axis) / distance));
            if (multiplier == 0) {
                result = Vec3.atCenterOf(anchor).add(0, pos.getY() - anchor.getY(), 0);
            } else {
                result = Vec3.atCenterOf(anchor).add(normalized.scale(distance * multiplier + axis));
            }
        } else {
            int multiplier = Math.max(0, (int) Math.round(currentDist / distance));
            result = Vec3.atCenterOf(anchor).add(normalized.scale(distance * multiplier));
        }
        return BlockPos.containing(result.multiply(1, 0, 1).add(0, pos.getY(), 0));
    }

    @Override
    public void onPlace(BlockPos placeAt, Direction direction) {
    }

    @Override
    public void setAnchor(BlockPos pos) {
        anchor = pos;
    }

    @Override
    public BlockPos anchor() {
        return anchor;
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public Vector2i size() {
        return size;
    }

    @Override
    public Vector2i offset() {
        return offset;
    }

    @Override
    public void setSize(Vector2i size) {
        this.size.set(size);
    }

    @Override
    public void modifySubmit(MutablePair<BlockPos, BlockPos> data) {
        BlockPos.MutableBlockPos end = data.right().mutable();
        BlockPos.MutableBlockPos start = data.left().mutable();
        boolean axisZ = end.getX() ==      start.getX();
        boolean axisX = end.getZ() ==      start.getZ();
        boolean alongXPos = end.getX() >   start.getX();
        boolean alongZPos = end.getZ() >   start.getZ();
        if (axisX && alongXPos) {
            end.setX(end.getX() - offset().x());
            start.setX(start.getX() + size().x() - offset().x() - 1);
        } else if (axisX) {
            end.setX(end.getX() + size().x() - offset().x() - 1);
            start.setX(start.getX() - offset().x());
        } else if (axisZ && alongZPos) {
            end.setZ(end.getZ() - offset().y());
            start.setZ(start.getZ() + size().y() - offset().y() - 1);
        } else if (axisZ) {
            end.setZ(end.getZ() + size().y() - offset().y() - 1);
            start.setZ(start.getZ() - offset().y());
        }
        data.setLeft(start.immutable());
        data.setRight(end.immutable());
    }

    @Override
    public void setOffset(Vector2i offset) {
        this.offset.set(offset);
    }
}
