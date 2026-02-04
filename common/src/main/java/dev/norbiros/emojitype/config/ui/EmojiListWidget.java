package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class EmojiListWidget extends ElementListWidget<EmojiListWidget.EmojiWidgetEntry> {

    public EmojiListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
        super(client, width, height, top, itemHeight);
    }

    public void addEntry(EmojiCode emojiCode) {
        this.addEntry(new EmojiWidgetEntry(width, emojiCode, this));
    }

    public void addEntryAfter(EmojiCode emojiCode, EmojiWidgetEntry element) {
        int index = this.children().indexOf(element);
        if (index >= 0 && index < this.children().size() - 1) {
            // Direct insertion if possible
            EmojiWidgetEntry newEntry = new EmojiWidgetEntry(width, emojiCode, this);
            this.children().add(index + 1, newEntry);
        } else if (index == this.children().size() - 1) {
            // Add at the end if it's the last element
            this.addEntry(emojiCode);
        }
    }

    public List<EmojiCode> getCurrentEmojiCodes() {
        List<EmojiCode> codes = new ArrayList<>();
        for (EmojiWidgetEntry entry : this.children()) {
            codes.add(entry.getEmojiCode());
        }
        return codes;
    }

    @Override
    public void removeEntry(EmojiWidgetEntry entry) {
        if (this.children().size() <= 1) {
            return;
        }
        super.removeEntry(entry);
    }

    public void reloadEntries(List<EmojiCode> emojiCodes) {
        this.clearEntries();

        if (emojiCodes.isEmpty()) {
            this.addEntry(new EmojiCode("", ""));
        } else {
            for (EmojiCode emojiCode : emojiCodes) {
                this.addEntry(emojiCode);
            }
        }
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

    public static class EmojiWidgetEntry extends ElementListWidget.Entry<EmojiWidgetEntry> {
        private static final int LEFT_PADDING = 6;
        private static final int GAP = 4;
        private static final int BUTTON_SIZE = 20;
        private static final int FIELD_HEIGHT = 18;
        private static final int BUTTONS_WIDTH = 44;

        private final EmojiListWidget parentWidget;
        private final List<ClickableWidget> elements = new ArrayList<>();
        private final EditBoxWidget emojiField;
        private final EditBoxWidget codeField;
        private final ButtonWidget addButton;
        private final ButtonWidget removeButton;

        public EmojiWidgetEntry(int entryWidth, EmojiCode emojiCode, EmojiListWidget parentWidget) {
            this.parentWidget = parentWidget;
            MinecraftClient client = MinecraftClient.getInstance();

            int available = Math.max(120, entryWidth - LEFT_PADDING - BUTTONS_WIDTH - GAP);
            int emojiWidth = Math.min(80, Math.max(40, available / 4));
            int codeWidth = Math.max(80, available - emojiWidth);

            this.emojiField = EditBoxWidget.builder()
                    .x(0)
                    .placeholder(Text.translatable("config.emojitype.emoji_placeholder"))
                    .build(client.textRenderer, emojiWidth, FIELD_HEIGHT, Text.empty());
            this.emojiField.setText(emojiCode.getEmoji());
            this.elements.add(emojiField);

            this.codeField = EditBoxWidget.builder()
                    .x(0)
                    .placeholder(Text.translatable("config.emojitype.code_placeholder"))
                    .build(client.textRenderer, codeWidth, FIELD_HEIGHT, Text.empty());
            this.codeField.setText(emojiCode.getCode());
            this.elements.add(codeField);

            this.addButton = ButtonWidget
                    .builder(Text.literal("+"), button -> this.parentWidget.addEntryAfter(new EmojiCode("", ""), this))
                    .dimensions(0, 0, BUTTON_SIZE, BUTTON_SIZE)
                    .tooltip(Tooltip.of(Text.translatable("config.emojitype.add_entry_below_tooltip")))
                    .build();
            this.elements.add(addButton);

            this.removeButton = ButtonWidget
                    .builder(Text.literal("-"), button -> this.parentWidget.removeEntry(this))
                    .dimensions(0, 0, BUTTON_SIZE, BUTTON_SIZE)
                    .tooltip(Tooltip.of(Text.translatable("config.emojitype.remove_entry_tooltip")))
                    .build();
            this.elements.add(removeButton);
        }

        public EmojiCode getEmojiCode() {
            return new EmojiCode(codeField.getText(), emojiField.getText());
        }

        public List<? extends Element> children() {
            return this.elements;
        }

        public List<? extends Selectable> selectableChildren() {
            return this.elements;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int rowX = this.getX();
            int rowY = this.getY();
            int rowWidth = this.parentWidget.getRowWidth();
            int rowHeight = this.parentWidget.getItemHeightValue();
            int index = this.parentWidget.children().indexOf(this);

            UIColors.drawRowBackground(context, rowX, rowY, rowWidth, rowHeight, index, hovered);

            int rightX = rowX + rowWidth;
            int removeX = rightX - BUTTON_SIZE;
            int addX = removeX - GAP - BUTTON_SIZE;

            int fieldsWidth = Math.max(0, addX - GAP - (rowX + LEFT_PADDING));
            int emojiWidth = Math.min(emojiField.getWidth(), Math.max(40, fieldsWidth / 4));
            int codeWidth = Math.max(40, fieldsWidth - emojiWidth - GAP);

            int emojiX = rowX + LEFT_PADDING;
            int codeX = emojiX + emojiWidth + GAP;

            emojiField.setX(emojiX);
            emojiField.setY(rowY + Math.max(0, (rowHeight - emojiField.getHeight()) / 2));
            if (emojiField.getWidth() != emojiWidth) {
                emojiField.setWidth(emojiWidth);
            }

            codeField.setX(codeX);
            codeField.setY(rowY + Math.max(0, (rowHeight - codeField.getHeight()) / 2));
            if (codeField.getWidth() != codeWidth) {
                codeField.setWidth(codeWidth);
            }

            addButton.setX(addX);
            addButton.setY(rowY + Math.max(0, (rowHeight - BUTTON_SIZE) / 2));
            removeButton.setX(removeX);
            removeButton.setY(rowY + Math.max(0, (rowHeight - BUTTON_SIZE) / 2));

            emojiField.render(context, mouseX, mouseY, tickDelta);
            codeField.render(context, mouseX, mouseY, tickDelta);
            addButton.render(context, mouseX, mouseY, tickDelta);
            removeButton.render(context, mouseX, mouseY, tickDelta);
        }
    }
}
