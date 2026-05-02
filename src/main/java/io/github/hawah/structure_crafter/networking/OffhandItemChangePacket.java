package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.StructureHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record OffhandItemChangePacket(ItemStack stack) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, OffhandItemChangePacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, OffhandItemChangePacket::stack,
            OffhandItemChangePacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        if (!player.getItemInHand(InteractionHand.OFF_HAND).is(stack.getItem()))
            return;
        player.getItemInHand(InteractionHand.OFF_HAND).applyComponents(stack.getComponents());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.OFFHAND_ITEM_CHANGED;
    }
}
