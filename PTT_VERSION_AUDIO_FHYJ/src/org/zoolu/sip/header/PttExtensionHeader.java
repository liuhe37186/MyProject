package org.zoolu.sip.header;

//import org.zoolu.sip.provider.SipParser;

/** SIP Header Ptt-Extension */
public class PttExtensionHeader extends Header {
	
	public PttExtensionHeader(String hvalue) {
		super(SipHeaders.Ptt_Extension, hvalue);
	}

	public PttExtensionHeader(Header hd) {
		super(hd);
	}
}
