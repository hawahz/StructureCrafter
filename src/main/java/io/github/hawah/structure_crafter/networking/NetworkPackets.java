package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public enum NetworkPackets implements BasePacketPayload.PacketTypeProvider {
    //C2S
    PLACE_STRUCTURE(PlaceStructurePacket.class, PlaceStructurePacket.STREAM_CODEC),
    HANDHOLD_ITEM_CHANGED(HandholdItemChangePacket.class, HandholdItemChangePacket.STREAM_CODEC),
    CLIENTBOUND_CONTAINER_SLOT_CHANGED(ClientboundContainerSlotChangedPacket.class, ClientboundContainerSlotChangedPacket.STREAM_CODEC),
    ;
    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> NetworkPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(asResource(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(StructureCrafter.MODID, "1.0");
        for (NetworkPackets packet : NetworkPackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, path);
    }
}
