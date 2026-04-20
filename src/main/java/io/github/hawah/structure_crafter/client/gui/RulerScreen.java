package io.github.hawah.structure_crafter.client.gui;

import io.github.hawah.structure_crafter.client.gui.utils.DraggableFloatWidget;
import io.github.hawah.structure_crafter.client.gui.utils.RulerMapWidget;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class RulerScreen extends BaseScreen{
    public RulerScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.setTextureSize(254, 152);
        super.init();
        int x = guiLeft;
        int y = guiTop;
        DraggableFloatWidget widget = new DraggableFloatWidget(
                x + 7,
                y + 56,
                Textures.RULER_DECO.getWidth(),
                Textures.RULER_DECO.getHeight(),
                Textures.RULER_DECO.getResource(),
                Textures.RULER_DECO.getStartX(),
                Textures.RULER_DECO.getStartY(),
                new Rect2i(x, y, textureWidth, textureHeight)
        );
        RulerMapWidget map = new RulerMapWidget(
                x + 28,
                y + 16,
                Textures.RULER_DECO_MAP.getWidth(),
                Textures.RULER_DECO_MAP.getHeight(),
                Textures.RULER_DECO_MAP.getResource(),
                Textures.RULER_DECO_MAP.getStartX(),
                Textures.RULER_DECO_MAP.getStartY(),
                new Rect2i(x, y, textureWidth, textureHeight),
                new Rect2i(x, y, 32, 32)
        );
        map.enableRotation(true);
        widget.enableRotation(true);
        this.addSortedRenderWidget(map);
        this.addSortedRenderWidget(widget);
        this.finishRegister();
    }

    @Override
    protected void renderWindowPre(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BaseScreen.blit(
                guiGraphics,
                Textures.RULER_BKG.getResource(),
                guiLeft,
                guiTop,
                Textures.RULER_BKG.getStartX(),
                Textures.RULER_BKG.getStartY(),
                textureWidth,
                textureHeight
        );
        super.renderWindowPre(guiGraphics, mouseX, mouseY, partialTick);
    }
}
