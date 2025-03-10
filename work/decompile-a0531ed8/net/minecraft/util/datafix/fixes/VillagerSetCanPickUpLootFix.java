package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerSetCanPickUpLootFix extends DataConverterNamedEntity {

    private static final String CAN_PICK_UP_LOOT = "CanPickUpLoot";

    public VillagerSetCanPickUpLootFix(Schema schema) {
        super(schema, true, "Villager CanPickUpLoot default value", DataConverterTypes.ENTITY, "Villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), VillagerSetCanPickUpLootFix::fixValue);
    }

    private static Dynamic<?> fixValue(Dynamic<?> dynamic) {
        return dynamic.set("CanPickUpLoot", dynamic.createBoolean(true));
    }
}
