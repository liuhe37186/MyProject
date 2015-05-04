package com.zed3.utils;

import java.util.ArrayList;
import java.util.List;

import android.media.AudioManager;

import com.zed3.audio.AudioUtil;
import com.zed3.flow.FlowStatistics;
import com.zed3.media.RtpStreamReceiver_group;
import com.zed3.net.RtpPacket;

public class RtpStreamReceiverUtil {

	private static final String tag = "RtpStreamReceiverUtil";
	
	static boolean mNeedWriteAudioData = true;
	final static long  SET_NEED_WRITE_AUDIO_DATA_TRUE_DELAY_TIME = 1000;
	public synchronized static void setNeedWriteAudioData(boolean need) {
		mNeedWriteAudioData = need;
	}
	public synchronized static boolean needWriteAudioData() {
		return mNeedWriteAudioData;
	}
	final static int STATE_INSTANCE_DESTROYED = 0;
	final static int STATE_INSTANCE_CREATED = 1;
	final static int STATE_RECEIVING_STARTED = 2;
	final static int STATE_RECEIVING_STOPED = 3;
	static List< RtpStreamReceiverType> types = new ArrayList<RtpStreamReceiverType>();
	static{
		types.add(RtpStreamReceiverType.GROUP_CALL_RECEIVER);
		types.add(RtpStreamReceiverType.SINGLE_CALL_RECEIVER);
	}
	public enum RtpStreamReceiverType {
		GROUP_CALL_RECEIVER(STATE_INSTANCE_DESTROYED),
		SINGLE_CALL_RECEIVER(STATE_INSTANCE_DESTROYED);
		private int mState = 0;
		private int mLostTotal = 0;
		private long mLateTotal = 0;
		private int mReceiveCount = 0;
		private int mReceiveTotalLen = 0;
		private long mLastReceiveTime;
		private long mLastWriteReceiveLogTime;
		private int mLastSequenceNumber;
		public int getLastSequenceNumber() {
			return mLastSequenceNumber;
		}
		public void setLastSequenceNumber(int mLastSequenceNumber) {
			this.mLastSequenceNumber = mLastSequenceNumber;
		}
		private RtpStreamReceiverType(int state){
			mState = state;
		}
		public int getState(){
			return mState;
		}
		public int setState(int state){
			return mState;
		}
		public int getLostTotal() {
			return mLostTotal;
		}
		public void setLostTotal(int mLostTotal) {
			this.mLostTotal = mLostTotal;
		}
		public long getLateTotal() {
			return mLateTotal;
		}
		public void setLateTotal(int mLateTotal) {
			this.mLateTotal = mLostTotal;
		}
		public int getReceiveCount() {
			return mReceiveCount;
		}
		public void setReceiveCount(int mReceiveCount) {
			this.mReceiveCount = mReceiveCount;
		}
		public int getReceiveTotalLen() {
			return mReceiveTotalLen;
		}
		public void setReceiveTotalLen(int mReceiveTotalLen) {
			this.mReceiveTotalLen = mReceiveTotalLen;
		}
		public long getLastReceiveTime() {
			return mLastReceiveTime;
		}
		public void setLastReceiveTime(long mLastReceiveTime) {
			this.mLastReceiveTime = mLastReceiveTime;
		}
		public long getLastWriteReceiveLogTime() {
			return mLastWriteReceiveLogTime;
		}
		public void setLastWriteReceiveLogTime(long mLastWriteReceiveLogTime) {
			this.mLastWriteReceiveLogTime = mLastWriteReceiveLogTime;
		}
	}
	//add by oumogang 2014-05-14
	
