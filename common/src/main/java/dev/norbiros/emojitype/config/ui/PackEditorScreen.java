package dev.norbiros.emojitype.config.ui;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import dev.norbiros.emojitype.packs.PackLoader;
import dev.norbiros.emojitype.packs.PackType;
import dev.norbiros.emojitype.packs.types.BundledPack;
import dev.norbiros.emojitype.packs.types.EmojiPack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class PackEditorScreen extends Screen {
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 30;

    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    protected final Screen parentScreen;
    private final EmojiPack pack;
    private String packFileName;
    private @Nullable EmojiListWidget body;

    public PackEditorScreen(Screen parentScreen, String packFileName, EmojiPack pack) {
        super(Text.translatable("config.emojitype.edit_pack_title", pack.getName() != null ? pack.getName() : packFileName));
        this.parentScreen = parentScreen;
        this.packFileName = packFileName;
        this.pack = pack;
        this.layout.setHeaderHeight(HEADER_HEIGHT);
        this.layout.setFooterHeight(FOOTER_HEIGHT);
    }

    @Override
    protected void init() {
        this.layout.addHeader(this.title, this.textRenderer);

        EmojiListWidget emojiListWidget = new EmojiListWidget(client, width, height - HEADER_HEIGHT - FOOTER_HEIGHT, HEADER_HEIGHT, 24);

        List<EmojiCode> emojiCodes = pack.getEmojiCodes();
        if (emojiCodes != null && !emojiCodes.isEmpty()) {
            for (EmojiCode emojiCode : emojiCodes) {
                emojiListWidget.addEntry(emojiCode);
            }
        } else {
            emojiListWidget.addEntry(new EmojiCode("", ""));
        }

        this.body = this.layout.addBody(emojiListWidget);

        this.initFooter();

        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        String typeLabel = pack.getPackType().getTranslatedLabel().getString();
        int emojiCount = this.body != null ? this.body.children().size() : (pack.getEmojis() != null ? pack.getEmojis().size() : 0);
        String metadata = typeLabel + "  |  " + Text.translatable("config.emojitype.emoji_count", emojiCount).getString();
        int metadataWidth = this.textRenderer.getWidth(metadata);
        context.drawText(this.textRenderer, metadata, this.width - 12 - metadataWidth, 8, UIColors.LIGHT_GRAY, false);
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
    }

    protected void initFooter() {
        DirectionalLayoutWidget footerLayout = this.layout.addFooter(DirectionalLayoutWidget.horizontal()).spacing(8);

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.cancel"), button -> this.close()).width(100).build());

        if (pack.getPackType() == PackType.LOCAL) {
            footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.edit_properties"), button -> this.editProperties()).width(120).build());
        }

        if (pack.getPackType() == PackType.BUNDLED) {
            footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.restore_default"), button -> this.restoreToDefault()).width(130).build());
        }

        if (pack.getPackType() == PackType.LOCAL) {
            footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.delete_pack"), button -> this.deletePack()).width(100).build());
        }

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.save"), button -> this.saveChanges()).width(100).build());
    }

    private void saveChanges() {
        if (this.body == null) {
            this.close();
            return;
        }

        pack.setEmojiCodes(this.body.getCurrentEmojiCodes());

        if (pack.save()) {
            EmojiType.reloadPacksFromDisk();
            this.close();
        } else {
            EmojiType.LOGGER.error("Failed to save pack: {}", packFileName);
        }
    }

    private void editProperties() {
        if (pack.getPackType() != PackType.LOCAL) {
            EmojiType.LOGGER.error("Cannot edit properties of non-local pack");
            return;
        }

        if (this.client != null) {
            String currentName = pack.getName() != null ? pack.getName() : "";
            String currentDescription = pack.getDescription() != null ? pack.getDescription() : "";

            this.client.setScreen(PackPropertiesDialog.edit(this, packFileName, currentName, currentDescription,
                    (newFileName, newName, newDescription) -> {
                        pack.setName(newName);
                        pack.setDescription(newDescription);

                        if (!newFileName.equals(packFileName)) {
                            pack.setFileName(newFileName);

                            if (pack.save()) {
                                boolean deletedOldFile = PackLoader.deletePack(packFileName);
                                if (!deletedOldFile) {
                                    EmojiType.LOGGER.warn("Failed to delete old emoji pack file '{}'", packFileName);
                                    if (this.client != null && this.client.player != null) {
                                        this.client.player.sendMessage(
                                            Text.literal("Warning: Old emoji pack file '" + packFileName + "' could not be deleted. It may still exist on disk."),
                                            false
                                        );
                                    }
                                }
                                
                                boolean wasEnabled = EmojiType.isPackEnabled(packFileName);
                                try {
                                    EmojiType.reloadPacksFromDisk();
                                    
                                    if (wasEnabled) {
                                        EmojiType.disablePack(packFileName);
                                        EmojiType.enablePack(newFileName);
                                    }

                                    this.packFileName = newFileName;

                                    EmojiPack updatedPack = EmojiType.getAvailablePacks().get(newFileName);
                                    if (updatedPack != null && this.client != null) {
                                        Screen packListScreen = getPackListScreen();
                                        this.client.setScreen(new PackEditorScreen(packListScreen, newFileName, updatedPack));
                                    }
                                } catch (Exception e) {
                                    EmojiType.LOGGER.error("Failed to reload or re-enable emoji pack after renaming file from {} to {}", packFileName, newFileName, e);
                                    if (this.client != null && this.client.player != null) {
                                        this.client.player.sendMessage(
                                            Text.literal("Failed to reload emoji packs after renaming. Check logs for details."),
                                            false
                                        );
                                    }
                                }
                            }
                        } else {
                            // Just updating name/description, same filename
                            if (pack.save()) {
                                try {
                                    EmojiType.reloadPacksFromDisk();
                                    if (this.client != null) {
                                        EmojiPack updatedPack = EmojiType.getAvailablePacks().get(packFileName);
                                        if (updatedPack != null) {
                                            Screen packListScreen = getPackListScreen();
                                            this.client.setScreen(new PackEditorScreen(packListScreen, packFileName, updatedPack));
                                        }
                                    }
                                } catch (Exception e) {
                                    EmojiType.LOGGER.error("Failed to reload emoji packs after saving pack {}", packFileName, e);
                                    if (this.client != null && this.client.player != null) {
                                        this.client.player.sendMessage(
                                            Text.literal("Failed to reload emoji packs after saving. Check logs for details."),
                                            false
                                        );
                                    }
                                }
                            }
                        }
                    }
            ));
        }
    }

    private Screen getPackListScreen() {
        if (this.parentScreen instanceof EmojiTypeConfig packListScreen) {
            return packListScreen;
        }
        return this.parentScreen;
    }

    private void restoreToDefault() {
        if (pack.getPackType() != PackType.BUNDLED) {
            EmojiType.LOGGER.error("Cannot restore non-bundled pack to default");
            return;
        }

        if (pack instanceof BundledPack bundledPack) {
            if (bundledPack.restoreToDefault()) {
                EmojiType.LOGGER.info("Restored pack to default: {}", packFileName);

                Optional<EmojiPack> reloadedOptional = PackLoader.loadPack(packFileName);
                if (reloadedOptional.isPresent()) {
                    EmojiPack reloaded = reloadedOptional.get();

                    pack.setEmojis(new LinkedHashMap<>(reloaded.getEmojis()));
                    pack.setName(reloaded.getName());
                    pack.setDescription(reloaded.getDescription());

                    if (this.body != null) {
                        this.body.reloadEntries(pack.getEmojiCodes());
                    }

                    EmojiType.reloadPacksFromDisk();
                }
            } else {
                EmojiType.LOGGER.error("Failed to restore pack to default: {}", packFileName);
            }
        }
    }

    private void deletePack() {
        if (pack.getPackType() != PackType.LOCAL) {
            EmojiType.LOGGER.error(String.format("Cannot delete non-local pack: %s", packFileName));
            return;
        }

        if (this.client != null) {
            String packDisplayName = pack.getName() != null && !pack.getName().isEmpty() ? pack.getName() : packFileName;

            this.client.setScreen(new ConfirmationDialog(
                    this,
                    Text.translatable("config.emojitype.delete_pack_title"),
                    Text.translatable("config.emojitype.delete_pack_confirm", packDisplayName),
                    () -> {
                        EmojiType.disablePack(packFileName);

                        if (PackLoader.deletePack(packFileName)) {
                            EmojiType.LOGGER.info("Pack deleted: {}", packFileName);
                            EmojiType.reloadPacksFromDisk();
                        } else {
                            EmojiType.LOGGER.error("Failed to delete pack: {}", packFileName);
                        }
                    },
                    new EmojiTypeConfig(this.parentScreen instanceof EmojiTypeConfig config ? config.parent : this.parentScreen)
            ));
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(getPackListScreen());
        }
    }
}
