package io.github.hawah.structure_crafter.block.blockentity;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.block.ConnectorBlock;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber
public class ConnectorBlockEntity extends BlockEntity {

    private final HashMap<ItemEntry, List<ItemEntry.Slot>> items = new HashMap<>();
    private final List<ItemEntry> keys = new ArrayList<>();
    private final Direction facing;
    // Server only
    public static final HashSet<BlockPos> containersTakeOver = new HashSet<>();
    public boolean hasTelephone = true;
    public final IItemHandler itemHandler = new IItemHandler() {
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

    public ConnectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.CONNECTOR.get(), pos, blockState);
        this.facing = blockState.getValue(ConnectorBlock.FACING).getOpposite();
    }
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
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
        hasTelephone = tag.getBoolean("hasTelephone");
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
        if (!ConnectorBlockEntity.containersTakeOver.contains(blockPos)) {
            return;
        }
        event.setUseBlock(TriState.FALSE);
        Player player = event.getEntity();
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
                insertItemWithoutRepeatCheck(itemStack, i, worldPosition.relative(facing), Math.max(iItemHandler.getSlotLimit(i), itemStack.getMaxStackSize()));
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }
}
