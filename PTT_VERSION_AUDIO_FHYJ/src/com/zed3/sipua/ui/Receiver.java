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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.zoolu.sip.provider.SipProvider;
import org.zoolu.tools.MyLog;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.BluetoothPaControlUtil;
import com.zed3.bluetooth.MyPhoneStateListener;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.flow.FlowRefreshService;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.media.Bluetooth;
import com.zed3.media.RtpStreamReceiver_group;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.media.RtpStreamSender_group;
import com.zed3.media.RtpStreamSender_signal;
import com.zed3.sipua.CallHistoryDatabase;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SipdroidEngine;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.phone.Call;
import com.zed3.sipua.phone.Connection;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LanguageChange;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RestoreReceiver;
import com.zed3.utils.Zed3Log;

public class Receiver extends BroadcastReceiver {
	public static final String NULLSTR = "--";
	// added by yangjian 接收PTT广播
	final static String ACTION_PTT_DOWN = "android.intent.action.PTT.down";
	final static String ACTION_PTT_UP = "android.intent.action.PTT.up";

	final static String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
	final static String ACTION_SIGNAL_STRENGTH_CHANGED = "android.intent.action.SIG_STR";
	final static String ACTION_DATA_STATE_CHANGED = "android.intent.action.ANY_DATA_STATE";
	final static String ACTION_DOCK_EVENT = "android.intent.action.DOCK_EVENT";
	final static String EXTRA_DOCK_STATE = "android.intent.extra.DOCK_STATE";
	final static String ACTION_SCO_AUDIO_STATE_CHANGED = "android.media.SCO_AUDIO_STATE_CHANGED";
	final static String EXTRA_SCO_AUDIO_STATE = "android.media.extra.SCO_AUDIO_STATE";
	final static String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	final static String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	final static String ACTION_DEVICE_IDLE = "com.android.server.WifiManager.action.DEVICE_IDLE";
	final static String ACTION_VPN_CONNECTIVITY = "vpn.connectivity";
	final static String ACTION_EXTERNAL_APPLICATIONS_AVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
	final static String ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
	final static String METADATA_DOCK_HOME = "android.dock_home";
	final static String CATEGORY_DESK_DOCK = "android.intent.category.DESK_DOCK";
	final static String CATEGORY_CAR_DOCK = "android.intent.category.CAR_DOCK";
	final static String ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
	final static int EXTRA_DOCK_STATE_DESK = 1;
	final static int EXTRA_DOCK_STATE_CAR = 2;

	public final static int MWI_NOTIFICATION = 1;
	public final static int CALL_NOTIFICATION = 2;
	public final static int MISSED_CALL_NOTIFICATION = 3;
	public final static int AUTO_ANSWER_NOTIFICATION = 4;
	public final static int REGISTER_NOTIFICATION = 5;
	
	
	//add by hu
	public static boolean hasIntent = false;
	public static boolean viewInvisible = false;

	final int MSG_SCAN = 1;
	final int MSG_ENABLE = 2;

	final static long[] vibratePattern = { 0, 1000, 1000 };

	public static int docked = -1, headset = -1, bluetooth = -1;
	public static SipdroidEngine mSipdroidEngine;

	public static final Context mContext;
	public static SipdroidListener listener_video;
	public static Call ccCall;
	public static Connection ccConn;
	public static int call_state;
	public static int call_end_reason = -1;

	public static String pstn_state;
	public static long pstn_time;
	public static String MWI_account;
	private static String laststate, lastnumber;

	// add by yangjian
	private static String call_incoming; // 存储来电
	private static int call_inState; // 存储来电状态
	private static long callBeginTime;

	//避免mContext为空；
	static {
		mContext = SipUAApp.getAppContext();
	}

	public static synchronized SipdroidEngine engine(Context context) {
		// Context is null when init intent,use application context instead
		// modify by oumogang 2014-05-20
		/*
		 Caused by: java.lang.NullPointerException
           at android.content.ComponentName.<init>(ComponentName.java:75)
           at android.content.Intent.<init>(Intent.java:3677)
           at com.zed3.sipua.ui.Receiver.engine(Receiver.java:173)
		 */
//		mContext = context;
		if (mSipdroidEngine == null) {
			mSipdroidEngine = new SipdroidEngine();
			mSipdroidEngine.StartEngine();
			if (Integer.parseInt(Build.VERSION.SDK) >= 8)
				Bluetooth.init();
			// context.startService(new Intent(context, RegisterService.class));
			mContext.startService(new Intent(mContext, RegisterService.class));
		} else
			mSipdroidEngine.CheckEngine();
		return mSipdroidEngine;
	}

	public static MediaPlayer mMediaPlayer;
//	= new MediaPlayer();
//	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//	mMediaPlayer.setDataSource(this, alert);
//	mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
//	public static Ringtone oRingtone;
	static PowerManager.WakeLock wl;

	public static void stopRingtone() {
		if (v != null) {
			v.cancel();
			v = null;
		}
//		if (Receiver.oRingtone != null) {
//			Ringtone ringtone = Receiver.oRingtone;
//			oRingtone = null;
		if (Receiver.mMediaPlayer != null) {
			//Delete by zzhan 2014-11-04
			/*
			MediaPlayer mp = Receiver.mMediaPlayer;
			mp.stop();
			mp = null;
			*/
			//Add by zzhan 2014-11-04
			try {
				Receiver.mMediaPlayer.stop();
			} catch (Exception e) {}
			Receiver.mMediaPlayer.release();
			Receiver.mMediaPlayer = null;
			
			//process in Receiver#onState() instead of here. modify by mou 2014-12-30
//			//set mode ,add by oumogang 2014-03-07
//			if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!=null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
//				if (call_state == UserAgent.UA_STATE_IDLE) {
//					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
//				}
//			}else {			
//			// use AudioUtil.MODE_HOOK instead of  AudioUtil.MODE_SPEAKER  for call
//				AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
//			}
		}
	}

	static android.os.Vibrator v;
	// Added by zzhan 2011-9-15
	static UserAgent ua;
	public static String USERNUMBER = "userNumber";
	public static String USERNAME = "userName";
	private static String HANDLER = "handler";
	public static int mCallState = -1;
	private static boolean mIsCallIn;
	private static boolean mHasInCalled;
	public static boolean mIsRejectedByUser;
	
	public static UserAgent GetCurUA() {
		ua = engine(mContext).GetCurUA();
		return ua;
	}

	//disable by oumoang 2013-05-21
//	public static void saveCallHistory() {
//		// if (call_incoming != null) {
//		switch (call_inState) {
//		case UserAgent.UA_STATE_INCOMING_CALL:
//			savePrtCallUnAc(call_incoming);
//			break;
//		case UserAgent.UA_STATE_INCALL:
//			if (callBeginTime != 0) {
//				saveCallTimeEnd(callBeginTime);
//			}
//			break;
//		case UserAgent.UA_STATE_IDLE:
//			if (callBeginTime != 0) {
//				saveCallTimeEnd(callBeginTime); 
//			}
//			break;
//		 }
////		}
//
//		call_incoming = null;
//		call_inState = UserAgent.UA_STATE_IDLE;
//	}


	// modify by oumogang 2013-05-21
	// added by yangjian --save call In unAccepted
	public static void savePrtCallUnAc(String phoneNum) {
		if(AntaCallUtil.isAntaCall)//会议
		{
			return;
		}
		String name;
		name = CallUtil.mName;
		phoneNum = CallUtil.mNumber;
		// guojunfeng code 20121128
		SimpleDateFormat formatter = new SimpleDateFormat(
				" yyyy-MM-dd HH:mm:ss ");
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// 获取当前时
		String strTime = formatter.format(curDate);
		CallHistoryDatabase db =CallHistoryDatabase.getInstance(mContext);
		ContentValues value = new ContentValues();
		value.put("name", name);
		value.put("number", phoneNum);
		value.put("begin", systemTime);
		value.put("begin_str", strTime);
		value.put("type", "CallUnak");
		db.insert("call_history", value);

	}

