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
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.sun.tools.javac.util.Pair;

import processing.app.Base;
import processing.app.Language;
import processing.app.Mode;
import processing.app.Platform;
import processing.app.Preferences;
import processing.app.Sketch;
import processing.app.Util;
import processing.app.ui.EditorState;
import processing.app.ui.Toolkit;
import processing.app.ui.EditorException;
import processing.app.ui.EditorHeader;
import processing.mode.java.JavaEditor;
import java.io.*; 
import java.util.*;

@SuppressWarnings("serial")
public class ShaderEditor extends JavaEditor {

  protected ShaderEditor(Base base, String path, EditorState state, 
                         Mode mode) throws EditorException {
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
	      final String modeName = mode.getTitle().equals("Shader") ?
	        "Processing" : (mode.getTitle() + " Mode");
	      throw new EditorException(modeName + " can only open its own sketches\n" +
	                                "and other files ending in " +
	                                mode.getDefaultExtension());
	    } else {
	      final String properParent =
	        file.getName().substring(0, file.getName().lastIndexOf('.'));

	      Object[] options = { Language.text("prompt.ok"), Language.text("prompt.cancel") };
	      String prompt =
	        "The file \"" + file.getName() + "\" needs to be inside\n" +
	        "a sketch folder named \"" + properParent + "\".\n" +
	        "Create this folder, move the file, and continue?";

	      int result = JOptionPane.showOptionDialog(this,
	                                                prompt,
	                                                "Moving",
	                                                JOptionPane.YES_NO_OPTION,
	                                                JOptionPane.QUESTION_MESSAGE,
	                                                null,
	                                                options,
	                                                options[0]);

	      if (result == JOptionPane.YES_OPTION) {
	        // create properly named folder
	        File properFolder = new File(file.getParent(), properParent);
	        if (properFolder.exists()) {
	          throw new EditorException("A folder named \"" + properParent + "\" " +
	                                    "already exists. Can't open sketch.");
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

	      } else {  //if (result == JOptionPane.NO_OPTION) {
	        // Catch all other cases, including Cancel or ESC
	        //return false;
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
  
  protected JMenu shaderModeMenu;
  protected JMenuItem shaderItem;
  
  @Override
  protected void buildMenuBar() {
	    JMenuBar menubar = new JMenuBar();
	    fileMenu = buildFileMenu();
	    menubar.add(fileMenu);
	    menubar.add(buildEditMenu());
	    menubar.add(buildSketchMenu());

	    // For 3.0a4 move mode menu to the left of the Tool menu
	    JMenu modeMenu = buildModeMenu();
	    if (modeMenu != null) {
	      menubar.add(modeMenu);
	    }
	    
	    JMenu shaderMenu = buildShaderMenu();
	    if (shaderMenu != null) {
	    	menubar.add(shaderMenu);
	    }

	    toolsMenu = new JMenu(Language.text("menu.tools"));
	    base.populateToolsMenu(toolsMenu);
	    menubar.add(toolsMenu);

	    menubar.add(buildHelpMenu());
	    Toolkit.setMenuMnemonics(menubar);
	    setJMenuBar(menubar);
	  }
  
  
  ArrayList<Pair<String, String>> readShaderTemplates(){
	  ArrayList<Pair<String, String>> templist = new ArrayList<Pair<String, String>>();	  
	  File templatefolder = new File(Base.getSketchbookModesFolder() + "\\ShaderMode\\templates");
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
					//templist.add(temp);
					shadercontent.append(temp).append("\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  
		    
		    // shadercontent.toString()
		    templist.add(new Pair<String, String>(shaderfile.getName(), shadercontent.toString()));
	  }
	  return templist;
	  
  }
  
  void addTemplateToFile(String fileContent) throws IOException {
	  String shdrFilename = ((ShaderSketch) getSketch()).handleNewShaderCode();
	  shdrFilename += ".glsl";
	  String directory = ((ShaderSketch) getSketch()).getFolder().getAbsolutePath();
	     
	    BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "\\" + shdrFilename));
	    writer.write(fileContent);
	    writer.close();
	  ((ShaderSketch) getSketch()).load();
  }
  
  public JMenu buildShaderMenu() {
	  shaderModeMenu = new JMenu(Language.text("menu.shader"));
	    JMenuItem item;

	    item = new JMenuItem(Language.text("menu.help.getting_started"));
	    item.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        Platform.openURL(Language.text("menu.help.getting_started_shaders.url"));
	      }
	    });
	    shaderModeMenu.add(item);
	    
	    ////////////////////////// ADDING TEMPLATES ////////////////////////////
	    final JMenu templates = new JMenu(Language.text("menu.shader.templates"));
	    // Populate only when sub-menu is opened, to avoid having spurious menu
	    // options if a library is deleted, or a missing menu option if a library is added
	    ArrayList<Pair<String, String>> templist = readShaderTemplates();
  
	    templates.addMenuListener(new MenuListener() {
	    	
	      @Override
	      public void menuSelected(MenuEvent e) {
	    	  templates.removeAll();
	    	  // read the template_names.txt file from shadermode location in Processing folder
	    	  //reading from disk everytime we click templates option in shader menu? inefficient?
	    	  // or load all files in constructor, then changes in files will only show when PDE is restarted
	    	  for (int i = 0; i < templist.size(); i++) {
	    		  //System.out.println(templist.get(i));
	    		  String name = templist.get(i).fst;
	    		  int index = name.lastIndexOf('.');
	    		  
	    		  final JMenuItem jitem = new JMenuItem(Language.text(name.substring(0, index)));
	    		  //System.out.println(templist.get(i).fst);
		    	  
	    		  final int tempIndex = i;
	    		  jitem.addActionListener(new ActionListener() {
		    	      public void actionPerformed(ActionEvent e) {
		    	    	  try {
							addTemplateToFile(templist.get(tempIndex).snd);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}		    	    	  
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
	    shaderModeMenu.add(templates);
	    
	    
	    return shaderModeMenu;
  }
  
  
  
  
  
  
  
  
}