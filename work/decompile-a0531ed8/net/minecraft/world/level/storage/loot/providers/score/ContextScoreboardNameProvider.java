package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.scores.ScoreHolder;

public record ContextScoreboardNameProvider(LootTableInfo.EntityTarget target) implements ScoreboardNameProvider {

    public static final MapCodec<ContextScoreboardNameProvider> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LootTableInfo.EntityTarget.CODEC.fieldOf("target").forGetter(ContextScoreboardNameProvider::target)).apply(instance, ContextScoreboardNameProvider::new);
    });
    public static final Codec<ContextScoreboardNameProvider> INLINE_CODEC = LootTableInfo.EntityTarget.CODEC.xmap(ContextScoreboardNameProvider::new, ContextScoreboardNameProvider::target);

    public static ScoreboardNameProvider forTarget(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new ContextScoreboardNameProvider(loottableinfo_entitytarget);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.CONTEXT;
    }

    @Nullable
    @Override
    public ScoreHolder getScoreHolder(LootTableInfo loottableinfo) {
        return (ScoreHolder) loottableinfo.getOptionalParameter(this.target.getParam());
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.target.getParam());
    }
}
