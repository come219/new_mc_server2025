package net.minecraft.world.entity.vehicle;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFurnaceFurace;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public class EntityMinecartFurnace extends EntityMinecartAbstract {

    private static final DataWatcherObject<Boolean> DATA_ID_FUEL = DataWatcher.defineId(EntityMinecartFurnace.class, DataWatcherRegistry.BOOLEAN);
    private static final int FUEL_TICKS_PER_ITEM = 3600;
    private static final int MAX_FUEL_TICKS = 32000;
    public int fuel;
    public Vec3D push;

    public EntityMinecartFurnace(EntityTypes<? extends EntityMinecartFurnace> entitytypes, World world) {
        super(entitytypes, world);
        this.push = Vec3D.ZERO;
    }

    @Override
    public boolean isFurnace() {
        return true;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityMinecartFurnace.DATA_ID_FUEL, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.fuel > 0) {
                --this.fuel;
            }

            if (this.fuel <= 0) {
                this.push = Vec3D.ZERO;
            }

            this.setHasFuel(this.fuel > 0);
        }

        if (this.hasFuel() && this.random.nextInt(4) == 0) {
            this.level().addParticle(Particles.LARGE_SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(), 0.0D, 0.0D, 0.0D);
        }

    }

    @Override
    protected double getMaxSpeed(WorldServer worldserver) {
        return this.isInWater() ? super.getMaxSpeed(worldserver) * 0.75D : super.getMaxSpeed(worldserver) * 0.5D;
    }

    @Override
    protected Item getDropItem() {
        return Items.FURNACE_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.FURNACE_MINECART);
    }

    @Override
    protected Vec3D applyNaturalSlowdown(Vec3D vec3d) {
        Vec3D vec3d1;

        if (this.push.lengthSqr() > 1.0E-7D) {
            this.push = this.calculateNewPushAlong(vec3d);
            vec3d1 = vec3d.multiply(0.8D, 0.0D, 0.8D).add(this.push);
            if (this.isInWater()) {
                vec3d1 = vec3d1.scale(0.1D);
            }
        } else {
            vec3d1 = vec3d.multiply(0.98D, 0.0D, 0.98D);
        }

        return super.applyNaturalSlowdown(vec3d1);
    }

    private Vec3D calculateNewPushAlong(Vec3D vec3d) {
        double d0 = 1.0E-4D;
        double d1 = 0.001D;

        return this.push.horizontalDistanceSqr() > 1.0E-4D && vec3d.horizontalDistanceSqr() > 0.001D ? this.push.projectedOn(vec3d).normalize().scale(this.push.length()) : this.push;
    }

    @Override
    public EnumInteractionResult interact(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (itemstack.is(TagsItem.FURNACE_MINECART_FUEL) && this.fuel + 3600 <= 32000) {
            itemstack.consume(1, entityhuman);
            this.fuel += 3600;
        }

        if (this.fuel > 0) {
            this.push = this.position().subtract(entityhuman.position()).horizontal();
        }

        return EnumInteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putDouble("PushX", this.push.x);
        nbttagcompound.putDouble("PushZ", this.push.z);
        nbttagcompound.putShort("Fuel", (short) this.fuel);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        double d0 = nbttagcompound.getDouble("PushX");
        double d1 = nbttagcompound.getDouble("PushZ");

        this.push = new Vec3D(d0, 0.0D, d1);
        this.fuel = nbttagcompound.getShort("Fuel");
    }

    protected boolean hasFuel() {
        return (Boolean) this.entityData.get(EntityMinecartFurnace.DATA_ID_FUEL);
    }

    protected void setHasFuel(boolean flag) {
        this.entityData.set(EntityMinecartFurnace.DATA_ID_FUEL, flag);
    }

    @Override
    public IBlockData getDefaultDisplayBlockState() {
        return (IBlockData) ((IBlockData) Blocks.FURNACE.defaultBlockState().setValue(BlockFurnaceFurace.FACING, EnumDirection.NORTH)).setValue(BlockFurnaceFurace.LIT, this.hasFuel());
    }
}
