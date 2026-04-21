package io.github.hawah.structure_crafter.item;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.compat.sable.SableLogicTransformCompat;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber
public class TelephoneHandset extends Item implements ITooltipItem{
    public TelephoneHandset() {
        super(new Properties().stacksTo(1));
    }

    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !Config.ClientConfig.RENDER_TELEPHONE_BOOST_POSITION.get()) {
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
        Outliner.getInstance().chaseBox(pos, pos, pos, true)
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player) || !stack.is(ItemRegistries.TELEPHONE_HANDSET)) {
            return;
        }
        TelephoneHandsetComponent handsetComponent = stack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
        if (!level.dimension().equals(handsetComponent.dimension())) {
            return;
        }
        BlockPos rawPos = handsetComponent.pos();
        BlockPos pos = SableLogicTransformCompat.instance().level(level).applyTransform(rawPos);
        if (!(level.getBlockEntity(rawPos) instanceof TelephoneBlockEntity telephoneBlockEntity)) {
            stack.shrink(1);
            StructureCrafterClient.TELEPHONE_WIRE_RENDERER.pop(rawPos);
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
            Vec3 center = SableLogicTransformCompat.instance().applyTransform(rawPos.getCenter())
                    .add(Vec3.atLowerCornerOf(telephoneBlockEntity.facing.getNormal()).multiply(0.5, 0.5, 0.5))
                    .add(new Vec3(0, -0.25, 0));
            StructureCrafterClient.TELEPHONE_WIRE_RENDERER.update(
                    rawPos,
                    center,
                    Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON ? player.position() : player.getEyePosition(),
                    telephoneBlockEntity.hasBeacon,
                    player.getMainHandItem().equals(stack) || shouldPull
            );
        }
        //updateChangedDataFromStack(stack, serverLevel, connectorBlockEntity, hashItemComponent);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (!(item.getItem() instanceof TelephoneHandset))
            return super.onDroppedByPlayer(item, player);

        if (player.level().isClientSide() || player.getServer() == null) {
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        TelephoneHandsetComponent handsetComponent = stack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY);
        if (!level.dimension().equals(handsetComponent.dimension())) {
            if (level.isClientSide()) {
                player.displayClientMessage(LangData.MESSAGE_TELEPHONE_CHANNEL_NOT_FOUND.get(), true);
            }
            return InteractionResultHolder.pass(stack);
        }
        return super.use(level, player, usedHand);
    }

    @SubscribeEvent
    public static void onPlayerTossHandset(ItemTossEvent event) {
        ItemEntity entity = event.getEntity();
        ItemStack item = entity.getItem();
        if (!(item.getItem() instanceof TelephoneHandset)) {
            return;
        }
        event.setCanceled(true);
        TelephoneHandsetComponent component;
        if ((component = item.get(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) == null)
            return;
        BlockPos pos = component.pos();
        Player player = event.getPlayer();
        if (player.level().isClientSide()) {
            StructureCrafterClient.TELEPHONE_WIRE_RENDERER.pop(pos);
        }
        if (player.level().getBlockEntity(pos) instanceof TelephoneBlockEntity blockEntity) {
            blockEntity.setHasTelephone(true);
        }
    }
}
