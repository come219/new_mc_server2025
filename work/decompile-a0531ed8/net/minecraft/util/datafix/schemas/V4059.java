package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V4059 extends DataConverterSchemaNamed {

    public V4059(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedmap = V3818_3.components(schema);

        sequencedmap.remove("minecraft:food");
        sequencedmap.put("minecraft:use_remainder", () -> {
            return DataConverterTypes.ITEM_STACK.in(schema);
        });
        sequencedmap.put("minecraft:equippable", () -> {
            return DSL.optionalFields("allowed_entities", DSL.or(DataConverterTypes.ENTITY_NAME.in(schema), DSL.list(DataConverterTypes.ENTITY_NAME.in(schema))));
        });
        return sequencedmap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.DATA_COMPONENTS, () -> {
            return DSL.optionalFieldsLazy(components(schema));
        });
    }
}
