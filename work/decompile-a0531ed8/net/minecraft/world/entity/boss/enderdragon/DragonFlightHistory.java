package net.minecraft.world.entity.boss.enderdragon;

import java.util.Arrays;
import net.minecraft.util.MathHelper;

public class DragonFlightHistory {

    public static final int LENGTH = 64;
    private static final int MASK = 63;
    private final DragonFlightHistory.a[] samples = new DragonFlightHistory.a[64];
    private int head = -1;

    public DragonFlightHistory() {
        Arrays.fill(this.samples, new DragonFlightHistory.a(0.0D, 0.0F));
    }

    public void copyFrom(DragonFlightHistory dragonflighthistory) {
        System.arraycopy(dragonflighthistory.samples, 0, this.samples, 0, 64);
        this.head = dragonflighthistory.head;
    }

    public void record(double d0, float f) {
        DragonFlightHistory.a dragonflighthistory_a = new DragonFlightHistory.a(d0, f);

        if (this.head < 0) {
            Arrays.fill(this.samples, dragonflighthistory_a);
        }

        if (++this.head == 64) {
            this.head = 0;
        }

        this.samples[this.head] = dragonflighthistory_a;
    }

    public DragonFlightHistory.a get(int i) {
        return this.samples[this.head - i & 63];
    }

    public DragonFlightHistory.a get(int i, float f) {
        DragonFlightHistory.a dragonflighthistory_a = this.get(i);
        DragonFlightHistory.a dragonflighthistory_a1 = this.get(i + 1);

        return new DragonFlightHistory.a(MathHelper.lerp((double) f, dragonflighthistory_a1.y, dragonflighthistory_a.y), MathHelper.rotLerp(f, dragonflighthistory_a1.yRot, dragonflighthistory_a.yRot));
    }

    public static record a(double y, float yRot) {

    }
}
