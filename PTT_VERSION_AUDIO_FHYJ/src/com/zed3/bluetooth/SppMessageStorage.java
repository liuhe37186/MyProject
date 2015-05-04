package com.zed3.bluetooth;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;


public class SppMessageStorage {
	 public static class SppMessage {

		/**
		 * SEND MSG
		 */
		public static final String PTT_START = "R_START";
		public static final String PTT_STOP = "R_STOP";
		public static final String PTT_SUCCESS = "PTT_SUCC";
		public static final String PTT_WAITING = "PTT_WAIT";
		
		public static final String PTT_PA_ON = "PA_ON";
		public static final String PTT_PA_OFF = "PA_OFF";
		
		/**
		 * RECEIVE MSG
		 */
		public static final String RESPOND_PTT_START = "R_START_OK";
		public static final String RESPOND_PTT_STOP = "R_STOP_OK";
		public static final String RESPOND_PTT_SUCCESS = "PTT_SUCC_OK";
		public static final String RESPOND_PTT_WAITING = "PTT_WAIT_OK";
		
		public static final String RESPOND_PTT_PA_ON = "PA_ON_OK";
		public static final String RESPOND_PTT_PA_OFF = "PA_OFF_OK";
		
		
		public static final String REQUEST_ADDRESS = "get addr";
		public static final String REQUEST_DEVICE_NAME = "request device name";
		
		public static final String PTT_DOWN = "PTT_DOWN";
		public static final String PTT_UP = "PTT_UP";
		public static final String VOL_LONG_DOWN = "VOL_LONG_DOWN";
		public static final String VOL_LONG_UP = "VOL_LONG_UP";
		public static final String VOL_SHORT_DOWN = "VOL_SHORT_DOWN";
		public static final String VOL_SHORT_UP = "VOL_SHORT_UP";
		
		public static final String FUNCTION = "FUNCTION";
		
		public static final String RESPOND_ADDRESS_HEAD = "addr:";
		public static final String RESPOND_DEVICE_NAME_HEAD = "device name:";
		protected static final int MAX_SEND_COUNT = 3;
		public static final long RESEND_SYCLE = 200;
	
		public long time;
		public String message;
		private int type;
		public int getType() {
			return type;
		}
		private boolean available = true;
		private int sendCount;
		
		public static int TYPE_SEND = 0;
		public static int TYPE_RECEIVE = 1;
		

		public SppMessage(long time, String message,int type) {
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
			if (message.equals(PTT_PA_ON)
					||message.equals(PTT_PA_ON)
					||message.equals(PTT_START)
					||message.equals(PTT_STOP)
					||message.equals(PTT_SUCCESS)
					||message.equals(PTT_WAITING)
					) {
				return true;
			}
			return false;
		}
	}

	private boolean isEmpty = true;
	private Lock lock = new ReentrantLock();

	private Condition con4In = lock.newCondition();
	private Condition con4Out = lock.newCondition();

	private Queue<SppMessage> storage = new LinkedList<SppMessage>();
	private final String tag = "SppMessageStorage";

	public SppMessage get() {
		SppMessage msg = null;
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
					
					Log.i(tag, "get() InterruptedException set flag to exit while");
					isEmpty = false;
				}
			}

			msg = storage.poll();
			Log.i(tag, "get() storage.size()"+storage.size()+"return "+((msg != null)?msg.getMessage():"null"));
			
//			isEmpty = true;
//			con4In.signal();

		} finally {
			// 释放锁。
			lock.unlock();
		}
		return msg;
	}

	public void put(SppMessage msg) {
		try {
			// 获取锁。
			lock.lock();
//			while (!isEmpty){
//				try {
//					con4In.await();
//				} catch (InterruptedException e) {
//					Log.i(tag, "get() InterruptedException set flag to exit while");
//					isEmpty = true;
//				}
//			}
			
			
			Log.i(tag, "put("+msg.getMessage()+") storage.size()"+storage.size() );
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

	private void checkRecieveMsg(SppMessage message) {
		// TODO Auto-generated method stub
		String msg = message.getMessage();
		if (msg.equals(SppMessage.PTT_DOWN)) {
			disableMsgs(SppMessage.PTT_DOWN);
			disableMsgs(SppMessage.PTT_UP);
		}
		else if (msg.equals(SppMessage.PTT_UP)) {
			disableMsgs(SppMessage.PTT_DOWN);
			disableMsgs(SppMessage.PTT_UP);
		}
		else if (msg.equals(SppMessage.VOL_SHORT_DOWN)) {
			disableMsgs(SppMessage.VOL_SHORT_DOWN);
			disableMsgs(SppMessage.VOL_SHORT_UP);
		}
		else if (msg.equals(SppMessage.VOL_SHORT_UP)) {
			disableMsgs(SppMessage.VOL_SHORT_DOWN);
			disableMsgs(SppMessage.VOL_SHORT_UP);
		}
		else if (msg.equals(SppMessage.VOL_LONG_DOWN)) {
			disableMsgs(SppMessage.VOL_LONG_DOWN);
			disableMsgs(SppMessage.VOL_LONG_UP);
		}
		else if (msg.equals(SppMessage.VOL_LONG_UP)) {
			disableMsgs(SppMessage.VOL_LONG_DOWN);
			disableMsgs(SppMessage.VOL_LONG_UP);
		}
		else if (msg.equals(SppMessage.FUNCTION)) {
			disableMsgs(SppMessage.FUNCTION);
		}
	}

	private void checkSendMsg(SppMessage message) {
		// TODO Auto-generated method stub
		String msg = message.getMessage();
		if (msg.equals(SppMessage.PTT_START)) {
			disableMsgs(SppMessage.PTT_START);
			disableMsgs(SppMessage.PTT_STOP);
			disableMsgs(SppMessage.PTT_PA_OFF);
		}
		else if (msg.equals(SppMessage.PTT_STOP)) {
			disableMsgs(SppMessage.PTT_STOP);
			disableMsgs(SppMessage.PTT_START);
			disableMsgs(SppMessage.PTT_SUCCESS);
		}
		else if (msg.equals(SppMessage.PTT_SUCCESS)) {
			disableMsgs(SppMessage.PTT_SUCCESS);
			disableMsgs(SppMessage.PTT_START);
			disableMsgs(SppMessage.PTT_STOP);
		}
		else if (msg.equals(SppMessage.PTT_WAITING)) {
			disableMsgs(SppMessage.PTT_WAITING);
			disableMsgs(SppMessage.PTT_START);
			disableMsgs(SppMessage.PTT_STOP);
			disableMsgs(SppMessage.PTT_SUCCESS);
		}
		else if (msg.equals(SppMessage.PTT_PA_ON)) {
			disableMsgs(SppMessage.PTT_PA_ON);
			disableMsgs(SppMessage.PTT_PA_OFF);
		}
		else if (msg.equals(SppMessage.PTT_PA_OFF)) {
			disableMsgs(SppMessage.PTT_PA_OFF);
			disableMsgs(SppMessage.PTT_PA_ON);
		}
	}

	private void disableMsgs(String removeMsg) {
		// TODO Auto-generated method stub
		for (SppMessage message : storage) {
			if (message.getMessage().equals(removeMsg)) {
//				storage.remove(message);
				message.setAvailable(false);
				Log.i(tag, "disableMsg("+message.getMessage()+")");
			}
		}

	}

}
