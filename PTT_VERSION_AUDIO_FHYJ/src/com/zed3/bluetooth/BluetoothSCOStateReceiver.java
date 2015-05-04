package com.zed3.bluetooth;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.BluetoothManagerInterface.HeadSetConnectStateListener;
import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;

public class BluetoothSCOStateReceiver extends BroadcastReceiver {

	public static final String ACTION_BLUETOOTH_CONTROL = "com.zed3.sipua_bluetooth";
	private String tag = "BluetoothSCOStateReceiver";
	
	private ZMBluetoothManager mInstance = ZMBluetoothManager.getInstance();
	protected BluetoothHeadset mBluetoothHeadset;
	private boolean flag;
	protected BluetoothDevice mSppConnectDevice;
	public static boolean isBluetoothAdapterEnabled;
	
	private static BluetoothSCOStateReceiver mReceiver;
	private static IntentFilter intentFilter;
	private static boolean isStarted; 
 
	static List<OnBluetoothAdapterStateChangedListener> bluetoothAdapterListeners = new ArrayList<OnBluetoothAdapterStateChangedListener>();
	static List<OnBluetoothConnectStateChangedListener> bluetoothConnectListeners = new ArrayList<OnBluetoothConnectStateChangedListener>();
    static{
    	mReceiver = new BluetoothSCOStateReceiver();
    	intentFilter = new IntentFilter(); 
//        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED); 
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED); 
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); 
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); 
        if ( BluetoothAdapter.getDefaultAdapter()!= null) {
        	isBluetoothAdapterEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
		}
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Settings.mNeedBlueTooth/* || !ZMBluetoothManager.getInstance().isSPPConnected()*/) {
			return ;
		}
		String action = intent.getAction();
		if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
			receiveScoAudioState(context,intent);
		}
		else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			receiveBluetoothAdapterState(context,intent);
		}
		else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
			receiveConnectionStateAudioState(context,intent);
		}
		
	}

	/**
	 *  String android.bluetooth.BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED"
		Intent used to broadcast the change in connection state of the local Bluetooth adapter to a profile of the remote device. 
		When the adapter is not connected to any profiles of any remote devices and it attempts a connection to a profile this intent will sent. 
		Once connected, this intent will not be sent for any more connection attempts to any profiles of any remote device. 
		When the adapter disconnects from the last profile its connected to of any remote device, this intent will be sent. 
		This intent is useful for applications that are only concerned about whether the local adapter is connected to 
		any profile of any device and are not really concerned about which profile. 
		For example, an application which displays an icon to display whether Bluetooth is connected or not can use this intent. 
		
		This intent will have 3 extras: 
		EXTRA_CONNECTION_STATE - The current connection state. 
		EXTRA_PREVIOUS_CONNECTION_STATE- The previous connection state. 
		BluetoothDevice.EXTRA_DEVICE - The remote device. 
		
		EXTRA_CONNECTION_STATE or EXTRA_PREVIOUS_CONNECTION_STATE 
		can be any of STATE_DISCONNECTED, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTING. 
	 * @param context
	 * @param intent
	 */
	private void receiveConnectionStateAudioState(Context context, Intent intent) {
		// TODO Auto-generated method stub
		int state = intent.getIntExtra(
				BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
		int previousState = intent.getIntExtra(
				BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);
		BluetoothDevice device = intent.getParcelableExtra(
				BluetoothDevice.EXTRA_DEVICE);
		
		String stateStr = getConnectionStateString(state);
		String previousStateStr = getConnectionStateString(previousState);
		String deviceName = "unkown device";
		if (device != null) {
			deviceName = device.getName();
		}
		stateStr += "  "+deviceName;
		MyLog.i(tag, previousStateStr+"-->"+stateStr);
		switch (state) {
		case BluetoothAdapter.STATE_CONNECTING:
			Toast.makeText(context,context.getResources().getString(R.string.status_1) + deviceName,0).show();
			MyLog.i(tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_CONNECTING  "+deviceName);
			break;
		case BluetoothAdapter.STATE_CONNECTED:
			Toast.makeText(context,context.getResources().getString(R.string.status_2) + deviceName,0).show();
			MyLog.i(tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_CONNECTED  "+deviceName);
			break;
		case BluetoothAdapter.STATE_DISCONNECTING:
			
			Toast.makeText(context,context.getResources().getString(R.string.status_3) + deviceName,0).show();
			MyLog.i(tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_DISCONNECTING  "+deviceName);
			break;
		case BluetoothAdapter.STATE_DISCONNECTED:
			Toast.makeText(context,context.getResources().getString(R.string.status_4) + deviceName,0).show();
			MyLog.i(tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_DISCONNECTED  "+deviceName);
			break;
			
		default:
			break;
		}
		
		
		for (OnBluetoothConnectStateChangedListener listener : bluetoothConnectListeners) {
			switch (state) {
			case BluetoothAdapter.STATE_CONNECTING:
				listener.onDeviceConnecting(device);
				break;
			case BluetoothAdapter.STATE_CONNECTED:
				listener.onDeviceConnected(device);
				break;
			case BluetoothAdapter.STATE_DISCONNECTING:
				listener.onDeviceDisConnecting(device);
				break;
			case BluetoothAdapter.STATE_DISCONNECTED:
				listener.onDeviceDisConnected(device);
				break;

			default:
				break;
			}
		}
	} 
	/**
	 * getHeadsetDevice for spp connect
	 */
	private void getHeadsetDevice() {
		// TODO Auto-generated method stub
		if (flag) {
			return;
		}
		HeadSetConnectStateListener listener = new HeadSetConnectStateListener() {

			@Override
			public void onHeadSetServiceDisConnected(
					BluetoothHeadset headset) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onHeadSetServiceConnected(
					BluetoothHeadset headset) {
				// TODO Auto-generated method stub
				String msg = "";
				mBluetoothHeadset = headset;
				List<BluetoothDevice> connectedDevices = mBluetoothHeadset.getConnectedDevices();
				
				if (connectedDevices.size() == 0) {
					msg = SipUAApp.mContext.getResources().getString(R.string.blv_notify);
					Log.i(tag, msg);
					Toast.makeText(SipUAApp.mContext,msg,0).show();
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 0 "+msg);
					ZMBluetoothManager.getInstance().askUserToConnectBluetooth();
				}else if (connectedDevices.size() == 1) {
					
					BluetoothDevice bluetoothDevice = connectedDevices.get(0);
					msg = "BluetoothProfile.HEADSET connected device:"+bluetoothDevice.getName();
					Log.i(tag, msg);
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 "+msg);
					
					String name = bluetoothDevice.getName();
					if (ZMBluetoothManager.getInstance().checkIsZM(bluetoothDevice.getName())) {
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 "+msg);
						ZMBluetoothManager.getInstance().connectSPP(bluetoothDevice);
					}else {
						Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.dev_nofity_1)+name,0).show();
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 当前设备为非蓝牙手咪！   "+name);
						ZMBluetoothManager.getInstance().askUserToConnectZMBluetooth(bluetoothDevice);
					}
					
				}else {
					msg = "BluetoothProfile.HEADSET connected device:";
					for (BluetoothDevice bluetoothDevice : connectedDevices) {
						msg += bluetoothDevice.getName()+",";
					}
					Log.i(tag, msg);
					BluetoothDevice bluetoothDevice = connectedDevices.get(0);
					mSppConnectDevice = bluetoothDevice;
					msg = "BluetoothProfile.HEADSET connected device:"+bluetoothDevice.getName();
					Log.i(tag, msg);
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = "+connectedDevices.size()+","+msg);
					
					String name = bluetoothDevice.getName();
					if (ZMBluetoothManager.getInstance().checkIsZM(bluetoothDevice.getName())) {
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = "+connectedDevices.size()+","+msg);
						ZMBluetoothManager.getInstance().connectSPP(bluetoothDevice);
					}else {
						Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.dev_nofity_1)+name,0).show();
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = "+connectedDevices.size()+", 当前设备为非蓝牙手咪！   "+name);
						ZMBluetoothManager.getInstance().askUserToConnectZMBluetooth(bluetoothDevice);
					}
				}
				
			}
			
		};
		ZMBluetoothManager.getInstance().setHeadSetConnectStateListener(listener );
		flag = true;
	}

	private String getConnectionStateString(int state) {
		// TODO Auto-generated method stub
		String stateStr = "";
		switch (state) {
		case BluetoothAdapter.STATE_CONNECTING:
			stateStr = "BluetoothAdapter.STATE_CONNECTING";
			break;
		case BluetoothAdapter.STATE_CONNECTED:
			stateStr = "BluetoothAdapter.STATE_CONNECTED";
			break;
		case BluetoothAdapter.STATE_DISCONNECTING:
			stateStr = "BluetoothAdapter.STATE_DISCONNECTING";
			break;
		case BluetoothAdapter.STATE_DISCONNECTED:
			stateStr = "BluetoothAdapter.STATE_DISCONNECTED";
			break;
		default:
			break;
		}
		return stateStr;
	} 
	/**
	 * getScoStateString for log
	 */
	private String getScoStateString(int state) {
		// TODO Auto-generated method stub
		String stateStr = "";
		switch (state) {
		case AudioManager.SCO_AUDIO_STATE_CONNECTING:
			stateStr = "AudioManager.SCO_AUDIO_STATE_CONNECTING";
			break;
		case AudioManager.SCO_AUDIO_STATE_CONNECTED:
			stateStr = "AudioManager.SCO_AUDIO_STATE_CONNECTED";
			break;
		case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
			stateStr = "AudioManager.SCO_AUDIO_STATE_DISCONNECTED";
			break;
		case AudioManager.SCO_AUDIO_STATE_ERROR:
			stateStr = "AudioManager.SCO_AUDIO_STATE_ERROR";
			break;

		default:
			break;
		}
		return stateStr;
	} 

//	 String android.bluetooth.BluetoothAdapter.EXTRA_STATE
//	 Used as an int extra field in ACTION_STATE_CHANGED intents to request the current power state. Possible values are: STATE_OFF, STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF,
	 /**
     * Used as an int extra field in {@link #ACTION_STATE_CHANGED}
     * intents to request the current power state. Possible values are:
     * {@link #STATE_OFF},
     * {@link #STATE_TURNING_ON},
     * {@link #STATE_ON},
     * {@link #STATE_TURNING_OFF},
     */
	private void receiveBluetoothAdapterState(Context context, Intent intent) {
		// TODO Auto-generated method stub
		int state = intent.getIntExtra(
				BluetoothAdapter.EXTRA_STATE, -1);
		int previousState = intent.getIntExtra(
				BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
		switch (state) {
		case BluetoothAdapter.STATE_TURNING_ON:
			MyLog.i(tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_TURNING_ON");
			break;
		case BluetoothAdapter.STATE_ON:
			MyLog.i(tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_ON");
			isBluetoothAdapterEnabled = true;
			break;
		case BluetoothAdapter.STATE_TURNING_OFF:
			MyLog.i(tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_TURNING_OFF");
			ZMBluetoothManager.getInstance().disConnectZMBluetooth(SipUAApp.mContext);
			break;
		case BluetoothAdapter.STATE_OFF:
			isBluetoothAdapterEnabled = false;
			MyLog.i(tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_OFF");
//			if (TalkBackNew.getInstance() != null) {
//				TalkBackNew.getInstance().reInitModeButtons(AudioUtil.MODE_SPEAKER);
//			}else {
//				TalkBackNew.mAudioMode = AudioUtil.MODE_SPEAKER;
//			}
//			AudioUtil.getInstance().setCustomMode(AudioUtil.TYPE_GROUPCALL, AudioUtil.MODE_SPEAKER);
//			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
			break;

		default:
			break;
		}
		
		for (OnBluetoothAdapterStateChangedListener listener : bluetoothAdapterListeners) {
			switch (state) {
			case BluetoothAdapter.STATE_TURNING_ON:
				listener.onStateTurnningOn();
				break;
			case BluetoothAdapter.STATE_ON:
				listener.onStateOn();
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				listener.onStateTurnningOff();
				break;
			case BluetoothAdapter.STATE_OFF:
				listener.onStateOff();
				break;

			default:
				break;
			}
		}
	}

	private void receiveScoAudioState(Context context, Intent intent) {
		// TODO Auto-generated method stub
		int state = intent.getIntExtra(
				AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
		int previousState = intent.getIntExtra(
				AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, -1);

		if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
		} else {// 等待一秒后再尝试启动SCO
		}
		String previousStateStr = getScoStateString(state);
		MyLog.i(tag, "receiveScoAudioState() previousState = "+previousStateStr);
		switch (state) {
		case AudioManager.SCO_AUDIO_STATE_CONNECTING:
			MyLog.i(tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_CONNECTING");
//			Toast.makeText(SipUAApp.mContext,"SCO_AUDIO_STATE_CONNECTING",0).show();
			
			break;
		case AudioManager.SCO_AUDIO_STATE_CONNECTED:
			MyLog.i(tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_CONNECTED");
//			Toast.makeText(SipUAApp.mContext,"SCO_AUDIO_STATE_CONNECTED",0).show();
//			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
			break;
		case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
			MyLog.i(tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_DISCONNECTED");
//			Toast.makeText(SipUAApp.mContext,"SCO_AUDIO_STATE_DISCONNECTED",0).show();
//			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
			break;
		case AudioManager.SCO_AUDIO_STATE_ERROR:
			MyLog.i(tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_ERROR");
//			Toast.makeText(SipUAApp.mContext,"SCO_AUDIO_STATE_ERROR",0).show();
			
			break;

		default:
			break;
		}
	}

	private synchronized void sleep(int i) {
	 	// TODO Auto-generated method stub
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void startReceive(Context mContext) {
		if (!isStarted) {
			mContext.registerReceiver(mReceiver, intentFilter);
		}
	}
	
	public static void stopReceive(Context mContext) {
		if (isStarted) {
			mContext.unregisterReceiver(mReceiver);
		}
	}

	/**
	 * setOnBluetoothAdapterStateChangedListener  call back
	 */
	public static void setOnBluetoothAdapterStateChangedListener(
			OnBluetoothAdapterStateChangedListener listener) {
		// TODO Auto-generated method stub
		bluetoothAdapterListeners.add(listener);
	}
	/**
	 * reMoveOnBluetoothAdapterStateChangedListener  give up call back
	 */
	public static void reMoveOnBluetoothAdapterStateChangedListener(
			OnBluetoothAdapterStateChangedListener listener) {
		// TODO Auto-generated method stub
		bluetoothAdapterListeners.add(listener);
	}
	/**
	 * setOnBluetoothConnectStateChangedListener  call back
	 */
	public static void setOnBluetoothConnectStateChangedListener(
			OnBluetoothConnectStateChangedListener listener) {
		// TODO Auto-generated method stub
		bluetoothConnectListeners.add(listener);
	}
	/**
	 * reMoveOnBluetoothConnectStateChangedListener  give up call back
	 */
	public static void reMoveOnBluetoothConnectStateChangedListener(
			OnBluetoothConnectStateChangedListener listener) {
		// TODO Auto-generated method stub
		bluetoothConnectListeners.add(listener);
	}
	
}
