/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 Part of the Processing project - http://processing.org
 Copyright (c) 2019 Izza Tariq
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 2
 as published by the Free Software Foundation.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package processing.mode.shader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

//import com.sun.tools.javac.util.Pair;

import processing.app.Base;
import processing.app.Language;
import processing.app.Mode;
import processing.app.Platform;
import processing.app.Preferences;
import processing.app.Util;
import processing.app.ui.EditorState;
import processing.app.ui.EditorException;
import processing.app.ui.EditorHeader;
import processing.mode.java.JavaEditor;
import java.io.*;
import java.util.*;

@SuppressWarnings("serial")
public class ShaderEditor extends JavaEditor {
  
  private JMenu shaderModeMenu;
    
  protected ShaderEditor(Base base, String path, EditorState state, Mode mode) throws EditorException {
    super(base, path, state, mode);
  }

  @Override
  public EditorHeader createHeader() {
    return new ShaderModeHeader(this);
  }

  @Override
  protected void handleOpenInternal(String path) throws EditorException {
    // check to make sure that this .pde file is
    // in a folder of the same name
    final File file = new File(path);
    final File parentFile = new File(file.getParent());
    final String parentName = parentFile.getName();
    final String defaultName = parentName + "." + mode.getDefaultExtension();
    final File altFile = new File(file.getParent(), defaultName);

    if (defaultName.equals(file.getName())) {
      // no beef with this guy
    } else if (altFile.exists()) {
      // The user selected a source file from the same sketch,
      // but open the file with the default extension instead.
      path = altFile.getAbsolutePath();
    } else if (!mode.canEdit(file)) {
      final String modeName = mode.getTitle().equals("Shader") ? "Processing" : (mode.getTitle() + " Mode");
      throw new EditorException(
          modeName + " can only open its own sketches\n" + "and other files ending in " + mode.getDefaultExtension());
    } else {
      final String properParent = file.getName().substring(0, file.getName().lastIndexOf('.'));

      Object[] options = { Language.text("prompt.ok"), Language.text("prompt.cancel") };
      String prompt = "The file \"" + file.getName() + "\" needs to be inside\n" + "a sketch folder named \""
          + properParent + "\".\n" + "Create this folder, move the file, and continue?";

      int result = JOptionPane.showOptionDialog(this, prompt, "Moving", JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

      if (result == JOptionPane.YES_OPTION) {
        // create properly named folder
        File properFolder = new File(file.getParent(), properParent);
        if (properFolder.exists()) {
          throw new EditorException("A folder named \"" + properParent + "\" " + "already exists. Can't open sketch.");
        }
        if (!properFolder.mkdirs()) {
          throw new EditorException("Could not create the sketch folder.");
        }
        // copy the sketch inside
        File properPdeFile = new File(properFolder, file.getName());
        File origPdeFile = new File(path);
        try {
          Util.copyFile(origPdeFile, properPdeFile);
        } catch (IOException e) {
          throw new EditorException("Could not copy to a proper location.", e);
        }

        // remove the original file, so user doesn't get confused
        origPdeFile.delete();

        // update with the new path
        path = properPdeFile.getAbsolutePath();

      } else { // if (result == JOptionPane.NO_OPTION) {
        // Catch all other cases, including Cancel or ESC
        // return false;
        throw new EditorException();
      }
    }

    try {
      sketch = new ShaderSketch(path, this);
    } catch (IOException e) {
      throw new EditorException("Could not create the shader sketch.", e);
    }

    header.rebuild();
    updateTitle();
    // Disable untitled setting from previous document, if any
//	    untitled = false;

    // Store information on who's open and running
    // (in case there's a crash or something that can't be recovered)
    // TODO this probably need not be here because of the Recent menu, right?
    Preferences.save();
  }
  
  public JMenu buildModeMenu() {
    buildShaderMenu();
    return shaderModeMenu;
  }

  ArrayList<Pair> readShaderTemplates() {
	  System.out.println("ENTERED readshader");
    ArrayList<Pair> templist = new ArrayList<Pair>();
    System.out.println("readShaderTemplates path:");
    String path = mode.getFolder().getAbsolutePath() + "/templates";
    path = path.replace("\\", "/");
    
    System.out.println(path);
    
    File templatefolder = new File(path);
    for (File shaderfile : templatefolder.listFiles()) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(shaderfile));
      } catch (FileNotFoundException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      StringBuilder shadercontent = new StringBuilder();
      String temp;
      try {
        while ((temp = br.readLine()) != null)
          // templist.add(temp);
          shadercontent.append(temp).append("\n");
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      templist.add(new Pair(shaderfile.getName(), shadercontent.toString()));
    }
    return templist;
  }

