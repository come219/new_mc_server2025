package net.minecraft.world.item;

import java.util.List;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.block.Block;

public class ItemAir extends Item {

    private final Block block;

    public ItemAir(Block block, Item.Info item_info) {
        super(item_info);
        this.block = block;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        this.block.appendHoverText(itemstack, item_b, list, tooltipflag);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        return this.getName();
    }
}
