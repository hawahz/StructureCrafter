package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.client.ClientSharedFlags;
import io.github.hawah.structure_crafter.client.gui.utils.ScrollPanel;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.client.render.ruler.RulerMaker;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.item.RulerItem;
import io.github.hawah.structure_crafter.networking.OffhandItemChangePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
import io.github.hawah.structure_crafter.util.SharedFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;

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
                Component.empty()
        ));

        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                this::isActive,
                () -> {
                    LocalPlayer player = Minecraft.getInstance().player;
                    assert player != null;
                    double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
                    BlockHitResult survival = RaycastHelper.rayTraceRange(
                            player.level(),
                            player,
                            range
                    );
                    BlockHitResult hitResult;
                    if ((survival.getType() == BlockHitResult.Type.BLOCK || !player.isCreative())) {
                        return;
                    }
                    hitResult = RaycastHelper.rayTraceRange(
                            player.level(),
                            player,
                            100
                    );
                    if (hitResult.getType() == BlockHitResult.Type.BLOCK && player.getMainHandItem().getItem() instanceof BlockItem item) {
                        BlockPlaceContext context = new BlockPlaceContext(
                                player,
                                InteractionHand.MAIN_HAND,
                                player.getMainHandItem(),
                                hitResult
                        );
                        item.place(context);
                        player.swing(InteractionHand.MAIN_HAND);
                        BlockPos.MutableBlockPos pos = RulerItem.modifyFixed(context, firstPos);
                        update(pos);
                    }
                },
                Component.empty()
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
            Outliner.getInstance().thickBox(this)
                    .discard()
                    .finish();
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
                    .finish();
            Outliner.getInstance().chaseThickBox(fistSlotHolder, firstPos, firstPos)
                    .smooth(1F)
                    .finish();
        }

        if (!isActive()) {
            reset();
            return;
        }
        if (firstPos != null && selectedPos != null) {
            Outliner.getInstance().chaseThickBox(this, selectedPos, selectedPos)
                    .setRGBA(0, 1, 0,1)
                    .smooth(1F)
                    .finish();
        } else {
            Outliner.getInstance().thickBox(this)
                    .fade()
                    .finish();
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
