package net.minecraft.world.effect;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;

public class WitherMobEffect extends MobEffectList {

    public static final int DAMAGE_INTERVAL = 40;

    protected WitherMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean applyEffectTick(WorldServer worldserver, EntityLiving entityliving, int i) {
        entityliving.hurtServer(worldserver, entityliving.damageSources().wither(), 1.0F);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        int k = 40 >> j;

        return k > 0 ? i % k == 0 : true;
    }
}
