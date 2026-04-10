package io.github.hawah.structure_crafter.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.structure_crafter.client.handler.StructureHandler;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.hawah.structure_crafter.StructureCrafter.LOGGER;

public final class ItemEntry implements DataComponentHolder {
    public static final ItemEntry EMPTY = new ItemEntry(0, 0);

    //    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEntry> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.INT, ItemEntry::id,
//            ByteBufCodecs.INT, ItemEntry::count,
//            ItemEntry::new
//    );
    public boolean isEmpty() {
        return this == EMPTY;
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEntry> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);

        public @NotNull ItemEntry decode(RegistryFriendlyByteBuf buffer) {
            int i = buffer.readVarInt();
            if (i <= 0) {
                return ItemEntry.EMPTY;
            } else {
                Holder<Item> holder = ITEM_STREAM_CODEC.decode(buffer);
                DataComponentPatch datacomponentpatch = DataComponentPatch.STREAM_CODEC.decode(buffer);
                return new ItemEntry(holder, i, datacomponentpatch);
            }
        }

        public void encode(RegistryFriendlyByteBuf buf, ItemEntry itemEntry) {
            buf.writeVarInt(itemEntry.getCount());
            ITEM_STREAM_CODEC.encode(buf, itemEntry.item);
            DataComponentPatch.STREAM_CODEC.encode(buf, itemEntry.components.asPatch());
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEntry> STREAM_CODEC = new StreamCodec<>() {
        public ItemEntry decode(RegistryFriendlyByteBuf buffer) {
            ItemEntry itemstack = ItemEntry.OPTIONAL_STREAM_CODEC.decode(buffer);
            if (itemstack.isEmpty()) {
                throw new DecoderException("Empty ItemStack not allowed");
            } else {
                return itemstack;
            }
        }

        public void encode(RegistryFriendlyByteBuf buffer, ItemEntry itemEntry) {
            if (itemEntry.isEmpty()) {
                throw new EncoderException("Empty ItemStack not allowed");
            } else {
                ItemEntry.OPTIONAL_STREAM_CODEC.encode(buffer, itemEntry);
            }
        }
    };

    public static final Codec<ItemEntry> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("id").forGetter(ItemEntry::id),
                    Codec.INT.fieldOf("count").forGetter(ItemEntry::count),
                    DataComponentPatch.CODEC
                            .optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(itemEntry -> itemEntry.components.asPatch())
            ).apply(instance, ItemEntry::new));
    private final Holder<Item> item;

    public void setCount(int count) {
        this.count = count;
    }

    private int count;
    private final PatchedDataComponentMap components;

    private ItemStack bufferedStack = null;

    public ItemEntry(int item, int count) {
        this(Item.byId(item).builtInRegistryHolder(), count);
    }

    public ItemEntry(int item, int count, DataComponentPatch components) {
        this(Item.byId(item).builtInRegistryHolder(), count, DataComponentPatch.EMPTY);
    }

    public ItemEntry(Holder<Item> item, int count) {
        this.item = item;
        this.count = count;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public ItemEntry(Holder<Item> item, int count, DataComponentPatch components) {
        this.item = item;
        this.count = count;
        this.components = PatchedDataComponentMap.fromPatch(item.value().components(), components);
    }

    public static ItemEntry fromStack(ItemStack stack) {
        return new ItemEntry(Item.getId(stack.getItem()), stack.getCount(), stack.getComponentsPatch());
    }

    public static ItemEntry fromEntry(Map.Entry<Item, Integer> entry) {
        return new ItemEntry(Item.getId(entry.getKey()), entry.getValue());
    }


    public Component getHoverName() {
        return item.value().getDefaultInstance().getHoverName();
    }

    public int getCount() {
        return count;
    }

    public Item getItem() {
        return item.value();
    }

    public ItemStack getDefaultStack() {
        if (bufferedStack == null) {
            bufferedStack = item.value().getDefaultInstance().copyWithCount(count);
        }
        return bufferedStack;
    }

    public int id() {
        return Item.getId(item.value());
    }

    public int count() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemEntry) obj;
        return this.item == that.item &&
                this.count == that.count &&
                this.components.equals(that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, count);
    }

    public boolean isSame(ItemEntry other) {
        return item == other.item && this.components.equals(other.components);
    }

    public static List<ItemEntry> fromStacks(List<ItemStack> stacks) {
        return stacks.stream().map(ItemEntry::fromStack).toList();
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        ItemStack itemStack = Item.byId(id()).getDefaultInstance();
        itemStack.applyComponents(components);
        itemStack.setCount(count);
        return itemStack;
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
                "id=" + item + ", " +
                "count=" + count + ']';
    }

