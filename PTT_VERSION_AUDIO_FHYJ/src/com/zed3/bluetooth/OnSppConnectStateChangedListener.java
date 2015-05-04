package com.zed3.bluetooth;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;

public interface OnSppConnectStateChangedListener {

	public void onDeviceConnectting(BluetoothIBridgeDevice device);
	public void onDeviceConnectFailed(BluetoothIBridgeDevice device);
	public void onDeviceConnected(BluetoothIBridgeDevice device);
	public void onDeviceDisconnectting(BluetoothIBridgeDevice device);
	public void onDeviceDisconnected(BluetoothIBridgeDevice device);
	public void onDeviceFound(BluetoothIBridgeDevice device);
}
