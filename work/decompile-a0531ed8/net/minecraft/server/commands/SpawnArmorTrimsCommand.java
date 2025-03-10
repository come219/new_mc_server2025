package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.SystemUtils;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.World;

public class SpawnArmorTrimsCommand {

    private static final List<ResourceKey<TrimPattern>> VANILLA_TRIM_PATTERNS = List.of(TrimPatterns.SENTRY, TrimPatterns.DUNE, TrimPatterns.COAST, TrimPatterns.WILD, TrimPatterns.WARD, TrimPatterns.EYE, TrimPatterns.VEX, TrimPatterns.TIDE, TrimPatterns.SNOUT, TrimPatterns.RIB, TrimPatterns.SPIRE, TrimPatterns.WAYFINDER, TrimPatterns.SHAPER, TrimPatterns.SILENCE, TrimPatterns.RAISER, TrimPatterns.HOST, TrimPatterns.FLOW, TrimPatterns.BOLT);
    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(TrimMaterials.QUARTZ, TrimMaterials.IRON, TrimMaterials.NETHERITE, TrimMaterials.REDSTONE, TrimMaterials.COPPER, TrimMaterials.GOLD, TrimMaterials.EMERALD, TrimMaterials.DIAMOND, TrimMaterials.LAPIS, TrimMaterials.AMETHYST, TrimMaterials.RESIN);
    private static final ToIntFunction<ResourceKey<TrimPattern>> TRIM_PATTERN_ORDER = SystemUtils.createIndexLookup(SpawnArmorTrimsCommand.VANILLA_TRIM_PATTERNS);
    private static final ToIntFunction<ResourceKey<TrimMaterial>> TRIM_MATERIAL_ORDER = SystemUtils.createIndexLookup(SpawnArmorTrimsCommand.VANILLA_TRIM_MATERIALS);

    public SpawnArmorTrimsCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("spawn_armor_trims").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).executes((commandcontext) -> {
            return spawnArmorTrims((CommandListenerWrapper) commandcontext.getSource(), ((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException());
        }));
    }

    private static int spawnArmorTrims(CommandListenerWrapper commandlistenerwrapper, EntityHuman entityhuman) {
        World world = entityhuman.level();
        NonNullList<ArmorTrim> nonnulllist = NonNullList.create();
        IRegistry<TrimPattern> iregistry = world.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
        IRegistry<TrimMaterial> iregistry1 = world.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
        HolderLookup<Item> holderlookup = world.holderLookup(Registries.ITEM);
        Map<ResourceKey<EquipmentAsset>, List<Item>> map = (Map) holderlookup.listElements().map(Holder.c::value).filter((item) -> {
            Equippable equippable = (Equippable) item.components().get(DataComponents.EQUIPPABLE);

            return equippable != null && equippable.slot().getType() == EnumItemSlot.Function.HUMANOID_ARMOR && equippable.assetId().isPresent();
        }).collect(Collectors.groupingBy((item) -> {
            return (ResourceKey) ((Equippable) item.components().get(DataComponents.EQUIPPABLE)).assetId().get();
        }));

        iregistry.stream().sorted(Comparator.comparing((trimpattern) -> {
            return SpawnArmorTrimsCommand.TRIM_PATTERN_ORDER.applyAsInt((ResourceKey) iregistry.getResourceKey(trimpattern).orElse((Object) null));
        })).forEachOrdered((trimpattern) -> {
            iregistry1.stream().sorted(Comparator.comparing((trimmaterial) -> {
                return SpawnArmorTrimsCommand.TRIM_MATERIAL_ORDER.applyAsInt((ResourceKey) iregistry1.getResourceKey(trimmaterial).orElse((Object) null));
            })).forEachOrdered((trimmaterial) -> {
                nonnulllist.add(new ArmorTrim(iregistry1.wrapAsHolder(trimmaterial), iregistry.wrapAsHolder(trimpattern)));
            });
        });
        BlockPosition blockposition = entityhuman.blockPosition().relative(entityhuman.getDirection(), 5);
        int i = map.size() - 1;
        double d0 = 3.0D;
        int j = 0;
        int k = 0;

        for (Iterator iterator = nonnulllist.iterator(); iterator.hasNext(); ++j) {
            ArmorTrim armortrim = (ArmorTrim) iterator.next();

            for (Iterator iterator1 = map.values().iterator(); iterator1.hasNext(); ++k) {
                List<Item> list = (List) iterator1.next();
                double d1 = (double) blockposition.getX() + 0.5D - (double) (j % iregistry1.size()) * 3.0D;
                double d2 = (double) blockposition.getY() + 0.5D + (double) (k % i) * 3.0D;
                double d3 = (double) blockposition.getZ() + 0.5D + (double) (j / iregistry1.size() * 10);
                EntityArmorStand entityarmorstand = new EntityArmorStand(world, d1, d2, d3);

                entityarmorstand.setYRot(180.0F);
                entityarmorstand.setNoGravity(true);
                Iterator iterator2 = list.iterator();

                while (iterator2.hasNext()) {
                    Item item = (Item) iterator2.next();
                    Equippable equippable = (Equippable) Objects.requireNonNull((Equippable) item.components().get(DataComponents.EQUIPPABLE));
                    ItemStack itemstack = new ItemStack(item);

                    itemstack.set(DataComponents.TRIM, armortrim);
                    entityarmorstand.setItemSlot(equippable.slot(), itemstack);
                    if (itemstack.is(Items.TURTLE_HELMET)) {
                        entityarmorstand.setCustomName(((TrimPattern) armortrim.pattern().value()).copyWithStyle(armortrim.material()).copy().append(" ").append(((TrimMaterial) armortrim.material().value()).description()));
                        entityarmorstand.setCustomNameVisible(true);
                    } else {
                        entityarmorstand.setInvisible(true);
                    }
                }

                world.addFreshEntity(entityarmorstand);
            }
        }

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.literal("Armorstands with trimmed armor spawned around you");
        }, true);
        return 1;
    }
}
