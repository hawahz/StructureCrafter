package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.structure_crafter.util.ItemEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HashItemComponent(
        Map<ItemEntry, LazySlotWarper> items,
        LazySlotWarper changedSlots,
        boolean dirty
) {

    public static final HashItemComponent EMPTY = new HashItemComponent(new HashMap<>(), LazySlotWarper.EMPTY, false);
    public static Codec<Map<ItemEntry, LazySlotWarper>> ITEMS_CODEC = Codec.unboundedMap(ItemEntry.CODEC, LazySlotWarper.CODEC);
    public static Codec<HashItemComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ITEMS_CODEC.fieldOf("items").forGetter(HashItemComponent::items),
                    LazySlotWarper.CODEC.fieldOf("changedSlots").forGetter(HashItemComponent::changedSlots),
                    Codec.BOOL.fieldOf("dirty").forGetter(HashItemComponent::dirty)
            ).apply(instance, HashItemComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Map<ItemEntry, LazySlotWarper>> ITEMS_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new,
            ItemEntry.STREAM_CODEC,
            LazySlotWarper.STREAM_CODEC
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, HashItemComponent> STREAM_CODEC = StreamCodec.composite(
            ITEMS_STREAM_CODEC, HashItemComponent::items,
            LazySlotWarper.STREAM_CODEC, HashItemComponent::changedSlots,
            ByteBufCodecs.BOOL, HashItemComponent::dirty,
            HashItemComponent::new
    );

    public void updateChangedSlots(LazySlotWarper changedSlots) {
        this.changedSlots().entries().clear();
        this.changedSlots().entries().addAll(changedSlots.entries());
    }

    public record LazySlotWarper(List<ItemEntry.LazySlot> entries) {
        public static final LazySlotWarper EMPTY = new LazySlotWarper(List.of());

        public static final Codec<LazySlotWarper> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.list(ItemEntry.LazySlot.CODEC).fieldOf("entries").forGetter(LazySlotWarper::entries)
                ).apply(instance, LazySlotWarper::new)
        );

        public static final StreamCodec<ByteBuf, LazySlotWarper> STREAM_CODEC =
                ItemEntry.LazySlot.STREAM_CODEC
                        .apply(ByteBufCodecs.list())
                        .map(LazySlotWarper::new, LazySlotWarper::entries);

        public static LazySlotWarper warp(List<ItemEntry.LazySlot> entries) {
            return new LazySlotWarper(entries);
        }
    }
}
