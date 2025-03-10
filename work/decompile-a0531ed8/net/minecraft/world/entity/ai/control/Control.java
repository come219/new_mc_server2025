package net.minecraft.world.entity.ai.control;

import net.minecraft.util.MathHelper;

public interface Control {

    default float rotateTowards(float f, float f1, float f2) {
        float f3 = MathHelper.degreesDifference(f, f1);
        float f4 = MathHelper.clamp(f3, -f2, f2);

        return f + f4;
    }
}