  protected void writeTemplateToSketch(String fileContent) throws IOException {
	  System.out.println("ENTERED writeTemplate");
    String shdrFilename = ((ShaderSketch) getSketch()).handleNewShaderCode();
    
    if (shdrFilename.equals("")) { // user cancelled template creation
    	return;
    }

    String directory = ((ShaderSketch) getSketch()).getFolder().getAbsolutePath();

    System.out.println("writeTemplate path is:");
    String path = directory + "/" + shdrFilename;
    path = path.replace("\\", "/");
    
    System.out.println(path);
    
    BufferedWriter writer = new BufferedWriter(new FileWriter(path));
    writer.write(fileContent);
    writer.close();
    ((ShaderSketch) getSketch()).load();
  }
  
  protected void addTemplatesToMenu() {
	  System.out.println("entering addtemplatetoMenu");
	  
	  final JMenu templates = new JMenu(Language.text("Shader Templates"));
	  
	  /*
	    // Populate only when sub-menu is opened, to avoid having spurious menu
	    // options if a library is deleted, or a missing menu option if a library is
	    // added
	  System.out.println("about to readshadertemplate");
	    ArrayList<Pair> templist = readShaderTemplates();
	    templates.addMenuListener(new MenuListener() {

	      @Override
	      public void menuSelected(MenuEvent e) {
	        templates.removeAll();
	        
	        // read the template_names.txt file from shadermode location in Processing
	        // folder
	        // reading from disk everytime we click templates option in shader menu?
	        // inefficient?
	        // or load all files in constructor, then changes in files will only show when
	        // PDE is restarted
	        System.out.println("entering writetemplatetosketch");
	        for (int i = 0; i < templist.size(); i++) {
	          // System.out.println(templist.get(i));
	          String name = templist.get(i).fst;
	          int index = name.lastIndexOf('.');

	          final JMenuItem jitem = new JMenuItem(Language.text(name.substring(0, index)));
	          System.out.println("Adding shader template item " + name.substring(0, index));

	          final int tempIndex = i;
	          
	          jitem.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
//	              try {
//	            	  writeTemplateToSketch(templist.get(tempIndex).snd);
//	              } catch (IOException e1) {
//	                // TODO Auto-generated catch block
//	                e1.printStackTrace();
//	              }
	            }
	          });
	          
	          templates.add(jitem);
	        }
	      }

	      @Override
	      public void menuDeselected(MenuEvent e) {
	        templates.removeAll();
	      }

	      @Override
	      public void menuCanceled(MenuEvent e) {
	        menuDeselected(e);
	      }
	    });
	    */
	    
	  
    // read the template_names.txt file from shadermode location in Processing
    // folder
    // reading from disk everytime we click templates option in shader menu?
    // inefficient?
    // or load all files in constructor, then changes in files will only show when
    // PDE is restarted
	  ArrayList<Pair> templist = readShaderTemplates();
    System.out.println("entering writetemplatetosketch");
    for (int i = 0; i < templist.size(); i++) {
      // System.out.println(templist.get(i));
      String name = templist.get(i).fst;
      int index = name.lastIndexOf('.');

      final JMenuItem jitem = new JMenuItem(Language.text(name.substring(0, index)));
      System.out.println("Adding shader template item " + name.substring(0, index));

      final int tempIndex = i;
      
      jitem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          try {
            writeTemplateToSketch(templist.get(tempIndex).snd);
          } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      });
      
