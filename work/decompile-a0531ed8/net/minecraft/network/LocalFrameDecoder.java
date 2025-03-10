package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LocalFrameDecoder extends ChannelInboundHandlerAdapter {

    public LocalFrameDecoder() {}

    public void channelRead(ChannelHandlerContext channelhandlercontext, Object object) {
        channelhandlercontext.fireChannelRead(HiddenByteBuf.unpack(object));
    }
}
