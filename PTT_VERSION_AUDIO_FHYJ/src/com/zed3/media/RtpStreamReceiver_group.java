/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package com.zed3.media;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.zoolu.tools.MyLog;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.MyPhoneStateListener;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.codecs.Codecs;
import com.zed3.location.MemoryMg;
import com.zed3.log.Logger;
import com.zed3.net.RtpPacket;
import com.zed3.net.RtpSocket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.exception.MyUncaughtExceptionHandler;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Sipdroid;
import com.zed3.utils.LogUtil;
import com.zed3.utils.MyHandler;
import com.zed3.utils.RtpStreamReceiverUtil;
import com.zed3.utils.RtpStreamReceiverUtil.RtpStreamReceiverType;
import com.zed3.utils.Tools;

/**
 * RtpStreamReceiver is a generic stream receiver. It receives packets from RTP
 * and writes them into an OutputStream.
 */
public class RtpStreamReceiver_group extends Thread {
	int flow = 0;

	/** Whether working in debug mode. */
	public static boolean DEBUG = true;
	
	/** Payload type */
	static Codecs.Map p_type;

	public static String codec = "";

	/** Size of the read buffer : 1KB */
	// MODIFY BY OUMOGANG 2012-12-25 10frame at most
	public static final int BUFFER_SIZE = /* 1024 */160 * 10;

	/**
	 * Maximum blocking time, spent waiting for reading new bytes [milliseconds]
	 */
	public static final int SO_TIMEOUT = 1000;

	/** The RtpSocket */
	RtpSocket rtp_socket = null;
	
	/**Queue for RtpPacket, add by oumogang 2013.02.28*/
	public static Queue<RtpPacket> rtpPacketQueue = new LinkedList<RtpPacket>();

	/** Whether it is running */
	boolean running;
	AudioManager am;
	ContentResolver cr;
	public static int speakermode = -1;
	public static boolean bluetoothmode;
	CallRecorder call_recorder = null;

	// Added by zzhan 2011-10-26
	private boolean rcvSuspend = false;

	private static Context mContext;

	/**
	 * writeDataTask, writedata to audiotrack for keeping bluetoothsco working.
	 * some device ,such as HUAWEI G700 and Coolpad 7296,can't keep bluetooth mic working in PTT groupcall.
	 * add by oumogang 2014-03-13
	 */
	TimerTask writeDataTask = new TimerTask() {
		private byte[] audioData = new byte[320*5];
		private boolean isFirst = true;
		private int count;

		@Override
		public void run() {
			if (rcvSuspend) {
				if (isFirst ) {
					isFirst = false;
					for (int i = 0; i < audioData.length; i++) {
						audioData[i]=(byte)0/* ((i%2==0)?0:1)*/;
					}
				}
				if (track != null) {
					track.write(audioData , 0, 320*5);
					count++;
					if (count>4) {
						count = 0;
						Log.i(tag, "writeDataTask write()");
					}
				}
			}
		}
	};

	public void RcvSuspend() {
//		if (track != null) {
//			MyLog.i(tag, "RcvResume() playsound()");
//			playsound(R.raw.pttaccept8k16bit);
//		}else {
//			if (ua == null) {
//				ua = Receiver.GetCurUA();
//			}
//			ua.playsound(R.raw.pttaccep t8k16bit);
//		}
		rcvSuspend = true;
	}

	public void RcvResume() {
		
//		if (track != null) {
//			MyLog.i(tag, "RcvSuspend() playsound()");
//			playsound(R.raw.pttrelease8k16bit);
//		}else {
//			if (ua == null) {
//				ua = Receiver.GetCurUA();
//			}
//			ua.playsound(R.raw.pttrelease8k16bit);
//		}
		rcvSuspend = false;
	}

	/**
	 * Constructs a RtpStreamReceiver.
	 * 
	 * @param output_stream
	 *            the stream sink
	 * @param socket
	 *            the local receiver SipdroidSocket
	 */
	
	static {
		mContext = SipUAApp.mContext;
	}
	
	public RtpStreamReceiver_group(SipdroidSocket socket, Codecs.Map payload_type,
			CallRecorder rec) {
		init(socket);
		p_type = payload_type;
		call_recorder = rec;
	}

	/** Inits the RtpStreamReceiver */
	private void init(SipdroidSocket socket) {
		if (socket != null)
			rtp_socket = new RtpSocket(socket);
	}

	/** Whether is running */
	public boolean isRunning() {
		return running;
	}

	/** Stops running */
	public void halt() {
		running = false;
	}

	void bluetooth() {
		speaker(AudioManager.MODE_IN_CALL);
		enableBluetooth(!bluetoothmode);
	}

	static boolean was_enabled;

	static void enableBluetooth(boolean mode) {
		if (bluetoothmode != mode && (!mode || isBluetoothAvailable())) {
			if (mode)
				was_enabled = true;
			Bluetooth.enable(bluetoothmode = mode);
		}
	}

