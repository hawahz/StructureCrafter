package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public record PlayerInventoryRemoveItemPacket(ItemStack stack) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerInventoryRemoveItemPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, PlayerInventoryRemoveItemPacket::stack,
            PlayerInventoryRemoveItemPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        int containerSize = inventory.getContainerSize();
        inventory.clearOrCountMatchingItems(
                itemStack -> itemStack.getComponentsPatch().equals(stack.getComponentsPatch()),
                stack.getCount(),
                inventory
        );
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.INVENTORY_REMOVE_ITEM;
    }
}
