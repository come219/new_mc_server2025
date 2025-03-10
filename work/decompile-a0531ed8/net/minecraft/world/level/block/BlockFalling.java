package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public abstract class BlockFalling extends Block implements Fallable {

    public BlockFalling(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected abstract MapCodec<? extends BlockFalling> codec();

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        world.scheduleTick(blockposition, (Block) this, this.getDelayAfterPlace());
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        scheduledtickaccess.scheduleTick(blockposition, (Block) this, this.getDelayAfterPlace());
        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (isFree(worldserver.getBlockState(blockposition.below())) && blockposition.getY() >= worldserver.getMinY()) {
            EntityFallingBlock entityfallingblock = EntityFallingBlock.fall(worldserver, blockposition, iblockdata);

            this.falling(entityfallingblock);
        }
    }

    protected void falling(EntityFallingBlock entityfallingblock) {}

    protected int getDelayAfterPlace() {
        return 2;
    }

    public static boolean isFree(IBlockData iblockdata) {
        return iblockdata.isAir() || iblockdata.is(TagsBlock.FIRE) || iblockdata.liquid() || iblockdata.canBeReplaced();
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(16) == 0) {
            BlockPosition blockposition1 = blockposition.below();

            if (isFree(world.getBlockState(blockposition1))) {
                ParticleUtils.spawnParticleBelow(world, blockposition, randomsource, new ParticleParamBlock(Particles.FALLING_DUST, iblockdata));
            }
        }

    }

    public int getDustColor(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return -16777216;
    }
}
