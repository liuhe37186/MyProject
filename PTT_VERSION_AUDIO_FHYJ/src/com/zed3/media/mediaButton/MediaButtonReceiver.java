package com.zed3.media.mediaButton;

import java.text.SimpleDateFormat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import com.zed3.ptt.PttEventDispatcher;
import com.zed3.ptt.PttEventDispatcher.PttEvent;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamSenderUtil;

public class MediaButtonReceiver extends BroadcastReceiver {

	
	protected static final int PressPttDown = 0;  
	protected static final int PressPttUp = 1;
	private static String TAG = "MediaButtonReceiver ";
	private Context mContext;
	public static boolean mIsPttDowned;
	public static boolean mIsHandFreeDevice;
	private static String deviceInfoStr = null;
//	public static boolean mPressed;
	protected static int downCount;
	protected static int upCount;
	private static SimpleDateFormat mFormatter;
	
	public static long mDownEventTime;
	public static long mLastDownEventTime;
	public static long mDownTime;
	public static long mLastDownTime;
//	private static String mMessageStr = null;
	private static StringBuilder builder = new StringBuilder();
	private static int mDownCount;
	private static int mLostCount;
	protected static int mDownCountTotal;
	public static int mDownEventTimeDiff;
	private static int mDownTimeDiff;
	private static final int mMinDownTimeDiff1 = /*760*//*800*//*850*/1000;
//	private static int RESETTOTAL = -1; 
	private static int mMinDownTimeDiff = mMinDownTimeDiff1;
	private static ComponentName mComponentName;
	private static SimpleDateFormat formatter;
	private static MediaButtonReceiver mInstance;
	private static AudioManager mAudioManager;

	static{
//		mFormatter = new SimpleDateFormat(
//				" yyyy-MM-dd HH:mm:ss ");
		mFormatter = new SimpleDateFormat(
				" yyyy-MM-dd hh:mm:ss SSS ");
		mComponentName = new ComponentName(SipUAApp.mContext.getPackageName(),MediaButtonReceiver.class.getName());  
		mAudioManager =(AudioManager)SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE);   
		mInstance = new MediaButtonReceiver();
		if (Build.MODEL.contains("HUAWEI MT7")) {
			mMinDownTimeDiff = 650;
		}
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		mContext = context/*.getApplicationContext()*/;
//		if (Receiver.call_state != UserAgent.UA_STATE_IDLE) {
//			mMessageStr = "\r\n ----------Receiver.call_state != UserAgent.UA_STATE_IDLE unprocess--------- ");
//			writeLog2File(mMessageStr);
//			return;
//		}
		if (builder.length()>0) {
			builder.delete(0, builder.length());
		}
		if (SipUAApp.mContext == null) {
			builder.append(" SipUAApp.mContext == null ignore");
			LogUtil.makeLog(TAG, builder.toString());
			return;
		}
		if (Receiver.mSipdroidEngine == null) {
			builder.append(" Receiver.mSipdroidEngine == null ignore");
			LogUtil.makeLog(TAG, builder.toString());
			return;
		}
		//如果是耳机插入产生的down事件，非目的事件，不需要处理。
		Long timeLong = System.currentTimeMillis() - SipUAApp.lastHeadsetConnectTime;
		
		// 获得Action
		String intentAction = intent.getAction();
		// 获得KeyEvent对象
		KeyEvent keyEvent = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			// 获得按键字节码
			int keyCode = keyEvent.getKeyCode();
//			Log.i(TAG, "keyCode ---->" + keyCode);
			// 按下 / 松开 按钮
			int keyAction = keyEvent.getAction();
//			mMessageStr= "手咪测试\r\n后台监听:\r\n ");
			String keyActionStr = keyAction == 0?"ACTION_DOWN":"ACTION_UP ";
			builder.append(" "+keyActionStr);
//			builder.append(" "+keyCode;
			// 获得事件的时间
			long downtime = keyEvent.getEventTime();

