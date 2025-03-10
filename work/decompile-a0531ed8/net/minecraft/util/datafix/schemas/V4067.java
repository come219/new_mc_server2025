package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V4067 extends DataConverterSchemaNamed {

    public V4067(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);

        map.remove("minecraft:boat");
        map.remove("minecraft:chest_boat");
        this.registerSimple(map, "minecraft:oak_boat");
        this.registerSimple(map, "minecraft:spruce_boat");
        this.registerSimple(map, "minecraft:birch_boat");
        this.registerSimple(map, "minecraft:jungle_boat");
        this.registerSimple(map, "minecraft:acacia_boat");
        this.registerSimple(map, "minecraft:cherry_boat");
        this.registerSimple(map, "minecraft:dark_oak_boat");
        this.registerSimple(map, "minecraft:mangrove_boat");
        this.registerSimple(map, "minecraft:bamboo_raft");
        this.registerChestBoat(map, "minecraft:oak_chest_boat");
        this.registerChestBoat(map, "minecraft:spruce_chest_boat");
        this.registerChestBoat(map, "minecraft:birch_chest_boat");
        this.registerChestBoat(map, "minecraft:jungle_chest_boat");
        this.registerChestBoat(map, "minecraft:acacia_chest_boat");
        this.registerChestBoat(map, "minecraft:cherry_chest_boat");
        this.registerChestBoat(map, "minecraft:dark_oak_chest_boat");
        this.registerChestBoat(map, "minecraft:mangrove_chest_boat");
        this.registerChestBoat(map, "minecraft:bamboo_chest_raft");
        return map;
    }

    private void registerChestBoat(Map<String, Supplier<TypeTemplate>> map, String s) {
        this.register(map, s, (s1) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(this)));
        });
    }
}
