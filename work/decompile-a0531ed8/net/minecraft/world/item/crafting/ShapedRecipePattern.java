package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.SystemUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public final class ShapedRecipePattern {

    private static final int MAX_SIZE = 3;
    public static final char EMPTY_SLOT = ' ';
    public static final MapCodec<ShapedRecipePattern> MAP_CODEC = ShapedRecipePattern.a.MAP_CODEC.flatXmap(ShapedRecipePattern::unpack, (shapedrecipepattern) -> {
        return (DataResult) shapedrecipepattern.data.map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
                return "Cannot encode unpacked recipe";
            });
        });
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipePattern> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, (shapedrecipepattern) -> {
        return shapedrecipepattern.width;
    }, ByteBufCodecs.VAR_INT, (shapedrecipepattern) -> {
        return shapedrecipepattern.height;
    }, RecipeItemStack.OPTIONAL_CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), (shapedrecipepattern) -> {
        return shapedrecipepattern.ingredients;
    }, ShapedRecipePattern::createFromNetwork);
    private final int width;
    private final int height;
    private final List<Optional<RecipeItemStack>> ingredients;
    private final Optional<ShapedRecipePattern.a> data;
    private final int ingredientCount;
    private final boolean symmetrical;

    public ShapedRecipePattern(int i, int j, List<Optional<RecipeItemStack>> list, Optional<ShapedRecipePattern.a> optional) {
        this.width = i;
        this.height = j;
        this.ingredients = list;
        this.data = optional;
        this.ingredientCount = (int) list.stream().flatMap(Optional::stream).count();
        this.symmetrical = SystemUtils.isSymmetrical(i, j, list);
    }

    private static ShapedRecipePattern createFromNetwork(Integer integer, Integer integer1, List<Optional<RecipeItemStack>> list) {
        return new ShapedRecipePattern(integer, integer1, list, Optional.empty());
    }

    public static ShapedRecipePattern of(Map<Character, RecipeItemStack> map, String... astring) {
        return of(map, List.of(astring));
    }

    public static ShapedRecipePattern of(Map<Character, RecipeItemStack> map, List<String> list) {
        ShapedRecipePattern.a shapedrecipepattern_a = new ShapedRecipePattern.a(map, list);

        return (ShapedRecipePattern) unpack(shapedrecipepattern_a).getOrThrow();
    }

    private static DataResult<ShapedRecipePattern> unpack(ShapedRecipePattern.a shapedrecipepattern_a) {
        String[] astring = shrink(shapedrecipepattern_a.pattern);
        int i = astring[0].length();
        int j = astring.length;
        List<Optional<RecipeItemStack>> list = new ArrayList(i * j);
        CharArraySet chararrayset = new CharArraySet(shapedrecipepattern_a.key.keySet());
        String[] astring1 = astring;
        int k = astring.length;

        for (int l = 0; l < k; ++l) {
            String s = astring1[l];

            for (int i1 = 0; i1 < s.length(); ++i1) {
                char c0 = s.charAt(i1);
                Optional optional;

                if (c0 == ' ') {
                    optional = Optional.empty();
                } else {
                    RecipeItemStack recipeitemstack = (RecipeItemStack) shapedrecipepattern_a.key.get(c0);

                    if (recipeitemstack == null) {
                        return DataResult.error(() -> {
                            return "Pattern references symbol '" + c0 + "' but it's not defined in the key";
                        });
                    }

                    optional = Optional.of(recipeitemstack);
                }

                chararrayset.remove(c0);
                list.add(optional);
            }
        }

        if (!chararrayset.isEmpty()) {
            return DataResult.error(() -> {
                return "Key defines symbols that aren't used in pattern: " + String.valueOf(chararrayset);
            });
        } else {
            return DataResult.success(new ShapedRecipePattern(i, j, list, Optional.of(shapedrecipepattern_a)));
        }
    }

    @VisibleForTesting
    static String[] shrink(List<String> list) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for (int i1 = 0; i1 < list.size(); ++i1) {
            String s = (String) list.get(i1);

            i = Math.min(i, firstNonEmpty(s));
            int j1 = lastNonEmpty(s);

            j = Math.max(j, j1);
            if (j1 < 0) {
                if (k == i1) {
                    ++k;
                }

                ++l;
            } else {
                l = 0;
            }
        }

        if (list.size() == l) {
            return new String[0];
        } else {
            String[] astring = new String[list.size() - l - k];

            for (int k1 = 0; k1 < astring.length; ++k1) {
                astring[k1] = ((String) list.get(k1 + k)).substring(i, j + 1);
            }

            return astring;
        }
    }

    private static int firstNonEmpty(String s) {
        int i;

        for (i = 0; i < s.length() && s.charAt(i) == ' '; ++i) {
            ;
        }

        return i;
    }

    private static int lastNonEmpty(String s) {
        int i;

        for (i = s.length() - 1; i >= 0 && s.charAt(i) == ' '; --i) {
            ;
        }

        return i;
    }

    public boolean matches(CraftingInput craftinginput) {
        if (craftinginput.ingredientCount() != this.ingredientCount) {
            return false;
        } else {
            if (craftinginput.width() == this.width && craftinginput.height() == this.height) {
                if (!this.symmetrical && this.matches(craftinginput, true)) {
                    return true;
                }

                if (this.matches(craftinginput, false)) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean matches(CraftingInput craftinginput, boolean flag) {
        for (int i = 0; i < this.height; ++i) {
            for (int j = 0; j < this.width; ++j) {
                Optional optional;

                if (flag) {
                    optional = (Optional) this.ingredients.get(this.width - j - 1 + i * this.width);
                } else {
                    optional = (Optional) this.ingredients.get(j + i * this.width);
                }

                ItemStack itemstack = craftinginput.getItem(j, i);

                if (!RecipeItemStack.testOptionalIngredient(optional, itemstack)) {
                    return false;
                }
            }
        }

        return true;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public List<Optional<RecipeItemStack>> ingredients() {
        return this.ingredients;
    }

    public static record a(Map<Character, RecipeItemStack> key, List<String> pattern) {

        private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap((list) -> {
            if (list.size() > 3) {
                return DataResult.error(() -> {
                    return "Invalid pattern: too many rows, 3 is maximum";
                });
            } else if (list.isEmpty()) {
                return DataResult.error(() -> {
                    return "Invalid pattern: empty pattern not allowed";
                });
            } else {
                int i = ((String) list.getFirst()).length();
                Iterator iterator = list.iterator();

                String s;

                do {
                    if (!iterator.hasNext()) {
                        return DataResult.success(list);
                    }

                    s = (String) iterator.next();
                    if (s.length() > 3) {
                        return DataResult.error(() -> {
                            return "Invalid pattern: too many columns, 3 is maximum";
                        });
                    }
                } while (i == s.length());

                return DataResult.error(() -> {
                    return "Invalid pattern: each row must be the same width";
                });
            }
        }, Function.identity());
        private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap((s) -> {
            return s.length() != 1 ? DataResult.error(() -> {
                return "Invalid key entry: '" + s + "' is an invalid symbol (must be 1 character only).";
            }) : (" ".equals(s) ? DataResult.error(() -> {
                return "Invalid key entry: ' ' is a reserved symbol.";
            }) : DataResult.success(s.charAt(0)));
        }, String::valueOf);
        public static final MapCodec<ShapedRecipePattern.a> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.strictUnboundedMap(ShapedRecipePattern.a.SYMBOL_CODEC, RecipeItemStack.CODEC).fieldOf("key").forGetter((shapedrecipepattern_a) -> {
                return shapedrecipepattern_a.key;
            }), ShapedRecipePattern.a.PATTERN_CODEC.fieldOf("pattern").forGetter((shapedrecipepattern_a) -> {
                return shapedrecipepattern_a.pattern;
            })).apply(instance, ShapedRecipePattern.a::new);
        });
    }
}
