package io.github.hawah.structure_crafter.util;

import io.github.hawah.structure_crafter.lib.client.render.block.SimpleBlockRenderer;
import io.github.hawah.structure_crafter.lib.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.lib.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.mixin.BlockItemAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;

public enum RulerShadowTool {
    OUTLINE(OutlineTool::fade, OutlineTool::discard, OutlineTool::chase),
    BLOCK(BlockTool::fade, BlockTool::discard, BlockTool::chase),
    BLOCK_WITH_OUTLINE(
            (o) -> {BlockTool.fade(o);OutlineTool.fade(o);},
            (o) -> {BlockTool.discard(o);OutlineTool.discard(o);},
            (o, pos, direction) -> {BlockTool.chase(o, pos, direction);OutlineTool.chase(o, pos, direction);}
    ),
    ;
    private final Consumer<Object> fadeTool, discardTool;
    private final TriConsumer<Object, BlockPos, BlockHitResult> chaseTool;
    RulerShadowTool(Consumer<Object> fadeTool, Consumer<Object> discardTool, TriConsumer<Object, BlockPos, BlockHitResult> chaseTool) {
        this.fadeTool = fadeTool;
        this.discardTool = discardTool;
        this.chaseTool = chaseTool;
    }

    public void fade(Object slot) {
        fadeTool.accept(slot);
        if (this.equals(OUTLINE)) {
            BlockTool.discard(slot);
        } else if (this.equals(BLOCK)) {
            OutlineTool.discard(slot);
        }
    }

    public void discard(Object slot) {
        discardTool.accept(slot);
        if (this.equals(OUTLINE)) {
            BlockTool.discard(slot);
        } else if (this.equals(BLOCK)) {
            OutlineTool.discard(slot);
        }
    }

    public void chase(Object slot, BlockPos validPos, BlockHitResult hitResult) {
        chaseTool.accept(slot, validPos, hitResult);
        if (this.equals(OUTLINE)) {
            BlockTool.discard(slot);
        } else if (this.equals(BLOCK)) {
            OutlineTool.discard(slot);
        }
    }

    static class OutlineTool {
        private static void fade(Object slot) {
            Outliner.getInstance().thickBox(slot)
                    .fade()
                    .finish();
        }

        private static void discard(Object slot) {
            Outliner.getInstance().thickBox(slot)
                    .discard()
                    .finish();
        }

        private static void chase(Object slot, BlockPos validPos, BlockHitResult hitResult) {
            Outliner.getInstance().chaseThickBox(slot, validPos, validPos)
                    .setRGBA(0, 1, 0,1)
                    .smooth(1F)
                    .finish();
        }
    }

    static class BlockTool {
        private static void fade(Object slot) {
            SimpleBlockRenderer.getInstance().block(slot)
                    .fade()
                    .finish();
        }

        private static void discard(Object slot) {
            SimpleBlockRenderer.getInstance().block(slot)
                    .discard()
                    .finish();
        }

        private static void chase(Object slot, BlockPos validPos, BlockHitResult hitResult) {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            ItemStack handItem = Minecraft.getInstance().player.getMainHandItem();
            BlockPlaceContext placeContext = BlockPlaceContext.at(
                    new BlockPlaceContext(
                            Minecraft.getInstance().player,
                            InteractionHand.MAIN_HAND,
                            handItem,
                            hitResult
                    ),
                    validPos,
                    hitResult.getDirection()
            );
            BlockState state = (handItem.getItem() instanceof BlockItem blockItem)?
                    ((BlockItemAccessor)blockItem).BlockItemAccessor$getPlacementState(placeContext):
                    null;
            SimpleBlockRenderer.getInstance().block(slot)
                    .block(state)
                    .setPositions(new Vec3(validPos.getX(), validPos.getY(), validPos.getZ()))
                    .setRGBA(1, 1, 1, Math.abs(AnimationTickHolder.getTicks() / 40F % 1 - 0.5F) + 0.3F)
                    .smooth(1F)
                    .finish();
        }
    }
}
