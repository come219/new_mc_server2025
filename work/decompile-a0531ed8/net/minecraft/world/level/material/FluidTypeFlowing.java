package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockDoor;
import net.minecraft.world.level.block.BlockIce;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IFluidContainer;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public abstract class FluidTypeFlowing extends FluidType {

    public static final BlockStateBoolean FALLING = BlockProperties.FALLING;
    public static final BlockStateInteger LEVEL = BlockProperties.LEVEL_FLOWING;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<FluidTypeFlowing.a>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<FluidTypeFlowing.a> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<FluidTypeFlowing.a>(200) {
            protected void rehash(int i) {}
        };

        object2bytelinkedopenhashmap.defaultReturnValue((byte) 127);
        return object2bytelinkedopenhashmap;
    });
    private final Map<Fluid, VoxelShape> shapes = Maps.newIdentityHashMap();

    public FluidTypeFlowing() {}

    @Override
    protected void createFluidStateDefinition(BlockStateList.a<FluidType, Fluid> blockstatelist_a) {
        blockstatelist_a.add(FluidTypeFlowing.FALLING);
    }

    @Override
    public Vec3D getFlow(IBlockAccess iblockaccess, BlockPosition blockposition, Fluid fluid) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();

            blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection);
            Fluid fluid1 = iblockaccess.getFluidState(blockposition_mutableblockposition);

            if (this.affectsFlow(fluid1)) {
                float f = fluid1.getOwnHeight();
                float f1 = 0.0F;

                if (f == 0.0F) {
                    if (!iblockaccess.getBlockState(blockposition_mutableblockposition).blocksMotion()) {
                        BlockPosition blockposition1 = blockposition_mutableblockposition.below();
                        Fluid fluid2 = iblockaccess.getFluidState(blockposition1);

                        if (this.affectsFlow(fluid2)) {
                            f = fluid2.getOwnHeight();
                            if (f > 0.0F) {
                                f1 = fluid.getOwnHeight() - (f - 0.8888889F);
                            }
                        }
                    }
                } else if (f > 0.0F) {
                    f1 = fluid.getOwnHeight() - f;
                }

                if (f1 != 0.0F) {
                    d0 += (double) ((float) enumdirection.getStepX() * f1);
                    d1 += (double) ((float) enumdirection.getStepZ() * f1);
                }
            }
        }

        Vec3D vec3d = new Vec3D(d0, 0.0D, d1);

        if ((Boolean) fluid.getValue(FluidTypeFlowing.FALLING)) {
            Iterator iterator1 = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

            while (iterator1.hasNext()) {
                EnumDirection enumdirection1 = (EnumDirection) iterator1.next();

                blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection1);
                if (this.isSolidFace(iblockaccess, blockposition_mutableblockposition, enumdirection1) || this.isSolidFace(iblockaccess, blockposition_mutableblockposition.above(), enumdirection1)) {
                    vec3d = vec3d.normalize().add(0.0D, -6.0D, 0.0D);
                    break;
                }
            }
        }

        return vec3d.normalize();
    }

    private boolean affectsFlow(Fluid fluid) {
        return fluid.isEmpty() || fluid.getType().isSame(this);
    }

    protected boolean isSolidFace(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition);
        Fluid fluid = iblockaccess.getFluidState(blockposition);

        return fluid.getType().isSame(this) ? false : (enumdirection == EnumDirection.UP ? true : (iblockdata.getBlock() instanceof BlockIce ? false : iblockdata.isFaceSturdy(iblockaccess, blockposition, enumdirection)));
    }

    protected void spread(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
        if (!fluid.isEmpty()) {
            BlockPosition blockposition1 = blockposition.below();
            IBlockData iblockdata1 = worldserver.getBlockState(blockposition1);
            Fluid fluid1 = iblockdata1.getFluidState();

            if (this.canMaybePassThrough(worldserver, blockposition, iblockdata, EnumDirection.DOWN, blockposition1, iblockdata1, fluid1)) {
                Fluid fluid2 = this.getNewLiquid(worldserver, blockposition1, iblockdata1);
                FluidType fluidtype = fluid2.getType();

                if (fluid1.canBeReplacedWith(worldserver, blockposition1, fluidtype, EnumDirection.DOWN) && canHoldSpecificFluid(worldserver, blockposition1, iblockdata1, fluidtype)) {
                    this.spreadTo(worldserver, blockposition1, iblockdata1, EnumDirection.DOWN, fluid2);
                    if (this.sourceNeighborCount(worldserver, blockposition) >= 3) {
                        this.spreadToSides(worldserver, blockposition, fluid, iblockdata);
                    }

                    return;
                }
            }

            if (fluid.isSource() || !this.isWaterHole(worldserver, blockposition, iblockdata, blockposition1, iblockdata1)) {
                this.spreadToSides(worldserver, blockposition, fluid, iblockdata);
            }

        }
    }

    private void spreadToSides(WorldServer worldserver, BlockPosition blockposition, Fluid fluid, IBlockData iblockdata) {
        int i = fluid.getAmount() - this.getDropOff(worldserver);

        if ((Boolean) fluid.getValue(FluidTypeFlowing.FALLING)) {
            i = 7;
        }

        if (i > 0) {
            Map<EnumDirection, Fluid> map = this.getSpread(worldserver, blockposition, iblockdata);
            Iterator iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<EnumDirection, Fluid> entry = (Entry) iterator.next();
                EnumDirection enumdirection = (EnumDirection) entry.getKey();
                Fluid fluid1 = (Fluid) entry.getValue();
                BlockPosition blockposition1 = blockposition.relative(enumdirection);

                this.spreadTo(worldserver, blockposition1, worldserver.getBlockState(blockposition1), enumdirection, fluid1);
            }

        }
    }

    protected Fluid getNewLiquid(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata) {
        int i = 0;
        int j = 0;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection);
            IBlockData iblockdata1 = worldserver.getBlockState(blockposition_mutableblockposition1);
            Fluid fluid = iblockdata1.getFluidState();

            if (fluid.getType().isSame(this) && canPassThroughWall(enumdirection, worldserver, blockposition, iblockdata, blockposition_mutableblockposition1, iblockdata1)) {
                if (fluid.isSource()) {
                    ++j;
                }

                i = Math.max(i, fluid.getAmount());
            }
        }

        if (j >= 2 && this.canConvertToSource(worldserver)) {
            IBlockData iblockdata2 = worldserver.getBlockState(blockposition_mutableblockposition.setWithOffset(blockposition, EnumDirection.DOWN));
            Fluid fluid1 = iblockdata2.getFluidState();

            if (iblockdata2.isSolid() || this.isSourceBlockOfThisType(fluid1)) {
                return this.getSource(false);
            }
        }

        BlockPosition.MutableBlockPosition blockposition_mutableblockposition2 = blockposition_mutableblockposition.setWithOffset(blockposition, EnumDirection.UP);
        IBlockData iblockdata3 = worldserver.getBlockState(blockposition_mutableblockposition2);
        Fluid fluid2 = iblockdata3.getFluidState();

        if (!fluid2.isEmpty() && fluid2.getType().isSame(this) && canPassThroughWall(EnumDirection.UP, worldserver, blockposition, iblockdata, blockposition_mutableblockposition2, iblockdata3)) {
            return this.getFlowing(8, true);
        } else {
            int k = i - this.getDropOff(worldserver);

            return k <= 0 ? FluidTypes.EMPTY.defaultFluidState() : this.getFlowing(k, false);
        }
    }

    private static boolean canPassThroughWall(EnumDirection enumdirection, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, BlockPosition blockposition1, IBlockData iblockdata1) {
        VoxelShape voxelshape = iblockdata1.getCollisionShape(iblockaccess, blockposition1);

        if (voxelshape == VoxelShapes.block()) {
            return false;
        } else {
            VoxelShape voxelshape1 = iblockdata.getCollisionShape(iblockaccess, blockposition);

            if (voxelshape1 == VoxelShapes.block()) {
                return false;
            } else if (voxelshape1 == VoxelShapes.empty() && voxelshape == VoxelShapes.empty()) {
                return true;
            } else {
                Object2ByteLinkedOpenHashMap object2bytelinkedopenhashmap;

                if (!iblockdata.getBlock().hasDynamicShape() && !iblockdata1.getBlock().hasDynamicShape()) {
                    object2bytelinkedopenhashmap = (Object2ByteLinkedOpenHashMap) FluidTypeFlowing.OCCLUSION_CACHE.get();
                } else {
                    object2bytelinkedopenhashmap = null;
                }

                FluidTypeFlowing.a fluidtypeflowing_a;

                if (object2bytelinkedopenhashmap != null) {
                    fluidtypeflowing_a = new FluidTypeFlowing.a(iblockdata, iblockdata1, enumdirection);
                    byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(fluidtypeflowing_a);

                    if (b0 != 127) {
                        return b0 != 0;
                    }
                } else {
                    fluidtypeflowing_a = null;
                }

                boolean flag = !VoxelShapes.mergedFaceOccludes(voxelshape1, voxelshape, enumdirection);

                if (object2bytelinkedopenhashmap != null) {
                    if (object2bytelinkedopenhashmap.size() == 200) {
                        object2bytelinkedopenhashmap.removeLastByte();
                    }

                    object2bytelinkedopenhashmap.putAndMoveToFirst(fluidtypeflowing_a, (byte) (flag ? 1 : 0));
                }

                return flag;
            }
        }
    }

    public abstract FluidType getFlowing();

    public Fluid getFlowing(int i, boolean flag) {
        return (Fluid) ((Fluid) this.getFlowing().defaultFluidState().setValue(FluidTypeFlowing.LEVEL, i)).setValue(FluidTypeFlowing.FALLING, flag);
    }

    public abstract FluidType getSource();

    public Fluid getSource(boolean flag) {
        return (Fluid) this.getSource().defaultFluidState().setValue(FluidTypeFlowing.FALLING, flag);
    }

    protected abstract boolean canConvertToSource(WorldServer worldserver);

    protected void spreadTo(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata, EnumDirection enumdirection, Fluid fluid) {
        Block block = iblockdata.getBlock();

        if (block instanceof IFluidContainer ifluidcontainer) {
            ifluidcontainer.placeLiquid(generatoraccess, blockposition, iblockdata, fluid);
        } else {
            if (!iblockdata.isAir()) {
                this.beforeDestroyingBlock(generatoraccess, blockposition, iblockdata);
            }

            generatoraccess.setBlock(blockposition, fluid.createLegacyBlock(), 3);
        }

    }

    protected abstract void beforeDestroyingBlock(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata);

    protected int getSlopeDistance(IWorldReader iworldreader, BlockPosition blockposition, int i, EnumDirection enumdirection, IBlockData iblockdata, FluidTypeFlowing.b fluidtypeflowing_b) {
        int j = 1000;
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection1 = (EnumDirection) iterator.next();

            if (enumdirection1 != enumdirection) {
                BlockPosition blockposition1 = blockposition.relative(enumdirection1);
                IBlockData iblockdata1 = fluidtypeflowing_b.getBlockState(blockposition1);
                Fluid fluid = iblockdata1.getFluidState();

                if (this.canPassThrough(iworldreader, this.getFlowing(), blockposition, iblockdata, enumdirection1, blockposition1, iblockdata1, fluid)) {
                    if (fluidtypeflowing_b.isHole(blockposition1)) {
                        return i;
                    }

                    if (i < this.getSlopeFindDistance(iworldreader)) {
                        int k = this.getSlopeDistance(iworldreader, blockposition1, i + 1, enumdirection1.getOpposite(), iblockdata1, fluidtypeflowing_b);

                        if (k < j) {
                            j = k;
                        }
                    }
                }
            }
        }

        return j;
    }

    boolean isWaterHole(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, BlockPosition blockposition1, IBlockData iblockdata1) {
        return !canPassThroughWall(EnumDirection.DOWN, iblockaccess, blockposition, iblockdata, blockposition1, iblockdata1) ? false : (iblockdata1.getFluidState().getType().isSame(this) ? true : canHoldFluid(iblockaccess, blockposition1, iblockdata1, this.getFlowing()));
    }

    private boolean canPassThrough(IBlockAccess iblockaccess, FluidType fluidtype, BlockPosition blockposition, IBlockData iblockdata, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, Fluid fluid) {
        return this.canMaybePassThrough(iblockaccess, blockposition, iblockdata, enumdirection, blockposition1, iblockdata1, fluid) && canHoldSpecificFluid(iblockaccess, blockposition1, iblockdata1, fluidtype);
    }

    private boolean canMaybePassThrough(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, Fluid fluid) {
        return !this.isSourceBlockOfThisType(fluid) && canHoldAnyFluid(iblockdata1) && canPassThroughWall(enumdirection, iblockaccess, blockposition, iblockdata, blockposition1, iblockdata1);
    }

    private boolean isSourceBlockOfThisType(Fluid fluid) {
        return fluid.getType().isSame(this) && fluid.isSource();
    }

    protected abstract int getSlopeFindDistance(IWorldReader iworldreader);

    private int sourceNeighborCount(IWorldReader iworldreader, BlockPosition blockposition) {
        int i = 0;
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            Fluid fluid = iworldreader.getFluidState(blockposition1);

            if (this.isSourceBlockOfThisType(fluid)) {
                ++i;
            }
        }

        return i;
    }

    protected Map<EnumDirection, Fluid> getSpread(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata) {
        int i = 1000;
        Map<EnumDirection, Fluid> map = Maps.newEnumMap(EnumDirection.class);
        FluidTypeFlowing.b fluidtypeflowing_b = null;
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            IBlockData iblockdata1 = worldserver.getBlockState(blockposition1);
            Fluid fluid = iblockdata1.getFluidState();

            if (this.canMaybePassThrough(worldserver, blockposition, iblockdata, enumdirection, blockposition1, iblockdata1, fluid)) {
                Fluid fluid1 = this.getNewLiquid(worldserver, blockposition1, iblockdata1);

                if (canHoldSpecificFluid(worldserver, blockposition1, iblockdata1, fluid1.getType())) {
                    if (fluidtypeflowing_b == null) {
                        fluidtypeflowing_b = new FluidTypeFlowing.b(worldserver, blockposition);
                    }

                    int j;

                    if (fluidtypeflowing_b.isHole(blockposition1)) {
                        j = 0;
                    } else {
                        j = this.getSlopeDistance(worldserver, blockposition1, 1, enumdirection.getOpposite(), iblockdata1, fluidtypeflowing_b);
                    }

                    if (j < i) {
                        map.clear();
                    }

                    if (j <= i) {
                        if (fluid.canBeReplacedWith(worldserver, blockposition1, fluid1.getType(), enumdirection)) {
                            map.put(enumdirection, fluid1);
                        }

                        i = j;
                    }
                }
            }
        }

        return map;
    }

    private static boolean canHoldAnyFluid(IBlockData iblockdata) {
        Block block = iblockdata.getBlock();

        return block instanceof IFluidContainer ? true : (iblockdata.blocksMotion() ? false : !(block instanceof BlockDoor) && !iblockdata.is(TagsBlock.SIGNS) && !iblockdata.is(Blocks.LADDER) && !iblockdata.is(Blocks.SUGAR_CANE) && !iblockdata.is(Blocks.BUBBLE_COLUMN) && !iblockdata.is(Blocks.NETHER_PORTAL) && !iblockdata.is(Blocks.END_PORTAL) && !iblockdata.is(Blocks.END_GATEWAY) && !iblockdata.is(Blocks.STRUCTURE_VOID));
    }

    private static boolean canHoldFluid(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, FluidType fluidtype) {
        return canHoldAnyFluid(iblockdata) && canHoldSpecificFluid(iblockaccess, blockposition, iblockdata, fluidtype);
    }

    private static boolean canHoldSpecificFluid(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, FluidType fluidtype) {
        Block block = iblockdata.getBlock();

        if (block instanceof IFluidContainer ifluidcontainer) {
            return ifluidcontainer.canPlaceLiquid((EntityHuman) null, iblockaccess, blockposition, iblockdata, fluidtype);
        } else {
            return true;
        }
    }

    protected abstract int getDropOff(IWorldReader iworldreader);

    protected int getSpreadDelay(World world, BlockPosition blockposition, Fluid fluid, Fluid fluid1) {
        return this.getTickDelay(world);
    }

    @Override
    public void tick(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
        if (!fluid.isSource()) {
            Fluid fluid1 = this.getNewLiquid(worldserver, blockposition, worldserver.getBlockState(blockposition));
            int i = this.getSpreadDelay(worldserver, blockposition, fluid, fluid1);

            if (fluid1.isEmpty()) {
                fluid = fluid1;
                iblockdata = Blocks.AIR.defaultBlockState();
                worldserver.setBlock(blockposition, iblockdata, 3);
            } else if (!fluid1.equals(fluid)) {
                fluid = fluid1;
                iblockdata = fluid1.createLegacyBlock();
                worldserver.setBlock(blockposition, iblockdata, 3);
                worldserver.scheduleTick(blockposition, fluid1.getType(), i);
            }
        }

        this.spread(worldserver, blockposition, iblockdata, fluid);
    }

    protected static int getLegacyLevel(Fluid fluid) {
        return fluid.isSource() ? 0 : 8 - Math.min(fluid.getAmount(), 8) + ((Boolean) fluid.getValue(FluidTypeFlowing.FALLING) ? 8 : 0);
    }

    private static boolean hasSameAbove(Fluid fluid, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return fluid.getType().isSame(iblockaccess.getFluidState(blockposition.above()).getType());
    }

    @Override
    public float getHeight(Fluid fluid, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return hasSameAbove(fluid, iblockaccess, blockposition) ? 1.0F : fluid.getOwnHeight();
    }

    @Override
    public float getOwnHeight(Fluid fluid) {
        return (float) fluid.getAmount() / 9.0F;
    }

    @Override
    public abstract int getAmount(Fluid fluid);

    @Override
    public VoxelShape getShape(Fluid fluid, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return fluid.getAmount() == 9 && hasSameAbove(fluid, iblockaccess, blockposition) ? VoxelShapes.block() : (VoxelShape) this.shapes.computeIfAbsent(fluid, (fluid1) -> {
            return VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double) fluid1.getHeight(iblockaccess, blockposition), 1.0D);
        });
    }

    private static record a(IBlockData first, IBlockData second, EnumDirection direction) {

        public boolean equals(Object object) {
            boolean flag;

            if (object instanceof FluidTypeFlowing.a fluidtypeflowing_a) {
                if (this.first == fluidtypeflowing_a.first && this.second == fluidtypeflowing_a.second && this.direction == fluidtypeflowing_a.direction) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }

        public int hashCode() {
            int i = System.identityHashCode(this.first);

            i = 31 * i + System.identityHashCode(this.second);
            i = 31 * i + this.direction.hashCode();
            return i;
        }
    }

    protected class b {

        private final IBlockAccess level;
        private final BlockPosition origin;
        private final Short2ObjectMap<IBlockData> stateCache = new Short2ObjectOpenHashMap();
        private final Short2BooleanMap holeCache = new Short2BooleanOpenHashMap();

        b(final IBlockAccess iblockaccess, final BlockPosition blockposition) {
            this.level = iblockaccess;
            this.origin = blockposition;
        }

        public IBlockData getBlockState(BlockPosition blockposition) {
            return this.getBlockState(blockposition, this.getCacheKey(blockposition));
        }

        private IBlockData getBlockState(BlockPosition blockposition, short short0) {
            return (IBlockData) this.stateCache.computeIfAbsent(short0, (short1) -> {
                return this.level.getBlockState(blockposition);
            });
        }

        public boolean isHole(BlockPosition blockposition) {
            return this.holeCache.computeIfAbsent(this.getCacheKey(blockposition), (short0) -> {
                IBlockData iblockdata = this.getBlockState(blockposition, short0);
                BlockPosition blockposition1 = blockposition.below();
                IBlockData iblockdata1 = this.level.getBlockState(blockposition1);

                return FluidTypeFlowing.this.isWaterHole(this.level, blockposition, iblockdata, blockposition1, iblockdata1);
            });
        }

        private short getCacheKey(BlockPosition blockposition) {
            int i = blockposition.getX() - this.origin.getX();
            int j = blockposition.getZ() - this.origin.getZ();

            return (short) ((i + 128 & 255) << 8 | j + 128 & 255);
        }
    }
}
