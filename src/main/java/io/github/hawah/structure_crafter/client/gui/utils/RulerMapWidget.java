package io.github.hawah.structure_crafter.client.gui.utils;

import io.github.hawah.structure_crafter.lib.client.gui.BaseScreen;
import io.github.hawah.structure_crafter.lib.client.gui.element.DraggableFloatWidget;
import io.github.hawah.structure_crafter.lib.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

public class RulerMapWidget extends DraggableFloatWidget {

    public enum State {
        CHAIN,
        NON_CHAIN,
    }

    public State state = State.NON_CHAIN;

    public RulerMapWidget(int x, int y, int width, int height, ResourceLocation texture, int startX, int startY, Rect2i parentRect) {
        super(x, y, width, height, texture, startX, startY, parentRect);
    }

    public RulerMapWidget(int x, int y, int width, int height, ResourceLocation texture, int startX, int startY, Rect2i parentRect, Rect2i limitArea) {
        super(x, y, width, height, texture, startX, startY, parentRect, limitArea);
    }

    public RulerMapWidget(int x, int y, Textures textureData, Rect2i parentRect) {
        super(x, y, textureData, parentRect);
    }

    public RulerMapWidget(int x, int y, Textures textureData, Rect2i parentRect, Rect2i limitArea) {
        super(x, y, textureData, parentRect, limitArea);
    }

    @Override
    protected void renderAbove(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderAbove(guiGraphics, mouseX, mouseY, partialTick);
        double tick = (AnimationTickHolder.getTicks(true) + partialTick)/30 % 3;

        if (state == State.CHAIN) {
            BaseScreen.line(
                    guiGraphics,
                    this.getX() + 14,
                    this.getY() + 45 - 5,
                    this.getX() + 50,
                    this.getY() + 43 - 5,
                    0xFFac8278,
                    tick
            );
            BaseScreen.line(
                    guiGraphics,
                    this.getX() + 50,
                    this.getY() + 43 - 5,
                    this.getX() + 48,
                    this.getY() + 14 - 5,
                    0xFFac8278,
                    tick - 1
            );
            BaseScreen.blit(
                    guiGraphics,
                    Textures.RULER_DECO_SIGN.getResource(),
                    this.getX() + 14 - Textures.RULER_DECO_SIGN.getWidth() / 2,
                    this.getY() + 45 - 5 - Textures.RULER_DECO_SIGN.getHeight() / 2,
                    Textures.RULER_DECO_SIGN.getStartX(),
                    Textures.RULER_DECO_SIGN.getStartY(),
                    Textures.RULER_DECO_SIGN.getWidth(),
                    Textures.RULER_DECO_SIGN.getHeight()
            );
            BaseScreen.blit(
                    guiGraphics,
                    Textures.RULER_DECO_SIGN.getResource(),
                    this.getX() + 50 - Textures.RULER_DECO_SIGN.getWidth() / 2,
                    this.getY() + 43 - 5 - Textures.RULER_DECO_SIGN.getHeight() / 2,
                    Textures.RULER_DECO_SIGN.getStartX(),
                    Textures.RULER_DECO_SIGN.getStartY(),
                    Textures.RULER_DECO_SIGN.getWidth(),
                    Textures.RULER_DECO_SIGN.getHeight()
            );
            BaseScreen.blit(
                    guiGraphics,
                    Textures.RULER_DECO_SIGN.getResource(),
                    this.getX() + 48 - Textures.RULER_DECO_SIGN.getWidth() / 2,
                    this.getY() + 14 - 5 - Textures.RULER_DECO_SIGN.getHeight() / 2,
                    Textures.RULER_DECO_SIGN.getStartX(),
                    Textures.RULER_DECO_SIGN.getStartY(),
                    Textures.RULER_DECO_SIGN.getWidth(),
                    Textures.RULER_DECO_SIGN.getHeight()
            );
        } else if (state == State.NON_CHAIN) {
            BaseScreen.line(
                    guiGraphics,
                    this.getX() + 14,
                    this.getY() + 45 - 5,
                    this.getX() + 50,
                    this.getY() + 43 - 5,
                    0xFFac8278,
                    tick
            );
            BaseScreen.line(
                    guiGraphics,
                    this.getX() + 14,
                    this.getY() + 45 - 5,
                    this.getX() + 12,
                    this.getY() + 16 - 5,
                    0xFFac8278,
                    tick - 1
            );
            BaseScreen.blit(
                    guiGraphics,
                    Textures.RULER_DECO_SIGN.getResource(),
                    this.getX() + 14 - Textures.RULER_DECO_SIGN.getWidth() / 2,
                    this.getY() + 45 - 5 - Textures.RULER_DECO_SIGN.getHeight() / 2,
                    Textures.RULER_DECO_SIGN.getStartX(),
                    Textures.RULER_DECO_SIGN.getStartY(),
                    Textures.RULER_DECO_SIGN.getWidth(),
                    Textures.RULER_DECO_SIGN.getHeight()
            );
            BaseScreen.blit(
                    guiGraphics,
                    Textures.RULER_DECO_SIGN.getResource(),
                    this.getX() + 50 - Textures.RULER_DECO_SIGN.getWidth() / 2,
                    this.getY() + 43 - 5 - Textures.RULER_DECO_SIGN.getHeight() / 2,
                    Textures.RULER_DECO_SIGN.getStartX(),
                    Textures.RULER_DECO_SIGN.getStartY(),
                    Textures.RULER_DECO_SIGN.getWidth(),
                    Textures.RULER_DECO_SIGN.getHeight()
            );
            BaseScreen.blit(
                    guiGraphics,
                    Textures.RULER_DECO_SIGN.getResource(),
                    this.getX() + 12 - Textures.RULER_DECO_SIGN.getWidth() / 2,
                    this.getY() + 16 - 5 - Textures.RULER_DECO_SIGN.getHeight() / 2,
                    Textures.RULER_DECO_SIGN.getStartX(),
                    Textures.RULER_DECO_SIGN.getStartY(),
                    Textures.RULER_DECO_SIGN.getWidth(),
                    Textures.RULER_DECO_SIGN.getHeight()
            );
        }

    }
}
