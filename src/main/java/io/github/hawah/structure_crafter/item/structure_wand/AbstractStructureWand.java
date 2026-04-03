package io.github.hawah.structure_crafter.item.structure_wand;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.StructureData;
import io.github.hawah.structure_crafter.client.handler.StructureWandHandler;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.ITooltipItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.GZIPInputStream;

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

    @SuppressWarnings("ConstantValue")
    public static StructureData loadSchematic(Level level, ItemStack blueprint) {
        StructureTemplate t = new StructureTemplate();
        String owner = "Dev";
        String schematic = blueprint.get(DataComponentTypeRegistries.STRUCTURE_FILE);

        if (owner == null || schematic == null || !schematic.endsWith(".nbt"))//TODO
            return null;

        Path dir;
        Path file;

        if (!level.isClientSide()) {
            dir = io.github.hawah.structure_crafter.Paths.STRUCTURE_DIR;
            file = Paths.get(schematic);
        } else {
            dir = io.github.hawah.structure_crafter.Paths.STRUCTURE_DIR;
            file = Paths.get(schematic);
        }

        Path path = dir.resolve(file).normalize();
        if (!path.startsWith(dir))
            return null;

        BlockPos pos;

        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {

            CompoundTag nbt = NbtIo.read(stream, NbtAccounter.create(0x20000000L));
            t.load(level.holderLookup(Registries.BLOCK), nbt);
            if (nbt.contains("center")) {
                ListTag center = nbt.getList("center", CompoundTag.TAG_INT);
                pos = new BlockPos(
                        center.getInt(0),
                        center.getInt(1),
                        center.getInt(2)
                );
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return new StructureData(t, pos);
    }

    @Override
    public void handleTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        int t = 1;
        if (!Screen.hasShiftDown()) {
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
//            StructureWandHandler.ItemStackData data = StructureCrafterClient.STRUCTURE_WAND_HANDLER.data;
//            tooltipElements
//                    .add(t++, Either.left((data.isUpdateAll?
//                            LangData.CONFIG_STRUCTURE_WAND_UPDATE_ALL :
//                            LangData.CONFIG_STRUCTURE_WAND_NO_UPDATE)
//                            .get()
//                            .withStyle(
//                                    data.currentConfiguration.equals(StructureWandHandler.ItemStackData.Configuration.UPDATE_ALL)?
//                                            ChatFormatting.WHITE :
//                                            ChatFormatting.DARK_GRAY)));
//            tooltipElements
//                    .add(t++, Either.left((data.isReplaceAir?
//                            LangData.CONFIG_STRUCTURE_WAND_CLEAR_AREA :
//                            LangData.CONFIG_STRUCTURE_WAND_KEEP_AREA)
//                            .get()
//                            .withStyle(
//                                    data.currentConfiguration.equals(StructureWandHandler.ItemStackData.Configuration.REPLACE_AIR)?
//                                            ChatFormatting.WHITE :
//                                            ChatFormatting.DARK_GRAY)));
//            tooltipElements
//                    .add(t++, Either.left((data.isRenderBoundingBox?
//                            LangData.CONFIG_STRUCTURE_WAND_RENDER_BOUNDS :
//                            LangData.CONFIG_STRUCTURE_WAND_RENDER_NONE)
//                            .get()
//                            .withStyle(
//                                    data.currentConfiguration.equals(StructureWandHandler.ItemStackData.Configuration.RENDER_BOUNDING_BOX)?
//                                            ChatFormatting.WHITE :
//                                            ChatFormatting.DARK_GRAY)));
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

    @OnlyIn(Dist.CLIENT)
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

}
