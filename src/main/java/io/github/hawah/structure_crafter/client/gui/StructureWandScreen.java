package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.gui.utils.LabelButton;
import io.github.hawah.structure_crafter.client.gui.utils.TextureButton;
import io.github.hawah.structure_crafter.client.gui.utils.TextureToggleButton;
import io.github.hawah.structure_crafter.client.handler.StructureHandler;
import io.github.hawah.structure_crafter.client.render.EaseHelper;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.structure_wand.AbstractStructureWand;
import io.github.hawah.structure_crafter.mixin.ScreenAccessor;
import io.github.hawah.structure_crafter.networking.HandholdItemChangePacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("FieldCanBeLocal")
public class StructureWandScreen extends Screen {

    private final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "wand_settings" + ".png");
    private final ResourceLocation textureDecoration =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "wand_settings_decoration" + ".png");
    public StructureWandScreen() {
        super(Component.literal("structure_wand"));
    }
    protected int textureWidth, textureHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;
    private int handTick = 0;
    private final List<String> structuresInFolder = new ArrayList<>();
    private List<MutableComponent> filteredStructures = new ArrayList<>();
    private final List<Button> labelButtons = new ArrayList<>();
    private static final List<String> discardedStructures = new ArrayList<>();
    private EditBox nameField;
    private TextureButton loadFile;
    private TextureButton refresh;
    private TextureButton discard;
    private TextureButton forward;
    private TextureButton backward;
    private TextureToggleButton updateToggle;
    private TextureToggleButton boundingBoxToggle;
    private TextureToggleButton replaceAirToggle;
    private String filteredName = "";
    private String selectedStructure = "";
    private int currentPage = 0, pages = 0;
    private final int MAX_SLOTS = 9;

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
    protected void setWindowOffset(int xOffset, int yOffset) {
        windowXOffset = xOffset;
        windowYOffset = yOffset;
    }

    @Override
    public void tick() {
        super.tick();
        handTick ++;
        handTick = Math.min(handTick, 10);
        for (int i = 0; i < this.labelButtons.size(); i++) {
            labelButtons.get(i)
                    .setMessage(i + currentPage*MAX_SLOTS >= filteredStructures.size()? Component.literal(""): filteredStructures.get(i + currentPage*MAX_SLOTS));
            labelButtons.get(i).active = !labelButtons.get(i).getMessage().getString().equals(selectedStructure);
        }
    }

    @Override
    protected void init() {

        setTextureSize(230, 140);

        guiLeft = (width - textureWidth) / 2;
        guiTop = (height - textureHeight) / 2;
        guiLeft += windowXOffset;
        guiTop += windowYOffset;

        int x = guiLeft;
        int y = guiTop;

        nameField = new EditBox(font, x + 30, y + 3, 54, 10, CommonComponents.EMPTY);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setFocused(true);
        nameField.setResponder(text -> {
            filteredName = text;
            List<String> searchResult = search(filteredName, structuresInFolder);
            filteredStructures = searchResult.stream().map(Component::literal).toList();
            updatePage();
        });
//        setFocused(nameField);
        addRenderableWidget(nameField);

        refresh();

        labelButtons.clear();
        for (int i = 0; i < Math.min(MAX_SLOTS, structuresInFolder.size()); i++) {
            LabelButton button = new LabelButton(Button.builder(filteredStructures.get(i), b -> {
                ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
                mainHandItem.set(DataComponentTypeRegistries.STRUCTURE_FILE, b.getMessage().getString());
                selectedStructure = b.getMessage().getString();
                labelButtons.forEach(other -> other.active = true);
                b.active = false;
                StructureCrafterClient.STRUCTURE_WAND_HANDLER.setCurrentStructure(selectedStructure);
                CatnipServices.NETWORK.sendToServer(new HandholdItemChangePacket(mainHandItem));
            }).bounds(x + 21, y + 21 + i * 11, 67, 10));
            labelButtons.add(button);
            addRenderableWidget(button);
        }

        this.loadFile = new TextureButton(
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
                () -> {
                    Util.getPlatform().openPath(Paths.STRUCTURE_DIR);
                }
        );
        addRenderableWidget(loadFile);

        refresh = new TextureButton(
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

        discard = new TextureButton(
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
        int toggleStartY = y + 20;

        updateToggle = new TextureToggleButton(
                toggleX + 1,
                toggleStartY,
                16,
                16,
                LangData.TOOLTIP_BUTTON_UPDATE.get(),
                LangData.TOOLTIP_BUTTON_NO_UPDATE.get(),
                texture,
                160,
                0,
                160,
                32,
                208,
                0,
                160,
                16,
                () -> {
                    AbstractStructureWand.setUpdateFlags(Minecraft.getInstance().player.getMainHandItem(), updateToggle.isToggled()? Block.UPDATE_ALL: 0);
                    StructureCrafterClient.STRUCTURE_WAND_HANDLER.setDirty(true);
                    CatnipServices.NETWORK.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                }
        );

        addRenderableWidget(updateToggle);
        updateToggle.setToggled(AbstractStructureWand.getUpdateFlags(Minecraft.getInstance().player.getMainHandItem()) != Block.UPDATE_ALL);

        replaceAirToggle = new TextureToggleButton(
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
                    AbstractStructureWand.setReplaceAir(Minecraft.getInstance().player.getMainHandItem(), !replaceAirToggle.isToggled());
                    StructureCrafterClient.STRUCTURE_WAND_HANDLER.setDirty(true);
                    CatnipServices.NETWORK.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                }
        );

        addRenderableWidget(replaceAirToggle);
        replaceAirToggle.setToggled(AbstractStructureWand.isReplaceAir(Minecraft.getInstance().player.getMainHandItem()));

        boundingBoxToggle = new TextureToggleButton(
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
                    AbstractStructureWand.setBoundsVisible(Minecraft.getInstance().player.getMainHandItem(), boundingBoxToggle.isToggled());
                    StructureCrafterClient.STRUCTURE_WAND_HANDLER.setDirty(true);
                    CatnipServices.NETWORK.sendToServer(new HandholdItemChangePacket(Minecraft.getInstance().player.getMainHandItem()));
                }
        );

        addRenderableWidget(boundingBoxToggle);
        boundingBoxToggle.setToggled(!AbstractStructureWand.isBoundsVisible(Minecraft.getInstance().player.getMainHandItem()));

        forward = new TextureButton(
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

        backward = new TextureButton(
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
        currentPage = Mth.clamp(currentPage + direction, 0, pages - 1);
        forward.active = currentPage < pages - 1;
        backward.active = currentPage > 0;
    }

    private void refresh() {
        StructureHandler.loadStructuresString(structuresInFolder);
        structuresInFolder.removeIf(discardedStructures::contains);
        List<String> result = search(filteredName, structuresInFolder);
        filteredStructures = result.stream().map(Component::literal).toList();
        updatePage();
    }

    private void updatePage() {
        pages = (int) Math.ceil(filteredStructures.size() / 10f);
        currentPage = 0;
    }

    private void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
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
        float delta = handTick == 10f? 1 : (handTick + partialTicks) / 10f;
        float ease = 1-EaseHelper.easeInPow(Mth.clamp(1-delta, 0, 1), 10);

        pose.pushPose();
        // 122, 33
        float y = (ease * 200) - 200;
        float shadeOffsetY = - 33 - y * 2;
        float clip = Mth.clamp(138 - shadeOffsetY, 0, 100000);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.455f, 0.267f, 0.267f, 0.5f);
        int handX = guiLeft + 121;
        if (shadeOffsetY < 138) {
            graphics.blit(
                    textureDecoration,
                    handX - 4,
                    (int)(guiTop + shadeOffsetY) + (int) (15 - shadeOffsetY),
                    80,
                    shadeOffsetY > 15? 0 : (int) (15 - shadeOffsetY),
                    80,
                    (int) Mth.lerp(Mth.clamp(clip/124, 0, 1), 0, 120)
            );

        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        pose.popPose();
        pose.pushPose();
        pose.translate(0, (guiTop - 33 - y) - (int) (guiTop - 33 - y), 0);
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

    private Iterable<? extends Renderable> getRenderables() {
        return ((ScreenAccessor) this).getRenderables();
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        renderMenuBackground(guiGraphics);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderWindow(guiGraphics, mouseX, mouseY, partialTick);

        for (Renderable renderable : getRenderables())
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);

        // + 15, + 120
        float tagY = Mth.lerp(Mth.clamp(currentPage / Math.max(1, pages - 1f), 0, 1), 15, 120);
        guiGraphics.blit(
                texture,
                guiLeft + 102,
                (int) (guiTop + tagY),
                112,
                0,
                16,
                16
        );

        poseStack.popPose();
    }

    public static List<String> search(String query, List<String> data) {
        String q = normalize(query);

        List<String> candidates = new ArrayList<>();

        // 阶段1：过滤
        for (String s : data) {
            String t = normalize(s);

            if (t.contains(q) || fuzzyMatch(q, t)) {
                candidates.add(s); // 注意：返回原字符串
            }
        }

        // 阶段2：排序
        candidates.sort(Comparator.comparingDouble(s -> {
            String t = normalize(s);
            return score(q, t);
        }));

        return candidates;
    }

    public static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace(" ", "");
    }

    public static float score(String q, String t) {
        int lev = levenshtein(q, t);

        float prefix = t.startsWith(q) ? 1.0f : 0.0f;
        float subseq = fuzzyMatch(q, t) ? 1.0f : 0.0f;

        float lenPenalty = Math.abs(t.length() - q.length()) * 0.1f;

        return lev - prefix * 2 - subseq * 1 + lenPenalty;
    }

    public static int levenshtein(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[n][m];
    }

    public static boolean fuzzyMatch(String pattern, String text) {
        int i = 0;
        for (char c : text.toCharArray()) {
            if (i < pattern.length() && c == pattern.charAt(i)) i++;
        }
        return i == pattern.length();
    }
}
