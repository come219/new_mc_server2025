package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;

public class V4071 extends DataConverterSchemaNamed {

    public V4071(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);

        schema.register(map, "minecraft:creaking", () -> {
            return DataConverterSchemaV100.equipment(schema);
        });
        schema.register(map, "minecraft:creaking_transient", () -> {
            return DataConverterSchemaV100.equipment(schema);
        });
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        schema.register(map, "minecraft:creaking_heart", () -> {
            return DSL.optionalFields(new Pair[0]);
        });
        return map;
    }
}
