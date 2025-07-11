package dev.norbiros.emojitype.neoforge;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.config.EmojiTypeConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

import java.nio.file.Path;

@Mod(EmojiType.MOD_ID)
public class EmojiTypeImpl {
    public EmojiTypeImpl() {
        EmojiType.init();

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (client, parent) -> EmojiTypeConfig.createConfigScreen(parent));
    }

    /**
     * Implementation of {@link EmojiType#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}