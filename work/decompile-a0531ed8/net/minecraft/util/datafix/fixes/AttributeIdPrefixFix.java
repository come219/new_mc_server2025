package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class AttributeIdPrefixFix extends AttributesRenameFix {

    private static final List<String> PREFIXES = List.of("generic.", "horse.", "player.", "zombie.");

    public AttributeIdPrefixFix(Schema schema) {
        super(schema, "AttributeIdPrefixFix", AttributeIdPrefixFix::replaceId);
    }

    private static String replaceId(String s) {
        String s1 = DataConverterSchemaNamed.ensureNamespaced(s);
        Iterator iterator = AttributeIdPrefixFix.PREFIXES.iterator();

        String s2;

        do {
            if (!iterator.hasNext()) {
                return s;
            }

            String s3 = (String) iterator.next();

            s2 = DataConverterSchemaNamed.ensureNamespaced(s3);
        } while (!s1.startsWith(s2));

        String s4 = s1.substring(s2.length());

        return "minecraft:" + s4;
    }
}
