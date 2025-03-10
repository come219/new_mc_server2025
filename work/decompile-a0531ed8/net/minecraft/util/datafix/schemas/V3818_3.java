package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3818_3 extends DataConverterSchemaNamed {

    public V3818_3(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedmap = new LinkedHashMap();

        sequencedmap.put("minecraft:bees", () -> {
            return DSL.list(DSL.optionalFields("entity_data", DataConverterTypes.ENTITY_TREE.in(schema)));
        });
        sequencedmap.put("minecraft:block_entity_data", () -> {
            return DataConverterTypes.BLOCK_ENTITY.in(schema);
        });
        sequencedmap.put("minecraft:bundle_contents", () -> {
            return DSL.list(DataConverterTypes.ITEM_STACK.in(schema));
        });
        sequencedmap.put("minecraft:can_break", () -> {
            return DSL.optionalFields("predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(DataConverterTypes.BLOCK_NAME.in(schema), DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))))));
        });
        sequencedmap.put("minecraft:can_place_on", () -> {
            return DSL.optionalFields("predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(DataConverterTypes.BLOCK_NAME.in(schema), DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))))));
        });
        sequencedmap.put("minecraft:charged_projectiles", () -> {
            return DSL.list(DataConverterTypes.ITEM_STACK.in(schema));
        });
        sequencedmap.put("minecraft:container", () -> {
            return DSL.list(DSL.optionalFields("item", DataConverterTypes.ITEM_STACK.in(schema)));
        });
        sequencedmap.put("minecraft:entity_data", () -> {
            return DataConverterTypes.ENTITY_TREE.in(schema);
        });
        sequencedmap.put("minecraft:pot_decorations", () -> {
            return DSL.list(DataConverterTypes.ITEM_NAME.in(schema));
        });
        sequencedmap.put("minecraft:food", () -> {
            return DSL.optionalFields("using_converts_to", DataConverterTypes.ITEM_STACK.in(schema));
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
