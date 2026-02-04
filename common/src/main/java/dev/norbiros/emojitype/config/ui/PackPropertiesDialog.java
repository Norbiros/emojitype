package dev.norbiros.emojitype.config.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PackPropertiesDialog extends Screen {
    private static final int FIELD_WIDTH = 400;
    private static final int FIELD_HEIGHT = 18;
    private static final int VERTICAL_SPACING = 36;

    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final Screen parentScreen;
    private final boolean isEditMode;
    private final String initialFileName;
    private final String initialName;
    private final String initialDescription;
    private final PackPropertiesCallback callback;

    private @Nullable EditBoxWidget fileNameField;
    private @Nullable EditBoxWidget nameField;
    private @Nullable EditBoxWidget descriptionField;

    private Text fileNameLabelText;
    private Text nameLabelText;
    private Text descriptionLabelText;

    private int fileNameX;
    private int fileNameY;
    private int nameX;
    private int nameY;
    private int descriptionX;
    private int descriptionY;

    private PackPropertiesDialog(Screen parentScreen, boolean isEditMode, String initialFileName, String initialName, String initialDescription, PackPropertiesCallback callback) {
        super(Text.translatable(isEditMode ? "config.emojitype.edit_pack_properties" : "config.emojitype.create_new_pack"));
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
                Text.translatable("config.emojitype.default_pack_filename").getString(),
                Text.translatable("config.emojitype.default_pack_name").getString(),
                Text.translatable("config.emojitype.default_pack_description").getString(),
                callback
        );
    }

    public static PackPropertiesDialog edit(Screen parentScreen, String fileName, String name, String description, PackPropertiesCallback callback) {
        return new PackPropertiesDialog(parentScreen, true, fileName, name, description, callback);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;

        this.fileNameX = centerX - FIELD_WIDTH / 2;
        this.fileNameY = startY;
        this.fileNameLabelText = Text.translatable("config.emojitype.pack_filename");
        this.fileNameField = EditBoxWidget.builder()
                .x(this.fileNameX)
                .y(this.fileNameY)
                .build(this.textRenderer, FIELD_WIDTH, FIELD_HEIGHT, this.fileNameLabelText);
        this.fileNameField.setText(initialFileName);
        this.addDrawableChild(this.fileNameField);

        this.nameX = centerX - FIELD_WIDTH / 2;
        this.nameY = startY + VERTICAL_SPACING;
        this.nameLabelText = Text.translatable("config.emojitype.pack_name");
        this.nameField = EditBoxWidget.builder()
                .x(this.nameX)
                .y(this.nameY)
                .build(this.textRenderer, FIELD_WIDTH, FIELD_HEIGHT, this.nameLabelText);
        this.nameField.setText(initialName);
        this.addDrawableChild(this.nameField);

        this.descriptionX = centerX - FIELD_WIDTH / 2;
        this.descriptionY = startY + VERTICAL_SPACING * 2;
        this.descriptionLabelText = Text.translatable("config.emojitype.pack_description");
        this.descriptionField = EditBoxWidget.builder()
                .x(this.descriptionX)
                .y(this.descriptionY)
                .build(this.textRenderer, FIELD_WIDTH, FIELD_HEIGHT, this.descriptionLabelText);
        this.descriptionField.setText(initialDescription);
        this.addDrawableChild(this.descriptionField);

        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();

        if (this.nameField != null) {
            this.setInitialFocus(this.nameField);
        }
    }

    protected void initFooter() {
        DirectionalLayoutWidget footerLayout = this.layout.addFooter(DirectionalLayoutWidget.horizontal()).spacing(8);

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.cancel"), button -> this.close()).width(100).build());

        Text confirmButtonText = Text.translatable(isEditMode ? "config.emojitype.save" : "config.emojitype.create");
        footerLayout.add(ButtonWidget.builder(confirmButtonText, button -> this.confirm()).width(100).build());
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int titleX = (this.width - this.textRenderer.getWidth(this.title)) / 2;
        context.drawText(this.textRenderer, this.title, titleX, 15, UIColors.WHITE, false);

        if (this.fileNameField != null) {
            context.drawText(this.textRenderer, this.fileNameLabelText, this.fileNameX, this.fileNameY - 10, UIColors.LABEL_GRAY, false);
        }
        if (this.nameField != null) {
            context.drawText(this.textRenderer, this.nameLabelText, this.nameX, this.nameY - 10, UIColors.LABEL_GRAY, false);
        }
        if (this.descriptionField != null) {
            context.drawText(this.textRenderer, this.descriptionLabelText, this.descriptionX, this.descriptionY - 10, UIColors.LABEL_GRAY, false);
        }
    }

    private void confirm() {
        if (this.fileNameField == null || this.nameField == null || this.descriptionField == null) {
            this.close();
            return;
        }

        String fileName = this.fileNameField.getText().trim();
        String name = this.nameField.getText().trim();
        String description = this.descriptionField.getText().trim();

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
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parentScreen);
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
