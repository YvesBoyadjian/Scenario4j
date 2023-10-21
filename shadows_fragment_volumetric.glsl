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
uniform LightSource s4j_LightSource[8];
const float EPSILON = 5.0E-5;
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
// ____________________ Begin ShadowLight 1
uniform sampler2D shadowMap1;
uniform sampler2D nearShadowMap1;
uniform float farval1;
uniform float nearval1;
uniform float farvalnear1;
uniform float nearvalnear1;
in vec4 shadowCoord1;
in vec4 nearShadowCoord1;
uniform vec4 lightplane1;
uniform vec4 lightnearplane1;
// ____________________ End ShadowLight
// ____________________ Begin ShadowLight 2
uniform sampler2D shadowMap2;
uniform sampler2D nearShadowMap2;
uniform float farval2;
uniform float nearval2;
uniform float farvalnear2;
uniform float nearvalnear2;
in vec4 shadowCoord2;
in vec4 nearShadowCoord2;
uniform vec4 lightplane2;
uniform vec4 lightnearplane2;
// ____________________ End ShadowLight
// ____________________ Begin ShadowLight 3
uniform sampler2D shadowMap3;
uniform sampler2D nearShadowMap3;
uniform float farval3;
uniform float nearval3;
uniform float farvalnear3;
uniform float nearvalnear3;
in vec4 shadowCoord3;
in vec4 nearShadowCoord3;
uniform vec4 lightplane3;
uniform vec4 lightnearplane3;
// ____________________ End ShadowLight
const float DISTRIBUTE_FACTOR = 64.0;

in vec3 ecPosition3;
in vec3 fragmentNormal;
in vec3 perVertexColor;
in vec2 texCoord;
in vec4 frontColor;
uniform mat4 textureMatrix0;
uniform mat4 textureMatrix1;
uniform mat4 textureMatrix2;
uniform mat4 textureMatrix3;
uniform sampler2D textureMap0;

uniform int coin_texunit0_model;

uniform int coin_light_model;

uniform int coin_two_sided_lighting;

uniform float maxshadowdistance0;

uniform float maxshadowdistance1;

uniform float maxshadowdistance2;

uniform float maxshadowdistance3;

