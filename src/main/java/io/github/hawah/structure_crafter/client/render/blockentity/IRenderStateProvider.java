package io.github.hawah.structure_crafter.client.render.blockentity;

import io.github.hawah.structure_crafter.client.render.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class IRenderStateProvider extends BlockEntity {
    public IRenderStateProvider(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public abstract BlockEntityRenderState getRenderState();

    @Override
    public void setRemoved() {
        super.setRemoved();
        BlockEntityRenderState.states.remove(this);
    }
}
