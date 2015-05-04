package com.zed3.bluetooth;

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeAdapter;
import com.ivt.bluetooth.ibridge.BluetoothIBridgeAdapter.DataReceiver;
import com.ivt.bluetooth.ibridge.BluetoothIBridgeAdapter.EventReceiver;
import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.SppMessageStorage.SppMessage;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

public class ZMBluetoothManager implements BluetoothManagerInterface,OnBluetoothConnectStateChangedListener{
	private static ZMBluetoothManager instance;
	ArrayList<OnSppConnectStateChangedListener> sppConnectStateChangedListeners = new ArrayList<OnSppConnectStateChangedListener>();
	static SppMessageStorage sppMessageStorage4Send = new SppMessageStorage();
	static SppMessageStorage sppMessageStorage4Receive = new SppMessageStorage();
	public static final String RESPOND_TYPE = "respond_type";
	public static final String RESPOND_TYPE_PTT = "respond_type_ptt";
	public static final String RESPOND_TYPE_VOL = "respond_type_vol";
	public static final String RESPOND_TYPE_FUNCTION = "respond_type_function";
	public static final String RESPOND_TYPE_PA_CONTROL = "respond_type_pa_control";
	public static final String RESPOND_TYPE_HEART = "respond_type_heart";
	public static final String RESPOND_TYPE_FHP_STATE = "respond_type_hfp_state";
	
	public static final String RESPOND_TYPE_CALL = "respond_type_call";
	public static final String RESPOND_ACTION = "respond_action";
	public static final String RESPOND_ACTION_PTT_DOWN = "respond_action_ptt_down";
	public static final String RESPOND_ACTION_PTT_UP = "respond_action_ptt_up";
	public static final String RESPOND_ACTION_PTT_DOWN_RECEIVED = "respond_action_ptt_down_received";
	public static final String RESPOND_ACTION_PTT_UP_RECEIVED = "respond_action_ptt_up_received";
	public static final String RESPOND_ACTION_VOL_SHORT_DOWN_RECEIVED = "respond_action_vol_short_down_received";
	public static final String RESPOND_ACTION_VOL_SHORT_UP_RECEIVED = "respond_action_vol_short_up_received";
	public static final String RESPOND_ACTION_VOL_LONG_DOWN_RECEIVED = "respond_action_vol_long_down_received";
	public static final String RESPOND_ACTION_VOL_LONG_UP_RECEIVED = "respond_action_vol_long_up_received";
	public static final String RESPOND_ACTION_FUNCTION_RECEIVED = "respond_action_function_received";
	
	
	public static final String ACTION_BLUETOOTH_RESPOND = "com.zed3.sipua_bluetooth_respond";

	
	
	public static final String CONTROL_TYPE = "control_type";
	public static final String CONTROL_TYPE_PTT = "control_type_ptt";
	public static final String CONTROL_TYPE_VOL = "control_type_vol";
	public static final String CONTROL_TYPE_FUNCTION = "control_type_function";
	public static final String CONTROL_TYPE_CALL = "control_type_call";
	public static final String CONTROL_ACTION = "control_action";
	public static final String CONTROL_ACTION_PTT_DOWN = "control_action_ptt_down";
	public static final String CONTROL_ACTION_PTT_UP = "control_action_ptt_up";
	public static final String CONTROL_ACTION_VOL_LONG_DOWN = "control_action_vol_long_down";
	public static final String CONTROL_ACTION_VOL_LONG_UP = "control_action_vol_long_up";
	public static final String CONTROL_ACTION_VOL_SHORT_DOWN = "control_action_vol_short_down";
	public static final String CONTROL_ACTION_VOL_SHORT_UP = "control_action_vol_short_up";
	public static final String CONTROL_ACTION_FUNCTION = "control_action_function";
	
	
	/**
	 * SEND MSG
	 */
	public static final String PTT_START = "R_START";
	public static final String PTT_STOP = "R_STOP";
	public static final String PTT_SUCCESS = "PTT_SUCC";
	public static final String PTT_WAITING = "PTT_WAIT";
	
	public static final String PTT_PA_ON = "PA_ON";
	public static final String PTT_PA_OFF = "PA_OFF";
	
	/**
	 * RECEIVE MSG
	 */
	public static final String RESPOND_PTT_START = "R_START_OK";
	public static final String RESPOND_PTT_STOP = "R_STOP_OK";
	public static final String RESPOND_PTT_SUCCESS = "PTT_SUCC_OK";
	public static final String RESPOND_PTT_WAITING = "PTT_WAIT_OK";
	
	public static final String RESPOND_PTT_PA_ON = "PA_ON_OK";
	public static final String RESPOND_PTT_PA_OFF = "PA_OFF_OK";
	
	
	public static final String REQUEST_ADDRESS = "get addr";
	public static final String REQUEST_DEVICE_NAME = "request device name";
	
	public static final String PTT_DOWN = "PTT_DOWN";
	public static final String PTT_UP = "PTT_UP";
	public static final String VOL_LONG_DOWN = "VOL_LONG_DOWN";
	public static final String VOL_LONG_UP = "VOL_LONG_UP";
	public static final String VOL_SHORT_DOWN = "VOL_SHORT_DOWN";
	public static final String VOL_SHORT_UP = "VOL_SHORT_UP";
	
	public static final String RESPOND_ADDRESS_HEAD = "addr:";
	public static final String RESPOND_DEVICE_NAME_HEAD = "device name:";
	
//	public static final String RESPOND_PTT_DOWN = "PTT_DOWN_OK";
//	public static final String RESPOND_PTT_UP = "PTT_UP_OK";
//	public static final String RESPOND_VOL_LONG_DOWN = "VOL_LONG_DOWN_OK";
//	public static final String RESPOND_VOL_LONG_UP = "VOL_LONG_UP_OK";
//	public static final String RESPOND_VOL_SHORT_DOWN = "VOL_SHORT_DOWN_OK";
//	public static final String RESPOND_VOL_SHORT_UP = "VOL_SHORT_UP_OK";
	
	
	private static final String FUNCTION = "FUNCTION";
	private static final String STATE_CODE_SCO_CONNECTED = "4";
	private static final String STATE_CODE_SCO_DISCONNECTED = "5";
	
	public static final String RESPOND_PTT_HEART = "HEART";
	private static final String VOL = "VOL";
	private static final String PTT = "PTT";
	
	public static final String ACTION_BLUETOOTH_CONTROL = "com.zed3.sipua_bluetooth";
	
	private boolean mNeedControlVol = false;
	
	static{
		instance = new ZMBluetoothManager();
	}


//	private boolean mIsBTServiceStarted;
//	private BluetoothDevice mConnectedDevice;
//	private BluetoothDevice mConnectedDevice;
	
	
	private ZMBluetoothManager() {
		// TODO Auto-generated constructor stub
	}


	public static ZMBluetoothManager getInstance() {
		// TODO Auto-generated method stub 
		return instance;
	}


	public String getLastConnectDevice(Context context) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = getSharedPreferences(context);
		return preferences.getString(Contants.KEY_LAST_ZMBLUETOOTH_SPP_ADDRESS, "");
	}
	public String getLastLogFileName(Context context) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = getSharedPreferences(context);
		return preferences.getString(Contants.KEY_LAST_ZMBLUETOOTH_LOGFILE_NAME, "");
	}
	public void saveLastLogFileName(Context context,String fileName) {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putString(Contants.KEY_LAST_ZMBLUETOOTH_LOGFILE_NAME, fileName);
		edit.commit();
	}

	private SharedPreferences getSharedPreferences(Context context) {
		// TODO Auto-generated method stub
		return context.getSharedPreferences("com.zed3.app", Context.MODE_PRIVATE);
	}

	private BluetoothIBridgeAdapter mIBridgeAdapter;
	private BluetoothIBridgeDevice mIBridgeDevice;
    private MyEventReceiver mEventReceiver  = new MyEventReceiver();
	private MyDataReceiver mDataReceiver = new MyDataReceiver();
