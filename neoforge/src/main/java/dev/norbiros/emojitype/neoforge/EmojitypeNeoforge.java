package dev.norbiros.emojitype.neoforge;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.config.EmojiTypeConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod(EmojiType.MOD_ID)
public class EmojitypeNeoforge {
    public EmojitypeNeoforge() {
        EmojiType.init();

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (client, parent) -> EmojiTypeConfig.createConfigScreen(parent));
    }
}