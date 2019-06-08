package processing.mode.shader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import processing.app.Sketch;
import processing.app.ui.Editor;

public class ShaderSketch extends Sketch {

	public ShaderSketch(String path, Editor editor) throws IOException {
		super(path, editor);
		// TODO Auto-generated constructor stub
	}
	
	@Override 
	public void getSketchCodeFiles(List<String> outFilenames, List<String> outExtensions) {
// get list of files in the sketch folder
		String list[] = getFolder().list();

		for (String filename : list) {
// Ignoring the dot prefix files is especially important to avoid files
// with the ._ prefix on Mac OS X. (You'll see this with Mac files on
// non-HFS drives, i.e. a thumb drive formatted FAT32.)
			if (filename.startsWith("."))
				continue;

// Don't let some wacko name a directory blah.pde or bling.java.
			if (new File(getFolder(), filename).isDirectory())
				continue;

// figure out the name without any extension
			String base = filename;
// now strip off the .pde and .java extensions
			for (String extension : getMode().getExtensions()) {
				if (base.toLowerCase().endsWith("." + extension)) {
					base = base.substring(0, base.length() - (extension.length() + 1));

// Don't allow people to use files with invalid names, since on load,
// it would be otherwise possible to sneak in nasty filenames. [0116]
					if (isSanitaryName(base)) {
						if (outFilenames != null)
							outFilenames.add(filename);
						if (outExtensions != null)
							outExtensions.add(extension);
					}
				}
			}
		}
		System.out.println("main folder contents opened");
		
		//////////////////////////
		System.out.println("Project has DATA folder? " + hasDataFolder());
		prepareDataFolder();
		
		System.out.println("Prepared data folder..does it exist now? " + hasDataFolder());
		//get list of files in the sketch folder
		list = getDataFolder().list();
		

		for (String filename : list) {
			System.out.println("filname found is " + filename);
// Ignoring the dot prefix files is especially important to avoid files
// with the ._ prefix on Mac OS X. (You'll see this with Mac files on
// non-HFS drives, i.e. a thumb drive formatted FAT32.)
			if (filename.startsWith("."))
				continue;

// Don't let some wacko name a directory blah.pde or bling.java.
			if (new File(getDataFolder(), filename).isDirectory())
				continue;

// figure out the name without any extension
			String base = filename;
// now strip off the .pde and .java extensions
			for (String extension : getMode().getExtensions()) {
				if (base.toLowerCase().endsWith("." + extension)) {
					base = base.substring(0, base.length() - (extension.length() + 1));

// Don't allow people to use files with invalid names, since on load,
// it would be otherwise possible to sneak in nasty filenames. [0116]
					if (isSanitaryName(base)) {
						if (outFilenames != null)
							outFilenames.add(filename);
						if (outExtensions != null)
							outExtensions.add(extension);
					}
				}
			}
		}
		System.out.println("DATA folder contents opened");
		///////////////////////////
		
	}
	
	
	

}
