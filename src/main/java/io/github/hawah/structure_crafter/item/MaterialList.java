package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.client.gui.MaterialListScreen;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.client.handler.StructureHandler;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.MaterialListComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@ParametersAreNonnullByDefault
public class MaterialList extends Item {
    public MaterialList() {
        super(new Properties().component(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {

        ItemStack itemStack = player.getItemInHand(usedHand);
        MaterialListComponent component = itemStack.getOrDefault(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY);

        if (component.isEmpty()) {
            return super.use(level, player, usedHand);
        }

        if (level.isClientSide()) {
            ScreenOpener.open(new MaterialListScreen());
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemInHand = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        ResourceHandler<ItemResource> iItemHandler;
        List<ItemEntry> consumes = itemInHand.getOrDefault(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY).itemWithCounts();


        Player player = context.getPlayer();
        // TODO
        if (level.isClientSide() && player != null && !consumes.isEmpty() && (iItemHandler = level.getCapability(Capabilities.Item.BLOCK, pos, context.getClickedFace())) != null) {

            CompletableFuture
                    .supplyAsync(() -> {
                        NonNullList<ItemStack> inventoryItems = player.isShiftKeyDown()? NonNullList.create() : StructureHandler.getInventoryItems(player);
                        for (int i = 0; i < iItemHandler.size(); i++) {
                            inventoryItems.add(iItemHandler.getResource(i).toStack());
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
                    .thenAccept((count) -> {
                        Minecraft.getInstance().execute(()->{
                            player.displayClientMessage(
                                    player.isShiftKeyDown()?
                                            LangData.INFO_CONTAINER_BUILD_CAPABILITY.get(count):
                                            LangData.INFO_CONTAINER_BUILD_CAPABILITY_WITH_INVENTORY.get(count),
                                    true
                            );
                        });
                    })
                    .exceptionally(e-> {
                        StructureCrafter.LOGGER.error("Error Occurred when calculate Container items.", e);
                        return null;
                    });
            return InteractionResult.CONSUME;
        }
        return super.useOn(context);
    }

}
