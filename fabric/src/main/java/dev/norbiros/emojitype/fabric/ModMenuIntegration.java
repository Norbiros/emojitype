package dev.norbiros.emojitype.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.norbiros.emojitype.config.ui.EmojiTypeConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EmojiTypeConfig::createConfigScreen;
    }
}