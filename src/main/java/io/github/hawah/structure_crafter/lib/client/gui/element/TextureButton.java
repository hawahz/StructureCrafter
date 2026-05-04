package io.github.hawah.structure_crafter.lib.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.lib.client.gui.BaseScreen;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

import java.util.List;

public class TextureButton extends AbstractWidget {

    protected final Component               messageToggled                      ;
    protected final Component               messageDisabled                     ;
    protected final ResourceLocation        disabledTexture                     ;
    protected final ResourceLocation        texture                             ;
    protected final ResourceLocation        pressedTexture                      ;
    protected final Vector2i                normalUV            = new Vector2i();
    protected final Vector2i                hoverUV             = new Vector2i();
    protected final Vector2i                hoverSize           = new Vector2i();
    protected final Vector2i                hoverOffset         = new Vector2i();
    protected final Vector2i                pressedUV           = new Vector2i();
    protected final Vector2i                pressedSize         = new Vector2i();
    protected final Vector2i                pressedOffset       = new Vector2i();
    protected final Vector2i                pressedHoverUV      = new Vector2i();
    protected final Vector2i                pressedHoverSize    = new Vector2i();
    protected final Vector2i                pressedHoverOffset  = new Vector2i();
    protected final Vector2i                inactiveUV          = new Vector2i();
    protected final Vector2i                inactiveSize        = new Vector2i();
    protected final Vector2i                inactiveOffset      = new Vector2i();
    protected final Vector2i                inactiveHoverUV     = new Vector2i();
    protected final Vector2i                inactiveHoverSize   = new Vector2i();
    protected final Vector2i                inactiveHoverOffset = new Vector2i();
    protected final boolean                 enableToggleUp                      ;
    protected final boolean                 covered                             ;
    protected final boolean                 toggled                             ;
    protected Runnable                      onPress                             ;
    private boolean pressed = false;

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    private TextureButton(int               x,
                          int               y,
                          int               width,
                          int               height,
                          Component         message,
                          Component         messageToggled,
                          Component         messageDisabled,
                          ResourceLocation  texture,
                          ResourceLocation  disabledTexture,
                          ResourceLocation  toggledTexture,
                          int               originStartX,
                          int               originStartY,
                          int               hoverStartX,
                          int               hoverStartY,
                          int               hoverSizeX,
                          int               hoverSizeY,
                          int               hoverOffsetX,
                          int               hoverOffsetY,
                          int               pressedStartX,
                          int               pressedStartY,
                          int               pressedSizeX,
                          int               pressedSizeY,
                          int               pressedOffsetX,
                          int               pressedOffsetY,
                          int               pressedHoverStartX,
                          int               pressedHoverStartY,
                          int               pressedHoverSizeX,
                          int               pressedHoverSizeY,
                          int               pressedHoverOffsetX,
                          int               pressedHoverOffsetY,
                          int               disabledStartX,
                          int               disabledStartY,
                          int               disabledSizeX,
                          int               disabledSizeY,
                          int               disabledOffsetX,
                          int               disabledOffsetY,
                          int               disabledHoverStartX,
                          int               disabledHoverStartY,
                          int               disabledHoverSizeX,
                          int               disabledHoverSizeY,
                          int               disabledHoverOffsetX,
                          int               disabledHoverOffsetY,
                          boolean           enableToggleUp,
                          boolean           toggled,
                          boolean           covered,
                          Runnable          onPress
    ) {
        super(x, y, width, height, message);
        this.messageToggled = messageToggled;
        this.messageDisabled = messageDisabled;
        this.disabledTexture = disabledTexture;
        this.texture = texture;
        this.pressedTexture = toggledTexture;
        this.normalUV.set(originStartX, originStartY);
        this.hoverUV.set(hoverStartX, hoverStartY);
        this.hoverSize.set(hoverSizeX, hoverSizeY);
        this.hoverOffset.set(hoverOffsetX, hoverOffsetY);
        this.pressedUV.set(pressedStartX, pressedStartY);
        this.pressedSize.set(pressedSizeX, pressedSizeY);
        this.pressedOffset.set(pressedOffsetX, pressedOffsetY);
        this.pressedHoverUV.set(pressedHoverStartX, pressedHoverStartY);
        this.pressedHoverSize.set(pressedHoverSizeX, pressedHoverSizeY);
        this.pressedHoverOffset.set(pressedHoverOffsetX, pressedHoverOffsetY);
        this.inactiveUV.set(disabledStartX, disabledStartY);
        this.inactiveSize.set(disabledSizeX, disabledSizeY);
        this.inactiveHoverUV.set(disabledHoverStartX, disabledHoverStartY);
        this.inactiveHoverSize.set(disabledHoverSizeX, disabledHoverSizeY);
        this.inactiveOffset.set(disabledOffsetX, disabledOffsetY);
        this.inactiveHoverOffset.set(disabledHoverOffsetX, disabledHoverOffsetY);
        this.enableToggleUp = enableToggleUp;
        this.toggled = toggled;
        this.covered = covered;
        this.onPress = onPress;
    }

