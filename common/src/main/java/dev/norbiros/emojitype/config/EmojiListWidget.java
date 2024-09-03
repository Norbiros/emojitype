package dev.norbiros.emojitype.config;

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

    public void addEntry(EmojiCode emoji) {
        this.addEntry(new EmojiListWidget.EmojiWidgetEntry(width, emoji, this));
    }

    public void addEntryAfter(EmojiCode emoji, EmojiWidgetEntry element) {
        var index = this.children().indexOf(element);
        this.children().add(index + 1, new EmojiListWidget.EmojiWidgetEntry(width, emoji, this));
    }

    public List<String> getCurrentCodes() {
        List<String> codes = new ArrayList<>();
        for (EmojiWidgetEntry entry : this.children()) {
            codes.add(entry.getEmojiCode().toString());
        }
        return codes;
    }

    @Override
    public int getRowWidth() {
        return width - 40;
    }

    @Override
    protected int getScrollbarX() {
        return width - 12;
    }

    public static class EmojiWidgetEntry extends ElementListWidget.Entry<EmojiWidgetEntry> {

        private final EmojiListWidget parent;
        List<ClickableWidget> elements = new ArrayList<>();

        public EmojiWidgetEntry(int entryWidth, EmojiCode emoji, EmojiListWidget parent) {
            this.parent = parent;
            MinecraftClient client = MinecraftClient.getInstance();

            var editBoxWidgetEmoji = new EditBoxWidget(client.textRenderer,
                    entryWidth / 2 - 200,
                    0,
                    110,
                    18,
                    Text.empty(),
                    Text.empty()
            );
            editBoxWidgetEmoji.setText(emoji.getEmoji());
            this.elements.add(editBoxWidgetEmoji);

            var editBoxWidgetCode = new EditBoxWidget(client.textRenderer,
                    entryWidth / 2 - 85,
                    0,
                    235,
                    18,
                    Text.empty(),
                    Text.empty()
            );
            var emojiCode = emoji.getCode();
            editBoxWidgetCode.setText(emojiCode.substring(1, emojiCode.length() - 1));
            this.elements.add(editBoxWidgetCode);

            this.elements.add(ButtonWidget
                    .builder(Text.literal("➕"), button -> this.parent.addEntryAfter(EmojiCode.EMPTY, this))
                    .dimensions(entryWidth / 2 + 157, 0, 20, 20)
                    .tooltip(Tooltip.of(Text.translatable("config.emojitype.add_entry_below_tooltip")))
                    .build()
            );
            this.elements.add(ButtonWidget
                    .builder(Text.literal("❌"), button -> this.parent.removeEntry(this))
                    .dimensions(entryWidth / 2 + 180, 0, 20, 20)
                    .tooltip(Tooltip.of(Text.translatable("config.emojitype.remove_entry_tooltip")))
                    .build()
            );
        }

        public EmojiCode getEmojiCode() {
            EditBoxWidget emoji = (EditBoxWidget) this.elements.get(0);
            EditBoxWidget key = (EditBoxWidget) this.elements.get(1);

            return new EmojiCode(":" + key.getText() + ":", emoji.getText());
        }

        public List<? extends Element> children() {
            return this.elements;
        }

        public List<? extends Selectable> selectableChildren() {
            return this.elements;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            for (ClickableWidget element : this.elements) {
                if (element instanceof ButtonWidget) {
                    element.setY(y - 1);
                } else {
                    element.setY(y);
                }
                element.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}