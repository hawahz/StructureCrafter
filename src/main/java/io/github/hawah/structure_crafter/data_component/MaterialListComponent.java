package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Objects;

public record MaterialListComponent(List<ItemEntry> itemWithCounts) {
    public static final MaterialListComponent EMPTY = new MaterialListComponent(List.of());
    public static final Codec<MaterialListComponent> CODEC =
            ItemEntry.CODEC
                    .sizeLimitedListOf(512)
                    .xmap(MaterialListComponent::new, MaterialListComponent::itemWithCounts);
    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialListComponent> STREAM_CODEC =
            ItemEntry.STREAM_CODEC
                    .apply(ByteBufCodecs.list(512))
                    .map(MaterialListComponent::new, MaterialListComponent::itemWithCounts);

    public boolean isEmpty() {
        return itemWithCounts.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MaterialListComponent(List<ItemEntry> withCounts))) return false;
        return Objects.equals(itemWithCounts, withCounts);
    }

}