	void cleanupBluetooth() {
		if (was_enabled && Integer.parseInt(Build.VERSION.SDK) == 8) {
			enableBluetooth(true);
			try {
				sleep(3000);
			} catch (InterruptedException e) {
			}
			if (Receiver.call_state == UserAgent.UA_STATE_IDLE)
				android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	public static boolean isBluetoothAvailable() {
		if (Receiver.headset > 0 || Receiver.docked > 0)
			return false;
		if (!isBluetoothSupported())
			return false;
		return Bluetooth.isAvailable();
	}

	public static boolean isBluetoothSupported() {
		if (Integer.parseInt(Build.VERSION.SDK) < 8)
			return false;
		return Bluetooth.isSupported();
	}

	public int speaker(int mode) {
		MyLog.e("SPEAKER", "group called mode = "+mode);
		int old = speakermode;

		if ((Receiver.headset > 0 || Receiver.docked > 0 || Receiver.bluetooth > 0)
				&& mode != Receiver.speakermode()){
			return old;
		}
		/*
		if (mode == old)
			return old;
			*/
		//enable by oumogang 2014-01-20
		//???????????
		if (mode == old)
			return old;
		 
		//Delete by oumogag 2014-02-24
//		enableBluetooth(false);
		//Delete by zzhan 2013-5-9
		//saveVolume();
		setMode(speakermode = mode);
//		setCodec();
		//Delete by zzhan 2013-5-9
		//restoreVolume();//去掉toast 显示“按住音量以强制通话提示”
		return old;
	}

	static ToneGenerator ringbackPlayer;
	static int oldvol = -1;

	/**
	 * 是否需要在sco连接成功后重新初始化track？
	 * 蓝牙中途中断？
	 * @return
	 */
	public static int stream() {
		Log.i("Bluetooth_control", "stream(),return "+(speakermode == AudioManager.MODE_IN_CALL?"MODE_IN_CALL":"STREAM_VOICE_CALL"));
		return speakermode == AudioManager.MODE_IN_CALL ? AudioManager.STREAM_VOICE_CALL
				: AudioManager.STREAM_MUSIC;
		//return AudioManager.STREAM_MUSIC;
	}

	public static synchronized void ringback(boolean ringback) {
		if (ringback && ringbackPlayer == null) {
			AudioManager am = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
			oldvol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			setMode(speakermode);
//			enableBluetooth(PreferenceManager.getDefaultSharedPreferences(
//					mContext).getBoolean(
//							com.zed3.sipua.ui.Settings.PREF_BLUETOOTH,
//							com.zed3.sipua.ui.Settings.DEFAULT_BLUETOOTH));
//			am.setStreamVolume(
//					stream(),
//					PreferenceManager
//							.getDefaultSharedPreferences(mContext)
//							.getInt("volume" + speakermode,
//									am.getStreamMaxVolume(stream())
//											* (speakermode == AudioManager.MODE_NORMAL ? 4
//													: 3) / 4), 0);
			ringbackPlayer = new ToneGenerator(
//					/*AudioManager.MODE_IN_CALL*/stream()
					AudioManager.STREAM_VOICE_CALL,100
					/*AudioManager.RINGER_MODE_NORMAL,
					(int) (ToneGenerator.MAX_VOLUME * 2 * com.zed3.sipua.ui.Settings
							.getEarGain())*/);
			if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!= null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
				AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
			}else {
				//ringback should use MODE_HOOK,modify by oumogang 2014-04-08
				AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
			}
			ringbackPlayer.startTone(ToneGenerator.TONE_SUP_RINGTONE);
		} else if (!ringback && ringbackPlayer != null) {
			ringbackPlayer.stopTone();
			ringbackPlayer.release();
			ringbackPlayer = null;
			if (Receiver.call_state == UserAgent.UA_STATE_IDLE) {
				AudioManager am = (AudioManager) mContext
						.getSystemService(Context.AUDIO_SERVICE);
				restoreMode();
//				enableBluetooth(false);
//				am.setStreamVolume(AudioManager.STREAM_MUSIC, oldvol, 0);
				oldvol = -1;
			}
//			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_RINGTONE);
		}
	}

	double smin = 200, s;
	public static int nearend;

	void calc(short[] lin, int off, int len) {
		int i, j;
		double sm = 30000, r;

		for (i = 0; i < len; i += 5) {
			j = lin[i + off];
			s = 0.03 * Math.abs(j) + 0.97 * s;
			if (s < sm)
				sm = s;
			if (s > smin)
				nearend = 6000 * mu / 5;
			else if (nearend > 0)
				nearend--;
		}
		for (i = 0; i < len; i++) {
			j = lin[i + off];
			if (j > 6550)
				lin[i + off] = 6550 * 5;
			else if (j < -6550)
				lin[i + off] = -6550 * 5;
			else
				lin[i + off] = (short) (j * 5);
		}
		r = (double) len / (100000 * mu);
		if (sm > 2 * smin || sm < smin / 2)
			smin = sm * r + smin * (1 - r);
	}

	static long down_time;

	public static void adjust(int keyCode, boolean down) {
		AudioManager mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);

		if (RtpStreamReceiver_group.speakermode == AudioManager.MODE_NORMAL)
			if (down ^ mAudioManager.getStreamVolume(stream()) == 0)
				mAudioManager.setStreamMute(stream(), down);
		if (down && down_time == 0)
			down_time = SystemClock.elapsedRealtime();
		if (!down ^ RtpStreamReceiver_group.speakermode != AudioManager.MODE_NORMAL)
			if (SystemClock.elapsedRealtime() - down_time < 500) {
				mAudioManager
						.adjustStreamVolume(
								stream(),
								keyCode == KeyEvent.KEYCODE_VOLUME_UP ? AudioManager.ADJUST_RAISE
										: AudioManager.ADJUST_LOWER,
								AudioManager.FLAG_SHOW_UI);
			}
		if (!down)
			down_time = 0;
	}

