#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 vertColor;
varying vec3 vertNormal;

void main() {
  gl_FragColor = vec4(vertNormal,1); // assigns the final pixel color
}
