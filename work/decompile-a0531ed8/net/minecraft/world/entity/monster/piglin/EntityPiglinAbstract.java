package net.minecraft.world.entity.monster.piglin;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.util.PathfinderGoalUtil;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.level.World;
import net.minecraft.world.level.pathfinder.PathType;

public abstract class EntityPiglinAbstract extends EntityMonster {

    protected static final DataWatcherObject<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = DataWatcher.defineId(EntityPiglinAbstract.class, DataWatcherRegistry.BOOLEAN);
    public static final int CONVERSION_TIME = 300;
    public int timeInOverworld;

    public EntityPiglinAbstract(EntityTypes<? extends EntityPiglinAbstract> entitytypes, World world) {
        super(entitytypes, world);
        this.setCanPickUpLoot(true);
        this.applyOpenDoorsAbility();
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
    }

    private void applyOpenDoorsAbility() {
        if (PathfinderGoalUtil.hasGroundPathNavigation(this)) {
            ((Navigation) this.getNavigation()).setCanOpenDoors(true);
        }

    }

    protected abstract boolean canHunt();

    public void setImmuneToZombification(boolean flag) {
        this.getEntityData().set(EntityPiglinAbstract.DATA_IMMUNE_TO_ZOMBIFICATION, flag);
    }

    public boolean isImmuneToZombification() {
        return (Boolean) this.getEntityData().get(EntityPiglinAbstract.DATA_IMMUNE_TO_ZOMBIFICATION);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityPiglinAbstract.DATA_IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        if (this.isImmuneToZombification()) {
            nbttagcompound.putBoolean("IsImmuneToZombification", true);
        }

        nbttagcompound.putInt("TimeInOverworld", this.timeInOverworld);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setImmuneToZombification(nbttagcompound.getBoolean("IsImmuneToZombification"));
        this.timeInOverworld = nbttagcompound.getInt("TimeInOverworld");
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        super.customServerAiStep(worldserver);
        if (this.isConverting()) {
            ++this.timeInOverworld;
        } else {
            this.timeInOverworld = 0;
        }

        if (this.timeInOverworld > 300) {
            this.playConvertedSound();
            this.finishConversion(worldserver);
        }

    }

    @VisibleForTesting
    public void setTimeInOverworld(int i) {
        this.timeInOverworld = i;
    }

    public boolean isConverting() {
        return !this.level().dimensionType().piglinSafe() && !this.isImmuneToZombification() && !this.isNoAi();
    }

    protected void finishConversion(WorldServer worldserver) {
        this.convertTo(EntityTypes.ZOMBIFIED_PIGLIN, ConversionParams.single(this, true, true), (entitypigzombie) -> {
            entitypigzombie.addEffect(new MobEffect(MobEffects.CONFUSION, 200, 0));
        });
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    public abstract EntityPiglinArmPose getArmPose();

    @Nullable
    @Override
    public EntityLiving getTarget() {
        return this.getTargetFromBrain();
    }

    protected boolean isHoldingMeleeWeapon() {
        return this.getMainHandItem().has(DataComponents.TOOL);
    }

    @Override
    public void playAmbientSound() {
        if (PiglinAI.isIdle(this)) {
            super.playAmbientSound();
        }

    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
    }

    protected abstract void playConvertedSound();
}
