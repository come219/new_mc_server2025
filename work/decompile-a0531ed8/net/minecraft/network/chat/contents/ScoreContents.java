package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.ScoreboardObjective;

public record ScoreContents(Either<SelectorPattern, String> name, String objective) implements ComponentContents {

    public static final MapCodec<ScoreContents> INNER_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.either(SelectorPattern.CODEC, Codec.STRING).fieldOf("name").forGetter(ScoreContents::name), Codec.STRING.fieldOf("objective").forGetter(ScoreContents::objective)).apply(instance, ScoreContents::new);
    });
    public static final MapCodec<ScoreContents> CODEC = ScoreContents.INNER_CODEC.fieldOf("score");
    public static final ComponentContents.a<ScoreContents> TYPE = new ComponentContents.a<>(ScoreContents.CODEC, "score");

    @Override
    public ComponentContents.a<?> type() {
        return ScoreContents.TYPE;
    }

    private ScoreHolder findTargetName(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        Optional<SelectorPattern> optional = this.name.left();

        if (optional.isPresent()) {
            List<? extends Entity> list = ((SelectorPattern) optional.get()).resolved().findEntities(commandlistenerwrapper);

            if (!list.isEmpty()) {
                if (list.size() != 1) {
                    throw ArgumentEntity.ERROR_NOT_SINGLE_ENTITY.create();
                } else {
                    return (ScoreHolder) list.getFirst();
                }
            } else {
                return ScoreHolder.forNameOnly(((SelectorPattern) optional.get()).pattern());
            }
        } else {
            return ScoreHolder.forNameOnly((String) this.name.right().orElseThrow());
        }
    }

    private IChatMutableComponent getScore(ScoreHolder scoreholder, CommandListenerWrapper commandlistenerwrapper) {
        MinecraftServer minecraftserver = commandlistenerwrapper.getServer();

        if (minecraftserver != null) {
            ScoreboardServer scoreboardserver = minecraftserver.getScoreboard();
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjective(this.objective);

            if (scoreboardobjective != null) {
                ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(scoreholder, scoreboardobjective);

                if (readonlyscoreinfo != null) {
                    return readonlyscoreinfo.formatValue(scoreboardobjective.numberFormatOrDefault(StyledFormat.NO_STYLE));
                }
            }
        }

        return IChatBaseComponent.empty();
    }

    @Override
    public IChatMutableComponent resolve(@Nullable CommandListenerWrapper commandlistenerwrapper, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (commandlistenerwrapper == null) {
            return IChatBaseComponent.empty();
        } else {
            ScoreHolder scoreholder = this.findTargetName(commandlistenerwrapper);
            Object object = entity != null && scoreholder.equals(ScoreHolder.WILDCARD) ? entity : scoreholder;

            return this.getScore((ScoreHolder) object, commandlistenerwrapper);
        }
    }

    public String toString() {
        String s = String.valueOf(this.name);

        return "score{name='" + s + "', objective='" + this.objective + "'}";
    }
}
