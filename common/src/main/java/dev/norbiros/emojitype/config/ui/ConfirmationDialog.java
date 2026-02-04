package dev.norbiros.emojitype.config.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;

public class ConfirmationDialog extends Screen {
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final Screen parentScreen;
    private final Text message;
    private final Runnable onConfirm;
    private final Screen targetScreenOnConfirm;

    public ConfirmationDialog(Screen parentScreen, Text title, Text message, Runnable onConfirm) {
        this(parentScreen, title, message, onConfirm, null);
    }

    public ConfirmationDialog(Screen parentScreen, Text title, Text message, Runnable onConfirm, Screen targetScreenOnConfirm) {
        super(title);
        this.parentScreen = parentScreen;
        this.message = message;
        this.onConfirm = onConfirm;
        this.targetScreenOnConfirm = targetScreenOnConfirm;
        this.layout.setHeaderHeight(60);
        this.layout.setFooterHeight(30);
    }

    @Override
    protected void init() {
        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    protected void initFooter() {
        DirectionalLayoutWidget footerLayout = this.layout.addFooter(DirectionalLayoutWidget.horizontal()).spacing(8);

        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.cancel"), button -> this.close()).width(100).build());
        footerLayout.add(ButtonWidget.builder(Text.translatable("config.emojitype.confirm"), button -> {
            onConfirm.run();
            if (this.client != null) {
                if (targetScreenOnConfirm != null) {
                    this.client.setScreen(targetScreenOnConfirm);
                } else {
                    this.close();
                }
            }
        }).width(100).build());
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int titleX = (this.width - this.textRenderer.getWidth(this.title)) / 2;
        context.drawText(this.textRenderer, this.title, titleX, 15, UIColors.WHITE, false);

        int messageX = (this.width - this.textRenderer.getWidth(this.message)) / 2;
        context.drawText(this.textRenderer, this.message, messageX, 35, UIColors.LIGHT_GRAY, false);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parentScreen);
        }
    }
}
