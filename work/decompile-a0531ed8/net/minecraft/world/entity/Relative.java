package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum Relative {

    X(0), Y(1), Z(2), Y_ROT(3), X_ROT(4), DELTA_X(5), DELTA_Y(6), DELTA_Z(7), ROTATE_DELTA(8);

    public static final Set<Relative> ALL = Set.of(values());
    public static final Set<Relative> ROTATION = Set.of(Relative.X_ROT, Relative.Y_ROT);
    public static final Set<Relative> DELTA = Set.of(Relative.DELTA_X, Relative.DELTA_Y, Relative.DELTA_Z, Relative.ROTATE_DELTA);
    public static final StreamCodec<ByteBuf, Set<Relative>> SET_STREAM_CODEC = ByteBufCodecs.INT.map(Relative::unpack, Relative::pack);
    private final int bit;

    @SafeVarargs
    public static Set<Relative> union(Set<Relative>... aset) {
        HashSet<Relative> hashset = new HashSet();
        Set[] aset1 = aset;
        int i = aset.length;

        for (int j = 0; j < i; ++j) {
            Set<Relative> set = aset1[j];

            hashset.addAll(set);
        }

        return hashset;
    }

    private Relative(final int i) {
        this.bit = i;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int i) {
        return (i & this.getMask()) == this.getMask();
    }

    public static Set<Relative> unpack(int i) {
        Set<Relative> set = EnumSet.noneOf(Relative.class);
        Relative[] arelative = values();
        int j = arelative.length;

        for (int k = 0; k < j; ++k) {
            Relative relative = arelative[k];

            if (relative.isSet(i)) {
                set.add(relative);
            }
        }

        return set;
    }

    public static int pack(Set<Relative> set) {
        int i = 0;

        Relative relative;

        for (Iterator iterator = set.iterator(); iterator.hasNext(); i |= relative.getMask()) {
            relative = (Relative) iterator.next();
        }

        return i;
    }
}