//	private ArrayList<BluetoothIBridgeDevice> mSPPConnectedDevices = new ArrayList<BluetoothIBridgeDevice>();
	private HashMap<String,BluetoothIBridgeDevice> mSPPConnectedDevices = new HashMap<String,BluetoothIBridgeDevice>();
	private boolean mIsSPPConnected;
	private String tag = "ZMBluetoothManager";
	public boolean mNeedCheckVersion = true;

	public static boolean mNeedBroadCast = true;

	private static SimpleDateFormat formatter;
	class MyDataReceiver implements DataReceiver{

		private String msg;

		@Override
		public void onDataReceived(BluetoothIBridgeDevice device, byte[] buffer,
				int len) {
			mCurrentIBridgeDevice = device;
			// TODO Auto-generated method stub
			try {
				msg = new String(buffer,0,len,"utf-8");
				
				Log.i(tag, "SPP in ,device:"+device.getDeviceName()+" msg:"+msg);
				writeLog2File("SPP in ,device:"+device.getDeviceName()+" msg:"+msg);
				
//				if (mNeedCheckVersion ) {
//					checkVersion();
//				}else if () {
//					processMsg(msg);
//				}
				checkVersion();
				
//				processMsg(msg);
				for (int i = 0; i < /*5*/1; i++) {
					long time = System.currentTimeMillis();
					SppMessage sppMessage = new SppMessage(time, msg,SppMessage.TYPE_RECEIVE);
					sppMessageStorage4Receive.put(sppMessage);
				}
				
				
				
//				if (mNeedBroadCast ) {
//					checkMessageAndSendBroadcast(msg);
//				}else {
////					processMsg(msg);
//					synchronized (inMsgStorage) {
//						inMsgStorage.offer(new String(msg));
//					}
//				}
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void checkMessageAndSendBroadcast(String s) {
		// TODO Auto-generated method stub
    	if (s.contains(PTT)) {
			sendPTTBroadcast(s);
		}
    	else if (s.contains(VOL)&&mNeedControlVol ) {
			sendVolumeBroadcast(s);
		}
    	else if (s.contains(FUNCTION)) {
    		sendFunctionBroadcast(s);
    	}
	}
	
	public void checkVersion() {
		// TODO Auto-generated method stub
		
	}


	public void processMsg(String msg) {
		// TODO Auto-generated method stub
		Log.i(tag, "processMsg() ,msg:"+msg);
		
		if (msg.equals(RESPOND_PTT_START)) {
			
		}
		else if (msg.equals(RESPOND_PTT_STOP)) {
			
		}
		else if (msg.equals(RESPOND_PTT_SUCCESS)) {
			
		}
		else if (msg.equals(RESPOND_PTT_WAITING)) {
			
		}
		else if (msg.equals(RESPOND_PTT_PA_ON)) {
			
		}
		else if (msg.equals(RESPOND_PTT_PA_OFF)) {
			
		}
		else if (msg.equals(PTT_DOWN)) {
			if (checkCallState()) {
				Log.i(tag, "processMsg("+msg+") ,checkCallState() is true unprocess");
				
			}else {
				Log.i(tag, "processMsg("+msg+") ,PTTHandler.pressPTT(true)");
				Log.i(tag, "processMsg() ,GroupCallUtil.makeGroupCall(true)");
				GroupCallUtil.makeGroupCall(true, true);
			}
		}
		else if (msg.equals(PTT_UP)) {
			Log.i(tag, "processMsg() ,GroupCallUtil.makeGroupCall(false)");
			GroupCallUtil.makeGroupCall(false, true);
		}
		else if (msg.equals(VOL_LONG_DOWN)) {
			
		}
		else if (msg.equals(VOL_LONG_UP)) {
			
		}
		else if (msg.equals(FUNCTION)) {
			
		}
		else if (msg.equals(STATE_CODE_SCO_CONNECTED)) {
			
		}
		else if (msg.equals(STATE_CODE_SCO_DISCONNECTED)) {
			
		}
	}


	/**
	 * when ptt down,if UA_STATE_IDLE process,else unprocess
	 * @return
	 */
	private boolean checkCallState() {
		// TODO Auto-generated method stub
		boolean result = false;
		switch (Receiver.call_state) {
		case UserAgent.UA_STATE_HOLD:
			result = true;
			Log.i(tag, "checkCallState(),UA_STATE_HOLD unprocess");
			break;
		case UserAgent.UA_STATE_IDLE:
			result = false;
			break;
		case UserAgent.UA_STATE_INCALL:
			result = true;
			Log.i(tag, "checkCallState(),UA_STATE_INCALL unprocess");
			break;
		case UserAgent.UA_STATE_INCOMING_CALL:
			result = true;
			Log.i(tag, "checkCallState(),UA_STATE_INCOMING_CALL unprocess");
			break;
		case UserAgent.UA_STATE_OUTGOING_CALL:
			result = true;
			Log.i(tag, "checkCallState(),UA_STATE_OUTGOING_CALL unprocess");
			break;

		default:
			break;
		}
		return result;
	}


	/**
     * 收到蓝牙串口消息
     * @param s
     */
	public void sendPTTBroadcast(String s) {
		Intent intent = new Intent(ACTION_BLUETOOTH_CONTROL);
		// TODO Auto-generated method stub
		String event = "";
//		sendLinkDetectedMessage(s, 0);
		intent.putExtra(CONTROL_TYPE, CONTROL_TYPE_PTT);
		if (s.equals(PTT_DOWN)) {
			intent.putExtra(CONTROL_ACTION, CONTROL_ACTION_PTT_DOWN);
			event = "down";
		}else if (s.equals(PTT_UP)) {
			intent.putExtra(CONTROL_ACTION, CONTROL_ACTION_PTT_UP);
			event = "up";
		}else {
			return;
		}
		
		SipUAApp.mContext.sendBroadcast(intent );
	}
	public void sendVolumeBroadcast(String s) {
		Intent intent = new Intent(ACTION_BLUETOOTH_CONTROL);
		// TODO Auto-generated method stub
		String event = "";
//		sendLinkDetectedMessage(s, 0);
		intent.putExtra(CONTROL_TYPE, CONTROL_TYPE_VOL);
		if (s.equals(VOL_SHORT_DOWN)) {
			intent.putExtra(CONTROL_ACTION, CONTROL_ACTION_VOL_SHORT_DOWN);
			event = "down";
		}
		else if (s.equals(VOL_SHORT_UP)) {
			intent.putExtra(CONTROL_ACTION, CONTROL_ACTION_VOL_SHORT_UP);
			event = "up";
		}
		else if (s.equals(VOL_LONG_DOWN)) {
			intent.putExtra(CONTROL_ACTION, CONTROL_ACTION_VOL_LONG_DOWN);
			event = "up";
		}
		else if (s.equals(VOL_LONG_UP)) {
			intent.putExtra(CONTROL_ACTION, CONTROL_ACTION_VOL_LONG_UP);
			event = "up";
		}
		else {
			return;
		}
//		if (isAdaptForPTT ) {
//		sendBroadcast(intent );
//	}
//		SipUAApp.mContext.sendBroadcast(intent );
//		sendLinkDetectedMessage(event, 0);
//		sendLinkDetectedMessage("sendBroadcast"+event, 0);
	}
	boolean flag = true;
	//add by hu
//	public static void setInstanceVoid(){
//		instance = null;
//	}
	
	private ProgressDialog mProgressDialog;

	public Context mContext = SipUAApp.mContext;

	public BluetoothIBridgeDevice mCurrentIBridgeDevice ;
	public Thread sender;
	public SppMessageSender sppMessageSender;
	public SppMessageReceiver sppMessageReceiver;
	public void sendFunctionBroadcast(String s) {
		Intent intent = new Intent(ACTION_BLUETOOTH_CONTROL);
		// TODO Auto-generated method stub
		String event = "";
//		sendLinkDetectedMessage(s, 0);
		intent.putExtra(CONTROL_TYPE, CONTROL_TYPE_FUNCTION);
		if (s.equals(FUNCTION)) {
			intent.putExtra(CONTROL_ACTION, CONTROL_ACTION_FUNCTION);
			event = "down";
			
//			if (!isAdaptForPTT) {
//				if (flag) {
//					mBluetoothRecorder.startRecordAndPlaying();
//				}else {
//					mBluetoothRecorder.stopRecordAndPlaying();
//				}
//				flag = !flag;
//			}
		}
		else {
			return;
		}
//		if (isAdaptForPTT ) {
//			sendBroadcast(intent );
//		}
		SipUAApp.mContext.sendBroadcast(intent );
//		sendLinkDetectedMessage(event, 0);
//		sendLinkDetectedMessage("sendBroadcast"+event, 0);
	}

	
	class MyEventReceiver implements EventReceiver{

		@Override
		public void onDeviceConnectFailed(BluetoothIBridgeDevice device) {
			// TODO Auto-generated method stub
			dismissProgressDialog();
			
//			//mInstance.sendLinkDetectedMessage(device.getDeviceName()+"("+device.getDeviceAddress()+"\r\nonDeviceConnectFailed()", 0);
//			Log.i(tag, device.getDeviceName()+"\r\n"+device.getDeviceAddress()+"\r\nonDeviceConnectFailed()");
			Log.i(tag, "SPP 连接失败  "+device.getDeviceName()+":"+device.getDeviceAddress()+" onDeviceConnectFailed()");
			Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.spp_failed)+device.getDeviceName(),0).show();
//			if (mNeedReConnectSPP) {
//				Log.i(tag, "SPP 连接失败  10秒后自动重连  "+device.getDeviceName());
//				reconnectSPPHandler.sendMessageDelayed(reconnectSPPHandler.obtainMessage(), 10000);
//			}
			changeUIStateAndAudioMode(false);
			
			
			askUserToCheckZMBluetooth();
			writeLog2File("SPP state onDeviceConnectFailed() device "+device.getDeviceName());
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);

			for (OnSppConnectStateChangedListener listener : sppConnectStateChangedListeners) {
				listener.onDeviceConnectFailed(device);
			}
		}

		@Override
		public void onDeviceConnected(BluetoothIBridgeDevice device) {
			// TODO Auto-generated method stub
			Tools.bringtoFront(SipUAApp.mContext);
			dismissProgressDialog();
			//mInstance.sendLinkDetectedMessage(device.getDeviceName()+"("+device.getDeviceAddress()+"\r\nonDeviceConnected()", 0);
			Log.i(tag, "SPP 连接成功  "+device.getDeviceName()+","+device.getDeviceAddress()+" onDeviceConnected()");
			Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.spp_success)+device.getDeviceName(),0).show();
