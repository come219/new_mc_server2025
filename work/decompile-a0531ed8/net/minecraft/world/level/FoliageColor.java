package net.minecraft.world.level;

public class FoliageColor {

    public static final int FOLIAGE_EVERGREEN = -10380959;
    public static final int FOLIAGE_BIRCH = -8345771;
    public static final int FOLIAGE_DEFAULT = -12012264;
    public static final int FOLIAGE_MANGROVE = -7158200;
    private static int[] pixels = new int[65536];

    public FoliageColor() {}

    public static void init(int[] aint) {
        FoliageColor.pixels = aint;
    }

    public static int get(double d0, double d1) {
        d1 *= d0;
        int i = (int) ((1.0D - d0) * 255.0D);
        int j = (int) ((1.0D - d1) * 255.0D);
        int k = j << 8 | i;

        return k >= FoliageColor.pixels.length ? -12012264 : FoliageColor.pixels[k];
    }
}
