package net.minecraft.network.protocol.login;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.cookie.ClientCookiePacketListener;

public interface PacketLoginOutListener extends ClientCookiePacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.LOGIN;
    }

    void handleHello(PacketLoginOutEncryptionBegin packetloginoutencryptionbegin);

    void handleLoginFinished(PacketLoginOutSuccess packetloginoutsuccess);

    void handleDisconnect(PacketLoginOutDisconnect packetloginoutdisconnect);

    void handleCompression(PacketLoginOutSetCompression packetloginoutsetcompression);

    void handleCustomQuery(PacketLoginOutCustomPayload packetloginoutcustompayload);
}
