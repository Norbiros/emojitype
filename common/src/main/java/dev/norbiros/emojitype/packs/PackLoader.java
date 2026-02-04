package dev.norbiros.emojitype.packs;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.packs.types.BundledPack;
import dev.norbiros.emojitype.packs.types.EmojiPack;
import dev.norbiros.emojitype.packs.types.LocalPack;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static dev.norbiros.emojitype.EmojiType.CONFIG_BASE_DIR;

public class PackLoader {

    public static final Path PACKS_DIRECTORY = CONFIG_BASE_DIR.resolve("emoji_packs");

    public static Map<String, EmojiPack> loadAllPacks() {
        Map<String, EmojiPack> packs = new LinkedHashMap<>();

        DefaultPackGenerator.generateDefaultPacks();
        loadPacksFromDirectory(packs);

        return packs;
    }

    private static void loadPacksFromDirectory(Map<String, EmojiPack> packs) {
        try {
            if (!Files.exists(PACKS_DIRECTORY)) {
                EmojiType.LOGGER.error("Config packs directory does not exist (should have been created)");
                return;
            }

            if (!Files.isDirectory(PACKS_DIRECTORY)) {
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(PACKS_DIRECTORY, "*.{yaml,yml}")) {
                for (Path path : stream) {
                    String fileName = path.getFileName().toString();
                    Optional<EmojiPack> packOption = loadPackFromPath(path);

                    if (packOption.isPresent()) {
                        EmojiPack pack = packOption.get();
                        packs.put(fileName, pack);
                        EmojiType.LOGGER.info("Loaded pack: {} ({}) [{}]", fileName, pack.name, pack.type);
                    } else {
                        EmojiType.LOGGER.error("Failed to load pack: {}", fileName);
                    }
                }
            }
        } catch (IOException exception) {
            EmojiType.LOGGER.error("Error accessing config emoji packs directory", exception);
        }
    }

    public static Optional<EmojiPack> loadPack(String fileName) {
        return loadPackFromPath(PACKS_DIRECTORY.resolve(fileName));
    }

    public static boolean savePack(EmojiPack pack, String fileName) {
        try {
            ensurePacksDirectory();
            Path packPath = PACKS_DIRECTORY.resolve(fileName);
            String yamlContent = pack.toYaml();
            Files.writeString(packPath, yamlContent, StandardCharsets.UTF_8);
            EmojiType.LOGGER.info("Saved pack: {}", fileName);
            return true;
        } catch (IOException exception) {
            EmojiType.LOGGER.error("Failed to save pack: {}", fileName, exception);
            return false;
        }
    }

    public static boolean deletePack(String fileName) {
        Path packPath = PACKS_DIRECTORY.resolve(fileName);
        if (!Files.exists(packPath)) {
            EmojiType.LOGGER.error("Cannot delete pack: file does not exist: {}", fileName);
            return false;
        }

        try {
            Files.delete(packPath);
            EmojiType.LOGGER.info("Deleted pack: {}", fileName);
            return true;
        } catch (IOException exception) {
            EmojiType.LOGGER.error("Failed to delete pack: " + fileName, exception);
            return false;
        }
    }

    public static void ensurePacksDirectory() throws IOException {
        if (!Files.exists(PACKS_DIRECTORY)) {
            Files.createDirectories(PACKS_DIRECTORY);
        }
    }

    private static Optional<EmojiPack> loadPackFromPath(Path packPath) {
        if (!Files.exists(packPath)) {
            return Optional.empty();
        }

        try {
            String content = Files.readString(packPath, StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();

            // Note: We need to parse the YAML twice due to SnakeYAML's limitations with polymorphic types.
            // First parse: determine the pack type from the 'type' field
            Map<String, Object> packData = yaml.load(content);
            
            // If packData is null, the YAML file is empty or invalid
            if (packData == null) {
                EmojiType.LOGGER.error("Pack file is empty or invalid: {}", packPath.getFileName());
                return Optional.empty();
            }
            
            String typeString = (String) packData.getOrDefault("type", PackType.LOCAL.getId());
            PackType packType = PackType.fromId(typeString);

            // Second parse: deserialize into the specific pack class based on type
            EmojiPack pack;
            switch (packType) {
                case BUNDLED:
                    pack = yaml.loadAs(content, BundledPack.class);
                    break;
                case LOCAL:
                default:
                    pack = yaml.loadAs(content, LocalPack.class);
                    break;
            }

            if (pack != null && pack.emojis != null) {
                String fileName = packPath.getFileName().toString();
                pack.setFileName(fileName);

                if (pack.name == null || pack.name.isEmpty()) {
                    pack.name = fileName.replace(".yaml", "").replace(".yml", "");
                }

                if (pack.type == null || pack.type.isEmpty()) {
                    pack.type = PackType.LOCAL.getId();
                }

                pack.rebuildEmojiCodes();

                return Optional.of(pack);
            }
        } catch (IOException exception) {
            EmojiType.LOGGER.error("Failed to load pack: {}", packPath.getFileName(), exception);
        }

        return Optional.empty();
    }
}
