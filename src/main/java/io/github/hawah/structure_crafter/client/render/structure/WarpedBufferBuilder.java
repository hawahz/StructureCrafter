package io.github.hawah.structure_crafter.client.render.structure;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WarpedBufferBuilder implements VertexConsumer {
    private BufferBuilder bufferBuilder;


    public void begin() {
        bufferBuilder = new BufferBuilder(
                new ByteBufferBuilder(512),
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.BLOCK
        );
    }

    // 将顶点打包成 MeshData
    public WarpedBufferRenderer end() {
        MeshData meshData = bufferBuilder.build();
        //ByteBuffer byteBuffer = meshData.indexBuffer();
        TemplateMesh templateMesh = new TemplateMesh(meshData);
        return new WarpedBufferRenderer(templateMesh);
    }



    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }

    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha) {
        bufferBuilder.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, readAlpha);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        throw new NotImplementedException("Not implemented");
    }
}
