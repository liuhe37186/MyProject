package com.zed3.audio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamReceiverUtil;
import com.zed3.utils.Tools;

public class AudioUtil implements AudioUitlInterface {

	
	public static final int MODE_RINGTONE = /*Integer.MAX_VALUE*/1;
	public static final int MODE_HOOK = /*Integer.MAX_VALUE*/2;
	public static final int MODE_SPEAKER = /*Integer.MAX_VALUE-1*/3;
	public static final int MODE_BLUETOOTH = /*Integer.MAX_VALUE-2*/4;
	public static final String tag = "AudioUtil";
	private static final String KEY_AUDIO_CONNECT_MODE = "KEY_AUDIO_CONNECT_MODE";
	private static final String KEY_GROUPCALL_AUDIO_CONNECT_MODE = "KEY_GROUPCALL_AUDIO_CONNECT_MODE";
	private static final String KEY_VIDEOCALL_AUDIO_CONNECT_MODE = "KEY_VIDEOCALL_AUDIO_CONNECT_MODE";
	private static final String KEY_AUDIOCALL_AUDIO_CONNECT_MODE = "KEY_AUDIOCALL_AUDIO_CONNECT_MODE";
	private static final String KEY_ANTOCALL_AUDIO_CONNECT_MODE = "KEY_ANTOCALL_AUDIO_CONNECT_MODE";
	
	public static final int TYPE_GROUPCALL = 10;
	public static final int TYPE_VIDEOCALL = 11;
	public static final int TYPE_AUDIOCALL = 12;
	public static final int TYPE_ANTOCALL = 13;
	public static final String ACTION_STREAM_CHANGED = "stream changed";
	public static final String ACTION_SPEAKERPHONE_STATE_CHANGED = "speakerphone changed";
	public static final String KEY_STREAM_INT = "key stream int";
	
	public static AudioUtil mInstance;
	public static AudioManager mAudioManager;
	private static int MODE_IN_COMMUNICATION;
	public static AudioUtil getInstance() {
		return mInstance;
	}

	private int mMode = 0;
	private int mStream = AudioManager.STREAM_VOICE_CALL;

	public synchronized int getMode() {
		int mode = mAudioManager.getMode();
		LogUtil.makeLog(tag, "getMode()"+getModeStr(mode));
		return mode;
	}

	public synchronized void setMode(int mode) {
		mAudioManager.setMode(mode);
		LogUtil.makeLog(tag, "setMode("+getModeStr(mode)+")");
	}

	private AudioUtil() {
		super();
	}

	static{
		MODE_IN_COMMUNICATION = getBestMode();
		mInstance = new AudioUtil();
		mAudioManager = (AudioManager) SipUAApp.mContext
				.getSystemService(Context.AUDIO_SERVICE);
	}
	@Override
	public synchronized void setAudioConnectMode(int mode) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("setAudioConnectMode()");
		//修改发起对讲后接通系统电话后声音走扬声器而不走听筒的bug。modify by mou 2015-01-14
		//系统通话中不处理音频  add by mou 2014-10-10
//		if (MyPhoneStateListener.getInstance().isInCall()) {
//			builder.append(" MyPhoneStateListener#isInCall() is true ignore");
//			ZMBluetoothManager.getInstance().makeLog(tag, builder.toString());
//			return;
//		}
		
		//idata device set to max volume。mofify by mou 2015-01-14
//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
		
