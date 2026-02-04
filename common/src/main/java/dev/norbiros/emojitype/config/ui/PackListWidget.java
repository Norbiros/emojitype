package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.packs.PackType;
import dev.norbiros.emojitype.packs.types.EmojiPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PackListWidget extends ElementListWidget<PackListWidget.PackEntry> {
    private final EmojiTypeConfig parentScreen;

    public PackListWidget(MinecraftClient client, int width, int height, int top, int itemHeight, EmojiTypeConfig parentScreen) {
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
        return this.itemHeight;
    }

    @Override
    protected int getScrollbarX() {
        return width - 12;
    }

    public static class PackEntry extends ElementListWidget.Entry<PackEntry> {
        private static final int BADGE_PADDING_X = 6;
        private static final int BADGE_PADDING_Y = 2;
        private static final int CHECKBOX_MARGIN = 4;
        private static final int TEXT_MARGIN = 6;
        private static final int BADGE_MARGIN = 8;

        private final PackListWidget parentWidget;
        private final EmojiPack pack;
        private final List<ClickableWidget> elements = new ArrayList<>();
        private final CheckboxWidget enabledCheckbox;
        private final ButtonWidget editButton;

        public PackEntry(int entryWidth, EmojiPack pack, PackListWidget parentWidget) {
            this.parentWidget = parentWidget;
            this.pack = pack;
            MinecraftClient client = MinecraftClient.getInstance();

            boolean enabled = EmojiType.isPackEnabled(pack.getFileName());
            this.enabledCheckbox = CheckboxWidget.builder(Text.empty(), client.textRenderer)
                    .pos(0, 0)
                    .checked(enabled)
                    .callback((checkbox, checked) -> {
                        if (checked) {
                            EmojiType.enablePack(pack.getFileName());
                        } else {
                            EmojiType.disablePack(pack.getFileName());
                        }
                    })
                    .build();
            this.elements.add(enabledCheckbox);

            this.editButton = ButtonWidget
                    .builder(Text.translatable("config.emojitype.edit_pack"), button -> {
                        parentWidget.parentScreen.openPackEditor(pack.getFileName(), pack);
                    })
                    .dimensions(0, 0, 80, 20)
                    .tooltip(Tooltip.of(Text.translatable("config.emojitype.edit_pack_tooltip")))
                    .build();
            this.elements.add(editButton);
        }

        public EmojiPack getPack() {
            return pack;
        }

        public List<? extends Element> children() {
            return this.elements;
        }

        public List<? extends Selectable> selectableChildren() {
            return this.elements;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            MinecraftClient client = MinecraftClient.getInstance();

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
            int lineHeight = client.textRenderer.fontHeight;
            int textY = rowY + 4;

            boolean enabled = enabledCheckbox.isChecked();
            int nameColor = UIColors.getEnabledTextColor(enabled);
            int descriptionColor = UIColors.getEnabledDescriptionColor(enabled);
            int statsColor = UIColors.getEnabledStatsColor(enabled);

            PackType packType = pack.getPackType();
            String typeLabel = packType.getTranslatedLabel().getString();
            int emojiCount = pack.getEmojis() != null ? pack.getEmojis().size() : 0;
            String countLabel = Text.translatable("config.emojitype.emoji_count", emojiCount).getString();

            String description = pack.getDescription() != null && !pack.getDescription().isEmpty()
                    ? pack.getDescription()
                    : Text.translatable("config.emojitype.no_description").getString();
            String statsLine = countLabel + "  |  " + pack.getFileName();

            int badgeTextWidth = client.textRenderer.getWidth(typeLabel);
            int badgeWidth = badgeTextWidth + BADGE_PADDING_X * 2;
            int badgeHeight = lineHeight + BADGE_PADDING_Y * 2;
            int badgeX = buttonX - badgeWidth - BADGE_MARGIN;
            int badgeY = rowY + Math.max(0, (rowHeight - badgeHeight) / 2);

            UIColors.drawBadge(context, badgeX, badgeY, badgeWidth, badgeHeight, packType);

            context.drawText(
                    client.textRenderer,
                    typeLabel,
                    badgeX + BADGE_PADDING_X,
                    badgeY + BADGE_PADDING_Y + 1,
                    UIColors.WHITE,
                    false
            );

            int textMaxWidth = Math.max(0, badgeX - textX - BADGE_MARGIN);
            String trimmedName = client.textRenderer.trimToWidth(pack.getName(), textMaxWidth);
            String trimmedDescription = client.textRenderer.trimToWidth(description, textMaxWidth);
            String trimmedStatsLine = client.textRenderer.trimToWidth(statsLine, textMaxWidth);

            context.drawText(
                    client.textRenderer,
                    trimmedName,
                    textX,
                    textY,
                    nameColor,
                    true
            );
            context.drawTextWithShadow(
                    client.textRenderer,
                    trimmedDescription,
                    textX,
                    textY + lineHeight + 2,
                    descriptionColor
            );
            context.drawTextWithShadow(
                    client.textRenderer,
                    trimmedStatsLine,
                    textX,
                    textY + (lineHeight * 2) + 4,
                    statsColor
            );

            enabledCheckbox.render(context, mouseX, mouseY, tickDelta);
            editButton.render(context, mouseX, mouseY, tickDelta);
        }
    }
}
