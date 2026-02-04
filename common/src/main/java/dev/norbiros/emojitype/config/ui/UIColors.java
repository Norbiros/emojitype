package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.packs.PackType;
import net.minecraft.client.gui.DrawContext;

public final class UIColors {

    public static final int WHITE = 0xFFFFFFFF;
    public static final int LIGHT_GRAY = 0xFFDDDDDD;
    public static final int GRAY = 0xFFC8C8C8;
    public static final int MEDIUM_GRAY = 0xFFB0B0B0;
    public static final int DARK_GRAY = 0xFF8A8A8A;
    public static final int DARKER_GRAY = 0xFF7A7A7A;
    public static final int LABEL_GRAY = 0xFFCCCCCC;

    public static final int ROW_EVEN_BACKGROUND = 0x14000000;
    public static final int ROW_ODD_BACKGROUND = 0x1C000000;
    public static final int ROW_HOVER_OVERLAY = 0x1EFFFFFF;

    public static final int PACK_ROW_EVEN_BACKGROUND = 0x16000000;
    public static final int PACK_ROW_ODD_BACKGROUND = 0x22000000;
    public static final int PACK_ROW_HOVER_OVERLAY = 0x22FFFFFF;

    public static final int BUNDLED_BADGE_BACKGROUND = 0xFF3C2F3F;
    public static final int BUNDLED_BADGE_BORDER = 0xFF6F4F7C;
    public static final int LOCAL_BADGE_BACKGROUND = 0xFF2F3C3F;
    public static final int LOCAL_BADGE_BORDER = 0xFF4F6F7C;

    private UIColors() {
    }

    public static int getEnabledTextColor(boolean enabled) {
        return enabled ? WHITE : MEDIUM_GRAY;
    }

    public static int getEnabledDescriptionColor(boolean enabled) {
        return enabled ? LIGHT_GRAY : DARK_GRAY;
    }

    public static int getEnabledStatsColor(boolean enabled) {
        return enabled ? GRAY : DARKER_GRAY;
    }

    public static int getBadgeBackground(PackType packType) {
        return switch (packType) {
            case BUNDLED -> BUNDLED_BADGE_BACKGROUND;
            case LOCAL -> LOCAL_BADGE_BACKGROUND;
        };
    }

    public static int getBadgeBorder(PackType packType) {
        return switch (packType) {
            case BUNDLED -> BUNDLED_BADGE_BORDER;
            case LOCAL -> LOCAL_BADGE_BORDER;
        };
    }

    public static int getRowBackground(int index) {
        return (index % 2 == 0) ? ROW_EVEN_BACKGROUND : ROW_ODD_BACKGROUND;
    }

    public static int getPackRowBackground(int index) {
        return (index % 2 == 0) ? PACK_ROW_EVEN_BACKGROUND : PACK_ROW_ODD_BACKGROUND;
    }

    public static void drawRowBackground(DrawContext context, int x, int y, int width, int height, int index, boolean hovered) {
        context.fill(x, y, x + width, y + height, getRowBackground(index));
        if (hovered) {
            context.fill(x, y, x + width, y + height, ROW_HOVER_OVERLAY);
        }
    }

    public static void drawPackRowBackground(DrawContext context, int x, int y, int width, int height, int index, boolean hovered) {
        context.fill(x, y, x + width, y + height, getPackRowBackground(index));
        if (hovered) {
            context.fill(x, y, x + width, y + height, PACK_ROW_HOVER_OVERLAY);
        }
    }

    public static void drawBadge(DrawContext context, int x, int y, int width, int height, PackType packType) {
        int backgroundColor = getBadgeBackground(packType);
        int borderColor = getBadgeBorder(packType);

        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, backgroundColor);
        context.fill(x + 1, y, x + width - 1, y + 1, borderColor);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, borderColor);
        context.fill(x, y + 1, x + 1, y + height - 1, borderColor);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, borderColor);
    }
}
