package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.item.RulerItem;
import io.github.hawah.structure_crafter.lib.networking.ClientToServerPacket;
import io.github.hawah.structure_crafter.lib.RaycastHelper;
import io.github.hawah.structure_crafter.lib.StreamCodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public record ClientboundTryPlaceBlockFixedPacket(
        Vec3 start,
        Vec3 dir,
        double range,
        InteractionHand hand,
        ItemStack itemStack,
        BlockPos anchor
) implements ClientToServerPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTryPlaceBlockFixedPacket> STREAM_CODEC = StreamCodec.composite(
            StreamCodecUtil.VEC3, ClientboundTryPlaceBlockFixedPacket::start,
            StreamCodecUtil.VEC3, ClientboundTryPlaceBlockFixedPacket::dir,
            StreamCodecUtil.DOUBLE, ClientboundTryPlaceBlockFixedPacket::range,
            StreamCodecUtil.ofEnum(InteractionHand.class), ClientboundTryPlaceBlockFixedPacket::hand,
            ItemStack.STREAM_CODEC, ClientboundTryPlaceBlockFixedPacket::itemStack,
            BlockPos.STREAM_CODEC, ClientboundTryPlaceBlockFixedPacket::anchor,
            ClientboundTryPlaceBlockFixedPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        BlockHitResult hitResult = RaycastHelper.rayTraceRange(player.level(), start(), dir(), range());
        if (hitResult.getType() == BlockHitResult.Type.BLOCK && itemStack.getItem() instanceof BlockItem item) {
            BlockPlaceContext context = new BlockPlaceContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    player.getMainHandItem(),
                    hitResult
            );
            BlockPos.MutableBlockPos pos = RulerItem.modifyFixed(context, anchor());
            for (int i = 0; i < 3; i++) {
                if (player.level().getBlockState(pos).canBeReplaced()) {
                    item.place(BlockPlaceContext.at(
                            context,
                            pos,
                            hitResult.getDirection()
                    ));
                    player.swing(InteractionHand.MAIN_HAND);
                    break;
                }
                pos.set(pos.relative(hitResult.getDirection()));
            }

        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.TRY_PLACE_BLOCK_FIXED;
    }
}
