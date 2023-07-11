package cyborgcabbage.emojitype.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import cyborgcabbage.emojitype.EmojiTypeMod;
import cyborgcabbage.emojitype.emoji.EmojiCode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class EmojiTypeConfig implements ModMenuApi {
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return screen -> {
            ConfigUtil.deserialise();
            ConfigBuilder builder = ConfigBuilder.create();
            builder.setTitle(Text.translatable("config.emojitype.title"));
            builder.setSavingRunnable(ConfigUtil::serialise);
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            builder.getOrCreateCategory(Text.translatable("config.emojitype.category"))
                    .addEntry(entryBuilder.startStrList(Text.translatable("config.emojitype.codes"), ConfigUtil.emojiCodeStrings)
                            .setDefaultValue(() -> {
                                ArrayList<String> l = new ArrayList<>();
                                for (EmojiCode ec : EmojiTypeMod.DEFAULT_EMOJI_CODES) {
                                    l.add(ec.toString());
                                }
                                return l;
                            })
                            .setSaveConsumer((List<String> lstr) -> {
                                ConfigUtil.emojiCodeStrings.clear();
                                ConfigUtil.emojiCodeStrings.addAll(lstr);
                            })
                            .build());
            return builder.setParentScreen(screen).build();
        };
    }
}
