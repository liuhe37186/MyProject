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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.KeyEvent;

import com.zed3.audio.AudioSettings;
import com.zed3.audio.AudioUtil;
import com.zed3.audio.TrackPlayQueue;
import com.zed3.codecs.Codecs;
import com.zed3.flow.FlowStatistics;
import com.zed3.net.RtpPacket;
import com.zed3.net.RtpSocket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Sipdroid;
import com.zed3.utils.LogUtil;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.ReceivePacketCompare;
import com.zed3.video.ReceivePacketInfo;
import com.zed3.utils.RtpStreamReceiverUtil;
import com.zed3.utils.RtpStreamReceiverUtil.RtpStreamReceiverType;

/**
 * RtpStreamReceiver is a generic stream receiver. It receives packets from RTP
 * and writes them into an OutputStream.
 */
public class RtpStreamReceiver_signal extends Thread {

	/** Whether working in debug mode. */
	public static boolean DEBUG = true;

	/** Payload type */
	Codecs.Map p_type;

	static String codec = "";
	
	/** 根据时延、丢包确定的cmr值，默认设置成15 */
	public static byte judged_cmr = 15;

	/** 从对方发送的RTP包中提取出来的cmr值，供自己的发送端使用*/
//	public static byte received_cmr = 15;

	/** Size of the read buffer */
	public static final int BUFFER_SIZE = 1024;

	/** Maximum blocking time, spent waiting for reading new bytes [milliseconds] */
	public static final int SO_TIMEOUT = 1000;

	/** The RtpSocket */
	RtpSocket rtp_socket = null;

	/** Whether it is running */
	boolean running;
	boolean trackRunning;
	Thread playThread;
	TrackPlayQueue playQueue;
	AudioManager am;
	ContentResolver cr;
	public static int speakermode = -1;
	public static boolean bluetoothmode;
	CallRecorder call_recorder = null;
	int timeCount = 0;
	float Dtr = 78.0f, cal_lost, cal_delay=0.0f, avg_delay=0.0f;;
	private int discardCount = 0,fillCount = 0;
	/**
	 * Constructs a RtpStreamReceiver.
	 * 
	 * @param output_stream
	 *            the stream sink
	 * @param socket
	 *            the local receiver SipdroidSocket
	 */
	byte[] buffer;
	public RtpStreamReceiver_signal(SipdroidSocket socket, Codecs.Map payload_type, CallRecorder rec) {
		init(socket);
		p_type = payload_type;
		call_recorder = rec;
		playQueue = new TrackPlayQueue();
		buffer = new byte[1612];
		rtp_packet = new RtpPacket(buffer, 0);
		TrackPlayer playRunnable= new TrackPlayer(playQueue);
		playThread = new Thread(playRunnable);
		playThread.start();
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
		trackRunning = false;
	}
	
	void bluetooth() {
		speaker(AudioManager.MODE_IN_CALL);
		enableBluetooth(!bluetoothmode);
	}
	
	static boolean was_enabled;
	
	static void enableBluetooth(boolean mode) {
		if (bluetoothmode != mode && (!mode || isBluetoothAvailable())) {
			if (mode) was_enabled = true;
			Bluetooth.enable(bluetoothmode = mode);
		}
	}
	
