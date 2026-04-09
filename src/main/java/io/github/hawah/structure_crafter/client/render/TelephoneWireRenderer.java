package io.github.hawah.structure_crafter.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class TelephoneWireRenderer {

    public final Map<Object, TelephoneStraitWire> telephoneWires = new HashMap<>();

    public void render(
            PoseStack poseStack,
            VertexConsumer buffer,
            Vec3 cameraPos,
            float width,
            float partialTicks
            ) {
        if (Minecraft.getInstance().level == null)
            return;
        for (var key : telephoneWires.keySet()) {
            var telephoneWire = telephoneWires.get(key);
            telephoneWire.render(poseStack, buffer, cameraPos, width, partialTicks);
        }
    }

    public void update(Object key, Vec3 start, Vec3 end, boolean hasBeacon, boolean shouldRender) {
        TelephoneStraitWire wire = telephoneWires.computeIfAbsent(key, k -> new TelephoneStraitWire(start, end));
        wire.startBuffer = start;
        wire.endBuffer = end;
        wire.hasBeacon = hasBeacon;
        wire.shouldRender = shouldRender;
    }

    public void pop(Object key) {
        telephoneWires.remove(key);
    }

    public void tick() {
        for (TelephoneStraitWire wire : telephoneWires.values()) {
            if (wire.endBuffer == null || wire.startBuffer == null)
                return;
            wire.oStart = wire.start;
            wire.oEnd = wire.end;
            wire.start = wire.startBuffer;
            wire.end = wire.endBuffer;
            wire.startBuffer = null;
            wire.endBuffer = null;
        }
    }
}