			// 获取按键码 keyCode
			// 这些都是可能的按键码 ， 打印出来用户按下的键
			if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
				builder.append(" KEYCODE_MEDIA_NEXT "+keyCode);
			}
			// 说明：当我们按下MEDIA_BUTTON中间按钮时，实际出发的是 KEYCODE_HEADSETHOOK 而不是
			// KEYCODE_MEDIA_PLAY_PAUSE
			if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
				builder.append("KEYCODE_MEDIA_PLAY_PAUSE "+keyCode);
			}
			if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
				builder.append("KEYCODE_HEADSETHOOK "+keyCode);
			}
			if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
				builder.append("KEYCODE_MEDIA_PREVIOUS "+keyCode);
			}
			if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) {
				builder.append("KEYCODE_MEDIA_STOP"+keyCode);
			}
			
			if (keyAction == 0) {
				pressDown(keyEvent);
			}else {
				pressUp(keyEvent);
			}
		}
		abortBroadcast();
	}

	/*
	       乱序原因：松开手咪时，第一或第二个事件丢失了。
	       如果丢了一个事件，那么下次按下时，mDownCount%3==0，就会不处理。
	       
	       如何判断松开手咪媒体按键事件丢失？
	       例如： mMinDownTimeDiff = 1000 mDownCount%3==0   不处理   downEventTimeDiffs：[605,4401,59425,1944] 
	      mMinDownTimeDiff = 1000 mDownCount%3==0   不处理   downEventTimeDiffs：[601,48506,603,540161]
	  
	  1.事件丢失判断确认；  
	    mDownCount%3==0
				&& downEventTimeDiffs[3]>mMinDownTimeDiff
      2.周期加强判断确认；
       downEventTimeDiffs[0]<mMinDownTimeDiff && downEventTimeDiffs[0]<downEventTimeDiffs[1]
				downEventTimeDiffs[3]<mMinDownTimeDiff && downEventTimeDiffs[3]<downEventTimeDiffs[2]
	       例如： mDownCount%3==0   不处理   downEventTimeDiffs：[605,621,720,605]
	      mDownCount%3==0   不处理   downEventTimeDiffs：[604,621,704,605]
	      mDownCount%3==0   不处理   downEventTimeDiffs：[606,625,700,604]
	 * 
	 */
	private boolean checkLost() {
		// TODO Auto-generated method stub
		//修改插入手咪后第二次按下手咪与上一次松开手咪的时间间隔小于1秒，申请话权后立马释放话权的问题； add by mou 2015-01-05
		if (downEventTimeDiffs[0] == 0) {
			return false;
		}
		return mDownCount%3==0 && downEventTimeDiffs[3]>mMinDownTimeDiff;
	}
	//mDownCount%3==2     松开PTT   downEventTimeDiffs：[910,2211,1254,941] mDownTimeDiff<mMinDownTimeDiff  checkCycle() is true 松开PTT
	//按下手咪不到一秒后松开手咪，且上一次松开手咪丢失一个down事件，导致松开手咪后又申请了话权。
	private boolean checkCycle() {
		// TODO Auto-generated method stub
		//count/total/lost:4/4/0 ReceiveTimeDiff/EventTimeDiff:573/571 mMinDownTimeDiff = 1000 mDownCount%3==1     按下PTT   downEventTimeDiffs：[0,699,683,571] mDownTimeDiff<mMinDownTimeDiff  checkCycle() is true 松开PTT
		//修改插入手咪后第二次按下手咪与上一次松开手咪的时间间隔小于1秒，申请话权后立马释放话权的问题； add by mou 2015-01-05
		if (downEventTimeDiffs[0] == 0) {
			return false;
		}
		if (downEventTimeDiffs[0]<mMinDownTimeDiff
				&&downEventTimeDiffs[1]>mMinDownTimeDiff
				&&downEventTimeDiffs[2]>mMinDownTimeDiff
				&&downEventTimeDiffs[3]<mMinDownTimeDiff) {
			return true;
		}
		//手咪按放周期加强判断确认
		if (downEventTimeDiffs[0]<mMinDownTimeDiff 
				&& downEventTimeDiffs[1]>downEventTimeDiffs[0]
				&& downEventTimeDiffs[2]>downEventTimeDiffs[3]
				&& downEventTimeDiffs[3]<mMinDownTimeDiff) {
			return true;
		}
		return false;
	}
	static int[] downEventTimeDiffs = new int[4];
	static int[] minDownTimeDiffs = new int[4];
	static int[] downEventTimeDiffs4calc = new int[30];
	private static boolean isStarted;
