package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.networking.utils.BasePacketPayload;
import io.github.hawah.structure_crafter.networking.utils.PacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public enum NetworkPackets implements BasePacketPayload.PacketTypeProvider {
    //C2S
    PLACE_STRUCTURE(PlaceStructurePacket.class, PlaceStructurePacket.STREAM_CODEC),
    HANDHOLD_ITEM_CHANGED(HandholdItemChangePacket.class, HandholdItemChangePacket.STREAM_CODEC),
    CLIENTBOUND_CONTAINER_SLOT_CHANGED(ClientboundContainerSlotChangedPacket.class, ClientboundContainerSlotChangedPacket.STREAM_CODEC),
    DROP_ITEM(DropItemPacket.class, DropItemPacket.STREAM_CODEC),
    MATERIAL_LIST_SCATTERED(MaterialListScatteredPacket.class, MaterialListScatteredPacket.STREAM_CODEC),
    MATERIAL_LIST_UPLOAD(MaterialListUploadPacket.class, MaterialListUploadPacket.STREAM_CODEC),
    INVENTORY_REMOVE_ITEM(PlayerInventoryRemoveItemPacket.class, PlayerInventoryRemoveItemPacket.STREAM_CODEC),
    TELEPHONE_CHANGED_TO_SERVER(ServerboundTelephoneChanged.class, ServerboundTelephoneChanged.STREAM_CODEC),
    TELEPHONE_BEACON_CHANGED_TO_CLIENT(TelephoneBlockEntityBeaconChangedPacket.class, TelephoneBlockEntityBeaconChangedPacket.STREAM_CODEC),
    ;
    private final PacketRegistry.PacketHolder<?> type;

    <T extends BasePacketPayload> NetworkPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new PacketRegistry.PacketHolder<>(
                new CustomPacketPayload.Type<>(asResource(name)),
                codec, clazz
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        for (NetworkPackets packet : NetworkPackets.values()) {
            PacketRegistry.INSTANCE.register(packet.type);
        }
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(StructureCrafter.MODID, path);
    }
}
