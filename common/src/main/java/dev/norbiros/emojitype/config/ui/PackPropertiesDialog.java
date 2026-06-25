package dev.norbiros.emojitype.config.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class PackPropertiesDialog extends Screen {
    private static final int FIELD_WIDTH = 400;
    private static final int FIELD_HEIGHT = 18;
    private static final int VERTICAL_SPACING = 36;

    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parentScreen;
    private final boolean isEditMode;
    private final String initialFileName;
    private final String initialName;
    private final String initialDescription;
    private final PackPropertiesCallback callback;

    private @Nullable EditBox fileNameField;
    private @Nullable EditBox nameField;
    private @Nullable EditBox descriptionField;

    private Component fileNameLabelText;
    private Component nameLabelText;
    private Component descriptionLabelText;

    private int fileNameX;
    private int fileNameY;
    private int nameX;
    private int nameY;
    private int descriptionX;
    private int descriptionY;

    private PackPropertiesDialog(Screen parentScreen, boolean isEditMode, String initialFileName, String initialName, String initialDescription, PackPropertiesCallback callback) {
        super(Component.translatable(isEditMode ? "config.emojitype.edit_pack_properties" : "config.emojitype.create_new_pack"));
        this.parentScreen = parentScreen;
        this.isEditMode = isEditMode;
        this.initialFileName = initialFileName;
        this.initialName = initialName;
        this.initialDescription = initialDescription;
        this.callback = callback;
        this.layout.setHeaderHeight(70);
        this.layout.setFooterHeight(30);
    }

    public static PackPropertiesDialog createNew(Screen parentScreen, PackPropertiesCallback callback) {
        return new PackPropertiesDialog(
                parentScreen,
                false,
                Component.translatable("config.emojitype.default_pack_filename").getString(),
                Component.translatable("config.emojitype.default_pack_name").getString(),
                Component.translatable("config.emojitype.default_pack_description").getString(),
                callback
        );
    }

    public static PackPropertiesDialog edit(Screen parentScreen, String fileName, String name, String description, PackPropertiesCallback callback) {
        return new PackPropertiesDialog(parentScreen, true, fileName, name, description, callback);
    }

    @Override
    protected void init() {
        this.layout.removeChildren();
        int centerX = this.width / 2;
        int startY = 40;

        this.fileNameX = centerX - FIELD_WIDTH / 2;
        this.fileNameY = startY;
        this.fileNameLabelText = Component.translatable("config.emojitype.pack_filename");
        this.fileNameField = new EditBox(this.font, this.fileNameX, this.fileNameY, FIELD_WIDTH, FIELD_HEIGHT, this.fileNameLabelText);
        this.fileNameField.setValue(initialFileName);
        this.addRenderableWidget(this.fileNameField);

        this.nameX = centerX - FIELD_WIDTH / 2;
        this.nameY = startY + VERTICAL_SPACING;
        this.nameLabelText = Component.translatable("config.emojitype.pack_name");
        this.nameField = new EditBox(this.font, this.nameX, this.nameY, FIELD_WIDTH, FIELD_HEIGHT, this.nameLabelText);
        this.nameField.setValue(initialName);
        this.addRenderableWidget(this.nameField);

        this.descriptionX = centerX - FIELD_WIDTH / 2;
        this.descriptionY = startY + VERTICAL_SPACING * 2;
        this.descriptionLabelText = Component.translatable("config.emojitype.pack_description");
        this.descriptionField = new EditBox(this.font, this.descriptionX, this.descriptionY, FIELD_WIDTH, FIELD_HEIGHT, this.descriptionLabelText);
        this.descriptionField.setValue(initialDescription);
        this.addRenderableWidget(this.descriptionField);

        this.initFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.refreshWidgetPositions();

        if (this.nameField != null) {
            this.setInitialFocus(this.nameField);
        }
    }

    protected void initFooter() {
        LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal()).spacing(8);

        footerLayout.addChild(Button.builder(Component.translatable("config.emojitype.cancel"), button -> this.close()).width(100).build());

        Component confirmButtonText = Component.translatable(isEditMode ? "config.emojitype.save" : "config.emojitype.create");
        footerLayout.addChild(Button.builder(confirmButtonText, button -> this.confirm()).width(100).build());
    }

    protected void refreshWidgetPositions() {
        this.layout.arrangeElements();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        int titleX = (this.width - this.font.width(this.title)) / 2;
        context.text(this.font, this.title, titleX, 15, UIColors.WHITE, false);

        if (this.fileNameField != null) {
            context.text(this.font, this.fileNameLabelText, this.fileNameX, this.fileNameY - 10, UIColors.LABEL_GRAY, false);
        }
        if (this.nameField != null) {
            context.text(this.font, this.nameLabelText, this.nameX, this.nameY - 10, UIColors.LABEL_GRAY, false);
        }
        if (this.descriptionField != null) {
            context.text(this.font, this.descriptionLabelText, this.descriptionX, this.descriptionY - 10, UIColors.LABEL_GRAY, false);
        }
    }

    private void confirm() {
        if (this.fileNameField == null || this.nameField == null || this.descriptionField == null) {
            this.close();
            return;
        }

        String fileName = this.fileNameField.getValue().trim();
        String name = this.nameField.getValue().trim();
        String description = this.descriptionField.getValue().trim();

        if (fileName.isEmpty()) {
            fileName = initialFileName;
        }

        if (name.isEmpty()) {
            name = initialName;
        }

        if (!fileName.endsWith(".yaml") && !fileName.endsWith(".yml")) {
            fileName += ".yaml";
        }

        callback.onConfirm(fileName, name, description);
        // Note: Don't call close() here - the callback is responsible for navigation
    }

    @Override
    public void onClose() {
        this.close();
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parentScreen);
        }
    }

    /**
     * Callback interface for handling pack property confirmations.
     * Invoked when the user confirms the pack properties dialog.
     */
    @FunctionalInterface
    public interface PackPropertiesCallback {
        /**
         * Called when the user confirms the pack properties.
         *
         * @param fileName The sanitized filename for the pack (including .yaml extension)
         * @param name The display name of the pack
         * @param description The description of the pack
         */
        void onConfirm(String fileName, String name, String description);
    }
}
