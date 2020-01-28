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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import processing.app.Language;
import processing.app.Messages;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.ui.Editor;

public class ShaderSketch extends Sketch {

  private boolean renamingShaderCode;
  private Editor shaderEditor;
  private List<String> folders;

  
  public ShaderSketch(String path, Editor editor) throws IOException {
    super(path, editor);
    shaderEditor = editor;
    renamingShaderCode = false;
  }

  
  @Override
  protected void load() {
    super.load();
    
    // Adding shader files that could be inside data
    folders  = new ArrayList<String>();
    List<String> filenames = new ArrayList<>();
    List<String> extensions = new ArrayList<>();    
    setSketchCodeFiles(getDataFolder(), filenames, extensions);

    for (int i = 0; i < filenames.size(); i++) {
      String filename = filenames.get(i);
      String extension = extensions.get(i);
      String path = folders.get(i);
      SketchCode newCode = new SketchCode(new File(path, filename), extension);
      insertCode(newCode);
    }
    
    
    /*
    List<String> filenames = new ArrayList<>();
    List<String> extensions = new ArrayList<>();

    getSketchCodeFiles(filenames, extensions);
    for (int i = 0; i < filenames.size(); i++) {
      String filename = filenames.get(i);
      String extension = extensions.get(i);
      String path = folders.get(i);
      SketchCode newCode = new SketchCode(new File(path, filename), extension);
      insertCode(newCode);
    }
    
    // Move the main class to the first tab
    SketchCode[] codeCopy = getCode();
    for (int i = 1; i < getCodeCount(); i++) {
      if (codeCopy[i].getFile().equals(getMainFile())) {
        SketchCode temp = codeCopy[0];
        codeCopy[0] = codeCopy[i];
        codeCopy[i] = temp;
        break;
      }
    }

    // Sort the entries at the top
    sortCode();

    // Set the main file to be the current tab
    if (shaderEditor != null) {
      setCurrentCode(0);
    }
    
    // This ensures that codeFolder and dataFolder get properly initialized
    updateInternal(getName(), getFolder(), false);
    */
  }
  
  
//  @Override
//  public void getSketchCodeFiles(List<String> outFilenames, List<String> outExtensions) {
//    folders  = new ArrayList<String>();
//    setSketchCodeFiles(getFolder(), outFilenames, outExtensions);
//    setSketchCodeFiles(getDataFolder(), outFilenames, outExtensions);
//  }  

  
  private void setSketchCodeFiles(File inFolder, List<String> outFilenames, List<String> outExtensions) {
    String list[] = inFolder.list();
    if (list == null) return;
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
            if (outFilenames != null) outFilenames.add(filename);
            if (outExtensions != null) outExtensions.add(extension);
            folders.add(inFolder.getAbsolutePath());
          }
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
    // editor.status.edit("Name for new file:", "");
    return promptForShaderTabName(Language.text("editor.tab.rename.description") + ":", "");
  }
  

  @Override
  protected void nameCode(String newName) {
    newName = newName.trim();
    if (newName.length() == 0) {
      return;
    }
    
    if (renamingShaderCode && newName.indexOf('.') == -1) {
      newName += "." + "glsl";
    }
    
    super.nameCode(newName);
    
    renamingShaderCode = false;
  }
}
