package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public interface ITooltipItem {
    void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements);
}
