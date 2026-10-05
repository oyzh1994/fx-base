package com.tangluobo.rdp4j.layers.nla;

import cn.oyzh.common.util.StringUtil;
import com.tangluobo.rdp4j.CredentialProvider.CredentialType;
import com.tangluobo.rdp4j.Packet;
import com.tangluobo.rdp4j.layers.nla.NTLMState;

import java.io.IOException;

public class NTLMNegotiate implements com.tangluobo.rdp4j.layers.nla.PacketPayload {
	private com.tangluobo.rdp4j.layers.nla.NTLMState state;

	public NTLMNegotiate(NTLMState state) {
		this.state = state;
	}

	@Override
	public Packet write() throws IOException {
		String domain = "";
		String workstation = "";
		if ((state.getFlags() & com.tangluobo.rdp4j.layers.nla.NTLM.NTLMSSP_NEGOTIATE_VERSION) == 0) {
			/*
			 * If the NTLMSSP_NEGOTIATE_VERSION flag is set by the client
			 * application, the Version field MUST be set to the current version
			 * (section 2.2.2.10), the DomainName field MUST be set to a
			 * zero-length string, and the Workstation field MUST be set to a
			 * zero-length string.
			 */
			domain = state.getState().getCredential("ntlm", 0, CredentialType.DOMAIN);
			workstation = state.getState().getWorkstationName();
		}
		byte[] domainBytes = com.tangluobo.rdp4j.layers.nla.NTLM.NULL_BYTES;
		byte[] clientBytes = com.tangluobo.rdp4j.layers.nla.NTLM.NULL_BYTES;
		int flags = state.getFlags();
		if (StringUtil.isNotBlank(domain)) {
			flags |= com.tangluobo.rdp4j.layers.nla.NTLM.NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED;
		} else
			flags &= (com.tangluobo.rdp4j.layers.nla.NTLM.NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED ^ 0xffffffff);
		if (StringUtil.isNotBlank(workstation)) {
			flags |= com.tangluobo.rdp4j.layers.nla.NTLM.NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED;
		} else
			flags &= (com.tangluobo.rdp4j.layers.nla.NTLM.NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED ^ 0xffffffff);
		int pktlen = 16 + domainBytes.length + clientBytes.length + 16;
		if ((state.getFlags() & com.tangluobo.rdp4j.layers.nla.NTLM.NTLMSSP_NEGOTIATE_VERSION) != 0)
			pktlen += 8;
		com.tangluobo.rdp4j.layers.nla.NTLMPacket packet = new NTLMPacket(pktlen);
		packet.copyFromByteArray(com.tangluobo.rdp4j.layers.nla.NTLM.SIG, 0, 0, com.tangluobo.rdp4j.layers.nla.NTLM.SIG.length);
		packet.incrementPosition(com.tangluobo.rdp4j.layers.nla.NTLM.SIG.length);
		packet.setLittleEndian32(1);
		packet.setLittleEndian32(flags);
		int off = 32;
		if ((state.getFlags() & com.tangluobo.rdp4j.layers.nla.NTLM.NTLMSSP_NEGOTIATE_VERSION) != 0)
			off += 8;
		off += packet.setOffsetArray(off, domainBytes);
		packet.setOffsetArray(off, clientBytes);
		if ((state.getFlags() & NTLM.NTLMSSP_NEGOTIATE_VERSION) != 0)
			packet.setPacket(state.getClientVersion().write());
		return packet;
	}

	@Override
	public void read(Packet packet) throws IOException {
		throw new UnsupportedOperationException();
	}
}