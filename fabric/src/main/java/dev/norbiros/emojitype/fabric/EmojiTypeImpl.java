package dev.norbiros.emojitype.fabric;

import dev.norbiros.emojitype.EmojiType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;

public class EmojiTypeImpl extends EmojiType implements ModInitializer {
    @Override
    public void onInitialize() {
        EmojiType.init();
    }

    /**
     * Implementation of {@link EmojiType#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}