package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;

public abstract class DataComponentRemainderFix extends DataFix {

    private final String name;
    private final String componentId;
    private final String newComponentId;

    public DataComponentRemainderFix(Schema schema, String s, String s1) {
        this(schema, s, s1, s1);
    }

    public DataComponentRemainderFix(Schema schema, String s, String s1, String s2) {
        super(schema, false);
        this.name = s;
        this.componentId = s1;
        this.newComponentId = s2;
    }

    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.DATA_COMPONENTS);

        return this.fixTypeEverywhereTyped(this.name, type, (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                Optional<? extends Dynamic<?>> optional = dynamic.get(this.componentId).result();

                if (optional.isEmpty()) {
                    return dynamic;
                } else {
                    Dynamic<?> dynamic1 = this.fixComponent((Dynamic) optional.get());

                    return dynamic.remove(this.componentId).setFieldIfPresent(this.newComponentId, Optional.ofNullable(dynamic1));
                }
            });
        });
    }

    @Nullable
    protected abstract <T> Dynamic<T> fixComponent(Dynamic<T> dynamic);
}
