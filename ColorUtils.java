//By ChatGPT.
public class ColorUtils {
    private static final float TAU = (float)Math.TAU;

    // 12-bit tables: 4096 entries
    private static final int LUT_SIZE = 4096;
    private static final float INV_LUT_SIZE = 1.0f / (LUT_SIZE - 1);

    // sRGB -> linear
    private static final float[] SRGB_TO_LINEAR_LUT = new float[LUT_SIZE];

    // linear -> sRGB
    private static final float[] LINEAR_TO_SRGB_LUT = new float[LUT_SIZE];

    static {
        // Fill tables
        for (int i = 0; i < LUT_SIZE; i++) {
            float x = i * INV_LUT_SIZE;  // normalized 0..1 float

            // --- Standard sRGB ↔ linear equations ---
            // sRGB -> linear
            if (x <= 0.04045f)
                SRGB_TO_LINEAR_LUT[i] = x / 12.92f;
            else
                SRGB_TO_LINEAR_LUT[i] = (float) Math.pow((x + 0.055f) / 1.055f, 2.4f);

            // linear -> sRGB
            if (x <= 0.0031308f)
                LINEAR_TO_SRGB_LUT[i] = 12.92f * x;
            else
                LINEAR_TO_SRGB_LUT[i] = 1.055f * (float) Math.pow(x, 1f / 2.4f) - 0.055f;
        }
    }

    // ========================================================================================
    // FAST LOOKUP FUNCTIONS
    // ========================================================================================

    public static float srgbToLinear(float c) {
        return SRGB_TO_LINEAR_LUT[(int) (c * (LUT_SIZE - 1))];
    }

    public static float linearToSrgb(float c) {
        return LINEAR_TO_SRGB_LUT[(int) (c * (LUT_SIZE - 1))];
    }
    /*
    // ---------- Linearize / Delinearize sRGB ----------
    public static float srgbToLinear(float c) {
        if (c <= 0.04045f) return c / 12.92f;
        return (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }
    public static float linearToSrgb(float c) {
        if (c <= 0.0031308f) return 12.92f * c;
        return 1.055f * (float) Math.pow(c, 1.0f / 2.4f) - 0.055f;
    }
    */

    // ---------- OKLab <-> linear sRGB ----------
    public static float[] linearSrgbToOklab(float r, float g, float b) {
        float l = 0.41222f * r + 0.53633f * g + 0.05145f * b;
        float m = 0.21190f * r + 0.68070f * g + 0.10740f * b;
        float s = 0.08830f * r + 0.28172f * g + 0.62998f * b;

        float l_ = (float) Math.cbrt(l);
        float m_ = (float) Math.cbrt(m);
        float s_ = (float) Math.cbrt(s);

        return new float[]{
            0.21045f * l_ + 0.79362f * m_ - 0.00407f * s_,  // L
            1.97800f * l_ - 2.42859f * m_ + 0.45059f * s_,  // a
            0.02590f * l_ + 0.78277f * m_ - 0.80868f * s_   // b
        };
    }

    public static float[] oklabToLinearSrgb(float L, float a, float b) {
        float l_ = L + 0.39634f * a + 0.21580f * b;
        float m_ = L - 0.10556f * a - 0.06385f * b;
        float s_ = L - 0.08948f * a - 1.29149f * b;

        float l = l_ * l_ * l_;
        float m = m_ * m_ * m_;
        float s = s_ * s_ * s_;

        return new float[]{
            +4.07674f * l - 3.30771f * m + 0.23097f * s,
            -1.26843f * l + 2.60976f * m - 0.34132f * s,
            -0.00420f * l - 0.70342f * m + 1.70761f * s
        };
    }

    // ---------- OKLCh <-> OKLab ----------
    public static float[] oklabToOklch(float L, float a, float b) {
        float C = (float) Math.sqrt(a * a + b * b);
        float h = (float) Math.atan2(b, a);
        if (h < 0) h += TAU;
        return new float[]{L, C, h};
    }

    public static float[] oklchToOklab(float L, float C, float h) {
        return new float[]{
            L,
            C * (float) Math.cos(h),
            C * (float) Math.sin(h)
        };
    }
    private static final int RAINBOW_LUT_SIZE = 4096;
    private static final float INV_RAINBOW_SIZE = 1.0f / RAINBOW_LUT_SIZE;

    private static final float[][] RAINBOW_LUT = new float[RAINBOW_LUT_SIZE][3];
    static {
        for (int i = 0; i < RAINBOW_LUT_SIZE; i++) {
            float seed = i * INV_RAINBOW_SIZE;     // 0..1
            float h = (float)(seed * Math.TAU);    // OKLCh hue

            // Choose your desired L and C here:
            final float L = 0.745f;  // or pass them as parameters
            final float C = 0.126f;

            // OKLCh -> OKLab
            float a = C * (float)Math.cos(h);
            float b = C * (float)Math.sin(h);

            // OKLab -> linear sRGB
            float l_ = L + 0.39634f * a + 0.21580f * b;
            float m_ = L - 0.10556f * a - 0.06385f * b;
            float s_ = L - 0.08948f * a - 1.29149f * b;

            float l = l_ * l_ * l_;
            float m = m_ * m_ * m_;
            float s = s_ * s_ * s_;

            float r = +4.07674f * l - 3.30771f * m + 0.23097f * s;
            float g = -1.26843f * l + 2.60976f * m - 0.34132f * s;
            float bb = -0.00420f * l - 0.70342f * m + 1.70761f * s;

            RAINBOW_LUT[i][0] = r;
            RAINBOW_LUT[i][1] = g;
            RAINBOW_LUT[i][2] = bb;
        }
    }
    public static void sampleRainbow(float seed, float[] out) {
        seed-=Math.floor(seed);
        float[] a = RAINBOW_LUT[(int) (seed * (RAINBOW_LUT_SIZE - 1))];

        out[0]=a[0];
        out[1]=a[1];
        out[2]=a[2];
    }

    /*
    // ---------- Sample around OKLCh hue circle ----------
    public static float[] sampleRainbow(float seed, float L, float C) {

        float h = (float) Math.TAU * seed;
        // OKLCh -> OKLab -> linear sRGB
        float[] lab = oklchToOklab(L, C, h);
        
        return oklabToLinearSrgb(lab[0], lab[1], lab[2]);
    }
    */
}