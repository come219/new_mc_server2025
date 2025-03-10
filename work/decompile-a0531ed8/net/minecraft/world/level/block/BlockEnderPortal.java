package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityEnderPortal;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockEnderPortal extends BlockTileEntity implements Portal {

    public static final MapCodec<BlockEnderPortal> CODEC = simpleCodec(BlockEnderPortal::new);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    @Override
    public MapCodec<BlockEnderPortal> codec() {
        return BlockEnderPortal.CODEC;
    }

    protected BlockEnderPortal(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityEnderPortal(blockposition, iblockdata);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockEnderPortal.SHAPE;
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return iblockdata.getShape(world, blockposition);
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (entity.canUsePortal(false)) {
            if (!world.isClientSide && world.dimension() == World.END && entity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entity;

                if (!entityplayer.seenCredits) {
                    entityplayer.showEndCredits();
                    return;
                }
            }

            entity.setAsInsidePortal(this, blockposition);
        }

    }

    @Override
    public TeleportTransition getPortalDestination(WorldServer worldserver, Entity entity, BlockPosition blockposition) {
        ResourceKey<World> resourcekey = worldserver.dimension() == World.END ? World.OVERWORLD : World.END;
        WorldServer worldserver1 = worldserver.getServer().getLevel(resourcekey);

        if (worldserver1 == null) {
            return null;
        } else {
            boolean flag = resourcekey == World.END;
            BlockPosition blockposition1 = flag ? WorldServer.END_SPAWN_POINT : worldserver1.getSharedSpawnPos();
            Vec3D vec3d = blockposition1.getBottomCenter();
            float f;
            Set set;

            if (flag) {
                EndPlatformFeature.createEndPlatform(worldserver1, BlockPosition.containing(vec3d).below(), true);
                f = EnumDirection.WEST.toYRot();
                set = Relative.union(Relative.DELTA, Set.of(Relative.X_ROT));
                if (entity instanceof EntityPlayer) {
                    vec3d = vec3d.subtract(0.0D, 1.0D, 0.0D);
                }
            } else {
                f = 0.0F;
                set = Relative.union(Relative.DELTA, Relative.ROTATION);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityplayer = (EntityPlayer) entity;

                    return entityplayer.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
                }

                vec3d = entity.adjustSpawnLocation(worldserver1, blockposition1).getBottomCenter();
            }

            return new TeleportTransition(worldserver1, vec3d, Vec3D.ZERO, f, 0.0F, set, TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
        }
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        double d0 = (double) blockposition.getX() + randomsource.nextDouble();
        double d1 = (double) blockposition.getY() + 0.8D;
        double d2 = (double) blockposition.getZ() + randomsource.nextDouble();

        world.addParticle(Particles.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBeReplaced(IBlockData iblockdata, FluidType fluidtype) {
        return false;
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.INVISIBLE;
    }
}
