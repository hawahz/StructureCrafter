package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.io.IOException;

public record ServerboundTelephoneChanged(TelephoneHandsetComponent component) implements ClientToServerPacket {

    public static final StreamCodec<? super RegistryFriendlyByteBuf, ServerboundTelephoneChanged> STREAM_CODEC =
            StreamCodec.composite(TelephoneHandsetComponent.STREAM_CODEC, ServerboundTelephoneChanged::component, ServerboundTelephoneChanged::new);
    @Override
    public void handle(ServerPlayer player) {
        try (ServerLevel level = player.serverLevel()) {
            BlockPos pos = player.blockPosition();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TelephoneBlockEntity telephoneBlockEntity) {
                telephoneBlockEntity.setHasTelephone(!telephoneBlockEntity.hasTelephone());
                telephoneBlockEntity.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.TELEPHONE_CHANGED_TO_SERVER;
    }
}
