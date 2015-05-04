package com.zed3.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.BluetoothManagerInterface.HeadSetConnectStateListener;
import com.zed3.dialog.DialogUtil;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;

public class HeadsetBluetoothUtil implements HeadsetBluetoothUtilInterface {

	private static HeadsetBluetoothUtil mInstance;
	private static boolean mGetProfileProxy;
	protected static BluetoothHeadset mBluetoothHeadset;
	protected static String tag = "HeadsetBluetoothUtil";
	protected BluetoothDevice mSCOConnectDevice;

    static{
    	mInstance = new HeadsetBluetoothUtil();
    }
    
	private HeadsetBluetoothUtil() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<BluetoothDevice> getHeadsetBluetooths(Context context) {
		// TODO Auto-generated method stub
		boolean success = ZMBluetoothManager.getInstance().mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, mProfileListener, BluetoothProfile.HEADSET);
		return null;
	}

	
	
	private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
		
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			
			
			String msg = "";
			switch (profile) {
			case BluetoothProfile.A2DP:
				Log.i(tag, "BluetoothProfile.A2DP onServiceConnected()");
				Toast.makeText(SipUAApp.mContext,"BluetoothProfile.A2DP connected",0).show();
				break;
			case BluetoothProfile.HEADSET:
				Log.i(tag, "BluetoothProfile.HEADSET onServiceConnected()");
				
				mBluetoothHeadset = (BluetoothHeadset) proxy;
				
				if (mHeadSetConnectStateListener != null) {
					mHeadSetConnectStateListener.onHeadSetServiceConnected(mBluetoothHeadset);
					mHeadSetConnectStateListener = null;
				}
				
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
					
					
//					Toast.makeText(SipUAApp.mContext,"SCO 正在连接 "+bluetoothDevice.getName(),0).show();
//					writeLog2File("SPP connect , onServiceConnected() SCO 正在连接 "+bluetoothDevice.getName());
//					Log.i(tag, "SCO 正在连接 "+bluetoothDevice.getName());
//					mBluetoothHeadset.startVoiceRecognition(bluetoothDevice);
					mSCOConnectDevice = bluetoothDevice;
//					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
					
				}else {
					msg = "BluetoothProfile.HEADSET connected device:";
					for (BluetoothDevice bluetoothDevice : connectedDevices) {
						msg += bluetoothDevice.getName()+",";
					}
					Log.i(tag, msg);
					
					
					BluetoothDevice bluetoothDevice = connectedDevices.get(0);
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
					
					
//					Toast.makeText(SipUAApp.mContext,"SCO 正在连接 "+bluetoothDevice.getName(),0).show();
//					writeLog2File("SPP connect , onServiceConnected() SCO 正在连接 "+bluetoothDevice.getName());
//					Log.i(tag, "SCO 正在连接 "+bluetoothDevice.getName());
//					mBluetoothHeadset.startVoiceRecognition(bluetoothDevice);
					mSCOConnectDevice = bluetoothDevice;
//					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
				}
				
				break;
			case BluetoothProfile.HEALTH:
				msg += "BluetoothProfile.HEALTH";
				Log.i(tag, msg);
				Toast.makeText(SipUAApp.mContext,msg,0).show();
				break;
			default:
				msg += "BluetoothProfile.???";
				Log.i(tag, msg);
				Toast.makeText(SipUAApp.mContext,msg,0).show();
				break;
			}
			
			closeProfileProxy();
		}
		
		public void onServiceDisconnected(int profile) {
			String msg = "";
			
			switch (profile) {
			case BluetoothProfile.A2DP:
				msg += "BluetoothProfile.A2DP";
				break;
			case BluetoothProfile.HEADSET:
				msg += "BluetoothProfile.HEADSET";
				mBluetoothHeadset = null;
				ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,mProfileListenerForSPP  BluetoothProfile.HEADSET onServiceConnected()  askUserToConnectBluetooth ");
				if (SipUAApp.isHeadsetConnected) {
					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
				}else {
					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
				}
//				if (TalkBackNew.getInstance() != null) {
//					TalkBackNew.getInstance().reInitModeButtons(AudioUtil.MODE_SPEAKER);
//				}else {
//					TalkBackNew.mAudioMode = AudioUtil.MODE_SPEAKER;
//				}
				
				if (ZMBluetoothManager.getInstance().mNeedAskUserToReconnectSpp) {
					ZMBluetoothManager.getInstance().askUserToConnectBluetooth();
				}
				break;
			case BluetoothProfile.HEALTH:
				msg += "BluetoothProfile.HEALTH";
				break;
			default:
				msg += "BluetoothProfile.???";
				break;
			}
			
			msg += "disconnected！！";
//			Toast.makeText(SipUAApp.mContext,msg,0).show();
			Log.i(tag, msg);
//			DialogUtil.showCheckDialog(SipUAApp.mContext, "SCO 连接中断，需要重新连接。", msg, "确定");
			closeProfileProxy();
		}
		
	};
	public HeadSetConnectStateListener mHeadSetConnectStateListener;


	@Override
	public BluetoothDevice getCurrentHeadsetBluetooth(Context context) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void initHeadSet() {
		// TODO Auto-generated method stub
		boolean success = ZMBluetoothManager.getInstance().mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, mProfileListener, BluetoothProfile.HEADSET);
	}
	
	public boolean connectZMBluetooth(Context context) {
		// TODO Auto-generated method stub
		ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth()");
		if (!ZMBluetoothManager.getInstance().isDeviceSupportBluetooth()) {
			Toast.makeText(context,context.getResources().getString(R.string.dev_nofity_2),0).show();
			ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth()  "+context.getResources().getString(R.string.dev_nofity_2));
			Log.i(tag, "手机不支持蓝牙");
			return false;
		}
		else if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
//			Toast.makeText(context,"蓝牙功能未开启",0).show();
//			ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth()  蓝牙功能未开启   askUserToEnableBluetooth");
//			Log.i(tag, "蓝牙功能未开启");
			ZMBluetoothManager.getInstance().enableAdapter();
		}
		Log.i(tag, "connectZMBluetooth()  get HeadSet connected devices");
		boolean success = ZMBluetoothManager.getInstance().mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, mProfileListener, BluetoothProfile.HEADSET);
		Log.i(tag, "connectZMBluetooth()  get HeadSet connected devices  success? "+success);
		
		if (!success) {
			Log.i(tag, "connectZMBluetooth()  askUserToConnectBluetooth");
			ZMBluetoothManager.getInstance().askUserToConnectBluetooth();
			ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth()  askUserToConnectBluetooth");
			return false;
		}
		ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth() wait for mBluetoothHeadset get service");
		return true;
	}

	public static HeadsetBluetoothUtil getInstance() {
		// TODO Auto-generated method stub
		return mInstance;
	}

	public void disConnectZMBluetooth(Context context) {
		// TODO Auto-generated method stub
		
	}

	public static void closeProfileProxy() {
		// TODO Auto-generated method stub
		if (mGetProfileProxy) {
			Log.i(tag, "closeProfileProxy() closeProfileProxy ...");
			ZMBluetoothManager.getInstance().mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
			mBluetoothHeadset = null;
			mGetProfileProxy = false;
		}else {
			Log.i(tag, "closeProfileProxy() need not closeProfileProxy");
		}
	}

}
