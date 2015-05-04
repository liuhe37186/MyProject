package com.zed3.utils;

public class HeartBeatPacket {
	// RRR:ºÅÂë:nonce:MD5Öµ\r\n
	// ºÅÂë:nonce:ÃÜÂë
	String nonce, md5value, num, pwd, grpNum,grpState;
	private static String preUserName ="";
	private static String prePassword="";
	private static String mNonce;
	private static String sCallId;
	/**
	 * @param grpState ON/OFF
	 * */
	public HeartBeatPacket(String dialnum, String pwd, String grpNum,String grpState) {
		this(dialnum,pwd,grpNum,grpState,null);
	}
	public HeartBeatPacket(String dialnum, String pwd, String grpNum,String grpState,String callId) {

		if (preUserName.equals(dialnum) && prePassword.equals(pwd)) {
		} else {
			nonce = Tools.getRandomCharNum(8);
			mNonce = new String(nonce);
		}
		preUserName = dialnum;
		prePassword = pwd;
		md5value = packetMd5value(preUserName, prePassword, mNonce);
		num = dialnum;
		this.grpNum = grpNum;
		this.pwd = pwd;
		this.grpState = grpState;
		sCallId = callId;
	}

	private String packetMd5value(String dialnum, String pwd, String nonce) {
		StringBuffer sb = new StringBuffer();
		sb.append(dialnum).append(":").append(mNonce).append(":").append(pwd);
		return MD5.toMd5(sb.toString())/*md5(sb.toString())*/;
	}

	// CCC:µ±Ç°¶Ô½²×éºÅÂë\r\n
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RRR:").append(num).append(":").append(mNonce).append(":")
				.append(md5value).append("\r\n");
		if (grpNum.length() > 0) {
			buffer.append("CCC:").append(grpNum).append(":").append(grpState).append("\r\n");
		}
		buffer.append("CID:").append(sCallId).append("\r\n");
		return buffer.toString();
	}
}
