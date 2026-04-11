package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.item.structure_wand.AbstractStructureWand;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.StructureHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record HandholdItemChangePacket(ItemStack stack) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, HandholdItemChangePacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, HandholdItemChangePacket::stack,
            HandholdItemChangePacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(stack.getItem()))
            return;
        player.getItemInHand(InteractionHand.MAIN_HAND).applyComponents(stack.getComponents());

        StructureHandler.checkFileExists(player, stack);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.HANDHOLD_ITEM_CHANGED;
    }
}
