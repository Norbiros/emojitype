package dev.norbiros.emojitype.packs;

import net.minecraft.text.Text;

public enum PackType {
    BUNDLED("bundled"),
    LOCAL("local");

    private final String id;

    PackType(String id) {
        this.id = id;
    }

    public static PackType fromId(String id) {
        if (id == null || id.isEmpty()) {
            return LOCAL;
        }

        for (PackType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }

        return LOCAL;
    }

    public String getId() {
        return id;
    }

    public Text getTranslatedLabel() {
        return Text.translatable("config.emojitype.pack_type." + id);
    }

    @Override
    public String toString() {
        return id;
    }
}
