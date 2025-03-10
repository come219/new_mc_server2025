package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.phys.Vec3D;

public record PacketPlayOutExplosion(Vec3D center, Optional<Vec3D> playerKnockback, ParticleParam explosionParticle, Holder<SoundEffect> explosionSound) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutExplosion> STREAM_CODEC = StreamCodec.composite(Vec3D.STREAM_CODEC, PacketPlayOutExplosion::center, Vec3D.STREAM_CODEC.apply(ByteBufCodecs::optional), PacketPlayOutExplosion::playerKnockback, Particles.STREAM_CODEC, PacketPlayOutExplosion::explosionParticle, SoundEffect.STREAM_CODEC, PacketPlayOutExplosion::explosionSound, PacketPlayOutExplosion::new);

    @Override
    public PacketType<PacketPlayOutExplosion> type() {
        return GamePacketTypes.CLIENTBOUND_EXPLODE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleExplosion(this);
    }
}