	void cleanupBluetooth() {
		if (was_enabled && Build.VERSION.SDK_INT == 8) {
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
		if (Build.VERSION.SDK_INT < 8)
			return false;
		return Bluetooth.isSupported();
	}
	
	public int speaker(int mode) {
		int old = speakermode;
		
		if ((Receiver.headset > 0 || Receiver.docked > 0 || Receiver.bluetooth > 0) &&
				mode != Receiver.speakermode())
			return old;
		if (mode == old)
			return old;
		enableBluetooth(false);
		saveVolume();
		setMode(speakermode = mode);
		setCodec();
		restoreVolume();
//		if (mode == AudioManager.MODE_NORMAL && Thread.currentThread().getName().equals("main"))
//			Toast.makeText(Receiver.mContext, R.string.help_speakerphone, Toast.LENGTH_LONG).show();
		return old;
	}

	static ToneGenerator ringbackPlayer;
	static int oldvol = -1;
	
	public static int stream() {
		return speakermode == AudioManager.MODE_IN_CALL?AudioManager.STREAM_VOICE_CALL:AudioManager.STREAM_MUSIC;
	}
	
	public static synchronized void ringback(boolean ringback) {
		if (ringback && ringbackPlayer == null) {
	        AudioManager am = (AudioManager) Receiver.mContext.getSystemService(
                    Context.AUDIO_SERVICE);
			oldvol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			setMode(speakermode);
			enableBluetooth(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_BLUETOOTH,
					com.zed3.sipua.ui.Settings.DEFAULT_BLUETOOTH));
			//去掉音量保存功能 。 modify by lwang 2015-01-14
//			am.setStreamVolume(stream(),
//					PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("volume"+speakermode, 
//					am.getStreamMaxVolume(stream())*
//					(speakermode == AudioManager.MODE_NORMAL?4:3)/4
//					),0);
			ringbackPlayer = new ToneGenerator(stream(),(int)(ToneGenerator.MAX_VOLUME*2*com.zed3.sipua.ui.Settings.getEarGain()));
			ringbackPlayer.startTone(ToneGenerator.TONE_SUP_RINGTONE);
		} else if (!ringback && ringbackPlayer != null) {
			ringbackPlayer.stopTone();
			ringbackPlayer.release();
			ringbackPlayer = null;
			if (Receiver.call_state == UserAgent.UA_STATE_IDLE) {
		        AudioManager am = (AudioManager) Receiver.mContext.getSystemService(
	                    Context.AUDIO_SERVICE);
				restoreMode();
				enableBluetooth(false);
				//去掉音量保存功能 。 modify by lwang 2015-01-14
//				am.setStreamVolume(AudioManager.STREAM_MUSIC,oldvol,0);
				oldvol = -1;
			}
		}
	}
	
	double smin = 200,s;
	public static int nearend;
	
	void calc(short[] lin,int off,int len) {
		int i,j;
		double sm = 30000,r;
		
		for (i = 0; i < len; i += 5) {
			j = lin[i+off];
			s = 0.03*Math.abs(j) + 0.97*s;
			if (s < sm) sm = s;
			if (s > smin) nearend = 6000*mu/5;
			else if (nearend > 0) nearend--;
		}
		for (i = 0; i < len; i++) {
			j = lin[i+off];
			if (j > 6550)
				lin[i+off] = 6550*5;
			else if (j < -6550)
				lin[i+off] = -6550*5;
			else
				lin[i+off] = (short)(j*5);
		}
		r = (double)len/(100000*mu);
		if (sm > 2*smin || sm < smin/2)
			smin = sm*r + smin*(1-r);
	}
	
	void calc2(short[] lin,int off,int len) {
		int i,j;
		
		for (i = 0; i < len; i++) {
			j = lin[i+off];
			if (j > 16350)
				lin[i+off] = 16350<<1;
			else if (j < -16350)
				lin[i+off] = -16350<<1;
			else
				lin[i+off] = (short)(j<<1);
		}
	}
	
	static long down_time;
	
