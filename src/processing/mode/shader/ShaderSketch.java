package processing.mode.shader;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import processing.app.Language;
import processing.app.Messages;
import processing.app.Platform;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.ui.Editor;
import java.awt.event.ActionEvent;

public class ShaderSketch extends Sketch {

  private boolean renamingShaderCode;
  private Editor shaderEditor;
  private List<String> folders;

  
  public ShaderSketch(String path, Editor editor) throws IOException {
    super(path, editor);
    shaderEditor = editor;
    renamingShaderCode = false;
  }

  protected void load() {
    moveShaderFilesToRoot();
    super.load();
  }
  
  private void moveShaderFilesToRoot() {
    File dataFolderTmp = new File(getFolder(), "data");
    // get list of files in the sketch folder
    String list[] = dataFolderTmp.list();
    if (list == null) 
    	return;    

    boolean allowed = false;
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
      String extension = "glsl";
      if (base.toLowerCase().endsWith("." + extension)) {
    	  if (!allowed) {
    		  if (!askUser()) {
    			  return;
    		  } else {
    			  allowed = true;
    		  }   			  
    	   }
        base = base.substring(0, base.length() - (extension.length() + 1));
        if (isSanitaryName(base)) {
          File srcFilename = new File(dataFolderTmp, filename);
          File dstFilename = new File(getFolder(), filename);
          srcFilename.renameTo(dstFilename);
        }
      }      
    }
  }


  private String promptForShaderTabName(String prompt, String oldName) {
    final JTextField field = new JTextField(oldName);

    field.addKeyListener(new KeyAdapter() {
      // Forget ESC, the JDialog should handle it.
      // Use keyTyped to catch when the feller is actually added to the text
      // field. With keyTyped, as opposed to keyPressed, the keyCode will be
      // zero, even if it's enter or backspace or whatever, so the keychar
      // should be used instead. Grr.
      public void keyTyped(KeyEvent event) {
        // System.out.println("got event " + event);
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
          if (field.getCaretPosition() == 0 || field.getSelectionStart() == 0) {
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
          final JPanel pnlBottom = (JPanel) pane.getComponent(pane.getComponentCount() - 1);
          for (int i = 0; i < pnlBottom.getComponents().length; i++) {
            Component component = pnlBottom.getComponents()[i];
            if (component instanceof JButton) {
              final JButton okButton = (JButton) component;
              if (okButton.getText().equalsIgnoreCase("OK")) {
                ActionListener[] actionListeners = okButton.getActionListeners();
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

    int userReply = JOptionPane.showOptionDialog(shaderEditor, new Object[] { prompt, field },
        Language.text("editor.tab.new"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
        new Object[] { Language.getPrompt("ok"), Language.getPrompt("cancel") }, field);

    if (userReply == JOptionPane.OK_OPTION) {
      return nameShaderCode(field.getText());
    } 
    return "";
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
    // editor.status.edit("Name for new file:", "");
    return promptForShaderTabName(Language.text("editor.tab.rename.description") + ":", "");
  }
  

  protected String nameShaderCode(String newName) {
    newName = newName.trim();
    if (newName.length() == 0) {
      return "";
    }
    
    if (renamingShaderCode && newName.indexOf('.') == -1) {
      newName += "." + "glsl";    
    } 
    
    super.nameCode(newName);    
    renamingShaderCode = false;
    return newName;
  }
  
  protected boolean askUser() {
	  //if (shaderEditor == null) return false;
	  
	    if (!Platform.isMacOS()) {
	      String prompt = Language.interpolate("Shader files will be moved to root sketch folder. Do you want to proceed?");
	      int result = JOptionPane.showConfirmDialog(shaderEditor, prompt, Language.text("menu.file.close"),
	          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

	      if (result == JOptionPane.YES_OPTION) {
	       return true;
	      } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
	        return false;
	      } else {
	        return false;
	      }

	    } else {
	      // This code is disabled unless Java 1.5 is being used on Mac OS X
	      // because of a Java bug that prevents the initial value of the
	      // dialog from being set properly (at least on my MacBook Pro).
	      // The bug causes the "Don't Save" option to be the highlighted,
	      // blinking, default. This sucks. But I'll tell you what doesn't
	      // suck--workarounds for the Mac and Apple's snobby attitude about it!
	      // I think it's nifty that they treat their developers like dirt.

	      // Pane formatting adapted from the quaqua guide
	      // http://www.randelshofer.ch/quaqua/guide/joptionpane.html
	      JOptionPane pane = new JOptionPane("<html> " + "<head> <style type=\"text/css\">"
	          + "b { font: 13pt \"Lucida Grande\" }" + "p { font: 11pt \"Lucida Grande\"; margin-top: 8px }"
	          + "</style> </head>" + "<b>" + Language.interpolate("Shader files will be moved to root sketch folder. Do you want to proceed?", getName())
	          + "</b>" + "<p>" + Language.text("save.hint") + "</p>", JOptionPane.QUESTION_MESSAGE);

	      String[] options = new String[] { Language.text("Yes"), Language.text("Cancel"), // put No here
	          Language.text("No") };
	      pane.setOptions(options);

	      // highlight the safest option ala apple hig
	      pane.setInitialValue(options[0]);

	      // on macosx, setting the destructive property places this option
	      // away from the others at the lefthand side
	      pane.putClientProperty("Quaqua.OptionPane.destructiveOption", Integer.valueOf(2));

	      JDialog dialog = pane.createDialog(shaderEditor, null);
	      dialog.setVisible(true);

	      Object result = pane.getValue();
	      if (result == options[0]) { // Yes already filled survey
	        return true;

	      } else if (result == options[2]) { // Haven't filled survey
	        return false;

	      } else { // cancel?
	        return false;
	      }
	    }
  }
  
}
