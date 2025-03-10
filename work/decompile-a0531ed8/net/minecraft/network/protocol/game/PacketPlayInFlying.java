package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public abstract class PacketPlayInFlying implements Packet<PacketListenerPlayIn> {

    private static final int FLAG_ON_GROUND = 1;
    private static final int FLAG_HORIZONTAL_COLLISION = 2;
    public final double x;
    public final double y;
    public final double z;
    public final float yRot;
    public final float xRot;
    protected final boolean onGround;
    protected final boolean horizontalCollision;
    public final boolean hasPos;
    public final boolean hasRot;

    static int packFlags(boolean flag, boolean flag1) {
        int i = 0;

        if (flag) {
            i |= 1;
        }

        if (flag1) {
            i |= 2;
        }

        return i;
    }

    static boolean unpackOnGround(int i) {
        return (i & 1) != 0;
    }

    static boolean unpackHorizontalCollision(int i) {
        return (i & 2) != 0;
    }

    protected PacketPlayInFlying(double d0, double d1, double d2, float f, float f1, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.yRot = f;
        this.xRot = f1;
        this.onGround = flag;
        this.horizontalCollision = flag1;
        this.hasPos = flag2;
        this.hasRot = flag3;
    }

    @Override
    public abstract PacketType<? extends PacketPlayInFlying> type();

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleMovePlayer(this);
    }

    public double getX(double d0) {
        return this.hasPos ? this.x : d0;
    }

    public double getY(double d0) {
        return this.hasPos ? this.y : d0;
    }

    public double getZ(double d0) {
        return this.hasPos ? this.z : d0;
    }

    public float getYRot(float f) {
        return this.hasRot ? this.yRot : f;
    }

    public float getXRot(float f) {
        return this.hasRot ? this.xRot : f;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean horizontalCollision() {
        return this.horizontalCollision;
    }

    public boolean hasPosition() {
        return this.hasPos;
    }

    public boolean hasRotation() {
        return this.hasRot;
    }

    public static class d extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.d> STREAM_CODEC = Packet.codec(PacketPlayInFlying.d::write, PacketPlayInFlying.d::read);

        public d(boolean flag, boolean flag1) {
            super(0.0D, 0.0D, 0.0D, 0.0F, 0.0F, flag, flag1, false, false);
        }

        private static PacketPlayInFlying.d read(PacketDataSerializer packetdataserializer) {
            short short0 = packetdataserializer.readUnsignedByte();
            boolean flag = PacketPlayInFlying.unpackOnGround(short0);
            boolean flag1 = PacketPlayInFlying.unpackHorizontalCollision(short0);

            return new PacketPlayInFlying.d(flag, flag1);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeByte(PacketPlayInFlying.packFlags(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<PacketPlayInFlying.d> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_STATUS_ONLY;
        }
    }

    public static class PacketPlayInLook extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.PacketPlayInLook> STREAM_CODEC = Packet.codec(PacketPlayInFlying.PacketPlayInLook::write, PacketPlayInFlying.PacketPlayInLook::read);

        public PacketPlayInLook(float f, float f1, boolean flag, boolean flag1) {
            super(0.0D, 0.0D, 0.0D, f, f1, flag, flag1, false, true);
        }

        private static PacketPlayInFlying.PacketPlayInLook read(PacketDataSerializer packetdataserializer) {
            float f = packetdataserializer.readFloat();
            float f1 = packetdataserializer.readFloat();
            short short0 = packetdataserializer.readUnsignedByte();
            boolean flag = PacketPlayInFlying.unpackOnGround(short0);
            boolean flag1 = PacketPlayInFlying.unpackHorizontalCollision(short0);

            return new PacketPlayInFlying.PacketPlayInLook(f, f1, flag, flag1);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeFloat(this.yRot);
            packetdataserializer.writeFloat(this.xRot);
            packetdataserializer.writeByte(PacketPlayInFlying.packFlags(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<PacketPlayInFlying.PacketPlayInLook> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_ROT;
        }
    }

    public static class PacketPlayInPosition extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.PacketPlayInPosition> STREAM_CODEC = Packet.codec(PacketPlayInFlying.PacketPlayInPosition::write, PacketPlayInFlying.PacketPlayInPosition::read);

        public PacketPlayInPosition(double d0, double d1, double d2, boolean flag, boolean flag1) {
            super(d0, d1, d2, 0.0F, 0.0F, flag, flag1, true, false);
        }

        private static PacketPlayInFlying.PacketPlayInPosition read(PacketDataSerializer packetdataserializer) {
            double d0 = packetdataserializer.readDouble();
            double d1 = packetdataserializer.readDouble();
            double d2 = packetdataserializer.readDouble();
            short short0 = packetdataserializer.readUnsignedByte();
            boolean flag = PacketPlayInFlying.unpackOnGround(short0);
            boolean flag1 = PacketPlayInFlying.unpackHorizontalCollision(short0);

            return new PacketPlayInFlying.PacketPlayInPosition(d0, d1, d2, flag, flag1);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeDouble(this.x);
            packetdataserializer.writeDouble(this.y);
            packetdataserializer.writeDouble(this.z);
            packetdataserializer.writeByte(PacketPlayInFlying.packFlags(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<PacketPlayInFlying.PacketPlayInPosition> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS;
        }
    }

    public static class PacketPlayInPositionLook extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.PacketPlayInPositionLook> STREAM_CODEC = Packet.codec(PacketPlayInFlying.PacketPlayInPositionLook::write, PacketPlayInFlying.PacketPlayInPositionLook::read);

        public PacketPlayInPositionLook(double d0, double d1, double d2, float f, float f1, boolean flag, boolean flag1) {
            super(d0, d1, d2, f, f1, flag, flag1, true, true);
        }

        private static PacketPlayInFlying.PacketPlayInPositionLook read(PacketDataSerializer packetdataserializer) {
            double d0 = packetdataserializer.readDouble();
            double d1 = packetdataserializer.readDouble();
            double d2 = packetdataserializer.readDouble();
            float f = packetdataserializer.readFloat();
            float f1 = packetdataserializer.readFloat();
            short short0 = packetdataserializer.readUnsignedByte();
            boolean flag = PacketPlayInFlying.unpackOnGround(short0);
            boolean flag1 = PacketPlayInFlying.unpackHorizontalCollision(short0);

            return new PacketPlayInFlying.PacketPlayInPositionLook(d0, d1, d2, f, f1, flag, flag1);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeDouble(this.x);
            packetdataserializer.writeDouble(this.y);
            packetdataserializer.writeDouble(this.z);
            packetdataserializer.writeFloat(this.yRot);
            packetdataserializer.writeFloat(this.xRot);
            packetdataserializer.writeByte(PacketPlayInFlying.packFlags(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<PacketPlayInFlying.PacketPlayInPositionLook> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS_ROT;
        }
    }
}
