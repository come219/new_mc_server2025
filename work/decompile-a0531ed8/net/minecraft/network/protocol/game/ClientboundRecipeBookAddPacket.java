package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

public record ClientboundRecipeBookAddPacket(List<ClientboundRecipeBookAddPacket.a> entries, boolean replace) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRecipeBookAddPacket> STREAM_CODEC = StreamCodec.composite(ClientboundRecipeBookAddPacket.a.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRecipeBookAddPacket::entries, ByteBufCodecs.BOOL, ClientboundRecipeBookAddPacket::replace, ClientboundRecipeBookAddPacket::new);

    @Override
    public PacketType<ClientboundRecipeBookAddPacket> type() {
        return GamePacketTypes.CLIENTBOUND_RECIPE_BOOK_ADD;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleRecipeBookAdd(this);
    }

    public static record a(RecipeDisplayEntry contents, byte flags) {

        public static final byte FLAG_NOTIFICATION = 1;
        public static final byte FLAG_HIGHLIGHT = 2;
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRecipeBookAddPacket.a> STREAM_CODEC = StreamCodec.composite(RecipeDisplayEntry.STREAM_CODEC, ClientboundRecipeBookAddPacket.a::contents, ByteBufCodecs.BYTE, ClientboundRecipeBookAddPacket.a::flags, ClientboundRecipeBookAddPacket.a::new);

        public a(RecipeDisplayEntry recipedisplayentry, boolean flag, boolean flag1) {
            this(recipedisplayentry, (byte) ((flag ? 1 : 0) | (flag1 ? 2 : 0)));
        }

        public boolean notification() {
            return (this.flags & 1) != 0;
        }

        public boolean highlight() {
            return (this.flags & 2) != 0;
        }
    }
}
