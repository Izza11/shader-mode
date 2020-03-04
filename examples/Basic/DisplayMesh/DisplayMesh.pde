/* Display Mesh by Izza Tariq

  Description: Uploads a 3D model contained in an .obj file (Other 3D geometry file formats are supported as well)
  and uses surface normal values as display colors.

  Note: Please note that examples are read-only, therefore if you modify an example you must save it as a new project for the changes to apply).
    
*/

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
