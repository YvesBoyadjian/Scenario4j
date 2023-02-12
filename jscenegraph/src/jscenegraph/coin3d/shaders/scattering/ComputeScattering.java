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
}
