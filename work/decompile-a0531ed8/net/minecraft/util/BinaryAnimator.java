package net.minecraft.util;

public class BinaryAnimator {

    private final int animationLength;
    private final BinaryAnimator.a easingFunction;
    private int ticks;
    private int ticksOld;

    public BinaryAnimator(int i, BinaryAnimator.a binaryanimator_a) {
        this.animationLength = i;
        this.easingFunction = binaryanimator_a;
    }

    public BinaryAnimator(int i) {
        this(i, (f) -> {
            return f;
        });
    }

    public void tick(boolean flag) {
        this.ticksOld = this.ticks;
        if (flag) {
            if (this.ticks < this.animationLength) {
                ++this.ticks;
            }
        } else if (this.ticks > 0) {
            --this.ticks;
        }

    }

    public float getFactor(float f) {
        float f1 = MathHelper.lerp(f, (float) this.ticksOld, (float) this.ticks) / (float) this.animationLength;

        return this.easingFunction.apply(f1);
    }

    public interface a {

        float apply(float f);
    }
}