	static void setStreamVolume(final int stream, final int vol, final int flags) {
		(new Thread() {
			public void run() {
//				AudioManager am = (AudioManager) mContext
//						.getSystemService(Context.AUDIO_SERVICE);
//				am.setStreamVolume(stream, vol, flags);
				if (stream == stream())
					restored = true;
			}
		}).start();
	}

	static boolean restored;

	/** ----- 重新设置音量 ------ */
	void restoreVolume() {
		/*
		if (track == null) {
			try {
				track = new AudioTrack(stream(), p_type.codec.samp_rate(),
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, 3200, maxjitter,
				AudioTrack.MODE_STREAM);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		
		switch (getMode()) {
		case AudioManager.MODE_IN_CALL:
			int oldring = PreferenceManager.getDefaultSharedPreferences(
					Receiver.mContext).getInt("oldring", 0);
			if (oldring > 0)
				setStreamVolume(
						AudioManager.STREAM_RING,
						(int) (am.getStreamMaxVolume(AudioManager.STREAM_RING)
								* com.zed3.sipua.ui.Settings.getEarGain() * 3 / 4),
						0);
			if (track != null)
				track.setStereoVolume(
						AudioTrack.getMaxVolume()
								* com.zed3.sipua.ui.Settings.getEarGain(),
						AudioTrack.getMaxVolume()
								* com.zed3.sipua.ui.Settings.getEarGain());
			break;
		case AudioManager.MODE_NORMAL:
			if (track != null)
				track.setStereoVolume(AudioTrack.getMaxVolume(),
						AudioTrack.getMaxVolume());
			break;
		}
		setStreamVolume(
				stream(),
				PreferenceManager
						.getDefaultSharedPreferences(Receiver.mContext)
						.getInt("volume" + speakermode,
								am.getStreamMaxVolume(stream())
										* (speakermode == AudioManager.MODE_NORMAL ? 4
												: 3) / 4), 0);
		*/
		switch (getMode()) {
		case AudioManager.MODE_IN_CALL:
				int oldring = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldring",0);
				if (oldring > 0) setStreamVolume(AudioManager.STREAM_RING,(int)(
						am.getStreamMaxVolume(AudioManager.STREAM_RING)*
						com.zed3.sipua.ui.Settings.getEarGain()*3/4), 0);
				track.setStereoVolume(AudioTrack.getMaxVolume()*
						com.zed3.sipua.ui.Settings.getEarGain()
						,AudioTrack.getMaxVolume()*
						com.zed3.sipua.ui.Settings.getEarGain());
				break;
		case AudioManager.MODE_NORMAL:
				track.setStereoVolume(AudioTrack.getMaxVolume(),AudioTrack.getMaxVolume());
				break;
		}
		setStreamVolume(stream(),
				PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("volume"+speakermode, 
				am.getStreamMaxVolume(stream())*
				(speakermode == AudioManager.MODE_NORMAL?4:3)/4
				),0);
	}

	/** --- 保存音量大小 --- */
	void saveVolume() {
		if (restored) {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(
					mContext).edit();
			edit.putInt("volume" + speakermode, am.getStreamVolume(stream()));
			edit.commit();
		}
	}

