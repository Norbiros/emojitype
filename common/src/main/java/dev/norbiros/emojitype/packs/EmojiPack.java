package dev.norbiros.emojitype.packs;

import java.util.Map;

public class EmojiPack {
    public String name;
    public String description;
    public Map<String, String> emojis;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EmojiPack: ").append(name).append("\n");
        stringBuilder.append("Description: ").append(description).append("\n");
        stringBuilder.append("Emojis:\n");
        for (Map.Entry<String, String> entry : emojis.entrySet()) {
            stringBuilder.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }
}
