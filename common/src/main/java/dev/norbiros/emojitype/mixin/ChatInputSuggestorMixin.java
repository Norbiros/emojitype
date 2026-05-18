package dev.norbiros.emojitype.mixin;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.norbiros.emojitype.EmojiType;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(CommandSuggestions.class)
public abstract class ChatInputSuggestorMixin {
    private static final Pattern COLON_PATTERN = Pattern.compile("(:)");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    @Shadow
    @Final
    private EditBox input;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    @Final
    private boolean commandsOnly;

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Inject(method = "updateCommandInfo", at = @At("TAIL"), cancellable = true)
    private void inject(CallbackInfo ci) {
        String text = this.input.getValue();
        StringReader stringReader = new StringReader(text);
        boolean hasSlash = stringReader.canRead() && stringReader.peek() == '/';
        if (hasSlash) {
            stringReader.skip();
        }
        boolean isCommand = this.commandsOnly || hasSlash;
        int cursor = this.input.getCursorPosition();
        if (!isCommand) {
            String textUptoCursor = text.substring(0, cursor);
            int start = Math.max(getLastPattern(textUptoCursor, COLON_PATTERN) - 1, 0);
            int whitespace = getLastPattern(textUptoCursor, WHITESPACE_PATTERN);
            if (start < textUptoCursor.length() && start >= whitespace) {
                if (textUptoCursor.charAt(start) == ':') {
                    this.pendingSuggestions = SharedSuggestionProvider.suggest(EmojiType.getChatSuggestions(), new SuggestionsBuilder(textUptoCursor, start));
                    this.pendingSuggestions.thenRun(() -> {
                        if (!this.pendingSuggestions.isDone()) {
                            return;
                        }
                        this.showSuggestions(false);
                    });
                    ci.cancel();
                }
            }
        }
    }

    private int getLastPattern(String input, Pattern pattern) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }
}
