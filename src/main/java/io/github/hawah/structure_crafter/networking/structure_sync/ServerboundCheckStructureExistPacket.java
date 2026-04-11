package io.github.hawah.structure_crafter.networking.structure_sync;

import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundCheckStructureExistPacket(String playerName, String structureName) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundCheckStructureExistPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), ServerboundCheckStructureExistPacket::playerName,
            ByteBufCodecs.stringUtf8(128), ServerboundCheckStructureExistPacket::structureName,
            ServerboundCheckStructureExistPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SERVER_CHECK_EXIST_STRUCTURE;
    }
}
