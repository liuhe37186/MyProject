package com.zed3.bluetooth;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStatReceiver extends BroadcastReceiver{  
    
    private static final String TAG = "PhoneStatReceiver";  
      
//    private static MyPhoneStateListener phoneListener = new MyPhoneStateListener();  
      
    private static boolean incomingFlag = false;  
      
    private static String incoming_number = null;  
    
    private static final String PTT_PA_ON = "PA_ON";
	private static final String PTT_PA_OFF = "PA_OFF";

	private ZMBluetoothManager mInstance;
	private static PhoneStatReceiver mReceiver;
	private static IntentFilter intentFilter;

	private static boolean isStarted; 
	private final static String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
	private final static String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    static{
    	mReceiver = new PhoneStatReceiver();
    	intentFilter = new IntentFilter(); 
        intentFilter.addAction(ACTION_PHONE_STATE); 
        intentFilter.addAction(ACTION_NEW_OUTGOING_CALL); 
    }
    @Override  
    public void onReceive(Context context, Intent intent) {  
//    	if (!Settings.mNeedBlueTooth) {
//			return;
//		}
	    	mInstance = ZMBluetoothManager.getInstance();
//			if (mInstance == null) {
//				return;
//			}
			
            TelephonyManager tm =   
                (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);                          
            //如果是拨打电话  
            if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){                          
                incomingFlag = false;  
                MyPhoneStateListener.getInstance().onPhoneStateChanged(MyPhoneStateListener.CALL_STATE_OUTGONING);
                String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);          
                Log.i(TAG, "call OUT:"+phoneNumber);  
                if (mInstance != null) {
                	mInstance.makeLog(TAG, "call OUT:"+phoneNumber);
                }
                if (mInstance != null && mInstance.isSPPConnected()) {
                	mInstance.sendSPPMessage(ZMBluetoothManager.PTT_PA_ON);
                }
            }else{                          
                    //如果是来电  
//                    TelephonyManager tm =   
//                        (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);                          
                      
//                    switch (tm.getCallState()) {  
//                    case TelephonyManager.CALL_STATE_RINGING:  
//                            incomingFlag = true;//标识当前是来电  
//                            incoming_number = intent.getStringExtra("incoming_number");  
//                            Log.i(TAG, "RINGING :"+ incoming_number);  
//                            if (RespondReceiver.checkNeedOpenSpeaker()) {
//                            	instance.sendMessageHandle(PTT_PA_ON);
//                            }
//                            instance.sendLinkDetectedMessage("RINGING :"+ incoming_number, 0);
//                            break;  
//                    case TelephonyManager.CALL_STATE_OFFHOOK:                                  
//                            if(incomingFlag){  
//                                    Log.i(TAG, "incoming ACCEPT :"+ incoming_number);  
//                                    instance.sendLinkDetectedMessage("incoming ACCEPT :"+ incoming_number, 0);
//                            }  
//                            break;  
//                      
//                    case TelephonyManager.CALL_STATE_IDLE:                                  
//                            if(incomingFlag){  
//                                    Log.i(TAG, "incoming IDLE");  
//                                    
//                                    if (RespondReceiver.checkNeedCloseSpeaker()) {
//                                    	instance.sendMessageHandle(PTT_PA_OFF);
//                                    }
//                                    instance.sendLinkDetectedMessage("incoming IDLE", 0);
//                            }  
//                            break;  
//                    }   
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
}  