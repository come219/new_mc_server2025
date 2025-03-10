package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetCustomModelDataFunction extends LootItemFunctionConditional {

    private static final Codec<NumberProvider> COLOR_PROVIDER_CODEC = Codec.withAlternative(NumberProviders.CODEC, ExtraCodecs.RGB_COLOR_CODEC, ConstantValue::new);
    public static final MapCodec<SetCustomModelDataFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(ListOperation.e.codec(NumberProviders.CODEC, Integer.MAX_VALUE).optionalFieldOf("floats").forGetter((setcustommodeldatafunction) -> {
            return setcustommodeldatafunction.floats;
        }), ListOperation.e.codec(Codec.BOOL, Integer.MAX_VALUE).optionalFieldOf("flags").forGetter((setcustommodeldatafunction) -> {
            return setcustommodeldatafunction.flags;
        }), ListOperation.e.codec(Codec.STRING, Integer.MAX_VALUE).optionalFieldOf("strings").forGetter((setcustommodeldatafunction) -> {
            return setcustommodeldatafunction.strings;
        }), ListOperation.e.codec(SetCustomModelDataFunction.COLOR_PROVIDER_CODEC, Integer.MAX_VALUE).optionalFieldOf("colors").forGetter((setcustommodeldatafunction) -> {
            return setcustommodeldatafunction.colors;
        }))).apply(instance, SetCustomModelDataFunction::new);
    });
    private final Optional<ListOperation.e<NumberProvider>> floats;
    private final Optional<ListOperation.e<Boolean>> flags;
    private final Optional<ListOperation.e<String>> strings;
    private final Optional<ListOperation.e<NumberProvider>> colors;

    public SetCustomModelDataFunction(List<LootItemCondition> list, Optional<ListOperation.e<NumberProvider>> optional, Optional<ListOperation.e<Boolean>> optional1, Optional<ListOperation.e<String>> optional2, Optional<ListOperation.e<NumberProvider>> optional3) {
        super(list);
        this.floats = optional;
        this.flags = optional1;
        this.strings = optional2;
        this.colors = optional3;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return (Set) Stream.concat(this.floats.stream(), this.colors.stream()).flatMap((listoperation_e) -> {
            return listoperation_e.value().stream();
        }).flatMap((numberprovider) -> {
            return numberprovider.getReferencedContextParams().stream();
        }).collect(Collectors.toSet());
    }

    @Override
    public LootItemFunctionType<SetCustomModelDataFunction> getType() {
        return LootItemFunctions.SET_CUSTOM_MODEL_DATA;
    }

    private static <T> List<T> apply(Optional<ListOperation.e<T>> optional, List<T> list) {
        return (List) optional.map((listoperation_e) -> {
            return listoperation_e.apply(list);
        }).orElse(list);
    }

    private static <T, E> List<E> apply(Optional<ListOperation.e<T>> optional, List<E> list, Function<T, E> function) {
        return (List) optional.map((listoperation_e) -> {
            List<E> list1 = listoperation_e.value().stream().map(function).toList();

            return listoperation_e.operation().apply(list, list1);
        }).orElse(list);
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        CustomModelData custommodeldata = (CustomModelData) itemstack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);

        itemstack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(apply(this.floats, custommodeldata.floats(), (numberprovider) -> {
            return numberprovider.getFloat(loottableinfo);
        }), apply(this.flags, custommodeldata.flags()), apply(this.strings, custommodeldata.strings()), apply(this.colors, custommodeldata.colors(), (numberprovider) -> {
            return numberprovider.getInt(loottableinfo);
        })));
        return itemstack;
    }
}
