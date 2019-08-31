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
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import processing.app.Base;
import processing.app.Language;
import processing.app.Mode;
import processing.app.Preferences;
import processing.app.Sketch;
import processing.app.Util;
import processing.app.ui.EditorState;
import processing.app.ui.Toolkit;
import processing.app.ui.EditorException;
import processing.app.ui.EditorHeader;
import processing.mode.java.JavaEditor;

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
  
  
  public JMenu buildShaderMenu() {
	  shaderModeMenu = new JMenu(Language.text("Shader"));
	    JMenuItem item;

	    item = Toolkit.newJMenuItem(Language.text("menu.debug.continue"), KeyEvent.VK_U);
	    item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	          handleContinue();
	        }
	      });
	    shaderModeMenu.add(item);
	    item.setEnabled(false);

	    return shaderModeMenu;
  }
  
  
  
  
  
  
  
  
}