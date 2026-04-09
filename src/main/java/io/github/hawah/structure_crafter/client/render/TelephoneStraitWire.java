package io.github.hawah.structure_crafter.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TelephoneStraitWire {

    public Vec3 start, oStart;
    public Vec3 end, oEnd;
    public boolean hasBeacon = false;
    public boolean shouldRender;

    public TelephoneStraitWire(Vec3 start, Vec3 end) {
        this.start = start;
        this.end = end;
        this.oStart = start;
        this.oEnd = end;
    }

    public void render(
            PoseStack poseStack,
            VertexConsumer buffer,
            Vec3 cameraPos,
            float width,
            float partialTicks) {
//        RenderSystem.setShaderTexture(0, Textures.TELEPHONE_WIRE.getResource());
        if (!shouldRender) {
            return;
        }
        Vec3 start = oStart.lerp(this.start, partialTicks);
        Vec3 end = oEnd.lerp(this.end, partialTicks);
        render(
                poseStack,
                buffer,
                start,
                end,
                cameraPos,
                width
        );
    }

    public void render(PoseStack poseStack,
                              VertexConsumer buffer,
                              Vec3 oriStart,
                              Vec3 oriEnd,
                              Vec3 cameraPos,
                              float width) {
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
        Vec3 green = new Vec3(0, 255, 0);
        Vec3 yellow = new Vec3(251, 242, 54);
        Vec3 red = new Vec3(172, 50, 50);
        double process = length / 32;
        double yellowRate = 0.75;
        Vec3 color = process < yellowRate?
                green.lerp(yellow, Mth.clamp( process / yellowRate, 0, 1)) :
                yellow.lerp(red, Mth.clamp((process - yellowRate) / (1 - yellowRate), 0, 1));
        color = hasBeacon? new Vec3(0, 250, 250) : color;

        addVertex(buffer, pose, v1, 0, 0, (float) color.x, (float) color.y, (float) color.z);
        addVertex(buffer, pose, v2, 0, 1, (float) color.x, (float) color.y, (float) color.z);
        addVertex(buffer, pose, v3, tile, 1, (float) color.x, (float) color.y, (float) color.z);
        addVertex(buffer, pose, v4, tile, 0, (float) color.x, (float) color.y, (float) color.z);
    }

    private static void addVertex(VertexConsumer buffer,
                                  PoseStack.Pose pose,
                                  Vec3 pos,
                                  float u,
                                  float v,
                                  float r,
                                  float g,
                                  float b) {

        buffer.addVertex(pose.pose(),
                        (float) pos.x,
                        (float) pos.y,
                        (float) pos.z)
                .setColor(r/255, g/255, b/255, 1)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(0xF000F0)
                .setNormal(0, 1, 0);
    }

    public Vec3 startBuffer = null, endBuffer = null;
}
