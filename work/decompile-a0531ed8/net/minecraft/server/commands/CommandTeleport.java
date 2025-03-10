package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.coordinates.ArgumentRotation;
import net.minecraft.commands.arguments.coordinates.ArgumentVec3;
import net.minecraft.commands.arguments.coordinates.IVectorPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;

public class CommandTeleport {

    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.teleport.invalidPosition"));

    public CommandTeleport() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        LiteralCommandNode<CommandListenerWrapper> literalcommandnode = commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("teleport").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.argument("location", ArgumentVec3.vec3()).executes((commandcontext) -> {
            return teleportToPos((CommandListenerWrapper) commandcontext.getSource(), Collections.singleton(((CommandListenerWrapper) commandcontext.getSource()).getEntityOrException()), ((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentVec3.getCoordinates(commandcontext, "location"), (IVectorPosition) null, (LookAt) null);
        }))).then(net.minecraft.commands.CommandDispatcher.argument("destination", ArgumentEntity.entity()).executes((commandcontext) -> {
            return teleportToEntity((CommandListenerWrapper) commandcontext.getSource(), Collections.singleton(((CommandListenerWrapper) commandcontext.getSource()).getEntityOrException()), ArgumentEntity.getEntity(commandcontext, "destination"));
        }))).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("location", ArgumentVec3.vec3()).executes((commandcontext) -> {
            return teleportToPos((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentVec3.getCoordinates(commandcontext, "location"), (IVectorPosition) null, (LookAt) null);
        })).then(net.minecraft.commands.CommandDispatcher.argument("rotation", ArgumentRotation.rotation()).executes((commandcontext) -> {
            return teleportToPos((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentVec3.getCoordinates(commandcontext, "location"), ArgumentRotation.getRotation(commandcontext, "rotation"), (LookAt) null);
        }))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("facing").then(net.minecraft.commands.CommandDispatcher.literal("entity").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("facingEntity", ArgumentEntity.entity()).executes((commandcontext) -> {
            return teleportToPos((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentVec3.getCoordinates(commandcontext, "location"), (IVectorPosition) null, new LookAt.a(ArgumentEntity.getEntity(commandcontext, "facingEntity"), ArgumentAnchor.Anchor.FEET));
        })).then(net.minecraft.commands.CommandDispatcher.argument("facingAnchor", ArgumentAnchor.anchor()).executes((commandcontext) -> {
            return teleportToPos((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentVec3.getCoordinates(commandcontext, "location"), (IVectorPosition) null, new LookAt.a(ArgumentEntity.getEntity(commandcontext, "facingEntity"), ArgumentAnchor.getAnchor(commandcontext, "facingAnchor")));
        }))))).then(net.minecraft.commands.CommandDispatcher.argument("facingLocation", ArgumentVec3.vec3()).executes((commandcontext) -> {
            return teleportToPos((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentVec3.getCoordinates(commandcontext, "location"), (IVectorPosition) null, new LookAt.b(ArgumentVec3.getVec3(commandcontext, "facingLocation")));
        }))))).then(net.minecraft.commands.CommandDispatcher.argument("destination", ArgumentEntity.entity()).executes((commandcontext) -> {
            return teleportToEntity((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentEntity.getEntity(commandcontext, "destination"));
        }))));

        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("tp").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).redirect(literalcommandnode));
    }

    private static int teleportToEntity(CommandListenerWrapper commandlistenerwrapper, Collection<? extends Entity> collection, Entity entity) throws CommandSyntaxException {
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity1 = (Entity) iterator.next();

            performTeleport(commandlistenerwrapper, entity1, (WorldServer) entity.level(), entity.getX(), entity.getY(), entity.getZ(), EnumSet.noneOf(Relative.class), entity.getYRot(), entity.getXRot(), (LookAt) null);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.teleport.success.entity.single", ((Entity) collection.iterator().next()).getDisplayName(), entity.getDisplayName());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.teleport.success.entity.multiple", collection.size(), entity.getDisplayName());
            }, true);
        }

        return collection.size();
    }

    private static int teleportToPos(CommandListenerWrapper commandlistenerwrapper, Collection<? extends Entity> collection, WorldServer worldserver, IVectorPosition ivectorposition, @Nullable IVectorPosition ivectorposition1, @Nullable LookAt lookat) throws CommandSyntaxException {
        Vec3D vec3d = ivectorposition.getPosition(commandlistenerwrapper);
        Vec2F vec2f = ivectorposition1 == null ? null : ivectorposition1.getRotation(commandlistenerwrapper);
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            Set<Relative> set = getRelatives(ivectorposition, ivectorposition1, entity.level().dimension() == worldserver.dimension());

            if (vec2f == null) {
                performTeleport(commandlistenerwrapper, entity, worldserver, vec3d.x, vec3d.y, vec3d.z, set, entity.getYRot(), entity.getXRot(), lookat);
            } else {
                performTeleport(commandlistenerwrapper, entity, worldserver, vec3d.x, vec3d.y, vec3d.z, set, vec2f.y, vec2f.x, lookat);
            }
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.teleport.success.location.single", ((Entity) collection.iterator().next()).getDisplayName(), formatDouble(vec3d.x), formatDouble(vec3d.y), formatDouble(vec3d.z));
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.teleport.success.location.multiple", collection.size(), formatDouble(vec3d.x), formatDouble(vec3d.y), formatDouble(vec3d.z));
            }, true);
        }

        return collection.size();
    }

    private static Set<Relative> getRelatives(IVectorPosition ivectorposition, @Nullable IVectorPosition ivectorposition1, boolean flag) {
        Set<Relative> set = EnumSet.noneOf(Relative.class);

        if (ivectorposition.isXRelative()) {
            set.add(Relative.DELTA_X);
            if (flag) {
                set.add(Relative.X);
            }
        }

        if (ivectorposition.isYRelative()) {
            set.add(Relative.DELTA_Y);
            if (flag) {
                set.add(Relative.Y);
            }
        }

        if (ivectorposition.isZRelative()) {
            set.add(Relative.DELTA_Z);
            if (flag) {
                set.add(Relative.Z);
            }
        }

        if (ivectorposition1 == null || ivectorposition1.isXRelative()) {
            set.add(Relative.X_ROT);
        }

        if (ivectorposition1 == null || ivectorposition1.isYRelative()) {
            set.add(Relative.Y_ROT);
        }

        return set;
    }

    private static String formatDouble(double d0) {
        return String.format(Locale.ROOT, "%f", d0);
    }

    private static void performTeleport(CommandListenerWrapper commandlistenerwrapper, Entity entity, WorldServer worldserver, double d0, double d1, double d2, Set<Relative> set, float f, float f1, @Nullable LookAt lookat) throws CommandSyntaxException {
        BlockPosition blockposition = BlockPosition.containing(d0, d1, d2);

        if (!World.isInSpawnableBounds(blockposition)) {
            throw CommandTeleport.INVALID_POSITION.create();
        } else {
            double d3 = set.contains(Relative.X) ? d0 - entity.getX() : d0;
            double d4 = set.contains(Relative.Y) ? d1 - entity.getY() : d1;
            double d5 = set.contains(Relative.Z) ? d2 - entity.getZ() : d2;
            float f2 = set.contains(Relative.Y_ROT) ? f - entity.getYRot() : f;
            float f3 = set.contains(Relative.X_ROT) ? f1 - entity.getXRot() : f1;
            float f4 = MathHelper.wrapDegrees(f2);
            float f5 = MathHelper.wrapDegrees(f3);

            if (entity.teleportTo(worldserver, d3, d4, d5, set, f4, f5, true)) {
                if (lookat != null) {
                    lookat.perform(commandlistenerwrapper, entity);
                }

                label46:
                {
                    if (entity instanceof EntityLiving) {
                        EntityLiving entityliving = (EntityLiving) entity;

                        if (entityliving.isFallFlying()) {
                            break label46;
                        }
                    }

                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
                    entity.setOnGround(true);
                }

                if (entity instanceof EntityCreature) {
                    EntityCreature entitycreature = (EntityCreature) entity;

                    entitycreature.getNavigation().stop();
                }

            }
        }
    }
}
