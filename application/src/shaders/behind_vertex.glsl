#version 400 core
layout (location = 0) in vec3 s4j_Vertex;
layout (location = 1) in vec3 s4j_Normal;
layout (location = 3) in vec4 s4j_Color;

uniform mat4 s4j_ModelViewMatrix;
uniform mat4 s4j_ProjectionMatrix;
uniform mat3 s4j_NormalMatrix;
uniform vec4 s4j_ColorUniform;
uniform bool s4j_PerVertexColor;
uniform vec4 s4j_FrontLightModelProduct_sceneColor;

out vec4 frontColor;

void main(void)
{
    gl_Position = s4j_ProjectionMatrix * s4j_ModelViewMatrix * vec4(s4j_Vertex, 1.0);//ftransform();
    gl_Position.z = 0.999f * gl_Position.w;
    //gl_Position.w = 1;
    vec4 diffuCol;
    //diffuCol = s4j_FrontLightModelProduct_sceneColor;
    diffuCol = s4j_ColorUniform; if(s4j_PerVertexColor) diffuCol = s4j_Color;

    frontColor = diffuCol;
}
