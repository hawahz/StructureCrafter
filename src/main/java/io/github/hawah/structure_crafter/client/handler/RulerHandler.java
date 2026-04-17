package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.client.render.ruler.RulerMaker;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

public class RulerHandler implements IHandler {

    private BlockPos selectedPos;
    private BlockPos firstPos;
    private BlockPos secondPos;
    private final Queue<RulerHolder> slots = new ArrayDeque<>();
    private Object fistSlotHolder, secondSlotHolder;

    public RulerHandler() {
        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                () -> isActive() && selectedPos != null,
                () -> {
                    if (firstPos == null) {
                        firstPos = selectedPos;
                    } else if (secondPos == null) {
                        secondPos = selectedPos;
                        push(fistSlotHolder, secondSlotHolder, firstPos, secondPos);
                        firstPos = null;
                        secondPos = null;
                        fistSlotHolder = null;
                        secondSlotHolder = null;
                    } else {
                        firstPos = selectedPos;
                        secondPos = null;
                    }
                    if (fistSlotHolder == null) {
                        fistSlotHolder = new Object();
                    }
                    if (secondSlotHolder == null) {
                        secondSlotHolder = new Object();
                    }
                    Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
                },
                Component.empty()
        ));
        KeyBinding.SHIFT_R.bind(KeyBinding.Action.of(
                this::isActive,
                () -> {
                    slots.forEach(
                            entry -> {
                                RulerMaker.getInstance().chase(entry.getRulerSlot())
                                        .discard()
                                        .finish();
                                Outliner.getInstance().thickBox(entry.getFirstSlot())
                                        .discard()
                                        .finish();
                                Outliner.getInstance().thickBox(entry.getSecondSlot())
                                        .discard()
                                        .finish();
                            }
                    );
                    slots.clear();
                },
                Component.empty()
        ));
    }

    private void push(Object slot0, Object slot1, BlockPos first, BlockPos second) {
        slots.add(new RulerHolder(first, second, slot0, slot1));
        if (slots.size() > 10) {
            RulerHolder toRemove = slots.remove();
            RulerMaker.getInstance().chase(toRemove.getRulerSlot())
                    .discard()
                    .finish();
            Outliner.getInstance().thickBox(toRemove.getFirstSlot())
                    .discard()
                    .finish();
            Outliner.getInstance().thickBox(toRemove.getSecondSlot())
                    .discard()
                    .finish();
        }
    }

    @Override
    public void tick() {
        if (!isVisible()) {
            Outliner.getInstance().thickBox(this)
                    .discard()
                    .finish();
        }
        if (fistSlotHolder != null && firstPos != null && selectedPos != null) {
            if (secondSlotHolder == null)
                secondSlotHolder = new Object();
            RulerMaker.getInstance().chase(fistSlotHolder, firstPos, selectedPos)
                    .finish();
            Outliner.getInstance().chaseThickBox(fistSlotHolder, firstPos, firstPos)
                    .finish();
            Outliner.getInstance().chaseThickBox(secondSlotHolder, selectedPos, selectedPos)
                    .finish();
        }
        slots.forEach(entry -> {
                    RulerMaker.getInstance().chase(entry.getRulerSlot(), entry.getFirstPos(), entry.getSecondPos())
                            .finish();
                    Outliner.getInstance().chaseThickBox(entry.getFirstSlot(), entry.getFirstPos(), entry.getFirstPos())
                            .finish();
                    Outliner.getInstance().chaseThickBox(entry.getSecondSlot(), entry.getSecondPos(), entry.getSecondPos())
                            .finish();
                }
        );

        if (!isActive()) {
            return;
        }
        if (selectedPos != null) {
            Outliner.getInstance().chaseThickBox(this, selectedPos, selectedPos)
                    .setRGBA(0, 1, 0, firstPos == null? 1: 0)
                    .smooth(0.8F)
                    .finish();
        }
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        ItemStack stack = player.getMainHandItem();

        BlockHitResult hitResult = RaycastHelper.rayTraceRange(player.level(), player, 100);
        if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
            selectedPos = hitResult.getBlockPos();
        } else {
            selectedPos = null;
        }
    }

    @Override
    public boolean isActive() {
        return isPresent() && Minecraft.getInstance().player.getMainHandItem().is(ItemRegistries.RULER_ITEM);
    }

    static class RulerHolder {
        private final BlockPos pos0, pos1;
        private final Object slot0, slot1;

        RulerHolder(BlockPos pos0, BlockPos pos1, Object slot0, Object slot1) {
            this.pos0 = pos0;
            this.pos1 = pos1;
            this.slot0 = slot0;
            this.slot1 = slot1;
        }

        public Object getRulerSlot() {
            return slot0;
        }

        public Object getFirstSlot() {
            return slot0;
        }

        public Object getSecondSlot() {
            return slot1;
        }

        public BlockPos getFirstPos() {
            return pos0;
        }

        public BlockPos getSecondPos() {
            return pos1;
        }
    }
}
