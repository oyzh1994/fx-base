package com.tangluobo.rdp4j.layers.nla;

import com.tangluobo.rdp4j.Packet;

import java.io.IOException;

interface PacketPayload {
	Packet write() throws IOException;
	
	void read(Packet packet) throws IOException;
}