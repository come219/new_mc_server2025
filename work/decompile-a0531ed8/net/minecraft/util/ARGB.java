package net.minecraft.util;

import net.minecraft.world.phys.Vec3D;
import org.joml.Vector3f;

public class ARGB {

    public ARGB() {}

    public static int alpha(int i) {
        return i >>> 24;
    }

    public static int red(int i) {
        return i >> 16 & 255;
    }

    public static int green(int i) {
        return i >> 8 & 255;
    }

    public static int blue(int i) {
        return i & 255;
    }

    public static int color(int i, int j, int k, int l) {
        return i << 24 | j << 16 | k << 8 | l;
    }

    public static int color(int i, int j, int k) {
        return color(255, i, j, k);
    }

    public static int color(Vec3D vec3d) {
        return color(as8BitChannel((float) vec3d.x()), as8BitChannel((float) vec3d.y()), as8BitChannel((float) vec3d.z()));
    }

    public static int multiply(int i, int j) {
        return i == -1 ? j : (j == -1 ? i : color(alpha(i) * alpha(j) / 255, red(i) * red(j) / 255, green(i) * green(j) / 255, blue(i) * blue(j) / 255));
    }

    public static int scaleRGB(int i, float f) {
        return scaleRGB(i, f, f, f);
    }

    public static int scaleRGB(int i, float f, float f1, float f2) {
        return color(alpha(i), Math.clamp((long) ((int) ((float) red(i) * f)), 0, 255), Math.clamp((long) ((int) ((float) green(i) * f1)), 0, 255), Math.clamp((long) ((int) ((float) blue(i) * f2)), 0, 255));
    }

    public static int scaleRGB(int i, int j) {
        return color(alpha(i), Math.clamp((long) red(i) * (long) j / 255L, 0, 255), Math.clamp((long) green(i) * (long) j / 255L, 0, 255), Math.clamp((long) blue(i) * (long) j / 255L, 0, 255));
    }

    public static int greyscale(int i) {
        int j = (int) ((float) red(i) * 0.3F + (float) green(i) * 0.59F + (float) blue(i) * 0.11F);

        return color(j, j, j);
    }

    public static int lerp(float f, int i, int j) {
        int k = MathHelper.lerpInt(f, alpha(i), alpha(j));
        int l = MathHelper.lerpInt(f, red(i), red(j));
        int i1 = MathHelper.lerpInt(f, green(i), green(j));
        int j1 = MathHelper.lerpInt(f, blue(i), blue(j));

        return color(k, l, i1, j1);
    }

    public static int opaque(int i) {
        return i | -16777216;
    }

    public static int transparent(int i) {
        return i & 16777215;
    }

    public static int color(int i, int j) {
        return i << 24 | j & 16777215;
    }

    public static int white(float f) {
        return as8BitChannel(f) << 24 | 16777215;
    }

    public static int colorFromFloat(float f, float f1, float f2, float f3) {
        return color(as8BitChannel(f), as8BitChannel(f1), as8BitChannel(f2), as8BitChannel(f3));
    }

    public static Vector3f vector3fFromRGB24(int i) {
        float f = (float) red(i) / 255.0F;
        float f1 = (float) green(i) / 255.0F;
        float f2 = (float) blue(i) / 255.0F;

        return new Vector3f(f, f1, f2);
    }

    public static int average(int i, int j) {
        return color((alpha(i) + alpha(j)) / 2, (red(i) + red(j)) / 2, (green(i) + green(j)) / 2, (blue(i) + blue(j)) / 2);
    }

    public static int as8BitChannel(float f) {
        return MathHelper.floor(f * 255.0F);
    }

    public static float alphaFloat(int i) {
        return from8BitChannel(alpha(i));
    }

    public static float redFloat(int i) {
        return from8BitChannel(red(i));
    }

    public static float greenFloat(int i) {
        return from8BitChannel(green(i));
    }

    public static float blueFloat(int i) {
        return from8BitChannel(blue(i));
    }

    private static float from8BitChannel(int i) {
        return (float) i / 255.0F;
    }

    public static int toABGR(int i) {
        return i & -16711936 | (i & 16711680) >> 16 | (i & 255) << 16;
    }

    public static int fromABGR(int i) {
        return toABGR(i);
    }
}
