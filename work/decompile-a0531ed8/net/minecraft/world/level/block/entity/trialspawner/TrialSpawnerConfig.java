package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

public record TrialSpawnerConfig(int spawnRange, float totalMobs, float simultaneousMobs, float totalMobsAddedPerPlayer, float simultaneousMobsAddedPerPlayer, int ticksBetweenSpawn, SimpleWeightedRandomList<MobSpawnerData> spawnPotentialsDefinition, SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject, ResourceKey<LootTable> itemsToDropWhenOminous) {

    public static final TrialSpawnerConfig DEFAULT = builder().build();
    public static final Codec<TrialSpawnerConfig> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.intRange(1, 128).optionalFieldOf("spawn_range", TrialSpawnerConfig.DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs", TrialSpawnerConfig.DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("simultaneous_mobs", TrialSpawnerConfig.DEFAULT.simultaneousMobs).forGetter(TrialSpawnerConfig::simultaneousMobs), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs_added_per_player", TrialSpawnerConfig.DEFAULT.totalMobsAddedPerPlayer).forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("simultaneous_mobs_added_per_player", TrialSpawnerConfig.DEFAULT.simultaneousMobsAddedPerPlayer).forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("ticks_between_spawn", TrialSpawnerConfig.DEFAULT.ticksBetweenSpawn).forGetter(TrialSpawnerConfig::ticksBetweenSpawn), MobSpawnerData.LIST_CODEC.optionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty()).forGetter(TrialSpawnerConfig::spawnPotentialsDefinition), SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceKey.codec(Registries.LOOT_TABLE)).optionalFieldOf("loot_tables_to_eject", TrialSpawnerConfig.DEFAULT.lootTablesToEject).forGetter(TrialSpawnerConfig::lootTablesToEject), ResourceKey.codec(Registries.LOOT_TABLE).optionalFieldOf("items_to_drop_when_ominous", TrialSpawnerConfig.DEFAULT.itemsToDropWhenOminous).forGetter(TrialSpawnerConfig::itemsToDropWhenOminous)).apply(instance, TrialSpawnerConfig::new);
    });
    public static final Codec<Holder<TrialSpawnerConfig>> CODEC = RegistryFileCodec.create(Registries.TRIAL_SPAWNER_CONFIG, TrialSpawnerConfig.DIRECT_CODEC);

    public int calculateTargetTotalMobs(int i) {
        return (int) Math.floor((double) (this.totalMobs + this.totalMobsAddedPerPlayer * (float) i));
    }

    public int calculateTargetSimultaneousMobs(int i) {
        return (int) Math.floor((double) (this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float) i));
    }

    public long ticksBetweenItemSpawners() {
        return 160L;
    }

    public static TrialSpawnerConfig.a builder() {
        return new TrialSpawnerConfig.a();
    }

    public TrialSpawnerConfig withSpawning(EntityTypes<?> entitytypes) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entitytypes).toString());
        MobSpawnerData mobspawnerdata = new MobSpawnerData(nbttagcompound, Optional.empty(), Optional.empty());

        return new TrialSpawnerConfig(this.spawnRange, this.totalMobs, this.simultaneousMobs, this.totalMobsAddedPerPlayer, this.simultaneousMobsAddedPerPlayer, this.ticksBetweenSpawn, SimpleWeightedRandomList.single(mobspawnerdata), this.lootTablesToEject, this.itemsToDropWhenOminous);
    }

    public static class a {

        private int spawnRange = 4;
        private float totalMobs = 6.0F;
        private float simultaneousMobs = 2.0F;
        private float totalMobsAddedPerPlayer = 2.0F;
        private float simultaneousMobsAddedPerPlayer = 1.0F;
        private int ticksBetweenSpawn = 40;
        private SimpleWeightedRandomList<MobSpawnerData> spawnPotentialsDefinition = SimpleWeightedRandomList.empty();
        private SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject;
        private ResourceKey<LootTable> itemsToDropWhenOminous;

        public a() {
            this.lootTablesToEject = SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES).add(LootTables.SPAWNER_TRIAL_CHAMBER_KEY).build();
            this.itemsToDropWhenOminous = LootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS;
        }

        public TrialSpawnerConfig.a spawnRange(int i) {
            this.spawnRange = i;
            return this;
        }

        public TrialSpawnerConfig.a totalMobs(float f) {
            this.totalMobs = f;
            return this;
        }

        public TrialSpawnerConfig.a simultaneousMobs(float f) {
            this.simultaneousMobs = f;
            return this;
        }

        public TrialSpawnerConfig.a totalMobsAddedPerPlayer(float f) {
            this.totalMobsAddedPerPlayer = f;
            return this;
        }

        public TrialSpawnerConfig.a simultaneousMobsAddedPerPlayer(float f) {
            this.simultaneousMobsAddedPerPlayer = f;
            return this;
        }

        public TrialSpawnerConfig.a ticksBetweenSpawn(int i) {
            this.ticksBetweenSpawn = i;
            return this;
        }

        public TrialSpawnerConfig.a spawnPotentialsDefinition(SimpleWeightedRandomList<MobSpawnerData> simpleweightedrandomlist) {
            this.spawnPotentialsDefinition = simpleweightedrandomlist;
            return this;
        }

        public TrialSpawnerConfig.a lootTablesToEject(SimpleWeightedRandomList<ResourceKey<LootTable>> simpleweightedrandomlist) {
            this.lootTablesToEject = simpleweightedrandomlist;
            return this;
        }

        public TrialSpawnerConfig.a itemsToDropWhenOminous(ResourceKey<LootTable> resourcekey) {
            this.itemsToDropWhenOminous = resourcekey;
            return this;
        }

        public TrialSpawnerConfig build() {
            return new TrialSpawnerConfig(this.spawnRange, this.totalMobs, this.simultaneousMobs, this.totalMobsAddedPerPlayer, this.simultaneousMobsAddedPerPlayer, this.ticksBetweenSpawn, this.spawnPotentialsDefinition, this.lootTablesToEject, this.itemsToDropWhenOminous);
        }
    }
}