//			DialogUtil.showCheckDialog(mContext, "设备扫描", "\r\n发现设备  "+ device.getDeviceName()+"\r\n"+device.getDeviceAddress(), "知道了");
			
			
//			mSPPConnectedDevices.add(device);
			mSPPConnectedDevices.put(device.getDeviceAddress(), device);
			
			
			
//			mIsAllSPPDisConnected = false;
//			new Thread(mInMsgProcessThread).start();
//			new Thread(mOutMsgProcessThread).start();
			
			mCurrentIBridgeDevice = device;
			writeLog2File("SPP state onDeviceConnected() device "+device.getDeviceName());

			startThreads();
			
			if (mLastSendMsg != null && !mLastSendMsg.equals("")) {
				sendSPPMessage(mLastSendMsg);
			}
			
			
			Activity activity = ZMBluetoothSelectActivity.getInstance();
			if (activity != null) {
				activity.finish();
			}
			activity = ZMBluetoothControlActivity.getInstance();
			if (activity != null) {
				activity.finish();
			}
			
			changeUIStateAndAudioMode(true);
			for (OnSppConnectStateChangedListener listener : sppConnectStateChangedListeners) {
				listener.onDeviceConnected(device);
			}
		}

		@Override
		public void onDeviceDisconnected(BluetoothIBridgeDevice device) {
			// TODO Auto-generated method stub
			//mInstance.sendLinkDetectedMessage(device.getDeviceName()+"("+device.getDeviceAddress()+"\r\nonDeviceDisconnected()", 0);
			Log.i(tag, "SPP 连接中断  "+device.getDeviceName()+","+device.getDeviceAddress()+" onDeviceDisconnected()");
			Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.spp_dis)+device.getDeviceName(),0).show();
//			DialogUtil.showCheckDialog(mContext, "SPP 连接",  device.getDeviceName()+"\r\n"+device.getDeviceAddress()+"\r\n连接中断，需要重新连接！", "知道了");
//			if (mNeedReConnectSPP) {
//				Log.i(tag, "SPP 连接中断  10秒后自动重连  "+device.getDeviceName());
//				reconnectSPPHandler.sendMessageDelayed(reconnectSPPHandler.obtainMessage(), 10000);
//			}
			
			
//			mSPPConnectedDevices.remove(device);
			BluetoothIBridgeDevice remove = mSPPConnectedDevices.remove(device.getDeviceAddress());
			if (mSPPConnectedDevices.size()== 0) {
				mIsAllSPPDisConnected = true;
				
				Log.i(tag, "SPP 所有连接设备已断开  停止通信  ");
				Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.hm_dis),0).show();
			}
			if (mNeedAskUserToReconnectSpp) {
				askUserToReConnectZMBluetooth();
			}
			writeLog2File("SPP state onDeviceDisconnected() device "+device.getDeviceName());
			
			changeUIStateAndAudioMode(false);
			for (OnSppConnectStateChangedListener listener : sppConnectStateChangedListeners) {
				listener.onDeviceDisconnected(device);
			}
			
			synchronized(ZMBluetoothManager.class){
				mCurrentIBridgeDevice = null;
			}
			
			GroupCallUtil.makeGroupCall(false, true);
			stopThreads();
		}

		@Override
		public void onDeviceFound(BluetoothIBridgeDevice device) {
			// TODO Auto-generated method stub
			//mInstance.sendLinkDetectedMessage(device.getDeviceName()+"("+device.getDeviceAddress()+"\r\nonDeviceFound()", 0);
			Log.i(tag , device.getDeviceName()+","+device.getDeviceAddress()+" onDeviceFound()");
//			DialogUtil.showCheckDialog(mContext, "设备扫描", "\r\n发现设备  "+ device.getDeviceName()+"\r\n"+device.getDeviceAddress(), "知道了");
			for (OnSppConnectStateChangedListener listener : sppConnectStateChangedListeners) {
				listener.onDeviceFound(device);
			}
		}

		@Override
		public void onDiscoveryFinished() {
			// TODO Auto-generated method stub
			//mInstance.sendLinkDetectedMessage("onDiscoveryFinished()", 0);
			Log.i(tag, "onDiscoveryFinished()");
//			DialogUtil.showCheckDialog(mContext, "设备扫描",  "\r\n设备扫描完成", "知道了");
		}
	}
	
//	public void connectSPP(Context context,BluetoothDevice device) {
//		// TODO Auto-generated method stub
////		if (mIsBTServiceStarted) {
////			mIsBTServiceStarted = false;
////			context.stopService(new Intent(context, BluetoothSPPService.class));
////		}
//		mContext = context;
////		mProgressDialog = DialogUtil.showProcessDailog(context, "正在连接  "+device.getName());
//		Toast.makeText(context,"正在连接  "+device.getName(),0).show();
//		BluetoothSPPService.mDevice = device;
//		mConnectedDevice = device;
//		
//		if (true) {
//			mIBridgeDevice = BluetoothIBridgeDevice.createBluetoothIBridgeDevice(device.getAddress() );
//			mIsSPPConnected = mIBridgeAdapter.connectDevice(mIBridgeDevice);
//			Log.i(tag, "SPP connect device："+device.getName());
//			return;
//		}
//		
//		disConnect(context, mConnectedDevice);
//		
//		try {
//			Thread.sleep(300);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		
//		
////		mIsBTServiceStarted = true;
////		context.startService(new Intent(context, BluetoothSPPService.class));
//	}
	
	private void dismissProgressDialog() {
		// TODO Auto-generated method stub
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	/**
	 * start spp message receiver and sender thread
	 */
	public void startThreads() {
		// TODO Auto-generated method stub
		stopThreads();
		
		sppMessageSender = new SppMessageSender(sppMessageStorage4Send);
		sppMessageSender.startSending();
		sppMessageSender.start();
		writeLog2File("SPP state onDeviceConnected() sppMessageSender.startSending();");
		
		sppMessageReceiver = new SppMessageReceiver(sppMessageStorage4Receive);
		sppMessageReceiver.startReceiving();
		sppMessageReceiver.start();
		writeLog2File("SPP state onDeviceConnected() sppMessageSender.startReceiving();");
	}
	/**
	 * stop spp message receiver and sender thread
	 */
	public void stopThreads() {
	// TODO Auto-generated method stub
		if (sppMessageSender != null) {
			sppMessageSender.stopSending();
			sppMessageSender.interrupt();
			try {
				sppMessageSender.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				sppMessageSender = null;
			}
			writeLog2File("SPP state onDeviceDisconnected() sppMessageSender.stopSending() ");
		}
		if (sppMessageReceiver != null) {
			sppMessageReceiver.stopReceiving();
			sppMessageReceiver.interrupt();
			try {
				sppMessageReceiver.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				sppMessageReceiver = null;
			}
			writeLog2File("SPP state onDeviceDisconnected() sppMessageSender.stopSending() ");
		}
	}


	public void disConnect(Context context,BluetoothDevice device) {
		// TODO Auto-generated method stub
		if (true) {
			if (mIBridgeDevice != null) {
				mIBridgeAdapter.disconnectDevice(mIBridgeDevice);
				
				Log.i(tag, "SPP disconnect device："+device.getName());
				mIBridgeDevice = null;
			}else {
				Toast.makeText(context, context.getResources().getString(R.string.no_notify), Toast.LENGTH_SHORT).show();
//				sendLinkDetectedMessage( "未连接，不需要断开！",0);
			}
			return;
		}
//		if (BluetoothSPPService.isCreated) {
//			context.stopService(new Intent(context, BluetoothSPPService.class));
////			mIsBTServiceStarted = false;
//			mConnectedDevice = null;
//		}
//		mIsBTServiceStarted = true;
//		context.startService(new Intent(context, BluetoothSPPService.class));
	}
	
	
	
	
	
	

	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

	private boolean mGetProfileProxy;

	private boolean mAntoConnectSCO;

	private BluetoothDevice mSCOConnectDevice;

	private AudioManager mAudioManager = ((AudioManager)SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE));
	public BluetoothDevice findBondedDevice(Context context, String address) {
		// TODO Auto-generated method stub
		if(mBtAdapter == null)return null;
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		for (BluetoothDevice device : pairedDevices) {
//    		String name = device.getName();
//    		if (checkIsZM(name)) {
//    			list.add(new SiriListItem(device.getName()+"("+getStateStr(device)+")" + "\n" + device.getAddress(), true));
//    		}
			if (device.getAddress().equals(address)) {
				return device;
			}
    	}
		return null;
	}
	
	boolean checkIsZM(String name) {
		// TODO Auto-generated method stub
		if (name != null&&name.startsWith("ZM")&&name.length()==8) {
			return true;
		}
		return false;
	}
	private String getStateStr(BluetoothDevice device) {
		// TODO Auto-generated method stub
		String stateStr = " unknow";
		switch (device.getBondState()) {
		case BluetoothDevice.BOND_NONE:
			
//			stateStr = " is not bonded";
			stateStr = SipUAApp.mContext.getResources().getString(R.string.bl_hm);
			break;
		case BluetoothDevice.BOND_BONDING:
//			stateStr = " is bonding";
			stateStr = SipUAApp.mContext.getResources().getString(R.string.bl_hm);
			break;
		case BluetoothDevice.BOND_BONDED:
			
			stateStr = SipUAApp.mContext.getResources().getString(R.string.bl_hm);
//			stateStr = " is bonded ";
			break;

		default:
			break;
		}
		return stateStr ;
	}


	public boolean isDeviceSupportBluetooth() {
		// TODO Auto-generated method stub
		return mBtAdapter == null?false:true;
	}


	public List<BluetoothDevice> getBondedZMDevices() {
		// TODO Auto-generated method stub
		List<BluetoothDevice> devices = new ArrayList();
		if(mBtAdapter == null)return devices;
		
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		for (BluetoothDevice device : pairedDevices) {
//    		String name = device.getName();
//    		if (checkIsZM(name)) {
//    			list.add(new SiriListItem(device.getName()+"("+getStateStr(device)+")" + "\n" + device.getAddress(), true));
//    		}
			if (checkIsZM(device.getName())) {
				devices.add(device);
			}
			
    	}
		return devices;
	}


	public boolean isBluetoothAdapterEnabled() {
		// TODO Auto-generated method stub
		if(mBtAdapter == null)return false;
		
		return mBtAdapter.isEnabled();
	}


	public BluetoothIBridgeDevice getSPPConnectedDevice() {
		// TODO Auto-generated method stub
		return mIBridgeAdapter != null?mIBridgeAdapter.getLastConnectedDevice():null;
	}
	
