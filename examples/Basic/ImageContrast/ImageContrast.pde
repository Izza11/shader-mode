/* Image Contrast by Izza Tariq

	Description: Changes contrast of image with respect to movement of mouse in the horizontal direction

	Texture from Jason Liebig's FLICKR collection of vintage labels and wrappers:
	http://www.flickr.com/photos/jasonliebigstuff/3739263136/in/photostream/

  Note: Please note that examples are read-only, therefore if you modify an example you must save it as a new project for the changes to apply).
  
*/



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
