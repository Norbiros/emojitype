package dev.norbiros.emojitype.neoforge;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.config.EmojiTypeConfig;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod(EmojiType.MOD_ID)
public class EmojitypeNeoforge {
    public EmojitypeNeoforge() {
        EmojiType.init();

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> EmojiTypeConfig.createConfigScreen(parent)));
    }
}