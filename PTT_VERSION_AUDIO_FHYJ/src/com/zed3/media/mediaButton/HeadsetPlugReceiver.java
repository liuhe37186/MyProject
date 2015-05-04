package com.zed3.media.mediaButton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.zed3.audio.AudioUtil;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamSenderUtil;

public class HeadsetPlugReceiver extends BroadcastReceiver { 
		 
    private static final String TAG = "HeadsetPlugReceiver";
	protected static final int HEADSET_PLUG_DISCONNECTED = 0;
	protected static final int HEADSET_PLUG_CONNECTED = 1;
	protected static final int CHECK_MUSIC_ACTIVE = 2;
	protected static final int REGISTER_MEDIABUTTON_EVENT_RECEIVER = 3;
	protected static final long REGISTER_AGAIN_DELAY = 5000;
	private static HeadsetPlugReceiver mReceiver;
	private static IntentFilter intentFilter; 
	private static boolean isStarted;// modify by liangzhang 2014-08-05
	private static AudioManager mAudioManager;
    static{
    	mReceiver = new HeadsetPlugReceiver();
    	intentFilter = new IntentFilter(); 
        intentFilter.addAction("android.intent.action.HEADSET_PLUG"); 
        mAudioManager = (AudioManager) SipUAApp.mContext
				.getSystemService(Context.AUDIO_SERVICE);
    }

	protected boolean mIsMusicActive;

	/**
	 * this class handle message for Media button event receiver .add by mou 2014-0-23
	 */
	private class StateChangedHandler extends Handler{
		public StateChangedHandler(){
			LogUtil.makeLog(tag, "new StateChangedHandler()");
		}
		private String tag = "StateChangedHandler";

