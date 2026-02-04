package dev.norbiros.emojitype.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import dev.norbiros.emojitype.EmojiType;
import dev.norbiros.emojitype.emoji.EmojiCode;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public abstract class SuggestionWindowMixin {

    @Shadow
    @Final
    ChatInputSuggestor field_21615;

    @Shadow
    private int selection;

    @Shadow
    @Final
    private List<Suggestion> suggestions;

    @Inject(method = "complete", at = @At("TAIL"))
    private void onComplete(CallbackInfo callbackInfo) {
        ChatInputSuggestorAccessor inputSuggestor = (ChatInputSuggestorAccessor) this.field_21615;
        if (inputSuggestor == null) {
            return;
        }
        
        if (this.suggestions == null || this.suggestions.isEmpty()) {
            return;
        }
        if (this.selection < 0 || this.selection >= this.suggestions.size()) {
            return;
        }
        
        TextFieldWidget textFieldWidget = inputSuggestor.getTextField();
        Suggestion suggestion = this.suggestions.get(this.selection);

        for (EmojiCode emojiCode : EmojiType.getActiveEmojiCodes()) {
            if (suggestion.getText().equals(emojiCode.getChatSuggestion())) {
                int suggestionLength = emojiCode.getChatSuggestion().length();
                textFieldWidget.eraseCharacters(-suggestionLength);
                textFieldWidget.setSelectionEnd(textFieldWidget.getCursor());
                textFieldWidget.write(emojiCode.getEmoji());
                break;
            }
        }
    }
}
