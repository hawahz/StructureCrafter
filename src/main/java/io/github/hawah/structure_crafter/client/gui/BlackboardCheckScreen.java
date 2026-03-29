package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.mixin.ScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class BlackboardCheckScreen extends Screen {

    private ColoredLabel alarmLabel;

    private final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "nametag" + ".png");

    public BlackboardCheckScreen() {
        super(Component.literal("blkb"));
    }

    protected int textureWidth, textureHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;
    private EditBox nameField;
    private TextureButton confirm;
    private TextureButton delete;
    private TextureButton discard;

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setWindowSize(int width, int height) {
        textureWidth = width;
        textureHeight = height;
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setWindowOffset(int xOffset, int yOffset) {
        windowXOffset = xOffset;
        windowYOffset = yOffset;
    }

    @Override
    protected void init() {
        setWindowSize(109, 32);

        guiLeft = (width - textureWidth) / 2 + 20;
        guiTop = (height - textureHeight) / 2;
        guiLeft += windowXOffset;
        guiTop += windowYOffset;

        int x = guiLeft;
        int y = guiTop + 2;

        nameField = new EditBox(font, x + 13, y + 2, 89, 10, CommonComponents.EMPTY);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setMaxLength(14);
        nameField.setFocused(true);
        setFocused(nameField);
        addRenderableWidget(nameField);

        confirm = new TextureButton(
                guiLeft + 113,
                guiTop - 2,
                16,
                13,
                Component.empty(),
                texture,
                80, 96,
                80, 112,
                80, 128,
                () -> {
                    if (nameField.getValue().isEmpty()) {
                        alarmNoName();
                        nameField.setFocused(true);
                        setFocused(nameField);
                        return;
                    }
                    StructureCrafterClient.BLACKBOARD_HANDLER.saveStructure(nameField.getValue(), true);
                    onClose();
                }
        );
        addRenderableWidget(confirm);

        delete = new TextureButton(
                guiLeft + 113,
                guiTop + 29,
                15,
                16,
                Component.empty(),
                texture,
                96, 96,
                96, 112,
                96, 128,
                () -> {
                    StructureCrafterClient.BLACKBOARD_HANDLER.delete();
                    onClose();
                }
        );
        addRenderableWidget(delete);

        discard = new TextureButton(
                guiLeft + 113,
                guiTop + 11,
                17,
                16,
                Component.empty(),
                texture,
                111, 96,
                111, 112,
                111, 128,
                this::onClose
        );
        addRenderableWidget(discard);

         alarmLabel = new ColoredLabel(
                Component.translatable("information.alarm_no_enter"),
                60,
                20,
                 new Color(255, 0, 0, 255),
                new Color(255, 0, 0, 0),
                 guiLeft + 113,
                 guiTop - 20
         );
    }

    public void alarmNoName() {
        alarmLabel.reset();
    }

    @Override
    public void tick() {
        super.tick();
        if (!nameField.isFocused()) {
            nameField.setFocused(true);
            setFocused(nameField);
        }
        if (alarmLabel.activate) {
            alarmLabel.tick();
        }
    }

    @Override
    public boolean keyPressed(int key, int p_96553_, int p_96554_) {
        if (key == GLFW.GLFW_KEY_ENTER) {
            confirm.onClick(0, 0, 0);
        }
        return super.keyPressed(key, p_96553_, p_96554_);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();

        renderMenuBackground(graphics);
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        renderWindow(graphics, mouseX, mouseY, partialTicks);

        for (Renderable renderable : getRenderables())
            renderable.render(graphics, mouseX, mouseY, partialTicks);

        alarmLabel.render(graphics, mouseX, mouseY, partialTicks);

        poseStack.popPose();
    }

    private Iterable<? extends Renderable> getRenderables() {
        return ((ScreenAccessor) this).getRenderables();
    }

    private void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(
                texture,
                guiLeft,
                guiTop,
                73,
                32,
                textureWidth,
                textureHeight
        );
        graphics.pose().pushPose();
        graphics.pose().scale(2, 2, 2);
        graphics.renderFakeItem(ItemRegistries.BLACKBOARD.toStack(), (guiLeft - 40)/2, guiTop/2 - 4);
        graphics.pose().popPose();

        graphics.drawCenteredString(
                font,
                Component.translatable("title.blackboard_name_tag"),
                guiLeft + textureWidth /2,
                guiTop - font.lineHeight * 5 - 2,
                0xFFFFFF
        );
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
