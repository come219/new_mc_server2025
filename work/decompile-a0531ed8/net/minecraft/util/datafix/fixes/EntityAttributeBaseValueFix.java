package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class EntityAttributeBaseValueFix extends DataConverterNamedEntity {

    private final String attributeId;
    private final DoubleUnaryOperator valueFixer;

    public EntityAttributeBaseValueFix(Schema schema, String s, String s1, String s2, DoubleUnaryOperator doubleunaryoperator) {
        super(schema, false, s, DataConverterTypes.ENTITY, s1);
        this.attributeId = s2;
        this.valueFixer = doubleunaryoperator;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixValue);
    }

    private Dynamic<?> fixValue(Dynamic<?> dynamic) {
        return dynamic.update("attributes", (dynamic1) -> {
            return dynamic.createList(dynamic1.asStream().map((dynamic2) -> {
                String s = DataConverterSchemaNamed.ensureNamespaced(dynamic2.get("id").asString(""));

                if (!s.equals(this.attributeId)) {
                    return dynamic2;
                } else {
                    double d0 = dynamic2.get("base").asDouble(0.0D);

                    return dynamic2.set("base", dynamic2.createDouble(this.valueFixer.applyAsDouble(d0)));
                }
            }));
        });
    }
}
