package com.tangluobo.rdp4j.io;

import com.tangluobo.rdp4j.io.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DefaultIO implements IO {
	private static final int CONNECT_TIMEOUT_MILLIS = Integer.getInteger("rdp.connect.timeoutMillis", 10_000);

	private InetAddress address;
	private int port;
	private volatile Socket socket;
	private volatile boolean closed;
	private boolean lowLatency;

	public DefaultIO(InetAddress server, int port) {
		this.address = server;
		this.port = port;
	}

	@Override
	public void closeIO() throws IOException {
		Socket current;
		synchronized (this) {
			closed = true;
			current = socket;
			socket = null;
		}
		if (current == null) return;
		current.close();
	}

	@Override
	public void setLowLatency(boolean lowLatency) throws IOException {
		this.lowLatency = lowLatency;
		Socket current = socket;
		if (current != null && !current.isClosed()) {
			current.setTcpNoDelay(lowLatency);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		checkConnected();
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		checkConnected();
		return socket.getOutputStream();
	}

	public Socket getSocket() {
		return socket;
	}

	@Override
	public byte[] getPublicKey() {
		return new byte[0];
	}

	void checkConnected() throws IOException {
		Socket current = socket;
		if (current != null && !current.isClosed()) {
			return;
		}
		synchronized (this) {
			if (closed) {
				throw new java.net.SocketException("RDP IO already closed");
			}
			current = socket;
			if (current == null || current.isClosed()) {
				current = new Socket();
				socket = current;
			}
		}
		try {
			current.setTcpNoDelay(lowLatency);
			current.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT_MILLIS);
			if (closed || current.isClosed()) {
				throw new java.net.SocketException("RDP IO closed while connecting");
			}
		} catch (IOException | RuntimeException error) {
			synchronized (this) {
				if (socket == current) {
					socket = null;
				}
			}
			try {
				current.close();
			} catch (IOException ignored) {
			}
			throw error;
		}
	}

	@Override
	public String getAddress() {
		return address.getHostAddress();
	}
}
