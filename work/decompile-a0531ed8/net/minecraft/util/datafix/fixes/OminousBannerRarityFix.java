package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class OminousBannerRarityFix extends DataFix {

    public OminousBannerRarityFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        Type<?> type1 = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("components");
        OpticFinder<?> opticfinder2 = type1.findField("components");

        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("Ominous Banner block entity common rarity to uncommon rarity fix", type, (typed) -> {
            Object object = ((Pair) typed.get(taggedchoicetype.finder())).getFirst();

            return object.equals("minecraft:banner") ? this.fix(typed, opticfinder1) : typed;
        }), this.fixTypeEverywhereTyped("Ominous Banner item stack common rarity to uncommon rarity fix", type1, (typed) -> {
            String s = (String) typed.getOptional(opticfinder).map(Pair::getSecond).orElse("");

            return s.equals("minecraft:white_banner") ? this.fix(typed, opticfinder2) : typed;
        }));
    }

    private Typed<?> fix(Typed<?> typed, OpticFinder<?> opticfinder) {
        return typed.updateTyped(opticfinder, (typed1) -> {
            return typed1.update(DSL.remainderFinder(), (dynamic) -> {
                boolean flag = dynamic.get("minecraft:item_name").asString().result().flatMap(ComponentDataFixUtils::extractTranslationString).filter((s) -> {
                    return s.equals("block.minecraft.ominous_banner");
                }).isPresent();

                return flag ? dynamic.set("minecraft:rarity", dynamic.createString("uncommon")).set("minecraft:item_name", ComponentDataFixUtils.createTranslatableComponent(dynamic.getOps(), "block.minecraft.ominous_banner")) : dynamic;
            });
        });
    }
}
