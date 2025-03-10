package net.minecraft.advancements.critereon;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;

public class CriterionValidator {

    private final ProblemReporter reporter;
    private final HolderGetter.a lootData;

    public CriterionValidator(ProblemReporter problemreporter, HolderGetter.a holdergetter_a) {
        this.reporter = problemreporter;
        this.lootData = holdergetter_a;
    }

    public void validateEntity(Optional<ContextAwarePredicate> optional, String s) {
        optional.ifPresent((contextawarepredicate) -> {
            this.validateEntity(contextawarepredicate, s);
        });
    }

    public void validateEntities(List<ContextAwarePredicate> list, String s) {
        this.validate(list, LootContextParameterSets.ADVANCEMENT_ENTITY, s);
    }

    public void validateEntity(ContextAwarePredicate contextawarepredicate, String s) {
        this.validate(contextawarepredicate, LootContextParameterSets.ADVANCEMENT_ENTITY, s);
    }

    public void validate(ContextAwarePredicate contextawarepredicate, ContextKeySet contextkeyset, String s) {
        contextawarepredicate.validate(new LootCollector(this.reporter.forChild(s), contextkeyset, this.lootData));
    }

    public void validate(List<ContextAwarePredicate> list, ContextKeySet contextkeyset, String s) {
        for (int i = 0; i < list.size(); ++i) {
            ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) list.get(i);

            contextawarepredicate.validate(new LootCollector(this.reporter.forChild(s + "[" + i + "]"), contextkeyset, this.lootData));
        }

    }
}
