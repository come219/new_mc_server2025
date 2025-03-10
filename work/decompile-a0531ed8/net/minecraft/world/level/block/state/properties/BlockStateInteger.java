package net.minecraft.world.level.block.state.properties;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public final class BlockStateInteger extends IBlockState<Integer> {

    private final IntImmutableList values;
    public final int min;
    public final int max;

    private BlockStateInteger(String s, int i, int j) {
        super(s, Integer.class);
        if (i < 0) {
            throw new IllegalArgumentException("Min value of " + s + " must be 0 or greater");
        } else if (j <= i) {
            throw new IllegalArgumentException("Max value of " + s + " must be greater than min (" + i + ")");
        } else {
            this.min = i;
            this.max = j;
            this.values = IntImmutableList.toList(IntStream.range(i, j + 1));
        }
    }

    @Override
    public List<Integer> getPossibleValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            if (object instanceof BlockStateInteger) {
                BlockStateInteger blockstateinteger = (BlockStateInteger) object;

                if (super.equals(object)) {
                    return this.values.equals(blockstateinteger.values);
                }
            }

            return false;
        }
    }

    @Override
    public int generateHashCode() {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }

    public static BlockStateInteger create(String s, int i, int j) {
        return new BlockStateInteger(s, i, j);
    }

    @Override
    public Optional<Integer> getValue(String s) {
        try {
            int i = Integer.parseInt(s);

            return i >= this.min && i <= this.max ? Optional.of(i) : Optional.empty();
        } catch (NumberFormatException numberformatexception) {
            return Optional.empty();
        }
    }

    public String getName(Integer integer) {
        return integer.toString();
    }

    public int getInternalIndex(Integer integer) {
        return integer <= this.max ? integer - this.min : -1;
    }
}
