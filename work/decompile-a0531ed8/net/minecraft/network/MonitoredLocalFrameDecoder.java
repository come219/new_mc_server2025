package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MonitoredLocalFrameDecoder extends ChannelInboundHandlerAdapter {

    private final BandwidthDebugMonitor monitor;

    public MonitoredLocalFrameDecoder(BandwidthDebugMonitor bandwidthdebugmonitor) {
        this.monitor = bandwidthdebugmonitor;
    }

    public void channelRead(ChannelHandlerContext channelhandlercontext, Object object) {
        object = HiddenByteBuf.unpack(object);
        if (object instanceof ByteBuf bytebuf) {
            this.monitor.onReceive(bytebuf.readableBytes());
        }

        channelhandlercontext.fireChannelRead(object);
    }
}
