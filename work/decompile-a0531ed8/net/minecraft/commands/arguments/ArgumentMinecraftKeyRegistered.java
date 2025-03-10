package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.resources.MinecraftKey;

public class ArgumentMinecraftKeyRegistered implements ArgumentType<MinecraftKey> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    public ArgumentMinecraftKeyRegistered() {}

    public static ArgumentMinecraftKeyRegistered id() {
        return new ArgumentMinecraftKeyRegistered();
    }

    public static MinecraftKey getId(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (MinecraftKey) commandcontext.getArgument(s, MinecraftKey.class);
    }

    public MinecraftKey parse(StringReader stringreader) throws CommandSyntaxException {
        return MinecraftKey.read(stringreader);
    }

    public Collection<String> getExamples() {
        return ArgumentMinecraftKeyRegistered.EXAMPLES;
    }
}
