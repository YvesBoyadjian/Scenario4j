#version 400 core
layout (location = 0) in vec3 s4j_Vertex;
layout (location = 1) in vec3 s4j_Normal;
layout (location = 2) in vec4 s4j_Color;
layout (location = 3) in vec2 s4j_MultiTexCoord0;
uniform mat4 s4j_ModelViewMatrix;
uniform mat4 s4j_ProjectionMatrix;
uniform mat3 s4j_NormalMatrix;
uniform vec4 s4j_ColorUniform;
uniform bool s4j_PerVertexColor;
uniform vec3 s4j_NormalUniform;
uniform bool s4j_PerVertexNormal;
uniform vec4 s4j_FrontLightModelProduct_sceneColor;
uniform bool s4j_FromXYUV;
uniform vec4 s4j_XYUV;
out vec4 shadowCoord0;
out vec4 nearShadowCoord0;
uniform mat4 textureMatrix0;
uniform mat4 nearTextureMatrix0;
out vec4 shadowCoord1;
out vec4 nearShadowCoord1;
uniform mat4 textureMatrix1;
uniform mat4 nearTextureMatrix1;
out vec4 shadowCoord2;
out vec4 nearShadowCoord2;
uniform mat4 textureMatrix2;
uniform mat4 nearTextureMatrix2;
out vec4 shadowCoord3;
out vec4 nearShadowCoord3;
uniform mat4 textureMatrix3;
uniform mat4 nearTextureMatrix3;
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

  shadowCoord1 = textureMatrix1 * pos;

  nearShadowCoord1 = nearTextureMatrix1 * pos;

  shadowCoord2 = textureMatrix2 * pos;

  nearShadowCoord2 = nearTextureMatrix2 * pos;

  shadowCoord3 = textureMatrix3 * pos;

  nearShadowCoord3 = nearTextureMatrix3 * pos;

  perVertexColor = vec3(clamp(color.r, 0.0, 1.0), clamp(color.g, 0.0, 1.0), clamp(color.b, 0.0, 1.0));
if (s4j_FromXYUV) {
	texCoord = vec2((s4j_Vertex.x - s4j_XYUV.x) * s4j_XYUV.z, (s4j_Vertex.y - s4j_XYUV.y) * s4j_XYUV.w);
}
else {
	texCoord = s4j_MultiTexCoord0;
}
gl_Position = s4j_ProjectionMatrix * s4j_ModelViewMatrix * vec4(s4j_Vertex, 1.0);
frontColor = diffuCol;

}
