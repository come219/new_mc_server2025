package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.redstone.Orientation;

public record RedstoneWireOrientationsDebugPayload(long time, List<RedstoneWireOrientationsDebugPayload.a> wires) implements CustomPacketPayload {

    public static final CustomPacketPayload.b<RedstoneWireOrientationsDebugPayload> TYPE = CustomPacketPayload.createType("debug/redstone_update_order");
    public static final StreamCodec<PacketDataSerializer, RedstoneWireOrientationsDebugPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_LONG, RedstoneWireOrientationsDebugPayload::time, RedstoneWireOrientationsDebugPayload.a.STREAM_CODEC.apply(ByteBufCodecs.list()), RedstoneWireOrientationsDebugPayload::wires, RedstoneWireOrientationsDebugPayload::new);

    @Override
    public CustomPacketPayload.b<RedstoneWireOrientationsDebugPayload> type() {
        return RedstoneWireOrientationsDebugPayload.TYPE;
    }

    public static record a(BlockPosition pos, Orientation orientation) {

        public static final StreamCodec<ByteBuf, RedstoneWireOrientationsDebugPayload.a> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, RedstoneWireOrientationsDebugPayload.a::pos, Orientation.STREAM_CODEC, RedstoneWireOrientationsDebugPayload.a::orientation, RedstoneWireOrientationsDebugPayload.a::new);
    }
}