//	public ArrayList<BluetoothIBridgeDevice> getSPPConnectedDevices() {
//		// TODO Auto-generated method stub
//		return mSPPConnectedDevices;
//	}
	public HashMap<String,BluetoothIBridgeDevice> getSPPConnectedDevices() {
		// TODO Auto-generated method stub
		return mSPPConnectedDevices;
	}


	public void saveDevice() {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences(SipUAApp.mContext);
		Editor edit = sharedPreferences.edit();
//		edit.putString(Contants.KEY_LAST_ZMBLUETOOTH_SPP_ADDRESS, mConnectedDevice.getAddress());
		edit.putString(Contants.KEY_LAST_ZMBLUETOOTH_SPP_ADDRESS, mIBridgeDevice.getDeviceAddress());
		edit.commit();
	}


	public boolean getZMBluetoothOnOffState(Context context) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = getSharedPreferences(context);
		return preferences.getBoolean(Contants.KEY_LAST_ZMBLUETOOTH_SPP_ONOFF_STATE, false);
	}


	public void saveZMBluetoothOnOffState(boolean on) {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences(SipUAApp.mContext);
		Editor edit = sharedPreferences.edit();
		edit.putBoolean(Contants.KEY_LAST_ZMBLUETOOTH_SPP_ONOFF_STATE, on);
		edit.commit();
	}


	public void disConnectSPP(Context context, BluetoothDevice connectedDevice) {
		// TODO Auto-generated method stub
		disConnect(context, /*getConnectedDevice()*/connectedDevice);
	}


	public boolean isSPPConnected() {
		// TODO Auto-generated method stub
		return mSPPConnectedDevices.size()==0?false:true;
	}
 

	public void connectSCO(Context context) {
		// TODO Auto-generated method stub
		
		Log.i(tag, "connectSCO()");
//		if (!isDeviceSupportBluetooth()) {
//			Log.i(tag, "Device did not Support Bluetooth ... stop");
//			return;
//		}else if (!isBluetoothAdapterEnabled()) {
//			Log.i(tag, "Bluetooth is not enabled ask user to enable it ");
////			askUserToEnableBluetooth();
//			enableAdapter();
//			return;
//		}
//		
//		if (!mGetProfileProxy) {
//			Log.i(tag, "mGetProfileProxy is false! need initHFP again!");
//			mAntoConnectSCO = true;
//			initHFP(context);
//			return;
//		}else if (mBluetoothHeadset != null) {
//			mBluetoothHeadset.startVoiceRecognition(mSCOConnectDevice);
//			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
//			return;
//		}
//		writeLog2File("SCO connect , connectSCO()   initHFP");
//		initHFP(context);
		
		if (isSPPConnected()) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
		}else {
			AudioUtil.getInstance().startBluetoothSCO();
		}
	}
	public void disConnectSCO(Context context) {
//		// TODO Auto-generated method stub
//		Log.i(tag, "disConnectSCO()");
//		
//		if (!isDeviceSupportBluetooth()) {
//			Log.i(tag, "disConnectSCO() Bluetooth is not enabled,need not disconnect!");
//			return;
//		}else if (!isBluetoothAdapterEnabled()) {
//			Log.i(tag, "disConnectSCO() Bluetooth is not enabled,need not disconnect!");
//			return;
//		}
//		
//		if (!mGetProfileProxy) {
//			Log.i(tag, "mGetProfileProxy is false! need initHFP again!");
//			mAntoConnectSCO = true;
//			return;
//		}
//		
//		
//		if (mBluetoothHeadset != null) {
//			List<BluetoothDevice> connectedDevices = mBluetoothHeadset.getConnectedDevices();
//			for (BluetoothDevice bluetoothDevice : connectedDevices) {
//				mBluetoothHeadset.stopVoiceRecognition(bluetoothDevice);
//				Log.i(tag, "正在断开SCO,device: "+bluetoothDevice.getName());
//			}
//			
//			//delete by oumogang 2014-03-04
////			mBluetoothHeadset = null;
//		}else {
//			Log.i(tag, "mBluetoothHeadset == null ,need not disConnectSco ");
//		}
		
		if (isSPPConnected()) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
		}else {
			AudioUtil.getInstance().stopBluetoothSCO();
		}
		
//		AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
	}




	// Get the default adapter
	
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	private boolean profileProxy;
	
	protected BluetoothHeadset mBluetoothHeadset;
	
	
