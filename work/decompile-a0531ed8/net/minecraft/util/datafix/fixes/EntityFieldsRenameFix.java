package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EntityFieldsRenameFix extends DataConverterNamedEntity {

    private final Map<String, String> renames;

    public EntityFieldsRenameFix(Schema schema, String s, String s1, Map<String, String> map) {
        super(schema, false, s, DataConverterTypes.ENTITY, s1);
        this.renames = map;
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        Entry entry;

        for (Iterator iterator = this.renames.entrySet().iterator(); iterator.hasNext(); dynamic = dynamic.renameField((String) entry.getKey(), (String) entry.getValue())) {
            entry = (Entry) iterator.next();
        }

        return dynamic;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}
