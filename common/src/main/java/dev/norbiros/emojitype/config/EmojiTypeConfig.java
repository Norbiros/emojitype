package dev.norbiros.emojitype.config;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class EmojiTypeConfig extends Screen {
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    protected final Screen parent;
    public boolean shouldSaveConfig = true;
    private @Nullable EmojiListWidget body;

    public EmojiTypeConfig(Screen parent) {
        super(Text.translatable("config.emojitype.title"));
        this.parent = parent;
        this.layout.setHeaderHeight(30);
        this.layout.setFooterHeight(30);
    }

    public static Screen createConfigScreen(Screen parent) {
        ConfigUtil.deserialise();
        return new EmojiTypeConfig(parent);
    }

    @Override
    protected void init() {
        this.layout.addHeader(this.title, this.textRenderer);

        var emojiListWidget = new EmojiListWidget(client, width, height - 30 - 30, 30, 20);
        for (EmojiCode emoji : EmojiType.emojiCodes) {
            emojiListWidget.addEntry(emoji);
        }
        this.body = this.layout.addBody(emojiListWidget);

        this.initFooter();

        this.layout.forEachChild(this::addDrawableChild);
        this.initTabNavigation();
    }


    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
    }

    protected void initFooter() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal()).spacing(8);

        directionalLayoutWidget.add(ButtonWidget.builder(Text.translatable("config.emojitype.cancel"), (button) -> {
            this.shouldSaveConfig = false;
            this.close();
        }).width(100).build());

        directionalLayoutWidget.add(ButtonWidget.builder(Text.translatable("config.emojitype.reset_all"), (button) -> {
            if (this.body == null) {
                return;
            }
            this.body.children().clear();
            for (EmojiCode emoji : EmojiType.DEFAULT_EMOJI_CODES) {
                this.body.addEntry(emoji);
            }
        }).width(100).build());

        directionalLayoutWidget.add(ButtonWidget.builder(Text.translatable("config.emojitype.save_and_quit"), (button) -> this.close()).width(100).build());
    }

    @Override
    public void close() {
        if (this.body != null && this.shouldSaveConfig) {
            var codes = this.body.getCurrentCodes();
            ConfigUtil.emojiCodeStrings.clear();
            ConfigUtil.emojiCodeStrings.addAll(codes);
            ConfigUtil.serialise();
        }

        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
