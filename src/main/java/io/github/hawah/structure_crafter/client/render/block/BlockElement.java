package io.github.hawah.structure_crafter.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.structure_crafter.client.render.PositionedElement;
import io.github.hawah.structure_crafter.client.render.structure.WarpedBufferBuilder;
import io.github.hawah.structure_crafter.client.render.structure.WarpedBufferRenderer;
import io.github.hawah.structure_crafter.compat.sable.RenderCompat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class BlockElement extends PositionedElement<BlockElement> {
    public BlockState blockState;
    public BakedModel bakedModel;
    public Map<RenderType, WarpedBufferRenderer> cache = new HashMap<>();
    public BlockElement block(BlockState blockState) {
        if (blockState == null) {
            this.blockState = null;
            return this;
        }
        if (this.blockState != null && this.blockState.equals(blockState)) {
            return this;
        }
        cache.clear();
        this.blockState = blockState;
        ClientLevel renderLevel = Minecraft.getInstance().level;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.bakedModel = blockRenderer.getBlockModelShaper().getBlockModel(blockState);
        assert renderLevel != null;
        ModelData modelData = bakedModel.getModelData(renderLevel, BlockPos.ZERO, blockState, ModelData.EMPTY);
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();
        for (RenderType renderType : bakedModel.getRenderTypes(blockState, renderLevel.getRandom(), modelData)){
            WarpedBufferBuilder builder = new WarpedBufferBuilder();
            builder.begin();
            modelRenderer.tesselateBlock(
                    renderLevel,
                    bakedModel,
                    blockState,
                    BlockPos.ZERO,
                    new PoseStack(),
                    builder,
                    true,
                    renderLevel.getRandom(),
                    blockState.getSeed(BlockPos.ZERO),
                    OverlayTexture.NO_OVERLAY,
                    modelData,
                    renderType
            );
            WarpedBufferRenderer bufferRenderer = builder.end();
            if (bufferRenderer.isEmpty()) {
                continue;
            }
            cache.put(renderType, bufferRenderer);
        }
        return this;
    }

    @Override
    public void render(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick) {
        throw new NotImplementedException("Not implemented");
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
        if (!isValid()) {
            return;
        }
        poseStack.pushPose();
        float delta = partialTick.getGameTimeDeltaPartialTick(true);

        Vec3 position = oPos.lerp(visualPos, delta);
        BlockPos blockPos = BlockPos.containing(position);

        Vec3 offset = RenderCompat.applyTransform(poseStack, cameraPos, blockPos, delta, position);

        poseStack.translate(
                - cameraPos.x(),
                - cameraPos.y(),
                - cameraPos.z()
        );
        poseStack.translate(
                offset.x(),
                offset.y(),
                offset.z()
        );
        cache.forEach(
                (renderType, renderer) -> {
                    float cr = Mth.lerp(delta, or, r);
                    float cg = Mth.lerp(delta, og, g);
                    float cb = Mth.lerp(delta, ob, b);
                    float ca = Mth.lerp(delta, oa, a);
                    renderer.setModulate(cr, cg, cb, ca);
                    RenderType type = renderType == RenderType.solid() || renderType == RenderType.cutoutMipped() && ca < 1?
                            RenderType.translucent():
                            renderType;
                    renderer.render(poseStack, bufferSource.getBuffer(type), Minecraft.getInstance().level);
                }
        );
        poseStack.popPose();
    }

    @Override
    public boolean isValid() {
        return blockState != null;
    }
}
