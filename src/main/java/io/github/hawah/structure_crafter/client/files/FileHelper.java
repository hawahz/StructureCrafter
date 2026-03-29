package io.github.hawah.structure_crafter.client.files;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileHelper {
    public static String getValidFileName(String fileName, Path dir, String suffix) {
        int index = 0;
        String filename;
        Path filepath;
        do {
            filename = fileName + ((index == 0) ? "" : "_" + index) + "." + suffix;
            index++;
            filepath = dir.resolve(filename);
        } while (Files.exists(filepath));
        return filename;
    }
}
