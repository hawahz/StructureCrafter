package io.github.hawah.structure_crafter.lib.networking;

import net.minecraft.server.level.ServerPlayer;

public non-sealed interface ClientToServerPacket extends BasePacketPayload {
    void handle(ServerPlayer player);
}
