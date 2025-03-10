package net.minecraft.util.datafix.fixes;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;

public class LockComponentPredicateFix extends DataComponentRemainderFix {

    public static final Escaper ESCAPER = Escapers.builder().addEscape('"', "\\\"").addEscape('\\', "\\\\").build();

    public LockComponentPredicateFix(Schema schema) {
        super(schema, "LockComponentPredicateFix", "minecraft:lock");
    }

    @Nullable
    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return fixLock(dynamic);
    }

    @Nullable
    public static <T> Dynamic<T> fixLock(Dynamic<T> dynamic) {
        Optional<String> optional = dynamic.asString().result();

        if (optional.isEmpty()) {
            return null;
        } else if (((String) optional.get()).isEmpty()) {
            return null;
        } else {
            Escaper escaper = LockComponentPredicateFix.ESCAPER;
            Dynamic<T> dynamic1 = dynamic.createString("\"" + escaper.escape((String) optional.get()) + "\"");
            Dynamic<T> dynamic2 = dynamic.emptyMap().set("minecraft:custom_name", dynamic1);

            return dynamic.emptyMap().set("components", dynamic2);
        }
    }
}
