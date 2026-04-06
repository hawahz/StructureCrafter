package io.github.hawah.structure_crafter.client.gui;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.gui.utils.ColoredLabel;
import io.github.hawah.structure_crafter.client.gui.utils.TextureButton;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.mixin.ScreenAccessor;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.util.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class BlackboardCheckScreen extends BaseScreen {

    public static final Textures BACKGROUND = Textures.NAMETAG_BACKGROUND;
    private ColoredLabel alarmLabel;

    public BlackboardCheckScreen() {
        super(Component.literal("blackboard"));
    }


    private EditBox nameField;
    private TextureButton confirm;
    private TextureButton delete;
    private TextureButton discard;

    @Override
    protected void init() {
        setTextureSize(BACKGROUND.getWidth(), BACKGROUND.getHeight());
        guiLeft += 20;

        super.init();

        int x = guiLeft;
        int y = guiTop + 2;

        nameField = new EditBox(font, x + 13, y + 2, 89, 10, CommonComponents.EMPTY);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
//        nameField.setMaxLength(14);
        nameField.setFocused(true);
        setFocused(nameField);
        addRenderableWidget(nameField);

        Textures.Builder confirmBuilder = Textures.CONFIRM_NAMETAG.builder();

        confirm = new TextureButton(
                guiLeft + 113,
                guiTop - 2,
                confirmBuilder.getWidth(),
                confirmBuilder.getHeight(),
                Component.empty(),
                confirmBuilder.getResource(),
                confirmBuilder.getStartX(), confirmBuilder.getStartY(),
                confirmBuilder.variant(Textures.Variants.HOVER).getStartX(), confirmBuilder.getStartY(),
                confirmBuilder.variant(Textures.Variants.DISABLED).getStartX(), confirmBuilder.getStartY(),
                () -> {
                    if (nameField.getValue().isEmpty()) {
                        alarmNoName();
                        nameField.setFocused(true);
                        setFocused(nameField);
                        return;
                    }
                    StructureCrafterClient.BLACKBOARD_HANDLER.saveStructure(nameField.getValue(), false);
                    onClose();
                }
        );
        addRenderableWidget(confirm);

        Textures.Builder deleteBuilder = Textures.DELETE_NAMETAG.builder();

        delete = new TextureButton(
                guiLeft + 113,
                guiTop + 29,
                deleteBuilder.getWidth(),
                deleteBuilder.getHeight(),
                Component.empty(),
                deleteBuilder.getResource(),
                deleteBuilder.getStartX(), deleteBuilder.getStartY(),
                deleteBuilder.variant(Textures.Variants.HOVER).getStartX(), deleteBuilder.getStartY(),
                deleteBuilder.variant(Textures.Variants.DISABLED).getStartX(), deleteBuilder.getStartY(),
                () -> {
                    StructureCrafterClient.BLACKBOARD_HANDLER.delete();
                    onClose();
                }
        );
        addRenderableWidget(delete);

        Textures.Builder discardBuilder = Textures.DISCARD_NAMETAG.builder();

        discard = new TextureButton(
                guiLeft + 113,
                guiTop + 11,
                discardBuilder.getWidth(),
                discardBuilder.getHeight(),
                Component.empty(),
                discardBuilder.getResource(),
                discardBuilder.getStartX(), discardBuilder.getStartY(),
                discardBuilder.variant(Textures.Variants.HOVER).getStartX(), discardBuilder.getStartY(),
                discardBuilder.variant(Textures.Variants.DISABLED).getStartX(), discardBuilder.getStartY(),
                this::onClose
        );
        addRenderableWidget(discard);

         alarmLabel = new ColoredLabel(
                 LangData.INFO_ALARM_NO_NAME.get(),
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

    private Iterable<? extends Renderable> getRenderables() {
        return ((ScreenAccessor) this).getRenderables();
    }

    @Override
    protected void renderWindowPre(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(
                BACKGROUND.getResource(),
                guiLeft,
                guiTop,
                BACKGROUND.getStartX(),
                BACKGROUND.getStartY(),
                textureWidth,
                textureHeight
        );
        graphics.pose().pushPose();
        graphics.pose().scale(2, 2, 2);
        graphics.renderFakeItem(ItemRegistries.BLACKBOARD.toStack(), (guiLeft - 40)/2, guiTop/2 - 4);
        graphics.pose().popPose();

        graphics.drawCenteredString(
                font,
                LangData.TITLE_BLACKBOARD_NAMETAG.get(),
                guiLeft + textureWidth /2,
                guiTop - font.lineHeight * 5 - 2,
                0xFFFFFF
        );
    }

    @Override
    protected void renderWindowPost(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        alarmLabel.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
