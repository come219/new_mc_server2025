package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class KilledByArrowTrigger extends CriterionTriggerAbstract<KilledByArrowTrigger.a> {

    public KilledByArrowTrigger() {}

    @Override
    public Codec<KilledByArrowTrigger.a> codec() {
        return KilledByArrowTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Collection<Entity> collection, @Nullable ItemStack itemstack) {
        List<LootTableInfo> list = Lists.newArrayList();
        Set<EntityTypes<?>> set = Sets.newHashSet();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            set.add(entity.getType());
            list.add(CriterionConditionEntity.createContext(entityplayer, entity));
        }

        this.trigger(entityplayer, (killedbyarrowtrigger_a) -> {
            return killedbyarrowtrigger_a.matches(list, set.size(), itemstack);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, CriterionConditionValue.IntegerRange uniqueEntityTypes, Optional<CriterionConditionItem> firedFromWeapon) implements CriterionTriggerAbstract.a {

        public static final Codec<KilledByArrowTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KilledByArrowTrigger.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(KilledByArrowTrigger.a::victims), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("unique_entity_types", CriterionConditionValue.IntegerRange.ANY).forGetter(KilledByArrowTrigger.a::uniqueEntityTypes), CriterionConditionItem.CODEC.optionalFieldOf("fired_from_weapon").forGetter(KilledByArrowTrigger.a::firedFromWeapon)).apply(instance, KilledByArrowTrigger.a::new);
        });

        public static Criterion<KilledByArrowTrigger.a> crossbowKilled(HolderGetter<Item> holdergetter, CriterionConditionEntity.a... acriterionconditionentity_a) {
            return CriterionTriggers.KILLED_BY_ARROW.createCriterion(new KilledByArrowTrigger.a(Optional.empty(), CriterionConditionEntity.wrap(acriterionconditionentity_a), CriterionConditionValue.IntegerRange.ANY, Optional.of(CriterionConditionItem.a.item().of(holdergetter, Items.CROSSBOW).build())));
        }

        public static Criterion<KilledByArrowTrigger.a> crossbowKilled(HolderGetter<Item> holdergetter, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.KILLED_BY_ARROW.createCriterion(new KilledByArrowTrigger.a(Optional.empty(), List.of(), criterionconditionvalue_integerrange, Optional.of(CriterionConditionItem.a.item().of(holdergetter, Items.CROSSBOW).build())));
        }

        public boolean matches(Collection<LootTableInfo> collection, int i, @Nullable ItemStack itemstack) {
            if (this.firedFromWeapon.isPresent() && (itemstack == null || !((CriterionConditionItem) this.firedFromWeapon.get()).test(itemstack))) {
                return false;
            } else {
                if (!this.victims.isEmpty()) {
                    List<LootTableInfo> list = Lists.newArrayList(collection);
                    Iterator iterator = this.victims.iterator();

                    while (iterator.hasNext()) {
                        ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) iterator.next();
                        boolean flag = false;
                        Iterator<LootTableInfo> iterator1 = list.iterator();

                        while (iterator1.hasNext()) {
                            LootTableInfo loottableinfo = (LootTableInfo) iterator1.next();

                            if (contextawarepredicate.matches(loottableinfo)) {
                                iterator1.remove();
                                flag = true;
                                break;
                            }
                        }

                        if (!flag) {
                            return false;
                        }
                    }
                }

                return this.uniqueEntityTypes.matches(i);
            }
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntities(this.victims, ".victims");
        }
    }
}
