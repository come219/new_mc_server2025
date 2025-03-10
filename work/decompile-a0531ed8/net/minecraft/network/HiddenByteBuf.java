package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCounted;

public record HiddenByteBuf(ByteBuf contents) implements ReferenceCounted {

    public HiddenByteBuf(final ByteBuf bytebuf) {
        this.contents = ByteBufUtil.ensureAccessible(bytebuf);
    }

    public static Object pack(Object object) {
        if (object instanceof ByteBuf bytebuf) {
            return new HiddenByteBuf(bytebuf);
        } else {
            return object;
        }
    }

    public static Object unpack(Object object) {
        if (object instanceof HiddenByteBuf hiddenbytebuf) {
            return ByteBufUtil.ensureAccessible(hiddenbytebuf.contents);
        } else {
            return object;
        }
    }

    public int refCnt() {
        return this.contents.refCnt();
    }

    public HiddenByteBuf retain() {
        this.contents.retain();
        return this;
    }

    public HiddenByteBuf retain(int i) {
        this.contents.retain(i);
        return this;
    }

    public HiddenByteBuf touch() {
        this.contents.touch();
        return this;
    }

    public HiddenByteBuf touch(Object object) {
        this.contents.touch(object);
        return this;
    }

    public boolean release() {
        return this.contents.release();
    }

    public boolean release(int i) {
        return this.contents.release(i);
    }
}
