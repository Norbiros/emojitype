package dev.norbiros.emojitype.emoji;

import java.util.Optional;

public class EmojiCode {
    public static String DELIMITER = ";";
    private final String code;
    private final String emoji;

    public EmojiCode(String code, String emoji) {
        this.code = code;
        this.emoji = emoji;
    }

    public static Optional<EmojiCode> fromString(String str) {
        int index = str.indexOf(DELIMITER);
        if (index != -1) {
            String code = ":" + str.substring(0, index) + ":";
            String emoji = str.substring(index + DELIMITER.length());
            return Optional.of(new EmojiCode(code, emoji));
        } else {
            return Optional.empty();
        }
    }

    public String getCode() {
        return code;
    }

    public String getEmoji() {
        return emoji;
    }

    public boolean match(String string, int charIndex) {
        for (int i = 0; i < code.length(); i++) {
            int stringIndex = charIndex - i;
            int codeIndex = code.length() - 1 - i;
            if (stringIndex < 0 || codeIndex < 0) return false;
            if (stringIndex >= string.length()) return false;
            if (string.charAt(stringIndex) != code.charAt(codeIndex)) return false;
        }
        return true;
    }

    public String toString() {
        if (code.length() >= 3) {
            return code.substring(1, code.length() - 1) + DELIMITER + emoji;
        } else {
            return code + DELIMITER + emoji;
        }
    }
}