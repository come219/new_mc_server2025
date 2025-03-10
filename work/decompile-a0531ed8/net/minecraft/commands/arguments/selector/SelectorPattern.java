package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record SelectorPattern(String pattern, EntitySelector resolved) {

    public static final Codec<SelectorPattern> CODEC = Codec.STRING.comapFlatMap(SelectorPattern::parse, SelectorPattern::pattern);

    public static DataResult<SelectorPattern> parse(String s) {
        try {
            ArgumentParserSelector argumentparserselector = new ArgumentParserSelector(new StringReader(s), true);

            return DataResult.success(new SelectorPattern(s, argumentparserselector.parse()));
        } catch (CommandSyntaxException commandsyntaxexception) {
            return DataResult.error(() -> {
                return "Invalid selector component: " + s + ": " + commandsyntaxexception.getMessage();
            });
        }
    }

    public boolean equals(Object object) {
        boolean flag;

        if (object instanceof SelectorPattern selectorpattern) {
            if (this.pattern.equals(selectorpattern.pattern)) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    public String toString() {
        return this.pattern;
    }
}
