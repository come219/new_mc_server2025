package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockMinecartTrackAbstract;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class ItemMinecart extends Item {

    private final EntityTypes<? extends EntityMinecartAbstract> type;

    public ItemMinecart(EntityTypes<? extends EntityMinecartAbstract> entitytypes, Item.Info item_info) {
        super(item_info);
        this.type = entitytypes;
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (!iblockdata.is(TagsBlock.RAILS)) {
            return EnumInteractionResult.FAIL;
        } else {
            ItemStack itemstack = itemactioncontext.getItemInHand();
            BlockPropertyTrackPosition blockpropertytrackposition = iblockdata.getBlock() instanceof BlockMinecartTrackAbstract ? (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty()) : BlockPropertyTrackPosition.NORTH_SOUTH;
            double d0 = 0.0D;

            if (blockpropertytrackposition.isSlope()) {
                d0 = 0.5D;
            }

            Vec3D vec3d = new Vec3D((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.0625D + d0, (double) blockposition.getZ() + 0.5D);
            EntityMinecartAbstract entityminecartabstract = EntityMinecartAbstract.createMinecart(world, vec3d.x, vec3d.y, vec3d.z, this.type, EntitySpawnReason.DISPENSER, itemstack, itemactioncontext.getPlayer());

            if (entityminecartabstract == null) {
                return EnumInteractionResult.FAIL;
            } else {
                if (EntityMinecartAbstract.useExperimentalMovement(world)) {
                    List<Entity> list = world.getEntities((Entity) null, entityminecartabstract.getBoundingBox());
                    Iterator iterator = list.iterator();

                    while (iterator.hasNext()) {
                        Entity entity = (Entity) iterator.next();

                        if (entity instanceof EntityMinecartAbstract) {
                            return EnumInteractionResult.FAIL;
                        }
                    }
                }

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    worldserver.addFreshEntity(entityminecartabstract);
                    worldserver.gameEvent((Holder) GameEvent.ENTITY_PLACE, blockposition, GameEvent.a.of(itemactioncontext.getPlayer(), worldserver.getBlockState(blockposition.below())));
                }

                itemstack.shrink(1);
                return EnumInteractionResult.SUCCESS;
            }
        }
    }
}