    public TextureButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Component messageToggled,
            ResourceLocation texture,
            int originStartX,
            int originStartY,
            int hoverStartX,
            int hoverStartY,
            int pressedHoverStartX,
            int pressedHoverStartY,
            int pressedStartX,
            int pressedStartY,
            Runnable onPress
    ) {
        this(
                x,
                y,
                width,
                height,
                message,
                messageToggled,
                texture,
                originStartX,
                originStartY,
                hoverStartX,
                hoverStartY,
                pressedHoverStartX,
                pressedHoverStartY,
                pressedStartX,
                pressedStartY,
                true,
                onPress
        );
    }

    public TextureButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Component messageToggled,
            ResourceLocation texture,
            int originStartX,
            int originStartY,
            int hoverStartX,
            int hoverStartY,
            int pressedHoverStartX,
            int pressedHoverStartY,
            int pressedStartX,
            int pressedStartY,
            boolean enableToggleUp,
            Runnable onPress
    ) {
        this(x,
                y,
                width,
                height,
                message,
                messageToggled,
                Component.empty(),
                texture,
                null,
                null,
                originStartX,
                originStartY,
                hoverStartX,
                hoverStartY,
                width,
                height,
                0,
                0,
                pressedStartX,
                pressedStartY,
                width,
                height,
                0,
                0,
                pressedHoverStartX,
                pressedHoverStartY,
                width,
                height,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                enableToggleUp,
                true,
                false,
                onPress);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if ((pressed && !enableToggleUp) || !isActive()) {
            setFocused(false);
            return;
        }
        if (toggled) {
            pressed = !pressed;
        }
        this.onPress.run();
    }

    protected ResourceLocation disabledTexture() {
        return disabledTexture == null? texture: disabledTexture;
    }
    protected ResourceLocation texture() {
        return texture;
    }
    protected ResourceLocation pressedTexture() {
        return pressedTexture == null? texture: pressedTexture;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        if ((!isPressed() || covered) && isActive()) {
            drawOrigin(guiGraphics);
        }
        if (isPressed() && isActive()) {
            drawPressed(guiGraphics);
        }
        if (!isActive()) {
            drawDisabled(guiGraphics);
        }
        Component component = isPressed() && !this.messageToggled.getString().isEmpty() ? this.messageToggled : this.getMessage();
        if (component.getString().isEmpty() || !this.isHovered())
            return;
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(-mouseX, -mouseY, 0);
        pose.scale(1, 1, 1);
        pose.translate(mouseX, mouseY, 0);
        guiGraphics.renderComponentTooltip(
                Minecraft.getInstance().font,
                List.of(component),
                mouseX,
                mouseY
        );
        pose.popPose();

    }

    private void drawOrigin(GuiGraphics guiGraphics){
        BaseScreen.blit(
                guiGraphics,
                texture(),
                this.getX(),
                this.getY(),
                this.normalUV.x(),
                this.normalUV.y(),
                this.getWidth(),
                this.getHeight()
        );
        if (isHovered()) {
            BaseScreen.blit(
                    guiGraphics,
                    texture(),
                    this.getX() + hoverOffset.x(),
                    this.getY() + hoverOffset.y(),
                    this.hoverUV.x(),
                    this.hoverUV.y(),
                    this.hoverSize.x(),
                    this.hoverSize.y()
            );
        }
    }

    private void drawPressed(GuiGraphics guiGraphics){
        BaseScreen.blit(
                guiGraphics,
                pressedTexture(),
                this.getX() + pressedOffset.x(),
                this.getY() + pressedOffset.y(),
                this.pressedUV.x(),
                this.pressedUV.y(),
                this.pressedSize.x(),
                this.pressedSize.y()
        );
        if (isHovered()) {
            BaseScreen.blit(
                    guiGraphics,
                    pressedTexture(),
                    this.getX() + pressedHoverOffset.x(),
                    this.getY() + pressedHoverOffset.y(),
                    this.pressedHoverUV.x(),
                    this.pressedHoverUV.y(),
                    this.pressedHoverSize.x(),
                    this.pressedHoverSize.y()
            );
        }
    }

    private void drawDisabled(GuiGraphics guiGraphics){
        BaseScreen.blit(
                guiGraphics,
                disabledTexture(),
                this.getX() + this.inactiveOffset.x(),
                this.getY() + this.inactiveOffset.y(),
                this.inactiveUV.x(),
                this.inactiveUV.y(),
                this.inactiveSize.x(),
                this.inactiveSize.y()
        );
        if (isHovered()) {
            BaseScreen.blit(
                    guiGraphics,
                    disabledTexture(),
                    this.getX() + this.inactiveHoverOffset.x(),
                    this.getY() + this.inactiveHoverOffset.y(),
                    this.inactiveHoverUV.x(),
                    this.inactiveHoverUV.y(),
                    this.inactiveHoverSize.x(),
                    this.inactiveHoverSize.y()
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }

    public static Builder builder(int x,
                                  int y,
                                  int width,
                                  int height,
                                  Component message,
                                  Runnable onPress) {
        return new Builder(x, y, width, height, message, onPress);
    }

    public static class Builder {
        protected final int             x                                       ;
        protected final int             y                                       ;
        protected final int             width                                   ;
        protected final int             height                                  ;
        protected final Component       message                                 ;
        protected Component             messageToggled      = Component.empty() ;
        protected Component             messageDisabled     = Component.empty() ;
        protected ResourceLocation      disabledTexture     = null              ;
        protected ResourceLocation      texture             = null              ;
        protected ResourceLocation      pressedTexture      = null              ;
        protected Vector2i              normalUV            = new Vector2i()    ;
        protected Vector2i              hoverUV             = new Vector2i()    ;
        protected Vector2i              hoverSize           = new Vector2i()    ;
        protected Vector2i              hoverOffset         = new Vector2i()    ;
        protected Vector2i              pressedUV           = new Vector2i()    ;
        protected Vector2i              pressedSize         = new Vector2i()    ;
        protected Vector2i              pressedOffset       = new Vector2i()    ;
        protected Vector2i              pressedHoverUV      = new Vector2i()    ;
        protected Vector2i              pressedHoverSize    = new Vector2i()    ;
        protected Vector2i              pressedHoverOffset  = new Vector2i()    ;
        protected Vector2i              inactiveUV          = new Vector2i()    ;
        protected Vector2i              inactiveSize        = new Vector2i()    ;
        protected Vector2i              inactiveOffset      = new Vector2i()    ;
        protected Vector2i              inactiveHoverUV     = new Vector2i()    ;
        protected Vector2i              inactiveHoverSize   = new Vector2i()    ;
        protected Vector2i              inactiveHoverOffset = new Vector2i()    ;

        protected boolean               enableToggleUp      = true              ;
        protected boolean               covered             = false             ;
        protected boolean               toggled             = false             ;
        protected boolean               disabled            = false             ;
        protected Runnable              onPress                                 ;

        public Builder(int x,
                       int y,
                       int width,
                       int height,
                       Component message,
                       Runnable onPress) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.message = message;
            this.onPress = onPress;
        }

        public Builder normalUV(int normalStartX, int normalStartY) {
            this.normalUV.set(normalStartX, normalStartY);
            return this;
        }

        public Builder hoverUV(int hoverStartX, int hoverStartY) {
            this.hoverUV.set(hoverStartX, hoverStartY);
            return this;
        }

        public Builder hoverSize(int hoverStartWidth, int hoverStartHeight) {
            this.hoverSize.set(hoverStartWidth, hoverStartHeight);
            return this;
        }

        public Builder hoverOffset(int hoverStartOffsetX, int hoverStartOffsetY) {
            this.hoverOffset.set(hoverStartOffsetX, hoverStartOffsetY);
            return this;
        }

        public Builder pressedUV(int pressedStartX, int pressedStartY) {
            this.pressedUV.set(pressedStartX, pressedStartY);
            return this;
        }

        public Builder pressedSize(int pressedStartWidth, int pressedStartHeight) {
            this.pressedSize.set(pressedStartWidth, pressedStartHeight);
            return this;
        }

        public Builder pressedOffset(int pressedStartOffsetX, int pressedStartOffsetY) {
            this.pressedOffset.set(pressedStartOffsetX, pressedStartOffsetY);
            return this;
        }

        public Builder pressedHoverUV(int pressedHoverStartX, int pressedHoverStartY) {
            this.pressedHoverUV.set(pressedHoverStartX, pressedHoverStartY);
            return this;
        }

        public Builder pressedHoverSize(int pressedHoverStartWidth, int pressedHoverStartHeight) {
            this.pressedHoverSize.set(pressedHoverStartWidth, pressedHoverStartHeight);
            return this;
        }

        public Builder pressedHoverOffset(int pressedHoverStartOffsetX, int pressedHoverStartOffsetY) {
            this.pressedHoverOffset.set(pressedHoverStartOffsetX, pressedHoverStartOffsetY);
            return this;
        }

        public Builder inactiveUV(int inactiveStartX, int inactiveStartY) {
            this.inactiveUV.set(inactiveStartX, inactiveStartY);
            return this;
        }

        public Builder inactiveSize(int inactiveStartWidth, int inactiveStartHeight) {
            this.inactiveSize.set(inactiveStartWidth, inactiveStartHeight);
            return this;
        }

        public Builder inactiveOffset(int inactiveStartOffsetX, int inactiveStartOffsetY) {
            this.inactiveOffset.set(inactiveStartOffsetX, inactiveStartOffsetY);
            return this;
        }

        public Builder inactiveHoverUV(int inactiveHoverStartX, int inactiveHoverStartY) {
            this.inactiveUV.set(inactiveHoverStartX, inactiveHoverStartY);
            return this;
        }

        public Builder inactiveHoverSize(int inactiveHoverStartWidth, int inactiveHoverStartHeight) {
            this.inactiveHoverSize.set(inactiveHoverStartWidth, inactiveHoverStartHeight);
            return this;
        }

        public Builder inactiveHoverOffset(int inactiveHoverStartOffsetX, int inactiveHoverStartOffsetY) {
            this.inactiveHoverOffset.set(inactiveHoverStartOffsetX, inactiveHoverStartOffsetY);
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder toggledTexture(ResourceLocation resourceLocation) {
            this.pressedTexture = resourceLocation;
            return this;
        }

        public Builder disabledTexture(ResourceLocation resourceLocation) {
            this.disabledTexture = resourceLocation;
            return this;
        }

        public Builder textureWithUV(Textures textureData) {
            return texture(textureData.getResource())
                    .normalUV(textureData.getStartX(), textureData.getStartY())
                    .hoverUV(textureData.getStartX(), textureData.getStartY());
        }

        public Builder toggledTextureWithUV(Textures textureData) {
            return toggledTexture(textureData.getResource())
                    .pressedUV(textureData.getStartX(), textureData.getStartY())
                    .pressedHoverUV(textureData.getStartX(), textureData.getStartY());
        }

        public Builder disabledTextureWithUV(Textures textureData) {
            return disabledTexture(textureData.getResource())
                    .inactiveUV(textureData.getStartX(), textureData.getStartY())
                    .inactiveHoverUV(textureData.getStartX(), textureData.getStartY());
        }

        public Builder covered(boolean flag) {
            covered = flag;
            return this;
        }

        public Builder toggled(boolean flag) {
            this.toggled = flag;
            return this;
        }

        public Builder enableToggleUp(boolean flag) {
            enableToggleUp = flag;
            return this;
        }

        public Builder onPress(Runnable onPress) {
            this.onPress = onPress;
            return this;
        }

        public Builder messageToggled(Component messageToggled) {
            this.messageToggled = messageToggled;
            return this;
        }

        public Builder messageDisabled(Component messageDisabled) {
            this.messageDisabled = messageDisabled;
            return this;
        }

        public TextureButton build() {
            return new TextureButton(
                    x,
                    y,
                    width,
                    height,
                    message,
                    messageToggled,
                    messageDisabled,
                    texture,
                    disabledTexture,
                    pressedTexture,
                    normalUV            .x(), normalUV            .y(),
                    hoverUV             .x(), hoverUV             .y(),
                    hoverSize           .x(), hoverSize           .y(),
                    hoverOffset         .x(), hoverOffset         .y(),
                    pressedUV           .x(), pressedUV           .y(),
                    pressedSize         .x(), pressedSize         .y(),
                    pressedOffset       .x(), pressedOffset       .y(),
                    pressedHoverUV      .x(), pressedHoverUV      .y(),
                    pressedHoverSize    .x(), pressedHoverSize    .y(),
                    pressedHoverOffset  .x(), pressedHoverOffset  .y(),
                    inactiveUV          .x(), inactiveUV          .y(),
                    inactiveSize        .x(), inactiveSize        .y(),
                    inactiveOffset      .x(), inactiveOffset      .y(),
                    inactiveHoverUV     .x(), inactiveHoverUV     .y(),
                    inactiveHoverSize   .x(), inactiveHoverSize   .y(),
                    inactiveHoverOffset .x(), inactiveHoverOffset .y(),
                    enableToggleUp,
                    toggled,
                    covered,
                    onPress
            );
        }
    }
}
