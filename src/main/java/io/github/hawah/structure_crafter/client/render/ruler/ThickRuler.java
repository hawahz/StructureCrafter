package io.github.hawah.structure_crafter.client.render.ruler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.client.render.outliner.ThickOutline;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class ThickRuler extends RulerElement<ThickRuler>{
    private float visualThickness = 0.0F, actualThickness;

    public ThickRuler() {
        actualThickness = 0.06F;
    }

    @Override
    public void renderEdge(Matrix4f mat, VertexConsumer buffer, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax, float r, float g, float b, float a) {
        if (isManhattan) {
            drawBox(mat, buffer, xMin - visualThickness/2, yMin - visualThickness/2, zMin - visualThickness/2, xMax + visualThickness/2, yMax + visualThickness/2, zMax + visualThickness/2, r, g, b, a); // 北西角
            return;
        }
    }

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
    public ThickRuler setThickness(float actualThickness) {
        this.actualThickness = actualThickness;
        return this;
    }
    public ThickRuler setThicknessForcefully(float thickness) {
        this.actualThickness = thickness;
        this.visualThickness = thickness;
        return this;
    }
}
