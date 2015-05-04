/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.zed3.sipua.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.zoolu.sip.provider.SipStack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.EditText;
import com.zed3.codecs.Codec;
import com.zed3.codecs.CodecBase;
import com.zed3.codecs.Codecs;
import com.zed3.flow.FlowRefreshService;
import com.zed3.location.GPSPacket;
import com.zed3.location.GpsTools;
import com.zed3.location.MemoryMg;
import com.zed3.log.CrashHandler;
import com.zed3.settings.AboutActivity;
import com.zed3.settings.AdvancedChoice;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.autoUpdate.UpdateVersionService;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.sipua.welcome.LoginActivity;
import com.zed3.toast.MyToast;

public class Settings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnClickListener {
	CheckBoxPreference mCheckbox0;

	/** -----------定位模式控制,在上报位置信息的时候，进行判断----------- */
	public static boolean needGPS = true;
	public static boolean needBDGPS = true;

	// Current settings handler
	private static SharedPreferences settings;
	CheckBoxPreference mCheckbox_flow;
	// Context definition
	public static Settings context = null;
	public static Context mContext;
	private static boolean needRestart = true;

	// Path where to store all profiles - !!!should be replaced by some system
	// variable!!!
	private final static String profilePath = "/sdcard/Zed3/";
	// Path where is stored the shared preference file - !!!should be replaced
	// by some system variable!!!
	private final String sharedPrefsPath = "/data/data/com.zed3.sipua/shared_prefs/";
	// Shared preference file name - !!!should be replaced by some system
	// variable!!!
	public final static String sharedPrefsFile = "com.zed3.sipua_preferences";

	// IDs of the menu items
	public static final String VAL_PREF_PSTN = "PSTN";
	public static final String VAL_PREF_SIP = "SIP";
	public static final String VAL_PREF_SIPONLY = "SIPONLY";
	public static final String VAL_PREF_ASK = "ASK";
	public static final String PREF_AECSWITCH ="aecswitch";
	public static final String PREF_USERNAME = "username";
	public static final String PREF_PASSWORD = "password";
	public static final String PREF_SERVER = "server";
	public static final String PREF_DOMAIN = "domain";
	public static final String PREF_FROMUSER = "fromuser";
	public static final String PREF_PORT = "port";
	public static final String PREF_PROTOCOL = "protocol";
	public static final String PREF_WLAN = "wlan";
	public static final String PREF_3G = "3g";
	public static final String PREF_EDGE = "edge";
	public static final String PREF_VPN = "vpn";
	public static final String PREF_PREF = "pref";
	public static final String PREF_AUTO_ON = "auto_on";
	public static final String PREF_MSG_ENCRYPT = "msg_encrypt";
	
	public static final String PREF_AUDIO_SWITCH = "audio_switch";
	public static final String PREF_GPS_REMOTE = "gps_remote";
	
	//注册间隔
	public static final String PREF_REGTIME_EXPIRES = "regtime_expires";
	//蓝牙
	public static final String PREF_BLUETOOTH_ONOFF = "bluetoothonoff";
	//唤醒开关
	public static final String PREF_MICWAKEUP_ONOFF = "micwakeuponoff";
	public static final String PREF_AUTO_ONDEMAND = "auto_on_demand";
	public static final String PREF_AUTO_HEADSET = "auto_headset";
	public static final String PREF_MWI_ENABLED = "MWI_enabled";
	public static final String PREF_REGISTRATION = "registration";
	public static final String PREF_NOTIFY = "notify";
	public static final String PREF_NODATA = "nodata";
	public static final String PREF_SIPRINGTONE = "sipringtone";
	public static final String PREF_SEARCH = "search";
	public static final String PREF_EXCLUDEPAT = "excludepat";
	public static final String PREF_EARGAIN = "eargain";
	public static final String PREF_MICGAIN = "micgain";
	public static final String PREF_HEARGAIN = "heargain";
	public static final String PREF_HMICGAIN = "hmicgain";
	public static final String PREF_OWNWIFI = "ownwifi";
	public static final String PREF_STUN = "stun";
	public static final String PREF_STUN_SERVER = "stun_server";
	public static final String PREF_STUN_SERVER_PORT = "stun_server_port";
	//通话类型
	public static final String PHONE_MODE = "phoneMode";
	public static final String DEFAULT_PHONE_MODE = "1";
	//语音会议
	public static final String PERF_AUDIO_CONFERENCE = "audio_conference";
	//程序检查更新
	public static final String PERF_CHECK_UPDATE = "check_update";
	//对讲地图模式
	public static final String PERF_PTT_MAP = "ptt_map";
	//图片拍传
	public static final String PERF_PIC_UPLOAD = "pic_upload";
	//短消息
	public static final String PERF_SMS = "spt_sms";
	
