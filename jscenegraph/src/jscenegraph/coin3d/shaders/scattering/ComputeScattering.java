package jscenegraph.coin3d.shaders.scattering;

public class ComputeScattering {

    /**
     *
     */
    public ComputeScattering() {
    }

    //https://web.gps.caltech.edu/~vijay/Papers/Polarisation/hansen-69b.pdf

    public static final String COMPUTE_MIE_SCATTERING_shadersource =
        "// Mie scaterring approximated with Henyey-Greenstein phase function.\n"+
        "float ComputeScattering(float lightDotView)\n"+
        "{\n"+
                "    float F1_SCATTERING = 0.8;\n"+
                "    float G1_SCATTERING = 0.6;\n"+
                "    float G2_SCATTERING = -0.2;\n"+
                "    float PI = 3.14159265359;\n"+
        "    float result1 = 1.0f - G1_SCATTERING * G1_SCATTERING;\n"+
        "    result1 /= (4.0f * PI * pow(1.0f + G1_SCATTERING * G1_SCATTERING - (2.0f * G1_SCATTERING) *      lightDotView, 1.5f));\n"+
        "    float result2 = 1.0f - G2_SCATTERING * G2_SCATTERING;\n"+
        "    result2 /= (4.0f * PI * pow(1.0f + G2_SCATTERING * G2_SCATTERING - (2.0f * G2_SCATTERING) *      lightDotView, 1.5f));\n"+
        "    return F1_SCATTERING * result1 + (1 - F1_SCATTERING) * result2;\n"+
        "}\n";

    public static final String COMPUTE_RAYLEIGH_SCATTERING_shadersource =
            "// Rayleigh scattering\n"+
            "float ComputeRayleighScattering(float lightDotView)\n"+
            "{\n"+
                    "    return 0.75 * ( 1.0 + lightDotView * lightDotView);\n"+
            "}\n";

    //https://www.youtube.com/watch?v=DxfEbulyFcY
    public static final String RAY_SPHERE_shadersource =
    "// Returns vector (dstToSphere, dstThroughSphere)\n"+
    "// If ray origin is inside sphere, dstToSphere = 0\n"+
    "// If ray misses sphere, dstToSphere = maxValue; dstThroughSphere = 0\n"+
    "vec2 raySphere(vec3 sphereCentre, float sphereRadius, vec3 rayOrigin, vec3 rayDir) {\n"+
    "  vec3 offset = rayOrigin - sphereCentre;\n"+
    "  float a = 1; // Set to dot(rayDir, rayDir) if rayDir might not be mormalized\n"+
    "  float b = 2 * dot(offset, rayDir);\n"+
    "  float c = dot (offset, offset) - sphereRadius * sphereRadius;\n"+
    "  float d = b * b - 4 * a * c; // Discriminant from quadratic formula\n"+
    "  // Number of intersections: 0 when d < 0; 1 when d = 0; 2 when d > 0\n"+
    "  if (d > 0) {\n"+
            "    float s = sqrt(d);\n"+
            "    float dstToSphereNear = max(0, (-b - s) / (2 * a));\n"+
            "    float dstToSphereFar = (-b + s) / (2 * a);\n"+
            "    // Ignore intersections that occur behind the ray\n"+
            "    if (dstToSphereFar >= 0) {\n"+
            "      return vec2(dstToSphereNear, dstToSphereFar - dstToSphereNear);\n"+
            "    }\n"+
    "  }\n"+
            "  //Ray did not instersect sphere\n"+
            "  return vec2(3.4e38, 0);\n"+
    "}\n"
    ;
}
