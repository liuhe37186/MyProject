package com.zed3.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface OnBluetoothConnectStateChangedListener {
	public void onDeviceConnecting(BluetoothDevice device);
	public void onDeviceConnected(BluetoothDevice device);
	public void onDeviceDisConnecting(BluetoothDevice device);
	public void onDeviceDisConnected(BluetoothDevice device);
}
