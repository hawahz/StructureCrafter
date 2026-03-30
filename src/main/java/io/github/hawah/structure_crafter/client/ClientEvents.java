package io.github.hawah.structure_crafter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (!RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS.equals(event.getStage())) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        DeltaTracker partialTick = event.getPartialTick();

        if (Outliner.hasInstance()) {
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
    public static void onRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();
        ItemStack itemStack = event.getItemStack();
        if (itemStack.is(ItemRegistries.STRUCTURE_WAND)) {
            int t = 1;
            if (!Screen.hasShiftDown()) {
                tooltipElements.add(t, Either.left(LangData.SHIFT.get()));
            } else {
                tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_0.get()));
                tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_1.get()));
                tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_2.get()));
                tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_3.get()));
                tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_4.get()));
                tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_5.get()));
                tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_6.get()));
                tooltipElements.add(t, Either.left(LangData.TOOLTIP_WAND_7.get()));
            }
        }
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "structure_wand"), StructureCrafterClient.STRUCTURE_WAND_HANDLER);
    }
}
