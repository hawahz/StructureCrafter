package io.github.hawah.structure_crafter.networking.telephone;

import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import io.github.hawah.structure_crafter.networking.utils.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundTelephoneBlockEntityTelephoneChangedPacket(BlockPos pos, Holder<DimensionType> dimension, boolean hasTelephone) implements ServerToClientPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTelephoneBlockEntityTelephoneChangedPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundTelephoneBlockEntityTelephoneChangedPacket::pos,
            DimensionType.STREAM_CODEC, ClientboundTelephoneBlockEntityTelephoneChangedPacket::dimension,
            ByteBufCodecs.BOOL, ClientboundTelephoneBlockEntityTelephoneChangedPacket::hasTelephone,
            ClientboundTelephoneBlockEntityTelephoneChangedPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        Level level = player.level();
        if (!level.dimensionTypeRegistration().equals(dimension())|| !(level.getBlockEntity(pos()) instanceof TelephoneBlockEntity telephoneBlockEntity)) {
            return;
        }
        telephoneBlockEntity.hasTelephone = hasTelephone;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.CLIENT_TELEPHONE_CHANGED;
    }
}
