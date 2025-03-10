package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import java.util.stream.Stream;

public class CustomModelDataExpandFix extends DataFix {

    public CustomModelDataExpandFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.DATA_COMPONENTS);

        return this.fixTypeEverywhereTyped("Custom Model Data expansion", type, (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("minecraft:custom_model_data", (dynamic1) -> {
                    float f = dynamic1.asNumber(0.0F).floatValue();

                    return dynamic1.createMap(Map.of(dynamic1.createString("floats"), dynamic1.createList(Stream.of(dynamic1.createFloat(f)))));
                });
            });
        });
    }
}
