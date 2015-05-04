package com.zed3.media.mediaButton;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Looper;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.DemoCallScreen;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamReceiverUtil;
import com.zed3.utils.RtpStreamSenderUtil;
/**
 * @author mou
 */
@Deprecated 
public class MediaButtonPttEventProcesser extends Thread{
	 public static class PttEvent {

		
		public static final String PTT_DOWN = "PTT_DOWN";
		public static final String PTT_UP = "PTT_UP";
		
		private long time;
		private String message;
		private int type;
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		private boolean available = true;
		private int sendCount;
		
		public static int TYPE_SEND = 0;
		public static int TYPE_RECEIVE = 1;
		

		public PttEvent() {
			super();
		}

		public PttEvent(long time, String message,int type) {
			// TODO Auto-generated constructor stub
			this.time = time;
			this.message = message;
			this.type = type;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
		public long getTime() {
			// TODO Auto-generated method stub
			return time;
		}
		
		public void setTime(long time) {
			this.time = time;
		}

		public void setAvailable(boolean available) {
			// TODO Auto-generated method stub
			this.available = available;
		}
		public boolean isAvailable() {
			// TODO Auto-generated method stub
			return available;
		}

		public void setSendCount(int count) {
			// TODO Auto-generated method stub
			sendCount = count;
		}
		public int getSendCount() {
			return sendCount;
		}

		public boolean needResend() {
			// TODO Auto-generated method stub
			if (message.equals(PTT_DOWN)
					||message.equals(PTT_UP)
					) {
				return true;
			}
			return false;
		}
		
		public void recycle() {
			// TODO Auto-generated method stub
			time = 0;
			message = "";
			type = -1;
		}
	}


	private boolean isEmpty = true;
	private Lock lock = new ReentrantLock();

	private Condition con4In = lock.newCondition();
	private Condition con4Out = lock.newCondition();

	private Queue<PttEvent> storage = new LinkedList<PttEvent>();
	private Queue<PttEvent> invalidEventstorage = new LinkedList<PttEvent>();
	
	private final String tag = "MediaButtonPttEventProcesser";
	private boolean isRunning;
	private PttEvent mLastEvent;
	public static boolean mIsPttDowned;
	private String logMsg;
	
	
	public PttEvent obtainMessage() {
		// TODO Auto-generated method stub
		PttEvent msg;
		if (invalidEventstorage.size() > 1) {
			msg = invalidEventstorage.poll();
			if (msg == null) {
				msg = new PttEvent(System.currentTimeMillis(), "", 0 );
			}
			msg.recycle();
			msg.setAvailable(true);
		}else{
			msg = new PttEvent(System.currentTimeMillis(), "", 0 );
		}
		return msg;
	}
	public void recycleMessage(PttEvent msg) {
		// TODO Auto-generated method stub
		invalidEventstorage.offer(msg);
	}
	public PttEvent get() {
		PttEvent msg = null;
		try {
			// 获取锁。
			lock.lock();
			isEmpty  = storage.size() == 0;
			/*while*/ if(isEmpty){
				try {
					con4Out.await();
//					isEmpty  = storage.size() == 0;
				} catch (InterruptedException e) {
					System.out.println(Thread.currentThread().getName());
					
					LogUtil.makeLog(tag,"get() InterruptedException set flag to exit while");
					isEmpty = false;
				}
			}

			msg = storage.poll();
//			makeLog("get() storage.size()"+storage.size()+"return "+((msg != null)?msg.getMessage():"null"));
			
//			isEmpty = true;
//			con4In.signal();

		} finally {
			// 释放锁。
			lock.unlock();
		}
		return msg;
	}

	public void put(PttEvent msg) {
		try {
			// 获取锁。
			lock.lock();
//			while (!isEmpty){
//				try {
//					con4In.await();
//				} catch (InterruptedException e) {
//					makeLog("get() InterruptedException set flag to exit while");
//					isEmpty = true;
//				}
//			}
			
			
//			makeLog("put("+msg.getMessage()+") storage.size()"+storage.size() );
//			isEmpty = false;
			switch (msg.getType()) {
			case 0:
				checkSendMsg(msg);
				break;
			case 1:
				checkRecieveMsg(msg);
				break;

			default:
				break;
			}
			
			storage.offer(msg);
			con4Out.signal();
		} finally {
			// 释放锁。
			lock.unlock();
		}
	}

	private void checkRecieveMsg(PttEvent message) {
		// TODO Auto-generated method stub
		String msg = message.getMessage();
		if (msg.equals(PttEvent.PTT_DOWN)) {
			disableMsgs(PttEvent.PTT_DOWN);
			disableMsgs(PttEvent.PTT_UP);
		}
		else if (msg.equals(PttEvent.PTT_UP)) {
			disableMsgs(PttEvent.PTT_DOWN);
			disableMsgs(PttEvent.PTT_UP);
		}
	}

