package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;

public class LootParams {

    private final WorldServer level;
    private final ContextMap params;
    private final Map<MinecraftKey, LootParams.b> dynamicDrops;
    private final float luck;

    public LootParams(WorldServer worldserver, ContextMap contextmap, Map<MinecraftKey, LootParams.b> map, float f) {
        this.level = worldserver;
        this.params = contextmap;
        this.dynamicDrops = map;
        this.luck = f;
    }

    public WorldServer getLevel() {
        return this.level;
    }

    public ContextMap contextMap() {
        return this.params;
    }

    public void addDynamicDrops(MinecraftKey minecraftkey, Consumer<ItemStack> consumer) {
        LootParams.b lootparams_b = (LootParams.b) this.dynamicDrops.get(minecraftkey);

        if (lootparams_b != null) {
            lootparams_b.add(consumer);
        }

    }

    public float getLuck() {
        return this.luck;
    }

    @FunctionalInterface
    public interface b {

        void add(Consumer<ItemStack> consumer);
    }

    public static class a {

        private final WorldServer level;
        private final ContextMap.a params = new ContextMap.a();
        private final Map<MinecraftKey, LootParams.b> dynamicDrops = Maps.newHashMap();
        private float luck;

        public a(WorldServer worldserver) {
            this.level = worldserver;
        }

        public WorldServer getLevel() {
            return this.level;
        }

        public <T> LootParams.a withParameter(ContextKey<T> contextkey, T t0) {
            this.params.withParameter(contextkey, t0);
            return this;
        }

        public <T> LootParams.a withOptionalParameter(ContextKey<T> contextkey, @Nullable T t0) {
            this.params.withOptionalParameter(contextkey, t0);
            return this;
        }

        public <T> T getParameter(ContextKey<T> contextkey) {
            return this.params.getParameter(contextkey);
        }

        @Nullable
        public <T> T getOptionalParameter(ContextKey<T> contextkey) {
            return this.params.getOptionalParameter(contextkey);
        }

        public LootParams.a withDynamicDrop(MinecraftKey minecraftkey, LootParams.b lootparams_b) {
            LootParams.b lootparams_b1 = (LootParams.b) this.dynamicDrops.put(minecraftkey, lootparams_b);

            if (lootparams_b1 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + String.valueOf(this.dynamicDrops) + "'");
            } else {
                return this;
            }
        }

        public LootParams.a withLuck(float f) {
            this.luck = f;
            return this;
        }

        public LootParams create(ContextKeySet contextkeyset) {
            ContextMap contextmap = this.params.create(contextkeyset);

            return new LootParams(this.level, contextmap, this.dynamicDrops, this.luck);
        }
    }
}