	void saveSettings() {
		if (!PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID,
						com.zed3.sipua.ui.Settings.DEFAULT_OLDVALID)) {
			int oldvibrate = am
					.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
			int oldvibrate2 = am
					.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION);
			if (!PreferenceManager.getDefaultSharedPreferences(
					mContext).contains(
					com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE2))
				oldvibrate2 = AudioManager.VIBRATE_SETTING_ON;
			int oldpolicy = android.provider.Settings.System.getInt(cr,
					android.provider.Settings.System.WIFI_SLEEP_POLICY,
					Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
			Editor edit = PreferenceManager.getDefaultSharedPreferences(
					mContext).edit();
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE, oldvibrate);
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE2,
					oldvibrate2);
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDPOLICY, oldpolicy);
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDRING,
					am.getStreamVolume(AudioManager.STREAM_RING));
			edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID, true);
			edit.commit();
		}
	}

	public static int getMode() {
		AudioManager am = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		if (Integer.parseInt(Build.VERSION.SDK) >= 5)
			return am.isSpeakerphoneOn() ? AudioManager.MODE_NORMAL
					: AudioManager.MODE_IN_CALL;
		else
			return am.getMode();
	}

	static boolean samsung;

	// 单呼菜单切换
	public static void setMode(int mode) {
		
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!= null && !ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
			return ;
		}
		//修改有线手咪/耳咪接听电话后声音不走手咪/耳咪的问题。
		else if (SipUAApp.isHeadsetConnected) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
			return ;
		}
		
		Editor edit = PreferenceManager.getDefaultSharedPreferences(
				mContext).edit();
		edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_SETMODE, true);
		edit.commit();

		AudioManager am = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		if (Integer.parseInt(Build.VERSION.SDK) >= 5) {
			// zzhan 2012-11-13
			// am.setMode(AudioManager.MODE_NORMAL);// add by zhan
			am.setSpeakerphoneOn(mode == AudioManager.MODE_NORMAL);
			MyLog.i(tag, "setMode() setSpeakerphoneOn   "+(mode == AudioManager.MODE_NORMAL?"true":"false"));
			if (samsung)
				RtpStreamSender_group.changed = true;
			AudioUtil.getInstance().setAudioConnectMode(mode == AudioManager.MODE_NORMAL?AudioUtil.MODE_SPEAKER:AudioUtil.MODE_HOOK);
		} else{
			am.setMode(mode);
			MyLog.i(tag, "setMode() mode == "+mode+","+((mode==AudioManager.MODE_IN_CALL)?"MODE_IN_CALL":"MODE_NORMAL"));
		}

		// Add by zzhan 2012-03-05
		// guojunfeng 注释：如下方法操作了媒体音量的大小，并未开启扬声器！ 由于媒体音量让用户来调节，此处不再调节
		// if (!Receiver.engine(mContext).GetCurUA().IsPttMode())
		// am.setStreamVolume(AudioManager.STREAM_MUSIC,
		// am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3, 0);
	}

	public static void restoreMode() {
		
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!= null && !ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
			return ;
		}else if (SipUAApp.isHeadsetConnected) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
			return ;
		}
		
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_SETMODE, com.zed3.sipua.ui.Settings.DEFAULT_SETMODE)) {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_SETMODE, false);
			edit.commit();
			if (Receiver.pstn_state == null || Receiver.pstn_state.equals("IDLE")) {
				AudioManager am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
				if (Integer.parseInt(Build.VERSION.SDK) >= 5)
					am.setSpeakerphoneOn(false);
				else
					am.setMode(AudioManager.MODE_NORMAL);
			}
		}
	}

	/** ------初始化音频播放模式----- */
	void initMode() {
		samsung = Build.MODEL.contains("SAMSUNG") || Build.MODEL.contains("SPH-") ||
				Build.MODEL.contains("SGH-") || Build.MODEL.contains("GT-");
			if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL &&
					(Receiver.pstn_state == null || Receiver.pstn_state.equals("IDLE")))
				setMode(AudioManager.MODE_NORMAL);	
	}

	public static void restoreSettings() {
		if (PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID,
						com.zed3.sipua.ui.Settings.DEFAULT_OLDVALID)) {
			AudioManager am = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
			ContentResolver cr = mContext.getContentResolver();
			int oldvibrate = PreferenceManager.getDefaultSharedPreferences(
					mContext).getInt(
					com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE,
					com.zed3.sipua.ui.Settings.DEFAULT_OLDVIBRATE);
			int oldvibrate2 = PreferenceManager.getDefaultSharedPreferences(
					mContext).getInt(
					com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE2,
					com.zed3.sipua.ui.Settings.DEFAULT_OLDVIBRATE2);
			int oldpolicy = PreferenceManager.getDefaultSharedPreferences(
					mContext).getInt(
					com.zed3.sipua.ui.Settings.PREF_OLDPOLICY,
					com.zed3.sipua.ui.Settings.DEFAULT_OLDPOLICY);
			am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, oldvibrate);
			am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
					oldvibrate2);
			Settings.System.putInt(cr, Settings.System.WIFI_SLEEP_POLICY,
					oldpolicy);
			int oldring = PreferenceManager.getDefaultSharedPreferences(
					mContext).getInt("oldring", 0);
//			if (oldring > 0)
//				am.setStreamVolume(AudioManager.STREAM_RING, oldring, 0);
			Editor edit = PreferenceManager.getDefaultSharedPreferences(
					mContext).edit();
			edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID, false);
			edit.commit();
			
