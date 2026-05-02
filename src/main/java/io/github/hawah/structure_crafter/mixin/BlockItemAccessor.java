package io.github.hawah.structure_crafter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(BlockItem.class)
public interface BlockItemAccessor {
    @Invoker("getPlacementState")
    BlockState BlockItemAccessor$getPlacementState(BlockPlaceContext context);

    @Invoker("placeBlock")
    boolean BlockItemAccessor$placeBlock(BlockPlaceContext context, BlockState state);

    @Invoker("updateBlockStateFromTag")
    BlockState BlockItemAccessor$updateBlockStateFromTag(BlockPos pos, Level level, ItemStack stack, BlockState state);

    @Invoker("updateCustomBlockEntityTag")
    boolean BlockItemAccessor$updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state);

    @Invoker("getPlaceSound")
    SoundEvent BlockItemAccessor$getPlaceSound(BlockState p_state, Level world, BlockPos pos, Player entity);
}
