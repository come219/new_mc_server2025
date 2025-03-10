package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class ContainerBlockEntityLockPredicateFix extends DataFix {

    public ContainerBlockEntityLockPredicateFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ContainerBlockEntityLockPredicateFix", this.getInputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY), ContainerBlockEntityLockPredicateFix::fixBlockEntity);
    }

    private static Typed<?> fixBlockEntity(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.renameAndFixField("Lock", "lock", LockComponentPredicateFix::fixLock);
        });
    }
}
