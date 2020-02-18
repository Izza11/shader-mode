// Texture from Jason Liebig's FLICKR collection of vintage labels and wrappers:
// http://www.flickr.com/photos/jasonliebigstuff/3739263136/in/photostream/

PImage label;
PShader texShader;

void setup() {
  size(600, 600, P3D);  
  
  // load sources
  label = loadImage("lachoy.jpg");
  texShader = loadShader("texfrag.glsl", "texvert.glsl");
  
}

void draw() {    
  background(0);
  shader(texShader); 
  image(label, 0, 0, width, height);
  texShader.set("contrast", map(mouseX, 0, width, 0, 1));
  fill(255);
}