	// amr add by oumogang 2013.01.05
	public static final String AMR_MODE = "amrMode";
	public static final String DEFAULT_AMR_MODE = "4.75";
	// ptime add by oumogang 2013.01.05
	public static final String PTIME_MODE = "ptime";
	public static final String DEFAULT_PTIME_MODE = "100";
	// ptime add by oumogang 2013.01.05
	public static final String PREF_AUTORUN = "autorunkey";
	public static final String DEFAULT_PREF_AUTORUN = "1";
	// locateModle add by oumogang 2013.01.05
	public static final String PREF_LOCATEMODE = "locateModle";
	public static final int DEFAULT_PREF_LOCATEMODE = 3;
	
	public static final String SERVER_UNIX_TIME = "serverUnixTime";
	public static final long DEFAULT_SERVER_UNIX_TIME = -1;
	
	public static final String UNIX_TIME = "UnixTime";
	public static final long DEFAULT_UNIX_TIME = -1;
	
	public static final String LOCAL_TIME = "LocalTime";
	public static final long DEFAULT_LOCAL_TIME = -1;
	
	public static final String REALTIME = "Realtime";
	public static final long DEFAULT_REALTIME = -1;
	//gps定位间隔
	public static final String PREF_LOCSETTIME = "locateSetTime";
	public static final int DEFAULT_PREF_LOCSETTIME = 1;
	//gps上报间隔
	public static final String PREF_LOCUPLOADTIME = "locateUploadTime";
	public static final int DEFAULT_PREF_LOCUPLOADTIME = 1;
	//静音检测
	public static final String AUDIO_VADCHK = "audiovadchk";
	public static final String DEFAULT_VAD_MODE = "0";
	
	// gps_toast add by oumogang 2013.01.06
	public static final String PREF_GPSTOAST = "gps_toast";
	public static final String DEFAULT_PREF_GPSTOAST = "true";
	// bdgps_toast add by oumogang 2013.01.06
	public static final String PREF_BDGPSTOAST = "bdgps_toast";
	public static final String DEFAULT_PREF_BDGPSTOAST = "true";
	// location_upload_toast add by oumogang 2013.01.06
	public static final String PREF_LOCATIONUPLOADTOAST = "location_upload_toast";
	public static final String DEFAULT_PREF_LOCATIONUPLOADTOAST = "true";
	// gpsOpenModleModle add by oumogang 2013.01.22
	public static final String PREF_GPSOPENMODLE = "gpsOpenModleModle";
	public static final int DEFAULT_PREF_GPSOPENMODLE = 0;
	// gpsOnOff add by oumogang 2013.01.24
	public static final String PREF_GPSONOFF = "gpsOnOffKey";
	public static final boolean DEFAULT_PREF_GPSONOFF = false;
	// videoCallOnOff add by oumogang 2013.04.28
	public static final String PREF_VIDEOCALL_ONOFF = "videoCallKey";
	public static final String DEFAULT_PREF_VIDEOCALL_ONOFF = "1";
	protected boolean isStarted;
	private Thread threadForGps;
	private Preference gpsOnOffPreference;

	// MMTel configurations (added by mandrajg)
	public static final String PREF_MMTEL = "mmtel";
	public static final String PREF_MMTEL_QVALUE = "mmtel_qvalue";

	// Call recording preferences.
	public static final String PREF_CALLRECORD = "callrecord";

	public static final String PREF_PAR = "par";
	public static final String PREF_IMPROVE = "improve";
	public static final String PREF_POSURL = "posurl";
	public static final String PREF_POS = "pos";
	public static final String PREF_CALLBACK = "callback";
	public static final String PREF_CALLTHRU = "callthru";
	public static final String PREF_CALLTHRU2 = "callthru2";
	public static final String PREF_CODECS = "codecs_new";
	public static final String PREF_DNS = "dns";
	public static final String PREF_VQUALITY = "vquality";
	public static final String PREF_MESSAGE = "vmessage";
	public static final String PREF_BLUETOOTH = "bluetooth";
	public static final String PREF_KEEPON = "keepon";
	public static final String PREF_SELECTWIFI = "selectwifi";
	public static final String PREF_ACCOUNT = "account";

