package io.github.hawah.structure_crafter.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.block.blockentity.TelephoneBlockEntity;
import io.github.hawah.structure_crafter.client.ClientDataHolder;
import io.github.hawah.structure_crafter.client.render.EaseHelper;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.util.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConnectorBlockEntityRenderer implements BlockEntityRenderer<TelephoneBlockEntity, TelephoneBlockEntityState> {

    public static boolean canRender(TelephoneBlockEntityState blockEntity) {
        if (blockEntity.hasTelephone())
            return true;
        if (!blockEntity.getBlockPos().equals(ClientDataHolder.Picker.pos()) || !blockEntity.facing.getOpposite().equals(ClientDataHolder.Picker.direction()))
            return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return false;
        ItemStack itemStack;
        if(!(itemStack = player.getMainHandItem()).is(ItemRegistries.TELEPHONE_HANDSET) || !itemStack.has(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE)) {
            return false;
        }
        return blockEntity.getBlockPos().equals(itemStack.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY).pos());
    }


    @Override
    public TelephoneBlockEntityState createRenderState() {
        return new TelephoneBlockEntityState();
    }

    @Override
    public void extractRenderState(TelephoneBlockEntity blockEntity, TelephoneBlockEntityState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.facing = blockEntity.facing;
        renderState.hasTelephone = blockEntity.hasTelephone();
        renderState.blockPos = blockEntity.getBlockPos();
        renderState.blockEntityType = blockEntity.getType();
        renderState.partialTicks = partialTick;
    }

    @Override
    public void submit(TelephoneBlockEntityState renderState,
                       PoseStack poseStack,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState) {
        if (!canRender(renderState)) {
            return;
        }
        QuadCollection bakedModel = Models.PHONE.getBakedModel();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(
                switch (renderState.facing) {
                    case SOUTH -> 180;
                    case EAST -> 90;
                    case WEST -> -90;
                    default -> 0;
        }));
        poseStack.translate(-0.5, -0.5, -0.5);
        float lerpAlpha = Mth.lerp(1-EaseHelper.easeInPow(1-pingPong((AnimationTickHolder.getTicks() + renderState.partialTicks) / 20F), 2), 0.6F, 0.8F);
        float aChannel = renderState.hasTelephone()? 1 : lerpAlpha;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.translucentMovingBlock());
        VertexConsumer warped = new VertexConsumer() {
            @Override
            public VertexConsumer addVertex(float x, float y, float z) {
                return consumer.addVertex(x, y, z);
            }

            @Override
            public VertexConsumer setColor(int red, int green, int blue, int alpha) {
                return consumer.setColor((int)(red * aChannel), (int)(green * aChannel), (int)(blue * aChannel), (int)(alpha * aChannel));
            }

            @Override
            public VertexConsumer setColor(int color) {
                return consumer.setColor(color);
            }

            @Override
            public VertexConsumer setUv(float u, float v) {
                return consumer.setUv(u, v);
            }

            @Override
            public VertexConsumer setUv1(int u, int v) {
                return consumer.setUv1(u, v);
            }

            @Override
            public VertexConsumer setUv2(int u, int v) {
                return consumer.setUv2(u, v);
            }

            @Override
            public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
                return consumer.setNormal(normalX, normalY, normalZ);
            }

            @Override
            public VertexConsumer setLineWidth(float lineWidth) {
                return consumer.setLineWidth(lineWidth);
            }
        };
//        ModelBlockRenderer.renderModel(
//                poseStack.last(),
//                warped, // 或 translucent/cutout
//                renderState.getBlockState(),
//                bakedModel,
//                1.0f, 1.0f, 1.0f, // RGB
//                packedLight,
//                packedOverlay,
//                ModelData.EMPTY,
//                RenderType.solid()
//        );

//        nodeCollector.submitModel(
//                bakedModel,
//                renderState,
//                poseStack,
//                RenderTypes.translucentMovingBlock(),
//                0xF000F0,
//                0xF000F0,
//                0xF000F0,
//                null,
//                0xF000F0,
//                null
//        );

        poseStack.popPose();
    }

    static float pingPong(float t) {
        return 1.0f - Math.abs((t % 2.0f) - 1.0f);
    }
}
