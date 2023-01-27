#version 400 core
struct LightSource {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec4 position;
};
struct Fog {
    vec4 color;
    float density;
};
struct FrontMaterial {
    vec4 specular;
    vec4 ambient;
    float shininess;
};
uniform Fog s4j_Fog;
uniform FrontMaterial s4j_FrontMaterial;
layout(location = 0) out vec4 s4j_FragColor;
uniform LightSource s4j_LightSource[5];
const float EPSILON = 3.0E-6;
const float THRESHOLD = 0.9;
const int NB_STEPS = 12;
uniform mat4 cameraTransform;
// ____________________ Begin ShadowLight 0
uniform sampler2D shadowMap0;
uniform sampler2D nearShadowMap0;
uniform float farval0;
uniform float nearval0;
uniform float farvalnear0;
uniform float nearvalnear0;
in vec4 shadowCoord0;
in vec4 nearShadowCoord0;
uniform vec4 lightplane0;
uniform vec4 lightnearplane0;
// ____________________ End ShadowLight
const float DISTRIBUTE_FACTOR = 64.0;

in vec3 ecPosition3;
in vec3 fragmentNormal;
in vec3 perVertexColor;
in vec2 texCoord;
in vec4 frontColor;
uniform sampler2D textureMap0;

uniform int coin_texunit0_model;

uniform int coin_light_model;

uniform int coin_two_sided_lighting;

uniform float maxshadowdistance0;

float VsmLookup(in vec4 map, in float dist, in float epsilon, float bleedthreshold)
{
  float mapdist = map.x;

  // replace 0.0 with some factor > 0.0 to make the light affect even parts in shadow
  float lit_factor = dist <= mapdist ? 1.0 : 0.0;
  float E_x2 = map.y;
  float Ex_2 = mapdist * mapdist;
  float variance = min(max(E_x2 - Ex_2, 0.0) + epsilon, 1.0);

  float m_d = mapdist - dist;
  float p_max = variance / (variance + m_d * m_d);

  p_max *= smoothstep(bleedthreshold, 1.0, p_max);

  return max(lit_factor, p_max);
}

// Mie scaterring approximated with Henyey-Greenstein phase function.
float ComputeScattering(float lightDotView)
{
    float F1_SCATTERING = 0.7;
    float G1_SCATTERING = 0.5;
    float G2_SCATTERING = -0.2;
    float PI = 3.14159265359;
    float result1 = 1.0f - G1_SCATTERING * G1_SCATTERING;
    result1 /= (4.0f * PI * pow(1.0f + G1_SCATTERING * G1_SCATTERING - (2.0f * G1_SCATTERING) *      lightDotView, 1.5f));
    float result2 = 1.0f - G2_SCATTERING * G2_SCATTERING;
    result2 /= (4.0f * PI * pow(1.0f + G2_SCATTERING * G2_SCATTERING - (2.0f * G2_SCATTERING) *      lightDotView, 1.5f));
    return F1_SCATTERING * result1 + (1 - F1_SCATTERING) * result2;
}


void DirectionalLight(in vec3 light_vector,
                      in vec3 eye,
                      in vec3 light_halfVector,
                      in vec3 normal,
                      inout vec4 diffuse,
                      inout vec4 specular)
{
  float nDotVP; // normal . light direction
  float nDotHV; // normal . light half vector
  float pf;     // power factor
  float fZero = 0.2;
  nDotVP = max(0.0, dot(normal, light_vector));
  nDotHV = max(0.0, dot(normal, light_halfVector));

  float shininess = s4j_FrontMaterial.shininess;
  if (nDotVP == 0.0)
    pf = 0.0;
  else
    pf = pow(nDotHV, shininess);

float base = 1-dot(eye, light_halfVector);float exp = pow(base, 5);float fresnel = fZero + (1 - fZero)*exp;  diffuse *= nDotVP;  
  specular *= pf;
  specular *= fresnel;
}


