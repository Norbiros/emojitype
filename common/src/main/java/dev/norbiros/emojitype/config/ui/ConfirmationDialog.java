package dev.norbiros.emojitype.config.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.Component;

public class ConfirmationDialog extends Screen {
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parentScreen;
    private final Component message;
    private final Runnable onConfirm;
    private final Screen targetScreenOnConfirm;

    public ConfirmationDialog(Screen parentScreen, Component title, Component message, Runnable onConfirm) {
        this(parentScreen, title, message, onConfirm, null);
    }

    public ConfirmationDialog(Screen parentScreen, Component title, Component message, Runnable onConfirm, Screen targetScreenOnConfirm) {
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
        this.layout.removeChildren();
        this.initFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.refreshWidgetPositions();
    }

    protected void initFooter() {
        LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal()).spacing(8);

        footerLayout.addChild(Button.builder(Component.translatable("config.emojitype.cancel"), button -> this.close()).width(100).build());
        footerLayout.addChild(Button.builder(Component.translatable("config.emojitype.confirm"), button -> {
            onConfirm.run();
            if (this.minecraft != null) {
                if (targetScreenOnConfirm != null) {
                    this.minecraft.setScreenAndShow(targetScreenOnConfirm);
                } else {
                    this.close();
                }
            }
        }).width(100).build());
    }

    protected void refreshWidgetPositions() {
        this.layout.arrangeElements();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        int titleX = (this.width - this.font.width(this.title)) / 2;
        context.text(this.font, this.title, titleX, 15, UIColors.WHITE, false);

        int messageX = (this.width - this.font.width(this.message)) / 2;
        context.text(this.font, this.message, messageX, 35, UIColors.LIGHT_GRAY, false);
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
}
