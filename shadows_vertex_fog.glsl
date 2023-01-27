#version 400 core
layout (location = 0) in vec3 s4j_Vertex;
layout (location = 1) in vec3 s4j_Normal;
layout (location = 2) in vec2 s4j_MultiTexCoord0;
layout (location = 3) in vec4 s4j_Color;
uniform mat4 s4j_ModelViewMatrix;
uniform mat4 s4j_ProjectionMatrix;
uniform mat3 s4j_NormalMatrix;
uniform vec4 s4j_ColorUniform;
uniform bool s4j_PerVertexColor;
uniform vec3 s4j_NormalUniform;
uniform bool s4j_PerVertexNormal;
uniform vec4 s4j_FrontLightModelProduct_sceneColor;
out vec4 shadowCoord0;
out vec4 nearShadowCoord0;
uniform mat4 textureMatrix0;
uniform mat4 nearTextureMatrix0;
uniform mat4 cameraTransform;
out vec3 ecPosition3;
out vec3 fragmentNormal;
out vec3 perVertexColor;
out vec2 texCoord;
out vec4 frontColor;
void main(void) {
  vec4 ecPosition = s4j_ModelViewMatrix * vec4(s4j_Vertex, 1.0);
ecPosition3 = ecPosition.xyz / ecPosition.w;
  vec3 normal3 = s4j_NormalUniform; if(s4j_PerVertexNormal) normal3 = s4j_Normal;
  vec3 normal = normalize(s4j_NormalMatrix * normal3);
vec3 eye = -normalize(ecPosition3);
vec4 ambient;
vec4 diffuse;
vec4 specular;
vec4 accambient = vec4(0.0);
vec4 accdiffuse = vec4(0.0);
vec4 accspecular = vec4(0.0);
vec4 color;
vec4 diffuCol;
  fragmentNormal = normal;
  diffuCol = s4j_ColorUniform; if(s4j_PerVertexColor) diffuCol = s4j_Color;
  color = s4j_FrontLightModelProduct_sceneColor;

  vec4 pos = cameraTransform * ecPosition;

  shadowCoord0 = textureMatrix0 * pos;

  nearShadowCoord0 = nearTextureMatrix0 * pos;

  perVertexColor = vec3(clamp(color.r, 0.0, 1.0), clamp(color.g, 0.0, 1.0), clamp(color.b, 0.0, 1.0));
texCoord = s4j_MultiTexCoord0;
gl_Position = s4j_ProjectionMatrix * s4j_ModelViewMatrix * vec4(s4j_Vertex, 1.0);
frontColor = diffuCol;

}
