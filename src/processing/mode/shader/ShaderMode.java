package processing.mode.shader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import processing.app.Base;
import processing.app.SketchCode;
import processing.app.syntax.TokenMarker;
import processing.app.ui.Editor;
import processing.app.ui.EditorException;
import processing.app.ui.EditorState;
import processing.core.PApplet;
import processing.mode.java.JavaMode;

public class ShaderMode extends JavaMode {

	protected TokenMarker tokenMarkerShader;

	public ShaderMode(Base base, File folder) {
		super(base, folder);
	}

	@Override
	public Editor createEditor(Base base, String path, EditorState state) throws EditorException {
		return new ShaderEditor(base, path, state, this);
	}

	@Override
	public String getTitle() {
		return "Shader";
	}

	@Override
	public String[] getExtensions() {
		return new String[] { "pde", "java", "glsl" };
	}

	@Override
	public File[] getKeywordFiles() {
		return new File[] { new File(folder, "keywords.txt"), new File(folder, "keywords_shader.txt") };
	}

	@Override
	protected void loadKeywords(File keywordFile) throws IOException {
		if (keywordFile.getName().equals("keywords_shader.txt")) {
			tokenMarkerShader = createTokenMarker();
			// System.out.println("Calling loadShaderKeywords() because keywords_shader
			// detected");
			loadShaderKeywords(keywordFile, "#");
		} else {
			// System.out.println("Entered simple loadKeywords()");
			// overridden for Python, where # is an actual keyword
			loadKeywords(keywordFile, "#");
		}
	}

	protected void loadShaderKeywords(File keywordFile, String commentPrefix) throws IOException {
		BufferedReader reader = PApplet.createReader(keywordFile);

		String line = null;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().startsWith(commentPrefix)) {
				// Was difficult to make sure that mode authors were properly doing
				// tab-separated values. By definition, there can't be additional
				// spaces inside a keyword (or filename), so just splitting on tokens.
				String[] pieces = PApplet.splitTokens(line);
				if (pieces.length >= 2) {
					String keyword = pieces[0];
					String coloring = pieces[1];

					if (coloring.length() > 0) {
						tokenMarkerShader.addColoring(keyword, coloring);
					}
					if (pieces.length == 3) {
						String htmlFilename = pieces[2];
						if (htmlFilename.length() > 0) {
							// if the file is for the version with parens,
							// add a paren to the keyword
							if (htmlFilename.endsWith("_")) {
								keyword += "_";
							}
							// Allow the bare size() command to override the lookup
							// for StringList.size() and others, but not vice-versa.
							// https://github.com/processing/processing/issues/4224
							boolean seen = keywordToReference.containsKey(keyword);
							if (!seen || (seen && keyword.equals(htmlFilename))) {
								keywordToReference.put(keyword, htmlFilename);
							}
						}
					}
				}
			}
		}
		reader.close();
	}

	@Override
	public TokenMarker getTokenMarker(SketchCode code) {
		if (code.getExtension().equals("glsl")) {
			return getTokenMarkerShader();
		}
		return getTokenMarker();
	}

	public TokenMarker getTokenMarkerShader() {
		return tokenMarkerShader;
	}

	/*
	 * has to be defined for each mode, these folders (e.g Basic, Advanced) should
	 * already be present in the mode examples folder and the relevant projects
	 * under them
	 */
	@Override
	public File[] getExampleCategoryFolders() {
		return new File[] { new File(examplesFolder, "Basic"), new File(examplesFolder, "Advanced") };
	}

}
