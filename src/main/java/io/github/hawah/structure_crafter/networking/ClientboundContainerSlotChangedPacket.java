package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public record ClientboundContainerSlotChangedPacket(int slotId, ItemStack itemStack) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSlotChangedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundContainerSlotChangedPacket::slotId,
            ItemStack.STREAM_CODEC, ClientboundContainerSlotChangedPacket::itemStack,
            ClientboundContainerSlotChangedPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;

        if (slotId >= 0 && slotId < menu.slots.size()) {

            Slot slot = menu.slots.get(slotId);

            if (slot.hasItem() && slot.getItem().is(itemStack.getItem())) {

                slot.getItem().applyComponents(itemStack.getComponents());
                player.getInventory().setChanged();
                // 标记更新
                menu.broadcastChanges();
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.CLIENTBOUND_CONTAINER_SLOT_CHANGED;
    }
}
