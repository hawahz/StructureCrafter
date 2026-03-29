package io.github.hawah.structure_crafter.item.structure_wand;

import io.github.hawah.structure_crafter.SchematicTransformation;
import io.github.hawah.structure_crafter.client.StructureData;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.StructureSelectorComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;

public abstract class AbstractStructureWand extends Item {
    public AbstractStructureWand(Properties properties) {
        super(properties.component(
                DataComponentTypeRegistries.STRUCTURE_FILE,
                "Debug.nbt"
        ));
    }

    public static StructureData loadSchematic(Level level, ItemStack blueprint) {
        StructureTemplate t = new StructureTemplate();
        String owner = "Dev";//blueprint.get(DataComponentTypeRegistries.SCHEMATIC_OWNER);
        String schematic = blueprint.get(DataComponentTypeRegistries.STRUCTURE_FILE);

        if (owner == null || schematic == null || !schematic.endsWith(".nbt"))
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

        BlockPos pos = null;

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

    public static SchematicTransformation getTransformation(Level level, ItemStack stack) {
        StructureData structureData = loadSchematic(level, stack);
        SchematicTransformation schematicTransformation = new SchematicTransformation();
        Vec3i size = structureData.structureTemplate().getSize();
        AABB aabb = new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ());
        schematicTransformation.init(structureData.center(), new StructurePlaceSettings(), aabb);
        return schematicTransformation;
    }
}
