package net.minecraft.world.level.portal;

import java.util.Set;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutWorldEvent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3D;

public record TeleportTransition(WorldServer newLevel, Vec3D position, Vec3D deltaMovement, float yRot, float xRot, boolean missingRespawnBlock, boolean asPassenger, Set<Relative> relatives, TeleportTransition.a postTeleportTransition) {

    public static final TeleportTransition.a DO_NOTHING = (entity) -> {
    };
    public static final TeleportTransition.a PLAY_PORTAL_SOUND = TeleportTransition::playPortalSound;
    public static final TeleportTransition.a PLACE_PORTAL_TICKET = TeleportTransition::placePortalTicket;

    public TeleportTransition(WorldServer worldserver, Vec3D vec3d, Vec3D vec3d1, float f, float f1, TeleportTransition.a teleporttransition_a) {
        this(worldserver, vec3d, vec3d1, f, f1, Set.of(), teleporttransition_a);
    }

    public TeleportTransition(WorldServer worldserver, Vec3D vec3d, Vec3D vec3d1, float f, float f1, Set<Relative> set, TeleportTransition.a teleporttransition_a) {
        this(worldserver, vec3d, vec3d1, f, f1, false, false, set, teleporttransition_a);
    }

    public TeleportTransition(WorldServer worldserver, Entity entity, TeleportTransition.a teleporttransition_a) {
        this(worldserver, findAdjustedSharedSpawnPos(worldserver, entity), Vec3D.ZERO, 0.0F, 0.0F, false, false, Set.of(), teleporttransition_a);
    }

    private static void playPortalSound(Entity entity) {
        if (entity instanceof EntityPlayer entityplayer) {
            entityplayer.connection.send(new PacketPlayOutWorldEvent(1032, BlockPosition.ZERO, 0, false));
        }

    }

    private static void placePortalTicket(Entity entity) {
        entity.placePortalTicket(BlockPosition.containing(entity.position()));
    }

    public static TeleportTransition missingRespawnBlock(WorldServer worldserver, Entity entity, TeleportTransition.a teleporttransition_a) {
        return new TeleportTransition(worldserver, findAdjustedSharedSpawnPos(worldserver, entity), Vec3D.ZERO, 0.0F, 0.0F, true, false, Set.of(), teleporttransition_a);
    }

    private static Vec3D findAdjustedSharedSpawnPos(WorldServer worldserver, Entity entity) {
        return entity.adjustSpawnLocation(worldserver, worldserver.getSharedSpawnPos()).getBottomCenter();
    }

    public TeleportTransition withRotation(float f, float f1) {
        return new TeleportTransition(this.newLevel(), this.position(), this.deltaMovement(), f, f1, this.missingRespawnBlock(), this.asPassenger(), this.relatives(), this.postTeleportTransition());
    }

    public TeleportTransition withPosition(Vec3D vec3d) {
        return new TeleportTransition(this.newLevel(), vec3d, this.deltaMovement(), this.yRot(), this.xRot(), this.missingRespawnBlock(), this.asPassenger(), this.relatives(), this.postTeleportTransition());
    }

    public TeleportTransition transitionAsPassenger() {
        return new TeleportTransition(this.newLevel(), this.position(), this.deltaMovement(), this.yRot(), this.xRot(), this.missingRespawnBlock(), true, this.relatives(), this.postTeleportTransition());
    }

    @FunctionalInterface
    public interface a {

        void onTransition(Entity entity);

        default TeleportTransition.a then(TeleportTransition.a teleporttransition_a) {
            return (entity) -> {
                this.onTransition(entity);
                teleporttransition_a.onTransition(entity);
            };
        }
    }
}