//	private static MediaButtonPttEventProcesser mPttEventProcesser = MediaButtonPttEventProcesser.getInstance();
	private void reSetDownTimeDiff(int downEventTimeDiff) {
		// TODO Auto-generated method stub
		for (int i = 0; i < downEventTimeDiffs.length-1; i++) {
			downEventTimeDiffs[i] = downEventTimeDiffs[i+1];
		}
		downEventTimeDiffs[3] = downEventTimeDiff;
	}

	private void pressDown(KeyEvent keyEvent) {//down
		// 获得事件的时间
		int keyCode = keyEvent.getKeyCode();
		mDownTime = System.currentTimeMillis();
		mDownEventTime = keyEvent.getEventTime();
		if (mLastDownEventTime != 0) {
			mDownEventTimeDiff = (int)(mDownEventTime-mLastDownEventTime);
		}
		if (mLastDownTime != 0) {
			mDownTimeDiff = (int)(mDownTime-mLastDownTime);
		}
		android.os.SystemClock.uptimeMillis();
//		mDownEventTime = keyEvent.getDownTime();
		mDownCount++;
		mDownCountTotal++;
		builder.append(" rece delay = "+(android.os.SystemClock.uptimeMillis()-mDownEventTime));
		builder.append(" down count/total/lost:"+mDownCount+"/"+mDownCountTotal+"/"+mLostCount);
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_HEADSETHOOK:
//			// add your code here
//			builder.append(" onKeyDown KeyEvent：KEYCODE_HEADSETHOOK  KeyCode：" + keyCode);
//			break;
//		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//			builder.append(" onKeyDown KeyEvent：KEYCODE_MEDIA_PLAY_PAUSE  KeyCode：" + keyCode);
//
//			break;
//		case KeyEvent.KEYCODE_MEDIA_STOP:
//			builder.append(" onKeyDown KeyEvent：KEYCODE_MEDIA_STOP  KeyCode：" + keyCode);
//
//			break;
//		}
		
//		Date curDate = new Date(mDownTime);// 获取当前时
//		String strTime = mFormatter.format(curDate);
//		builder.append(" "+strTime);
//		builder.append(" thisReceiveTime - lastReceiveTime = "+mDownTimeDiff);
//		builder.append(" thisEventTime - lastEventTime = "+mDownEventTimeDiff);
		builder.append(" ReceiveTimeDiff/EventTimeDiff:"+mDownTimeDiff+"/"+mDownEventTimeDiff);
		builder.append(" mMinDownTimeDiff = "+mMinDownTimeDiff);
		switch (mDownCount%3) {
		case 1:
			downPTT(true);
			builder.append(" mDownCount%3==1     按下PTT  ");
			break;
		case 2:
			builder.append(" mDownCount%3==2     松开PTT  ");
			downPTT(false);
			break;
		case 0:
			builder.append(" mDownCount%3==0   不处理  ");
			
			break;

		default:
			break;
		}
		//calc the best value for MinDownTimeDiff。add by mou 2015-01-05
		calcMinDownTimeDiff(mDownEventTimeDiff);
		reSetDownTimeDiff(mDownEventTimeDiff);
		builder.append(" downEventTimeDiffs：");
		for (int i = 0; i < downEventTimeDiffs.length; i++) {
			if (i == 0) {
				builder.append("["+downEventTimeDiffs[i]);
			}else if (i == downEventTimeDiffs.length - 1) {
				builder.append(","+downEventTimeDiffs[i]+"]");
			}else {
				builder.append(","+downEventTimeDiffs[i]);
			}
		}
		if (mDownTimeDiff<mMinDownTimeDiff && checkCycle()) {
			//确认是有线手咪，而不是有线耳机
			mIsHandFreeDevice = true;
			mDownCount = 0;
			builder.append(" mDownTimeDiff<mMinDownTimeDiff  checkCycle() is true 松开PTT \r\nreset mDownCount = 0 ");
			downPTT(false);
		}else if (mDownTimeDiff>mMinDownTimeDiff && checkLost()) {
			mDownCount = 1;
			mLostCount ++;
			builder.append(" mDownTimeDiff>mMinDownTimeDiff  checkLost() is true 按下PTT \r\nreset mDownCount = 1 ");
			downPTT(true);
		}
		writeLog2File(builder.toString());
		mLastDownTime = mDownTime;
		mLastDownEventTime = mDownEventTime;
	}
	
	private void calcMinDownTimeDiff(int downEventTimeDiff) {
		// TODO Auto-generated method stub
		//已填满，忽略。
		if (downEventTimeDiffs4calc[downEventTimeDiffs4calc.length-1] != 0) {
			return;
		}
		StringBuilder builder = new StringBuilder("calcMinDownTimeDiff("+downEventTimeDiff+")");
		//填值
		for (int i = 0; i < downEventTimeDiffs4calc.length; i++) {
			if (downEventTimeDiffs4calc[i]==0) {
				downEventTimeDiffs4calc[i] = downEventTimeDiff;
				break;
			}
		}
		builder.append(" downEventTimeDiffs4calc:");
		for (int i = 0; i < downEventTimeDiffs4calc.length; i++) {
			if (i == 0) {
				builder.append("{"+downEventTimeDiffs4calc[i]);
			}else if (i == downEventTimeDiffs4calc.length-1) {
				builder.append("{"+downEventTimeDiffs4calc[i]);
			}else {
				builder.append(","+downEventTimeDiffs4calc[i]);
			}
		}
		//未填满，先不计算。
		if (downEventTimeDiffs4calc[downEventTimeDiffs4calc.length-1] == 0) {
			LogUtil.makeLog(TAG,builder.toString() );
			return;
		}
		//已填满，计算。
		int[] minEventTimeDiffs = new int[10];
		getMinValues(downEventTimeDiffs4calc,minEventTimeDiffs );
		//去掉三个最小值，去掉三个最大值，取平均值。
		int total = 0;
		int cunt = 0;
		//[580, 600, 600, 601, 604, 605, 605, 605, 605, 606]
		for (int i = 2; i < minEventTimeDiffs.length-2; i++) {
			total += minEventTimeDiffs[i];
			cunt++;
		}
		builder.append(" mDownEventTimeDiff = "+mDownEventTimeDiff);
		mMinDownTimeDiff = total/cunt+50;
		LogUtil.makeLog(TAG,builder.toString() );
	}

	private void getMinValues(int[] srcIn,
			int[] dst) {
		// TODO Auto-generated method stub
		if (srcIn .length <= dst.length) {
			return;
		}
		//copy the array
		int[] src = new int[srcIn.length];
		System.arraycopy(srcIn, 0, src, 0, srcIn.length);
		for (int i = 0; i < dst.length; i++) {
			//find the min value
			int temp =  Integer.MAX_VALUE;
			for (int j = 0; j < src.length; j++) {
				if (src[j] < temp) {
					temp = src[j];
				}
			}
			//repalace the min value by Integer.MAX_VALUE
			for (int k = 0; k < src.length; k++) {
				if (src[k] == temp) {
					src[k]= Integer.MAX_VALUE;
					break;
				}
			}
			dst[i] = temp;
		}
	}
	private void getMaxValues(int[] srcIn,
			int[] dst) {
		// TODO Auto-generated method stub
		if (srcIn .length <= dst.length) {
			return;
		}
		//copy the array
		int[] src = new int[srcIn.length];
		System.arraycopy(srcIn, 0, src, 0, srcIn.length);
		for (int i = 0; i < dst.length; i++) {
			//find the max value
			int temp =  Integer.MIN_VALUE;
			for (int j = 0; j < src.length; j++) {
				if (src[j] > temp) {
					temp = src[j];
				}
			}
			//repalace the max value by Integer.MIN_VALUE
			for (int k = 0; k < src.length; k++) {
				if (src[k] == temp) {
					src[k]= Integer.MIN_VALUE;
					break;
				}
			}
			dst[i] = temp;
		}
	}

	private void pressUp(KeyEvent keyEvent) {
		// TODO Auto-generated method stub
//		Log.i(TAG, mMessageStr);
//		writeLog2File(mMessageStr);
	}




	public static void downPTT(boolean pressed) {
		// TODO Auto-generated method stub
		synchronized (TAG) {
//			GroupCallActivity.pressPTT(pressed);
//			PTTHandler.pressPTT(pressed);
			if (mIsPttDowned == pressed) {
				return;
			}
			
			mIsPttDowned = pressed;
//			String message = pressed? PttEvent.PTT_DOWN : PttEvent.PTT_UP; 
			

//			if (mPttEventProcesser != null) {
//				PttEvent event = mPttEventProcesser.obtainMessage();
//				event.setTime(System.currentTimeMillis());
//				event.setMessage(message);
//				event.setType(0);
//				mPttEventProcesser.put(event);
//				mPressed = pressed;
//			}else {
//				writeLog2File("downPTT("+pressed+")  mPttEventProcesser == null error ");
//			}
			PttEventDispatcher.getInstance().dispatch(pressed?PttEvent.PTT_DOWN:PttEvent.PTT_UP);
		}
	}

	public synchronized static void registerMediaButtonEventReceiver(Context mContext){
		//构造一个ComponentName，指向MediaoButtonReceiver类  
		//下面为了叙述方便，我直接使用ComponentName类来替代MediaoButtonReceiver类  
//	mComponentName = new ComponentName(mContext.getPackageName(),MediaButtonReceiver.class.getName());  
		//注册一个MedioButtonReceiver广播监听  
		mAudioManager.registerMediaButtonEventReceiver(mComponentName); 
	}
	public synchronized static void unregisterMediaButtonEventReceiver(Context mContext){
		//获得AudioManager对象  
		//构造一个ComponentName，指向MediaoButtonReceiver类  
		//下面为了叙述方便，我直接使用ComponentName类来替代MediaoButtonReceiver类  
//	ComponentName mComponentName = new ComponentName(mContext.getPackageName(),MediaButtonReceiver.class.getName());  
		//注册一个MedioButtonReceiver广播监听  
		mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);  
	}
	public synchronized static void startReceive(Context mContext) {
		// TODO Auto-generated method stub
		if (!isStarted) {
			isStarted = true;
			mIsHandFreeDevice = false;
//			if (mPttEventProcesser == null) {
//				mPttEventProcesser = new MediaButtonPttEventProcesser();
//			}
//			mPttEventProcesser.startProcessing();
			Log.i(TAG, "startReceive() ");
			writeLog2File("startReceive() ");
			
			
			
			reInitFields();
			registerMediaButtonEventReceiver(mContext);
			RtpStreamSenderUtil.reCheckNeedSendMuteData("MediaButtonReceiver.startReceive");
		
		}
	}


	public synchronized static void stopReceive(Context mContext) {
		// TODO Auto-generated method stub
		if (isStarted) {
			isStarted = false;
			Log.i(TAG, "stopReceive() ");
			writeLog2File("stopReceive() ");
			
			
//			if (mPttEventProcesser != null) {
//				mPttEventProcesser.stopProcessing();
//				mPttEventProcesser = null;
//			}
			
			//停止接收时释放ptt
			if (mIsPttDowned) {
				downPTT(false);
			}
//			unregisterMediaButtonEventReceiver(mContext);
			RtpStreamSenderUtil.reCheckNeedSendMuteData("MediaButtonReceiver.stopReceive");
		}
	}
	
	
	
	public synchronized static void reInitFields() {
		// TODO Auto-generated method stub
		downCount = 0;
		upCount = 0;
		mDownEventTime = 0;
		mDownTime = 0;
		mLastDownTime = /*0*//*1*/0;
		mDownCount = 0;
		mDownTimeDiff = 0;
	}
	
	private static boolean needWriteLog = true;
	public static void writeLog2File(String mMessageStr) {
		// TODO Auto-generated method stub
		if (!needWriteLog) {
			return;
		}
		LogUtil.makeLog(TAG, mMessageStr);
	}
	




	//add by oumogang 2013-10-28
	public static void releasePTT() {
		// TODO Auto-generated method stub
		if (mIsPttDowned) {
//			GroupCallActivity.pressPTT(!mPressed);
			//need not releas here.modify by mou 2015-01-21
//			GroupCallUtil.makeGroupCall(false, true);
		}
	}




	public static MediaButtonReceiver getInstance() {
		// TODO Auto-generated method stub
		return mInstance;
	}
	

}
