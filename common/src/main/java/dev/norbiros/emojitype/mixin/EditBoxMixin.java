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
public abstract class EditBoxMixin {
    @Shadow
    public abstract String getValue();

    @Shadow
    public abstract void setValue(String value);

    @Shadow
    public abstract int getCursorPosition();

    @Shadow
    public abstract void setCursorPosition(int cursorPosition);

    @Shadow
    public abstract void setHighlightPos(int highlightPos);

    @Inject(method = "charTyped", at = @At("RETURN"))
    private void onCharTyped(CharacterEvent input, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!callbackInfo.getReturnValue()) {
            return;
        }

        String value = getValue();
        String result = value;
        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            result = result.replace(emojiCode.getCodeWithColons(), emojiCode.getEmoji());
        }

        if (!Objects.equals(value, result)) {
            int lengthDifference = value.length() - result.length();
            int newCursorPosition = Math.clamp(this.getCursorPosition() - lengthDifference + 1, 0, result.length());
            this.setValue(result);
            this.setCursorPosition(newCursorPosition);
            this.setHighlightPos(newCursorPosition);
        }
    }
}