	// Default values of the preferences
	public static final String DEFAULT_USERNAME = "";
	public static final String DEFAULT_PASSWORD = "";
	public static final String DEFAULT_SERVER = "";
	public static final String DEFAULT_DOMAIN = "";
	public static final String DEFAULT_FROMUSER = "";
	public static final String DEFAULT_PORT = "" + SipStack.default_port;
	public static final String DEFAULT_PROTOCOL = "tcp";
	public static final boolean DEFAULT_WLAN = true;
	public static final boolean DEFAULT_3G = false;
	public static final boolean DEFAULT_EDGE = false;
	public static final boolean DEFAULT_VPN = false;

	// public static final String DEFAULT_PREF = VAL_PREF_SIP;
	public static final String DEFAULT_PREF = VAL_PREF_PSTN;
	public static final boolean DEFAULT_AUTO_ON = false;
	public static final boolean DEFAULT_AUTO_ONDEMAND = false;
	public static final boolean DEFAULT_AUTO_HEADSET = false;
	public static final boolean DEFAULT_MWI_ENABLED = true;
	public static final boolean DEFAULT_REGISTRATION = true;
	public static final boolean DEFAULT_NOTIFY = false;
	public static final boolean DEFAULT_NODATA = false;
	public static final String DEFAULT_SIPRINGTONE = "";
	public static final String DEFAULT_SEARCH = "";
	public static final String DEFAULT_EXCLUDEPAT = "";
	public static final float DEFAULT_EARGAIN = (float) 0.5/*0.25*/;
	public static final float DEFAULT_MICGAIN = (float) /*0.25*/0.5;
	public static final float DEFAULT_HEARGAIN = (float) 0.25;
	public static final float DEFAULT_HMICGAIN = (float) 1.0;
	public static final boolean DEFAULT_OWNWIFI = false;
	public static final boolean DEFAULT_STUN = false;
	public static final String DEFAULT_STUN_SERVER = "stun.ekiga.net";
	public static final String DEFAULT_STUN_SERVER_PORT = "3478";

	// MMTel configuration (added by mandrajg)
	public static final boolean DEFAULT_MMTEL = false;
	public static final String DEFAULT_MMTEL_QVALUE = "1.00";

	// Call recording preferences.
	public static final boolean DEFAULT_CALLRECORD = false;

	public static final boolean DEFAULT_PAR = false;
	public static final boolean DEFAULT_IMPROVE = false;
	public static final String DEFAULT_POSURL = "";
	public static final boolean DEFAULT_POS = false;
	public static final boolean DEFAULT_CALLBACK = false;
	public static final boolean DEFAULT_CALLTHRU = false;
	public static final String DEFAULT_CALLTHRU2 = "";
	public static final String DEFAULT_CODECS = null;
	public static final String DEFAULT_DNS = "";
	public static final String DEFAULT_VQUALITY = "low";
	public static final boolean DEFAULT_MESSAGE = false;
	public static final boolean DEFAULT_BLUETOOTH = false;
	public static final boolean DEFAULT_KEEPON = false;
	public static final boolean DEFAULT_SELECTWIFI = false;
	public static final int DEFAULT_ACCOUNT = 0;

	// An other preference keys (not in the Preferences XML file)
	public static final String PREF_OLDVALID = "oldvalid";
	public static final String PREF_SETMODE = "setmode";
	public static final String PREF_OLDVIBRATE = "oldvibrate";
	public static final String PREF_OLDVIBRATE2 = "oldvibrate2";
	public static final String PREF_OLDPOLICY = "oldpolicy";
	public static final String PREF_OLDRING = "oldring";
	public static final String PREF_AUTO_DEMAND = "auto_demand";
	public static final String PREF_WIFI_DISABLED = "wifi_disabled";
	public static final String PREF_ON_VPN = "on_vpn";
	public static final String PREF_NODEFAULT = "nodefault";
	public static final String PREF_NOPORT = "noport";
	public static final String PREF_ON = "on";
	public static final String PREF_PREFIX = "prefix";
	public static final String PREF_COMPRESSION = "compression";
	// public static final String PREF_RINGMODEx = "ringmodeX";
	// public static final String PREF_VOLUMEx = "volumeX";

	// Default values of the other preferences
	public static final boolean DEFAULT_OLDVALID = false;
	public static final boolean DEFAULT_SETMODE = false;
	public static final int DEFAULT_OLDVIBRATE = 0;
	public static final int DEFAULT_OLDVIBRATE2 = 0;
	public static final int DEFAULT_OLDPOLICY = 0;
	public static final int DEFAULT_OLDRING = 0;
	public static final boolean DEFAULT_AUTO_DEMAND = false;
	public static final boolean DEFAULT_WIFI_DISABLED = false;
	public static final boolean DEFAULT_ON_VPN = false;
	public static final boolean DEFAULT_NODEFAULT = false;
	public static final boolean DEFAULT_NOPORT = false;
	public static final boolean DEFAULT_ON = false;
	public static final String DEFAULT_PREFIX = "";
	public static final String DEFAULT_COMPRESSION = null;

