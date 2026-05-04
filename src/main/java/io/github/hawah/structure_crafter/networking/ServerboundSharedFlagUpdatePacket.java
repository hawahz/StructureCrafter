package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.lib.networking.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.SharedFlags;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ServerboundSharedFlagUpdatePacket(UUID playerUUID, int flag) implements ClientToServerPacket {

    public static final StreamCodec<ByteBuf, ServerboundSharedFlagUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundSharedFlagUpdatePacket::playerUUID,
            ByteBufCodecs.INT, ServerboundSharedFlagUpdatePacket::flag,
            ServerboundSharedFlagUpdatePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        SharedFlags.setSharedFlags(playerUUID(), flag());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHARED_FLAG_UPDATE_PACKET;
    }

}
