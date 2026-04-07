package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.block.blockentity.ConnectorBlockEntity;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerboundTelephoneChanged(BlockPos pos) implements ClientToServerPacket {

    public static final StreamCodec<? super RegistryFriendlyByteBuf, ServerboundTelephoneChanged> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, ServerboundTelephoneChanged::pos, ServerboundTelephoneChanged::new);
    @Override
    public void handle(ServerPlayer player) {
        Level level = player.level();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ConnectorBlockEntity telephoneBlockEntity) {
            telephoneBlockEntity.setHasTelephone(!telephoneBlockEntity.hasTelephone());
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.TELEPHONE_CHANGED_TO_SERVER;
    }
}
