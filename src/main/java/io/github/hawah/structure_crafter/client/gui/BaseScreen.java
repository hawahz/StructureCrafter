package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.mixin.ScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class BaseScreen extends Screen {

    protected int textureWidth, textureHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;

    protected final float getScale() {
        return scale;
    }

    protected final void setScale(float scale) {
        this.scale = scale;
    }

    private float scale = 1;
    private float pausedPartialTick = -1;
    protected boolean disableRenderComponents = false;

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
        guiLeft = (width - textureWidth) / 2;
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
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        mouseX = (int) ((mouseX - width/2F) / scale + width/2F);
        mouseY = (int) ((mouseY - height/2F) / scale + height/2F);

        partialTick = AnimationTickHolder.getPartialTicks();

        PoseStack poseStack = guiGraphics.pose();

        renderMenuBackground(guiGraphics);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderWindowPre(guiGraphics, mouseX, mouseY, partialTick);

        poseStack.pushPose();
        applyScaleTransform(poseStack);
        if (!disableRenderComponents) {
            for (Renderable renderable : getRenderables())
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        poseStack.popPose();

        renderWindowPost(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = (int) ((mouseX - width/2F) / scale + width/2F);
        mouseY = (int) ((mouseY - height/2F) / scale + height/2F);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void applyScaleTransform(PoseStack poseStack) {
        if (scale == 1) {
            return;
        }
        poseStack.translate(guiLeft +textureWidth/2F, guiTop +textureHeight/2F, 0);
        poseStack.scale(getScale(), getScale(), getScale());
        poseStack.translate(-guiLeft-textureWidth/2F, -guiTop-textureHeight/2F, 0);
    }

    protected Vec2 getOriginalMousePos(int mouseX, int mouseY) {
        return new Vec2(
                (int) ((mouseX - width/2F) * scale + width/2F),
                (int) ((mouseY - height/2F) * scale + height/2F)
        );
    }

    protected void renderWindowPre(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    protected void renderWindowPost(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
}
