package io.github.hawah.structure_crafter.compat.sable;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

public class RenderCompat {

    public static AABB applyTransform(AABB box, PoseStack poseStack, Vec3 cameraPos, Vec3 center, float delta) {
        ClientSubLevelAccess containingClient = SableCompanion.INSTANCE.getContainingClient(center);

        //poseStack.mulPose(Axis.YP.rotationDegrees(AnimationTickHolder.getTicks()));

        if (containingClient != null) {

            final Pose3dc pose = containingClient.renderPose(delta);
            Vec3 vec = JOMLConversion.toMojang(pose.rotationPoint()).scale(-1);
            box = box.move(vec);

            final Vector3dc pos = pose.position();
            final Vector3dc scale = pose.scale();
            final Quaterniondc orientation = pose.orientation();

            Matrix4d matrix4d = new Matrix4d();
            pose.bakeIntoMatrix(matrix4d);

            poseStack.translate(pos.x() - cameraPos.x, pos.y() - cameraPos.y, pos.z() - cameraPos.z);
            poseStack.mulPose(new Quaternionf(orientation));

            poseStack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());

            poseStack.translate(cameraPos.x, cameraPos.y, cameraPos.z);
            //            poseStack.mulPose(new Matrix4f(matrix4d));
        }

        return box;
    }

    public static Vec3 applyTransform(PoseStack poseStack, Vec3 camera, BlockPos anchorPos, float partialTicks, Vec3 offset) {
        ClientSubLevelAccess containingClient = SableCompanion.INSTANCE.getContainingClient(anchorPos);

        if (containingClient != null) {
            final Pose3dc pose = containingClient.renderPose(partialTicks);
            Vector3dc rotationPoint = pose.rotationPoint();
            Vec3 vec = JOMLConversion.toMojang(rotationPoint).scale(-1);
            offset = offset.add(vec);



            final Vector3dc pos = pose.position();
            final Vector3dc scale = pose.scale();
            final Quaterniondc orientation = pose.orientation();

            Matrix4d matrix4d = new Matrix4d();
            pose.bakeIntoMatrix(matrix4d);

            poseStack.translate(pos.x() - camera.x, pos.y() - camera.y, pos.z() - camera.z);
            poseStack.mulPose(new Quaternionf(orientation));

            poseStack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());

            poseStack.translate(camera.x, camera.y, camera.z);
        }
        return offset;
    }
}