//			PowerManager pm = (PowerManager) mContext
//					.getSystemService(Context.POWER_SERVICE);
//			PowerManager.WakeLock wl = pm.newWakeLock(
//					PowerManager.SCREEN_BRIGHT_WAKE_LOCK
//							| PowerManager.ACQUIRE_CAUSES_WAKEUP,
//					"Sipdroid.RtpStreamReceiver");
//			wl.acquire(1000);
		}
		restoreMode();
	}

	public static float good, late, lost, loss;
	double avgheadroom;
	public static int timeout;
	int seq;

	/** --- 清空？？？？？？？？？？？？？？？------ */
	void empty() {
		try {
			rtp_socket.getDatagramSocket().setSoTimeout(1);
			for (;;)
				rtp_socket.receive(rtp_packet);
			
		} catch (SocketException e2) {
			if (!Sipdroid.release)
				e2.printStackTrace();
		} catch (IOException e) {
		}
		try {
			// rtp_socket.getDatagramSocket().setSoTimeout(SO_TIMEOUT);
			rtp_socket.getDatagramSocket().setSoTimeout(100);
		} catch (SocketException e2) {
			if (!Sipdroid.release)
				e2.printStackTrace();
		}
		seq = 0;
	}

	RtpPacket rtp_packet;
	public AudioTrack track;
	/** --- 音轨缓冲大小 getMinBufferSize --- */
	int maxjitter;
	/** --- 音轨缓冲大小 --- */
	int minjitter;
	/** --- 音轨缓冲大小 --- */
	int minjitteradjust;
	/** --- 音轨缓冲大小 --- */
	int minheadroom;
	int cnt, cnt2, user, luser, luser2, lserver;

	private static String tag = "RtpStreamReceiver_group";
	/** ------ */
	public static int jitter;
	/** ---采样率是8000的倍数--- */
	public static int mu;

	private static boolean isStartAudioPlay;

	/** ------设置音频解码器------- */
	void setCodec() {
		synchronized (this) {
			p_type.codec.init();
			codec = p_type.codec.getTitle();
			reinitAudioTrack();
		}
	}
	void reinitAudioTrack() {
		synchronized (this) {
			try {
				AudioTrack oldtrack;
				mu = p_type.codec.samp_rate() / 8000;
				maxjitter = AudioTrack.getMinBufferSize(p_type.codec.samp_rate(),
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int bufferSize = maxjitter+320;
				MyLog.i(tag, "getMinBufferSize() maxjitter = " + maxjitter);
				
				if (maxjitter < 2 * 2 * BUFFER_SIZE * 3 * mu)
					maxjitter = 2 * 2 * BUFFER_SIZE * 3 * mu;
				oldtrack = track;
				
				//Add by zzhan 2014-11-04
				if (oldtrack != null) {
					try {
						oldtrack.stop();
					} catch(Exception e){}
					oldtrack.release();
					oldtrack = null;
				}
				
				Log.i(tag, "stream()   setCodec()");
				
				int streamType = stream();
				AudioUtil.getInstance().setStream(streamType);
				
				// use maxjitter instead of 3200,modify by oumogang 2013-12-16;
				//java.lang.IllegalStateException: play() called on uninitialized AudioTrack.
//				12-16 16:37:23.339: E/AudioTrack(16861): Invalid buffer size: minFrameCount 1857, frameCount 1600
//				12-16 16:37:23.339: E/AudioTrack-JNI(16861): Error initializing AudioTrack
//				12-16 16:37:23.349: E/AudioTrack-Java(16861): [ android.media.AudioTrack ] Error code -20 when initializing AudioTrack.
//				track = new AudioTrack(/*stream()*/streamType, p_type.codec.samp_rate(),
//						AudioFormat.CHANNEL_CONFIGURATION_MONO,
//						AudioFormat.ENCODING_PCM_16BIT, /*3200*//*maxjitter*//*maxjitter*/320*5,
//						AudioTrack.MODE_STREAM);
				track = new AudioTrack(/*stream()*/streamType, p_type.codec.samp_rate(),
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize,/*3200*//*maxjitter*///*320*5*//*bufferSize*/,
						AudioTrack.MODE_STREAM);
				maxjitter /= 2 * 2;
				minjitter = minjitteradjust = 500 * mu;
				jitter = 875 * mu;
				minheadroom = maxjitter * 2;
				timeout = 1;
				luser = luser2 = -8000 * mu;
				cnt = cnt2 = user = lserver = 0;
				
				//Delete by zzhan 2014-11-04
				/*
				if (oldtrack != null) {
					oldtrack.stop();
					oldtrack.release();
				}
				*/
				currentSpeakermode = speakermode;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				MyUncaughtExceptionHandler.saveExceptionLog(e);
				LogUtil.makeLog(tag, "Exception "+e.getMessage());
			}
		}
	}

	public void write(short a[], int b, int c) {
		synchronized (this) {
			user += track.write(a, b, c);
		}
	}

	PowerManager.WakeLock pwl, pwl2;
	static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
	boolean lockLast, lockFirst;

	/** ------- 是否允许锁屏 -------- */
	void lock(boolean lock) {
		try {
			if (lock) {
				boolean lockNew = (keepon && Receiver.on_wlan)
						|| Receiver.call_state != UserAgent.UA_STATE_INCALL
						|| RtpStreamSender_group.delay != 0;
				if (lockFirst || lockLast != lockNew) {
					lockLast = lockNew;
					lock(false);
					lockFirst = false;
					if (pwl == null) {
						PowerManager pm = (PowerManager) mContext
								.getSystemService(Context.POWER_SERVICE);
						pwl = pm.newWakeLock(
								lockNew ? (PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP)
										: PROXIMITY_SCREEN_OFF_WAKE_LOCK,
								"Sipdroid.Receiver");
						pwl.acquire();
					}
				}
			} else {
				lockFirst = true;
				if (pwl != null) {
					pwl.release();
					pwl = null;
				}
			}
		} catch (Exception e) {
		}
		if (lock) {
			if (pwl2 == null) {
				PowerManager pm = (PowerManager) mContext
						.getSystemService(Context.POWER_SERVICE);
				pwl2 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
						"Sipdroid.Receiver");
				pwl2.acquire();
			}
		} else if (pwl2 != null) {
			pwl2.release();
			pwl2 = null;
		}
	}

	boolean keepon;

	private long end_receive;

	private int times;

	private boolean needLog = false;

	private Timer writeDataTimer;

	private int lastTime;

	private int errorCount;

	private int timeCount;

	private int receiveCount;

	private int currentSpeakermode;

	/** Runs it in a new Thread. */
	public void run() {
		LogUtil.makeLog(tag, "run begin");
		RtpStreamReceiverUtil.onStartReceiving(RtpStreamReceiverType.GROUP_CALL_RECEIVER);
		boolean nodata = PreferenceManager.getDefaultSharedPreferences(
				mContext).getBoolean(
				com.zed3.sipua.ui.Settings.PREF_NODATA,
				com.zed3.sipua.ui.Settings.DEFAULT_NODATA);
		keepon = PreferenceManager.getDefaultSharedPreferences(
				mContext).getBoolean(
				com.zed3.sipua.ui.Settings.PREF_KEEPON,
				com.zed3.sipua.ui.Settings.DEFAULT_KEEPON);

		if (rtp_socket == null) {
			if (DEBUG)
				println("ERROR: RTP socket is null");
			return;
		}

		byte[] buffer = new byte[BUFFER_SIZE + 12];
		rtp_packet = new RtpPacket(buffer, 0);

		if (DEBUG)
			println("Reading blocks of max " + buffer.length + " bytes");

		running = true;
//		enableBluetooth(PreferenceManager.getDefaultSharedPreferences(
//				mContext).getBoolean(
//				com.zed3.sipua.ui.Settings.PREF_BLUETOOTH,
//				com.zed3.sipua.ui.Settings.DEFAULT_BLUETOOTH));
		restored = false;

		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
		am = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		cr = mContext.getContentResolver();
		saveSettings();
		Settings.System.putInt(cr, Settings.System.WIFI_SLEEP_POLICY,
				Settings.System.WIFI_SLEEP_POLICY_NEVER);
		am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
				AudioManager.VIBRATE_SETTING_OFF);
		am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_OFF);
		if (oldvol == -1)
			oldvol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	    //init mode before set codec modify by mou 2014-12-29
		if (SipUAApp.isHeadsetConnected) {
			speaker(AudioManager.MODE_IN_CALL);
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
		}else {
			speaker(AudioManager.MODE_NORMAL);
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
		}
		initMode();
		setCodec();
		short lin[] = new short[BUFFER_SIZE];
		short lin2[] = new short[BUFFER_SIZE];
		short lin4amr[] = new short[160];
		int server, headroom, todo, len = 0, m = 1, expseq, getseq, vm = 1, gap, gseq;
		/** ----- 设置按键 震动和声音 ---- */
