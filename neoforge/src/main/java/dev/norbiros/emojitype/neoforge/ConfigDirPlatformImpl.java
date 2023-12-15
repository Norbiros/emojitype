package dev.norbiros.emojitype.neoforge;

import dev.norbiros.emojitype.ConfigDirPlatform;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigDirPlatformImpl {
    /**
     * This is our actual method to {@link ConfigDirPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}