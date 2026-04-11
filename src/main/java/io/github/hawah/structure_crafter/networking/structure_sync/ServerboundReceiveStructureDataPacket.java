package io.github.hawah.structure_crafter.networking.structure_sync;

import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.ServerStructureExport;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public record ServerboundReceiveStructureDataPacket(CompoundTag data, String owner, String fileName) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundReceiveStructureDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, ServerboundReceiveStructureDataPacket::data,
            ByteBufCodecs.stringUtf8(32), ServerboundReceiveStructureDataPacket::owner,
            ByteBufCodecs.stringUtf8(128), ServerboundReceiveStructureDataPacket::fileName,
            ServerboundReceiveStructureDataPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        Path dir = Paths.UPLOAD_STRUCTURE_DIR.resolve(owner());
        Path file = java.nio.file.Paths.get(fileName());
        ServerStructureExport.saveDataToDirectory(dir, file, data);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SERVER_RECEIVE_STRUCTURE_DATA;
    }
}
