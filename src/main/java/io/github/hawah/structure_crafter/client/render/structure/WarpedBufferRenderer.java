package io.github.hawah.structure_crafter.client.render.structure;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class WarpedBufferRenderer {

    private final TemplateMesh templateMesh;

    // 使用joml库的Matrix和Vector，便于快速复制和使用，没有实际意义
    private final Matrix4f modelMat = new Matrix4f();
    private final Matrix3f normalMat = new Matrix3f();
    private final Vector4f pos = new Vector4f();
    private final Vector3f normal = new Vector3f();

    // 便于快速复制和使用，没有实际意义
    private final PoseStack transforms = new PoseStack();

    public WarpedBufferRenderer(TemplateMesh templateMesh) {
        this.templateMesh = templateMesh;
        reset();
    }

    public void render(PoseStack poseStack, VertexConsumer builder, BlockAndTintGetter level) {

        Matrix4f modelMat = this.modelMat.set(poseStack.last()
                .pose());
        Matrix4f localTransforms = transforms.last()
                .pose();
        modelMat.mul(localTransforms);

        Matrix3f normalMat = this.normalMat.set(poseStack.last()
                .normal());
        Matrix3f localNormalTransforms = transforms.last()
                .normal();
        normalMat.mul(localNormalTransforms);

        Vector4f pos = this.pos;
        Vector3f normal = this.normal;

        int vertexCount = templateMesh.vertexCount();

        for (int i = 0; i < vertexCount; i++) {
            float x = templateMesh.x(i);
            float y = templateMesh.y(i);
            float z = templateMesh.z(i);

            pos.set(x, y, z, 1.0f);
            pos.mul(modelMat);

            int packedNormal = templateMesh.normal(i);
            float normalX = (packedNormal & 0xFF) / 127.0f;
            float normalY = ((packedNormal >>> 8) & 0xFF) / 127.0f;
            float normalZ = ((packedNormal >>> 16) & 0xFF) / 127.0f;

            normal.set(normalX, normalY, normalZ);
            normal.mul(normalMat);

            int color = templateMesh.color(i);
            float r = (color & 0xFF) / 255.0f;
            float g = ((color >>> 8) & 0xFF) / 255.0f;
            float b = ((color >>> 16) & 0xFF) / 255.0f;
            float a = ((color >>> 24) & 0xFF) / 255.0f;

            float u = templateMesh.u(i);
            float v = templateMesh.v(i);

            int overlay = templateMesh.overlay(i);

            int light = LightTexture.pack(
                    level.getBrightness(LightLayer.BLOCK, new BlockPos(
                            (int) x,
                            (int) y,
                            (int) z)),
                    15
            );


            builder.addVertex(pos.x(), pos.y(), pos.z())
                    .setColor(r, g, b, a)
                    .setUv(u, v)
                    .setNormal(normal.x(), normal.y(), normal.z())
                    .setLight(light)
                    .setOverlay(overlay);
        }

        reset();
    }

    private void reset() {
        while(!transforms.clear()){
            transforms.popPose();
        }
        transforms.pushPose();
    }

    public boolean isEmpty() {
        return templateMesh.isEmpty();
    }
}
