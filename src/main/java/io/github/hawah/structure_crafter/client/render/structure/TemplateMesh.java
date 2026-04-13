package io.github.hawah.structure_crafter.client.render.structure;

import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.nio.ByteBuffer;

public class TemplateMesh {

    // 一个顶点有x, y, z, color, u, v, overlay, light, normal九个数据，所以一个顶点的INT步长是9
    private static final int VERTEX_STRIDE_INT = 9;
    private static final int X_OFFSET;
    private static final int Y_OFFSET;
    private static final int Z_OFFSET;
    private static final int COLOR_OFFSET;
    private static final int U_OFFSET;
    private static final int V_OFFSET;
    private static final int OVERLAY_OFFSET;
    private static final int LIGHT_OFFSET;
    private static final int NORMAL_OFFSET;
    private final int vertexCount;
    private final int[] data;

    static {
        int i = 0;
        X_OFFSET = i++;
        Y_OFFSET = i++;
        Z_OFFSET = i++;
        COLOR_OFFSET = i++;
        U_OFFSET = i++;
        V_OFFSET = i++;
        OVERLAY_OFFSET = i++;
        LIGHT_OFFSET = i++;
        NORMAL_OFFSET = i;
    }

    public TemplateMesh(MeshData meshData) {
        if (meshData == null) {
            this.vertexCount = 0;
            this.data = new int[0];
            return;
        }
        // 顶点个数
        this.vertexCount = meshData.drawState().vertexCount();
        // 顶点数据
        ByteBuffer vertexBuffer = meshData.vertexBuffer();
        // stride 就是一个顶点的大小，读取下一个顶点需要经过的步长
        int stride = meshData.drawState().format().getVertexSize();

        this.data = new int[vertexCount * VERTEX_STRIDE_INT];

        for (int i = 0; i < vertexCount; i++) {
            x(i, vertexBuffer.getFloat(i * stride));
            y(i, vertexBuffer.getFloat(i * stride + 4));
            z(i, vertexBuffer.getFloat(i * stride + 8));
            color(i, vertexBuffer.getInt(i * stride + 12));
            u(i, vertexBuffer.getFloat(i * stride + 16));
            v(i, vertexBuffer.getFloat(i * stride + 20));
            overlay(i, OverlayTexture.NO_OVERLAY);
            light(i, vertexBuffer.getInt(i * stride + 24));
            normal(i, vertexBuffer.getInt(i * stride + 28));
        }
    }

    private void x(int index, float x) {
        data[index * VERTEX_STRIDE_INT + X_OFFSET] = Float.floatToRawIntBits(x);
    }

    private void y(int index, float y) {
        data[index * VERTEX_STRIDE_INT + Y_OFFSET] = Float.floatToRawIntBits(y);
    }

    private void z(int index, float z) {
        data[index * VERTEX_STRIDE_INT + Z_OFFSET] = Float.floatToRawIntBits(z);
    }

    private void color(int index, int color) {
        data[index * VERTEX_STRIDE_INT + COLOR_OFFSET] = color;
    }

    private void u(int index, float u) {
        data[index * VERTEX_STRIDE_INT + U_OFFSET] = Float.floatToRawIntBits(u);
    }

    private void v(int index, float v) {
        data[index * VERTEX_STRIDE_INT + V_OFFSET] = Float.floatToRawIntBits(v);
    }

    @SuppressWarnings("SameParameterValue")
    private void overlay(int index, int overlay) {
        data[index * VERTEX_STRIDE_INT + OVERLAY_OFFSET] = overlay;
    }

    private void light(int index, int light) {
        data[index * VERTEX_STRIDE_INT + LIGHT_OFFSET] = light;
    }

    private void normal(int index, int normal) {
        data[index * VERTEX_STRIDE_INT + NORMAL_OFFSET] = normal;
    }

    public float x(int index) {
        return Float.intBitsToFloat(data[index * VERTEX_STRIDE_INT + X_OFFSET]);
    }

    public float y(int index) {
        return Float.intBitsToFloat(data[index * VERTEX_STRIDE_INT + Y_OFFSET]);
    }

    public float z(int index) {
        return Float.intBitsToFloat(data[index * VERTEX_STRIDE_INT + Z_OFFSET]);
    }

    // 0xAABBGGRR
    public int color(int index) {
        return data[index * VERTEX_STRIDE_INT + COLOR_OFFSET];
    }

    public float u(int index) {
        return Float.intBitsToFloat(data[index * VERTEX_STRIDE_INT + U_OFFSET]);
    }

    public float v(int index) {
        return Float.intBitsToFloat(data[index * VERTEX_STRIDE_INT + V_OFFSET]);
    }

    public int overlay(int index) {
        return data[index * VERTEX_STRIDE_INT + OVERLAY_OFFSET];
    }

    @SuppressWarnings("unused")
    public int light(int index) {
        return data[index * VERTEX_STRIDE_INT + LIGHT_OFFSET];
    }

    public int normal(int index) {
        return data[index * VERTEX_STRIDE_INT + NORMAL_OFFSET];
    }

    public int vertexCount() {
        return vertexCount;
    }

    public boolean isEmpty() {
        return vertexCount == 0;
    }

}
