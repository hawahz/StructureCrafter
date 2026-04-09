package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class TelephoneHandset extends Item implements ITooltipItem{
    public TelephoneHandset() {
        super(new Properties().stacksTo(1));
    }

    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        TelephoneHandsetComponent handset;
        if ((handset = player.getMainHandItem().get(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) == null) {
            if (slot != null) {
                Outliner.getInstance().box(slot)
                        .fade()
                        .discard()
                        .finish();
                slot = null;
            }
            return;
        }
        BlockPos pos = handset.pos();
        if (slot == pos) {
            return;
        } else if (slot != null) {
            Outliner.getInstance().box(slot)
                    .fade()
                    .discard()
                    .finish();
        }
        slot = pos;
        Outliner.getInstance().chaseBox(pos, pos, pos)
                .setRGBA(0, 1, 0, 1)
                .finish();
    }

    @Override
    public void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements, ItemStack itemStack) {
        if (itemStack.has(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) {
            TelephoneHandsetComponent handsetComponent = itemStack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
            BlockPos blockPos = handsetComponent.pos();
            tooltipElements.add(1, Either.left(LangData.TOOLTIP_TELEPHONE_HANDSET.get(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player)) {
            return;
        }
        TelephoneHandsetComponent handsetComponent = stack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
        if (!level.dimension().equals(handsetComponent.dimension())) {
            return;
        }
        BlockPos pos = handsetComponent.pos();
        if (!(level.getBlockEntity(pos) instanceof TelephoneBlockEntity telephoneBlockEntity)) {
            return;
        }
        Vec3 directionVec = pos.getCenter().subtract(player.position());
        float maxDistance = 32;
        boolean shouldPull = directionVec.length() > maxDistance && !telephoneBlockEntity.hasBeacon;
        if (shouldPull) {
            float factor = (float) ((directionVec.length() - maxDistance) / 32f);
            player.addDeltaMovement(directionVec.normalize().multiply(factor, factor, factor));
        }
        if (level.isClientSide()) {
            StructureCrafterClient.TELEPHONE_WIRE_RENDERER.update(
                    pos,
                    pos.getCenter(),
                    Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON ? player.position() : player.getEyePosition(),
                    telephoneBlockEntity.hasBeacon,
                    player.getMainHandItem().equals(stack) || shouldPull
            );
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (!(item.getItem() instanceof TelephoneHandset))
            return super.onDroppedByPlayer(item, player);

        if (player.level().isClientSide() || player.level().getServer() == null) {
            item.shrink(1);
            return false;
        }
        placeBack(item, (ServerLevel) player.level());
        item.shrink(1);
        return false;
    }

    public static void placeBack(ItemStack itemStack, ServerLevel level) {
        TelephoneHandsetComponent telephoneComponent = itemStack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
        BlockPos pos = telephoneComponent.pos();
        ServerLevel serverLevel = level.getServer().getLevel(telephoneComponent.dimension());
        if (serverLevel == null) {
            throw new IllegalStateException("Telephone handset dimension does not exist");
        }
        serverLevel.getChunk(pos);
        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (blockEntity instanceof TelephoneBlockEntity telephoneBlockEntity) {
            telephoneBlockEntity.setHasTelephone(true);
            telephoneBlockEntity.setChanged();
            serverLevel.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
        }
    }

    public static Object slot = new Object();
}
