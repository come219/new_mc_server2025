package net.minecraft.world.item;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemPotion extends Item {

    public ItemPotion(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack itemstack = super.getDefaultInstance();

        itemstack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        return itemstack;
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        EntityHuman entityhuman = itemactioncontext.getPlayer();
        ItemStack itemstack = itemactioncontext.getItemInHand();
        PotionContents potioncontents = (PotionContents) itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (itemactioncontext.getClickedFace() != EnumDirection.DOWN && iblockdata.is(TagsBlock.CONVERTABLE_TO_MUD) && potioncontents.is(Potions.WATER)) {
            world.playSound((EntityHuman) null, blockposition, SoundEffects.GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            entityhuman.setItemInHand(itemactioncontext.getHand(), ItemLiquidUtil.createFilledResult(itemstack, entityhuman, new ItemStack(Items.GLASS_BOTTLE)));
            entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
            if (!world.isClientSide) {
                WorldServer worldserver = (WorldServer) world;

                for (int i = 0; i < 5; ++i) {
                    worldserver.sendParticles(Particles.SPLASH, (double) blockposition.getX() + world.random.nextDouble(), (double) (blockposition.getY() + 1), (double) blockposition.getZ() + world.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
                }
            }

            world.playSound((EntityHuman) null, blockposition, SoundEffects.BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PLACE, blockposition);
            world.setBlockAndUpdate(blockposition, Blocks.MUD.defaultBlockState());
            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        PotionContents potioncontents = (PotionContents) itemstack.get(DataComponents.POTION_CONTENTS);

        return potioncontents != null ? potioncontents.getName(this.descriptionId + ".effect.") : super.getName(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        PotionContents potioncontents = (PotionContents) itemstack.get(DataComponents.POTION_CONTENTS);

        if (potioncontents != null) {
            Objects.requireNonNull(list);
            potioncontents.addPotionTooltip(list::add, 1.0F, item_b.tickRate());
        }
    }
}
