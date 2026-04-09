package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.client.ClientEvents;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class TelephoneHandset extends Item implements ITooltipItem{
    public TelephoneHandset() {
        super(new Properties().stacksTo(1));
    }

    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        TelephoneHandsetComponent handset;
        if ((handset = player.getMainHandItem().get(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) == null) {
            if (slot != null) {
                Outliner.getInstance().box(slot)
                        .fade()
                        .discard()
                        .finish();
                slot = null;
            }
            return;
        }
        BlockPos pos = handset.pos();
        if (slot == pos) {
            return;
        } else if (slot != null) {
            Outliner.getInstance().box(slot)
                    .fade()
                    .discard()
                    .finish();
        }
        slot = pos;
        Outliner.getInstance().chaseBox(pos, pos, pos)
                .setRGBA(0, 1, 0, 1)
                .finish();
    }

    @Override
    public void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements, ItemStack itemStack) {
        if (itemStack.has(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) {
            TelephoneHandsetComponent handsetComponent = itemStack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
            BlockPos blockPos = handsetComponent.pos();
            tooltipElements.add(1, Either.left(LangData.TOOLTIP_TELEPHONE_HANDSET.get(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) {
            return;
        }
        TelephoneHandsetComponent handsetComponent = stack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
        if (!level.dimension().equals(handsetComponent.dimension())) {
            return;
        }
        BlockPos pos = handsetComponent.pos();
        if (!(level.getBlockEntity(pos) instanceof TelephoneBlockEntity telephoneBlockEntity)) {
            return;
        }
        Vec3 directionVec = pos.getCenter().subtract(player.position());
        float maxDistance = 32;
        boolean shouldPull = directionVec.length() > maxDistance && !telephoneBlockEntity.hasBeacon;
        if (shouldPull) {
            float factor = (float) ((directionVec.length() - maxDistance) / 32f);
            player.addDeltaMovement(directionVec.normalize().multiply(factor, factor, factor));
        }
        if (level.isClientSide()) {
            StructureCrafterClient.TELEPHONE_WIRE_RENDERER.update(
                    pos,
                    pos.getCenter(),
                    Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON ? player.position() : player.getEyePosition(),
                    telephoneBlockEntity.hasBeacon,
                    player.getMainHandItem().equals(stack) || shouldPull
            );
        }
        //updateChangedDataFromStack(stack, serverLevel, connectorBlockEntity, hashItemComponent);
    }

//    private static void updateChangedDataFromStack(ItemStack stack, ServerLevel serverLevel, TelephoneBlockEntity telephoneBlockEntity, HashItemComponent hashItemComponent) {
//        List<ItemEntry.LazySlot> originalChangedSlots = hashItemComponent.changedSlots().entries();
//        Map<ItemEntry.Slot, ItemEntry.LazySlot> changedSlots =
//                originalChangedSlots.stream()
//                        .collect(Collectors.toMap(ItemEntry.LazySlot::toSlot, changedSlot -> changedSlot));
//        try {
//            final long start = System.nanoTime();
//            final long budget = 1_000;
//            // 尝试将更新的内容更新到源方块上
//            // 如果超时，保存到下一tick执行
//            // 取出保存下来标记为变化的格子
//            // 将其转换成Map，一是操作是需要Slot，二是Map可以直接通过Slot获取原数据，方便更新回物品中
//            // 对相关联的connector遍历格子
//            // 每个格子遍历改变的格子，如果击中，那么开始操作
//            for (ItemEntry itemsInConnector : telephoneBlockEntity.getKeys()) {
//                for (ItemEntry.Slot changedSlot : changedSlots.keySet()) {
//                    // 击中时，从原初方块获取能力
//                    // 先操作改变能力
//                    // 然后再把改变的内容更新到be上
//                    if (telephoneBlockEntity.getItems().get(itemsInConnector).contains(changedSlot)) {
//                        ServerLevel dimensionLevel = serverLevel.getServer().getLevel(changedSlots.get(changedSlot).dimension());
//                        if (dimensionLevel == null)
//                            continue;
//                        IItemHandler capability = dimensionLevel
//                                .getCapability(Capabilities.ItemHandler.BLOCK, changedSlot.pos(), Direction.UP);
//                        if (capability == null)
//                            continue;
//                        if (itemsInConnector.isEmpty()) {
//                            tryInsertItemBack(telephoneBlockEntity, hashItemComponent, itemsInConnector, changedSlot, changedSlots, capability);
//                            originalChangedSlots.remove(changedSlots.get(changedSlot));
//                            if (System.nanoTime() - start > budget) {
//                                return;
//                            }
//                            continue;
//                        }
//                        tryRemoveItem(telephoneBlockEntity, itemsInConnector, changedSlot, capability, originalChangedSlots, changedSlots);
//                    }
//                    if (System.nanoTime() - start > budget) {
//                        return;
//                    }
//                }
//            }
//        } finally {
//            stack.update(DataComponentTypeRegistries.HASH_ITEM, HashItemComponent.EMPTY, (h) ->
//                    new HashItemComponent(
//                            h.items(),
//                            HashItemComponent.LazySlotWarper.warp(originalChangedSlots),
//                            !originalChangedSlots.isEmpty()
//                    ));
//        }
//    }
//
//    private static void tryRemoveItem(TelephoneBlockEntity telephoneBlockEntity, ItemEntry itemsInConnector, ItemEntry.Slot changedSlot, IItemHandler capability, List<ItemEntry.LazySlot> originalChangedSlots, Map<ItemEntry.Slot, ItemEntry.LazySlot> changedSlots) {
//        if (changedSlot.counts() == 0) {
//            telephoneBlockEntity.getItems().get(itemsInConnector).remove(changedSlot);
//            telephoneBlockEntity.getItems().get(ItemEntry.EMPTY).add(changedSlot);
//        }
//        capability.extractItem(changedSlot.slot(), changedSlot.counts(), false);
//        originalChangedSlots.remove(changedSlots.get(changedSlot));
//    }
//
//    private static void tryInsertItemBack(TelephoneBlockEntity telephoneBlockEntity, HashItemComponent hashItemComponent, ItemEntry itemsInConnector, ItemEntry.Slot changedSlot, Map<ItemEntry.Slot, ItemEntry.LazySlot> changedSlots, IItemHandler capability) {
//        for (ItemEntry itemEntry : hashItemComponent.items().keySet()) {
//            if (!hashItemComponent.items().get(itemEntry).entries().contains(changedSlots.get(changedSlot))) {
//                continue;
//            }
//            capability.insertItem(changedSlot.slot(), itemEntry.toStack(changedSlot.counts()), false);
//            telephoneBlockEntity.getItems().get(itemsInConnector).remove(changedSlot);
//            telephoneBlockEntity.getItems().get(itemEntry).add(changedSlot);
//            break;
//        }
//    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (!(item.getItem() instanceof TelephoneHandset))
            return super.onDroppedByPlayer(item, player);

        if (player.level().isClientSide() || player.getServer() == null) {
            item.shrink(1);
            return false;
        }
        placeBack(item, (ServerLevel) player.level());
        item.shrink(1);
        return false;
    }

    public static void placeBack(ItemStack itemStack, ServerLevel level) {
        TelephoneHandsetComponent telephoneComponent = itemStack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
        BlockPos pos = telephoneComponent.pos();
        ServerLevel serverLevel = level.getServer().getLevel(telephoneComponent.dimension());
        if (serverLevel == null) {
            throw new IllegalStateException("Telephone handset dimension does not exist");
        }
        serverLevel.getChunk(pos);
        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (blockEntity instanceof TelephoneBlockEntity telephoneBlockEntity) {
            telephoneBlockEntity.setHasTelephone(true);
            telephoneBlockEntity.setChanged();
            serverLevel.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
        }
    }

    public static Object slot = new Object();
}
