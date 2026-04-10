package io.github.hawah.structure_crafter.client.gui;

import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.mixin.ScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3x2fStack;

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

        Matrix3x2fStack poseStack = guiGraphics.pose();

        renderMenuBackground(guiGraphics);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderWindowPre(guiGraphics, mouseX, mouseY, partialTick);

        poseStack.pushMatrix();
        applyScaleTransform(poseStack);
        if (!disableRenderComponents) {
            for (Renderable renderable : getRenderables())
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        poseStack.popMatrix();

        renderWindowPost(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {

        int mouseX = (int) ((event.x() - width/2F) / scale + width/2F);
        int mouseY = (int) ((event.y() - height/2F) / scale + height/2F);
        return super.mouseClicked(new MouseButtonEvent(mouseX, mouseY, event.buttonInfo()), isDoubleClick);
    }

    protected void applyScaleTransform(Matrix3x2fStack poseStack) {
        if (scale == 1) {
            return;
        }
        poseStack.translate(guiLeft +textureWidth/2F, guiTop +textureHeight/2F);
        poseStack.scale(getScale(), getScale());
        poseStack.translate(-guiLeft-textureWidth/2F, -guiTop-textureHeight/2F);
    }

    protected Vec2 getOriginalMousePos(int mouseX, int mouseY) {
        return new Vec2(
                (int) ((mouseX - width/2F) * scale + width/2F),
                (int) ((mouseY - height/2F) * scale + height/2F)
        );
    }

    public static void blit(GuiGraphics guiGraphics,
                     Identifier texture,
                     int x,
                     int y,
                     int u,
                     int v,
                     int width,
                     int height,
                     int r,
                     int g,
                     int b,
                     int a) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                u/256F,
                v/256F,
                width,
                height,
                256,
                256,
                a << 24 | r << 16 | g << 8 | b
        );
    }

    public static void blit(GuiGraphics guiGraphics,
                     Identifier texture,
                     int x,
                     int y,
                     int u,
                     int v,
                     int width,
                     int height,
                     float r,
                     float g,
                     float b,
                     float a) {
        blit(guiGraphics, texture, x, y, u, v, width, height, (int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public static void blit(GuiGraphics guiGraphics,
                     Identifier texture,
                     int x,
                     int y,
                     int u,
                     int v,
                     int width,
                     int height) {
        blit(guiGraphics, texture, x, y, u, v, width, height, 1, 1, 1, 1);
    }

    public static void blit(GuiGraphics guiGraphics,
                     Identifier texture,
                     int x,
                     int y,
                     int u,
                     int v,
                     int width,
                     int height,
                     int argb) {
        blit(guiGraphics, texture, x, y, u, v, width, height, argb & 0xFF, (argb >> 8) & 0xFF, (argb >> 16) & 0xFF, (argb >> 24) & 0xFF);
    }

    protected void renderWindowPre(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    protected void renderWindowPost(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
}