	//add by oumogang 2013-05-21
	//save call out history
	public static void savePrtCallOut(String number) {
		if(AntaCallUtil.isAntaCall)//会议
		{
			return ;
		}
		// add by oumogang 2013-05-05
		String name;
		name = CallUtil.mName;
		number = CallUtil.mNumber;
		SimpleDateFormat formatter = new SimpleDateFormat(
				" yyyy-MM-dd HH:mm:ss ");
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// 获取当前时
		String strTime = formatter.format(curDate);
		CallHistoryDatabase db = CallHistoryDatabase.getInstance(mContext);
		ContentValues value = new ContentValues();
		//name add by oumogang 2013-05-05
		//存储名称，本地优先，号码其次；
		value.put("name", name);
		value.put("number", number);
		value.put("begin", systemTime);
		value.put("begin_str", strTime);
//		value.put("type", "CallUnout");
		value.put("type", "CallOut");
		db.insert("call_history", value);
		callBeginTime = systemTime;

	}
	//add by oumogang 2013-05-21
	//save call out history
	public static void savePrtCallUnOut(String number) {
		if(AntaCallUtil.isAntaCall)//会议
		{
			return;
		}
		// add by oumogang 2013-05-05
		String name;
		
		name = CallUtil.mName;
		number = CallUtil.mNumber;
		SimpleDateFormat formatter = new SimpleDateFormat(
				" yyyy-MM-dd HH:mm:ss ");
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// 获取当前时
		String strTime = formatter.format(curDate);
		CallHistoryDatabase db = CallHistoryDatabase.getInstance(mContext);
		ContentValues value = new ContentValues();
		//name add by oumogang 2013-05-05
		//存储名称，本地优先，号码其次；
		value.put("name", name);
		value.put("number", number);
		value.put("begin", systemTime);
		value.put("begin_str", strTime);
		value.put("type", "CallUnout");
//		value.put("type", "CallOut");
		db.insert("call_history", value);
		callBeginTime = systemTime;
		
	}
	//保存来电信息     add by oumogang 2013-05-05
	private static void savePrtCallIn(String phoneNum) {
		if(AntaCallUtil.isAntaCall)//会议状态
		{
			return;
		}
		String name;
		name = CallUtil.mName;
		phoneNum = CallUtil.mNumber;
		
		SimpleDateFormat formatter = new SimpleDateFormat(
				" yyyy-MM-dd HH:mm:ss ");
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// 获取当前时
		String strTime = formatter.format(curDate);
		CallHistoryDatabase db =CallHistoryDatabase.getInstance(mContext);
		ContentValues value = new ContentValues();
		value.put("name", name);
		value.put("number", phoneNum);
		value.put("begin", systemTime);
		value.put("begin_str", strTime);
		value.put("type", "CallIn");
		db.insert("call_history", value);
		callBeginTime = systemTime;
	}

	// added by yangjian ---save call in history
	public static void saveCallTimeEnd(long time) {
		if(AntaCallUtil.isAntaCall)
			return;
		if (time != 0) {
			CallHistoryDatabase db = CallHistoryDatabase.getInstance(mContext);
			String where = "begin= " + time;
			ContentValues value = new ContentValues();
			long end = System.currentTimeMillis();
			value.put("end", end);
			db.update("call_history", where, value);
			callBeginTime = 0;
		}
	}
	// added by yangjian ---save call in history
	public static void saveUnAcceptCall(long time) {
		if (time != 0) {
			CallHistoryDatabase db = CallHistoryDatabase.getInstance(mContext);
			String where = "begin= " + time;
			ContentValues value = new ContentValues();
//			long end = System.currentTimeMillis();
//			value.put("end", end);
			value.put("type", "CallUnout");
			db.update("call_history", where, value);
			callBeginTime = 0;
		}
	}
	// added by yangjian ---save call in history
	public static void saveCallTimeBegin(long time) {
		if (time != 0) {
			CallHistoryDatabase db = CallHistoryDatabase.getInstance(mContext);
			String where = "begin= " + time;
			ContentValues value = new ContentValues();
			long begin = System.currentTimeMillis();
			value.put("begin", begin);
			db.update("call_history", where, value);
			callBeginTime = 0;
		}
		
	}

