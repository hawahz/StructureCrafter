package io.github.hawah.structure_crafter.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;

public class TelephoneWireRenderer {

    public static void render(PoseStack poseStack,
                       VertexConsumer buffer,
                       Vec3 oriStart,
                       Vec3 oriEnd,
                       Vec3 cameraPos,
                       float width) {
        RenderSystem.setShaderTexture(0, Textures.FULL_RED.getResource());
        Vec3 end = oriEnd.subtract(cameraPos);
        Vec3 start = oriStart.subtract(cameraPos);

        PoseStack.Pose pose = poseStack.last();

        Vec3 dir = end.subtract(start).normalize();
        Vec3 viewDir = end.add(start).normalize();

        Vec3 right = dir.cross(viewDir).normalize();
        Vec3 offset = right.scale(width / 2.0);

        Vec3 v1 = start.add(offset);
        Vec3 v2 = start.subtract(offset);
        Vec3 v3 = end.subtract(offset);
        Vec3 v4 = end.add(offset);

        double length = start.distanceTo(end);
        float tile = (float)(length / width); // 每1格重复一次

        addVertex(buffer, pose, v1, 0, 0);
        addVertex(buffer, pose, v2, 0, 1);
        addVertex(buffer, pose, v3, tile, 1);
        addVertex(buffer, pose, v4, tile, 0);
    }

    private static void addVertex(VertexConsumer buffer,
                           PoseStack.Pose pose,
                           Vec3 pos,
                           float u, float v) {

        buffer.addVertex(pose.pose(),
                        (float) pos.x,
                        (float) pos.y,
                        (float) pos.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(0xF000F0)
                .setNormal(0, 1, 0);
    }
}
