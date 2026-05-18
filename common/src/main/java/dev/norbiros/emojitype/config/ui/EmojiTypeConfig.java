package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.utils.DesktopUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class EmojiTypeConfig extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final @Nullable Screen parent;

    public EmojiTypeConfig(@Nullable Screen parent) {
        super(Component.translatable("config.emojitype.packs_title"));
        this.parent = parent;
        this.layout.setHeaderHeight(40);
        this.layout.setFooterHeight(40);
        EmojiType.reloadPacksFromDisk();
    }

    public static Screen createConfigScreen(Screen parent) {
        return new EmojiTypeConfig(parent);
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        LinearLayout footer = LinearLayout.horizontal().spacing(8);
        footer.addChild(Button.builder(Component.translatable("config.emojitype.back"), button -> this.onClose()).width(100).build());
        footer.addChild(Button.builder(Component.translatable("config.emojitype.open_folder"), button -> DesktopUtils.openInFileManager(PackLoader.PACKS_DIRECTORY)).width(120).build());
        footer.addChild(Button.builder(Component.translatable("config.emojitype.reload_packs"), button -> EmojiType.reloadPacksFromDisk()).width(120).build());
        this.layout.addToFooter(footer);

        this.layout.visitChildren(element -> {
            if (element instanceof AbstractWidget widget) {
                this.addRenderableWidget(widget);
            }
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
