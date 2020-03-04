#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif


uniform sampler2D texture;
uniform float contrast;


varying vec4 vertTexCoord;

void main() {


	vec3 color = vec3(texture2D(texture, vertTexCoord.st));
    const vec3 LumCoeff = vec3(0.2125, 0.7154, 0.0721);

    vec3 AvgLumin = vec3(0.5, 0.5, 0.5);

    vec3 intensity = vec3(dot(color, LumCoeff));

    // could substitute a uniform for this 1. and have variable saturation
    vec3 satColor = mix(intensity, color, 1.);
    vec3 conColor = mix(AvgLumin, satColor, contrast);

    gl_FragColor = vec4(conColor, 1); // assigns the final pixel color

    // Challenge 1:
    // Could you implement a filter that inverts the color of the image?

    // Challenge 2:
    // Could you do the above using the vertical motion of your mouse?
}