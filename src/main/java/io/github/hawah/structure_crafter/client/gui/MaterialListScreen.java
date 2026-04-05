package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.client.gui.utils.ButtonGroup;
import io.github.hawah.structure_crafter.client.gui.utils.TextureButton;
import io.github.hawah.structure_crafter.client.gui.utils.TextureToggleButton;
import io.github.hawah.structure_crafter.client.handler.StructureHandler;
import io.github.hawah.structure_crafter.client.render.EaseHelper;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.MaterialListComponent;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.networking.DropItemPacket;
import io.github.hawah.structure_crafter.networking.MaterialListScatteredPacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
@ParametersAreNonnullByDefault
public class MaterialListScreen extends BaseScreen{
    private static final int MAX_SLOTS = 9;
    private final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "material_list" + ".png");
    private TextureToggleButton materialToggleButton;
    private TextureToggleButton previewToggleButton;
    private ButtonGroup buttonGroup;
    private TextureButton clips;
    private TextureButton forward;
    private TextureButton backward;
    public List<String> materialTextPages = new ArrayList<>();
    public List<List<ItemEntry>> materialPage = new ArrayList<>();
    public List<String> statisticsTextPages = new ArrayList<>();
    public List<List<ItemEntry>> statisticsPage = new ArrayList<>();

    private int scatteredTick = -1;
    private int currentPage = 0;
    private int pages = 1;
    private boolean scattered = false;


    public MaterialListScreen() {
        super(Component.literal("material_list"));
    }

    @Override
    protected void init() {

        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        MaterialListComponent component = mainHandItem.getOrDefault(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY);

        ItemStack defaultInstance = Items.PAPER.getDefaultInstance();
        defaultInstance.setCount(99);

        CompletableFuture
                .supplyAsync(() -> {
                    List<ItemEntry> lines = component.getItemWithCounts();
                    List<String> pages = new ArrayList<>();
                    StringBuilder page = new StringBuilder();
                    int lineCounter = 0;
                    List<List<ItemEntry>> itemPages = new ArrayList<>();
                    itemPages.add(new ArrayList<>());

                    for (ItemEntry lineItem : lines) {
                        if (lineCounter >= MAX_SLOTS) {
                            pages.add(page.toString());
                            itemPages.add(new ArrayList<>());
                            page = new StringBuilder();
                            lineCounter = 0;
                        }
                        lineCounter ++;
                        page.append(lineItem.getHoverName().getString())
                                .append(" x")
                                .append(lineItem.getCount())
                                .append("\n");
                        itemPages.getLast().add(lineItem);
                    }

                    if (!page.isEmpty()) {
                        pages.add(page.toString());
                    }

                    return new Tuple<>(pages, itemPages);
                }).thenAccept( pages ->
                        Minecraft.getInstance().execute(() -> {
                            this.materialTextPages.clear();
                            this.materialTextPages.addAll(pages.getA());
                            this.materialPage.clear();
                            this.materialPage.addAll(pages.getB());
                            this.updatePage();
                        })
                ).thenApplyAsync((v)->{
                    LocalPlayer player = Minecraft.getInstance().player;
                    NonNullList<ItemStack> itemsRaw = StructureHandler.getInventoryItems(player);
                    HashMap<Integer, Integer> inventory = new HashMap<>();

                    for (ItemStack item : itemsRaw) {
                        inventory.put(Item.getId(item.getItem()), inventory.getOrDefault(Item.getId(item.getItem()), 0) + item.getCount());
                    }

                    List<ItemEntry> consumes = component.getItemWithCounts();
                    HashMap<Item, Integer> batchedMap = new HashMap<>();
                    for (ItemEntry consume : consumes) {
                        batchedMap.put(
                                consume.getItem(),
                                inventory.getOrDefault(Item.getId(consume.getItem()), 0) / consume.count()
                        );
                    }


                    return batchedMap.entrySet().stream().collect(Collectors.groupingBy(
                            Map.Entry::getValue,
                            Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                    ));
                }).thenAccept(batchedMap ->
                    Minecraft.getInstance().execute(() -> {
                        List<Integer> buildables = batchedMap.keySet().stream().sorted().toList();
                        statisticsTextPages.clear();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(LangData.GUI_PAGE_TOTAL_BUILD.get(buildables.getFirst()).getString())
                                .append("\n")
                                .append(LangData.GUI_PAGE_BOTTLENECK_MATERIAL.get().getString())
                                .append("\n");
                        int length = 2;
                        for (Item item : batchedMap.get(buildables.getFirst())) {
                            if (length >= MAX_SLOTS) {
                                statisticsTextPages.add(stringBuilder.toString());
                                stringBuilder = new StringBuilder();
                                length = 0;
                            }
                            stringBuilder.append(item.getDefaultInstance().getHoverName().getString())
                                    .append("\n");
                            length++;
                        }
                        statisticsTextPages.add(stringBuilder.toString());
                    })
                ).exceptionally(e -> {
                    StructureCrafter.LOGGER.error("Error When Sync", e);
                    return null;
                });

        setTextureSize(96, 137);
        super.init();

        int scale = Math.min(Math.floorDiv(width, textureWidth), Math.floorDiv(height, textureHeight));
        System.out.println(scale);

//        if (textureWidth * 2 < width && textureHeight * 2 < height) {
//            setScale(2);
//        } else {
//            setScale(1);
//        }

        int x = guiLeft;
        int y = guiTop;
        buttonGroup = new ButtonGroup();

        materialToggleButton = new TextureToggleButton(
                x + 11,
                y - 4,
                9,
                19,
                LangData.TOOLTIP_BUTTON_MATERIAL.get(),
                LangData.TOOLTIP_BUTTON_MATERIAL.get(),
                texture,
                18,
                0,
                18,
                0,
                0,
                0,
                0,
                0,
                false,
                this::updatePage
        );

        addRenderableWidget(materialToggleButton);
        materialToggleButton.setToggled(true);
        buttonGroup.addButton(materialToggleButton);

        previewToggleButton = new TextureToggleButton(
                x + 27,
                y - 4,
                9,
                21,
                LangData.TOOLTIP_BUTTON_PREVIEW.get(),
                LangData.TOOLTIP_BUTTON_PREVIEW.get(),
                texture,
                27,
                0,
                27,
                0,
                9,
                0,
                9,
                0,
                false,
                this::updatePage
        );

        addRenderableWidget(previewToggleButton);
        buttonGroup.addButton(previewToggleButton);

        clips = new TextureButton(
                x + 42,
                y,
                13,
                17,
                LangData.TOOLTIP_BUTTON_CLIP.get(),
                texture,
                80,
                0,
                80,
                0,
                80,
                0,
                () -> {
                    if (!Config.MATERIAL_LIST_SCATTERED_ENABLED.get()) {
                        clips.setFocused(false);
                        return;
                    }
                    scattered = true;
                    disableRenderComponents = true;
                    buttonGroup.disable();
                }
        );

        addRenderableWidget(clips);

        forward = new TextureButton(
                x + 73 - 6,
                y + 117 - 3,
                16,
                16,
                Component.empty(),
                texture,
                48,
                208,
                48,
                208,
                48,
                240,
                () -> turnPage(1)
        );
        addRenderableWidget(forward);
        forward.active = pages > 1;

        backward = new TextureButton(
                x + 18 - 6,
                y + 117 - 3,
                16,
                16,
                Component.empty(),
                texture,
                64,
                208,
                64,
                208,
                64,
                240 ,
                () -> turnPage(-1)
        );
        addRenderableWidget(backward);
        backward.active = false;

        setScale(Math.min(2, scale));
    }



    @Override
    protected void renderWindowPost(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWindowPost(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWindowPre(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        applyScaleTransform(pose);


        final int FALL_DURATION = 18;
        final int DELAY = 0;

        float frontFallProcess = EaseHelper.easeInPow(Mth.clamp((scatteredTick + partialTick - DELAY) / FALL_DURATION, 0, 1), 2);
        float backFallProcess = EaseHelper.easeInPow(Mth.clamp((scatteredTick + partialTick - DELAY - 2) / FALL_DURATION, 0, 1), 2);

        float frontY = frontFallProcess * (height - guiTop + 20);
        float backY = backFallProcess * (height - guiTop + 20);

        if (scattered) {
            // Clip Bottom
            guiGraphics.blit(
                    texture,
                    guiLeft + 42,
                    guiTop,
                    36,
                    0,
                    13,
                    14
            );
        }

        // Bottom Paper
        guiGraphics.blit(
                texture,
                guiLeft,
                scattered? (int) (guiTop + 9 + backY) : guiTop,
                scattered? 98: 0,
                scattered? 126: 21,
                textureWidth,
                scattered? 128: textureHeight
        );
        if (scattered) {

            // Front Paper
            guiGraphics.blit(
                    texture,
                    guiLeft,
                    (int) (guiTop + 10 + frontY),
                    98,
                    0,
                    96,
                    125
            );

            // Red Tip
            guiGraphics.blit(
                    texture,
                    guiLeft + 11,
                    (int) (guiTop - 4 + (materialToggleButton.isToggled()? frontY : backY)),
                    materialToggleButton.isToggled()? 0: 18,
                    0,
                    9,
                    19
            );

            // Blue Tip
            guiGraphics.blit(
                    texture,
                    guiLeft + 27,
                    (int) (guiTop - 4 + (previewToggleButton.isToggled()? frontY : backY)),
                    previewToggleButton.isToggled()? 9: 27,
                    0,
                    9,
                    20
            );

            // Clip Top
            guiGraphics.blit(
                    texture,
                    guiLeft + 42,
                    guiTop,
                    65,
                    0,
                    13,
                    14
            );
        }

        // Page Indicator
        guiGraphics.drawCenteredString(
                font,
                Component.literal("- " + (currentPage + 1) + "/" + pages + " -"),
                guiLeft+54 - 6,
                (int)( guiTop + 121 - 3 + frontY),
                0xFFFFFF
        );

        if (materialTextPages.isEmpty()) {
            pose.popPose();
            return;
        }

        String firstPage;
        List<ItemEntry> itemPageFirst;
        try {
            firstPage = materialToggleButton.isToggled()? materialTextPages.get(currentPage) : statisticsTextPages.isEmpty()? "" : statisticsTextPages.get(currentPage);
            itemPageFirst = materialToggleButton.isToggled()? materialPage.get(currentPage): List.of();
        } catch (Exception e) {
            StructureCrafter.LOGGER.error("Error occurred when turning pages. Page {}, Total {}", pages, currentPage, e);
            pose.popPose();
            return;
        }

        int i = 0;

        pose.popPose();

        pose.pushPose();
        float sc = 0.5F;
        int left = guiLeft + 40 - 20;
        int top = guiTop -30;
        pose.translate(guiLeft + 26, guiTop + 19 - font.lineHeight/2F + 55, 0);
        pose.scale(sc,sc,sc);
        pose.translate(-(guiLeft + 26), -(guiTop + 19 - font.lineHeight/2F + 55), 0);
        applyScaleTransform(pose);


        for (String line : firstPage.split("\n")) {
            guiGraphics.drawString(
                    font,
                    line,
                    itemPageFirst.isEmpty() || itemPageFirst.get(i).count() <= 0? left - 26: left,
                    top + 8 - font.lineHeight/2F + i * 11*2 + frontY / sc,
                    0x000000,
                    false
            );

            if (itemPageFirst.isEmpty()) {
                i++;
                continue;
            }
            ItemStack instance = itemPageFirst.get(Math.min(i, itemPageFirst.size()-1)).getDefaultStack();
            guiGraphics.renderFakeItem(
                    instance,
                    left - 26,
                    (int) (top + i * 11*2 + frontY/ sc)
            );
            i++;
        }

        pose.popPose();
        pose.pushPose();

        Vec2 originalMousePos = getOriginalMousePos(mouseX, mouseY);
        mouseX = (int) originalMousePos.x;
        mouseY = (int) originalMousePos.y;
        left = (int) ((left - width/2F)*getScale() + width/2F);

        if ((mouseY - top) % 22 > 0 && (mouseY - top) / 22 < itemPageFirst.size() && mouseX > left - 26/getScale() && mouseX < left + 26/getScale() && !scattered) {
            guiGraphics.renderTooltip(
                    font,
                    itemPageFirst.get((mouseY - top) / 22).getDefaultStack(),
                    mouseX,
                    mouseY
            );
        }

        pose.popPose();

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void updatePage() {
        pages = (int) (double) (materialToggleButton.isToggled() ? materialPage.size() : statisticsTextPages.size());
        currentPage = 0;
        if (forward != null && backward != null) {
            forward.active = currentPage < pages - 1;
            backward.active = false;
        }
    }

    private void turnPage(int direction) {
//        prevPage = currentPage;
        currentPage = Mth.clamp(currentPage + direction, 0, pages - 1);
        forward.active = currentPage < pages - 1;
        backward.active = currentPage > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (scattered) {
            scatteredTick ++;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        LocalPlayer player = Minecraft.getInstance().player;
        if (scattered && !player.isSpectator() && !player.isCreative()) {
            Networking.sendToServer(new DropItemPacket(List.of(Items.PAPER.getDefaultInstance())));
            Networking.sendToServer(new MaterialListScatteredPacket(0));
        }
    }
}