//		ToneGenerator tg = new ToneGenerator(
//				AudioManager.STREAM_VOICE_CALL,
//				(int) (ToneGenerator.MAX_VOLUME * 2 * com.zed3.sipua.ui.Settings
//						.getEarGain()));
		
		//Delete by zzhan 2014-11-04 
		//Dear Dongxu, your code dosen't work.
		/**
		 * 解决集群通因java.lang.IllegalStateException: play() called on uninitialized
		 * AudioTrack.异常退出的问题 modify by liangzhang 2014-10-09
		 */
		/*
		if (track.getState() == AudioTrack.STATE_UNINITIALIZED) {
			setCodec();
		}
		*/
		
		track.play();
		
		System.gc();
		empty();
		lockFirst = true;
		int packageNum = 0;
		//开启音频解码和播放线程
//		isStartAudioPlay = true;
//		new Thread(decodeAndPlayThread).start();
		
		//Delete by zzhan 2013-5-15
		/*
		// add by oumogang 2013-05-07
		am.setSpeakerphoneOn(UserAgent.ua_ptt_mode);
		*/
		
		while (running) { 
			if (speakermode != currentSpeakermode) {
				reinitAudioTrack();
				track.play();
			}
//			restoreVolume();
//			lock(true);
//
//			/** oumogang 121219 是否采用amr编解码，是则引入ptime参数进行解码； */
//
//			if (Receiver.call_state == UserAgent.UA_STATE_HOLD) {
//				lock(false);
//				tg.stopTone();
//				track.pause();
//				while (running
//						&& Receiver.call_state == UserAgent.UA_STATE_HOLD) {
//					try {
//						sleep(1000);// ????????????为什么要停1秒？？？？？？？
//					} catch (InterruptedException e1) {
//					}
//				}
//
//				track.play();
//				System.gc();
//				timeout = 1;
//				luser = luser2 = -8000 * mu;
//			}
//			try {
//				rtp_socket.receive(rtp_packet);
//				if (lastReceiveTime == 0) {
//					lastReceiveTime = System.currentTimeMillis();
//				}else {
//					endReceive = System.currentTimeMillis();
//					long time = endReceive -lastReceiveTime;
//					Logger.i(needLog,tag, "receiver receive rate = "+time);
//					lastReceiveTime = endReceive;
//				}
//				packageNum++;
//				// 统计流量
//				if (rtp_packet.getLength() + 42 > 60) {
//					flow += rtp_packet.getLength() + 42;
//
//				} else {
//					flow += 60;
//				}
//				// MyLog.i(tag, "rtp_packet.getLength()="
//				// +rtp_packet.getLength());
//				MyLog.i(tag, "flow  receive length =" + rtp_packet.getLength());
//				FlowStatistics.Voice_Receive_Data = flow;
//				end_receive = System.currentTimeMillis();
//				Logger.i(needLog,"receive()", "time for receive : "
//						+ (end_receive - begin_receive));
//				begin_receive = end_receive;
//
//			} catch (IOException e) {
//
//			}
//			
//			lastDecodeAndPlay = System.currentTimeMillis();
//			if (isAmr) {
//				byte[] payload = rtp_packet.getPayload();
//				int frameNum = getFrameNum(payload);
//				len = p_type.codec.decode(buffer, lin,
//						rtp_packet.getPayloadLength(), frameNum);
//
//			} else {
//				len = p_type.codec.decode(buffer, lin,
//						rtp_packet.getPayloadLength());
//			}
//			track.write(lin, 0, len);
//			endDecodeAndPlay = System.currentTimeMillis();
//			time4DecodeAndPlay = endDecodeAndPlay - lastDecodeAndPlay;
//			Logger.i(needLog,tag, "receiver decode and play use time = "+time4DecodeAndPlay);
//			lastDecodeAndPlay = endDecodeAndPlay;
//			
//			
//			Logger.i(needLog,tag, "receiver track.write() packageNum = "+packageNum);

		

//			lock(true);
			//???????????????????????
			if (Receiver.call_state == UserAgent.UA_STATE_HOLD) {
				lock(false);
//				tg.stopTone();
				track.pause();
				while (running
						&& Receiver.call_state == UserAgent.UA_STATE_HOLD) {
					try {
						sleep(1000);//????????????为什么要停1秒？？？？？？？
					} catch (InterruptedException e1) {
					}
				}

				track.play();
				System.gc();
				timeout = 1;
				luser = luser2 = -8000 * mu;
			}
			//接包
			try {
				rtp_packet = new RtpPacket(buffer, 0);
				rtp_socket.receive(rtp_packet);
				RtpStreamReceiverUtil.onReceive(RtpStreamReceiverType.GROUP_CALL_RECEIVER, rtp_packet);
				//add log for receive packet. add by mou 2014-11-03
				receiveCount ++;
				if (receiveCount % 20 == 0) {
					LogUtil.makeLog(tag, "receiveCount % 20 == 0 receiveCount "+receiveCount);
				}
				if (times == 0) {
					times = 1;
				}else {
					times++;
				}
				
//				Logger.i(needLog,tag, "rtp_socket.receive(rtp_packet) times = "+times);
				
				//统计流量
//				if(rtp_packet.getLength()+flow>60){
//					flow =rtp_packet.getLength() +42;
//				}else{
//					flow = 60 ;
//				}
//				FlowStatistics.Voice_Receive_Data = FlowStatistics.Voice_Receive_Data+flow;
					if (rtp_packet==null) {
						Logger.i(needLog ,tag, "rtpPacket==null");
					}
					if (p_type.codec==null) {
						Logger.i(needLog,tag, "p_type.codec==null");
					}
					byte[] decoded = rtp_packet.getPacket();
					int size = rtp_packet.getPayloadLength();
					len = p_type.codec.decode(decoded, lin,
							size);
				//add by hu 2014/2/24
				int d = (MemoryMg.SdpPtime == 0) ? MemoryMg.PTIME: MemoryMg.SdpPtime;

				timeCount ++;
				if(Receiver.GetCurUA().IsPttMode() && timeCount == (300/d)){
					timeCount = 0;
					byte[] speechBuffer = Tools.shortArray2ByteArray(lin);
					if(speechBuffer.length > 0){
						int t = 0; 
						for (int i = 0; i < speechBuffer.length; i++) {
							t += Math.abs(speechBuffer[i]);
						}
						t /= speechBuffer.length;
//						t = t-(t%10);
//						if(lastTime  != t){
	//						MyHandler.sendMessage(t);
							MyHandler.sendReceiveMessage(t);
//							lastTime = t;
//						}
					}
				}
				//SIM卡来电接听后（来电,去电或通话中），我们软件切换到听筒模式，并且不播放对讲声音。 modify by mou 2014-10-08
				if (!MyPhoneStateListener.getInstance().isInCall()) {
					track.write(lin, 0, len);
					track.flush();
				}
			} catch (IOException e) {
				if (errorCount==10) {
					errorCount = 0;
					//非SocketTimeoutException时输出log
					if((e!=null && !(e instanceof SocketTimeoutException))) {
						MyLog.i(tag, "IOException "+e.getMessage());
					}
				}
				errorCount++;
			}
		
		}
		
