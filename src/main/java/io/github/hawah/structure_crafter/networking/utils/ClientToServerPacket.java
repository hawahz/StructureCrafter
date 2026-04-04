package io.github.hawah.structure_crafter.networking.utils;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public non-sealed interface ClientToServerPacket extends BasePacketPayload {
    void handle(ServerPlayer player);
}
