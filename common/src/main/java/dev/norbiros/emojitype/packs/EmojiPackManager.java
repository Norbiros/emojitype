package dev.norbiros.emojitype.packs;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.config.ConfigUtil;
import dev.norbiros.emojitype.emoji.EmojiCode;
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmojiPackManager {
    public static List<EmojiCode> getAllEmojis() {
        List<EmojiCode> emojis = new ArrayList<>();
        for (EmojiPack pack : getEmojiPacks()) {
            for (Map.Entry<String, String> entry : pack.emojis.entrySet()) {
                emojis.add(new EmojiCode(":" + entry.getKey() + ":", entry.getValue()));
            }
        }
        return emojis;
    }

    public static List<EmojiPack> getEmojiPacks() {
        Yaml yaml = new Yaml();
        return readEmojiPackFiles().values().stream()
                .map(yamlStr -> (EmojiPack) yaml.loadAs(yamlStr, EmojiPack.class))
                .collect(Collectors.toList());
    }

    public static Map<String, String> readEmojiPackFiles() {
        Map<String, String> emojiPacks = new HashMap<>();

        try {
            CodeSource codeSource = EmojiPackManager.class.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                throw new IllegalStateException("CodeSource is null");
            }

            URL jarUrl = codeSource.getLocation();
            Path jarPath = Paths.get(jarUrl.toURI());

            try (FileSystem fs = FileSystems.newFileSystem(jarPath, (ClassLoader) null)) {
                Path packDir = fs.getPath("assets", EmojiType.MOD_ID, "emoji_packs");

                if (Files.exists(packDir)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(packDir)) {
                        for (Path path : stream) {
                            if (Files.isRegularFile(path)) {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                emojiPacks.put(path.getFileName().toString(), content);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load emoji packs: " + e.getMessage());
            e.printStackTrace();
        }

        return emojiPacks;
    }
}
