package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public class EntityMinecartTNT extends EntityMinecartAbstract {

    private static final byte EVENT_PRIME = 10;
    private static final String TAG_EXPLOSION_POWER = "explosion_power";
    private static final String TAG_EXPLOSION_SPEED_FACTOR = "explosion_speed_factor";
    private static final String TAG_FUSE = "fuse";
    private static final float DEFAULT_EXPLOSION_POWER_BASE = 4.0F;
    private static final float DEFAULT_EXPLOSION_SPEED_FACTOR = 1.0F;
    public int fuse = -1;
    public float explosionPowerBase = 4.0F;
    public float explosionSpeedFactor = 1.0F;

    public EntityMinecartTNT(EntityTypes<? extends EntityMinecartTNT> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    public IBlockData getDefaultDisplayBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.fuse > 0) {
            --this.fuse;
            this.level().addParticle(Particles.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
        } else if (this.fuse == 0) {
            this.explode(this.getDeltaMovement().horizontalDistanceSqr());
        }

        if (this.horizontalCollision) {
            double d0 = this.getDeltaMovement().horizontalDistanceSqr();

            if (d0 >= 0.009999999776482582D) {
                this.explode(d0);
            }
        }

    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        Entity entity = damagesource.getDirectEntity();

        if (entity instanceof IProjectile iprojectile) {
            if (iprojectile.isOnFire()) {
                DamageSource damagesource1 = this.damageSources().explosion(this, damagesource.getEntity());

                this.explode(damagesource1, iprojectile.getDeltaMovement().lengthSqr());
            }
        }

        return super.hurtServer(worldserver, damagesource, f);
    }

    @Override
    public void destroy(WorldServer worldserver, DamageSource damagesource) {
        double d0 = this.getDeltaMovement().horizontalDistanceSqr();

        if (!damageSourceIgnitesTnt(damagesource) && d0 < 0.009999999776482582D) {
            this.destroy(worldserver, this.getDropItem());
        } else {
            if (this.fuse < 0) {
                this.primeFuse();
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }

        }
    }

    @Override
    protected Item getDropItem() {
        return Items.TNT_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.TNT_MINECART);
    }

    public void explode(double d0) {
        this.explode((DamageSource) null, d0);
    }

    protected void explode(@Nullable DamageSource damagesource, double d0) {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            double d1 = Math.min(Math.sqrt(d0), 5.0D);

            worldserver.explode(this, damagesource, (ExplosionDamageCalculator) null, this.getX(), this.getY(), this.getZ(), (float) ((double) this.explosionPowerBase + (double) this.explosionSpeedFactor * this.random.nextDouble() * 1.5D * d1), false, World.a.TNT);
            this.discard();
        }

    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
        if (f >= 3.0F) {
            float f2 = f / 10.0F;

            this.explode((double) (f2 * f2));
        }

        return super.causeFallDamage(f, f1, damagesource);
    }

    @Override
    public void activateMinecart(int i, int j, int k, boolean flag) {
        if (flag && this.fuse < 0) {
            this.primeFuse();
        }

    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 10) {
            this.primeFuse();
        } else {
            super.handleEntityEvent(b0);
        }

    }

    public void primeFuse() {
        this.fuse = 80;
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 10);
            if (!this.isSilent()) {
                this.level().playSound((EntityHuman) null, this.getX(), this.getY(), this.getZ(), SoundEffects.TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }

    }

    public int getFuse() {
        return this.fuse;
    }

    public boolean isPrimed() {
        return this.fuse > -1;
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid, float f) {
        return this.isPrimed() && (iblockdata.is(TagsBlock.RAILS) || iblockaccess.getBlockState(blockposition.above()).is(TagsBlock.RAILS)) ? 0.0F : super.getBlockExplosionResistance(explosion, iblockaccess, blockposition, iblockdata, fluid, f);
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, float f) {
        return this.isPrimed() && (iblockdata.is(TagsBlock.RAILS) || iblockaccess.getBlockState(blockposition.above()).is(TagsBlock.RAILS)) ? false : super.shouldBlockExplode(explosion, iblockaccess, blockposition, iblockdata, f);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        if (nbttagcompound.contains("fuse", 99)) {
            this.fuse = nbttagcompound.getInt("fuse");
        }

        if (nbttagcompound.contains("explosion_power", 99)) {
            this.explosionPowerBase = MathHelper.clamp(nbttagcompound.getFloat("explosion_power"), 0.0F, 128.0F);
        }

        if (nbttagcompound.contains("explosion_speed_factor", 99)) {
            this.explosionSpeedFactor = MathHelper.clamp(nbttagcompound.getFloat("explosion_speed_factor"), 0.0F, 128.0F);
        }

    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putInt("fuse", this.fuse);
        if (this.explosionPowerBase != 4.0F) {
            nbttagcompound.putFloat("explosion_power", this.explosionPowerBase);
        }

        if (this.explosionSpeedFactor != 1.0F) {
            nbttagcompound.putFloat("explosion_speed_factor", this.explosionSpeedFactor);
        }

    }

    @Override
    boolean shouldSourceDestroy(DamageSource damagesource) {
        return damageSourceIgnitesTnt(damagesource);
    }

    private static boolean damageSourceIgnitesTnt(DamageSource damagesource) {
        return damagesource.is(DamageTypeTags.IS_FIRE) || damagesource.is(DamageTypeTags.IS_EXPLOSION);
    }
}
