package com.zed3.utils;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.media.mediaButton.MediaButtonPttEventProcesser;
import com.zed3.ptt.PttEventDispatcher;
import com.zed3.ptt.PttEventDispatcher.PttEvent;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;

public class RtpStreamSenderUtil {

	private static boolean sNeedSendMuteData;
	private static final String TAG = "RtpStreamSenderUtil";


	/**
	 * recheck need send mute data for encoder 。 add by mou 2014-08-21
	 * need send mute data：
	 * 1.UserAgent.ua_ptt_mode is false,ptt up,headset is connected or bluetooth spp is connected;
	 * 2.UserAgent.ua_ptt_mode is true,ptt up;
	 * 3.MODE_BLUETOOTHSCO and isBluetoothScoOn is false。
	 * Call this method when the value in the method is changed。
	 * @param tag 
	 */
	public static void reCheckNeedSendMuteData(String tag) {
		// TODO Auto-generated method stub
		// 做有线手咪松开ppt时发静音处理
		// 单呼状态下，插入手咪后，用户松开手咪，发静音包 modify by oumogang 2014-04-28
		// 在蓝牙模式下，需要发静音包。modify by mou 2014-07-30
		// 对讲状态下，松开PTT后发静音包。modify by mou 2014-07-30
		synchronized (RtpStreamSenderUtil.class) {
			StringBuilder builder = new StringBuilder("RtpStreamSenderUtil#reCheckNeedSendMuteData("+tag+") ");
			boolean result = false;
//			MyAudioManager audioManager = MyAudioManager.getInstance();
			if ((!UserAgent.ua_ptt_mode && !/*MediaButtonPttEventProcesser.mIsPttDowned*/(PttEventDispatcher.sPttEvent == PttEvent.PTT_DOWN))
					&& (SipUAApp.isHeadsetConnected /*|| MyBluetoothManager
						.getInstance().isBluetoothSppConnected()*/)) {
				if (SipUAApp.isHeadsetConnected) {
					builder.append(" SipUAApp.isHeadsetConnected is true");
				}else {
					builder.append(" MyBluetoothManager#isBluetoothSppConnected() is true");
				}
				result = true;
			} else if (UserAgent.ua_ptt_mode && !GroupCallUtil.mIsPttDown) {
				builder.append("UserAgent.ua_ptt_mode && !GroupCallUtil.mIsPttDown");
				result = true;
			} /*else if (audioManager.getRealMode() == MyAudioManager.MODE_BLUETOOTHSCO
					&& !audioManager.isBluetoothScoOn()) {
				builder.append(" realmode is MODE_BLUETOOTHSCO && !audioManager.isBluetoothScoOn()";
				result = true;
			}*/
			sNeedSendMuteData = result;
			builder.append(" sNeedSendMuteData is "+sNeedSendMuteData);
			//		sNeedSendMuteData = (!UserAgent.ua_ptt_mode && !MediaButtonPttEventProcesser.mIsPttDowned)
	//				&& (SipUAApp.isHeadsetConnected || MyBluetoothManager
	//						.getInstance().isBluetoothSppConnected())
	//				|| (UserAgent.ua_ptt_mode && !GroupCallUtil.mIsPttDown)
	//				|| (MyAudioManager.getInstance().getRealMode() == MyAudioManager.MODE_BLUETOOTHSCO && !MyAudioManager
	//						.getInstance().isBluetoothScoOn());
	//		MyBluetoothManager.getInstance().makeLog(tag, logMsg);
			LogUtil.makeLog(tag, builder.toString());
		}
	}
	public static boolean needSendMuteData() {
		return sNeedSendMuteData;
	}
	public static void setNeedSendMuteData(boolean needSendMuteData, String tag) {
		// TODO Auto-generated method stub
		sNeedSendMuteData = needSendMuteData;
		LogUtil.makeLog(TAG, " setNeedSendMuteData("+needSendMuteData+","+tag+")");
	}

}
