package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class LocalFrameEncoder extends ChannelOutboundHandlerAdapter {

    public LocalFrameEncoder() {}

    public void write(ChannelHandlerContext channelhandlercontext, Object object, ChannelPromise channelpromise) {
        channelhandlercontext.write(HiddenByteBuf.pack(object), channelpromise);
    }
}
