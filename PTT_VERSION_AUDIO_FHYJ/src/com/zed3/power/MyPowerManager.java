package com.zed3.power;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;

import com.zed3.alarm.MyAlarmManager;
import com.zed3.sipua.exception.MyUncaughtExceptionHandler;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.LogUtil;
/**
 * control screen onoff and other things about power. add by mou 2014-12-11
 * @author oumogang
 *
 */
public class MyPowerManager {

	private static final String TAG = "MyPowerManager";
	public static final int SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX = 0;
	public static final String KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX = "screen_wakeup_period_index";
	private boolean mIsInited;
	private int mScreenWakeupPeriod = 0;
	private int mScreenWakeupCount = 0;
	private static Context mContext;
	private SharedPreferences mSharedPreferences;
	private PowerManager mPowerMananger;
	private WakeLock mWakeLock;
	private WakeLock cpuWakeLock;
	private MyPowerManager(){
	}
	private static final class InstanceCreater {
		public static MyPowerManager sInstance = new MyPowerManager();
	}
	public static MyPowerManager getInstance() {
		// TODO Auto-generated method stub
		return InstanceCreater.sInstance;
	}

	public void setScreenWakeupPeriod(int minutes) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("setScreenWakeupPeriod("+minutes+")");
		mScreenWakeupPeriod = minutes;
		if (mScreenWakeupPeriod > 0) {
			builder.append(" ");
			mScreenWakeupCount = 0;
		}else {
		}
		LogUtil.makeLog(TAG, "setScreenWakeupPeriod("+minutes+")");
	}
	public boolean init(Context context) {
		// TODO Auto-generated method stub
		mContext = context;
		String logMsg = "";
		if (!mIsInited) {
			mIsInited = true;
			logMsg = "MyPowerManager.init() begin";
			makeLog(TAG, logMsg );
			
			mSharedPreferences = mContext.getSharedPreferences(Settings.sharedPrefsFile,
					Context.MODE_PRIVATE);
			int screenWakeupPeriod = getScreenWakeupPeriodFromArray(mSharedPreferences.getInt(KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX, SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX));
			setScreenWakeupPeriod(screenWakeupPeriod);
			
			startReceive(mContext);
			
	        mPowerMananger = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
	        mWakeLock = mPowerMananger.newWakeLock(
	 				PowerManager.ACQUIRE_CAUSES_WAKEUP
	 						| PowerManager.FULL_WAKE_LOCK, TAG);
	        cpuWakeLock = mPowerMananger.newWakeLock(
	        		PowerManager.PARTIAL_WAKE_LOCK, TAG);
	        mPowerMananger.isScreenOn();
	        
	        acquireCpuWakeLock(TAG);
	        
			logMsg = "MyPowerManager.init() end";
			makeLog(TAG, logMsg );
		}else {
			logMsg = "MyPowerManager.init() mIsInited is true ignore";
			makeLog(TAG, logMsg );
		}
		return false;
	}
	
	public boolean exit(Context context) {
		// TODO Auto-generated method stub
		String logMsg = "";
		if (mIsInited) {
			mIsInited = false;
			logMsg = "MyPowerManager.exit() begin";
			makeLog(TAG, logMsg );
			
			stopReceive(context);
			
			mScreenWakeupCount = 0;
			mScreenWakeupPeriod = 0;
			
			releaseCpuWakeLock(TAG);
			 
			logMsg = "MyPowerManager.exit() end";
			makeLog(TAG, logMsg );
		}else {
			logMsg = "MyPowerManager.exit() mIsInited is false ignore";
			makeLog(TAG, logMsg );
		}
		return false;
	}
	private void makeLog(String tag, String logMsg) {
		// TODO Auto-generated method stub
		LogUtil.makeLog(tag, logMsg);
	}
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
	private String mScreamWakeLockAcquireTag;
