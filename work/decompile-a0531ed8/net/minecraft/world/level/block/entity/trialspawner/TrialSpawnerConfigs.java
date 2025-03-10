package net.minecraft.world.level.block.entity.trialspawner;

import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

public class TrialSpawnerConfigs {

    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_BREEZE = TrialSpawnerConfigs.a.of("trial_chamber/breeze");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_MELEE_HUSK = TrialSpawnerConfigs.a.of("trial_chamber/melee/husk");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_MELEE_SPIDER = TrialSpawnerConfigs.a.of("trial_chamber/melee/spider");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_MELEE_ZOMBIE = TrialSpawnerConfigs.a.of("trial_chamber/melee/zombie");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_RANGED_POISON_SKELETON = TrialSpawnerConfigs.a.of("trial_chamber/ranged/poison_skeleton");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_RANGED_SKELETON = TrialSpawnerConfigs.a.of("trial_chamber/ranged/skeleton");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_RANGED_STRAY = TrialSpawnerConfigs.a.of("trial_chamber/ranged/stray");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_SLOW_RANGED_POISON_SKELETON = TrialSpawnerConfigs.a.of("trial_chamber/slow_ranged/poison_skeleton");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_SLOW_RANGED_SKELETON = TrialSpawnerConfigs.a.of("trial_chamber/slow_ranged/skeleton");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_SLOW_RANGED_STRAY = TrialSpawnerConfigs.a.of("trial_chamber/slow_ranged/stray");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_SMALL_MELEE_BABY_ZOMBIE = TrialSpawnerConfigs.a.of("trial_chamber/small_melee/baby_zombie");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_SMALL_MELEE_CAVE_SPIDER = TrialSpawnerConfigs.a.of("trial_chamber/small_melee/cave_spider");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_SMALL_MELEE_SILVERFISH = TrialSpawnerConfigs.a.of("trial_chamber/small_melee/silverfish");
    private static final TrialSpawnerConfigs.a TRIAL_CHAMBER_SMALL_MELEE_SLIME = TrialSpawnerConfigs.a.of("trial_chamber/small_melee/slime");

    public TrialSpawnerConfigs() {}

