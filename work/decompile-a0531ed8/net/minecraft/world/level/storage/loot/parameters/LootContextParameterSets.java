package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.context.ContextKeySet;

public class LootContextParameterSets {

    private static final BiMap<MinecraftKey, ContextKeySet> REGISTRY = HashBiMap.create();
    public static final Codec<ContextKeySet> CODEC;
    public static final ContextKeySet EMPTY;
    public static final ContextKeySet CHEST;
    public static final ContextKeySet COMMAND;
    public static final ContextKeySet SELECTOR;
    public static final ContextKeySet FISHING;
    public static final ContextKeySet ENTITY;
    public static final ContextKeySet EQUIPMENT;
    public static final ContextKeySet ARCHAEOLOGY;
    public static final ContextKeySet GIFT;
    public static final ContextKeySet PIGLIN_BARTER;
    public static final ContextKeySet VAULT;
    public static final ContextKeySet ADVANCEMENT_REWARD;
    public static final ContextKeySet ADVANCEMENT_ENTITY;
    public static final ContextKeySet ADVANCEMENT_LOCATION;
    public static final ContextKeySet BLOCK_USE;
    public static final ContextKeySet ALL_PARAMS;
    public static final ContextKeySet BLOCK;
    public static final ContextKeySet SHEARING;
    public static final ContextKeySet ENCHANTED_DAMAGE;
    public static final ContextKeySet ENCHANTED_ITEM;
    public static final ContextKeySet ENCHANTED_LOCATION;
    public static final ContextKeySet ENCHANTED_ENTITY;
    public static final ContextKeySet HIT_BLOCK;

    public LootContextParameterSets() {}

    private static ContextKeySet register(String s, Consumer<ContextKeySet.a> consumer) {
        ContextKeySet.a contextkeyset_a = new ContextKeySet.a();

        consumer.accept(contextkeyset_a);
        ContextKeySet contextkeyset = contextkeyset_a.build();
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace(s);
        ContextKeySet contextkeyset1 = (ContextKeySet) LootContextParameterSets.REGISTRY.put(minecraftkey, contextkeyset);

        if (contextkeyset1 != null) {
            throw new IllegalStateException("Loot table parameter set " + String.valueOf(minecraftkey) + " is already registered");
        } else {
            return contextkeyset;
        }
    }

    static {
        Codec codec = MinecraftKey.CODEC;
        Function function = (minecraftkey) -> {
            return (DataResult) Optional.ofNullable((ContextKeySet) LootContextParameterSets.REGISTRY.get(minecraftkey)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    return "No parameter set exists with id: '" + String.valueOf(minecraftkey) + "'";
                });
            });
        };
        BiMap bimap = LootContextParameterSets.REGISTRY.inverse();

        Objects.requireNonNull(bimap);
        CODEC = codec.comapFlatMap(function, bimap::get);
        EMPTY = register("empty", (contextkeyset_a) -> {
        });
        CHEST = register("chest", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
        COMMAND = register("command", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
        SELECTOR = register("selector", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY);
        });
        FISHING = register("fishing", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).required(LootContextParameters.TOOL).optional(LootContextParameters.THIS_ENTITY);
        });
        ENTITY = register("entity", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.DAMAGE_SOURCE).optional(LootContextParameters.ATTACKING_ENTITY).optional(LootContextParameters.DIRECT_ATTACKING_ENTITY).optional(LootContextParameters.LAST_DAMAGE_PLAYER);
        });
        EQUIPMENT = register("equipment", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY);
        });
        ARCHAEOLOGY = register("archaeology", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.TOOL);
        });
        GIFT = register("gift", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY);
        });
        PIGLIN_BARTER = register("barter", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY);
        });
        VAULT = register("vault", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY).optional(LootContextParameters.TOOL);
        });
        ADVANCEMENT_REWARD = register("advancement_reward", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN);
        });
        ADVANCEMENT_ENTITY = register("advancement_entity", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN);
        });
        ADVANCEMENT_LOCATION = register("advancement_location", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.TOOL).required(LootContextParameters.BLOCK_STATE);
        });
        BLOCK_USE = register("block_use", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.BLOCK_STATE);
        });
        ALL_PARAMS = register("generic", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.LAST_DAMAGE_PLAYER).required(LootContextParameters.DAMAGE_SOURCE).required(LootContextParameters.ATTACKING_ENTITY).required(LootContextParameters.DIRECT_ATTACKING_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.BLOCK_STATE).required(LootContextParameters.BLOCK_ENTITY).required(LootContextParameters.TOOL).required(LootContextParameters.EXPLOSION_RADIUS);
        });
        BLOCK = register("block", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.BLOCK_STATE).required(LootContextParameters.ORIGIN).required(LootContextParameters.TOOL).optional(LootContextParameters.THIS_ENTITY).optional(LootContextParameters.BLOCK_ENTITY).optional(LootContextParameters.EXPLOSION_RADIUS);
        });
        SHEARING = register("shearing", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.TOOL);
        });
        ENCHANTED_DAMAGE = register("enchanted_damage", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN).required(LootContextParameters.DAMAGE_SOURCE).optional(LootContextParameters.DIRECT_ATTACKING_ENTITY).optional(LootContextParameters.ATTACKING_ENTITY);
        });
        ENCHANTED_ITEM = register("enchanted_item", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.TOOL).required(LootContextParameters.ENCHANTMENT_LEVEL);
        });
        ENCHANTED_LOCATION = register("enchanted_location", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN).required(LootContextParameters.ENCHANTMENT_ACTIVE);
        });
        ENCHANTED_ENTITY = register("enchanted_entity", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN);
        });
        HIT_BLOCK = register("hit_block", (contextkeyset_a) -> {
            contextkeyset_a.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN).required(LootContextParameters.BLOCK_STATE);
        });
    }
}
