package dev.norbiros.emojitype.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.packs.types.LocalPack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ConfigManager {
    private static final Path CONFIG_FILE = EmojiType.CONFIG_BASE_DIR
            .resolve("config.json");

    private static final Path LEGACY_CONFIG_FILE = EmojiType.getConfigDirectory()
            .resolve(EmojiType.MOD_ID + ".json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Known hashes of default configs that can be safely migrated
    private static final Set<String> KNOWN_DEFAULT_CONFIG_HASHES = new HashSet<>(Arrays.asList(
            // Empty legacy config: []
            "58e0494c51d30eb3494f7c9198986bb9",
            "d06f855de8fc0fe01eec5c4f104ec177",
            "3395d53b28f43b4f368a6a86705df98c"
    ));

    public static Config load() {
        if (Files.exists(LEGACY_CONFIG_FILE) && !Files.exists(CONFIG_FILE)) {
            EmojiType.LOGGER.info("Found config at old location, migrating...");
            migrateFromOldLocation();
            return load();
        }

        try {
            String json = Files.readString(CONFIG_FILE);
            Config config = GSON.fromJson(json, Config.class);
            if (config != null) {
                EmojiType.LOGGER.info("Loaded config with {} enabled packs", config.enabledPacks.size());
                return config;
            }
        } catch (Exception e) {
            EmojiType.LOGGER.error("Failed to load config", e);
        }

        return createDefaultConfig();
    }


    public static void save(Config config) {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_FILE, json);
            EmojiType.LOGGER.info("Saved config");
        } catch (IOException e) {
            EmojiType.LOGGER.error("Failed to save config", e);
        }
    }


    private static Config createDefaultConfig() {
        Config config = new Config();
        // Will be populated with all available pack names on first load
        return config;
    }


    private static void migrateFromOldLocation() {
        try {
            String json = Files.readString(LEGACY_CONFIG_FILE);

            // It's the newer format with packs, check the hash
            String configHash = calculateMD5(json);
            EmojiType.LOGGER.info("Old config hash: {}", configHash);

            Config config;
            if (KNOWN_DEFAULT_CONFIG_HASHES.contains(configHash)) {
                EmojiType.LOGGER.info("Config matches known default, using default packs");
                config = createDefaultConfig();
            } else {
                EmojiType.LOGGER.info("Config has custom modifications, creating legacy pack");
                config = migrateVeryOldLegacyConfig(json);
            }

            // Save to new location
            save(config);

            // Backup old config
            backupLegacyConfig();
        } catch (Exception e) {
            EmojiType.LOGGER.error("Failed to migrate config from old location", e);
            Config config = createDefaultConfig();
            save(config);
        }
    }


    private static String calculateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            EmojiType.LOGGER.error("MD5 algorithm not found", e);
            return "";
        }
    }

    private static Config migrateVeryOldLegacyConfig(String json) {
        try {
            // Parse the old format
            List<String> legacyEmojis = GSON.fromJson(json, new TypeToken<List<String>>() {
            }.getType());

            if (legacyEmojis == null || legacyEmojis.isEmpty()) {
                EmojiType.LOGGER.info("Legacy config was empty, using defaults");
                return createDefaultConfig();
            }

            // Create a legacy pack from the old emojis
            Map<String, String> emojiMap = new LinkedHashMap<>();
            for (String entry : legacyEmojis) {
                String[] parts = entry.split(";", 2);
                if (parts.length == 2) {
                    emojiMap.put(parts[0], parts[1]);
                }
            }

            return createLegacyPack(emojiMap);

        } catch (Exception e) {
            EmojiType.LOGGER.error("Failed to parse very old legacy config", e);
            return createDefaultConfig();
        }
    }

    /**
     * Create a "Legacy Emojis" pack and disable all default packs
     */
    private static Config createLegacyPack(Map<String, String> emojis) {
        Config config = new Config();

        // Disable all default packs
        config.enabledPacks.clear();

        // Create legacy pack
        LocalPack legacyPack = new LocalPack();
        legacyPack.name = "Legacy Emojis";
        legacyPack.description = "Your custom emojis migrated from EmojiType v2";
        legacyPack.emojis = emojis;

        // Save the legacy pack
        String legacyPackFileName = "legacy_emojis.yaml";
        if (PackLoader.savePack(legacyPack, legacyPackFileName)) {
            config.enabledPacks.add(legacyPackFileName);
            EmojiType.LOGGER.info("Created legacy pack with {} emojis", emojis.size());
            
            // Use the existing save method instead of duplicating logic
            try {
                save(config);
                EmojiType.LOGGER.info("Successfully saved config with legacy pack");
                return config;
            } catch (Exception e) {
                EmojiType.LOGGER.error("Failed to save config after creating legacy pack, cleaning up", e);
                // Rollback: delete the legacy pack we just created
                PackLoader.deletePack(legacyPackFileName);
                return createDefaultConfig();
            }
        } else {
            EmojiType.LOGGER.error("Failed to save legacy pack, falling back to defaults");
            return createDefaultConfig();
        }
    }

    private static void backupLegacyConfig() {
        try {
            Path backup = LEGACY_CONFIG_FILE.resolveSibling(EmojiType.MOD_ID + ".legacy.json");
            if (Files.exists(LEGACY_CONFIG_FILE) && !Files.exists(backup)) {
                Files.copy(LEGACY_CONFIG_FILE, backup);
                EmojiType.LOGGER.info("Backed up old config to: {}", backup);
            }
            Files.deleteIfExists(LEGACY_CONFIG_FILE);
        } catch (IOException e) {
            EmojiType.LOGGER.warn("Failed to backup/remove legacy config", e);
        }
    }

    public static class Config {
        public Set<String> enabledPacks = new LinkedHashSet<>();
        public int version = 1;
    }
}
