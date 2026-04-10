package io.github.hawah.structure_crafter.client;

import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClientDataHolder {

    public static void tick() {
        Picker.tick();
    }

    public static class Picker {
        public static HitResult hitResult = null;
        public static BlockPos pos() {
            if (hitResult == null)
                return null;
            return hitResult instanceof BlockHitResult blockHitResult ?
                    blockHitResult.getBlockPos() :
                    BlockPos.containing(hitResult.getLocation());
        }

        public static Vec3 location() {
            if (hitResult == null)
                return null;
            return hitResult.getLocation();
        }

        public static Direction direction() {
            if (hitResult == null)
                return null;
            return hitResult instanceof BlockHitResult blockHitResult ?
                    blockHitResult.getDirection() :
                    Direction.fromYRot(hitResult.getLocation().y);
        }

        public static void tick() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            if (!player.getMainHandItem().is(ItemRegistries.TELEPHONE_HANDSET)) {
                return;
            }

            hitResult = player.pick(4.5D, 0.0F, false);
        }
    }
}
