package io.github.hawah.structure_crafter.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class TelephoneBlockEntityState extends BlockEntityRenderState {
    public boolean hasTelephone() {
        return hasTelephone;
    }

    public boolean hasTelephone;

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public BlockPos blockPos;
    public final Direction facing;
}
