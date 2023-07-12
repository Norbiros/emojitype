package cyborgcabbage.emojitype.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cyborgcabbage.emojitype.EmojiTypeMod;
import cyborgcabbage.emojitype.emoji.EmojiCode;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {
    public static Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(EmojiTypeMod.MOD_ID + ".json");
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
        EmojiTypeMod.update(emojiCodeStrings);
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
            EmojiTypeMod.update(emojiCodeStrings);
        } else {
            // From defaults
            emojiCodeStrings.clear();
            for (EmojiCode ec : EmojiTypeMod.DEFAULT_EMOJI_CODES) {
                emojiCodeStrings.add(ec.toString());
            }
            serialise();
        }
    }
}
