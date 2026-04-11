package io.github.hawah.structure_crafter.networking.structure_sync;

import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.networking.utils.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;

public record ClientboundUploadStructureToServerPacket(String playerName, String structureName) implements ServerToClientPacket {

    public static final StreamCodec<ByteBuf, ClientboundUploadStructureToServerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), ClientboundUploadStructureToServerPacket::playerName,
            ByteBufCodecs.stringUtf8(32), ClientboundUploadStructureToServerPacket::structureName,
            ClientboundUploadStructureToServerPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        if (playerName == null || !structureName.endsWith(".nbt"))
            return;

        Path dir = Paths.STRUCTURE_DIR;
        Path file = Path.of(structureName);

        Path path = dir.resolve(file).normalize();
        if (!path.startsWith(dir))
            return;

        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
            CompoundTag nbt = NbtIo.read(stream, NbtAccounter.create(0x20000000L));
            if (nbt.sizeInBytes() > 2097152) {
                // TODO Split File To Fragments
                throw new FileSystemException("Structure is too big");
            }
            Networking.sendToServer(new ServerboundReceiveStructureDataPacket(nbt, playerName, structureName));
        } catch (IOException ignored) {
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.CLIENT_UPLOAD_STRUCTURE_TO_SERVER;
    }
}
