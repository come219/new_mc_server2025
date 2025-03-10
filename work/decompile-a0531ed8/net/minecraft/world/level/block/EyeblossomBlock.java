package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.EntityBee;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class EyeblossomBlock extends BlockFlowers {

    public static final MapCodec<EyeblossomBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.BOOL.fieldOf("open").forGetter((eyeblossomblock) -> {
            return eyeblossomblock.type.open;
        }), propertiesCodec()).apply(instance, EyeblossomBlock::new);
    });
    private static final int EYEBLOSSOM_XZ_RANGE = 3;
    private static final int EYEBLOSSOM_Y_RANGE = 2;
    private final EyeblossomBlock.a type;

    @Override
    public MapCodec<? extends EyeblossomBlock> codec() {
        return EyeblossomBlock.CODEC;
    }

    public EyeblossomBlock(EyeblossomBlock.a eyeblossomblock_a, BlockBase.Info blockbase_info) {
        super(eyeblossomblock_a.effect, eyeblossomblock_a.effectDuration, blockbase_info);
        this.type = eyeblossomblock_a;
    }

    public EyeblossomBlock(boolean flag, BlockBase.Info blockbase_info) {
        super(EyeblossomBlock.a.fromBoolean(flag).effect, EyeblossomBlock.a.fromBoolean(flag).effectDuration, blockbase_info);
        this.type = EyeblossomBlock.a.fromBoolean(flag);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (this.type.emitSounds() && randomsource.nextInt(700) == 0) {
            IBlockData iblockdata1 = world.getBlockState(blockposition.below());

            if (iblockdata1.is(Blocks.PALE_MOSS_BLOCK)) {
                world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.EYEBLOSSOM_IDLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }

    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (this.tryChangingState(iblockdata, worldserver, blockposition, randomsource)) {
            worldserver.playSound((EntityHuman) null, blockposition, this.type.transform().longSwitchSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        super.randomTick(iblockdata, worldserver, blockposition, randomsource);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (this.tryChangingState(iblockdata, worldserver, blockposition, randomsource)) {
            worldserver.playSound((EntityHuman) null, blockposition, this.type.transform().shortSwitchSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        super.tick(iblockdata, worldserver, blockposition, randomsource);
    }

    private boolean tryChangingState(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!worldserver.dimensionType().natural()) {
            return false;
        } else if (worldserver.isDay() != this.type.open) {
            return false;
        } else {
            EyeblossomBlock.a eyeblossomblock_a = this.type.transform();

            worldserver.setBlock(blockposition, eyeblossomblock_a.state(), 3);
            worldserver.gameEvent((Holder) GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(iblockdata));
            eyeblossomblock_a.spawnTransformParticle(worldserver, blockposition, randomsource);
            BlockPosition.betweenClosed(blockposition.offset(-3, -2, -3), blockposition.offset(3, 2, 3)).forEach((blockposition1) -> {
                IBlockData iblockdata1 = worldserver.getBlockState(blockposition1);

                if (iblockdata1 == iblockdata) {
                    double d0 = Math.sqrt(blockposition.distSqr(blockposition1));
                    int i = randomsource.nextIntBetweenInclusive((int) (d0 * 5.0D), (int) (d0 * 10.0D));

                    worldserver.scheduleTick(blockposition1, iblockdata.getBlock(), i);
                }

            });
            return true;
        }
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (!world.isClientSide() && world.getDifficulty() != EnumDifficulty.PEACEFUL && entity instanceof EntityBee entitybee) {
            if (EntityBee.attractsBees(iblockdata) && !entitybee.hasEffect(MobEffects.POISON)) {
                entitybee.addEffect(this.getBeeInteractionEffect());
            }
        }

    }

    @Override
    public MobEffect getBeeInteractionEffect() {
        return new MobEffect(MobEffects.POISON, 25);
    }

    public static enum a {

        OPEN(true, MobEffects.BLINDNESS, 11.0F, SoundEffects.EYEBLOSSOM_OPEN_LONG, SoundEffects.EYEBLOSSOM_OPEN, 16545810), CLOSED(false, MobEffects.CONFUSION, 7.0F, SoundEffects.EYEBLOSSOM_CLOSE_LONG, SoundEffects.EYEBLOSSOM_CLOSE, 6250335);

        final boolean open;
        final Holder<MobEffectList> effect;
        final float effectDuration;
        final SoundEffect longSwitchSound;
        final SoundEffect shortSwitchSound;
        private final int particleColor;

        private a(final boolean flag, final Holder holder, final float f, final SoundEffect soundeffect, final SoundEffect soundeffect1, final int i) {
            this.open = flag;
            this.effect = holder;
            this.effectDuration = f;
            this.longSwitchSound = soundeffect;
            this.shortSwitchSound = soundeffect1;
            this.particleColor = i;
        }

        public Block block() {
            return this.open ? Blocks.OPEN_EYEBLOSSOM : Blocks.CLOSED_EYEBLOSSOM;
        }

        public IBlockData state() {
            return this.block().defaultBlockState();
        }

        public EyeblossomBlock.a transform() {
            return fromBoolean(!this.open);
        }

        public boolean emitSounds() {
            return this.open;
        }

        public static EyeblossomBlock.a fromBoolean(boolean flag) {
            return flag ? EyeblossomBlock.a.OPEN : EyeblossomBlock.a.CLOSED;
        }

        public void spawnTransformParticle(WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
            Vec3D vec3d = blockposition.getCenter();
            double d0 = 0.5D + randomsource.nextDouble();
            Vec3D vec3d1 = new Vec3D(randomsource.nextDouble() - 0.5D, randomsource.nextDouble() + 1.0D, randomsource.nextDouble() - 0.5D);
            Vec3D vec3d2 = vec3d.add(vec3d1.scale(d0));
            TrailParticleOption trailparticleoption = new TrailParticleOption(vec3d2, this.particleColor, (int) (20.0D * d0));

            worldserver.sendParticles(trailparticleoption, vec3d.x, vec3d.y, vec3d.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        public SoundEffect longSwitchSound() {
            return this.longSwitchSound;
        }
    }
}
