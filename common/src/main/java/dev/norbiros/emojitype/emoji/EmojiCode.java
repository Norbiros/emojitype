package dev.norbiros.emojitype.emoji;

public record EmojiCode(String code, String emoji) {

    public String getCode() {
        return code;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getCodeWithColons() {
        return ":" + code + ":";
    }

    public String getChatSuggestion() {
        return ":" + code + ": " + emoji;
    }

    public boolean matchesAtPosition(String text, int position) {
        String codeWithColons = getCodeWithColons();
        for (int i = 0; i < codeWithColons.length(); i++) {
            int textIndex = position - i;
            int codeIndex = codeWithColons.length() - 1 - i;
            if (textIndex < 0 || codeIndex < 0) {
                return false;
            }
            if (textIndex >= text.length()) {
                return false;
            }
            if (text.charAt(textIndex) != codeWithColons.charAt(codeIndex)) {
                return false;
            }
        }
        return true;
    }
}
