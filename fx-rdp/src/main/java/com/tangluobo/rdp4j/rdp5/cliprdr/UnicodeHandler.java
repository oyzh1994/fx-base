/* UnicodeHandler.java
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

/**
 * Registered in {@link ClipChannel} only as a placeholder slot:
 * {@code FixedClipChannel} reflectively swaps this instance out for
 * {@link com.tangluobo.rdp4j.clipboard.FixedUnicodeHandler}, which encodes with
 * the UTF-16LE the protocol actually requires. Kept so that swap stays
 * type-safe.
 */
public class UnicodeHandler extends TypeHandler {
	@Override
	public boolean formatValid(int format) {
		return (format == CF_UNICODETEXT);
	}

	public byte[] fromText(String s) {
		if (s == null) {
			return null;
		}
		String normalised = s.replace('\n', (char) 0x0a);
		normalised = Utilities.strReplaceAll(normalised, "" + (char) 0x0a, "" + (char) 0x0d + (char) 0x0a);
		byte[] sBytes = normalised.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
		return sBytes;
	}

	@Override
	public void handleData(Packet data, int length, ClipInterface c) {
		StringBuilder thingy = new StringBuilder();
		for (int i = 0; i + 1 < length; i += 2) {
			int aByte = data.getLittleEndian16();
			if (aByte == 0)
				break;
			thingy.append((char) aByte);
		}
		c.copyTextToClipboard(thingy.toString());
	}

	@Override
	public boolean mimeTypeValid(String mimeType) {
		return mimeType.equals("text");
	}

	@Override
	public String name() {
		return "CF_UNICODETEXT";
	}

	@Override
	public int preferredFormat() {
		return CF_UNICODETEXT;
	}

	@Override
	public void send_data(String localText, ClipInterface c) throws RdesktopException, IOException {
		byte[] data = fromText(localText);
		if (data == null || data.length == 0) {
			return;
		}
		c.send_data(data, data.length);
	}
}
