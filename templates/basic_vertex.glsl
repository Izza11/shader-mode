// A minimal shader that performs the most basic tasks: 
// transform the vertices and render the primitives in a single color.

uniform mat4 transform; 
attribute vec4 position;

void main()
{

	vec4 v = vec4(position);
	v.x = sin(time*0.01);
	v.y = sin(time*0.05);
	gl_Position = transform * position;
}