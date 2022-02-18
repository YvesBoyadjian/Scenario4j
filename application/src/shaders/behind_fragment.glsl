#version 400 core

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
in vec4 frontColor;
layout(location = 0) out vec4 s4j_FragColor;

void main() {
    s4j_FragColor = frontColor;
}
