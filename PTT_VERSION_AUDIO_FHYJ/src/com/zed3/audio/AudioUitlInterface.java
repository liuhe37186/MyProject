package com.zed3.audio;

import android.app.Activity;

/**
 * interface for audio control
 * @author oumogang
 *
 */
public interface AudioUitlInterface {

	/**
	 * user can shoose one mode for call,MODE_HOOK,MODE_SPEAKER or MODE_BLUETOOTH;
	 * @param mode
	 */
	void setAudioConnectMode(int mode);
	/**
	 * get current mode
	 * @return
	 */
	int getCurrentMode();
	/**
	 * get user mode
	 */
	int getCustomMode(int type);
	/**
	 * set user mode
	 */
	boolean setCustomMode(int type,int mode);
	/**
	 * audio record from bluetooth mic;
	 */
	void startBluetoothSCO();
	/**
	 * audio record from phone mic,not from bluetooth mic;
	 */
	void stopBluetoothSCO();
	/**
	 * check mode;
	 * @return 
	 */
	boolean checkMode(int mode);
	/**
	 * setVolumeControlStream;
	 * @return 
	 */
	public void setVolumeControlStream(Activity activity);
	
}