	public static void adjust(int keyCode,boolean down) {
        AudioManager mAudioManager = (AudioManager) Receiver.mContext.getSystemService(
                Context.AUDIO_SERVICE);
        
		if (RtpStreamReceiver_signal.speakermode == AudioManager.MODE_NORMAL)
			if (down ^ mAudioManager.getStreamVolume(stream()) == 0)
				mAudioManager.setStreamMute(stream(), down);
		if (down && down_time == 0)
			down_time = SystemClock.elapsedRealtime();
		if (!down ^ RtpStreamReceiver_signal.speakermode != AudioManager.MODE_NORMAL)
			if (SystemClock.elapsedRealtime()-down_time < 500) {
				if (!down)
					down_time = 0;
				if (ogain > 1)
					if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
						if (gain != ogain) {
							gain = ogain;
							return;
						}
						if (mAudioManager.getStreamVolume(stream()) ==
							mAudioManager.getStreamMaxVolume(stream())) return;
						gain = ogain/2;
					} else {
						if (gain == ogain) {
							gain = ogain/2;
							return;
						}
						if (mAudioManager.getStreamVolume(stream()) == 0) return;
						gain = ogain;
					}
		        mAudioManager.adjustStreamVolume(
		                    stream(),
		                    keyCode == KeyEvent.KEYCODE_VOLUME_UP
		                            ? AudioManager.ADJUST_RAISE
		                            : AudioManager.ADJUST_LOWER,
		                    AudioManager.FLAG_SHOW_UI);
			}
		if (!down)
			down_time = 0;
	}

	static void setStreamVolume(final int stream,final int vol,final int flags) {
        (new Thread() {
			public void run() {
				AudioManager am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
				//去掉音量保存功能 。 modify by lwang 2015-01-14
				//am.setStreamVolume(stream, vol, flags);
				if (stream == stream()) restored = true;
			}
        }).start();
	}
	
	static boolean restored;
	static float gain,ogain;
	
	void restoreVolume() {
		if(am == null){
			am = (AudioManager) Receiver.mContext.getSystemService(
					Context.AUDIO_SERVICE);
		}
		switch (getMode()) {
		case AudioManager.MODE_IN_CALL:
				int oldring = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldring",0);
				if (oldring > 0) setStreamVolume(AudioManager.STREAM_RING,(int)(
						am.getStreamMaxVolume(AudioManager.STREAM_RING)*
						com.zed3.sipua.ui.Settings.getEarGain()*3/4), 0);
				track.setStereoVolume(AudioTrack.getMaxVolume()*
						(ogain = com.zed3.sipua.ui.Settings.getEarGain()*2)
						,AudioTrack.getMaxVolume()*
						com.zed3.sipua.ui.Settings.getEarGain()*2);
				if (gain == 0 || ogain <= 1) gain = ogain;
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
	
	void saveVolume() {
		if (restored) {
			if(am == null){
				am = (AudioManager) Receiver.mContext.getSystemService(
	                    Context.AUDIO_SERVICE);
			}
			Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putInt("volume"+speakermode, am.getStreamVolume(stream()));
			edit.commit();
		}
	}
	
	void saveSettings() {
		if (!PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID, com.zed3.sipua.ui.Settings.DEFAULT_OLDVALID)) {
			int oldvibrate = am.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
			int oldvibrate2 = am.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION);
			if (!PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).contains(com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE2))
				oldvibrate2 = AudioManager.VIBRATE_SETTING_ON;
			int oldpolicy = android.provider.Settings.System.getInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY, 
					Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
			Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE, oldvibrate);
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE2, oldvibrate2);
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDPOLICY, oldpolicy);
			edit.putInt(com.zed3.sipua.ui.Settings.PREF_OLDRING, am.getStreamVolume(AudioManager.STREAM_RING));
			edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID, true);
			edit.commit();
		}
	}
	
	public static int getMode() {
		AudioManager am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (Integer.parseInt(Build.VERSION.SDK) >= 5)
			return am.isSpeakerphoneOn()?AudioManager.MODE_NORMAL:AudioManager.MODE_IN_CALL;
		else
			return am.getMode();
	}
	
	static boolean samsung;
	
	public static void setMode(int mode) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
		edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_SETMODE, true);
		edit.commit();
		AudioManager am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (Build.VERSION.SDK_INT >= 5) {
			am.setSpeakerphoneOn(mode == AudioManager.MODE_NORMAL);
			if (samsung) RtpStreamSender_signal.changed = true;
			AudioUtil.getInstance().setAudioConnectMode(mode == AudioManager.MODE_NORMAL?AudioUtil.MODE_SPEAKER:AudioUtil.MODE_HOOK);
		} else
			am.setMode(mode);
	}
	
	public static void restoreMode() {
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_SETMODE, com.zed3.sipua.ui.Settings.DEFAULT_SETMODE)) {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_SETMODE, false);
			edit.commit();
			if (Receiver.pstn_state == null || Receiver.pstn_state.equals("IDLE")) {
				AudioManager am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
				if (Build.VERSION.SDK_INT >= 5){
					AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
					am.setSpeakerphoneOn(false);
					}
				else
					am.setMode(AudioManager.MODE_NORMAL);
			}
		}
	}

	void initMode() {
		samsung = Build.MODEL.contains("SAMSUNG") || Build.MODEL.contains("SPH-") ||
			Build.MODEL.contains("SGH-") || Build.MODEL.contains("GT-");
		if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL &&
				(Receiver.pstn_state == null || Receiver.pstn_state.equals("IDLE")))
			setMode(AudioManager.MODE_NORMAL);	
		AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
	}
	
	public static void restoreSettings() {
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID, com.zed3.sipua.ui.Settings.DEFAULT_OLDVALID)) {
			AudioManager am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
	        ContentResolver cr = Receiver.mContext.getContentResolver();
			int oldvibrate = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt(com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE, com.zed3.sipua.ui.Settings.DEFAULT_OLDVIBRATE);
			int oldvibrate2 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt(com.zed3.sipua.ui.Settings.PREF_OLDVIBRATE2, com.zed3.sipua.ui.Settings.DEFAULT_OLDVIBRATE2);
			int oldpolicy = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt(com.zed3.sipua.ui.Settings.PREF_OLDPOLICY, com.zed3.sipua.ui.Settings.DEFAULT_OLDPOLICY);
			am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,oldvibrate);
			am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,oldvibrate2);
			Settings.System.putInt(cr, Settings.System.WIFI_SLEEP_POLICY, oldpolicy);
			int oldring = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldring",0);
			//去掉音量保存功能 。 modify by lwang 2015-01-14
			//if (oldring > 0) am.setStreamVolume(AudioManager.STREAM_RING, oldring, 0);
			Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putBoolean(com.zed3.sipua.ui.Settings.PREF_OLDVALID, false);
			edit.commit();
			PowerManager pm = (PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
					PowerManager.ACQUIRE_CAUSES_WAKEUP, "Sipdroid.RtpStreamReceiver");
			wl.acquire(1000);
		}
		restoreMode();
	}

	public static float good, late, lost, loss, loss2;
	double avgheadroom,devheadroom;
	int avgcnt;
	public static int timeout;
	int seq,nowSeq,lastSeq=-1;
	
	void empty() {
		try {
			rtp_socket.getDatagramSocket().setSoTimeout(1);
			for (;;){
				rtp_socket.receive(rtp_packet);
			}
		} catch (SocketException e2) {
			if (!Sipdroid.release) e2.printStackTrace();
		} catch (IOException e) {
		}
		try {
			rtp_socket.getDatagramSocket().setSoTimeout(SO_TIMEOUT);
		} catch (SocketException e2) {
			if (!Sipdroid.release) e2.printStackTrace();
		}
		seq = 0;
		lastSeq = -1;
	}
	
	RtpPacket rtp_packet;
	AudioTrack track;
	int maxjitter,minjitter,minjitteradjust;
	int cnt,cnt2,user,luser,luser2,lserver;
	public static int jitter,mu;
	
	void setCodec() {
		synchronized (this) {
			AudioTrack oldtrack;
			
			p_type.codec.init();
			codec = p_type.codec.getTitle();
			mu = p_type.codec.samp_rate()/8000;
			maxjitter = AudioTrack.getMinBufferSize(p_type.codec.samp_rate(), 
					AudioFormat.CHANNEL_CONFIGURATION_MONO, 
					AudioFormat.ENCODING_PCM_16BIT);
			if (maxjitter < 2*2*BUFFER_SIZE*3*mu)
				maxjitter = 2*2*BUFFER_SIZE*3*mu;
			oldtrack = track;
			int streamType = stream();
			AudioUtil.getInstance().setStream(streamType);
			track = new AudioTrack(stream(), p_type.codec.samp_rate(), AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
					maxjitter, AudioTrack.MODE_STREAM);
			maxjitter /= 2*2;
			minjitter = minjitteradjust = 500*mu;
			jitter = 875*mu;
			devheadroom = Math.pow(jitter/5, 2);
			timeout = 1;
			playQueue.clear();
			luser = luser2 = -8000*mu;
			cnt = cnt2 = user = lserver = 0;
			
			if (oldtrack != null) {
				oldtrack.stop();
				oldtrack.release();
			}
			currentSpeakermode = speakermode;
		}
	}
	
	void write(short a[],int b,int c) {
		synchronized (this) {
			if(AudioSettings.isAECOpen){
			int num = c /160;
			for(int i = 0; i < num; i++)
			{
				short[] qin = new short[160];
				System.arraycopy(a, b+i*160, qin, 0, 160);
				queue.offer(qin);		
			}
			}
			user += track.write(a,b,c);
		}
	}

    PowerManager.WakeLock pwl,pwl2;
	WifiManager.WifiLock wwl;
	static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;

	private static final String tag = "RtpStreamReceiver_signal";
	boolean lockLast,lockFirst;
	
	void lock(boolean lock) {
		try {
			if (lock) {
				boolean lockNew = false;
				if (lockFirst || lockLast != lockNew) {
					lockLast = lockNew;
					lock(false);
					lockFirst = false;
					if (pwl == null) {
						PowerManager pm = (PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE);
						pwl = pm.newWakeLock(lockNew?(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP):PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Sipdroid.Receiver");
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
				PowerManager pm = (PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE);
				WifiManager wm = (WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE);
				pwl2 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sipdroid.Receiver");
				pwl2.acquire();
				wwl = wm.createWifiLock(3,"Sipdroid.Receiver");
				wwl.acquire();
			}
		} else if (pwl2 != null) {
			pwl2.release();
			pwl2 = null;
			wwl.release();
		}
	}

	void newjitter(boolean inc) {
		 if (good == 0 || lost/good > 0.01 || call_recorder != null)
			 return;
		 int newjitter = (int)Math.sqrt(devheadroom)*5 + (inc?minjitteradjust:0);
		 if (newjitter < minjitter)
			 newjitter = minjitter;
		 if (newjitter > maxjitter)
			 newjitter = maxjitter;
		 if (!inc && (Math.abs(jitter-newjitter) < minjitteradjust || newjitter >= jitter))
			 return;
		 if (inc && newjitter <= jitter)
			 return;
		 jitter = newjitter;
		 late = 0;
		 avgcnt = 0;
		 luser2 = user;
	}
	public static LinkedBlockingQueue<short[]> queue = new LinkedBlockingQueue<short[]>();
	public static boolean state_flg;
	boolean keepon;
	private int  flow = 0;
	private int currentSpeakermode;

	/** Runs it in a new Thread. */
	public void run() {
		LogUtil.makeLog(tag, "run begin");
		RtpStreamReceiverUtil.onStartReceiving(RtpStreamReceiverType.SINGLE_CALL_RECEIVER);
		boolean nodata = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_NODATA, com.zed3.sipua.ui.Settings.DEFAULT_NODATA);
		keepon = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_KEEPON, com.zed3.sipua.ui.Settings.DEFAULT_KEEPON);

		if (rtp_socket == null) {
			if (DEBUG)
				println("ERROR: RTP socket is null");
			return;
		}

		if (DEBUG)
			println("Reading blocks of max " + buffer.length + " bytes");

		running = true;
		enableBluetooth(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_BLUETOOTH,
				com.zed3.sipua.ui.Settings.DEFAULT_BLUETOOTH));
		restored = false;
		am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
        cr = Receiver.mContext.getContentResolver();
		saveSettings();
		Settings.System.putInt(cr, Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_NEVER);
		am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_OFF);
		am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,AudioManager.VIBRATE_SETTING_OFF);
		if (oldvol == -1) oldvol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		initMode();
		
		int m = 1, expseq, getseq, vm = 1, gap, gseq = -1;
		long timeStamp = 0,lastStamp = 0;
		List<ReceivePacketInfo> mList = new ArrayList<ReceivePacketInfo>();
		
		System.gc();
		empty();
		lockFirst = true;
		int len = 0;
		while (running) {
			if (speakermode != currentSpeakermode) {
				setCodec();
			}
			try {
				rtp_socket.receive(rtp_packet);
				RtpStreamReceiverUtil.onReceive(RtpStreamReceiverType.SINGLE_CALL_RECEIVER, rtp_packet);
				gseq = rtp_packet.getSequenceNumber();
				timeStamp = rtp_packet.getTimestamp();
				len = rtp_packet.getPayloadLength();
				//统计流量
//				if(rtp_packet.getLength()+ flow > 60){
//					flow = rtp_packet.getLength() + 42;
//				}else{
//					flow = 60 ;
//				}
//				FlowStatistics.Voice_Receive_Data = FlowStatistics.Voice_Receive_Data + flow;
			} catch (Exception e) {
//				e.printStackTrace();
			}
			if (running && timeout == 0) {	
				if(lastSeq == -1){
					handleReceiveAudio(buffer,len);
					lastSeq = gseq;
					lastStamp = timeStamp;
					continue;
				}
				mList.add(new ReceivePacketInfo(gseq, buffer,timeStamp,len));
				Collections.sort(mList,new ReceivePacketCompare());
				while(mList.size() > 0){
					if(mList.size() > 0 && !IsNowSeqLarger(mList.get(0).getSeqNum(),lastSeq)){
						mList.remove(0);
					} else {
						if (mList.size() > 0
								&& isExpectSeqNum(mList.get(0)
										.getSeqNum(), lastSeq)) {
							handleReceiveAudio(mList.get(0).getData(),mList.get(0).getLength());
							lastStamp = timeStamp;
							lastSeq = mList.get(0).getSeqNum();
							mList.remove(0);
						} else {
							if (mList.size() != 0
									&& isDelayOverLimitTime(timeStamp,
											lastStamp)) {
								lastStamp = mList.get(0)
										.getTimeStamp();
								lastSeq = mList.get(0).getSeqNum();
								handleReceiveAudio(mList.get(0).getData(),mList.get(0).getLength());
								mList.remove(0);
							} else {
								break;
							}
						}
					}
				}
				
			//原来的保留	
			 if (seq == gseq) {
				 m++;
				 continue;
			 }
			 gap = (gseq - seq) & 0xff;
			 if (gap > 240){
				 continue;
			 }
			 if (seq != 0) {
				 getseq = gseq&0xff;
				 expseq = ++seq&0xff;
				 if (m == RtpStreamSender_signal.m) vm = m;
				 gap = (getseq - expseq) & 0xff;
				 if (gap > 0) {
					 System.out.println("RTP:lost");
					 if (gap > 100) gap = 1;
					 loss += gap;
					 lost += gap;
					 good += gap - 1;
					 loss2++;
				 } else {
					 if (m < vm) {
						 loss++;
						 loss2++;
					 }
				 }
				 good++;
				 if (good > 110) {
					 good *= 0.99;
					 lost *= 0.99;
					 loss *= 0.99;
					 loss2 *= 0.99;
					 late *= 0.99;
				 }
			 }
			 m = 1;
			 seq = gseq;
			}
			
		}
		saveVolume();
		//去掉音量保存功能 。 modify by lwang 2015-01-14
