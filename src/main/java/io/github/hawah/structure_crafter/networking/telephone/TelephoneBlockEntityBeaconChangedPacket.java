package io.github.hawah.structure_crafter.networking.telephone;

import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record TelephoneBlockEntityBeaconChangedPacket(BlockPos pos, boolean hasBeacon) implements ServerToClientPacket {
    public static final StreamCodec<ByteBuf, TelephoneBlockEntityBeaconChangedPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TelephoneBlockEntityBeaconChangedPacket::pos,
            ByteBufCodecs.BOOL, TelephoneBlockEntityBeaconChangedPacket::hasBeacon,
            TelephoneBlockEntityBeaconChangedPacket::new
    );

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        Level level = player.level();
        if (!(level.getBlockEntity(pos()) instanceof TelephoneBlockEntity telephoneBlockEntity))
            return;
        telephoneBlockEntity.hasBeacon = hasBeacon();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.TELEPHONE_BEACON_CHANGED_TO_CLIENT;
    }
}
