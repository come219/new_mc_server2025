package net.minecraft.world.level.storage.loot.providers.nbt;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public interface NbtProvider {

    @Nullable
    NBTBase get(LootTableInfo loottableinfo);

    Set<ContextKey<?>> getReferencedContextParams();

    LootNbtProviderType getType();
}
