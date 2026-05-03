package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.client.ClientSharedFlags;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.client.render.ruler.RulerMaker;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.item.RulerItem;
import io.github.hawah.structure_crafter.networking.ClientboundTryPlaceBlockFixedPacket;
import io.github.hawah.structure_crafter.networking.ClientboundTryPlaceBlockPacket;
import io.github.hawah.structure_crafter.networking.OffhandItemChangePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RulerOffHandler implements IHandler {

    private BlockPos selectedPos;
    private BlockPos firstPos;
    private Object fistSlotHolder, secondSlotHolder;
    private boolean swapped = false;

    public RulerOffHandler() {

        ClientSharedFlags.registerSyncValidator(this::isActive);

        KeyBinding.LEFT.bind(KeyBinding.Action.of(
                this::isActive,
                this::swap,
                LangData.HUD_TIP_RULER_SWAP.get()
        ));

        KeyBinding.CTRL_R.bind(KeyBinding.Action.of(
                this::isActive,
                () -> {},
                LangData.HUD_TIP_RULER_FIXED_PLACE.get()
        ).blocking(false));

        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                this::isActive,
                () -> {
                    LocalPlayer player = Minecraft.getInstance().player;
                    assert player != null;
                    double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
                    BlockHitResult hitResult = RaycastHelper.rayTraceRange(
                            player.level(),
                            player,
                            player.isCreative()? 100: range
                    );
                    if (hitResult.getType() == BlockHitResult.Type.BLOCK && player.getMainHandItem().getItem() instanceof BlockItem) {
                        BlockPlaceContext context = new BlockPlaceContext(
                                player,
                                InteractionHand.MAIN_HAND,
                                player.getMainHandItem(),
                                hitResult
                        );
                        player.swing(InteractionHand.MAIN_HAND);
                        BlockPos pos = Screen.hasControlDown()?
                                RulerItem.modifyFixed(context, firstPos):
                                context.getClickedPos();
                        update(pos);
                    }
                    if (Screen.hasControlDown()) {
                        Networking.sendToServer(new ClientboundTryPlaceBlockFixedPacket(
                                player.getEyePosition(),
                                RaycastHelper.getTraceTarget(player, 100, Vec3.ZERO),
                                100,
                                InteractionHand.MAIN_HAND,
                                player.getMainHandItem(),
                                firstPos
                        ));
                    } else {
                        Networking.sendToServer(new ClientboundTryPlaceBlockPacket(
                                player.getEyePosition(),
                                RaycastHelper.getTraceTarget(player, 100, Vec3.ZERO),
                                100,
                                InteractionHand.MAIN_HAND,
                                player.getMainHandItem()
                        ));
                    }
                },
                LangData.HUD_TIP_RULER_PLACE.get()
        ).blocking(false));
    }

    private void reset() {
        Outliner.getInstance().thickBox(fistSlotHolder).discard().finish();
        Outliner.getInstance().thickBox(secondSlotHolder).discard().finish();
        RulerMaker.getInstance().chase(fistSlotHolder)
                .discard()
                .finish();
        firstPos = null;
        fistSlotHolder = null;
        secondSlotHolder = null;
    }

    public void update(BlockPos pos) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        ItemStack stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof RulerItem))
            return;
        if ((RulerItem.settingOf(stack) & RulerItem.CHANGE_CENTER) != 0 || firstPos == null) {
            firstPos = pos;
            stack.set(DataComponentTypeRegistries.RULER_ANCHOR, firstPos);
            Networking.sendToServer(new OffhandItemChangePacket(stack));
        }
    }

    @Override
    public void tick() {
        if (!isVisible()) {
            Config.ClientConfig.RULER_SHADOW_TOOL.get().discard(this);
        }
        if (fistSlotHolder == null) {
            fistSlotHolder = new Object();
        }
        if (secondSlotHolder == null)
            secondSlotHolder = new Object();
        if (firstPos != null && selectedPos != null) {
            RulerMaker.getInstance().chase(
                    fistSlotHolder,
                            swapped? selectedPos: firstPos,
                            swapped? firstPos: selectedPos)
                    .smooth(1F)
                    .finish();
            Outliner.getInstance().chaseThickBox(fistSlotHolder, firstPos, firstPos)
                    .smooth(1F)
                    .finish();
        }

        if (!isActive()) {
            reset();
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;

        BlockHitResult hitResult = RaycastHelper.rayTraceRange(
                player.level(),
                player,
                player.isCreative()? 100: player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)
        );
        if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
            BlockPos targetPos = hitResult.getBlockPos().relative(hitResult.getDirection());
            selectedPos = Screen.hasControlDown()?
                    RulerItem.modifyFixed(targetPos, firstPos):
                    targetPos;
        } else {
            Outliner.getInstance().thickBox(secondSlotHolder).discard().finish();
            RulerMaker.getInstance().chase(fistSlotHolder)
                    .discard()
                    .finish();
            selectedPos = null;
        }
        BlockPos validPos;
        if (firstPos != null && selectedPos != null && (validPos = getValidPos(player, selectedPos, hitResult.getDirection())) != null) {
            Config.ClientConfig.RULER_SHADOW_TOOL.get().chase(this, validPos, hitResult);
        } else {
            Config.ClientConfig.RULER_SHADOW_TOOL.get().fade(this);
        }
    }

    public BlockPos getValidPos(LocalPlayer player, BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (int i = 0; i < 3; i++) {
            if (player.level().getBlockState(mutable).canBeReplaced()) {
                return mutable.immutable();
            }
            mutable.set(mutable.relative(direction));
        }
        return null;
    }

    @Override
    public boolean isActive() {
        assert Minecraft.getInstance().player != null;
        return isPresent() &&
                Minecraft.getInstance().player.getOffhandItem().is(ItemRegistries.RULER_ITEM) &&
                (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof BlockItem);
    }

    private void swap() {
        this.swapped = !swapped;
    }
}
