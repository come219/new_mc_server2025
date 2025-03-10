package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

public record PacketPlayInRecipeDisplayed(RecipeDisplayId recipe) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInRecipeDisplayed> STREAM_CODEC = StreamCodec.composite(RecipeDisplayId.STREAM_CODEC, PacketPlayInRecipeDisplayed::recipe, PacketPlayInRecipeDisplayed::new);

    @Override
    public PacketType<PacketPlayInRecipeDisplayed> type() {
        return GamePacketTypes.SERVERBOUND_RECIPE_BOOK_SEEN_RECIPE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleRecipeBookSeenRecipePacket(this);
    }
}
