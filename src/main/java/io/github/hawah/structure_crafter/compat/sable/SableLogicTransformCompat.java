package io.github.hawah.structure_crafter.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SableLogicTransformCompat{

    public static SableLogicTransformCompat instance() {
        return new SableLogicTransformCompat();
    }

    private Level level;

    public BlockPos applyTransform(final BlockPos local) {
        return applyTransform(local, local);
    }

    public BlockPos applyTransform(final BlockPos local, final BlockPos source) {

        SubLevelAccess levelAccess = getContaining(source);
        if (levelAccess == null) {
            return local;
        }

        return BlockPos.containing(levelAccess.logicalPose().transformPosition(local.getCenter()));
    }

    public Vec3 applyTransform(final Vec3 local) {

        return applyTransform(local, local);
    }

    public Vec3 applyTransform(final Vec3 local, Vec3 source) {

        if (source == null) {
            source = local;
        }

        SubLevelAccess levelAccess = getContaining(source);
        if (levelAccess == null) {
            return local;
        }

        return levelAccess.logicalPose().transformPosition(local);
    }

    public Vec3 applyTransformInverse(final Vec3 global, Vec3 source) {

        if (source == null) {
            source = global;
        }

        SubLevelAccess levelAccess = getContaining(source);
        if (levelAccess == null) {
            return global;
        }

        return levelAccess.logicalPose().transformPositionInverse(global);
    }

    public BlockPos applyTransformInverse(final BlockPos global, BlockPos source) {

        if (source == null) {
            source = global;
        }

        SubLevelAccess levelAccess = getContaining(source);
        if (levelAccess == null) {
            return global;
        }

        return BlockPos.containing(levelAccess.logicalPose().transformPositionInverse((global).getCenter()));
    }

    public boolean isPhysical(final Vec3 pos) {
        if (pos == null)
            return false;
        SubLevelAccess levelAccess = getContaining(pos);
        return levelAccess != null;
    }
    public boolean isPhysical(final BlockPos pos) {
        if (pos == null)
            return false;
        SubLevelAccess levelAccess = getContaining(pos);
        return levelAccess != null;
    }

    public boolean isSameSide(final Vec3 pos1, final Vec3 pos2) {
        SubLevelAccess side1 = getContaining(pos1);
        SubLevelAccess side2 = getContaining(pos2);
        if (side1==null && side2 == null) {
            return true;
        }

        if ((side2 == null) || (side1 == null)) {
            return false;
        }

        return side1.getUniqueId().equals(side2.getUniqueId());
    }

    public boolean isSameSide(final BlockPos pos1, final BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            return true;
        }
        return isSameSide(pos1.getCenter(), pos2.getCenter());
    }


    public void transformRayIntersectData(Vec3 from, Vec3 direction, List<Vec3> dataHolder, Vec3 center) {
        SubLevelAccess containingClient = getContaining(center);

        if (containingClient != null) {
            dataHolder.set(0, containingClient.logicalPose().transformPositionInverse(from));
            dataHolder.set(1, containingClient.logicalPose().transformPositionInverse(direction));
        }
    }

    public void applyReverseAreaTotalTransform(BlockPos secondPos, List<BlockPos> resultHolder) {
        if (resultHolder.getFirst() != null){
            SubLevelAccess containingClient = getContaining(resultHolder.getFirst());
            if (containingClient != null && secondPos != null && resultHolder.getFirst().distSqr(secondPos) > 100000) {
                Pose3dc pose3dc = containingClient.logicalPose();
                BoundingBox3dc boundingBox3dc = containingClient.boundingBox();
                boundingBox3dc.transformInverse(pose3dc, (BoundingBox3d) boundingBox3dc);
                resultHolder.set(0, BlockPos.containing((boundingBox3dc.toMojang().getMinPosition())));
                resultHolder.set(1, BlockPos.containing((boundingBox3dc.toMojang().getMaxPosition())));
            }
        }
    }

    public SubLevelAccess getContaining(BlockPos pos) {
        if (level != null) {
            return SableCompanion.INSTANCE.getContaining(level, pos);
        } else {
            return SableCompanion.INSTANCE.getContainingClient(pos);
        }
    }

    public SubLevelAccess getContaining(Vec3 pos) {
        if (level != null) {
            return SableCompanion.INSTANCE.getContaining(level, pos);
        } else {
            return SableCompanion.INSTANCE.getContainingClient(pos);
        }
    }

    public SableLogicTransformCompat level(Level level) {
        this.level = level;
        return this;
    }
}
