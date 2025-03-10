package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.ARGB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.World;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem extends Item {

    public static final int MAX_SHOWN_GRID_ITEMS_X = 4;
    public static final int MAX_SHOWN_GRID_ITEMS_Y = 3;
    public static final int MAX_SHOWN_GRID_ITEMS = 12;
    public static final int OVERFLOWING_MAX_SHOWN_GRID_ITEMS = 11;
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);
    private static final int TICKS_AFTER_FIRST_THROW = 10;
    private static final int TICKS_BETWEEN_THROWS = 2;
    private static final int TICKS_MAX_THROW_DURATION = 200;

    public BundleItem(Item.Info item_info) {
        super(item_info);
    }

    public static float getFullnessDisplay(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return bundlecontents.weight().floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemstack, Slot slot, ClickAction clickaction, EntityHuman entityhuman) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents == null) {
            return false;
        } else {
            ItemStack itemstack1 = slot.getItem();
            BundleContents.a bundlecontents_a = new BundleContents.a(bundlecontents);

            if (clickaction == ClickAction.PRIMARY && !itemstack1.isEmpty()) {
                if (bundlecontents_a.tryTransfer(slot, entityhuman) > 0) {
                    playInsertSound(entityhuman);
                } else {
                    playInsertFailSound(entityhuman);
                }

                itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
                this.broadcastChangesOnContainerMenu(entityhuman);
                return true;
            } else if (clickaction == ClickAction.SECONDARY && itemstack1.isEmpty()) {
                ItemStack itemstack2 = bundlecontents_a.removeOne();

                if (itemstack2 != null) {
                    ItemStack itemstack3 = slot.safeInsert(itemstack2);

                    if (itemstack3.getCount() > 0) {
                        bundlecontents_a.tryInsert(itemstack3);
                    } else {
                        playRemoveOneSound(entityhuman);
                    }
                }

                itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
                this.broadcastChangesOnContainerMenu(entityhuman);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemstack, ItemStack itemstack1, Slot slot, ClickAction clickaction, EntityHuman entityhuman, SlotAccess slotaccess) {
        if (clickaction == ClickAction.PRIMARY && itemstack1.isEmpty()) {
            toggleSelectedItem(itemstack, -1);
            return false;
        } else {
            BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

            if (bundlecontents == null) {
                return false;
            } else {
                BundleContents.a bundlecontents_a = new BundleContents.a(bundlecontents);

                if (clickaction == ClickAction.PRIMARY && !itemstack1.isEmpty()) {
                    if (slot.allowModification(entityhuman) && bundlecontents_a.tryInsert(itemstack1) > 0) {
                        playInsertSound(entityhuman);
                    } else {
                        playInsertFailSound(entityhuman);
                    }

                    itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
                    this.broadcastChangesOnContainerMenu(entityhuman);
                    return true;
                } else if (clickaction == ClickAction.SECONDARY && itemstack1.isEmpty()) {
                    if (slot.allowModification(entityhuman)) {
                        ItemStack itemstack2 = bundlecontents_a.removeOne();

                        if (itemstack2 != null) {
                            playRemoveOneSound(entityhuman);
                            slotaccess.set(itemstack2);
                        }
                    }

                    itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
                    this.broadcastChangesOnContainerMenu(entityhuman);
                    return true;
                } else {
                    toggleSelectedItem(itemstack, -1);
                    return false;
                }
            }
        }
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        entityhuman.startUsingItem(enumhand);
        return EnumInteractionResult.SUCCESS;
    }

    private void dropContent(World world, EntityHuman entityhuman, ItemStack itemstack) {
        if (this.dropContent(itemstack, entityhuman)) {
            playDropContentsSound(world, entityhuman);
            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        }

    }

    @Override
    public boolean isBarVisible(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return bundlecontents.weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return Math.min(1 + MathHelper.mulAndTruncate(bundlecontents.weight(), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return bundlecontents.weight().compareTo(Fraction.ONE) >= 0 ? BundleItem.FULL_BAR_COLOR : BundleItem.BAR_COLOR;
    }

    public static void toggleSelectedItem(ItemStack itemstack, int i) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null) {
            BundleContents.a bundlecontents_a = new BundleContents.a(bundlecontents);

            bundlecontents_a.toggleSelectedItem(i);
            itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
        }
    }

    public static boolean hasSelectedItem(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        return bundlecontents != null && bundlecontents.getSelectedItem() != -1;
    }

    public static int getSelectedItem(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return bundlecontents.getSelectedItem();
    }

    public static ItemStack getSelectedItemStack(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        return bundlecontents != null && bundlecontents.getSelectedItem() != -1 ? bundlecontents.getItemUnsafe(bundlecontents.getSelectedItem()) : ItemStack.EMPTY;
    }

    public static int getNumberOfItemsToShow(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return bundlecontents.getNumberOfItemsToShow();
    }

    private boolean dropContent(ItemStack itemstack, EntityHuman entityhuman) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null && !bundlecontents.isEmpty()) {
            Optional<ItemStack> optional = removeOneItemFromBundle(itemstack, entityhuman, bundlecontents);

            if (optional.isPresent()) {
                entityhuman.drop((ItemStack) optional.get(), true);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static Optional<ItemStack> removeOneItemFromBundle(ItemStack itemstack, EntityHuman entityhuman, BundleContents bundlecontents) {
        BundleContents.a bundlecontents_a = new BundleContents.a(bundlecontents);
        ItemStack itemstack1 = bundlecontents_a.removeOne();

        if (itemstack1 != null) {
            playRemoveOneSound(entityhuman);
            itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
            return Optional.of(itemstack1);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void onUseTick(World world, EntityLiving entityliving, ItemStack itemstack, int i) {
        if (entityliving instanceof EntityHuman entityhuman) {
            int j = this.getUseDuration(itemstack, entityliving);
            boolean flag = i == j;

            if (flag || i < j - 10 && i % 2 == 0) {
                this.dropContent(world, entityhuman, itemstack);
            }
        }

    }

    @Override
    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        return 200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        return ItemUseAnimation.BUNDLE;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemstack) {
        return !itemstack.has(DataComponents.HIDE_TOOLTIP) && !itemstack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP) ? Optional.ofNullable((BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new) : Optional.empty();
    }

    @Override
    public void onDestroyed(EntityItem entityitem) {
        BundleContents bundlecontents = (BundleContents) entityitem.getItem().get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null) {
            entityitem.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            ItemLiquidUtil.onContainerDestroyed(entityitem, bundlecontents.itemsCopy());
        }
    }

    public static List<BundleItem> getAllBundleItemColors() {
        return Stream.of(Items.BUNDLE, Items.WHITE_BUNDLE, Items.ORANGE_BUNDLE, Items.MAGENTA_BUNDLE, Items.LIGHT_BLUE_BUNDLE, Items.YELLOW_BUNDLE, Items.LIME_BUNDLE, Items.PINK_BUNDLE, Items.GRAY_BUNDLE, Items.LIGHT_GRAY_BUNDLE, Items.CYAN_BUNDLE, Items.BLACK_BUNDLE, Items.BROWN_BUNDLE, Items.GREEN_BUNDLE, Items.RED_BUNDLE, Items.BLUE_BUNDLE, Items.PURPLE_BUNDLE).map((item) -> {
            return (BundleItem) item;
        }).toList();
    }

    public static Item getByColor(EnumColor enumcolor) {
        Item item;

        switch (enumcolor) {
            case WHITE:
                item = Items.WHITE_BUNDLE;
                break;
            case ORANGE:
                item = Items.ORANGE_BUNDLE;
                break;
            case MAGENTA:
                item = Items.MAGENTA_BUNDLE;
                break;
            case LIGHT_BLUE:
                item = Items.LIGHT_BLUE_BUNDLE;
                break;
            case YELLOW:
                item = Items.YELLOW_BUNDLE;
                break;
            case LIME:
                item = Items.LIME_BUNDLE;
                break;
            case PINK:
                item = Items.PINK_BUNDLE;
                break;
            case GRAY:
                item = Items.GRAY_BUNDLE;
                break;
            case LIGHT_GRAY:
                item = Items.LIGHT_GRAY_BUNDLE;
                break;
            case CYAN:
                item = Items.CYAN_BUNDLE;
                break;
            case BLUE:
                item = Items.BLUE_BUNDLE;
                break;
            case BROWN:
                item = Items.BROWN_BUNDLE;
                break;
            case GREEN:
                item = Items.GREEN_BUNDLE;
                break;
            case RED:
                item = Items.RED_BUNDLE;
                break;
            case BLACK:
                item = Items.BLACK_BUNDLE;
                break;
            case PURPLE:
                item = Items.PURPLE_BUNDLE;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return item;
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEffects.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEffects.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEffects.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

    private static void playDropContentsSound(World world, Entity entity) {
        world.playSound((EntityHuman) null, entity.blockPosition(), SoundEffects.BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void broadcastChangesOnContainerMenu(EntityHuman entityhuman) {
        Container container = entityhuman.containerMenu;

        if (container != null) {
            container.slotsChanged(entityhuman.getInventory());
        }

    }
}
