package io.github.hawah.structure_crafter.networking.utils;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public non-sealed interface ServerToClientPacket extends BasePacketPayload {
    void handle(LocalPlayer player);
}
