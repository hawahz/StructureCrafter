package io.github.hawah.structure_crafter.client.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.gui.BaseScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Random;

public class DraggableFloatWidget extends AbstractWidget {
    private final int startX;
    private final int startY;
    private final ResourceLocation texture;
    private boolean dragging = false;
    private int offsetX;
    private int offsetY;
    private double oDragV = 0;
    private double dragHeight = 0;
    private double dragV = 0;
    private final Rect2i parentRect;
    private final Rect2i limitArea;
    private boolean enableRotation = false;

    private double oRotation = 0;
    private double rotation = 0;

    public void setRotation(double rotation) {
        this.rotation = rotation % 360;
    }

    public DraggableFloatWidget(int x,
                                int y,
                                int width,
                                int height,
                                ResourceLocation texture,
                                int startX,
                                int startY,
                                Rect2i parentRect) {
        this(x, y, width, height, texture, startX, startY, parentRect, new Rect2i(-1, -1, 0, 0));
    }

    public DraggableFloatWidget(int x,
                                int y,
                                int width,
                                int height,
                                ResourceLocation texture,
                                int startX,
                                int startY,
                                Rect2i parentRect,
                                Rect2i limitArea) {
        super(x, y, width, height, Component.empty());
        this.startX = startX;
        this.startY = startY;
        this.texture = texture;
        this.parentRect = parentRect;
        this.limitArea = limitArea;
    }

    public void enableRotation(boolean enableRotation) {
        this.enableRotation = enableRotation;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) {
            return false;
        }
        this.setX((int) (mouseX + offsetX));
        this.setY((int) (mouseY + offsetY));
        dragV = dragX;
        dragging = true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void setX(int x) {
        if (limitArea.getX() > 0) {
            x = Mth.clamp(x, limitArea.getX(), limitArea.getX()+limitArea.getWidth());
        }
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        if (limitArea.getY() > 0) {
            y = Mth.clamp(y, limitArea.getY(), limitArea.getY()+limitArea.getHeight());
        }
        super.setY(y);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!dragging) {
            dragging = true;
            offsetX = (int) (this.getX() - mouseX);
            offsetY = (int) (this.getY() - mouseY);
            setRotation(rotation + new Random().nextDouble() * 10 - 5);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dragging && enableRotation) {
            this.setRotation(rotation - scrollY*10);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (dragging && (mouseX < getX() || getX()+getWidth() < mouseX || mouseY < getY() || getY()+getHeight() < mouseY)) {
            dragging = false;
        }
        if (dragging) {
            dragHeight = Mth.lerp(StructureCrafterClient.ANI_DELTAF * 0.5F, dragHeight, 6);
            oDragV = Mth.lerp(StructureCrafterClient.ANI_DELTAF, oDragV, dragV);
            oRotation = Mth.rotLerp(StructureCrafterClient.ANI_DELTAF * 0.5F, oRotation, rotation);
        } else {
            dragHeight = 0;
        }

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(this.getX(), this.getY(), 0);
        pose.translate(this.getWidth()/2F, this.getHeight()/2F, 0);

        // Shade
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees((float) (oDragV + oRotation)));
        pose.translate(-this.getWidth()/2F, -this.getHeight()/2F, 0);
        pose.translate(-this.getX(), -this.getY(), 0);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0,0, 0, 0.5f);
        guiGraphics.enableScissor(
                parentRect.getX(),
                parentRect.getY(),
                parentRect.getX()+parentRect.getWidth(),
                parentRect.getY()+parentRect.getHeight()
        );
        BaseScreen.blit(
                guiGraphics,
                texture,
                this.getX(),
                this.getY(),
                startX,
                startY,
                this.getWidth(),
                this.getHeight()
        );
        guiGraphics.disableScissor();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        pose.popPose();

        // Content
        pose.pushPose();
        pose.translate(dragHeight*0.3, -dragHeight-1, 0);
        pose.mulPose(Axis.ZP.rotationDegrees((float) (oDragV + oRotation) ));
        pose.translate(-this.getWidth()/2F, -this.getHeight()/2F, 0);
        pose.translate(-this.getX(), -this.getY(), 0);
        BaseScreen.blit(
                guiGraphics,
                texture,
                this.getX(),
                this.getY(),
                startX,
                startY,
                this.getWidth(),
                this.getHeight()
        );
        renderAbove(guiGraphics, mouseX, mouseY, partialTick);
        pose.popPose();


        pose.popPose();
//        guiGraphics.hLine(100, (int) (100 + dragV*10), 100, 0xFFFFFFFF);
    }

    protected void renderAbove(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }


}
