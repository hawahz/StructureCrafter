package io.github.hawah.structure_crafter.client.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.*;

public class LabelButton extends Button {
    public LabelButton(Button.Builder builder) {
        super(builder);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        new Color(0x00CF68);
        int i = this.isHoveredOrFocused()?
                this.isActive()?
                        0xFFFFFF :
                        0x00CF68 :
                0xBCBCBC;
        this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
