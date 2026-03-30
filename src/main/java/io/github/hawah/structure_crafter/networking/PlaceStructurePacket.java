package io.github.hawah.structure_crafter.networking;

import io.github.hawah.structure_crafter.client.StructureData;
import io.github.hawah.structure_crafter.item.structure_wand.AbstractStructureWand;
import io.github.hawah.structure_crafter.client.handler.StructureWandHandler;
import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record PlaceStructurePacket(ItemStack stack, BlockPos pos, Direction direction) implements ServerboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceStructurePacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, PlaceStructurePacket::stack,
            BlockPos.STREAM_CODEC, PlaceStructurePacket::pos,
            Direction.STREAM_CODEC, PlaceStructurePacket::direction,
            PlaceStructurePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) {
            return;
        }

        Level level = player.level();

        StructureData activeTemplateData =
                AbstractStructureWand.loadSchematic(level, stack);
        StructureTemplate activeTemplate = activeTemplateData.structureTemplate();
        StructurePlaceSettings settings = new StructurePlaceSettings();
        Rotation rotation = StructureWandHandler.transferDirectionToRotation(direction());
        settings.setRotation(rotation);
        List<StructureTemplate.StructureBlockInfo> blockInfos = StructureTemplate.processBlockInfos(
                (ServerLevelAccessor) level,
                pos,
                BlockPos.ZERO,
                settings,
                settings.getRandomPalette(((StructureTemplateAccessor) activeTemplate).getPalettes(), BlockPos.ZERO).blocks(),
                activeTemplate
        );
        HashMap<Item, Integer> consumes = getNeededItems(blockInfos);
        int totalConsumes = consumes.values().stream().mapToInt(Integer::intValue).sum();
        HashMap<ItemStack, Integer> playerInventory = new HashMap<>();

        if (!player.isCreative()) {
            for (ItemStack item : player.getInventory().items) {
                if (item.isEmpty()) {
                    continue;
                }
                if (!consumes.containsKey(item.getItem())) {
                    continue;
                }
                int count = consumes.get(item.getItem());
                int consumeCounts = getMin(item, count);
                playerInventory.put(item, consumeCounts);
                if (count - consumeCounts <= 0) {
                    consumes.remove(item.getItem());
                } else {
                    consumes.put(item.getItem(), count - consumeCounts);
                }
            }

            if (!consumes.isEmpty()) {
                consumes.forEach((item, count) -> player.sendSystemMessage(Component.literal("Need item " + item.getDescriptionId() + ":" + count)));
                return;
            }
            playerInventory.forEach(ItemStack::shrink);
        }

//        StructureTemplate.StructureBlockInfo info = activeTemplate.processBlockInfos(level, )
        player.causeFoodExhaustion(totalConsumes * 0.1F);
        player.swing(InteractionHand.MAIN_HAND, true);
        settings.setIgnoreEntities(true);
        activeTemplate.placeInWorld(
                (ServerLevelAccessor) level,
                pos.subtract(activeTemplateData.center().rotate(rotation)),
                BlockPos.ZERO,
                settings,
                level.getRandom(),
                Block.UPDATE_ALL
        );
        SoundType sound = level.getBlockState(pos).getSoundType(level, pos, null);
        level.playSound(
                null,
                pos,
                sound.getPlaceSound(),
                SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F,
                sound.getPitch() * 0.8F
        );
    }

    private static int getMin(ItemStack item, int count) {
        return Math.min(count, item.getCount());
    }

    private static @NotNull HashMap<Item, Integer> getNeededItems(List<StructureTemplate.StructureBlockInfo> blockInfos) {
        HashMap<Item, Integer> consumes = new HashMap<>();
        for (StructureTemplate.StructureBlockInfo info : blockInfos) {
//            player.sendSystemMessage(
//                    Component.literal( info.state().getBlock() + " "+ info.pos().getX() + " " + info.pos().getY() + " " + info.pos().getZ())
//            );
            Block block = info.state().getBlock();
            ItemStack itemStack = new ItemStack(block);
            if (itemStack.isEmpty()) {
                continue;
            }

            Item item = getItem(itemStack);
            if (consumes.containsKey(item)) {
                consumes.put(item, consumes.get(item) + 1);
            } else {
                consumes.put(item, 1);
            }
        }
        return consumes;
    }

    private static @NotNull Item getItem(ItemStack itemStack) {
        return itemStack.getItem();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.PLACE_STRUCTURE;
    }
}
