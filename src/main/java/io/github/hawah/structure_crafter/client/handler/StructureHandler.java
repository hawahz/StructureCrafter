package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.Paths;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class StructureHandler {
    public static void loadStructures(List<Component> allStructures) {
        allStructures.clear();
        try (Stream<Path> paths = Files.list(Paths.STRUCTURE_DIR)) {
            paths.filter(f -> !Files.isDirectory(f) && f.getFileName().toString().endsWith(".nbt"))
                    .forEach(path -> {
                        if (Files.isDirectory(path))
                            return;

                        allStructures.add(Component.literal(path.getFileName().toString()));
                    });
        } catch (NoSuchFileException ignored) {
            // No Schematics created yet
        } catch (IOException ignored) {
        }
    }

    public static void loadStructuresString(List<String> allStructures) {
        allStructures.clear();
        try (Stream<Path> paths = Files.list(Paths.STRUCTURE_DIR)) {
            paths.filter(f -> !Files.isDirectory(f) && f.getFileName().toString().endsWith(".nbt"))
                    .forEach(path -> {
                        if (Files.isDirectory(path))
                            return;

                        allStructures.add(path.getFileName().toString());
                    });
        } catch (NoSuchFileException ignored) {
            // No Schematics created yet
        } catch (IOException ignored) {
        }
    }
}