		needSetSpeakerphoneOnFalse = needSetSpeakerphoneOnFalse(builder);
		long beginTime = System.currentTimeMillis();
		this.mMode = mode;
		switch (mode) {
		case MODE_RINGTONE:
			builder.append(" MODE_RINGTONE");
//			if (mAudioManager.isBluetoothScoOn()) {
//				builder.append(" setAudioConnectMode(MODE_HOOK) stopBluetoothSco(),setBluetoothScoOn(false)");
//				mAudioManager.stopBluetoothSco();
//				mAudioManager.setBluetoothScoOn(false);
//			}
//			if (!mAudioManager.isBluetoothA2dpOn()) {
//				builder.append(" setAudioConnectMode(MODE_HOOK) setBluetoothA2dpOn(true)");
//				mAudioManager.setBluetoothA2dpOn(true);
//			}
			
			if (checkDevice()&&mAudioManager.getMode()!= AudioManager.MODE_RINGTONE) {
				builder.append(" setAudioConnectMode(MODE_RING) setMode(AudioManager.MODE_RINGTONE)");
				setMode(AudioManager.MODE_RINGTONE);
			}else {
				if (Receiver.mSipdroidEngine != null) {
//					Receiver.mSipdroidEngine.speaker(AudioManager.MODE_IN_CALL);
				}
			}
//			if (!mAudioManager.isSpeakerphoneOn()) {
//				builder.append(" setAudioConnectMode(MODE_RING) setSpeakerphoneOn(true");
//				setSpeakerphoneOn(true);
//			}
			break;
		case MODE_HOOK:
			builder.append(" MODE_HOOK");
			if (Build.BRAND.equalsIgnoreCase("motorola")&&Build.MODEL.equals("XT885")) {
				return;
			}
			
			if (mAudioManager.isBluetoothScoOn()) {
				builder.append(" setAudioConnectMode(MODE_HOOK) stopBluetoothSco(),setBluetoothScoOn(false)");
				mAudioManager.stopBluetoothSco();
				mAudioManager.setBluetoothScoOn(false);
			}
//			if (!mAudioManager.isBluetoothA2dpOn()) {
//				builder.append(" setAudioConnectMode(MODE_HOOK) setBluetoothA2dpOn(true)");
//				mAudioManager.setBluetoothA2dpOn(true);
//			}
			if (checkDevice()/*&&mAudioManager.getMode()!= AudioManager.MODE_IN_CALL*/) {
				if (SipUAApp.isHeadsetConnected) {
					builder.append(" SipUAApp.isHeadsetConnected");
					//HUAWEI MT7-UL00
					if (needSetModeInCommunication(builder)) {
						setMode(getBestMode());
						setSpeakerphoneOn(false);
						break;
					}
					if (mAudioManager.getMode()!= AudioManager.MODE_NORMAL) {
						builder.append(" setMode(AudioManager.MODE_NORMAL)");
						setMode(AudioManager.MODE_NORMAL);
					}
				}else {
					if(Build.MODEL.contains("RG") || Build.MODEL.contains("7296") || Build.MODEL.contains("V818") || Build.MODEL.contains("Y600") || Build.MODEL.contains("7295")){
						builder.append(" setMode(AudioManager.MODE_NORMAL)");
						setMode(AudioManager.MODE_NORMAL);
					}
					else if (mAudioManager.getMode()!= MODE_IN_COMMUNICATION) {
						builder.append(" setMode("+getModeStr(MODE_IN_COMMUNICATION)+")");
						setMode(MODE_IN_COMMUNICATION);
					}
				}
			}else {
				if (Receiver.mSipdroidEngine != null) {
//					Receiver.mSipdroidEngine.speaker(AudioManager.MODE_IN_CALL);
				}
			}
//			if (mAudioManager.isSpeakerphoneOn()) {
//				builder.append(" setSpeakerphoneOn(false");
//				setSpeakerphoneOn(false);
//			}
			setSpeakerphoneOn(false);
			break;
		case MODE_SPEAKER:
			builder.append(" MODE_SPEAKER");
			if (mAudioManager.isBluetoothScoOn()) {
				builder.append(" stopBluetoothSco(),setBluetoothScoOn(false)");
				mAudioManager.stopBluetoothSco();
				mAudioManager.setBluetoothScoOn(false);
			}
//			if (!mAudioManager.isBluetoothA2dpOn()) {
//				builder.append(" setAudioConnectMode(MODE_SPEAKER) setBluetoothA2dpOn(true)");
//				mAudioManager.setBluetoothA2dpOn(true);
//			}
			if (checkDevice()/*&&mAudioManager.getMode()!= AudioManager.MODE_NORMAL*/) {
				if (SipUAApp.isHeadsetConnected) {
					builder.append(" SipUAApp.isHeadsetConnected");
					//HUAWEI MT7-UL00
					if (needSetModeInCommunication(builder)) {
						setMode(getBestMode());
						setSpeakerphoneOn(true);
						break;
					}
				}
				if (mAudioManager.getMode()!= AudioManager.MODE_NORMAL) {
					builder.append(" setMode(AudioManager.MODE_NORMAL)");
					if(!Build.MODEL.toLowerCase().contains("g716-l070"))
						setMode(AudioManager.MODE_NORMAL);
				}
			}else {
				if (Receiver.mSipdroidEngine != null) {
//					Receiver.mSipdroidEngine.speaker(AudioManager.MODE_NORMAL);
				}
			}
			//如果耳机已插入，就不需要设置 SpeakerphoneOn 为true。 modify by mou 2014-09-10
//			if (SipUAApp.isHeadsetConnected) {
//				if (mAudioManager.isSpeakerphoneOn()) {
//					builder.append(" SipUAApp.isHeadsetConnected is true setSpeakerphoneOn(false)");
//					setSpeakerphoneOn(false);
//				}
//			}else {
//				if (needSetSpeakerphoneOnFalse && mAudioManager.isSpeakerphoneOn()) {
//					setSpeakerphoneOn(false);
//				}else if(!needSetSpeakerphoneOnFalse && !mAudioManager.isSpeakerphoneOn()) {
//					builder.append(" setSpeakerphoneOn(true)");
//					setSpeakerphoneOn(true);
//				}
//			}
			if (needSetSpeakerphoneOnFalse /*&& mAudioManager.isSpeakerphoneOn()*/) {
				setSpeakerphoneOn(false);
			}else if(!needSetSpeakerphoneOnFalse /*&& !mAudioManager.isSpeakerphoneOn()*/) {
				builder.append(" setSpeakerphoneOn(true)");
				setSpeakerphoneOn(true);
			}
			break;
		case MODE_BLUETOOTH:
			
			if (checkDevice()&&mAudioManager.getMode()!= AudioManager.MODE_IN_CALL) {
				if (mAudioManager.getMode()!= MODE_IN_COMMUNICATION) {
					builder.append(" setMode("+getModeStr(MODE_IN_COMMUNICATION)+")");
					setMode(MODE_IN_COMMUNICATION);
				}
			}
//			if (mAudioManager.isBluetoothA2dpOn()) {
//				builder.append(" setAudioConnectMode(MODE_BLUETOOTH) mAudioManager.setBluetoothA2dpOn(false)");
//				mAudioManager.setBluetoothA2dpOn(false);
//			}
			if (!mAudioManager.isBluetoothScoOn()) {
				builder.append(" mAudioManager.startBluetoothSco(),setBluetoothScoOn(true)");
				mAudioManager.startBluetoothSco();
				mAudioManager.setBluetoothScoOn(true);
			}
//			if (mAudioManager.isSpeakerphoneOn()) {
//				builder.append(" setSpeakerphoneOn(false");
//				setSpeakerphoneOn(false);
//			}
			setSpeakerphoneOn(false);
			break;

		default:
			builder.append(" setAudioConnectMode() unkown mode error");
			break;
		}
		
