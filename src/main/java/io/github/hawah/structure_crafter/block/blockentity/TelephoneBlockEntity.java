package io.github.hawah.structure_crafter.block.blockentity;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.block.TelephoneBlock;
import io.github.hawah.structure_crafter.client.render.TelephoneWireRenderer;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.TelephoneBlockEntityBeaconChangedPacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

//TODO 强绑定玩家，以及解绑后的判断逻辑，密钥
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber
public class TelephoneBlockEntity extends BlockEntity {

    public final Direction facing;


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

    @OnlyIn(Dist.CLIENT)
    public boolean playerLookingAt = false;

    public void setDirty() {
        dirty = true;
    }

    public final IItemHandler itemHandler = new IItemHandler() {

        private final List<IItemHandler> handlers = new ArrayList<>();
        private static final int MAX_CONTAINER_CHECK = 25;
        private boolean updating = false;

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
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, facing);
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

        @Override
        public int getSlots() {
            if (interacting) return 0;
            interacting = true;
            try {
                update();
                return handlers.stream().mapToInt(IItemHandler::getSlots).sum();
            } finally {
                interacting = false;
            }
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (interacting) return ItemStack.EMPTY;
            interacting = true;
            try {
                update();
                for (IItemHandler handler : handlers) {
                    if (slot < handler.getSlots()) {
                        return handler.getStackInSlot(slot);
                    }
                    slot -= handler.getSlots();
                }
                return ItemStack.EMPTY;
            } finally {
                interacting = false;
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (interacting) return stack; // 如果陷入循环，拒绝插入原样返回
            interacting = true;
            try {
                update();
                for (IItemHandler handler : handlers) {
                    if (slot < handler.getSlots()) {
                        return handler.insertItem(slot, stack, simulate);
                    }
                    slot -= handler.getSlots();
                }
                return stack;
            } finally {
                interacting = false;
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (interacting) return ItemStack.EMPTY;
            interacting = true;
            try {
                update();
                for (IItemHandler handler : handlers) {
                    if (slot < handler.getSlots()) {
                        return handler.extractItem(slot, amount, simulate);
                    }
                    slot -= handler.getSlots();
                }
                return ItemStack.EMPTY;
            } finally {
                interacting = false;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            if (interacting) return 0;
            interacting = true;
            try {
                update();
                for (IItemHandler handler : handlers) {
                    if (slot < handler.getSlots()) {
                        return handler.getSlotLimit(slot);
                    }
                    slot -= handler.getSlots();
                }
                return 0;
            } finally {
                interacting = false;
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // 如果你的逻辑允许，建议这里也做向下分发而不是固定返回 false
            return false;
        }
    };
    public TelephoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.TELEPHONE_BLOCK_ENTITY.get(), pos, blockState);
        this.facing = blockState.getValue(TelephoneBlock.FACING).getOpposite();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hasTelephone = tag.getBoolean("hasTelephone");
        hasBeacon = tag.getBoolean("hasBeacon");
        if (hasTelephone() && level != null && level.isClientSide()) {
            StructureCrafterClient.TELEPHONE_WIRE_RENDERER.pop(worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("hasTelephone", hasTelephone);
        tag.putBoolean("hasBeacon", hasBeacon);
    }

    private List<IItemHandler> getAttachedContainer(Level level, BlockPos pos) {
        IItemHandler cap;
        return (cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, facing)) == null? List.of(): List.of(cap);
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

    public boolean hasBeacon() {
        return hasBeacon;
    }

    public void setHasBeacon(boolean hasBeacon) {
        setChanged();
        this.hasBeacon = hasBeacon;
    }

    public boolean hasBeacon = false;
    public static void tick(Level level, BlockPos pos, BlockState state, TelephoneBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ChunkPos chunkPos = new ChunkPos(blockEntity.getBlockPos());
        List<LevelChunk> nearChunks = new ArrayList<>();
        for (int x = chunkPos.x - 1; x <= chunkPos.x + 1; x++) {
            for (int z = chunkPos.z - 1; z <= chunkPos.z + 1; z++) {
                nearChunks.add(level.getChunk(x, z));
            }
        }

        for (LevelChunk chunk : nearChunks){
            Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();
            for (BlockEntity be : blockEntities.values()) {
                if (be instanceof BeaconBlockEntity) {
                    if (blockEntity.hasBeacon()) {
                        return;
                    }
                    blockEntity.setHasBeacon(true);
                    Networking.sendToAll(new TelephoneBlockEntityBeaconChangedPacket(blockEntity.getBlockPos(), true));
                    serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true);
                    return;
                }
            }
        }
        if (!blockEntity.hasBeacon()) {
            return;
        }
        Networking.sendToAll(new TelephoneBlockEntityBeaconChangedPacket(blockEntity.getBlockPos(), false));
        blockEntity.setHasBeacon(false);
        for (int x = chunkPos.x - 1; x <= chunkPos.x + 1; x++) {
            for (int z = chunkPos.z - 1; z <= chunkPos.z + 1; z++) {
                serverLevel.setChunkForced(x, z, false);
            }
        }

    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (!hasBeacon)
            return;
        ChunkPos chunkPos = new ChunkPos(getBlockPos());
        for (int x = chunkPos.x - 1; x <= chunkPos.x + 1; x++) {
            for (int z = chunkPos.z - 1; z <= chunkPos.z + 1; z++) {
                serverLevel.setChunkForced(x, z, false);
            }
        }

    }
}
