package io.github.hawah.structure_crafter.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleBlockRenderer {
    private static SimpleBlockRenderer INSTANCE = null;

    private final HashMap<Object, BlockElement> blocks = new HashMap<>();

    public static boolean hasInstance() {
        return INSTANCE != null;
    }

    public static SimpleBlockRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SimpleBlockRenderer();
        }
        return INSTANCE;
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
        blocks.forEach((object, outlineElement) ->
                outlineElement.render(poseStack, bufferSource, cameraPos, partialTick)
        );
        ((MultiBufferSource.BufferSource) bufferSource).endBatch();
    }

    public BlockElement block(Object slot) {
        return blocks.computeIfAbsent(slot, key -> new BlockElement());
    }

    public BlockElement chaseBlock(Object slot, BlockState blockState) {
        return blocks.computeIfAbsent(slot, key -> new BlockElement().block(blockState));
    }

    public static void tick() {
        if (!hasInstance()) {
            return;
        }
        List<Object> slotsToRemove = new ArrayList<>();
        INSTANCE.blocks.forEach((object, blockElement) -> {
            blockElement.tick();
            if (Math.abs(blockElement.oa) < 0.01 && blockElement.discarded) {
                slotsToRemove.add(object);
            }
        });
        slotsToRemove.forEach(object -> INSTANCE.clearSlot(object));
    }

    public void clearSlot(Object slot) {
        blocks.computeIfPresent(slot, (object, blockElement) -> blockElement.discard());
        blocks.remove(slot);
    }
}
