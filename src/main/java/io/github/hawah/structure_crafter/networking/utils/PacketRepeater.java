package io.github.hawah.structure_crafter.networking.utils;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRepeater {


    public static void handleClient(final ServerToClientPacket packet, final IPayloadContext context) {
        packet.handle((LocalPlayer) context.player());
    }

    public static void handleServer(final ClientToServerPacket packet, final IPayloadContext context) {
        packet.handle((ServerPlayer) context.player());
    }

}
