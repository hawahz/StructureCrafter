package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

import java.util.List;
import java.util.Objects;

public class MaterialListComponent {
    public static final MaterialListComponent EMPTY = new MaterialListComponent(List.of());
    public static final Codec<MaterialListComponent> CODEC = ItemEntry.CODEC.listOf().xmap(
            MaterialListComponent::new, MaterialListComponent::getItemWithCounts
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialListComponent> STREAM_CODEC =
            ItemEntry.STREAM_CODEC
                    .apply(ByteBufCodecs.list())
                    .map(MaterialListComponent::new, MaterialListComponent::getItemWithCounts);

    public List<ItemEntry> getItemWithCounts() {
        return itemWithCounts;
    }
    public boolean isEmpty() {
        return itemWithCounts.isEmpty();
    }
    public final List<ItemEntry> itemWithCounts;

    public MaterialListComponent(List<ItemEntry> items) {
        this.itemWithCounts = items;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MaterialListComponent that)) return false;
        return Objects.equals(itemWithCounts, that.itemWithCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemWithCounts);
    }
}
