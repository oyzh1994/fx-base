/* TextHandler.java
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

import com.tangluobo.rdp4j.Packet;
import com.tangluobo.rdp4j.RdesktopException;
import com.tangluobo.rdp4j.Utilities;
import com.tangluobo.rdp4j.rdp5.cliprdr.ClipInterface;
import com.tangluobo.rdp4j.rdp5.cliprdr.TypeHandler;

import java.io.IOException;

public class TextHandler extends TypeHandler {
	@Override
	public boolean formatValid(int format) {
		return (format == CF_TEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elusiva.rdp.rdp5.cliprdr.TypeHandler#handleData(com.elusiva.rdp.
	 * RdpPacket , int, com.elusiva.rdp.rdp5.cliprdr.ClipInterface)
	 */
	@Override
	public void handleData(Packet data, int length, ClipInterface c) {
		String thingy = "";
		for (int i = 0; i < length; i++) {
			int aByte = data.get8();
			if (aByte != 0)
				thingy += (char) (aByte & 0xFF);
		}
		c.copyTextToClipboard(thingy);
	}

	@Override
	public boolean mimeTypeValid(String mimeType) {
		return mimeType.equals("text");
	}

	@Override
	public String name() {
		return "CF_TEXT";
	}

	@Override
	public int preferredFormat() {
		return CF_TEXT;
	}

	@Override
	public void send_data(String localText, ClipInterface c) throws RdesktopException, IOException {
		if (localText == null || localText.isEmpty()) {
			return;
		}
		// CF_TEXT is an ANSI payload.
		String s = localText.replace('\n', (char) 0x0a);
		s = Utilities.strReplaceAll(s, "" + (char) 0x0a, "" + (char) 0x0d + (char) 0x0a);
		byte[] data = s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
		// Send the encoded byte count, not the character count.
		c.send_data(data, data.length);
	}
}
