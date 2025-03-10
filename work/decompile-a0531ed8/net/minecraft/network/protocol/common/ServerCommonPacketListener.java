package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;

public interface ServerCommonPacketListener extends ServerCookiePacketListener {

    void handleKeepAlive(ServerboundKeepAlivePacket serverboundkeepalivepacket);

    void handlePong(ServerboundPongPacket serverboundpongpacket);

    void handleCustomPayload(ServerboundCustomPayloadPacket serverboundcustompayloadpacket);

    void handleResourcePackResponse(ServerboundResourcePackPacket serverboundresourcepackpacket);

    void handleClientInformation(ServerboundClientInformationPacket serverboundclientinformationpacket);
}
