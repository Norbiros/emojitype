package dev.norbiros.emojitype.fabric;

import dev.norbiros.emojitype.ConfigDirPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigDirPlatformImpl {
    /**
     * This is our actual method to {@link ConfigDirPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}