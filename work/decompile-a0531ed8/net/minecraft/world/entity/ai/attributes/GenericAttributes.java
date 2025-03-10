package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public class GenericAttributes {

    public static final Holder<AttributeBase> ARMOR = register("armor", (new AttributeRanged("attribute.name.armor", 0.0D, 0.0D, 30.0D)).setSyncable(true));
    public static final Holder<AttributeBase> ARMOR_TOUGHNESS = register("armor_toughness", (new AttributeRanged("attribute.name.armor_toughness", 0.0D, 0.0D, 20.0D)).setSyncable(true));
    public static final Holder<AttributeBase> ATTACK_DAMAGE = register("attack_damage", new AttributeRanged("attribute.name.attack_damage", 2.0D, 0.0D, 2048.0D));
    public static final Holder<AttributeBase> ATTACK_KNOCKBACK = register("attack_knockback", new AttributeRanged("attribute.name.attack_knockback", 0.0D, 0.0D, 5.0D));
    public static final Holder<AttributeBase> ATTACK_SPEED = register("attack_speed", (new AttributeRanged("attribute.name.attack_speed", 4.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> BLOCK_BREAK_SPEED = register("block_break_speed", (new AttributeRanged("attribute.name.block_break_speed", 1.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> BLOCK_INTERACTION_RANGE = register("block_interaction_range", (new AttributeRanged("attribute.name.block_interaction_range", 4.5D, 0.0D, 64.0D)).setSyncable(true));
    public static final Holder<AttributeBase> BURNING_TIME = register("burning_time", (new AttributeRanged("attribute.name.burning_time", 1.0D, 0.0D, 1024.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEGATIVE));
    public static final Holder<AttributeBase> EXPLOSION_KNOCKBACK_RESISTANCE = register("explosion_knockback_resistance", (new AttributeRanged("attribute.name.explosion_knockback_resistance", 0.0D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> ENTITY_INTERACTION_RANGE = register("entity_interaction_range", (new AttributeRanged("attribute.name.entity_interaction_range", 3.0D, 0.0D, 64.0D)).setSyncable(true));
    public static final Holder<AttributeBase> FALL_DAMAGE_MULTIPLIER = register("fall_damage_multiplier", (new AttributeRanged("attribute.name.fall_damage_multiplier", 1.0D, 0.0D, 100.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEGATIVE));
    public static final Holder<AttributeBase> FLYING_SPEED = register("flying_speed", (new AttributeRanged("attribute.name.flying_speed", 0.4D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> FOLLOW_RANGE = register("follow_range", new AttributeRanged("attribute.name.follow_range", 32.0D, 0.0D, 2048.0D));
    public static final Holder<AttributeBase> GRAVITY = register("gravity", (new AttributeRanged("attribute.name.gravity", 0.08D, -1.0D, 1.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEUTRAL));
    public static final Holder<AttributeBase> JUMP_STRENGTH = register("jump_strength", (new AttributeRanged("attribute.name.jump_strength", 0.41999998688697815D, 0.0D, 32.0D)).setSyncable(true));
    public static final Holder<AttributeBase> KNOCKBACK_RESISTANCE = register("knockback_resistance", new AttributeRanged("attribute.name.knockback_resistance", 0.0D, 0.0D, 1.0D));
    public static final Holder<AttributeBase> LUCK = register("luck", (new AttributeRanged("attribute.name.luck", 0.0D, -1024.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MAX_ABSORPTION = register("max_absorption", (new AttributeRanged("attribute.name.max_absorption", 0.0D, 0.0D, 2048.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MAX_HEALTH = register("max_health", (new AttributeRanged("attribute.name.max_health", 20.0D, 1.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MINING_EFFICIENCY = register("mining_efficiency", (new AttributeRanged("attribute.name.mining_efficiency", 0.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MOVEMENT_EFFICIENCY = register("movement_efficiency", (new AttributeRanged("attribute.name.movement_efficiency", 0.0D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MOVEMENT_SPEED = register("movement_speed", (new AttributeRanged("attribute.name.movement_speed", 0.7D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> OXYGEN_BONUS = register("oxygen_bonus", (new AttributeRanged("attribute.name.oxygen_bonus", 0.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SAFE_FALL_DISTANCE = register("safe_fall_distance", (new AttributeRanged("attribute.name.safe_fall_distance", 3.0D, -1024.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SCALE = register("scale", (new AttributeRanged("attribute.name.scale", 1.0D, 0.0625D, 16.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEUTRAL));
    public static final Holder<AttributeBase> SNEAKING_SPEED = register("sneaking_speed", (new AttributeRanged("attribute.name.sneaking_speed", 0.3D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SPAWN_REINFORCEMENTS_CHANCE = register("spawn_reinforcements", new AttributeRanged("attribute.name.spawn_reinforcements", 0.0D, 0.0D, 1.0D));
    public static final Holder<AttributeBase> STEP_HEIGHT = register("step_height", (new AttributeRanged("attribute.name.step_height", 0.6D, 0.0D, 10.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SUBMERGED_MINING_SPEED = register("submerged_mining_speed", (new AttributeRanged("attribute.name.submerged_mining_speed", 0.2D, 0.0D, 20.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SWEEPING_DAMAGE_RATIO = register("sweeping_damage_ratio", (new AttributeRanged("attribute.name.sweeping_damage_ratio", 0.0D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> TEMPT_RANGE = register("tempt_range", new AttributeRanged("attribute.name.tempt_range", 10.0D, 0.0D, 2048.0D));
    public static final Holder<AttributeBase> WATER_MOVEMENT_EFFICIENCY = register("water_movement_efficiency", (new AttributeRanged("attribute.name.water_movement_efficiency", 0.0D, 0.0D, 1.0D)).setSyncable(true));

    public GenericAttributes() {}

    private static Holder<AttributeBase> register(String s, AttributeBase attributebase) {
        return IRegistry.registerForHolder(BuiltInRegistries.ATTRIBUTE, MinecraftKey.withDefaultNamespace(s), attributebase);
    }

    public static Holder<AttributeBase> bootstrap(IRegistry<AttributeBase> iregistry) {
        return GenericAttributes.MAX_HEALTH;
    }
}
