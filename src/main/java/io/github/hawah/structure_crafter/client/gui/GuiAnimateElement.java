package io.github.hawah.structure_crafter.client.gui;

import net.minecraft.client.gui.GuiGraphics;

public abstract class GuiAnimateElement {

    float animateTick = 0;
    boolean activate;

    void active() {
        activate = true;
    }
    int getAnimateTick() {
        return (int) animateTick;
    }
    public abstract void tick();
    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);
}
