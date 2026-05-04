package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.client.gui.RulerScreen;
import io.github.hawah.structure_crafter.lib.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.lib.client.handler.IHandler;
import io.github.hawah.structure_crafter.lib.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.lib.client.render.ruler.RulerMaker;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.lib.client.KeyBinding;
import io.github.hawah.structure_crafter.lib.RaycastHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

public class RulerHandler implements IHandler {

    private BlockPos selectedPos;
    private BlockPos firstPos;
    private BlockPos secondPos;
    private final Queue<RulerHolder> slots = new ArrayDeque<>();
    private Object fistSlotHolder, secondSlotHolder, rulerSlotHolder;
    private boolean swapped = false;

    public RulerHandler() {
        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                () -> isActive() && selectedPos != null,
                () -> {
                    if (firstPos == null) {
                        firstPos = selectedPos;
                    } else if (secondPos == null) {
                        secondPos = selectedPos;
                        push(
                                swapped? secondSlotHolder: fistSlotHolder,
                                swapped? fistSlotHolder: secondSlotHolder,
                                rulerSlotHolder,
                                swapped? secondPos: firstPos,
                                swapped? firstPos: secondPos
                        );
                        firstPos = null;
                        secondPos = null;
                        fistSlotHolder = null;
                        secondSlotHolder = null;
                        rulerSlotHolder = null;
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
                    if (rulerSlotHolder == null) {
                        rulerSlotHolder = new Object();
                    }
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
                },
                LangData.HUD_TIP_RULER_SELECT_POINT.get()
        ));
        KeyBinding.LEFT.bind(KeyBinding.Action.of(
                this::isActive,
                this::swap,
                LangData.HUD_TIP_RULER_SWAP.get()
        ));
        KeyBinding.ALT_R.bind(KeyBinding.Action.of(
                this::isActive,
                () -> {
                    ScreenOpener.open(new RulerScreen());
                },
                LangData.HUD_TIP_RULER_OPEN_SCREEN.get()
        ));
        KeyBinding.SHIFT_R.bind(KeyBinding.Action.of(
                this::isActive,
                () -> {
                    Outliner.getInstance().thickBox(this)
                            .discard()
                            .finish();
                    RulerMaker.getInstance().chase(rulerSlotHolder)
                            .discard()
                            .finish();
                    Outliner.getInstance().thickBox(fistSlotHolder)
                            .discard()
                            .finish();
                    Outliner.getInstance().thickBox(secondSlotHolder)
                            .discard()
                            .finish();
                    firstPos = null;
                    secondPos = null;
                    fistSlotHolder = null;
                    secondSlotHolder = null;
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
                LangData.HUD_TIP_RULER_CLEAR.get()
        ));
    }

    private void push(Object slot0, Object slot1, Object rulerSlot, BlockPos first, BlockPos second) {
        slots.add(new RulerHolder(first, second, slot0, slot1, rulerSlot));
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
            RulerMaker.getInstance().chase(rulerSlotHolder)
                    .discard()
                    .finish();
            Outliner.getInstance().thickBox(fistSlotHolder)
                    .discard()
                    .finish();
            Outliner.getInstance().thickBox(secondSlotHolder)
                    .discard()
                    .finish();
        } else if (fistSlotHolder != null && firstPos != null && selectedPos != null) {
            if (secondSlotHolder == null)
                secondSlotHolder = new Object();
            RulerMaker.getInstance().chase(rulerSlotHolder,
                            swapped? selectedPos: firstPos,
                            swapped? firstPos: selectedPos)
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
        } else {
            Outliner.getInstance().thickBox(this)
                    .fade()
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

    @Override
    public boolean isVisible() {
        return IHandler.super.isVisible() && selectedPos != null;
    }

    private void swap() {
        swapped = !swapped;
    }

    static final class RulerHolder {
        private final BlockPos pos0, pos1;
        private final Object slot0, slot1, rulerSlot;

        RulerHolder(BlockPos pos0, BlockPos pos1, Object slot0, Object slot1, Object ruler) {
            this.pos0 = pos0;
            this.pos1 = pos1;
            this.slot0 = slot0;
            this.slot1 = slot1;
            this.rulerSlot = ruler;
        }

        public Object getRulerSlot() {
            return rulerSlot;
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
