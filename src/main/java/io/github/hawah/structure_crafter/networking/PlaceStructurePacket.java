package io.github.hawah.structure_crafter.networking;

import com.mojang.blaze3d.resource.ResourceHandle;
import io.github.hawah.structure_crafter.client.handler.StructureHandler;
import io.github.hawah.structure_crafter.client.utils.StructureData;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.item.structure_wand.AbstractStructureWand;
import io.github.hawah.structure_crafter.client.handler.StructureWandHandler;
import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;

public record PlaceStructurePacket(ItemStack stack, BlockPos pos, Direction direction) implements ClientToServerPacket {
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

        int updateFlags = AbstractStructureWand.getUpdateFlags(stack);
        boolean replaceAir = AbstractStructureWand.isReplaceAir(stack);

        Level level = player.level();

        StructureData activeTemplateData =
                AbstractStructureWand.loadSchematic(level, stack);
        assert activeTemplateData != null;

        StructureTemplate activeTemplate = activeTemplateData.structureTemplate();
        StructurePlaceSettings settings = new StructurePlaceSettings();
        Rotation rotation = StructureWandHandler.transferDirectionToRotation(direction());
        settings.setRotation(rotation);
        settings.setIgnoreEntities(true);
        List<StructureTemplate.StructureBlockInfo> blockInfos = StructureTemplate.processBlockInfos(
                (ServerLevelAccessor) level,
                pos,
                BlockPos.ZERO,
                settings,
                settings.getRandomPalette(((StructureTemplateAccessor) activeTemplate).getPalettes(), BlockPos.ZERO).blocks(),
                activeTemplate
        );

        HashMap<Item, Integer> consumes = StructureHandler.getNeededItems(blockInfos);
        int totalConsumes = consumes.values().stream().mapToInt(Integer::intValue).sum();


        if (!player.isCreative() && !canPlaceStructure(player, consumes, totalConsumes)) {
            return;
        }

//        StructureTemplate.StructureBlockInfo info = activeTemplate.processBlockInfos(level, )
        player.swing(InteractionHand.MAIN_HAND, true);

        HashMap<BlockPos, BlockState> invalidBlocks = detectOrReplaceAir(activeTemplate, settings, activeTemplateData, rotation, level, updateFlags, replaceAir);

        activeTemplate.placeInWorld(
                (ServerLevelAccessor) level,
                pos.subtract(activeTemplateData.center().rotate(rotation)),
                BlockPos.ZERO,
                settings,
                level.getRandom(),
                updateFlags
        );

        invalidBlocks.forEach((pos, block) -> level.setBlock(pos, block, updateFlags));

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

