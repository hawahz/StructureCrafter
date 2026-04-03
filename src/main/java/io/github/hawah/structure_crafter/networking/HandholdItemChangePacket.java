package io.github.hawah.structure_crafter.networking;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public record HandholdItemChangePacket(ItemStack stack) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, HandholdItemChangePacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, HandholdItemChangePacket::stack,
            HandholdItemChangePacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(stack.getItem()))
            return;
        player.getItemInHand(InteractionHand.MAIN_HAND).applyComponents(stack.getComponents());

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.HANDHOLD_ITEM_CHANGED;
    }
}
