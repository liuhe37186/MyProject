package com.zed3.sipua;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.zoolu.tools.MyLog;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.zed3.alarm.MyAlarmManager;
import com.zed3.audio.AudioSettings;
import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.location.GPSInfoDataBase;
import com.zed3.location.GpsTools;
import com.zed3.location.MemoryMg;
import com.zed3.location.MyHandlerThread;
import com.zed3.media.TipSoundPlayer;
import com.zed3.media.mediaButton.HeadsetPlugReceiver;
import com.zed3.media.mediaButton.MediaButtonPttEventProcesser;
import com.zed3.power.MyPowerManager;
import com.zed3.sipua.exception.MyUncaughtExceptionHandler;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LanguageChange;
import com.zed3.utils.LogUtil;
import com.zed3.utils.NetChangedReceiver;
import com.zed3.utils.Zed3Log;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.PhoneSupportTest;

/*import com.baidu.mapapi.*;*/

public class SipUAApp extends Application {

	public static Context mContext;

	private SharedPreferences settings;

	// add by hu
	public NetChangedReceiver ncReceiver;
	public static boolean updateNextTime = false;

	public static boolean isHeadsetConnected;

	public static long lastHeadsetConnectTime;
	public BMapManager mBMapManager = null;
	public final String strKey ="hQzXk2qgLE193GnFd1S5NQi7"; //"wkK8h02YvL4fzYpOUDRl9G0t";//"hQzXk2qgLE193GnFd1S5NQi7";//// "wkK8h02YvL4fzYpOUDRl9G0t";
	// add by hdf
	public static LocationClient mLocationClient = null;
	public static GPSInfoDataBase gpsDB;
	SQLiteDatabase db;
	private static final Handler sMainThreadHandler = new Handler();
	AlarmManager alarmManager=null;
	PendingIntent pi=null;
	MyHandlerThread mHandlerThread;
	static final String PROCESS_BAIDU_SERVICE = "com.zed3.sipua:remote";

	private static final String tag = "SipUAApp";
	@Override
	public void onCreate() {
		Log.e("Build.MODEL", Build.MODEL);
		String processName = getProcessName();
		mContext = this;
		
		Zed3Log.debug("testcrash", "SipUAApp#onCreate enter process name = " + processName);
		if (!TextUtils.isEmpty(processName)) {
			if (processName.equals(PROCESS_BAIDU_SERVICE)) {
				Log.i("testapp", "SipUAApp#onCreate enter process name = " + processName + " return");
				LogUtil.makeLog(tag, "onCreate() processName "+ PROCESS_BAIDU_SERVICE);
				return;
			}
		}
		LogUtil.makeLog(tag, "onCreate()");
		// UncaughtException add by oumogang 2013-07-19
//		Thread.currentThread()
//				.setUncaughtExceptionHandler(
//						MyUncaughtExceptionHandler
//								.getInstance(getApplicationContext()));

		Thread.setDefaultUncaughtExceptionHandler(
				MyUncaughtExceptionHandler
				.getInstance(getApplicationContext()));
		LanguageChange.upDateLanguage(mContext);
		// add by hu 2013-7-8
		try {
			LocalConfigSettings.loadSettings(this);
			AutoConfigManager.LoadSettings(this);// 初始化配置信息
		} catch (IOException e) {
			e.printStackTrace();
		}

		settings = getSharedPreferences(Settings.sharedPrefsFile, MODE_PRIVATE);
		// String locateModle = sharedPreferences.getString("locateModle", "0");
		// LocationUtils.initLocateManagers(getApplicationContext(),locateModle);
		DeviceVideoInfo.supportRotate = settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_ROTATE, DeviceVideoInfo.DEFAULT_VIDEO_SUPPORT_ROTATE);
		DeviceVideoInfo.supportFullScreen = settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_FULLSCREEN, DeviceVideoInfo.DEFAULT_VIDEO_SUPPORT_ROTATE);
		DeviceVideoInfo.isHorizontal = settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_LAND, DeviceVideoInfo.DEFAULT_VIDEO_SUPPORT_LAND);
		DeviceVideoInfo.color_correct = settings.getBoolean(DeviceVideoInfo.VIDEO_COLOR_CORRECT, DeviceVideoInfo.DEFAULT_VIDEO_COLOR_CORRECT);
		DeviceVideoInfo.screen_type = settings.getString(DeviceVideoInfo.SCREEN_TYPE, DeviceVideoInfo.DEFAULT_SCREEN_TYPE);
