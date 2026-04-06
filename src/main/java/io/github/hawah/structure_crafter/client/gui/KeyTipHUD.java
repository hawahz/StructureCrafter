package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.platform.Window;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class KeyTipHUD extends Screen implements LayeredDraw.Layer {

    private boolean initialized = false;

    public KeyTipHUD() {
        super(Component.literal("key_tip_hud"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {

        Window mainWindow = Minecraft.getInstance().getWindow();
        if (!initialized) {
            init(Minecraft.getInstance(), mainWindow.getGuiScaledWidth(), mainWindow.getGuiScaledHeight());
            initialized = true;
        }

//        int startY = Math.max(mainWindow.getGuiScaledWidth() - 32, 0);
        int startY = mainWindow.getGuiScaledHeight() - 36;

        for (KeyBinding value : KeyBinding.values()) {
            int startX = Math.max(mainWindow.getGuiScaledWidth() - 32, 0);
            if (!value.canDisplay()) {
                continue;
            }
            for (KeyBinding.KeyNode key : value.keys) {
                if (key.isActive() && !KeyBinding.KeyNode.RIGHT.equals(key) && !KeyBinding.KeyNode.LEFT.equals(key) && !KeyBinding.KeyNode.SCROLL.equals(key)) {
                    continue;
                }
                Textures.Builder builder = Textures.KEYMAP.builder();
                builder.variant(key.ordinal());
                guiGraphics.blit(
                        builder.getResource(),
                        startX,
                        startY,
                        builder.getStartX(),
                        builder.getStartY(),
                        builder.getWidth(),
                        builder.getHeight()
                );
                startX -= 16;
            }
            Font font = Minecraft.getInstance().font;
            Component validDescription = value.getValidDescription();
            guiGraphics.drawString(
                    font,
                    validDescription,
                    startX - font.width(validDescription) + 10,
                    startY + 8 - font.lineHeight/2,
                    0xFFFFFF,
                    true
            );
            startY -= 20;
        }
    }
}
