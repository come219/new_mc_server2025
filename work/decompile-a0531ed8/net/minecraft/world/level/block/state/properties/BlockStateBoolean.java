package net.minecraft.world.level.block.state.properties;

import java.util.List;
import java.util.Optional;

public final class BlockStateBoolean extends IBlockState<Boolean> {

    private static final List<Boolean> VALUES = List.of(true, false);
    private static final int TRUE_INDEX = 0;
    private static final int FALSE_INDEX = 1;

    private BlockStateBoolean(String s) {
        super(s, Boolean.class);
    }

    @Override
    public List<Boolean> getPossibleValues() {
        return BlockStateBoolean.VALUES;
    }

    public static BlockStateBoolean create(String s) {
        return new BlockStateBoolean(s);
    }

    @Override
    public Optional<Boolean> getValue(String s) {
        Optional optional;

        switch (s) {
            case "true":
                optional = Optional.of(true);
                break;
            case "false":
                optional = Optional.of(false);
                break;
            default:
                optional = Optional.empty();
        }

        return optional;
    }

    public String getName(Boolean obool) {
        return obool.toString();
    }

    public int getInternalIndex(Boolean obool) {
        return obool ? 0 : 1;
    }
}
