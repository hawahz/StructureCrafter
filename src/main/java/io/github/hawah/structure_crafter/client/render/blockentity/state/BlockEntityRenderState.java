package io.github.hawah.structure_crafter.client.render.blockentity.state;

import io.github.hawah.structure_crafter.client.render.blockentity.IRenderStateProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public interface BlockEntityRenderState<T extends BlockEntity> {
    Map<BlockEntity, BlockEntityRenderState<?>> states = new HashMap<>();
    static <S extends BlockEntity> Optional<BlockEntityRenderState<S>> get(IRenderStateProvider stateProvider) {
        if (stateProvider.isRemoved()) {
            return Optional.empty();
        }
        return Optional.of( (BlockEntityRenderState<S>) states.compute(stateProvider,
                (blockEntity, state) -> {
                    if (state == null) {
                        return stateProvider.getRenderState();
                    }
                    ( (BlockEntityRenderState<S>) state).update((S) blockEntity);
                    return state;
                }));
    }

    void update(T blockEntity);
}
