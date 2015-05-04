package com.zed3.bluetooth;

import android.content.Context;
import android.content.Intent;

import com.zed3.audio.AudioUtil;
import com.zed3.sipua.ui.Settings;

public class BluetoothPaControlUtil {
	public static final String RESPOND_TYPE = "respond_type";
	public static final String RESPOND_TYPE_PTT = "respond_type_ptt";
	public static final String RESPOND_TYPE_VOL = "respond_type_vol";
	public static final String RESPOND_TYPE_FUNCTION = "respond_type_function";
	public static final String RESPOND_TYPE_PA_CONTROL = "respond_type_pa_control";
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
	
	
	public static final String RESPOND_ACTION_PTT_IDLE = "respond_action_ptt_idle";
	public static final String RESPOND_ACTION_PTT_LISTENING = "respond_action_ptt_listening";
	public static final String RESPOND_ACTION_PTT_SUCCESS = "respond_action_ptt_success";
	public static final String RESPOND_ACTION_PTT_WAITTING = "respond_action_ptt_waitting";
	
	public static final String PTT_START = "R_START";
	public static final String PTT_STOP = "R_STOP";
	public static final String PTT_SUCCESS = "PTT_SUCC";
	public static final String PTT_WAITING = "PTT_WAIT";
	
	public static final String ACTION_BLUETOOTH_RESPOND = "com.zed3.sipua_bluetooth_respond";
	
	public static final String PTT_PA_ON = "PA_ON";
	public static final String PTT_PA_OFF = "PA_OFF";
	public static boolean mNeedOff;
	protected static String tag = "BluetoothPaControlUtil";
	public synchronized static void setPaOn(final Context context, final boolean on) {
		// TODO Auto-generated method stub
		if (!Settings.mNeedBlueTooth || !ZMBluetoothManager.getInstance().isSPPConnected()) {
			return ;
		}
//		if(on){
//			mNeedOff = false;
//			Log.i(tag, "mNeedOff false  turn on");
//			sendRespondBroadcast(context,ACTION_BLUETOOTH_RESPOND,RESPOND_TYPE_PA_CONTROL,on?PTT_PA_ON:PTT_PA_OFF);
//		}else {
//			mNeedOff = true;
//			Log.i(tag, "mNeedOff true  turn off");
////			new Thread(new Runnable() {
////				
////				@Override
////				public void run() {
////					// TODO Auto-generated method stub
////					try {
////						Thread.sleep(10000);
////					} catch (InterruptedException e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
////					if (mNeedOff) {
////						Log.i(tag, "mNeedOff true");
////						
////						sendRespondBroadcast(context,ACTION_BLUETOOTH_RESPOND,RESPOND_TYPE_PA_CONTROL,on?PTT_PA_ON:PTT_PA_OFF);
////					}else {
////						mNeedOff = true;
////						Log.i(tag, "mNeedOff false");
////					}
////				}
////			}).start();
//			sendRespondBroadcast(context,ACTION_BLUETOOTH_RESPOND,RESPOND_TYPE_PA_CONTROL,on?PTT_PA_ON:PTT_PA_OFF);
//		}
		if (on) {
			//disable by oumogang 2014-03-22
//			int currentMode = AudioUtil.getInstance().getCurrentMode();
//			if (currentMode != /*AudioUtil.MODE_BLUETOOTH*/0) {
//				AudioUtil.getInstance().setAudioConnectMode(/*AudioUtil.MODE_BLUETOOTH*/currentMode);
//			}
			ZMBluetoothManager.getInstance().sendSPPMessage(ZMBluetoothManager.PTT_PA_ON);
		}else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//避免在释放话权后立马有人发起讲话，PA_ON	后被PA_OFF掉。
			if (GroupCallStateReceiver.mLastState != GroupCallStateReceiver.STATE_LISTENING) {
				ZMBluetoothManager.getInstance().sendSPPMessage(ZMBluetoothManager.PTT_PA_OFF);
			}
		}
		
	}
	private static void sendRespondBroadcast(Context context, String action, String type,String respondAction) {
		
		// TODO Auto-generated method stub
//		Log.i(tag, "sendRespondBroadcast() " +
//				"action = "+action+
//				"type = "+type+
//				"respondAction = "+respondAction
//				);
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(RESPOND_TYPE, type);
		intent.putExtra(RESPOND_ACTION, respondAction);
		context.sendBroadcast(intent);
	}
}
