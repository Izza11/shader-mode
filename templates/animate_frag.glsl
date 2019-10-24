#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 vertColor;
varying vec3 vertNormal;
varying vec3 vertLightDir;

void main() {  
  gl_FragColor = vertColor;
}