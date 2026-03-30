package io.github.hawah.structure_crafter.client.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FineOutline extends OutlineElement {
    /**
     * 渲染选框
     * @param poseStack  变换矩阵栈
     * @param buffer     顶点消费者 (通常建议使用 MultiBufferSource.getBuffer(RenderType.lines()))
     * @param cameraPos  当前摄像机坐标 (用于平移到相对坐标)
     */
    @Override
    public void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick) {

        // 构建 AABB 范围，确保 pos0 和 pos1 的大小关系正确
        AABB box = new AABB(visualPos0, visualPos1).inflate(0.002); // 稍微膨胀一点防止与方块表面闪烁 (Z-Fighting)

        poseStack.pushPose();

        // 平移到相对于摄像机的坐标
        poseStack.translate(box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z);

        float width = (float) box.getXsize();
        float height = (float) box.getYsize();
        float depth = (float) box.getZsize();

        // 使用 LevelRenderer 的内置方法渲染线框
        // 注意：原生的线宽调节在很多现代 GPU 上受限，通常取决于 RenderType
        LevelRenderer.renderLineBox(
                poseStack,
                buffer,
                0,
                0,
                0,
                width,
                height,
                depth,
                r,
                g,
                b,
                a
        );

        poseStack.popPose();
    }
}
