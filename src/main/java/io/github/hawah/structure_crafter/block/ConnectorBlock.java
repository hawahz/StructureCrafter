package io.github.hawah.structure_crafter.block;

import com.mojang.serialization.MapCodec;
import io.github.hawah.structure_crafter.block.blockentity.ConnectorBlockEntity;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.util.VoxelShapeMaker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConnectorBlock extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock, IPlacePriority {

    public static final MapCodec<ConnectorBlock> CODEC = simpleCodec(ConnectorBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape NORTH = Block.box(2, 2, 11, 14, 14, 16);
    public static final VoxelShape SOUTH = VoxelShapeMaker.getByHorizontalDirection(Direction.SOUTH, NORTH);
    public static final VoxelShape EAST = VoxelShapeMaker.getByHorizontalDirection(Direction.EAST, NORTH);
    public static final VoxelShape WEST = VoxelShapeMaker.getByHorizontalDirection(Direction.WEST, NORTH);

    public ConnectorBlock() {
        super(Properties.of());
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(WATERLOGGED, false)
        );
    }

    public ConnectorBlock(Properties properties) {
        this();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> EAST;
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (state.getValue(WATERLOGGED)) {
                level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }

            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        return !level.getBlockState(pos.relative(direction.getOpposite())).isEmpty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConnectorBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @javax.annotation.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!context.replacingClickedOnBlock()) {
            BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
            if (blockstate.is(this) && blockstate.getValue(FACING) == context.getClickedFace()) {
                return null;
            }
        }
        if (!context.getClickedFace().getAxis().isHorizontal()) {
            return null;
        }

        BlockState blockstate1 = this.defaultBlockState();
        LevelReader levelreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());

        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                blockstate1 = blockstate1.setValue(FACING, direction.getOpposite());
                if (blockstate1.canSurvive(levelreader, blockpos)) {
                    return blockstate1.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
                }
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean isPriority(PlayerInteractEvent.RightClickBlock event) {
        BlockHitResult hitVec = event.getHitVec();
        BlockPos blockPos = hitVec.getBlockPos();
        return event.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, blockPos, event.getFace()) != null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof ConnectorBlockEntity blockEntity) {
            //TODO Remove Take Over Container Blocks
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state,
                                               Level level,
                                               BlockPos pos,
                                               Player player,
                                               BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ConnectorBlockEntity blockEntity && blockEntity.hasTelephone && player.getMainHandItem().isEmpty()) {
            blockEntity.hasTelephone = false;
            ItemStack telephoneHandset = ItemRegistries.TELEPHONE_HANDSET.toStack();
            telephoneHandset.set(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, pos);
            player.addItem(telephoneHandset);
            if (level.isClientSide()){
                Outliner.getInstance().chaseThickBox(new Object(), pos, pos)
                        .setRGBA(0, 1, 0, 1)
                        .lazyDiscard(50)
                        .finish();
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack,
                                              BlockState state,
                                              Level level,
                                              BlockPos pos,
                                              Player player,
                                              InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (pos.equals(stack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, null)) && level.getBlockEntity(pos) instanceof ConnectorBlockEntity blockEntity && !blockEntity.hasTelephone) {
            blockEntity.hasTelephone = true;
            stack.shrink(1);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
