package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

public record PacketPlayInAutoRecipe(int containerId, RecipeDisplayId recipe, boolean useMaxItems) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInAutoRecipe> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.CONTAINER_ID, PacketPlayInAutoRecipe::containerId, RecipeDisplayId.STREAM_CODEC, PacketPlayInAutoRecipe::recipe, ByteBufCodecs.BOOL, PacketPlayInAutoRecipe::useMaxItems, PacketPlayInAutoRecipe::new);

    @Override
    public PacketType<PacketPlayInAutoRecipe> type() {
        return GamePacketTypes.SERVERBOUND_PLACE_RECIPE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePlaceRecipe(this);
    }
}