	// 优先级
	public static final String HIGH_PRI_KEY = "highPriority";
	public static final String SAME_PRI_KEY = "samePriority";
	public static final String LOW_PRI_KEY = "lowPriority";

	public static final String RESTORE_AFTER_OTHER_GROUP = "restoreAfterOtherGrp";
	// public static final int HIGH_PRIORITY = 0;
	// public static final int SAME_PRIORITY = 0;
	// public static final int LOW_PRIORITY = 0;
	// public static final String DEFAULT_RINGTONEx = "";
	// public static final String DEFAULT_VOLUMEx = "";

	public static float getEarGain() {
		try {
			return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(
					Receiver.mContext).getString(
					Receiver.headset > 0 ? PREF_HEARGAIN : PREF_EARGAIN,
					"" + DEFAULT_EARGAIN));
		} catch (NumberFormatException i) {
			return DEFAULT_EARGAIN;
		}
	}

	public static float getMicGain() {
		if (Receiver.headset > 0 || Receiver.bluetooth > 0) {
			try {
				return Float.valueOf(PreferenceManager
						.getDefaultSharedPreferences(Receiver.mContext)
						.getString(PREF_HMICGAIN, "" + DEFAULT_HMICGAIN));
			} catch (NumberFormatException i) {
				return DEFAULT_HMICGAIN;
			}
		}

		try {
			return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(
					Receiver.mContext).getString(PREF_MICGAIN,
					"" + DEFAULT_MICGAIN));
		} catch (NumberFormatException i) {
			return DEFAULT_MICGAIN;
		}
	}

	Handler gpsStataHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			gpsOnOffPreference.setSummary(getGpsOnOffSummary());
		};
	};
	protected boolean isDestroied;

	private boolean needToast = true;

	@Override
	protected void onResume() {
		if (threadForGps == null) {
			threadForGps = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					isDestroied = false;
					while (!isDestroied) {
						// 当除于可操作时，每半秒钟更新一次；
						while (isDestroied) {
							// gpsOnOffPreference.setSummary(getGpsOnOffSummary());
							gpsStataHandler.sendEmptyMessage(0);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						// 当界面不可操作时暂停更新；
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			threadForGps.start();
		} else {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			boolean providerEnabled = locationManager.isProviderEnabled("gps");
			mSharedPreferences.edit()
					.putBoolean(PREF_GPSONOFF, providerEnabled).commit();
		}
		Receiver.engine(this);
		super.onResume();
	}

	private void exitApp() {
		Intent it = new Intent(mContext, FlowRefreshService.class);
		mContext.stopService(it);
		Receiver.engine(mContext).expire(-1);
		// 延迟
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// deleted by hdf 停止
		Receiver.engine(mContext).halt();
		// 停止服务
		stopService(new Intent(mContext, RegisterService.class));
		// 取消全局定时器
		Receiver.alarm(0, OneShotAlarm.class);
		startActivity(new Intent(Settings.this,LoginActivity.class));
		this.finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = /* this */getApplicationContext();
//		if (Receiver.mContext == null) {
//			Receiver.mContext = mContext;
//		}

		// MyToast.showToast(needToast ,mContext,"setting is creatting",0);

		super.onCreate(savedInstanceState);
		this.getListView().setBackgroundColor(Color.WHITE);
//		int x = this.getListView().geti.getChildCount();
		WindowManager windowManager = getWindowManager();
		setDefaultValues();
		Codecs.check();
//		PreferenceScreen b = (PreferenceScreen) findPreference("videosizesettingkey");
//		b.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//			@Override
//			public boolean onPreferenceClick(Preference preference) {
//				Intent intent = new Intent();
//				intent.setClass(Settings.this, SettingVideoSize.class);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(intent);
//				return false;
//			}
//		});
		PreferenceScreen about = (PreferenceScreen) findPreference("aboutkey");
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(Settings.this, AboutActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return false;
			}
		});
		PreferenceScreen adchoice = (PreferenceScreen) findPreference("advanced_choice");
		adchoice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(Settings.this, AdvancedChoice.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return false;
			}
		});
		
		PreferenceScreen logoff = (PreferenceScreen) findPreference("logoff");
		if(!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN){
			logoff.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Editor edit = settings.edit();
				edit.putString(PREF_USERNAME, DEFAULT_USERNAME);
				edit.putString(PREF_PASSWORD, DEFAULT_PASSWORD);
				edit.commit();
				exitApp();
				return false;
			}
		});
		}else{
			PreferenceScreen parent = (PreferenceScreen) findPreference("parent");
			parent.removePreference(logoff);

		}
		PreferenceScreen checkversion = (PreferenceScreen) findPreference("checkVersion");
		checkversion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String serverIp =settings.getString(Settings.PREF_SERVER, Settings.DEFAULT_SERVER);
				if(!TextUtils.isEmpty(serverIp) && serverIp.split("\\.").length == 4){
				UpdateVersionService service = new UpdateVersionService(Settings.this,serverIp);
				service.checkUpdate(true);
				}
				return false;
			}
		});
