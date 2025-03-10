package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayOutUpdateTime(long gameTime, long dayTime, boolean tickDayTime) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutUpdateTime> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.LONG, PacketPlayOutUpdateTime::gameTime, ByteBufCodecs.LONG, PacketPlayOutUpdateTime::dayTime, ByteBufCodecs.BOOL, PacketPlayOutUpdateTime::tickDayTime, PacketPlayOutUpdateTime::new);

    @Override
    public PacketType<PacketPlayOutUpdateTime> type() {
        return GamePacketTypes.CLIENTBOUND_SET_TIME;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetTime(this);
    }
}
