package net.minecraft.network.protocol.game;

import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutWorldParticles implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutWorldParticles> STREAM_CODEC = Packet.codec(PacketPlayOutWorldParticles::write, PacketPlayOutWorldParticles::new);
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float maxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final boolean alwaysShow;
    private final ParticleParam particle;

    public <T extends ParticleParam> PacketPlayOutWorldParticles(T t0, boolean flag, boolean flag1, double d0, double d1, double d2, float f, float f1, float f2, float f3, int i) {
        this.particle = t0;
        this.overrideLimiter = flag;
        this.alwaysShow = flag1;
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.xDist = f;
        this.yDist = f1;
        this.zDist = f2;
        this.maxSpeed = f3;
        this.count = i;
    }

    private PacketPlayOutWorldParticles(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.overrideLimiter = registryfriendlybytebuf.readBoolean();
        this.alwaysShow = registryfriendlybytebuf.readBoolean();
        this.x = registryfriendlybytebuf.readDouble();
        this.y = registryfriendlybytebuf.readDouble();
        this.z = registryfriendlybytebuf.readDouble();
        this.xDist = registryfriendlybytebuf.readFloat();
        this.yDist = registryfriendlybytebuf.readFloat();
        this.zDist = registryfriendlybytebuf.readFloat();
        this.maxSpeed = registryfriendlybytebuf.readFloat();
        this.count = registryfriendlybytebuf.readInt();
        this.particle = (ParticleParam) Particles.STREAM_CODEC.decode(registryfriendlybytebuf);
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeBoolean(this.overrideLimiter);
        registryfriendlybytebuf.writeBoolean(this.alwaysShow);
        registryfriendlybytebuf.writeDouble(this.x);
        registryfriendlybytebuf.writeDouble(this.y);
        registryfriendlybytebuf.writeDouble(this.z);
        registryfriendlybytebuf.writeFloat(this.xDist);
        registryfriendlybytebuf.writeFloat(this.yDist);
        registryfriendlybytebuf.writeFloat(this.zDist);
        registryfriendlybytebuf.writeFloat(this.maxSpeed);
        registryfriendlybytebuf.writeInt(this.count);
        Particles.STREAM_CODEC.encode(registryfriendlybytebuf, this.particle);
    }

    @Override
    public PacketType<PacketPlayOutWorldParticles> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleParticleEvent(this);
    }

    public boolean isOverrideLimiter() {
        return this.overrideLimiter;
    }

    public boolean alwaysShow() {
        return this.alwaysShow;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getXDist() {
        return this.xDist;
    }

    public float getYDist() {
        return this.yDist;
    }

    public float getZDist() {
        return this.zDist;
    }

    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    public int getCount() {
        return this.count;
    }

    public ParticleParam getParticle() {
        return this.particle;
    }
}
