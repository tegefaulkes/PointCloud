#version 330 core
out vec4 FragColor;

in float height;
in vec4 position;

uniform float minZ;
uniform float midZ;
uniform float maxZ;

uniform sampler2D tex;

vec3 minColor = vec3(1.0, 0.0, 0.0);
vec3 midColor = vec3(0.0, 1.0, 0.0);
vec3 maxColor = vec3(0.0, 0.0, 1.0);

float normalize(float height, float low, float high) {
//    if (height < low) return 0.0;
//    if (height > high) return 1.0;
    return (height - low) / (high - low);
}

void circleCheck(vec2 pointCoord) {
    if (pow(pointCoord.x - 0.5, 2.0) + pow(pointCoord.y - 0.5, 2) >= 0.25) discard;
}

void main() {

    circleCheck(gl_PointCoord);
    vec3 mixedColor = mix(minColor, midColor, normalize(height, minZ, midZ));
    mixedColor = mix(mixedColor, maxColor, normalize(height, midZ, maxZ));
    FragColor = vec4(mixedColor, 0.5);
}