package net.minecraft.world.entity.player;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record Input(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {

    private static final byte FLAG_FORWARD = 1;
    private static final byte FLAG_BACKWARD = 2;
    private static final byte FLAG_LEFT = 4;
    private static final byte FLAG_RIGHT = 8;
    private static final byte FLAG_JUMP = 16;
    private static final byte FLAG_SHIFT = 32;
    private static final byte FLAG_SPRINT = 64;
    public static final StreamCodec<PacketDataSerializer, Input> STREAM_CODEC = new StreamCodec<PacketDataSerializer, Input>() {
        public void encode(PacketDataSerializer packetdataserializer, Input input) {
            byte b0 = 0;
            byte b1 = (byte) (b0 | (input.forward() ? 1 : 0));

            b1 = (byte) (b1 | (input.backward() ? 2 : 0));
            b1 = (byte) (b1 | (input.left() ? 4 : 0));
            b1 = (byte) (b1 | (input.right() ? 8 : 0));
            b1 = (byte) (b1 | (input.jump() ? 16 : 0));
            b1 = (byte) (b1 | (input.shift() ? 32 : 0));
            b1 = (byte) (b1 | (input.sprint() ? 64 : 0));
            packetdataserializer.writeByte(b1);
        }

        public Input decode(PacketDataSerializer packetdataserializer) {
            byte b0 = packetdataserializer.readByte();
            boolean flag = (b0 & 1) != 0;
            boolean flag1 = (b0 & 2) != 0;
            boolean flag2 = (b0 & 4) != 0;
            boolean flag3 = (b0 & 8) != 0;
            boolean flag4 = (b0 & 16) != 0;
            boolean flag5 = (b0 & 32) != 0;
            boolean flag6 = (b0 & 64) != 0;

            return new Input(flag, flag1, flag2, flag3, flag4, flag5, flag6);
        }
    };
    public static Input EMPTY = new Input(false, false, false, false, false, false, false);
}
