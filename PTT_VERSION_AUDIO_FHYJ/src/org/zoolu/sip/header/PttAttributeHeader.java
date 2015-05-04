package org.zoolu.sip.header;

public class PttAttributeHeader extends Header {
	
	public PttAttributeHeader(String hvalue) {
		super(SipHeaders.Ptt_Attribute, hvalue);
	}

	public PttAttributeHeader(Header hd) {
		super(hd);
	}
	
	public static String getString(String version,String thid){
		StringBuilder headerStr = new StringBuilder();
		headerStr.append("version=");
		headerStr.append('"');
		headerStr.append(version);
		headerStr.append('"');
		headerStr.append(",");
		headerStr.append("thid=");
		headerStr.append('"');
		headerStr.append(thid);
		headerStr.append('"');
		return headerStr.toString();
	}
}
