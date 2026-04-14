package io.github.hawah.structure_crafter.client.render.ruler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.render.ColoredElement;
import io.github.hawah.structure_crafter.client.render.DoublePointElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public abstract class RulerElement<T extends RulerElement<T>> extends DoublePointElement<T> {

    protected boolean isManhattan = true;

    public T createManhattan(Vec3 start, Vec3 end) {
        init(start, end);
        this.isManhattan = true;
        return (T) this;
    }
    public T createStrait(Vec3 start, Vec3 end) {
        init(start, end);
        this.isManhattan = false;
        return (T) this;
    }

    private void init(Vec3 start, Vec3 end) {
        this.actualPos0 = start;
        this.actualPos1 = end;
        this.visualPos0 = start;
        this.visualPos1 = end;
        this.oPos0 = start;
        this.oPos1 = end;
    }

    public abstract void renderEdge(Matrix4f mat, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a);

    @Override
    public void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick) {
        float delta = partialTick.getGameTimeDeltaPartialTick(true);

        float cr = Mth.lerp(delta, or, r),
                cg = Mth.lerp(delta, og, g),
                cb = Mth.lerp(delta, ob, b),
                ca = Mth.lerp(delta, oa, a);
        Matrix4f mat = poseStack.last().pose();
        if (!isManhattan) {
            renderEdge(mat, buffer, (float) (actualPos0.x - cameraPos.x), (float) (actualPos0.y - cameraPos.y), (float) (actualPos0.z - cameraPos.z), (float) (actualPos1.x - cameraPos.x), (float) (actualPos1.y - cameraPos.y), (float) (actualPos1.z - cameraPos.z), cr, cg, cb, ca);
//            Minecraft.getInstance().font.drawInBatch(
//                    String.valueOf(actualPos0.subtract(actualPos1).length()),
//                    (float) (box.minX - cameraPos.x + box.getXsize() / 2 - Minecraft.getInstance().font.width(String.valueOf(Math.round(box.getXsize() * 100) / 100.0)) / 2),
//                    (float) (box.minY - cameraPos.y + box.getYsize() / 2 + Minecraft.getInstance().font.lineHeight / 2),
//                    )
            return;
        }
        AABB box = new AABB(actualPos0, actualPos1);//.inflate(0.002 * (1 + priority));
        float xMin = (float) (box.minX - cameraPos.x);
        float yMin = (float) (box.minY - cameraPos.y);
        float zMin = (float) (box.minZ - cameraPos.z);
        float xMax = (float) (box.maxX - cameraPos.x);
        float yMax = (float) (box.maxY - cameraPos.y);
        float zMax = (float) (box.maxZ - cameraPos.z);

    }
}