		RtpStreamReceiverUtil.onAudioModeChanged(mAudioManager.getMode());
		
		long time = System.currentTimeMillis()-beginTime;
		builder.append(" setAudioConnectMode() need time "+time+" ms");
		ZMBluetoothManager.getInstance().makeLog(tag, builder.toString());
	}

	private boolean checkDevice() {
		// TODO Auto-generated method stub
		
		if (Build.MODEL.equals("XT885")) {
			return false;
		}
		return true;
	}

	@Override
	public int getCurrentMode() {
		// TODO Auto-generated method stub
		return mMode;
	}

	private static SharedPreferences getSharedPreferences(Context context) {
		// TODO Auto-generated method stub
		return context.getSharedPreferences("com.zed3.app", Context.MODE_PRIVATE);
	} 
	@Override
	public void startBluetoothSCO() {
		// TODO Auto-generated method stub
		if (!mAudioManager.isBluetoothScoOn()) {
			MyLog.i(tag, "startConnectSco() startBluetoothSco(),setBluetoothScoOn(true)");
			mAudioManager.startBluetoothSco();
			mAudioManager.setBluetoothScoOn(true);
		}
	}

	@Override
	public void stopBluetoothSCO() {
		// TODO Auto-generated method stub
		if (mAudioManager.isBluetoothScoOn()) {
			MyLog.i(tag, "stopConnectSco() stopBluetoothSco(),setBluetoothScoOn(false)");
			mAudioManager.setBluetoothScoOn(false);
			mAudioManager.stopBluetoothSco();
		}
	}


	@Override
	public boolean checkMode(int mode) {
		// TODO Auto-generated method stub
		boolean result = false;
		switch (mode) {
		case MODE_HOOK:
			MyLog.i(tag, "checkMode("+mode+"),is MODE_HOOK");
			result = true;
			break;
		case MODE_SPEAKER:
			MyLog.i(tag, "checkMode("+mode+"),is MODE_SPEAKER");
			result = true;
			break;
		case MODE_BLUETOOTH:
			MyLog.i(tag, "checkMode("+mode+"),is MODE_BLUETOOTH");
			result = true;
			break;
		default:
			MyLog.e(tag, "checkMode("+mode+"),unkown mode error");
			result = false;
			break;
		}
		return result;
	}

	@Override
	public boolean setCustomMode(int type,int mode) {
		
		MyLog.i(tag, "saveMode("+mode+")");
		if (checkMode(mode)) {
			SharedPreferences sharedPreferences = getSharedPreferences(SipUAApp.mContext);
			Editor editor = sharedPreferences.edit();//获取编辑器
			switch (type) {
			case TYPE_GROUPCALL:
				editor.putInt(KEY_GROUPCALL_AUDIO_CONNECT_MODE,  mode);
				break;
			case TYPE_VIDEOCALL:
				editor.putInt(KEY_VIDEOCALL_AUDIO_CONNECT_MODE,  mode);
				break;
			case TYPE_AUDIOCALL:
				editor.putInt(KEY_AUDIOCALL_AUDIO_CONNECT_MODE,  mode);
				break;
			case TYPE_ANTOCALL:
				editor.putInt(KEY_ANTOCALL_AUDIO_CONNECT_MODE,  mode);
				break;
			default:
				break;
			}
			editor.commit();//提交修改
			MyLog.i(tag, "saveMode("+mode+")");
			return true;
		}else {
			MyLog.e(tag, "saveMode("+mode+"),bad mode error");
			return false;
		}
	}

	@Override
	public int getCustomMode(int type) {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences(SipUAApp.mContext);
		switch (type) {
		case TYPE_GROUPCALL:
			mMode = sharedPreferences.getInt(KEY_GROUPCALL_AUDIO_CONNECT_MODE, MODE_SPEAKER);
			break;
		case TYPE_VIDEOCALL:
			mMode = sharedPreferences.getInt(KEY_VIDEOCALL_AUDIO_CONNECT_MODE, MODE_HOOK);
			break;
		case TYPE_AUDIOCALL:
			mMode = sharedPreferences.getInt(KEY_AUDIOCALL_AUDIO_CONNECT_MODE, MODE_HOOK);
			break;
		case TYPE_ANTOCALL:
			mMode = sharedPreferences.getInt(KEY_ANTOCALL_AUDIO_CONNECT_MODE, MODE_HOOK);
			break;
		default:
			mMode = MODE_SPEAKER;
			break;
		}
		return mMode;
	}

	@Override
	public void setVolumeControlStream(Activity activity) {
		// TODO Auto-generated method stub
//		switch (mStream) {
//		case AudioManager.STREAM_VOICE_CALL:
//			activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//			break;
//		case AudioManager.STREAM_MUSIC:
//			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//			break;
//		default:
//			activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//			break;
//		}
//		int mode = mAudioManager.getMode();
//		switch (mode) {
//		case AudioManager.MODE_IN_CALL:
//			activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//			break;
//		case AudioManager.MODE_NORMAL:
//			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//			break;
//		case AudioManager.MODE_RINGTONE:
//			activity.setVolumeControlStream(AudioManager.STREAM_RING);
//			break;
//		case AudioManager.MODE_CURRENT:
//			
//			break;
//		case AudioManager.MODE_IN_COMMUNICATION:
//			break;
//		case AudioManager.MODE_INVALID:
//			break;
//		default:
//			break;
//		}
	}

	/**
	 * setStream for ui VolumeControl
	 * add by oumogang 2014-05-05
	 */
	public void setStream(int streamType) {
		// TODO Auto-generated method stub
		mStream  = streamType;
		Intent intent = new Intent(ACTION_STREAM_CHANGED);
		Bundle extras = new Bundle();
		extras.putInt(KEY_STREAM_INT, streamType);
		intent.putExtras(extras);
		SipUAApp.getAppContext().sendBroadcast(intent);
	}
	public synchronized void setSpeakerphoneOn(Boolean on) {
		mAudioManager.setSpeakerphoneOn(on);
		Intent intent = new Intent(ACTION_SPEAKERPHONE_STATE_CHANGED);
		SipUAApp.getAppContext().sendBroadcast(intent);
		LogUtil.makeLog(tag, "setSpeakerphoneOn("+on+")");
	}
	public synchronized Boolean isSpeakerphoneOn() {
		Boolean on = mAudioManager.isSpeakerphoneOn();
		LogUtil.makeLog(tag, "isSpeakerphoneOn() "+on);
		return on;
	}
	/**
	 * checkMate7     add by mou 2014-10-20
	 * @param builder
	 * @return
	 */
	private boolean checkMate7(StringBuilder builder) {
		// TODO Auto-generated method stub
		//适配 HUAWEI MT7-UL00
		if (Build.MODEL.contains("HUAWEI MT7")) {
			builder.append(" Build.MODEL.contains(HUAWEI MT7) "+Build.MODEL);
//			if (mAudioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
//				builder.append(" setMode(AudioManager.MODE_IN_COMMUNICATION");
//				setMode(AudioManager.MODE_IN_COMMUNICATION);
//			}
//			if (mAudioManager.isSpeakerphoneOn() == true) {
//				builder.append(" setSpeakerphoneOn(false)");
//				setSpeakerphoneOn(false);
//			}
			return true;
		}
		return false;
	}
	private boolean needSetModeInCommunication(StringBuilder builder) {
		// TODO Auto-generated method stub
		if (Tools.matchDevice(Contants.MODEL_ZTE_V5)) {
			builder.append(" Build.MODEL.contains("+Contants.MODEL_ZTE_V5+") "+Build.MODEL);
		}else if (Tools.matchDevice(Contants.MODEL_HUAWEI_MATE7)) {
			builder.append(" Build.MODEL.contains("+Contants.MODEL_HUAWEI_MATE7+") "+Build.MODEL);
		}else {
			return false;
		}
		return true;
	}
	/**
	 * checkMate7     add by mou 2014-10-20
	 * @param builder
	 * @return
	 */
	private boolean checkV5(StringBuilder builder) {
		// TODO Auto-generated method stub
		//适配 HUAWEI MT7-UL00
		if (Build.MODEL.contains("N918St")) {
			builder.append(" Build.MODEL.contains(N918St) "+Build.MODEL);
//			if (mAudioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
//				builder.append(" setMode(AudioManager.MODE_IN_COMMUNICATION");
//				setMode(AudioManager.MODE_IN_COMMUNICATION);
//			}
//			if (mAudioManager.isSpeakerphoneOn() == true) {
//				builder.append(" setSpeakerphoneOn(false)");
//				setSpeakerphoneOn(false);
//			}
			return true;
		}
		return false;
	}
	/**
	 * exit to set to MODE_NORMAL and set speakerphone on fasle add by mou 2014-10-20
	 */
	public void exit() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("exit()");
		if (mAudioManager.getMode() != AudioManager.MODE_NORMAL) {
			builder.append(" setMode(AudioManager.MODE_NORMAL)");
			setMode(AudioManager.MODE_NORMAL);
		}
		if (mAudioManager.isSpeakerphoneOn()) {
			builder.append(" setSpeakerphoneOn(false)");
			setSpeakerphoneOn(false);
		}
		LogUtil.makeLog(tag, builder.toString());
	}

	/**
	 * Devices that microphone's volume is higher when speakphone on in MODE_NORMAL. Such as SAMUMG SM-N9008.
	 */
	//这样适配 SM-N9008，会导致对讲在通话过后不走扬声器。暂时回退。 modify by mou 2014-11-03
	private static String[] needSetSpeakerphoneOnFalseDevices = new String[] {/*"SM-N9008"*/ };
	private boolean isFirstChecking = true;
	private static boolean needSetSpeakerphoneOnFalse = false;

	/**
	 * Some device's microphone's volume is higher when speakerphone on in MODE_NORMAL. 
	 * Need to set speakerphone on to false.
	 * add by mou 2014-10-27
	 * @param builder
	 * @return
	 */
	private boolean needSetSpeakerphoneOnFalse(
			StringBuilder builder) {
		// TODO Auto-generated method stub
		boolean result = needSetSpeakerphoneOnFalse;
		if (isFirstChecking) {
			isFirstChecking = false;
			for (String deviceModel : needSetSpeakerphoneOnFalseDevices) {
				if (Build.MODEL.contains(deviceModel)) {
					builder.append(" device " + Build.MODEL);
					builder.append(" needSetSpeakerphoneOnFalse is true");
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * get the best mode for current device. add by mou 2014-11-05
	 * @return
	 */
	public synchronized static int getBestMode() {
		// TODO Auto-generated method stub
		int mode = AudioManager.MODE_IN_CALL;
		if (Build.VERSION.SDK_INT>10) {
			mode = AudioManager.MODE_IN_COMMUNICATION;
		}else {
			mode = AudioManager.MODE_IN_CALL;
		}
		return mode;
	}
	/**
	 * get mode string for logs. add by mou 2014-11-05
	 * @param mode
	 * @return
	 */
	public String getModeStr(int mode) {
		// TODO Auto-generated method stub
		String modeStr = null;
		switch (mode) {
		case AudioManager.MODE_CURRENT:
			modeStr = "AudioManager.MODE_CURRENT";
			break;
		case AudioManager.MODE_IN_CALL:
			modeStr = "AudioManager.MODE_IN_CALL";
			break;
		case AudioManager.MODE_IN_COMMUNICATION:
			modeStr = "AudioManager.MODE_IN_COMMUNICATION";
			break;
		case AudioManager.MODE_INVALID:
			modeStr = "AudioManager.MODE_INVALID";
			break;
		case AudioManager.MODE_NORMAL:
			modeStr = "AudioManager.MODE_NORMAL";
			break;
		case AudioManager.MODE_RINGTONE:
			modeStr = "AudioManager.MODE_RINGTONE";
			break;
		default:
			modeStr = "mode("+mode+") MODE_???????";
			break;
		}
		return modeStr;
	}

}
