package io.github.hawah.structure_crafter.client.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class ImplTextureButton extends AbstractWidget {

    private final Runnable onPress;
    private final ResourceLocation texture;
    private final int normalStartX;
    private final int hoverStartX;
    private final int inactiveStartX;
    private final int normalStartY;
    private final int hoverStartY;
    private final int inactiveStartY;

    public ImplTextureButton(
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

    public ImplTextureButton(
            int x,
            int y,
            Textures textureData,
            int hoverStartX,
            int hoverStartY,
            int inactiveStartX,
            int inactiveStartY,
            Component message,
            Runnable onPress
    ) {
        this(x,
                y,
                textureData.getWidth(),
                textureData.getHeight(),
                message,
                textureData.getResource(),
                textureData.getStartX(),
                textureData.getStartY(),
                hoverStartX,
                hoverStartY,
                inactiveStartX,
                inactiveStartY,
                onPress
        );
    }

    public ImplTextureButton(
            int x,
            int y,
            Textures textureData,
            int inactiveStartX,
            int inactiveStartY,
            Component message,
            Runnable onPress
    ) {
        this(x,
                y,
                textureData,
                textureData.getStartX(),
                textureData.getStartY(),
                inactiveStartX,
                inactiveStartY,
                message,
                onPress
        );
    }

    public ImplTextureButton(
            int x,
            int y,
            Textures textureData,
            Component message,
            Runnable onPress
    ) {
        this(x,
                y,
                textureData,
                textureData.getStartX(),
                textureData.getStartY(),
                message,
                onPress
        );
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
                        this.isHovered()?
                                hoverStartX :
                                normalStartX :
                        inactiveStartX,
                this.isActive()?
                        this.isHovered()?
                                hoverStartY :
                                normalStartY:
                        inactiveStartY,
                this.getWidth(),
                this.getHeight()
        );

        if (this.getMessage().getString().isEmpty() || !this.isHovered())
            return;
        guiGraphics.renderComponentTooltip(
                Minecraft.getInstance().font,
                List.of(this.getMessage()),
                mouseX,
                mouseY
        );

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}
