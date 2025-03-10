package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.portal.BlockPortalShape;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import org.slf4j.Logger;

public class BlockPortal extends Block implements Portal {

    public static final MapCodec<BlockPortal> CODEC = simpleCodec(BlockPortal::new);
    public static final BlockStateEnum<EnumDirection.EnumAxis> AXIS = BlockProperties.HORIZONTAL_AXIS;
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final int AABB_OFFSET = 2;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    @Override
    public MapCodec<BlockPortal> codec() {
        return BlockPortal.CODEC;
    }

    public BlockPortal(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockPortal.AXIS, EnumDirection.EnumAxis.X));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        switch ((EnumDirection.EnumAxis) iblockdata.getValue(BlockPortal.AXIS)) {
            case Z:
                return BlockPortal.Z_AXIS_AABB;
            case X:
            default:
                return BlockPortal.X_AXIS_AABB;
        }
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (worldserver.dimensionType().natural() && worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && randomsource.nextInt(2000) < worldserver.getDifficulty().getId()) {
            while (worldserver.getBlockState(blockposition).is((Block) this)) {
                blockposition = blockposition.below();
            }

            if (worldserver.getBlockState(blockposition).isValidSpawn(worldserver, blockposition, EntityTypes.ZOMBIFIED_PIGLIN)) {
                Entity entity = EntityTypes.ZOMBIFIED_PIGLIN.spawn(worldserver, blockposition.above(), EntitySpawnReason.STRUCTURE);

                if (entity != null) {
                    entity.setPortalCooldown();
                    Entity entity1 = entity.getVehicle();

                    if (entity1 != null) {
                        entity1.setPortalCooldown();
                    }
                }
            }
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        EnumDirection.EnumAxis enumdirection_enumaxis = enumdirection.getAxis();
        EnumDirection.EnumAxis enumdirection_enumaxis1 = (EnumDirection.EnumAxis) iblockdata.getValue(BlockPortal.AXIS);
        boolean flag = enumdirection_enumaxis1 != enumdirection_enumaxis && enumdirection_enumaxis.isHorizontal();

        return !flag && !iblockdata1.is((Block) this) && !BlockPortalShape.findAnyShape(iworldreader, blockposition, enumdirection_enumaxis1).isComplete() ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, blockposition);
        }

    }

    @Override
    public int getPortalTransitionTime(WorldServer worldserver, Entity entity) {
        if (entity instanceof EntityHuman entityhuman) {
            return Math.max(0, worldserver.getGameRules().getInt(entityhuman.getAbilities().invulnerable ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public TeleportTransition getPortalDestination(WorldServer worldserver, Entity entity, BlockPosition blockposition) {
        ResourceKey<World> resourcekey = worldserver.dimension() == World.NETHER ? World.OVERWORLD : World.NETHER;
        WorldServer worldserver1 = worldserver.getServer().getLevel(resourcekey);

        if (worldserver1 == null) {
            return null;
        } else {
            boolean flag = worldserver1.dimension() == World.NETHER;
            WorldBorder worldborder = worldserver1.getWorldBorder();
            double d0 = DimensionManager.getTeleportationScale(worldserver.dimensionType(), worldserver1.dimensionType());
            BlockPosition blockposition1 = worldborder.clampToBounds(entity.getX() * d0, entity.getY(), entity.getZ() * d0);

            return this.getExitPortal(worldserver1, entity, blockposition, blockposition1, flag, worldborder);
        }
    }

    @Nullable
    private TeleportTransition getExitPortal(WorldServer worldserver, Entity entity, BlockPosition blockposition, BlockPosition blockposition1, boolean flag, WorldBorder worldborder) {
        Optional<BlockPosition> optional = worldserver.getPortalForcer().findClosestPortalPosition(blockposition1, flag, worldborder);
        BlockUtil.Rectangle blockutil_rectangle;
        TeleportTransition.a teleporttransition_a;

        if (optional.isPresent()) {
            BlockPosition blockposition2 = (BlockPosition) optional.get();
            IBlockData iblockdata = worldserver.getBlockState(blockposition2);

            blockutil_rectangle = BlockUtil.getLargestRectangleAround(blockposition2, (EnumDirection.EnumAxis) iblockdata.getValue(BlockProperties.HORIZONTAL_AXIS), 21, EnumDirection.EnumAxis.Y, 21, (blockposition3) -> {
                return worldserver.getBlockState(blockposition3) == iblockdata;
            });
            teleporttransition_a = TeleportTransition.PLAY_PORTAL_SOUND.then((entity1) -> {
                entity1.placePortalTicket(blockposition2);
            });
        } else {
            EnumDirection.EnumAxis enumdirection_enumaxis = (EnumDirection.EnumAxis) entity.level().getBlockState(blockposition).getOptionalValue(BlockPortal.AXIS).orElse(EnumDirection.EnumAxis.X);
            Optional<BlockUtil.Rectangle> optional1 = worldserver.getPortalForcer().createPortal(blockposition1, enumdirection_enumaxis);

            if (optional1.isEmpty()) {
                BlockPortal.LOGGER.error("Unable to create a portal, likely target out of worldborder");
                return null;
            }

            blockutil_rectangle = (BlockUtil.Rectangle) optional1.get();
            teleporttransition_a = TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET);
        }

        return getDimensionTransitionFromExit(entity, blockposition, blockutil_rectangle, worldserver, teleporttransition_a);
    }

    private static TeleportTransition getDimensionTransitionFromExit(Entity entity, BlockPosition blockposition, BlockUtil.Rectangle blockutil_rectangle, WorldServer worldserver, TeleportTransition.a teleporttransition_a) {
        IBlockData iblockdata = entity.level().getBlockState(blockposition);
        EnumDirection.EnumAxis enumdirection_enumaxis;
        Vec3D vec3d;

        if (iblockdata.hasProperty(BlockProperties.HORIZONTAL_AXIS)) {
            enumdirection_enumaxis = (EnumDirection.EnumAxis) iblockdata.getValue(BlockProperties.HORIZONTAL_AXIS);
            BlockUtil.Rectangle blockutil_rectangle1 = BlockUtil.getLargestRectangleAround(blockposition, enumdirection_enumaxis, 21, EnumDirection.EnumAxis.Y, 21, (blockposition1) -> {
                return entity.level().getBlockState(blockposition1) == iblockdata;
            });

            vec3d = entity.getRelativePortalPosition(enumdirection_enumaxis, blockutil_rectangle1);
        } else {
            enumdirection_enumaxis = EnumDirection.EnumAxis.X;
            vec3d = new Vec3D(0.5D, 0.0D, 0.0D);
        }

        return createDimensionTransition(worldserver, blockutil_rectangle, enumdirection_enumaxis, vec3d, entity, teleporttransition_a);
    }

    private static TeleportTransition createDimensionTransition(WorldServer worldserver, BlockUtil.Rectangle blockutil_rectangle, EnumDirection.EnumAxis enumdirection_enumaxis, Vec3D vec3d, Entity entity, TeleportTransition.a teleporttransition_a) {
        BlockPosition blockposition = blockutil_rectangle.minCorner;
        IBlockData iblockdata = worldserver.getBlockState(blockposition);
        EnumDirection.EnumAxis enumdirection_enumaxis1 = (EnumDirection.EnumAxis) iblockdata.getOptionalValue(BlockProperties.HORIZONTAL_AXIS).orElse(EnumDirection.EnumAxis.X);
        double d0 = (double) blockutil_rectangle.axis1Size;
        double d1 = (double) blockutil_rectangle.axis2Size;
        EntitySize entitysize = entity.getDimensions(entity.getPose());
        int i = enumdirection_enumaxis == enumdirection_enumaxis1 ? 0 : 90;
        double d2 = (double) entitysize.width() / 2.0D + (d0 - (double) entitysize.width()) * vec3d.x();
        double d3 = (d1 - (double) entitysize.height()) * vec3d.y();
        double d4 = 0.5D + vec3d.z();
        boolean flag = enumdirection_enumaxis1 == EnumDirection.EnumAxis.X;
        Vec3D vec3d1 = new Vec3D((double) blockposition.getX() + (flag ? d2 : d4), (double) blockposition.getY() + d3, (double) blockposition.getZ() + (flag ? d4 : d2));
        Vec3D vec3d2 = BlockPortalShape.findCollisionFreePosition(vec3d1, worldserver, entity, entitysize);

        return new TeleportTransition(worldserver, vec3d2, Vec3D.ZERO, (float) i, 0.0F, Relative.union(Relative.DELTA, Relative.ROTATION), teleporttransition_a);
    }

    @Override
    public Portal.a getLocalTransition() {
        return Portal.a.CONFUSION;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(100) == 0) {
            world.playLocalSound((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, SoundEffects.PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, randomsource.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < 4; ++i) {
            double d0 = (double) blockposition.getX() + randomsource.nextDouble();
            double d1 = (double) blockposition.getY() + randomsource.nextDouble();
            double d2 = (double) blockposition.getZ() + randomsource.nextDouble();
            double d3 = ((double) randomsource.nextFloat() - 0.5D) * 0.5D;
            double d4 = ((double) randomsource.nextFloat() - 0.5D) * 0.5D;
            double d5 = ((double) randomsource.nextFloat() - 0.5D) * 0.5D;
            int j = randomsource.nextInt(2) * 2 - 1;

            if (!world.getBlockState(blockposition.west()).is((Block) this) && !world.getBlockState(blockposition.east()).is((Block) this)) {
                d0 = (double) blockposition.getX() + 0.5D + 0.25D * (double) j;
                d3 = (double) (randomsource.nextFloat() * 2.0F * (float) j);
            } else {
                d2 = (double) blockposition.getZ() + 0.5D + 0.25D * (double) j;
                d5 = (double) (randomsource.nextFloat() * 2.0F * (float) j);
            }

            world.addParticle(Particles.PORTAL, d0, d1, d2, d3, d4, d5);
        }

    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return ItemStack.EMPTY;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (enumblockrotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch ((EnumDirection.EnumAxis) iblockdata.getValue(BlockPortal.AXIS)) {
                    case Z:
                        return (IBlockData) iblockdata.setValue(BlockPortal.AXIS, EnumDirection.EnumAxis.X);
                    case X:
                        return (IBlockData) iblockdata.setValue(BlockPortal.AXIS, EnumDirection.EnumAxis.Z);
                    default:
                        return iblockdata;
                }
            default:
                return iblockdata;
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPortal.AXIS);
    }
}
