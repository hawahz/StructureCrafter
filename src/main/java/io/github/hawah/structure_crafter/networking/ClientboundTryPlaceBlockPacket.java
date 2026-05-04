package io.github.hawah.structure_crafter.networking;

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

public record ClientboundTryPlaceBlockPacket(
        Vec3 start,
        Vec3 dir,
        double range,
        InteractionHand hand,
        ItemStack itemStack
) implements ClientToServerPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTryPlaceBlockPacket> STREAM_CODEC = StreamCodec.composite(
            StreamCodecUtil.VEC3, ClientboundTryPlaceBlockPacket::start,
            StreamCodecUtil.VEC3, ClientboundTryPlaceBlockPacket::dir,
            StreamCodecUtil.DOUBLE, ClientboundTryPlaceBlockPacket::range,
            StreamCodecUtil.ofEnum(InteractionHand.class), ClientboundTryPlaceBlockPacket::hand,
            ItemStack.STREAM_CODEC, ClientboundTryPlaceBlockPacket::itemStack,
            ClientboundTryPlaceBlockPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        BlockHitResult hitResult = RaycastHelper.rayTraceRange(player.level(), start(), dir(), range());
        if (hitResult.getType() != BlockHitResult.Type.BLOCK || !(itemStack.getItem() instanceof BlockItem item)) {
            return;
        }

        BlockPlaceContext context = new BlockPlaceContext(
                player,
                InteractionHand.MAIN_HAND,
                player.getMainHandItem(),
                hitResult
        );
        BlockPos pos = context.getClickedPos();
        if (!player.level().getBlockState(pos).canBeReplaced()) {
            return;
        }
        item.place(BlockPlaceContext.at(
                context,
                pos,
                hitResult.getDirection()
        ));
        player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.TRY_PLACE_BLOCK;
    }
}
