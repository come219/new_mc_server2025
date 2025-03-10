package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class EquippableAssetRenameFix extends DataFix {

    public EquippableAssetRenameFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.DATA_COMPONENTS);
        OpticFinder<?> opticfinder = type.findField("minecraft:equippable");

        return this.fixTypeEverywhereTyped("equippable asset rename fix", type, (typed) -> {
            return typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), (dynamic) -> {
                    return dynamic.renameField("model", "asset_id");
                });
            });
        });
    }
}
