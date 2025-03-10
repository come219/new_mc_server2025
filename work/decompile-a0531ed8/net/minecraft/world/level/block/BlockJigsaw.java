package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.BlockPropertyJigsawOrientation;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityJigsaw;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockJigsaw extends Block implements ITileEntity, GameMasterBlock {

    public static final MapCodec<BlockJigsaw> CODEC = simpleCodec(BlockJigsaw::new);
    public static final BlockStateEnum<BlockPropertyJigsawOrientation> ORIENTATION = BlockProperties.ORIENTATION;

    @Override
    public MapCodec<BlockJigsaw> codec() {
        return BlockJigsaw.CODEC;
    }

    protected BlockJigsaw(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockJigsaw.ORIENTATION, BlockPropertyJigsawOrientation.NORTH_UP));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockJigsaw.ORIENTATION);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockJigsaw.ORIENTATION, enumblockrotation.rotation().rotate((BlockPropertyJigsawOrientation) iblockdata.getValue(BlockJigsaw.ORIENTATION)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return (IBlockData) iblockdata.setValue(BlockJigsaw.ORIENTATION, enumblockmirror.rotation().rotate((BlockPropertyJigsawOrientation) iblockdata.getValue(BlockJigsaw.ORIENTATION)));
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        EnumDirection enumdirection = blockactioncontext.getClickedFace();
        EnumDirection enumdirection1;

        if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
            enumdirection1 = blockactioncontext.getHorizontalDirection().getOpposite();
        } else {
            enumdirection1 = EnumDirection.UP;
        }

        return (IBlockData) this.defaultBlockState().setValue(BlockJigsaw.ORIENTATION, BlockPropertyJigsawOrientation.fromFrontAndTop(enumdirection, enumdirection1));
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityJigsaw(blockposition, iblockdata);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityJigsaw && entityhuman.canUseGameMasterBlocks()) {
            entityhuman.openJigsawBlock((TileEntityJigsaw) tileentity);
            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    public static boolean canAttach(DefinedStructure.a definedstructure_a, DefinedStructure.a definedstructure_a1) {
        EnumDirection enumdirection = getFrontFacing(definedstructure_a.info().state());
        EnumDirection enumdirection1 = getFrontFacing(definedstructure_a1.info().state());
        EnumDirection enumdirection2 = getTopFacing(definedstructure_a.info().state());
        EnumDirection enumdirection3 = getTopFacing(definedstructure_a1.info().state());
        TileEntityJigsaw.JointType tileentityjigsaw_jointtype = definedstructure_a.jointType();
        boolean flag = tileentityjigsaw_jointtype == TileEntityJigsaw.JointType.ROLLABLE;

        return enumdirection == enumdirection1.getOpposite() && (flag || enumdirection2 == enumdirection3) && definedstructure_a.target().equals(definedstructure_a1.name());
    }

    public static EnumDirection getFrontFacing(IBlockData iblockdata) {
        return ((BlockPropertyJigsawOrientation) iblockdata.getValue(BlockJigsaw.ORIENTATION)).front();
    }

    public static EnumDirection getTopFacing(IBlockData iblockdata) {
        return ((BlockPropertyJigsawOrientation) iblockdata.getValue(BlockJigsaw.ORIENTATION)).top();
    }
}
