package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class InvalidLockComponentFix extends DataComponentRemainderFix {

    private static final Optional<String> INVALID_LOCK_CUSTOM_NAME = Optional.of("\"\"");

    public InvalidLockComponentFix(Schema schema) {
        super(schema, "InvalidLockComponentPredicateFix", "minecraft:lock");
    }

    @Nullable
    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return fixLock(dynamic);
    }

    @Nullable
    public static <T> Dynamic<T> fixLock(Dynamic<T> dynamic) {
        return isBrokenLock(dynamic) ? null : dynamic;
    }

    private static <T> boolean isBrokenLock(Dynamic<T> dynamic) {
        return isMapWithOneField(dynamic, "components", (dynamic1) -> {
            return isMapWithOneField(dynamic1, "minecraft:custom_name", (dynamic2) -> {
                return dynamic2.asString().result().equals(InvalidLockComponentFix.INVALID_LOCK_CUSTOM_NAME);
            });
        });
    }

    private static <T> boolean isMapWithOneField(Dynamic<T> dynamic, String s, Predicate<Dynamic<T>> predicate) {
        Optional<Map<Dynamic<T>, Dynamic<T>>> optional = dynamic.getMapValues().result();

        return !optional.isEmpty() && ((Map) optional.get()).size() == 1 ? dynamic.get(s).result().filter(predicate).isPresent() : false;
    }
}
