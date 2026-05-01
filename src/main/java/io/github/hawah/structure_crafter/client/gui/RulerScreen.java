package io.github.hawah.structure_crafter.client.gui;

import io.github.hawah.structure_crafter.client.gui.utils.ButtonGroup;
import io.github.hawah.structure_crafter.client.gui.utils.DraggableFloatWidget;
import io.github.hawah.structure_crafter.client.gui.utils.RulerMapWidget;
import io.github.hawah.structure_crafter.client.gui.utils.TextureButton;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.item.RulerItem;
import io.github.hawah.structure_crafter.networking.HandholdItemChangePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class RulerScreen extends BaseScreen{
    public RulerScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        assert Minecraft.getInstance().player != null;
        this.setTextureSize(254, 152);
        super.init();
        int x = guiLeft;
        int y = guiTop;
        DraggableFloatWidget widget = new DraggableFloatWidget(
                x + 7,
                y + 56,
                Textures.RULER_DECO,
                new Rect2i(x, y, textureWidth, textureHeight)
        );
        RulerMapWidget map = new RulerMapWidget(
                x + 28,
                y + 16,
                Textures.RULER_DECO_MAP,
                new Rect2i(x, y, textureWidth, textureHeight),
                new Rect2i(x, y, 32, 32)
        );
        Runnable EMPTY = () -> {
        };
        final int OFFSET_X = 19;
        final int OFFSET_Y = 0;
        TextureButton chain = TextureButton.builder(
                x + 36 + OFFSET_X,
                y + 102 + OFFSET_Y,
                Textures.RULER_DECO_CHAIN.getWidth(),
                Textures.RULER_DECO_CHAIN.getHeight(),
                Component.literal("Chain Like"),
                () -> {
                    int setting = RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem());
                    Minecraft.getInstance().player.getMainHandItem().set(DataComponentTypeRegistries.RULER_SETTINGS, setting | RulerItem.CHANGE_CENTER);
                    Networking.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                }
        ).normalUV(Textures.RULER_DECO_CHAIN.getStartX(), Textures.RULER_DECO_CHAIN.getStartY())
                .texture(Textures.RULER_DECO_CHAIN.getResource())
                .pressedUV(83, 152)
                .pressedOffset(-6, -5)
                .pressedSize(21, 17)
                .toggled(true)
                .enableToggleUp(false)
                .covered(true)
                .build();

        chain.setPressed((RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem()) & RulerItem.CHANGE_CENTER) != 0);

        TextureButton nonchain = TextureButton.builder(
                        x + 36+ OFFSET_X,
                        y + 120+ OFFSET_Y,
                        12,
                        11,
                        Component.literal("Web Like"),
                        () -> {
                            int setting = RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem());
                            Minecraft.getInstance().player.getMainHandItem().set(DataComponentTypeRegistries.RULER_SETTINGS, setting & ~RulerItem.CHANGE_CENTER);
                            Networking.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                        }
        ).normalUV(68, 179)
                .texture(Textures.RULER_DECO_CHAIN.getResource())
                .pressedUV(83, 169)
                .pressedOffset(-6, -3)
                .pressedSize(21, 18)
                .toggled(true)
                .enableToggleUp(false)
                .covered(true)
                .build();

        nonchain.setPressed((RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem()) & RulerItem.CHANGE_CENTER) == 0);

        TextureButton strait = TextureButton.builder(
                        x + 36 + 30+ OFFSET_X,
                        y + 100+ OFFSET_Y,
                        13,
                        12,
                        Component.literal("Strait"),
                        () -> {
                            int setting = RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem());
                            Minecraft.getInstance().player.getMainHandItem().set(DataComponentTypeRegistries.RULER_SETTINGS, setting & ~RulerItem.IS_CIRCLE);
                            Networking.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                        }
        ).normalUV(106, 157)
                .texture(Textures.RULER_DECO_CHAIN.getResource())
                .pressedUV(83, 169)
                .pressedOffset(-6, -3)
                .pressedSize(21, 18)
                .toggled(true)
                .enableToggleUp(false)
                .covered(true)
                .build();

        strait.setPressed((RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem()) & RulerItem.IS_CIRCLE) == 0);

        TextureButton nonstrait = TextureButton.builder(
                        x + 36 + 30+ OFFSET_X,
                        y + 120+ OFFSET_Y,
                        13,
                        12,
                        Component.literal("Any"),
                        () -> {
                            int setting = RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem());
                            Minecraft.getInstance().player.getMainHandItem().set(DataComponentTypeRegistries.RULER_SETTINGS, setting | RulerItem.IS_CIRCLE);
                            Networking.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                        }
        ).normalUV(106, 174)
                .texture(Textures.RULER_DECO_CHAIN.getResource())
                .pressedUV(83, 152)
                .pressedOffset(-6, -5)
                .pressedSize(21, 17)
                .toggled(true)
                .enableToggleUp(false)
                .covered(true)
                .build();

        nonstrait.setPressed((RulerItem.settingOf(Minecraft.getInstance().player.getMainHandItem()) & RulerItem.IS_CIRCLE) != 0);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.addButton(chain);
        buttonGroup.addButton(nonchain);

        buttonGroup = new ButtonGroup();
        buttonGroup.addButton(strait);
        buttonGroup.addButton(nonstrait);

        map.enableRotation(true);
        widget.enableRotation(true);
        this.addSortedRenderWidget(map);
        this.addSortedRenderWidget(widget);

        addRenderableWidget(chain);
        addRenderableWidget(nonchain);
        addRenderableWidget(strait);
        addRenderableWidget(nonstrait);
        this.finishRegister();
    }

    @Override
    protected void renderWindowPre(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        blit(
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
