package org.audio.audioEngine;

public class SlientCheck {

	static {
		try {
			System.loadLibrary("webrtcvad");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public  native int WebRtcVadCreate();
	
	public  native int WebRtcVadInit();
	
	public  native int WebRtcVadProcess(int fs, byte[] audio_frame,
			int frame_length);
	
	public  native int WebRtcVadFree();
	
	
}
