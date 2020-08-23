#version 330 core
out vec4 FragColor;

in float height;
in vec4 position;

uniform float minZ;
uniform float midZ;
uniform float maxZ;

vec3 minColor = vec3(1.0, 0.0, 0.0);
vec3 midColor = vec3(0.0, 1.0, 0.0);
vec3 maxColor = vec3(0.0, 0.0, 1.0);

float normalize(float height, float low, float high) {
//    if (height < low) return 0.0;
//    if (height > high) return 1.0;
    return (height - low) / (high - low);
}

void main() {
    vec3 mixedColor = mix(minColor, midColor, normalize(height, minZ, midZ));
    mixedColor = mix(mixedColor, maxColor, normalize(height, midZ, maxZ));
    FragColor = vec4(mixedColor, 0.5);//vec4(lightColor * objectColor, 1.0);
//    FragColor = vec4(position.xy, 0.0, 1.0);
}