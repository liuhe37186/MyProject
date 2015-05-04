package com.zed3.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;

public class GroupCallStateReceiver extends BroadcastReceiver {

	public static final String ACTION_BLUETOOTH_CONTROL = "com.zed3.sipua_bluetooth";
	private String tag = "GroupCallStateReceiver";
	
	private final static String ACTION_GROUP_STATUS = "com.zed3.sipua.ui_groupcall.group_status";
	public static final int STATE_IDLE = 0;
	public static final int STATE_LISTENING = 1;
	public static final int STATE_TALKING = 2;
	public static final int STATE_QUEUE = 3;
	public static final int STATE_INITIATING = 4;
	private ZMBluetoothManager mInstance = ZMBluetoothManager.getInstance();
	public static int mLastState = STATE_IDLE;

	
	private static GroupCallStateReceiver mReceiver;
	private static IntentFilter intentFilter;
	private static boolean isStarted; 
 
    static{
    	mReceiver = new GroupCallStateReceiver();
    	intentFilter = new IntentFilter(); 
        intentFilter.addAction(ACTION_GROUP_STATUS); 
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
//		if (!Settings.mNeedBlueTooth || !ZMBluetoothManager.getInstance().isSPPConnected()) {
//			return ;
//		}
		if (intent.getAction().equalsIgnoreCase(ACTION_GROUP_STATUS)) {
			Bundle bundle = intent.getExtras();
	
			String speaker = bundle.getString("1");
			String userNum = null;
			if (speaker != null) {
				String[] arr = speaker.split(" ");
				if (arr.length == 1) {
					userNum = arr[0];
	//				speaker = arr[0];
				} else {
					userNum = arr[0];
					speaker = arr[1];
				}
			}
	
//			PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
			PttGrp pttGrp;
			UserAgent ua = Receiver.mSipdroidEngine.GetCurUA();
			if (ua == null) {
				return;
			}
			pttGrp = ua.GetCurGrp();
				MyLog.i(tag, "speaker:"+speaker+",userNum:"+userNum);
			if (pttGrp != null) {
				processState(pttGrp);
			}else {
				MyLog.i(tag, "pttGrp = null unprocess");
			}
		}
	}

	private void processState(PttGrp pttGrp) {
		// TODO Auto-generated method stub
		
					switch (pttGrp.state) {
					// houyuchun modify 20120620 begin
					case GRP_STATE_SHOUDOWN:
						mInstance.sendSPPMessage(ZMBluetoothManager.PTT_STOP);
						if (mLastState == STATE_LISTENING) {
							sleep(50);
							mInstance.sendSPPMessage(ZMBluetoothManager.PTT_PA_OFF);
						}
						GroupCallUtil.setGroupCallState(GroupCallUtil.STATE_SHUTDOWN);
						break;
					case GRP_STATE_IDLE:
						MyLog.i(tag, "idle");
						if (mLastState != STATE_IDLE) {
							mInstance.sendSPPMessage(ZMBluetoothManager.PTT_STOP);
							if (mLastState == STATE_LISTENING) {
								sleep(50);
								mInstance.sendSPPMessage(ZMBluetoothManager.PTT_PA_OFF);
							}
						}else {
							MyLog.i(tag, "mLastState == STATE_IDLE do not send again");
							
						}
						mLastState = STATE_IDLE;
						GroupCallUtil.setGroupCallState(GroupCallUtil.STATE_IDLE);
						break;
					case GRP_STATE_TALKING:
						MyLog.i(tag, "spearking");
						
						if (mLastState != STATE_TALKING) {
							mInstance.sendSPPMessage(ZMBluetoothManager.PTT_SUCCESS);
						}else {
							MyLog.i(tag, "mLastState == STATE_TALKING do not send again");
							
						}
						mLastState = STATE_TALKING;
						GroupCallUtil.setGroupCallState(GroupCallUtil.STATE_TALKING);
						break;
					case GRP_STATE_LISTENING:
						MyLog.i(tag, "listening");
						if (mLastState != STATE_LISTENING) {
							mInstance.sendSPPMessage(ZMBluetoothManager.PTT_PA_ON);
							sleep(50);
							mInstance.sendSPPMessage(ZMBluetoothManager.PTT_START);
						}else {
							MyLog.i(tag, "mLastState == STATE_LISTENING do not send again");
							
						}
						mLastState = STATE_LISTENING;
						GroupCallUtil.setGroupCallState(GroupCallUtil.STATE_LISTENING);
						break;
					case GRP_STATE_QUEUE:
						MyLog.i(tag, "waiting");
						//need not stop
//						mInstance.sendSPPMessage(ZMBluetoothManager.PTT_STOP);
						mLastState = STATE_QUEUE;
						GroupCallUtil.setGroupCallState(GroupCallUtil.STATE_QUEUE);
						break;
					case GRP_STATE_INITIATING:
						MyLog.i(tag, "initlating");
						mLastState = STATE_INITIATING;
						GroupCallUtil.setGroupCallState(GroupCallUtil.STATE_INITIATING);
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
	
	
//	01-13 14:05:37.185: I/BluetoothReceiver(12383): groupName:4000段组groupState:讲话speaker:无
//	01-13 14:05:37.220: I/BluetoothReceiver(12383): groupName:4000段组groupState:讲话speaker:4002-莫刚(自己)

//	01-13 14:05:42.460: I/BluetoothReceiver(12383): groupName:4000段组groupState:空闲speaker:4002-莫刚
//	01-13 14:05:42.505: I/BluetoothReceiver(12383): groupName:4000段组groupState:空闲speaker:无
//	01-13 14:05:42.520: I/BluetoothReceiver(12383): groupName:4000段组groupState:空闲speaker:无

//	01-13 14:05:45.385: I/BluetoothReceiver(12383): groupName:4000段组groupState:听讲speaker:4001-莫刚

//	01-13 14:05:48.905: I/BluetoothReceiver(12383): groupName:4000段组groupState:排队speaker:无
//	01-13 14:05:48.925: I/BluetoothReceiver(12383): groupName:4000段组groupState:排队speaker:4001-莫刚

//	01-13 14:05:52.205: I/BluetoothReceiver(12383): groupName:4000段组groupState:听讲speaker:4001-莫刚
//	01-13 14:05:52.230: I/BluetoothReceiver(12383): groupName:4000段组groupState:听讲speaker:4001-莫刚

//	01-13 14:05:53.565: I/BluetoothReceiver(12383): groupName:4000段组groupState:排队speaker:无
//	01-13 14:05:53.585: I/BluetoothReceiver(12383): groupName:4000段组groupState:排队speaker:4001-莫刚

//	01-13 14:05:56.085: I/BluetoothReceiver(12383): groupName:4000段组groupState:排队speaker:无
//	01-13 14:05:56.660: I/BluetoothReceiver(12383): groupName:4000段组groupState:讲话speaker:无
//	01-13 14:05:56.690: I/BluetoothReceiver(12383): groupName:4000段组groupState:讲话speaker:4002-莫刚(自己)

//	01-13 14:05:58.955: I/BluetoothReceiver(12383): groupName:4000段组groupState:空闲speaker:4002-莫刚
//	01-13 14:05:58.980: I/BluetoothReceiver(12383): groupName:4000段组groupState:空闲speaker:无
//	01-13 14:05:58.995: I/BluetoothReceiver(12383): groupName:4000段组groupState:空闲speaker:无

	
	
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
}