//		am.setStreamVolume(AudioManager.STREAM_MUSIC,oldvol,0);
		restoreSettings();
		enableBluetooth(false);
		//去掉音量保存功能 。 modify by lwang 2015-01-14
//		am.setStreamVolume(AudioManager.STREAM_MUSIC,oldvol,0);
		oldvol = -1;
		p_type.codec.close();
		rtp_socket.close();
		rtp_socket = null;
		cleanupBluetooth();
		RtpStreamReceiverUtil.onStopReceiving(RtpStreamReceiverType.SINGLE_CALL_RECEIVER);
		LogUtil.makeLog(tag, "run end");
	}
	private boolean IsNowSeqLarger(long nowSeq, long lastSeq) {
		if (nowSeq > lastSeq) {
			if ((nowSeq - lastSeq) < 0x7fff)
				return true;
			else
				return false;
		} else if (nowSeq < lastSeq) {
			if ((lastSeq - nowSeq) < 0x7fff)
				return false;
			else
				return true;
		} else
			return false;
	}
	private boolean isExpectSeqNum(long nowSeq,long lastSeq){
		if(lastSeq+1 == nowSeq){
			return true;
		}
		if(lastSeq == 0xffff && nowSeq == 0){
			return true;
		}
		return false;
	}
	private boolean isDelayOverLimitTime(long tailTS,long lastTS){
		int diff = 0;
		if(tailTS >= lastTS){
			diff=(int)(tailTS - lastTS)/8;
			if(diff > DeviceVideoInfo.allow_audio_MaxDelay){
				return true;
			}else{
				return false;
			}
		}else{
			if((lastTS - tailTS) > 0x7fffffff){
				diff=(int)((0x100000000l-lastTS+tailTS)/8);
				if(diff > DeviceVideoInfo.allow_audio_MaxDelay)
					return true;
				else
					return false;
			}else{
				return true;
			}
		}
	}
	private final int MIN_MEAN_VALUE = 10000;
	private int lastContinueSilenceCount = 0;
	int IsActiveVoice(short[] pdata,int length)
	{
		int result = 0;
		int i;
		long tvalue = 0, meanvalue;
		for (i = 0; i < length; i++) {
			tvalue += pdata[i] *pdata[i];
		}
		meanvalue = tvalue / length;
		if (meanvalue < MIN_MEAN_VALUE) {
			lastContinueSilenceCount++;
			result = 0;
		} else {
			lastContinueSilenceCount = 0;
			result = 1;
		}
		return result;
	}
	void handleReceiveAudio(byte[] todecode,int length){
		short[] lin = new short[1600];
		int len;
		len =  p_type.codec.decode(todecode, lin, length);
		short[] temp = new short[len];
		System.arraycopy(lin, 0, temp, 0, len);
		 if(len % 160 == 0){
			 for(int i= 0;i<len/160;i++){
				 short[] tmp = new short[160];
				 System.arraycopy(lin, i*160, tmp, 0, 160);
				 try {
					playQueue.push(tmp);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			 }
		 }
	}
	/** Debug output */
	private static void println(String str) {
		if (!Sipdroid.release) System.out.println("RtpStreamReceiver: " + str);
	}

	public static int byte2int(byte b) { // return (b>=0)? b : -((b^0xFF)+1);
		// return (b>=0)? b : b+0x100;
		return (b + 0x100) % 0x100;
	}

	public static int byte2int(byte b1, byte b2) {
		return (((b1 + 0x100) % 0x100) << 8) + (b2 + 0x100) % 0x100;
	}
	void writeNoqueue(short a[],int b,int c) {
		synchronized (this) {
			int num = c /160;
			user += track.write(a,b,num*160);
		}
	}
	class TrackPlayer implements Runnable {
		short lin2[] = new short[160];
		TrackPlayQueue mQueue;
		public TrackPlayer(TrackPlayQueue tQueue) {
			trackRunning = true;
			mQueue = tQueue;
		}
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
			int server, headroom, todo, len = 160;
			setCodec();
			track.play();
			while(trackRunning){
				lock(true);
				if (Receiver.call_state == UserAgent.UA_STATE_HOLD) {
					lock(false);
					track.pause();
					while (running && Receiver.call_state == UserAgent.UA_STATE_HOLD) {
						try {
							sleep(1000);
						} catch (InterruptedException e1) {
						}
					}
					track.play();
					System.gc();
					timeout = 1;
					luser = luser2 = -8000*mu;
				}
					short[] s = null;
					try {
						s = mQueue.pop();
						if (!RtpStreamReceiverUtil.needWriteAudioData()) {
							continue;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(s != null){
						IsActiveVoice(s, s.length);
					}else{
						
					}
					if (timeout != 0) {
						track.pause();
						if(AudioSettings.isAECOpen){
							queue.clear();
						}
						for (int i = maxjitter*2; i > 0; i -= 160){
							writeNoqueue(lin2,0,160);///??
						}
						cnt += maxjitter*2;
						track.play();
					}
					timeout = 0;
					if (running && timeout == 0) {		
						 server = track.getPlaybackHeadPosition();
						 headroom = user-server;
						 if (headroom > 2*jitter)
							 cnt += len;
						 else
							 cnt = 0;
						 
						 if (lserver == server){
							 cnt2++;
						 }
						 else
							 cnt2 = 0;

						if (rtp_packet.getPayloadType() != p_type.number
								&& p_type.change(rtp_packet.getPayloadType())) {
							saveVolume();
							setCodec();
							restoreVolume();
						}

						 avgheadroom = avgheadroom * 0.99 + (double)headroom * 0.01;
						 if (avgcnt++ > 300)
							 devheadroom = devheadroom * 0.999 + Math.pow(Math.abs(headroom - avgheadroom),2) * 0.001;
			 			 if (headroom < 250*mu) {
			 				 late++;
			 				 newjitter(true);
			 				 System.out.println("RTP:underflow "+(int)Math.sqrt(devheadroom));
							 todo = jitter - headroom;
							 if(s != null){
								 write(s, 0, 160);
								 s = null;
							 }
								 write(lin2,0,160);
								 write(lin2,0,160);
			 			 }else if(headroom < 500){
							 if(s != null){
								 write(s, 0, 160);
								 s = null;
							 }
							 if(lastContinueSilenceCount > 4){
//								 todo = jitter - headroom;
//								 for(int i= todo;i>160;i-= 160){
									 write(lin2,0,160);
//								 }
			 				}
//							 MyLog.e(TAG.voice_delay, "no add,count = "+lastContinueSilenceCount);
						 }else if(headroom < 700){
							 if(s != null){
								 write(s, 0, 160);
								 s = null;
							 }
							 if(lastContinueSilenceCount > 6){
									 write(lin2,0,160);
			 				}
						 }
						 if ((cnt > 500*mu ||(headroom > 2000)) && (cnt2 < 2)) {
//							 todo = headroom - jitter;
							 if(headroom >5000){
							 }else if(headroom >4000){
								 discardCount += len;
								 if(s!= null){
								 if(IsActiveVoice(s, 160) == 1 ){
									 write(s,0,160);
								 }else{
								 }
								 } 
							 }else 
							 if (headroom > 3000){
								 discardCount += len;
								 if(s!= null){
								 if(IsActiveVoice(s, 160) == 1 ){
									 write(s,0,160);
								 }else if(lastContinueSilenceCount < 4){
									 write(s,0,160);
								 }else{
								 }
								 }
							 }else if(headroom > 2000){
								 discardCount += len;
								 if(s!= null){
								 if(IsActiveVoice(s, 160) == 1 ){
									 write(s,0,160);
								 }else if(lastContinueSilenceCount < 5){
									 write(s,0,160);
								 }else{
								 }
								 }
							 }else{
								 discardCount += len;
								 if(s!= null){
								 if(IsActiveVoice(s, 160) == 1 ){
									 write(s,0,160);
								 }else if(lastContinueSilenceCount < 6){
									 write(s,0,160);
								 }else{
								 }
								 }
							 }
						 } else {
							 if(s != null){
								 write(s,0,160);
							 }
						 }

						 if (user >= luser + 8000*mu && (
								 Receiver.call_state == UserAgent.UA_STATE_INCALL ||
								 Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL)) {
							 if (luser == -8000*mu || getMode() != speakermode) {
								 saveVolume();
//								 setMode(speakermode);
								 restoreVolume();
							 }
							 luser = user;
							 if (user >= luser2 + 160000*mu)
								 newjitter(false);
						 }
						 lserver = server;
					}
			}
			lock(false);
			if (track != null) {
				track.stop();
				track.release();
				track = null;
			}
		}

	}
}
