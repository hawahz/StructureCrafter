package io.github.hawah.structure_crafter.client.render.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.ClientHooks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlackboardRenderer extends BlockEntityWithoutLevelRenderer {

    public BlackboardRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemDisplayContext itemDisplayContext,
                             PoseStack poseStack,
                             MultiBufferSource bufferSource,
                             int light,
                             int overlay) {
//        super.renderByItem(stack, itemDisplayContext, poseStack, bufferSource, light, overlay);
        poseStack.pushPose();
        if (stack.has(DataComponentTypeRegistries.BLACKBOARD_WRITING)) {
        }



        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        HumanoidArm mainArm = player.getMainArm();


        if (mainArm.equals(HumanoidArm.RIGHT) && itemDisplayContext.equals(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)) {
            renderBlackboardLeftArm(stack, poseStack, bufferSource, light, overlay);
        } else {
            renderGeneralItemByBakedModel(stack, itemDisplayContext, poseStack, bufferSource, light, overlay, mc, getBakedModel());
        }

        //poseStack.translate(0.5, 0.5, 0.5);

        poseStack.popPose();
    }

    private void renderBlackboardLeftArm(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        poseStack.pushPose();


        LocalPlayer player = Minecraft.getInstance().player;
        int useDuration = stack.getUseDuration(player);
        int ticks = player.getUseItemRemainingTicks();
        float partialTicks = AnimationTickHolder.getPartialTicks();
        float smoothTicks = Mth.lerp(partialTicks, ticks - 1, ticks);
        float f8 = (float) stack.getUseDuration(player) - ((float)player.getUseItemRemainingTicks() - partialTicks + 1.0F);
        boolean isUsingThis = player.isUsingItem() && player.getUseItem().equals(stack);
        float progress = player.isUsingItem()? f8 / (float) useDuration : 0;
        float tickProgress = progress * useDuration;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        PlayerRenderer playerrenderer = (PlayerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().<AbstractClientPlayer>getRenderer(player);


        if (isUsingThis) {
            poseStack.translate(0.5, 0.2, 0.4);

            poseStack.pushPose();
            poseStack.translate(-0.5, -0.1, -0.4);

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YN.rotationDegrees(45));
            float scale = 0.8F;

            poseStack.scale(
                    scale,
                    scale,
                    scale
            );
            poseStack.translate(-0.5, -0.5, -0.5);
            float showRate = 4;
            float showProgress = Mth.clamp(progress * showRate, 0, 1);
            poseStack.translate(
                    1.5 - 0.896 * showProgress * showProgress,
                    1.5 - 1.4*Math.pow(showProgress - 0.5, 2),
                    0.3
            );


            float writeProgress = Mth.clamp(progress * showRate - 1, 0, 3)/3F;
            poseStack.translate(
                    writeProgress/3,
                    0.1*Math.sin(writeProgress*20),
                    -writeProgress/3
            );

            ItemStack pen = Items.FEATHER.getDefaultInstance();
            itemRenderer.renderStatic(
                    pen,
                    ItemDisplayContext.NONE,
                    light,
                    overlay,
                    poseStack,
                    bufferSource,
                    Minecraft.getInstance().level,
                    0
            );

            poseStack.translate(0.5, -0.75, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(35));
            playerrenderer.renderRightHand(poseStack, bufferSource, light, player);
            poseStack.popPose();
        }
        //poseStack.mulPose(Axis.YN.rotationDegrees(tickProgress < 10? progress * 15 : 10F * 15 / useDuration));

        poseStack.pushPose();
        //poseStack.translate(0.5, 0.5, 0.5);
        poseStack.translate(0, -0.1, 0.3);
        poseStack.mulPose(Axis.YN.rotationDegrees(60));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80));
        poseStack.mulPose(Axis.XN.rotationDegrees(15));
        //poseStack.translate(-0.5, -0.5, -0.5);

        playerrenderer.renderLeftHand(poseStack, bufferSource, light, player);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(-15));
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(-0.2, 0.2, -0.8);


        BakedModel bakedModel = getBakedModel();
        for (var model : bakedModel.getRenderPasses(stack, true)) {
            for (var rendertype : model.getRenderTypes(stack, true)) {
                VertexConsumer vertexconsumer;
                vertexconsumer = ItemRenderer.getFoilBufferDirect(bufferSource, rendertype, true, stack.hasFoil());

                itemRenderer.renderModelLists(model, stack, light, overlay, poseStack, vertexconsumer);
            }
        }
        poseStack.popPose();
        poseStack.popPose();
    }

    private BakedModel getBakedModel() {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "addition/blackboard_raw")
        ));
    }

    private static void renderGeneralItemByBakedModel(ItemStack stack,
                                                      ItemDisplayContext itemDisplayContext,
                                                      PoseStack poseStack,
                                                      MultiBufferSource bufferSource,
                                                      int light,
                                                      int overlay,
                                                      Minecraft mc,
                                                      BakedModel bakedModel) {
        ItemRenderer itemRenderer = mc.getItemRenderer();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        bakedModel = ClientHooks.handleCameraTransforms(poseStack, bakedModel, itemDisplayContext, false);
        poseStack.translate(-0.5, -0.5, -0.5);
        if (itemDisplayContext.equals(ItemDisplayContext.GUI)) {
            Lighting.setupForFlatItems();
            light = LightTexture.FULL_BRIGHT;
        }
        for (var model : bakedModel.getRenderPasses(stack, true)) {
            for (var rendertype : model.getRenderTypes(stack, true)) {
                VertexConsumer vertexconsumer;
                vertexconsumer = ItemRenderer.getFoilBufferDirect(bufferSource, rendertype, true, stack.hasFoil());

                itemRenderer.renderModelLists(model, stack, light, overlay, poseStack, vertexconsumer);
            }
        }
        poseStack.popPose();
    }

    private void renderOneHandedBlackboard(PoseStack poseStack,
                                           MultiBufferSource buffer,
                                           int packedLight,
                                           int overlay,
                                           float equippedProgress,
                                           HumanoidArm hand,
                                           float swingProgress,
                                           ItemStack stack) {
        float f = hand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.pushPose();
        poseStack.translate(f * 0.125F, -0.125F, 0.0F);
        if (!Minecraft.getInstance().player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
            this.renderPlayerArm(poseStack, buffer, packedLight, equippedProgress, swingProgress, hand);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(f * 0.51F, -0.08F + equippedProgress * -1.2F, -0.75F);
        float f1 = Mth.sqrt(swingProgress);
        float f2 = Mth.sin(f1 * (float) Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * Mth.sin(f1 * (float) (Math.PI * 2));
        float f5 = -0.3F * Mth.sin(swingProgress * (float) Math.PI);
        poseStack.translate(f * f3, f4 - 0.3F * f2, f5);
        poseStack.mulPose(Axis.XP.rotationDegrees(f2 * -45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(f * f2 * -30.0F));
        this.renderBlackboard(poseStack, buffer, packedLight, stack, overlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    private void renderBlackboard(PoseStack poseStack,
                                  MultiBufferSource buffer,
                                  int packedLight,
                                  ItemStack stack,
                                  int overlay) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = getBakedModel();
        for (var model : bakedModel.getRenderPasses(stack, true)) {
            for (var rendertype : model.getRenderTypes(stack, true)) {
                VertexConsumer vertexconsumer;
                vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, stack.hasFoil());

                itemRenderer.renderModelLists(model, stack, packedLight, overlay, poseStack, vertexconsumer);
            }
        }
        poseStack.popPose();
    }

    private void renderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float equippedProgress, float swingProgress, HumanoidArm side) {
        boolean flag = side != HumanoidArm.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(swingProgress);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * (float) (Math.PI * 2));
        float f4 = -0.4F * Mth.sin(swingProgress * (float) Math.PI);
        poseStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + equippedProgress * -0.6F, f4 + -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));
        AbstractClientPlayer abstractclientplayer = Minecraft.getInstance().player;
        poseStack.translate(f * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
        poseStack.translate(f * 5.6F, 0.0F, 0.0F);
        PlayerRenderer playerrenderer = (PlayerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().<AbstractClientPlayer>getRenderer(abstractclientplayer);
        if (flag) {
            playerrenderer.renderRightHand(poseStack, buffer, packedLight, abstractclientplayer);
        } else {
            playerrenderer.renderLeftHand(poseStack, buffer, packedLight, abstractclientplayer);
        }
    }


}