//		mCheckbox0 = (CheckBoxPreference) findPreference("flowOnOffKey");
//		mCheckbox0
//				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//					@Override
//					public boolean onPreferenceChange(Preference arg0,
//							Object newValue) {
//						// 这里可以监听到checkBox中值是否改变了
//						// 并且可以拿到新改变的值
//						Intent intent = new Intent();
//						intent.setFlags(Service.START_NOT_STICKY);
//						intent = new Intent();
//						intent.setAction("com.zed3.flow.FlowRefreshService");
//						if (!mSharedPreferences.getBoolean("flowOnOffKey",
//								false)) {
//							startService(intent);
//						} else {
//							stopService(intent);
//						}
//						return true;
//					}
//				});
//		ListPreference port = (ListPreference) findPreference(PREF_PORT);
//		port.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//			@Override
//			public boolean onPreferenceClick(Preference preference) {
//				if (mDialog != null)
//					mDialog.cancel();
//				needRestart = false;
//				onSharedPreferenceChanged(settings, PREF_PORT);
//				needRestart = true;
//				return false;
//			}
//		});
//		setEditMaxLength(PREF_USERNAME,11);//username
//		setEditMaxLength(PREF_PASSWORD,11);
//		setEditMaxLength(PREF_SERVER,30);
	}

	//add by hu
	//设置editpreference的最大长度
	private void setEditMaxLength(String key,int length){
		EditTextPreference pre  = (EditTextPreference) findPreference(key);
		pre.getEditText().setFilters(new InputFilter[]{
		                    new InputFilter.LengthFilter(length)
		            });

	}
	private void restoreTitle(String key,final String value){
		PreferenceScreen group = (PreferenceScreen) findPreference(key);
		group.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PreferenceScreen f = (PreferenceScreen) preference;
				f.setTitle(value);

				return true;
			}
		});
	}

	private void setDefaultValues() {
		settings = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
		settings.registerOnSharedPreferenceChangeListener(this);
		updateSummaries();
	}

	/**
	 * 获取字符串 1011@192.168.100.45
	 * 
	 * @param s
	 * @return
	 */
	public static String getProfileNameString(SharedPreferences s) {
		String provider = s.getString(PREF_SERVER, DEFAULT_SERVER);

		if (!s.getString(PREF_DOMAIN, "").equals("")) {
			provider = s.getString(PREF_DOMAIN, DEFAULT_DOMAIN);
		}

		return s.getString(PREF_USERNAME, DEFAULT_USERNAME) + "@" + provider;
	}
	public void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
		}
	}

	@Override
	public void onDestroy() {
		settings.unregisterOnSharedPreferenceChangeListener(this);

		super.onDestroy();

	}

	EditText transferText;
	Dialog mDialog;
	String mKey;

	private boolean needLog = true;

	// private Preference videoCallOnOffPreference;

	public static boolean needVideoCall = false;
	public static SharedPreferences mSharedPreferences;

	public static boolean needUpdate2Contacts;

	public static String mUserName;
	public static String mPassword;

	public static boolean needSendLocateBroadcast;

	public static boolean needCanselEcho = true;

	public static int mEchofilterlength = /*1024*/8000;

	public static boolean needGIS = false;

	public static boolean needReleaseRecord = true;
	
	public static boolean needBluetoothPTT = true;

	public static boolean mNeedBlueTooth = false;
	

	/**
	 * 配置改变监听器，当配置文件改变时，回调的方法；
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		mSharedPreferences = sharedPreferences;

		// add by hdf
		if (!MemoryMg.getInstance().IsChangeListener)
			return;

		try {

			if (!Thread.currentThread().getName().equals("main"))
				return;
			//add by oumogang 2013-05-27
			if (key.startsWith(PREF_PORT)
					&& sharedPreferences.getString(key, DEFAULT_PORT).equals(
							"0")) {
				// Editor edit = sharedPreferences.edit();
				// edit.putString(key, DEFAULT_PORT);
				// edit.commit();

				transferText = new InstantAutoCompleteTextView(this, null);
				transferText.setInputType(InputType.TYPE_CLASS_NUMBER);
				transferText.setTextColor(Color.BLACK);
				transferText.setFilters(new InputFilter[]{
	                    new InputFilter.LengthFilter(5)
	            });
				mKey = key;
				if (mDialog != null)
					mDialog.cancel();
				mDialog = new AlertDialog.Builder(Settings.this)
						.setTitle(
								Receiver.mContext
										.getString(R.string.settings_port))
						.setView(transferText)
						.setPositiveButton(android.R.string.ok, Settings.this)
						.create();
				mDialog.show();
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (mDialog != null) {
								Editor edit = settings.edit();
								edit.putString(mKey, DEFAULT_PORT);
								edit.commit();
							}
						}
						return false;
					}
				});
				mDialog.setOwnerActivity(Settings.this);
				mDialog.getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

				return;
			} else if (key.startsWith(PREF_SERVER)) {
//				Editor edit = sharedPreferences.edit();
//					edit.putString(PREF_DNS, DEFAULT_DNS);
//					if (key.equals(PREF_SERVER)) {
//						// ListPreference lp = (ListPreference)
//						// getPreferenceScreen().findPreference(PREF_PROTOCOL+j);
//						// lp.setValue(sharedPreferences.getString(PREF_SERVER+j,
//						// DEFAULT_SERVER).equals(DEFAULT_SERVER) ? "tcp" :
//						// "udp");
//
//						ListPreference lp = (ListPreference) getPreferenceScreen()
//								.findPreference(PREF_PORT );
//						lp.setValue(sharedPreferences.getString(
//								PREF_SERVER , DEFAULT_SERVER).equals(
//								DEFAULT_SERVER) ? "5061" : DEFAULT_PORT);
//
//					}
//				edit.commit();
				
				//add by oumogang 2013-06-03
				//更换服务器后，gps服务器没变，导致上报失败。
				GpsTools.setServer(sharedPreferences.getString(PREF_SERVER,DEFAULT_SERVER));
				Receiver.engine(this).updateDNS();
				
//				Checkin.checkin(false);
				// 让服务器号立即生效 add by oumogang 2013.01.06
				Receiver.engine(this).halt();
				Receiver.engine(this).StartEngine();

				// Toast.makeText(getApplicationContext(),"重新登录",0).show();

			} else if (sharedPreferences.getBoolean(PREF_CALLBACK,
					DEFAULT_CALLBACK)
					&& sharedPreferences.getBoolean(PREF_CALLTHRU,
							DEFAULT_CALLTHRU)) {
				CheckBoxPreference cb = (CheckBoxPreference) getPreferenceScreen()
						.findPreference(
								key.equals(PREF_CALLBACK) ? PREF_CALLTHRU
										: PREF_CALLBACK);
				cb.setChecked(false);
			} else if (key.startsWith(PREF_WLAN)
					|| key.startsWith(PREF_3G)
					|| key.startsWith(PREF_EDGE)
//					|| key.startsWith(PREF_USERNAME)
//					|| key.startsWith(PREF_PASSWORD)
					|| key.startsWith(PREF_DOMAIN)
					|| key.startsWith(PREF_SERVER)
					|| key.startsWith(PREF_PORT)
					|| key.equals(PREF_STUN)
					|| key.equals(PREF_STUN_SERVER)
					|| key.equals(PREF_STUN_SERVER_PORT)
					|| key.equals(PREF_MMTEL)
					|| // (added by mandrajg)
					key.equals(PREF_MMTEL_QVALUE)
					|| // (added by mandrajg)
					key.startsWith(PREF_PROTOCOL) || key.startsWith(PREF_VPN)
					|| key.equals(PREF_POS) || key.equals(PREF_POSURL)
					|| key.startsWith(PREF_FROMUSER)
					|| key.equals(PREF_AUTO_ONDEMAND)
					|| key.equals(PREF_MWI_ENABLED)
					|| key.equals(PREF_MSG_ENCRYPT)
					|| key.equals(PREF_REGISTRATION) || key.equals(PREF_KEEPON)) {
				// key.startsWith(PREF_DNS)) {
				if (key.equals(PREF_USERNAME)){
					sendBroadcast(new Intent(Contants.ACTION_CLEAR_GROUPLIST));
				}
				
				if (needRestart) {
					Receiver.engine(this).halt();
					Receiver.engine(this).StartEngine();
				}
				
			}
			// 组呼来电处理方式设置，"提示"，"接听"，"拒接"；
			else if (key.startsWith(HIGH_PRI_KEY)
					|| key.startsWith(SAME_PRI_KEY)
					|| key.startsWith(LOW_PRI_KEY)) {
				// saveProirity();
				Editor edit = sharedPreferences.edit();
				edit.commit();
			}
			//
			else if (key.startsWith(PREF_WLAN) || key.startsWith(PREF_3G)
					|| key.startsWith(PREF_EDGE)
					|| key.startsWith(PREF_OWNWIFI)) {
				updateSleep();
			}
			// GPS搜星失败提示
			else if (key.equals("gpsfailtoolkey")) {
				/*
				 * String val = sharedPreferences.getString("gpsfailtoolkey",
				 * "0");// if (val.equals("0"))// 终端主动开启GPS {
				 * MemoryMg.getInstance().GPSSatelliteFailureTip=true; } else {
				 * MemoryMg.getInstance().GPSSatelliteFailureTip=false; }
				 */
				MemoryMg.getInstance().GPSSatelliteFailureTip = sharedPreferences
						.getBoolean(key, false);

			}
			// GPS登录错误提示
			else if (key.equals("gpstoolkey")) {
				/*
				 * String val = sharedPreferences.getString("gpsfailtoolkey",
				 * "0");// if (val.equals("0"))// 终端主动开启GPS {
				 * MemoryMg.getInstance().GPSSatelliteFailureTip=true; } else {
				 * MemoryMg.getInstance().GPSSatelliteFailureTip=false; }
				 */
//				GPSPacket.needToastForGPSLogin = sharedPreferences.getBoolean(
//						key, false);
			}
		

			// BDGPS位置信息Toast提示 add by oumogang 2013.01.06
