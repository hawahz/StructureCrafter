package io.github.hawah.structure_crafter.client.render.ruler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.client.render.DoublePointElement;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

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
        Vec3 start = oPos0.lerp(visualPos0, delta);
        Vec3 end = oPos1.lerp(visualPos1, delta);

        double lenX = Math.abs(end.x - start.x);
        double lenY = Math.abs(end.y - start.y);
        double lenZ = Math.abs(end.z - start.z);

        // === 1️⃣ 计算 delta ===
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;

        // === 2️⃣ 按轴长度排序（长轴优先） ===
        List<Integer> axes = new ArrayList<>();
        axes.add(0); // X
        axes.add(1); // Y
        axes.add(2); // Z

        axes.sort((a, b) -> {
            double da = (a == 0 ? Math.abs(dx) : a == 1 ? Math.abs(dy) : Math.abs(dz));
            double db = (b == 0 ? Math.abs(dx) : b == 1 ? Math.abs(dy) : Math.abs(dz));
            return Double.compare(da, db); // 大的在前
        });

        // === 3️⃣ 构造路径点（逐轴逼近 end） ===
        Vec3[] points = new Vec3[4];
        points[0] = start;

        double cx = start.x;
        double cy = start.y;
        double cz = start.z;

        for (int i = 0; i < 3; i++) {
            int axis = axes.get(i);

            if (axis == 0) cx = end.x;
            else if (axis == 1) cy = end.y;
            else cz = end.z;

            points[i + 1] = new Vec3(cx, cy, cz);
        }

        // === 4️⃣ 转换到相机坐标 ===
        float[][] v = new float[4][3];
        for (int i = 0; i < 4; i++) {
            v[i][0] = (float)(points[i].x - cameraPos.x);
            v[i][1] = (float)(points[i].y - cameraPos.y);
            v[i][2] = (float)(points[i].z - cameraPos.z);
        }

        // === 5️⃣ 渲染三段（按轴自动上色） ===
        for (int i = 0; i < 3; i++) {
            renderEdge(
                    mat, buffer,
                    v[i][0], v[i][1], v[i][2],
                    v[i+1][0], v[i+1][1], v[i+1][2],
                    cr, cg, cb, ca
            );
        }

        // === 4️⃣ （可选）计算每段中点（用于文字） ===
        // === 4️⃣ 计算每段中点（适配长轴优先） ===
        Vec3 midX = null, midY = null, midZ = null;

        for (int i = 0; i < 3; i++) {
            Vec3 mid = points[i].add(points[i + 1]).scale(0.5);
            int axis = axes.get(i);

            if (axis == 0) midX = mid;
            else if (axis == 1) midY = mid;
            else midZ = mid;
        }

        // === 5️⃣ （可选）长度 ===


        poseStack.pushPose();

        Minecraft mc = Minecraft.getInstance();
        if (lenX > 0.1) {
            drawString(poseStack, String.valueOf(Math.round(lenX * 100) / 100.0), mc, cameraPos, midX, 0,
                    (int) cr*255,
                    (int) cg*255,
                    (int) cb*255,
                    (int) ca*255);
        }
        if (lenY > 0.1) {
            drawString(poseStack, String.valueOf(Math.round(lenY * 100) / 100.0), mc, cameraPos, midY, 1,
                    (int) cr*255,
                    (int) cg*255,
                    (int) cb*255,
                    (int) ca*255);
        }
        if (lenZ > 0.1) {
            drawString(poseStack, String.valueOf(Math.round(lenZ * 100) / 100.0), mc, cameraPos, midZ, 2,
                    (int) cr*255,
                    (int) cg*255,
                    (int) cb*255,
                    (int) ca*255);
        }
        poseStack.popPose();

        return;

    }

    private static void drawString(PoseStack poseStack,
                                   String text,
                                   Minecraft mc,
                                   Vec3 cameraPos,
                                   Vec3 position,
                                   int type,
                                   int r,
                                   int g,
                                   int b,
                                   int a) {
        Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!mainCamera.isInitialized())
            return;
        Vec3 upVector = Minecraft.getInstance().player.getUpVector(AnimationTickHolder.getPartialTicks());
        float scale = 0.05F;
        Font font = mc.font;
        double d0 = cameraPos.x;
        double d1 = cameraPos.y;
        double d2 = cameraPos.z;
        poseStack.pushPose();
        poseStack.translate((float)(position.x - d0), (float)(position.y - d1), (float)(position.z - d2));
        poseStack.mulPose(switch (type) {
            case 0 -> {
                double angle = position.subtract(new Vec3(position.x, cameraPos.y, cameraPos.z)).normalize().dot(new Vec3(0, 0, -1));
                if (position.y() <= cameraPos.y()) {
                    if (angle <= 0.5 && upVector.dot(new Vec3(0, 0, -1)) > 0) {
                        yield Axis.XN.rotation((float) (Math.PI / 2));
                    } else if (angle >= 0.5) {
                        yield Axis.YN.rotation(0);
                    } else if (angle <= -0.5) {
                        yield Axis.YN.rotation((float) Math.PI);
                    } else {
                        yield Axis.YN.rotation((float) Math.PI).mul(Axis.XN.rotation((float) (Math.PI / 2)));
                    }
                } else {
                    if (angle <= 0.5 && upVector.dot(new Vec3(0, 0, -1)) < 0) {
                        yield Axis.XN.rotation((float) (3 * Math.PI / 2));
                    } else if (angle >= 0.5) {
                        yield Axis.YN.rotation(0);
                    } else if (angle <= -0.5) {
                        yield Axis.YN.rotation((float) Math.PI);
                    }
                    yield Axis.YN.rotation((float) Math.PI).mul(Axis.XP.rotation((float) (Math.PI / 2)));

                }
            }
            case 1 -> {
                double angle0 = position.subtract(cameraPos).normalize().dot(new Vec3(0, 0, -1));
                double angle1 = position.subtract(cameraPos).normalize().dot(new Vec3(1, 0, 0));
                if (angle0 > 0) {
                    yield Axis.ZN.rotation((float) (Math.PI / 2));
                } else if (cameraPos.x() < position.x() && cameraPos.z() > position.z()) {
                    yield Axis.ZN.rotation((float) (Math.PI / 2)).mul(Axis.XP.rotation((float) (Math.PI / 2)));
                } else if (cameraPos.x() > position.x() && cameraPos.z() < position.z()) {
                    yield Axis.ZN.rotation((float) (Math.PI / 2)).mul(Axis.XP.rotation((float) (3 * Math.PI / 2)));
                } else{
                    yield Axis.ZN.rotation((float) (Math.PI / 2)).mul(Axis.XP.rotation((float) (Math.PI)));
                }
            }
            case 2 -> {
                double angle = position.subtract(cameraPos).normalize().dot(new Vec3(1, 0, 0));
                if (position.y() <= cameraPos.y()) {
                    if (angle <= 0.8 && upVector.dot(new Vec3(1, 0, 0)) > 0) {
                        yield Axis.YN.rotation((float) (Math.PI / 2)).mul(Axis.XN.rotation((float) (Math.PI / 2)));
                    } else if (angle >= 0.8) {
                        yield Axis.YN.rotation((float) (Math.PI / 2));
                    } else if (angle <= -0.8) {
                        yield Axis.YP.rotation((float) (Math.PI / 2));
                    } else {
                        yield Axis.YP.rotation((float) (Math.PI / 2)).mul(Axis.XN.rotation((float) (Math.PI / 2)));
                    }
                } else {
                    if (angle <= 0.8 && upVector.dot(new Vec3(1, 0, 0)) < 0) {
                        yield Axis.YN.rotation((float) (Math.PI / 2)).mul(Axis.XP.rotation((float) (Math.PI / 2)));
                    } else if (angle >= 0.8) {
                        yield Axis.YN.rotation((float) (Math.PI / 2));
                    } else if (angle <= -0.8) {
                        yield Axis.YP.rotation((float) (Math.PI / 2));
                    } else {
                        yield Axis.YP.rotation((float) (Math.PI / 2)).mul(Axis.XP.rotation((float) (Math.PI / 2)));
                    }
                }

            }

            default -> Axis.YN.rotation(0);
        });
        if (type == 4) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }
        poseStack.translate(0, font.lineHeight * scale, 0);
        poseStack.scale(scale, -scale, scale);

        boolean middle = true, transparent = true;
        float f = middle ? (float)(-font.width(text)) / 2.0F : 0.0F;
        f -= 0 / scale;
        font.drawInBatch(
                text,
                f,
                0.0F,
                a << 24 | r << 16 | g << 8 | b,
                false,
                poseStack.last().pose(),
                mc.renderBuffers().bufferSource(),
                transparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                0,
                15728880
        );
        poseStack.popPose();
    }
}
