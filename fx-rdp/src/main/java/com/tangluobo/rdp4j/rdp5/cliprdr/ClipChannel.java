/* ClipChannel.java
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
import com.tangluobo.rdp4j.SecurityType;
import com.tangluobo.rdp4j.layers.Secure;
import com.tangluobo.rdp4j.rdp5.VChannel;
import com.tangluobo.rdp4j.rdp5.VChannels;

import java.io.IOException;

/**
 * Shared packet plumbing for the cliprdr virtual channel.
 *
 * <p>Abstract: the concrete protocol handling lives in
 * {@link com.tangluobo.rdp4j.clipboard.FixedClipChannel}, which overrides
 * {@code process} and reaches the package-private helpers below. Local
 * clipboard access is expressed through {@link ClipInterface} so this class
 * needs no platform clipboard dependency.</p>
 */
public abstract class ClipChannel extends VChannel implements ClipInterface {
	// Message types
	public static final int CLIPRDR_CONNECT = 1;
	public static final int CLIPRDR_DATA_REQUEST = 4;
	public static final int CLIPRDR_DATA_RESPONSE = 5;
	public static final int CLIPRDR_ERROR = 2;
	public static final int CLIPRDR_FORMAT_ACK = 3;
	public static final int CLIPRDR_FORMAT_ANNOUNCE = 2;
	// Message status codes
	public static final int CLIPRDR_REQUEST = 0;
	public static final int CLIPRDR_RESPONSE = 1;
	// All type handlers available
	TypeHandlerList allHandlers = null;
	// TypeHandler for data currently being awaited
	TypeHandler currentHandler = null;

	protected ClipChannel() {
		// initialise all clipboard format handlers
		allHandlers = new TypeHandlerList();
		allHandlers.add(new UnicodeHandler());
		allHandlers.add(new TextHandler());
	}

	@Override
	public int flags() {
		return VChannels.CHANNEL_OPTION_INITIALIZED | VChannels.CHANNEL_OPTION_ENCRYPT_RDP | VChannels.CHANNEL_OPTION_COMPRESS_RDP
				| VChannels.CHANNEL_OPTION_SHOW_PROTOCOL;
	}

	/*
	 * VChannel inherited abstract methods
	 */
	@Override
	public String name() {
		return "cliprdr";
	}

	@Override
	public void send_data(byte[] data, int length) throws RdesktopException, IOException {
		try {
			state.getCommLock().acquire();
			try {
				Packet all = new Packet(12 + length);
				all.setLittleEndian16(CLIPRDR_DATA_RESPONSE);
				all.setLittleEndian16(CLIPRDR_RESPONSE);
				all.setLittleEndian32(length + 4); // don't know why, but we
													// need to add
				// between 1 and 4 to the length,
				// otherwise the server cliprdr thread hangs
				all.copyFromByteArray(data, 0, all.getPosition(), length);
				all.incrementPosition(length);
				all.setLittleEndian32(0);
				this.send_packet(all);
			} finally {
				state.getCommLock().release();
			}
		} catch (InterruptedException ie) {
			throw new RdesktopException("Interrupted waiting to send data.", ie);
		}
	}

	@Override
	public void send_null(int type, int status) {
		Packet s;
		s = new Packet(12);
		s.setLittleEndian16(type);
		s.setLittleEndian16(status);
		s.setLittleEndian32(0);
		s.setLittleEndian32(0); // pad
		s.markEnd();
		try {
			this.send_packet(s);
		} catch (RdesktopException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	void handle_data_request(Packet data) throws RdesktopException, IOException {
		int format = data.getLittleEndian32();
		TypeHandler outputHandler = allHandlers.getHandlerForFormat(format);
		if (outputHandler != null) {
			outputHandler.send_data(getLocalText(), this);
		}
		// this.send_null(CLIPRDR_DATA_RESPONSE,CLIPRDR_ERROR);
	}

	void handle_data_response(Packet data, int length) {
		if (currentHandler != null)
			currentHandler.handleData(data, length, this);
		currentHandler = null;
	}

	void request_clipboard_data(int formatcode) throws RdesktopException, IOException {
		Packet s = secure.init(state.getSecurityType() == SecurityType.STANDARD ? Secure.SEC_ENCRYPT : 0, 24);
		s.setLittleEndian32(16); // length
		int flags = VChannels.CHANNEL_FLAG_FIRST | VChannels.CHANNEL_FLAG_LAST;
		if ((this.flags() & VChannels.CHANNEL_OPTION_SHOW_PROTOCOL) != 0)
			flags |= VChannels.CHANNEL_FLAG_SHOW_PROTOCOL;
		s.setLittleEndian32(flags);
		s.setLittleEndian16(CLIPRDR_DATA_REQUEST);
		s.setLittleEndian16(CLIPRDR_REQUEST);
		s.setLittleEndian32(4); // Remaining length
		s.setLittleEndian32(formatcode);
		s.setLittleEndian32(0); // Unknown. Garbage pad?
		s.markEnd();
		secure.send_to_channel(s, state.getSecurityType() == SecurityType.STANDARD ? Secure.SEC_ENCRYPT : 0, this.mcs_id());
	}

}
