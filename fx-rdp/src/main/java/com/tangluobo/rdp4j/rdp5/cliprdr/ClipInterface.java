/* ClipInterface.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: 1.1 $
 * Author: $Author: brett $
 * Date: $Date: 2011/11/28 14:13:42 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose:
 */
package com.tangluobo.rdp4j.rdp5.cliprdr;

import com.tangluobo.rdp4j.RdesktopException;

import java.io.IOException;

/**
 * The channel side of clipboard redirection: what a {@link TypeHandler} needs
 * in order to move text between the local system clipboard and the wire.
 *
 * <p>Deliberately AWT-free. The local clipboard is accessed through
 * {@link com.tangluobo.rdp4j.clipboard.SystemClipboardAdapter}, which the
 * implementation owns.</p>
 */
public interface ClipInterface {

	/** Publish text to the local system clipboard. */
	void copyTextToClipboard(String text);

	/** Read the local system clipboard as text, or null when it holds none. */
	String getLocalText();

	void send_data(byte[] data, int length) throws RdesktopException, IOException;

	void send_null(int type, int status);
}
