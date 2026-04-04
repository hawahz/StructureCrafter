package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.MaterialListComponent;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record MaterialListUploadPacket(List<ItemEntry> items) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialListUploadPacket> STREAM_CODEC =
            ItemEntry.STREAM_CODEC
                    .apply(ByteBufCodecs.list())
                    .map(MaterialListUploadPacket::new, MaterialListUploadPacket::items);

    @Override
    public void handle(ServerPlayer player) {
        player.getOffhandItem().set(DataComponentTypeRegistries.MATERIAL_LIST, new MaterialListComponent(items));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MATERIAL_LIST_UPLOAD;
    }
}
