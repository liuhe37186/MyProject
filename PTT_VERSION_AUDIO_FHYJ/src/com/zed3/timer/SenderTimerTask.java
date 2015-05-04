package com.zed3.timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.zed3.net.RtpPacket;

import android.inputmethodservice.Keyboard.Key;

public class SenderTimerTask implements CustomTimerTask {

	Map<Integer, RtpPacket> map;
	private int seqnumber = 0;
	private RtpPacket packet;
	
	public SenderTimerTask(){
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (map == null) {
			map = new HashMap<Integer, RtpPacket>();
		}
		packet = null;
		packet = map.get(seqnumber );
		if (packet == null&&map.size()==0) {
			while (map.size()>0) {
				packet = map.get(seqnumber );
			}
		}
		
		seqnumber++;
	}
}
