package io.github.hawah.structure_crafter.util;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.HashItemComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Deprecated
public class HashItemHandler implements IItemHandler {

    private final Map<ItemEntry, List<ItemEntry.LazySlot>> items = new HashMap<>();
    private final List<ItemEntry.LazySlot> changedSlots = new ArrayList<>();
    private final List<ItemEntry> keys = new ArrayList<>();
    private boolean initialized = false;
    protected final MutableDataComponentHolder parent;
    public boolean dirty = false;

    public HashItemHandler(MutableDataComponentHolder parent) {
        this.parent = parent;
    }

    private void init() {
        if (initialized)
            return;
        initialized = true;
        HashItemComponent hashItemComponent = parent.get(DataComponentTypeRegistries.HASH_ITEM);
        if (hashItemComponent == null)
            return;
        for (ItemEntry itemEntry : hashItemComponent.items().keySet()) {
            items.put(itemEntry, hashItemComponent.items().get(itemEntry).entries());
        }
        changedSlots.addAll(hashItemComponent.changedSlots().entries());
        keys.addAll(items.keySet());
        dirty = hashItemComponent.dirty();
    }

    @Override
    public int getSlots() {
        return items.size();
    }

    public void setChanged() {
        dirty = true;
        parent.update(
                DataComponentTypeRegistries.HASH_ITEM,
                HashItemComponent.EMPTY,
                (h) -> new HashItemComponent(
                        items.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> new HashItemComponent.LazySlotWarper(e.getValue()))),
                        HashItemComponent.LazySlotWarper.warp(changedSlots),
                        dirty
                )
        );
    }

    @Override
    public ItemStack getStackInSlot(int idx) {
        init();
        try {
            ItemEntry itemEntry = keys.get(idx);
            List<ItemEntry.LazySlot> slots = items.get(itemEntry);
            ItemStack stack = itemEntry.toStack();
            int counts = 0;
            for (ItemEntry.LazySlot slot : slots) {
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
        init();
        ItemEntry itemEntry = ItemEntry.fromStack(stack);
        itemEntry.setCount(1);
        List<ItemEntry.LazySlot> s;
        // 寻找匹配物品
        if (items.containsKey(itemEntry)) {
            s = items.get(itemEntry);
            for (ItemEntry.LazySlot toInsert : s) {
                int i = toInsert.validCounts();
                toInsert.setCounts(Math.min(i, toInsert.counts()));
                stack.shrink(i);
                if (!stack.isEmpty())
                    continue;
                setChanged();
                return stack;
            }
        }
        if (stack.isEmpty()) {
            return stack;
        }
        for (int i = 0; i < items.get(ItemEntry.EMPTY).size(); i++) {
            ItemEntry.LazySlot toInsert = items.get(ItemEntry.EMPTY).get(i);
            int c = toInsert.validCounts();
            toInsert.setCounts(Math.min(c, toInsert.counts()));
            items.get(ItemEntry.EMPTY).remove(c);
            items.get(itemEntry).add(i, toInsert);
            stack.shrink(c);
            if (!stack.isEmpty()) {
                continue;
            }
            setChanged();
            return stack;
        }

        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= keys.size() || keys.get(slot).isEmpty())
            return net.minecraft.world.item.ItemStack.EMPTY.copy();

        init();
        ItemEntry itemEntry = keys.get(slot);
        int retAmount = 0;
        amount = Math.min(itemEntry.toStack().getMaxStackSize(), amount);
        for (ItemEntry.LazySlot toExtract : items.get(itemEntry)) {
            if (toExtract.counts() <  amount) {
                retAmount += toExtract.counts();
                amount -= toExtract.counts();
                toExtract.setCounts(0);
                items.get(itemEntry).remove(toExtract);
                items.get(ItemEntry.EMPTY).add(toExtract);
            } else {
                retAmount += amount;
                toExtract.setCounts(toExtract.counts() - amount);
                break;
            }
        }
        setChanged();

        return itemEntry.toStack(retAmount);
    }

    @Override
    public int getSlotLimit(int slot) {
        return items.get(slot < keys.size()? keys.get(slot) : ItemEntry.EMPTY).stream().mapToInt(ItemEntry.LazySlot::validCounts).sum();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }
}
