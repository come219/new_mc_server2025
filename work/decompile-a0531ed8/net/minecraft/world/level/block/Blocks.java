package net.minecraft.world.level.block;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IRegistry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.references.Items;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityShulkerBox;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.grower.WorldGenTreeProvider;
import net.minecraft.world.level.block.piston.BlockPiston;
import net.minecraft.world.level.block.piston.BlockPistonExtension;
import net.minecraft.world.level.block.piston.BlockPistonMoving;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyBedPart;
import net.minecraft.world.level.block.state.properties.BlockPropertyInstrument;
import net.minecraft.world.level.block.state.properties.BlockPropertyWood;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.material.EnumPistonReaction;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.material.MaterialMapColor;

public class Blocks {

    private static final BlockBase.f NOT_CLOSED_SHULKER = (iblockdata, iblockaccess, blockposition) -> {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityShulkerBox tileentityshulkerbox) {
            return tileentityshulkerbox.isClosed();
        } else {
            return true;
        }
    };
    private static final BlockBase.f NOT_EXTENDED_PISTON = (iblockdata, iblockaccess, blockposition) -> {
        return !(Boolean) iblockdata.getValue(BlockPiston.EXTENDED);
    };
    public static final Block AIR = register("air", BlockAir::new, BlockBase.Info.of().replaceable().noCollission().noLootTable().air());
    public static final Block STONE = register("stone", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block GRANITE = register("granite", BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block POLISHED_GRANITE = register("polished_granite", BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block DIORITE = register("diorite", BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block POLISHED_DIORITE = register("polished_diorite", BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block ANDESITE = register("andesite", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block POLISHED_ANDESITE = register("polished_andesite", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block GRASS_BLOCK = register("grass_block", BlockGrass::new, BlockBase.Info.of().mapColor(MaterialMapColor.GRASS).randomTicks().strength(0.6F).sound(SoundEffectType.GRASS));
    public static final Block DIRT = register("dirt", BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.5F).sound(SoundEffectType.GRAVEL));
    public static final Block COARSE_DIRT = register("coarse_dirt", BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.5F).sound(SoundEffectType.GRAVEL));
    public static final Block PODZOL = register("podzol", BlockDirtSnow::new, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).strength(0.5F).sound(SoundEffectType.GRAVEL));
    public static final Block COBBLESTONE = register("cobblestone", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block OAK_PLANKS = register("oak_planks", BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block SPRUCE_PLANKS = register("spruce_planks", BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BIRCH_PLANKS = register("birch_planks", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block JUNGLE_PLANKS = register("jungle_planks", BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block ACACIA_PLANKS = register("acacia_planks", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block CHERRY_PLANKS = register("cherry_planks", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava());
    public static final Block DARK_OAK_PLANKS = register("dark_oak_planks", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block PALE_OAK_WOOD = register("pale_oak_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block PALE_OAK_PLANKS = register("pale_oak_planks", BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block MANGROVE_PLANKS = register("mangrove_planks", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BAMBOO_PLANKS = register("bamboo_planks", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava());
    public static final Block BAMBOO_MOSAIC = register("bamboo_mosaic", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava());
    public static final Block OAK_SAPLING = register("oak_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SPRUCE_SAPLING = register("spruce_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.SPRUCE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BIRCH_SAPLING = register("birch_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.BIRCH, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block JUNGLE_SAPLING = register("jungle_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.JUNGLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ACACIA_SAPLING = register("acacia_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.ACACIA, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CHERRY_SAPLING = register("cherry_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.CHERRY, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).noCollission().randomTicks().instabreak().sound(SoundEffectType.CHERRY_SAPLING).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DARK_OAK_SAPLING = register("dark_oak_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.DARK_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PALE_OAK_SAPLING = register("pale_oak_sapling", (blockbase_info) -> {
        return new BlockSapling(WorldGenTreeProvider.PALE_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block MANGROVE_PROPAGULE = register("mangrove_propagule", (blockbase_info) -> {
        return new MangrovePropaguleBlock(WorldGenTreeProvider.MANGROVE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BEDROCK = register("bedrock", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn(Blocks::never));
    public static final Block WATER = register("water", (blockbase_info) -> {
        return new BlockFluids(FluidTypes.WATER, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().strength(100.0F).pushReaction(EnumPistonReaction.DESTROY).noLootTable().liquid().sound(SoundEffectType.EMPTY));
    public static final Block LAVA = register("lava", (blockbase_info) -> {
        return new BlockFluids(FluidTypes.LAVA, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).replaceable().noCollission().randomTicks().strength(100.0F).lightLevel((iblockdata) -> {
        return 15;
    }).pushReaction(EnumPistonReaction.DESTROY).noLootTable().liquid().sound(SoundEffectType.EMPTY));
    public static final Block SAND = register("sand", (blockbase_info) -> {
        return new ColoredFallingBlock(new ColorRGBA(14406560), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block SUSPICIOUS_SAND = register("suspicious_sand", (blockbase_info) -> {
        return new BrushableBlock(Blocks.SAND, SoundEffects.BRUSH_SAND, SoundEffects.BRUSH_SAND, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.SNARE).strength(0.25F).sound(SoundEffectType.SUSPICIOUS_SAND).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block RED_SAND = register("red_sand", (blockbase_info) -> {
        return new ColoredFallingBlock(new ColorRGBA(11098145), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block GRAVEL = register("gravel", (blockbase_info) -> {
        return new ColoredFallingBlock(new ColorRGBA(-8356741), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.SNARE).strength(0.6F).sound(SoundEffectType.GRAVEL));
    public static final Block SUSPICIOUS_GRAVEL = register("suspicious_gravel", (blockbase_info) -> {
        return new BrushableBlock(Blocks.GRAVEL, SoundEffects.BRUSH_GRAVEL, SoundEffects.BRUSH_GRAVEL, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.SNARE).strength(0.25F).sound(SoundEffectType.SUSPICIOUS_GRAVEL).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block GOLD_ORE = register("gold_ore", (blockbase_info) -> {
        return new DropExperienceBlock(ConstantInt.of(0), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final Block DEEPSLATE_GOLD_ORE = register("deepslate_gold_ore", (blockbase_info) -> {
        return new DropExperienceBlock(ConstantInt.of(0), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.GOLD_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block IRON_ORE = register("iron_ore", (blockbase_info) -> {
        return new DropExperienceBlock(ConstantInt.of(0), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final Block DEEPSLATE_IRON_ORE = register("deepslate_iron_ore", (blockbase_info) -> {
        return new DropExperienceBlock(ConstantInt.of(0), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.IRON_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block COAL_ORE = register("coal_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(0, 2), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final Block DEEPSLATE_COAL_ORE = register("deepslate_coal_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(0, 2), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.COAL_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block NETHER_GOLD_ORE = register("nether_gold_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(0, 1), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundEffectType.NETHER_GOLD_ORE));
    public static final Block OAK_LOG = register("oak_log", BlockRotatable::new, logProperties(MaterialMapColor.WOOD, MaterialMapColor.PODZOL, SoundEffectType.WOOD));
    public static final Block SPRUCE_LOG = register("spruce_log", BlockRotatable::new, logProperties(MaterialMapColor.PODZOL, MaterialMapColor.COLOR_BROWN, SoundEffectType.WOOD));
    public static final Block BIRCH_LOG = register("birch_log", BlockRotatable::new, logProperties(MaterialMapColor.SAND, MaterialMapColor.QUARTZ, SoundEffectType.WOOD));
    public static final Block JUNGLE_LOG = register("jungle_log", BlockRotatable::new, logProperties(MaterialMapColor.DIRT, MaterialMapColor.PODZOL, SoundEffectType.WOOD));
    public static final Block ACACIA_LOG = register("acacia_log", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_ORANGE, MaterialMapColor.STONE, SoundEffectType.WOOD));
    public static final Block CHERRY_LOG = register("cherry_log", BlockRotatable::new, logProperties(MaterialMapColor.TERRACOTTA_WHITE, MaterialMapColor.TERRACOTTA_GRAY, SoundEffectType.CHERRY_WOOD));
    public static final Block DARK_OAK_LOG = register("dark_oak_log", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_BROWN, MaterialMapColor.COLOR_BROWN, SoundEffectType.WOOD));
    public static final Block PALE_OAK_LOG = register("pale_oak_log", BlockRotatable::new, logProperties(Blocks.PALE_OAK_PLANKS.defaultMapColor(), Blocks.PALE_OAK_WOOD.defaultMapColor(), SoundEffectType.WOOD));
    public static final Block MANGROVE_LOG = register("mangrove_log", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_RED, MaterialMapColor.PODZOL, SoundEffectType.WOOD));
    public static final Block MANGROVE_ROOTS = register("mangrove_roots", MangroveRootsBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(0.7F).sound(SoundEffectType.MANGROVE_ROOTS).noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never).noOcclusion().ignitedByLava());
    public static final Block MUDDY_MANGROVE_ROOTS = register("muddy_mangrove_roots", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).strength(0.7F).sound(SoundEffectType.MUDDY_MANGROVE_ROOTS));
    public static final Block BAMBOO_BLOCK = register("bamboo_block", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_YELLOW, MaterialMapColor.PLANT, SoundEffectType.BAMBOO_WOOD));
    public static final Block STRIPPED_SPRUCE_LOG = register("stripped_spruce_log", BlockRotatable::new, logProperties(MaterialMapColor.PODZOL, MaterialMapColor.PODZOL, SoundEffectType.WOOD));
    public static final Block STRIPPED_BIRCH_LOG = register("stripped_birch_log", BlockRotatable::new, logProperties(MaterialMapColor.SAND, MaterialMapColor.SAND, SoundEffectType.WOOD));
    public static final Block STRIPPED_JUNGLE_LOG = register("stripped_jungle_log", BlockRotatable::new, logProperties(MaterialMapColor.DIRT, MaterialMapColor.DIRT, SoundEffectType.WOOD));
    public static final Block STRIPPED_ACACIA_LOG = register("stripped_acacia_log", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_ORANGE, MaterialMapColor.COLOR_ORANGE, SoundEffectType.WOOD));
    public static final Block STRIPPED_CHERRY_LOG = register("stripped_cherry_log", BlockRotatable::new, logProperties(MaterialMapColor.TERRACOTTA_WHITE, MaterialMapColor.TERRACOTTA_PINK, SoundEffectType.CHERRY_WOOD));
    public static final Block STRIPPED_DARK_OAK_LOG = register("stripped_dark_oak_log", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_BROWN, MaterialMapColor.COLOR_BROWN, SoundEffectType.WOOD));
    public static final Block STRIPPED_PALE_OAK_LOG = register("stripped_pale_oak_log", BlockRotatable::new, logProperties(Blocks.PALE_OAK_PLANKS.defaultMapColor(), Blocks.PALE_OAK_PLANKS.defaultMapColor(), SoundEffectType.WOOD));
    public static final Block STRIPPED_OAK_LOG = register("stripped_oak_log", BlockRotatable::new, logProperties(MaterialMapColor.WOOD, MaterialMapColor.WOOD, SoundEffectType.WOOD));
    public static final Block STRIPPED_MANGROVE_LOG = register("stripped_mangrove_log", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_RED, MaterialMapColor.COLOR_RED, SoundEffectType.WOOD));
    public static final Block STRIPPED_BAMBOO_BLOCK = register("stripped_bamboo_block", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_YELLOW, MaterialMapColor.COLOR_YELLOW, SoundEffectType.BAMBOO_WOOD));
    public static final Block OAK_WOOD = register("oak_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block SPRUCE_WOOD = register("spruce_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BIRCH_WOOD = register("birch_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block JUNGLE_WOOD = register("jungle_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block ACACIA_WOOD = register("acacia_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block CHERRY_WOOD = register("cherry_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GRAY).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava());
    public static final Block DARK_OAK_WOOD = register("dark_oak_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block MANGROVE_WOOD = register("mangrove_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_OAK_WOOD = register("stripped_oak_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_SPRUCE_WOOD = register("stripped_spruce_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_BIRCH_WOOD = register("stripped_birch_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_JUNGLE_WOOD = register("stripped_jungle_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_ACACIA_WOOD = register("stripped_acacia_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_CHERRY_WOOD = register("stripped_cherry_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PINK).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava());
    public static final Block STRIPPED_DARK_OAK_WOOD = register("stripped_dark_oak_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_PALE_OAK_WOOD = register("stripped_pale_oak_wood", BlockRotatable::new, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STRIPPED_MANGROVE_WOOD = register("stripped_mangrove_wood", BlockRotatable::new, logProperties(MaterialMapColor.COLOR_RED, MaterialMapColor.COLOR_RED, SoundEffectType.WOOD));
    public static final Block OAK_LEAVES = register("oak_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.GRASS));
    public static final Block SPRUCE_LEAVES = register("spruce_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.GRASS));
    public static final Block BIRCH_LEAVES = register("birch_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.GRASS));
    public static final Block JUNGLE_LEAVES = register("jungle_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.GRASS));
    public static final Block ACACIA_LEAVES = register("acacia_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.GRASS));
    public static final Block CHERRY_LEAVES = register("cherry_leaves", (blockbase_info) -> {
        return new ParticleLeavesBlock(10, Particles.CHERRY_LEAVES, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).strength(0.2F).randomTicks().sound(SoundEffectType.CHERRY_LEAVES).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never));
    public static final Block DARK_OAK_LEAVES = register("dark_oak_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.GRASS));
    public static final Block PALE_OAK_LEAVES = register("pale_oak_leaves", (blockbase_info) -> {
        return new ParticleLeavesBlock(50, Particles.PALE_OAK_LEAVES, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GREEN).strength(0.2F).randomTicks().sound(SoundEffectType.GRASS).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never));
    public static final Block MANGROVE_LEAVES = register("mangrove_leaves", MangroveLeavesBlock::new, leavesProperties(SoundEffectType.GRASS));
    public static final Block AZALEA_LEAVES = register("azalea_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.AZALEA_LEAVES));
    public static final Block FLOWERING_AZALEA_LEAVES = register("flowering_azalea_leaves", BlockLeaves::new, leavesProperties(SoundEffectType.AZALEA_LEAVES));
    public static final Block SPONGE = register("sponge", BlockSponge::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).strength(0.6F).sound(SoundEffectType.SPONGE));
    public static final Block WET_SPONGE = register("wet_sponge", BlockWetSponge::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).strength(0.6F).sound(SoundEffectType.WET_SPONGE));
    public static final Block GLASS = register("glass", BlockGlassAbstract::new, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
    public static final Block LAPIS_ORE = register("lapis_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(2, 5), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final Block DEEPSLATE_LAPIS_ORE = register("deepslate_lapis_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(2, 5), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.LAPIS_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block LAPIS_BLOCK = register("lapis_block", BlockBase.Info.of().mapColor(MaterialMapColor.LAPIS).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final Block DISPENSER = register("dispenser", BlockDispenser::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F));
    public static final Block SANDSTONE = register("sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block CHISELED_SANDSTONE = register("chiseled_sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block CUT_SANDSTONE = register("cut_sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block NOTE_BLOCK = register("note_block", BlockNote::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).sound(SoundEffectType.WOOD).strength(0.8F).ignitedByLava());
    public static final Block WHITE_BED = registerBed("white_bed", EnumColor.WHITE);
    public static final Block ORANGE_BED = registerBed("orange_bed", EnumColor.ORANGE);
    public static final Block MAGENTA_BED = registerBed("magenta_bed", EnumColor.MAGENTA);
    public static final Block LIGHT_BLUE_BED = registerBed("light_blue_bed", EnumColor.LIGHT_BLUE);
    public static final Block YELLOW_BED = registerBed("yellow_bed", EnumColor.YELLOW);
    public static final Block LIME_BED = registerBed("lime_bed", EnumColor.LIME);
    public static final Block PINK_BED = registerBed("pink_bed", EnumColor.PINK);
    public static final Block GRAY_BED = registerBed("gray_bed", EnumColor.GRAY);
    public static final Block LIGHT_GRAY_BED = registerBed("light_gray_bed", EnumColor.LIGHT_GRAY);
    public static final Block CYAN_BED = registerBed("cyan_bed", EnumColor.CYAN);
    public static final Block PURPLE_BED = registerBed("purple_bed", EnumColor.PURPLE);
    public static final Block BLUE_BED = registerBed("blue_bed", EnumColor.BLUE);
    public static final Block BROWN_BED = registerBed("brown_bed", EnumColor.BROWN);
    public static final Block GREEN_BED = registerBed("green_bed", EnumColor.GREEN);
    public static final Block RED_BED = registerBed("red_bed", EnumColor.RED);
    public static final Block BLACK_BED = registerBed("black_bed", EnumColor.BLACK);
    public static final Block POWERED_RAIL = register("powered_rail", BlockPoweredRail::new, BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL));
    public static final Block DETECTOR_RAIL = register("detector_rail", BlockMinecartDetector::new, BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL));
    public static final Block STICKY_PISTON = register("sticky_piston", (blockbase_info) -> {
        return new BlockPiston(true, blockbase_info);
    }, pistonProperties());
    public static final Block COBWEB = register("cobweb", BlockWeb::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOL).sound(SoundEffectType.COBWEB).forceSolidOn().noCollission().requiresCorrectToolForDrops().strength(4.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SHORT_GRASS = register("short_grass", BlockLongGrass::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XYZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block FERN = register("fern", BlockLongGrass::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XYZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DEAD_BUSH = register("dead_bush", BlockDeadBush::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SEAGRASS = register("seagrass", SeagrassBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block TALL_SEAGRASS = register("tall_seagrass", TallSeagrassBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().instabreak().sound(SoundEffectType.WET_GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PISTON = register("piston", (blockbase_info) -> {
        return new BlockPiston(false, blockbase_info);
    }, pistonProperties());
    public static final Block PISTON_HEAD = register("piston_head", BlockPistonExtension::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).strength(1.5F).noLootTable().pushReaction(EnumPistonReaction.BLOCK));
    public static final Block WHITE_WOOL = register("white_wool", BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block ORANGE_WOOL = register("orange_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block MAGENTA_WOOL = register("magenta_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block LIGHT_BLUE_WOOL = register("light_blue_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_BLUE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block YELLOW_WOOL = register("yellow_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block LIME_WOOL = register("lime_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GREEN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block PINK_WOOL = register("pink_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block GRAY_WOOL = register("gray_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block LIGHT_GRAY_WOOL = register("light_gray_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block CYAN_WOOL = register("cyan_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block PURPLE_WOOL = register("purple_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block BLUE_WOOL = register("blue_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block BROWN_WOOL = register("brown_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block GREEN_WOOL = register("green_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block RED_WOOL = register("red_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block BLACK_WOOL = register("black_wool", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block MOVING_PISTON = register("moving_piston", BlockPistonMoving::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).forceSolidOn().strength(-1.0F).dynamicShape().noLootTable().noOcclusion().isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).pushReaction(EnumPistonReaction.BLOCK));
    public static final Block DANDELION = register("dandelion", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.SATURATION, 0.35F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block TORCHFLOWER = register("torchflower", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.NIGHT_VISION, 5.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block POPPY = register("poppy", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.NIGHT_VISION, 5.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BLUE_ORCHID = register("blue_orchid", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.SATURATION, 0.35F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ALLIUM = register("allium", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.FIRE_RESISTANCE, 3.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block AZURE_BLUET = register("azure_bluet", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.BLINDNESS, 11.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block RED_TULIP = register("red_tulip", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.WEAKNESS, 7.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ORANGE_TULIP = register("orange_tulip", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.WEAKNESS, 7.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WHITE_TULIP = register("white_tulip", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.WEAKNESS, 7.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PINK_TULIP = register("pink_tulip", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.WEAKNESS, 7.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block OXEYE_DAISY = register("oxeye_daisy", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.REGENERATION, 7.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CORNFLOWER = register("cornflower", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.JUMP, 5.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WITHER_ROSE = register("wither_rose", (blockbase_info) -> {
        return new BlockWitherRose(MobEffects.WITHER, 7.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block LILY_OF_THE_VALLEY = register("lily_of_the_valley", (blockbase_info) -> {
        return new BlockFlowers(MobEffects.POISON, 11.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BROWN_MUSHROOM = register("brown_mushroom", (blockbase_info) -> {
        return new BlockMushroom(TreeFeatures.HUGE_BROWN_MUSHROOM, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).lightLevel((iblockdata) -> {
        return 1;
    }).hasPostProcess(Blocks::always).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block RED_MUSHROOM = register("red_mushroom", (blockbase_info) -> {
        return new BlockMushroom(TreeFeatures.HUGE_RED_MUSHROOM, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).hasPostProcess(Blocks::always).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block GOLD_BLOCK = register("gold_block", BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).instrument(BlockPropertyInstrument.BELL).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.METAL));
    public static final Block IRON_BLOCK = register("iron_block", BlockBase.Info.of().mapColor(MaterialMapColor.METAL).instrument(BlockPropertyInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL));
    public static final Block BRICKS = register("bricks", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block TNT = register("tnt", BlockTNT::new, BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).instabreak().sound(SoundEffectType.GRASS).ignitedByLava().isRedstoneConductor(Blocks::never));
    public static final Block BOOKSHELF = register("bookshelf", BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(1.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block CHISELED_BOOKSHELF = register("chiseled_bookshelf", ChiseledBookShelfBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(1.5F).sound(SoundEffectType.CHISELED_BOOKSHELF).ignitedByLava());
    public static final Block MOSSY_COBBLESTONE = register("mossy_cobblestone", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block OBSIDIAN = register("obsidian", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F));
    public static final Block TORCH = register("torch", (blockbase_info) -> {
        return new BlockTorch(Particles.FLAME, blockbase_info);
    }, BlockBase.Info.of().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 14;
    }).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WALL_TORCH = register("wall_torch", (blockbase_info) -> {
        return new BlockTorchWall(Particles.FLAME, blockbase_info);
    }, wallVariant(Blocks.TORCH, true).noCollission().instabreak().lightLevel((iblockdata) -> {
        return 14;
    }).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block FIRE = register("fire", BlockFire::new, BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).replaceable().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SOUL_FIRE = register("soul_fire", BlockSoulFire::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_BLUE).replaceable().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 10;
    }).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SPAWNER = register("spawner", BlockMobSpawner::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F).sound(SoundEffectType.SPAWNER).noOcclusion());
    public static final Block CREAKING_HEART = register("creaking_heart", CreakingHeartBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).strength(10.0F).sound(SoundEffectType.CREAKING_HEART));
    public static final Block OAK_STAIRS = registerLegacyStair("oak_stairs", Blocks.OAK_PLANKS);
    public static final Block CHEST = register("chest", (blockbase_info) -> {
        return new BlockChest(() -> {
            return TileEntityTypes.CHEST;
        }, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block REDSTONE_WIRE = register("redstone_wire", BlockRedstoneWire::new, BlockBase.Info.of().noCollission().instabreak().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DIAMOND_ORE = register("diamond_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(3, 7), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final Block DEEPSLATE_DIAMOND_ORE = register("deepslate_diamond_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(3, 7), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.DIAMOND_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block DIAMOND_BLOCK = register("diamond_block", BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL));
    public static final Block CRAFTING_TABLE = register("crafting_table", BlockWorkbench::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block WHEAT = register("wheat", BlockCrops::new, BlockBase.Info.of().mapColor((iblockdata) -> {
        return (Integer) iblockdata.getValue(BlockCrops.AGE) >= 6 ? MaterialMapColor.COLOR_YELLOW : MaterialMapColor.PLANT;
    }).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block FARMLAND = register("farmland", BlockSoil::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).randomTicks().strength(0.6F).sound(SoundEffectType.GRAVEL).isViewBlocking(Blocks::always).isSuffocating(Blocks::always));
    public static final Block FURNACE = register("furnace", BlockFurnaceFurace::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(13)));
    public static final Block OAK_SIGN = register("oak_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block SPRUCE_SIGN = register("spruce_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.SPRUCE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.SPRUCE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block BIRCH_SIGN = register("birch_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.BIRCH, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block ACACIA_SIGN = register("acacia_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.ACACIA, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block CHERRY_SIGN = register("cherry_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.CHERRY, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block JUNGLE_SIGN = register("jungle_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.JUNGLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block DARK_OAK_SIGN = register("dark_oak_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.DARK_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block PALE_OAK_SIGN = register("pale_oak_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.PALE_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block MANGROVE_SIGN = register("mangrove_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.MANGROVE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block BAMBOO_SIGN = register("bamboo_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.BAMBOO, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block OAK_DOOR = register("oak_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block LADDER = register("ladder", BlockLadder::new, BlockBase.Info.of().forceSolidOff().strength(0.4F).sound(SoundEffectType.LADDER).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block RAIL = register("rail", BlockMinecartTrack::new, BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL));
    public static final Block COBBLESTONE_STAIRS = registerLegacyStair("cobblestone_stairs", Blocks.COBBLESTONE);
    public static final Block OAK_WALL_SIGN = register("oak_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.OAK, blockbase_info);
    }, wallVariant(Blocks.OAK_SIGN, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block SPRUCE_WALL_SIGN = register("spruce_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.SPRUCE, blockbase_info);
    }, wallVariant(Blocks.SPRUCE_SIGN, true).mapColor(Blocks.SPRUCE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block BIRCH_WALL_SIGN = register("birch_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.BIRCH, blockbase_info);
    }, wallVariant(Blocks.BIRCH_SIGN, true).mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block ACACIA_WALL_SIGN = register("acacia_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.ACACIA, blockbase_info);
    }, wallVariant(Blocks.ACACIA_SIGN, true).mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block CHERRY_WALL_SIGN = register("cherry_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.CHERRY, blockbase_info);
    }, wallVariant(Blocks.CHERRY_SIGN, true).mapColor(Blocks.CHERRY_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block JUNGLE_WALL_SIGN = register("jungle_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.JUNGLE, blockbase_info);
    }, wallVariant(Blocks.JUNGLE_SIGN, true).mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block DARK_OAK_WALL_SIGN = register("dark_oak_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.DARK_OAK, blockbase_info);
    }, wallVariant(Blocks.DARK_OAK_SIGN, true).mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block PALE_OAK_WALL_SIGN = register("pale_oak_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.PALE_OAK, blockbase_info);
    }, wallVariant(Blocks.PALE_OAK_SIGN, true).mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block MANGROVE_WALL_SIGN = register("mangrove_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.MANGROVE, blockbase_info);
    }, wallVariant(Blocks.MANGROVE_SIGN, true).mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block BAMBOO_WALL_SIGN = register("bamboo_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.BAMBOO, blockbase_info);
    }, wallVariant(Blocks.BAMBOO_SIGN, true).mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block OAK_HANGING_SIGN = register("oak_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block SPRUCE_HANGING_SIGN = register("spruce_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.SPRUCE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.SPRUCE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block BIRCH_HANGING_SIGN = register("birch_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.BIRCH, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block ACACIA_HANGING_SIGN = register("acacia_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.ACACIA, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block CHERRY_HANGING_SIGN = register("cherry_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.CHERRY, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PINK).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block JUNGLE_HANGING_SIGN = register("jungle_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.JUNGLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block DARK_OAK_HANGING_SIGN = register("dark_oak_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.DARK_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block PALE_OAK_HANGING_SIGN = register("pale_oak_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.PALE_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block CRIMSON_HANGING_SIGN = register("crimson_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.CRIMSON, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F));
    public static final Block WARPED_HANGING_SIGN = register("warped_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.WARPED, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F));
    public static final Block MANGROVE_HANGING_SIGN = register("mangrove_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.MANGROVE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block BAMBOO_HANGING_SIGN = register("bamboo_hanging_sign", (blockbase_info) -> {
        return new CeilingHangingSignBlock(BlockPropertyWood.BAMBOO, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block OAK_WALL_HANGING_SIGN = register("oak_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.OAK, blockbase_info);
    }, wallVariant(Blocks.OAK_HANGING_SIGN, true).mapColor(Blocks.OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block SPRUCE_WALL_HANGING_SIGN = register("spruce_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.SPRUCE, blockbase_info);
    }, wallVariant(Blocks.SPRUCE_HANGING_SIGN, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block BIRCH_WALL_HANGING_SIGN = register("birch_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.BIRCH, blockbase_info);
    }, wallVariant(Blocks.BIRCH_HANGING_SIGN, true).mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block ACACIA_WALL_HANGING_SIGN = register("acacia_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.ACACIA, blockbase_info);
    }, wallVariant(Blocks.ACACIA_HANGING_SIGN, true).mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block CHERRY_WALL_HANGING_SIGN = register("cherry_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.CHERRY, blockbase_info);
    }, wallVariant(Blocks.CHERRY_HANGING_SIGN, true).mapColor(MaterialMapColor.TERRACOTTA_PINK).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block JUNGLE_WALL_HANGING_SIGN = register("jungle_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.JUNGLE, blockbase_info);
    }, wallVariant(Blocks.JUNGLE_HANGING_SIGN, true).mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block DARK_OAK_WALL_HANGING_SIGN = register("dark_oak_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.DARK_OAK, blockbase_info);
    }, wallVariant(Blocks.DARK_OAK_HANGING_SIGN, true).mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block PALE_OAK_WALL_HANGING_SIGN = register("pale_oak_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.PALE_OAK, blockbase_info);
    }, wallVariant(Blocks.PALE_OAK_HANGING_SIGN, true).mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block MANGROVE_WALL_HANGING_SIGN = register("mangrove_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.MANGROVE, blockbase_info);
    }, wallVariant(Blocks.MANGROVE_HANGING_SIGN, true).mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block CRIMSON_WALL_HANGING_SIGN = register("crimson_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.CRIMSON, blockbase_info);
    }, wallVariant(Blocks.CRIMSON_HANGING_SIGN, true).mapColor(MaterialMapColor.CRIMSON_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F));
    public static final Block WARPED_WALL_HANGING_SIGN = register("warped_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.WARPED, blockbase_info);
    }, wallVariant(Blocks.WARPED_HANGING_SIGN, true).mapColor(MaterialMapColor.WARPED_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F));
    public static final Block BAMBOO_WALL_HANGING_SIGN = register("bamboo_wall_hanging_sign", (blockbase_info) -> {
        return new WallHangingSignBlock(BlockPropertyWood.BAMBOO, blockbase_info);
    }, wallVariant(Blocks.BAMBOO_HANGING_SIGN, true).mapColor(MaterialMapColor.COLOR_YELLOW).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava());
    public static final Block LEVER = register("lever", BlockLever::new, BlockBase.Info.of().noCollission().strength(0.5F).sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block STONE_PRESSURE_PLATE = register("stone_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.STONE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block IRON_DOOR = register("iron_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.IRON, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).strength(5.0F).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block OAK_PRESSURE_PLATE = register("oak_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SPRUCE_PRESSURE_PLATE = register("spruce_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.SPRUCE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BIRCH_PRESSURE_PLATE = register("birch_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.BIRCH, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block JUNGLE_PRESSURE_PLATE = register("jungle_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.JUNGLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ACACIA_PRESSURE_PLATE = register("acacia_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.ACACIA, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CHERRY_PRESSURE_PLATE = register("cherry_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.CHERRY, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DARK_OAK_PRESSURE_PLATE = register("dark_oak_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.DARK_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PALE_OAK_PRESSURE_PLATE = register("pale_oak_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.PALE_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block MANGROVE_PRESSURE_PLATE = register("mangrove_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.MANGROVE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BAMBOO_PRESSURE_PLATE = register("bamboo_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.BAMBOO, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block REDSTONE_ORE = register("redstone_ore", BlockRedstoneOre::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().randomTicks().lightLevel(litBlockEmission(9)).strength(3.0F, 3.0F));
    public static final Block DEEPSLATE_REDSTONE_ORE = register("deepslate_redstone_ore", BlockRedstoneOre::new, BlockBase.Info.ofLegacyCopy(Blocks.REDSTONE_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block REDSTONE_TORCH = register("redstone_torch", BlockRedstoneTorch::new, BlockBase.Info.of().noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block REDSTONE_WALL_TORCH = register("redstone_wall_torch", BlockRedstoneTorchWall::new, wallVariant(Blocks.REDSTONE_TORCH, true).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block STONE_BUTTON = register("stone_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.STONE, 20, blockbase_info);
    }, buttonProperties());
    public static final Block SNOW = register("snow", BlockSnow::new, BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).replaceable().forceSolidOff().randomTicks().strength(0.1F).requiresCorrectToolForDrops().sound(SoundEffectType.SNOW).isViewBlocking((iblockdata, iblockaccess, blockposition) -> {
        return (Integer) iblockdata.getValue(BlockSnow.LAYERS) >= 8;
    }).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ICE = register("ice", BlockIce::new, BlockBase.Info.of().mapColor(MaterialMapColor.ICE).friction(0.98F).randomTicks().strength(0.5F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn((iblockdata, iblockaccess, blockposition, entitytypes) -> {
        return entitytypes == EntityTypes.POLAR_BEAR;
    }).isRedstoneConductor(Blocks::never));
    public static final Block SNOW_BLOCK = register("snow_block", BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).requiresCorrectToolForDrops().strength(0.2F).sound(SoundEffectType.SNOW));
    public static final Block CACTUS = register("cactus", BlockCactus::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().strength(0.4F).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CLAY = register("clay", BlockBase.Info.of().mapColor(MaterialMapColor.CLAY).instrument(BlockPropertyInstrument.FLUTE).strength(0.6F).sound(SoundEffectType.GRAVEL));
    public static final Block SUGAR_CANE = register("sugar_cane", BlockReed::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block JUKEBOX = register("jukebox", BlockJukeBox::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 6.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block OAK_FENCE = register("oak_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block NETHERRACK = register("netherrack", BlockNetherrack::new, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundEffectType.NETHERRACK));
    public static final Block SOUL_SAND = register("soul_sand", BlockSlowSand::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.COW_BELL).strength(0.5F).speedFactor(0.4F).sound(SoundEffectType.SOUL_SAND).isValidSpawn(Blocks::always).isRedstoneConductor(Blocks::always).isViewBlocking(Blocks::always).isSuffocating(Blocks::always));
    public static final Block SOUL_SOIL = register("soul_soil", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).strength(0.5F).sound(SoundEffectType.SOUL_SOIL));
    public static final Block BASALT = register("basalt", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F).sound(SoundEffectType.BASALT));
    public static final Block POLISHED_BASALT = register("polished_basalt", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F).sound(SoundEffectType.BASALT));
    public static final Block SOUL_TORCH = register("soul_torch", (blockbase_info) -> {
        return new BlockTorch(Particles.SOUL_FIRE_FLAME, blockbase_info);
    }, BlockBase.Info.of().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 10;
    }).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SOUL_WALL_TORCH = register("soul_wall_torch", (blockbase_info) -> {
        return new BlockTorchWall(Particles.SOUL_FIRE_FLAME, blockbase_info);
    }, wallVariant(Blocks.SOUL_TORCH, true).noCollission().instabreak().lightLevel((iblockdata) -> {
        return 10;
    }).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block GLOWSTONE = register("glowstone", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.PLING).strength(0.3F).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 15;
    }).isRedstoneConductor(Blocks::never));
    public static final Block NETHER_PORTAL = register("nether_portal", BlockPortal::new, BlockBase.Info.of().noCollission().randomTicks().strength(-1.0F).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 11;
    }).pushReaction(EnumPistonReaction.BLOCK));
    public static final Block CARVED_PUMPKIN = register("carved_pumpkin", BlockPumpkinCarved::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(1.0F).sound(SoundEffectType.WOOD).isValidSpawn(Blocks::always).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block JACK_O_LANTERN = register("jack_o_lantern", BlockPumpkinCarved::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(1.0F).sound(SoundEffectType.WOOD).lightLevel((iblockdata) -> {
        return 15;
    }).isValidSpawn(Blocks::always).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CAKE = register("cake", BlockCake::new, BlockBase.Info.of().forceSolidOn().strength(0.5F).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block REPEATER = register("repeater", BlockRepeater::new, BlockBase.Info.of().instabreak().sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WHITE_STAINED_GLASS = registerStainedGlass("white_stained_glass", EnumColor.WHITE);
    public static final Block ORANGE_STAINED_GLASS = registerStainedGlass("orange_stained_glass", EnumColor.ORANGE);
    public static final Block MAGENTA_STAINED_GLASS = registerStainedGlass("magenta_stained_glass", EnumColor.MAGENTA);
    public static final Block LIGHT_BLUE_STAINED_GLASS = registerStainedGlass("light_blue_stained_glass", EnumColor.LIGHT_BLUE);
    public static final Block YELLOW_STAINED_GLASS = registerStainedGlass("yellow_stained_glass", EnumColor.YELLOW);
    public static final Block LIME_STAINED_GLASS = registerStainedGlass("lime_stained_glass", EnumColor.LIME);
    public static final Block PINK_STAINED_GLASS = registerStainedGlass("pink_stained_glass", EnumColor.PINK);
    public static final Block GRAY_STAINED_GLASS = registerStainedGlass("gray_stained_glass", EnumColor.GRAY);
    public static final Block LIGHT_GRAY_STAINED_GLASS = registerStainedGlass("light_gray_stained_glass", EnumColor.LIGHT_GRAY);
    public static final Block CYAN_STAINED_GLASS = registerStainedGlass("cyan_stained_glass", EnumColor.CYAN);
    public static final Block PURPLE_STAINED_GLASS = registerStainedGlass("purple_stained_glass", EnumColor.PURPLE);
    public static final Block BLUE_STAINED_GLASS = registerStainedGlass("blue_stained_glass", EnumColor.BLUE);
    public static final Block BROWN_STAINED_GLASS = registerStainedGlass("brown_stained_glass", EnumColor.BROWN);
    public static final Block GREEN_STAINED_GLASS = registerStainedGlass("green_stained_glass", EnumColor.GREEN);
    public static final Block RED_STAINED_GLASS = registerStainedGlass("red_stained_glass", EnumColor.RED);
    public static final Block BLACK_STAINED_GLASS = registerStainedGlass("black_stained_glass", EnumColor.BLACK);
    public static final Block OAK_TRAPDOOR = register("oak_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block SPRUCE_TRAPDOOR = register("spruce_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.SPRUCE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block BIRCH_TRAPDOOR = register("birch_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.BIRCH, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block JUNGLE_TRAPDOOR = register("jungle_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.JUNGLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block ACACIA_TRAPDOOR = register("acacia_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.ACACIA, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block CHERRY_TRAPDOOR = register("cherry_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.CHERRY, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block DARK_OAK_TRAPDOOR = register("dark_oak_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.DARK_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block PALE_OAK_TRAPDOOR = register("pale_oak_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.PALE_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block MANGROVE_TRAPDOOR = register("mangrove_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.MANGROVE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block BAMBOO_TRAPDOOR = register("bamboo_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.BAMBOO, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava());
    public static final Block STONE_BRICKS = register("stone_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block MOSSY_STONE_BRICKS = register("mossy_stone_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block CRACKED_STONE_BRICKS = register("cracked_stone_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block CHISELED_STONE_BRICKS = register("chiseled_stone_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block PACKED_MUD = register("packed_mud", BlockBase.Info.ofLegacyCopy(Blocks.DIRT).strength(1.0F, 3.0F).sound(SoundEffectType.PACKED_MUD));
    public static final Block MUD_BRICKS = register("mud_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 3.0F).sound(SoundEffectType.MUD_BRICKS));
    public static final Block INFESTED_STONE = register("infested_stone", (blockbase_info) -> {
        return new BlockMonsterEggs(Blocks.STONE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY));
    public static final Block INFESTED_COBBLESTONE = register("infested_cobblestone", (blockbase_info) -> {
        return new BlockMonsterEggs(Blocks.COBBLESTONE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY));
    public static final Block INFESTED_STONE_BRICKS = register("infested_stone_bricks", (blockbase_info) -> {
        return new BlockMonsterEggs(Blocks.STONE_BRICKS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY));
    public static final Block INFESTED_MOSSY_STONE_BRICKS = register("infested_mossy_stone_bricks", (blockbase_info) -> {
        return new BlockMonsterEggs(Blocks.MOSSY_STONE_BRICKS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY));
    public static final Block INFESTED_CRACKED_STONE_BRICKS = register("infested_cracked_stone_bricks", (blockbase_info) -> {
        return new BlockMonsterEggs(Blocks.CRACKED_STONE_BRICKS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY));
    public static final Block INFESTED_CHISELED_STONE_BRICKS = register("infested_chiseled_stone_bricks", (blockbase_info) -> {
        return new BlockMonsterEggs(Blocks.CHISELED_STONE_BRICKS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY));
    public static final Block BROWN_MUSHROOM_BLOCK = register("brown_mushroom_block", BlockHugeMushroom::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block RED_MUSHROOM_BLOCK = register("red_mushroom_block", BlockHugeMushroom::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block MUSHROOM_STEM = register("mushroom_stem", BlockHugeMushroom::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOL).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block IRON_BARS = register("iron_bars", BlockIronBars::new, BlockBase.Info.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL).noOcclusion());
    public static final Block CHAIN = register("chain", BlockChain::new, BlockBase.Info.of().forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.CHAIN).noOcclusion());
    public static final Block GLASS_PANE = register("glass_pane", BlockIronBars::new, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block PUMPKIN = register(net.minecraft.references.Blocks.PUMPKIN, BlockPumpkin::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.DIDGERIDOO).strength(1.0F).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block MELON = register(net.minecraft.references.Blocks.MELON, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ATTACHED_PUMPKIN_STEM = register(net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM, (blockbase_info) -> {
        return new BlockStemAttached(net.minecraft.references.Blocks.PUMPKIN_STEM, net.minecraft.references.Blocks.PUMPKIN, Items.PUMPKIN_SEEDS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ATTACHED_MELON_STEM = register(net.minecraft.references.Blocks.ATTACHED_MELON_STEM, (blockbase_info) -> {
        return new BlockStemAttached(net.minecraft.references.Blocks.MELON_STEM, net.minecraft.references.Blocks.MELON, Items.MELON_SEEDS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PUMPKIN_STEM = register(net.minecraft.references.Blocks.PUMPKIN_STEM, (blockbase_info) -> {
        return new BlockStem(net.minecraft.references.Blocks.PUMPKIN, net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM, Items.PUMPKIN_SEEDS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.HARD_CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block MELON_STEM = register(net.minecraft.references.Blocks.MELON_STEM, (blockbase_info) -> {
        return new BlockStem(net.minecraft.references.Blocks.MELON, net.minecraft.references.Blocks.ATTACHED_MELON_STEM, Items.MELON_SEEDS, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.HARD_CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block VINE = register("vine", BlockVine::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().randomTicks().strength(0.2F).sound(SoundEffectType.VINE).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block GLOW_LICHEN = register("glow_lichen", GlowLichenBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.GLOW_LICHEN).replaceable().noCollission().strength(0.2F).sound(SoundEffectType.GLOW_LICHEN).lightLevel(GlowLichenBlock.emission(7)).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block RESIN_CLUMP = register("resin_clump", MultifaceBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).replaceable().noCollission().sound(SoundEffectType.RESIN).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block OAK_FENCE_GATE = register("oak_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block BRICK_STAIRS = registerLegacyStair("brick_stairs", Blocks.BRICKS);
    public static final Block STONE_BRICK_STAIRS = registerLegacyStair("stone_brick_stairs", Blocks.STONE_BRICKS);
    public static final Block MUD_BRICK_STAIRS = registerLegacyStair("mud_brick_stairs", Blocks.MUD_BRICKS);
    public static final Block MYCELIUM = register("mycelium", BlockMycel::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).randomTicks().strength(0.6F).sound(SoundEffectType.GRASS));
    public static final Block LILY_PAD = register("lily_pad", BlockWaterLily::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).instabreak().sound(SoundEffectType.LILY_PAD).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block RESIN_BLOCK = register("resin_block", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.RESIN));
    public static final Block RESIN_BRICKS = register("resin_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().sound(SoundEffectType.RESIN_BRICKS).strength(1.5F, 6.0F));
    public static final Block RESIN_BRICK_STAIRS = registerLegacyStair("resin_brick_stairs", Blocks.RESIN_BRICKS);
    public static final Block RESIN_BRICK_SLAB = register("resin_brick_slab", (blockbase_info) -> {
        return new BlockStepAbstract(blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().sound(SoundEffectType.RESIN_BRICKS).strength(1.5F, 6.0F));
    public static final Block RESIN_BRICK_WALL = register("resin_brick_wall", (blockbase_info) -> {
        return new BlockCobbleWall(blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().sound(SoundEffectType.RESIN_BRICKS).strength(1.5F, 6.0F));
    public static final Block CHISELED_RESIN_BRICKS = register("chiseled_resin_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().sound(SoundEffectType.RESIN_BRICKS).strength(1.5F, 6.0F));
    public static final Block NETHER_BRICKS = register("nether_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS));
    public static final Block NETHER_BRICK_FENCE = register("nether_brick_fence", BlockFence::new, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS));
    public static final Block NETHER_BRICK_STAIRS = registerLegacyStair("nether_brick_stairs", Blocks.NETHER_BRICKS);
    public static final Block NETHER_WART = register("nether_wart", BlockNetherWart::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().randomTicks().sound(SoundEffectType.NETHER_WART).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ENCHANTING_TABLE = register("enchanting_table", BlockEnchantmentTable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel((iblockdata) -> {
        return 7;
    }).strength(5.0F, 1200.0F));
    public static final Block BREWING_STAND = register("brewing_stand", BlockBrewingStand::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).strength(0.5F).lightLevel((iblockdata) -> {
        return 1;
    }).noOcclusion());
    public static final Block CAULDRON = register("cauldron", BlockCauldron::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).requiresCorrectToolForDrops().strength(2.0F).noOcclusion());
    public static final Block WATER_CAULDRON = register("water_cauldron", (blockbase_info) -> {
        return new LayeredCauldronBlock(BiomeBase.Precipitation.RAIN, CauldronInteraction.WATER, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CAULDRON));
    public static final Block LAVA_CAULDRON = register("lava_cauldron", LavaCauldronBlock::new, BlockBase.Info.ofLegacyCopy(Blocks.CAULDRON).lightLevel((iblockdata) -> {
        return 15;
    }));
    public static final Block POWDER_SNOW_CAULDRON = register("powder_snow_cauldron", (blockbase_info) -> {
        return new LayeredCauldronBlock(BiomeBase.Precipitation.SNOW, CauldronInteraction.POWDER_SNOW, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CAULDRON));
    public static final Block END_PORTAL = register("end_portal", BlockEnderPortal::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).noCollission().lightLevel((iblockdata) -> {
        return 15;
    }).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(EnumPistonReaction.BLOCK));
    public static final Block END_PORTAL_FRAME = register("end_portal_frame", BlockEnderPortalFrame::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 1;
    }).strength(-1.0F, 3600000.0F).noLootTable());
    public static final Block END_STONE = register("end_stone", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F));
    public static final Block DRAGON_EGG = register("dragon_egg", BlockDragonEgg::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(3.0F, 9.0F).lightLevel((iblockdata) -> {
        return 1;
    }).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block REDSTONE_LAMP = register("redstone_lamp", BlockRedstoneLamp::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).lightLevel(litBlockEmission(15)).strength(0.3F).sound(SoundEffectType.GLASS).isValidSpawn(Blocks::always));
    public static final Block COCOA = register("cocoa", BlockCocoa::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().strength(0.2F, 3.0F).sound(SoundEffectType.WOOD).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SANDSTONE_STAIRS = registerLegacyStair("sandstone_stairs", Blocks.SANDSTONE);
    public static final Block EMERALD_ORE = register("emerald_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(3, 7), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F));
    public static final Block DEEPSLATE_EMERALD_ORE = register("deepslate_emerald_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(3, 7), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.EMERALD_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block ENDER_CHEST = register("ender_chest", BlockEnderChest::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).strength(22.5F, 600.0F).lightLevel((iblockdata) -> {
        return 7;
    }));
    public static final Block TRIPWIRE_HOOK = register("tripwire_hook", BlockTripwireHook::new, BlockBase.Info.of().noCollission().sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block TRIPWIRE = register("tripwire", (blockbase_info) -> {
        return new BlockTripwire(Blocks.TRIPWIRE_HOOK, blockbase_info);
    }, BlockBase.Info.of().noCollission().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block EMERALD_BLOCK = register("emerald_block", BlockBase.Info.of().mapColor(MaterialMapColor.EMERALD).instrument(BlockPropertyInstrument.BIT).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL));
    public static final Block SPRUCE_STAIRS = registerLegacyStair("spruce_stairs", Blocks.SPRUCE_PLANKS);
    public static final Block BIRCH_STAIRS = registerLegacyStair("birch_stairs", Blocks.BIRCH_PLANKS);
    public static final Block JUNGLE_STAIRS = registerLegacyStair("jungle_stairs", Blocks.JUNGLE_PLANKS);
    public static final Block COMMAND_BLOCK = register("command_block", (blockbase_info) -> {
        return new BlockCommand(false, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable());
    public static final Block BEACON = register("beacon", BlockBeacon::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.HAT).strength(3.0F).lightLevel((iblockdata) -> {
        return 15;
    }).noOcclusion().isRedstoneConductor(Blocks::never));
    public static final Block COBBLESTONE_WALL = register("cobblestone_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.COBBLESTONE).forceSolidOn());
    public static final Block MOSSY_COBBLESTONE_WALL = register("mossy_cobblestone_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.COBBLESTONE).forceSolidOn());
    public static final Block FLOWER_POT = register("flower_pot", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.AIR, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_TORCHFLOWER = register("potted_torchflower", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.TORCHFLOWER, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_OAK_SAPLING = register("potted_oak_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.OAK_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_SPRUCE_SAPLING = register("potted_spruce_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.SPRUCE_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_BIRCH_SAPLING = register("potted_birch_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.BIRCH_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_JUNGLE_SAPLING = register("potted_jungle_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.JUNGLE_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_ACACIA_SAPLING = register("potted_acacia_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.ACACIA_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_CHERRY_SAPLING = register("potted_cherry_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.CHERRY_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_DARK_OAK_SAPLING = register("potted_dark_oak_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.DARK_OAK_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_PALE_OAK_SAPLING = register("potted_pale_oak_sapling", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.PALE_OAK_SAPLING, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_MANGROVE_PROPAGULE = register("potted_mangrove_propagule", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.MANGROVE_PROPAGULE, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_FERN = register("potted_fern", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.FERN, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_DANDELION = register("potted_dandelion", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.DANDELION, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_POPPY = register("potted_poppy", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.POPPY, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_BLUE_ORCHID = register("potted_blue_orchid", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.BLUE_ORCHID, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_ALLIUM = register("potted_allium", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.ALLIUM, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_AZURE_BLUET = register("potted_azure_bluet", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.AZURE_BLUET, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_RED_TULIP = register("potted_red_tulip", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.RED_TULIP, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_ORANGE_TULIP = register("potted_orange_tulip", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.ORANGE_TULIP, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_WHITE_TULIP = register("potted_white_tulip", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.WHITE_TULIP, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_PINK_TULIP = register("potted_pink_tulip", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.PINK_TULIP, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_OXEYE_DAISY = register("potted_oxeye_daisy", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.OXEYE_DAISY, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_CORNFLOWER = register("potted_cornflower", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.CORNFLOWER, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_LILY_OF_THE_VALLEY = register("potted_lily_of_the_valley", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.LILY_OF_THE_VALLEY, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_WITHER_ROSE = register("potted_wither_rose", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.WITHER_ROSE, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_RED_MUSHROOM = register("potted_red_mushroom", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.RED_MUSHROOM, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_BROWN_MUSHROOM = register("potted_brown_mushroom", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.BROWN_MUSHROOM, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_DEAD_BUSH = register("potted_dead_bush", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.DEAD_BUSH, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_CACTUS = register("potted_cactus", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.CACTUS, blockbase_info);
    }, flowerPotProperties());
    public static final Block CARROTS = register("carrots", BlockCarrots::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block POTATOES = register("potatoes", BlockPotatoes::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block OAK_BUTTON = register("oak_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.OAK, 30, blockbase_info);
    }, buttonProperties());
    public static final Block SPRUCE_BUTTON = register("spruce_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.SPRUCE, 30, blockbase_info);
    }, buttonProperties());
    public static final Block BIRCH_BUTTON = register("birch_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.BIRCH, 30, blockbase_info);
    }, buttonProperties());
    public static final Block JUNGLE_BUTTON = register("jungle_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.JUNGLE, 30, blockbase_info);
    }, buttonProperties());
    public static final Block ACACIA_BUTTON = register("acacia_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.ACACIA, 30, blockbase_info);
    }, buttonProperties());
    public static final Block CHERRY_BUTTON = register("cherry_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.CHERRY, 30, blockbase_info);
    }, buttonProperties());
    public static final Block DARK_OAK_BUTTON = register("dark_oak_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.DARK_OAK, 30, blockbase_info);
    }, buttonProperties());
    public static final Block PALE_OAK_BUTTON = register("pale_oak_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.PALE_OAK, 30, blockbase_info);
    }, buttonProperties());
    public static final Block MANGROVE_BUTTON = register("mangrove_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.MANGROVE, 30, blockbase_info);
    }, buttonProperties());
    public static final Block BAMBOO_BUTTON = register("bamboo_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.BAMBOO, 30, blockbase_info);
    }, buttonProperties());
    public static final Block SKELETON_SKULL = register("skeleton_skull", (blockbase_info) -> {
        return new BlockSkull(BlockSkull.Type.SKELETON, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.SKELETON).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SKELETON_WALL_SKULL = register("skeleton_wall_skull", (blockbase_info) -> {
        return new BlockSkullWall(BlockSkull.Type.SKELETON, blockbase_info);
    }, wallVariant(Blocks.SKELETON_SKULL, true).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WITHER_SKELETON_SKULL = register("wither_skeleton_skull", BlockWitherSkull::new, BlockBase.Info.of().instrument(BlockPropertyInstrument.WITHER_SKELETON).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WITHER_SKELETON_WALL_SKULL = register("wither_skeleton_wall_skull", BlockWitherSkullWall::new, wallVariant(Blocks.WITHER_SKELETON_SKULL, true).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ZOMBIE_HEAD = register("zombie_head", (blockbase_info) -> {
        return new BlockSkull(BlockSkull.Type.ZOMBIE, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.ZOMBIE).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ZOMBIE_WALL_HEAD = register("zombie_wall_head", (blockbase_info) -> {
        return new BlockSkullWall(BlockSkull.Type.ZOMBIE, blockbase_info);
    }, wallVariant(Blocks.ZOMBIE_HEAD, true).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PLAYER_HEAD = register("player_head", BlockSkullPlayer::new, BlockBase.Info.of().instrument(BlockPropertyInstrument.CUSTOM_HEAD).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PLAYER_WALL_HEAD = register("player_wall_head", BlockSkullPlayerWall::new, wallVariant(Blocks.PLAYER_HEAD, true).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CREEPER_HEAD = register("creeper_head", (blockbase_info) -> {
        return new BlockSkull(BlockSkull.Type.CREEPER, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.CREEPER).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CREEPER_WALL_HEAD = register("creeper_wall_head", (blockbase_info) -> {
        return new BlockSkullWall(BlockSkull.Type.CREEPER, blockbase_info);
    }, wallVariant(Blocks.CREEPER_HEAD, true).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DRAGON_HEAD = register("dragon_head", (blockbase_info) -> {
        return new BlockSkull(BlockSkull.Type.DRAGON, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.DRAGON).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DRAGON_WALL_HEAD = register("dragon_wall_head", (blockbase_info) -> {
        return new BlockSkullWall(BlockSkull.Type.DRAGON, blockbase_info);
    }, wallVariant(Blocks.DRAGON_HEAD, true).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PIGLIN_HEAD = register("piglin_head", (blockbase_info) -> {
        return new BlockSkull(BlockSkull.Type.PIGLIN, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.PIGLIN).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PIGLIN_WALL_HEAD = register("piglin_wall_head", PiglinWallSkullBlock::new, wallVariant(Blocks.PIGLIN_HEAD, true).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ANVIL = register("anvil", BlockAnvil::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.BLOCK));
    public static final Block CHIPPED_ANVIL = register("chipped_anvil", BlockAnvil::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.BLOCK));
    public static final Block DAMAGED_ANVIL = register("damaged_anvil", BlockAnvil::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.BLOCK));
    public static final Block TRAPPED_CHEST = register("trapped_chest", BlockChestTrapped::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = register("light_weighted_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateWeighted(15, BlockSetType.GOLD, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).forceSolidOn().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = register("heavy_weighted_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateWeighted(150, BlockSetType.IRON, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).forceSolidOn().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block COMPARATOR = register("comparator", BlockRedstoneComparator::new, BlockBase.Info.of().instabreak().sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DAYLIGHT_DETECTOR = register("daylight_detector", BlockDaylightDetector::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block REDSTONE_BLOCK = register("redstone_block", BlockPowered::new, BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL).isRedstoneConductor(Blocks::never));
    public static final Block NETHER_QUARTZ_ORE = register("nether_quartz_ore", (blockbase_info) -> {
        return new DropExperienceBlock(UniformInt.of(2, 5), blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundEffectType.NETHER_ORE));
    public static final Block HOPPER = register("hopper", BlockHopper::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 4.8F).sound(SoundEffectType.METAL).noOcclusion());
    public static final Block QUARTZ_BLOCK = register("quartz_block", BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block CHISELED_QUARTZ_BLOCK = register("chiseled_quartz_block", BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block QUARTZ_PILLAR = register("quartz_pillar", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block QUARTZ_STAIRS = registerLegacyStair("quartz_stairs", Blocks.QUARTZ_BLOCK);
    public static final Block ACTIVATOR_RAIL = register("activator_rail", BlockPoweredRail::new, BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL));
    public static final Block DROPPER = register("dropper", BlockDropper::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F));
    public static final Block WHITE_TERRACOTTA = register("white_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block ORANGE_TERRACOTTA = register("orange_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block MAGENTA_TERRACOTTA = register("magenta_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block LIGHT_BLUE_TERRACOTTA = register("light_blue_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block YELLOW_TERRACOTTA = register("yellow_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block LIME_TERRACOTTA = register("lime_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block PINK_TERRACOTTA = register("pink_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block GRAY_TERRACOTTA = register("gray_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block LIGHT_GRAY_TERRACOTTA = register("light_gray_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block CYAN_TERRACOTTA = register("cyan_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block PURPLE_TERRACOTTA = register("purple_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block BLUE_TERRACOTTA = register("blue_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block BROWN_TERRACOTTA = register("brown_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BROWN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block GREEN_TERRACOTTA = register("green_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block RED_TERRACOTTA = register("red_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block BLACK_TERRACOTTA = register("black_terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block WHITE_STAINED_GLASS_PANE = register("white_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.WHITE, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block ORANGE_STAINED_GLASS_PANE = register("orange_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.ORANGE, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block MAGENTA_STAINED_GLASS_PANE = register("magenta_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.MAGENTA, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = register("light_blue_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.LIGHT_BLUE, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block YELLOW_STAINED_GLASS_PANE = register("yellow_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.YELLOW, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block LIME_STAINED_GLASS_PANE = register("lime_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.LIME, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block PINK_STAINED_GLASS_PANE = register("pink_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.PINK, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block GRAY_STAINED_GLASS_PANE = register("gray_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.GRAY, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = register("light_gray_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.LIGHT_GRAY, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block CYAN_STAINED_GLASS_PANE = register("cyan_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.CYAN, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block PURPLE_STAINED_GLASS_PANE = register("purple_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.PURPLE, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block BLUE_STAINED_GLASS_PANE = register("blue_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.BLUE, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block BROWN_STAINED_GLASS_PANE = register("brown_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.BROWN, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block GREEN_STAINED_GLASS_PANE = register("green_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.GREEN, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block RED_STAINED_GLASS_PANE = register("red_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.RED, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block BLACK_STAINED_GLASS_PANE = register("black_stained_glass_pane", (blockbase_info) -> {
        return new BlockStainedGlassPane(EnumColor.BLACK, blockbase_info);
    }, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion());
    public static final Block ACACIA_STAIRS = registerLegacyStair("acacia_stairs", Blocks.ACACIA_PLANKS);
    public static final Block CHERRY_STAIRS = registerLegacyStair("cherry_stairs", Blocks.CHERRY_PLANKS);
    public static final Block DARK_OAK_STAIRS = registerLegacyStair("dark_oak_stairs", Blocks.DARK_OAK_PLANKS);
    public static final Block PALE_OAK_STAIRS = registerLegacyStair("pale_oak_stairs", Blocks.PALE_OAK_PLANKS);
    public static final Block MANGROVE_STAIRS = registerLegacyStair("mangrove_stairs", Blocks.MANGROVE_PLANKS);
    public static final Block BAMBOO_STAIRS = registerLegacyStair("bamboo_stairs", Blocks.BAMBOO_PLANKS);
    public static final Block BAMBOO_MOSAIC_STAIRS = registerLegacyStair("bamboo_mosaic_stairs", Blocks.BAMBOO_MOSAIC);
    public static final Block SLIME_BLOCK = register("slime_block", BlockSlime::new, BlockBase.Info.of().mapColor(MaterialMapColor.GRASS).friction(0.8F).sound(SoundEffectType.SLIME_BLOCK).noOcclusion());
    public static final Block BARRIER = register("barrier", BlockBarrier::new, BlockBase.Info.of().strength(-1.0F, 3600000.8F).mapColor(waterloggedMapColor(MaterialMapColor.NONE)).noLootTable().noOcclusion().isValidSpawn(Blocks::never).noTerrainParticles().pushReaction(EnumPistonReaction.BLOCK));
    public static final Block LIGHT = register("light", LightBlock::new, BlockBase.Info.of().replaceable().strength(-1.0F, 3600000.8F).mapColor(waterloggedMapColor(MaterialMapColor.NONE)).noLootTable().noOcclusion().lightLevel(LightBlock.LIGHT_EMISSION));
    public static final Block IRON_TRAPDOOR = register("iron_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.IRON, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).noOcclusion().isValidSpawn(Blocks::never));
    public static final Block PRISMARINE = register("prismarine", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block PRISMARINE_BRICKS = register("prismarine_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block DARK_PRISMARINE = register("dark_prismarine", BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block PRISMARINE_STAIRS = registerLegacyStair("prismarine_stairs", Blocks.PRISMARINE);
    public static final Block PRISMARINE_BRICK_STAIRS = registerLegacyStair("prismarine_brick_stairs", Blocks.PRISMARINE_BRICKS);
    public static final Block DARK_PRISMARINE_STAIRS = registerLegacyStair("dark_prismarine_stairs", Blocks.DARK_PRISMARINE);
    public static final Block PRISMARINE_SLAB = register("prismarine_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block PRISMARINE_BRICK_SLAB = register("prismarine_brick_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block DARK_PRISMARINE_SLAB = register("dark_prismarine_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block SEA_LANTERN = register("sea_lantern", BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 15;
    }).isRedstoneConductor(Blocks::never));
    public static final Block HAY_BLOCK = register("hay_block", BlockHay::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BANJO).strength(0.5F).sound(SoundEffectType.GRASS));
    public static final Block WHITE_CARPET = register("white_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.WHITE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block ORANGE_CARPET = register("orange_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.ORANGE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block MAGENTA_CARPET = register("magenta_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.MAGENTA, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block LIGHT_BLUE_CARPET = register("light_blue_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.LIGHT_BLUE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_BLUE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block YELLOW_CARPET = register("yellow_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.YELLOW, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block LIME_CARPET = register("lime_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.LIME, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GREEN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block PINK_CARPET = register("pink_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.PINK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block GRAY_CARPET = register("gray_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.GRAY, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block LIGHT_GRAY_CARPET = register("light_gray_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.LIGHT_GRAY, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block CYAN_CARPET = register("cyan_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.CYAN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block PURPLE_CARPET = register("purple_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.PURPLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block BLUE_CARPET = register("blue_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.BLUE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block BROWN_CARPET = register("brown_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.BROWN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block GREEN_CARPET = register("green_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.GREEN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block RED_CARPET = register("red_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.RED, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block BLACK_CARPET = register("black_carpet", (blockbase_info) -> {
        return new BlockCarpet(EnumColor.BLACK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava());
    public static final Block TERRACOTTA = register("terracotta", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F));
    public static final Block COAL_BLOCK = register("coal_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F));
    public static final Block PACKED_ICE = register("packed_ice", BlockBase.Info.of().mapColor(MaterialMapColor.ICE).instrument(BlockPropertyInstrument.CHIME).friction(0.98F).strength(0.5F).sound(SoundEffectType.GLASS));
    public static final Block SUNFLOWER = register("sunflower", BlockTallPlantFlower::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block LILAC = register("lilac", BlockTallPlantFlower::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ROSE_BUSH = register("rose_bush", BlockTallPlantFlower::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PEONY = register("peony", BlockTallPlantFlower::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block TALL_GRASS = register("tall_grass", BlockTallPlant::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block LARGE_FERN = register("large_fern", BlockTallPlant::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WHITE_BANNER = register("white_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.WHITE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block ORANGE_BANNER = register("orange_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.ORANGE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block MAGENTA_BANNER = register("magenta_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.MAGENTA, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block LIGHT_BLUE_BANNER = register("light_blue_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.LIGHT_BLUE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block YELLOW_BANNER = register("yellow_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.YELLOW, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block LIME_BANNER = register("lime_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.LIME, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block PINK_BANNER = register("pink_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.PINK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block GRAY_BANNER = register("gray_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.GRAY, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block LIGHT_GRAY_BANNER = register("light_gray_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.LIGHT_GRAY, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block CYAN_BANNER = register("cyan_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.CYAN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block PURPLE_BANNER = register("purple_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.PURPLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BLUE_BANNER = register("blue_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.BLUE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BROWN_BANNER = register("brown_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.BROWN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block GREEN_BANNER = register("green_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.GREEN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block RED_BANNER = register("red_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.RED, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BLACK_BANNER = register("black_banner", (blockbase_info) -> {
        return new BlockBanner(EnumColor.BLACK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block WHITE_WALL_BANNER = register("white_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.WHITE, blockbase_info);
    }, wallVariant(Blocks.WHITE_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block ORANGE_WALL_BANNER = register("orange_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.ORANGE, blockbase_info);
    }, wallVariant(Blocks.ORANGE_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block MAGENTA_WALL_BANNER = register("magenta_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.MAGENTA, blockbase_info);
    }, wallVariant(Blocks.MAGENTA_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block LIGHT_BLUE_WALL_BANNER = register("light_blue_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.LIGHT_BLUE, blockbase_info);
    }, wallVariant(Blocks.LIGHT_BLUE_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block YELLOW_WALL_BANNER = register("yellow_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.YELLOW, blockbase_info);
    }, wallVariant(Blocks.YELLOW_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block LIME_WALL_BANNER = register("lime_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.LIME, blockbase_info);
    }, wallVariant(Blocks.LIME_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block PINK_WALL_BANNER = register("pink_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.PINK, blockbase_info);
    }, wallVariant(Blocks.PINK_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block GRAY_WALL_BANNER = register("gray_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.GRAY, blockbase_info);
    }, wallVariant(Blocks.GRAY_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block LIGHT_GRAY_WALL_BANNER = register("light_gray_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.LIGHT_GRAY, blockbase_info);
    }, wallVariant(Blocks.LIGHT_GRAY_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block CYAN_WALL_BANNER = register("cyan_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.CYAN, blockbase_info);
    }, wallVariant(Blocks.CYAN_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block PURPLE_WALL_BANNER = register("purple_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.PURPLE, blockbase_info);
    }, wallVariant(Blocks.PURPLE_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BLUE_WALL_BANNER = register("blue_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.BLUE, blockbase_info);
    }, wallVariant(Blocks.BLUE_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BROWN_WALL_BANNER = register("brown_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.BROWN, blockbase_info);
    }, wallVariant(Blocks.BROWN_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block GREEN_WALL_BANNER = register("green_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.GREEN, blockbase_info);
    }, wallVariant(Blocks.GREEN_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block RED_WALL_BANNER = register("red_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.RED, blockbase_info);
    }, wallVariant(Blocks.RED_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BLACK_WALL_BANNER = register("black_wall_banner", (blockbase_info) -> {
        return new BlockBannerWall(EnumColor.BLACK, blockbase_info);
    }, wallVariant(Blocks.BLACK_BANNER, true).mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block RED_SANDSTONE = register("red_sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block CHISELED_RED_SANDSTONE = register("chiseled_red_sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block CUT_RED_SANDSTONE = register("cut_red_sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F));
    public static final Block RED_SANDSTONE_STAIRS = registerLegacyStair("red_sandstone_stairs", Blocks.RED_SANDSTONE);
    public static final Block OAK_SLAB = register("oak_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block SPRUCE_SLAB = register("spruce_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BIRCH_SLAB = register("birch_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block JUNGLE_SLAB = register("jungle_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block ACACIA_SLAB = register("acacia_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block CHERRY_SLAB = register("cherry_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava());
    public static final Block DARK_OAK_SLAB = register("dark_oak_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block PALE_OAK_SLAB = register("pale_oak_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block MANGROVE_SLAB = register("mangrove_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BAMBOO_SLAB = register("bamboo_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava());
    public static final Block BAMBOO_MOSAIC_SLAB = register("bamboo_mosaic_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava());
    public static final Block STONE_SLAB = register("stone_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block SMOOTH_STONE_SLAB = register("smooth_stone_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block SANDSTONE_SLAB = register("sandstone_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block CUT_SANDSTONE_SLAB = register("cut_sandstone_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block PETRIFIED_OAK_SLAB = register("petrified_oak_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block COBBLESTONE_SLAB = register("cobblestone_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block BRICK_SLAB = register("brick_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block STONE_BRICK_SLAB = register("stone_brick_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block MUD_BRICK_SLAB = register("mud_brick_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 3.0F).sound(SoundEffectType.MUD_BRICKS));
    public static final Block NETHER_BRICK_SLAB = register("nether_brick_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS));
    public static final Block QUARTZ_SLAB = register("quartz_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block RED_SANDSTONE_SLAB = register("red_sandstone_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block CUT_RED_SANDSTONE_SLAB = register("cut_red_sandstone_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block PURPUR_SLAB = register("purpur_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block SMOOTH_STONE = register("smooth_stone", BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block SMOOTH_SANDSTONE = register("smooth_sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block SMOOTH_QUARTZ = register("smooth_quartz", BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block SMOOTH_RED_SANDSTONE = register("smooth_red_sandstone", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F));
    public static final Block SPRUCE_FENCE_GATE = register("spruce_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.SPRUCE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block BIRCH_FENCE_GATE = register("birch_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.BIRCH, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block JUNGLE_FENCE_GATE = register("jungle_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.JUNGLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block ACACIA_FENCE_GATE = register("acacia_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.ACACIA, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block CHERRY_FENCE_GATE = register("cherry_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.CHERRY, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block DARK_OAK_FENCE_GATE = register("dark_oak_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.DARK_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block PALE_OAK_FENCE_GATE = register("pale_oak_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.PALE_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block MANGROVE_FENCE_GATE = register("mangrove_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.MANGROVE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block BAMBOO_FENCE_GATE = register("bamboo_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.BAMBOO, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());
    public static final Block SPRUCE_FENCE = register("spruce_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD));
    public static final Block BIRCH_FENCE = register("birch_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD));
    public static final Block JUNGLE_FENCE = register("jungle_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD));
    public static final Block ACACIA_FENCE = register("acacia_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD));
    public static final Block CHERRY_FENCE = register("cherry_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.CHERRY_WOOD));
    public static final Block DARK_OAK_FENCE = register("dark_oak_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD));
    public static final Block PALE_OAK_FENCE = register("pale_oak_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD));
    public static final Block MANGROVE_FENCE = register("mangrove_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD));
    public static final Block BAMBOO_FENCE = register("bamboo_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava());
    public static final Block SPRUCE_DOOR = register("spruce_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.SPRUCE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BIRCH_DOOR = register("birch_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.BIRCH, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block JUNGLE_DOOR = register("jungle_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.JUNGLE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ACACIA_DOOR = register("acacia_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.ACACIA, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CHERRY_DOOR = register("cherry_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.CHERRY, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DARK_OAK_DOOR = register("dark_oak_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.DARK_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PALE_OAK_DOOR = register("pale_oak_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.PALE_OAK, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block MANGROVE_DOOR = register("mangrove_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.MANGROVE, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BAMBOO_DOOR = register("bamboo_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.BAMBOO, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block END_ROD = register("end_rod", BlockEndRod::new, BlockBase.Info.of().forceSolidOff().instabreak().lightLevel((iblockdata) -> {
        return 14;
    }).sound(SoundEffectType.WOOD).noOcclusion());
    public static final Block CHORUS_PLANT = register("chorus_plant", BlockChorusFruit::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).forceSolidOff().strength(0.4F).sound(SoundEffectType.WOOD).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CHORUS_FLOWER = register("chorus_flower", (blockbase_info) -> {
        return new BlockChorusFlower(Blocks.CHORUS_PLANT, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).forceSolidOff().randomTicks().strength(0.4F).sound(SoundEffectType.WOOD).noOcclusion().isValidSpawn(Blocks::never).pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never));
    public static final Block PURPUR_BLOCK = register("purpur_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block PURPUR_PILLAR = register("purpur_pillar", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block PURPUR_STAIRS = registerLegacyStair("purpur_stairs", Blocks.PURPUR_BLOCK);
    public static final Block END_STONE_BRICKS = register("end_stone_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F));
    public static final Block TORCHFLOWER_CROP = register("torchflower_crop", TorchflowerCropBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PITCHER_CROP = register("pitcher_crop", PitcherCropBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PITCHER_PLANT = register("pitcher_plant", BlockTallPlant::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.CROP).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BEETROOTS = register("beetroots", BlockBeetroot::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DIRT_PATH = register("dirt_path", BlockGrassPath::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.65F).sound(SoundEffectType.GRASS).isViewBlocking(Blocks::always).isSuffocating(Blocks::always));
    public static final Block END_GATEWAY = register("end_gateway", BlockEndGateway::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).noCollission().lightLevel((iblockdata) -> {
        return 15;
    }).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(EnumPistonReaction.BLOCK));
    public static final Block REPEATING_COMMAND_BLOCK = register("repeating_command_block", (blockbase_info) -> {
        return new BlockCommand(false, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable());
    public static final Block CHAIN_COMMAND_BLOCK = register("chain_command_block", (blockbase_info) -> {
        return new BlockCommand(true, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable());
    public static final Block FROSTED_ICE = register("frosted_ice", BlockIceFrost::new, BlockBase.Info.of().mapColor(MaterialMapColor.ICE).friction(0.98F).strength(0.5F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn((iblockdata, iblockaccess, blockposition, entitytypes) -> {
        return entitytypes == EntityTypes.POLAR_BEAR;
    }).isRedstoneConductor(Blocks::never));
    public static final Block MAGMA_BLOCK = register("magma_block", BlockMagma::new, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel((iblockdata) -> {
        return 3;
    }).strength(0.5F).isValidSpawn((iblockdata, iblockaccess, blockposition, entitytypes) -> {
        return entitytypes.fireImmune();
    }).hasPostProcess(Blocks::always).emissiveRendering(Blocks::always));
    public static final Block NETHER_WART_BLOCK = register("nether_wart_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(1.0F).sound(SoundEffectType.WART_BLOCK));
    public static final Block RED_NETHER_BRICKS = register("red_nether_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS));
    public static final Block BONE_BLOCK = register("bone_block", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.XYLOPHONE).requiresCorrectToolForDrops().strength(2.0F).sound(SoundEffectType.BONE_BLOCK));
    public static final Block STRUCTURE_VOID = register("structure_void", BlockStructureVoid::new, BlockBase.Info.of().replaceable().noCollission().noLootTable().noTerrainParticles().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block OBSERVER = register("observer", BlockObserver::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).strength(3.0F).requiresCorrectToolForDrops().isRedstoneConductor(Blocks::never));
    public static final Block SHULKER_BOX = register("shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox((EnumColor) null, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_PURPLE));
    public static final Block WHITE_SHULKER_BOX = register("white_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.WHITE, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.SNOW));
    public static final Block ORANGE_SHULKER_BOX = register("orange_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.ORANGE, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_ORANGE));
    public static final Block MAGENTA_SHULKER_BOX = register("magenta_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.MAGENTA, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_MAGENTA));
    public static final Block LIGHT_BLUE_SHULKER_BOX = register("light_blue_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.LIGHT_BLUE, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_LIGHT_BLUE));
    public static final Block YELLOW_SHULKER_BOX = register("yellow_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.YELLOW, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_YELLOW));
    public static final Block LIME_SHULKER_BOX = register("lime_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.LIME, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_LIGHT_GREEN));
    public static final Block PINK_SHULKER_BOX = register("pink_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.PINK, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_PINK));
    public static final Block GRAY_SHULKER_BOX = register("gray_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.GRAY, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_GRAY));
    public static final Block LIGHT_GRAY_SHULKER_BOX = register("light_gray_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.LIGHT_GRAY, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_LIGHT_GRAY));
    public static final Block CYAN_SHULKER_BOX = register("cyan_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.CYAN, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_CYAN));
    public static final Block PURPLE_SHULKER_BOX = register("purple_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.PURPLE, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.TERRACOTTA_PURPLE));
    public static final Block BLUE_SHULKER_BOX = register("blue_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.BLUE, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_BLUE));
    public static final Block BROWN_SHULKER_BOX = register("brown_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.BROWN, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_BROWN));
    public static final Block GREEN_SHULKER_BOX = register("green_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.GREEN, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_GREEN));
    public static final Block RED_SHULKER_BOX = register("red_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.RED, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_RED));
    public static final Block BLACK_SHULKER_BOX = register("black_shulker_box", (blockbase_info) -> {
        return new BlockShulkerBox(EnumColor.BLACK, blockbase_info);
    }, shulkerBoxProperties(MaterialMapColor.COLOR_BLACK));
    public static final Block WHITE_GLAZED_TERRACOTTA = register("white_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.WHITE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block ORANGE_GLAZED_TERRACOTTA = register("orange_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block MAGENTA_GLAZED_TERRACOTTA = register("magenta_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = register("light_blue_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.LIGHT_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block YELLOW_GLAZED_TERRACOTTA = register("yellow_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block LIME_GLAZED_TERRACOTTA = register("lime_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.LIME).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block PINK_GLAZED_TERRACOTTA = register("pink_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block GRAY_GLAZED_TERRACOTTA = register("gray_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = register("light_gray_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block CYAN_GLAZED_TERRACOTTA = register("cyan_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block PURPLE_GLAZED_TERRACOTTA = register("purple_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block BLUE_GLAZED_TERRACOTTA = register("blue_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block BROWN_GLAZED_TERRACOTTA = register("brown_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.BROWN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block GREEN_GLAZED_TERRACOTTA = register("green_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block RED_GLAZED_TERRACOTTA = register("red_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block BLACK_GLAZED_TERRACOTTA = register("black_glazed_terracotta", BlockGlazedTerracotta::new, BlockBase.Info.of().mapColor(EnumColor.BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY));
    public static final Block WHITE_CONCRETE = register("white_concrete", BlockBase.Info.of().mapColor(EnumColor.WHITE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block ORANGE_CONCRETE = register("orange_concrete", BlockBase.Info.of().mapColor(EnumColor.ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block MAGENTA_CONCRETE = register("magenta_concrete", BlockBase.Info.of().mapColor(EnumColor.MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block LIGHT_BLUE_CONCRETE = register("light_blue_concrete", BlockBase.Info.of().mapColor(EnumColor.LIGHT_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block YELLOW_CONCRETE = register("yellow_concrete", BlockBase.Info.of().mapColor(EnumColor.YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block LIME_CONCRETE = register("lime_concrete", BlockBase.Info.of().mapColor(EnumColor.LIME).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block PINK_CONCRETE = register("pink_concrete", BlockBase.Info.of().mapColor(EnumColor.PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block GRAY_CONCRETE = register("gray_concrete", BlockBase.Info.of().mapColor(EnumColor.GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block LIGHT_GRAY_CONCRETE = register("light_gray_concrete", BlockBase.Info.of().mapColor(EnumColor.LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block CYAN_CONCRETE = register("cyan_concrete", BlockBase.Info.of().mapColor(EnumColor.CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block PURPLE_CONCRETE = register("purple_concrete", BlockBase.Info.of().mapColor(EnumColor.PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block BLUE_CONCRETE = register("blue_concrete", BlockBase.Info.of().mapColor(EnumColor.BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block BROWN_CONCRETE = register("brown_concrete", BlockBase.Info.of().mapColor(EnumColor.BROWN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block GREEN_CONCRETE = register("green_concrete", BlockBase.Info.of().mapColor(EnumColor.GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block RED_CONCRETE = register("red_concrete", BlockBase.Info.of().mapColor(EnumColor.RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block BLACK_CONCRETE = register("black_concrete", BlockBase.Info.of().mapColor(EnumColor.BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F));
    public static final Block WHITE_CONCRETE_POWDER = register("white_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.WHITE_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.WHITE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block ORANGE_CONCRETE_POWDER = register("orange_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.ORANGE_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.ORANGE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block MAGENTA_CONCRETE_POWDER = register("magenta_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.MAGENTA_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.MAGENTA).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block LIGHT_BLUE_CONCRETE_POWDER = register("light_blue_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.LIGHT_BLUE_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.LIGHT_BLUE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block YELLOW_CONCRETE_POWDER = register("yellow_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.YELLOW_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.YELLOW).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block LIME_CONCRETE_POWDER = register("lime_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.LIME_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.LIME).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block PINK_CONCRETE_POWDER = register("pink_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.PINK_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.PINK).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block GRAY_CONCRETE_POWDER = register("gray_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.GRAY_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.GRAY).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block LIGHT_GRAY_CONCRETE_POWDER = register("light_gray_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.LIGHT_GRAY_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.LIGHT_GRAY).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block CYAN_CONCRETE_POWDER = register("cyan_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.CYAN_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.CYAN).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block PURPLE_CONCRETE_POWDER = register("purple_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.PURPLE_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.PURPLE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block BLUE_CONCRETE_POWDER = register("blue_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.BLUE_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.BLUE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block BROWN_CONCRETE_POWDER = register("brown_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.BROWN_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.BROWN).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block GREEN_CONCRETE_POWDER = register("green_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.GREEN_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.GREEN).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block RED_CONCRETE_POWDER = register("red_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.RED_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.RED).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block BLACK_CONCRETE_POWDER = register("black_concrete_powder", (blockbase_info) -> {
        return new BlockConcretePowder(Blocks.BLACK_CONCRETE, blockbase_info);
    }, BlockBase.Info.of().mapColor(EnumColor.BLACK).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND));
    public static final Block KELP = register("kelp", BlockKelp::new, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).noCollission().randomTicks().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block KELP_PLANT = register("kelp_plant", BlockKelpPlant::new, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DRIED_KELP_BLOCK = register("dried_kelp_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.5F, 2.5F).sound(SoundEffectType.GRASS));
    public static final Block TURTLE_EGG = register("turtle_egg", BlockTurtleEgg::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().strength(0.5F).sound(SoundEffectType.METAL).randomTicks().noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SNIFFER_EGG = register("sniffer_egg", SnifferEggBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(0.5F).sound(SoundEffectType.METAL).noOcclusion());
    public static final Block DEAD_TUBE_CORAL_BLOCK = register("dead_tube_coral_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block DEAD_BRAIN_CORAL_BLOCK = register("dead_brain_coral_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block DEAD_BUBBLE_CORAL_BLOCK = register("dead_bubble_coral_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block DEAD_FIRE_CORAL_BLOCK = register("dead_fire_coral_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block DEAD_HORN_CORAL_BLOCK = register("dead_horn_coral_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block TUBE_CORAL_BLOCK = register("tube_coral_block", (blockbase_info) -> {
        return new BlockCoral(Blocks.DEAD_TUBE_CORAL_BLOCK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK));
    public static final Block BRAIN_CORAL_BLOCK = register("brain_coral_block", (blockbase_info) -> {
        return new BlockCoral(Blocks.DEAD_BRAIN_CORAL_BLOCK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK));
    public static final Block BUBBLE_CORAL_BLOCK = register("bubble_coral_block", (blockbase_info) -> {
        return new BlockCoral(Blocks.DEAD_BUBBLE_CORAL_BLOCK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK));
    public static final Block FIRE_CORAL_BLOCK = register("fire_coral_block", (blockbase_info) -> {
        return new BlockCoral(Blocks.DEAD_FIRE_CORAL_BLOCK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK));
    public static final Block HORN_CORAL_BLOCK = register("horn_coral_block", (blockbase_info) -> {
        return new BlockCoral(Blocks.DEAD_HORN_CORAL_BLOCK, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK));
    public static final Block DEAD_TUBE_CORAL = register("dead_tube_coral", BlockCoralDead::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_BRAIN_CORAL = register("dead_brain_coral", BlockCoralDead::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_BUBBLE_CORAL = register("dead_bubble_coral", BlockCoralDead::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_FIRE_CORAL = register("dead_fire_coral", BlockCoralDead::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_HORN_CORAL = register("dead_horn_coral", BlockCoralDead::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block TUBE_CORAL = register("tube_coral", (blockbase_info) -> {
        return new BlockCoralPlant(Blocks.DEAD_TUBE_CORAL, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BRAIN_CORAL = register("brain_coral", (blockbase_info) -> {
        return new BlockCoralPlant(Blocks.DEAD_BRAIN_CORAL, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BUBBLE_CORAL = register("bubble_coral", (blockbase_info) -> {
        return new BlockCoralPlant(Blocks.DEAD_BUBBLE_CORAL, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block FIRE_CORAL = register("fire_coral", (blockbase_info) -> {
        return new BlockCoralPlant(Blocks.DEAD_FIRE_CORAL, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block HORN_CORAL = register("horn_coral", (blockbase_info) -> {
        return new BlockCoralPlant(Blocks.DEAD_HORN_CORAL, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DEAD_TUBE_CORAL_FAN = register("dead_tube_coral_fan", BlockCoralFanAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_BRAIN_CORAL_FAN = register("dead_brain_coral_fan", BlockCoralFanAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_BUBBLE_CORAL_FAN = register("dead_bubble_coral_fan", BlockCoralFanAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_FIRE_CORAL_FAN = register("dead_fire_coral_fan", BlockCoralFanAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_HORN_CORAL_FAN = register("dead_horn_coral_fan", BlockCoralFanAbstract::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block TUBE_CORAL_FAN = register("tube_coral_fan", (blockbase_info) -> {
        return new BlockCoralFan(Blocks.DEAD_TUBE_CORAL_FAN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BRAIN_CORAL_FAN = register("brain_coral_fan", (blockbase_info) -> {
        return new BlockCoralFan(Blocks.DEAD_BRAIN_CORAL_FAN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BUBBLE_CORAL_FAN = register("bubble_coral_fan", (blockbase_info) -> {
        return new BlockCoralFan(Blocks.DEAD_BUBBLE_CORAL_FAN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block FIRE_CORAL_FAN = register("fire_coral_fan", (blockbase_info) -> {
        return new BlockCoralFan(Blocks.DEAD_FIRE_CORAL_FAN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block HORN_CORAL_FAN = register("horn_coral_fan", (blockbase_info) -> {
        return new BlockCoralFan(Blocks.DEAD_HORN_CORAL_FAN, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block DEAD_TUBE_CORAL_WALL_FAN = register("dead_tube_coral_wall_fan", BlockCoralFanWallAbstract::new, wallVariant(Blocks.DEAD_TUBE_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_BRAIN_CORAL_WALL_FAN = register("dead_brain_coral_wall_fan", BlockCoralFanWallAbstract::new, wallVariant(Blocks.DEAD_BRAIN_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = register("dead_bubble_coral_wall_fan", BlockCoralFanWallAbstract::new, wallVariant(Blocks.DEAD_BUBBLE_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_FIRE_CORAL_WALL_FAN = register("dead_fire_coral_wall_fan", BlockCoralFanWallAbstract::new, wallVariant(Blocks.DEAD_FIRE_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block DEAD_HORN_CORAL_WALL_FAN = register("dead_horn_coral_wall_fan", BlockCoralFanWallAbstract::new, wallVariant(Blocks.DEAD_HORN_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak());
    public static final Block TUBE_CORAL_WALL_FAN = register("tube_coral_wall_fan", (blockbase_info) -> {
        return new BlockCoralFanWall(Blocks.DEAD_TUBE_CORAL_WALL_FAN, blockbase_info);
    }, wallVariant(Blocks.TUBE_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BRAIN_CORAL_WALL_FAN = register("brain_coral_wall_fan", (blockbase_info) -> {
        return new BlockCoralFanWall(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, blockbase_info);
    }, wallVariant(Blocks.BRAIN_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_PINK).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BUBBLE_CORAL_WALL_FAN = register("bubble_coral_wall_fan", (blockbase_info) -> {
        return new BlockCoralFanWall(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, blockbase_info);
    }, wallVariant(Blocks.BUBBLE_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block FIRE_CORAL_WALL_FAN = register("fire_coral_wall_fan", (blockbase_info) -> {
        return new BlockCoralFanWall(Blocks.DEAD_FIRE_CORAL_WALL_FAN, blockbase_info);
    }, wallVariant(Blocks.FIRE_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_RED).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block HORN_CORAL_WALL_FAN = register("horn_coral_wall_fan", (blockbase_info) -> {
        return new BlockCoralFanWall(Blocks.DEAD_HORN_CORAL_WALL_FAN, blockbase_info);
    }, wallVariant(Blocks.HORN_CORAL_FAN, false).mapColor(MaterialMapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SEA_PICKLE = register("sea_pickle", BlockSeaPickle::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).lightLevel((iblockdata) -> {
        return BlockSeaPickle.isDead(iblockdata) ? 0 : 3 + 3 * (Integer) iblockdata.getValue(BlockSeaPickle.PICKLES);
    }).sound(SoundEffectType.SLIME_BLOCK).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BLUE_ICE = register("blue_ice", BlockHalfTransparent::new, BlockBase.Info.of().mapColor(MaterialMapColor.ICE).strength(2.8F).friction(0.989F).sound(SoundEffectType.GLASS));
    public static final Block CONDUIT = register("conduit", BlockConduit::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).forceSolidOn().instrument(BlockPropertyInstrument.HAT).strength(3.0F).lightLevel((iblockdata) -> {
        return 15;
    }).noOcclusion());
    public static final Block BAMBOO_SAPLING = register("bamboo_sapling", BlockBambooSapling::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().randomTicks().instabreak().noCollission().strength(1.0F).sound(SoundEffectType.BAMBOO_SAPLING).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BAMBOO = register("bamboo", BlockBamboo::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOn().randomTicks().instabreak().strength(1.0F).sound(SoundEffectType.BAMBOO).noOcclusion().dynamicShape().offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never));
    public static final Block POTTED_BAMBOO = register("potted_bamboo", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.BAMBOO, blockbase_info);
    }, flowerPotProperties());
    public static final Block VOID_AIR = register("void_air", BlockAir::new, BlockBase.Info.of().replaceable().noCollission().noLootTable().air());
    public static final Block CAVE_AIR = register("cave_air", BlockAir::new, BlockBase.Info.of().replaceable().noCollission().noLootTable().air());
    public static final Block BUBBLE_COLUMN = register("bubble_column", BlockBubbleColumn::new, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().noLootTable().pushReaction(EnumPistonReaction.DESTROY).liquid().sound(SoundEffectType.EMPTY));
    public static final Block POLISHED_GRANITE_STAIRS = registerLegacyStair("polished_granite_stairs", Blocks.POLISHED_GRANITE);
    public static final Block SMOOTH_RED_SANDSTONE_STAIRS = registerLegacyStair("smooth_red_sandstone_stairs", Blocks.SMOOTH_RED_SANDSTONE);
    public static final Block MOSSY_STONE_BRICK_STAIRS = registerLegacyStair("mossy_stone_brick_stairs", Blocks.MOSSY_STONE_BRICKS);
    public static final Block POLISHED_DIORITE_STAIRS = registerLegacyStair("polished_diorite_stairs", Blocks.POLISHED_DIORITE);
    public static final Block MOSSY_COBBLESTONE_STAIRS = registerLegacyStair("mossy_cobblestone_stairs", Blocks.MOSSY_COBBLESTONE);
    public static final Block END_STONE_BRICK_STAIRS = registerLegacyStair("end_stone_brick_stairs", Blocks.END_STONE_BRICKS);
    public static final Block STONE_STAIRS = registerLegacyStair("stone_stairs", Blocks.STONE);
    public static final Block SMOOTH_SANDSTONE_STAIRS = registerLegacyStair("smooth_sandstone_stairs", Blocks.SMOOTH_SANDSTONE);
    public static final Block SMOOTH_QUARTZ_STAIRS = registerLegacyStair("smooth_quartz_stairs", Blocks.SMOOTH_QUARTZ);
    public static final Block GRANITE_STAIRS = registerLegacyStair("granite_stairs", Blocks.GRANITE);
    public static final Block ANDESITE_STAIRS = registerLegacyStair("andesite_stairs", Blocks.ANDESITE);
    public static final Block RED_NETHER_BRICK_STAIRS = registerLegacyStair("red_nether_brick_stairs", Blocks.RED_NETHER_BRICKS);
    public static final Block POLISHED_ANDESITE_STAIRS = registerLegacyStair("polished_andesite_stairs", Blocks.POLISHED_ANDESITE);
    public static final Block DIORITE_STAIRS = registerLegacyStair("diorite_stairs", Blocks.DIORITE);
    public static final Block POLISHED_GRANITE_SLAB = register("polished_granite_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_GRANITE));
    public static final Block SMOOTH_RED_SANDSTONE_SLAB = register("smooth_red_sandstone_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.SMOOTH_RED_SANDSTONE));
    public static final Block MOSSY_STONE_BRICK_SLAB = register("mossy_stone_brick_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.MOSSY_STONE_BRICKS));
    public static final Block POLISHED_DIORITE_SLAB = register("polished_diorite_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_DIORITE));
    public static final Block MOSSY_COBBLESTONE_SLAB = register("mossy_cobblestone_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.MOSSY_COBBLESTONE));
    public static final Block END_STONE_BRICK_SLAB = register("end_stone_brick_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.END_STONE_BRICKS));
    public static final Block SMOOTH_SANDSTONE_SLAB = register("smooth_sandstone_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.SMOOTH_SANDSTONE));
    public static final Block SMOOTH_QUARTZ_SLAB = register("smooth_quartz_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.SMOOTH_QUARTZ));
    public static final Block GRANITE_SLAB = register("granite_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.GRANITE));
    public static final Block ANDESITE_SLAB = register("andesite_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.ANDESITE));
    public static final Block RED_NETHER_BRICK_SLAB = register("red_nether_brick_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.RED_NETHER_BRICKS));
    public static final Block POLISHED_ANDESITE_SLAB = register("polished_andesite_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_ANDESITE));
    public static final Block DIORITE_SLAB = register("diorite_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.DIORITE));
    public static final Block BRICK_WALL = register("brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.BRICKS).forceSolidOn());
    public static final Block PRISMARINE_WALL = register("prismarine_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.PRISMARINE).forceSolidOn());
    public static final Block RED_SANDSTONE_WALL = register("red_sandstone_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.RED_SANDSTONE).forceSolidOn());
    public static final Block MOSSY_STONE_BRICK_WALL = register("mossy_stone_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.MOSSY_STONE_BRICKS).forceSolidOn());
    public static final Block GRANITE_WALL = register("granite_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.GRANITE).forceSolidOn());
    public static final Block STONE_BRICK_WALL = register("stone_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.STONE_BRICKS).forceSolidOn());
    public static final Block MUD_BRICK_WALL = register("mud_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.MUD_BRICKS).forceSolidOn());
    public static final Block NETHER_BRICK_WALL = register("nether_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.NETHER_BRICKS).forceSolidOn());
    public static final Block ANDESITE_WALL = register("andesite_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.ANDESITE).forceSolidOn());
    public static final Block RED_NETHER_BRICK_WALL = register("red_nether_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.RED_NETHER_BRICKS).forceSolidOn());
    public static final Block SANDSTONE_WALL = register("sandstone_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.SANDSTONE).forceSolidOn());
    public static final Block END_STONE_BRICK_WALL = register("end_stone_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.END_STONE_BRICKS).forceSolidOn());
    public static final Block DIORITE_WALL = register("diorite_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.DIORITE).forceSolidOn());
    public static final Block SCAFFOLDING = register("scaffolding", BlockScaffolding::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).noCollission().sound(SoundEffectType.SCAFFOLDING).dynamicShape().isValidSpawn(Blocks::never).pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never));
    public static final Block LOOM = register("loom", BlockLoom::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BARREL = register("barrel", BlockBarrel::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block SMOKER = register("smoker", BlockSmoker::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(13)));
    public static final Block BLAST_FURNACE = register("blast_furnace", BlockBlastFurnace::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(13)));
    public static final Block CARTOGRAPHY_TABLE = register("cartography_table", BlockCartographyTable::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block FLETCHING_TABLE = register("fletching_table", BlockFletchingTable::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block GRINDSTONE = register("grindstone", BlockGrindstone::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.BLOCK));
    public static final Block LECTERN = register("lectern", BlockLectern::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block SMITHING_TABLE = register("smithing_table", BlockSmithingTable::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block STONECUTTER = register("stonecutter", BlockStonecutter::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F));
    public static final Block BELL = register("bell", BlockBell::new, BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).forceSolidOn().strength(5.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block LANTERN = register("lantern", BlockLantern::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundEffectType.LANTERN).lightLevel((iblockdata) -> {
        return 15;
    }).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SOUL_LANTERN = register("soul_lantern", BlockLantern::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundEffectType.LANTERN).lightLevel((iblockdata) -> {
        return 10;
    }).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CAMPFIRE = register("campfire", (blockbase_info) -> {
        return new BlockCampfire(true, 1, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).lightLevel(litBlockEmission(15)).noOcclusion().ignitedByLava());
    public static final Block SOUL_CAMPFIRE = register("soul_campfire", (blockbase_info) -> {
        return new BlockCampfire(false, 2, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).lightLevel(litBlockEmission(10)).noOcclusion().ignitedByLava());
    public static final Block SWEET_BERRY_BUSH = register("sweet_berry_bush", BlockSweetBerryBush::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().noCollission().sound(SoundEffectType.SWEET_BERRY_BUSH).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WARPED_STEM = register("warped_stem", BlockRotatable::new, netherStemProperties(MaterialMapColor.WARPED_STEM));
    public static final Block STRIPPED_WARPED_STEM = register("stripped_warped_stem", BlockRotatable::new, netherStemProperties(MaterialMapColor.WARPED_STEM));
    public static final Block WARPED_HYPHAE = register("warped_hyphae", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM));
    public static final Block STRIPPED_WARPED_HYPHAE = register("stripped_warped_hyphae", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM));
    public static final Block WARPED_NYLIUM = register("warped_nylium", BlockNylium::new, BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_NYLIUM).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundEffectType.NYLIUM).randomTicks());
    public static final Block WARPED_FUNGUS = register("warped_fungus", (blockbase_info) -> {
        return new BlockFungi(TreeFeatures.WARPED_FUNGUS_PLANTED, Blocks.WARPED_NYLIUM, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instabreak().noCollission().sound(SoundEffectType.FUNGUS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WARPED_WART_BLOCK = register("warped_wart_block", BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_WART_BLOCK).strength(1.0F).sound(SoundEffectType.WART_BLOCK));
    public static final Block WARPED_ROOTS = register("warped_roots", BlockRoots::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).replaceable().noCollission().instabreak().sound(SoundEffectType.ROOTS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block NETHER_SPROUTS = register("nether_sprouts", BlockNetherSprouts::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).replaceable().noCollission().instabreak().sound(SoundEffectType.NETHER_SPROUTS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CRIMSON_STEM = register("crimson_stem", BlockRotatable::new, netherStemProperties(MaterialMapColor.CRIMSON_STEM));
    public static final Block STRIPPED_CRIMSON_STEM = register("stripped_crimson_stem", BlockRotatable::new, netherStemProperties(MaterialMapColor.CRIMSON_STEM));
    public static final Block CRIMSON_HYPHAE = register("crimson_hyphae", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM));
    public static final Block STRIPPED_CRIMSON_HYPHAE = register("stripped_crimson_hyphae", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM));
    public static final Block CRIMSON_NYLIUM = register("crimson_nylium", BlockNylium::new, BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_NYLIUM).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundEffectType.NYLIUM).randomTicks());
    public static final Block CRIMSON_FUNGUS = register("crimson_fungus", (blockbase_info) -> {
        return new BlockFungi(TreeFeatures.CRIMSON_FUNGUS_PLANTED, Blocks.CRIMSON_NYLIUM, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instabreak().noCollission().sound(SoundEffectType.FUNGUS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SHROOMLIGHT = register("shroomlight", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(1.0F).sound(SoundEffectType.SHROOMLIGHT).lightLevel((iblockdata) -> {
        return 15;
    }));
    public static final Block WEEPING_VINES = register("weeping_vines", BlockWeepingVines::new, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).randomTicks().noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WEEPING_VINES_PLANT = register("weeping_vines_plant", BlockWeepingVinesPlant::new, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block TWISTING_VINES = register("twisting_vines", BlockTwistingVines::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).randomTicks().noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block TWISTING_VINES_PLANT = register("twisting_vines_plant", BlockTwistingVinesPlant::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CRIMSON_ROOTS = register("crimson_roots", BlockRoots::new, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).replaceable().noCollission().instabreak().sound(SoundEffectType.ROOTS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CRIMSON_PLANKS = register("crimson_planks", BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_STEM).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD));
    public static final Block WARPED_PLANKS = register("warped_planks", BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_STEM).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD));
    public static final Block CRIMSON_SLAB = register("crimson_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD));
    public static final Block WARPED_SLAB = register("warped_slab", BlockStepAbstract::new, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD));
    public static final Block CRIMSON_PRESSURE_PLATE = register("crimson_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.CRIMSON, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WARPED_PRESSURE_PLATE = register("warped_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.WARPED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CRIMSON_FENCE = register("crimson_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD));
    public static final Block WARPED_FENCE = register("warped_fence", BlockFence::new, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD));
    public static final Block CRIMSON_TRAPDOOR = register("crimson_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.CRIMSON, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never));
    public static final Block WARPED_TRAPDOOR = register("warped_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.WARPED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never));
    public static final Block CRIMSON_FENCE_GATE = register("crimson_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.CRIMSON, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F));
    public static final Block WARPED_FENCE_GATE = register("warped_fence_gate", (blockbase_info) -> {
        return new BlockFenceGate(BlockPropertyWood.WARPED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F));
    public static final Block CRIMSON_STAIRS = registerLegacyStair("crimson_stairs", Blocks.CRIMSON_PLANKS);
    public static final Block WARPED_STAIRS = registerLegacyStair("warped_stairs", Blocks.WARPED_PLANKS);
    public static final Block CRIMSON_BUTTON = register("crimson_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.CRIMSON, 30, blockbase_info);
    }, buttonProperties());
    public static final Block WARPED_BUTTON = register("warped_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.WARPED, 30, blockbase_info);
    }, buttonProperties());
    public static final Block CRIMSON_DOOR = register("crimson_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.CRIMSON, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block WARPED_DOOR = register("warped_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.WARPED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CRIMSON_SIGN = register("crimson_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.CRIMSON, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F));
    public static final Block WARPED_SIGN = register("warped_sign", (blockbase_info) -> {
        return new BlockFloorSign(BlockPropertyWood.WARPED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F));
    public static final Block CRIMSON_WALL_SIGN = register("crimson_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.CRIMSON, blockbase_info);
    }, wallVariant(Blocks.CRIMSON_SIGN, true).mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F));
    public static final Block WARPED_WALL_SIGN = register("warped_wall_sign", (blockbase_info) -> {
        return new BlockWallSign(BlockPropertyWood.WARPED, blockbase_info);
    }, wallVariant(Blocks.WARPED_SIGN, true).mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F));
    public static final Block STRUCTURE_BLOCK = register("structure_block", BlockStructure::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable());
    public static final Block JIGSAW = register("jigsaw", BlockJigsaw::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable());
    public static final Block COMPOSTER = register("composter", BlockComposter::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(0.6F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block TARGET = register("target", BlockTarget::new, BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).strength(0.5F).sound(SoundEffectType.GRASS));
    public static final Block BEE_NEST = register("bee_nest", BlockBeehive::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(0.3F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block BEEHIVE = register("beehive", BlockBeehive::new, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(0.6F).sound(SoundEffectType.WOOD).ignitedByLava());
    public static final Block HONEY_BLOCK = register("honey_block", BlockHoney::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).speedFactor(0.4F).jumpFactor(0.5F).noOcclusion().sound(SoundEffectType.HONEY_BLOCK));
    public static final Block HONEYCOMB_BLOCK = register("honeycomb_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(0.6F).sound(SoundEffectType.CORAL_BLOCK));
    public static final Block NETHERITE_BLOCK = register("netherite_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).sound(SoundEffectType.NETHERITE_BLOCK));
    public static final Block ANCIENT_DEBRIS = register("ancient_debris", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(30.0F, 1200.0F).sound(SoundEffectType.ANCIENT_DEBRIS));
    public static final Block CRYING_OBSIDIAN = register("crying_obsidian", BlockCryingObsidian::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel((iblockdata) -> {
        return 10;
    }));
    public static final Block RESPAWN_ANCHOR = register("respawn_anchor", BlockRespawnAnchor::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel((iblockdata) -> {
        return BlockRespawnAnchor.getScaledChargeLevel(iblockdata, 15);
    }));
    public static final Block POTTED_CRIMSON_FUNGUS = register("potted_crimson_fungus", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.CRIMSON_FUNGUS, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_WARPED_FUNGUS = register("potted_warped_fungus", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.WARPED_FUNGUS, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_CRIMSON_ROOTS = register("potted_crimson_roots", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.CRIMSON_ROOTS, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_WARPED_ROOTS = register("potted_warped_roots", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.WARPED_ROOTS, blockbase_info);
    }, flowerPotProperties());
    public static final Block LODESTONE = register("lodestone", BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(3.5F).sound(SoundEffectType.LODESTONE).pushReaction(EnumPistonReaction.BLOCK));
    public static final Block BLACKSTONE = register("blackstone", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block BLACKSTONE_STAIRS = registerLegacyStair("blackstone_stairs", Blocks.BLACKSTONE);
    public static final Block BLACKSTONE_WALL = register("blackstone_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).forceSolidOn());
    public static final Block BLACKSTONE_SLAB = register("blackstone_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).strength(2.0F, 6.0F));
    public static final Block POLISHED_BLACKSTONE = register("polished_blackstone", BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).strength(2.0F, 6.0F));
    public static final Block POLISHED_BLACKSTONE_BRICKS = register("polished_blackstone_bricks", BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE).strength(1.5F, 6.0F));
    public static final Block CRACKED_POLISHED_BLACKSTONE_BRICKS = register("cracked_polished_blackstone_bricks", BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE_BRICKS));
    public static final Block CHISELED_POLISHED_BLACKSTONE = register("chiseled_polished_blackstone", BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE).strength(1.5F, 6.0F));
    public static final Block POLISHED_BLACKSTONE_BRICK_SLAB = register("polished_blackstone_brick_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE_BRICKS).strength(2.0F, 6.0F));
    public static final Block POLISHED_BLACKSTONE_BRICK_STAIRS = registerLegacyStair("polished_blackstone_brick_stairs", Blocks.POLISHED_BLACKSTONE_BRICKS);
    public static final Block POLISHED_BLACKSTONE_BRICK_WALL = register("polished_blackstone_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE_BRICKS).forceSolidOn());
    public static final Block GILDED_BLACKSTONE = register("gilded_blackstone", BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).sound(SoundEffectType.GILDED_BLACKSTONE));
    public static final Block POLISHED_BLACKSTONE_STAIRS = registerLegacyStair("polished_blackstone_stairs", Blocks.POLISHED_BLACKSTONE);
    public static final Block POLISHED_BLACKSTONE_SLAB = register("polished_blackstone_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE));
    public static final Block POLISHED_BLACKSTONE_PRESSURE_PLATE = register("polished_blackstone_pressure_plate", (blockbase_info) -> {
        return new BlockPressurePlateBinary(BlockSetType.POLISHED_BLACKSTONE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block POLISHED_BLACKSTONE_BUTTON = register("polished_blackstone_button", (blockbase_info) -> {
        return new BlockButtonAbstract(BlockSetType.STONE, 20, blockbase_info);
    }, buttonProperties());
    public static final Block POLISHED_BLACKSTONE_WALL = register("polished_blackstone_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE).forceSolidOn());
    public static final Block CHISELED_NETHER_BRICKS = register("chiseled_nether_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS));
    public static final Block CRACKED_NETHER_BRICKS = register("cracked_nether_bricks", BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS));
    public static final Block QUARTZ_BRICKS = register("quartz_bricks", BlockBase.Info.ofLegacyCopy(Blocks.QUARTZ_BLOCK));
    public static final Block CANDLE = register("candle", CandleBlock::new, candleProperties(MaterialMapColor.SAND));
    public static final Block WHITE_CANDLE = register("white_candle", CandleBlock::new, candleProperties(MaterialMapColor.WOOL));
    public static final Block ORANGE_CANDLE = register("orange_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_ORANGE));
    public static final Block MAGENTA_CANDLE = register("magenta_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_MAGENTA));
    public static final Block LIGHT_BLUE_CANDLE = register("light_blue_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_LIGHT_BLUE));
    public static final Block YELLOW_CANDLE = register("yellow_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_YELLOW));
    public static final Block LIME_CANDLE = register("lime_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_LIGHT_GREEN));
    public static final Block PINK_CANDLE = register("pink_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_PINK));
    public static final Block GRAY_CANDLE = register("gray_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_GRAY));
    public static final Block LIGHT_GRAY_CANDLE = register("light_gray_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_LIGHT_GRAY));
    public static final Block CYAN_CANDLE = register("cyan_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_CYAN));
    public static final Block PURPLE_CANDLE = register("purple_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_PURPLE));
    public static final Block BLUE_CANDLE = register("blue_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_BLUE));
    public static final Block BROWN_CANDLE = register("brown_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_BROWN));
    public static final Block GREEN_CANDLE = register("green_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_GREEN));
    public static final Block RED_CANDLE = register("red_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_RED));
    public static final Block BLACK_CANDLE = register("black_candle", CandleBlock::new, candleProperties(MaterialMapColor.COLOR_BLACK));
    public static final Block CANDLE_CAKE = register("candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CAKE).lightLevel(litBlockEmission(3)));
    public static final Block WHITE_CANDLE_CAKE = register("white_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.WHITE_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block ORANGE_CANDLE_CAKE = register("orange_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.ORANGE_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block MAGENTA_CANDLE_CAKE = register("magenta_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.MAGENTA_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block LIGHT_BLUE_CANDLE_CAKE = register("light_blue_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.LIGHT_BLUE_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block YELLOW_CANDLE_CAKE = register("yellow_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.YELLOW_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block LIME_CANDLE_CAKE = register("lime_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.LIME_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block PINK_CANDLE_CAKE = register("pink_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.PINK_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block GRAY_CANDLE_CAKE = register("gray_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.GRAY_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block LIGHT_GRAY_CANDLE_CAKE = register("light_gray_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.LIGHT_GRAY_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block CYAN_CANDLE_CAKE = register("cyan_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.CYAN_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block PURPLE_CANDLE_CAKE = register("purple_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.PURPLE_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block BLUE_CANDLE_CAKE = register("blue_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.BLUE_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block BROWN_CANDLE_CAKE = register("brown_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.BROWN_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block GREEN_CANDLE_CAKE = register("green_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.GREEN_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block RED_CANDLE_CAKE = register("red_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.RED_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block BLACK_CANDLE_CAKE = register("black_candle_cake", (blockbase_info) -> {
        return new CandleCakeBlock(Blocks.BLACK_CANDLE, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE));
    public static final Block AMETHYST_BLOCK = register("amethyst_block", AmethystBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).strength(1.5F).sound(SoundEffectType.AMETHYST).requiresCorrectToolForDrops());
    public static final Block BUDDING_AMETHYST = register("budding_amethyst", BuddingAmethystBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).randomTicks().strength(1.5F).sound(SoundEffectType.AMETHYST).requiresCorrectToolForDrops().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block AMETHYST_CLUSTER = register("amethyst_cluster", (blockbase_info) -> {
        return new AmethystClusterBlock(7.0F, 3.0F, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).forceSolidOn().noOcclusion().sound(SoundEffectType.AMETHYST_CLUSTER).strength(1.5F).lightLevel((iblockdata) -> {
        return 5;
    }).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block LARGE_AMETHYST_BUD = register("large_amethyst_bud", (blockbase_info) -> {
        return new AmethystClusterBlock(5.0F, 3.0F, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.AMETHYST_CLUSTER).sound(SoundEffectType.MEDIUM_AMETHYST_BUD).lightLevel((iblockdata) -> {
        return 4;
    }));
    public static final Block MEDIUM_AMETHYST_BUD = register("medium_amethyst_bud", (blockbase_info) -> {
        return new AmethystClusterBlock(4.0F, 3.0F, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.AMETHYST_CLUSTER).sound(SoundEffectType.LARGE_AMETHYST_BUD).lightLevel((iblockdata) -> {
        return 2;
    }));
    public static final Block SMALL_AMETHYST_BUD = register("small_amethyst_bud", (blockbase_info) -> {
        return new AmethystClusterBlock(3.0F, 4.0F, blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.AMETHYST_CLUSTER).sound(SoundEffectType.SMALL_AMETHYST_BUD).lightLevel((iblockdata) -> {
        return 1;
    }));
    public static final Block TUFF = register("tuff", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.TUFF).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
    public static final Block TUFF_SLAB = register("tuff_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.TUFF));
    public static final Block TUFF_STAIRS = register("tuff_stairs", (blockbase_info) -> {
        return new BlockStairs(Blocks.TUFF.defaultBlockState(), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.TUFF));
    public static final Block TUFF_WALL = register("tuff_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.TUFF).forceSolidOn());
    public static final Block POLISHED_TUFF = register("polished_tuff", BlockBase.Info.ofLegacyCopy(Blocks.TUFF).sound(SoundEffectType.POLISHED_TUFF));
    public static final Block POLISHED_TUFF_SLAB = register("polished_tuff_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_TUFF));
    public static final Block POLISHED_TUFF_STAIRS = register("polished_tuff_stairs", (blockbase_info) -> {
        return new BlockStairs(Blocks.POLISHED_TUFF.defaultBlockState(), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_TUFF));
    public static final Block POLISHED_TUFF_WALL = register("polished_tuff_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_TUFF).forceSolidOn());
    public static final Block CHISELED_TUFF = register("chiseled_tuff", BlockBase.Info.ofLegacyCopy(Blocks.TUFF));
    public static final Block TUFF_BRICKS = register("tuff_bricks", BlockBase.Info.ofLegacyCopy(Blocks.TUFF).sound(SoundEffectType.TUFF_BRICKS));
    public static final Block TUFF_BRICK_SLAB = register("tuff_brick_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS));
    public static final Block TUFF_BRICK_STAIRS = register("tuff_brick_stairs", (blockbase_info) -> {
        return new BlockStairs(Blocks.TUFF_BRICKS.defaultBlockState(), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS));
    public static final Block TUFF_BRICK_WALL = register("tuff_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS).forceSolidOn());
    public static final Block CHISELED_TUFF_BRICKS = register("chiseled_tuff_bricks", BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS));
    public static final Block CALCITE = register("calcite", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.CALCITE).requiresCorrectToolForDrops().strength(0.75F));
    public static final Block TINTED_GLASS = register("tinted_glass", TintedGlassBlock::new, BlockBase.Info.ofLegacyCopy(Blocks.GLASS).mapColor(MaterialMapColor.COLOR_GRAY).noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
    public static final Block POWDER_SNOW = register("powder_snow", PowderSnowBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).strength(0.25F).sound(SoundEffectType.POWDER_SNOW).dynamicShape().noOcclusion().isRedstoneConductor(Blocks::never));
    public static final Block SCULK_SENSOR = register("sculk_sensor", SculkSensorBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).strength(1.5F).sound(SoundEffectType.SCULK_SENSOR).lightLevel((iblockdata) -> {
        return 1;
    }).emissiveRendering((iblockdata, iblockaccess, blockposition) -> {
        return SculkSensorBlock.getPhase(iblockdata) == SculkSensorPhase.ACTIVE;
    }));
    public static final Block CALIBRATED_SCULK_SENSOR = register("calibrated_sculk_sensor", CalibratedSculkSensorBlock::new, BlockBase.Info.ofLegacyCopy(Blocks.SCULK_SENSOR));
    public static final Block SCULK = register("sculk", SculkBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(0.2F).sound(SoundEffectType.SCULK));
    public static final Block SCULK_VEIN = register("sculk_vein", SculkVeinBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).forceSolidOn().noCollission().strength(0.2F).sound(SoundEffectType.SCULK_VEIN).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SCULK_CATALYST = register("sculk_catalyst", SculkCatalystBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundEffectType.SCULK_CATALYST).lightLevel((iblockdata) -> {
        return 6;
    }));
    public static final Block SCULK_SHRIEKER = register("sculk_shrieker", SculkShriekerBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundEffectType.SCULK_SHRIEKER));
    public static final Block COPPER_BLOCK = register("copper_block", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.COPPER));
    public static final Block EXPOSED_COPPER = register("exposed_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY));
    public static final Block WEATHERED_COPPER = register("weathered_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MaterialMapColor.WARPED_STEM));
    public static final Block OXIDIZED_COPPER = register("oxidized_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MaterialMapColor.WARPED_NYLIUM));
    public static final Block COPPER_ORE = register("copper_ore", (blockbase_info) -> {
        return new DropExperienceBlock(ConstantInt.of(0), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.IRON_ORE));
    public static final Block DEEPSLATE_COPPER_ORE = register("deepslate_copper_ore", (blockbase_info) -> {
        return new DropExperienceBlock(ConstantInt.of(0), blockbase_info);
    }, BlockBase.Info.ofLegacyCopy(Blocks.COPPER_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block OXIDIZED_CUT_COPPER = register("oxidized_cut_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER));
    public static final Block WEATHERED_CUT_COPPER = register("weathered_cut_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER));
    public static final Block EXPOSED_CUT_COPPER = register("exposed_cut_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER));
    public static final Block CUT_COPPER = register("cut_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final Block OXIDIZED_CHISELED_COPPER = register("oxidized_chiseled_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER));
    public static final Block WEATHERED_CHISELED_COPPER = register("weathered_chiseled_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER));
    public static final Block EXPOSED_CHISELED_COPPER = register("exposed_chiseled_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER));
    public static final Block CHISELED_COPPER = register("chiseled_copper", (blockbase_info) -> {
        return new WeatheringCopperFullBlock(WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final Block WAXED_OXIDIZED_CHISELED_COPPER = register("waxed_oxidized_chiseled_copper", BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_CHISELED_COPPER));
    public static final Block WAXED_WEATHERED_CHISELED_COPPER = register("waxed_weathered_chiseled_copper", BlockBase.Info.ofFullCopy(Blocks.WEATHERED_CHISELED_COPPER));
    public static final Block WAXED_EXPOSED_CHISELED_COPPER = register("waxed_exposed_chiseled_copper", BlockBase.Info.ofFullCopy(Blocks.EXPOSED_CHISELED_COPPER));
    public static final Block WAXED_CHISELED_COPPER = register("waxed_chiseled_copper", BlockBase.Info.ofFullCopy(Blocks.CHISELED_COPPER));
    public static final Block OXIDIZED_CUT_COPPER_STAIRS = register("oxidized_cut_copper_stairs", (blockbase_info) -> {
        return new WeatheringCopperStairBlock(WeatheringCopper.a.OXIDIZED, Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_CUT_COPPER));
    public static final Block WEATHERED_CUT_COPPER_STAIRS = register("weathered_cut_copper_stairs", (blockbase_info) -> {
        return new WeatheringCopperStairBlock(WeatheringCopper.a.WEATHERED, Blocks.WEATHERED_CUT_COPPER.defaultBlockState(), blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER));
    public static final Block EXPOSED_CUT_COPPER_STAIRS = register("exposed_cut_copper_stairs", (blockbase_info) -> {
        return new WeatheringCopperStairBlock(WeatheringCopper.a.EXPOSED, Blocks.EXPOSED_CUT_COPPER.defaultBlockState(), blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER));
    public static final Block CUT_COPPER_STAIRS = register("cut_copper_stairs", (blockbase_info) -> {
        return new WeatheringCopperStairBlock(WeatheringCopper.a.UNAFFECTED, Blocks.CUT_COPPER.defaultBlockState(), blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final Block OXIDIZED_CUT_COPPER_SLAB = register("oxidized_cut_copper_slab", (blockbase_info) -> {
        return new WeatheringCopperSlabBlock(WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_CUT_COPPER));
    public static final Block WEATHERED_CUT_COPPER_SLAB = register("weathered_cut_copper_slab", (blockbase_info) -> {
        return new WeatheringCopperSlabBlock(WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_CUT_COPPER));
    public static final Block EXPOSED_CUT_COPPER_SLAB = register("exposed_cut_copper_slab", (blockbase_info) -> {
        return new WeatheringCopperSlabBlock(WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_CUT_COPPER));
    public static final Block CUT_COPPER_SLAB = register("cut_copper_slab", (blockbase_info) -> {
        return new WeatheringCopperSlabBlock(WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.CUT_COPPER));
    public static final Block WAXED_COPPER_BLOCK = register("waxed_copper_block", BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final Block WAXED_WEATHERED_COPPER = register("waxed_weathered_copper", BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER));
    public static final Block WAXED_EXPOSED_COPPER = register("waxed_exposed_copper", BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER));
    public static final Block WAXED_OXIDIZED_COPPER = register("waxed_oxidized_copper", BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER));
    public static final Block WAXED_OXIDIZED_CUT_COPPER = register("waxed_oxidized_cut_copper", BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER));
    public static final Block WAXED_WEATHERED_CUT_COPPER = register("waxed_weathered_cut_copper", BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER));
    public static final Block WAXED_EXPOSED_CUT_COPPER = register("waxed_exposed_cut_copper", BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER));
    public static final Block WAXED_CUT_COPPER = register("waxed_cut_copper", BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final Block WAXED_OXIDIZED_CUT_COPPER_STAIRS = registerStair("waxed_oxidized_cut_copper_stairs", Blocks.WAXED_OXIDIZED_CUT_COPPER);
    public static final Block WAXED_WEATHERED_CUT_COPPER_STAIRS = registerStair("waxed_weathered_cut_copper_stairs", Blocks.WAXED_WEATHERED_CUT_COPPER);
    public static final Block WAXED_EXPOSED_CUT_COPPER_STAIRS = registerStair("waxed_exposed_cut_copper_stairs", Blocks.WAXED_EXPOSED_CUT_COPPER);
    public static final Block WAXED_CUT_COPPER_STAIRS = registerStair("waxed_cut_copper_stairs", Blocks.WAXED_CUT_COPPER);
    public static final Block WAXED_OXIDIZED_CUT_COPPER_SLAB = register("waxed_oxidized_cut_copper_slab", BlockStepAbstract::new, BlockBase.Info.ofFullCopy(Blocks.WAXED_OXIDIZED_CUT_COPPER).requiresCorrectToolForDrops());
    public static final Block WAXED_WEATHERED_CUT_COPPER_SLAB = register("waxed_weathered_cut_copper_slab", BlockStepAbstract::new, BlockBase.Info.ofFullCopy(Blocks.WAXED_WEATHERED_CUT_COPPER).requiresCorrectToolForDrops());
    public static final Block WAXED_EXPOSED_CUT_COPPER_SLAB = register("waxed_exposed_cut_copper_slab", BlockStepAbstract::new, BlockBase.Info.ofFullCopy(Blocks.WAXED_EXPOSED_CUT_COPPER).requiresCorrectToolForDrops());
    public static final Block WAXED_CUT_COPPER_SLAB = register("waxed_cut_copper_slab", BlockStepAbstract::new, BlockBase.Info.ofFullCopy(Blocks.WAXED_CUT_COPPER).requiresCorrectToolForDrops());
    public static final Block COPPER_DOOR = register("copper_door", (blockbase_info) -> {
        return new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block EXPOSED_COPPER_DOOR = register("exposed_copper_door", (blockbase_info) -> {
        return new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR).mapColor(Blocks.EXPOSED_COPPER.defaultMapColor()));
    public static final Block OXIDIZED_COPPER_DOOR = register("oxidized_copper_door", (blockbase_info) -> {
        return new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR).mapColor(Blocks.OXIDIZED_COPPER.defaultMapColor()));
    public static final Block WEATHERED_COPPER_DOOR = register("weathered_copper_door", (blockbase_info) -> {
        return new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR).mapColor(Blocks.WEATHERED_COPPER.defaultMapColor()));
    public static final Block WAXED_COPPER_DOOR = register("waxed_copper_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR));
    public static final Block WAXED_EXPOSED_COPPER_DOOR = register("waxed_exposed_copper_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_DOOR));
    public static final Block WAXED_OXIDIZED_COPPER_DOOR = register("waxed_oxidized_copper_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_DOOR));
    public static final Block WAXED_WEATHERED_COPPER_DOOR = register("waxed_weathered_copper_door", (blockbase_info) -> {
        return new BlockDoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_DOOR));
    public static final Block COPPER_TRAPDOOR = register("copper_trapdoor", (blockbase_info) -> {
        return new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion().isValidSpawn(Blocks::never));
    public static final Block EXPOSED_COPPER_TRAPDOOR = register("exposed_copper_trapdoor", (blockbase_info) -> {
        return new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR).mapColor(Blocks.EXPOSED_COPPER.defaultMapColor()));
    public static final Block OXIDIZED_COPPER_TRAPDOOR = register("oxidized_copper_trapdoor", (blockbase_info) -> {
        return new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR).mapColor(Blocks.OXIDIZED_COPPER.defaultMapColor()));
    public static final Block WEATHERED_COPPER_TRAPDOOR = register("weathered_copper_trapdoor", (blockbase_info) -> {
        return new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR).mapColor(Blocks.WEATHERED_COPPER.defaultMapColor()));
    public static final Block WAXED_COPPER_TRAPDOOR = register("waxed_copper_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR));
    public static final Block WAXED_EXPOSED_COPPER_TRAPDOOR = register("waxed_exposed_copper_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_TRAPDOOR));
    public static final Block WAXED_OXIDIZED_COPPER_TRAPDOOR = register("waxed_oxidized_copper_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_TRAPDOOR));
    public static final Block WAXED_WEATHERED_COPPER_TRAPDOOR = register("waxed_weathered_copper_trapdoor", (blockbase_info) -> {
        return new BlockTrapdoor(BlockSetType.COPPER, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_TRAPDOOR));
    public static final Block COPPER_GRATE = register("copper_grate", (blockbase_info) -> {
        return new WeatheringCopperGrateBlock(WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.of().strength(3.0F, 6.0F).sound(SoundEffectType.COPPER_GRATE).mapColor(MaterialMapColor.COLOR_ORANGE).noOcclusion().requiresCorrectToolForDrops().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
    public static final Block EXPOSED_COPPER_GRATE = register("exposed_copper_grate", (blockbase_info) -> {
        return new WeatheringCopperGrateBlock(WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE).mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY));
    public static final Block WEATHERED_COPPER_GRATE = register("weathered_copper_grate", (blockbase_info) -> {
        return new WeatheringCopperGrateBlock(WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE).mapColor(MaterialMapColor.WARPED_STEM));
    public static final Block OXIDIZED_COPPER_GRATE = register("oxidized_copper_grate", (blockbase_info) -> {
        return new WeatheringCopperGrateBlock(WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE).mapColor(MaterialMapColor.WARPED_NYLIUM));
    public static final Block WAXED_COPPER_GRATE = register("waxed_copper_grate", WaterloggedTransparentBlock::new, BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE));
    public static final Block WAXED_EXPOSED_COPPER_GRATE = register("waxed_exposed_copper_grate", WaterloggedTransparentBlock::new, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_GRATE));
    public static final Block WAXED_WEATHERED_COPPER_GRATE = register("waxed_weathered_copper_grate", WaterloggedTransparentBlock::new, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_GRATE));
    public static final Block WAXED_OXIDIZED_COPPER_GRATE = register("waxed_oxidized_copper_grate", WaterloggedTransparentBlock::new, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_GRATE));
    public static final Block COPPER_BULB = register("copper_bulb", (blockbase_info) -> {
        return new WeatheringCopperBulbBlock(WeatheringCopper.a.UNAFFECTED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).sound(SoundEffectType.COPPER_BULB).requiresCorrectToolForDrops().isRedstoneConductor(Blocks::never).lightLevel(litBlockEmission(15)));
    public static final Block EXPOSED_COPPER_BULB = register("exposed_copper_bulb", (blockbase_info) -> {
        return new WeatheringCopperBulbBlock(WeatheringCopper.a.EXPOSED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB).mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).lightLevel(litBlockEmission(12)));
    public static final Block WEATHERED_COPPER_BULB = register("weathered_copper_bulb", (blockbase_info) -> {
        return new WeatheringCopperBulbBlock(WeatheringCopper.a.WEATHERED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB).mapColor(MaterialMapColor.WARPED_STEM).lightLevel(litBlockEmission(8)));
    public static final Block OXIDIZED_COPPER_BULB = register("oxidized_copper_bulb", (blockbase_info) -> {
        return new WeatheringCopperBulbBlock(WeatheringCopper.a.OXIDIZED, blockbase_info);
    }, BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB).mapColor(MaterialMapColor.WARPED_NYLIUM).lightLevel(litBlockEmission(4)));
    public static final Block WAXED_COPPER_BULB = register("waxed_copper_bulb", CopperBulbBlock::new, BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB));
    public static final Block WAXED_EXPOSED_COPPER_BULB = register("waxed_exposed_copper_bulb", CopperBulbBlock::new, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_BULB));
    public static final Block WAXED_WEATHERED_COPPER_BULB = register("waxed_weathered_copper_bulb", CopperBulbBlock::new, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_BULB));
    public static final Block WAXED_OXIDIZED_COPPER_BULB = register("waxed_oxidized_copper_bulb", CopperBulbBlock::new, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_BULB));
    public static final Block LIGHTNING_ROD = register("lightning_rod", LightningRodBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.COPPER).noOcclusion());
    public static final Block POINTED_DRIPSTONE = register("pointed_dripstone", PointedDripstoneBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BROWN).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).noOcclusion().sound(SoundEffectType.POINTED_DRIPSTONE).randomTicks().strength(1.5F, 3.0F).dynamicShape().offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never));
    public static final Block DRIPSTONE_BLOCK = register("dripstone_block", BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BROWN).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.DRIPSTONE_BLOCK).requiresCorrectToolForDrops().strength(1.5F, 1.0F));
    public static final Block CAVE_VINES = register("cave_vines", CaveVinesBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().noCollission().lightLevel(CaveVines.emission(14)).instabreak().sound(SoundEffectType.CAVE_VINES).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block CAVE_VINES_PLANT = register("cave_vines_plant", CaveVinesPlantBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().lightLevel(CaveVines.emission(14)).instabreak().sound(SoundEffectType.CAVE_VINES).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SPORE_BLOSSOM = register("spore_blossom", SporeBlossomBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).instabreak().noCollission().sound(SoundEffectType.SPORE_BLOSSOM).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block AZALEA = register("azalea", AzaleaBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOff().instabreak().sound(SoundEffectType.AZALEA).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block FLOWERING_AZALEA = register("flowering_azalea", AzaleaBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOff().instabreak().sound(SoundEffectType.FLOWERING_AZALEA).noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block MOSS_CARPET = register("moss_carpet", CarpetBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.1F).sound(SoundEffectType.MOSS_CARPET).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PINK_PETALS = register("pink_petals", PinkPetalsBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().sound(SoundEffectType.PINK_PETALS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block MOSS_BLOCK = register("moss_block", (blockbase_info) -> {
        return new BonemealableFeaturePlacerBlock(CaveFeatures.MOSS_PATCH_BONEMEAL, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.1F).sound(SoundEffectType.MOSS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BIG_DRIPLEAF = register("big_dripleaf", BigDripleafBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOff().strength(0.1F).sound(SoundEffectType.BIG_DRIPLEAF).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block BIG_DRIPLEAF_STEM = register("big_dripleaf_stem", BigDripleafStemBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().strength(0.1F).sound(SoundEffectType.BIG_DRIPLEAF).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block SMALL_DRIPLEAF = register("small_dripleaf", SmallDripleafBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.SMALL_DRIPLEAF).offsetType(BlockBase.EnumRandomOffset.XYZ).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block HANGING_ROOTS = register("hanging_roots", HangingRootsBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).replaceable().noCollission().instabreak().sound(SoundEffectType.HANGING_ROOTS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    public static final Block ROOTED_DIRT = register("rooted_dirt", RootedDirtBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.5F).sound(SoundEffectType.ROOTED_DIRT));
    public static final Block MUD = register("mud", MudBlock::new, BlockBase.Info.ofLegacyCopy(Blocks.DIRT).mapColor(MaterialMapColor.TERRACOTTA_CYAN).isValidSpawn(Blocks::always).isRedstoneConductor(Blocks::always).isViewBlocking(Blocks::always).isSuffocating(Blocks::always).sound(SoundEffectType.MUD));
    public static final Block DEEPSLATE = register("deepslate", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.DEEPSLATE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.DEEPSLATE));
    public static final Block COBBLED_DEEPSLATE = register("cobbled_deepslate", BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE).strength(3.5F, 6.0F));
    public static final Block COBBLED_DEEPSLATE_STAIRS = registerLegacyStair("cobbled_deepslate_stairs", Blocks.COBBLED_DEEPSLATE);
    public static final Block COBBLED_DEEPSLATE_SLAB = register("cobbled_deepslate_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE));
    public static final Block COBBLED_DEEPSLATE_WALL = register("cobbled_deepslate_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).forceSolidOn());
    public static final Block POLISHED_DEEPSLATE = register("polished_deepslate", BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.POLISHED_DEEPSLATE));
    public static final Block POLISHED_DEEPSLATE_STAIRS = registerLegacyStair("polished_deepslate_stairs", Blocks.POLISHED_DEEPSLATE);
    public static final Block POLISHED_DEEPSLATE_SLAB = register("polished_deepslate_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_DEEPSLATE));
    public static final Block POLISHED_DEEPSLATE_WALL = register("polished_deepslate_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_DEEPSLATE).forceSolidOn());
    public static final Block DEEPSLATE_TILES = register("deepslate_tiles", BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.DEEPSLATE_TILES));
    public static final Block DEEPSLATE_TILE_STAIRS = registerLegacyStair("deepslate_tile_stairs", Blocks.DEEPSLATE_TILES);
    public static final Block DEEPSLATE_TILE_SLAB = register("deepslate_tile_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_TILES));
    public static final Block DEEPSLATE_TILE_WALL = register("deepslate_tile_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_TILES).forceSolidOn());
    public static final Block DEEPSLATE_BRICKS = register("deepslate_bricks", BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.DEEPSLATE_BRICKS));
    public static final Block DEEPSLATE_BRICK_STAIRS = registerLegacyStair("deepslate_brick_stairs", Blocks.DEEPSLATE_BRICKS);
    public static final Block DEEPSLATE_BRICK_SLAB = register("deepslate_brick_slab", BlockStepAbstract::new, BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_BRICKS));
    public static final Block DEEPSLATE_BRICK_WALL = register("deepslate_brick_wall", BlockCobbleWall::new, BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_BRICKS).forceSolidOn());
    public static final Block CHISELED_DEEPSLATE = register("chiseled_deepslate", BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.DEEPSLATE_BRICKS));
    public static final Block CRACKED_DEEPSLATE_BRICKS = register("cracked_deepslate_bricks", BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_BRICKS));
    public static final Block CRACKED_DEEPSLATE_TILES = register("cracked_deepslate_tiles", BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_TILES));
    public static final Block INFESTED_DEEPSLATE = register("infested_deepslate", (blockbase_info) -> {
        return new InfestedRotatedPillarBlock(Blocks.DEEPSLATE, blockbase_info);
    }, BlockBase.Info.of().mapColor(MaterialMapColor.DEEPSLATE).sound(SoundEffectType.DEEPSLATE));
    public static final Block SMOOTH_BASALT = register("smooth_basalt", BlockBase.Info.ofLegacyCopy(Blocks.BASALT));
    public static final Block RAW_IRON_BLOCK = register("raw_iron_block", BlockBase.Info.of().mapColor(MaterialMapColor.RAW_IRON).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F));
    public static final Block RAW_COPPER_BLOCK = register("raw_copper_block", BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F));
    public static final Block RAW_GOLD_BLOCK = register("raw_gold_block", BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F));
    public static final Block POTTED_AZALEA = register("potted_azalea_bush", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.AZALEA, blockbase_info);
    }, flowerPotProperties());
    public static final Block POTTED_FLOWERING_AZALEA = register("potted_flowering_azalea_bush", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.FLOWERING_AZALEA, blockbase_info);
    }, flowerPotProperties());
    public static final Block OCHRE_FROGLIGHT = register("ochre_froglight", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).strength(0.3F).lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.FROGLIGHT));
    public static final Block VERDANT_FROGLIGHT = register("verdant_froglight", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.GLOW_LICHEN).strength(0.3F).lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.FROGLIGHT));
    public static final Block PEARLESCENT_FROGLIGHT = register("pearlescent_froglight", BlockRotatable::new, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).strength(0.3F).lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.FROGLIGHT));
    public static final Block FROGSPAWN = register("frogspawn", FrogspawnBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).instabreak().noOcclusion().noCollission().sound(SoundEffectType.FROGSPAWN).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block REINFORCED_DEEPSLATE = register("reinforced_deepslate", BlockBase.Info.of().mapColor(MaterialMapColor.DEEPSLATE).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.DEEPSLATE).strength(55.0F, 1200.0F));
    public static final Block DECORATED_POT = register("decorated_pot", DecoratedPotBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_RED).strength(0.0F, 0.0F).pushReaction(EnumPistonReaction.DESTROY).noOcclusion());
    public static final Block CRAFTER = register("crafter", CrafterBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).strength(1.5F, 3.5F));
    public static final Block TRIAL_SPAWNER = register("trial_spawner", TrialSpawnerBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).lightLevel((iblockdata) -> {
        return ((TrialSpawnerState) iblockdata.getValue(TrialSpawnerBlock.STATE)).lightLevel();
    }).strength(50.0F).sound(SoundEffectType.TRIAL_SPAWNER).isViewBlocking(Blocks::never).noOcclusion());
    public static final Block VAULT = register("vault", VaultBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).noOcclusion().sound(SoundEffectType.VAULT).lightLevel((iblockdata) -> {
        return ((VaultState) iblockdata.getValue(VaultBlock.STATE)).lightLevel();
    }).strength(50.0F).isViewBlocking(Blocks::never));
    public static final Block HEAVY_CORE = register("heavy_core", HeavyCoreBlock::new, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).instrument(BlockPropertyInstrument.SNARE).sound(SoundEffectType.HEAVY_CORE).strength(10.0F).pushReaction(EnumPistonReaction.NORMAL).explosionResistance(1200.0F));
    public static final Block PALE_MOSS_BLOCK = register("pale_moss_block", (blockbase_info) -> {
        return new BonemealableFeaturePlacerBlock(VegetationFeatures.PALE_MOSS_PATCH_BONEMEAL, blockbase_info);
    }, BlockBase.Info.of().ignitedByLava().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundEffectType.MOSS).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PALE_MOSS_CARPET = register("pale_moss_carpet", MossyCarpetBlock::new, BlockBase.Info.of().ignitedByLava().mapColor(Blocks.PALE_MOSS_BLOCK.defaultMapColor()).strength(0.1F).sound(SoundEffectType.MOSS_CARPET).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block PALE_HANGING_MOSS = register("pale_hanging_moss", HangingMossBlock::new, BlockBase.Info.of().ignitedByLava().mapColor(Blocks.PALE_MOSS_BLOCK.defaultMapColor()).noCollission().sound(SoundEffectType.MOSS_CARPET).pushReaction(EnumPistonReaction.DESTROY));
    public static final Block OPEN_EYEBLOSSOM = register("open_eyeblossom", (blockbase_info) -> {
        return new EyeblossomBlock(EyeblossomBlock.a.OPEN, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.CREAKING_HEART.defaultMapColor()).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY).randomTicks());
    public static final Block CLOSED_EYEBLOSSOM = register("closed_eyeblossom", (blockbase_info) -> {
        return new EyeblossomBlock(EyeblossomBlock.a.CLOSED, blockbase_info);
    }, BlockBase.Info.of().mapColor(Blocks.PALE_OAK_LEAVES.defaultMapColor()).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY).randomTicks());
    public static final Block POTTED_OPEN_EYEBLOSSOM = register("potted_open_eyeblossom", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.OPEN_EYEBLOSSOM, blockbase_info);
    }, flowerPotProperties().randomTicks());
    public static final Block POTTED_CLOSED_EYEBLOSSOM = register("potted_closed_eyeblossom", (blockbase_info) -> {
        return new BlockFlowerPot(Blocks.CLOSED_EYEBLOSSOM, blockbase_info);
    }, flowerPotProperties().randomTicks());

    public Blocks() {}

    private static ToIntFunction<IBlockData> litBlockEmission(int i) {
        return (iblockdata) -> {
            return (Boolean) iblockdata.getValue(BlockProperties.LIT) ? i : 0;
        };
    }

    private static Function<IBlockData, MaterialMapColor> waterloggedMapColor(MaterialMapColor materialmapcolor) {
        return (iblockdata) -> {
            return (Boolean) iblockdata.getValue(BlockProperties.WATERLOGGED) ? MaterialMapColor.WATER : materialmapcolor;
        };
    }

    private static Boolean never(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EntityTypes<?> entitytypes) {
        return false;
    }

    private static Boolean always(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EntityTypes<?> entitytypes) {
        return true;
    }

    private static Boolean ocelotOrParrot(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EntityTypes<?> entitytypes) {
        return entitytypes == EntityTypes.OCELOT || entitytypes == EntityTypes.PARROT;
    }

    private static Block registerBed(String s, EnumColor enumcolor) {
        return register(s, (blockbase_info) -> {
            return new BlockBed(enumcolor, blockbase_info);
        }, BlockBase.Info.of().mapColor((iblockdata) -> {
            return iblockdata.getValue(BlockBed.PART) == BlockPropertyBedPart.FOOT ? enumcolor.getMapColor() : MaterialMapColor.WOOL;
        }).sound(SoundEffectType.WOOD).strength(0.2F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    }

    private static BlockBase.Info logProperties(MaterialMapColor materialmapcolor, MaterialMapColor materialmapcolor1, SoundEffectType soundeffecttype) {
        return BlockBase.Info.of().mapColor((iblockdata) -> {
            return iblockdata.getValue(BlockRotatable.AXIS) == EnumDirection.EnumAxis.Y ? materialmapcolor : materialmapcolor1;
        }).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(soundeffecttype).ignitedByLava();
    }

    private static BlockBase.Info netherStemProperties(MaterialMapColor materialmapcolor) {
        return BlockBase.Info.of().mapColor((iblockdata) -> {
            return materialmapcolor;
        }).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM);
    }

    private static boolean always(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return true;
    }

    private static boolean never(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return false;
    }

    private static Block registerStainedGlass(String s, EnumColor enumcolor) {
        return register(s, (blockbase_info) -> {
            return new BlockStainedGlass(enumcolor, blockbase_info);
        }, BlockBase.Info.of().mapColor(enumcolor).instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
    }

    private static BlockBase.Info leavesProperties(SoundEffectType soundeffecttype) {
        return BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).strength(0.2F).randomTicks().sound(soundeffecttype).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never);
    }

    private static BlockBase.Info shulkerBoxProperties(MaterialMapColor materialmapcolor) {
        return BlockBase.Info.of().mapColor(materialmapcolor).forceSolidOn().strength(2.0F).dynamicShape().noOcclusion().isSuffocating(Blocks.NOT_CLOSED_SHULKER).isViewBlocking(Blocks.NOT_CLOSED_SHULKER).pushReaction(EnumPistonReaction.DESTROY);
    }

    private static BlockBase.Info pistonProperties() {
        return BlockBase.Info.of().mapColor(MaterialMapColor.STONE).strength(1.5F).isRedstoneConductor(Blocks::never).isSuffocating(Blocks.NOT_EXTENDED_PISTON).isViewBlocking(Blocks.NOT_EXTENDED_PISTON).pushReaction(EnumPistonReaction.BLOCK);
    }

    private static BlockBase.Info buttonProperties() {
        return BlockBase.Info.of().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY);
    }

    private static BlockBase.Info flowerPotProperties() {
        return BlockBase.Info.of().instabreak().noOcclusion().pushReaction(EnumPistonReaction.DESTROY);
    }

    private static BlockBase.Info candleProperties(MaterialMapColor materialmapcolor) {
        return BlockBase.Info.of().mapColor(materialmapcolor).noOcclusion().strength(0.1F).sound(SoundEffectType.CANDLE).lightLevel(CandleBlock.LIGHT_EMISSION).pushReaction(EnumPistonReaction.DESTROY);
    }

    /** @deprecated */
    @Deprecated
    private static Block registerLegacyStair(String s, Block block) {
        return register(s, (blockbase_info) -> {
            return new BlockStairs(block.defaultBlockState(), blockbase_info);
        }, BlockBase.Info.ofLegacyCopy(block));
    }

    private static Block registerStair(String s, Block block) {
        return register(s, (blockbase_info) -> {
            return new BlockStairs(block.defaultBlockState(), blockbase_info);
        }, BlockBase.Info.ofFullCopy(block));
    }

    private static BlockBase.Info wallVariant(Block block, boolean flag) {
        BlockBase.Info blockbase_info = block.properties();
        BlockBase.Info blockbase_info1 = BlockBase.Info.of().overrideLootTable(block.getLootTable());

        if (flag) {
            blockbase_info1 = blockbase_info1.overrideDescription(block.getDescriptionId());
        }

        return blockbase_info1;
    }

    private static Block register(ResourceKey<Block> resourcekey, Function<BlockBase.Info, Block> function, BlockBase.Info blockbase_info) {
        Block block = (Block) function.apply(blockbase_info.setId(resourcekey));

        return (Block) IRegistry.register(BuiltInRegistries.BLOCK, resourcekey, block);
    }

    private static Block register(ResourceKey<Block> resourcekey, BlockBase.Info blockbase_info) {
        return register(resourcekey, Block::new, blockbase_info);
    }

    private static ResourceKey<Block> vanillaBlockId(String s) {
        return ResourceKey.create(Registries.BLOCK, MinecraftKey.withDefaultNamespace(s));
    }

    private static Block register(String s, Function<BlockBase.Info, Block> function, BlockBase.Info blockbase_info) {
        return register(vanillaBlockId(s), function, blockbase_info);
    }

    private static Block register(String s, BlockBase.Info blockbase_info) {
        return register(s, Block::new, blockbase_info);
    }

    static {
        Iterator iterator = BuiltInRegistries.BLOCK.iterator();

        while (iterator.hasNext()) {
            Block block = (Block) iterator.next();
            UnmodifiableIterator unmodifiableiterator = block.getStateDefinition().getPossibleStates().iterator();

            while (unmodifiableiterator.hasNext()) {
                IBlockData iblockdata = (IBlockData) unmodifiableiterator.next();

                Block.BLOCK_STATE_REGISTRY.add(iblockdata);
                iblockdata.initCache();
            }
        }

    }
}
