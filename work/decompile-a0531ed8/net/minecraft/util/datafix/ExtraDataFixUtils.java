package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class ExtraDataFixUtils {

    public ExtraDataFixUtils() {}

    public static Dynamic<?> fixBlockPos(Dynamic<?> dynamic) {
        Optional<Number> optional = dynamic.get("X").asNumber().result();
        Optional<Number> optional1 = dynamic.get("Y").asNumber().result();
        Optional<Number> optional2 = dynamic.get("Z").asNumber().result();

        return !optional.isEmpty() && !optional1.isEmpty() && !optional2.isEmpty() ? dynamic.createIntList(IntStream.of(new int[]{((Number) optional.get()).intValue(), ((Number) optional1.get()).intValue(), ((Number) optional2.get()).intValue()})) : dynamic;
    }

    public static <T, R> Typed<R> cast(Type<R> type, Typed<T> typed) {
        return new Typed(type, typed.getOps(), typed.getValue());
    }

    public static Type<?> patchSubType(Type<?> type, Type<?> type1, Type<?> type2) {
        return type.all(typePatcher(type1, type2), true, false).view().newType();
    }

    private static <A, B> TypeRewriteRule typePatcher(Type<A> type, Type<B> type1) {
        RewriteResult<A, B> rewriteresult = RewriteResult.create(View.create("Patcher", type, type1, (dynamicops) -> {
            return (object) -> {
                throw new UnsupportedOperationException();
            };
        }), new BitSet());

        return TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(type, rewriteresult), PointFreeRule.nop(), true, true);
    }

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... afunction) {
        return (typed) -> {
            Function[] afunction1 = afunction;
            int i = afunction.length;

            for (int j = 0; j < i; ++j) {
                Function<Typed<?>, Typed<?>> function = afunction1[j];

                typed = (Typed) function.apply(typed);
            }

            return typed;
        };
    }

    public static Dynamic<?> blockState(String s, Map<String, String> map) {
        Dynamic<NBTBase> dynamic = new Dynamic(DynamicOpsNBT.INSTANCE, new NBTTagCompound());
        Dynamic<NBTBase> dynamic1 = dynamic.set("Name", dynamic.createString(s));

        if (!map.isEmpty()) {
            dynamic1 = dynamic1.set("Properties", dynamic.createMap((Map) map.entrySet().stream().collect(Collectors.toMap((entry) -> {
                return dynamic.createString((String) entry.getKey());
            }, (entry) -> {
                return dynamic.createString((String) entry.getValue());
            }))));
        }

        return dynamic1;
    }

    public static Dynamic<?> blockState(String s) {
        return blockState(s, Map.of());
    }

    public static Dynamic<?> fixStringField(Dynamic<?> dynamic, String s, UnaryOperator<String> unaryoperator) {
        return dynamic.update(s, (dynamic1) -> {
            DataResult dataresult = dynamic1.asString().map(unaryoperator);

            Objects.requireNonNull(dynamic);
            return (Dynamic) DataFixUtils.orElse(dataresult.map(dynamic::createString).result(), dynamic1);
        });
    }
}
