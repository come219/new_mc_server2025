package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.nbt.NBTBase;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public record StorageNbtProvider(MinecraftKey id) implements NbtProvider {

    public static final MapCodec<StorageNbtProvider> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("source").forGetter(StorageNbtProvider::id)).apply(instance, StorageNbtProvider::new);
    });

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.STORAGE;
    }

    @Override
    public NBTBase get(LootTableInfo loottableinfo) {
        return loottableinfo.getLevel().getServer().getCommandStorage().get(this.id);
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of();
    }
}
