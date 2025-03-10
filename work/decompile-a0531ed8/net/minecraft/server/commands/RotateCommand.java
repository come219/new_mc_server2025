package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.coordinates.ArgumentRotation;
import net.minecraft.commands.arguments.coordinates.ArgumentVec3;
import net.minecraft.commands.arguments.coordinates.IVectorPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2F;

public class RotateCommand {

    public RotateCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("rotate").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("target", ArgumentEntity.entity()).then(net.minecraft.commands.CommandDispatcher.argument("rotation", ArgumentRotation.rotation()).executes((commandcontext) -> {
            return rotate((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "target"), ArgumentRotation.getRotation(commandcontext, "rotation"));
        }))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("facing").then(net.minecraft.commands.CommandDispatcher.literal("entity").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("facingEntity", ArgumentEntity.entity()).executes((commandcontext) -> {
            return rotate((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "target"), (LookAt) (new LookAt.a(ArgumentEntity.getEntity(commandcontext, "facingEntity"), ArgumentAnchor.Anchor.FEET)));
        })).then(net.minecraft.commands.CommandDispatcher.argument("facingAnchor", ArgumentAnchor.anchor()).executes((commandcontext) -> {
            return rotate((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "target"), (LookAt) (new LookAt.a(ArgumentEntity.getEntity(commandcontext, "facingEntity"), ArgumentAnchor.getAnchor(commandcontext, "facingAnchor"))));
        }))))).then(net.minecraft.commands.CommandDispatcher.argument("facingLocation", ArgumentVec3.vec3()).executes((commandcontext) -> {
            return rotate((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "target"), (LookAt) (new LookAt.b(ArgumentVec3.getVec3(commandcontext, "facingLocation"))));
        })))));
    }

    private static int rotate(CommandListenerWrapper commandlistenerwrapper, Entity entity, IVectorPosition ivectorposition) {
        Vec2F vec2f = ivectorposition.getRotation(commandlistenerwrapper);

        entity.forceSetRotation(vec2f.y, vec2f.x);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.rotate.success", entity.getDisplayName());
        }, true);
        return 1;
    }

    private static int rotate(CommandListenerWrapper commandlistenerwrapper, Entity entity, LookAt lookat) {
        lookat.perform(commandlistenerwrapper, entity);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.rotate.success", entity.getDisplayName());
        }, true);
        return 1;
    }
}