void main(void) {
  vec3 normal = normalize(fragmentNormal);

  if (coin_two_sided_lighting != 0 && !gl_FrontFacing) normal = -normal;

  vec3 eye = -normalize(ecPosition3);

  vec4 ambient = vec4(0.0);
vec4 diffuse = vec4(0.0);
vec4 specular = vec4(0.0);
vec4 mydiffuse = frontColor;
vec4 texcolor = (coin_texunit0_model != 0) ? texture2D(textureMap0, texCoord) : vec4(1.0);

  vec3 color = perVertexColor;
vec3 scolor = vec3(0.0);
float dist;
float neardist;
float shadeFactor;
vec3 coord;
vec3 nearcoord;
vec4 map;
vec4 nearmap;
mydiffuse.a *= texcolor.a;

  // _______________________ Begin ShadowLight 0

  dist = dot(ecPosition3.xyz, lightplane0.xyz) - lightplane0.w;

  neardist = dot(ecPosition3.xyz, lightnearplane0.xyz) - lightnearplane0.w;

  ambient = s4j_LightSource[4].ambient;
diffuse = s4j_LightSource[4].diffuse;
specular = s4j_LightSource[4].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[4].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[4].position))+normalize(eye)), normal, diffuse, specular);
  coord = 0.5 * (shadowCoord0.xyz / shadowCoord0.w + vec3(1.0));

  nearcoord = 0.5 * (nearShadowCoord0.xyz / nearShadowCoord0.w + vec3(1.0));

  map = texture2D(shadowMap0, coord.xy);

  nearmap = texture2D(nearShadowMap0, nearcoord.xy);

  map = (map + vec4(1.0)) * 0.5;

  nearmap = (nearmap + vec4(1.0)) * 0.5;

  map.xy += map.zw / DISTRIBUTE_FACTOR;

  nearmap.xy += nearmap.zw / DISTRIBUTE_FACTOR;

  if(nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0){

  shadeFactor = ((nearmap.x < 0.9999) && (nearShadowCoord0.z > -1.0 && nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0)) ? VsmLookup(nearmap, (neardist - nearvalnear0) / (farvalnear0 - nearvalnear0), EPSILON, THRESHOLD) : 1.0;
}else{

  shadeFactor = ((map.x < 0.9999) && (shadowCoord0.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval0) / (farval0 - nearval0), EPSILON, THRESHOLD) : 1.0;
}

  shadeFactor = 1.0 - shadeFactor;

  shadeFactor *= min(1.0, exp(2.35*ecPosition3.z*abs(ecPosition3.z)/(maxshadowdistance0*maxshadowdistance0)));

  shadeFactor = 1.0 - shadeFactor;

  color += shadeFactor * diffuse.rgb * mydiffuse.rgb;
  scolor += shadeFactor * s4j_FrontMaterial.specular.rgb * specular.rgb;

  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb;

  // ____________________ End ShadowLight

  // _______________________ Begin Light 0

  ambient = s4j_LightSource[0].ambient;
diffuse = s4j_LightSource[0].diffuse;
specular = s4j_LightSource[0].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[0].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[0].position))+normalize(eye)), normal, diffuse, specular);
  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb + diffuse.rgb * mydiffuse.rgb;

  scolor += specular.rgb * s4j_FrontMaterial.specular.rgb;

  // _______________________ End Light

  // _______________________ Begin Light 1

  ambient = s4j_LightSource[1].ambient;
diffuse = s4j_LightSource[1].diffuse;
specular = s4j_LightSource[1].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[1].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[1].position))+normalize(eye)), normal, diffuse, specular);
  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb + diffuse.rgb * mydiffuse.rgb;

  scolor += specular.rgb * s4j_FrontMaterial.specular.rgb;

  // _______________________ End Light

  // _______________________ Begin Light 2

  ambient = s4j_LightSource[2].ambient;
diffuse = s4j_LightSource[2].diffuse;
specular = s4j_LightSource[2].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[2].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[2].position))+normalize(eye)), normal, diffuse, specular);
  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb + diffuse.rgb * mydiffuse.rgb;

  scolor += specular.rgb * s4j_FrontMaterial.specular.rgb;

  // _______________________ End Light

  // _______________________ Begin Light 3

  ambient = s4j_LightSource[3].ambient;
diffuse = s4j_LightSource[3].diffuse;
specular = s4j_LightSource[3].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[3].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[3].position))+normalize(eye)), normal, diffuse, specular);
  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb + diffuse.rgb * mydiffuse.rgb;

  scolor += specular.rgb * s4j_FrontMaterial.specular.rgb;

  // _______________________ End Light

  color = vec3(clamp(color.r, 0.0, 1.0), clamp(color.g, 0.0, 1.0), clamp(color.b, 0.0, 1.0));
  if (coin_light_model != 0) { color *= texcolor.rgb; color += scolor; }
else color = mydiffuse.rgb * texcolor.rgb;

  float fog = exp(-s4j_Fog.density * abs(ecPosition3.z));

  color = mix(s4j_Fog.color.rgb, color, clamp(fog, 0.0, 1.0));

  float noise1 = fract((gl_FragCoord.x*2+gl_FragCoord.y)*0.125f)/256.0f;

  float noise2 = fract((3+gl_FragCoord.x*2+gl_FragCoord.y)*0.125f)/256.0f;

  float noise3 = fract((5+gl_FragCoord.x*2+gl_FragCoord.y)*0.125f)/256.0f;

  color = pow(color,vec3(0.47f))+vec3(noise1,noise2,noise3);

  s4j_FragColor = vec4(color, mydiffuse.a);
}
