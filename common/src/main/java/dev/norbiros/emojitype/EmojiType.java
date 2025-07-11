package dev.norbiros.emojitype;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.norbiros.emojitype.config.ConfigUtil;
import dev.norbiros.emojitype.emoji.EmojiCode;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EmojiType {
    public static final String MOD_ID = "emojitype";

    public static ArrayList<String> allCodes = new ArrayList<>();
    public static ArrayList<String> allEmoji = new ArrayList<>();
    public static ArrayList<String> emojiCodesCombined = new ArrayList<>();
    public static ArrayList<EmojiCode> emojiCodes = new ArrayList<>();

    public static void update(List<String> emojiCodeStrings) {
        emojiCodes.clear();
        allCodes.clear();
        allEmoji.clear();
        emojiCodesCombined.clear();
        for (String str : emojiCodeStrings) {
            EmojiCode.fromString(str).ifPresent(emojiCodes::add);
        }
        for (EmojiCode ec : emojiCodes) {
            allCodes.add(ec.getCode());
            allEmoji.add(ec.getEmoji());
            emojiCodesCombined.add(ec.getCode() + " " + ec.getEmoji());
        }
    }

    public static void init() {
        System.out.println("EmojiType initialized!");
        ConfigUtil.deserialise();
    }

    @ExpectPlatform
    @Contract(value = "-> _", pure = true)
    public static Path getConfigDirectory() {
        //noinspection Contract
        throw new AssertionError("Missing implementation of EmojiType.getConfigDirectory()");
    }
}