		public void handleMessage(Message msg) {
			StringBuilder builder = new StringBuilder("handleMessage()"+" what = "+msg.what+", arg1 = "+msg.arg1);
			if (!isStarted) {
				builder.append(" isStarted is false ignore");
				LogUtil.makeLog(tag, builder.toString());
				return;
			}
			if (!SipUAApp.isHeadsetConnected) {
				builder.append(" SipUAApp.isHeadsetConnected is flase ignore");
				LogUtil.makeLog(tag, builder.toString());
				return;
			}
			Message message = obtainMessage();
			switch (msg.what) {
			case HEADSET_PLUG_DISCONNECTED:
				builder.append(" HEADSET_PLUG_DISCONNECTED");
				if (msg.arg1 != mStateChangeCount) {
					builder.append(" msg.arg1 != mStateChangeCount ignore");
					break;
				}
				builder.append(" stop MediaButtonReceiver");
				MediaButtonReceiver.stopReceive(SipUAApp.mContext);
				break;
			case HEADSET_PLUG_CONNECTED:
				builder.append(" HEADSET_PLUG_CONNECTED");
				if (msg.arg1 != mStateChangeCount) {
					builder.append(" msg.arg1 != mStateChangeCount ignore");
					break;
				}
				builder.append(" registerMediaButtonEventReceiver");
				MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
				removeMessages(REGISTER_MEDIABUTTON_EVENT_RECEIVER);
				if (needRegisterMBERP) {
					builder.append(" send regiser again delay "+REGISTER_AGAIN_DELAY);
					message.what = REGISTER_MEDIABUTTON_EVENT_RECEIVER;
					sendMessageDelayed(message,REGISTER_AGAIN_DELAY );
				}
				break;
			case CHECK_MUSIC_ACTIVE:
				builder.append(" CHECK_MUSIC_ACTIVE");
				boolean active = mAudioManager.isMusicActive();
				if (active != mIsMusicActive) {
					mIsMusicActive = active;
					builder.append(" stop and start MediaButtonReceiver");
				}
				sendMessageDelayed(message, 2000);
				MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
				break;
			case REGISTER_MEDIABUTTON_EVENT_RECEIVER:
				builder.append(" REGISTER_MEDIABUTTON_EVENT_RECEIVER");
				MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
				removeMessages(REGISTER_MEDIABUTTON_EVENT_RECEIVER);
				if (needRegisterMBERP) {
					builder.append(" send regiser again delay "+REGISTER_AGAIN_DELAY);
					message.what = REGISTER_MEDIABUTTON_EVENT_RECEIVER;
					sendMessageDelayed(message,REGISTER_AGAIN_DELAY );
				}
				break;
			default:
				builder.append(" error message");
				break;
			}
			LogUtil.makeLog(tag, builder.toString());
		};
		public void removeAllMessages() {
			removeMessages(HEADSET_PLUG_DISCONNECTED);
			removeMessages(HEADSET_PLUG_CONNECTED);
			removeMessages(CHECK_MUSIC_ACTIVE);
			removeMessages(REGISTER_MEDIABUTTON_EVENT_RECEIVER);
			LogUtil.makeLog(tag, "removeAllMessages()");
		}
	}
    /**
	 * stateChangedHandler 
	 * add by oumogang 2014-10-17
	 */
	private static StateChangedHandler stateChangedHandler ;
	/**
	 * delay one second to start or stop MediaButtonReceiver. add by mou 2014-10-17
	 * @param state
	 */
	private void onStateChanged(int state) {
		// TODO Auto-generated method stub
		synchronized (HeadsetPlugReceiver.this) {
			mLastState = state;
			mStateChangeCount++;
			Message msg = stateChangedHandler.obtainMessage();
			msg.what = state;
			msg.arg1 = mStateChangeCount;
			stateChangedHandler.sendMessageDelayed(msg, 1000);
		}
	}
	private int mLastState = 0;
	private int mStateChangeCount;
    @Override 
    public void onReceive(Context context, Intent intent) {
    	StringBuilder builder = new StringBuilder("HeadsetPlugReceiver#onReceive()");
        if (intent.hasExtra("state")){ 
        	int state = intent.getIntExtra("state", 0);
        	builder.append(" state = "+state);
        	// 避免处理重复的消息。
			if (state == mLastState ) {
				builder.append(" state == mLastState ignore");
				LogUtil.makeLog(TAG, builder.toString());
				return;
			}
			//remove all messages and reinit handler.
			if (stateChangedHandler != null) {
				stateChangedHandler.removeAllMessages();
			}
			needRegisterMBERP = needRegisterMediaButtonEventReceiverPeriodically(builder);
			stateChangedHandler = new StateChangedHandler();
			onStateChanged(state);
            if (intent.getIntExtra("state", 0) == 0){ 
				SipUAApp.lastHeadsetConnectTime = System.currentTimeMillis();
				SipUAApp.isHeadsetConnected = false;
				builder.append(" isHeadsetConnected is false");
				if (UserAgent.ua_ptt_mode) {
					// 拔出耳机时释放话权 add by oumogang 2013-09-04
					if (GroupCallUtil.mIsPttDown) {
						builder.append(" GroupCallUtil.mIsPttDown is true, makeGroupCall(false, true)");
						GroupCallUtil.makeGroupCall(false, true);
					}
					builder.append(" is ptt mode setMode AudioUtil.MODE_SPEAKER");
					AudioUtil.getInstance().setAudioConnectMode(
							AudioUtil.MODE_SPEAKER);
				} else {
					builder.append(" is not ptt mode setMode AudioUtil.MODE_HOOK");
					AudioUtil.getInstance().setAudioConnectMode(
							AudioUtil.MODE_HOOK);
					//修改单呼中切换成扬声器模式后拨出手咪，自动切换成听筒模式后第一次切换成扬声器失败的问题；add by mou 2015-01-05
					if (CallUtil.isInCall()) {
						RtpStreamReceiver_signal.speakermode = AudioManager.MODE_IN_CALL;
					}
				}
			} else if (intent.getIntExtra("state", 0) == 1) {
				SipUAApp.isHeadsetConnected = true;
				builder.append(" isHeadsetConnected is true");
				if (GroupCallUtil.getGroupCallState() != GroupCallUtil.STATE_SHUTDOWN
						|| CallUtil.isInCall()) {
					AudioUtil.getInstance()
					.setAudioConnectMode(AudioUtil.MODE_HOOK);
					//修改单呼中切换成扬声器模式后插入手咪，自动切换成听筒模式后第一次切换成扬声器失败的问题；add by mou 2015-01-05
					if (CallUtil.isInCall()) {
						RtpStreamReceiver_signal.speakermode = AudioManager.MODE_IN_CALL;
					}
				}
				MediaButtonReceiver.stopReceive(SipUAApp.mContext);
				MediaButtonReceiver.startReceive(SipUAApp.mContext);
			}
			RtpStreamSenderUtil.reCheckNeedSendMuteData("HeadsetPlugReceiver#onReceive()");
		}
		LogUtil.makeLog(TAG, builder.toString());
    } 
    