//    public Tag save(HolderLookup.Provider levelRegistryAccess, Tag outputTag) {
//        if (this.isEmpty()) {
//            throw new IllegalStateException("Cannot encode empty ItemStack");
//        } else {
//            // Neo: Logs extra information about this ItemStack on error
//            return net.neoforged.neoforge.common.util.DataComponentUtil.wrapEncodingExceptions(this, CODEC, levelRegistryAccess, outputTag);
//        }
//    }
//
//    public static Optional<ItemEntry> parse(HolderLookup.Provider lookupProvider, Tag tag) {
//        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
//                .resultOrPartial(s -> LOGGER.error("Tried to load invalid item: '{}'", s));
//    }

    @Override
    public DataComponentMap getComponents() {
        return components;
    }

    public static final class LazySlot {
        private final ResourceKey<Level> dimension;
        private final BlockPos pos;
        private final int slot;
        private int counts;
        private final int limit;

        public static final Codec<LazySlot> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(LazySlot::dimension),
                        BlockPos.CODEC.fieldOf("pos").forGetter(LazySlot::pos),
                        Codec.INT.fieldOf("slot").forGetter(LazySlot::slot),
                        Codec.INT.fieldOf("counts").forGetter(LazySlot::counts),
                        Codec.INT.fieldOf("limit").forGetter(LazySlot::limit)
                ).apply(instance, LazySlot::new));

        public ResourceKey<Level> dimension() {
            return dimension;
        }

        public static final StreamCodec<ByteBuf, LazySlot> STREAM_CODEC = StreamCodec.composite(
                ResourceKey.streamCodec(Registries.DIMENSION), LazySlot::dimension,
                BlockPos.STREAM_CODEC, LazySlot::pos,
                ByteBufCodecs.INT, LazySlot::slot,
                ByteBufCodecs.INT, LazySlot::counts,
                ByteBufCodecs.INT, LazySlot::limit,
                LazySlot::new
        );

        public boolean setCounts(int counts) {
            int delta = counts - this.counts;
            this.counts = counts;
            return delta != 0;
        }

        public LazySlot(ResourceKey<Level> dimension, BlockPos pos, int slot, int counts, int limit) {
            this.dimension = dimension;
            this.pos = pos;
            this.slot = slot;
            this.counts = counts;
            this.limit = limit;
        }

        public static LazySlot fromSlot(Slot slot, Level level) {
            return new LazySlot(level.dimension(), slot.pos(), slot.slot(), slot.counts(), slot.limit());
        }

        public Slot toSlot() {
            return new Slot(pos, slot, counts, limit);
        }

        public CompoundTag save(CompoundTag tag) {
            tag.put("dimension", ResourceKey.codec(Registries.DIMENSION).encodeStart(NbtOps.INSTANCE, dimension).result().orElseThrow());
            tag.put("pos", StructureHandler.posTag(pos));
            tag.putInt("slot", slot);
            tag.putInt("counts", counts);
            tag.putInt("limit", limit);
            return tag;
        }

        public static LazySlot parse(CompoundTag tag) {
            return new LazySlot(
                    ResourceKey.codec(Registries.DIMENSION)
                            .parse(NbtOps.INSTANCE, tag.get("dimension"))
                            .resultOrPartial(System.err::println)
                            .orElseThrow(),
                    StructureHandler.parsePos(tag.getList("pos").orElseThrow()),
                    tag.getInt("slot").orElseThrow(),
                    tag.getInt("counts").orElseThrow(),
                    tag.getInt("limit").orElseThrow()
            );
        }

        public int validCounts() {
            return limit - counts;
        }

        public BlockPos pos() {
            return pos;
        }

        public int slot() {
            return slot;
        }

        public int counts() {
            return counts;
        }

        public int limit() {
            return limit;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Slot) obj;
            return Objects.equals(this.pos, that.pos) &&
                    this.slot == that.slot &&
                    this.counts == that.counts &&
                    this.limit == that.limit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, slot, counts, limit);
        }

        @Override
        public String toString() {
            return "LazySlot[" +
                    "pos=" + pos + ", " +
                    "slot=" + slot + ", " +
                    "counts=" + counts + ", " +
                    "limit=" + limit + ']';
        }
    }

    public static final class Slot {
        private final BlockPos pos;
        private final int slot;

        public void setCounts(int counts, Level level) {
            int delta = counts - this.counts;
            this.counts = counts;
            if (level == null || level.isClientSide() || delta == 0)
                return;
            ResourceHandler<ItemResource> capability = level.getCapability(Capabilities.Item.BLOCK, pos, Direction.NORTH);
            if (capability == null)
                return;
            if (delta < 0) {
                //TODO check
                capability.extract(
                        ItemResource.of(capability.getResource(slot).toStack().copy()),
                        -delta,
                        Transaction.openRoot()
                );
            } else {
                capability.insert(
                        slot,
                        ItemResource.of(ItemStack.EMPTY.copy()),
                        delta,
                        Transaction.openRoot()
                );
            }
        }

        private int counts;
        private final int limit;

        public Slot(BlockPos pos, int slot, int counts, int limit) {
            this.pos = pos;
            this.slot = slot;
            this.counts = counts;
            this.limit = limit;
        }

        public CompoundTag save(CompoundTag tag) {
                tag.put("pos", StructureHandler.posTag(pos));
                tag.putInt("slot", slot);
                tag.putInt("counts", counts);
                tag.putInt("limit", limit);
                return tag;
            }

        public static Slot parse(CompoundTag tag) {
                return new Slot(
                        StructureHandler.parsePos(tag.getList("pos").orElse(new ListTag())),
                        tag.getInt("slot").orElseThrow(),
                        tag.getInt("counts").orElseThrow(),
                        tag.getInt("limit").orElseThrow());
            }

        public int validCounts() {
                return limit - counts;
            }

        public BlockPos pos() {
            return pos;
        }

        public int slot() {
            return slot;
        }

        public int counts() {
            return counts;
        }

        public int limit() {
            return limit;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Slot) obj;
            return Objects.equals(this.pos, that.pos) &&
                    this.slot == that.slot &&
                    this.counts == that.counts &&
                    this.limit == that.limit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, slot, counts, limit);
        }

        @Override
        public String toString() {
            return "Slot[" +
                    "pos=" + pos + ", " +
                    "slot=" + slot + ", " +
                    "counts=" + counts + ", " +
                    "limit=" + limit + ']';
        }

    }
}

