package dev.norbiros.emojitype.mixin;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(EditBox.class)
public abstract class TextFieldWidgetMixin {
    @Shadow
    private String value;

    @Shadow
    private int cursorPos;

    @Shadow
    private int highlightPos;

    @Shadow
    public abstract String getValue();

    @Shadow
    private void onValueChange(String newText) {
    }

    @Inject(method = "charTyped", at = @At("RETURN"))
    private void onCharTyped(CharacterEvent input, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!callbackInfo.getReturnValue()) {
            return;
        }

        String result = getValue();
        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            result = result.replace(emojiCode.getCodeWithColons(), emojiCode.getEmoji());
        }

        if (!Objects.equals(this.value, result)) {
            int lengthDifference = this.value.length() - result.length();
            int newCursorPosition = Math.max(Math.min(this.highlightPos - lengthDifference + 1, result.length()), 0);
            this.highlightPos = this.cursorPos = newCursorPosition;
        }

        this.value = result;
        this.onValueChange(result);
    }
}
