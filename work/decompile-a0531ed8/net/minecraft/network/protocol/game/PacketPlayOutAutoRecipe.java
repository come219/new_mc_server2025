package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public record PacketPlayOutAutoRecipe(int containerId, RecipeDisplay recipeDisplay) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutAutoRecipe> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.CONTAINER_ID, PacketPlayOutAutoRecipe::containerId, RecipeDisplay.STREAM_CODEC, PacketPlayOutAutoRecipe::recipeDisplay, PacketPlayOutAutoRecipe::new);

    @Override
    public PacketType<PacketPlayOutAutoRecipe> type() {
        return GamePacketTypes.CLIENTBOUND_PLACE_GHOST_RECIPE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handlePlaceRecipe(this);
    }
}