float VsmLookup(in float map, in float dist, in float epsilon, float bleedthreshold)
{
  float mapdist = map;

  // replace 0.0 with some factor > 0.0 to make the light affect even parts in shadow
  float lit_factor = dist <= mapdist ? 1.0 : 0.0;
  float variance = min(epsilon, 1.0);

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

// Rayleigh scattering
float ComputeRayleighScattering(float lightDotView)
{
    return 0.75 * ( 1.0 + lightDotView * lightDotView);
}

// Returns vector (dstToSphere, dstThroughSphere)
// If ray origin is inside sphere, dstToSphere = 0
// If ray misses sphere, dstToSphere = maxValue; dstThroughSphere = 0
vec2 raySphere(vec3 sphereCentre, float sphereRadius, vec3 rayOrigin, vec3 rayDir) {
  vec3 offset = rayOrigin - sphereCentre;
  float a = 1; // Set to dot(rayDir, rayDir) if rayDir might not be mormalized
  float b = 2 * dot(offset, rayDir);
  float c = dot (offset, offset) - sphereRadius * sphereRadius;
  float d = b * b - 4 * a * c; // Discriminant from quadratic formula
  // Number of intersections: 0 when d < 0; 1 when d = 0; 2 when d > 0
  if (d > 0) {
    float s = sqrt(d);
    float dstToSphereNear = max(0, (-b - s) / (2 * a));
    float dstToSphereFar = (-b + s) / (2 * a);
    // Ignore intersections that occur behind the ray
    if (dstToSphereFar >= 0) {
      return vec2(dstToSphereNear, dstToSphereFar - dstToSphereNear);
    }
  }
  //Ray did not instersect sphere
  return vec2(3.4e38, 0);
}

float densityAtPoint(vec3 densitySamplePoint) {
  vec3 planetCentre = vec3(0,0,-6371e3);
  float planetRadius = 6371e3;
  float atmosphereRadius = 1e4;
  float densityFalloff = 1;
  float heightAboveSurface = length(densitySamplePoint - planetCentre) - planetRadius;
  float height01 = heightAboveSurface / (atmosphereRadius - planetRadius);
  float localDensity = exp(-height01 * densityFalloff) * (1 - height01);
  return localDensity;
}

float opticalDepth(vec3 rayOrigin, vec3 rayDir, float rayLength) {
  int numOpticalDepthPoints = 10;
  vec3 densitySamplePoint = rayOrigin;
  float stepSize = rayLength / (numOpticalDepthPoints - 1);
  float opticalDepth = 0;
  for (int i = 0; i < numOpticalDepthPoints; i ++) {
    float localDensity = densityAtPoint(densitySamplePoint);
    opticalDepth += localDensity * stepSize;
    densitySamplePoint += rayDir * stepSize;
  }
  return opticalDepth;
}

vec3 calculateLight(vec3 rayOrigin, vec3 rayDir, float rayLength, vec3 originalCol, vec3 dirToSun, vec3 scatteringCoefficients) {
  int numInScatteringPoints = 10;
  vec3 planetCentre = vec3(0,0,-6371e3);
  float atmosphereRadius = 1e4;
  vec3 inScatterPoint = rayOrigin;
  float stepSize = rayLength / (numInScatteringPoints - 1);
  vec3 inScatteredLight = vec3(0,0,0);
  float viewRayOpticalDepth = 0;
  for (int i = 0; i < numInScatteringPoints; i ++) {
    float sunRayLength = raySphere(planetCentre, atmosphereRadius, inScatterPoint, dirToSun).y;
    float sunRayOpticalDepth = opticalDepth(inScatterPoint, dirToSun, sunRayLength);
    viewRayOpticalDepth = opticalDepth(inScatterPoint, -rayDir, stepSize * i);
    vec3 transmittance = exp(-(sunRayOpticalDepth + viewRayOpticalDepth) * scatteringCoefficients);
    float localDensity = densityAtPoint(inScatterPoint);
    inScatteredLight += localDensity * transmittance * scatteringCoefficients * stepSize;
    inScatterPoint += rayDir * stepSize;
  }
  float originalColTransmittance = exp(-viewRayOpticalDepth);
  return originalCol * originalColTransmittance + inScatteredLight;
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
float map;
float nearmap;
mydiffuse.a *= texcolor.a;

  vec3 g_CameraPosition = vec3(0.0f,0.0f,0.0f);
  vec3 endRayPosition = ecPosition3;
  mat4 g_ShadowViewProjectionMatrix;
  vec3 sunDirection;
  vec3 g_SunColor;
  vec3 startPosition = g_CameraPosition;
  vec3 rayVector = endRayPosition.xyz - startPosition;
  float rayLength = length(rayVector);
  rayLength = min(rayLength,20000);
  vec3 rayDirection = rayVector / rayLength;
  rayDirection = normalize(rayDirection);
  float stepLength = rayLength / NB_STEPS;
  vec3 step = rayDirection * stepLength;
  vec3 currentPosition;
  vec3 accumFog;
  vec3 colorFog = vec3(0.0f,0.0f,0.0f);
  vec4 pos;
  // _______________________ Begin ShadowLight 0

  dist = dot(ecPosition3.xyz, lightplane0.xyz) - lightplane0.w;

  neardist = dot(ecPosition3.xyz, lightnearplane0.xyz) - lightnearplane0.w;

  ambient = s4j_LightSource[4].ambient;
diffuse = s4j_LightSource[4].diffuse;
specular = s4j_LightSource[4].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[4].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[4].position))+normalize(eye)), normal, diffuse, specular);
  coord = 0.5 * (shadowCoord0.xyz / shadowCoord0.w + vec3(1.0));

  nearcoord = 0.5 * (nearShadowCoord0.xyz / nearShadowCoord0.w + vec3(1.0));

  map = float(texture2D(shadowMap0, coord.xy));

  nearmap = float(texture2D(nearShadowMap0, nearcoord.xy));

  map = (map + 1.0) * 0.5;

  nearmap = (nearmap + 1.0) * 0.5;

  if(nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0){

  shadeFactor = ((nearmap < 0.9999) && (nearShadowCoord0.z > -1.0 && nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0)) ? VsmLookup(nearmap, (neardist - nearvalnear0) / (farvalnear0 - nearvalnear0), EPSILON, THRESHOLD) : 1.0;
}else{

  shadeFactor = ((map < 0.9999) && (shadowCoord0.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval0) / (farval0 - nearval0), EPSILON, THRESHOLD) : 1.0;
}

  shadeFactor = 1.0 - shadeFactor;

  shadeFactor *= min(1.0, exp(2.35*ecPosition3.z*abs(ecPosition3.z)/(maxshadowdistance0*maxshadowdistance0)));

  shadeFactor = 1.0 - shadeFactor;

  color += shadeFactor * diffuse.rgb * mydiffuse.rgb;
  scolor += shadeFactor * s4j_FrontMaterial.specular.rgb * specular.rgb;

  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb;

  sunDirection = normalize(vec3(s4j_LightSource[4].position));
  g_SunColor = s4j_LightSource[4].diffuse.rgb;
  g_ShadowViewProjectionMatrix = textureMatrix0;
  currentPosition = startPosition;
  accumFog = vec3(0.0f,0.0f,0.0f);
  for (int i = 0; i < NB_STEPS; i++)
  {
    dist = dot(currentPosition.xyz, lightplane0.xyz) - lightplane0.w;
    pos = cameraTransform * vec4(currentPosition.xyz,1);
    vec4 worldInShadowCameraSpace = g_ShadowViewProjectionMatrix * pos;
    coord = 0.5 * (worldInShadowCameraSpace.xyz/worldInShadowCameraSpace.w + vec3(1.0));
    map = float(texture2D(shadowMap0, coord.xy));
    map = (map + 1.0) * 0.5;
    shadeFactor = ((map < 0.9999) && (worldInShadowCameraSpace.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval0) / (farval0 - nearval0), EPSILON, THRESHOLD) : 1.0;
    {
      float cosinusRaySun = dot(rayDirection, sunDirection);
      float scatter = ComputeScattering(cosinusRaySun);
      scatter = scatter * shadeFactor;
      accumFog += vec3(scatter,scatter,scatter) * g_SunColor * 2.4;
      scatter = ComputeRayleighScattering(cosinusRaySun);
      float density = densityAtPoint(pos.xyz);
      accumFog += vec3(0.10662224,0.32444155,0.68301344) * g_SunColor * shadeFactor * density * 0.5;
    }
    currentPosition += step;
  }
  accumFog /= NB_STEPS;
  colorFog += accumFog * rayLength * s4j_Fog.density;

  // ____________________ End ShadowLight

  // _______________________ Begin ShadowLight 1

  dist = dot(ecPosition3.xyz, lightplane1.xyz) - lightplane1.w;

  neardist = dot(ecPosition3.xyz, lightnearplane1.xyz) - lightnearplane1.w;

  ambient = s4j_LightSource[5].ambient;
