package io.github.hawah.structure_crafter.item.structure_wand;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.util.StructureHandler;
import io.github.hawah.structure_crafter.util.StructureData;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.ITooltipItem;
import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import io.github.hawah.structure_crafter.networking.MaterialListUploadPacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import io.github.hawah.structure_crafter.util.ItemEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractStructureWand extends Item implements ITooltipItem {

    public static final int REPLACE_AIR = 1;
    public static final int UPDATE_ALL = 2;
    public static final int FORCE_BOUNDS_VISIBLE = 4;

    public AbstractStructureWand(Properties properties) {
        super(properties.component(
                DataComponentTypeRegistries.STRUCTURE_FILE,
                "Debug.nbt"
        ).stacksTo(1));
    }

    @Override
    public void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        int t = 1;
        if (!ITooltipItem.isShiftDown()) {
            tooltipElements.add(t, Either.left(LangData.SHIFT.get()));
        } else {
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_0.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_1.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_2.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_3.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_4.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_5.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_6.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_7.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_8.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_9.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_10.get()));
            tooltipElements.add(t++, Either.left(LangData.TOOLTIP_WAND_11.get()));
        }
    }

    public static boolean isReplaceAir(ItemStack stack) {
        return (stack.getOrDefault(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, 0) & REPLACE_AIR) != 0;
    }

    public static void setReplaceAir(ItemStack stack, boolean replaceAir) {
        int settings = stack.getOrDefault(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, 0);
        if (replaceAir)
            settings |= REPLACE_AIR;
        else
            settings &= ~REPLACE_AIR;
        stack.set(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, settings);
    }

    public static int getUpdateFlags(ItemStack stack) {
        return (stack.getOrDefault(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, 0) & UPDATE_ALL) == 0 ?
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE :
                Block.UPDATE_ALL;
    }

    public static void setUpdateFlags(ItemStack stack, int updateFlags) {
        int settings = stack.getOrDefault(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, 0);
        if (updateFlags == Block.UPDATE_ALL)
            settings |= UPDATE_ALL;
        else
            settings &= ~UPDATE_ALL;
        stack.set(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, settings);
    }

    public static void setOwnerName(ItemStack stack, Player player) {
        setOwnerName(stack, player.getName().getString());
    }

    public static void setOwnerName(ItemStack stack, String name) {
        stack.set(DataComponentTypeRegistries.STRUCTURE_OWNER, name);
    }

    public static boolean isBoundsVisible(ItemStack stack) {
        return (stack.getOrDefault(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, 0) & FORCE_BOUNDS_VISIBLE) != 0;
    }

    public static void setBoundsVisible(ItemStack stack, boolean visible) {
        int settings = stack.getOrDefault(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, 0);
        if (visible)
            settings |= FORCE_BOUNDS_VISIBLE;
        else
            settings &= ~FORCE_BOUNDS_VISIBLE;
        stack.set(DataComponentTypeRegistries.STRUCTURE_WAND_SETTINGS, settings);
    }

    public static void selectStructure(ItemStack stack, String structure, Player player) {
        stack.set(DataComponentTypeRegistries.STRUCTURE_FILE, structure);
        if (player == null) {
            return;
        }
        recordMaterialListOffhand(stack, player);
    }

    private static void recordMaterialListOffhand(ItemStack stack, Player player) {
        ItemStack itemStack = player.getOffhandItem();
        if (!isRecordMaterialValid(itemStack))
            return;

        StructureData structureData = StructureHandler.loadSchematic(player.level(), stack);
        List<StructureTemplate.StructureBlockInfo> blockInfos = new StructurePlaceSettings().getRandomPalette(((StructureTemplateAccessor) structureData.structureTemplate()).getPalettes(), BlockPos.ZERO).blocks();
        HashMap<Item, Integer> neededItems = getNeededItems(blockInfos);
        Stream<Map.Entry<Item, Integer>> consumedItems = neededItems.entrySet().stream().sorted(
                Comparator.comparingInt(e -> -e.getValue())
        );
        List<String> lines = consumedItems
                .map(e -> e.getKey().getDescription().getString() + " x" + e.getValue())
                .toList();
        List<String> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();
        int lineCounter = 0;

        for (String line : lines) {
            if (lineCounter > 13) {
                pages.add(page.toString());
                page = new StringBuilder();
                lineCounter = 0;
            }
            lineCounter ++;
            page.append(line).append("\n");
        }

        // TODO Configurable
        if (!page.isEmpty()) pages.add(page.toString());
        if (itemStack.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
//            Minecraft.getInstance()
//                    .getConnection()
//                    .send(new ServerboundEditBookPacket(
//                            40,
//                            pages,
//                            Optional.empty())
//                    );
        } else if (itemStack.is(Items.WRITTEN_BOOK)) {
        } else if (itemStack.has(DataComponentTypeRegistries.MATERIAL_LIST)) {
            List<ItemEntry> listedItemStack = neededItems.entrySet().stream().sorted(
                    Comparator.comparingInt(e -> -e.getValue())
            ).map(ItemEntry::fromEntry).toList();
            Networking.sendToServer(new MaterialListUploadPacket(listedItemStack));
        }
    }

    private static @NotNull HashMap<Item, Integer> getNeededItems(List<StructureTemplate.StructureBlockInfo> blockInfos) {
        return StructureHandler.getNeededItems(blockInfos);
    }

    public static boolean isRecordMaterialValid(ItemStack stack) {
        return stack.has(DataComponents.WRITABLE_BOOK_CONTENT) ||
                stack.has(DataComponents.WRITTEN_BOOK_CONTENT) ||
                stack.has(DataComponentTypeRegistries.MATERIAL_LIST);
    }

}
