package processing.mode.shader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.tools.javac.util.ArrayUtils;

import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.ui.Editor;

public class ShaderSketch extends Sketch {

	int codeFilesCount;
	
	public ShaderSketch(String path, Editor editor) throws IOException {		
		super(path, editor);
		codeFilesCount = 0;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void load() {
		    codeFolder = new File(getFolder(), "code");
		    dataFolder = new File(getFolder(), "data");

		    List<String> filenames = new ArrayList<>();
		    List<String> extensions = new ArrayList<>();

		    getSketchCodeFiles(filenames, extensions);

		    codeCount = filenames.size();
		    code = new SketchCode[codeCount];

		    for (int i = 0; i < codeCount; i++) {
		      String filename = filenames.get(i);
		      String extension = extensions.get(i);
		      
		      if (i >= codeFilesCount) {
		    	  code[i] = new SketchCode(new File(getDataFolder(), filename), extension);
		      } else {
		    	  code[i] = new SketchCode(new File(getFolder(), filename), extension);
		      }
		        
		    }
		    codeFilesCount = 0;

		    // move the main class to the first tab
		    // start at 1, if it's at zero, don't bother
		    for (int i = 1; i < codeCount; i++) {
		      //if (code[i].file.getName().equals(mainFilename)) {
		      if (code[i].getFile().equals(primaryFile)) {
		        SketchCode temp = code[0];
		        code[0] = code[i];
		        code[i] = temp;
		        break;
		      }
		    }

		    // sort the entries at the top
		    sortCode();

		    // set the main file to be the current tab
		    if (editor != null) {
		      setCurrentCode(0);
		    }
		  }


	public void setSketchCodeFiles(List<String> outFilenames, List<String> outExtensions, 
			String[] list, String folder) {
		
		for (String filename : list) {
		      // Ignoring the dot prefix files is especially important to avoid files
		      // with the ._ prefix on Mac OS X. (You'll see this with Mac files on
		      // non-HFS drives, i.e. a thumb drive formatted FAT32.)
		      if (filename.startsWith(".")) continue;

		      // Don't let some wacko name a directory blah.pde or bling.java.
		      if (new File(getFolder(), filename).isDirectory()) continue;

		      // figure out the name without any extension
		      String base = filename;
		      // now strip off the .pde and .java extensions
		      for (String extension : getMode().getExtensions()) {
		        if (base.toLowerCase().endsWith("." + extension)) {
		          base = base.substring(0, base.length() - (extension.length() + 1));

		          // Don't allow people to use files with invalid names, since on load,
		          // it would be otherwise possible to sneak in nasty filenames. [0116]
		          if (isSanitaryName(base)) {
		        	  if (outFilenames != null) {
		        		  outFilenames.add(filename);
		        		  if (folder.equals("code")) codeFilesCount++;		        		  
		        	  }		        		  
		        	  if (outExtensions != null)
		        		  outExtensions.add(extension);
		          }
		        }
		      }
		    }
		
	}
		  public void getSketchCodeFiles(List<String> outFilenames,
		                                 List<String> outExtensions) {
		    // get list of files in the sketch folder
		    String list[] = getFolder().list();
		    String list2[] = getDataFolder().list();
		    setSketchCodeFiles(outFilenames, outExtensions, list, "code");
		    if (list2 != null) 
		    	setSketchCodeFiles(outFilenames, outExtensions, list2, "data");
		    
		  }

}