//	private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
//		
//		public void onServiceConnected(int profile, BluetoothProfile proxy) {
//			
//			
//			String msg = "";
//			switch (profile) {
//			case BluetoothProfile.A2DP:
//				Log.i(tag, "BluetoothProfile.A2DP onServiceConnected()");
//				Toast.makeText(SipUAApp.mContext,"BluetoothProfile.A2DP connected",0).show();
//				break;
//			case BluetoothProfile.HEADSET:
//				Log.i(tag, "BluetoothProfile.HEADSET onServiceConnected()");
//				
//				mBluetoothHeadset = (BluetoothHeadset) proxy;
//				List<BluetoothDevice> connectedDevices = mBluetoothHeadset.getConnectedDevices();
//				
//				if (connectedDevices.size() == 0) {
//					msg = "蓝牙设备语音未连接";
//					Log.i(tag, msg);
//					Toast.makeText(SipUAApp.mContext,msg,0).show();
//					writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 0 "+msg);
//					askUserToConnectBluetooth();
//				}else if (connectedDevices.size() == 1) {
//					
//					BluetoothDevice bluetoothDevice = connectedDevices.get(0);
//					msg = "BluetoothProfile.HEADSET connected device:"+bluetoothDevice.getName();
//					Log.i(tag, msg);
//					writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 "+msg);
//					
//					String name = bluetoothDevice.getName();
//					if (checkIsZM(bluetoothDevice.getName())) {
//						writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 "+msg);
//						connectSPP(bluetoothDevice);
//					}else {
//						Toast.makeText(SipUAApp.mContext,"当前设备为非蓝牙手咪！   "+name,0).show();
//						writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 当前设备为非蓝牙手咪！   "+name);
//						askUserToConnectZMBluetooth();
//					}
//					
//					
////					Toast.makeText(SipUAApp.mContext,"SCO 正在连接 "+bluetoothDevice.getName(),0).show();
////					writeLog2File("SPP connect , onServiceConnected() SCO 正在连接 "+bluetoothDevice.getName());
////					Log.i(tag, "SCO 正在连接 "+bluetoothDevice.getName());
////					mBluetoothHeadset.startVoiceRecognition(bluetoothDevice);
//					mSCOConnectDevice = bluetoothDevice;
////					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
//					
//				}else {
//					msg = "BluetoothProfile.HEADSET connected device:";
//					for (BluetoothDevice bluetoothDevice : connectedDevices) {
//						msg += bluetoothDevice.getName()+",";
//					}
//					Log.i(tag, msg);
//					
//					
//					BluetoothDevice bluetoothDevice = connectedDevices.get(0);
//					msg = "BluetoothProfile.HEADSET connected device:"+bluetoothDevice.getName();
//					Log.i(tag, msg);
//					writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = "+connectedDevices.size()+","+msg);
//					
//					String name = bluetoothDevice.getName();
//					if (checkIsZM(bluetoothDevice.getName())) {
//						writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = "+connectedDevices.size()+","+msg);
//						connectSPP(bluetoothDevice);
//					}else {
//						Toast.makeText(SipUAApp.mContext,"当前设备为非蓝牙手咪！   "+name,0).show();
//						writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = "+connectedDevices.size()+", 当前设备为非蓝牙手咪！   "+name);
//						askUserToConnectZMBluetooth();
//					}
//					
//					
////					Toast.makeText(SipUAApp.mContext,"SCO 正在连接 "+bluetoothDevice.getName(),0).show();
////					writeLog2File("SPP connect , onServiceConnected() SCO 正在连接 "+bluetoothDevice.getName());
////					Log.i(tag, "SCO 正在连接 "+bluetoothDevice.getName());
////					mBluetoothHeadset.startVoiceRecognition(bluetoothDevice);
//					mSCOConnectDevice = bluetoothDevice;
////					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
//				}
//				
//				break;
//			case BluetoothProfile.HEALTH:
//				msg += "BluetoothProfile.HEALTH";
//				Log.i(tag, msg);
//				Toast.makeText(SipUAApp.mContext,msg,0).show();
//				break;
//			default:
//				msg += "BluetoothProfile.???";
//				Log.i(tag, msg);
//				Toast.makeText(SipUAApp.mContext,msg,0).show();
//				break;
//			}
//			
//		}
//		
//		public void onServiceDisconnected(int profile) {
//			String msg = "";
//			
//			switch (profile) {
//			case BluetoothProfile.A2DP:
//				msg += "BluetoothProfile.A2DP";
//				break;
//			case BluetoothProfile.HEADSET:
//				msg += "BluetoothProfile.HEADSET";
//				mBluetoothHeadset = null;
//				writeLog2File("SPP connect ,mProfileListenerForSPP  BluetoothProfile.HEADSET onServiceConnected()  askUserToConnectBluetooth ");
//				AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
//				askUserToConnectBluetooth();
//				break;
//			case BluetoothProfile.HEALTH:
//				msg += "BluetoothProfile.HEALTH";
//				break;
//			default:
//				msg += "BluetoothProfile.???";
//				break;
//			}
//			
//			msg += "disconnected！！";
//			Toast.makeText(SipUAApp.mContext,msg,0).show();
//			Log.i(tag, msg);
//			DialogUtil.showCheckDialog(SipUAApp.mContext, "SCO 连接中断，需要重新连接。", msg, "确定");
//			askUserToConnectBluetooth();
//		}
//		
//	};

	private String mState;

	BluetoothDevice mLastSPPConnectDevice;
	private long lastTime;

	private static String mLastSendMsg = "";



	public boolean isSCOConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void connectSPP(BluetoothDevice device){
		// TODO Auto-generated method stub
		mNeedAskUserToReconnectSpp = true;
		mLastSPPConnectDevice = device;
		Log.i(tag, "connectSPP() device："+device.getName());
		if (mIBridgeAdapter == null) {
			Log.i(tag, "mIBridgeAdapter() == null need initSPP");
			writeLog2File("mIBridgeAdapter() == null need initSPP");
			initSPP(SipUAApp.mContext);
		}
		
		if (findSPPConnectedDevice(device)) {
			Log.i(tag, "mIBridgeAdapter() == null need initSPP");
			writeLog2File("findSPPConnectedDevice(device) "+device.getName()+"is true need not connect again ");
			return;
		}
		BluetoothIBridgeDevice iBridgeDevice = BluetoothIBridgeDevice.createBluetoothIBridgeDevice(device.getAddress() );
		
		//onDeviceConnectting 
		for (OnSppConnectStateChangedListener listener : sppConnectStateChangedListeners) {
			listener.onDeviceConnectting(iBridgeDevice);
		}
		
		Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.spp_connecting)+iBridgeDevice.getDeviceName(),0).show();
		mIsSPPConnected = mIBridgeAdapter.connectDevice(iBridgeDevice);
		if (mIsSPPConnected) {
			Log.i(tag, "SPP connect device："+iBridgeDevice.getDeviceName()+" success");
//			Toast.makeText(SipUAApp.mContext,"成功连接SPP "+iBridgeDevice.getDeviceName(),0).show();
//			mSPPConnectedDevices.add(iBridgeDevice);
			
//			mSPPConnectedDevices.put(iBridgeDevice.getDeviceAddress(), iBridgeDevice);
			
		}else {
			Log.i(tag, "SPP connect device："+iBridgeDevice.getDeviceName()+"faile");
			Toast.makeText(SipUAApp.mContext,SipUAApp.mContext.getResources().getString(R.string.hm_connect_failed)+iBridgeDevice.getDeviceName(),0).show();
		}
	}


	private boolean findSPPConnectedDevice(BluetoothDevice device) {
		// TODO Auto-generated method stub
		BluetoothIBridgeDevice iBridgeDevice = mSPPConnectedDevices.get(device.getAddress());
		return iBridgeDevice==null?false:true;
	}


	/**
	 * update ui for bluetooth state
	 * add by oumogang 2014-03-13
	 */
	private void changeUIStateAndAudioMode(boolean state) {
		// TODO Auto-generated method stub
//		try {
//			TalkBackNew.mIsBTServiceStarted = state;
//			TalkBackNew.mAudioMode = state?AudioUtil.MODE_BLUETOOTH:AudioUtil.MODE_SPEAKER;
//			AudioUtil.getInstance().setAudioConnectMode(state?AudioUtil.MODE_BLUETOOTH:AudioUtil.MODE_SPEAKER);
//			if (TalkBackNew.getInstance()!=null) {
//				TalkBackNew.getInstance().reInitBluetoothButton(state);
//				TalkBackNew.getInstance().reInitModeButtons(state?AudioUtil.MODE_BLUETOOTH:AudioUtil.MODE_SPEAKER);
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
	}
	
	public void askUserToSelectBluetooth() {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToConnectZMBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothSelectActivity.askUserToSelectBluetooth();
	}
	public void askUserToReConnectZMBluetooth() {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToConnectZMBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToReConnectZMBluetooth();
	}
	public void askUserToSelectHeadSetBluetooth() {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToConnectZMBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToSelectHeadSetBluetooth();
	}
	public void askUserToConnectZMBluetooth(BluetoothDevice bluetoothDevice) {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToConnectZMBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToConnectZMBluetooth(bluetoothDevice);
	}
	public void askUserToConnectBluetooth() {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToConnectBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToConnectBluetooth();
	}
	public void askUserToCheckZMBluetooth() {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToCheckZMBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToCheckZMBluetooth();
	}
	public void askUserToEnableBluetooth() {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToCheckZMBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToEnableBluetooth();
	}
	public void askUserToDisableBluetooth() {
		// TODO Auto-generated method stub
		Log.i(tag, "askUserToCheckZMBluetooth()");
		changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToDisableBluetooth();
	}


	@Deprecated
	public void sendSPPMessage(final String msg) {
		// TODO Auto-generated method stub
		//disable by mou 2015-01-05
//		for (int i = 0; i < /*20*//*5*/1; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, msg,SppMessage.TYPE_SEND);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		for (int i = 0; i < /*20*/2; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, PTT_SUCCESS);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		for (int i = 0; i < /*20*/2; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, PTT_STOP);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		for (int i = 0; i < /*20*/2; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, PTT_SUCCESS);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		for (int i = 0; i < /*20*/2; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, PTT_START);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		for (int i = 0; i < /*20*/2; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, PTT_STOP);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		for (int i = 0; i < /*20*/2; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, PTT_START);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		for (int i = 0; i < /*20*/2; i++) {
//			long time = System.currentTimeMillis();
//			SppMessage sppMessage = new SppMessage(time, PTT_SUCCESS);
//			sppMessageStorage4Send.put(sppMessage);
//		}
//		if (!checkTime(50)) {
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				mLastSendMsg  = msg;
//				if (mIBridgeAdapter == null) {
//					writeLog2File("error   mIBridgeAdapter == null");
//					Log.i(tag, "error   mIBridgeAdapter == null");
//					return;
//				}
////		if (mSPPConnectedDevices.size()<1) {
////			writeLog2File("error   mSPPConnectedDevices.size() == 0");
////			return;
////		}
//				if (mCurrentIBridgeDevice == null) {
//					writeLog2File("error   mCurrentIBridgeDevice == null,no device connect spp");
//					Log.i(tag, "error   mCurrentIBridgeDevice == null,no device connect spp");
//					return;
//				}
//				byte[] bytes = msg.getBytes();
//				
//				Log.i(tag, "SPP out ,device:"+mCurrentIBridgeDevice.getDeviceName()+" msg:"+msg/*+" begin"*/);
//				writeLog2File("SPP out ,device:"+mCurrentIBridgeDevice.getDeviceName()+" msg:"+msg);
//				mIBridgeAdapter.send(mCurrentIBridgeDevice, bytes, bytes.length);
////		Log.i(tag, "SPP out ,device:"+mCurrentIBridgeDevice.getDeviceName()+" msg:"+msg+" end");
//				
//				
////		for (BluetoothIBridgeDevice iBridgeDevice : mSPPConnectedDevices) {
////			mIBridgeAdapter.send(iBridgeDevice, bytes, bytes.length);
//////		Log.i(tag, "SPP send msg:"+msg);
////			Log.i(tag, "SPP out ,device:"+iBridgeDevice.getDeviceName()+" msg:"+msg);
////			writeLog2File("SPP out ,device:"+iBridgeDevice.getDeviceName()+" msg:"+msg);
////		}
//				
//			}
//		}).start();
	}


	private boolean checkTime(int cycle) {
		// TODO Auto-generated method stub
		long time = System.currentTimeMillis();
		if (lastTime == 0) {
			lastTime = time;
			return true;
		}
		if (time - lastTime<cycle) {
			return false;
		}else {
			lastTime = time;
		}
		
		return true;
	}