//		DeviceVideoInfo.MaxVideoJitterbufferDelay = settings.getInt(DeviceVideoInfo.MaxJitterDelay, DeviceVideoInfo.MaxVideoJitterbufferDelay);
//		DeviceVideoInfo.MinVideoJitterbufferDelay = settings.getInt(DeviceVideoInfo.MinJitterDelay, DeviceVideoInfo.MinVideoJitterbufferDelay);
		if(DeviceVideoInfo.screen_type.equals("ver")){
			DeviceVideoInfo.isHorizontal = false;
			DeviceVideoInfo.supportRotate = false;
			DeviceVideoInfo.onlyCameraRotate = true;
		}else if(DeviceVideoInfo.screen_type.equals("hor")){
			DeviceVideoInfo.isHorizontal = true;
			DeviceVideoInfo.supportRotate = false;
			DeviceVideoInfo.onlyCameraRotate = true;
		}else{
			DeviceVideoInfo.isHorizontal = false;
			DeviceVideoInfo.supportRotate = true;
			DeviceVideoInfo.onlyCameraRotate = false;
		}
		AudioSettings.isAECOpen = settings.getBoolean(
				DeviceVideoInfo.AUDIO_AEC_SWITCH,
				DeviceVideoInfo.DEFAULT_AUDIO_AEC_SWITCH);
		DeviceVideoInfo.lostLevel = settings.getInt(DeviceVideoInfo.PACKET_LOST_LEVEL, DeviceVideoInfo.DEFAULT_PACKET_LOST_LEVEL);
		mContext = getApplicationContext();
		MemoryMg.PTIME = initPTime();
		Settings.needVideoCall = initVideoOnOff();
		Settings.mNeedBlueTooth = initBluetoothOnOff();
		super.onCreate();
		//init for tip sound pool added by wei.deng 2014-05-20
	    TipSoundPlayer.getInstance().init(getApplicationContext());
	    MyAlarmManager.getInstance().init(getApplicationContext());
	    MyPowerManager.getInstance().init(getApplicationContext());
	    
		// --------百度定位初始化---------
		mHandlerThread = MyHandlerThread.getMHThreadInstance();
		mHandlerThread.start();
		// add by hdf
		mLocationClient = new LocationClient(mContext);
		mLocationClient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				GpsTools.IsUploadGpsInfo(location,mHandlerThread);
			}
			public void onReceivePoi(BDLocation location) {
				// return ;
			}
		});
		
		GroupListUtil.registerReceiver();
		GpsTools.registerTimeChangedReceiver();
		init();
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(this);
		}
