package io.github.hawah.structure_crafter.client.wand_modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class FixedDistanceModifier implements Modifiers.Pos{

    public BlockPos anchor;
    public int distance = 0;

    public FixedDistanceModifier(int distance) {
        this.distance = distance;
    }

    @Override
    public BlockPos modify(BlockPos pos) {
        if (anchor == null) {
            return pos;
        }

        Vec3 direction = Vec3.atCenterOf(pos).subtract(Vec3.atCenterOf(anchor));
        double currentDist = direction.length();

        if (currentDist < 0.001) {
            return anchor;
        }

        Vec3 normalized = direction.normalize();
        int multiplier = Math.max(1, (int) Math.round(currentDist / distance));
        Vec3 result = Vec3.atCenterOf(anchor).add(normalized.scale(distance * multiplier));
        return BlockPos.containing(result);
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
}
