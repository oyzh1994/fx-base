package com.tangluobo.rdp4j.layers.nla;

import com.tangluobo.rdp4j.jasn1.ber.types.BerType;

import java.io.IOException;

interface BerPayload {
	BerType write() throws IOException;
}