package io.github.hawah.structure_crafter;

import io.github.hawah.structure_crafter.networking.structure_sync.ServerCompressedTagReceiver;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class ServerEvents {
    static long runningTick = 0;
    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Pre event) {
        ServerTaskManager.tick();
        runningTick++;
        if (event.getServer().getTickCount() % (Config.ServerConfig.UPLOAD_WAIT_TIME.getAsInt() * 2) == 0) {
            ServerCompressedTagReceiver.cleanSparePackets();
        }
    }

    public static long runningTick() {
        return runningTick;
    }
}