//	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	/**
     * acquire WakeLock of cpu to keep cpu running.
     * add by mou 2014-12-23
     */
	public void acquireCpuWakeLock(String tag) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("acquireCpuWakeLock("+tag+")");
		try {
			if (!cpuWakeLock.isHeld()) {
				cpuWakeLock.acquire();
			}else {
				builder.append(" cpuWakeLock.isHeld() is true ignore");
			}
		} catch (Exception e) {
			// TODO: handle exception
			builder.append(" Exception "+e.getMessage());
			MyUncaughtExceptionHandler.saveExceptionLog(e);
		}finally{
			LogUtil.makeLog(TAG, builder.toString());
		}
	}
	/**
	 * release WakeLock of cpu.
	 * add by mou 2014-12-23
	 */
	public void releaseCpuWakeLock(String tag) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("releaseCpuWakeLock("+tag+")");
		try {
			if (cpuWakeLock.isHeld()) {
				cpuWakeLock.release();
			}else {
				builder.append(" cpuWakeLock.isHeld() is false ignore");
			}
		} catch (Exception e) {
			// TODO: handle exception
			builder.append(" Exception "+e.getMessage());
			MyUncaughtExceptionHandler.saveExceptionLog(e);
		}finally{
			LogUtil.makeLog(TAG, builder.toString());
		}
	}
	/**
	 * wakeup screen
	 * @return key to release wakelock
	 */
	public synchronized String wakeupScreen(String tag) {
		// TODO Auto-generated method stub
		return wakeupScreen(tag,-1);
	}
	 /**
     * wakeup screen
     * @param timeout The lock will be released after the given timeout.
     * @return key to release wakelock
     */
	public synchronized String wakeupScreen(String tag,int timeout) {
		StringBuilder builder = new StringBuilder("wakeupScreen("+tag+","+timeout+")");
		try {
			releaseScreenWakeLock(mScreamWakeLockAcquireTag); 
			if (timeout > 0) {
				mWakeLock.acquire(timeout);
			}else {
				mWakeLock.acquire();
				mScreamWakeLockAcquireTag = tag;
			}
			
			mScreenWakeupCount++;
			long currentTime = System.currentTimeMillis();
			builder.append(" count "+mScreenWakeupCount+" time "+simpleDateFormat.format(new Date(currentTime)));
			if (mScreenOffTime>0) {
				builder.append(" screenOffTime "+simpleDateFormat.format(new Date(mScreenOffTime)));
			}
			builder.append(" mScreenWakeupPeriod "+mScreenWakeupPeriod+" m");
//	 		if (handler4Ui != null) {
//	 			Message msg = handler4Ui.obtainMessage();
//	 			msg.obj = msgStr;
//	 			handler4Ui.sendMessage(msg );
//			}
		} catch (Exception e) {
			// TODO: handle exception
			builder.append(" Exception "+e.getMessage());
			MyUncaughtExceptionHandler.saveExceptionLog(e);
		}finally{
			LogUtil.makeLog(TAG, builder.toString());
		}
		return tag;
	}
    /**
     * release screen wakelock
     * @param timeout The lock will be released after the given timeout.
     */
	public synchronized void releaseScreenWakeLock(String tag) {
		StringBuilder builder = new StringBuilder("releaseScreenWakeLock("+tag+")");
		builder.append(" mScreamWakeLockAcquireTag "+mScreamWakeLockAcquireTag);
		try {
			if (mWakeLock != null && mWakeLock.isHeld()) {
				if (!TextUtils.isEmpty(mScreamWakeLockAcquireTag) && !TextUtils.isEmpty(tag) && mScreamWakeLockAcquireTag.equals(tag)) {
					mWakeLock.release();
					builder.append(" release ");
				}else {
					builder.append(" ignore ");
				}
			}else {
				builder.append(" ignore ");
			}
		} catch (Exception e) {
			// TODO: handle exception
			builder.append(" Exception "+e.getMessage());
			MyUncaughtExceptionHandler.saveExceptionLog(e);
		}finally{
			LogUtil.makeLog(TAG, builder.toString());
		}
	}

	/*
	<string-array name="screen_wakeup_period_List">
	    <item >doesn't auto wakeup screen</item>
	    <item >wakeup screen every 5 minutes</item>
	    <item >wakeup screen every 10 minutes</item>
	    <item >wakeup screen every 20 minutes</item>
	    <item >wakeup screen every 30 minutes</item>
	</string-array>
	*/
	int[] screenWakeupPeriods = new int[]{0,5,10,20,30};
	private boolean isStarted;
	public int getScreenWakeupPeriodFromArray(int screenWakeupPeriodIndex) {
		// TODO Auto-generated method stub
		return screenWakeupPeriods[screenWakeupPeriodIndex];
	}
	
	ScreenOnOffStateReceiver mScreenOnOffStateReceiver = new ScreenOnOffStateReceiver();
	public long mScreenOffTime;
	class ScreenOnOffStateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			StringBuilder builder = new StringBuilder("ScreenOnOffStateReceiver#onReceive() "+action);
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				MyAlarmManager.getInstance().cancelAlarm(ScreenWakeupActionReceiver.class);
			}else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				mScreenOffTime = System.currentTimeMillis();
				if (mScreenWakeupPeriod > 0) {
					MyAlarmManager.getInstance().setAlarm(mScreenWakeupPeriod*60, ScreenWakeupActionReceiver.class);
				}
			}
			makeLog(TAG, builder.toString());
		}
		
	}

	private void startReceive(Context context) {
		// TODO Auto-generated method stub
		if (!isStarted) {
			isStarted = true;
			IntentFilter infilter = new IntentFilter(); 
			//ÁÁÆÁ ¹ØÆÁ²Ù×÷
			infilter.addAction(Intent.ACTION_SCREEN_ON);
			infilter.addAction(Intent.ACTION_SCREEN_OFF);
			context.registerReceiver(mScreenOnOffStateReceiver, infilter);
		}
	}
	private void stopReceive(Context context) {
		// TODO Auto-generated method stub
		if (isStarted) {
			isStarted = false;
			context.unregisterReceiver(mScreenOnOffStateReceiver);
		}
	}

	public boolean isScreenOn() {
		// TODO Auto-generated method stub
		boolean result = false;
		if (mPowerMananger != null) {
			result = mPowerMananger.isScreenOn();
		}
		return result;
	}
}
