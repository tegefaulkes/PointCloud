#version 330 core
out vec4 FragColor;

uniform vec3 objectColor;
uniform vec3 lightColor;

//in vec3 ourColor;
//in vec2 TexCoord;

//uniform sampler2D texture1;
//uniform sampler2D texture2;

void main() {
    FragColor = vec4(1.0,0.0,0.0,1.0);//vec4(lightColor * objectColor, 1.0);
}
