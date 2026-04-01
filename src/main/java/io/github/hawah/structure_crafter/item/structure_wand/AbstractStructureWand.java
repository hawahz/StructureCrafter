package io.github.hawah.structure_crafter.item.structure_wand;

import com.mojang.datafixers.util.Either;
import io.github.hawah.structure_crafter.client.StructureData;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.item.ITooltipItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
    public AbstractStructureWand(Properties properties) {
        super(properties.component(
                DataComponentTypeRegistries.STRUCTURE_FILE,
                "Debug.nbt"
        ));
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
            tooltipElements.add(t, Either.left(LangData.TOOLTIP_WAND_9.get()));
        }
    }
}
