package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

public class BehaviorVillageHeroGift extends Behavior<EntityVillager> {

    private static final int THROW_GIFT_AT_DISTANCE = 5;
    private static final int MIN_TIME_BETWEEN_GIFTS = 600;
    private static final int MAX_TIME_BETWEEN_GIFTS = 6600;
    private static final int TIME_TO_DELAY_FOR_HEAD_TO_FINISH_TURNING = 20;
    private static final Map<VillagerProfession, ResourceKey<LootTable>> GIFTS = ImmutableMap.builder().put(VillagerProfession.ARMORER, LootTables.ARMORER_GIFT).put(VillagerProfession.BUTCHER, LootTables.BUTCHER_GIFT).put(VillagerProfession.CARTOGRAPHER, LootTables.CARTOGRAPHER_GIFT).put(VillagerProfession.CLERIC, LootTables.CLERIC_GIFT).put(VillagerProfession.FARMER, LootTables.FARMER_GIFT).put(VillagerProfession.FISHERMAN, LootTables.FISHERMAN_GIFT).put(VillagerProfession.FLETCHER, LootTables.FLETCHER_GIFT).put(VillagerProfession.LEATHERWORKER, LootTables.LEATHERWORKER_GIFT).put(VillagerProfession.LIBRARIAN, LootTables.LIBRARIAN_GIFT).put(VillagerProfession.MASON, LootTables.MASON_GIFT).put(VillagerProfession.SHEPHERD, LootTables.SHEPHERD_GIFT).put(VillagerProfession.TOOLSMITH, LootTables.TOOLSMITH_GIFT).put(VillagerProfession.WEAPONSMITH, LootTables.WEAPONSMITH_GIFT).build();
    private static final float SPEED_MODIFIER = 0.5F;
    private int timeUntilNextGift = 600;
    private boolean giftGivenDuringThisRun;
    private long timeSinceStart;

    public BehaviorVillageHeroGift(int i) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.VALUE_PRESENT), i);
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, EntityVillager entityvillager) {
        if (!this.isHeroVisible(entityvillager)) {
            return false;
        } else if (this.timeUntilNextGift > 0) {
            --this.timeUntilNextGift;
            return false;
        } else {
            return true;
        }
    }

    protected void start(WorldServer worldserver, EntityVillager entityvillager, long i) {
        this.giftGivenDuringThisRun = false;
        this.timeSinceStart = i;
        EntityHuman entityhuman = (EntityHuman) this.getNearestTargetableHero(entityvillager).get();

        entityvillager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, (Object) entityhuman);
        BehaviorUtil.lookAtEntity(entityvillager, entityhuman);
    }

    protected boolean canStillUse(WorldServer worldserver, EntityVillager entityvillager, long i) {
        return this.isHeroVisible(entityvillager) && !this.giftGivenDuringThisRun;
    }

    protected void tick(WorldServer worldserver, EntityVillager entityvillager, long i) {
        EntityHuman entityhuman = (EntityHuman) this.getNearestTargetableHero(entityvillager).get();

        BehaviorUtil.lookAtEntity(entityvillager, entityhuman);
        if (this.isWithinThrowingDistance(entityvillager, entityhuman)) {
            if (i - this.timeSinceStart > 20L) {
                this.throwGift(worldserver, entityvillager, entityhuman);
                this.giftGivenDuringThisRun = true;
            }
        } else {
            BehaviorUtil.setWalkAndLookTargetMemories(entityvillager, (Entity) entityhuman, 0.5F, 5);
        }

    }

    protected void stop(WorldServer worldserver, EntityVillager entityvillager, long i) {
        this.timeUntilNextGift = calculateTimeUntilNextGift(worldserver);
        entityvillager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        entityvillager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entityvillager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    private void throwGift(WorldServer worldserver, EntityVillager entityvillager, EntityLiving entityliving) {
        entityvillager.dropFromGiftLootTable(worldserver, getLootTableToThrow(entityvillager), (worldserver1, itemstack) -> {
            BehaviorUtil.throwItem(entityvillager, itemstack, entityliving.position());
        });
    }

    private static ResourceKey<LootTable> getLootTableToThrow(EntityVillager entityvillager) {
        if (entityvillager.isBaby()) {
            return LootTables.BABY_VILLAGER_GIFT;
        } else {
            VillagerProfession villagerprofession = entityvillager.getVillagerData().getProfession();

            return (ResourceKey) BehaviorVillageHeroGift.GIFTS.getOrDefault(villagerprofession, LootTables.UNEMPLOYED_GIFT);
        }
    }

    private boolean isHeroVisible(EntityVillager entityvillager) {
        return this.getNearestTargetableHero(entityvillager).isPresent();
    }

    private Optional<EntityHuman> getNearestTargetableHero(EntityVillager entityvillager) {
        return entityvillager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
    }

    private boolean isHero(EntityHuman entityhuman) {
        return entityhuman.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
    }

    private boolean isWithinThrowingDistance(EntityVillager entityvillager, EntityHuman entityhuman) {
        BlockPosition blockposition = entityhuman.blockPosition();
        BlockPosition blockposition1 = entityvillager.blockPosition();

        return blockposition1.closerThan(blockposition, 5.0D);
    }

    private static int calculateTimeUntilNextGift(WorldServer worldserver) {
        return 600 + worldserver.random.nextInt(6001);
    }
}
