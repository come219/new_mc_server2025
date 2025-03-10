package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayInBEdit(int slot, List<String> pages, Optional<String> title) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInBEdit> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayInBEdit::slot, ByteBufCodecs.stringUtf8(1024).apply(ByteBufCodecs.list(100)), PacketPlayInBEdit::pages, ByteBufCodecs.stringUtf8(32).apply(ByteBufCodecs::optional), PacketPlayInBEdit::title, PacketPlayInBEdit::new);

    public PacketPlayInBEdit(int i, List<String> list, Optional<String> optional) {
        list = List.copyOf(list);
        this.slot = i;
        this.pages = list;
        this.title = optional;
    }

    @Override
    public PacketType<PacketPlayInBEdit> type() {
        return GamePacketTypes.SERVERBOUND_EDIT_BOOK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleEditBook(this);
    }
}
