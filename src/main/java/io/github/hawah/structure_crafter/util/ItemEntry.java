package io.github.hawah.structure_crafter.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class ItemEntry {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ItemEntry::id,
            ByteBufCodecs.INT, ItemEntry::count,
            ItemEntry::new
    );

    public static final Codec<ItemEntry> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("id").forGetter(ItemEntry::id),
                    Codec.INT.fieldOf("count").forGetter(ItemEntry::count)
            ).apply(instance, ItemEntry::new));
    private final int id;
    private final int count;

    private ItemStack bufferedStack = null;

    public ItemEntry(int id, int count) {
        this.id = id;
        this.count = count;
    }

    public static ItemEntry fromStack(ItemStack stack) {
        return new ItemEntry(Item.getId(stack.getItem()), stack.getCount());
    }

    public static ItemEntry fromEntry(Map.Entry<Item, Integer> entry) {
        return new ItemEntry(Item.getId(entry.getKey()), entry.getValue());
    }


    public Component getHoverName() {
        return Item.byId(id).getDefaultInstance().getHoverName();
    }

    public int getCount() {
        return count;
    }

    public Item getItem() {
        return Item.byId(id);
    }

    public ItemStack getDefaultStack() {
        if (bufferedStack == null) {
            bufferedStack = Item.byId(id).getDefaultInstance().copyWithCount(count);
        }
        return bufferedStack;
    }

    public int id() {
        return id;
    }

    public int count() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemEntry) obj;
        return this.id == that.id &&
                this.count == that.count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, count);
    }

    public boolean isSame(ItemEntry other) {
        return id == other.id;
    }

    public static List<ItemEntry> fromStacks(List<ItemStack> stacks) {
        return stacks.stream().map(ItemEntry::fromStack).toList();
    }

    public static List<ItemEntry> flat(List<ItemEntry> before) {
        Map<Integer, ItemEntry> merged = new HashMap<>();
        for (ItemEntry entry : before) {
            merged.merge(entry.id(), entry, (existing, newEntry) ->
                    new ItemEntry(existing.id(), existing.count() + newEntry.count())
            );
        }
        return new ArrayList<>(merged.values());
    }


    @Override
    public String toString() {
        return "ItemEntry[" +
                "id=" + id + ", " +
                "count=" + count + ']';
    }
}
