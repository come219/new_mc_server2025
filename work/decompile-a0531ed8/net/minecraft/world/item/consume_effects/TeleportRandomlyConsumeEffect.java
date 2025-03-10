package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntityFox;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public record TeleportRandomlyConsumeEffect(float diameter) implements ConsumeEffect {

    private static final float DEFAULT_DIAMETER = 16.0F;
    public static final MapCodec<TeleportRandomlyConsumeEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("diameter", 16.0F).forGetter(TeleportRandomlyConsumeEffect::diameter)).apply(instance, TeleportRandomlyConsumeEffect::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportRandomlyConsumeEffect> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, TeleportRandomlyConsumeEffect::diameter, TeleportRandomlyConsumeEffect::new);

    public TeleportRandomlyConsumeEffect() {
        this(16.0F);
    }

    @Override
    public ConsumeEffect.a<TeleportRandomlyConsumeEffect> getType() {
        return ConsumeEffect.a.TELEPORT_RANDOMLY;
    }

    @Override
    public boolean apply(World world, ItemStack itemstack, EntityLiving entityliving) {
        boolean flag = false;

        for (int i = 0; i < 16; ++i) {
            double d0 = entityliving.getX() + (entityliving.getRandom().nextDouble() - 0.5D) * (double) this.diameter;
            double d1 = MathHelper.clamp(entityliving.getY() + (entityliving.getRandom().nextDouble() - 0.5D) * (double) this.diameter, (double) world.getMinY(), (double) (world.getMinY() + ((WorldServer) world).getLogicalHeight() - 1));
            double d2 = entityliving.getZ() + (entityliving.getRandom().nextDouble() - 0.5D) * (double) this.diameter;

            if (entityliving.isPassenger()) {
                entityliving.stopRiding();
            }

            Vec3D vec3d = entityliving.position();

            if (entityliving.randomTeleport(d0, d1, d2, true)) {
                world.gameEvent((Holder) GameEvent.TELEPORT, vec3d, GameEvent.a.of((Entity) entityliving));
                SoundEffect soundeffect;
                SoundCategory soundcategory;

                if (entityliving instanceof EntityFox) {
                    soundeffect = SoundEffects.FOX_TELEPORT;
                    soundcategory = SoundCategory.NEUTRAL;
                } else {
                    soundeffect = SoundEffects.CHORUS_FRUIT_TELEPORT;
                    soundcategory = SoundCategory.PLAYERS;
                }

                world.playSound((EntityHuman) null, entityliving.getX(), entityliving.getY(), entityliving.getZ(), soundeffect, soundcategory);
                entityliving.resetFallDistance();
                flag = true;
                break;
            }
        }

        if (flag && entityliving instanceof EntityHuman entityhuman) {
            entityhuman.resetCurrentImpulseContext();
        }

        return flag;
    }
}
