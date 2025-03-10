package net.minecraft.world.item;

import net.minecraft.network.protocol.game.PacketPlayOutSetCooldown;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;

public class ItemCooldownPlayer extends ItemCooldown {

    private final EntityPlayer player;

    public ItemCooldownPlayer(EntityPlayer entityplayer) {
        this.player = entityplayer;
    }

    @Override
    protected void onCooldownStarted(MinecraftKey minecraftkey, int i) {
        super.onCooldownStarted(minecraftkey, i);
        this.player.connection.send(new PacketPlayOutSetCooldown(minecraftkey, i));
    }

    @Override
    protected void onCooldownEnded(MinecraftKey minecraftkey) {
        super.onCooldownEnded(minecraftkey);
        this.player.connection.send(new PacketPlayOutSetCooldown(minecraftkey, 0));
    }
}
