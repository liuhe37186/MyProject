package com.zed3.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

public interface HeadsetBluetoothUtilInterface {

	List<BluetoothDevice> getHeadsetBluetooths(Context context);
	BluetoothDevice getCurrentHeadsetBluetooth(Context context);
	
}
