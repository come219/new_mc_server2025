package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class InvalidBlockEntityLockFix extends DataFix {

    public InvalidBlockEntityLockFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlockEntityLockToComponentFix", this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                Optional<? extends Dynamic<?>> optional = dynamic.get("lock").result();

                if (optional.isEmpty()) {
                    return dynamic;
                } else {
                    Dynamic<?> dynamic1 = InvalidLockComponentFix.fixLock((Dynamic) optional.get());

                    return dynamic1 != null ? dynamic.set("lock", dynamic1) : dynamic.remove("lock");
                }
            });
        });
    }
}