	static final StringBuilder mStringBuilder = new StringBuilder();
	public synchronized static void onStartReceiving(RtpStreamReceiverType type) {
		// TODO Auto-generated method stub
		StringBuilder builder = getStringBuilder();
		builder.append("onStartReceiving("+type+") ");
		type.setLostTotal(0);
		type.setLateTotal(0);
		type.setReceiveCount(0);
		type.setReceiveTotalLen(0);
		builder.append(" mLostTotal "+type.getLostTotal());
		builder.append(" mLateTotal "+type.getLateTotal());
		builder.append(" mReceiveCount "+type.getReceiveCount());
		builder.append(" ReceiveTotalLen "+type.getReceiveTotalLen());
		type.setState(STATE_RECEIVING_STARTED);
		
		LogUtil.makeLog(tag, builder.toString());
	}
	private synchronized static StringBuilder getStringBuilder() {
		// TODO Auto-generated method stub
		if (mStringBuilder.length()>0) {
			mStringBuilder.delete(0, mStringBuilder.length());
		}
		return mStringBuilder;
	}
	public synchronized static void onStopReceiving(RtpStreamReceiverType type) {
		// TODO Auto-generated method stub
		StringBuilder builder = getStringBuilder();
		builder.append("onStopReceiving("+type+") ");
		builder.append(" mLostTotal "+type.getLostTotal());
		builder.append(" mLateTotal "+type.getLateTotal());
		builder.append(" mReceiveCount "+type.getReceiveCount());
		builder.append(" ReceiveTotalLen "+type.getReceiveTotalLen());
		boolean needSetNormal = true;
		for (RtpStreamReceiverType receiverType : types) {
			if (receiverType != type && receiverType.getState() != STATE_INSTANCE_DESTROYED) {
				needSetNormal = false;
			}
		}
		if (needSetNormal) {
			builder.append(" needSetNormal "+needSetNormal);
			AudioUtil.getInstance().setMode(AudioManager.MODE_NORMAL);
		}
		type.setState(STATE_RECEIVING_STOPED);
		type.setState(STATE_INSTANCE_DESTROYED);
		
		LogUtil.makeLog(tag, builder.toString());
	}
	public synchronized static void onReceive(RtpStreamReceiverType type, RtpPacket rtp_packet) {
		// TODO Auto-generated method stub
		
		StringBuilder builder = getStringBuilder();
		builder.append("onReceive("+type+",...)");
		type.setReceiveCount(type.getReceiveCount()+1);
		//统计流量
//		if(rtp_packet.getLength()+flow>60){
//			flow =rtp_packet.getLength() +42;
//		}else{
//			flow = 60 ;
//		}
		int flow = rtp_packet.getLength()+42;
		if (flow < 60)
			flow = 60;
		type.setReceiveTotalLen(type.getReceiveTotalLen()+flow);
		FlowStatistics.Voice_Receive_Data += flow;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - type.getLastWriteReceiveLogTime() > 5000) {
			type.setLastWriteReceiveLogTime(currentTime);
			builder.append(" mReceiveCount "+type.getReceiveCount()+" mReceiveLen "+flow+" mReceiveTotalLen "+type.getReceiveTotalLen());
			LogUtil.makeLog(tag, builder.toString());
		}
		
		long timeMillis = java.lang.System.currentTimeMillis();
		int sequenceNumber = rtp_packet.getSequenceNumber();
		if (type.getLastReceiveTime() != 0) {
			int time = (int) (timeMillis - type.getLastReceiveTime());
			if (time > 1000) {
				builder.append(" receive sycle "+time+" >1000ms");
				LogUtil.makeLog(tag, builder.toString());
			}
			int lost = sequenceNumber - type.getLastSequenceNumber() -1;
			if (lost>1) {
				type.setLostTotal(type.getLostTotal()+lost);
				builder.append(" lost rtpPackets :"+lost+" mLostTotal "+type.getLostTotal());
				LogUtil.makeLog(tag, builder.toString());
			}
		}
		type.setLastReceiveTime(timeMillis);
		type.setLastSequenceNumber(sequenceNumber);
		
//		LogUtil.makeLog(tag, builder.toString());
	}
	public static void onAudioModeChanged(int mode) {
		// TODO Auto-generated method stub
		switch (mode) {
		case AudioManager.MODE_NORMAL:
			RtpStreamReceiver_group.speakermode = AudioManager.MODE_NORMAL;
//			RtpStreamReceiver_signal.speakermode = AudioManager.MODE_NORMAL;
			break;
		case AudioManager.MODE_IN_COMMUNICATION:
			RtpStreamReceiver_group.speakermode = AudioManager.MODE_IN_CALL;
//			RtpStreamReceiver_signal.speakermode = AudioManager.MODE_IN_CALL;
			break;
		case AudioManager.MODE_IN_CALL:
			RtpStreamReceiver_group.speakermode = AudioManager.MODE_IN_CALL;
//			RtpStreamReceiver_signal.speakermode = AudioManager.MODE_IN_CALL;
			break;
		default:
			break;
		}
	}
}
