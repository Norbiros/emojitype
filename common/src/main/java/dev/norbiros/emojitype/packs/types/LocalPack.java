package dev.norbiros.emojitype.packs.types;

import dev.norbiros.emojitype.packs.PackType;

public class LocalPack extends EmojiPack {
    public LocalPack() {
        super();
        this.type = PackType.LOCAL.getId();
    }

    public LocalPack(String name, String description) {
        super(name, description, PackType.LOCAL);
    }
}
