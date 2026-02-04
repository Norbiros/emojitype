package dev.norbiros.emojitype.packs.types;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.packs.PackType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class EmojiPack {
    public String name;
    public String description;
    public String type;
    public Map<String, String> emojis;

    protected transient String fileName;
    protected transient List<EmojiCode> emojiCodes;
    protected transient boolean emojiCodesInitialized = false;

    public EmojiPack() {
        this.emojis = new LinkedHashMap<>();
        this.emojiCodes = new ArrayList<>();
    }

    public EmojiPack(String name, String description, PackType packType) {
        this.name = name;
        this.description = description;
        this.type = packType.getId();
        this.emojis = new LinkedHashMap<>();
        this.emojiCodes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PackType getPackType() {
        return PackType.fromId(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getEmojis() {
        return emojis;
    }

    public void setEmojis(Map<String, String> emojis) {
        this.emojis = emojis;
        rebuildEmojiCodes();
    }

    public List<EmojiCode> getEmojiCodes() {
        if (!emojiCodesInitialized) {
            rebuildEmojiCodes();
        }
        return emojiCodes;
    }

    public void setEmojiCodes(List<EmojiCode> emojiCodes) {
        this.emojiCodes = emojiCodes;
        rebuildEmojisMap();
    }

    public void rebuildEmojiCodes() {
        if (emojiCodes == null) {
            emojiCodes = new ArrayList<>();
        } else {
            emojiCodes.clear();
        }

        if (emojis != null) {
            for (Map.Entry<String, String> entry : emojis.entrySet()) {
                emojiCodes.add(new EmojiCode(entry.getKey(), entry.getValue()));
            }
        }
        
        emojiCodesInitialized = true;
    }

    public void rebuildEmojisMap() {
        if (emojis == null) {
            emojis = new LinkedHashMap<>();
        } else {
            emojis.clear();
        }

        if (emojiCodes != null) {
            for (EmojiCode emojiCode : emojiCodes) {
                emojis.put(emojiCode.code(), emojiCode.emoji());
            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean save() {
        if (fileName == null) {
            EmojiType.LOGGER.error("Cannot save pack: missing file name");
            return false;
        }

        if (type == null || type.isEmpty()) {
            type = PackType.LOCAL.getId();
        }

        try {
            PackLoader.ensurePacksDirectory();
            return PackLoader.savePack(this, fileName);
        } catch (Exception exception) {
            EmojiType.LOGGER.error("Failed to save pack: " + fileName, exception);
            return false;
        }
    }

    public String toYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        Yaml yaml = new Yaml(options);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("description", description);
        data.put("type", type);
        data.put("emojis", emojis);

        return yaml.dump(data);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EmojiPack: ").append(name).append("\n");
        builder.append("Type: ").append(getPackType()).append("\n");
        if (fileName != null) {
            builder.append("File: ").append(fileName).append("\n");
        }
        builder.append("Description: ").append(description).append("\n");
        builder.append("Emojis: ").append(emojis != null ? emojis.size() : 0).append(" entries\n");
        return builder.toString();
    }
}
