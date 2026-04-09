package io.github.hawah.structure_crafter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.block.blockentity.BlockEntityRegistry;
import io.github.hawah.structure_crafter.client.render.OverRenderType;
import io.github.hawah.structure_crafter.client.render.blockentity.ConnectorBlockEntityRenderer;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.TelephoneHandsetComponent;
import io.github.hawah.structure_crafter.item.ITooltipItem;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.item.TelephoneHandset;
import io.github.hawah.structure_crafter.networking.PlayerInventoryRemoveItemPacket;
import io.github.hawah.structure_crafter.networking.ServerboundTelephoneChanged;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.BlackboardRenderType;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.Models;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterParticles event) {
        PoseStack poseStack = event.getPoseStack();
        RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();
        DeltaTracker partialTick = StructureCrafterClient.TIMER_NORMAL.warp(Minecraft.getInstance().getDeltaTracker());
        MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
        if (Outliner.hasInstance()) {
            Outliner.getInstance().render(
                    poseStack,
                    bufferSource.getBuffer(RenderTypes.debugQuads()),
                    cameraPos,
                    partialTick
            );
        }
        StructureCrafterClient.STRUCTURE_WAND_HANDLER.render(
                poseStack,
                bufferSource,
                cameraPos
        );
        //bufferSource.endBatch();

        Outliner.getInstance().renderOverlay(
                poseStack,
                bufferSource.getBuffer(OverRenderType.OVERLAY_LINES),
                cameraPos,
                partialTick
        );
        StructureCrafterClient.TELEPHONE_WIRE_RENDERER.render(
                poseStack,
                bufferSource.getBuffer(RenderTypes.entityCutout(Textures.TELEPHONE_WIRE.getResource())),
                cameraPos,
                0.2F,
                partialTick.getGameTimeDeltaPartialTick(true)
        );

        bufferSource.endBatch();
    }

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        AnimationTickHolder.tick();
        KeyBinding.tick();
        StructureCrafterClient.BLACKBOARD_HANDLER.tick();
        StructureCrafterClient.STRUCTURE_WAND_HANDLER.tick();
        Outliner.tick();
        StructureCrafterClient.TELEPHONE_WIRE_RENDERER.tick();
        TelephoneHandset.clientTick();
    }

    @SubscribeEvent
    public static void onMouseInputScreen(ScreenEvent.MouseButtonPressed.Pre event) {
        int button = event.getButton();
        Screen screen = event.getScreen();
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            ItemStack hoveredItem = containerScreen.getSlotUnderMouse() != null ? containerScreen.getSlotUnderMouse().getItem() : null;
            ItemStack carried = containerScreen.getMenu().getCarried();
            boolean cancelInteract = (hoveredItem != null &&
                    button >= 0 && button <= 2 &&
                    hoveredItem.is(ItemRegistries.TELEPHONE_HANDSET) &&
                    !(screen instanceof InventoryScreen)
            );
            if (cancelInteract) {
                event.setCanceled(true);
            } else if (carried.is(ItemRegistries.TELEPHONE_HANDSET) && hoveredItem == null) {
                Networking.sendToServer(new PlayerInventoryRemoveItemPacket(carried.copy()));
                Networking.sendToServer(new ServerboundTelephoneChanged(carried.getOrDefault(DataComponentTypeRegistries.TELEPHONE_HANDSET_SOURCE, TelephoneHandsetComponent.EMPTY)));
                carried.shrink(1);
            }
        }

    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        int button = event.getButton();
        boolean pressed = event.getAction() != 0;
        if (KeyBinding.KeyBuffer.onMousePressed(button, pressed)) {
            event.setCanceled(true);
        }
    }

    private static ItemStack hoveredItem = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onContainerScreenEvent(ContainerScreenEvent.Render.Foreground event) {
        AbstractContainerScreen<?> containerScreen = event.getContainerScreen();
        Slot slotUnderMouse = containerScreen.getSlotUnderMouse();
        if (slotUnderMouse == null)
            return;
        hoveredItem = slotUnderMouse.getItem();
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

        if (KeyBinding.KeyBuffer.onMouseScrolled(delta)) {
            event.setCanceled(true);
        }
//        if (StructureCrafterClient.BLACKBOARD_HANDLER.onMouseScroll(delta)) {
//            event.setCanceled(true);
//        }
        if (StructureCrafterClient.STRUCTURE_WAND_HANDLER.onMouseScroll(delta)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Outliner.getInstance().clear();
        StructureCrafterClient.BLACKBOARD_HANDLER.discard();
    }

    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();
        ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem() instanceof ITooltipItem tooltipItem) {
            tooltipItem.handleTooltip(tooltipElements, itemStack);
        }
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(StructureCrafter.MODID, "structure_wand"), StructureCrafterClient.STRUCTURE_WAND_HANDLER);
        event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(StructureCrafter.MODID, "key_tip_hud"), StructureCrafterClient.KEY_TIP_HUD);

    }

    @SubscribeEvent
    public static void registerRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(StructureCrafter.MODID, "Blackboard"),

                );
    }

    @SubscribeEvent
    public static void registerBlockRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BlockEntityRegistry.TELEPHONE_BLOCK_ENTITY.get(),
                (ctx) -> new ConnectorBlockEntityRenderer()
        );
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRegisterModel(ModelEvent.RegisterLoaders event) {
        Models.register(event);
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        LocalPlayer player;
        if (event.getItemStack().is(Items.INK_SAC) && Minecraft.getInstance().player.getOffhandItem().is(ItemRegistries.BLACKBOARD) && Config.BLACKBOARD_RENDER_TYPE.get().equals(BlackboardRenderType.WRITE)) {
            event.setCanceled(true);
        }
    }
}
