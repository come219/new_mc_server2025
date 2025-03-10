package net.minecraft.core.dispenser;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.BlockDispenser;

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

public class DispenseBehaviorProjectile extends DispenseBehaviorItem {

    private final ProjectileItem projectileItem;
    private final ProjectileItem.a dispenseConfig;

    public DispenseBehaviorProjectile(Item item) {
        if (item instanceof ProjectileItem projectileitem) {
            this.projectileItem = projectileitem;
            this.dispenseConfig = projectileitem.createDispenseConfig();
        } else {
            String s = String.valueOf(item);

            throw new IllegalArgumentException(s + " not instance of " + ProjectileItem.class.getSimpleName());
        }
    }

    @Override
    public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        WorldServer worldserver = sourceblock.level();
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
        IPosition iposition = this.dispenseConfig.positionFunction().getDispensePosition(sourceblock, enumdirection);

        // CraftBukkit start
        // IProjectile.spawnProjectileUsingShoot(this.projectileItem.asProjectile(worldserver, iposition, itemstack, enumdirection), worldserver, itemstack, (double) enumdirection.getStepX(), (double) enumdirection.getStepY(), (double) enumdirection.getStepZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty()); // CraftBukkit - call when finish the BlockDispenseEvent
        ItemStack itemstack1 = itemstack.split(1);
        org.bukkit.block.Block block = CraftBlock.at(worldserver, sourceblock.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector((double) enumdirection.getStepX(), (double) enumdirection.getStepY(), (double) enumdirection.getStepZ()));
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

        // SPIGOT-7923: Avoid create projectiles with empty item
        if (!itemstack1.isEmpty()) {
            IProjectile iprojectile = IProjectile.spawnProjectileUsingShoot(this.projectileItem.asProjectile(worldserver, iposition, itemstack1, enumdirection), worldserver, itemstack1, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
            iprojectile.projectileSource = new org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource(sourceblock.blockEntity());
        }
        // itemstack.shrink(1); // CraftBukkit - Handled during event processing
        // CraftBukkit end
        return itemstack;
    }

    @Override
    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), sourceblock.pos(), 0);
    }
}