//		mBMapManager.init(strKey, new MyGeneralListener());
		mBMapManager.init(new MyGeneralListener());
		mInstance = this;
		if(Build.VERSION.SDK_INT >= 16){
			DeviceInfo.isSupportHWChange = new PhoneSupportTest().startTest();
		}
		Zed3Log.debug("testcrash", "SipUAApp#onCreate exit");
	}
	private static SipUAApp mInstance = null;
	public static SipUAApp getInstance() {
		if(mInstance == null){
			mInstance = new SipUAApp();
		}
		return mInstance;
	}
	
	private boolean initVideoOnOff() {
		String string = settings.getString(Settings.PREF_VIDEOCALL_ONOFF,
				Settings.DEFAULT_PREF_VIDEOCALL_ONOFF);
		return string.equals("0") ? false : true;
	}
	private boolean initBluetoothOnOff() {
		boolean flag = settings.getBoolean(Settings.PREF_BLUETOOTH_ONOFF,
				false);
		Settings.mNeedBlueTooth = flag;
		return flag;
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		LogUtil.makeLog(tag, "SipUAApp#onLowMemory()");
		super.onLowMemory();
	}
	@Override
	// 建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
	public void onTerminate() {
		// LocationUtils.destroyLocationManagers();
		// Log.i(tag, "取消百度定位功能");
		LogUtil.makeLog(tag, "SipUAApp#onTerminate()");
		// 释放百度定位
		if (mLocationClient != null) {
			if (mLocationClient.isStarted())
				{
					mLocationClient.stop();
				}
			mLocationClient = null;
		}
		// 取消流量悬浮窗
		Intent intent = new Intent();
		intent.setAction("com.zed3.flow.FlowRefreshService");
		stopService(intent);

		GroupListUtil.unRegisterReceiver();
		GpsTools.unRegisterTimeChangedReceiver();
		super.onTerminate();
	}

	// modify by yangjian 获取默认呼叫程序
	public static boolean on(Context context) {
		if (context == null) {
			context = SipUAApp.mContext;
		}
		Boolean pref = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(Settings.PREF_ON, Settings.DEFAULT_ON);
		return pref;
		// return true;
	}

	public static void on(Context context, boolean on) {
		if (context == null) {
			context = SipUAApp.mContext;
		}
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		edit.putBoolean(Settings.PREF_ON, on);
		edit.commit();
		if (on)
			Receiver.engine(context).isRegistered();
	}

	public static String getVersion() {
		return getVersion(mContext);
	}

	public static String getVersion(Context context) {
		final String unknown = "Unknown";

		if (context == null) {
			return unknown;
		}

		try {
			String ret = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
			if (ret.contains(" + "))
				ret = ret.substring(0, ret.indexOf(" + ")) + "b";
			return ret;
		} catch (NameNotFoundException ex) {
		}

		return unknown;
	}

	private int initPTime() {
		String ptime = settings.getString(Settings.PTIME_MODE,
				Settings.DEFAULT_PTIME_MODE);
		int ptimeInt = 20;
		switch (Integer.parseInt(ptime)) {
		case 20:
			ptimeInt = 20;
			break;
		case 40:
			ptimeInt = 40;
			break;
		case 80:
			ptimeInt = 80;
			break;
		case 100:
			ptimeInt = 100;
			break;
		case 200:
			ptimeInt = 200;
			break;
		}
		return ptimeInt;
	}
	/**
	 * init receivers and start threads. add by mou 2014-11-05
	 */
	private void init() {
		// TODO Auto-generated method stub
		ZMBluetoothManager.getInstance().registerReceivers(mContext);
//		MediaButtonPttEventProcesser.getInstance().startProcessing();
	}
	public static void exit() {
		LogUtil.makeLog(tag, "exit()");
		TipSoundPlayer.getInstance().exit();
		MyAlarmManager.getInstance().exit(mContext);
		MyPowerManager.getInstance().exit(mContext);
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!= null ) {
			ZMBluetoothManager.getInstance().exit(mContext);
//			ZMBluetoothManager.setInstanceVoid();
		}
		NetChangedReceiver.unregisterSelf();
		HeadsetPlugReceiver.stopReceive(mContext);
		ZMBluetoothManager.getInstance().unregisterReceivers(mContext);
		//音频恢复标准模式状态  add by mou 2014-10-20
		AudioUtil.getInstance().exit();
        //modify by liangzhang 2014-11-05 修改程序正常或异常退出时PTT事件处理线程(MediaButtonPttEventProcesser)没有主动结束的问题
//		MediaButtonPttEventProcesser.getInstance().stopProcessing();
	}

	class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				MyLog.e("SIPUAAPP", "baidu map network error");
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				MyLog.e("SIPUAAPP", "baidu map network data error");
			}
		}

		@Override
		public void onGetPermissionState(int iError) {
			// 非零值表示key验证未通过
			if (iError != 0) {
				// 授权Key错误：
				MyLog.e("SIPUAAPP", "baidu map key error");
			}
		}
	}

	/**
	 * startHomeActivity instead of exit activitys ,add by oumogang 2014-03-22
	 */
	public static void startHomeActivity(Context context) {
		// 跳转手机桌面
		Intent intent_ = new Intent(Intent.ACTION_MAIN);
		intent_.addCategory(Intent.CATEGORY_HOME);
		intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent_);
	}
	/**
	 * get {@link Application} Context
	 * @return
	 */
	public static Context getAppContext() {
		// TODO Auto-generated method stub
		return mContext;
	}
	
	/**
	 * get process name for diffrent initializations
	 * @return process name
	 */
	public String getProcessName(){
		File cmdFile = new File("/proc/self/cmdline");
		
		if ( cmdFile.exists() && !cmdFile.isDirectory() ){
			BufferedReader reader = null;
			try{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(cmdFile)));
				String procName = reader.readLine();
				
				if (!TextUtils.isEmpty(procName) )
					return procName.trim();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(reader != null){
					try {
						reader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return getApplicationInfo().processName;
	}
	
	public static Handler getMainThreadHandler(){
		return sMainThreadHandler;
	}
	
}
