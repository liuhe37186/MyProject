package com.zed3.bluetooth;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.zed3.audio.AudioUtil;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.media.mediaButton.MediaButtonReceiver;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
	  
public class MyPhoneStateListener extends PhoneStateListener {  

	public static final int CALL_STATE_IDLE = TelephonyManager.CALL_STATE_IDLE;
	public static final int CALL_STATE_RINGING = TelephonyManager.CALL_STATE_RINGING;
	public static final int CALL_STATE_OFFHOOK = TelephonyManager.CALL_STATE_OFFHOOK;
	public static final int CALL_STATE_OUTGONING = TelephonyManager.CALL_STATE_OFFHOOK+1;
	private int mPhoneState = CALL_STATE_IDLE;
	private boolean mIsFirstState = true;
	private ZMBluetoothManager mInstance;
	private final String tag = "MyPhoneStateListener";
	private int mLastMode;

	@Override  
    public void onCallStateChanged(int state, String incomingNumber) { 
//    	if (!Settings.mNeedBlueTooth) {
//			return;
//		}
		mInstance = ZMBluetoothManager.getInstance();
//		if (mInstance == null&& !mInstance.isSPPConnected()) {
//			return;
//		}
		String logMsg = "MyPhoneStateListener.onCallStateChanged("+getPhoneStateStr(state)+")";
		//���⴦���ظ�����Ϣ��
		if (state == mPhoneState) {
			logMsg += "state == mPhoneState ignore";
			makeLog(tag, logMsg);
			return;
		}
        switch(state) {  
        case TelephonyManager.CALL_STATE_IDLE: //����  
        	makeLog(tag, "CALL_STATE_IDLE");
            break;  
        case TelephonyManager.CALL_STATE_RINGING: //����  
        	mLastMode = AudioUtil.getInstance().getMode();
        	makeLog(tag, "CALL_STATE_RINGING"+" RINGING :"+ incomingNumber);
            break;  
        case TelephonyManager.CALL_STATE_OFFHOOK: //ժ��������ͨ���У�  
        	mLastMode = AudioUtil.getInstance().getMode();
        	makeLog(tag, "CALL_STATE_OFFHOOK"+" OFFHOOK :"+ incomingNumber);
        	break;  
        }  
        onPhoneStateChanged(state);
    }  
	
	private MyPhoneStateListener() {
	}
	
	private static final class InstanceCreater {
		public static MyPhoneStateListener sInstance = new MyPhoneStateListener();
	}
	
	public static MyPhoneStateListener getInstance() {
		return InstanceCreater.sInstance;
	}
	
