package io.github.hawah.structure_crafter;

import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.common.SpecialPlantable;
import org.jetbrains.annotations.Nullable;

public class BlockHelper {


    public static BlockState setZeroAge(BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.AGE_1))
            return blockState.setValue(BlockStateProperties.AGE_1, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_2))
            return blockState.setValue(BlockStateProperties.AGE_2, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_3))
            return blockState.setValue(BlockStateProperties.AGE_3, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_5))
            return blockState.setValue(BlockStateProperties.AGE_5, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_7))
            return blockState.setValue(BlockStateProperties.AGE_7, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_15))
            return blockState.setValue(BlockStateProperties.AGE_15, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_25))
            return blockState.setValue(BlockStateProperties.AGE_25, 0);
        if (blockState.hasProperty(BlockStateProperties.LEVEL_HONEY))
            return blockState.setValue(BlockStateProperties.LEVEL_HONEY, 0);
        if (blockState.hasProperty(BlockStateProperties.HATCH))
            return blockState.setValue(BlockStateProperties.HATCH, 0);
        if (blockState.hasProperty(BlockStateProperties.STAGE))
            return blockState.setValue(BlockStateProperties.STAGE, 0);
        if (blockState.is(BlockTags.CAULDRONS))
            return Blocks.CAULDRON.defaultBlockState();
        if (blockState.hasProperty(BlockStateProperties.LEVEL_COMPOSTER))
            return blockState.setValue(BlockStateProperties.LEVEL_COMPOSTER, 0);
        if (blockState.hasProperty(BlockStateProperties.EXTENDED))
            return blockState.setValue(BlockStateProperties.EXTENDED, false);
        return blockState;
    }

//    public static CompoundTag prepareBlockEntityData(Level level, BlockState blockState, BlockEntity blockEntity) {
//        CompoundTag data = null;
//        if (blockEntity == null)
//            return null;
//        RegistryAccess access = level.registryAccess();
//        SafeNbtWriter writer = SafeNbtWriterRegistry.REGISTRY.get(blockEntity.getType());
//        if (AllBlockTags.SAFE_NBT.matches(blockState)) {
//            data = blockEntity.saveWithFullMetadata(access);
//        } else if (writer != null) {
//            data = new CompoundTag();
//            writer.writeSafe(blockEntity, data, access);
//        } else if (blockEntity instanceof PartialSafeNBT safeNbtBE) {
//            data = new CompoundTag();
//            safeNbtBE.writeSafe(data, access);
//        } else if (Mods.FRAMEDBLOCKS.contains(blockState.getBlock())) {
//            data = FramedBlocksInSchematics.prepareBlockEntityData(blockState, blockEntity);
//        }
//
//        return NBTProcessors.process(blockState, blockEntity, data, true);
//    }

    public static void placeSchematicBlock(Level world, BlockState state, BlockPos target, ItemStack stack,
                                           @Nullable CompoundTag data) {
        Block block = state.getBlock();
        BlockEntity existingBlockEntity = world.getBlockEntity(target);
        boolean alreadyPlaced = false;

//        StateFilter filter = SchematicStateFilterRegistry.REGISTRY.get(state);
//        if (filter != null) {
//            state = filter.filterStates(existingBlockEntity, state);
//        } else if (block instanceof SchematicStateFilter schematicStateFilter) {
//            state = schematicStateFilter.filterStates(existingBlockEntity, state);
//        }

        // Piston
        if (state.hasProperty(BlockStateProperties.EXTENDED))
            state = state.setValue(BlockStateProperties.EXTENDED, Boolean.FALSE);
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE);

        if (block == Blocks.COMPOSTER) {
            state = Blocks.COMPOSTER.defaultBlockState();
        } else if (block != Blocks.SEA_PICKLE && block instanceof SpecialPlantable specialPlantable) {
            alreadyPlaced = true;
            if (specialPlantable.canPlacePlantAtPosition(stack, world, target, null))
                specialPlantable.spawnPlantAtPosition(stack, world, target, null);
        } else if (state.is(BlockTags.CAULDRONS)) {
            state = Blocks.CAULDRON.defaultBlockState();
        }

        if (world.dimensionType()
                .ultraWarm() && state.getFluidState().is(FluidTags.WATER)) {
            int i = target.getX();
            int j = target.getY();
            int k = target.getZ();
            world.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(),
                        0.0D, 0.0D, 0.0D);
            }
            Block.dropResources(state, world, target);
            return;
        }

        //noinspection StatementWithEmptyBody
        if (alreadyPlaced) {
            // pass
//        } else if (state.getBlock() instanceof BaseRailBlock) {
//            placeRailWithoutUpdate(world, state, target);
//        } else if (AllBlocks.BELT.has(state)) {
//            world.setBlock(target, state, Block.UPDATE_CLIENTS);
        } else {
            world.setBlock(target, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }

        if (data != null) {
//            if (existingBlockEntity instanceof IMergeableBE mergeable) {
//                BlockEntity loaded = BlockEntity.loadStatic(target, state, data, world.registryAccess());
//                if (loaded != null) {
//                    if (existingBlockEntity.getType()
//                            .equals(loaded.getType())) {
//                        mergeable.accept(loaded);
//                        return;
//                    }
//                }
//            }
            BlockEntity blockEntity = world.getBlockEntity(target);
            if (blockEntity != null) {
                data.putInt("x", target.getX());
                data.putInt("y", target.getY());
                data.putInt("z", target.getZ());
//                if (blockEntity instanceof KineticBlockEntity kbe)
//                    kbe.warnOfMovement();
//                if (blockEntity instanceof IMultiBlockEntityContainer imbe)
//                    if (!imbe.isController())
//                        data.put("Controller", NbtUtils.writeBlockPos(imbe.getController()));
                blockEntity.loadWithComponents(data, world.registryAccess());
            }
        }

        try {
            state.getBlock()
                    .setPlacedBy(world, target, state, null, stack);
        } catch (Exception ignored) {
        }
    }
}
