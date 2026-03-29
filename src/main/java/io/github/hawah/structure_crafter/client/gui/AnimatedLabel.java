package io.github.hawah.structure_crafter.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.*;

public class AnimatedLabel extends GuiAnimateElement{

    private Component text;
    private final int duration;
    private final int delay;
    private final Color targetColor;
    private final Color sourceColor;
    private final int x;
    private final int y;


    public AnimatedLabel(Component text, int delay, int duration, Color targetColor, Color sourceColor, int x, int y) {
        this.text = text;
        this.duration = duration;
        this.delay = delay;
        this.targetColor = targetColor;
        this.sourceColor = sourceColor;
        this.x = x;
        this.y = y;
        this.activate = false;
    }

    public void reset() {
        animateTick = 0;
        activate = true;
    }

    @Override
    public void tick() {
        animateTick++;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!activate) {
            return;
        }

        int r, g, b, a;
        animateTick += partialTicks == 0? 1 : 0;
        if (animateTick > delay + duration) {
            animateTick = (animateTick - delay - duration) % duration + delay + duration;
        }

        if (animateTick < delay) {
            r = sourceColor.getRed();
            g = sourceColor.getGreen();
            b = sourceColor.getBlue();
            a = sourceColor.getAlpha();
        } else if (animateTick - delay < duration) {
            double delta = (animateTick - delay + partialTicks) / duration;
            r = (int) Mth.lerp(delta, sourceColor.getRed(), targetColor.getRed());
            g = (int) Mth.lerp(delta, sourceColor.getGreen(), targetColor.getGreen());
            b = (int) Mth.lerp(delta, sourceColor.getBlue(), targetColor.getBlue());
            a = (int) Mth.lerp(delta, sourceColor.getAlpha(), targetColor.getAlpha());
        } else {
            r = targetColor.getRed();
            g = targetColor.getGreen();
            b = targetColor.getBlue();
            a = targetColor.getAlpha();
        }

        graphics.drawString(
                Minecraft.getInstance().font,
                text,
                x,
                y,
                (a << 24) | (r << 16) | (g << 8) | b,
                false
        );

    }

    public void setText(Component text) {
        this.text = text;
    }
}
