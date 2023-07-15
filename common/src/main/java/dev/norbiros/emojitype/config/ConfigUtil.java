package dev.norbiros.emojitype.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.norbiros.emojitype.ConfigDirPlatform;
import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {
    public static Path CONFIG_PATH = ConfigDirPlatform.getConfigDirectory().resolve(EmojiType.MOD_ID + ".json");
    public static List<String> emojiCodeStrings = new ArrayList<>();

    public static void serialise() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(emojiCodeStrings));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EmojiType.update(emojiCodeStrings);
    }

    public static void deserialise() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                BufferedReader reader = Files.newBufferedReader(CONFIG_PATH);
                emojiCodeStrings = new Gson().fromJson(reader, new TypeToken<ArrayList<String>>() {
                }.getType());
                reader.close();
            } catch (IOException e) {
                e.printStackTrace(); //dirty consume
            }
            EmojiType.update(emojiCodeStrings);
        } else {
            // From defaults
            emojiCodeStrings.clear();
            for (EmojiCode ec : EmojiType.DEFAULT_EMOJI_CODES) {
                emojiCodeStrings.add(ec.toString());
            }
            serialise();
        }
    }
}