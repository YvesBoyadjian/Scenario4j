package application.scenegraph.douglas;

public class PerlinNoise {

    public static final int IYMAX = 100;
    public static final int IXMAX = 100;
    // Precomputed (or otherwise) gradient vectors at each grid node
    public static float[][][] Gradient = new float[IYMAX][IXMAX][2];

    // Function to transition smoothly from 0.0 to 1.0 in the range [0.0, 1.0]
    float smoothstep(float w) {
        if (w <= 0.0) return 0.0f;
        if (w >= 1.0) return 1.0f;
        return w * w * (3.0f - 2.0f * w);
    }

    // Function to interpolate smoothly between a0 and a1
    // Weight w should be in the range [0.0, 1.0]
    float interpolate(float a0, float a1, float w) {
        return a0 + (a1 - a0) * smoothstep(w);
    }

    // Computes the dot product of the distance and gradient vectors.
    float dotGridGradient(int ix, int iy, float x, float y) {


        // Compute the distance vector
        float dx = x - (float)ix;
        float dy = y - (float)iy;

        // Compute the dot-product
        return (dx*Gradient[iy][ix][0] + dy*Gradient[iy][ix][1]);
    }

    // Compute Perlin noise at coordinates x, y
    float perlin(float x, float y) {

        // Determine grid cell coordinates
        int x0 = (int)Math.floor(x);
        int x1 = x0 + 1;
        int y0 = (int)Math.floor(y);
        int y1 = y0 + 1;

        // Determine interpolation weights
        // Could also use higher order polynomial/s-curve here
        float sx = x - (float)x0;
        float sy = y - (float)y0;

        // Interpolate between grid point gradients
        float n0, n1, ix0, ix1, value;
        n0 = dotGridGradient(x0, y0, x, y);
        n1 = dotGridGradient(x1, y0, x, y);
        ix0 = interpolate(n0, n1, sx);
        n0 = dotGridGradient(x0, y1, x, y);
        n1 = dotGridGradient(x1, y1, x, y);
        ix1 = interpolate(n0, n1, sx);
        value = interpolate(ix0, ix1, sy);

        return value;
    }
}
