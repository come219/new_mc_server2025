package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.village.ReputationEvent;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.trading.MerchantRecipeList;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import org.slf4j.Logger;

// CraftBukkit start
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.npc.EntityVillager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
// CraftBukkit end

public class EntityZombieVillager extends EntityZombie implements VillagerDataHolder {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DataWatcherObject<Boolean> DATA_CONVERTING_ID = DataWatcher.defineId(EntityZombieVillager.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<VillagerData> DATA_VILLAGER_DATA = DataWatcher.defineId(EntityZombieVillager.class, DataWatcherRegistry.VILLAGER_DATA);
    private static final int VILLAGER_CONVERSION_WAIT_MIN = 3600;
    private static final int VILLAGER_CONVERSION_WAIT_MAX = 6000;
    private static final int MAX_SPECIAL_BLOCKS_COUNT = 14;
    private static final int SPECIAL_BLOCK_RADIUS = 4;
    public int villagerConversionTime;
    @Nullable
    public UUID conversionStarter;
    @Nullable
    private NBTBase gossips;
    @Nullable
    private MerchantRecipeList tradeOffers;
    private int villagerXp;
    private int lastTick = MinecraftServer.currentTick; // CraftBukkit - add field

    public EntityZombieVillager(EntityTypes<? extends EntityZombieVillager> entitytypes, World world) {
        super(entitytypes, world);
        BuiltInRegistries.VILLAGER_PROFESSION.getRandom(this.random).ifPresent((holder_c) -> {
            this.setVillagerData(this.getVillagerData().setProfession((VillagerProfession) holder_c.value()));
        });
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityZombieVillager.DATA_CONVERTING_ID, false);
        datawatcher_a.define(EntityZombieVillager.DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        DataResult<NBTBase> dataresult = VillagerData.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.getVillagerData()); // CraftBukkit - decompile error
        Logger logger = EntityZombieVillager.LOGGER;

        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
            nbttagcompound.put("VillagerData", nbtbase);
        });
        if (this.tradeOffers != null) {
            nbttagcompound.put("Offers", (NBTBase) MerchantRecipeList.CODEC.encodeStart(this.registryAccess().createSerializationContext(DynamicOpsNBT.INSTANCE), this.tradeOffers).getOrThrow());
        }

        if (this.gossips != null) {
            nbttagcompound.put("Gossips", this.gossips);
        }

        nbttagcompound.putInt("ConversionTime", this.isConverting() ? this.villagerConversionTime : -1);
        if (this.conversionStarter != null) {
            nbttagcompound.putUUID("ConversionPlayer", this.conversionStarter);
        }

        nbttagcompound.putInt("Xp", this.villagerXp);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        if (nbttagcompound.contains("VillagerData", 10)) {
            DataResult<VillagerData> dataresult = VillagerData.CODEC.parse(new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound.get("VillagerData")));
            Logger logger = EntityZombieVillager.LOGGER;

            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent(this::setVillagerData);
        }

        if (nbttagcompound.contains("Offers")) {
            DataResult<MerchantRecipeList> dataresult1 = MerchantRecipeList.CODEC.parse(this.registryAccess().createSerializationContext(DynamicOpsNBT.INSTANCE), nbttagcompound.get("Offers")); // CraftBukkit - decompile error
            Logger logger1 = EntityZombieVillager.LOGGER;

            Objects.requireNonNull(logger1);
            dataresult1.resultOrPartial(SystemUtils.prefix("Failed to load offers: ", logger1::warn)).ifPresent((merchantrecipelist) -> {
                this.tradeOffers = merchantrecipelist;
            });
        }

        if (nbttagcompound.contains("Gossips", 9)) {
            this.gossips = nbttagcompound.getList("Gossips", 10);
        }

        if (nbttagcompound.contains("ConversionTime", 99) && nbttagcompound.getInt("ConversionTime") > -1) {
            this.startConverting(nbttagcompound.hasUUID("ConversionPlayer") ? nbttagcompound.getUUID("ConversionPlayer") : null, nbttagcompound.getInt("ConversionTime"));
        }

        if (nbttagcompound.contains("Xp", 3)) {
            this.villagerXp = nbttagcompound.getInt("Xp");
        }

    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isConverting()) {
            int i = this.getConversionProgress();
            // CraftBukkit start - Use wall time instead of ticks for villager conversion
            int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
            i *= elapsedTicks;
            // CraftBukkit end

            this.villagerConversionTime -= i;
            if (this.villagerConversionTime <= 0) {
                this.finishConversion((WorldServer) this.level());
            }
        }

        super.tick();
        this.lastTick = MinecraftServer.currentTick; // CraftBukkit
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (itemstack.is(Items.GOLDEN_APPLE)) {
            if (this.hasEffect(MobEffects.WEAKNESS)) {
                itemstack.consume(1, entityhuman);
                if (!this.level().isClientSide) {
                    this.startConverting(entityhuman.getUUID(), this.random.nextInt(2401) + 3600);
                }

                return EnumInteractionResult.SUCCESS_SERVER;
            } else {
                return EnumInteractionResult.CONSUME;
            }
        } else {
            return super.mobInteract(entityhuman, enumhand);
        }
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double d0) {
        return !this.isConverting() && this.villagerXp == 0;
    }

    public boolean isConverting() {
        return (Boolean) this.getEntityData().get(EntityZombieVillager.DATA_CONVERTING_ID);
    }

    public void startConverting(@Nullable UUID uuid, int i) {
        this.conversionStarter = uuid;
        this.villagerConversionTime = i;
        this.getEntityData().set(EntityZombieVillager.DATA_CONVERTING_ID, true);
        // CraftBukkit start
        this.removeEffect(MobEffects.WEAKNESS, org.bukkit.event.entity.EntityPotionEffectEvent.Cause.CONVERSION);
        this.addEffect(new MobEffect(MobEffects.DAMAGE_BOOST, i, Math.min(this.level().getDifficulty().getId() - 1, 0)), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.CONVERSION);
        // CraftBukkit end
        this.level().broadcastEntityEvent(this, (byte) 16);
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 16) {
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEffects.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }

        } else {
            super.handleEntityEvent(b0);
        }
    }

    private void finishConversion(WorldServer worldserver) {
        EntityVillager converted = this.convertTo(EntityTypes.VILLAGER, ConversionParams.single(this, false, false), (entityvillager) -> { // CraftBukkit
            Iterator iterator = this.dropPreservedEquipment(worldserver, (itemstack) -> {
                return !EnchantmentManager.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
            }).iterator();
            this.forceDrops = false; // CraftBukkit

            while (iterator.hasNext()) {
                EnumItemSlot enumitemslot = (EnumItemSlot) iterator.next();
                SlotAccess slotaccess = entityvillager.getSlot(enumitemslot.getIndex() + 300);

                slotaccess.set(this.getItemBySlot(enumitemslot));
            }

            entityvillager.setVillagerData(this.getVillagerData());
            if (this.gossips != null) {
                entityvillager.setGossips(this.gossips);
            }

            if (this.tradeOffers != null) {
                entityvillager.setOffers(this.tradeOffers.copy());
            }

            entityvillager.setVillagerXp(this.villagerXp);
            entityvillager.finalizeSpawn(worldserver, worldserver.getCurrentDifficultyAt(entityvillager.blockPosition()), EntitySpawnReason.CONVERSION, (GroupDataEntity) null);
            entityvillager.refreshBrain(worldserver);
            if (this.conversionStarter != null) {
                EntityHuman entityhuman = worldserver.getPlayerByUUID(this.conversionStarter);

                if (entityhuman instanceof EntityPlayer) {
                    CriterionTriggers.CURED_ZOMBIE_VILLAGER.trigger((EntityPlayer) entityhuman, this, entityvillager);
                    worldserver.onReputationEvent(ReputationEvent.ZOMBIE_VILLAGER_CURED, entityhuman, entityvillager);
                }
            }

            entityvillager.addEffect(new MobEffect(MobEffects.CONFUSION, 200, 0), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.CONVERSION); // CraftBukkit
            if (!this.isSilent()) {
                worldserver.levelEvent((EntityHuman) null, 1027, this.blockPosition(), 0);
            }
        // CraftBukkit start
        }, EntityTransformEvent.TransformReason.CURED, CreatureSpawnEvent.SpawnReason.CURED);
        if (converted == null) {
            ((ZombieVillager) getBukkitEntity()).setConversionTime(-1); // SPIGOT-5208: End conversion to stop event spam
        }
        // CraftBukkit end
    }

    @VisibleForTesting
    public void setVillagerConversionTime(int i) {
        this.villagerConversionTime = i;
    }

    private int getConversionProgress() {
        int i = 1;

        if (this.random.nextFloat() < 0.01F) {
            int j = 0;
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

            for (int k = (int) this.getX() - 4; k < (int) this.getX() + 4 && j < 14; ++k) {
                for (int l = (int) this.getY() - 4; l < (int) this.getY() + 4 && j < 14; ++l) {
                    for (int i1 = (int) this.getZ() - 4; i1 < (int) this.getZ() + 4 && j < 14; ++i1) {
                        IBlockData iblockdata = this.level().getBlockState(blockposition_mutableblockposition.set(k, l, i1));

                        if (iblockdata.is(Blocks.IRON_BARS) || iblockdata.getBlock() instanceof BlockBed) {
                            if (this.random.nextFloat() < 0.3F) {
                                ++i;
                            }

                            ++j;
                        }
                    }
                }
            }
        }

        return i;
    }

    @Override
    public float getVoicePitch() {
        return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.0F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    public SoundEffect getAmbientSound() {
        return SoundEffects.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    public SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.ZOMBIE_VILLAGER_HURT;
    }

    @Override
    public SoundEffect getDeathSound() {
        return SoundEffects.ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    public SoundEffect getStepSound() {
        return SoundEffects.ZOMBIE_VILLAGER_STEP;
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    public void setTradeOffers(MerchantRecipeList merchantrecipelist) {
        this.tradeOffers = merchantrecipelist;
    }

    public void setGossips(NBTBase nbtbase) {
        this.gossips = nbtbase;
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(worldaccess.getBiome(this.blockPosition()))));
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    @Override
    public void setVillagerData(VillagerData villagerdata) {
        VillagerData villagerdata1 = this.getVillagerData();

        if (villagerdata1.getProfession() != villagerdata.getProfession()) {
            this.tradeOffers = null;
        }

        this.entityData.set(EntityZombieVillager.DATA_VILLAGER_DATA, villagerdata);
    }

    @Override
    public VillagerData getVillagerData() {
        return (VillagerData) this.entityData.get(EntityZombieVillager.DATA_VILLAGER_DATA);
    }

    public int getVillagerXp() {
        return this.villagerXp;
    }

    public void setVillagerXp(int i) {
        this.villagerXp = i;
    }
}
