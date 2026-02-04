package dev.norbiros.emojitype.mixin;

import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.util.SelectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(SelectionManager.class)
public abstract class SelectionManagerMixin {

    @Shadow
    private int selectionStart;

    @Shadow
    private int selectionEnd;

    @Shadow
    @Final
    private Supplier<String> stringGetter;

    @Shadow
    @Final
    private Consumer<String> stringSetter;

    @Inject(method = "insert(Ljava/lang/String;Ljava/lang/String;)V", at = @At("TAIL"))
    private void onInsert(String _unused, String insertion, CallbackInfo callbackInfo) {
        String result = stringGetter.get();
        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            result = result.replace(emojiCode.getCodeWithColons(), emojiCode.getEmoji());
        }

        if (!Objects.equals(stringGetter.get(), result)) {
            int lengthDifference = stringGetter.get().length() - result.length();
            int newCursorPosition = Math.max(Math.min(this.selectionEnd - lengthDifference + 1, result.length()), 0);
            this.selectionEnd = this.selectionStart = newCursorPosition;
        }

        stringSetter.accept(result);
    }
}