//		isStartAudioPlay = false;
		lock(false);
		synchronized (this) {
			track.stop();
			track.release();
			track = null;
		}
//		tg.stopTone();
//		tg.release();
		saveVolume();
//		am.setStreamVolume(AudioManager.STREAM_MUSIC, oldvol, 0);
		restoreSettings();
//		enableBluetooth(false);
//		am.setStreamVolume(AudioManager.STREAM_MUSIC, oldvol, 0);
		oldvol = -1;
		p_type.codec.close();
		rtp_socket.getDatagramSocket().disconnect();
		rtp_socket.close();
		rtp_socket = null;
		codec = "";

		// Call recording: stop incoming receive.
		if (call_recorder != null) {
			call_recorder.stopIncoming();
			call_recorder = null;
		}

		if (DEBUG)
			println("rtp receiver terminated");

//		cleanupBluetooth();
		
		//Delete by zzhan 2013-5-14
		/*
		// add by oumogang 2013-05-07
		am.setSpeakerphoneOn(false);
		*/
		RtpStreamReceiverUtil.onStopReceiving(RtpStreamReceiverType.GROUP_CALL_RECEIVER);
		LogUtil.makeLog(tag, "run end");
	}

	/**
	 * add by oumogang 2012.12.21 计算获取语音帧数
	 */
	private synchronized int getFrameNum(byte[] payload) {
		// TODO Auto-generated method stub
		// 先获取速率
		int encodeDode = payload[0] >> 4;
		int frameNum = 0;
		int payoadLen = payload.length;

		// 计算帧数
		switch (encodeDode) {
		case 7:
			frameNum = (payoadLen - 1) / 32;
			break;
		case 6:
			frameNum = (payoadLen - 1) / 27;
			break;
		case 5:
			frameNum = (payoadLen - 1) / 21;
			break;
		case 4:
			frameNum = (payoadLen - 1) / 20;
			break;
		case 3:
			frameNum = (payoadLen - 1) / 18;
			break;
		case 2:
			frameNum = (payoadLen - 1) / 16;
			break;
		case 1:
			frameNum = (payoadLen - 1) / 14;
			break;
		case 0:
			frameNum = (payoadLen - 1) / 13;
			break;
		default:
			break;
		}
//		Logger.i(needLog,tag, "getFrameNum() " + frameNum);
		return frameNum;
	}

	/** Debug output */
	protected static void println(String str) {
		if (!Sipdroid.release)
			System.out.println("RtpStreamReceiver: " + str);
	}

	public static int byte2int(byte b) { // return (b>=0)? b : -((b^0xFF)+1);
		// return (b>=0)? b : b+0x100;
		return (b + 0x100) % 0x100;
	}

	public static int byte2int(byte b1, byte b2) {
		return (((b1 + 0x100) % 0x100) << 8) + (b2 + 0x100) % 0x100;
	}

	public static String getCodec() {
		return codec;
	}

	/**
	 * init something after thread started.
	 */
	public void startBackgroudAfterThreadStarting() {
		// TODO Auto-generated method stub
		//set audio mode
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!= null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
			//writeData for Huawei G700 and Coolpad 7296
			if (UserAgent.ua_ptt_mode && writeDataTimer == null) {
				writeDataTimer = new Timer();
				writeDataTimer.schedule(writeDataTask, 0, 100);
				MyLog.i(tag, "startBackgroud() writeDataTimer.schedule(writeDataTask, 0, 100)");
			}
		}else {
			if (UserAgent.ua_ptt_mode) {
				MyLog.i(tag, "startBackgroud() setAudioConnectMode() setAudioConnectMode(AudioUtil.MODE_SPEAKER)");
				AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
			}else {
				MyLog.i(tag, "startBackgroud() setAudioConnectMode() setAudioConnectMode(AudioUtil.MODE_HOOK)");
				AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
			}
		}
	}
	/**
	 * exit something before thread stop.
	 */
	public void stopBackgroudBeforeThreadStopping() {
		// TODO Auto-generated method stub
		//set audio mode
		if (writeDataTimer != null) {
			MyLog.i(tag, "stopBackgroud() writeDataTimer.cancel()");
			writeDataTimer.cancel();
		}
		MyLog.i(tag, "stopBackgroud() setAudioConnectMode(TalkBackNew.mAudioMode)");
//		AudioUtil.getInstance().setAudioConnectMode(TalkBackNew.mAudioMode);
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!=null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
		}else if (SipUAApp.isHeadsetConnected) {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
		}else {
			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
		}
	}

	public AudioTrack getAudioTrack() {
		// TODO Auto-generated method stub
		return track;
	}
	
