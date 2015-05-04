package com.zed3.ace;

import android.media.audiofx.AcousticEchoCanceler;

import com.zed3.sipua.ui.Receiver;

public class AECManager {
	private static final String tag = "AECManager";

	public static int recordSessionId = 0;
	
	public static boolean recordReady = false;
	private static AcousticEchoCanceler aecInstance;
	
	public static void createRecordAEC(int recordSessionId){
		AECManager.recordSessionId = recordSessionId;
		if(isDeviceSupportAec() && TestTools.isAECOPen(Receiver.mContext)){
			if(aecInstance != null){//œ»ªÿ ’
				releaseAEC();
			}
			while(recordSessionId == 0){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while(aecInstance == null){
				aecInstance = AcousticEchoCanceler.create(recordSessionId);
				if(aecInstance == null){
				}else{
					break;
				}
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
			if(aecInstance != null){
				if(!aecInstance.getEnabled()){
					aecInstance.setEnabled(true);
				}
				recordReady = true;
			}
		}
//		TestTools.count++;
	}
	public static void releaseAEC(){
		if(aecInstance != null){
			aecInstance.setEnabled(false);
			aecInstance.release();
			aecInstance = null;
			recordReady = false;
		}
	}
	public static boolean isDeviceSupportAec() {
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
		if(aecInstance != null){
			aecInstance.setEnabled(b);
		}
	}
}
