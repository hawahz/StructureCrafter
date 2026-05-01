package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.intellij.lang.annotations.MagicConstant;

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

    public void applySettings(ItemStack itemStack, @RulerSetting int setting) {
        itemStack.set(DataComponentTypeRegistries.RULER_SETTINGS, setting);
    }
    public static boolean settingRemained(ItemStack itemStack, @RulerSetting int setting) {
        return (itemStack.getOrDefault(DataComponentTypeRegistries.RULER_SETTINGS, 0) ^ setting) == 0;
    }

    public static int settingOf(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponentTypeRegistries.RULER_SETTINGS, 0);
    }

    public static int getDistance(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponentTypeRegistries.RULER_SETTINGS, 0) & DISTANCE_MASK;
    }

    public static final int DISTANCE_MASK = 0b011111111;
    public static final int CHANGE_CENTER = 1 << 8;
    public static final int IS_CIRCLE = 1 << 9;

    @MagicConstant(flags = {CHANGE_CENTER, DISTANCE_MASK, IS_CIRCLE})
    public @interface RulerSetting {}
}
