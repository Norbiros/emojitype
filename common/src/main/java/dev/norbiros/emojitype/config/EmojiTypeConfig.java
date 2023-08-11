package dev.norbiros.emojitype.config;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

    public class EmojiTypeConfig    {
        public static Screen createConfigScreen(Screen parent) {
            ConfigUtil.deserialise();
            ConfigBuilder builder = ConfigBuilder.create();
            builder.setTitle(new TranslatableText("config.emojitype.title"));
            builder.setSavingRunnable(ConfigUtil::serialise);
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            builder.getOrCreateCategory(new TranslatableText("config.emojitype.category"))
                .addEntry(entryBuilder.startStrList(new TranslatableText("config.emojitype.codes"), ConfigUtil.emojiCodeStrings)
                    .setDefaultValue(() -> {
                        ArrayList<String> l = new ArrayList<>();
                        for (EmojiCode ec : EmojiType.DEFAULT_EMOJI_CODES) {
                            l.add(ec.toString());
                        }
                        return l;
                    })
                    .setSaveConsumer((List<String> lstr) -> {
                        ConfigUtil.emojiCodeStrings.clear();
                        ConfigUtil.emojiCodeStrings.addAll(lstr);
                    })
                    .build());
            return builder.setParentScreen(parent).build();
        }
    }
