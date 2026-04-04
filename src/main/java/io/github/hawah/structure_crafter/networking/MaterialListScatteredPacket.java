package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public record MaterialListScatteredPacket(int tmp) implements ClientToServerPacket {

    public static final StreamCodec<? super RegistryFriendlyByteBuf, MaterialListScatteredPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, MaterialListScatteredPacket::tmp, MaterialListScatteredPacket::new);

    @Override
    public void handle(ServerPlayer player) {
        player.getMainHandItem().shrink(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_NUGGET.getDefaultInstance());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MATERIAL_LIST_SCATTERED;
    }
}
