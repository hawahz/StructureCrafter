package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.block.blockentity.ConnectorBlockEntity;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class TelephoneHandset extends Item implements ITooltipItem{
    public TelephoneHandset() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements, ItemStack itemStack) {
        if (itemStack.has(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) {
            BlockPos blockPos = itemStack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, BlockPos.ZERO);
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
        BlockPos pos = stack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, BlockPos.ZERO);
        player.addDeltaMovement(pos.getCenter().subtract(player.position()).multiply(factor, factor, factor));
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (!(item.getItem() instanceof TelephoneHandset))
            return super.onDroppedByPlayer(item, player);
        BlockPos pos = item.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, BlockPos.ZERO);
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof ConnectorBlockEntity connectorBlockEntity) {
            connectorBlockEntity.setHasTelephone(true);
            player.level().sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
        }
        item.shrink(1);
        return false;
    }
}
