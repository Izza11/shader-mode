uniform mat4 transform;
attribute vec4 position;
attribute vec3 normal;
void main()
{
gl_Position = transform * position;
}
