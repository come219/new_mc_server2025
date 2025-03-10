package net.minecraft.core;

import net.minecraft.SystemUtils;
import net.minecraft.util.INamable;

public enum BlockPropertyJigsawOrientation implements INamable {

    DOWN_EAST("down_east", EnumDirection.DOWN, EnumDirection.EAST), DOWN_NORTH("down_north", EnumDirection.DOWN, EnumDirection.NORTH), DOWN_SOUTH("down_south", EnumDirection.DOWN, EnumDirection.SOUTH), DOWN_WEST("down_west", EnumDirection.DOWN, EnumDirection.WEST), UP_EAST("up_east", EnumDirection.UP, EnumDirection.EAST), UP_NORTH("up_north", EnumDirection.UP, EnumDirection.NORTH), UP_SOUTH("up_south", EnumDirection.UP, EnumDirection.SOUTH), UP_WEST("up_west", EnumDirection.UP, EnumDirection.WEST), WEST_UP("west_up", EnumDirection.WEST, EnumDirection.UP), EAST_UP("east_up", EnumDirection.EAST, EnumDirection.UP), NORTH_UP("north_up", EnumDirection.NORTH, EnumDirection.UP), SOUTH_UP("south_up", EnumDirection.SOUTH, EnumDirection.UP);

    private static final int NUM_DIRECTIONS = EnumDirection.values().length;
    private static final BlockPropertyJigsawOrientation[] BY_TOP_FRONT = (BlockPropertyJigsawOrientation[]) SystemUtils.make(new BlockPropertyJigsawOrientation[BlockPropertyJigsawOrientation.NUM_DIRECTIONS * BlockPropertyJigsawOrientation.NUM_DIRECTIONS], (ablockpropertyjigsaworientation) -> {
        BlockPropertyJigsawOrientation[] ablockpropertyjigsaworientation1 = values();
        int i = ablockpropertyjigsaworientation1.length;

        for (int j = 0; j < i; ++j) {
            BlockPropertyJigsawOrientation blockpropertyjigsaworientation = ablockpropertyjigsaworientation1[j];

            ablockpropertyjigsaworientation[lookupKey(blockpropertyjigsaworientation.front, blockpropertyjigsaworientation.top)] = blockpropertyjigsaworientation;
        }

    });
    private final String name;
    private final EnumDirection top;
    private final EnumDirection front;

    private static int lookupKey(EnumDirection enumdirection, EnumDirection enumdirection1) {
        return enumdirection.ordinal() * BlockPropertyJigsawOrientation.NUM_DIRECTIONS + enumdirection1.ordinal();
    }

    private BlockPropertyJigsawOrientation(final String s, final EnumDirection enumdirection, final EnumDirection enumdirection1) {
        this.name = s;
        this.front = enumdirection;
        this.top = enumdirection1;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static BlockPropertyJigsawOrientation fromFrontAndTop(EnumDirection enumdirection, EnumDirection enumdirection1) {
        return BlockPropertyJigsawOrientation.BY_TOP_FRONT[lookupKey(enumdirection, enumdirection1)];
    }

    public EnumDirection front() {
        return this.front;
    }

    public EnumDirection top() {
        return this.top;
    }
}
