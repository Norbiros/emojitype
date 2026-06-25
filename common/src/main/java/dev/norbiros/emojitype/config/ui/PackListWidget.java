package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.packs.PackType;
import dev.norbiros.emojitype.packs.types.EmojiPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PackListWidget extends ContainerObjectSelectionList<PackListWidget.PackEntry> {
    private final EmojiTypeConfig parentScreen;

    public PackListWidget(Minecraft client, int width, int height, int top, int itemHeight, EmojiTypeConfig parentScreen) {
        super(client, width, height, top, itemHeight);
        this.parentScreen = parentScreen;
    }

    public void addPack(EmojiPack pack) {
        this.addEntry(new PackEntry(width, pack, this));
    }

    @Override
    public int getRowWidth() {
        return width - 40;
    }

    public int getItemHeightValue() {
        return this.defaultEntryHeight;
    }

    @Override
    protected int scrollBarX() {
        return width - 12;
    }

    public static class PackEntry extends ContainerObjectSelectionList.Entry<PackEntry> {
        private static final int BADGE_PADDING_X = 6;
        private static final int BADGE_PADDING_Y = 2;
        private static final int CHECKBOX_MARGIN = 4;
        private static final int TEXT_MARGIN = 6;
        private static final int BADGE_MARGIN = 8;

        private final PackListWidget parentWidget;
        private final EmojiPack pack;
        private final List<AbstractWidget> elements = new ArrayList<>();
        private final Checkbox enabledCheckbox;
        private final Button editButton;

        public PackEntry(int entryWidth, EmojiPack pack, PackListWidget parentWidget) {
            this.parentWidget = parentWidget;
            this.pack = pack;
            Minecraft client = Minecraft.getInstance();

            boolean enabled = EmojiType.isPackEnabled(pack.getFileName());
            this.enabledCheckbox = Checkbox.builder(Component.empty(), client.font)
                    .pos(0, 0)
                    .selected(enabled)
                    .onValueChange((checkbox, checked) -> {
                        if (checked) {
                            EmojiType.enablePack(pack.getFileName());
                        } else {
                            EmojiType.disablePack(pack.getFileName());
                        }
                    })
                    .build();
            this.elements.add(enabledCheckbox);

            this.editButton = Button
                    .builder(Component.translatable("config.emojitype.edit_pack"), button -> {
                        parentWidget.parentScreen.openPackEditor(pack.getFileName(), pack);
                    })
                    .bounds(0, 0, 80, 20)
                    .tooltip(Tooltip.create(Component.translatable("config.emojitype.edit_pack_tooltip")))
                    .build();
            this.elements.add(editButton);
        }

        public EmojiPack getPack() {
            return pack;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.elements;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.elements;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Minecraft client = Minecraft.getInstance();

            int rowX = this.getX();
            int rowY = this.getY();
            int rowWidth = this.parentWidget.getRowWidth();
            int rowHeight = this.parentWidget.getItemHeightValue();
            int index = this.parentWidget.children().indexOf(this);

            UIColors.drawPackRowBackground(context, rowX, rowY, rowWidth, rowHeight, index, hovered);

            int checkboxWidth = enabledCheckbox.getWidth();
            int checkboxHeight = enabledCheckbox.getHeight();
            int buttonWidth = editButton.getWidth();
            int buttonHeight = editButton.getHeight();

            int checkboxX = rowX + CHECKBOX_MARGIN;
            int checkboxY = rowY + Math.max(0, (rowHeight - checkboxHeight) / 2);
            int buttonX = rowX + rowWidth - buttonWidth;
            int buttonY = rowY + Math.max(0, (rowHeight - buttonHeight) / 2);

            enabledCheckbox.setX(checkboxX);
            enabledCheckbox.setY(checkboxY);
            editButton.setX(buttonX);
            editButton.setY(buttonY);

            int textX = checkboxX + checkboxWidth + TEXT_MARGIN;
            int lineHeight = client.font.lineHeight;
            int textY = rowY + 4;

            boolean enabled = enabledCheckbox.selected();
            int nameColor = UIColors.getEnabledTextColor(enabled);
            int descriptionColor = UIColors.getEnabledDescriptionColor(enabled);
            int statsColor = UIColors.getEnabledStatsColor(enabled);

            PackType packType = pack.getPackType();
            String typeLabel = packType.getTranslatedLabel().getString();
            int emojiCount = pack.getEmojis() != null ? pack.getEmojis().size() : 0;
            String countLabel = Component.translatable("config.emojitype.emoji_count", emojiCount).getString();

            String description = pack.getDescription() != null && !pack.getDescription().isEmpty()
                    ? pack.getDescription()
                    : Component.translatable("config.emojitype.no_description").getString();
            String statsLine = countLabel + "  |  " + pack.getFileName();

            int badgeTextWidth = client.font.width(typeLabel);
            int badgeWidth = badgeTextWidth + BADGE_PADDING_X * 2;
            int badgeHeight = lineHeight + BADGE_PADDING_Y * 2;
            int badgeX = buttonX - badgeWidth - BADGE_MARGIN;
            int badgeY = rowY + Math.max(0, (rowHeight - badgeHeight) / 2);

            UIColors.drawBadge(context, badgeX, badgeY, badgeWidth, badgeHeight, packType);

            context.text(
                    client.font,
                    typeLabel,
                    badgeX + BADGE_PADDING_X,
                    badgeY + BADGE_PADDING_Y + 1,
                    UIColors.WHITE,
                    false
            );

            int textMaxWidth = Math.max(0, badgeX - textX - BADGE_MARGIN);
            String trimmedName = client.font.plainSubstrByWidth(pack.getName(), textMaxWidth);
            String trimmedDescription = client.font.plainSubstrByWidth(description, textMaxWidth);
            String trimmedStatsLine = client.font.plainSubstrByWidth(statsLine, textMaxWidth);

            context.text(
                    client.font,
                    trimmedName,
                    textX,
                    textY,
                    nameColor,
                    true
            );
            context.text(
                    client.font,
                    trimmedDescription,
                    textX,
                    textY + lineHeight + 2,
                    descriptionColor
            );
            context.text(
                    client.font,
                    trimmedStatsLine,
                    textX,
                    textY + (lineHeight * 2) + 4,
                    statsColor
            );

            enabledCheckbox.extractRenderState(context, mouseX, mouseY, tickDelta);
            editButton.extractRenderState(context, mouseX, mouseY, tickDelta);
        }
    }
}