diffuse = s4j_LightSource[5].diffuse;
specular = s4j_LightSource[5].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[5].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[5].position))+normalize(eye)), normal, diffuse, specular);
  coord = 0.5 * (shadowCoord1.xyz / shadowCoord1.w + vec3(1.0));

  nearcoord = 0.5 * (nearShadowCoord1.xyz / nearShadowCoord1.w + vec3(1.0));

  map = float(texture2D(shadowMap1, coord.xy));

  nearmap = float(texture2D(nearShadowMap1, nearcoord.xy));

  map = (map + 1.0) * 0.5;

  nearmap = (nearmap + 1.0) * 0.5;

  if(nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0){

  shadeFactor = ((nearmap < 0.9999) && (nearShadowCoord1.z > -1.0 && nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0)) ? VsmLookup(nearmap, (neardist - nearvalnear1) / (farvalnear1 - nearvalnear1), EPSILON, THRESHOLD) : 1.0;
}else{

  shadeFactor = ((map < 0.9999) && (shadowCoord1.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval1) / (farval1 - nearval1), EPSILON, THRESHOLD) : 1.0;
}

  shadeFactor = 1.0 - shadeFactor;

  shadeFactor *= min(1.0, exp(2.35*ecPosition3.z*abs(ecPosition3.z)/(maxshadowdistance1*maxshadowdistance1)));

  shadeFactor = 1.0 - shadeFactor;

  color += shadeFactor * diffuse.rgb * mydiffuse.rgb;
  scolor += shadeFactor * s4j_FrontMaterial.specular.rgb * specular.rgb;

  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb;

  sunDirection = normalize(vec3(s4j_LightSource[5].position));
  g_SunColor = s4j_LightSource[5].diffuse.rgb;
  g_ShadowViewProjectionMatrix = textureMatrix1;
  currentPosition = startPosition;
  accumFog = vec3(0.0f,0.0f,0.0f);
  for (int i = 0; i < NB_STEPS; i++)
  {
    dist = dot(currentPosition.xyz, lightplane1.xyz) - lightplane1.w;
    pos = cameraTransform * vec4(currentPosition.xyz,1);
    vec4 worldInShadowCameraSpace = g_ShadowViewProjectionMatrix * pos;
    coord = 0.5 * (worldInShadowCameraSpace.xyz/worldInShadowCameraSpace.w + vec3(1.0));
    map = float(texture2D(shadowMap1, coord.xy));
    map = (map + 1.0) * 0.5;
    shadeFactor = ((map < 0.9999) && (worldInShadowCameraSpace.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval1) / (farval1 - nearval1), EPSILON, THRESHOLD) : 1.0;
    {
      float cosinusRaySun = dot(rayDirection, sunDirection);
      float scatter = ComputeScattering(cosinusRaySun);
      scatter = scatter * shadeFactor;
      accumFog += vec3(scatter,scatter,scatter) * g_SunColor * 2.4;
      scatter = ComputeRayleighScattering(cosinusRaySun);
      float density = densityAtPoint(pos.xyz);
      accumFog += vec3(0.10662224,0.32444155,0.68301344) * g_SunColor * shadeFactor * density * 0.5;
    }
    currentPosition += step;
  }
  accumFog /= NB_STEPS;
  colorFog += accumFog * rayLength * s4j_Fog.density;

  // ____________________ End ShadowLight

  // _______________________ Begin ShadowLight 2

  dist = dot(ecPosition3.xyz, lightplane2.xyz) - lightplane2.w;

  neardist = dot(ecPosition3.xyz, lightnearplane2.xyz) - lightnearplane2.w;

  ambient = s4j_LightSource[6].ambient;
