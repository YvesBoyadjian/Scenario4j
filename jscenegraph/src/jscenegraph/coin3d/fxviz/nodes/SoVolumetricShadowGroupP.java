package jscenegraph.coin3d.fxviz.nodes;

import jscenegraph.coin3d.inventor.elements.SoEnvironmentElement;
import jscenegraph.coin3d.inventor.misc.SoShaderGenerator;

public class SoVolumetricShadowGroupP extends SoShadowGroupP {

    public SoVolumetricShadowGroupP(SoVolumetricShadowGroup master) {
        super(master);
    }

    private SoVolumetricShadowGroup getMaster() {
        return (SoVolumetricShadowGroup)master;
    }

    protected void startFragmentShader(SoShaderGenerator gen) {

        if(!getMaster().isVolumetricActive.getValue()) {
            return;
        }

        int numshadowlights = this.shadowlights.getLength();

        for(int i=0;i<numshadowlights;i++) {
            gen.addDeclaration("uniform mat4 textureMatrix" + i + ";", false);
        }

        if (numshadowlights != 0) {
            gen.addMainStatement("vec3 g_CameraPosition = vec3(0.0f,0.0f,0.0f);");
            gen.addMainStatement("vec3 endRayPosition = ecPosition3;");
            gen.addMainStatement("mat4 g_ShadowViewProjectionMatrix;");
            gen.addMainStatement("vec3 sunDirection;");
            gen.addMainStatement("vec3 g_SunColor;");

            gen.addMainStatement("vec3 startPosition = g_CameraPosition;");
            gen.addMainStatement("vec3 rayVector = endRayPosition.xyz - startPosition;");
            gen.addMainStatement("float rayLength = length(rayVector);");
            gen.addMainStatement("rayLength = min(rayLength,20000);");
            gen.addMainStatement("vec3 rayDirection = rayVector / rayLength;");
            gen.addMainStatement("rayDirection = normalize(rayDirection);");
            gen.addMainStatement("float stepLength = rayLength / NB_STEPS;");
            gen.addMainStatement("vec3 step = rayDirection * stepLength;");
            gen.addMainStatement("vec3 currentPosition;");
            gen.addMainStatement("vec3 accumFog;");
            gen.addMainStatement("vec3 colorFog = vec3(0.0f,0.0f,0.0f);");
            gen.addMainStatement("vec4 pos;");
        }
    }

    protected void endShadowLight(SoShaderGenerator gen,int index,int texunit,int shadowlightnumber) {

        if(!getMaster().isVolumetricActive.getValue()) {
            return;
        }
        gen.addMainStatement("sunDirection = normalize(vec3(s4j_LightSource["+index+"].position));");
        gen.addMainStatement("g_SunColor = s4j_LightSource["+index+"].diffuse.rgb;");
        //gen.addMainStatement("g_ShadowViewProjectionMatrix = gl_TextureMatrix["+texunit+"];");
        gen.addMainStatement("g_ShadowViewProjectionMatrix = textureMatrix"+shadowlightnumber+";");

        gen.addMainStatement("currentPosition = startPosition;");
        gen.addMainStatement("accumFog = vec3(0.0f,0.0f,0.0f);");
        gen.addMainStatement("for (int i = 0; i < NB_STEPS; i++)");
        gen.addMainStatement("{");
        gen.addMainStatement("  dist = dot(currentPosition.xyz, lightplane"+shadowlightnumber+".xyz) - lightplane"+shadowlightnumber+".w;");
        gen.addMainStatement("  pos = cameraTransform * vec4(currentPosition.xyz,1);");
        gen.addMainStatement("  vec4 worldInShadowCameraSpace = g_ShadowViewProjectionMatrix * pos;");
        gen.addMainStatement("  coord = 0.5 * (worldInShadowCameraSpace.xyz/worldInShadowCameraSpace.w + vec3(1.0));");
        gen.addMainStatement("  map = texture2D(shadowMap"+shadowlightnumber+", coord.xy);");
        gen.addMainStatement("  map = (map + vec4(1.0)) * 0.5;");
        gen.addMainStatement("  map.xy += map.zw / DISTRIBUTE_FACTOR;");
        gen.addMainStatement("  shadeFactor = ((map.x < 0.9999) && (worldInShadowCameraSpace.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval"+shadowlightnumber+") / (farval"+shadowlightnumber+" - nearval"+shadowlightnumber+"), EPSILON, THRESHOLD) : 1.0;");
        //gen.addMainStatement("  shadeFactor = 1.0 - shadeFactor;");
        //gen.addMainStatement("  shadeFactor *= min(1.0, exp(2.35*currentPosition.z*abs(currentPosition.z)/(maxshadowdistance"+shadowlightnumber+"*maxshadowdistance"+shadowlightnumber+")));");
        //gen.addMainStatement("  shadeFactor = 1.0 - shadeFactor;");
        //https://www.youtube.com/watch?v=DxfEbulyFcY
        final float RED_WAVELENGTH = 700;//650;
        final float GREEN_WAVELENGTH = 530;//550;
        final float BLUE_WAVELENGTH = 440;//440;
        final float SCATTER_R = (float)Math.pow(400.0/RED_WAVELENGTH,4.0);
        final float SCATTER_G = (float)Math.pow(400.0/GREEN_WAVELENGTH,4.0);
        final float SCATTER_B = (float)Math.pow(400.0/BLUE_WAVELENGTH,4.0);
        final String RED_FACTOR = Float.toString(SCATTER_R);
        final String GREEN_FACTOR = Float.toString(SCATTER_G);
        final String BLUE_FACTOR = Float.toString(SCATTER_B);
        gen.addMainStatement("  {");
        gen.addMainStatement("    float cosinusRaySun = dot(rayDirection, sunDirection);");
        gen.addMainStatement("    float scatter = ComputeScattering(cosinusRaySun);");
        gen.addMainStatement("    scatter = scatter * shadeFactor;");
        gen.addMainStatement("    accumFog += vec3(scatter,scatter,scatter) * g_SunColor * 2.4;");
        gen.addMainStatement("    scatter = ComputeRayleighScattering(cosinusRaySun);");
        gen.addMainStatement("    accumFog += vec3("+RED_FACTOR+","+GREEN_FACTOR+","+BLUE_FACTOR+") * g_SunColor * shadeFactor;");
        gen.addMainStatement("  }");
        gen.addMainStatement("  currentPosition += step;");
        gen.addMainStatement("}");
        gen.addMainStatement("accumFog /= NB_STEPS;");
        gen.addMainStatement("colorFog += accumFog * rayLength * s4j_Fog.density;\n");
    }

    protected void endFragmentShader(SoShaderGenerator gen, SoEnvironmentElement.FogType fogType) {

        if(!getMaster().isVolumetricActive.getValue()) {
            super.endFragmentShader(gen,fogType);
            return;
        }

        switch (fogType) {
            case NONE:
                break;
            case HAZE:
                break;
            case FOG:
                gen.addMainStatement("float fog = exp(-s4j_Fog.density * abs(ecPosition3.z));");
                gen.addMainStatement("color = mix(s4j_Fog.color.rgb, color, clamp(fog, 0.0, 1.0));");
                //gen.addMainStatement("color = color * clamp(fog, 0.0, 1.0);");
                gen.addMainStatement("color += colorFog;");
                break;
            case SMOKE:
                break;
        }
    }

    protected void postFragmentShaderShadowLight(int i) {

        if(!getMaster().isVolumetricActive.getValue()) {
            return;
        }

        this.fragmentshader.parameter.set1Value(this.fragmentshader.parameter.getNum(), this.texturematrix[i]);
    }
}