    private static String[] needRegisterMBERPDevices = new String[]{"HUAWEI MT7"};
    private boolean isFirstChecking = true;
    private static boolean needRegisterMBERP = false;
    /**
     * 有线手咪机型适配，有些机型需要定期注册媒体按键事件接收器。
     * @param builder
     * @return
     */
    private boolean needRegisterMediaButtonEventReceiverPeriodically(StringBuilder builder) {
		// TODO Auto-generated method stub
    	boolean result = false;
    	for (String deviceModel : needRegisterMBERPDevices) {
			if (Build.MODEL.contains(deviceModel)) {
				builder.append(" device "+Build.MODEL);
				builder.append(" needRegisterMBERP is true");
				result = true;
				break;
			}
		}
		return result;
	}

    /**
     * on scream on or off ,send message to register receiver delay 1000ms.
     * @param on
     */
    public static synchronized void onScreamStateChanged(Boolean on) {
    	StringBuilder builder = new StringBuilder("onScreamStateChanged("+(on?"on":"off")+")");
    	if (!SipUAApp.isHeadsetConnected) {
    		builder.append(" SipUAApp.isHeadsetConnected is false ignore");
    		LogUtil.makeLog(TAG, builder.toString());
    		return;
		}
    	Message message = stateChangedHandler.obtainMessage();
    	message.what = REGISTER_MEDIABUTTON_EVENT_RECEIVER;
    	stateChangedHandler.sendMessageDelayed(message,1000);
    	builder.append(" sendMessage REGISTER_MEDIABUTTON_EVENT_RECEIVER Delayed 1000");
    	LogUtil.makeLog(TAG, builder.toString());
    }
    @Deprecated
	public static synchronized void restartReceive(Context mContext) {
    	LogUtil.makeLog(TAG, "restartReceive()");
    	mReceiver.mLastState = 0;
    	stopReceive(mContext);
    	startReceive(mContext);
    }
	public static synchronized void startReceive(Context mContext) {
		StringBuilder builder = new StringBuilder("startReceive()");
		// modify by liangzhang 2014-08-05 修改退出集群通后长按HOME键进入打开集群通黑屏问题
		if (!isStarted) {
			isStarted = true;
			builder.append(" isStarted is false registerReceiver()");
			mContext.registerReceiver(mReceiver, intentFilter);
		}else {
			builder.append(" isStarted is true ignore");
		}
		LogUtil.makeLog(TAG, builder.toString());
	}

	public static synchronized void stopReceive(Context mContext) {
		// modify by liangzhang 2014-08-05 修改退出集群通后长按HOME键进入打开集群通黑屏问题
		StringBuilder builder = new StringBuilder("stopReceive()");
		if (isStarted) {
			isStarted = false;
			builder.append(" isStarted is true unregisterReceiver()");
			mContext.unregisterReceiver(mReceiver);
		}else {
			builder.append(" isStarted is false ignore");
		}
		LogUtil.makeLog(TAG, builder.toString());
	}
 
}


