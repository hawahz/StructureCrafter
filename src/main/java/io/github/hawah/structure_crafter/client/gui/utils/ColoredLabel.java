package io.github.hawah.structure_crafter.client.gui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.awt.*;

public class ColoredLabel extends GuiAnimateElement{

    private Component text;
    private final int duration;
    private final int delay;
    private final Color targetColor;
    private final Color sourceColor;
    private final int x;
    private final int y;


    public ColoredLabel(Component text, int delay, int duration, Color sourceColor, Color targetColor, int x, int y) {
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
//        if (animateTick > delay + duration) {
//            animateTick = (animateTick - delay - duration) % duration + delay + duration;
//        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!activate) {
            return;
        }
        if (animateTick > delay + duration) {
            return;
        }

        int r, g, b, a;

        if (animateTick < delay) {
            r = sourceColor.getRed();
            g = sourceColor.getGreen();
            b = sourceColor.getBlue();
            a = sourceColor.getAlpha();
        } else if (animateTick - delay < duration) {
            float delta = (animateTick - delay + partialTicks) / duration;
            r = Mth.lerpInt(delta, sourceColor.getRed(), targetColor.getRed());
            g = Mth.lerpInt(delta, sourceColor.getGreen(), targetColor.getGreen());
            b = Mth.lerpInt(delta, sourceColor.getBlue(), targetColor.getBlue());
            a = Mth.lerpInt(delta, sourceColor.getAlpha(), targetColor.getAlpha());
        } else {
            r = targetColor.getRed();
            g = targetColor.getGreen();
            b = targetColor.getBlue();
            a = targetColor.getAlpha();
        }

        a = Math.max(a, 9);
        graphics.drawStringWithBackdrop(
                Minecraft.getInstance().font,
                text,
                x - Minecraft.getInstance().font.width(text)/2,
                y,
                Minecraft.getInstance().font.width(text),
                FastColor.ARGB32.color(a, r, g, b)
        );


    }

    public void setText(Component text) {
        this.text = text;
    }
}