//	public void sendLinkDetectedMessage(String string, int i) {
//		// TODO Auto-generated method stub
//	}


//	public void send(String respondAction) {
//		// TODO Auto-generated method stub
//		if (mIsAllSPPDisConnected) {
//			return;
//		}
//		synchronized (outMsgStorage) {
//			//ptt down
//			String outMsg;
//			if (respondAction.equalsIgnoreCase(RESPOND_ACTION_PTT_DOWN_RECEIVED)) {
//				//mInstance.sendLinkDetectedMessage("RESPOND_ACTION_PTT_DOWN_RECEIVED", 0);
////			mNeedSetPaOn = checkNeedOpenSpeaker();
////			setPaOn(true);
////				sendSPPMessage(PTT_PA_ON);
//				outMsgStorage.offer(new String(PTT_PA_ON));
//			}
//			//ptt up
//			else if (respondAction.equalsIgnoreCase(RESPOND_ACTION_PTT_UP_RECEIVED)){
//				//mInstance.sendLinkDetectedMessage("RESPOND_ACTION_PTT_UP_RECEIVED", 0);
////			mNeedSetPaOff = checkNeedCloseSpeaker();
//			}
//			//ptt success
//			else if (respondAction.equalsIgnoreCase(RESPOND_ACTION_PTT_SUCCESS)){
//				mState = RESPOND_ACTION_PTT_SUCCESS;
//				//mInstance.sendSPPMessage(PTT_SUCCESS);
////				sendSPPMessage(PTT_SUCCESS);
//				outMsgStorage.offer(new String(PTT_SUCCESS));
//			}
//			//ptt waiting
//			else if (respondAction.equalsIgnoreCase(RESPOND_ACTION_PTT_WAITTING)){
//				//mState = RESPOND_ACTION_PTT_WAITTING;
////			//mInstance.sendSPPMessage(PTT_WAITING);
//				//mInstance.sendSPPMessage(PTT_STOP);
////				sendSPPMessage(PTT_STOP);
//				outMsgStorage.offer(new String(PTT_STOP));
//			}
//			//ptt listening
//			else if (respondAction.equalsIgnoreCase(RESPOND_ACTION_PTT_LISTENING)){
//				mState = RESPOND_ACTION_PTT_LISTENING;
//				//mInstance.sendSPPMessage(PTT_START);
////			mNeedSetPaOn = true;
////			setPaOn(true);
////				sendSPPMessage(PTT_START);
//				outMsgStorage.offer(new String(PTT_START));
////				sendSPPMessage(PTT_PA_ON);
//				outMsgStorage.offer(new String(PTT_PA_ON));
//			}
//			//ptt idle
//			else if (respondAction.equalsIgnoreCase(RESPOND_ACTION_PTT_IDLE)){
//				//mInstance.sendSPPMessage(PTT_STOP);
////			setPaOn(false);
////				sendSPPMessage(PTT_STOP);
//				outMsgStorage.offer(new String(PTT_STOP));
////				sendSPPMessage(PTT_PA_OFF);
//				if (mState == RESPOND_ACTION_PTT_LISTENING) {
//					outMsgStorage.offer(new String(PTT_PA_OFF));
//				}
//			}
//		}
//	}
	
	private Queue<String> inMsgStorage = new LinkedList<String>();

	public boolean mIsAllSPPDisConnected;
	
	private Queue<String> outMsgStorage = new LinkedList<String>();
	Runnable mOutMsgProcessThread = new Runnable() {
		String msg;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!mIsAllSPPDisConnected) {
				synchronized (outMsgStorage) {
					if (outMsgStorage.size() > 0 ) {
						msg = outMsgStorage.poll();
						if (msg != null) {
							sendSPPMessage(msg);
						}
					}
//					try {
//						Thread.sleep(50);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
		}
	};
	
	Runnable mInMsgProcessThread = new Runnable() {
		String msg;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!mIsAllSPPDisConnected) {
				synchronized (inMsgStorage) {
					if (inMsgStorage.size() > 0 ) {
						msg = inMsgStorage.poll();
						if (msg != null) {
							checkMessageAndSendBroadcast(msg);
						}
					}
//					try {
//						Thread.sleep(50);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
		}
	};
	

	protected boolean mNeedReConnectSPP = true;


//	public void connectSPP(Context context,
//			BluetoothDevice device) {
//		
//	}
	
	
//	public void connectSPP(Context context,
//			BluetoothIBridgeDevice device) {
//		// TODO Auto-generated method stub
//		
//		
//	}

	private String deviceInfoStr;

	private FileWriter fileWriter;

	private StringBuilder sb;

	public boolean mNeedAskUserToReconnectSpp;

	private OnSppConnectStateChangedListener mOnSppConnectStateChangedListener;
	private long mLastSendTime;
	private HeadSetConnectStateListener mHeadSetConnectStateListener;


//	public void initHeadSet() {
//		// TODO Auto-generated method stub
//		boolean success = mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, mProfileListener, BluetoothProfile.HEADSET);
//	}


//	public void disConnectSPP() {
//		// TODO Auto-generated method stub
//		if (mIBridgeDevice != null && mIBridgeAdapter != null) {
//			Log.i(tag, "disConnectSPP() "+mIBridgeDevice.getDeviceName());
//			mIBridgeAdapter.disconnectDevice(mIBridgeDevice);
//			Toast.makeText(SipUAApp.mContext,"disConnectSPP  "+mIBridgeDevice.getDeviceName(),0).show();
//			
//		}else {
//			Log.i(tag, "disConnectSPP() mIBridgeDevice is null ");
//		}
//	}

	/**
	 * init zmBluetooth spp and headset(sco);
	 * @param context
	 */
	public void init(Context context) {
		// TODO Auto-generated method stub
		Log.i(tag, "init() begin");
		initSPP(context);
		initHFP(context);
		Log.i(tag, "init() end");
	}
	
	/**
	 * 是否定时监测headset连接状态？切换蓝牙手咪？后台服务监测？
	 * @param context
	 */
	private void initHFP(Context context) {
		// TODO Auto-generated method stub
		Log.i(tag, "initHFP()");
//		mGetProfileProxy = mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, mProfileListener, BluetoothProfile.HEADSET);
		Log.i(tag, "getProfileProxy success is "+mGetProfileProxy+" waitting for headset serviceconnect");
		writeLog2File("initHFP()"+"getProfileProxy success is "+mGetProfileProxy+" waitting for headset serviceconnect");
	}
	private void initSPP(Context context) {
		// TODO Auto-generated method stub
		Log.i(tag, "initSPP()");
		if (mIBridgeAdapter == null) {
			Log.i(tag, "initSPP() init...");
			writeLog2File("initSPP() init...");
			mIBridgeAdapter = new BluetoothIBridgeAdapter(context);
			mIBridgeAdapter.registerEventReceiver(mEventReceiver);
			mIBridgeAdapter.registerDataReceiver(mDataReceiver);
		}else {
			Log.i(tag, "initSPP() need not init again");
			writeLog2File("initSPP() need not init again");
		}
	}


	/**
	 * 
	 * @param context
	 */
	public void exit(Context context) {
		// TODO Auto-generated method stub
		Log.i(tag, "exit() begin");
		exitSPP(context);
		exitHFP(context);
		Log.i(tag, "exit() end");
		writeLog2File("exit()");
		AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
	}


	public void exitHFP(Context context) {
		// TODO Auto-generated method stub
		Log.i(tag, "exitHFP()");
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			disConnectSCO(context);
			HeadsetBluetoothUtil.closeProfileProxy();
		}else {
			askUserToSelectBluetooth();
		}
	}


	private void exitSPP(Context context) {
		// TODO Auto-generated method stub
		Log.i(tag, "exitSPP()");
		if (mIBridgeAdapter != null) {
			writeLog2File("exitSPP() ...");
			disConnectAllSPP();
			mIBridgeAdapter.unregisterDataReceiver(mDataReceiver);
			mIBridgeAdapter.unregisterEventReceiver(mEventReceiver);
			mIBridgeAdapter = null;
		}else {
			Log.i(tag, "exitSPP() mIBridgeAdapter == null  ");
			writeLog2File("exitSPP() mIBridgeAdapter == null");
		}
	}


	private void disConnectAllSPP() {
		// TODO Auto-generated method stub
		if (mIBridgeAdapter != null) {
			Log.i(tag, "disConnectAllSPP() mIBridgeAdapter != null,has ("+mSPPConnectedDevices.size()+")device to disconnect");
//			for ( BluetoothIBridgeDevice device : mSPPConnectedDevices) {
//				mIBridgeAdapter.disconnectDevice(device);
//				Log.i(tag, "disConnectAllSPP() disconnectDevice "+device.getDeviceName());
//			}
			writeLog2File( "disConnectAllSPP() mIBridgeAdapter != null,has ("+mSPPConnectedDevices.size()+")device to disconnect");
			List<BluetoothDevice> bondedZMDevices = getBondedZMDevices();
			for (BluetoothDevice bluetoothDevice : bondedZMDevices) {
				BluetoothIBridgeDevice device = mSPPConnectedDevices.get(bluetoothDevice.getAddress());
				if (device != null) {
					if (!device.isConnected()) {
						writeLog2File("disConnectAllSPP() disconnectDevice()  device "+bluetoothDevice.getName()+",device is not connected ");
						Log.i(tag, "disConnectAllSPP() disconnectDevice()  device "+bluetoothDevice.getName()+",device is not connected ");
						
					}else {
						//onDeviceConnectting 
						for (OnSppConnectStateChangedListener listener : sppConnectStateChangedListeners) {
							listener.onDeviceDisconnectting(device);
						}
						
						mIBridgeAdapter.disconnectDevice(device);
						Log.i(tag, "disConnectAllSPP() disconnectDevice()  device "+bluetoothDevice.getName());
						writeLog2File("disConnectAllSPP() disconnectDevice()  device "+bluetoothDevice.getName());
					}
				}else {
					Log.i(tag, "disConnectAllSPP() disconnectDevice() is not connected device "+bluetoothDevice.getName());
					writeLog2File( "disConnectAllSPP() disconnectDevice() is not connected device "+bluetoothDevice.getName());
				}
			}
			
			mSPPConnectedDevices.clear();
		}else {
			writeLog2File( "disConnectAllSPP() mIBridgeAdapter == null");
			Log.i(tag, "disConnectAllSPP() mIBridgeAdapter == null  ");
		}
		
		stopThreads();
//		disableAdapter();
	}


