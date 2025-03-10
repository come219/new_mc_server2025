package net.minecraft.world.item.crafting.display;

import net.minecraft.core.IRegistry;

public class SlotDisplays {

    public SlotDisplays() {}

    public static SlotDisplay.i<?> bootstrap(IRegistry<SlotDisplay.i<?>> iregistry) {
        IRegistry.register(iregistry, "empty", SlotDisplay.c.TYPE);
        IRegistry.register(iregistry, "any_fuel", SlotDisplay.a.TYPE);
        IRegistry.register(iregistry, "item", SlotDisplay.d.TYPE);
        IRegistry.register(iregistry, "item_stack", SlotDisplay.f.TYPE);
        IRegistry.register(iregistry, "tag", SlotDisplay.h.TYPE);
        IRegistry.register(iregistry, "smithing_trim", SlotDisplay.g.TYPE);
        IRegistry.register(iregistry, "with_remainder", SlotDisplay.j.TYPE);
        return (SlotDisplay.i) IRegistry.register(iregistry, "composite", SlotDisplay.b.TYPE);
    }
}
