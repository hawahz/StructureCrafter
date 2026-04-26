package io.github.hawah.structure_crafter.networking.structure_sync;

import com.mojang.logging.LogUtils;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.networking.utils.ServerToClientPacket;
import io.github.hawah.structure_crafter.util.CompressedTag;
import io.github.hawah.structure_crafter.util.StructureHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public record ClientboundUploadStructureToServerPacket(String playerName, String structureName) implements ServerToClientPacket {

    public static final StreamCodec<ByteBuf, ClientboundUploadStructureToServerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), ClientboundUploadStructureToServerPacket::playerName,
            ByteBufCodecs.stringUtf8(32), ClientboundUploadStructureToServerPacket::structureName,
            ClientboundUploadStructureToServerPacket::new
    );

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        if (playerName == null || !structureName.endsWith(".nbt"))
            return;

        Path dir = Paths.STRUCTURE_DIR;
        Path file = Path.of(structureName);

        Path path = dir.resolve(file).normalize();
        if (!path.startsWith(dir))
            return;
        StructureTemplate st = new StructureTemplate();
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
            CompoundTag nbt = NbtIo.read(stream, NbtAccounter.create(0x20000000L));
            st.load(Minecraft.getInstance().level.holderLookup(Registries.BLOCK), nbt);
            if (!StructureHandler.isSizeValid(st.getSize())) {
                throw new RuntimeException(LangData.WARN_STRUCTURE_PLACED_OVERSIZE.get().getString());
            }
            if (nbt.sizeInBytes() > 2097152) {
                CompletableFuture
                        .supplyAsync(() -> CompressedTag.split(nbt, true))
                        .thenAccept(split -> {
                            for (CompressedTag tag : split) {
                                Networking.sendToServer(
                                        new ServerboundReceiveSplitStructureDataPacket(tag, structureName, playerName)
                                );
                            }
                        });
            } else {
                Networking.sendToServer(new ServerboundReceiveStructureDataPacket(nbt, playerName, structureName));
            }
        } catch (IOException | RuntimeException e) {
            LogUtils.getLogger().warn("Failed to upload structure file.", e);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.CLIENT_UPLOAD_STRUCTURE_TO_SERVER;
    }
}
