package net.minecraft.world.item.crafting.display;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.FuelValues;

public class SlotDisplayContext {

    public static final ContextKey<FuelValues> FUEL_VALUES = ContextKey.vanilla("fuel_values");
    public static final ContextKey<HolderLookup.a> REGISTRIES = ContextKey.vanilla("registries");
    public static final ContextKeySet CONTEXT = (new ContextKeySet.a()).optional(SlotDisplayContext.FUEL_VALUES).optional(SlotDisplayContext.REGISTRIES).build();

    public SlotDisplayContext() {}

    public static ContextMap fromLevel(World world) {
        return (new ContextMap.a()).withParameter(SlotDisplayContext.FUEL_VALUES, world.fuelValues()).withParameter(SlotDisplayContext.REGISTRIES, world.registryAccess()).create(SlotDisplayContext.CONTEXT);
    }
}
