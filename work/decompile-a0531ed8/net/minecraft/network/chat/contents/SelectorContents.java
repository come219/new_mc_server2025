package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatFormatted;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.world.entity.Entity;

public record SelectorContents(SelectorPattern selector, Optional<IChatBaseComponent> separator) implements ComponentContents {

    public static final MapCodec<SelectorContents> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(SelectorPattern.CODEC.fieldOf("selector").forGetter(SelectorContents::selector), ComponentSerialization.CODEC.optionalFieldOf("separator").forGetter(SelectorContents::separator)).apply(instance, SelectorContents::new);
    });
    public static final ComponentContents.a<SelectorContents> TYPE = new ComponentContents.a<>(SelectorContents.CODEC, "selector");

    @Override
    public ComponentContents.a<?> type() {
        return SelectorContents.TYPE;
    }

    @Override
    public IChatMutableComponent resolve(@Nullable CommandListenerWrapper commandlistenerwrapper, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (commandlistenerwrapper == null) {
            return IChatBaseComponent.empty();
        } else {
            Optional<? extends IChatBaseComponent> optional = ChatComponentUtils.updateForEntity(commandlistenerwrapper, this.separator, entity, i);

            return ChatComponentUtils.formatList(this.selector.resolved().findEntities(commandlistenerwrapper), optional, Entity::getDisplayName);
        }
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.b<T> ichatformatted_b, ChatModifier chatmodifier) {
        return ichatformatted_b.accept(chatmodifier, this.selector.pattern());
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.a<T> ichatformatted_a) {
        return ichatformatted_a.accept(this.selector.pattern());
    }

    public String toString() {
        return "pattern{" + String.valueOf(this.selector) + "}";
    }
}
