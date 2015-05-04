package com.zed3.alarm;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.exception.MyUncaughtExceptionHandler;
import com.zed3.utils.LogUtil;
/**
 * add or cancel alarms. add by mou 2014-12-11
 * @author oumogang
 *
 */
public class MyAlarmManager {
	private static final String TAG = "MyAlarmManager";
	private static Context mContext;
	private boolean mIsInited;
	private AlarmManager mAlarmManager;

	private MyAlarmManager(){
	}
	private static final class InstanceCreater {
		public static MyAlarmManager sInstance = new MyAlarmManager();
	}
	public static MyAlarmManager getInstance() {
		// TODO Auto-generated method stub
		if (mContext == null) {
			mContext = SipUAApp.getAppContext();
		}
		return InstanceCreater.sInstance;
	}
	/**
	 * setAlarm
	 * @param renew_time second
	 * @param cls
	 */
	public synchronized void setAlarm(int renew_time, Class<?> cls) {
		StringBuilder builder = new StringBuilder("setAlarm(" + renew_time + "," + cls + ")");
		try {
			Intent intent = new Intent(mContext, cls);
			PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,
					0);
			// 如果有时间间隔 则开启定时器
			if (renew_time > 0) {
				if (isXiaoMI()) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.add(Calendar.SECOND, renew_time);
					mAlarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
				} else {
					mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							SystemClock.elapsedRealtime() + renew_time * 1000,
							sender);
				}
			} else {
				// 取消AlarmManager（其中需要注意的是取消的Intent必须与启动Intent保持绝对一致才能支持取消AlarmManager）
				mAlarmManager.cancel(sender);
				builder.append(" cancel alarm");
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
	 * cancelAlarm
	 * @param cls
	 */
	public synchronized void cancelAlarm(Class<?> cls) {
		StringBuilder builder = new StringBuilder("cancelAlarm(" + cls + ")");
		try {
			Intent intent = new Intent(mContext, cls);
			PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,
					0);
			mAlarmManager.cancel(sender);
		} catch (Exception e) {
			// TODO: handle exception
			builder.append(" Exception "+e.getMessage());
			MyUncaughtExceptionHandler.saveExceptionLog(e);
		}finally{
			LogUtil.makeLog(TAG, builder.toString());
		}
	}
	
	// 判断是否是小米
	private static boolean isXiaoMI() {
		return (Build.MODEL.contains("MI 1S") || Build.MODEL.contains("MI 2S")
				|| Build.MODEL.contains("HUAWEI G700-U00")
				|| Build.MODEL.contains("HUAWEI P6-U06")
				|| Build.MODEL.contains("HUAWEI MT1-U06") || Build.MODEL
					.contains("HUAWEI Y511-T00"));
	}
	
	public synchronized boolean init(Context context) {
		// TODO Auto-generated method stub
		mContext = context;
		String logMsg = "";
		if (!mIsInited) {
			mIsInited = true;
			logMsg = "MyPowerManager.init() begin";
			makeLog(TAG, logMsg );
			
			mAlarmManager = (AlarmManager) mContext
					.getSystemService(Context.ALARM_SERVICE);
			
			logMsg = "MyPowerManager.init() end";
			makeLog(TAG, logMsg );
		}else {
			logMsg = "MyPowerManager.init() mIsInited is true ignore";
			makeLog(TAG, logMsg );
		}
		return false;
	}
	
	public synchronized boolean exit(Context context) {
		// TODO Auto-generated method stub
		String logMsg = "";
		if (mIsInited) {
			mIsInited = false;
			logMsg = "MyPowerManager.exit() begin";
			makeLog(TAG, logMsg );
			
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
}