package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import io.github.hawah.structure_crafter.mixin.ScreenAccessor;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class BaseScreen extends Screen {

    protected int textureWidth, textureHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;
    private final List<AbstractWidget> lazyRegisterComponents = new ArrayList<>();

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
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 按照显示的覆盖顺序来添加符合逻辑的组件。后加入的组件会被渲染在更上层，逻辑上也会更先被触发
     */
    protected void addSortedRenderWidget(AbstractWidget widget) {
        this.lazyRegisterComponents.add(widget);
    }

    protected void finishRegister() {
        for (int i = 0; i < lazyRegisterComponents.size(); i++) {
            this.addRenderableOnly(lazyRegisterComponents.get(i));
            this.addWidget(lazyRegisterComponents.get(lazyRegisterComponents.size()-1-i));
        }
        this.lazyRegisterComponents.clear();
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

    public static void blit(GuiGraphics guiGraphics,
                            ResourceLocation resource,
                            int x,
                            int y,
                            int u,
                            int v,
                            int textureWidth,
                            int textureHeight) {
        guiGraphics.blit(
                resource,
                x,
                y,
                u,
                v,
                textureWidth,
                textureHeight
        );
    }

    public static void line(GuiGraphics guiGraphics,
                            int x1,
                            int y1,
                            int x2,
                            int y2,
                            int color) {
        line(guiGraphics, x1, y1, x2, y2, color, 1);
    }
    public static void line(GuiGraphics guiGraphics,
                            int x1,
                            int y1,
                            int x2,
                            int y2,
                            int color,
                            double process) {

        if (process <= 0) return;

        process = Mth.clamp(process, 0, 1);

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int err = dx - dy;

        int totalSteps = Math.max(dx, dy) + 1;

        int drawSteps = (int) Math.round(totalSteps * process);

        int x = x1;
        int y = y1;

        for (int i = 0; i < drawSteps; i++) {

            // 画当前像素（用1px hLine避免重复逻辑）
            guiGraphics.hLine(x, x, y, color);

            if (x == x2 && y == y2) break;

            int e2 = err << 1;

            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }

            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private static final int[] DIGIT_X = new int[]{0, 11, 22, 32, 42, 53, 63, 73, 84, 94};
    private static final int[] DIGIT_WIDTH = new int[]{11, 11, 10, 10, 11, 10, 10, 11, 10, 9};
    public static void drawHandwriteNumber(GuiGraphics guiGraphics,
                                           int x,
                                           int y,
                                           int number) {
        int digitOffset = 0, readLen = 0;
        String numberString = String.valueOf(number);
        while (readLen < numberString.length()) {
            int num = (numberString.charAt(readLen++) - '0') - 1;
            num = num<0? 9: num;
            blit(
                    guiGraphics,
                    Textures.NUMBER_SPRITE.getResource(),
                    x + digitOffset,
                    y,
                    DIGIT_X[num],
                    Textures.NUMBER_SPRITE.getStartY(),
                    DIGIT_WIDTH[num],
                    Textures.NUMBER_SPRITE.getHeight()
            );
            digitOffset += DIGIT_WIDTH[num];
        }
    }
}
