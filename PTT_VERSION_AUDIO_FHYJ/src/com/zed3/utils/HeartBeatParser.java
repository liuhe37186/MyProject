package com.zed3.utils;


public class HeartBeatParser {
	//CCC:当前对讲组号码:状态\r\n
	public static HeartBeatGrpState parser(String msg){
		if(msg == null || msg.length()< 7 || !msg.startsWith("CCC:") || msg.split(":").length != 3) return null;
		String[] state = msg.split(":");
		HeartBeatGrpState grpState = new HeartBeatGrpState();
		grpState.setGrpName(state[1]);
		grpState.setGrpState(state[2].trim());
		return grpState;
	}
}