	public static void onState(int state, String caller) {

		if (ccCall == null) {
			ccCall = new Call();
			ccConn = new Connection();
			ccCall.setConn(ccConn);
			ccConn.setCall(ccCall);
		}
		if (call_state != state) {
			if (state != UserAgent.UA_STATE_IDLE)
				call_end_reason = -1;
			call_state = state;
			switch (call_state) {
			// 电话呼入到响铃的过程
			case UserAgent.UA_STATE_INCOMING_CALL: // 来电
				Zed3Log.debug("testcrash", "Receiver#onState() enter incoming call");
				BluetoothPaControlUtil.setPaOn(SipUAApp.mContext,true);
				//add by oumogang 2013-05-21
				//表示电话类型
				mIsRejectedByUser = false;
				mIsCallIn = true;
				enable_wifi(true);
				if(Receiver.GetCurUA().IsPttMode()){
					RtpStreamReceiver_group.good = RtpStreamReceiver_group.lost = RtpStreamReceiver_group.loss = RtpStreamReceiver_group.late = 0;
					RtpStreamReceiver_group.speakermode = speakermode();
				}else{
					RtpStreamReceiver_signal.good = RtpStreamReceiver_signal.lost = RtpStreamReceiver_signal.loss = RtpStreamReceiver_signal.late = 0;
					RtpStreamReceiver_signal.speakermode = speakermode();
				}
				bluetooth = -1;
				String text = caller.toString();
				if (text.indexOf("<sip:") >= 0 && text.indexOf("@") >= 0)
					text = text.substring(text.indexOf("<sip:") + 5,
							text.indexOf("@"));
				String text2 = caller.toString();
				if (text2.indexOf("\"") >= 0)
					text2 = text2.substring(text2.indexOf("\"") + 1,
							text2.lastIndexOf("\""));
				//Add by zzzhan 2013-5-9
				else
					text2 = text;
				
				broadcastCallStateChanged("RINGING", caller);

				// save incoming call yangjian
				// if (!ua.IsPttMode())
				// {
				call_incoming = text; // 存储状态为来电
				call_inState = UserAgent.UA_STATE_INCOMING_CALL;
				// savePrtCallIn(caller);
				// }

				// 关闭所有正在显示的对话框？
				mContext.sendBroadcast(new Intent(
						Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
				ccCall.setState(Call.State.INCOMING);
				ccConn.setUserData(null);
				ccConn.setAddress(text, text2);
				ccConn.setIncoming(true);
				ccConn.date = System.currentTimeMillis();
				ccCall.base = 0;
				
				//modify by oumogang 2013-05-16
				//add by oumogang 2013-05-08
				//更新名称和号码
//				CallUtil.mName = text2;
//				CallUtil.mNumber = text;
				CallUtil.initNameAndNumber(text, text2);
				
//				savePrtCallIn(CallUtil.mNumber);
				
				//sim卡电话来电中、去电中或通话中，voip禁止去电，拒接来电；  add by mou 2014-10-08
				if (MyPhoneStateListener.getInstance().isInCall()) {
					ZMBluetoothManager.getInstance().writeLog2File("Receiver.onState() UA_STATE_INCOMING_CALL  MyPhoneStateListener#isInCall() is true reject call");
					CallUtil.rejectCall();
					return;
				}
				
				AudioManager am = (AudioManager) mContext
						.getSystemService(Context.AUDIO_SERVICE);
				int rm = am.getRingerMode();
				int vs = am.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);

				// 获得音频信号和震动？
				KeyguardManager mKeyguardManager = (KeyguardManager) mContext
						.getSystemService(Context.KEYGUARD_SERVICE);
				if (v == null)
					v = (Vibrator) mContext
							.getSystemService(Context.VIBRATOR_SERVICE);

				// Modified by zzhan 2011-9-15
				// Added by zzhan 2011-9-15
				ua = engine(mContext).GetCurUA();

				// 查看设置是否打开震动和铃声以及是否锁定键盘
				// if (!ua.IsPttMode()){
				if ((pstn_state == null || pstn_state.equals("IDLE"))
						&& PreferenceManager.getDefaultSharedPreferences(
								mContext).getBoolean(
								com.zed3.sipua.ui.Settings.PREF_AUTO_ON,
								com.zed3.sipua.ui.Settings.DEFAULT_AUTO_ON)
						&& !mKeyguardManager.inKeyguardRestrictedInputMode())
					v.vibrate(vibratePattern, 1);
				else {
					if ((pstn_state == null || pstn_state.equals("IDLE"))
							&& (rm == AudioManager.RINGER_MODE_VIBRATE || (rm == AudioManager.RINGER_MODE_NORMAL && vs == AudioManager.VIBRATE_SETTING_ON)))
						v.vibrate(vibratePattern, 1);
					if (am.getStreamVolume(AudioManager.STREAM_RING) > 0) {
						String sUriSipRingtone = PreferenceManager
								.getDefaultSharedPreferences(mContext)
								.getString(
										com.zed3.sipua.ui.Settings.PREF_SIPRINGTONE,
										Settings.System.DEFAULT_RINGTONE_URI
												.toString());
						if (!TextUtils.isEmpty(sUriSipRingtone)) {
//							oRingtone = RingtoneManager.getRingtone(mContext,
//									Uri.parse(sUriSipRingtone));
							mMediaPlayer = new MediaPlayer();
							//Delete by zzhan 2013-5-14
							/*
							if (oRingtone != null)
								// guojunfeng add 2012-11-22 for QA提出铃音bug
								am.setMode(AudioManager.MODE_NORMAL);
								*/
							//set mode ,add by oumogang 2014-03-07
							AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_RINGTONE);
							try {
								mMediaPlayer.setDataSource(mContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
							mMediaPlayer.setLooping(true);
							try {
								mMediaPlayer.prepare();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							mMediaPlayer.start();
							
						}
					}
				}
				// }
				if (UserAgent.ServerCallType == 0) {// 语音
					// 创建来电呼叫的页面并置顶
					String userName = text2;
					String userNumber = call_incoming;
					moveTop(IN, userNumber, userName);
				} else if (UserAgent.ServerCallType == 1)
					moveTop();

				// 创建来电呼叫的页面并置顶
				//String userName = call_incoming;
//				String userName = text2;
//				String userNumber = call_incoming;
//				moveTop(IN, userNumber, userName);

				// 查看电源状态
				if (wl == null) {
					PowerManager pm = (PowerManager) mContext
							.getSystemService(Context.POWER_SERVICE);
					wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP,
							"SipUAApp.onState");
				}

				// 解锁
				wl.acquire();

				// 自启动一个url线程
//				Checkin.checkin(true);
				break;
			// 呼出的过程
			case UserAgent.UA_STATE_OUTGOING_CALL:
				BluetoothPaControlUtil.setPaOn(SipUAApp.mContext,true);
				//set mode ,add by oumogang 2014-03-07
				if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!=null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
				}else if (SipUAApp.isHeadsetConnected) {
					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
				}else {
					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
				}
				//add by oumogang 2013-05-21
				//保存记录，呼叫时间
//				savePrtCallOut(CallUtil.mNumber);
				mIsCallIn = false;
				if(Receiver.GetCurUA().IsPttMode()){
					RtpStreamReceiver_group.good = RtpStreamReceiver_group.lost = RtpStreamReceiver_group.loss = RtpStreamReceiver_group.late = 0;
					RtpStreamReceiver_group.speakermode = speakermode();
				}else{
					RtpStreamReceiver_signal.good = RtpStreamReceiver_signal.lost = RtpStreamReceiver_signal.loss = RtpStreamReceiver_signal.late = 0;
					RtpStreamReceiver_signal.speakermode = speakermode();
				}
				bluetooth = -1;
				onText(MISSED_CALL_NOTIFICATION, null, 0, 0);
//				engine(mContext).registerUdp();
				broadcastCallStateChanged("OFFHOOK", caller);
				ccCall.setState(Call.State.DIALING);
				ccConn.setUserData(null);
				ccConn.setAddress(caller, caller);
				ccConn.setIncoming(false);
				ccConn.date = System.currentTimeMillis();
				ccCall.base = 0;
				
				//add by oumogang 2013-05-08
				//名称与号码
//				CallUtil.mName = caller;
//				CallUtil.mNumber = caller;
				String outNumber = caller.toString();
				if (mSipdroidEngine.isMakeVideoCall == 0) {// 主动呼叫应用 语音
					moveTop(OUT, outNumber, outNumber);
				} else if (mSipdroidEngine.isMakeVideoCall == 1)
				{
					CallUtil.initNameAndNumber(outNumber, outNumber);
					moveTop();
				}
//				Checkin.checkin(true);
				break;

			// 对方挂断
			case UserAgent.UA_STATE_IDLE:
				BluetoothPaControlUtil.setPaOn(SipUAApp.mContext,false);
				stopRingtone();
				//add by oumogang 2013-05-21
				//如果没有进入通话，就保存未接来电，否则就保存结束时间；
				if (mIsCallIn && !mHasInCalled) {// 未接来电
					// 非用户主动挂断为未接电话，发送广播更新UI modify by liangzhang 2014-11-03
					if (!mIsRejectedByUser) {
						savePrtCallUnAc(CallUtil.mNumber);
						SipUAApp.mContext.sendBroadcast(new Intent(Contants.ACTION_HISTORY_CHANGED));
					} else {
						savePrtCallIn(CallUtil.mNumber);
					}
					if (GroupCallUtil.getGroupCallState() == GroupCallUtil.STATE_SHUTDOWN) {
						AudioUtil.getInstance().setSpeakerphoneOn(false);
						AudioUtil.getInstance().setMode(AudioManager.MODE_NORMAL);
					}else if (SipUAApp.isHeadsetConnected) {
						AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
					}else {
						AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
					}
				} else if (!mIsCallIn && !mHasInCalled) {// 未通去电
					savePrtCallUnOut(CallUtil.mNumber);
				} else {// 已接
					saveCallTimeEnd(callBeginTime);
				}
				mIsRejectedByUser = false;
				SipUAApp.mContext.sendBroadcast(new Intent("sipdroid.history.fresh"));
				mHasInCalled = false;
				
				broadcastCallStateChanged("IDLE", null);
				onText(CALL_NOTIFICATION, null, 0, 0);
				ccCall.setState(Call.State.DISCONNECTED);
				if (listener_video != null)
					listener_video.onHangup();
//				stopRingtone();
				if (wl != null && wl.isHeld())
					wl.release();

				// if (!ua.IsPttMode())
				// {
//				saveCallHistory();
				// }

				// Added by zzhan 2011-9-15
				ua = engine(mContext).GetCurUA();
				//del by hdf 
				// if (UserAgent.ServerCallType == 0 &&
				// mSipdroidEngine.isMakeVideoCall==0) {//语音
				sendBroadcast4Cal(UserAgent.UA_STATE_IDLE);
				// } else {
				Intent broadcastIntent = new Intent(ACTION_CALL_END);
				Receiver.mContext.sendBroadcast(broadcastIntent);
				// }
				//恢复初始值
				UserAgent.ServerCallType =-1;
				UserAgent.VideoEncodeType =-1;
				
				ccConn.log(ccCall.base);
				ccConn.date = 0;
				engine(mContext).listen();
				
				//add by oumogang 2013-05-08
				//恢复名称
//				CallUtil.mName = "";
//				CallUtil.mNumber = "";
				CallUtil.reInit();
				AntaCallUtil.reInit();
				CallActivity.resetCallParams();
				

				//清空会议发起时间 add by liangzhang 2014-11-27
				Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
				editor.putString("AntaCallCreateTime", "").commit();
				break;
			// guojunfeng 呼入电话接通
			case UserAgent.UA_STATE_INCALL: // 对方接听
				//add by oumoang 2013-05-21
				//the begintime for CallActivity2  
				CallUtil.mCallBeginTime = System.currentTimeMillis();
				//add by lwang 2014-10-31
				CallUtil.mCallBeginTime2 = System.currentTimeMillis();
				mHasInCalled = true;
				// add by guojunfeng 存储接通时间点
				// SharedPreferences endShare = mContext.getSharedPreferences(
				// "PrtCallInHistory_begin", Activity.MODE_PRIVATE);
				// SharedPreferences.Editor endEditor = endShare.edit();
				// endEditor.remove(call_incoming);
				// endEditor.putLong(call_incoming, System.currentTimeMillis());
				// endEditor.commit();
				

				//add by oumogang 2013-05-21
				//重新记录通话开始时间 
				saveCallTimeBegin(callBeginTime);
				if (mIsCallIn) {
					savePrtCallIn(CallUtil.mNumber);
				}else {
					savePrtCallOut(CallUtil.mNumber);
				}
				// disable by oumogang 2013-05-21
//				if (call_inState == UserAgent.UA_STATE_OUTGOING_CALL) {
//					SimpleDateFormat formatter = new SimpleDateFormat(
//							" yyyy-MM-dd HH:mm:ss ");
//					long systemTime = System.currentTimeMillis();
//					CallHistoryDatabase db = new CallHistoryDatabase(mContext);
//					ContentValues value = new ContentValues();
//					value.put("begin", systemTime);
//					value.put("type", "CallOut");
//					String where = "begin= " + callBeginTime;
//					db.update("call_history", where, value);
//					callBeginTime = systemTime;
//				}

				// Add by zzhan 2012-03-21
				// guojunfeng注释
				// ua = engine(mContext).GetCurUA();
				// if (!ua.IsPttMode()){
				// Receiver.engine(mContext).speaker(AudioManager.MODE_NORMAL);
				// }

				broadcastCallStateChanged("OFFHOOK", null);
				if (ccCall.base == 0) {
					ccCall.base = SystemClock.elapsedRealtime();
				}
				// added by yangjian
				if (/* !ua.IsPttMode() && */call_inState == UserAgent.UA_STATE_INCOMING_CALL) {
					call_inState = UserAgent.UA_STATE_INCALL;
				}

				progress();
				ccCall.setState(Call.State.ACTIVE);
				stopRingtone();
				if (wl != null && wl.isHeld())
					wl.release();

				// Added by zzhan 2011-9-15
				ua = engine(mContext).GetCurUA();

				if (UserAgent.ServerCallType == 0 ) // 语音
					sendBroadcast4Cal(UserAgent.UA_STATE_INCALL);
				else if (UserAgent.ServerCallType == 1){
					try {
//						mDialog = creatRequestDialog("正在启动视频");
//						mDialog.show();
//						
						Receiver.mContext
								.startActivity(createIntent(CameraCall.class));
						//((Activity) Receiver.mContext).finish();// del by hdf

					} catch (Exception e) {
						MyLog.e("Receiver error",
								"((Activity) Receiver.mContext).finish() "
										+ e.toString());
						e.printStackTrace();
					}
					// 关闭DemoCallScreen页面
					Receiver.mContext.sendBroadcast(new Intent(
							"android.action.closeDemoCallScreen"));// del by hdf
				}
				

				break;
			// guojunfeng 呼出电话接通
			case UserAgent.UA_STATE_HOLD:
				onText(CALL_NOTIFICATION,
						mContext.getString(R.string.card_title_on_hold),
						android.R.drawable.stat_sys_phone_call_on_hold,
						ccCall.base);
				ccCall.setState(Call.State.HOLDING);

				if (UserAgent.ServerCallType == 0) {// 语音
					mContext.startActivity(createIntent(CallActivity2.class));
				} else {
					mContext.startActivity(createIntent(CameraCall.class));// InCallScreen
				}
				
				break;
			}
			pos(true);
			if(Receiver.GetCurUA().IsPttMode()){
				RtpStreamReceiver_group.ringback(false);
			}else{
				RtpStreamReceiver_signal.ringback(false);
			}
		}
	}

//	public static Dialog mDialog;
//	
//	public static Dialog creatRequestDialog(String tip) {
//		final Dialog dialog = new Dialog(SipUAApp.mContext, R.style.dialog);
//		dialog.setContentView(R.layout.dialog_layout);
//		// Window window = dialog.getWindow();
//		// WindowManager.LayoutParams lp = window.getAttributes();
//		// int width = Utils.getScreenWidth(context);
//		// lp.width = (int) (0.6 * width);
//
//		TextView titleTxtv = (TextView) dialog.findViewById(R.id.tvLoad);
//		titleTxtv.setText(tip);
//		return dialog;
//	}
	
	
	/** send broadcast for CallActivity2 to change state */
	private static void sendBroadcast4Cal(int uaStateIdle) {
		// TODO Auto-generated method stub
		Intent intent;
		if (AntaCallUtil.isAntaCall) {
			intent = new Intent(CallActivity2.ACTION_CHANGE_CALL_STATE);
			Bundle extras = new Bundle();
			extras.putInt(CallActivity2.NEWSTATE, uaStateIdle);
			intent.putExtras(extras);
		} else {
			intent = new Intent(CallActivity.ACTION_CHANGE_CALL_STATE);
			Bundle extras = new Bundle();
			extras.putInt(CallActivity.NEWSTATE, uaStateIdle);
			intent.putExtras(extras);
		}
		//修改集群通退到后台后语音来电不弹通话界面的问题；
		//use SipUAApp.mContext, modify by oumogang 2014-04-05
		SipUAApp.mContext.sendBroadcast(intent);
	}

	private static void moveTop(Boolean isIn, String userNum, String userName) {
		// TODO Auto-generated method stub
		// Added by zzhan 2011-9-15
		ua = engine(mContext).GetCurUA();
		/*
		 * if (ua.IsPttMode()) return;
		 */
		progress();
		
		Intent intent ;
		
		Zed3Log.debug("testcrash", "Receiver#moveTop() enter AntaCallUtil#isAntaCall = " + AntaCallUtil.isAntaCall);
		if (AntaCallUtil.isAntaCall){//会议界面
			//保存会议发起时间 modified by liangzhang 2014-11-27
			Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
			editor.putString("AntaCallCreateTime", AntaCallUtil.mCreateTime()).commit();
			intent = new Intent(mContext, CallActivity2.class);
		} else { //语音界面
			intent = new Intent(mContext, CallActivity.class);
		}
		Bundle extras = new Bundle();

		extras.putBoolean(INOROUT, isIn);
		extras.putString(USERNUMBER, userNum);
		extras.putString(USERNAME, userName);
		// mCallHandler = new CallHandler();
		// extras.putParcelable(HANDLER , mCallHandler);
		// extras.putString(key, value);
		intent.putExtras(extras);
		// Bundle extras = intent.getExtras();
		// intent.putExtra(INOROUT, isIn);
		// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		SipUAApp.mContext.startActivity(intent);
	}

	static String cache_text;
	static int cache_res;

	public static void onText(int type, String text, int mInCallResId, long base) {
		//移动位置，保存登陆成功时候的数据 modify by lwang 2014-10-24
//		SharedPreferences sharedPreferences = Receiver.mContext.getSharedPreferences("notifyInfo", 110);
//		sharedPreferences.edit().putInt("type", type).putString("text", text).putInt("mInCallResId", mInCallResId).putLong("base", base).commit();
		if (mSipdroidEngine != null
				&& type == REGISTER_NOTIFICATION + mSipdroidEngine.pref) {
			cache_text = text;
			cache_res = mInCallResId;
		}
		if (type >= REGISTER_NOTIFICATION
				&& mInCallResId == R.drawable.icon64
				&& !PreferenceManager.getDefaultSharedPreferences(
						Receiver.mContext).getBoolean(
						com.zed3.sipua.ui.Settings.PREF_REGISTRATION,
						com.zed3.sipua.ui.Settings.DEFAULT_REGISTRATION))
			text = null;
		NotificationManager mNotificationMgr = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (text != null) {
			Notification notification = new Notification();
			notification.icon = mInCallResId;
//			MainTabActivity.mTabIndex = MainTabActivity
			if (type == MISSED_CALL_NOTIFICATION) {
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notification.contentIntent = PendingIntent
						.getActivity(mContext, 0, createCallLogIntent(), 0);
				RemoteViews contentView = new RemoteViews(
						mContext.getPackageName(),
						R.layout.ongoing_call_notification);
				contentView.setImageViewResource(R.id.icon, notification.icon);
				contentView.setTextViewText(R.id.text1, text);
				contentView.setTextViewText(R.id.text2, mContext
						.getString(R.string.app_name));
				notification.contentView = contentView;
				if (PreferenceManager.getDefaultSharedPreferences(
						Receiver.mContext).getBoolean(
						com.zed3.sipua.ui.Settings.PREF_NOTIFY,
						com.zed3.sipua.ui.Settings.DEFAULT_NOTIFY)) {
					notification.flags |= Notification.FLAG_SHOW_LIGHTS;
					notification.ledARGB = 0xff0000ff; /* blue */
					notification.ledOnMS = 125;
					notification.ledOffMS = 2875;
				}
			} else {
				switch (type) {
				case MWI_NOTIFICATION:
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					notification.contentIntent = PendingIntent.getActivity(
							mContext, 0, createMWIIntent(), 0);
					notification.flags |= Notification.FLAG_SHOW_LIGHTS;
					notification.ledARGB = 0xff00ff00; /* green */
					notification.ledOnMS = 125;
					notification.ledOffMS = 2875;
					break;
				case AUTO_ANSWER_NOTIFICATION:
					notification.contentIntent = PendingIntent.getActivity(
							mContext, 0, createIntent(AutoAnswer.class), 0);
					break;
				default:
					if (type >= REGISTER_NOTIFICATION
							&& mSipdroidEngine != null
							&& type != REGISTER_NOTIFICATION
									+ mSipdroidEngine.pref
							&& mInCallResId == R.drawable.icon64) {
						notification.contentIntent = PendingIntent.getActivity(
								mContext, 0, createIntent(ChangeAccount.class),
								0);
					}
					// notification.contentIntent =
					// PendingIntent.getActivity(mContext, 0,
					// createIntent(Sipdroid.class), 0);
					else if (type == Receiver.CALL_NOTIFICATION) {
						
						if (call_state == UserAgent.UA_STATE_INCALL) {
							if (UserAgent.ServerCallType == 0){// 语音
								notification.contentIntent = PendingIntent
										.getActivity(
												mContext,
												0,
												createIntent(AntaCallUtil.isAntaCall ? CallActivity2.class:CallActivity.class),// ,
												0);
								MyLog.e("Receiver", "通话中 AudioCall");
							}else {
								notification.contentIntent = PendingIntent
										.getActivity(mContext, 0,
												createIntent(CameraCall.class),// ,
												0);
								MyLog.e("Receiver", "通话中cameracall");
							}
						} else {

							if (call_state == UserAgent.UA_STATE_OUTGOING_CALL
									&& mSipdroidEngine.isMakeVideoCall == 0)
							{
								notification.contentIntent = PendingIntent
										.getActivity(
												mContext,
												0,
												createIntent(AntaCallUtil.isAntaCall ? CallActivity2.class:CallActivity.class),
												0);
								
							}	
							else if (UserAgent.ServerCallType == 0)
							{	
								notification.contentIntent = PendingIntent
										.getActivity(
												mContext,
												0,
												createIntent(AntaCallUtil.isAntaCall ? CallActivity2.class:CallActivity.class),
												0);
								
							}
							else
								// 视频
								notification.contentIntent = PendingIntent
										.getActivity(
												mContext,
												0,
												createIntent(DemoCallScreen.class),
												0);

							MyLog.e("Receiver", "呼叫或来电中。。。");
						}

					} else {
						//use SipUAApp.mContext modify by oumogang 2013-07-17
						Intent restore = new Intent(SipUAApp.mContext,RestoreReceiver.class);
						restore.setAction("com.zed3.restore");
						notification.contentIntent = PendingIntent.getBroadcast(
								SipUAApp.mContext, 0, restore,
								0);//
					}

					if (mInCallResId == R.drawable.sym_presence_away) {
						notification.flags |= Notification.FLAG_SHOW_LIGHTS;
						notification.ledARGB = 0xffff0000; /* red */
						notification.ledOnMS = 125;
						notification.ledOffMS = 2875;
					}

					break;
				}
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				RemoteViews contentView = new RemoteViews(
						mContext.getPackageName(),
						R.layout.ongoing_call_notification);
				contentView.setImageViewResource(R.id.icon, notification.icon);
				if (base != 0) {
					contentView.setChronometer(R.id.text1, base,
							text + " (%s)", true);
				} else if (type >= REGISTER_NOTIFICATION) {
					//当现实登陆状态的时候，要保存数据 add by lwang 2014-10-24
					SharedPreferences sharedPreferences = Receiver.mContext
							.getSharedPreferences("notifyInfo",  Context.MODE_PRIVATE);
					sharedPreferences.edit().putInt("type", type)
							.putString("text", text)
							.putInt("mInCallResId", mInCallResId)
							.putLong("base", base).commit();
					if (PreferenceManager.getDefaultSharedPreferences(mContext)
							.getBoolean(com.zed3.sipua.ui.Settings.PREF_POS,
									com.zed3.sipua.ui.Settings.DEFAULT_POS))
						contentView.setTextViewText(R.id.text2, text + "/"
								+ mContext.getString(R.string.settings_pos3));
					else
						contentView.setTextViewText(R.id.text2, text);
					if (mSipdroidEngine != null){
						String name = DeviceInfo.AutoVNoName;
						String grpName = "";// 对讲组名称
						// delete by liangzhang 2014-09-10
						// 协议升级后不再从联系人或成员列表中获取用户名
						// if (TextUtils.isEmpty(name)) {
						// name = Tools.queryNamebyNum(SipUAApp.mContext,
						// mSipdroidEngine.user_profiles[type
						// - REGISTER_NOTIFICATION].username);
						// }

						if(ua == null){
							ua = engine(mContext).GetCurUA();
						}
						if (ua != null) {
							PttGrp curGrp = ua.GetCurGrp();
							if (curGrp != null) {
								if (!TextUtils.isEmpty(curGrp.grpName)) {
									grpName = curGrp.grpName;
								}
							}
						}
						// modify by liangzhang 2014-09-10 获取并在通知栏显示用户名
						SharedPreferences preferences = SipUAApp.mContext
								.getSharedPreferences(
										com.zed3.sipua.ui.Settings.sharedPrefsFile,
										Context.MODE_PRIVATE);
						// 用户名获取不到时显示用户号码 modify by liangzhang 2014-10-23
						String displayName = preferences.getString("displayname", "");
						if (displayName != null && !displayName.equals("")
								&& !displayName.equalsIgnoreCase("null")) {
							name = displayName;
						} else {
							name = preferences.getString("userName", "");
						}
						if (!grpName.equals("")) {// 对讲组不为空时显示对讲组
							name = name + "@" + grpName;
						}
						contentView.setTextViewText(R.id.text1, name // mofify
																		// by hu
																		// 2014/2/22
								/*
								 * mSipdroidEngine.user_profiles[type -
								 * REGISTER_NOTIFICATION].username
								 */
								/*
								 * + "@" //delete by hu +
								 * mSipdroidEngine.user_profiles[type -
								 * REGISTER_NOTIFICATION].realm_orig
								 */);
					}
				} else{
					contentView.setTextViewText(R.id.text1, text);
					contentView.setTextViewText(R.id.text2, mContext.getResources().getString(R.string.app_name));
				}
				notification.contentView = contentView;
			}
			mNotificationMgr.notify(type, notification);
		} else {
			mNotificationMgr.cancel(type);
		}
		if (type != AUTO_ANSWER_NOTIFICATION)
			updateAutoAnswer();
		if (mSipdroidEngine != null && type >= REGISTER_NOTIFICATION
				&& type != REGISTER_NOTIFICATION + mSipdroidEngine.pref)
			onText(REGISTER_NOTIFICATION + mSipdroidEngine.pref, cache_text,
					cache_res, 0);
	}

	static void updateAutoAnswer() {
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
				com.zed3.sipua.ui.Settings.PREF_AUTO_ONDEMAND,
				com.zed3.sipua.ui.Settings.DEFAULT_AUTO_ONDEMAND)
				&& SipUAApp.on(mContext)) {
			if (PreferenceManager.getDefaultSharedPreferences(mContext)
					.getBoolean(com.zed3.sipua.ui.Settings.PREF_AUTO_DEMAND,
							com.zed3.sipua.ui.Settings.DEFAULT_AUTO_DEMAND))
				updateAutoAnswer(1);
			else
				updateAutoAnswer(0);
		} else
			updateAutoAnswer(-1);
	}

	private static int autoAnswerState = -1;

	static void updateAutoAnswer(int status) {
		/*if (status != autoAnswerState) {
			switch (autoAnswerState = status) {
			case 0:
				Receiver.onText(Receiver.AUTO_ANSWER_NOTIFICATION,
						mContext.getString(R.string.auto_disabled),
						R.drawable.auto_answer_disabled, 0);
				break;
			case 1:
				Receiver.onText(Receiver.AUTO_ANSWER_NOTIFICATION,
						mContext.getString(R.string.auto_enabled),
						R.drawable.auto_answer, 0);
				break;
			case -1:
				Receiver.onText(Receiver.AUTO_ANSWER_NOTIFICATION, null, 0, 0);
				break;
			}
		}*/
	}

	public static void registered() {
		pos(true);
	}

	static LocationManager lm;
	static AlarmManager am;// 为上传gps设置的全局定时器???????
	static PendingIntent gps_sender, net_sender;
	static boolean net_enabled;

	static final int GPS_UPDATES = 4000 * 1000;
	static final int NET_UPDATES = 600 * 1000;
	public static final String INOROUT = "isCallingIn";
	public static final boolean IN = true;
	public static final boolean OUT = false;

	// gps?????
	public static void pos(boolean enable) {
		if (lm == null)
			lm = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
		if (am == null)
			am = (AlarmManager) mContext
					.getSystemService(Context.ALARM_SERVICE);
		pos_gps(false);
		if (enable) {
			if (call_state == UserAgent.UA_STATE_IDLE
					&& SipUAApp.on(mContext)
					&& PreferenceManager.getDefaultSharedPreferences(mContext)
							.getBoolean(com.zed3.sipua.ui.Settings.PREF_POS,
									com.zed3.sipua.ui.Settings.DEFAULT_POS)
					&& PreferenceManager
							.getDefaultSharedPreferences(mContext)
							.getString(com.zed3.sipua.ui.Settings.PREF_POSURL,
									com.zed3.sipua.ui.Settings.DEFAULT_POSURL)
							.length() > 0) {
				Location last = lm
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (last == null
						|| System.currentTimeMillis() - last.getTime() > GPS_UPDATES) {
					pos_gps(true);
					pos_net(false);
				}
				pos_net(true);
			} else
				pos_net(false);
		}
	}

	// gps???????
	static void pos_gps(boolean enable) {
		if (gps_sender == null) {
			Intent intent = new Intent(mContext, OneShotLocation.class);
			gps_sender = PendingIntent.getBroadcast(mContext, 0, intent, 0);
		}
		if (enable) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					GPS_UPDATES, 3000, gps_sender);
			if (isXiaoMI()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, 10);
			am.set(AlarmManager.RTC,
					calendar.getTimeInMillis(), gps_sender);
			}else{
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						SystemClock.elapsedRealtime() + 10 * 1000, gps_sender);
			}
		} else {
			am.cancel(gps_sender);
			lm.removeUpdates(gps_sender);
		}
	}

	static void pos_net(boolean enable) {
		if (net_sender == null) {
			Intent loopintent = new Intent(mContext, LoopLocation.class);
			net_sender = PendingIntent.getBroadcast(mContext, 0, loopintent, 0);
		}
		if (net_enabled != enable) {
			if (enable) {
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
						NET_UPDATES, 3000, net_sender);
			} else {
				lm.removeUpdates(net_sender);
			}
			net_enabled = enable;
		}
	}

	static void enable_wifi(boolean enable) {
		if (!PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(com.zed3.sipua.ui.Settings.PREF_OWNWIFI,
						com.zed3.sipua.ui.Settings.DEFAULT_OWNWIFI))
			return;
		if (enable
				&& !PreferenceManager
						.getDefaultSharedPreferences(mContext)
						.getBoolean(
								com.zed3.sipua.ui.Settings.PREF_WIFI_DISABLED,
								com.zed3.sipua.ui.Settings.DEFAULT_WIFI_DISABLED))
			return;
		WifiManager wm = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		ContentResolver cr = Receiver.mContext.getContentResolver();
		if (!enable
				&& Settings.Secure.getInt(cr, Settings.Secure.WIFI_ON, 0) == 0)
			return;
		Editor edit = PreferenceManager.getDefaultSharedPreferences(
				Receiver.mContext).edit();

		edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_WIFI_DISABLED, !enable);
		edit.commit();
		if (enable) {
			Intent intent = new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intent.putExtra(WifiManager.EXTRA_NEW_STATE, wm.getWifiState());
			mContext.sendBroadcast(intent);
		}
		wm.setWifiEnabled(enable);
	}

	public static void url(final String opt) {
		(new Thread() {
			public void run() {
				try {
					URL url = new URL(PreferenceManager
							.getDefaultSharedPreferences(mContext).getString(
									com.zed3.sipua.ui.Settings.PREF_POSURL,
									com.zed3.sipua.ui.Settings.DEFAULT_POSURL)
							+ "?" + opt);
					BufferedReader in;
					in = new BufferedReader(new InputStreamReader(
							url.openStream()));
					in.close();
				} catch (IOException e) {
					if (!Sipdroid.release)
						e.printStackTrace();
				}

			}
		}).start();
	}

	static boolean was_playing;

	static void broadcastCallStateChanged(String state, String number) {
		if (state == null) {
			state = laststate;
			number = lastnumber;
		}
//		Intent intent = new Intent(ACTION_PHONE_STATE_CHANGED);
//		intent.putExtra("state", state);
//		if (number != null)
//			intent.putExtra("incoming_number", number);
//		intent.putExtra(mContext.getString(R.string.app_name), true);
//		mContext.sendBroadcast(intent,
//				android.Manifest.permission.READ_PHONE_STATE);
		if (state.equals("IDLE")) {
			if (was_playing) {
				if (pstn_state == null || pstn_state.equals("IDLE"))
					mContext.sendBroadcast(new Intent(TOGGLEPAUSE_ACTION));
				was_playing = false;
			}
		} else {
			AudioManager am = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
			if ((laststate == null || laststate.equals("IDLE"))
					&& (was_playing = am.isMusicActive()))
				mContext.sendBroadcast(new Intent(PAUSE_ACTION));
		}
		laststate = state;
		lastnumber = number;
	}

	// edit by hdf
	// 全局定时器 AlarmManager
	public static void alarm(int renew_time, Class<?> cls) {
		if (!Sipdroid.release)
			Log.i("SipUA:", "alarm " + renew_time);
		LogUtil.makeLog(tag, "alarm("+renew_time+","+cls+")");
		Intent intent = new Intent(mContext, cls);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,
				0);

		AlarmManager am = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);

		// 如果有时间间隔 则开启定时器
		if (renew_time > 0) {
			if (isXiaoMI()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, renew_time);
			am.set(AlarmManager.RTC,
					calendar.getTimeInMillis(), sender);
			}else{
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						SystemClock.elapsedRealtime() + renew_time * 1000, sender);
			}

		} else {
			// 取消AlarmManager（其中需要注意的是取消的Intent必须与启动Intent保持绝对一致才能支持取消AlarmManager）
			am.cancel(sender);
		}
	}

	public static long expire_time;

	public static synchronized void reRegister(int renew_time) {
		if (renew_time == 0)
			expire_time = 0;
		else {
			if (expire_time != 0
					&& renew_time * 1000 + SystemClock.elapsedRealtime() > expire_time)
				return;
			expire_time = renew_time * 1000 + SystemClock.elapsedRealtime();
		}
		alarm(renew_time - 15, OneShotAlarm.class);
	}

	static Intent createIntent(Class<?> cls) {
		Intent startActivity = new Intent();

		// 当外部调用来电界面时，需要设置Flags标记FLAG_ACTIVITY_NEW_TASK
		startActivity.setClass(mContext, cls);
		startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return startActivity;
	}

	public static Intent createCallLogIntent() {
		// guojunfeng 此处跳转到系统通话记录改为跳转到本应用的通话记录
		// Intent intent = new Intent(Intent.ACTION_VIEW, null);
		// intent.setType("vnd.android.cursor.dir/calls");
		// return intent;
//		Intent intent = new Intent(mContext, com.zed3.sipua.ui.MainTabActivity.class);
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.putExtra("other_intent", "SipdroidActivity");
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.putExtra("Tab", "History");
		return intent;
	}

	static Intent createHomeDockIntent() {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		if (docked == EXTRA_DOCK_STATE_CAR) {
			intent.addCategory(CATEGORY_CAR_DOCK);
		} else if (docked == EXTRA_DOCK_STATE_DESK) {
			intent.addCategory(CATEGORY_DESK_DOCK);
		} else {
			return null;
		}

		ActivityInfo ai = intent.resolveActivityInfo(
				mContext.getPackageManager(), PackageManager.GET_META_DATA);
		if (ai == null) {
			return null;
		}

		if (ai.metaData != null && ai.metaData.getBoolean(METADATA_DOCK_HOME)) {
			intent.setClassName(ai.packageName, ai.name);
			return intent;
		}

		return null;
	}

	public static Intent createHomeIntent() {
		Intent intent = createHomeDockIntent();
		if (intent != null) {
			try {
				return intent;
			} catch (ActivityNotFoundException e) {
			}
		}
		intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_HOME);
		return intent;
	}

	static Intent createMWIIntent() {
		Intent intent;

		if (MWI_account != null)
			intent = new Intent(Intent.ACTION_CALL, Uri.parse(MWI_account
					.replaceFirst("sip:", "sipdroid:")));
		else
			intent = new Intent(Intent.ACTION_DIAL);
		return intent;
	}

	public static void moveTop() {
		// Added by zzhan 2011-9-15
		ua = engine(mContext).GetCurUA();
		/*
		 * if (ua.IsPttMode()) return;
		 */

		progress();
		// mContext.startActivity(createIntent(Activity2.class));

		Intent broadcastIntent = new Intent(
				"android.intent.action.StartDemoCallScreen");
		Receiver.mContext.sendBroadcast(broadcastIntent);

	}

	private static Intent createIntent(Class<CallActivity2> class1, Boolean isIn) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(mContext, CallActivity2.class);
		Bundle extras = new Bundle();
		extras.putBoolean(INOROUT, isIn);
		// extras.putString(key, value);

		// extras.putString(key, value);
		intent.putExtras(extras);
		// Bundle extras = intent.getExtras();
		// intent.putExtra(INOROUT, isIn);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// intent.
		return intent;
	}

	public static void progress() {
		// Added by zzhan 2011-9-15
		ua = engine(mContext).GetCurUA();
		/*
		 * if (ua.IsPttMode()) return;
		 */

		if (call_state == UserAgent.UA_STATE_IDLE)
			return;
		int mode = -1;
		if(Receiver.GetCurUA().IsPttMode()){
			mode = RtpStreamReceiver_group.speakermode;
		}else{
			mode = RtpStreamReceiver_signal.speakermode;
		}
		if (mode == -1)
			mode = speakermode();
		if (mode == AudioManager.MODE_NORMAL){}
//			Receiver.onText(Receiver.CALL_NOTIFICATION,
//					mContext.getString(R.string.menu_speaker),
//					android.R.drawable.stat_sys_speakerphone,
//					Receiver.ccCall.base);
		else if (bluetooth > 0)
			Receiver.onText(Receiver.CALL_NOTIFICATION,
					mContext.getString(R.string.menu_bluetooth),
					R.drawable.stat_sys_phone_call_bluetooth,
					Receiver.ccCall.base);
		else
			Receiver.onText(Receiver.CALL_NOTIFICATION,
					mContext.getString(R.string.card_title_in_progress),
					R.drawable.stat_sys_phone_call, Receiver.ccCall.base);
	}

	public static boolean on_wlan;

	static boolean on_vpn() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(com.zed3.sipua.ui.Settings.PREF_ON_VPN,
						com.zed3.sipua.ui.Settings.DEFAULT_ON_VPN);

	}

	static void on_vpn(boolean enable) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(
				Receiver.mContext).edit();

		edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_ON_VPN, enable);
		edit.commit();
	}

	public static boolean isFast(int i) {
		// Modifieded byzzhan 2011-9-19
		return true;
		/*
		 * WifiManager wm = (WifiManager)
		 * mContext.getSystemService(Context.WIFI_SERVICE); WifiInfo wi =
		 * wm.getConnectionInfo();
		 * 
		 * if
		 * (PreferenceManager.getDefaultSharedPreferences(mContext).getString(
		 * com.zed3.sipua.ui.Settings.PREF_USERNAME+(i!=0?i:""),"").equals("")
		 * ||
		 * PreferenceManager.getDefaultSharedPreferences(mContext).getString(com
		 * .zed3.sipua.ui.Settings.PREF_SERVER+(i!=0?i:""),"").equals(""))
		 * return false; if (wi != null) { if (!Sipdroid.release)
		 * Log.i("SipUA:",
		 * "isFastWifi() "+WifiInfo.getDetailedStateOf(wi.getSupplicantState())
		 * +" "+wi.getIpAddress()); if (wi.getIpAddress() != 0 &&
		 * (WifiInfo.getDetailedStateOf(wi.getSupplicantState()) ==
		 * DetailedState.OBTAINING_IPADDR ||
		 * WifiInfo.getDetailedStateOf(wi.getSupplicantState()) ==
		 * DetailedState.CONNECTED)) { on_wlan = true; if (!on_vpn()) return
		 * PreferenceManager
		 * .getDefaultSharedPreferences(mContext).getBoolean(com
		 * .zed3.sipua.ui.Settings.PREF_WLAN+(i!=0?i:""),
		 * com.zed3.sipua.ui.Settings.DEFAULT_WLAN); else return
		 * PreferenceManager
		 * .getDefaultSharedPreferences(mContext).getBoolean(com
		 * .zed3.sipua.ui.Settings.PREF_VPN+(i!=0?i:""),
		 * com.zed3.sipua.ui.Settings.DEFAULT_VPN); } } on_wlan = false; return
		 * isFastGSM(i);
		 */
	}

	static boolean isFastGSM(int i) {
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (Sipdroid.market)
			return false;
		if (on_vpn()
				&& (tm.getNetworkType() >= TelephonyManager.NETWORK_TYPE_EDGE))
			return PreferenceManager.getDefaultSharedPreferences(mContext)
					.getBoolean(
							com.zed3.sipua.ui.Settings.PREF_VPN
									+ (i != 0 ? i : ""),
							com.zed3.sipua.ui.Settings.DEFAULT_VPN);
		if (tm.getNetworkType() >= TelephonyManager.NETWORK_TYPE_UMTS)
			return PreferenceManager.getDefaultSharedPreferences(mContext)
					.getBoolean(
							com.zed3.sipua.ui.Settings.PREF_3G
									+ (i != 0 ? i : ""),
							com.zed3.sipua.ui.Settings.DEFAULT_3G);
		if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE)
			return PreferenceManager.getDefaultSharedPreferences(mContext)
					.getBoolean(
							com.zed3.sipua.ui.Settings.PREF_EDGE
									+ (i != 0 ? i : ""),
							com.zed3.sipua.ui.Settings.DEFAULT_EDGE);
		return false;
	}

	public static int speakermode() {
		if (docked > 0 && headset <= 0)
			return AudioManager.MODE_NORMAL;
		else
			return AudioManager.MODE_IN_CALL;
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SCAN:
				WifiManager wm = (WifiManager) mContext
						.getSystemService(Context.WIFI_SERVICE);
				wm.startScan();
				break;
			case MSG_ENABLE:
				enable_wifi(true);
				break;
			}
		}
	};
	private static String tag = "Receiver";

	int asu(ScanResult scan) {
		if (scan == null)
			return 0;
		return Math.round((scan.level + 113f) / 2f);
	}

	
	