//	public boolean connectSPP(Context context) {
//		// TODO Auto-generated method stub
//		if (!isDeviceSupportBluetooth()) {
//			Toast.makeText(context,"手机不支持蓝牙",0).show();
//			Log.i(tag, "手机不支持蓝牙");
//			return false;
//		}
//		else if (!isBluetoothAdapterEnabled()) {
//			Toast.makeText(context,"蓝牙功能未开启",0).show();
//			Log.i(tag, "蓝牙功能未开启");
////			askUserToEnableBluetooth();
//			enableAdapter();
//		}
//		
//		connectSCO(context);
//		return true;
//	}


	public boolean connectZMBluetooth(Context context) {
		// TODO Auto-generated method stub
		writeLog2File("SPP connect ,connectZMBluetooth()");
		if (!isDeviceSupportBluetooth()) {
			Toast.makeText(context,context.getResources().getString(R.string.mobile_notify),0).show();
			writeLog2File("SPP connect ,connectZMBluetooth()  "+context.getResources().getString(R.string.mobile_notify));
			Log.i(tag, "手机不支持蓝牙");
			return false;
		}
		else if (!isBluetoothAdapterEnabled()) {
//			Toast.makeText(context,"蓝牙功能未开启",0).show();
//			writeLog2File("SPP connect ,connectZMBluetooth()  蓝牙功能未开启   askUserToEnableBluetooth");
//			Log.i(tag, "蓝牙功能未开启");
//			askUserToEnableBluetooth();
//			return false;
			enableAdapter();
		}
		
		BluetoothSCOStateReceiver.setOnBluetoothConnectStateChangedListener(this);
		
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			HeadsetBluetoothUtil.getInstance().connectZMBluetooth(mContext);
		}else {
			askUserToSelectBluetooth();
		}
