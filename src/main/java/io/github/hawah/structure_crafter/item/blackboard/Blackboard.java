package io.github.hawah.structure_crafter.item.blackboard;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.gui.BlackboardCheckScreen;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.Empty;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Blackboard extends Item {
    public Blackboard() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (interactionHand.equals(InteractionHand.MAIN_HAND)) {
            return super.use(level, player, interactionHand);
        }
        ItemStack itemStack = player.getOffhandItem();
        if (itemStack.has(DataComponentTypeRegistries.BLACKBOARD_WRITING)) {
            player.startUsingItem(interactionHand);
            return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
        }
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            return super.use(level, player, interactionHand);
        }

        if (mainHandItem.is(Items.INK_SAC)) {
            itemStack.set(DataComponentTypeRegistries.BLACKBOARD_WRITING, new Empty());
            player.startUsingItem(interactionHand);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }

        return super.use(level, player, interactionHand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player))
            return;
        if (stack.has(DataComponentTypeRegistries.BLACKBOARD_WRITING)) {
            stack.remove(DataComponentTypeRegistries.BLACKBOARD_WRITING);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {

        if (!(livingEntity instanceof Player player))
            return stack;

        player.getCooldowns().addCooldown(stack.getItem(), 20);

        if (stack.has(DataComponentTypeRegistries.BLACKBOARD_WRITING)) {
            stack.remove(DataComponentTypeRegistries.BLACKBOARD_WRITING);
        }
        if (level.isClientSide()) {
            if (StructureCrafterClient.BLACKBOARD_HANDLER.hasSelection() && StructureCrafterClient.BLACKBOARD_HANDLER.hasCenter()) {
                ScreenOpener.open(new BlackboardCheckScreen());
            } else {
                player.displayClientMessage(
                        !StructureCrafterClient.BLACKBOARD_HANDLER.hasSelection()?
                                Component.translatable("information.no_selection"):
                                Component.translatable("information.no_center"),
                        true
                );
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

}
