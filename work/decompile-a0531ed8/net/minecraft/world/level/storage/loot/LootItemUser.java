package net.minecraft.world.level.storage.loot;

import java.util.Set;
import net.minecraft.util.context.ContextKey;

public interface LootItemUser {

    default Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of();
    }

    default void validate(LootCollector lootcollector) {
        lootcollector.validateContextUsage(this);
    }
}