//	Runnable decodeAndPlayThread = new Runnable() {
//		
//
//		private RtpPacket rtpPacket;
//		private int len;
//		private short lin[] = new short[1600];
//
//		@Override
//		public void run() {
//			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
//			Logger.i(needLog,tag, "decode and play is begin");
//			// TODO Auto-generated method stub
////			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,// stream
////					// type
////					8000,// sample rate
////					AudioFormat.CHANNEL_CONFIGURATION_MONO,// channel config
////					AudioFormat.ENCODING_PCM_16BIT,// audio format
////					320 * 100,// buffer size: 8KB
////					AudioTrack.MODE_STREAM);// buffer mode
//
//			track.play();
//			
//			while (RtpStreamReceiver.isStartAudioPlay()) {
//				//add by oumogang 2012 12 21 amr ptime 
//				while (running&&rtpPacketQueue.size() < 2) {
////					Logger.i(needLog,tag, "encode() storage.size()=" + ",Thread.sleep(20)");
//					/*try {
//						Thread.sleep(20);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}*/
//				}
//				/*Logger.i(needLog,tag,"rtpPacketQueue.poll() get one rtpPacket, rtpPacketQueue.size()= "
//								+ rtpPacketQueue.size());*/
//				
//				
//				rtpPacket = rtpPacketQueue.poll();
//				if (rtpPacket == null) {
//					continue;
//				}
//			
//				
//				if (isAmr) {
//					Logger.i(needLog,tag, "rtp_packet.getPayloadLength()"+rtp_packet.getPayloadLength());
//					
//					byte[] payload = rtpPacket.getPayload();
////					long timestamp = rtp_packet.getSequenceNumber();
//					int frameNum = getFrameNum(payload);
//					len = amr.decode(rtpPacket.getPacket(), lin,
//							rtpPacket.getPayloadLength(),frameNum);
//				} else {
//					if (rtpPacket==null) {
//						Logger.i(needLog,tag, "rtpPacket==null");
//					}
//					if (p_type.codec==null) {
//						Logger.i(needLog,tag, "p_type.codec==null");
//					}
//					len = p_type.codec.decode(rtpPacket.getPacket(), lin,
//							rtpPacket.getPayloadLength());
//				}
//				track.write(lin, 0, len);
//			}
//			Logger.i(needLog,tag, "decode and play is over");
//			track.stop();
//			track.release();
//		}
//	};
//
//	protected static boolean isStartAudioPlay() {
//		// TODO Auto-generated method stub
//		return isStartAudioPlay;
//	}
	
	

}
