#version 330 core
layout (location = 0) in vec3 aPos;

out float height;
out vec4 position;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main(){
    float pointSize = 7.0;
    vec4 tempPos = projection * view * model * vec4(aPos, 1.0);
    gl_Position = tempPos;
    height = aPos.z;
    position = tempPos;
    gl_PointSize = clamp(mix(0.5, pointSize, 1.0 - tempPos.z / 1000.0), 0.5, pointSize);
}

