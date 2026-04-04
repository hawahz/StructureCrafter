package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.client.gui.utils.ButtonGroup;
import io.github.hawah.structure_crafter.client.gui.utils.TextureButton;
import io.github.hawah.structure_crafter.client.gui.utils.TextureToggleButton;
import io.github.hawah.structure_crafter.client.render.EaseHelper;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.MaterialListComponent;
import io.github.hawah.structure_crafter.networking.DropItemPacket;
import io.github.hawah.structure_crafter.networking.MaterialListScatteredPacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class MaterialListScreen extends BaseScreen{
    private final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "material_list" + ".png");
    private TextureToggleButton materialToggleButton;
    private TextureToggleButton previewToggleButton;
    private ButtonGroup buttonGroup;
    private TextureButton clips;
    public List<String> materialTextPages = new ArrayList<>();
    public List<List<ItemEntry>> materialPage = new ArrayList<>();
    private int scatteredTick = -1;
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
                        if (lineCounter > 9) {
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
                        })
                );

        setTextureSize(96, 137);
        super.init();
//        System.out.println(textureWidth * 2);
//        System.out.println(width);
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
                Component.literal("Materials"),
                Component.literal("Materials"),
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
                () -> {
                }
        );

        addRenderableWidget(materialToggleButton);
        materialToggleButton.setToggled(true);
        buttonGroup.addButton(materialToggleButton);

        previewToggleButton = new TextureToggleButton(
                x + 27,
                y - 4,
                9,
                21,
                Component.literal("Preview"),
                Component.literal("Preview"),
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
                () -> {
                }
        );

        addRenderableWidget(previewToggleButton);
        buttonGroup.addButton(previewToggleButton);

        clips = new TextureButton(
                x + 42,
                y,
                13,
                17,
                Component.literal("Clips"),
                texture,
                80,
                0,
                80,
                0,
                80,
                0,
                () -> {
                    scattered = true;
                    disableRenderComponents = true;
                    buttonGroup.disable();
                }
        );

        addRenderableWidget(clips);

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

        if (materialTextPages.isEmpty()) {
            return;
        }

        String firstPage = materialTextPages.getFirst();
        List<ItemEntry> itemPageFirst = materialPage.getFirst();

        int i = 0;

        pose.popPose();

        pose.pushPose();

        for (String line : firstPage.split("\n")) {
            guiGraphics.drawString(
                    font,
                    line,
                    guiLeft + 26,
                    guiTop + 19 + 6 - font.lineHeight/2 + i * 11 + frontY,
                    0x000000,
                    false
            );

            ItemStack instance = itemPageFirst.get(i).getDefaultStack();
            guiGraphics.renderFakeItem(
                    instance,
                    guiLeft + 14,
                    (int) (guiTop + 19 + i * 11 + frontY)
            );
            i++;
        }

        pose.popPose();

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
