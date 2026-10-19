package pl.airr.cinderac.util;

/** Table-based trigonometry as in the Minecraft client (same results as the game when predicting movement). */
public final class McMath {
    private static final float[] SIN = new float[65536];

    static {
        for (int i = 0; i < 65536; ++i) SIN[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
    }

    private McMath() {
    }

    public static float sin(float f) {
        return SIN[(int) (f * 10430.378f) & 0xFFFF];
    }

    public static float cos(float f) {
        return SIN[(int) (f * 10430.378f + 16384.0f) & 0xFFFF];
    }

    public static float sqrt(float f) {
        return (float) Math.sqrt(f);
    }
}
