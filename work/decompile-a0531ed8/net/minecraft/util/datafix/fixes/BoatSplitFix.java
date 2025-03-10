package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class BoatSplitFix extends DataFix {

    public BoatSplitFix(Schema schema) {
        super(schema, true);
    }

    private static boolean isNormalBoat(String s) {
        return s.equals("minecraft:boat");
    }

    private static boolean isChestBoat(String s) {
        return s.equals("minecraft:chest_boat");
    }

    private static boolean isAnyBoat(String s) {
        return isNormalBoat(s) || isChestBoat(s);
    }

    private static String mapVariantToNormalBoat(String s) {
        String s1;

        switch (s) {
            case "spruce":
                s1 = "minecraft:spruce_boat";
                break;
            case "birch":
                s1 = "minecraft:birch_boat";
                break;
            case "jungle":
                s1 = "minecraft:jungle_boat";
                break;
            case "acacia":
                s1 = "minecraft:acacia_boat";
                break;
            case "cherry":
                s1 = "minecraft:cherry_boat";
                break;
            case "dark_oak":
                s1 = "minecraft:dark_oak_boat";
                break;
            case "mangrove":
                s1 = "minecraft:mangrove_boat";
                break;
            case "bamboo":
                s1 = "minecraft:bamboo_raft";
                break;
            default:
                s1 = "minecraft:oak_boat";
        }

        return s1;
    }

    private static String mapVariantToChestBoat(String s) {
        String s1;

        switch (s) {
            case "spruce":
                s1 = "minecraft:spruce_chest_boat";
                break;
            case "birch":
                s1 = "minecraft:birch_chest_boat";
                break;
            case "jungle":
                s1 = "minecraft:jungle_chest_boat";
                break;
            case "acacia":
                s1 = "minecraft:acacia_chest_boat";
                break;
            case "cherry":
                s1 = "minecraft:cherry_chest_boat";
                break;
            case "dark_oak":
                s1 = "minecraft:dark_oak_chest_boat";
                break;
            case "mangrove":
                s1 = "minecraft:mangrove_chest_boat";
                break;
            case "bamboo":
                s1 = "minecraft:bamboo_chest_raft";
                break;
            default:
                s1 = "minecraft:oak_chest_boat";
        }

        return s1;
    }

    public TypeRewriteRule makeRule() {
        OpticFinder<String> opticfinder = DSL.fieldFinder("id", DataConverterSchemaNamed.namespacedString());
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ENTITY);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.ENTITY);

        return this.fixTypeEverywhereTyped("BoatSplitFix", type, type1, (typed) -> {
            Optional<String> optional = typed.getOptional(opticfinder);

            if (optional.isPresent() && isAnyBoat((String) optional.get())) {
                Dynamic<?> dynamic = (Dynamic) typed.getOrCreate(DSL.remainderFinder());
                Optional<String> optional1 = dynamic.get("Type").asString().result();
                String s;

                if (isChestBoat((String) optional.get())) {
                    s = (String) optional1.map(BoatSplitFix::mapVariantToChestBoat).orElse("minecraft:oak_chest_boat");
                } else {
                    s = (String) optional1.map(BoatSplitFix::mapVariantToNormalBoat).orElse("minecraft:oak_boat");
                }

                return ExtraDataFixUtils.cast(type1, typed).update(DSL.remainderFinder(), (dynamic1) -> {
                    return dynamic1.remove("Type");
                }).set(opticfinder, s);
            } else {
                return ExtraDataFixUtils.cast(type1, typed);
            }
        });
    }
}
