package com.zed3.ace;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;

import com.zed3.sipua.ui.Receiver;
public class NSManager {
	private static final String tag = "NSManager";

	public static int recordSessionId = 0;
	public static boolean recordReady = false;
	private static NoiseSuppressor nsInstance;
	
	public static void createRecordNS(int recordSessionId){
		NSManager.recordSessionId = recordSessionId;
		if(isDeviceSupportNS() && TestTools.isAECOPen(Receiver.mContext)){
			if(nsInstance != null){//œ»ªÿ ’
				releaseNS();
			}
			while(recordSessionId == 0){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while(nsInstance == null){
				nsInstance = NoiseSuppressor.create(recordSessionId);
				if(nsInstance == null){
				}else{
					break;
				}
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
			if(nsInstance != null){
				if(!nsInstance.getEnabled()){
					nsInstance.setEnabled(true);
				}
				recordReady = true;
			}
		}
	}
	public static void releaseNS(){
		if(nsInstance != null){
			nsInstance.setEnabled(false);
			nsInstance.release();
			nsInstance = null;
			recordReady = false;
		}
	}
	public static boolean isDeviceSupportNS() {
		if(getApiLevel() < 16) return false;
		boolean result = false;
		if (AcousticEchoCanceler.isAvailable()) {
			result = true;
		}else{
			result = false;
		}
		return result;
	}
	
	private static int getApiLevel() {
		int version = android.os.Build.VERSION.SDK_INT;
		return version;
	}
	public static void enable(boolean b) {
		if(nsInstance != null){
			nsInstance.setEnabled(b);
		}
	}
}
