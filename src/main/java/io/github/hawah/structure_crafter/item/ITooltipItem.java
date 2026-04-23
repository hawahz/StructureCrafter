package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.util.AllClientHooks;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ITooltipItem {

    static boolean isShiftDown() {
        return AllClientHooks.hasShiftDown();
    }
    default void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements) {
    };
    default void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements, ItemStack item) {
        handleTooltip(tooltipElements);
    };
}
