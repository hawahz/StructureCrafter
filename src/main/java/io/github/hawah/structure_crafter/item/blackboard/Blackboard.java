package io.github.hawah.structure_crafter.item.blackboard;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.gui.BlackboardCheckScreen;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.Empty;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.ITooltipItem;
import io.github.hawah.structure_crafter.util.BlackboardRenderType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings("DataFlowIssue")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Blackboard extends Item implements ITooltipItem {
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
        if (player.isCreative()) {
            finishUsingItem(itemStack, level, player);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
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
        if (!(entityLiving instanceof Player))
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

                if (!StructureCrafterClient.BLACKBOARD_HANDLER.isValidSize()) {
                    player.displayClientMessage(
                            LangData.ERROR_AREA_TOO_LARGE.get(),
                            true
                    );
                } else if (!StructureCrafterClient.BLACKBOARD_HANDLER.isValidCenter()) {
                    player.displayClientMessage(
                            LangData.ERROR_ANCHOR_OUT_OF_BOUNDS.get(),
                            true
                    );
                } else {
                    ScreenOpener.open(new BlackboardCheckScreen());
                }
            } else {
                player.displayClientMessage(
                        !StructureCrafterClient.BLACKBOARD_HANDLER.hasSelection()?
                                LangData.INFO_NO_SELECTION.get():
                                LangData.INFO_NO_ANCHOR.get(),
                        true
                );
            }
        } else if (!player.isCreative()) {
            player.getMainHandItem().shrink(1);
        }
        player.level().playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return (BlackboardRenderType.WRITE.equals(Config.ClientConfig.BLACKBOARD_ANIMATION_TYPE.get()) && Minecraft.getInstance().player.getMainArm().equals(HumanoidArm.RIGHT))?
                UseAnim.NONE :
                UseAnim.EAT;
    }

    @Override
    public void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        int t = 1;
        if (!Screen.hasShiftDown()) {
            tooltipElements.add(t, Either.left(LangData.SHIFT.get()));
        } else {
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_0.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_1.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_2.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_3.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_4.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_5.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_6.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_7.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_8.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_8_1.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_9.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_BLACKBOARD_10.get()));
            tooltipElements.add(t, Either.left(LangData.TOOLTIP_BLACKBOARD_11.get()));
        }
    }
}
