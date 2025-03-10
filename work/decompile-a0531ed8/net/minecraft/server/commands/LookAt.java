package net.minecraft.server.commands;

import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

@FunctionalInterface
public interface LookAt {

    void perform(CommandListenerWrapper commandlistenerwrapper, Entity entity);

    public static record b(Vec3D position) implements LookAt {

        @Override
        public void perform(CommandListenerWrapper commandlistenerwrapper, Entity entity) {
            entity.lookAt(commandlistenerwrapper.getAnchor(), this.position);
        }
    }

    public static record a(Entity entity, ArgumentAnchor.Anchor anchor) implements LookAt {

        @Override
        public void perform(CommandListenerWrapper commandlistenerwrapper, Entity entity) {
            if (entity instanceof EntityPlayer entityplayer) {
                entityplayer.lookAt(commandlistenerwrapper.getAnchor(), this.entity, this.anchor);
            } else {
                entity.lookAt(commandlistenerwrapper.getAnchor(), this.anchor.apply(this.entity));
            }

        }
    }
}
