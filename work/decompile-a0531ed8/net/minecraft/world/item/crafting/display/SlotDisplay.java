package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.block.entity.FuelValues;

public interface SlotDisplay {

    Codec<SlotDisplay> CODEC = BuiltInRegistries.SLOT_DISPLAY.byNameCodec().dispatch(SlotDisplay::type, SlotDisplay.i::codec);
    StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> STREAM_CODEC = ByteBufCodecs.registry(Registries.SLOT_DISPLAY).dispatch(SlotDisplay::type, SlotDisplay.i::streamCodec);

    <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory);

    SlotDisplay.i<? extends SlotDisplay> type();

    default boolean isEnabled(FeatureFlagSet featureflagset) {
        return true;
    }

    default List<ItemStack> resolveForStacks(ContextMap contextmap) {
        return this.resolve(contextmap, SlotDisplay.e.INSTANCE).toList();
    }

    default ItemStack resolveForFirstStack(ContextMap contextmap) {
        return (ItemStack) this.resolve(contextmap, SlotDisplay.e.INSTANCE).findFirst().orElse(ItemStack.EMPTY);
    }

    public static class e implements DisplayContentsFactory.b<ItemStack> {

        public static final SlotDisplay.e INSTANCE = new SlotDisplay.e();

        public e() {}

        @Override
        public ItemStack forStack(ItemStack itemstack) {
            return itemstack;
        }
    }

    public static record j(SlotDisplay input, SlotDisplay remainder) implements SlotDisplay {

        public static final MapCodec<SlotDisplay.j> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(SlotDisplay.CODEC.fieldOf("input").forGetter(SlotDisplay.j::input), SlotDisplay.CODEC.fieldOf("remainder").forGetter(SlotDisplay.j::remainder)).apply(instance, SlotDisplay.j::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.j> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, SlotDisplay.j::input, SlotDisplay.STREAM_CODEC, SlotDisplay.j::remainder, SlotDisplay.j::new);
        public static final SlotDisplay.i<SlotDisplay.j> TYPE = new SlotDisplay.i<>(SlotDisplay.j.MAP_CODEC, SlotDisplay.j.STREAM_CODEC);

        @Override
        public SlotDisplay.i<SlotDisplay.j> type() {
            return SlotDisplay.j.TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            if (displaycontentsfactory instanceof DisplayContentsFactory.a<T> displaycontentsfactory_a) {
                List<T> list = this.remainder.resolve(contextmap, displaycontentsfactory).toList();

                return this.input.resolve(contextmap, displaycontentsfactory).map((object) -> {
                    return displaycontentsfactory_a.addRemainder(object, list);
                });
            } else {
                return this.input.resolve(contextmap, displaycontentsfactory);
            }
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureflagset) {
            return this.input.isEnabled(featureflagset) && this.remainder.isEnabled(featureflagset);
        }
    }

    public static record b(List<SlotDisplay> contents) implements SlotDisplay {

        public static final MapCodec<SlotDisplay.b> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(SlotDisplay.CODEC.listOf().fieldOf("contents").forGetter(SlotDisplay.b::contents)).apply(instance, SlotDisplay.b::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.b> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), SlotDisplay.b::contents, SlotDisplay.b::new);
        public static final SlotDisplay.i<SlotDisplay.b> TYPE = new SlotDisplay.i<>(SlotDisplay.b.MAP_CODEC, SlotDisplay.b.STREAM_CODEC);

        @Override
        public SlotDisplay.i<SlotDisplay.b> type() {
            return SlotDisplay.b.TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            return this.contents.stream().flatMap((slotdisplay) -> {
                return slotdisplay.resolve(contextmap, displaycontentsfactory);
            });
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureflagset) {
            return this.contents.stream().allMatch((slotdisplay) -> {
                return slotdisplay.isEnabled(featureflagset);
            });
        }
    }

    public static record h(TagKey<Item> tag) implements SlotDisplay {

        public static final MapCodec<SlotDisplay.h> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(SlotDisplay.h::tag)).apply(instance, SlotDisplay.h::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.h> STREAM_CODEC = StreamCodec.composite(TagKey.streamCodec(Registries.ITEM), SlotDisplay.h::tag, SlotDisplay.h::new);
        public static final SlotDisplay.i<SlotDisplay.h> TYPE = new SlotDisplay.i<>(SlotDisplay.h.MAP_CODEC, SlotDisplay.h.STREAM_CODEC);

        @Override
        public SlotDisplay.i<SlotDisplay.h> type() {
            return SlotDisplay.h.TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            if (displaycontentsfactory instanceof DisplayContentsFactory.b<T> displaycontentsfactory_b) {
                HolderLookup.a holderlookup_a = (HolderLookup.a) contextmap.getOptional(SlotDisplayContext.REGISTRIES);

                if (holderlookup_a != null) {
                    return holderlookup_a.lookupOrThrow(Registries.ITEM).get(this.tag).map((holderset_named) -> {
                        Stream stream = holderset_named.stream();

                        Objects.requireNonNull(displaycontentsfactory_b);
                        return stream.map(displaycontentsfactory_b::forStack);
                    }).stream().flatMap((stream) -> {
                        return stream;
                    });
                }
            }

            return Stream.empty();
        }
    }

    public static record f(ItemStack stack) implements SlotDisplay {

        public static final MapCodec<SlotDisplay.f> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ItemStack.STRICT_CODEC.fieldOf("item").forGetter(SlotDisplay.f::stack)).apply(instance, SlotDisplay.f::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.f> STREAM_CODEC = StreamCodec.composite(ItemStack.STREAM_CODEC, SlotDisplay.f::stack, SlotDisplay.f::new);
        public static final SlotDisplay.i<SlotDisplay.f> TYPE = new SlotDisplay.i<>(SlotDisplay.f.MAP_CODEC, SlotDisplay.f.STREAM_CODEC);

        @Override
        public SlotDisplay.i<SlotDisplay.f> type() {
            return SlotDisplay.f.TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            if (displaycontentsfactory instanceof DisplayContentsFactory.b<T> displaycontentsfactory_b) {
                return Stream.of(displaycontentsfactory_b.forStack(this.stack));
            } else {
                return Stream.empty();
            }
        }

        public boolean equals(Object object) {
            boolean flag;

            if (this != object) {
                label22:
                {
                    if (object instanceof SlotDisplay.f) {
                        SlotDisplay.f slotdisplay_f = (SlotDisplay.f) object;

                        if (ItemStack.matches(this.stack, slotdisplay_f.stack)) {
                            break label22;
                        }
                    }

                    flag = false;
                    return flag;
                }
            }

            flag = true;
            return flag;
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureflagset) {
            return this.stack.getItem().isEnabled(featureflagset);
        }
    }

    public static record d(Holder<Item> item) implements SlotDisplay {

        public static final MapCodec<SlotDisplay.d> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Item.CODEC.fieldOf("item").forGetter(SlotDisplay.d::item)).apply(instance, SlotDisplay.d::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.d> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.ITEM), SlotDisplay.d::item, SlotDisplay.d::new);
        public static final SlotDisplay.i<SlotDisplay.d> TYPE = new SlotDisplay.i<>(SlotDisplay.d.MAP_CODEC, SlotDisplay.d.STREAM_CODEC);

        public d(Item item) {
            this((Holder) item.builtInRegistryHolder());
        }

        @Override
        public SlotDisplay.i<SlotDisplay.d> type() {
            return SlotDisplay.d.TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            if (displaycontentsfactory instanceof DisplayContentsFactory.b<T> displaycontentsfactory_b) {
                return Stream.of(displaycontentsfactory_b.forStack(this.item));
            } else {
                return Stream.empty();
            }
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureflagset) {
            return ((Item) this.item.value()).isEnabled(featureflagset);
        }
    }

    public static record g(SlotDisplay base, SlotDisplay material, SlotDisplay pattern) implements SlotDisplay {

        public static final MapCodec<SlotDisplay.g> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(SlotDisplay.CODEC.fieldOf("base").forGetter(SlotDisplay.g::base), SlotDisplay.CODEC.fieldOf("material").forGetter(SlotDisplay.g::material), SlotDisplay.CODEC.fieldOf("pattern").forGetter(SlotDisplay.g::pattern)).apply(instance, SlotDisplay.g::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.g> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, SlotDisplay.g::base, SlotDisplay.STREAM_CODEC, SlotDisplay.g::material, SlotDisplay.STREAM_CODEC, SlotDisplay.g::pattern, SlotDisplay.g::new);
        public static final SlotDisplay.i<SlotDisplay.g> TYPE = new SlotDisplay.i<>(SlotDisplay.g.MAP_CODEC, SlotDisplay.g.STREAM_CODEC);

        @Override
        public SlotDisplay.i<SlotDisplay.g> type() {
            return SlotDisplay.g.TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            if (displaycontentsfactory instanceof DisplayContentsFactory.b<T> displaycontentsfactory_b) {
                HolderLookup.a holderlookup_a = (HolderLookup.a) contextmap.getOptional(SlotDisplayContext.REGISTRIES);

                if (holderlookup_a != null) {
                    RandomSource randomsource = RandomSource.create((long) System.identityHashCode(this));
                    List<ItemStack> list = this.base.resolveForStacks(contextmap);

                    if (list.isEmpty()) {
                        return Stream.empty();
                    }

                    List<ItemStack> list1 = this.material.resolveForStacks(contextmap);

                    if (list1.isEmpty()) {
                        return Stream.empty();
                    }

                    List<ItemStack> list2 = this.pattern.resolveForStacks(contextmap);

                    if (list2.isEmpty()) {
                        return Stream.empty();
                    }

                    Stream stream = Stream.generate(() -> {
                        ItemStack itemstack = (ItemStack) SystemUtils.getRandom(list, randomsource);
                        ItemStack itemstack1 = (ItemStack) SystemUtils.getRandom(list1, randomsource);
                        ItemStack itemstack2 = (ItemStack) SystemUtils.getRandom(list2, randomsource);

                        return SmithingTrimRecipe.applyTrim(holderlookup_a, itemstack, itemstack1, itemstack2);
                    }).limit(256L).filter((itemstack) -> {
                        return !itemstack.isEmpty();
                    }).limit(16L);

                    Objects.requireNonNull(displaycontentsfactory_b);
                    return stream.map(displaycontentsfactory_b::forStack);
                }
            }

            return Stream.empty();
        }
    }

    public static class a implements SlotDisplay {

        public static final SlotDisplay.a INSTANCE = new SlotDisplay.a();
        public static final MapCodec<SlotDisplay.a> MAP_CODEC = MapCodec.unit(SlotDisplay.a.INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.a> STREAM_CODEC = StreamCodec.unit(SlotDisplay.a.INSTANCE);
        public static final SlotDisplay.i<SlotDisplay.a> TYPE = new SlotDisplay.i<>(SlotDisplay.a.MAP_CODEC, SlotDisplay.a.STREAM_CODEC);

        private a() {}

        @Override
        public SlotDisplay.i<SlotDisplay.a> type() {
            return SlotDisplay.a.TYPE;
        }

        public String toString() {
            return "<any fuel>";
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            if (displaycontentsfactory instanceof DisplayContentsFactory.b<T> displaycontentsfactory_b) {
                FuelValues fuelvalues = (FuelValues) contextmap.getOptional(SlotDisplayContext.FUEL_VALUES);

                if (fuelvalues != null) {
                    Stream stream = fuelvalues.fuelItems().stream();

                    Objects.requireNonNull(displaycontentsfactory_b);
                    return stream.map(displaycontentsfactory_b::forStack);
                }
            }

            return Stream.empty();
        }
    }

    public static class c implements SlotDisplay {

        public static final SlotDisplay.c INSTANCE = new SlotDisplay.c();
        public static final MapCodec<SlotDisplay.c> MAP_CODEC = MapCodec.unit(SlotDisplay.c.INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.c> STREAM_CODEC = StreamCodec.unit(SlotDisplay.c.INSTANCE);
        public static final SlotDisplay.i<SlotDisplay.c> TYPE = new SlotDisplay.i<>(SlotDisplay.c.MAP_CODEC, SlotDisplay.c.STREAM_CODEC);

        private c() {}

        @Override
        public SlotDisplay.i<SlotDisplay.c> type() {
            return SlotDisplay.c.TYPE;
        }

        public String toString() {
            return "<empty>";
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextmap, DisplayContentsFactory<T> displaycontentsfactory) {
            return Stream.empty();
        }
    }

    public static record i<T extends SlotDisplay>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

    }
}
