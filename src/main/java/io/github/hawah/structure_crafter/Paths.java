package io.github.hawah.structure_crafter;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Paths {
    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
    public static final Path STRUCTURE_DIR = GAME_DIR.resolve("schematics");
    public static final Path UPLOAD_STRUCTURE_DIR = STRUCTURE_DIR.resolve("uploaded");
}
