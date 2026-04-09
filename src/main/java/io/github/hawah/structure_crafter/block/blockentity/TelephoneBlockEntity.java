package io.github.hawah.structure_crafter.block.blockentity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.structure_crafter.block.TelephoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

//TODO 强绑定玩家，以及解绑后的判断逻辑，密钥
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber
public class TelephoneBlockEntity extends BlockEntity {

    private final Direction facing;

    // Server only
    public static final HashSet<BlockPos> containersTakeOver = new HashSet<>();

    public void setHasTelephone(boolean hasTelephone) {
        this.hasTelephone = hasTelephone;
        setChanged();
    }

    public boolean hasTelephone() {
        return hasTelephone;
    }

    private boolean hasTelephone = true;
    private boolean dirty = true;

    public void setDirty() {
        dirty = true;
    }

    public final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<ItemResource>() {

        private final List<ResourceHandler<ItemResource>> handlers = new ArrayList<>();
        private static final int MAX_CONTAINER_CHECK = 25;
        private boolean updating = false;

        @Override
        public int size() {
            if (interacting) return 0;
            interacting = true;
            try {
                update();
                return handlers.stream().mapToInt(ResourceHandler::size).sum();
            } finally {
                interacting = false;
            }
        }

        @Override
        public ItemResource getResource(int index) {
            if (interacting) return ItemResource.EMPTY;
            interacting = true;
            try {
                update();
                for (var handler : handlers) {
                    if (index < handler.size()) {
                        return handler.getResource(index);
                    }
                    index -= handler.size();
                }
                return ItemResource.EMPTY;
            } finally {
                interacting = false;
            }
        }

        @Override
        public long getAmountAsLong(int index) {
            if (interacting) return 0;
            interacting = true;
            try {
                update();
                for (var handler : handlers) {
                    if (index < handler.size()) {
                        return handler.getAmountAsLong(index);
                    }
                    index -= handler.size();
                }
                return 0;
            } finally {
                interacting = false;
            }
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            if (interacting) return 0;
            interacting = true;
            try {
                update();
                for (var handler : handlers) {
                    if (slot < handler.size()) {
                        return handler.getCapacityAsLong(slot, resource);
                    }
                    slot -= handler.size();
                }
                return 0;
            } finally {
                interacting = false;
            }
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return false;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (interacting) return amount; // 如果陷入循环，拒绝插入原样返回
            interacting = true;
            try {
                update();
                for (var handler : handlers) {
                    if (index < handler.size()) {
                        return handler.insert(index, resource, amount, transaction);
                    }
                    index -= handler.size();
                }
                return amount;
            } finally {
                interacting = false;
            }
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (interacting) return 0;
            interacting = true;
            try {
                update();
                for (var handler : handlers) {
                    if (index < handler.size()) {
                        return handler.extract(index, resource, amount, transaction);
                    }
                    index -= handler.size();
                }
                return 0;
            } finally {
                interacting = false;
            }
        }



        // 添加一个防重入锁，防止外部模组的恶性代理导致循环调用
        private boolean interacting = false;

        private void update() {
            if (!dirty || level == null || updating) return;
            if (level.isClientSide()) return;

            dirty = false;
            updating = true;
            handlers.clear();

            try {
                BlockPos startPos = worldPosition.relative(facing);
                Set<BlockPos> visited = new HashSet<>();
                visited.add(startPos);

                Queue<BlockPos> queue = new LinkedList<>();
                queue.add(startPos);

                // 检查起始方块
                checkAndAddHandler(startPos, queue);

                while (!queue.isEmpty() && handlers.size() < MAX_CONTAINER_CHECK) {
                    BlockPos currentPos = queue.poll();

                    for (Direction direction : Direction.values()) {
                        if (handlers.size() >= MAX_CONTAINER_CHECK) break;

                        BlockPos neighborPos = currentPos.relative(direction);
                        if (!visited.add(neighborPos)) continue; // .add() 返回 false 说明已访问过

                        checkAndAddHandler(neighborPos, queue);
                    }
                }
            } finally {
                updating = false;
            }
        }

        // 将检查逻辑提取为独立方法
        private void checkAndAddHandler(BlockPos pos, Queue<BlockPos> queue) {
            var handler = level.getCapability(Capabilities.Item.BLOCK, pos, facing);
            if (handler != null) {
                BlockEntity be = level.getBlockEntity(pos);
                // 核心修复：如果是另一个连接器，则只导通路径，不将其作为目标容器添加
                if (be instanceof TelephoneBlockEntity) {
                    //queue.add(pos);
                } else {
                    // 是普通的容器（如箱子、熔炉等）
                    handlers.add(handler);
                    queue.add(pos); // 如果你需要穿透箱子继续寻找，保留这行；如果只靠连接器相连，移除这行
                }
            }
        }
    };
    public TelephoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.TELEPHONE_BLOCK_ENTITY.get(), pos, blockState);
        this.facing = blockState.getValue(TelephoneBlock.FACING).getOpposite();
    }


    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        hasTelephone = tag.getBooleanOr("hasTelephone", true);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        tag.putBoolean("hasTelephone", hasTelephone);
    }

    @SubscribeEvent
    public static void onOpenBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockHitResult hitVec = event.getHitVec();
        BlockPos blockPos = hitVec.getBlockPos();
        if (!TelephoneBlockEntity.containersTakeOver.contains(blockPos)) {
            return;
        }
        event.setUseBlock(TriState.FALSE);
        Player player = event.getEntity();
        //TODO Translatable
        player.displayClientMessage(
                Component.literal("This is taken over."),
                true
        );
    }
}
