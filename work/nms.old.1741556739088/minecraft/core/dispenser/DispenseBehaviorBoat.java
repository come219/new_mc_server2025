package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

public class DispenseBehaviorBoat extends DispenseBehaviorItem {

    private final DispenseBehaviorItem defaultDispenseItemBehavior = new DispenseBehaviorItem();
    private final EntityTypes<? extends AbstractBoat> type;

    public DispenseBehaviorBoat(EntityTypes<? extends AbstractBoat> entitytypes) {
        this.type = entitytypes;
    }

    @Override
    public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
        WorldServer worldserver = sourceblock.level();
        Vec3D vec3d = sourceblock.center();
        double d0 = 0.5625D + (double) this.type.getWidth() / 2.0D;
        double d1 = vec3d.x() + (double) enumdirection.getStepX() * d0;
        double d2 = vec3d.y() + (double) ((float) enumdirection.getStepY() * 1.125F);
        double d3 = vec3d.z() + (double) enumdirection.getStepZ() * d0;
        BlockPosition blockposition = sourceblock.pos().relative(enumdirection);
        double d4;

        if (worldserver.getFluidState(blockposition).is(TagsFluid.WATER)) {
            d4 = 1.0D;
        } else {
            if (!worldserver.getBlockState(blockposition).isAir() || !worldserver.getFluidState(blockposition.below()).is(TagsFluid.WATER)) {
                return this.defaultDispenseItemBehavior.dispense(sourceblock, itemstack);
            }

            d4 = 0.0D;
        }

        // CraftBukkit start
        ItemStack itemstack1 = itemstack.split(1);
        org.bukkit.block.Block block = CraftBlock.at(worldserver, sourceblock.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d1, d2 + d4, d3));
        if (!BlockDispenser.eventFired) {
            worldserver.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            itemstack.grow(1);
            return itemstack;
        }

        if (!event.getItem().equals(craftItem)) {
            itemstack.grow(1);
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != IDispenseBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(sourceblock, eventStack);
                return itemstack;
            }
        }
        // CraftBukkit end
        AbstractBoat abstractboat = (AbstractBoat) this.type.create(worldserver, EntitySpawnReason.DISPENSER);

        if (abstractboat != null) {
            abstractboat.setInitialPos(event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ()); // CraftBukkit
            EntityTypes.createDefaultStackConfig(worldserver, itemstack, (EntityHuman) null).accept(abstractboat);
            abstractboat.setYRot(enumdirection.toYRot());
            if (!worldserver.addFreshEntity(abstractboat)) itemstack.grow(1); // CraftBukkit
            // itemstack.shrink(1); // CraftBukkit - handled during event processing
        }

        return itemstack;
    }

    @Override
    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(1000, sourceblock.pos(), 0);
    }
}
