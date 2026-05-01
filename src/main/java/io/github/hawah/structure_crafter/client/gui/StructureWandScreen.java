package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.gui.utils.LabelButton;
import io.github.hawah.structure_crafter.client.gui.utils.ImplTextureButton;
import io.github.hawah.structure_crafter.client.gui.utils.TextureButton;
import io.github.hawah.structure_crafter.util.StructureHandler;
import io.github.hawah.structure_crafter.client.render.EaseHelper;
import io.github.hawah.structure_crafter.client.utils.SearchHelper;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.structure_wand.AbstractStructureWand;
import io.github.hawah.structure_crafter.networking.HandholdItemChangePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class StructureWandScreen extends BaseScreen {

    private final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "wand_settings" + ".png");
    private final ResourceLocation textureDecoration =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "wand_settings_decoration" + ".png");
    public StructureWandScreen() {
        super(Component.literal("structure_wand"));
    }
    private int handTick = 0;
    private final List<String> structuresInFolder = new ArrayList<>();
    private List<MutableComponent> filteredStructures = new ArrayList<>();
    private final List<Button> labelButtons = new ArrayList<>();
    private static final List<String> discardedStructures = new ArrayList<>();
    private EditBox nameField;
    private ImplTextureButton loadFile;
    private ImplTextureButton refresh;
    private ImplTextureButton discard;
    private ImplTextureButton forward;
    private ImplTextureButton backward;
    private TextureButton updateToggle;
    private TextureButton boundingBoxToggle;
    private TextureButton replaceAirToggle;
    private TextureButton lockToggle;
    private TextureButton rotateLockToggle;
    private String filteredName = "";
    private String selectedStructure = "";
    private int currentPage = 0, pages = 0;
    float prevPage = 0;
    private final int MAX_SLOTS = 9;

    @Override
    public void tick() {
        super.tick();
        handTick ++;
        handTick = Math.min(handTick, 100);
        for (int i = 0; i < this.labelButtons.size(); i++) {
            labelButtons.get(i)
                    .setMessage(i + currentPage*MAX_SLOTS >= filteredStructures.size()? Component.literal(""): filteredStructures.get(i + currentPage*MAX_SLOTS));
            labelButtons.get(i).active = !labelButtons.get(i).getMessage().getString().equals(selectedStructure);
        }
//        prevPage = currentPage;
    }

    @Override
    protected void init() {

        setTextureSize(230, 140);

        super.init();

        int x = guiLeft;
        int y = guiTop;

        placeComponents(x, y);

    }

    private void placeComponents(int x, int y) {
        nameField = new EditBox(font, x + 30, y + 3, 54, 10, CommonComponents.EMPTY);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setFocused(true);
        nameField.setResponder(text -> {
            filteredName = text;
            List<String> searchResult = SearchHelper.search(filteredName, structuresInFolder);
            filteredStructures = searchResult.stream().map(Component::literal).toList();
            updatePage();
        });
        setFocused(nameField);
        nameField.setFocused(false);
        addRenderableWidget(nameField);

        refresh();
        selectedStructure = Minecraft.getInstance().player.getMainHandItem().get(DataComponentTypeRegistries.STRUCTURE_FILE);

        labelButtons.clear();
        for (int i = 0; i < Math.min(MAX_SLOTS, structuresInFolder.size()); i++) {
            LabelButton button = new LabelButton(Button.builder(filteredStructures.get(i), b -> {
                ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
                AbstractStructureWand.selectStructure(mainHandItem, b.getMessage().getString(), Minecraft.getInstance().player);
                AbstractStructureWand.setOwnerName(mainHandItem, Minecraft.getInstance().player);
                selectedStructure = b.getMessage().getString();
                labelButtons.forEach(other -> other.active = true);
                b.active = false;
                StructureCrafterClient.STRUCTURE_WAND_HANDLER.setCurrentStructure(selectedStructure);
                Networking.sendToServer(new HandholdItemChangePacket(mainHandItem));
            }).bounds(x + 21, y + 21 + i * 11, 67, 10));
            labelButtons.add(button);
            addRenderableWidget(button);

            String stringMessage = button.getMessage().getString();
            if (stringMessage.equals(selectedStructure)) {
                button.active = false;
            }

        }

        this.loadFile = new ImplTextureButton(
                x + 19,
                y + 142,
                16,
                16,
                LangData.TOOLTIP_BUTTON_OPEN_FOLDER.get(),
                texture,
                0,
                0,
                0,
                16,
                0,
                32,
                () -> Util.getPlatform().openPath(Paths.STRUCTURE_DIR)
        );
        addRenderableWidget(loadFile);

        refresh = new ImplTextureButton(
                x + 46,
                y + 142,
                16,
                16,
                LangData.TOOLTIP_BUTTON_REFRESH.get(),
                texture,
                32,
                0,
                32,
                16,
                32,
                32,
                this::refresh
        );

        addRenderableWidget(refresh);

        discard = new ImplTextureButton(
                x + 73,
                y + 142,
                16,
                16,
                LangData.TOOLTIP_BUTTON_DELETE.get(),
                texture,
                16,
                0,
                16,
                16,
                16,
                32,
                () -> {
                    if (selectedStructure.isEmpty()) {
                        return;
                    }
                    if (discardedStructures.contains(selectedStructure)) {
                        return;
                    }
                    discardedStructures.add(selectedStructure);
                    refresh();
                }
        );

        addRenderableWidget(discard);

        int toggleX = x + 205;
        int toggleStartY = y + 22;

        updateToggle = TextureButton.builder(
                toggleX + 1,
                toggleStartY,
                16,
                16,
                LangData.TOOLTIP_BUTTON_UPDATE.get(),
                () -> {
                    AbstractStructureWand.setUpdateFlags(Minecraft.getInstance().player.getMainHandItem(), updateToggle.isPressed()? 0: Block.UPDATE_ALL);
                    StructureCrafterClient.STRUCTURE_WAND_HANDLER.setDirty();
                    Networking.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                }
        ).messageToggled(LangData.TOOLTIP_BUTTON_NO_UPDATE.get())
                .texture(texture)
                .normalUV(160, 0)
                .hoverUV(160, 32)
                .hoverSize(16, 16)
                .pressedHoverUV(208, 0)
                .pressedHoverSize(16, 16)
                .pressedUV(160, 16)
                .pressedSize(16, 16)
                .toggled(true)
                .enableToggleUp(true)
                .build();

        addRenderableWidget(updateToggle);
        updateToggle.setPressed(AbstractStructureWand.getUpdateFlags(Minecraft.getInstance().player.getMainHandItem()) != Block.UPDATE_ALL);

        replaceAirToggle = new TextureButton(
                toggleX,
                toggleStartY + 21,
                16,
                16,
                LangData.TOOLTIP_BUTTON_REPLACE.get(),
                LangData.TOOLTIP_BUTTON_PADDING.get(),
                texture,
                128,
                0,
                128,
                32,
                176,
                0,
                128,
                16,
                () -> {
                    AbstractStructureWand.setReplaceAir(Minecraft.getInstance().player.getMainHandItem(), replaceAirToggle.isPressed());
                    StructureCrafterClient.STRUCTURE_WAND_HANDLER.setDirty();
                    Networking.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                }
        );

        addRenderableWidget(replaceAirToggle);
        replaceAirToggle.setPressed(AbstractStructureWand.isReplaceAir(Minecraft.getInstance().player.getMainHandItem()));

        boundingBoxToggle = new TextureButton(
                toggleX,
                toggleStartY + 44,
                16,
                16,
                LangData.TOOLTIP_BUTTON_BOUNDS_VISIBLE.get(),
                LangData.TOOLTIP_BUTTON_BOUNDS_HIDDEN.get(),
                texture,
                144,
                0,
                144,
                32,
                192,
                0,
                144,
                16,
                () -> {
                    AbstractStructureWand.setBoundsVisible(Minecraft.getInstance().player.getMainHandItem(), !boundingBoxToggle.isPressed());
                    StructureCrafterClient.STRUCTURE_WAND_HANDLER.setDirty();
                    Networking.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                }
        );

        addRenderableWidget(boundingBoxToggle);
        boundingBoxToggle.setPressed(!AbstractStructureWand.isBoundsVisible(Minecraft.getInstance().player.getMainHandItem()));

        lockToggle = new TextureButton(
                toggleX,
                toggleStartY + 65,
                16,
                16,
                LangData.TOOLTIP_BUTTON_LOCK.get(),
                LangData.TOOLTIP_BUTTON_UNLOCK.get(),
                texture,
                240,
                0,
                240,
                16,
                240,
                48,
                240,
                32,

                () -> StructureCrafterClient.STRUCTURE_WAND_HANDLER.setLock(!lockToggle.isPressed())
         );
        addRenderableWidget(lockToggle);
        lockToggle.setPressed(!StructureCrafterClient.STRUCTURE_WAND_HANDLER.isLock());

        rotateLockToggle = new TextureButton(
                toggleX,
                toggleStartY + 90,
                16,
                16,
                LangData.TOOLTIP_BUTTON_ROTATE_UNLOCK.get(),
                LangData.TOOLTIP_BUTTON_ROTATE_LOCK.get(),
                texture,
                224,
                0,
                224,
                16,
                208,
                16,
                224,
                32,
                () -> StructureCrafterClient.STRUCTURE_WAND_HANDLER.setRotateLock(rotateLockToggle.isPressed())
        );
        addRenderableWidget(rotateLockToggle);
        rotateLockToggle.setPressed(StructureCrafterClient.STRUCTURE_WAND_HANDLER.isRotateLock());

        forward = new ImplTextureButton(
                x + 73,
                y + 117,
                16,
                16,
                Component.empty(),
                texture,
                48,
                0,
                48,
                16,
                48,
                32,
                () -> turnPage(1)
        );
        addRenderableWidget(forward);
        forward.active = pages > 1;

        backward = new ImplTextureButton(
                x + 18,
                y + 119,
                16,
                14,
                Component.empty(),
                texture,
                64,
                2,
                64,
                18,
                64,
                34,
                () -> turnPage(-1)
        );
        addRenderableWidget(backward);
        backward.active = false;
    }

    private void turnPage(int direction) {
//        prevPage = currentPage;
        currentPage = Mth.clamp(currentPage + direction, 0, pages - 1);
        forward.active = currentPage < pages - 1;
        backward.active = currentPage > 0;
    }

    private void refresh() {
        StructureHandler.loadStructuresString(structuresInFolder);
        structuresInFolder.removeIf(discardedStructures::contains);
        List<String> result = SearchHelper.search(filteredName, structuresInFolder);
        filteredStructures = result.stream().map(Component::literal).toList();
        updatePage();
    }

    private void updatePage() {
        pages = (int) Math.ceil((double) filteredStructures.size() / MAX_SLOTS);
        currentPage = 0;
        prevPage = 0;
        if (forward != null && backward != null) {
            forward.active = currentPage < pages - 1;
            backward.active = false;
        }
    }

    @Override
    protected void renderWindowPre(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Background
        graphics.blit(
                texture,
                guiLeft,
                guiTop,
                10,
                52,
                textureWidth,
                textureHeight
        );
        PoseStack pose = graphics.pose();
        float delta = handTick >= 10f? 1 : (handTick + partialTicks) / 10f;
        float ease = 1-EaseHelper.easeInPow(Mth.clamp(1-delta, 0, 1), 3);

        // Wand Shade
        pose.pushPose();
        // 122, 33
        float y = (ease * 200) - 200;
        float shadeOffsetY = - 33 - y * 1.25F;
        float clip = Mth.clamp(138 - shadeOffsetY, 0, 100000);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.455f, 0.267f, 0.267f, 0.5f);
        int handX = guiLeft + 121;
        if (shadeOffsetY < 138) {
            graphics.blit(
                    textureDecoration,
                    handX - 4,
                    (int)(guiTop + Mth.clamp(shadeOffsetY, 15, 138)),
                    80,
                    shadeOffsetY > 15? 0 : (int) (15 - shadeOffsetY),
                    80,
                    (int) Mth.lerp(Mth.clamp(clip/124, 0, 1), 0, 120)
            );

        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        pose.popPose();

        // Wand
        pose.pushPose();
        graphics.blit(
                textureDecoration,
                handX,
                (int) (guiTop - 33 - y),
                0,
                0,
                80,
                195
        );
        pose.popPose();

        // Page Indicator
        graphics.drawCenteredString(
                font,
                Component.literal("- " + (currentPage + 1) + "/" + pages + " -"),
                guiLeft+54,
                guiTop + 121,
                0xFFFFFF
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        int intDelta = (int) (scrollY > 0 ? Math.ceil(scrollY) : Math.floor(scrollY));
        turnPage(-intDelta);

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWindowPost(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        prevPage = Mth.lerp(StructureCrafterClient.ANI_DELTAF/3, prevPage, currentPage);
        float tagY = Mth.lerp(Mth.clamp(prevPage / Math.max(1, pages - 1f), 0, 1), 15, 120);
        guiGraphics.blit(
                texture,
                guiLeft + 102,
                (int) (guiTop + tagY),
                112,
                0,
                16,
                16
        );
    }
}
