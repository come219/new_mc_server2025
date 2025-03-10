package net.minecraft.world.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodInfo;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class Item implements FeatureElement, IMaterial {

    public static final Codec<Holder<Item>> CODEC = BuiltInRegistries.ITEM.holderByNameCodec().validate((holder) -> {
        return holder.is((Holder) Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> {
            return "Item must not be minecraft:air";
        }) : DataResult.success(holder);
    });
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    public static final MinecraftKey BASE_ATTACK_DAMAGE_ID = MinecraftKey.withDefaultNamespace("base_attack_damage");
    public static final MinecraftKey BASE_ATTACK_SPEED_ID = MinecraftKey.withDefaultNamespace("base_attack_speed");
    public static final int DEFAULT_MAX_STACK_SIZE = 64;
    public static final int ABSOLUTE_MAX_STACK_SIZE = 99;
    public static final int MAX_BAR_WIDTH = 13;
    private final Holder.c<Item> builtInRegistryHolder;
    private final DataComponentMap components;
    @Nullable
    private final Item craftingRemainingItem;
    protected final String descriptionId;
    private final FeatureFlagSet requiredFeatures;

    public static int getId(Item item) {
        return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
    }

    public static Item byId(int i) {
        return (Item) BuiltInRegistries.ITEM.byId(i);
    }

    /** @deprecated */
    @Deprecated
    public static Item byBlock(Block block) {
        return (Item) Item.BY_BLOCK.getOrDefault(block, Items.AIR);
    }

    public Item(Item.Info item_info) {
        this.builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
        this.descriptionId = item_info.effectiveDescriptionId();
        this.components = item_info.buildAndValidateComponents(IChatBaseComponent.translatable(this.descriptionId), item_info.effectiveModel());
        this.craftingRemainingItem = item_info.craftingRemainingItem;
        this.requiredFeatures = item_info.requiredFeatures;
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            String s = this.getClass().getSimpleName();

            if (!s.endsWith("Item")) {
                Item.LOGGER.error("Item classes should end with Item and {} doesn't.", s);
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public Holder.c<Item> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public DataComponentMap components() {
        return this.components;
    }

    public int getDefaultMaxStackSize() {
        return (Integer) this.components.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    public void onUseTick(World world, EntityLiving entityliving, ItemStack itemstack, int i) {}

    public void onDestroyed(EntityItem entityitem) {}

    public void verifyComponentsAfterLoad(ItemStack itemstack) {}

    public boolean canAttackBlock(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        return EnumInteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack itemstack, IBlockData iblockdata) {
        Tool tool = (Tool) itemstack.get(DataComponents.TOOL);

        return tool != null ? tool.getMiningSpeed(iblockdata) : 1.0F;
    }

    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        Consumable consumable = (Consumable) itemstack.get(DataComponents.CONSUMABLE);

        if (consumable != null) {
            return consumable.startConsuming(entityhuman, itemstack, enumhand);
        } else {
            Equippable equippable = (Equippable) itemstack.get(DataComponents.EQUIPPABLE);

            return (EnumInteractionResult) (equippable != null && equippable.swappable() ? equippable.swapWithEquipmentSlot(itemstack, entityhuman) : EnumInteractionResult.PASS);
        }
    }

    public ItemStack finishUsingItem(ItemStack itemstack, World world, EntityLiving entityliving) {
        Consumable consumable = (Consumable) itemstack.get(DataComponents.CONSUMABLE);

        return consumable != null ? consumable.onConsume(world, entityliving, itemstack) : itemstack;
    }

    public boolean isBarVisible(ItemStack itemstack) {
        return itemstack.isDamaged();
    }

    public int getBarWidth(ItemStack itemstack) {
        return MathHelper.clamp(Math.round(13.0F - (float) itemstack.getDamageValue() * 13.0F / (float) itemstack.getMaxDamage()), 0, 13);
    }

    public int getBarColor(ItemStack itemstack) {
        int i = itemstack.getMaxDamage();
        float f = Math.max(0.0F, ((float) i - (float) itemstack.getDamageValue()) / (float) i);

        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public boolean overrideStackedOnOther(ItemStack itemstack, Slot slot, ClickAction clickaction, EntityHuman entityhuman) {
        return false;
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemstack, ItemStack itemstack1, Slot slot, ClickAction clickaction, EntityHuman entityhuman, SlotAccess slotaccess) {
        return false;
    }

    public float getAttackDamageBonus(Entity entity, float f, DamageSource damagesource) {
        return 0.0F;
    }

    @Nullable
    public DamageSource getDamageSource(EntityLiving entityliving) {
        return null;
    }

    public boolean hurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        return false;
    }

    public void postHurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {}

    public boolean mineBlock(ItemStack itemstack, World world, IBlockData iblockdata, BlockPosition blockposition, EntityLiving entityliving) {
        Tool tool = (Tool) itemstack.get(DataComponents.TOOL);

        if (tool == null) {
            return false;
        } else {
            if (!world.isClientSide && iblockdata.getDestroySpeed(world, blockposition) != 0.0F && tool.damagePerBlock() > 0) {
                itemstack.hurtAndBreak(tool.damagePerBlock(), entityliving, EnumItemSlot.MAINHAND);
            }

            return true;
        }
    }

    public boolean isCorrectToolForDrops(ItemStack itemstack, IBlockData iblockdata) {
        Tool tool = (Tool) itemstack.get(DataComponents.TOOL);

        return tool != null && tool.isCorrectForDrops(iblockdata);
    }

    public EnumInteractionResult interactLivingEntity(ItemStack itemstack, EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        return EnumInteractionResult.PASS;
    }

    public String toString() {
        return BuiltInRegistries.ITEM.wrapAsHolder(this).getRegisteredName();
    }

    public final ItemStack getCraftingRemainder() {
        return this.craftingRemainingItem == null ? ItemStack.EMPTY : new ItemStack(this.craftingRemainingItem);
    }

    public void inventoryTick(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {}

    public void onCraftedBy(ItemStack itemstack, World world, EntityHuman entityhuman) {
        this.onCraftedPostProcess(itemstack, world);
    }

    public void onCraftedPostProcess(ItemStack itemstack, World world) {}

    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        Consumable consumable = (Consumable) itemstack.get(DataComponents.CONSUMABLE);

        return consumable != null ? consumable.animation() : ItemUseAnimation.NONE;
    }

    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        Consumable consumable = (Consumable) itemstack.get(DataComponents.CONSUMABLE);

        return consumable != null ? consumable.consumeTicks() : 0;
    }

    public boolean releaseUsing(ItemStack itemstack, World world, EntityLiving entityliving, int i) {
        return false;
    }

    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {}

    public Optional<TooltipComponent> getTooltipImage(ItemStack itemstack) {
        return Optional.empty();
    }

    @VisibleForTesting
    public final String getDescriptionId() {
        return this.descriptionId;
    }

    public final IChatBaseComponent getName() {
        return (IChatBaseComponent) this.components.getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }

    public IChatBaseComponent getName(ItemStack itemstack) {
        return (IChatBaseComponent) itemstack.getComponents().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }

    public boolean isFoil(ItemStack itemstack) {
        return itemstack.isEnchanted();
    }

    protected static MovingObjectPositionBlock getPlayerPOVHitResult(World world, EntityHuman entityhuman, RayTrace.FluidCollisionOption raytrace_fluidcollisionoption) {
        Vec3D vec3d = entityhuman.getEyePosition();
        Vec3D vec3d1 = vec3d.add(entityhuman.calculateViewVector(entityhuman.getXRot(), entityhuman.getYRot()).scale(entityhuman.blockInteractionRange()));

        return world.clip(new RayTrace(vec3d, vec3d1, RayTrace.BlockCollisionOption.OUTLINE, raytrace_fluidcollisionoption, entityhuman));
    }

    public boolean useOnRelease(ItemStack itemstack) {
        return false;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public SoundEffect getBreakingSound() {
        return SoundEffects.ITEM_BREAK;
    }

    public boolean canFitInsideContainerItems() {
        return true;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public boolean shouldPrintOpWarning(ItemStack itemstack, @Nullable EntityHuman entityhuman) {
        return false;
    }

    public static class Info {

        private static final DependantName<Item, String> BLOCK_DESCRIPTION_ID = (resourcekey) -> {
            return SystemUtils.makeDescriptionId("block", resourcekey.location());
        };
        private static final DependantName<Item, String> ITEM_DESCRIPTION_ID = (resourcekey) -> {
            return SystemUtils.makeDescriptionId("item", resourcekey.location());
        };
        private final DataComponentMap.a components;
        @Nullable
        Item craftingRemainingItem;
        FeatureFlagSet requiredFeatures;
        @Nullable
        private ResourceKey<Item> id;
        private DependantName<Item, String> descriptionId;
        private DependantName<Item, MinecraftKey> model;

        public Info() {
            this.components = DataComponentMap.builder().addAll(DataComponents.COMMON_ITEM_COMPONENTS);
            this.requiredFeatures = FeatureFlags.VANILLA_SET;
            this.descriptionId = Item.Info.ITEM_DESCRIPTION_ID;
            this.model = ResourceKey::location;
        }

        public Item.Info food(FoodInfo foodinfo) {
            return this.food(foodinfo, Consumables.DEFAULT_FOOD);
        }

        public Item.Info food(FoodInfo foodinfo, Consumable consumable) {
            return this.component(DataComponents.FOOD, foodinfo).component(DataComponents.CONSUMABLE, consumable);
        }

        public Item.Info usingConvertsTo(Item item) {
            return this.component(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStack(item)));
        }

        public Item.Info useCooldown(float f) {
            return this.component(DataComponents.USE_COOLDOWN, new UseCooldown(f));
        }

        public Item.Info stacksTo(int i) {
            return this.component(DataComponents.MAX_STACK_SIZE, i);
        }

        public Item.Info durability(int i) {
            this.component(DataComponents.MAX_DAMAGE, i);
            this.component(DataComponents.MAX_STACK_SIZE, 1);
            this.component(DataComponents.DAMAGE, 0);
            return this;
        }

        public Item.Info craftRemainder(Item item) {
            this.craftingRemainingItem = item;
            return this;
        }

        public Item.Info rarity(EnumItemRarity enumitemrarity) {
            return this.component(DataComponents.RARITY, enumitemrarity);
        }

        public Item.Info fireResistant() {
            return this.component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(DamageTypeTags.IS_FIRE));
        }

        public Item.Info jukeboxPlayable(ResourceKey<JukeboxSong> resourcekey) {
            return this.component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(resourcekey), true));
        }

        public Item.Info enchantable(int i) {
            return this.component(DataComponents.ENCHANTABLE, new Enchantable(i));
        }

        public Item.Info repairable(Item item) {
            return this.component(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(item.builtInRegistryHolder())));
        }

        public Item.Info repairable(TagKey<Item> tagkey) {
            HolderGetter<Item> holdergetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM);

            return this.component(DataComponents.REPAIRABLE, new Repairable(holdergetter.getOrThrow(tagkey)));
        }

        public Item.Info equippable(EnumItemSlot enumitemslot) {
            return this.component(DataComponents.EQUIPPABLE, Equippable.builder(enumitemslot).build());
        }

        public Item.Info equippableUnswappable(EnumItemSlot enumitemslot) {
            return this.component(DataComponents.EQUIPPABLE, Equippable.builder(enumitemslot).setSwappable(false).build());
        }

        public Item.Info requiredFeatures(FeatureFlag... afeatureflag) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
            return this;
        }

        public Item.Info setId(ResourceKey<Item> resourcekey) {
            this.id = resourcekey;
            return this;
        }

        public Item.Info overrideDescription(String s) {
            this.descriptionId = DependantName.fixed(s);
            return this;
        }

        public Item.Info useBlockDescriptionPrefix() {
            this.descriptionId = Item.Info.BLOCK_DESCRIPTION_ID;
            return this;
        }

        public Item.Info useItemDescriptionPrefix() {
            this.descriptionId = Item.Info.ITEM_DESCRIPTION_ID;
            return this;
        }

        protected String effectiveDescriptionId() {
            return (String) this.descriptionId.get((ResourceKey) Objects.requireNonNull(this.id, "Item id not set"));
        }

        public MinecraftKey effectiveModel() {
            return (MinecraftKey) this.model.get((ResourceKey) Objects.requireNonNull(this.id, "Item id not set"));
        }

        public <T> Item.Info component(DataComponentType<T> datacomponenttype, T t0) {
            this.components.set(datacomponenttype, t0);
            return this;
        }

        public Item.Info attributes(ItemAttributeModifiers itemattributemodifiers) {
            return this.component(DataComponents.ATTRIBUTE_MODIFIERS, itemattributemodifiers);
        }

        DataComponentMap buildAndValidateComponents(IChatBaseComponent ichatbasecomponent, MinecraftKey minecraftkey) {
            DataComponentMap datacomponentmap = this.components.set(DataComponents.ITEM_NAME, ichatbasecomponent).set(DataComponents.ITEM_MODEL, minecraftkey).build();

            if (datacomponentmap.has(DataComponents.DAMAGE) && (Integer) datacomponentmap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
                throw new IllegalStateException("Item cannot have both durability and be stackable");
            } else {
                return datacomponentmap;
            }
        }
    }

    public interface b {

        Item.b EMPTY = new Item.b() {
            @Nullable
            @Override
            public HolderLookup.a registries() {
                return null;
            }

            @Override
            public float tickRate() {
                return 20.0F;
            }

            @Nullable
            @Override
            public WorldMap mapData(MapId mapid) {
                return null;
            }
        };

        @Nullable
        HolderLookup.a registries();

        float tickRate();

        @Nullable
        WorldMap mapData(MapId mapid);

        static Item.b of(@Nullable final World world) {
            return world == null ? Item.b.EMPTY : new Item.b() {
                @Override
                public HolderLookup.a registries() {
                    return world.registryAccess();
                }

                @Override
                public float tickRate() {
                    return world.tickRateManager().tickrate();
                }

                @Override
                public WorldMap mapData(MapId mapid) {
                    return world.getMapData(mapid);
                }
            };
        }

        static Item.b of(final HolderLookup.a holderlookup_a) {
            return new Item.b() {
                @Override
                public HolderLookup.a registries() {
                    return holderlookup_a;
                }

                @Override
                public float tickRate() {
                    return 20.0F;
                }

                @Nullable
                @Override
                public WorldMap mapData(MapId mapid) {
                    return null;
                }
            };
        }
    }
}
