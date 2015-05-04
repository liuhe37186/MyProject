package com.zed3.ptt;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.DemoCallScreen;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamSenderUtil;

public final class PttEventDispatcher {
	private static final String TAG = "PttEventDispatcher";
	private StringBuilder builder = new StringBuilder();
	public static PttEvent sPttEvent = PttEvent.PTT_UP;

	private PttEventDispatcher() {
		// TODO Auto-generated constructor stub
	}
	private static final class InstanceCreater {
		public static PttEventDispatcher sInstance = new PttEventDispatcher();
	}
	public static PttEventDispatcher getInstance() {
		// TODO Auto-generated method stub
		return InstanceCreater.sInstance;
	}
	
	public enum PttEvent {
		PTT_DOWN,PTT_UP
	}
	
	public synchronized boolean dispatch(PttEvent event){
		sPttEvent  = event;
		interceptEvent(event);
		
		if(PttEvent.PTT_DOWN == event){
			
			onPttDown(event);
			
		} else if(PttEvent.PTT_UP == event){
			
			onPttUp(event);
			
		}
		
		return true;
		
	}

	private void onPttUp(PttEvent event) {
		// TODO Auto-generated method stub
		clearBuilder();
		/**
		 * 通过手咪控制单呼通话
		 * 1.语音来电，按住手咪PTT键接听，或单击有线耳机媒体按键接听；
		 * 2.语音通话中，松开手咪PTT键，只听不讲；
		 * 3.语音通话中，按下手咪PTT键，恢复讲话；
		 * 
		 * 修改：
		 * 1.只考虑有线手咪，不兼容有线耳机。（正常模式和有线手咪模式，只需判断是否有插入有线设备）
		 * 2.进入通话后，先停止语音发送，当用户再按下后才发送；
		 *   来电时头一次按下接听，在进入通话状态前，不再处理down up事件，进入通话中后根据PTT状态决定是否开启讲话模式；
		 *   去电时按下不处理，在进入通话状态前，不再处理down up事件，进入通话中后根据PTT状态决定是否开启讲话模式；
		 *   接听后按下
		 * 
		 */
		
		if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
			//单呼模式
			builder.append(" UA_STATE_INCOMING_CALL GroupCallUtil.makeGroupCall(false, true);");
			GroupCallUtil.makeGroupCall(false, true);
		}
		//sometimes call_state stay on UA_STATE_INCALL,should check ua_ptt_mode before checking call_state. modify by mou 2015-01-23
		else if(UserAgent.ua_ptt_mode){
			//对讲模式
			builder.append(" GroupCallUtil.makeGroupCall(false, true);");
			GroupCallUtil.makeGroupCall(false, true);
		}
		else if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
//			AmrEncodSender.reCheckNeedSendMuteData();
			builder.append(" UserAgent.UA_STATE_INCALL setNeedWriteAudioData(true)");
//			RtpStreamReceiverUtil.setNeedWriteAudioData(true);
			RtpStreamSenderUtil.setNeedSendMuteData(true,TAG);
		}
		LogUtil.makeLog(TAG, builder.toString());
	}

	private void onPttDown(PttEvent event) {
		// TODO Auto-generated method stub
		clearBuilder();
		builder.append(" onPttDown()");
		/**
		 * 通过手咪控制单呼通话
		 * 1.语音来电，按住手咪PTT键接听，或单击有线耳机媒体按键接听；
		 * 2.语音通话中，松开手咪PTT键，只听不讲；
		 * 3.语音通话中，按下手咪PTT键，恢复讲话；
		 * 
		 * 修改：
		 * 1.只考虑有线手咪，不兼容有线耳机。（正常模式和有线手咪模式，只需判断是否有插入有线设备）
		 * 2.进入通话后，先停止语音发送，当用户再按下后才发送；
		 *   来电时头一次按下接听，在进入通话状态前，不再处理down up事件，进入通话中后根据PTT状态决定是否开启讲话模式；
		 *   去电时按下不处理，在进入通话状态前，不再处理down up事件，进入通话中后根据PTT状态决定是否开启讲话模式；
		 *   接听后按下
		 * 
		 */
		
		if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
			//单呼模式
			builder.append(" UserAgent.UA_STATE_INCOMING_CALL answercall()");
			//1.语音来电，按住手咪PTT键接听，或单击有线耳机媒体按键接听；
//			GroupCallUtil.makeGroupCall(false, true);
//			Receiver.engine(Receiver.mContext).answercall();
			//视频来电接听，单呼来电接听
			if (DemoCallScreen.getInstance() != null) {
				builder.append(" DemoCallScreen.getInstance() != null  DemoCallScreen.answerCall()");
				DemoCallScreen.getInstance().answerCall();
			}else {
				builder.append(" CallUtil.answerCall()");
				CallUtil.answerCall();
			}
		}
		//sometimes call_state stay on UA_STATE_INCALL,should check ua_ptt_mode before checking call_state. modify by mou 2015-01-23
		else if(UserAgent.ua_ptt_mode){
			builder.append(" GroupCallUtil.makeGroupCall(true, true)");
			GroupCallUtil.makeGroupCall(true, true);
		}
		else if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
//			AmrEncodSender.reCheckNeedSendMuteData();
			builder.append(" UserAgent.UA_STATE_INCALL setNeedWriteAudioData(false)");
//			RtpStreamReceiverUtil.setNeedWriteAudioData(false);
			RtpStreamSenderUtil.reCheckNeedSendMuteData(TAG);
		}
		LogUtil.makeLog(TAG, builder.toString());
	}

	private void clearBuilder() {
		// TODO Auto-generated method stub
		if (builder.length()>0) {
			builder.delete(0, builder.length());
		}
	}

	private void interceptEvent(PttEvent event) {
		
	}
	
}
