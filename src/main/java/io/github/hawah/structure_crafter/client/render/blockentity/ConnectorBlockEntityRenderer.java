package io.github.hawah.structure_crafter.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.block.blockentity.ConnectorBlockEntity;
import io.github.hawah.structure_crafter.util.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConnectorBlockEntityRenderer implements BlockEntityRenderer<ConnectorBlockEntity> {


    @Override
    public void render(ConnectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
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
