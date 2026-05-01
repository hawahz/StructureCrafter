package io.github.hawah.structure_crafter.client.wand_modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class StraitModifier implements Modifiers.Pos{
    public BlockPos anchor;

    @Override
    public BlockPos modify(BlockPos pos) {
        if (anchor == null) {
            return pos;
        }

        int x = pos.getX();
        int z = pos.getZ();
        if (Math.abs(x - anchor.getX()) < Math.abs(z - anchor.getZ())) {
            return new BlockPos(anchor.getX(), pos.getY(), z);
        }
        return new BlockPos(x, pos.getY(), anchor.getZ());
    }

    @Override
    public void onPlace(BlockPos placeAt, Direction direction) {
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void setAnchor(BlockPos pos) {
        this.anchor = pos;
    }

    @Override
    public BlockPos anchor() {
        return anchor;
    }
}
