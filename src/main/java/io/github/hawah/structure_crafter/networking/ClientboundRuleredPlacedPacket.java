package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.networking.utils.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ClientboundRuleredPlacedPacket(BlockPos pos) implements ServerToClientPacket {

    public static final StreamCodec<ByteBuf, ClientboundRuleredPlacedPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundRuleredPlacedPacket::pos,
            ClientboundRuleredPlacedPacket::new
    );

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        StructureCrafterClient.RULER_OFF_HANDLER.update(pos);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.RULERED_PLACED;
    }
}
