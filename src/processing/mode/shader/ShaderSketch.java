package processing.mode.shader;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.sun.tools.javac.util.ArrayUtils;

import processing.app.Language;
import processing.app.Messages;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.ui.Editor;

public class ShaderSketch extends Sketch {

	int codeFilesCount;
	boolean renamingShaderCode;
	
	public ShaderSketch(String path, Editor editor) throws IOException {		
		super(path, editor);
		codeFilesCount = 0;
		renamingShaderCode = false;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void load() {
		    codeFolder = new File(getFolder(), "code");
		    dataFolder = new File(getFolder(), "data");

		    List<String> filenames = new ArrayList<>();
		    List<String> extensions = new ArrayList<>();

		    codeFilesCount = 0;
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
	public void getSketchCodeFiles(List<String> outFilenames, List<String> outExtensions) {
		    // get list of files in the sketch folder
		    String list[] = getFolder().list();
		    String list2[] = getDataFolder().list();
		    setSketchCodeFiles(outFilenames, outExtensions, list, "code");
		    if (list2 != null) 
		    	setSketchCodeFiles(outFilenames, outExtensions, list2, "data");
	}
	
	
	protected String promptForShaderTabName(String prompt, String oldName) {
	    final JTextField field = new JTextField(oldName);

	    field.addKeyListener(new KeyAdapter() {
	      // Forget ESC, the JDialog should handle it.
	      // Use keyTyped to catch when the feller is actually added to the text
	      // field. With keyTyped, as opposed to keyPressed, the keyCode will be
	      // zero, even if it's enter or backspace or whatever, so the keychar
	      // should be used instead. Grr.
	      public void keyTyped(KeyEvent event) {
	        //System.out.println("got event " + event);
	        char ch = event.getKeyChar();
	        if ((ch == '_') || (ch == '.') || // allow.pde and .java
	            (('A' <= ch) && (ch <= 'Z')) || (('a' <= ch) && (ch <= 'z'))) {
	          // These events are allowed straight through.
	        } else if (ch == ' ') {
	          String t = field.getText();
	          int start = field.getSelectionStart();
	          int end = field.getSelectionEnd();
	          field.setText(t.substring(0, start) + "_" + t.substring(end));
	          field.setCaretPosition(start + 1);
	          event.consume();
	        } else if ((ch >= '0') && (ch <= '9')) {
	          // getCaretPosition == 0 means that it's the first char
	          // and the field is empty.
	          // getSelectionStart means that it *will be* the first
	          // char, because the selection is about to be replaced
	          // with whatever is typed.
	          if (field.getCaretPosition() == 0 ||
	              field.getSelectionStart() == 0) {
	            // number not allowed as first digit
	            event.consume();
	          }
	        } else if (ch == KeyEvent.VK_ENTER) {
	          // Slightly ugly hack that ensures OK button of the dialog consumes
	          // the Enter key event. Since the text field is the default component
	          // in the dialog, OK doesn't consume Enter key event, by default.
	          Container parent = field.getParent();
	          while (!(parent instanceof JOptionPane)) {
	            parent = parent.getParent();
	          }
	          JOptionPane pane = (JOptionPane) parent;
	          final JPanel pnlBottom = (JPanel)
	            pane.getComponent(pane.getComponentCount() - 1);
	          for (int i = 0; i < pnlBottom.getComponents().length; i++) {
	            Component component = pnlBottom.getComponents()[i];
	            if (component instanceof JButton) {
	              final JButton okButton = (JButton) component;
	              if (okButton.getText().equalsIgnoreCase("OK")) {
	                ActionListener[] actionListeners =
	                  okButton.getActionListeners();
	                if (actionListeners.length > 0) {
	                  actionListeners[0].actionPerformed(null);
	                  event.consume();
	                }
	              }
	            }
	          }
	        } else {
	          event.consume();
	        }
	      }
	    });

	    int userReply = JOptionPane.showOptionDialog(editor, new Object[] {
	                                                 prompt, field },
	                                                 Language.text("editor.tab.new"),
	                                                 JOptionPane.OK_CANCEL_OPTION,
	                                                 JOptionPane.PLAIN_MESSAGE,
	                                                 null, new Object[] {
	                                                 Language.getPrompt("ok"),
	                                                 Language.getPrompt("cancel") },
	                                                 field);

	    if (userReply == JOptionPane.OK_OPTION) {
	      nameCode(field.getText());
	    }
	    return field.getText();
	}

	
	public String handleNewShaderCode() {
	    // make sure the user didn't hide the sketch folder
	    ensureExistence();

	    // if read-only, give an error
	    if (isReadOnly()) {
	      // if the files are read-only, need to first do a "save as".
	      Messages.showMessage(Language.text("new.messages.is_read_only"),
	                           Language.text("new.messages.is_read_only.description"));
	      return null;
	    }
	    renamingShaderCode = true;
	    renamingCode = false;
	    // editor.status.edit("Name for new file:", "");
	    return promptForShaderTabName(Language.text("editor.tab.rename.description")+":", "");
	  }
		  
	
	protected void nameCode(String newName) {
	    newName = newName.trim();
	    if (newName.length() == 0) {
	      return;
	    }

	    // make sure the user didn't hide the sketch folder
	    ensureExistence();

	    // Add the extension here, this simplifies some of the logic below.
	    if (newName.indexOf('.') == -1) {
	    	if (renamingShaderCode) {
	    		newName += "." + "glsl";
	    	} else {
	    		newName += "." + (renamingCode ? getMode().getDefaultExtension() : getMode().getModuleExtension());
	    	}	      
	    }
	    // if renaming to the same thing as before, just ignore.
	    // also ignoring case here, because i don't want to write
	    // a bunch of special stuff for each platform
	    // (osx is case insensitive but preserving, windows insensitive,
	    // *nix is sensitive and preserving.. argh)
	    if (renamingCode) {
	      if (newName.equalsIgnoreCase(current.getFileName())) {
	        // exit quietly for the 'rename' case.
	        // if it's a 'new' then an error will occur down below
	        return;
	      }
	    }

	    if (newName.startsWith(".")) {
	      Messages.showWarning(Language.text("name.messages.problem_renaming"),
	                           Language.text("name.messages.starts_with_dot.description"));
	      return;
	    }

	    int dot = newName.lastIndexOf('.');    
	    String newExtension = newName.substring(dot+1).toLowerCase();
	    if (!getMode().validExtension(newExtension)) {
	      Messages.showWarning(Language.text("name.messages.problem_renaming"),
	                           Language.interpolate("name.messages.invalid_extension.description",
	                           newExtension));
	      return;
	    }

	    // Don't let the user create the main tab as a .java file instead of .pde
	    if (!getMode().isDefaultExtension(newExtension)) {
	      if (renamingCode) {  // If creating a new tab, don't show this error
	        if (current == code[0]) {  // If this is the main tab, disallow
	          Messages.showWarning(Language.text("name.messages.problem_renaming"),
	                               Language.interpolate("name.messages.main_java_extension.description",
	                               newExtension));
	          return;
	        }
	      }
	    }

	    // dots are allowed for the .pde and .java, but not in the name
	    // make sure the user didn't name things poo.time.pde
	    // or something like that (nothing against poo time)
	    String shortName = newName.substring(0, dot);
	    String sanitaryName = Sketch.sanitizeName(shortName);
	    if (!shortName.equals(sanitaryName)) {
	      newName = sanitaryName + "." + newExtension;
	    }

	    // If changing the extension of a file from .pde to .java, then it's ok.
	    // http://code.google.com/p/processing/issues/detail?id=776
	    // A regression introduced by Florian's bug report (below) years earlier.
	    if (!(renamingCode && sanitaryName.equals(current.getPrettyName()))) {
	      // Make sure no .pde *and* no .java files with the same name already exist
	      // (other than the one we are currently attempting to rename)
	      // http://processing.org/bugs/bugzilla/543.html
	      for (SketchCode c : code) {
	        if (c != current && sanitaryName.equalsIgnoreCase(c.getPrettyName())) {
	          Messages.showMessage(Language.text("name.messages.new_sketch_exists"),
	                               Language.interpolate("name.messages.new_sketch_exists.description",
	                               c.getFileName(), getFolder().getAbsolutePath()));
	          return;
	        }
	      }
	    }

	    File newFile = new File(getFolder(), newName);

	    if (renamingCode) {
	      if (currentIndex == 0) {
	        // get the new folder name/location
	        String folderName = newName.substring(0, newName.indexOf('.'));
	        File newFolder = new File(getFolder().getParentFile(), folderName);
	        if (newFolder.exists()) {
	          Messages.showWarning(Language.text("name.messages.new_folder_exists"),
	                               Language.interpolate("name.messages.new_folder_exists.description",
	                               newName));
	          return;
	        }

	        // renaming the containing sketch folder
	        boolean success = getFolder().renameTo(newFolder);
	        if (!success) {
	          Messages.showWarning(Language.text("name.messages.error"),
	                               Language.text("name.messages.no_rename_folder.description"));
	          return;
	        }
	        // let this guy know where he's living (at least for a split second)
	        current.setFolder(newFolder);
	        // folder will be set to newFolder by updateInternal()

	        // unfortunately this can't be a "save as" because that
	        // only copies the sketch files and the data folder
	        // however this *will* first save the sketch, then rename

	        // moved this further up in the process (before prompting for the name)
//	        if (isModified()) {
//	          Base.showMessage("Save", "Please save the sketch before renaming.");
//	          return;
//	        }

	        // This isn't changing folders, just changes the name
	        newFile = new File(newFolder, newName);
	        if (!current.renameTo(newFile, newExtension)) {
	          Messages.showWarning(Language.text("name.messages.error"),
	                               Language.interpolate("name.messages.no_rename_file.description",
	                               current.getFileName(), newFile.getName()));
	          return;
	        }

	        // Tell each code file the good news about their new home.
	        // current.renameTo() above already took care of the main tab.
	        for (int i = 1; i < codeCount; i++) {
	          code[i].setFolder(newFolder);
	        }
	       // Update internal state to reflect the new location
	        updateInternal(sanitaryName, newFolder);

//	        File newMainFile = new File(newFolder, newName + ".pde");
//	        String newMainFilePath = newMainFile.getAbsolutePath();
	//
//	        // having saved everything and renamed the folder and the main .pde,
//	        // use the editor to re-open the sketch to re-init state
//	        // (unfortunately this will kill positions for carets etc)
//	        editor.handleOpenUnchecked(newMainFilePath,
//	                                   currentIndex,
//	                                   editor.getSelectionStart(),
//	                                   editor.getSelectionStop(),
//	                                   editor.getScrollPosition());
	//
//	        // get the changes into the sketchbook menu
//	        // (re-enabled in 0115 to fix bug #332)
//	        editor.base.rebuildSketchbookMenusAsync();

	      } else {  // else if something besides code[0]
	        if (!current.renameTo(newFile, newExtension)) {
	          Messages.showWarning(Language.text("name.messages.error"),
	                               Language.interpolate("name.messages.no_rename_file.description",
	                               current.getFileName(), newFile.getName()));
	          return;
	        }
	      }

	    } else {  // not renaming, creating a new file
	      try {
	        if (!newFile.createNewFile()) {
	          // Already checking for IOException, so make our own.
	          throw new IOException("createNewFile() returned false");
	        }
	      } catch (IOException e) {
	        Messages.showWarning(Language.text("name.messages.error"),
	                             Language.interpolate("name.messages.no_create_file.description",
	                             newFile, getFolder().getAbsolutePath()), e);
	        return;
	      }
	      SketchCode newCode = new SketchCode(newFile, newExtension);
	      //System.out.println("new code is named " + newCode.getPrettyName() + " " + newCode.getFile());
	      insertCode(newCode);
	    }

	    // sort the entries
	    sortCode();

	    // set the new guy as current
	    setCurrentCode(newName);

	    // update the tabs
	    editor.rebuildHeader();
	    renamingShaderCode = false;
	  }
	
		  

}
