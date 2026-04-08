package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.block.blockentity.ConnectorBlockEntity;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.HashItemComponent;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class TelephoneHandset extends Item implements ITooltipItem{
    public TelephoneHandset() {
        super(new Properties().stacksTo(1));
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
        final double factor = 0.001;
        TelephoneHandsetComponent handsetComponent = stack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
        if (!level.dimension().equals(handsetComponent.dimension())) {
            return;
        }
        player.addDeltaMovement(handsetComponent.pos().getCenter().subtract(player.position()).multiply(factor, factor, factor));
        HashItemComponent hashItemComponent;
        if (!(level instanceof ServerLevel serverLevel) || !(hashItemComponent = stack.getOrDefault(DataComponentTypeRegistries.HASH_ITEM, HashItemComponent.EMPTY)).dirty()) {
            return;
        }
        final long start = System.nanoTime();
        final long budget = 1_000_000;

        IItemHandler capability = serverLevel.getCapability(Capabilities.ItemHandler.BLOCK, handsetComponent.pos(), Direction.NORTH);
        List<ItemEntry.LazySlot> changedSlots = new ArrayList<>();
        changedSlots.addAll(hashItemComponent.changedSlots().entries());
        for (ItemEntry.LazySlot changedSlot : changedSlots) {

        }
    }

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
        if (blockEntity instanceof ConnectorBlockEntity connectorBlockEntity) {
            connectorBlockEntity.setHasTelephone(true);
            connectorBlockEntity.setChanged();
            serverLevel.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
        }
    }


}
