package com.zed3.bluetooth;

public interface OnBluetoothAdapterStateChangedListener {
	public void onStateTurnningOn();
	public void onStateOn();
	public void onStateTurnningOff();
	public void onStateOff();
}
