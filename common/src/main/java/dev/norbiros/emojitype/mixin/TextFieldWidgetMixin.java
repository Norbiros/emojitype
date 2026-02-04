package dev.norbiros.emojitype.mixin;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {
    @Shadow
    private String text;

    @Shadow
    private int selectionStart;

    @Shadow
    private int selectionEnd;

    @Shadow
    public abstract String getText();

    @Shadow
    protected abstract void onChanged(String newText);

    @Inject(method = "charTyped", at = @At("RETURN"))
    private void onCharTyped(CharInput input, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!callbackInfo.getReturnValue()) {
            return;
        }

        String result = getText();
        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            result = result.replace(emojiCode.getCodeWithColons(), emojiCode.getEmoji());
        }

        if (!Objects.equals(this.text, result)) {
            int lengthDifference = this.text.length() - result.length();
            int newCursorPosition = Math.max(Math.min(this.selectionEnd - lengthDifference + 1, result.length()), 0);
            this.selectionEnd = this.selectionStart = newCursorPosition;
        }

        this.text = result;
        this.onChanged(result);
    }
}
