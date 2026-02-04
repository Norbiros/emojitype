package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.packs.types.EmojiPack;
import dev.norbiros.emojitype.packs.types.LocalPack;
import dev.norbiros.emojitype.utils.DesktopUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmojiTypeConfig extends Screen {
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    protected final Screen parent;
    private @Nullable PackListWidget body;

    public EmojiTypeConfig(Screen parent) {
        super(Text.translatable("config.emojitype.packs_title"));
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
        this.layout.addHeader(this.title, this.textRenderer);

        PackListWidget packListWidget = new PackListWidget(client, width, height - 30 - 30, 30, 46, this);

        Map<String, EmojiPack> availablePacks = EmojiType.getAvailablePacks();
        for (Map.Entry<String, EmojiPack> entry : availablePacks.entrySet()) {
            packListWidget.addPack(entry.getValue());
        }

        this.body = this.layout.addBody(packListWidget);

        this.initFooter();

        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
    }

    protected void initFooter() {
        DirectionalLayoutWidget footerLayout = this.layout.addFooter(DirectionalLayoutWidget.horizontal()).spacing(8);

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.back"), button -> this.close()).width(100).build());

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.open_folder"), button -> {
            DesktopUtils.openInFileManager(PackLoader.PACKS_DIRECTORY);
        }).width(120).build());

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.create_new_pack"), button -> this.createNewPack()).width(120).build());

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.reload_packs"), button -> {
            EmojiType.reloadPacksFromDisk();
            this.refreshScreen();
        }).width(100).build());
    }

    private void refreshScreen() {
        if (this.client != null) {
            this.client.setScreen(new EmojiTypeConfig(this.parent));
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void createNewPack() {
        if (this.client != null) {
            this.client.setScreen(PackPropertiesDialog.createNew(this, (fileName, name, description) -> {
                // Check if filename ends with .yaml or .yml
                if (!fileName.endsWith(".yaml") && !fileName.endsWith(".yml")) {
                    fileName += ".yaml";
                }
                
                // Check for duplicates and show toast if exists
                if (EmojiType.getAvailablePacks().containsKey(fileName)) {
                    if (this.client != null) {
                        this.client.getToastManager().add(new net.minecraft.client.toast.SystemToast(
                            SystemToast.Type.PACK_LOAD_FAILURE,
                            Text.translatable("config.emojitype.pack_duplicate_title"),
                            Text.translatable("config.emojitype.pack_duplicate_message", fileName)
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

                    if (this.client != null && reloadedPack != null) {
                        this.client.setScreen(new PackEditorScreen(new EmojiTypeConfig(this.parent), fileName, reloadedPack));
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
        if (this.client != null) {
            this.client.setScreen(new PackEditorScreen(this, packFileName, pack));
        }
    }
}
