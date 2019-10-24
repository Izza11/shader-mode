// Simple vertex animation 

uniform mat4 transform;
uniform mat3 normalMatrix;
uniform vec3 lightNormal;

uniform int time; // make sure to pass time in the sketch using set("time", millis())

attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

varying vec4 vertColor;
varying vec3 vertNormal;
varying vec3 vertLightDir;

void main() {
  vec4 v = vec4(position);
  v.x = 50.0*sin(0.01*time);
  gl_Position = transform * v;  
  vertColor = color;
}