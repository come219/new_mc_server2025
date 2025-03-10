package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockFlowerPot extends Block {

    public static final MapCodec<BlockFlowerPot> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("potted").forGetter((blockflowerpot) -> {
            return blockflowerpot.potted;
        }), propertiesCodec()).apply(instance, BlockFlowerPot::new);
    });
    private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.newHashMap();
    public static final float AABB_SIZE = 3.0F;
    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);
    private final Block potted;

    @Override
    public MapCodec<BlockFlowerPot> codec() {
        return BlockFlowerPot.CODEC;
    }

    public BlockFlowerPot(Block block, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.potted = block;
        BlockFlowerPot.POTTED_BY_CONTENT.put(block, this);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockFlowerPot.SHAPE;
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        Item item = itemstack.getItem();
        Block block;

        if (item instanceof ItemBlock itemblock) {
            block = (Block) BlockFlowerPot.POTTED_BY_CONTENT.getOrDefault(itemblock.getBlock(), Blocks.AIR);
        } else {
            block = Blocks.AIR;
        }

        IBlockData iblockdata1 = block.defaultBlockState();

        if (iblockdata1.isAir()) {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        } else if (!this.isEmpty()) {
            return EnumInteractionResult.CONSUME;
        } else {
            world.setBlock(blockposition, iblockdata1, 3);
            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.BLOCK_CHANGE, blockposition);
            entityhuman.awardStat(StatisticList.POT_FLOWER);
            itemstack.consume(1, entityhuman);
            return EnumInteractionResult.SUCCESS;
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (this.isEmpty()) {
            return EnumInteractionResult.CONSUME;
        } else {
            ItemStack itemstack = new ItemStack(this.potted);

            if (!entityhuman.addItem(itemstack)) {
                entityhuman.drop(itemstack, false);
            }

            world.setBlock(blockposition, Blocks.FLOWER_POT.defaultBlockState(), 3);
            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.BLOCK_CHANGE, blockposition);
            return EnumInteractionResult.SUCCESS;
        }
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return this.isEmpty() ? super.getCloneItemStack(iworldreader, blockposition, iblockdata, flag) : new ItemStack(this.potted);
    }

    private boolean isEmpty() {
        return this.potted == Blocks.AIR;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return enumdirection == EnumDirection.DOWN && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    public Block getPotted() {
        return this.potted;
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return iblockdata.is(Blocks.POTTED_OPEN_EYEBLOSSOM) || iblockdata.is(Blocks.POTTED_CLOSED_EYEBLOSSOM);
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (this.isRandomlyTicking(iblockdata) && worldserver.dimensionType().natural()) {
            boolean flag = this.potted == Blocks.OPEN_EYEBLOSSOM;
            boolean flag1 = CreakingHeartBlock.isNaturalNight(worldserver);

            if (flag != flag1) {
                worldserver.setBlock(blockposition, this.opposite(iblockdata), 3);
                EyeblossomBlock.a eyeblossomblock_a = EyeblossomBlock.a.fromBoolean(flag).transform();

                eyeblossomblock_a.spawnTransformParticle(worldserver, blockposition, randomsource);
                worldserver.playSound((EntityHuman) null, blockposition, eyeblossomblock_a.longSwitchSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }

        super.randomTick(iblockdata, worldserver, blockposition, randomsource);
    }

    public IBlockData opposite(IBlockData iblockdata) {
        return iblockdata.is(Blocks.POTTED_OPEN_EYEBLOSSOM) ? Blocks.POTTED_CLOSED_EYEBLOSSOM.defaultBlockState() : (iblockdata.is(Blocks.POTTED_CLOSED_EYEBLOSSOM) ? Blocks.POTTED_OPEN_EYEBLOSSOM.defaultBlockState() : iblockdata);
    }
}
