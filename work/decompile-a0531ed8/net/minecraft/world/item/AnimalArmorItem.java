package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.equipment.ArmorMaterial;

public class AnimalArmorItem extends Item {

    private final AnimalArmorItem.a bodyType;

    public AnimalArmorItem(ArmorMaterial armormaterial, AnimalArmorItem.a animalarmoritem_a, Item.Info item_info) {
        super(armormaterial.animalProperties(item_info, animalarmoritem_a.allowedEntities));
        this.bodyType = animalarmoritem_a;
    }

    public AnimalArmorItem(ArmorMaterial armormaterial, AnimalArmorItem.a animalarmoritem_a, Holder<SoundEffect> holder, boolean flag, Item.Info item_info) {
        super(armormaterial.animalProperties(item_info, holder, flag, animalarmoritem_a.allowedEntities));
        this.bodyType = animalarmoritem_a;
    }

    @Override
    public SoundEffect getBreakingSound() {
        return this.bodyType.breakingSound;
    }

    public static enum a {

        EQUESTRIAN(SoundEffects.ITEM_BREAK, new EntityTypes[]{EntityTypes.HORSE}), CANINE(SoundEffects.WOLF_ARMOR_BREAK, new EntityTypes[]{EntityTypes.WOLF});

        final SoundEffect breakingSound;
        final HolderSet<EntityTypes<?>> allowedEntities;

        private a(final SoundEffect soundeffect, final EntityTypes... aentitytypes) {
            this.breakingSound = soundeffect;
            this.allowedEntities = HolderSet.direct(EntityTypes::builtInRegistryHolder, (Object[]) aentitytypes);
        }
    }
}
