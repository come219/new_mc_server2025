package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public record EnchantmentActiveCheck(boolean active) implements LootItemCondition {

    public static final MapCodec<EnchantmentActiveCheck> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.BOOL.fieldOf("active").forGetter(EnchantmentActiveCheck::active)).apply(instance, EnchantmentActiveCheck::new);
    });

    public boolean test(LootTableInfo loottableinfo) {
        return (Boolean) loottableinfo.getParameter(LootContextParameters.ENCHANTMENT_ACTIVE) == this.active;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENCHANTMENT_ACTIVE_CHECK;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParameters.ENCHANTMENT_ACTIVE);
    }

    public static LootItemCondition.a enchantmentActiveCheck() {
        return () -> {
            return new EnchantmentActiveCheck(true);
        };
    }

    public static LootItemCondition.a enchantmentInactiveCheck() {
        return () -> {
            return new EnchantmentActiveCheck(false);
        };
    }
}
