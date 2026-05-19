package dev.norbiros.emojitype.mixin;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.gui.font.TextFieldHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(TextFieldHelper.class)
public abstract class SelectionManagerMixin {

    @Shadow
    private int cursorPos;

    @Shadow
    private int selectionPos;

    @Shadow
    @Final
    private Supplier<String> getMessageFn;

    @Shadow
    @Final
    private Consumer<String> setMessageFn;

    @Inject(method = "insertText(Ljava/lang/String;Ljava/lang/String;)V", at = @At("TAIL"))
    private void onInsert(String _unused, String insertion, CallbackInfo callbackInfo) {
        String result = getMessageFn.get();
        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            result = result.replace(emojiCode.getCodeWithColons(), emojiCode.getEmoji());
        }

        if (!Objects.equals(getMessageFn.get(), result)) {
            int lengthDifference = getMessageFn.get().length() - result.length();
            int newCursorPosition = Math.max(Math.min(this.selectionPos - lengthDifference + 1, result.length()), 0);
            this.selectionPos = this.cursorPos = newCursorPosition;
        }

        setMessageFn.accept(result);
    }
}
