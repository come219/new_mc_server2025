package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;

public class ItemBannerPattern extends Item {

    private final TagKey<EnumBannerPatternType> bannerPattern;

    public ItemBannerPattern(TagKey<EnumBannerPatternType> tagkey, Item.Info item_info) {
        super(item_info);
        this.bannerPattern = tagkey;
    }

    public TagKey<EnumBannerPatternType> getBannerPattern() {
        return this.bannerPattern;
    }
}
