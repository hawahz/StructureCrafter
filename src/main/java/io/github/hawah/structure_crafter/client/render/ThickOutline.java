package io.github.hawah.structure_crafter.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ThickOutline extends OutlineElement {
    private float visualThickness = 0.0F, actualThickness;

    public ThickOutline() {
        actualThickness = 0.06F;
    }

    /**
     * 渲染向内延伸且角整齐的实体边框
     * @param poseStack 变换矩阵栈
     * @param cameraPos 当前摄像机坐标 (用于平移到相对坐标)
     * @param buffer    顶点消费者，建议使用 RenderType.entityTranslucentCull() 或 RenderType.debugQuads()
     */
    @Override
    public void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick) {

        // 构建 AABB 范围，确保 pos0 和 pos1 的大小关系正确
        AABB box = new AABB(
                oPos0.lerp(visualPos0, partialTick.getGameTimeDeltaPartialTick(true)),
                oPos1.lerp(visualPos1, partialTick.getGameTimeDeltaPartialTick(true))
        ).inflate(0.002 * (1 + priority)); // 稍微膨胀一点防止与方块表面闪烁

        float cr = Mth.lerp(partialTick.getGameTimeDeltaPartialTick(true), or, r),
                cg = Mth.lerp(partialTick.getGameTimeDeltaPartialTick(true), og, g),
                cb = Mth.lerp(partialTick.getGameTimeDeltaPartialTick(true), ob, b),
                ca = Mth.lerp(partialTick.getGameTimeDeltaPartialTick(true), oa, a);

        // 计算相对于摄像机的 AABB 边界
        float xMin = (float) (box.minX - cameraPos.x);
        float yMin = (float) (box.minY - cameraPos.y);
        float zMin = (float) (box.minZ - cameraPos.z);
        float xMax = (float) (box.maxX - cameraPos.x);
        float yMax = (float) (box.maxY - cameraPos.y);

        float zMax = (float) (box.maxZ - cameraPos.z);

        Matrix4f mat = poseStack.last().pose();

        // --- 12条边，每条边都是一个完整的长方体，且完全向内延伸 ---

        // --- 4条沿 Y 轴的边 (竖柱) ---
        // 它们占据 AABB 角落的 X-Z 空间，高度为全长
        drawBox(mat, buffer, xMin, yMin, zMin, xMin + visualThickness, yMax, zMin + visualThickness, cr, cg, cb, ca); // 北西角
        drawBox(mat, buffer, xMax - visualThickness, yMin, zMin, xMax, yMax, zMin + visualThickness, cr, cg, cb, ca); // 北东角
        drawBox(mat, buffer, xMin, yMin, zMax - visualThickness, xMin + visualThickness, yMax, zMax, cr, cg, cb, ca); // 南西角
        drawBox(mat, buffer, xMax - visualThickness, yMin, zMax - visualThickness, xMax, yMax, zMax, cr, cg, cb, ca); // 南东角

        // --- 4条沿 X 轴的边 (横梁) ---
        // 它们紧贴 AABB 的 Y-Z 边缘，长度为全长
        drawBox(mat, buffer, xMin, yMin, zMin, xMax, yMin + visualThickness, zMin + visualThickness, cr, cg, cb, ca); // 底北边
        drawBox(mat, buffer, xMin, yMax - visualThickness, zMin, xMax, yMax, zMin + visualThickness, cr, cg, cb, ca); // 顶北边
        drawBox(mat, buffer, xMin, yMin, zMax - visualThickness, xMax, yMin + visualThickness, zMax, cr, cg, cb, ca); // 底南边
        drawBox(mat, buffer, xMin, yMax - visualThickness, zMax - visualThickness, xMax, yMax, zMax, cr, cg, cb, ca); // 顶南边

        // --- 4条沿 Z 轴的边 (纵梁) ---
        // 它们紧贴 AABB 的 X-Y 边缘，长度为全长
        drawBox(mat, buffer, xMin, yMin, zMin, xMin + visualThickness, yMin + visualThickness, zMax, cr, cg, cb, ca); // 底西边
        drawBox(mat, buffer, xMax - visualThickness, yMin, zMin, xMax, yMin + visualThickness, zMax, cr, cg, cb, ca); // 底东边
        drawBox(mat, buffer, xMin, yMax - visualThickness, zMin, xMin + visualThickness, yMax, zMax, cr, cg, cb, ca); // 顶西边
        drawBox(mat, buffer, xMax - visualThickness, yMax - visualThickness, zMin, xMax, yMax, zMax, cr, cg, cb, ca); // 顶东边
    }

    /**
     * 绘制一个标准的、具有 6 个面的实体长方体
     */
    private void drawBox(Matrix4f matrix, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        // 下面 (Bottom)
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a);

        // 上面 (Top)
        buffer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a);

        // 北面 (North / -Z)
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a);

        // 南面 (South / +Z)
        buffer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a);

        // 西面 (West / -X)
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a);

        // 东面 (East / +X)
        buffer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a);
    }

    public void tick() {
        super.tick();
        this.visualThickness = Mth.lerp(visualThickness, actualThickness, 0.2F);
    }
    public void setThickness(float actualThickness) {
        this.actualThickness = actualThickness;
    }
    public void setThicknessForcefully(float thickness) {
        this.actualThickness = thickness;
        this.visualThickness = thickness;
    }
}
