package net.minecraft.network.protocol.game;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeStonecutting;
import net.minecraft.world.item.crafting.SelectableRecipe;

public record PacketPlayOutRecipeUpdate(Map<ResourceKey<RecipePropertySet>, RecipePropertySet> itemSets, SelectableRecipe.b<RecipeStonecutting> stonecutterRecipes) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutRecipeUpdate> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.map(HashMap::new, ResourceKey.streamCodec(RecipePropertySet.TYPE_KEY), RecipePropertySet.STREAM_CODEC), PacketPlayOutRecipeUpdate::itemSets, SelectableRecipe.b.noRecipeCodec(), PacketPlayOutRecipeUpdate::stonecutterRecipes, PacketPlayOutRecipeUpdate::new);

    @Override
    public PacketType<PacketPlayOutRecipeUpdate> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_RECIPES;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleUpdateRecipes(this);
    }
}