	private void checkSendMsg(PttEvent message) {
		// TODO Auto-generated method stub
		String msg = message.getMessage();
		if (msg.equals(PttEvent.PTT_DOWN)) {
			disableMsgs(PttEvent.PTT_DOWN);
			disableMsgs(PttEvent.PTT_UP);
		}
		else if (msg.equals(PttEvent.PTT_UP)) {
			disableMsgs(PttEvent.PTT_UP);
			disableMsgs(PttEvent.PTT_DOWN);
		}
	}

	private void disableMsgs(String removeMsg) {
		// TODO Auto-generated method stub
		for (PttEvent message : storage) {
			if (message.getMessage().equals(removeMsg)) {
//				storage.remove(message);
				message.setAvailable(false);
//				makeLog("disableMsg("+message.getMessage()+")");
			}
		}

	}

	private MediaButtonPttEventProcesser() {
		// TODO Auto-generated constructor stub
	}
//	private static final MyAudioManager instance = new MyAudioManager();
//	static{
//		instance = new MyAudioManager();
//	}
	private static final class InstanceCreater {
		public static MediaButtonPttEventProcesser sInstance = new MediaButtonPttEventProcesser();
	}
	public static MediaButtonPttEventProcesser getInstance() {
		// TODO Auto-generated method stub
		return InstanceCreater.sInstance;
	}

	public void startProcessing() {
		// TODO Auto-generated method stub
		isRunning = true;
		try {
			start();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LogUtil.makeLog(tag,"startProcessing() Exception "+e.getMessage());
		}finally{
			LogUtil.makeLog(tag,"MediaButtonPttEventProcesser   startProcessing()");
		}
	}
	public void stopProcessing() {
		// TODO Auto-generated method stub
		isRunning = false;
		try {
			interrupt();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LogUtil.makeLog(tag,"stopProcessing() Exception "+e.getMessage());
		}finally{
			LogUtil.makeLog(tag,"MediaButtonPttEventProcesser   stopProcessing()");
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Define handler
		Looper.prepare();
		LogUtil.makeLog(tag,"MediaButtonPttEventProcesser   run begin");
		StringBuilder builder = new StringBuilder();
		while (isRunning) {
			if (builder.length()>0) {
				builder.delete(0, builder.length());
			}
			PttEvent message = get();
			builder.append(" get()");
			builder.append(" storage.size() "+storage.size());
			if (message != null) {
				builder.append(" delay "+(System.currentTimeMillis()- message.getTime()));
				String msg = message.getMessage();
				if (!message.isAvailable()) {
					builder.append("  message.isAvailable() is false   continue");
					LogUtil.makeLog(tag,builder.toString());
					continue;
				}
				
				boolean pressed = msg.equals(PttEvent.PTT_DOWN);
				builder.append("  msg.equals(PttEvent.PTT_DOWN) is "+pressed);
				mIsPttDowned = pressed;
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
					if (pressed) {
						builder.append("  downPTT("+pressed+") UserAgent.UA_STATE_INCOMING_CALL answercall()");
						//1.语音来电，按住手咪PTT键接听，或单击有线耳机媒体按键接听；
						GroupCallUtil.makeGroupCall(false, true);
						Receiver.engine(Receiver.mContext).answercall();
						//视频来电接听，单呼来电接听
						if (DemoCallScreen.getInstance() != null) {
							logMsg += " DemoCallScreen.getInstance() != null  DemoCallScreen.answerCall()";
							DemoCallScreen.getInstance().answerCall();
						}else {
							logMsg += " CallUtil.answerCall()";
							CallUtil.answerCall();
						}
					}
				}
				//sometimes call_state stay on UA_STATE_INCALL,should check ua_ptt_mode before checking call_state. modify by mou 2015-01-23
				else if(UserAgent.ua_ptt_mode){
					//对讲模式
					if (msg.equals(PttEvent.PTT_DOWN)) {
						builder.append(" MediaButtonPttEventProcesser   GroupCallUtil.makeGroupCall(true, true)");
						GroupCallUtil.makeGroupCall(true, true);
					}else if (msg.equals(PttEvent.PTT_UP)) {
						builder.append(" MediaButtonPttEventProcesser   GroupCallUtil.makeGroupCall(false, true);");
						GroupCallUtil.makeGroupCall(false, true);
					}else {
						builder.append(" MediaButtonPttEventProcesser   unkown msg "+msg);
					}
				}
				else if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
//					AmrEncodSender.reCheckNeedSendMuteData();
					RtpStreamReceiverUtil.setNeedWriteAudioData(!mIsPttDowned);
					RtpStreamSenderUtil.reCheckNeedSendMuteData("MediaButtonPttEventProcesser");
				}
				mLastEvent = message;
				recycleMessage(message);
			}else {
				builder.append(" return null ");
			}
			LogUtil.makeLog(tag,builder.toString());
		}
		LogUtil.makeLog(tag,"MediaButtonPttEventProcesser   run end");		
		Looper.loop();
	}
}
