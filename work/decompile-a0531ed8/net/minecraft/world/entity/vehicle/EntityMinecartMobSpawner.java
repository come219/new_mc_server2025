package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public class EntityMinecartMobSpawner extends EntityMinecartAbstract {

    private final MobSpawnerAbstract spawner = new MobSpawnerAbstract() {
        @Override
        public void broadcastEvent(World world, BlockPosition blockposition, int i) {
            world.broadcastEntityEvent(EntityMinecartMobSpawner.this, (byte) i);
        }
    };
    private final Runnable ticker;

    public EntityMinecartMobSpawner(EntityTypes<? extends EntityMinecartMobSpawner> entitytypes, World world) {
        super(entitytypes, world);
        this.ticker = this.createTicker(world);
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
    }

    private Runnable createTicker(World world) {
        return world instanceof WorldServer ? () -> {
            this.spawner.serverTick((WorldServer) world, this.blockPosition());
        } : () -> {
            this.spawner.clientTick(world, this.blockPosition());
        };
    }

    @Override
    public IBlockData getDefaultDisplayBlockState() {
        return Blocks.SPAWNER.defaultBlockState();
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.spawner.load(this.level(), this.blockPosition(), nbttagcompound);
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        this.spawner.save(nbttagcompound);
    }

    @Override
    public void handleEntityEvent(byte b0) {
        this.spawner.onEventTriggered(this.level(), b0);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticker.run();
    }

    public MobSpawnerAbstract getSpawner() {
        return this.spawner;
    }
}
