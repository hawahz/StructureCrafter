package io.github.hawah.structure_crafter.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class SableLogicTransformCompat {

    public static BlockPos applyTransform(final BlockPos pos) {
        return applyTransform(pos, pos);
    }

    public static BlockPos applyTransform(final BlockPos pos, final BlockPos source) {

        SubLevelAccess levelAccess = SableCompanion.INSTANCE.getContainingClient(source);
        if (levelAccess == null) {
            return pos;
        }

        return BlockPos.containing(levelAccess.logicalPose().transformPosition(pos.getCenter()));
    }

    public static Vec3 applyTransform(final Vec3 vec) {

        SubLevelAccess levelAccess = SableCompanion.INSTANCE.getContainingClient(vec);
        if (levelAccess == null) {
            return vec;
        }

        return levelAccess.logicalPose().transformPosition(vec);
    }
}