//		Log.i(tag, "connectZMBluetooth()  get HeadSet connected devices");
//		boolean success = mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, mProfileListener, BluetoothProfile.HEADSET);
//		Log.i(tag, "connectZMBluetooth()  get HeadSet connected devices  success? "+success);
//		
//		if (!success) {
//			Log.i(tag, "connectZMBluetooth()  askUserToConnectBluetooth");
//			askUserToConnectBluetooth();
//			writeLog2File("SPP connect ,connectZMBluetooth()  askUserToConnectBluetooth");
//			return false;
//		}
//		writeLog2File("SPP connect ,connectZMBluetooth() wait for mBluetoothHeadset get service");
		return true;
	}


	public void disConnectZMBluetooth(Context context) {
		// TODO Auto-generated method stub
		disConnectAllSPP();
		AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
		BluetoothSCOStateReceiver.reMoveOnBluetoothConnectStateChangedListener(this);
	}


	public boolean isHeadSetEnabled() {
		// TODO Auto-generated method stub
		//add by oumogang 2014-03-07
		if (isSPPConnected()) {
			return true;
		}
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			mBluetoothHeadset = HeadsetBluetoothUtil.getInstance().mBluetoothHeadset;
			if (mBluetoothHeadset != null&&mBluetoothHeadset.getConnectedDevices().size()>0) {
				Log.i(tag, "isHeadSetEnabled() = true");
				return true;
			}
		}
		
		Log.i(tag, "isHeadSetEnabled() = false");
		return false;
	}

	private FileWriter mFileWriter;
	private File mLogFile;
	private String deviceModelStr;
	/**
	 * make log and write to file,add Parameter tag
	 */
	//modify by oumogang 2014-05-08
	public void makeLog(String tag,String logMsg) {
		// TODO Auto-generated method stub
		if (TextUtils.isEmpty(tag)) {
			tag = "--";
		}
		LogUtil.makeLog(tag, logMsg);
	}
	private final byte[] block4MakeLog = new byte[0];
	public void writeLog2File(String logMsg) {
		// TODO Auto-generated method stub
		makeLog("--", logMsg);
	}

	public void writeLog2File(String tag,String mMessageStr) {
		// TODO Auto-generated method stub
		
		// TODO Auto-generated method stub
//		StringWriter wr = new StringWriter();
//		PrintWriter pw = new PrintWriter(wr);
		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				if (sb == null) {
					sb = new StringBuilder();
				}
//				sb.append("\r\nBluetoothMsgLog:");
//				sb.append("\r\n=========> ");
				sb.append("\r\n"+getDeviceModel());
				String time = "TIME:" + getTimeString();
				sb.append(" "+time);
				sb.append(" Thread:"+Thread.currentThread().getName());
				sb.append(" "+tag);
				sb.append(" "+mMessageStr);
//				sb.append("\r\n"+getDeviceInfo());
//				sb.append("\r\n"+getDeviceModel());
//				String time = "\r\nTIME:" + getTimeString();
//				sb.append(time);
//				sb.append(" Thread:"+Thread.currentThread().getName());
//				sb.append("\r\n===============================");
				String log = sb.toString();
				if (mFileWriter == null) {
					initFile();
					mFileWriter = new FileWriter(mLogFile, true);
				}
				if (!mLogFile.exists()) {
					initFile();
					mFileWriter = new FileWriter(mLogFile, true);
				}
				if (mLogFile.length()>1024*1024*50) {
					mFileWriter.close();
					mLogFile.delete();
					saveLastLogFileName(SipUAApp.getAppContext(),"");
					initFile();
					mFileWriter = new FileWriter(mLogFile, true);
				}
				
				mFileWriter.write(log);
				mFileWriter.flush();
//					mFileWriter.close();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (sb != null) {
				sb.delete(0, sb.length());
			}
		}
		
	}
	/**
	 * 如果没有历史文件名，就新建一个文件。
	 */
	private void initFile() {
		// TODO Auto-generated method stub
		String fileName = getLastLogFileName(SipUAApp.getAppContext());
		String dirName = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + "com.zed3.sipua";
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdir();
		
		File[] listFiles = dir.listFiles();
		File file;
		int count = 0;
		boolean isExistsed = false;
		for (int i = 0; i < listFiles.length; i++) {
			file = listFiles[i];
			if (file.getName().equals(fileName)) {
				isExistsed = true;
			}
		}
		if (TextUtils.isEmpty(fileName)||!isExistsed) {
//			File[] listFiles = dir.listFiles();
//			File file;
//			int count = 0;
//			for (int i = 0; i < listFiles.length; i++) {
//				file = listFiles[i];
//				if (file.getName().contains("ZMBluetoothMsgLog")) {
////				count ++;
//					file.delete();
//				}
//			}
			SimpleDateFormat formatter = new SimpleDateFormat(
					"MMdd-hhmmss");
			long systemTime = System.currentTimeMillis();
			Date curDate = new Date(systemTime);// 获取当前时
			String time = formatter.format(curDate);
			String filename = "ZMBluetoothMsgLog-"+getDeviceModel()+"-"+time+".txt";
			mLogFile = new File(dir, filename);
			saveLastLogFileName(SipUAApp.getAppContext(),filename);
		}else {
			mLogFile = new File(dir, fileName);
		}
	}

	
	private static String getTimeString() {
		// TODO Auto-generated method stub
		if (formatter == null) {
			formatter = new SimpleDateFormat(
					" yyyy-MM-dd hh:mm:ss SSS ");
		}
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// 获取当前时
		return formatter.format(curDate);
	}
	private String getDeviceInfo() {
		// TODO Auto-generated method stub
		if (deviceInfoStr == null) {
			// 获取当前手机操作系统的信息.
			try {
				//add by oumogang 2013-10-28
				PackageInfo packageInfo = SipUAApp.mContext.getPackageManager().getPackageInfo(SipUAApp.mContext.getPackageName(), 0);
				deviceInfoStr = ("\r\n"+packageInfo.versionName);
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);// 暴力反射,可以获取私有成员变量的信息
				String name = field.getName();
				String value = "";
				try {
					value = field.get(null).toString();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (/*name.equalsIgnoreCase("PRODUCT")||*/name.equalsIgnoreCase("MODEL")) {
					deviceInfoStr+=("\r\n"+name + "=" + value);
					break;
				}
			}
		}
		return deviceInfoStr;
	}
	private final byte[] block4GetDeviceModel = new byte[0];
	private String getDeviceModel() {
		// TODO Auto-generated method stub
		synchronized (block4GetDeviceModel) {
			if (deviceModelStr == null) {
				// 获取当前手机操作系统的信息.
				String version = null;
				try {
					PackageInfo packinfo = SipUAApp.getAppContext().getPackageManager().getPackageInfo(
							SipUAApp.getAppContext().getPackageName(), 0);
					version = packinfo.versionName;
					//add by oumogang 2013-10-28
					PackageInfo packageInfo = SipUAApp.getAppContext().getPackageManager().getPackageInfo(SipUAApp.getAppContext().getPackageName(), 0);
					deviceModelStr = "";
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Field[] fields = Build.class.getDeclaredFields();
				for (Field field : fields) {
					field.setAccessible(true);// 暴力反射,可以获取私有成员变量的信息
					String name = field.getName();
					String value = "";
					try {
						value = field.get(null).toString();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (/*name.equalsIgnoreCase("PRODUCT")||*/name.equalsIgnoreCase("MODEL")) {
//					deviceModelStr += (name + ":" + value);
						deviceModelStr += value;
						break;
					}
				}
				deviceModelStr += ("["+Build.VERSION.SDK_INT+"]["+Build.VERSION.RELEASE+"]["+version+"]");
			}
		}
		return deviceModelStr;
	}
	
	
	private void disconnectBluetoothSco(Context mContext) {
		// TODO Auto-generated method stub
		Log.i(tag, "disconnectBluetoothSco() beging");
		final AudioManager mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setBluetoothScoOn(false);
		mAudioManager.stopBluetoothSco();// 如果SCO没有断开，由于SCO优先级高于A2DP，A2DP可能无声音
		if (!mAudioManager.isBluetoothA2dpOn())
			mAudioManager.setBluetoothA2dpOn(true); // 如果A2DP没建立，则建立A2DP连接
		Log.i(tag, "disconnectBluetoothSco() end");
//		mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
		// 让声音路由到蓝牙A2DP。此方法虽已弃用，但就它比较直接、好用。
	}

	/**
	 * connect BluetoothSco to use BTMic and BTSpeaker;
	 * add by oumogang 2013-12-17
	 * @param mContext
	 */
//	private void connectBluetoothSco(Context mContext) {
//		// TODO Auto-generated method stub
//		Log.i(tag, "connectBluetoothSco() beging");
//		final AudioManager mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
//		// 蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
//		mAudioManager.startBluetoothSco();
//		// 蓝牙SCO连接建立需要时间，连接建立后会发出ACTION_SCO_AUDIO_STATE_CHANGED消息，通过接收该消息而进入后续逻辑。
//		// 也有可能此时SCO已经建立，则不会收到上述消息，可以startBluetoothSco()前先stopBluetoothSco()
//		
//		if (mAudioManager.isBluetoothA2dpOn())
//			mAudioManager.setBluetoothA2dpOn(false);
//		
//		mContext.registerReceiver(new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				int state = intent.getIntExtra(
//						AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
//
//				switch (state) {
//				case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
//					Log.i(tag, "SCO_AUDIO_STATE_DISCONNECTED");
//					mAudioManager.setBluetoothScoOn(false);
//					try {
//						Thread.sleep(/*1000*/5000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					mAudioManager.startBluetoothSco();
//					break;
//				case AudioManager.SCO_AUDIO_STATE_CONNECTED:
//					Log.i(tag, "SCO_AUDIO_STATE_CONNECTED");
//					mAudioManager.setBluetoothScoOn(true); // 打开SCO
//					mAudioManager.setBluetoothA2dpOn(false); // 打开SCO
//					int flags = 1;
//					mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 10, flags );
////					mRecorder.start();// 开始录音
////					startBluetoothRecordAndPlay(mContex);
////					context.unregisterReceiver(this); // 别遗漏
//					break;
//				case AudioManager.SCO_AUDIO_STATE_CONNECTING:
//					Log.i(tag, "SCO_AUDIO_STATE_CONNECTING");
//					mAudioManager.setBluetoothScoOn(false);
//					break;
//
//				default:
//					Log.i(tag, "unknow state state = "+state);
//					break;
//				}
//			}
//		}, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
//		Log.i(tag, "connectBluetoothSco() end");
//	}

	
	public void disableAdapter() {
		// TODO Auto-generated method stub
		if (isBluetoothAdapterEnabled()) {
			mBluetoothAdapter.disable();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void enableAdapter() {
		// TODO Auto-generated method stub
		if (!isBluetoothAdapterEnabled()) {
			mBluetoothAdapter.enable();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@Override
	public void startReConnectingSPP(String deviceAddress, long cycle,
			int maxTimes) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean stopReConnectingSPP() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean setHeadSetConnectStateListener(
			HeadSetConnectStateListener listener) {
		// TODO Auto-generated method stub
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			HeadsetBluetoothUtil.getInstance().mHeadSetConnectStateListener = listener;
			HeadsetBluetoothUtil.getInstance().getHeadsetBluetooths(mContext);
			return true;
		}else {
			askUserToSelectBluetooth();
			return false;
		}
	}


	@Override
	public void registerReceivers(Context context) {
		// TODO Auto-generated method stub
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			// add by oumogang 2014-03-13
			GroupCallStateReceiver.startReceive(mContext);
			// add by oumogang 2014-03-18
			PhoneStatReceiver.startReceive(mContext);
			// add by oumogang 2014-03-22
			BluetoothSCOStateReceiver.startReceive(mContext);
			
			//获取电话通讯服务  
			TelephonyManager tpm = (TelephonyManager) context  
					.getSystemService(Context.TELEPHONY_SERVICE);  
			//创建一个监听对象，监听电话状态改变事件  
			tpm.listen(MyPhoneStateListener.getInstance(),  
					PhoneStateListener.LISTEN_CALL_STATE); 
		}
	}


	@Override
	public void unregisterReceivers(Context context) {
		// TODO Auto-generated method stub
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			GroupCallStateReceiver.stopReceive(mContext);
			PhoneStatReceiver.stopReceive(mContext);	
			BluetoothSCOStateReceiver.stopReceive(mContext);	
		}
	}


	@Override
	public void setSppConnectStateListener(
			OnSppConnectStateChangedListener listener) {
		// TODO Auto-generated method stub
		sppConnectStateChangedListeners.add(listener);
	}


	@Override
	public void removeSppConnectStateListener(
			OnSppConnectStateChangedListener listener) {
		// TODO Auto-generated method stub
		sppConnectStateChangedListeners.remove(listener);
	}


	public synchronized void send(String msg) {
		// TODO Auto-generated method stub
		synchronized(ZMBluetoothManager.class){
			mLastSendMsg  = msg;
			if (mIBridgeAdapter == null) {
				writeLog2File("error   mIBridgeAdapter == null");
				Log.i(tag, "error   mIBridgeAdapter == null");
				return;
			}
			if (mCurrentIBridgeDevice == null) {
				writeLog2File("error   mCurrentIBridgeDevice == null,no device connect spp");
				Log.i(tag, "error   mCurrentIBridgeDevice == null,no device connect spp");
				return;
			}
			byte[] bytes = msg.getBytes();
			
			long time = System.currentTimeMillis();
			if (mLastSendTime == 0) {
				mLastSendTime = time;
			}
			long cycle = time - mLastSendTime;
			Log.i(tag, "SPP out ,device:"+mCurrentIBridgeDevice.getDeviceName()+" msg:"+msg+"cycle = "+cycle);
			writeLog2File("SPP out ,device:"+mCurrentIBridgeDevice.getDeviceName()+" msg:"+msg+"cycle = "+cycle);
			mIBridgeAdapter.send(mCurrentIBridgeDevice, bytes, bytes.length);
			mLastSendTime = time;
		}
	}
	
	public void receive(String msg) {
		// TODO Auto-generated method stub
		processMsg(msg);
	}


	@Override
	public void onDeviceConnecting(BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDeviceConnected(BluetoothDevice device) {
		// TODO Auto-generated method stub
		if (device != null) {
			if (ZMBluetoothManager.getInstance().checkIsZM(device.getName())) {
				Log.i(tag, "onDeviceConnected(),device:"+device.getName()+" connectSPP");
				writeLog2File("onDeviceConnected(),device:"+device.getName()+" connectSPP");
				ZMBluetoothManager.getInstance().connectSPP(device);
			}else {
				Log.i(tag, "onDeviceConnected(),device:"+device.getName()+" askUserToConnectZMBluetooth");
				writeLog2File("onDeviceConnected(),device:"+device.getName()+" askUserToConnectZMBluetooth");
				ZMBluetoothManager.getInstance().askUserToConnectZMBluetooth(device);
			}
		}
	}


	@Override
	public void onDeviceDisConnecting(BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDeviceDisConnected(BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}

	
	
}
