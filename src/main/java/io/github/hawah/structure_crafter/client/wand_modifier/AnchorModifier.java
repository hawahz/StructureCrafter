package io.github.hawah.structure_crafter.client.wand_modifier;

import net.minecraft.core.BlockPos;

public abstract class AnchorModifier implements Modifiers.Pos{
    public BlockPos anchor;

    @Override
    public BlockPos modify(BlockPos pos) {
        return pos;
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
