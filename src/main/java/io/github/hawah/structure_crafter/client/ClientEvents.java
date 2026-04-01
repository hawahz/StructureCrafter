package io.github.hawah.structure_crafter.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.render.item.BlackboardRenderer;
import io.github.hawah.structure_crafter.client.render.item.ClientItemRendererExtensions;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.item.ITooltipItem;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

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
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "addition/blackboard_raw")
        ));
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        LocalPlayer player;
        if (event.getItemStack().is(Items.INK_SAC) && (player = Minecraft.getInstance().player).getUseItem().is(ItemRegistries.BLACKBOARD)) {
            event.setCanceled(true);
        }
    }
}
