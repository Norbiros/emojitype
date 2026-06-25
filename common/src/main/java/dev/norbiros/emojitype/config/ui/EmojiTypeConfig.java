package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.packs.types.EmojiPack;
import dev.norbiros.emojitype.packs.types.LocalPack;
import dev.norbiros.emojitype.utils.DesktopUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmojiTypeConfig extends Screen {
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected final Screen parent;
    private @Nullable PackListWidget body;

    public EmojiTypeConfig(Screen parent) {
        super(Component.translatable("config.emojitype.packs_title"));
        this.parent = parent;
        this.layout.setHeaderHeight(30);
        this.layout.setFooterHeight(30);
        EmojiType.reloadPacksFromDisk();
    }

    public static Screen createConfigScreen(Screen parent) {
        return new EmojiTypeConfig(parent);
    }

    @Override
    protected void init() {
        this.layout.removeChildren();
        this.layout.addTitleHeader(this.title, this.font);

        PackListWidget packListWidget = new PackListWidget(this.minecraft, width, height - 30 - 30, 30, 46, this);

        Map<String, EmojiPack> availablePacks = EmojiType.getAvailablePacks();
        for (Map.Entry<String, EmojiPack> entry : availablePacks.entrySet()) {
            packListWidget.addPack(entry.getValue());
        }

        this.body = this.layout.addToContents(packListWidget);

        this.initFooter();

        this.layout.visitWidgets(this::addRenderableWidget);
        this.refreshWidgetPositions();
    }

    protected void refreshWidgetPositions() {
        this.layout.arrangeElements();
        if (this.body != null) {
            this.body.updateSize(this.width, this.layout);
        }
    }

    protected void initFooter() {
        LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal()).spacing(8);

        footerLayout.addChild(Button.builder(Component.translatable("config.emojitype.back"), button -> this.close()).width(100).build());

        footerLayout.addChild(Button.builder(Component.translatable("config.emojitype.open_folder"), button -> {
            DesktopUtils.openInFileManager(PackLoader.PACKS_DIRECTORY);
        }).width(120).build());

        footerLayout.addChild(Button.builder(Component.translatable("config.emojitype.create_new_pack"), button -> this.createNewPack()).width(120).build());

        footerLayout.addChild(Button.builder(Component.translatable("config.emojitype.reload_packs"), button -> {
            EmojiType.reloadPacksFromDisk();
            this.refreshScreen();
        }).width(100).build());
    }

    private void refreshScreen() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(new EmojiTypeConfig(this.parent));
        }
    }

    @Override
    public void onClose() {
        this.close();
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    private void createNewPack() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(PackPropertiesDialog.createNew(this, (fileName, name, description) -> {
                // Check if filename ends with .yaml or .yml
                if (!fileName.endsWith(".yaml") && !fileName.endsWith(".yml")) {
                    fileName += ".yaml";
                }
                
                // Check for duplicates and show toast if exists
                if (EmojiType.getAvailablePacks().containsKey(fileName)) {
                    if (this.minecraft != null) {
                        this.minecraft.gui.toastManager().addToast(new net.minecraft.client.gui.components.toasts.SystemToast(
                            SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                            Component.translatable("config.emojitype.pack_duplicate_title"),
                            Component.translatable("config.emojitype.pack_duplicate_message", fileName)
                        ));
                    }
                    return;
                }

                LocalPack newPack = new LocalPack(name, description);

                List<EmojiCode> emojiCodes = new ArrayList<>();
                emojiCodes.add(new EmojiCode("", ""));
                newPack.setEmojiCodes(emojiCodes);

                newPack.setFileName(fileName);

                if (newPack.save()) {
                    EmojiType.LOGGER.info("Created new pack: {}", fileName);

                    EmojiType.reloadPacksFromDisk();
                    EmojiType.enablePack(fileName);

                    EmojiPack reloadedPack = EmojiType.getAvailablePacks().get(fileName);

                    if (this.minecraft != null && reloadedPack != null) {
                        this.minecraft.setScreenAndShow(new PackEditorScreen(new EmojiTypeConfig(this.parent), fileName, reloadedPack));
                    } else {
                        this.refreshScreen();
                    }
                } else {
                    EmojiType.LOGGER.error("Failed to create new pack");
                    this.refreshScreen();
                }
            }));
        }
    }

    public void openPackEditor(String packFileName, EmojiPack pack) {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(new PackEditorScreen(this, packFileName, pack));
        }
    }
}
