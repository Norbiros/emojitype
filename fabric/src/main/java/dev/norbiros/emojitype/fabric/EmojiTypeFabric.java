package dev.norbiros.emojitype.fabric;

import dev.norbiros.emojitype.EmojiType;
import net.fabricmc.api.ModInitializer;

public class EmojiTypeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EmojiType.init();
    }
}