package io.github.hawah.structure_crafter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.render.item.BlackboardRenderer;
import io.github.hawah.structure_crafter.client.render.item.ClientItemRendererExtensions;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.item.ITooltipItem;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.util.BlackboardRenderType;
import io.github.hawah.structure_crafter.util.Models;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (!RenderLevelStageEvent.Stage.AFTER_PARTICLES.equals(event.getStage())) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();



        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        DeltaTracker partialTick = StructureCrafterClient.TIMER.warp(event.getPartialTick());

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
        AnimationTickHolder.tick();
        StructureCrafterClient.BLACKBOARD_HANDLER.tick();
        StructureCrafterClient.STRUCTURE_WAND_HANDLER.tick();
        Outliner.tick();
    }
    @SubscribeEvent
    public static void onMouseInputScreen(ScreenEvent.MouseButtonPressed.Pre event) {
//        int button = event.getButton();
//        if (StructureCrafterClient.STRUCTURE_WAND_HANDLER.data.onMouseInput(button, true)) {
//            event.setCanceled(true);
//        }
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
    public static void onContainerScreenEvent(ContainerScreenEvent.Render.Foreground event) {
//        AbstractContainerScreen<?> containerScreen = event.getContainerScreen();
//        Slot slotUnderMouse = containerScreen.getSlotUnderMouse();
//        if (slotUnderMouse == null)
//            return;
//        StructureWandHandler.ItemStackData configData = StructureCrafterClient.STRUCTURE_WAND_HANDLER.data;
//        if (slotUnderMouse.getItem().getItem() instanceof AbstractStructureWand) {
//            configData.init(slotUnderMouse.getItem(), slotUnderMouse.index);
//        } else {
//            configData.clear();
//        }
    }
    @SubscribeEvent
    public static void onMouseScrollScreen(ScreenEvent.MouseScrolled.Pre event) {
//        if (StructureCrafterClient.STRUCTURE_WAND_HANDLER.data.onMouseScroll(event.getScrollDeltaY())) {
//            event.setCanceled(true);
//        }
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
        if (itemStack.getItem() instanceof ITooltipItem tooltipItem) {
            tooltipItem.handleTooltip(tooltipElements);
        }
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "structure_wand"), StructureCrafterClient.STRUCTURE_WAND_HANDLER);
    }

    @SubscribeEvent
    public static void registerRenderers(RegisterClientExtensionsEvent event) {
        event.registerItem(
                ClientItemRendererExtensions.of(new BlackboardRenderer()),
                ItemRegistries.BLACKBOARD
        );
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRegisterModel(ModelEvent.RegisterAdditional event) {
        Models.register(event);
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        LocalPlayer player;
        if (event.getItemStack().is(Items.INK_SAC) && Minecraft.getInstance().player.getOffhandItem().is(ItemRegistries.BLACKBOARD) && Config.BLACKBOARD_RENDER_TYPE.get().equals(BlackboardRenderType.WRITE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void loadCompleted(FMLLoadCompleteEvent event) {
        ModContainer modContainer = ModList.get()
                .getModContainerById(StructureCrafter.MODID)
                .orElseThrow(() -> new IllegalStateException("Structure Crafter Container missing after loadCompleted"));

        Supplier<IConfigScreenFactory> configScreen = () ->
                (mc, previousScreen) -> new BaseConfigScreen(previousScreen, StructureCrafter.MODID);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
    }
}
