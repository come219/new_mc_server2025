package net.minecraft.world.level.redstone;

import com.google.common.annotations.VisibleForTesting;
import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.SystemUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

public class Orientation {

    public static final StreamCodec<ByteBuf, Orientation> STREAM_CODEC = ByteBufCodecs.idMapper(Orientation::fromIndex, Orientation::getIndex);
    private static final Orientation[] ORIENTATIONS = (Orientation[]) SystemUtils.make(() -> {
        Orientation[] aorientation = new Orientation[48];

        generateContext(new Orientation(EnumDirection.UP, EnumDirection.NORTH, Orientation.a.LEFT), aorientation);
        return aorientation;
    });
    private final EnumDirection up;
    private final EnumDirection front;
    private final EnumDirection side;
    private final Orientation.a sideBias;
    private final int index;
    private final List<EnumDirection> neighbors;
    private final List<EnumDirection> horizontalNeighbors;
    private final List<EnumDirection> verticalNeighbors;
    private final Map<EnumDirection, Orientation> withFront = new EnumMap(EnumDirection.class);
    private final Map<EnumDirection, Orientation> withUp = new EnumMap(EnumDirection.class);
    private final Map<Orientation.a, Orientation> withSideBias = new EnumMap(Orientation.a.class);

    private Orientation(EnumDirection enumdirection, EnumDirection enumdirection1, Orientation.a orientation_a) {
        this.up = enumdirection;
        this.front = enumdirection1;
        this.sideBias = orientation_a;
        this.index = generateIndex(enumdirection, enumdirection1, orientation_a);
        BaseBlockPosition baseblockposition = enumdirection1.getUnitVec3i().cross(enumdirection.getUnitVec3i());
        EnumDirection enumdirection2 = EnumDirection.getNearest(baseblockposition, (EnumDirection) null);

        Objects.requireNonNull(enumdirection2);
        if (this.sideBias == Orientation.a.RIGHT) {
            this.side = enumdirection2;
        } else {
            this.side = enumdirection2.getOpposite();
        }

        this.neighbors = List.of(this.front.getOpposite(), this.front, this.side, this.side.getOpposite(), this.up.getOpposite(), this.up);
        this.horizontalNeighbors = this.neighbors.stream().filter((enumdirection3) -> {
            return enumdirection3.getAxis() != this.up.getAxis();
        }).toList();
        this.verticalNeighbors = this.neighbors.stream().filter((enumdirection3) -> {
            return enumdirection3.getAxis() == this.up.getAxis();
        }).toList();
    }

    public static Orientation of(EnumDirection enumdirection, EnumDirection enumdirection1, Orientation.a orientation_a) {
        return Orientation.ORIENTATIONS[generateIndex(enumdirection, enumdirection1, orientation_a)];
    }

    public Orientation withUp(EnumDirection enumdirection) {
        return (Orientation) this.withUp.get(enumdirection);
    }

    public Orientation withFront(EnumDirection enumdirection) {
        return (Orientation) this.withFront.get(enumdirection);
    }

    public Orientation withFrontPreserveUp(EnumDirection enumdirection) {
        return enumdirection.getAxis() == this.up.getAxis() ? this : (Orientation) this.withFront.get(enumdirection);
    }

    public Orientation withFrontAdjustSideBias(EnumDirection enumdirection) {
        Orientation orientation = this.withFront(enumdirection);

        return this.front == orientation.side ? orientation.withMirror() : orientation;
    }

    public Orientation withSideBias(Orientation.a orientation_a) {
        return (Orientation) this.withSideBias.get(orientation_a);
    }

    public Orientation withMirror() {
        return this.withSideBias(this.sideBias.getOpposite());
    }

    public EnumDirection getFront() {
        return this.front;
    }

    public EnumDirection getUp() {
        return this.up;
    }

    public EnumDirection getSide() {
        return this.side;
    }

