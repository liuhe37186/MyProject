package com.zed3.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;

public interface BluetoothManagerInterface {

	public interface HeadSetConnectStateListener {
		/**
		 * ServiceDisConnected
		 */
		void onHeadSetServiceDisConnected(BluetoothHeadset headset);
		/**
		 * ServiceConnected
		 * @return
		 */
		void onHeadSetServiceConnected(BluetoothHeadset headset);
	}

	/**
	 * reconnect spp in background
	 * @param deviceAddress
	 * @param cycle
	 * @param maxTimes
	 */
	void startReConnectingSPP(String deviceAddress,long cycle,int maxTimes);
	
	/**
	 * stop reconnecting spp
	 * @return
	 */
	boolean stopReConnectingSPP();
	
	/**
	 * set HeadSetConnectStateListener
	 * @return
	 */
	boolean setHeadSetConnectStateListener(HeadSetConnectStateListener listener);
	/**
	 * set HeadSetConnectStateListener
	 * @return
	 */
	void setSppConnectStateListener(OnSppConnectStateChangedListener listener);
	/**
	 * set HeadSetConnectStateListener
	 * @return
	 */
	void removeSppConnectStateListener(OnSppConnectStateChangedListener listener);
	/**
	 * registerReceivers
	 * @return
	 */
	void registerReceivers(Context context);
	/**
	 * unregisterReceivers
	 * @return
	 */
	void unregisterReceivers(Context context);
	
	
}
