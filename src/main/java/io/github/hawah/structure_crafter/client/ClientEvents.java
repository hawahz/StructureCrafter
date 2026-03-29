package io.github.hawah.structure_crafter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.render.Outliner;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (!RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS.equals(event.getStage())) {
            return;
        }

        LevelRenderer renderer = event.getLevelRenderer();
        PoseStack poseStack = event.getPoseStack();
        Frustum frustum = event.getFrustum();
        RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();

        Camera camera = event.getCamera();
        poseStack.pushPose();
        Vec3 cameraPos = camera.getPosition();

        // 关键点 1: 平移到世界坐标 (比如 x:10, y:70, z:10)
        // 需要减去相机坐标，转为相对渲染位置
        poseStack.translate(10 - cameraPos.x, 70 - cameraPos.y, 10 - cameraPos.z);
//        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//        MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
//
//        blockRenderer.renderSingleBlock(
//                Blocks.ACACIA_FENCE.defaultBlockState(),
//                poseStack,
//                bufferSource,
//                LightTexture.FULL_BRIGHT,
//                OverlayTexture.NO_OVERLAY
//        );
//        poseStack.translate(0, 1, 0);
//        blockRenderer.renderSingleBlock(
//                Blocks.HAY_BLOCK.defaultBlockState(),
//                poseStack,
//                bufferSource,
//                LightTexture.FULL_BRIGHT,
//                OverlayTexture.NO_OVERLAY
//        );
        poseStack.popPose();
        DeltaTracker partialTick = event.getPartialTick();

//        bufferSource.endBatch();

        if (Outliner.hasInstance()) {
//            Outliner.renderThickBox(
//                    poseStack,
//                    renderBuffers.bufferSource().getBuffer(RenderType.debugQuads()),
//                    cameraPos,
//                    0.08F,
//                    172/225F,
//                    219/225F,
//                    221/225F,
//                    1
//            );
            Outliner.getInstance().render(
                    poseStack,
                    renderBuffers.bufferSource().getBuffer(RenderType.debugQuads()),
                    cameraPos,
                    partialTick
            );
        }
        StructureCrafterClient.STRUCTURE_WAND_HANDLER.render(
                poseStack,
                renderBuffers.bufferSource(),
                cameraPos
        );
    }

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        Outliner.tick();
        StructureCrafterClient.BLACKBOARD_HANDLER.tick();
        StructureCrafterClient.STRUCTURE_WAND_HANDLER.tick();
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }

        int button = event.getButton();
        boolean pressed = event.getAction() != 0;
        if (StructureCrafterClient.BLACKBOARD_HANDLER.onMouseInput(button, pressed)) {
            event.setCanceled(true);
        }
        if (StructureCrafterClient.STRUCTURE_WAND_HANDLER.onMouseInput(button, pressed)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }

        double delta = event.getScrollDeltaY();
        if (StructureCrafterClient.BLACKBOARD_HANDLER.onMouseScroll(delta)) {
            event.setCanceled(true);
        }
        if (StructureCrafterClient.STRUCTURE_WAND_HANDLER.onMouseScroll(delta)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        // Register overlays in reverse order
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "structure_wand"), StructureCrafterClient.STRUCTURE_WAND_HANDLER);
    }
}
