package dev.norbiros.emojitype;

import dev.norbiros.emojitype.config.ConfigManager;
import dev.norbiros.emojitype.emoji.EmojiCode;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.packs.types.EmojiPack;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.*;

public class EmojiType {
    public static final String MOD_ID = "emojitype";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Path CONFIG_BASE_DIR = EmojiType.getConfigDirectory()
            .resolve(MOD_ID);
    private static final List<EmojiCode> activeEmojiCodes = new ArrayList<>();
    private static final List<String> chatSuggestions = new ArrayList<>();
    private static Map<String, EmojiPack> availablePacks = new LinkedHashMap<>();
    private static ConfigManager.Config config;

    public static void init() {
        LOGGER.info("EmojiType initializing...");

        config = ConfigManager.load();
        availablePacks = PackLoader.loadAllPacks();

        if (config.enabledPacks.isEmpty()) {
            config.enabledPacks.addAll(availablePacks.keySet());
            ConfigManager.save(config);
        }

        reloadActiveEmojiCodes();

        LOGGER.info("EmojiType initialized with {} emojis from {} packs", activeEmojiCodes.size(), config.enabledPacks.size());
    }

    public static void reloadActiveEmojiCodes() {
        activeEmojiCodes.clear();
        chatSuggestions.clear();

        for (String packName : config.enabledPacks) {
            EmojiPack pack = availablePacks.get(packName);
            if (pack != null) {
                for (EmojiCode emojiCode : pack.getEmojiCodes()) {
                    // Filter out empty emoji codes (both code and emoji must be non-empty)
                    if (emojiCode.getCode() != null && !emojiCode.getCode().trim().isEmpty() &&
                        emojiCode.getEmoji() != null && !emojiCode.getEmoji().trim().isEmpty()) {
                        activeEmojiCodes.add(emojiCode);
                        chatSuggestions.add(emojiCode.getChatSuggestion());
                    }
                }
            }
        }

        LOGGER.info("Reloaded {} active emojis", activeEmojiCodes.size());
    }

    public static void reloadPacksFromDisk() {
        LOGGER.info("Reloading emoji packs from disk...");

        availablePacks = PackLoader.loadAllPacks();
        reloadActiveEmojiCodes();

        LOGGER.info("Packs reloaded successfully");
    }

    public static List<EmojiCode> getActiveEmojiCodes() {
        return Collections.unmodifiableList(activeEmojiCodes);
    }

    public static List<String> getChatSuggestions() {
        return Collections.unmodifiableList(chatSuggestions);
    }

    public static Map<String, EmojiPack> getAvailablePacks() {
        return Collections.unmodifiableMap(availablePacks);
    }

    public static ConfigManager.Config getConfig() {
        return config;
    }

    public static void enablePack(String packName) {
        if (availablePacks.containsKey(packName)) {
            config.enabledPacks.add(packName);
            ConfigManager.save(config);
            reloadActiveEmojiCodes();
        }
    }

    public static void disablePack(String packName) {
        config.enabledPacks.remove(packName);
        ConfigManager.save(config);
        reloadActiveEmojiCodes();
    }

    public static boolean isPackEnabled(String packName) {
        return config.enabledPacks.contains(packName);
    }

    @Contract(value = "-> _", pure = true)
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
