package com.zed3.bluetooth;

import android.util.Log;

import com.zed3.bluetooth.SppMessageStorage.SppMessage;

public class SppMessageReceiver extends Thread {

	private SppMessageStorage mStorage;
	private boolean isRunning = true;
	private String tag = "SppMessageReceiver";

	public SppMessageReceiver(SppMessageStorage sppMessageStorage) {
		// TODO Auto-generated constructor stub
		this.mStorage = sppMessageStorage;
	}

	public void startReceiving() {
		// TODO Auto-generated method stub
		isRunning = true;
	}
	public void stopReceiving() {
		// TODO Auto-generated method stub
		isRunning = false;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageReceiver   start receiving");
		Log.i(tag, "SppMessageReceiver   start receiving");
		while (isRunning) {
			SppMessage message = mStorage.get();
			if (message != null) {
				String msg = message.getMessage();
				if (!message.isAvailable()) {
					Log.i(tag, "message.isAvailable() is false   continue");
					continue;
				}
				ZMBluetoothManager.getInstance().receive(msg);
	//			try {
	//				Thread.sleep(/*20*/10);
	//			} catch (InterruptedException e) {
	//				// TODO Auto-generated catch block
	//				stopSending();
	//				ZMBluetoothManager.getInstance().writeLog2File("SppMessageReceiver  InterruptedException  stop receiving");
	//				Log.i(tag, "SppMessageReceiver  InterruptedException  stop receiving");
	//				e.printStackTrace();
	//			}
			}else {
				ZMBluetoothManager.getInstance().writeLog2File("SppMessageReceiver  mStorage.get() return null");
				Log.i(tag, "SppMessageReceiver  mStorage.get() return null");
			}
		}
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageReceiver   stop receiving");
		Log.i(tag, "SppMessageReceiver   stop receiving");
		
	}

}
