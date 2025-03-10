package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;

public class ItemBow extends ItemProjectileWeapon {

    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public ItemBow(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public boolean releaseUsing(ItemStack itemstack, World world, EntityLiving entityliving, int i) {
        if (!(entityliving instanceof EntityHuman entityhuman)) {
            return false;
        } else {
            ItemStack itemstack1 = entityhuman.getProjectile(itemstack);

            if (itemstack1.isEmpty()) {
                return false;
            } else {
                int j = this.getUseDuration(itemstack, entityliving) - i;
                float f = getPowerForTime(j);

                if ((double) f < 0.1D) {
                    return false;
                } else {
                    List<ItemStack> list = draw(itemstack, itemstack1, entityhuman);

                    if (world instanceof WorldServer) {
                        WorldServer worldserver = (WorldServer) world;

                        if (!list.isEmpty()) {
                            this.shoot(worldserver, entityhuman, entityhuman.getUsedItemHand(), itemstack, list, f * 3.0F, 1.0F, f == 1.0F, (EntityLiving) null);
                        }
                    }

                    world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                    return true;
                }
            }
        }
    }

    @Override
    protected void shootProjectile(EntityLiving entityliving, IProjectile iprojectile, int i, float f, float f1, float f2, @Nullable EntityLiving entityliving1) {
        iprojectile.shootFromRotation(entityliving, entityliving.getXRot(), entityliving.getYRot() + f2, 0.0F, f, f1);
    }

    public static float getPowerForTime(int i) {
        float f = (float) i / 20.0F;

        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        boolean flag = !entityhuman.getProjectile(itemstack).isEmpty();

        if (!entityhuman.hasInfiniteMaterials() && !flag) {
            return EnumInteractionResult.FAIL;
        } else {
            entityhuman.startUsingItem(enumhand);
            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ItemBow.ARROW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }
}
