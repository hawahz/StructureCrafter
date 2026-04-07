package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.block.IPlacePriority;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class PriorityBlockItem extends BlockItem {
    public PriorityBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @SubscribeEvent
    public static void onBlockPlacedWhenInteractFirst(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof PriorityBlockItem blockItem && blockItem.getBlock() instanceof IPlacePriority priority && priority.isPriority(event))
            event.setUseBlock(TriState.FALSE);
    }


}
