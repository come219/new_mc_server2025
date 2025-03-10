package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.datafix.fixes.DataConverterTypes;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.piston.TileEntityPiston;
import net.minecraft.world.level.block.state.IBlockData;
import org.slf4j.Logger;

public class TileEntityTypes<T extends TileEntity> {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final TileEntityTypes<TileEntityFurnaceFurnace> FURNACE = register("furnace", TileEntityFurnaceFurnace::new, Blocks.FURNACE);
    public static final TileEntityTypes<TileEntityChest> CHEST = register("chest", TileEntityChest::new, Blocks.CHEST);
    public static final TileEntityTypes<TileEntityChestTrapped> TRAPPED_CHEST = register("trapped_chest", TileEntityChestTrapped::new, Blocks.TRAPPED_CHEST);
    public static final TileEntityTypes<TileEntityEnderChest> ENDER_CHEST = register("ender_chest", TileEntityEnderChest::new, Blocks.ENDER_CHEST);
    public static final TileEntityTypes<TileEntityJukeBox> JUKEBOX = register("jukebox", TileEntityJukeBox::new, Blocks.JUKEBOX);
    public static final TileEntityTypes<TileEntityDispenser> DISPENSER = register("dispenser", TileEntityDispenser::new, Blocks.DISPENSER);
    public static final TileEntityTypes<TileEntityDropper> DROPPER = register("dropper", TileEntityDropper::new, Blocks.DROPPER);
    public static final TileEntityTypes<TileEntitySign> SIGN = register("sign", TileEntitySign::new, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.PALE_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.PALE_OAK_WALL_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN, Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN);
    public static final TileEntityTypes<HangingSignBlockEntity> HANGING_SIGN = register("hanging_sign", HangingSignBlockEntity::new, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.PALE_OAK_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
    public static final TileEntityTypes<TileEntityMobSpawner> MOB_SPAWNER = register("mob_spawner", TileEntityMobSpawner::new, Blocks.SPAWNER);
    public static final TileEntityTypes<CreakingHeartBlockEntity> CREAKING_HEART = register("creaking_heart", CreakingHeartBlockEntity::new, Blocks.CREAKING_HEART);
    public static final TileEntityTypes<TileEntityPiston> PISTON = register("piston", TileEntityPiston::new, Blocks.MOVING_PISTON);
    public static final TileEntityTypes<TileEntityBrewingStand> BREWING_STAND = register("brewing_stand", TileEntityBrewingStand::new, Blocks.BREWING_STAND);
    public static final TileEntityTypes<TileEntityEnchantTable> ENCHANTING_TABLE = register("enchanting_table", TileEntityEnchantTable::new, Blocks.ENCHANTING_TABLE);
    public static final TileEntityTypes<TileEntityEnderPortal> END_PORTAL = register("end_portal", TileEntityEnderPortal::new, Blocks.END_PORTAL);
    public static final TileEntityTypes<TileEntityBeacon> BEACON = register("beacon", TileEntityBeacon::new, Blocks.BEACON);
    public static final TileEntityTypes<TileEntitySkull> SKULL = register("skull", TileEntitySkull::new, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD);
    public static final TileEntityTypes<TileEntityLightDetector> DAYLIGHT_DETECTOR = register("daylight_detector", TileEntityLightDetector::new, Blocks.DAYLIGHT_DETECTOR);
    public static final TileEntityTypes<TileEntityHopper> HOPPER = register("hopper", TileEntityHopper::new, Blocks.HOPPER);
    public static final TileEntityTypes<TileEntityComparator> COMPARATOR = register("comparator", TileEntityComparator::new, Blocks.COMPARATOR);
    public static final TileEntityTypes<TileEntityBanner> BANNER = register("banner", TileEntityBanner::new, Blocks.WHITE_BANNER, Blocks.ORANGE_BANNER, Blocks.MAGENTA_BANNER, Blocks.LIGHT_BLUE_BANNER, Blocks.YELLOW_BANNER, Blocks.LIME_BANNER, Blocks.PINK_BANNER, Blocks.GRAY_BANNER, Blocks.LIGHT_GRAY_BANNER, Blocks.CYAN_BANNER, Blocks.PURPLE_BANNER, Blocks.BLUE_BANNER, Blocks.BROWN_BANNER, Blocks.GREEN_BANNER, Blocks.RED_BANNER, Blocks.BLACK_BANNER, Blocks.WHITE_WALL_BANNER, Blocks.ORANGE_WALL_BANNER, Blocks.MAGENTA_WALL_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.YELLOW_WALL_BANNER, Blocks.LIME_WALL_BANNER, Blocks.PINK_WALL_BANNER, Blocks.GRAY_WALL_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.CYAN_WALL_BANNER, Blocks.PURPLE_WALL_BANNER, Blocks.BLUE_WALL_BANNER, Blocks.BROWN_WALL_BANNER, Blocks.GREEN_WALL_BANNER, Blocks.RED_WALL_BANNER, Blocks.BLACK_WALL_BANNER);
    public static final TileEntityTypes<TileEntityStructure> STRUCTURE_BLOCK = register("structure_block", TileEntityStructure::new, Blocks.STRUCTURE_BLOCK);
    public static final TileEntityTypes<TileEntityEndGateway> END_GATEWAY = register("end_gateway", TileEntityEndGateway::new, Blocks.END_GATEWAY);
    public static final TileEntityTypes<TileEntityCommand> COMMAND_BLOCK = register("command_block", TileEntityCommand::new, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK);
    public static final TileEntityTypes<TileEntityShulkerBox> SHULKER_BOX = register("shulker_box", TileEntityShulkerBox::new, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX);
    public static final TileEntityTypes<TileEntityBed> BED = register("bed", TileEntityBed::new, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED);
    public static final TileEntityTypes<TileEntityConduit> CONDUIT = register("conduit", TileEntityConduit::new, Blocks.CONDUIT);
    public static final TileEntityTypes<TileEntityBarrel> BARREL = register("barrel", TileEntityBarrel::new, Blocks.BARREL);
    public static final TileEntityTypes<TileEntitySmoker> SMOKER = register("smoker", TileEntitySmoker::new, Blocks.SMOKER);
    public static final TileEntityTypes<TileEntityBlastFurnace> BLAST_FURNACE = register("blast_furnace", TileEntityBlastFurnace::new, Blocks.BLAST_FURNACE);
    public static final TileEntityTypes<TileEntityLectern> LECTERN = register("lectern", TileEntityLectern::new, Blocks.LECTERN);
    public static final TileEntityTypes<TileEntityBell> BELL = register("bell", TileEntityBell::new, Blocks.BELL);
    public static final TileEntityTypes<TileEntityJigsaw> JIGSAW = register("jigsaw", TileEntityJigsaw::new, Blocks.JIGSAW);
    public static final TileEntityTypes<TileEntityCampfire> CAMPFIRE = register("campfire", TileEntityCampfire::new, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
    public static final TileEntityTypes<TileEntityBeehive> BEEHIVE = register("beehive", TileEntityBeehive::new, Blocks.BEE_NEST, Blocks.BEEHIVE);
    public static final TileEntityTypes<SculkSensorBlockEntity> SCULK_SENSOR = register("sculk_sensor", SculkSensorBlockEntity::new, Blocks.SCULK_SENSOR);
    public static final TileEntityTypes<CalibratedSculkSensorBlockEntity> CALIBRATED_SCULK_SENSOR = register("calibrated_sculk_sensor", CalibratedSculkSensorBlockEntity::new, Blocks.CALIBRATED_SCULK_SENSOR);
    public static final TileEntityTypes<SculkCatalystBlockEntity> SCULK_CATALYST = register("sculk_catalyst", SculkCatalystBlockEntity::new, Blocks.SCULK_CATALYST);
    public static final TileEntityTypes<SculkShriekerBlockEntity> SCULK_SHRIEKER = register("sculk_shrieker", SculkShriekerBlockEntity::new, Blocks.SCULK_SHRIEKER);
    public static final TileEntityTypes<ChiseledBookShelfBlockEntity> CHISELED_BOOKSHELF = register("chiseled_bookshelf", ChiseledBookShelfBlockEntity::new, Blocks.CHISELED_BOOKSHELF);
    public static final TileEntityTypes<BrushableBlockEntity> BRUSHABLE_BLOCK = register("brushable_block", BrushableBlockEntity::new, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL);
    public static final TileEntityTypes<DecoratedPotBlockEntity> DECORATED_POT = register("decorated_pot", DecoratedPotBlockEntity::new, Blocks.DECORATED_POT);
    public static final TileEntityTypes<CrafterBlockEntity> CRAFTER = register("crafter", CrafterBlockEntity::new, Blocks.CRAFTER);
    public static final TileEntityTypes<TrialSpawnerBlockEntity> TRIAL_SPAWNER = register("trial_spawner", TrialSpawnerBlockEntity::new, Blocks.TRIAL_SPAWNER);
    public static final TileEntityTypes<VaultBlockEntity> VAULT = register("vault", VaultBlockEntity::new, Blocks.VAULT);
    private static final Set<TileEntityTypes<?>> OP_ONLY_CUSTOM_DATA = Set.of(TileEntityTypes.COMMAND_BLOCK, TileEntityTypes.LECTERN, TileEntityTypes.SIGN, TileEntityTypes.HANGING_SIGN, TileEntityTypes.MOB_SPAWNER, TileEntityTypes.TRIAL_SPAWNER);
    private final TileEntityTypes.a<? extends T> factory;
    private final Set<Block> validBlocks;
    private final Holder.c<TileEntityTypes<?>> builtInRegistryHolder;

    @Nullable
    public static MinecraftKey getKey(TileEntityTypes<?> tileentitytypes) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(tileentitytypes);
    }

    private static <T extends TileEntity> TileEntityTypes<T> register(String s, TileEntityTypes.a<? extends T> tileentitytypes_a, Block... ablock) {
        if (ablock.length == 0) {
            TileEntityTypes.LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", s);
        }

        SystemUtils.fetchChoiceType(DataConverterTypes.BLOCK_ENTITY, s);
        return (TileEntityTypes) IRegistry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, s, new TileEntityTypes<>(tileentitytypes_a, Set.of(ablock)));
    }

    private TileEntityTypes(TileEntityTypes.a<? extends T> tileentitytypes_a, Set<Block> set) {
        this.builtInRegistryHolder = BuiltInRegistries.BLOCK_ENTITY_TYPE.createIntrusiveHolder(this);
        this.factory = tileentitytypes_a;
        this.validBlocks = set;
    }

    @Nullable
    public T create(BlockPosition blockposition, IBlockData iblockdata) {
        return this.factory.create(blockposition, iblockdata);
    }

    public boolean isValid(IBlockData iblockdata) {
        return this.validBlocks.contains(iblockdata.getBlock());
    }

    /** @deprecated */
    @Deprecated
    public Holder.c<TileEntityTypes<?>> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    @Nullable
    public T getBlockEntity(IBlockAccess iblockaccess, BlockPosition blockposition) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        return tileentity != null && tileentity.getType() == this ? tileentity : null;
    }

    public boolean onlyOpCanSetNbt() {
        return TileEntityTypes.OP_ONLY_CUSTOM_DATA.contains(this);
    }

    @FunctionalInterface
    private interface a<T extends TileEntity> {

        T create(BlockPosition blockposition, IBlockData iblockdata);
    }
}
