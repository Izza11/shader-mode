PShader simple;
PShape s;
void setup() {
  size(850, 850, P3D);
  s = loadShape("blender_monkey.obj");
  s.rotateX(TWO_PI/4);
  s.rotateY(TWO_PI/2);
  simple = loadShader("frag.glsl", "vert.glsl");
  noStroke();
}

void draw() {  
  background(0); 
  shader(simple);
  translate(width/2, height/2);
  shape(s,0,0,450,400);
}
