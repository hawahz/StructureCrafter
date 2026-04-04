package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.mixin.ScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class BaseScreen extends Screen {

    protected int textureWidth, textureHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;
    private float pausedPartialTick = -1;

    protected BaseScreen(Component title) {
        super(title);
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setTextureSize(int width, int height) {
        textureWidth = width;
        textureHeight = height;
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    @SuppressWarnings("unused")
    protected void setWindowOffset(int xOffset, int yOffset) {
        windowXOffset = xOffset;
        windowYOffset = yOffset;
    }

    @Override
    protected void init() {
        guiLeft = (width - textureWidth) / 2 + 20;
        guiTop = (height - textureHeight) / 2;
        guiLeft += windowXOffset;
        guiTop += windowYOffset;
    }

    @Override
    public void tick() {
        super.tick();
        pausedPartialTick = 0;
    }

    private Iterable<? extends Renderable> getRenderables() {
        return ((ScreenAccessor) this).getRenderables();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        partialTick = AnimationTickHolder.getPartialTicks();

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        renderMenuBackground(guiGraphics);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderWindowPre(guiGraphics, mouseX, mouseY, partialTick);

        for (Renderable renderable : getRenderables())
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);

        renderWindowPost(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderWindowPre(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    protected void renderWindowPost(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
}