    private static boolean canPlaceStructure(ServerPlayer player, HashMap<Item, Integer> consumes, int totalConsumes) {
        HashMap<ItemStack, Integer> playerInventory = new HashMap<>();
        HashMap<ResourceHandler<ItemResource>, HashMap<Integer, Integer>> itemHandlerMap = new HashMap<>();

        NonNullList<ItemStack> items = StructureHandler.getInventoryItems(player);

        for (ItemStack item : items) {
            if (item.isEmpty()) {
                continue;
            }
            ResourceHandler<ItemResource> handler;
            if ((handler = item.getCapability(Capabilities.Item.ITEM, null)) != null) {
                itemHandlerMap.put(handler, new HashMap<>());
                for (int i = 0; i < handler.size(); i++) {
                    shrinkIfMatch(itemHandlerMap.getOrDefault(handler, new HashMap<>()), consumes, handler, i);
                }
            } else if (item.has(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) {
                TelephoneHandsetComponent handsetComponent = item.get(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE);
                BlockPos sourcePos = handsetComponent.pos();
                ResourceKey<Level> dimension = handsetComponent.dimension();
                handler = player.level()
                        .getServer()
                        .getLevel(dimension)
                        .getCapability(Capabilities.Item.BLOCK, sourcePos, Direction.NORTH);
                itemHandlerMap.put(handler, new HashMap<>());
                for (int i = 0; i < handler.size(); i++) {
                    shrinkIfMatch(itemHandlerMap.getOrDefault(handler, new HashMap<>()), consumes, handler, i);
                }
            }
            shrinkIfMatch(playerInventory, consumes, item);
        }

        if (!consumes.isEmpty()) {
            if (consumes.size() > 4) {
                player.displayClientMessage(LangData.WARN_STRUCTURE_WAND_NOT_ENOUGH_ITEM_TOO_LONG.get(), false);
                return false;
            }
            consumes.forEach((item, count) -> player.displayClientMessage(LangData.WARN_STRUCTURE_WAND_NOT_ENOUGH_ITEM.get(
                    count, Component.translatable(item.getDescriptionId())
            ), false));
            return false;
        }
        playerInventory.forEach(ItemStack::shrink);
        itemHandlerMap.forEach((handler, map) -> map.forEach((slot, count) -> handler.extract(slot, handler.getResource(slot), count, Transaction.openRoot())));
        player.causeFoodExhaustion(totalConsumes * 0.1F);
        return true;
    }

    /**
     * 检查并减少匹配的物品消耗数量
     * 如果槽位物品在消耗列表中，则从玩家库存和消耗列表中扣除相应数量
     *
     * @param playerInventory 玩家库存映射，键为物品堆，方式为剩余数量
     * @param consumes 需要消耗的物品映射，键为物品类型，值为所需数量
     * @param slotItem 要检查的槽位物品堆
     */
    private static void shrinkIfMatch(HashMap<ItemStack, Integer> playerInventory,
                                      HashMap<Item, Integer> consumes,
                                      ItemStack slotItem) {
        if (!consumes.containsKey(slotItem.getItem())) {
            return;
        }
        int count = consumes.get(slotItem.getItem());
        int consumeCounts = getMin(slotItem, count);
        playerInventory.put(slotItem, consumeCounts);
        if (count - consumeCounts <= 0) {
            consumes.remove(slotItem.getItem());
        } else {
            consumes.put(slotItem.getItem(), count - consumeCounts);
        }
    }

    private static void shrinkIfMatch(HashMap<Integer, Integer> slotToCount,
                                      HashMap<Item, Integer> consumes,
                                      ResourceHandler<ItemResource> handler,
                                      int slot) {
        ItemStack slotItem = handler.getResource(slot).toStack();
        if (!consumes.containsKey(slotItem.getItem())) {
            return;
        }
        int count = consumes.get(slotItem.getItem());
        int consumeCounts = getMin(slotItem, count);
        slotToCount.put(slot, consumeCounts);
        if (count - consumeCounts <= 0) {
            consumes.remove(slotItem.getItem());
        } else {
            consumes.put(slotItem.getItem(), count - consumeCounts);
        }
    }

    private HashMap<BlockPos, BlockState> detectOrReplaceAir(StructureTemplate activeTemplate, StructurePlaceSettings settings, StructureData activeTemplateData, Rotation rotation, Level level, int updateFlags, boolean replace) {
        BoundingBox boundingBox = activeTemplate.getBoundingBox(settings, pos.subtract(activeTemplateData.center().rotate(rotation)));
        HashMap<BlockPos, BlockState> invalidBlocks = new HashMap<>();
        for (int i = boundingBox.minX(); i < boundingBox.maxX() + 1; i++) {
            for (int j = boundingBox.minY(); j < boundingBox.maxY() + 1; j++) {
                for (int k = boundingBox.minZ(); k < boundingBox.maxZ() + 1; k++) {
                    BlockPos currentPos = new BlockPos(i, j, k);
                    if (level.getBlockState(currentPos).getBlock().defaultDestroyTime() < 0) {
                        invalidBlocks.put(currentPos, level.getBlockState(currentPos));
                        continue;
                    }
                    if (replace) {
                        level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), updateFlags);
                    }
                }
            }
        }
        return invalidBlocks;
    }

    private static int getMin(ItemStack item, int count) {
        return Math.min(count, item.getCount());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.PLACE_STRUCTURE;
    }
}
