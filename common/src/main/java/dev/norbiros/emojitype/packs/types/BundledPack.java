package dev.norbiros.emojitype.packs.types;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.packs.PackType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BundledPack extends EmojiPack {
    public static final String ASSETS_PACKS_PATH = "assets/" + EmojiType.MOD_ID + "/emoji_packs/";

    public BundledPack() {
        super();
        this.type = PackType.BUNDLED.getId();
    }

    public BundledPack(String name, String description) {
        super(name, description, PackType.BUNDLED);
    }

    public static boolean copyBundledPackToLocal(String identifier, boolean overwrite) {
        try {
            PackLoader.ensurePacksDirectory();

            Path destination = PackLoader.PACKS_DIRECTORY.resolve(identifier);
            if (!overwrite && Files.exists(destination)) {
                return false;
            }

            String resourcePath = ASSETS_PACKS_PATH + identifier;
            try (InputStream is = BundledPack.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    EmojiType.LOGGER.error("Bundled pack not found in assets: {}", identifier);
                    return false;
                }

                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Files.writeString(destination, content, StandardCharsets.UTF_8);
                EmojiType.LOGGER.info("{} bundled pack: {}", overwrite ? "Restored" : "Copied", identifier);
                return true;
            }
        } catch (IOException e) {
            EmojiType.LOGGER.error("Failed to copy bundled pack: {}", identifier, e);
            return false;
        }
    }

    public boolean restoreToDefault() {
        if (fileName == null) {
            return false;
        }

        return copyBundledPackToLocal(fileName, true);
    }
}
