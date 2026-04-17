package io.github.hawah.structure_crafter.util;

import com.mojang.logging.LogUtils;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.util.exception.IllegalStructureNameException;
import io.github.hawah.structure_crafter.util.files.FileHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ServerStructureExport {

    public static void saveStructure(String fileName, Player owner, BlockPos firstPos, BlockPos secondPos, BlockPos centerPos, boolean overwrite) {
        if (firstPos == null) {
            return;
        }

        Path dir = Paths.UPLOAD_STRUCTURE_DIR.resolve(owner.getName().getString());

        BoundingBox bb = BoundingBox.fromCorners(firstPos, secondPos);
        BlockPos origin = new BlockPos(bb.minX(), bb.minY(), bb.minZ());
        BlockPos bounds = new BlockPos(bb.getXSpan(), bb.getYSpan(), bb.getZSpan());

        StructureTemplate structure = new StructureTemplate();
        Level level = owner.level();
        structure.fillFromWorld(level, origin, bounds, true, Blocks.AIR);
        CompoundTag data = structure.save(new CompoundTag());

        data.put("center", StructureHandler.newIntegerList(
                centerPos.getX() - origin.getX(),
                centerPos.getY() - origin.getY(),
                centerPos.getZ() - origin.getZ()
        ));

        if (!fileName.endsWith(".nbt") || (!overwrite && !FileHelper.getValidFileName(fileName, dir, "nbt").equals(fileName))) {
            throw new IllegalStructureNameException("Invalid Structure Name " + fileName + ".");
        }

        saveDataToDirectory(dir, Path.of(fileName), data);
    }

    public static void saveDataToDirectory(Path dir, Path fileName, CompoundTag data) {
        Path path = dir.resolve(fileName).toAbsolutePath();

        try {
            Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
                NbtIo.writeCompressed(data, out);
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("Occurred Error when saving structure.", e);
        }
    }
}
