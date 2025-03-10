package net.minecraft.world.item.equipment.trim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class TrimMaterials {

    public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");
    public static final ResourceKey<TrimMaterial> RESIN = registryKey("resin");

    public TrimMaterials() {}

    public static void bootstrap(BootstrapContext<TrimMaterial> bootstrapcontext) {
        register(bootstrapcontext, TrimMaterials.QUARTZ, Items.QUARTZ, ChatModifier.EMPTY.withColor(14931140));
        register(bootstrapcontext, TrimMaterials.IRON, Items.IRON_INGOT, ChatModifier.EMPTY.withColor(15527148), Map.of(EquipmentAssets.IRON, "iron_darker"));
        register(bootstrapcontext, TrimMaterials.NETHERITE, Items.NETHERITE_INGOT, ChatModifier.EMPTY.withColor(6445145), Map.of(EquipmentAssets.NETHERITE, "netherite_darker"));
        register(bootstrapcontext, TrimMaterials.REDSTONE, Items.REDSTONE, ChatModifier.EMPTY.withColor(9901575));
        register(bootstrapcontext, TrimMaterials.COPPER, Items.COPPER_INGOT, ChatModifier.EMPTY.withColor(11823181));
        register(bootstrapcontext, TrimMaterials.GOLD, Items.GOLD_INGOT, ChatModifier.EMPTY.withColor(14594349), Map.of(EquipmentAssets.GOLD, "gold_darker"));
        register(bootstrapcontext, TrimMaterials.EMERALD, Items.EMERALD, ChatModifier.EMPTY.withColor(1155126));
        register(bootstrapcontext, TrimMaterials.DIAMOND, Items.DIAMOND, ChatModifier.EMPTY.withColor(7269586), Map.of(EquipmentAssets.DIAMOND, "diamond_darker"));
        register(bootstrapcontext, TrimMaterials.LAPIS, Items.LAPIS_LAZULI, ChatModifier.EMPTY.withColor(4288151));
        register(bootstrapcontext, TrimMaterials.AMETHYST, Items.AMETHYST_SHARD, ChatModifier.EMPTY.withColor(10116294));
        register(bootstrapcontext, TrimMaterials.RESIN, Items.RESIN_BRICK, ChatModifier.EMPTY.withColor(16545810));
    }

    public static Optional<Holder.c<TrimMaterial>> getFromIngredient(HolderLookup.a holderlookup_a, ItemStack itemstack) {
        return holderlookup_a.lookupOrThrow(Registries.TRIM_MATERIAL).listElements().filter((holder_c) -> {
            return itemstack.is(((TrimMaterial) holder_c.value()).ingredient());
        }).findFirst();
    }

    private static void register(BootstrapContext<TrimMaterial> bootstrapcontext, ResourceKey<TrimMaterial> resourcekey, Item item, ChatModifier chatmodifier) {
        register(bootstrapcontext, resourcekey, item, chatmodifier, Map.of());
    }

    private static void register(BootstrapContext<TrimMaterial> bootstrapcontext, ResourceKey<TrimMaterial> resourcekey, Item item, ChatModifier chatmodifier, Map<ResourceKey<EquipmentAsset>, String> map) {
        TrimMaterial trimmaterial = TrimMaterial.create(resourcekey.location().getPath(), item, IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("trim_material", resourcekey.location())).withStyle(chatmodifier), map);

        bootstrapcontext.register(resourcekey, trimmaterial);
    }

    private static ResourceKey<TrimMaterial> registryKey(String s) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, MinecraftKey.withDefaultNamespace(s));
    }
}
