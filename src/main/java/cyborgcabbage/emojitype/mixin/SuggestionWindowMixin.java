package cyborgcabbage.emojitype.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import cyborgcabbage.emojitype.EmojiTypeMod;
import cyborgcabbage.emojitype.emoji.EmojiCode;
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
    @Final ChatInputSuggestor inputSuggestor;

    @Shadow private int selection;

    @Shadow @Final private List<Suggestion> suggestions;

    @Shadow @Final
    ChatInputSuggestor field_21615;

    @Inject(method="complete",at=@At("TAIL"))
    private void overwriteComplete(CallbackInfo ci){
        ChatInputSuggestorAccessor inputSuggestor1 = (ChatInputSuggestorAccessor)inputSuggestor;
        ChatInputSuggestorAccessor inputSuggestor = (ChatInputSuggestorAccessor)this.field_21615;
        if (inputSuggestor == null) return;
        TextFieldWidget textFieldWidget = inputSuggestor.getTextField();
        Suggestion suggestion = this.suggestions.get(this.selection);
        int just = suggestion.getRange().getStart() + suggestion.getText().length()-2;
        for(EmojiCode ec: EmojiTypeMod.emojiCodes){
            int justTyped = just-ec.getEmoji().length();
            if(ec.match(textFieldWidget.getText(),justTyped)){
                textFieldWidget.eraseCharacters(-ec.getCode().length()-(1+ec.getEmoji().length()));
                textFieldWidget.setSelectionEnd(textFieldWidget.getCursor());
                textFieldWidget.write(ec.getEmoji());
                break;
            }
        }
    }
}
