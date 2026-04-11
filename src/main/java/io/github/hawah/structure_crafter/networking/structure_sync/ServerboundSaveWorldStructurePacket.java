package io.github.hawah.structure_crafter.networking.structure_sync;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.ServerStructureExport;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundSaveWorldStructurePacket(String fileName, BlockPos firstPos, BlockPos secondPos, BlockPos centerPos, boolean overwrite) implements ClientToServerPacket {

    public static final StreamCodec<ByteBuf, ServerboundSaveWorldStructurePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), ServerboundSaveWorldStructurePacket::fileName,
            BlockPos.STREAM_CODEC, ServerboundSaveWorldStructurePacket::firstPos,
            BlockPos.STREAM_CODEC, ServerboundSaveWorldStructurePacket::secondPos,
            BlockPos.STREAM_CODEC, ServerboundSaveWorldStructurePacket::centerPos,
            ByteBufCodecs.BOOL, ServerboundSaveWorldStructurePacket::overwrite,
            ServerboundSaveWorldStructurePacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        try {
            ServerStructureExport.saveStructure(
                    fileName(),
                    player,
                    firstPos(),
                    secondPos(),
                    centerPos(),
                    overwrite()
            );
        } catch (Exception e) {
            StructureCrafter.LOGGER.error("Failed to save structure named {} from player {} on server.", fileName, player.getName().getString(), e);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SERVER_SAVE_WORLD_STRUCTURE;
    }
}
