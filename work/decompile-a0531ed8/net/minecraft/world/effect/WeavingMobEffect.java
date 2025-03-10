package net.minecraft.world.effect;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;

class WeavingMobEffect extends MobEffectList {

    private final ToIntFunction<RandomSource> maxCobwebs;

    protected WeavingMobEffect(MobEffectInfo mobeffectinfo, int i, ToIntFunction<RandomSource> tointfunction) {
        super(mobeffectinfo, i, Particles.ITEM_COBWEB);
        this.maxCobwebs = tointfunction;
    }

    @Override
    public void onMobRemoved(WorldServer worldserver, EntityLiving entityliving, int i, Entity.RemovalReason entity_removalreason) {
        if (entity_removalreason == Entity.RemovalReason.KILLED && (entityliving instanceof EntityHuman || worldserver.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))) {
            this.spawnCobwebsRandomlyAround(worldserver, entityliving.getRandom(), entityliving.blockPosition());
        }

    }

    private void spawnCobwebsRandomlyAround(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition) {
        Set<BlockPosition> set = Sets.newHashSet();
        int i = this.maxCobwebs.applyAsInt(randomsource);
        Iterator iterator = BlockPosition.randomInCube(randomsource, 15, blockposition, 1).iterator();

        BlockPosition blockposition1;

        while (iterator.hasNext()) {
            blockposition1 = (BlockPosition) iterator.next();
            BlockPosition blockposition2 = blockposition1.below();

            if (!set.contains(blockposition1) && worldserver.getBlockState(blockposition1).canBeReplaced() && worldserver.getBlockState(blockposition2).isFaceSturdy(worldserver, blockposition2, EnumDirection.UP)) {
                set.add(blockposition1.immutable());
                if (set.size() >= i) {
                    break;
                }
            }
        }

        iterator = set.iterator();

        while (iterator.hasNext()) {
            blockposition1 = (BlockPosition) iterator.next();
            worldserver.setBlock(blockposition1, Blocks.COBWEB.defaultBlockState(), 3);
            worldserver.levelEvent(3018, blockposition1, 0);
        }

    }
}
