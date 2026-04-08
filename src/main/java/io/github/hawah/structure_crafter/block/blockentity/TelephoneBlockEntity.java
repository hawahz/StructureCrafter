package io.github.hawah.structure_crafter.block.blockentity;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.block.TelephoneBlock;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.TriState;
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

    @Deprecated
    private final HashMap<ItemEntry, List<ItemEntry.Slot>> items = new HashMap<>();
    @Deprecated
    private final List<ItemEntry> keys = new ArrayList<>();
    private final Direction facing;

    public HashMap<ItemEntry, List<ItemEntry.Slot>> getItems() {
        return items;
    }

    public List<ItemEntry> getKeys() {
        return keys;
    }

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

    @Deprecated
    public final IItemHandler itemHandlerOld = new IItemHandler() {
        @Override
        public int getSlots() {
            return items.size();
        }

        @Override
        public ItemStack getStackInSlot(int idx) {
            try {
                ItemEntry itemEntry = keys.get(idx);
                List<ItemEntry.Slot> slots = items.get(itemEntry);
                ItemStack stack = itemEntry.toStack();
                int counts = 0;
                for (ItemEntry.Slot slot : slots) {
                    counts += slot.counts();
                }
                stack.setCount(counts);
                return stack;
            } catch (Exception e) {
                StructureCrafter.LOGGER.error("Slot {} Not Valid.", idx, e);
                return ItemStack.EMPTY.copy();
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (simulate || stack.isEmpty())
                return stack;
            ItemEntry itemEntry = ItemEntry.fromStack(stack);
            itemEntry.setCount(1);
            List<ItemEntry.Slot> s;
            setChanged();
            // 寻找匹配物品
            if (items.containsKey(itemEntry)) {
                s = items.get(itemEntry);
                for (ItemEntry.Slot toInsert : s) {
                    int i = toInsert.validCounts();
                    toInsert.setCounts(Math.min(i, toInsert.counts()), level);
                    stack.shrink(i);
                    if (stack.isEmpty())
                        return stack;
                }
            }
            if (!stack.isEmpty()) {
                for (int i = 0; i < items.get(ItemEntry.EMPTY).size(); i++) {
                    ItemEntry.Slot toInsert = items.get(ItemEntry.EMPTY).get(i);
                    int c = toInsert.validCounts();
                    toInsert.setCounts(Math.min(c, toInsert.counts()), level);
                    items.get(ItemEntry.EMPTY).remove(c);
                    items.get(itemEntry).add(i, toInsert);
                    stack.shrink(c);
                    if (stack.isEmpty())
                        return stack;
                }
            }

            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= keys.size() || keys.get(slot).isEmpty())
                return ItemStack.EMPTY.copy();

            ItemEntry itemEntry = keys.get(slot);
            int retAmount = 0;
            amount = Math.min(itemEntry.toStack().getMaxStackSize(), amount);
            for (ItemEntry.Slot toExtract : items.get(itemEntry)) {
                if (toExtract.counts() <  amount) {
                    retAmount += toExtract.counts();
                    amount -= toExtract.counts();
                    toExtract.setCounts(0, level);
                    items.get(itemEntry).remove(toExtract);
                    items.get(ItemEntry.EMPTY).add(toExtract);
                } else {
                    retAmount += amount;
                    toExtract.setCounts(toExtract.counts() - amount, level);
                    break;
                }
            }
            setChanged();

            return itemEntry.toStack(retAmount);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.get(slot < keys.size()? keys.get(slot) : ItemEntry.EMPTY).stream().mapToInt(ItemEntry.Slot::validCounts).sum();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };

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
                    queue.add(pos);
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
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hasTelephone = tag.getBoolean("hasTelephone");
        if (!tag.contains("items")) {
            return;
        }
        this.items.clear();
        ListTag listTag = tag.getList("items", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundtag = listTag.getCompound(i);
            if (!compoundtag.contains("value") || !compoundtag.contains("key")) {
                continue;
            }
            CompoundTag key = compoundtag.getCompound("key");
            ItemEntry itemEntry = ItemEntry.parse(registries, key).orElse(ItemEntry.EMPTY);
            List<ItemEntry.Slot> slots = new ArrayList<>();

            ListTag value = compoundtag.getList("value", CompoundTag.TAG_COMPOUND);
            for (int j = 0; j < value.size(); j++) {
                CompoundTag slotTag = value.getCompound(j);
                ItemEntry.Slot slot = ItemEntry.Slot.parse(slotTag);
                slots.add(slot);
            }
            items.put(itemEntry, slots);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveAll(tag, items, registries);
        tag.putBoolean("hasTelephone", hasTelephone);
    }

    private List<IItemHandler> getAttachedContainer(Level level, BlockPos pos) {
        IItemHandler cap;
        return (cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, facing)) == null? List.of(): List.of(cap);
    }

    protected static void saveAll(CompoundTag tag, HashMap<ItemEntry, List<ItemEntry.Slot>> items, HolderLookup.Provider levelRegistry) {
        ListTag listtag = new ListTag();

        for (ItemEntry item: items.keySet()) {
            if (item.isEmpty()) {
                continue;
            }
            CompoundTag compoundtag = new CompoundTag();
            ListTag value = new ListTag();
            for (ItemEntry.Slot slot : items.get(item)) {
                CompoundTag compoundTag = new CompoundTag();
                value.add(slot.save(compoundTag));
            }
            if (value.isEmpty()) {
                continue;
            }
            Tag key = item.save(levelRegistry, new CompoundTag());
            compoundtag.put("value", value);
            compoundtag.put("key", key);

            listtag.add(compoundtag);
        }

        if (!listtag.isEmpty()) {
            tag.put("items", listtag);
        }

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
    // Only Callable When initialized
    private void insertItemWithoutRepeatCheck(ItemStack itemStack, int slot, BlockPos pos, int limits) {
        ItemEntry itemEntry = ItemEntry.fromStack(itemStack);
        itemEntry.setCount(1);
        int counts = itemStack.getCount();
        assert level != null;
        items.merge(itemEntry, List.of(new ItemEntry.Slot(pos, slot, counts, limits)), (existing, newEntry) -> {
            ArrayList<ItemEntry.Slot> ret = new ArrayList<>();
            ret.addAll(newEntry);
            ret.addAll(existing);
            return ret;
        });
        if (this.keys.contains(itemEntry)) {
            return;
        }
        keys.add(itemEntry);
    }
    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (level.getCapability(Capabilities.ItemHandler.BLOCK, this.worldPosition.relative(facing), facing) == null) {
            return;
        }
        containersTakeOver.add(worldPosition.relative(facing));
        List<IItemHandler> attachedContainer = getAttachedContainer(level, worldPosition.relative(facing));
        if (attachedContainer.isEmpty()) {
            return;
        }
        for (IItemHandler iItemHandler : attachedContainer) {
            for (int i = 0; i < iItemHandler.getSlots(); i++) {
                ItemStack itemStack = iItemHandler.getStackInSlot(i);
                insertItemWithoutRepeatCheck(
                        itemStack,
                        i,
                        worldPosition.relative(facing),
                        Math.max(iItemHandler.getSlotLimit(i), itemStack.getMaxStackSize())
                );
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }


}
