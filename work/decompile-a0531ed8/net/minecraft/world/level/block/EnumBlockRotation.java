package net.minecraft.world.level.block;

import com.mojang.math.PointGroupO;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.INamable;
import net.minecraft.util.RandomSource;

public enum EnumBlockRotation implements INamable {

    NONE("none", PointGroupO.IDENTITY), CLOCKWISE_90("clockwise_90", PointGroupO.ROT_90_Y_NEG), CLOCKWISE_180("180", PointGroupO.ROT_180_FACE_XZ), COUNTERCLOCKWISE_90("counterclockwise_90", PointGroupO.ROT_90_Y_POS);

    public static final Codec<EnumBlockRotation> CODEC = INamable.fromEnum(EnumBlockRotation::values);
    private final String id;
    private final PointGroupO rotation;

    private EnumBlockRotation(final String s, final PointGroupO pointgroupo) {
        this.id = s;
        this.rotation = pointgroupo;
    }

    public EnumBlockRotation getRotated(EnumBlockRotation enumblockrotation) {
        EnumBlockRotation enumblockrotation1;

        switch (enumblockrotation.ordinal()) {
            case 1:
                switch (this.ordinal()) {
                    case 0:
                        enumblockrotation1 = EnumBlockRotation.CLOCKWISE_90;
                        return enumblockrotation1;
                    case 1:
                        enumblockrotation1 = EnumBlockRotation.CLOCKWISE_180;
                        return enumblockrotation1;
                    case 2:
                        enumblockrotation1 = EnumBlockRotation.COUNTERCLOCKWISE_90;
                        return enumblockrotation1;
                    case 3:
                        enumblockrotation1 = EnumBlockRotation.NONE;
                        return enumblockrotation1;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }
            case 2:
                switch (this.ordinal()) {
                    case 0:
                        enumblockrotation1 = EnumBlockRotation.CLOCKWISE_180;
                        return enumblockrotation1;
                    case 1:
                        enumblockrotation1 = EnumBlockRotation.COUNTERCLOCKWISE_90;
                        return enumblockrotation1;
                    case 2:
                        enumblockrotation1 = EnumBlockRotation.NONE;
                        return enumblockrotation1;
                    case 3:
                        enumblockrotation1 = EnumBlockRotation.CLOCKWISE_90;
                        return enumblockrotation1;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }
            case 3:
                switch (this.ordinal()) {
                    case 0:
                        enumblockrotation1 = EnumBlockRotation.COUNTERCLOCKWISE_90;
                        return enumblockrotation1;
                    case 1:
                        enumblockrotation1 = EnumBlockRotation.NONE;
                        return enumblockrotation1;
                    case 2:
                        enumblockrotation1 = EnumBlockRotation.CLOCKWISE_90;
                        return enumblockrotation1;
                    case 3:
                        enumblockrotation1 = EnumBlockRotation.CLOCKWISE_180;
                        return enumblockrotation1;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }
            default:
                enumblockrotation1 = this;
                return enumblockrotation1;
        }
    }

    public PointGroupO rotation() {
        return this.rotation;
    }

    public EnumDirection rotate(EnumDirection enumdirection) {
        if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
            return enumdirection;
        } else {
            EnumDirection enumdirection1;

            switch (this.ordinal()) {
                case 1:
                    enumdirection1 = enumdirection.getClockWise();
                    break;
                case 2:
                    enumdirection1 = enumdirection.getOpposite();
                    break;
                case 3:
                    enumdirection1 = enumdirection.getCounterClockWise();
                    break;
                default:
                    enumdirection1 = enumdirection;
            }

            return enumdirection1;
        }
    }

    public int rotate(int i, int j) {
        int k;

        switch (this.ordinal()) {
            case 1:
                k = (i + j / 4) % j;
                break;
            case 2:
                k = (i + j / 2) % j;
                break;
            case 3:
                k = (i + j * 3 / 4) % j;
                break;
            default:
                k = i;
        }

        return k;
    }

    public static EnumBlockRotation getRandom(RandomSource randomsource) {
        return (EnumBlockRotation) SystemUtils.getRandom((Object[]) values(), randomsource);
    }

    public static List<EnumBlockRotation> getShuffled(RandomSource randomsource) {
        return SystemUtils.shuffledCopy((Object[]) values(), randomsource);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