//	// 读取是否开机启动 配置
//	private String IsAutoRunConfig() {
//		return PreferenceManager.getDefaultSharedPreferences(Receiver.mContext)
//				.getString(com.zed3.sipua.ui.Settings.PREF_AUTORUN, com.zed3.sipua.ui.Settings.DEFAULT_PREF_AUTORUN);
//	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		//GQT英文版 2014-8-29 系统语言改变，更改通知栏状态
		if(intentAction.equals(Intent.ACTION_LOCALE_CHANGED)){
			//GQT英文版 2014-9-4 判断用户的语言是否选择的是跟随系统
			if(mContext.getSharedPreferences(com.zed3.sipua.ui.Settings.sharedPrefsFile, 
					Context.MODE_PRIVATE).getInt("languageId", 0)!=0){
				//接收到系统语言改变的广播之后，如果用户选择的并不是“跟随系统” ，重新进行一次本地资源更新的操作
				LanguageChange.upDateLanguage(mContext);
				return;
			}
			//系统语言改变，将对讲组数据清空
			GroupListUtil.removeDataOfGroupList();
			SharedPreferences sharedPreferences = context.getSharedPreferences("notifyInfo",  Context.MODE_PRIVATE);
			//当程序正常退出之后，如果切换系统语言，不应该更新通知栏,重启应用。 modify by lwang 2014-11-13
			int type = sharedPreferences.getInt("type", -1);
			if (type == -1)
				return ;
			String text = sharedPreferences.getString("text", null);
			int mInCallResId = sharedPreferences.getInt("mInCallResId", 0);
			long base = sharedPreferences.getLong("base", 0);
			if("登录成功".equals(text)||"Login successfully!".equals(text)){
				text = mContext.getResources().getString(R.string.regok);
			}else if("登录失败".equals(text)||"Login failed".equals(text)){
				text = mContext.getResources().getString(R.string.regfailed);
			}else if("正在登录...".equals(text)||"Loginning".equals(text)){
				text = mContext.getResources().getString(R.string.reg);
			}else if("通话中".equals(text)||"In-Call".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_in_progress);
			}else if("来电".equals(text)||"Incoming".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_incoming_call);
			}else if("通话结束".equals(text)||"Call End".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_call_ended);
			}else if("通话保持中".equals(text)||"In the to keep".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_on_hold);
			}else if("拨号中".equals(text)||"Dialing".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_dialing);
			}
			Receiver.onText(type, text, mInCallResId, base);
			//GQT英文版 2014-9-3
			SharedPreferences mSharedPreferences = mContext.getSharedPreferences(com.zed3.sipua.ui.Settings.sharedPrefsFile, 
					Context.MODE_PRIVATE);
			boolean flag = mSharedPreferences.getBoolean("flowOnOffKey",
					false);
			if(flag){
		    	Intent it = new Intent(mContext, FlowRefreshService.class);
				mContext.stopService(it);
				mContext.startService(it);
				mSharedPreferences.edit().putBoolean("flowOnOffKey", flag).commit();
			}
			if(DeviceInfo.ISAlarmShowing ){
				Intent intent2 = new Intent(mContext, AlarmService.class);
				mContext.stopService(intent2);
				mContext.startService(intent2);
			}
		}
		//GQT英文版 2014-9-2 手动切换语言，更改通知栏状态
		if(intentAction.equals("SettingLanguage")){
			//应用的语言改变，清空数据
			GroupListUtil.removeDataOfGroupList();
			SharedPreferences sharedPreferences = context.getSharedPreferences("notifyInfo",  Context.MODE_PRIVATE);
			int type = sharedPreferences.getInt("type", 0);
			String text = sharedPreferences.getString("text", null);
			int mInCallResId = sharedPreferences.getInt("mInCallResId", 0);
			long base = sharedPreferences.getLong("base", 0);
			if("登录成功".equals(text)||"Login successfully!".equals(text)){
				text = mContext.getResources().getString(R.string.regok);
			}else if("登录失败".equals(text)||"Login failed".equals(text)){
				text = mContext.getResources().getString(R.string.regfailed);
			}else if("正在登录...".equals(text)||"Loginning".equals(text)){
				text = mContext.getResources().getString(R.string.reg);
			}else if("通话中".equals(text)||"In-Call".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_in_progress);
			}else if("来电".equals(text)||"Incoming".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_incoming_call);
			}else if("通话结束".equals(text)||"Call End".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_call_ended);
			}else if("通话保持中".equals(text)||"In the to keep".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_on_hold);
			}else if("拨号中".equals(text)||"Dialing".equals(text)){
				text = mContext.getResources().getString(R.string.card_title_dialing);
			}
			Receiver.onText(type, text, mInCallResId, base);
			//GQT英文版 2014-9-3 设置语言的时候更新速率检测悬浮窗
			SharedPreferences mSharedPreferences = mContext.getSharedPreferences(com.zed3.sipua.ui.Settings.sharedPrefsFile, 
					Context.MODE_PRIVATE);
			boolean flag = mSharedPreferences.getBoolean("flowOnOffKey",
					false);
		    if(flag){
		    	Intent it = new Intent(mContext, FlowRefreshService.class);
				mContext.stopService(it);
				mContext.startService(it);
				mSharedPreferences.edit().putBoolean("flowOnOffKey", flag).commit();
			}
		    if(DeviceInfo.ISAlarmShowing ){
				Intent intent2 = new Intent(mContext, AlarmService.class);
				mContext.stopService(intent2);
				mContext.startService(intent2);
			}
		}
		if (!SipUAApp.on(context))
			return;
		if (!Sipdroid.release)
			Log.i("SipUA:", intentAction);
		/*if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
			// edit by hdf 1开机就自动， 0为开机不运行
			String autoRunConfig = IsAutoRunConfig();
			Log.e("autoopen1", autoRunConfig);
			if (autoRunConfig.equals("1")) {
				Logger.i(true, tag , "autoRun begin");
				on_vpn(false);
				engine(context).register(true);
				Logger.i(true, tag, "autoRun end");
			}else {
				Logger.i(true, tag, "autoRun false");
			}
		} else */if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)
				|| intentAction.equals(ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)
				|| intentAction
						.equals(ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)
				|| intentAction.equals(Intent.ACTION_PACKAGE_REPLACED)) {

			//Log.e("autoopen2", IsAutoRunConfig());
			//Delete by zzhan 2013-5-10
			/*
			// edit by hdf 1开机就自动， 0为开机不运行
			if (IsAutoRunConfig().equals("1"))
				engine(context).register(true);
				*/
			// Restart the engine
			
			//modify by hu 13-7-8
//			if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
//				
//				
//		        
//			} else {
//				engine(context).register(true);
//				MyLog.e("Receiver", "engine register.");
//			}
		} else if (intentAction.equals(ACTION_VPN_CONNECTIVITY)
				&& intent.hasExtra("connection_state")) {
			String state = intent.getSerializableExtra("connection_state")
					.toString();
			if (state != null && on_vpn() != state.equals("CONNECTED")) {
				on_vpn(state.equals("CONNECTED"));
				//modify by oumogang 2013-07-22
				if (mSipdroidEngine != null){
					for (SipProvider sip_provider : engine(context).sip_providers)
						if (sip_provider != null)
							sip_provider.haltConnections();
					if (mSipdroidEngine != null) {
						engine(context).register(true);
					}
				}
				
			}
		}/* else if (intentAction.equals(ACTION_DATA_STATE_CHANGED)) { //delete by hu 2013-7-8
			engine(context).registerMore();
		} */else if (intentAction.equals(ACTION_PHONE_STATE_CHANGED)
				&& !intent.getBooleanExtra(
						context.getString(R.string.app_name), false)) {
			stopRingtone();
			pstn_state = intent.getStringExtra("state");
			pstn_time = SystemClock.elapsedRealtime();
			if (pstn_state.equals("IDLE")
					&& call_state != UserAgent.UA_STATE_IDLE)
				broadcastCallStateChanged(null, null);
			if ((pstn_state.equals("OFFHOOK") && call_state == UserAgent.UA_STATE_INCALL)
					|| (pstn_state.equals("IDLE") && call_state == UserAgent.UA_STATE_HOLD)){
				//modify by oumogang 2013-07-22
//				engine(context).togglehold();
				if (mSipdroidEngine != null) {
					engine(context).togglehold();
				}
			}
					
		} else if (intentAction.equals(ACTION_DOCK_EVENT)) {
			docked = intent.getIntExtra(EXTRA_DOCK_STATE, -1);
			if (call_state == UserAgent.UA_STATE_INCALL){
				//modify by oumogang 2013-07-22
//				engine(mContext).speaker(speakermode());
				if (mSipdroidEngine != null) {
					engine(mContext).speaker(speakermode());
				}
			}
				
		} else if (intentAction.equals(ACTION_SCO_AUDIO_STATE_CHANGED)) {
			bluetooth = intent.getIntExtra(EXTRA_SCO_AUDIO_STATE, -1);
			//modify by oumogang 2013-07-22
//			progress();
			if (mSipdroidEngine != null) {
				progress();
				if(Receiver.GetCurUA().IsPttMode()){
					RtpStreamSender_group.changed = true;
				}else{
					RtpStreamSender_signal.changed = true;
				}
			}
		} else if (intentAction.equals(Intent.ACTION_HEADSET_PLUG)) {
			headset = intent.getIntExtra("state", -1);
			//modify by oumogang 2013-07-22
			if (mSipdroidEngine != null) {
				//Modify by zzhan 2013-5-15
				if (call_state == UserAgent.UA_STATE_INCALL)
					engine(mContext).speaker(speakermode());
				else if (engine(mContext).GetCurUA().IsPttMode()) {
					engine(mContext).GetCurUA().pttSpeakerControl();
				}
			}
		} else if (intentAction.equals(Intent.ACTION_SCREEN_ON)) {
			//alarm(0, OwnWifi.class);
			MyLog.e(tag, "ACTION_SCREEN_ON");
		} else if (intentAction.equals(Intent.ACTION_USER_PRESENT)) {
			mHandler.sendEmptyMessageDelayed(MSG_ENABLE, 3000);
		} else if (intentAction.equals(Intent.ACTION_SCREEN_OFF)) {
//			WifiManager wm = (WifiManager) mContext
//					.getSystemService(Context.WIFI_SERVICE);
//			WifiInfo wi = wm.getConnectionInfo();
//			if (wm.getWifiState() != WifiManager.WIFI_STATE_ENABLED
//					|| wi == null
//					|| wi.getSupplicantState() != SupplicantState.COMPLETED
//					|| wi.getIpAddress() == 0)
//				alarm(2 * 60, OwnWifi.class);
//			else
//				alarm(15 * 60, OwnWifi.class);
//			if (SipdroidEngine.pwl != null)
//				for (PowerManager.WakeLock pwl : SipdroidEngine.pwl)
//					if (pwl != null && pwl.isHeld()) {
//						pwl.release();
//						pwl.acquire();
//					}
			MyLog.e(tag, "ACTION_SCREEN_OFF");
		} else if (intentAction.equals(ACTION_PTT_DOWN)) {
				GroupCallUtil.makeGroupCall(true, true);
		} else if (intentAction.equals(ACTION_PTT_UP)) {
			GroupCallUtil.makeGroupCall(false, true);
		} else if (intentAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			mHandler.sendEmptyMessageDelayed(MSG_SCAN, 3000);
		} else if (intentAction
				.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
			if (PreferenceManager.getDefaultSharedPreferences(mContext)
					.getBoolean(com.zed3.sipua.ui.Settings.PREF_SELECTWIFI,
							com.zed3.sipua.ui.Settings.DEFAULT_SELECTWIFI)) {
				WifiManager wm = (WifiManager) mContext
						.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wi = wm.getConnectionInfo();
				String activeSSID = null;
				boolean activeFound = false;
				if (wi != null)
					activeSSID = wi.getSSID();
				List<ScanResult> mScanResults = wm.getScanResults();
				List<WifiConfiguration> configurations = wm
						.getConfiguredNetworks();
				if (configurations != null) {
					WifiConfiguration bestconfig = null, maxconfig = null;
					for (final WifiConfiguration config : configurations) {
						if (maxconfig == null
								|| config.priority > maxconfig.priority) {
							maxconfig = config;
						}
					}
					ScanResult bestscan = null, maxscan = null;
					if (mScanResults != null)
						for (final ScanResult scan : mScanResults) {
							if (activeSSID != null
									&& activeSSID.equals(scan.SSID))
								activeFound = true;
							for (final WifiConfiguration config : configurations) {
								if (config.SSID != null
										&& config.SSID.equals("\"" + scan.SSID
												+ "\"")) {
									if (bestscan == null
											|| scan.level > bestscan.level) {
										bestscan = scan;
										bestconfig = config;
									}
									if (config == maxconfig) {
										maxscan = scan;
									}
								}
							}
						}
					if (bestconfig != null
							&& bestconfig.priority != maxconfig.priority
							&& asu(bestscan) > asu(maxscan) * 1.5
							&& (activeSSID == null || activeFound)) {
						if (!Sipdroid.release)
							Log.i("SipUA:", "changing to " + bestconfig.SSID);
						if (activeSSID == null
								|| !activeSSID.equals(bestscan.SSID))
							wm.disconnect();
						bestconfig.priority = maxconfig.priority + 1;
						wm.updateNetwork(bestconfig);
						wm.enableNetwork(bestconfig.networkId, true);
						wm.saveConfiguration();
						if (activeSSID == null
								|| !activeSSID.equals(bestscan.SSID))
							wm.reconnect();
					}
				}
			}
		}
			

	}
	//判断是否是小米
	public static boolean isXiaoMI() {
		return (Build.MODEL.contains("MI 1S") 
				|| Build.MODEL.contains("MI 2S")
				|| Build.MODEL.contains("HUAWEI G700-U00")
				|| Build.MODEL.contains("HUAWEI P6-U06") 
				|| Build.MODEL.contains("HUAWEI MT1-U06")
				|| Build.MODEL.contains("HUAWEI Y511-T00"));
	}

	// add by oumogang 2013-06-05
	// cancel notification of call if state is idle;
	public static boolean isCallNotificationNeedClose() {
		// TODO Auto-generated method stub
		if (Receiver.call_state == UserAgent.UA_STATE_IDLE) {
			Receiver.onText(Receiver.CALL_NOTIFICATION, null, 0, 0);
			return true;
		}
		return false;
	}
}
