package processing.mode.shader;

import java.io.File;

import processing.app.Base;
import processing.mode.java.JavaMode;

public class ShaderMode extends JavaMode {

  public ShaderMode(Base base, File folder) {
    super(base, folder);
  }
  
  @Override
  public String getTitle() {
    return "Shader";
  }
  
  @Override
  public String[] getExtensions() {
    return new String[] { "pde", "java", "glsl" };
  }
}