	public boolean isInCall() {
		boolean result = true;
		TelephonyManager manager = (TelephonyManager) Receiver.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		result = (mPhoneState != CALL_STATE_IDLE) && !(manager.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
		return result;
	}
	
	public String getPhoneStateStr(int state) {
		String stateStr = "";
		switch (state) {
		case CALL_STATE_IDLE:
			stateStr = "CALL_STATE_IDLE";
			break;
		case CALL_STATE_RINGING:
			stateStr = "CALL_STATE_RINGING";
			break;
		case CALL_STATE_OFFHOOK:
			stateStr = "CALL_STATE_OFFHOOK";
			break;
		case CALL_STATE_OUTGONING:
			stateStr = "CALL_STATE_OUTGONING";
			break;
		default:
			stateStr = "unkown state";
			break;
		}
		return stateStr;
	} 
      
	private void  makeLog(String tag,String logMsg) {
		// TODO Auto-generated method stub
		ZMBluetoothManager.getInstance().makeLog(tag, logMsg);
	} 
	/**
	 * on PhoneState Changed  call state,
	 * see {@link CALL_STATE_IDLE},{@link CALL_STATE_RINGING},{@link CALL_STATE_OFFHOOK},{@link CALL_STATE_OUTGONING}
	 * @param phoneState
	 */
	public void onPhoneStateChanged(int phoneState) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("onPhoneStateChanged("+getPhoneStateStr(phoneState)+")");
		// �����SIM��ʱ�����ƶ��绰���绰״̬����ȷ�����⡣ add by lwang 2014-11-24
		TelephonyManager manager = (TelephonyManager) SipUAApp.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		builder.append(" TelephonyManager.getSimState() is"+manager.getSimState());
		LogUtil.makeLog(" MyPhoneStateListener ", builder.toString());
		if(manager.getSimState()!=TelephonyManager.SIM_STATE_READY){
			return ;
		}
		//���⴦���ظ�����Ϣ��
		if (phoneState == mPhoneState) {
			builder.append(" state == mPhoneState ignore");
			makeLog(tag, builder.toString());
			return;
		}
		mPhoneState = phoneState;
		switch (phoneState) {
		case CALL_STATE_IDLE:
            if (mInstance != null && mInstance.isSPPConnected()) {
            	builder.append(" send PTT_PA_OFF");
            	mInstance.sendSPPMessage(ZMBluetoothManager.PTT_PA_OFF);
			}
            //SIM���绰�ҶϺ���������ָ���ԭ����ģʽ��ȱʡ����ţ�����ʼ���ŶԽ�������
            //ͨ�������������ǰ�ǶԽ�����������������ǵ���������Ͳ add by mou 2014-08-19
            if (UserAgent.ua_ptt_mode) {
            	builder.append(" set MODE_SPEAKER");
            	new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							if (SipUAApp.isHeadsetConnected) {
								AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
							}else {
								AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
							}
						}
					}
				}).start();
			}else/* if (Receiver.call_state == UserAgent.UA_STATE_INCALL 
					|| Receiver.call_state == UserAgent.UA_STATE_HOLD)*/ {
				builder.append(" set mLastMode");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							AudioUtil.getInstance().setAudioConnectMode(mLastMode);
						}
					}
				}).start();
			}
            //ϵͳ�绰����������ע��ý�尴���㲥������   add by mou 2014-08-25
        	if (SipUAApp.isHeadsetConnected) {
        		MediaButtonReceiver.startReceive(SipUAApp.mContext);
        		builder.append(" startReceive MediaButtonReceiver");
        	}
			break;
		case CALL_STATE_OUTGONING:
			if (mInstance != null && mInstance.isSPPConnected()) {
				builder.append(" send PTT_PA_ON");
	        	mInstance.sendSPPMessage(ZMBluetoothManager.PTT_PA_ON);
 			}
			//ϵͳȥ���ֹͣ����ý�尴���㲥  add by mou 2014-08-25
			if (SipUAApp.isHeadsetConnected) {
        		MediaButtonReceiver.stopReceive(SipUAApp.mContext);
        		builder.append(" stopReceive MediaButtonReceiver");
        	}
        	//����Ѿ�����PTT������ʱ�ɿ�PTT,�ͷŻ�Ȩ add by mou 2014-08-19
        	if (GroupCallUtil.mIsPttDown) {
        		builder.append(" makeGroupCall(false, true)");
        		GroupCallUtil.makeGroupCall(false, true);
        	}
        	//VOIP���������С�ȥ���к�ͨ���У�sim�������ȥ�磬ֱ�ӹҶ�VOIP������   add by mou 2014-10-09
        	if (CallUtil.isInCall()) {
        		builder.append(" rejectCall");
        		CallUtil.rejectCall();
        	}
			break;
		case CALL_STATE_RINGING:
			if (mInstance != null && mInstance.isSPPConnected()) {
				builder.append(" send PTT_PA_ON");
	        	mInstance.sendSPPMessage(ZMBluetoothManager.PTT_PA_ON);
 			}
			//ϵͳ�����ֹͣ����ý�尴���㲥  add by mou 2014-08-25
			if (SipUAApp.isHeadsetConnected) {
				MediaButtonReceiver.stopReceive(SipUAApp.mContext);
				builder.append(" stopReceive MediaButtonReceiver");
			}
        	//����Ѿ�����PTT������ʱ�ɿ�PTT,�ͷŻ�Ȩ add by mou 2014-08-19
        	if (GroupCallUtil.mIsPttDown) {
        		builder.append(" makeGroupCall(false, true)");
        		GroupCallUtil.makeGroupCall(false, true);
        	}
        	//VOIP���������С�ȥ���к�ͨ���У�sim�������ȥ�磬ֱ�ӹҶ�VOIP������   add by mou 2014-10-09
			if (CallUtil.isInCall()) {
				builder.append(" rejectCall");
				CallUtil.rejectCall();
			}
			break;
		case CALL_STATE_OFFHOOK:
			//SIM�������������������л�����Ͳģʽ�����Ҳ����ŶԽ�������
        	//ͨ��������Ͳ add by mou 2014-08-19
        	AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
        	//VOIP���������У����SIM�����磬����֮��ֱ�ӹҶ�VOIP������ add by mou 2014-10-09
        	
        	//SIM�����������ȥ���ͨ����������л�����Ͳģʽ�����Ҳ����ŶԽ�������add by mou 2014-10-09
        	if (GroupCallUtil.mIsPttDown) {
        		builder.append(" makeGroupCall(false, true)");
        		GroupCallUtil.makeGroupCall(false, true);
        	}
        	//VOIP���������У����SIM�����磬����֮��ֱ�ӹҶ�VOIP������   add by mou 2014-10-09
        	if (CallUtil.isInCall()) {
        		builder.append(" rejectCall");
        		CallUtil.rejectCall();
        	}
			break;
		default:
			builder.append(" unkown state");
            break;  
        }  
		
		makeLog(tag, builder.toString());
    }  
      
}