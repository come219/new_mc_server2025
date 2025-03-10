package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class LootItemConditionKilledByPlayer implements LootItemCondition {

    private static final LootItemConditionKilledByPlayer INSTANCE = new LootItemConditionKilledByPlayer();
    public static final MapCodec<LootItemConditionKilledByPlayer> CODEC = MapCodec.unit(LootItemConditionKilledByPlayer.INSTANCE);

    private LootItemConditionKilledByPlayer() {}

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.KILLED_BY_PLAYER;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    public boolean test(LootTableInfo loottableinfo) {
        return loottableinfo.hasParameter(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.a killedByPlayer() {
        return () -> {
            return LootItemConditionKilledByPlayer.INSTANCE;
        };
    }
}
