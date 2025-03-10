package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public class ServerExplosion implements Explosion {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private static final float LARGE_EXPLOSION_RADIUS = 2.0F;
    private final boolean fire;
    private final Explosion.Effect blockInteraction;
    private final WorldServer level;
    private final Vec3D center;
    @Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final Map<EntityHuman, Vec3D> hitPlayers = new HashMap();

    public ServerExplosion(WorldServer worldserver, @Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, Vec3D vec3d, float f, boolean flag, Explosion.Effect explosion_effect) {
        this.level = worldserver;
        this.source = entity;
        this.radius = f;
        this.center = vec3d;
        this.fire = flag;
        this.blockInteraction = explosion_effect;
        this.damageSource = damagesource == null ? worldserver.damageSources().explosion(this) : damagesource;
        this.damageCalculator = explosiondamagecalculator == null ? this.makeDamageCalculator(entity) : explosiondamagecalculator;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return (ExplosionDamageCalculator) (entity == null ? ServerExplosion.EXPLOSION_DAMAGE_CALCULATOR : new ExplosionDamageCalculatorEntity(entity));
    }

    public static float getSeenPercent(Vec3D vec3d, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            int i = 0;
            int j = 0;

            for (double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
                for (double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
                    for (double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
                        double d8 = MathHelper.lerp(d5, axisalignedbb.minX, axisalignedbb.maxX);
                        double d9 = MathHelper.lerp(d6, axisalignedbb.minY, axisalignedbb.maxY);
                        double d10 = MathHelper.lerp(d7, axisalignedbb.minZ, axisalignedbb.maxZ);
                        Vec3D vec3d1 = new Vec3D(d8 + d3, d9, d10 + d4);

                        if (entity.level().clip(new RayTrace(vec3d1, vec3d, RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, entity)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float) i / (float) j;
        } else {
            return 0.0F;
        }
    }

    @Override
    public float radius() {
        return this.radius;
    }

    @Override
    public Vec3D center() {
        return this.center;
    }

    private List<BlockPosition> calculateExplodedPositions() {
        Set<BlockPosition> set = new HashSet();
        boolean flag = true;

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    if (i == 0 || i == 15 || j == 0 || j == 15 || k == 0 || k == 15) {
                        double d0 = (double) ((float) i / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.center.x;
                        double d5 = this.center.y;
                        double d6 = this.center.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPosition blockposition = BlockPosition.containing(d4, d5, d6);
                            IBlockData iblockdata = this.level.getBlockState(blockposition);
                            Fluid fluid = this.level.getFluidState(blockposition);

                            if (!this.level.isInWorldBounds(blockposition)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockposition, iblockdata, fluid);

                            if (optional.isPresent()) {
                                f -= ((Float) optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockposition, iblockdata, f)) {
                                set.add(blockposition);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d5 += d1 * 0.30000001192092896D;
                            d6 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        return new ObjectArrayList(set);
    }

    private void hurtEntities() {
        float f = this.radius * 2.0F;
        int i = MathHelper.floor(this.center.x - (double) f - 1.0D);
        int j = MathHelper.floor(this.center.x + (double) f + 1.0D);
        int k = MathHelper.floor(this.center.y - (double) f - 1.0D);
        int l = MathHelper.floor(this.center.y + (double) f + 1.0D);
        int i1 = MathHelper.floor(this.center.z - (double) f - 1.0D);
        int j1 = MathHelper.floor(this.center.z + (double) f + 1.0D);
        List<Entity> list = this.level.getEntities(this.source, new AxisAlignedBB((double) i, (double) k, (double) i1, (double) j, (double) l, (double) j1));
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (!entity.ignoreExplosion(this)) {
                double d0 = Math.sqrt(entity.distanceToSqr(this.center)) / (double) f;

                if (d0 <= 1.0D) {
                    double d1 = entity.getX() - this.center.x;
                    double d2 = (entity instanceof EntityTNTPrimed ? entity.getY() : entity.getEyeY()) - this.center.y;
                    double d3 = entity.getZ() - this.center.z;
                    double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);

                    if (d4 != 0.0D) {
                        d1 /= d4;
                        d2 /= d4;
                        d3 /= d4;
                        boolean flag = this.damageCalculator.shouldDamageEntity(this, entity);
                        float f1 = this.damageCalculator.getKnockbackMultiplier(entity);
                        float f2 = !flag && f1 == 0.0F ? 0.0F : getSeenPercent(this.center, entity);

                        if (flag) {
                            entity.hurtServer(this.level, this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity, f2));
                        }

                        double d5 = (1.0D - d0) * (double) f2 * (double) f1;
                        double d6;

                        if (entity instanceof EntityLiving) {
                            EntityLiving entityliving = (EntityLiving) entity;

                            d6 = d5 * (1.0D - entityliving.getAttributeValue(GenericAttributes.EXPLOSION_KNOCKBACK_RESISTANCE));
                        } else {
                            d6 = d5;
                        }

                        d1 *= d6;
                        d2 *= d6;
                        d3 *= d6;
                        Vec3D vec3d = new Vec3D(d1, d2, d3);

                        entity.push(vec3d);
                        if (entity instanceof EntityHuman) {
                            EntityHuman entityhuman = (EntityHuman) entity;

                            if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.getAbilities().flying)) {
                                this.hitPlayers.put(entityhuman, vec3d);
                            }
                        }

                        entity.onExplosionHit(this.source);
                    }
                }
            }
        }

    }

    private void interactWithBlocks(List<BlockPosition> list) {
        List<ServerExplosion.a> list1 = new ArrayList();

        SystemUtils.shuffle(list, this.level.random);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            BlockPosition blockposition = (BlockPosition) iterator.next();

            this.level.getBlockState(blockposition).onExplosionHit(this.level, blockposition, this, (itemstack, blockposition1) -> {
                addOrAppendStack(list1, itemstack, blockposition1);
            });
        }

        iterator = list1.iterator();

        while (iterator.hasNext()) {
            ServerExplosion.a serverexplosion_a = (ServerExplosion.a) iterator.next();

            Block.popResource(this.level, serverexplosion_a.pos, serverexplosion_a.stack);
        }

    }

    private void createFire(List<BlockPosition> list) {
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            BlockPosition blockposition = (BlockPosition) iterator.next();

            if (this.level.random.nextInt(3) == 0 && this.level.getBlockState(blockposition).isAir() && this.level.getBlockState(blockposition.below()).isSolidRender()) {
                this.level.setBlockAndUpdate(blockposition, BlockFireAbstract.getState(this.level, blockposition));
            }
        }

    }

    public void explode() {
        this.level.gameEvent(this.source, (Holder) GameEvent.EXPLODE, this.center);
        List<BlockPosition> list = this.calculateExplodedPositions();

        this.hurtEntities();
        if (this.interactsWithBlocks()) {
            GameProfilerFiller gameprofilerfiller = Profiler.get();

            gameprofilerfiller.push("explosion_blocks");
            this.interactWithBlocks(list);
            gameprofilerfiller.pop();
        }

        if (this.fire) {
            this.createFire(list);
        }

    }

    private static void addOrAppendStack(List<ServerExplosion.a> list, ItemStack itemstack, BlockPosition blockposition) {
        Iterator iterator = list.iterator();

        do {
            if (!iterator.hasNext()) {
                list.add(new ServerExplosion.a(blockposition, itemstack));
                return;
            }

            ServerExplosion.a serverexplosion_a = (ServerExplosion.a) iterator.next();

            serverexplosion_a.tryMerge(itemstack);
        } while (!itemstack.isEmpty());

    }

    private boolean interactsWithBlocks() {
        return this.blockInteraction != Explosion.Effect.KEEP;
    }

    public Map<EntityHuman, Vec3D> getHitPlayers() {
        return this.hitPlayers;
    }

    @Override
    public WorldServer level() {
        return this.level;
    }

    @Nullable
    @Override
    public EntityLiving getIndirectSourceEntity() {
        return Explosion.getIndirectSourceEntity(this.source);
    }

    @Nullable
    @Override
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    @Override
    public Explosion.Effect getBlockInteraction() {
        return this.blockInteraction;
    }

    @Override
    public boolean canTriggerBlocks() {
        return this.blockInteraction != Explosion.Effect.TRIGGER_BLOCK ? false : (this.source != null && this.source.getType() == EntityTypes.BREEZE_WIND_CHARGE ? this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) : true);
    }

    @Override
    public boolean shouldAffectBlocklikeEntities() {
        boolean flag = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        boolean flag1 = this.source == null || !this.source.isInWater();
        boolean flag2 = this.source == null || this.source.getType() != EntityTypes.BREEZE_WIND_CHARGE && this.source.getType() != EntityTypes.WIND_CHARGE;

        return flag ? flag1 && flag2 : this.blockInteraction.shouldAffectBlocklikeEntities() && flag1 && flag2;
    }

    public boolean isSmall() {
        return this.radius < 2.0F || !this.interactsWithBlocks();
    }

    private static class a {

        final BlockPosition pos;
        ItemStack stack;

        a(BlockPosition blockposition, ItemStack itemstack) {
            this.pos = blockposition;
            this.stack = itemstack;
        }

        public void tryMerge(ItemStack itemstack) {
            if (EntityItem.areMergable(this.stack, itemstack)) {
                this.stack = EntityItem.merge(this.stack, itemstack, 16);
            }

        }
    }
}
