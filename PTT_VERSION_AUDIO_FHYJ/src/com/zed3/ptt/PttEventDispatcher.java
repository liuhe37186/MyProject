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
		 * ͨ��������Ƶ���ͨ��
		 * 1.�������磬��ס����PTT���������򵥻����߶���ý�尴��������
		 * 2.����ͨ���У��ɿ�����PTT����ֻ��������
		 * 3.����ͨ���У���������PTT�����ָ�������
		 * 
		 * �޸ģ�
		 * 1.ֻ�����������䣬���������߶�����������ģʽ����������ģʽ��ֻ���ж��Ƿ��в��������豸��
		 * 2.����ͨ������ֹͣ�������ͣ����û��ٰ��º�ŷ��ͣ�
		 *   ����ʱͷһ�ΰ��½������ڽ���ͨ��״̬ǰ�����ٴ���down up�¼�������ͨ���к����PTT״̬�����Ƿ�������ģʽ��
		 *   ȥ��ʱ���²������ڽ���ͨ��״̬ǰ�����ٴ���down up�¼�������ͨ���к����PTT״̬�����Ƿ�������ģʽ��
		 *   ��������
		 * 
		 */
		
		if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
			//����ģʽ
			builder.append(" UA_STATE_INCOMING_CALL GroupCallUtil.makeGroupCall(false, true);");
			GroupCallUtil.makeGroupCall(false, true);
		}
		//sometimes call_state stay on UA_STATE_INCALL,should check ua_ptt_mode before checking call_state. modify by mou 2015-01-23
		else if(UserAgent.ua_ptt_mode){
			//�Խ�ģʽ
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
		 * ͨ��������Ƶ���ͨ��
		 * 1.�������磬��ס����PTT���������򵥻����߶���ý�尴��������
		 * 2.����ͨ���У��ɿ�����PTT����ֻ��������
		 * 3.����ͨ���У���������PTT�����ָ�������
		 * 
		 * �޸ģ�
		 * 1.ֻ�����������䣬���������߶�����������ģʽ����������ģʽ��ֻ���ж��Ƿ��в��������豸��
		 * 2.����ͨ������ֹͣ�������ͣ����û��ٰ��º�ŷ��ͣ�
		 *   ����ʱͷһ�ΰ��½������ڽ���ͨ��״̬ǰ�����ٴ���down up�¼�������ͨ���к����PTT״̬�����Ƿ�������ģʽ��
		 *   ȥ��ʱ���²������ڽ���ͨ��״̬ǰ�����ٴ���down up�¼�������ͨ���к����PTT״̬�����Ƿ�������ģʽ��
		 *   ��������
		 * 
		 */
		
		if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
			//����ģʽ
			builder.append(" UserAgent.UA_STATE_INCOMING_CALL answercall()");
			//1.�������磬��ס����PTT���������򵥻����߶���ý�尴��������
//			GroupCallUtil.makeGroupCall(false, true);
//			Receiver.engine(Receiver.mContext).answercall();
			//��Ƶ��������������������
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
