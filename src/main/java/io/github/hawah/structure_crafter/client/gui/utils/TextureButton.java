package io.github.hawah.structure_crafter.client.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.hawah.structure_crafter.client.gui.BaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class TextureButton extends AbstractWidget {

    private final Runnable onPress;
    private final Identifier texture;
    private final int normalStartX;
    private final int hoverStartX;
    private final int inactiveStartX;
    private final int normalStartY;
    private final int hoverStartY;
    private final int inactiveStartY;

    public TextureButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Identifier texture,
            int normalStartX,
            int normalStartY,
            int hoverStartX,
            int hoverStartY,
            int inactiveStartX,
            int inactiveStartY,
            Runnable onPress
    ) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.texture = texture;
        this.normalStartX = normalStartX;
        this.hoverStartX = hoverStartX;
        this.inactiveStartX = inactiveStartX;
        this.normalStartY = normalStartY;
        this.hoverStartY = hoverStartY;
        this.inactiveStartY = inactiveStartY;
    }

    public void onClick(double mouseX, double mouseY, int button) {
        this.onClick(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        this.onPress.run();
        super.onClick(event, isDoubleClick);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
//        RenderSystem.enableBlend();
//        RenderSystem.enableDepthTest();
        BaseScreen.blit(
                guiGraphics,
                texture,
                this.getX(),
                this.getY(),
                (this.isActive()?
                        this.isHovered()?
                                hoverStartX :
                                normalStartX :
                        inactiveStartX),
                (this.isActive()?
                        this.isHovered()?
                                hoverStartY :
                                normalStartY:
                        inactiveStartY),
                this.getWidth(),
                this.getHeight(),
                0xFFFFFF << 4 | (int) (this.alpha*255)
        );

        if (this.getMessage().getString().isEmpty() || !this.isHovered())
            return;
        guiGraphics.renderTooltip(
                Minecraft.getInstance().font,
                List.of((ClientTooltipComponent) this.getMessage()),
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }


    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }
}
