package io.github.hawah.structure_crafter.compat.sable;

import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SableLogicTransformCompat {

    public static BlockPos applyTransform(final BlockPos local) {
        return applyTransform(local, local);
    }

    public static BlockPos applyTransform(final BlockPos local, final BlockPos source) {

        SubLevelAccess levelAccess = SableCompanion.INSTANCE.getContainingClient(source);
        if (levelAccess == null) {
            return local;
        }

        return BlockPos.containing(levelAccess.logicalPose().transformPosition(local.getCenter()));
    }

    public static Vec3 applyTransform(final Vec3 local) {

        return applyTransform(local, local);
    }

    public static Vec3 applyTransform(final Vec3 local, Vec3 source) {

        if (source == null) {
            source = local;
        }

        SubLevelAccess levelAccess = SableCompanion.INSTANCE.getContainingClient(source);
        if (levelAccess == null) {
            return local;
        }

        return levelAccess.logicalPose().transformPosition(local);
    }

    public static Vec3 applyTransformInverse(final Vec3 global, Vec3 source) {

        if (source == null) {
            source = global;
        }

        SubLevelAccess levelAccess = SableCompanion.INSTANCE.getContainingClient(source);
        if (levelAccess == null) {
            return global;
        }

        return levelAccess.logicalPose().transformPositionInverse(global);
    }

    public static void transformRayIntersectData(Vec3 from, Vec3 direction, List<Vec3> dataHolder, Vec3 center) {
        ClientSubLevelAccess containingClient = SableCompanion.INSTANCE.getContainingClient(center);

        if (containingClient != null) {
            dataHolder.set(0, containingClient.logicalPose().transformPositionInverse(from));
            dataHolder.set(1, containingClient.logicalPose().transformPositionInverse(direction));
        }
    }

    public static void applyReverseAreaTotalTransform(BlockPos secondPos, List<BlockPos> resultHolder) {
        if (resultHolder.getFirst() != null){
            ClientSubLevelAccess containingClient = SableCompanion.INSTANCE.getContainingClient(resultHolder.getFirst());
            if (containingClient != null && secondPos != null && resultHolder.getFirst().distSqr(secondPos) > 100000) {
                Pose3dc pose3dc = containingClient.logicalPose();
                BoundingBox3dc boundingBox3dc = containingClient.boundingBox();
                boundingBox3dc.transformInverse(pose3dc, (BoundingBox3d) boundingBox3dc);
                resultHolder.set(0, BlockPos.containing((boundingBox3dc.toMojang().getMinPosition())));
                resultHolder.set(1, BlockPos.containing((boundingBox3dc.toMojang().getMaxPosition())));
            }
        }
    }
}
