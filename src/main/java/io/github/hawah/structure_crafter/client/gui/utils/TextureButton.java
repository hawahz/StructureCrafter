package io.github.hawah.structure_crafter.client.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TextureButton extends AbstractWidget {

    private final Runnable onPress;
    private final ResourceLocation texture;
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
            ResourceLocation texture,
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

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        this.onPress.run();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blit(
                texture,
                this.getX(),
                this.getY(),
                this.isActive()?
                        this.isHoveredOrFocused()?
                                hoverStartX :
                                normalStartX :
                        inactiveStartX,
                this.isActive()?
                        this.isHoveredOrFocused()?
                                hoverStartY :
                                normalStartY:
                        inactiveStartY,
                this.getWidth(),
                this.getHeight()
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
