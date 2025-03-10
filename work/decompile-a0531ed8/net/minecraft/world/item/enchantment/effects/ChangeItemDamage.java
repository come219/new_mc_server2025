package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3D;

public record ChangeItemDamage(LevelBasedValue amount) implements EnchantmentEntityEffect {

    public static final MapCodec<ChangeItemDamage> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter((changeitemdamage) -> {
            return changeitemdamage.amount;
        })).apply(instance, ChangeItemDamage::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        ItemStack itemstack = enchantediteminuse.itemStack();

        if (itemstack.has(DataComponents.MAX_DAMAGE) && itemstack.has(DataComponents.DAMAGE)) {
            EntityLiving entityliving = enchantediteminuse.owner();
            EntityPlayer entityplayer;

            if (entityliving instanceof EntityPlayer) {
                EntityPlayer entityplayer1 = (EntityPlayer) entityliving;

                entityplayer = entityplayer1;
            } else {
                entityplayer = null;
            }

            EntityPlayer entityplayer2 = entityplayer;
            int j = (int) this.amount.calculate(i);

            itemstack.hurtAndBreak(j, worldserver, entityplayer2, enchantediteminuse.onBreak());
        }

    }

    @Override
    public MapCodec<ChangeItemDamage> codec() {
        return ChangeItemDamage.CODEC;
    }
}