diffuse = s4j_LightSource[6].diffuse;
specular = s4j_LightSource[6].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[6].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[6].position))+normalize(eye)), normal, diffuse, specular);
  coord = 0.5 * (shadowCoord2.xyz / shadowCoord2.w + vec3(1.0));

  nearcoord = 0.5 * (nearShadowCoord2.xyz / nearShadowCoord2.w + vec3(1.0));

  map = float(texture2D(shadowMap2, coord.xy));

  nearmap = float(texture2D(nearShadowMap2, nearcoord.xy));

  map = (map + 1.0) * 0.5;

  nearmap = (nearmap + 1.0) * 0.5;

  if(nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0){

  shadeFactor = ((nearmap < 0.9999) && (nearShadowCoord2.z > -1.0 && nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0)) ? VsmLookup(nearmap, (neardist - nearvalnear2) / (farvalnear2 - nearvalnear2), EPSILON, THRESHOLD) : 1.0;
}else{

  shadeFactor = ((map < 0.9999) && (shadowCoord2.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval2) / (farval2 - nearval2), EPSILON, THRESHOLD) : 1.0;
}

  shadeFactor = 1.0 - shadeFactor;

  shadeFactor *= min(1.0, exp(2.35*ecPosition3.z*abs(ecPosition3.z)/(maxshadowdistance2*maxshadowdistance2)));

  shadeFactor = 1.0 - shadeFactor;

  color += shadeFactor * diffuse.rgb * mydiffuse.rgb;
  scolor += shadeFactor * s4j_FrontMaterial.specular.rgb * specular.rgb;

  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb;

  sunDirection = normalize(vec3(s4j_LightSource[6].position));
  g_SunColor = s4j_LightSource[6].diffuse.rgb;
  g_ShadowViewProjectionMatrix = textureMatrix2;
  currentPosition = startPosition;
  accumFog = vec3(0.0f,0.0f,0.0f);
  for (int i = 0; i < NB_STEPS; i++)
  {
    dist = dot(currentPosition.xyz, lightplane2.xyz) - lightplane2.w;
    pos = cameraTransform * vec4(currentPosition.xyz,1);
    vec4 worldInShadowCameraSpace = g_ShadowViewProjectionMatrix * pos;
    coord = 0.5 * (worldInShadowCameraSpace.xyz/worldInShadowCameraSpace.w + vec3(1.0));
    map = float(texture2D(shadowMap2, coord.xy));
    map = (map + 1.0) * 0.5;
    shadeFactor = ((map < 0.9999) && (worldInShadowCameraSpace.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval2) / (farval2 - nearval2), EPSILON, THRESHOLD) : 1.0;
    {
      float cosinusRaySun = dot(rayDirection, sunDirection);
      float scatter = ComputeScattering(cosinusRaySun);
      scatter = scatter * shadeFactor;
      accumFog += vec3(scatter,scatter,scatter) * g_SunColor * 2.4;
      scatter = ComputeRayleighScattering(cosinusRaySun);
      float density = densityAtPoint(pos.xyz);
      accumFog += vec3(0.10662224,0.32444155,0.68301344) * g_SunColor * shadeFactor * density * 0.5;
    }
    currentPosition += step;
  }
  accumFog /= NB_STEPS;
  colorFog += accumFog * rayLength * s4j_Fog.density;

  // ____________________ End ShadowLight

  // _______________________ Begin ShadowLight 3

  dist = dot(ecPosition3.xyz, lightplane3.xyz) - lightplane3.w;

  neardist = dot(ecPosition3.xyz, lightnearplane3.xyz) - lightnearplane3.w;

  ambient = s4j_LightSource[7].ambient;
