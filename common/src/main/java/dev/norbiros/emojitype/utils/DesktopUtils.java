package dev.norbiros.emojitype.utils;

import dev.norbiros.emojitype.EmojiType;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DesktopUtils {

    private DesktopUtils() {
    }

    public static void openInFileManager(Path path) {
        try {
            Files.createDirectories(path);

            String operatingSystem = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (operatingSystem.contains("win")) {
                processBuilder = new ProcessBuilder("explorer", path.toString());
            } else if (operatingSystem.contains("mac")) {
                processBuilder = new ProcessBuilder("open", path.toString());
            } else {
                processBuilder = new ProcessBuilder("xdg-open", path.toString());
            }

            processBuilder.start();
            EmojiType.LOGGER.info("Opened folder: {}", path.toAbsolutePath());
        } catch (Exception exception) {
            EmojiType.LOGGER.error("Failed to open folder: {}", exception.getMessage());
        }
    }
}
