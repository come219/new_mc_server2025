package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.CriterionConditionEntity;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;

public record LootItemConditionEntityProperty(Optional<CriterionConditionEntity> predicate, LootTableInfo.EntityTarget entityTarget) implements LootItemCondition {

    public static final MapCodec<LootItemConditionEntityProperty> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(CriterionConditionEntity.CODEC.optionalFieldOf("predicate").forGetter(LootItemConditionEntityProperty::predicate), LootTableInfo.EntityTarget.CODEC.fieldOf("entity").forGetter(LootItemConditionEntityProperty::entityTarget)).apply(instance, LootItemConditionEntityProperty::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_PROPERTIES;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParameters.ORIGIN, this.entityTarget.getParam());
    }

    public boolean test(LootTableInfo loottableinfo) {
        Entity entity = (Entity) loottableinfo.getOptionalParameter(this.entityTarget.getParam());
        Vec3D vec3d = (Vec3D) loottableinfo.getOptionalParameter(LootContextParameters.ORIGIN);

        return this.predicate.isEmpty() || ((CriterionConditionEntity) this.predicate.get()).matches(loottableinfo.getLevel(), vec3d, entity);
    }

    public static LootItemCondition.a entityPresent(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return hasProperties(loottableinfo_entitytarget, CriterionConditionEntity.a.entity());
    }

    public static LootItemCondition.a hasProperties(LootTableInfo.EntityTarget loottableinfo_entitytarget, CriterionConditionEntity.a criterionconditionentity_a) {
        return () -> {
            return new LootItemConditionEntityProperty(Optional.of(criterionconditionentity_a.build()), loottableinfo_entitytarget);
        };
    }

    public static LootItemCondition.a hasProperties(LootTableInfo.EntityTarget loottableinfo_entitytarget, CriterionConditionEntity criterionconditionentity) {
        return () -> {
            return new LootItemConditionEntityProperty(Optional.of(criterionconditionentity), loottableinfo_entitytarget);
        };
    }
}
