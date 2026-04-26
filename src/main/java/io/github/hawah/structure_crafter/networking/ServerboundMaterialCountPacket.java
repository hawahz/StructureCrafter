package io.github.hawah.structure_crafter.networking;

import com.mojang.logging.LogUtils;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.MaterialListComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.networking.utils.ClientToServerPacket;
import io.github.hawah.structure_crafter.util.ItemEntry;
import io.github.hawah.structure_crafter.util.StructureHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public record ServerboundMaterialCountPacket(ItemStack itemInHand, BlockPos pos, Direction face, UUID playerId) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMaterialCountPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ServerboundMaterialCountPacket::itemInHand,
            BlockPos.STREAM_CODEC, ServerboundMaterialCountPacket::pos,
            Direction.STREAM_CODEC, ServerboundMaterialCountPacket::face,
            UUIDUtil.STREAM_CODEC, ServerboundMaterialCountPacket::playerId,
            ServerboundMaterialCountPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        if (!player.getUUID().equals(playerId)) {
            return;
        }
        Level level = player.level();
        IItemHandler iItemHandler;
        List<ItemEntry> consumes = itemInHand.getOrDefault(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY).itemWithCounts();
        iItemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);
        if (iItemHandler != null && !consumes.isEmpty()) {
            CompletableFuture
                    .supplyAsync(() -> {
                        NonNullList<ItemStack> inventoryItems = player.isShiftKeyDown()? NonNullList.create() : StructureHandler.getInventoryItems(player);
                        for (int i = 0; i < iItemHandler.getSlots(); i++) {
                            inventoryItems.add(iItemHandler.getStackInSlot(i));
                        }
                        List<ItemEntry> inventory = ItemEntry.flat(ItemEntry.fromStacks(inventoryItems));

                        Map<Integer, Integer> consumeMap = new HashMap<>();
                        for (ItemEntry consume : consumes) {
                            consumeMap.put(consume.id(), consume.count());
                        }

                        List<ItemEntry> batched = new ArrayList<>();
                        for (ItemEntry invEntry : inventory) {
                            int consumeCount = consumeMap.getOrDefault(invEntry.id(), 0);
                            if (consumeCount > 0) {
                                int batchCount = invEntry.count() / consumeCount;
                                if (batchCount > 0) {
                                    batched.add(new ItemEntry(invEntry.id(), batchCount));
                                }
                            }
                        }

                        batched.sort(Comparator.comparingInt(ItemEntry::count));
                        return batched.isEmpty()?0 : batched.getFirst().count();
                    })
                    .thenAccept((count) -> player.displayClientMessage(
                            player.isShiftKeyDown()?
                                    LangData.INFO_CONTAINER_BUILD_CAPABILITY.get(count):
                                    LangData.INFO_CONTAINER_BUILD_CAPABILITY_WITH_INVENTORY.get(count),
                            true
                    ))
                    .exceptionally(e-> {
                        LogUtils.getLogger().error("Error Occurred when calculate Container items.", e);
                        return null;
                    });
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MATERIAL_COUNT_PACKET;
    }
}