//			else if (PREF_GPSTOAST.equals(key)) {
//				GpsManager.needToastForGPS = sharedPreferences.getBoolean(key,
//						false);
//			}
			// 基站定位信息Toast提示 add by oumogang 2013.01.06
//			else if (PREF_BDGPSTOAST.equals(key)) {
//				BDGPSManager.needToastForBDGps = sharedPreferences.getBoolean(
//						key, false);
//			}
			// 版本信息选项 guojunfeng add
			else if (key.equals("version_information")) {
				new AlertDialog.Builder(context)
						.setMessage(
								getString(R.string.about).replace("\\n", "\n")
										.replace("${VERSION}",
												getVersion(context)))
						.setTitle(getString(R.string.menu_about))
						.setCancelable(true).show();
			}
			// amr mode 设置 add by oumogang 2012-12-25
			else if (AMR_MODE.equals(key)) {
			}
			// autorun 设置 add by oumogang 2013.01.05
			else if (PREF_AUTORUN.equals(key)) {
				//
			}
			// videoCallOnOff 设置 add by oumogang 2013.04.28
			else if (PREF_VIDEOCALL_ONOFF.equals(key)) {
				needVideoCall = (sharedPreferences.getString(key,
						DEFAULT_PREF_VIDEOCALL_ONOFF).equals("0")) ? false
						: true;
			} else if ("logOnOffKey".equals(key)) {// log开关
				boolean logflag = sharedPreferences.getBoolean(key, false);

				if (logflag) {

					CrashHandler csh = CrashHandler.getInstance();
					csh.init(this, true);
				} else {
					CrashHandler.EndLog();
				}
			}

			updateSummaries();

			// Receiver.engine(this).register();
		} catch (Exception e) {
			Log.e("settings tag", e.toString());
			e.printStackTrace();
		}
	}



	private String getVersion(Context context) {
		// TODO Auto-generated method stub
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

	int updateSleepPolicy() {
		ContentResolver cr = getContentResolver();
		int get = android.provider.Settings.System.getInt(cr,
				android.provider.Settings.System.WIFI_SLEEP_POLICY, -1);
		int set = get;
		boolean wlan = false, g3 = true, valid = false;
			if (!settings.getString(PREF_USERNAME, "").equals("")
					&& !settings.getString(PREF_SERVER, "").equals("")) {
				valid = true;
				wlan |= settings.getBoolean(PREF_WLAN , DEFAULT_WLAN);
				g3 &= settings.getBoolean(PREF_3G, DEFAULT_3G)
						|| settings.getBoolean(PREF_EDGE, DEFAULT_EDGE);
			}
		boolean ownwifi = settings.getBoolean(PREF_OWNWIFI, DEFAULT_OWNWIFI);

		if (g3 && valid && !ownwifi) {
			set = android.provider.Settings.System.WIFI_SLEEP_POLICY_DEFAULT;
		} else if (wlan || ownwifi) {
			set = android.provider.Settings.System.WIFI_SLEEP_POLICY_NEVER;
		}
		return set;
	}

	void updateSleep() {
		ContentResolver cr = getContentResolver();
		int get = android.provider.Settings.System.getInt(cr,
				android.provider.Settings.System.WIFI_SLEEP_POLICY, -1);
		int set = updateSleepPolicy();

		if (set != get) {
			MyToast.showToast(
					true,
					this,
					set == android.provider.Settings.System.WIFI_SLEEP_POLICY_DEFAULT ? R.string.settings_policy_default
							: R.string.settings_policy_never);
			android.provider.Settings.System.putInt(cr,
					android.provider.Settings.System.WIFI_SLEEP_POLICY, set);
		}
	}

	private void setSummaries(String key, String defaultvalue, String unit) {
		PreferenceScreen preferenceScreen = getPreferenceScreen();
		getPreferenceScreen().findPreference(key).setSummary(
				settings.getString(key, defaultvalue) + unit);
	}

	private void setSummaries(String key, String value) {
		PreferenceScreen preferenceScreen = getPreferenceScreen();
		getPreferenceScreen().findPreference(key).setSummary(value);
	}

	public void updateSummaries() {
		setSummaries(PREF_USERNAME, DEFAULT_USERNAME, "");
		setSummaries(PREF_SERVER, DEFAULT_SERVER, "");
		setSummaries(PREF_PORT, DEFAULT_PORT, "");
		String autoRunVal = settings.getString(PREF_AUTORUN,
				DEFAULT_PREF_AUTORUN);
		String value = autoRunVal.equals(DEFAULT_PREF_AUTORUN) ? getString(R.string.autoRun_on)
				: getString(R.string.autoRun_off);
		setSummaries(PREF_AUTORUN, value);
	}

	private String getGpsOnOffSummary() {
		// TODO Auto-generated method stub
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean providerEnabled = locationManager.isProviderEnabled("gps");
		mSharedPreferences.edit().putBoolean(PREF_GPSONOFF, providerEnabled)
				.commit();
		return providerEnabled ? getString(R.string.gpsOnOff_summaryOn)
				: getString(R.string.gpsOnOff_summaryOff);
	}


//	/***
//	 * 如果用户名，密码，服务器有一个为空就不重启 上述4个选项不为空进行修改时重启。 此函数判断是否需要重启。
//	 * */
//	private boolean isModify() {
//		return (!settings.getString(PREF_USERNAME, DEFAULT_USERNAME).equals("")
//				&& !settings.getString(PREF_PASSWORD, DEFAULT_PASSWORD).equals(
//						"") && !settings.getString(PREF_SERVER, DEFAULT_SERVER)
//				.equals(""));
//	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		isStarted = true;
		super.onStart();
	}

	@Override
	protected void onStop() {
		isStarted = false;
		Editor edit = settings.edit();
		edit.commit();

		super.onStop();
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		String port = transferText.getText().toString();
		if ((port.length() > 5)
				|| (!port.equals("") && Integer.valueOf(port) > 65535)
				|| (!port.equals("") && Integer.valueOf(port) < 1)) {
			MyToast.showToast(true, Settings.this, "请输入1-65535之间的正确端口号");
			Editor edit = settings.edit();
			edit.putString(mKey, DEFAULT_PORT);
			edit.commit();
			if (mDialog != null) {
				mDialog.cancel();
				mDialog = null;
			}
			return;
		}
		if (port.equals("")) {
			MyToast.showToast(true, Settings.this, "端口号不能为空");
			Editor edit = settings.edit();
			edit.putString(mKey, DEFAULT_PORT);
			edit.commit();
			if (mDialog != null) {
				mDialog.cancel();
				mDialog = null;
			}
			return;
		}
		if (Integer.valueOf(port) > 0 && Integer.valueOf(port) < 65536) {
			Editor edit = settings.edit();
			edit.putString(mKey, transferText.getText().toString());
			edit.commit();
		}
		
	}
	
	//add by oumogang 2013-05-28
	public static String getUserName() {
		// TODO Auto-generated method stub
		if (mUserName == null) {
			mUserName = SipUAApp.mContext.getSharedPreferences(Settings.sharedPrefsFile, Context.MODE_PRIVATE).getString(Settings.PREF_USERNAME,  Settings.DEFAULT_USERNAME);
		}
		return mUserName;
	}
	public static String getPassword(){
		if (mPassword == null) {
			mPassword = SipUAApp.mContext.getSharedPreferences(Settings.sharedPrefsFile, Context.MODE_PRIVATE).getString(Settings.PREF_PASSWORD,  Settings.DEFAULT_PASSWORD);
		}
		return mPassword;
	}
}