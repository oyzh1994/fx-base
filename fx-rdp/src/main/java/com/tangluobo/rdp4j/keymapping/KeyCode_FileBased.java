/*
 * Copyright (c) 2005 Propero Limited Purpose: Read and supply keymapping
 * information from a file
 */
package com.tangluobo.rdp4j.keymapping;

import cn.oyzh.common.log.JulLog;
import com.tangluobo.rdp4j.Options;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

public class KeyCode_FileBased {
	public static final int DOWN = 1;
	public static final int QUIETDOWN = 3;
	public static final int QUIETUP = 2;
	public static final int SCANCODE_EXTENDED = 0x80;
	public static final int UP = 0;
	protected Options options;
	Vector keyMap = new Vector();
	private int mapCode = -1;

	/**
	 * Constructor for a keymap generated from a specified file.
	 *
	 * @param options options
	 * @param keyMapFile File containing keymap data
	 * @throws com.tangluobo.rdp4j.keymapping.KeyMapException on error
	 */
	public KeyCode_FileBased(Options options, String keyMapFile) throws com.tangluobo.rdp4j.keymapping.KeyMapException {
		this.options = options;
		InputStream in;
		File file = new File(keyMapFile);
		try {
			in = new FileInputStream(file);
			try {
				readMapFile(file.toURI().toURL(), in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new com.tangluobo.rdp4j.keymapping.KeyMapException("KeyMap file not found: " + keyMapFile);
		}
	}

	public KeyCode_FileBased(Options options, URL base, InputStream fstream) throws IOException, com.tangluobo.rdp4j.keymapping.KeyMapException {
		this.options = options;
		readMapFile(base, fstream);
	}

	/**
	 * Retrieve the scancode corresponding to the supplied character as defined
	 * within this object. Also update the mod array to hold any modifier keys
	 * that are required to send alongside it.
	 * 
	 * @param c Character to obtain scancode for
	 * @param mod List of modifiers to be updated by method
	 * @return Scancode of supplied key
	 */
	public int charToScancode(char c, String[] mod) {
		Iterator i = keyMap.iterator();
		int smallestDist = -1;
		com.tangluobo.rdp4j.keymapping.MapDef best = null;
		while (i.hasNext()) {
			com.tangluobo.rdp4j.keymapping.MapDef current = (com.tangluobo.rdp4j.keymapping.MapDef) i.next();
			if (current.appliesTo(c)) {
				best = current;
			}
		}
		if (best != null) {
			if (best.isShiftDown())
				mod[0] = "SHIFT";
			else if (best.isCtrlDown() && best.isAltDown())
				mod[0] = "ALTGR";
			else
				mod[0] = "NONE";
			return best.getScancode();
		} else
			return -1;
	}

	/**
	 * Get the RDP code specifying the key map in use
	 *
	 * @return ID for current key map
	 */
	public int getMapCode() {
		return mapCode;
	}

	/**
	 * Retrieve the scancode corresponding to the supplied character as defined
	 * within this object. Also update the mod array to hold any modifier keys
	 * that are required to send alongside it.
	 *
	 * @param c Character to obtain scancode for
	 * @return Scancode of supplied key
	 */
	public boolean hasScancode(char c) {
		Iterator i = keyMap.iterator();
		com.tangluobo.rdp4j.keymapping.MapDef best = null;
		while (i.hasNext()) {
			com.tangluobo.rdp4j.keymapping.MapDef current = (com.tangluobo.rdp4j.keymapping.MapDef) i.next();
			if (current.appliesTo(c)) {
				best = current;
			}
		}
		return (best != null);
	}

	/**
	 * Read in a keymap definition file and add mappings to internal keymap
	 * 
	 * @param base base
	 * @param fstream Stream connected to keymap file
	 * @throws IOException on error
	 * @throws com.tangluobo.rdp4j.keymapping.KeyMapException on error
	 */
	public void readMapFile(URL base, InputStream fstream) throws IOException, com.tangluobo.rdp4j.keymapping.KeyMapException {
		// JulLog.info("Stream-based keycode reader");
		int lineNum = 0; // current line number being parsed
		String line = ""; // contents of line being parsed
		if (fstream == null)
			throw new com.tangluobo.rdp4j.keymapping.KeyMapException("Could not find specified keymap file");
		boolean mapCodeSet = false;
		try {
			DataInputStream in = new DataInputStream(fstream);
			if (in == null)
				JulLog.warn("in == null");
			while (in.available() != 0) {
				lineNum++;
				line = in.readLine();
				char fc = 0x0;
				if ((line != null) && (line.length() > 0))
					fc = line.charAt(0);
				// ignore blank and commented lines
				if ((line != null) && line.startsWith("include ")) {
					URL newUrl = new URL(base, line.substring(8));
					InputStream incIn = newUrl.openStream();
					try {
						readMapFile(newUrl, incIn);
					} finally {
						incIn.close();
					}
				} else if ((line != null) && (line.length() > 0) && (fc != '#') && (fc != 'c')) {
					keyMap.add(new com.tangluobo.rdp4j.keymapping.MapDef(options, line)); // parse line into a
					// MapDef
					// object and add to list
				} else if (fc == 'c') {
					StringTokenizer st = new StringTokenizer(line);
					String s = st.nextToken();
					s = st.nextToken();
					mapCode = Integer.decode(s).intValue();
					mapCodeSet = true;
				}
			}
			in.close();
		} catch (NumberFormatException nfEx) {
			throw new com.tangluobo.rdp4j.keymapping.KeyMapException("" + nfEx.getMessage() + " is not numeric at line " + lineNum);
		} catch (NoSuchElementException nseEx) {
			throw new com.tangluobo.rdp4j.keymapping.KeyMapException("Not enough parameters in definition at line " + lineNum);
		} catch (com.tangluobo.rdp4j.keymapping.KeyMapException kmEx) {
			kmEx.printStackTrace();
			throw new com.tangluobo.rdp4j.keymapping.KeyMapException("Error parsing keymap file: " + kmEx.getMessage() + " at line " + lineNum);
		} catch (Exception e) {
			JulLog.error(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			throw new com.tangluobo.rdp4j.keymapping.KeyMapException(e.getClass().getName() + ": " + e.getMessage());
		}
		if (!mapCodeSet)
			throw new KeyMapException("No map identifier found in file");
	}
}
