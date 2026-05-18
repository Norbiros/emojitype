package dev.norbiros.emojitype.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class SuggestionWindowMixin {

    @Shadow
    @Final
    CommandSuggestions this$0;

    @Shadow
    private int current;

    @Shadow
    @Final
    private List<Suggestion> suggestionList;

    @Inject(method = "useSuggestion", at = @At("TAIL"))
    private void onComplete(CallbackInfo callbackInfo) {
        if (this.suggestionList == null || this.suggestionList.isEmpty()) {
            return;
        }
        if (this.current < 0 || this.current >= this.suggestionList.size()) {
            return;
        }

        EditBox input = ((ChatInputSuggestorAccessor) this.this$0).emojitype$getInput();
        Suggestion suggestion = this.suggestionList.get(this.current);

        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            if (suggestion.getText().equals(emojiCode.getChatSuggestion())) {
                String value = input.getValue().replace(emojiCode.getChatSuggestion(), emojiCode.getEmoji());
                input.setValue(value);
                input.setCursorPosition(Math.min(input.getCursorPosition(), value.length()));
                break;
            }
        }
    }
}
