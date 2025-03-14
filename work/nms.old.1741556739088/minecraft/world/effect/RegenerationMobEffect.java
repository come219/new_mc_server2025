package net.minecraft.world.effect;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;

class RegenerationMobEffect extends MobEffectList {

    protected RegenerationMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean applyEffectTick(WorldServer worldserver, EntityLiving entityliving, int i) {
        if (entityliving.getHealth() < entityliving.getMaxHealth()) {
            entityliving.heal(1.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.MAGIC_REGEN); // CraftBukkit
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        int k = 50 >> j;

        return k > 0 ? i % k == 0 : true;
    }
}