diffuse = s4j_LightSource[7].diffuse;
specular = s4j_LightSource[7].specular;

  DirectionalLight(normalize(vec3(s4j_LightSource[7].position)),normalize(eye),normalize(normalize(vec3(s4j_LightSource[7].position))+normalize(eye)), normal, diffuse, specular);
  coord = 0.5 * (shadowCoord3.xyz / shadowCoord3.w + vec3(1.0));

  nearcoord = 0.5 * (nearShadowCoord3.xyz / nearShadowCoord3.w + vec3(1.0));

  map = float(texture2D(shadowMap3, coord.xy));

  nearmap = float(texture2D(nearShadowMap3, nearcoord.xy));

  map = (map + 1.0) * 0.5;

  nearmap = (nearmap + 1.0) * 0.5;

  if(nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0){

  shadeFactor = ((nearmap < 0.9999) && (nearShadowCoord3.z > -1.0 && nearcoord.x >= 0.0 && nearcoord.x <= 1.0 && nearcoord.y >= 0.0 && nearcoord.y <= 1.0)) ? VsmLookup(nearmap, (neardist - nearvalnear3) / (farvalnear3 - nearvalnear3), EPSILON, THRESHOLD) : 1.0;
}else{

  shadeFactor = ((map < 0.9999) && (shadowCoord3.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval3) / (farval3 - nearval3), EPSILON, THRESHOLD) : 1.0;
}

  shadeFactor = 1.0 - shadeFactor;

  shadeFactor *= min(1.0, exp(2.35*ecPosition3.z*abs(ecPosition3.z)/(maxshadowdistance3*maxshadowdistance3)));

  shadeFactor = 1.0 - shadeFactor;

  color += shadeFactor * diffuse.rgb * mydiffuse.rgb;
  scolor += shadeFactor * s4j_FrontMaterial.specular.rgb * specular.rgb;

  color += ambient.rgb * s4j_FrontMaterial.ambient.rgb;

  sunDirection = normalize(vec3(s4j_LightSource[7].position));
  g_SunColor = s4j_LightSource[7].diffuse.rgb;
  g_ShadowViewProjectionMatrix = textureMatrix3;
  currentPosition = startPosition;
  accumFog = vec3(0.0f,0.0f,0.0f);
  for (int i = 0; i < NB_STEPS; i++)
  {
    dist = dot(currentPosition.xyz, lightplane3.xyz) - lightplane3.w;
    pos = cameraTransform * vec4(currentPosition.xyz,1);
    vec4 worldInShadowCameraSpace = g_ShadowViewProjectionMatrix * pos;
    coord = 0.5 * (worldInShadowCameraSpace.xyz/worldInShadowCameraSpace.w + vec3(1.0));
    map = float(texture2D(shadowMap3, coord.xy));
    map = (map + 1.0) * 0.5;
    shadeFactor = ((map < 0.9999) && (worldInShadowCameraSpace.z > -1.0 && coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0)) ? VsmLookup(map, (dist - nearval3) / (farval3 - nearval3), EPSILON, THRESHOLD) : 1.0;
    {
      float cosinusRaySun = dot(rayDirection, sunDirection);
      float scatter = ComputeScattering(cosinusRaySun);
      scatter = scatter * shadeFactor;
      accumFog += vec3(scatter,scatter,scatter) * g_SunColor * 2.4;
      scatter = ComputeRayleighScattering(cosinusRaySun);
      float density = densityAtPoint(pos.xyz);
      accumFog += vec3(0.10662224,0.32444155,0.68301344) * g_SunColor * shadeFactor * density * 0.5;
    }
    currentPosition += step;
  }
  accumFog /= NB_STEPS;
  colorFog += accumFog * rayLength * s4j_Fog.density;

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

  if (coin_light_model != 0) { color *= texcolor.rgb; color += scolor; }
else color = mydiffuse.rgb * texcolor.rgb;

  float fog = exp(-s4j_Fog.density * abs(ecPosition3.z));
  color = mix(s4j_Fog.color.rgb, color, clamp(fog, 0.0, 1.0));
  color += colorFog;
  float noise1 = fract((gl_FragCoord.x*2+gl_FragCoord.y)*0.125f)/256.0f;

  float noise2 = fract((3+gl_FragCoord.x*2+gl_FragCoord.y)*0.125f)/256.0f;

  float noise3 = fract((5+gl_FragCoord.x*2+gl_FragCoord.y)*0.125f)/256.0f;

  color = vec3(clamp(color.r, 0.0, 1.0), clamp(color.g, 0.0, 1.0), clamp(color.b, 0.0, 1.0));
  color = pow(color,vec3(0.46f))+vec3(noise1,noise2,noise3);

  s4j_FragColor = vec4(color, mydiffuse.a);
}