      templates.add(jitem);
    }
	    
	  shaderModeMenu.add(templates);
  }

  public JMenu buildShaderMenu() {
    shaderModeMenu = new JMenu(Language.text("Shader"));
    JMenuItem item;

    // Adding link to processing shader tutorial
    item = new JMenuItem(Language.text("Getting Started"));
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL(Language.text("https://processing.org/tutorials/pshader/"));
      }
    });
    shaderModeMenu.add(item);
    
    // Reading and adding all templates from the mode template folder to the Shader Menu
    addTemplatesToMenu(); 
    
    // Adding link to Post survey
    item = new JMenuItem(Language.text("Post-survey"));
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
    	  Platform.openURL(Language.text(
    	            "https://docs.google.com/forms/d/1UjbqwCgthD-z8_YYzVZ2XzkGacS6Qhp67zNUDJb788o/edit?usp=forms_home&ths=true"));
      }
    });
    shaderModeMenu.add(item);

    return shaderModeMenu;
  }

  public void surveyPrompt() {
    toFront();

    if (!Platform.isMacOS()) {
      String prompt = Language.interpolate("Are you ready to fill the post-survey?");
      int result = JOptionPane.showConfirmDialog(this, prompt, Language.text("menu.file.close"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

      if (result == JOptionPane.YES_OPTION) {
        Platform.openURL(Language.text(
            "https://docs.google.com/forms/d/1UjbqwCgthD-z8_YYzVZ2XzkGacS6Qhp67zNUDJb788o/edit?usp=forms_home&ths=true"));
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
          + "</style> </head>" + "<b>" + Language.interpolate("Are you ready to fill the post-survey?", sketch.getName())
          + "</b>" + "<p>" + Language.text("save.hint") + "</p>", JOptionPane.QUESTION_MESSAGE);

      String[] options = new String[] { Language.text("Yes"), Language.text("Cancel"), // put No here
          Language.text("No") };
      pane.setOptions(options);

      // highlight the safest option ala apple hig
      pane.setInitialValue(options[0]);

      // on macosx, setting the destructive property places this option
      // away from the others at the lefthand side
      pane.putClientProperty("Quaqua.OptionPane.destructiveOption", Integer.valueOf(2));

      JDialog dialog = pane.createDialog(this, null);
      dialog.setVisible(true);

      Object result = pane.getValue();
      if (result == options[0]) { // Haven't filled survey
        Platform.openURL(Language.text(
            "https://docs.google.com/forms/d/1UjbqwCgthD-z8_YYzVZ2XzkGacS6Qhp67zNUDJb788o/edit?usp=forms_home&ths=true"));
      } 
    }
  }

  /**
   * Check if the sketch is modified and ask user to save changes.
   * 
   * @return false if canceling the close/quit operation
   */
  @Override
  public boolean checkModified() {
    surveyPrompt();

    if (!sketch.isModified())
      return true;

    // As of Processing 1.0.10, this always happens immediately.
    // http://dev.processing.org/bugs/show_bug.cgi?id=1456

    // With Java 7u40 on OS X, need to bring the window forward.
    toFront();

    if (!Platform.isMacOS()) {
      String prompt = Language.interpolate("close.unsaved_changes", sketch.getName());
      int result = JOptionPane.showConfirmDialog(this, prompt, Language.text("menu.file.close"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

      if (result == JOptionPane.YES_OPTION) {
        return handleSave(true);

      } else if (result == JOptionPane.NO_OPTION) {
        return true; // ok to continue

      } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
        return false;

      } else {
        throw new IllegalStateException();
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
          + "</style> </head>" + "<b>" + Language.interpolate("save.title", sketch.getName()) + "</b>" + "<p>"
          + Language.text("save.hint") + "</p>", JOptionPane.QUESTION_MESSAGE);

      String[] options = new String[] { Language.text("save.btn.save"), Language.text("prompt.cancel"),
          Language.text("save.btn.dont_save") };
      pane.setOptions(options);

      // highlight the safest option ala apple hig
      pane.setInitialValue(options[0]);

      // on macosx, setting the destructive property places this option
      // away from the others at the lefthand side
      pane.putClientProperty("Quaqua.OptionPane.destructiveOption", Integer.valueOf(2));

      JDialog dialog = pane.createDialog(this, null);
      dialog.setVisible(true);

      Object result = pane.getValue();
      if (result == options[0]) { // save (and close/quit)
        return handleSave(true);

      } else if (result == options[2]) { // don't save (still close/quit)
        return true;

      } else { // cancel?
        return false;
      }
    }
  }
  
  class Pair {
    String fst;
    String snd;
    
    Pair(String f, String s) {
      fst = f;
      snd = s;
    }
  }

}