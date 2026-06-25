package dev.norbiros.emojitype.mixin;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MultilineTextField.class)
public abstract class MultilineTextFieldMixin {

    @Shadow
    private int cursor;

    @Shadow
    private int selectCursor;

    @Shadow
    public abstract String value();

    @Shadow
    public abstract void setValue(String value);

    @Inject(method = "insertText(Ljava/lang/String;)V", at = @At("TAIL"))
    private void onInsertText(String insertion, CallbackInfo ci) {
        String value = this.value();
        String result = value;
        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            result = result.replace(emojiCode.getCodeWithColons(), emojiCode.getEmoji());
        }

        if (!Objects.equals(value, result)) {
            int lengthDifference = value.length() - result.length();
            int newCursorPos = Math.clamp(this.cursor - lengthDifference + 1, 0, result.length());
            this.setValue(result);
            this.cursor = newCursorPos;
            this.selectCursor = newCursorPos;
        }
    }
}
