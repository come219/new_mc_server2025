package net.minecraft.world.item;

import net.minecraft.tags.TagsBlock;

public class ItemPickaxe extends ItemTool {

    public ItemPickaxe(ToolMaterial toolmaterial, float f, float f1, Item.Info item_info) {
        super(toolmaterial, TagsBlock.MINEABLE_WITH_PICKAXE, f, f1, item_info);
    }
}
