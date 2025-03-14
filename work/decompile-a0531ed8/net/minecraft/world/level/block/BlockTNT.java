package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockTNT extends Block {

    public static final MapCodec<BlockTNT> CODEC = simpleCodec(BlockTNT::new);
    public static final BlockStateBoolean UNSTABLE = BlockProperties.UNSTABLE;

    @Override
    public MapCodec<BlockTNT> codec() {
        return BlockTNT.CODEC;
    }

    public BlockTNT(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(BlockTNT.UNSTABLE, false));
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata1.is(iblockdata.getBlock())) {
            if (world.hasNeighborSignal(blockposition)) {
                explode(world, blockposition);
                world.removeBlock(blockposition, false);
            }

        }
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (world.hasNeighborSignal(blockposition)) {
            explode(world, blockposition);
            world.removeBlock(blockposition, false);
        }

    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (!world.isClientSide() && !entityhuman.isCreative() && (Boolean) iblockdata.getValue(BlockTNT.UNSTABLE)) {
            explode(world, blockposition);
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    @Override
    public void wasExploded(WorldServer worldserver, BlockPosition blockposition, Explosion explosion) {
        EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(worldserver, (double) blockposition.getX() + 0.5D, (double) blockposition.getY(), (double) blockposition.getZ() + 0.5D, explosion.getIndirectSourceEntity());
        int i = entitytntprimed.getFuse();

        entitytntprimed.setFuse((short) (worldserver.random.nextInt(i / 4) + i / 8));
        worldserver.addFreshEntity(entitytntprimed);
    }

    public static void explode(World world, BlockPosition blockposition) {
        explode(world, blockposition, (EntityLiving) null);
    }

    private static void explode(World world, BlockPosition blockposition, @Nullable EntityLiving entityliving) {
        if (!world.isClientSide) {
            EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, (double) blockposition.getX() + 0.5D, (double) blockposition.getY(), (double) blockposition.getZ() + 0.5D, entityliving);

            world.addFreshEntity(entitytntprimed);
            world.playSound((EntityHuman) null, entitytntprimed.getX(), entitytntprimed.getY(), entitytntprimed.getZ(), SoundEffects.TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.gameEvent((Entity) entityliving, (Holder) GameEvent.PRIME_FUSE, blockposition);
        }
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!itemstack.is(Items.FLINT_AND_STEEL) && !itemstack.is(Items.FIRE_CHARGE)) {
            return super.useItemOn(itemstack, iblockdata, world, blockposition, entityhuman, enumhand, movingobjectpositionblock);
        } else {
            explode(world, blockposition, entityhuman);
            world.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 11);
            Item item = itemstack.getItem();

            if (itemstack.is(Items.FLINT_AND_STEEL)) {
                itemstack.hurtAndBreak(1, entityhuman, EntityLiving.getSlotForHand(enumhand));
            } else {
                itemstack.consume(1, entityhuman);
            }

            entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
            return EnumInteractionResult.SUCCESS;
        }
    }

    @Override
    protected void onProjectileHit(World world, IBlockData iblockdata, MovingObjectPositionBlock movingobjectpositionblock, IProjectile iprojectile) {
        if (world instanceof WorldServer worldserver) {
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
            Entity entity = iprojectile.getOwner();

            if (iprojectile.isOnFire() && iprojectile.mayInteract(worldserver, blockposition)) {
                explode(world, blockposition, entity instanceof EntityLiving ? (EntityLiving) entity : null);
                world.removeBlock(blockposition, false);
            }
        }

    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockTNT.UNSTABLE);
    }
}
