#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 vertColor;
attribute vec2 gl_FragCoord;

void main() {
    vec2 xy = gl_FragCoord.xy; //We obtain our coordinates for the current pixel
    vec4 solidRed = vec4(0,0.0,0.0,1.0);//This is actually black right now
    if(xy.x > 300.0){//Arbitrary number, we don't know how big our screen is!
        solidRed.r = 1.0;//Set its red component to 1.0
    }
    gl_FragColor = solidRed;
}
