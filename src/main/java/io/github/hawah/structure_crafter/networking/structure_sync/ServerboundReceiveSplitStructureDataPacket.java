package io.github.hawah.structure_crafter.networking.structure_sync;

import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.CompressedTag;
import io.github.hawah.structure_crafter.util.ServerStructureExport;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.Objects;

public final class ServerboundReceiveSplitStructureDataPacket implements ClientToServerPacket {

    public static final StreamCodec<ByteBuf, ServerboundReceiveSplitStructureDataPacket> STREAM_CODEC = StreamCodec.composite(
            CompressedTag.STREAM_CODEC, ServerboundReceiveSplitStructureDataPacket::tag,
            ByteBufCodecs.stringUtf8(128), ServerboundReceiveSplitStructureDataPacket::fileName,
            ByteBufCodecs.stringUtf8(32), ServerboundReceiveSplitStructureDataPacket::owner,
            ServerboundReceiveSplitStructureDataPacket::new
    );
    private final CompressedTag tag;
    private final String fileName;
    private final String owner;
    private CompoundTag parsedTag = null;

    public ServerboundReceiveSplitStructureDataPacket(CompressedTag tag, String fileName, String owner) {
        this.tag = tag;
        this.fileName = fileName;
        this.owner = owner;
    }

    @Override
    public void handleData() {
        ClientToServerPacket.super.handleData();
        ServerCompressedTagReceiver.receive(tag).ifPresent((tag) -> parsedTag = tag);
    }

    @Override
    public void handle(ServerPlayer player) {
        if (parsedTag == null)
            return;
        Path dir = Paths.UPLOAD_STRUCTURE_DIR.resolve(owner());
        Path file = java.nio.file.Paths.get(fileName());
        ServerStructureExport.saveDataToDirectory(dir, file, parsedTag);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SERVER_RECEIVE_SPLIT_STRUCTURE_DATA;
    }

    public CompressedTag tag() {
        return tag;
    }

    public String fileName() {
        return fileName;
    }

    public String owner() {
        return owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ServerboundReceiveSplitStructureDataPacket) obj;
        return Objects.equals(this.tag, that.tag) &&
                Objects.equals(this.fileName, that.fileName) &&
                Objects.equals(this.owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, fileName, owner);
    }

    @Override
    public String toString() {
        return "ServerboundReceiveSplitStructureDataPacket[" +
                "tag=" + tag + ", " +
                "fileName=" + fileName + ", " +
                "owner=" + owner + ']';
    }

}
