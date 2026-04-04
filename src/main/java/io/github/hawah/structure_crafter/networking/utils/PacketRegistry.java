package io.github.hawah.structure_crafter.networking.utils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PacketRegistry{

    public static final PacketRegistry INSTANCE = new PacketRegistry();

    private final Set<PacketHolder<?>> packets = new HashSet<>();
    public final Set<PacketHolder<?>> packetView = Collections.unmodifiableSet(packets);

    private final Set<PacketHolder<?>> networkPacket = new HashSet<>();
    public final Set<PacketHolder<?>> networkPacketView = Collections.unmodifiableSet(networkPacket);

    public <T extends CustomPacketPayload> void register(PacketHolder<T> packet) {
        if (ClientToServerPacket.class.isAssignableFrom(packet.clazz) || ServerToClientPacket.class.isAssignableFrom(packet.clazz)) {
            packets.add(packet);
        } else {
            networkPacket.add(packet);
        }

    }

    public record PacketHolder<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            Class<T> clazz
    ){}
}