    public Orientation.a getSideBias() {
        return this.sideBias;
    }

    public List<EnumDirection> getDirections() {
        return this.neighbors;
    }

    public List<EnumDirection> getHorizontalDirections() {
        return this.horizontalNeighbors;
    }

    public List<EnumDirection> getVerticalDirections() {
        return this.verticalNeighbors;
    }

    public String toString() {
        String s = String.valueOf(this.up);

        return "[up=" + s + ",front=" + String.valueOf(this.front) + ",sideBias=" + String.valueOf(this.sideBias) + "]";
    }

    public int getIndex() {
        return this.index;
    }

    public static Orientation fromIndex(int i) {
        return Orientation.ORIENTATIONS[i];
    }

    public static Orientation random(RandomSource randomsource) {
        return (Orientation) SystemUtils.getRandom((Object[]) Orientation.ORIENTATIONS, randomsource);
    }

    private static Orientation generateContext(Orientation orientation, Orientation[] aorientation) {
        if (aorientation[orientation.getIndex()] != null) {
            return aorientation[orientation.getIndex()];
        } else {
            aorientation[orientation.getIndex()] = orientation;
            Orientation.a[] aorientation_a = Orientation.a.values();
            int i = aorientation_a.length;

            int j;

            for (j = 0; j < i; ++j) {
                Orientation.a orientation_a = aorientation_a[j];

                orientation.withSideBias.put(orientation_a, generateContext(new Orientation(orientation.up, orientation.front, orientation_a), aorientation));
            }

            EnumDirection[] aenumdirection = EnumDirection.values();

            i = aenumdirection.length;

            EnumDirection enumdirection;
            EnumDirection enumdirection1;

            for (j = 0; j < i; ++j) {
                enumdirection = aenumdirection[j];
                enumdirection1 = orientation.up;
                if (enumdirection == orientation.up) {
                    enumdirection1 = orientation.front.getOpposite();
                }

                if (enumdirection == orientation.up.getOpposite()) {
                    enumdirection1 = orientation.front;
                }

                orientation.withFront.put(enumdirection, generateContext(new Orientation(enumdirection1, enumdirection, orientation.sideBias), aorientation));
            }

            aenumdirection = EnumDirection.values();
            i = aenumdirection.length;

            for (j = 0; j < i; ++j) {
                enumdirection = aenumdirection[j];
                enumdirection1 = orientation.front;
                if (enumdirection == orientation.front) {
                    enumdirection1 = orientation.up.getOpposite();
                }

                if (enumdirection == orientation.front.getOpposite()) {
                    enumdirection1 = orientation.up;
                }

                orientation.withUp.put(enumdirection, generateContext(new Orientation(enumdirection, enumdirection1, orientation.sideBias), aorientation));
            }

            return orientation;
        }
    }

    @VisibleForTesting
    protected static int generateIndex(EnumDirection enumdirection, EnumDirection enumdirection1, Orientation.a orientation_a) {
        if (enumdirection.getAxis() == enumdirection1.getAxis()) {
            throw new IllegalStateException("Up-vector and front-vector can not be on the same axis");
        } else {
            int i;

            if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
                i = enumdirection1.getAxis() == EnumDirection.EnumAxis.X ? 1 : 0;
            } else {
                i = enumdirection1.getAxis() == EnumDirection.EnumAxis.Y ? 1 : 0;
            }

            int j = i << 1 | enumdirection1.getAxisDirection().ordinal();

            return ((enumdirection.ordinal() << 2) + j << 1) + orientation_a.ordinal();
        }
    }

    public static enum a {

        LEFT("left"), RIGHT("right");

        private final String name;

        private a(final String s) {
            this.name = s;
        }

        public Orientation.a getOpposite() {
            return this == Orientation.a.LEFT ? Orientation.a.RIGHT : Orientation.a.LEFT;
        }

        public String toString() {
            return this.name;
        }
    }
}
