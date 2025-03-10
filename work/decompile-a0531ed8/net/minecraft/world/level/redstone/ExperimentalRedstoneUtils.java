package net.minecraft.world.level.redstone;

import javax.annotation.Nullable;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.World;

public class ExperimentalRedstoneUtils {

    public ExperimentalRedstoneUtils() {}

    @Nullable
    public static Orientation initialOrientation(World world, @Nullable EnumDirection enumdirection, @Nullable EnumDirection enumdirection1) {
        if (world.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
            Orientation orientation = Orientation.random(world.random).withSideBias(Orientation.a.LEFT);

            if (enumdirection1 != null) {
                orientation = orientation.withUp(enumdirection1);
            }

            if (enumdirection != null) {
                orientation = orientation.withFront(enumdirection);
            }

            return orientation;
        } else {
            return null;
        }
    }

    @Nullable
    public static Orientation withFront(@Nullable Orientation orientation, EnumDirection enumdirection) {
        return orientation == null ? null : orientation.withFront(enumdirection);
    }
}
