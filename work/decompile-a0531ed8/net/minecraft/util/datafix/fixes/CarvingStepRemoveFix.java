package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class CarvingStepRemoveFix extends DataFix {

    public CarvingStepRemoveFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("CarvingStepRemoveFix", this.getInputSchema().getType(DataConverterTypes.CHUNK), CarvingStepRemoveFix::fixChunk);
    }

    private static Typed<?> fixChunk(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            Dynamic<?> dynamic1 = dynamic;
            Optional<? extends Dynamic<?>> optional = dynamic.get("CarvingMasks").result();

            if (optional.isPresent()) {
                Optional<? extends Dynamic<?>> optional1 = ((Dynamic) optional.get()).get("AIR").result();

                if (optional1.isPresent()) {
                    dynamic1 = dynamic.set("carving_mask", (Dynamic) optional1.get());
                }
            }

            return dynamic1.remove("CarvingMasks");
        });
    }
}
