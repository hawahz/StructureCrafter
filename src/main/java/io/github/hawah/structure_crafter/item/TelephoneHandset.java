package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
}
