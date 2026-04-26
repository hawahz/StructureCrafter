package io.github.hawah.structure_crafter;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Pre event) {
        ServerTaskHandler.tick();
    }
}
