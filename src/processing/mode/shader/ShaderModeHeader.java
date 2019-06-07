package processing.mode.shader;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import processing.app.Language;
import processing.app.Messages;
import processing.app.Platform;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.ui.Editor;
import processing.app.ui.EditorHeader;
import processing.app.ui.Toolkit;
import processing.app.ui.EditorHeader;

public class ShaderModeHeader extends EditorHeader{
	
	//////////////// Do not re-declare them!
	
	//Editor editor;
	//JMenu menu;
	//JPopupMenu popup;
	
	////////////////

	public ShaderModeHeader(Editor eddie) {
		super(eddie);
		System.out.println("CREATING SHADER MODE HEADER");
		// TODO Auto-generated constructor stub
	}
	
	
	public void rebuild() {
		super.rebuild();
	}


	@Override 
	public void rebuildMenu() {
	    System.out.println("rebuilding SHADER MODE HEADER");
	    
	    if (getMenu() != null) {
	      getMenu().removeAll();

	    } else {
	      setMenu(new JMenu());
	      setPopup(getMenu().getPopupMenu());
	      add(getPopup());
	      getPopup().setLightWeightPopupEnabled(true);

	      /*
	      popup.addPopupMenuListener(new PopupMenuListener() {
	          public void popupMenuCanceled(PopupMenuEvent e) {
	            // on redraw, the isVisible() will get checked.
	            // actually, a repaint may be fired anyway, so this
	            // may be redundant.
	            repaint();
	          }
	          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
	          public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
	        });
	      */
	    }
	    JMenuItem item;
	    final JRootPane rootPane = getEditor().getRootPane();
	    InputMap inputMap =
	      rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    ActionMap actionMap = rootPane.getActionMap();

	    Action action;
	    String mapKey;
	    KeyStroke keyStroke;
	    
	    /////////////////////////
	    item = Toolkit.newJMenuItemShift(Language.text("editor.header.new_shader_tab"), KeyEvent.VK_N);
	    action = new AbstractAction() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        getEditor().getSketch().handleNewCode();
	      }
	    };
	    mapKey = "editor.header.new_shader_tab";
	    //keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.SHORTCUT_SHIFT_KEY_MASK);
	    //inputMap.put(keyStroke, mapKey);
	    actionMap.put(mapKey, action);
	    item.addActionListener(action);
	    getMenu().add(item);
	    /////////////////////////
	    
	    item = Toolkit.newJMenuItemShift(Language.text("editor.header.new_tab"), KeyEvent.VK_N);
	    action = new AbstractAction() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        getEditor().getSketch().handleNewCode();
	      }
	    };
	    mapKey = "editor.header.new_tab";
	    //keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.SHORTCUT_SHIFT_KEY_MASK);
	    //inputMap.put(keyStroke, mapKey);
	    actionMap.put(mapKey, action);
	    item.addActionListener(action);
	    getMenu().add(item);

	    item = new JMenuItem(Language.text("editor.header.rename"));
	    action = new AbstractAction() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        getEditor().getSketch().handleRenameCode();
	      }
	    };
	    item.addActionListener(action);
	    getMenu().add(item);

	    item = Toolkit.newJMenuItemShift(Language.text("editor.header.delete"), KeyEvent.VK_D);
	    action = new AbstractAction() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        Sketch sketch = getEditor().getSketch();
	        if (!Platform.isMacOS() &&  // ok on OS X
	            getEditor().getBase().getEditors().size() == 1 &&  // mmm! accessor
	            sketch.getCurrentCodeIndex() == 0) {
	            Messages.showWarning(Language.text("editor.header.delete.warning.title"),
	                                 Language.text("editor.header.delete.warning.text"));
	        } else {
	          getEditor().getSketch().handleDeleteCode();
	        }
	      }
	    };
	    mapKey = "editor.header.delete";
	    //keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.SHORTCUT_SHIFT_KEY_MASK);
	    //inputMap.put(keyStroke, mapKey);
	    actionMap.put(mapKey, action);
	    item.addActionListener(action);
	    getMenu().add(item);

	    getMenu().addSeparator();

	    //  KeyEvent.VK_LEFT and VK_RIGHT will make Windows beep

	    mapKey = "editor.header.previous_tab";
	    item = Toolkit.newJMenuItemExt(mapKey);
	    action = new AbstractAction() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        getEditor().getSketch().handlePrevCode();
	      }
	    };
	    keyStroke = item.getAccelerator();
	    inputMap.put(keyStroke, mapKey);
	    actionMap.put(mapKey, action);
	    item.addActionListener(action);
	    getMenu().add(item);

	    mapKey = "editor.header.next_tab";
	    item = Toolkit.newJMenuItemExt(mapKey);
	    action = new AbstractAction() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        getEditor().getSketch().handleNextCode();
	      }
	    };
	    keyStroke = item.getAccelerator();
	    inputMap.put(keyStroke, mapKey);
	    actionMap.put(mapKey, action);
	    item.addActionListener(action);
	    getMenu().add(item);

	    Sketch sketch = getEditor().getSketch();
	    if (sketch != null) {
	      getMenu().addSeparator();

	      ActionListener jumpListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	          getEditor().getSketch().setCurrentCode(e.getActionCommand());
	        }
	      };
	      for (SketchCode code : sketch.getCode()) {
	        item = new JMenuItem(code.getPrettyName());
	        item.addActionListener(jumpListener);
	        getMenu().add(item);
	      }
	    }

	    Toolkit.setMenuMnemonics(getMenu());
	  }


}
