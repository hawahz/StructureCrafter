package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.client.StructureWandModifier;
import io.github.hawah.structure_crafter.client.render.ruler.RulerMaker;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RulerItem extends Item implements IModifierItem{
    public RulerItem(Properties properties) {
        super(properties.stacksTo(1).component(DataComponentTypeRegistries.RULER_EDGE_MODE, false));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide())
            return InteractionResult.PASS;
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown() && usedHand.equals(InteractionHand.MAIN_HAND)) {
            player.getItemInHand(usedHand).set(DataComponentTypeRegistries.RULER_EDGE_MODE,
                    !player.getItemInHand(usedHand).getOrDefault(DataComponentTypeRegistries.RULER_EDGE_MODE, false));
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public StructureWandModifier.Type getType() {
        return StructureWandModifier.Type.RULER;
    }
}