    public static void bootstrap(BootstrapContext<TrialSpawnerConfig> bootstrapcontext) {
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_BREEZE, TrialSpawnerConfig.builder().simultaneousMobs(1.0F).simultaneousMobsAddedPerPlayer(0.5F).ticksBetweenSpawn(20).totalMobs(2.0F).totalMobsAddedPerPlayer(1.0F).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.BREEZE))).build(), TrialSpawnerConfig.builder().simultaneousMobsAddedPerPlayer(0.5F).ticksBetweenSpawn(20).totalMobs(4.0F).totalMobsAddedPerPlayer(1.0F).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.BREEZE))).lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_MELEE_HUSK, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.HUSK))).build(), trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.HUSK, LootTables.EQUIPMENT_TRIAL_CHAMBER_MELEE))).lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_MELEE_SPIDER, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.SPIDER))).build(), trialChamberMeleeOminous().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.SPIDER))).lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_MELEE_ZOMBIE, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.ZOMBIE))).build(), trialChamberBase().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.ZOMBIE, LootTables.EQUIPMENT_TRIAL_CHAMBER_MELEE))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_RANGED_POISON_SKELETON, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.BOGGED))).build(), trialChamberBase().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.BOGGED, LootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_RANGED_SKELETON, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.SKELETON))).build(), trialChamberBase().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.SKELETON, LootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_RANGED_STRAY, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.STRAY))).build(), trialChamberBase().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.STRAY, LootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_SLOW_RANGED_POISON_SKELETON, trialChamberSlowRanged().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.BOGGED))).build(), trialChamberSlowRanged().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.BOGGED, LootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_SLOW_RANGED_SKELETON, trialChamberSlowRanged().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.SKELETON))).build(), trialChamberSlowRanged().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.SKELETON, LootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_SLOW_RANGED_STRAY, trialChamberSlowRanged().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.STRAY))).build(), trialChamberSlowRanged().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnDataWithEquipment(EntityTypes.STRAY, LootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_SMALL_MELEE_BABY_ZOMBIE, TrialSpawnerConfig.builder().simultaneousMobsAddedPerPlayer(0.5F).ticksBetweenSpawn(20).spawnPotentialsDefinition(SimpleWeightedRandomList.single(customSpawnDataWithEquipment(EntityTypes.ZOMBIE, (nbttagcompound) -> {
            nbttagcompound.putBoolean("IsBaby", true);
        }, (ResourceKey) null))).build(), TrialSpawnerConfig.builder().simultaneousMobsAddedPerPlayer(0.5F).ticksBetweenSpawn(20).lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(customSpawnDataWithEquipment(EntityTypes.ZOMBIE, (nbttagcompound) -> {
            nbttagcompound.putBoolean("IsBaby", true);
        }, LootTables.EQUIPMENT_TRIAL_CHAMBER_MELEE))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_SMALL_MELEE_CAVE_SPIDER, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.CAVE_SPIDER))).build(), trialChamberMeleeOminous().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.CAVE_SPIDER))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_SMALL_MELEE_SILVERFISH, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.SILVERFISH))).build(), trialChamberMeleeOminous().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.single(spawnData(EntityTypes.SILVERFISH))).build());
        register(bootstrapcontext, TrialSpawnerConfigs.TRIAL_CHAMBER_SMALL_MELEE_SLIME, trialChamberBase().spawnPotentialsDefinition(SimpleWeightedRandomList.builder().add(customSpawnData(EntityTypes.SLIME, (nbttagcompound) -> {
            nbttagcompound.putByte("Size", (byte) 1);
        }), 3).add(customSpawnData(EntityTypes.SLIME, (nbttagcompound) -> {
            nbttagcompound.putByte("Size", (byte) 2);
        }), 1).build()).build(), trialChamberMeleeOminous().lootTablesToEject(SimpleWeightedRandomList.builder().add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3).add(LootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7).build()).spawnPotentialsDefinition(SimpleWeightedRandomList.builder().add(customSpawnData(EntityTypes.SLIME, (nbttagcompound) -> {
            nbttagcompound.putByte("Size", (byte) 1);
        }), 3).add(customSpawnData(EntityTypes.SLIME, (nbttagcompound) -> {
            nbttagcompound.putByte("Size", (byte) 2);
        }), 1).build()).build());
    }

    private static <T extends Entity> MobSpawnerData spawnData(EntityTypes<T> entitytypes) {
        return customSpawnDataWithEquipment(entitytypes, (nbttagcompound) -> {
        }, (ResourceKey) null);
    }

    private static <T extends Entity> MobSpawnerData customSpawnData(EntityTypes<T> entitytypes, Consumer<NBTTagCompound> consumer) {
        return customSpawnDataWithEquipment(entitytypes, consumer, (ResourceKey) null);
    }

    private static <T extends Entity> MobSpawnerData spawnDataWithEquipment(EntityTypes<T> entitytypes, ResourceKey<LootTable> resourcekey) {
        return customSpawnDataWithEquipment(entitytypes, (nbttagcompound) -> {
        }, resourcekey);
    }

    private static <T extends Entity> MobSpawnerData customSpawnDataWithEquipment(EntityTypes<T> entitytypes, Consumer<NBTTagCompound> consumer, @Nullable ResourceKey<LootTable> resourcekey) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entitytypes).toString());
        consumer.accept(nbttagcompound);
        Optional<EquipmentTable> optional = Optional.ofNullable(resourcekey).map((resourcekey1) -> {
            return new EquipmentTable(resourcekey1, 0.0F);
        });

        return new MobSpawnerData(nbttagcompound, Optional.empty(), optional);
    }

    private static void register(BootstrapContext<TrialSpawnerConfig> bootstrapcontext, TrialSpawnerConfigs.a trialspawnerconfigs_a, TrialSpawnerConfig trialspawnerconfig, TrialSpawnerConfig trialspawnerconfig1) {
        bootstrapcontext.register(trialspawnerconfigs_a.normal, trialspawnerconfig);
        bootstrapcontext.register(trialspawnerconfigs_a.ominous, trialspawnerconfig1);
    }

    static ResourceKey<TrialSpawnerConfig> registryKey(String s) {
        return ResourceKey.create(Registries.TRIAL_SPAWNER_CONFIG, MinecraftKey.withDefaultNamespace(s));
    }

    private static TrialSpawnerConfig.a trialChamberMeleeOminous() {
        return TrialSpawnerConfig.builder().simultaneousMobs(4.0F).simultaneousMobsAddedPerPlayer(0.5F).ticksBetweenSpawn(20).totalMobs(12.0F);
    }

    private static TrialSpawnerConfig.a trialChamberSlowRanged() {
        return TrialSpawnerConfig.builder().simultaneousMobs(4.0F).simultaneousMobsAddedPerPlayer(2.0F).ticksBetweenSpawn(160);
    }

    private static TrialSpawnerConfig.a trialChamberBase() {
        return TrialSpawnerConfig.builder().simultaneousMobs(3.0F).simultaneousMobsAddedPerPlayer(0.5F).ticksBetweenSpawn(20);
    }

    private static record a(ResourceKey<TrialSpawnerConfig> normal, ResourceKey<TrialSpawnerConfig> ominous) {

        public static TrialSpawnerConfigs.a of(String s) {
            return new TrialSpawnerConfigs.a(TrialSpawnerConfigs.registryKey(s + "/normal"), TrialSpawnerConfigs.registryKey(s + "/ominous"));
        }
    }
}
