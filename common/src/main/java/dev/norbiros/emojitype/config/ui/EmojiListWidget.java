package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class EmojiListWidget extends ContainerObjectSelectionList<EmojiListWidget.EmojiWidgetEntry> {

    public EmojiListWidget(Minecraft client, int width, int height, int top, int itemHeight) {
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
        return this.defaultEntryHeight;
    }

    @Override
    protected int scrollBarX() {
        return width - 12;
    }

    public static class EmojiWidgetEntry extends ContainerObjectSelectionList.Entry<EmojiWidgetEntry> {
        private static final int LEFT_PADDING = 6;
        private static final int GAP = 4;
        private static final int BUTTON_SIZE = 20;
        private static final int FIELD_HEIGHT = 18;
        private static final int BUTTONS_WIDTH = 44;

        private final EmojiListWidget parentWidget;
        private final List<AbstractWidget> elements = new ArrayList<>();
        private final EditBox emojiField;
        private final EditBox codeField;
        private final Button addButton;
        private final Button removeButton;

        public EmojiWidgetEntry(int entryWidth, EmojiCode emojiCode, EmojiListWidget parentWidget) {
            this.parentWidget = parentWidget;
            Minecraft client = Minecraft.getInstance();

            int available = Math.max(120, entryWidth - LEFT_PADDING - BUTTONS_WIDTH - GAP);
            int emojiWidth = Math.min(80, Math.max(40, available / 4));
            int codeWidth = Math.max(80, available - emojiWidth);

            this.emojiField = new EditBox(client.font, 0, 0, emojiWidth, FIELD_HEIGHT, Component.empty());
            this.emojiField.setHint(Component.translatable("config.emojitype.emoji_placeholder"));
            this.emojiField.setValue(emojiCode.getEmoji());
            this.elements.add(emojiField);

            this.codeField = new EditBox(client.font, 0, 0, codeWidth, FIELD_HEIGHT, Component.empty());
            this.codeField.setHint(Component.translatable("config.emojitype.code_placeholder"));
            this.codeField.setValue(emojiCode.getCode());
            this.elements.add(codeField);

            this.addButton = Button
                    .builder(Component.literal("+"), button -> this.parentWidget.addEntryAfter(new EmojiCode("", ""), this))
                    .bounds(0, 0, BUTTON_SIZE, BUTTON_SIZE)
                    .tooltip(Tooltip.create(Component.translatable("config.emojitype.add_entry_below_tooltip")))
                    .build();
            this.elements.add(addButton);

            this.removeButton = Button
                    .builder(Component.literal("-"), button -> this.parentWidget.removeEntry(this))
                    .bounds(0, 0, BUTTON_SIZE, BUTTON_SIZE)
                    .tooltip(Tooltip.create(Component.translatable("config.emojitype.remove_entry_tooltip")))
                    .build();
            this.elements.add(removeButton);
        }

        public EmojiCode getEmojiCode() {
            return new EmojiCode(codeField.getValue(), emojiField.getValue());
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
            int emojiWidth = Math.clamp(fieldsWidth / 4, 40, emojiField.getWidth());
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

            emojiField.extractRenderState(context, mouseX, mouseY, tickDelta);
            codeField.extractRenderState(context, mouseX, mouseY, tickDelta);
            addButton.extractRenderState(context, mouseX, mouseY, tickDelta);
            removeButton.extractRenderState(context, mouseX, mouseY, tickDelta);
        }
    }
}
