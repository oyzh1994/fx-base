/* MapDef.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.1 $
 * Author: $Author: brett $
 * Date: $Date: 2011/11/28 14:13:43 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Encapsulates an individual key mapping
 */
package com.tangluobo.rdp4j.keymapping;

import com.tangluobo.rdp4j.Options;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class MapDef {
	private boolean altDown;
	private boolean capslockDown;
	private boolean characterDef;
	private boolean ctrlDown;
	private final int FLAG_ALT = 0x04; // flag mask for an alt modifier
	private final int FLAG_CAPSLOCK = 0x08; // flag mask for a capslock modifier
	private final int FLAG_CTRL = 0x02; // flag mask for a control modifier
	// Flag masks for use in generating an integer modifiers value (for text
	// definition output)
	private final int FLAG_SHIFT = 0x01; // flag mask for a shift modifier
	private char keyChar;
	private int keyCode;
	private int keyLocation;
	private Options options;
	private int scancode;
	private boolean shiftDown;

	/**
	 * Constructor for a character-defined mapping definition
	 * 
	 * @param options options
	 * @param keyChar key character
	 * @param keyLocation key location
	 * @param scancode scan code
	 * @param ctrlDown CTRL down
	 * @param shiftDown Shift down
	 * @param altDown Alt down
	 * @param capslockDown CapsLock down
	 */
	public MapDef(Options options, char keyChar, int keyLocation, int scancode, boolean ctrlDown, boolean shiftDown,
			boolean altDown, boolean capslockDown) {
		this.options = options;
		this.keyChar = keyChar;
		this.characterDef = true;
		this.keyLocation = keyLocation;
		this.scancode = scancode;
		this.ctrlDown = ctrlDown;
		this.altDown = altDown;
		this.shiftDown = shiftDown;
		this.capslockDown = capslockDown;
	}

	/**
	 * Constructor for a keycode-defined mapping definition
	 * 
	 * @param options options
	 * @param keyCode key code
	 * @param keyLocation key location
	 * @param scancode scan code
	 * @param ctrlDown CTRL down
	 * @param shiftDown Shift down
	 * @param altDown Alt down
	 * @param capslockDown CapsLock down
	 */
	public MapDef(Options options, int keyCode, int keyLocation, int scancode, boolean ctrlDown, boolean shiftDown, boolean altDown,
			boolean capslockDown) {
		this.options = options;
		this.keyCode = keyCode;
		this.characterDef = false;
		this.keyLocation = keyLocation;
		this.scancode = scancode;
		this.ctrlDown = ctrlDown;
		this.altDown = altDown;
		this.shiftDown = shiftDown;
		this.capslockDown = capslockDown;
	}

	/**
	 * Constructor for a mapping definition based on a given string
	 * representation (as would be output to a stream by the writeToStream
	 * method).
	 * 
	 * @param options options
	 * @param definition One-line definition string
	 * @throws KeyMapException Any parsing errors which may occur
	 */
	public MapDef(Options options, String definition) throws KeyMapException {
		this.options = options;
		StringTokenizer st = new StringTokenizer(definition);
		try {
			// determine whether the definition is character-oriented
			characterDef = ((Integer.parseInt(st.nextToken()) == 1) ? true : false);
			// read in the character or keycode
			if (characterDef)
				keyChar = (char) Integer.parseInt(st.nextToken());
			else
				keyCode = Integer.parseInt(st.nextToken());
			// read in the key location
			keyLocation = Integer.parseInt(st.nextToken());
			// read in the scancode (from a HEX string)
			scancode = Integer.decode(st.nextToken()).intValue();
			// read in the modifiers and interpret
			int modifiers = Integer.parseInt(st.nextToken());
			shiftDown = ((modifiers & this.FLAG_SHIFT) != 0);
			ctrlDown = ((modifiers & this.FLAG_CTRL) != 0);
			altDown = ((modifiers & this.FLAG_ALT) != 0);
			capslockDown = ((modifiers & this.FLAG_CAPSLOCK) != 0);
		} catch (NumberFormatException nfEx) {
			throw new KeyMapException("" + nfEx.getMessage() + " is not numeric");
		} catch (NoSuchElementException nseEx) {
			throw new KeyMapException("Not enough parameters in definition");
		}
	}

	public boolean appliesTo(char c) {
		return ((characterDef) && (this.keyChar == c) && !(capslockDown));
	}

	public char getKeyChar() {
		return keyChar;
	}

	public int getKeyCode() {
		return keyCode;
	}

	/**
	 * Return the scancode associated with this mapping
	 * 
	 * @return scan code
	 */
	public int getScancode() {
		return scancode;
	}

	/**
	 * Return true if the keystroke defined in this mapping requires that the
	 * Alt key be down
	 * 
	 * @return alt down
	 */
	public boolean isAltDown() {
		return altDown;
	}

	/**
	 * Return true if the keystroke defined in this mapping requires that Caps
	 * Lock is on
	 * 
	 * @return capslock on
	 */
	public boolean isCapslockOn() {
		return capslockDown;
	}

	/**
	 * Return true if this mapping is defined by a character, false otherwise
	 * 
	 * @return character def
	 */
	public boolean isCharacterDef() {
		return characterDef;
	}

	/**
	 * Return true if the keystroke defined in this mapping requires that the
	 * Control key be down
	 * 
	 * @return ctrl down
	 */
	public boolean isCtrlDown() {
		return ctrlDown;
	}

	/**
	 * Return true if the keystroke defined in this mapping requires that the
	 * Shift key be down
	 * 
	 * @return shift down
	 */
	public boolean isShiftDown() {
		return shiftDown;
	}

}
