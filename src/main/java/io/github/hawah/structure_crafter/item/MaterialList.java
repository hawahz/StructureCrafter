package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.client.gui.MaterialListScreen;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.networking.ServerboundMaterialCountPacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.MaterialListComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MaterialList extends Item implements ITooltipItem{
    public MaterialList() {
        super(new Properties().component(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        ItemStack itemStack = player.getItemInHand(usedHand);
        MaterialListComponent component = itemStack.getOrDefault(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY);

        if (component.isEmpty()) {
            return super.use(level, player, usedHand);
        }

        if (level.isClientSide()) {
            openScreen();
        }
        return super.use(level, player, usedHand);
    }

    @OnlyIn(Dist.CLIENT)
    public static void openScreen() {
        ScreenOpener.open(new MaterialListScreen());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemInHand = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        // TODO
        if (player != null) {
            Direction face = context.getClickedFace();
            if (level.isClientSide()) {
                Networking.sendToServer(new ServerboundMaterialCountPacket(itemInHand, pos, face, player.getUUID()));
            }
            return InteractionResult.CONSUME;
        }
        return super.useOn(context);
    }

    public void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        int t = 1;
        if (!ITooltipItem.isShiftDown()) {
            tooltipElements.add(t, Either.left(LangData.SHIFT.get()));
        } else {
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_MATERIAL_LIST_0.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_MATERIAL_LIST_1.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_MATERIAL_LIST_2.get()));
            tooltipElements.add(t, Either.left(LangData.TOOLTIP_MATERIAL_LIST_3.get()));
        }
    }

}
