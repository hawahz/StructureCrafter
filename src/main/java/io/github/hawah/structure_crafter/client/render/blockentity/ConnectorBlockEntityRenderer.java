package io.github.hawah.structure_crafter.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConnectorBlockEntityRenderer implements BlockEntityRenderer<TelephoneBlockEntity> {

    public static boolean canRender(TelephoneBlockEntity blockEntity) {
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
    public void render(TelephoneBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!canRender(blockEntity)) {
            return;
        }

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
        float lerpAlpha = Mth.lerp(1-EaseHelper.easeInPow(1-pingPong((AnimationTickHolder.getTicks() + partialTick) / 20F), 2), 0.6F, 0.8F);
        float aChannel = blockEntity.hasTelephone()? 1 : lerpAlpha;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
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
        };
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                warped, // 或 translucent/cutout
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

    static float pingPong(float t) {
        return 1.0f - Math.abs((t % 2.0f) - 1.0f);
    }
}
