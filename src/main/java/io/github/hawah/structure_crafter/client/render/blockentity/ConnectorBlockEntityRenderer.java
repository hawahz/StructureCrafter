package io.github.hawah.structure_crafter.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.util.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConnectorBlockEntityRenderer implements BlockEntityRenderer<TelephoneBlockEntity> {


    @Override
    public void render(TelephoneBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return null;
    }

    @Override
    public void submit(BlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        if (!blockEntity.hasTelephone())
            return;
        BakedModel bakedModel = Models.PHONE.getBakedModel();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(
                switch (blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING)) {
                    case SOUTH -> 180;
                    case EAST -> 90;
                    case WEST -> -90;
                    default -> 0;
                }));
        poseStack.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()), // 或 translucent/cutout
                blockEntity.getBlockState(),
                bakedModel,
                1.0f, 1.0f, 1.0f, // RGB
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                RenderType.solid()
        );

        poseStack.popPose();
    }
}
