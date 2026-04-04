package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record DropItemPacket(List<ItemStack> itemStacks) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, DropItemPacket> STREAM_CODEC = ItemStack.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(DropItemPacket::new, DropItemPacket::itemStacks);
    @Override
    public void handle(ServerPlayer player) {
        for (ItemStack itemStack : itemStacks) {
            player.drop(itemStack, true);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.DROP_ITEM;
    }
}
