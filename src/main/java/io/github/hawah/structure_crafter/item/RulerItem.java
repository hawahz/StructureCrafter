package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber
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

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!(player.getOffhandItem().getItem() instanceof RulerItem)) {
            return;
        }
        event.setCanceled(true);
    }

    public static BlockPos.@NotNull MutableBlockPos modifyFixed(BlockPos pos, ItemStack itemStack) {
        return modifyFixed(pos, itemStack.get(DataComponentTypeRegistries.RULER_ANCHOR));
    }

    public static BlockPos.@NotNull MutableBlockPos modifyFixed(BlockPlaceContext context, @Nullable BlockPos firstPos) {
        return modifyFixed(context.getClickedPos(), firstPos);
    }

    public static BlockPos.@NotNull MutableBlockPos modifyFixed(BlockPos blockPos, @Nullable BlockPos firstPos) {
        BlockPos.MutableBlockPos pos = blockPos.mutable();
        if (firstPos == null) {
            return pos;
        }
        pos.setY(firstPos.getY());
        final int MAX_DIST = 3;
        if (Math.abs(pos.getX() - firstPos.getX()) <= MAX_DIST || Math.abs(pos.getZ() - firstPos.getZ()) <= MAX_DIST) {
            if (Math.abs(pos.getX() - firstPos.getX()) <= Math.abs(pos.getZ() - firstPos.getZ())) {
                pos.setX(firstPos.getX());
            } else {
                pos.setZ(firstPos.getZ());
            }
        }
        return pos;
    }
}
