package dev.norbiros.emojitype.packs;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.packs.types.BundledPack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class DefaultPackGenerator {

    public static final String[] DEFAULT_PACK_FILES = {
            "minecraft.yaml",
            "text_art_faces.yaml",
            "letters.yaml"
    };

    public static void generateDefaultPacks() {
        try {
            if (!Files.exists(PackLoader.PACKS_DIRECTORY)) {
                Files.createDirectories(PackLoader.PACKS_DIRECTORY);
                EmojiType.LOGGER.info("Created emoji packs directory: {}", PackLoader.PACKS_DIRECTORY);
            }

            for (String packFileName : DEFAULT_PACK_FILES) {
                Path packFile = PackLoader.PACKS_DIRECTORY.resolve(packFileName);
                if (!Files.exists(packFile)) {
                    BundledPack.copyBundledPackToLocal(packFileName, false);
                }
            }
        } catch (IOException e) {
            EmojiType.LOGGER.error("Failed to generate default packs", e);
        }
    }
}
