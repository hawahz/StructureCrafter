package io.github.hawah.structure_crafter.client.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TextureToggleButton extends AbstractWidget {

    private final boolean enableToggleUp;
    public Runnable onPress;
    private final ResourceLocation texture;
    private final int toggleStartX;
    private final int originStartX;
    private final int hoverStartX;
    private final int toggleHoverStartX;
    private final int toggleStartY;
    private final int originStartY;
    private final int hoverStartY;
    private final int toggleHoverStartY;

    private final Component messageToggled;

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    private boolean toggled = false;

    public TextureToggleButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Component messageToggled,
            ResourceLocation texture,
            int originStartX,
            int originStartY,
            int hoverStartX,
            int hoverStartY,
            int toggleHoverStartX,
            int toggleHoverStartY,
            int toggleStartX,
            int toggleStartY,
            Runnable onPress
    ) {
        this(
                x,
                y,
                width,
                height,
                message,
                messageToggled,
                texture,
                originStartX,
                originStartY,
                hoverStartX,
                hoverStartY,
                toggleHoverStartX,
                toggleHoverStartY,
                toggleStartX,
                toggleStartY,
                true,
                onPress
        );
    }

    public TextureToggleButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Component messageToggled,
            ResourceLocation texture,
            int originStartX,
            int originStartY,
            int hoverStartX,
            int hoverStartY,
            int toggleHoverStartX,
            int toggleHoverStartY,
            int toggleStartX,
            int toggleStartY,
            boolean enableToggleUp,
            Runnable onPress
    ) {
        super(x, y, width, height, message);
        this.enableToggleUp = enableToggleUp;
        this.onPress = onPress;
        this.texture = texture;
        this.originStartX = originStartX;
        this.hoverStartX = hoverStartX;
        this.toggleHoverStartX = toggleHoverStartX;
        this.originStartY = originStartY;
        this.hoverStartY = hoverStartY;
        this.toggleHoverStartY = toggleHoverStartY;
        this.toggleStartX = toggleStartX;
        this.toggleStartY = toggleStartY;
        this.messageToggled = messageToggled;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (toggled && !enableToggleUp) {
            setFocused(false);
            return;
        }
        this.onPress.run();
        toggled = !toggled;
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
                this.toggled?
                        this.isHovered()?
                                toggleHoverStartX :
                                toggleStartX :
                        this.isHovered()?
                                hoverStartX:
                                originStartX,
                this.toggled?
                        this.isHovered()?
                                toggleHoverStartY :
                                toggleStartY :
                        this.isHovered()?
                                hoverStartY:
                                originStartY,
                this.getWidth(),
                this.getHeight()
        );

        if (this.getMessage().getString().isEmpty() || !this.isHovered())
            return;
        guiGraphics.renderComponentTooltip(
                Minecraft.getInstance().font,
                List.of(isToggled()?this.messageToggled: this.getMessage()),
                mouseX,
                mouseY
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
