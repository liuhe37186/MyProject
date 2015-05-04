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
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.audio.audioEngine.SlientCheck;
import org.zoolu.tools.MyLog;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zed3.ace.TestTools;
import com.zed3.codecs.Codecs;
import com.zed3.codecs.G711;
import com.zed3.location.MemoryMg;
import com.zed3.log.Logger;
import com.zed3.net.RtpPacket;
import com.zed3.net.RtpSocket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.Sipdroid;
import com.zed3.utils.LogUtil;
import com.zed3.utils.MyHandler;
import com.zed3.utils.Tools;
import com.zed3.utils.Zed3Log;

/**
 * RtpStreamSender is a generic stream sender. It takes an InputStream and sends
 * it through RTP.
 */
public class RtpStreamSender_group extends Thread {
	int flow = 0;
//	Timer timer = null;
//	RtpPacket pp = null;
	int length = 0;
	String HTag = "htag";
	private byte[] bLock = new byte[0];
	final int discardNum = 2;
	int sendCount = 0;
	private Queue<RtpPacket> storage = new LinkedList<RtpPacket>();
//	TimerTask ttask = new TimerTask() {//20ms发送一个包,因为一直发送，所以不会累积
//		@Override
//		public void run() {
////			MyLog.e(HTag, "zzc timer run wanna get bLock, size = "+storage.size());
//			synchronized (bLock) {
////				MyLog.e(HTag, "zzc timer run get bLock, size = "+storage.size());
//				if (!storage.isEmpty() && running && rtp_socket != null) {
//					try {
//						MyLog.e(HTag, "zzc storage.poll(), size = "+storage.size());
//						pp = storage.poll();
//						length = rtp_socket.send(pp) + 42;
//						MyLog.e(HTag, "to send , rtp_packet.seq = "+pp.getSequenceNumber());
//						if(length < 60){
//							flow = 60;
//						}else{
//							flow = length;
//						}
//						FlowStatistics.Voice_Send_Data = flow
//								+ FlowStatistics.Voice_Send_Data;
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//
//			}
//		}
//	};

	int MINIMUM_VALUE = 60;
	int IP_UDP_VALUE = 42;
	// guojunfeng add e
	/** Whether working in debug mode. */
	public static boolean DEBUG = true;
	/** The RtpSocket */
	RtpSocket rtp_socket = null;

	/** The vadcheck */
	SlientCheck slientChk = null;

	/** Payload type */
	Codecs.Map p_type;

	/** Number of frame per second */
	int frame_rate;

	/** Number of bytes per frame */
	int frame_size;

	/**
	 * Whether it works synchronously with a local clock, or it it acts as slave
	 * of the InputStream
	 */
	boolean do_sync = true;

	/**
	 * Synchronization correction value, in milliseconds. It accellarates the
	 * sending rate respect to the nominal value, in order to compensate program
	 * latencies.
	 */
	int sync_adj = 0;

	/** Whether it is running */
	private boolean running = false;
	boolean muted = false;

	// DTMF change
	String dtmf = "";
	int dtmf_payload_type = 101;

	// Added by zzhan 2011-10-26
	private boolean sndSuspend = false;
	// 10 * 1000 --> 50 * 1000 modify by oumogang 2014-03-26
	private final int INTERVAL_RTP_SEND_3G = /* 10 * 1000 */20 * 1000;
	private long intervalSendOfSuspend = 0;
	private long SuspendTime = 0;

	public void SndSuspend() {
		mPTTPause = true;
		sndSuspend = true;
		SuspendTime = System.currentTimeMillis();
	}

	public void SndResume() {
		mPTTPause = false;
		sndSuspend = false;
		// clear old data add by oumogang 2014-03-13
		synchronized (bLock) {
			if (!storage.isEmpty()) {//如果不是频繁点击，storage.size == 0
				MyLog.e(HTag, "zzc SndResume() clear size = "+storage.size());
				storage.clear();
			}
		}
	}

	private static HashMap<Character, Byte> rtpEventMap = new HashMap<Character, Byte>() {
		{
			put('0', (byte) 0);
			put('1', (byte) 1);
			put('2', (byte) 2);
			put('3', (byte) 3);
			put('4', (byte) 4);
			put('5', (byte) 5);
			put('6', (byte) 6);
			put('7', (byte) 7);
			put('8', (byte) 8);
			put('9', (byte) 9);
			put('*', (byte) 10);
			put('#', (byte) 11);
			put('A', (byte) 12);
			put('B', (byte) 13);
			put('C', (byte) 14);
			put('D', (byte) 15);
		}
	};
	// DTMF change

	CallRecorder call_recorder = null;

	/**
	 * Constructs a RtpStreamSender.
	 * 
	 * @param input_stream
	 *            the stream to be sent
	 * @param do_sync
	 *            whether time synchronization must be performed by the
	 *            RtpStreamSender, or it is performed by the InputStream (e.g.
	 *            the system audio input)
	 * @param payload_type
	 *            the payload type
	 * @param frame_rate
	 *            the frame rate, i.e. the number of frames that should be sent
	 *            per second; it is used to calculate the nominal packet time
	 *            and,in case of do_sync==true, the next departure time
	 * @param frame_size
	 *            the size of the payload
	 * @param src_socket
	 *            the socket used to send the RTP packet
	 * @param dest_addr
	 *            the destination address
	 * @param dest_port
	 *            the destination port
	 */
	public RtpStreamSender_group(boolean do_sync, Codecs.Map payload_type,
			long frame_rate, int frame_size, SipdroidSocket src_socket,
			String dest_addr, int dest_port, CallRecorder rec) {
		slientChk = new SlientCheck();
		init(do_sync, payload_type, frame_rate, frame_size, src_socket,
				dest_addr, dest_port);
		call_recorder = rec;
		Logger.i(needLog, tag, System.currentTimeMillis()
				+ "AudioRecord   new RtpStreamSender()");
	}

	/** Inits the RtpStreamSender */
	private void init(boolean do_sync, Codecs.Map payload_type,
			long frame_rate, int frame_size, SipdroidSocket src_socket,
			String dest_addr, int dest_port) {
		this.p_type = payload_type;
		this.frame_rate = (int) frame_rate;
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext)
				.getString(Settings.PREF_SERVER, "")
				.equals(Settings.DEFAULT_SERVER))
			switch (payload_type.codec.number()) {
			case 0:
			case 8:
				this.frame_size = 1024;
				break;
			case 9:
				this.frame_size = 960;
				break;
			default:
				this.frame_size = frame_size;
				break;
			}
		else
			this.frame_size = frame_size;
		this.do_sync = do_sync;
		try {
			rtp_socket = new RtpSocket(src_socket,
					InetAddress.getByName(dest_addr), dest_port);
		} catch (Exception e) {
			if (!Sipdroid.release)
				e.printStackTrace();
		}
		// init slient check
		if (slientChk.WebRtcVadCreate() == 0)
			MyLog.e(tag, "Create ok");
		else
			MyLog.e(tag, "Create error");
		if (slientChk.WebRtcVadInit() == 0)
			MyLog.e(tag, "Init ok");
		else
			MyLog.e(tag, "Init error");
		if (MemoryMg.getInstance().isAudioVAD) {// 静音检测
			payload_type.codec.setVad(slientChk);
		}
	}

	/** Sets the synchronization adjustment time (in milliseconds). */
	public void setSyncAdj(int millisecs) {
		sync_adj = millisecs;
	}

	/** Whether is running */
	public boolean isRunning() {
		return running;
	}

	public boolean mute() {
		return muted = !muted;
	}

	public static int delay = 0;
	public static boolean changed;

	/** Stops running */
	public void halt() {
		running = false;
		// should not stop recorder here. delete by mou 2014-11-25
		// if(record != null){
		// record.stop();
		// }
	}

	Random random;
	double smin = 200, s;
	int nearend;

	void calc(short[] lin, int off, int len) {
		int i, j;
		double sm = 30000, r;

		for (i = 0; i < len; i += 5) {
			j = lin[i + off];
			s = 0.03 * Math.abs(j) + 0.97 * s;
			if (s < sm)
				sm = s;
			if (s > smin)
				nearend = 3000 * mu / 5;
			else if (nearend > 0)
				nearend--;
		}
		r = (double) len / (100000 * mu);
		if (sm > 2 * smin || sm < smin / 2)
			smin = sm * r + smin * (1 - r);
	}

	void calc1(short[] lin, int off, int len) {
		int i, j;

		for (i = 0; i < len; i++) {
			j = lin[i + off];
			lin[i + off] = (short) (j >> 2);
		}
	}

	void calc2(short[] lin, int off, int len) {
		int i, j;

		for (i = 0; i < len; i++) {
			j = lin[i + off];
			lin[i + off] = (short) (j >> 1);
		}
	}

	void calc10(short[] lin, int off, int len) {
		int i, j;

		for (i = 0; i < len; i++) {
			j = lin[i + off];
			if (j > 16350)
				lin[i + off] = 16350 << 1;
			else if (j < -16350)
				lin[i + off] = -16350 << 1;
			else
				lin[i + off] = (short) (j << 1);
		}
	}

	void noise(short[] lin, int off, int len, double power) {
		int i, r = (int) (power * 2);
		short ran;

		if (r == 0)
			r = 1;
		for (i = 0; i < len; i += 4) {
			ran = (short) (random.nextInt(r * 2) - r);
			lin[i + off] = ran;
			lin[i + off + 1] = ran;
			lin[i + off + 2] = ran;
			lin[i + off + 3] = ran;
		}
	}

	public static int m;
	int mu;

	private String tag = "RtpStreamSender_group";
	public static String codecName;
	private boolean needLog = false;
	public static long time = 0;
	public static boolean mPTTPause;
	private int seqn = 0;
	private long mutedTimeMillion = 0;
	private int mframeNumber;
	/** Runs it in a new Thread. */
//	private int mPTIME;
//	private int mStorageBufferSize = 25;
	AudioRecord record = null;

	public void run() {
		MyLog.e("htag", "group- run begin ,time = "+ System.currentTimeMillis());
		LogUtil.makeLog(tag, "run begin");
		int timeCount = 0;

//		mPTIME = (MemoryMg.SdpPtime == 0 ? MemoryMg.PTIME : MemoryMg.SdpPtime);

//		mStorageBufferSize = 200 / mPTIME + 1;
		WifiManager wm = (WifiManager) Receiver.mContext
				.getSystemService(Context.WIFI_SERVICE);
		long lastscan = 0;

		if (rtp_socket == null)
			return;

		double p = 0;
		boolean improve = PreferenceManager.getDefaultSharedPreferences(
				Receiver.mContext).getBoolean(Settings.PREF_IMPROVE,
				Settings.DEFAULT_IMPROVE);
		boolean selectWifi = PreferenceManager.getDefaultSharedPreferences(
				Receiver.mContext).getBoolean(
				com.zed3.sipua.ui.Settings.PREF_SELECTWIFI,
				com.zed3.sipua.ui.Settings.DEFAULT_SELECTWIFI);
		/** ------????????------- */
		int micgain = 0;
//		long last_tx_time = 0;
//		long next_tx_delay;
//		long now;
		running = true;
		m = 1;
		int dtframesize = 4;

		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		mu = p_type.codec.samp_rate() / 8000;

		int samp_rate = p_type.codec.samp_rate();
		// 获取最小缓冲大小
		int min = AudioRecord.getMinBufferSize(
		/* p_type.codec.samp_rate() */samp_rate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		MyLog.i(tag, "getMinBufferSize() min = " + min);

		frame_rate = p_type.codec.samp_rate() / frame_size;
		/** ----------OUMOGANG */
		long frame_period = 1000 / frame_rate;
		frame_rate *= 1.5;

		p_type.codec.init();
		codecName = p_type.codec.name();
		mframeNumber = frame_size * 2 / 320;
		println("Sample rate  = " + p_type.codec.samp_rate());
		println("Buffer size = " + min);

		// AudioRecord record = null;

		short[] lin = new short[frame_size * (frame_rate + 1)];
		//
		int num, ring = 0, pos;
		random = new Random();
		InputStream alerting = null;
		try {
			alerting = Receiver.mContext.getAssets().open("alerting");
		} catch (IOException e2) {
			if (!Sipdroid.release)
				e2.printStackTrace();
		}
		// p_type.codec.init();
		if (running /* &&UserAgent.ua_ptt_mode */) {
//			timer = new Timer();
//			timer.schedule(ttask, 0, mPTIME/4*3  /*
//											 * (MemoryMg.SdpPtime == 0 ?
//											 * MemoryMg.PTIME :
//											 * MemoryMg.SdpPtime)
//											 */);
			MyLog.e(tag, "ptime schedule val:"
					+ (MemoryMg.SdpPtime == 0 ? MemoryMg.PTIME
							: MemoryMg.SdpPtime));
		}
		boolean isException = false;
		while (running) {
			// move to the head of while. modify by oumogang 2014-03-24
			// Added by zzhan 2011-10-26
			// Must send something to server in 3G version when do not talk.
			if (sndSuspend) {
				if(record != null && record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
					record.stop();
					sendCount = 0;
//					TestTools.release();
					MyLog.e(HTag, "--------stop called");
				}
				if (SystemClock.uptimeMillis() - intervalSendOfSuspend > INTERVAL_RTP_SEND_3G) {
					intervalSendOfSuspend = SystemClock.uptimeMillis();

					MyLog.e(tag, "NAT process...");
					// Send three times
					/** ---when end ptt call do it--- */
					for (int i = 0; i < 3; i++) {
						byte[] data = new byte[1];
						DatagramPacket pack = new DatagramPacket(data, 1);
						pack.setAddress(rtp_socket.GetAddress());
						pack.setPort(rtp_socket.GetPort());
						try {
							rtp_socket.GetSocket().send(pack);
							sleep(10);
							MyLog.e(tag, "NAT send " + i + " port:"
									+ rtp_socket.GetPort());
						} catch (Exception e) {
							MyLog.e(tag, "NAT exception" + e.toString());
							if (isSocketInvalidArgmentException(e)
									&& !isException) {
								isException = true;
								onRtpStreamSenderException(e);
							}
						}
					}
				}

				int count = 0;
				while (sndSuspend && count < frame_size * 2 / 320) {
					count++;
					try {
						sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				// should continue ,add by oumogang 2014-04-04
				continue;
			}else{
				if(record != null && record.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED){
					record.startRecording();
					MyLog.e(HTag, "--------startRecording called");
				}
			}
			byte[] buffer = new byte[frame_size + 12];

			RtpPacket rtp_packet = new RtpPacket(buffer, 0);
			// add by zdx 2014-09-06
			rtp_packet.setPackedTime(System.currentTimeMillis());

			rtp_packet.setPayloadType(p_type.number);
			// 判断是否需要初始化录音器
			if (changed || record == null) {
				if (record != null) {
					record.stop();
					record.release();
					if (RtpStreamReceiver_group.samsung) {
						AudioManager am = (AudioManager) Receiver.mContext
								.getSystemService(Context.AUDIO_SERVICE);
						am.setMode(AudioManager.MODE_IN_CALL);
						am.setMode(AudioManager.MODE_NORMAL);
					}
				}
				changed = false;
				MyLog.e(HTag, "PTIME = "+frame_period +",MIN = "+min);
				// add by oumogang 2013-01-04
				int frameSize = (min<3360? 3360:min);//3200+160//1600*10;//Math.max(2560, min) * 4;
				record = AudioRecordUitls.getRecord(
						MediaRecorder.AudioSource.MIC,
						p_type.codec.samp_rate(),
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, frameSize);
				MyLog.e("htag", "group- record wanna init ,time = "+ System.currentTimeMillis());
				MyLog.e(HTag, "frameSize = "+frameSize);
				MyLog.i(tag, System.currentTimeMillis()
						+ "AudioRecord   new AudioRecord() min = ");
				// 是否成功初始化录音器
				if (record.getState() == /* AudioRecord.STATE_INITIALIZED */AudioRecord.STATE_UNINITIALIZED) {
					Logger.i(needLog, tag, "AudioRecord  fail 录音器初始化失败 ");
					Receiver.engine(Receiver.mContext).rejectcall();
					Logger.i(needLog, tag, "AudioRecord  rejectcall 拒绝通话 ");
					AudioRecordUtils2.releaseAudioRecord(record);
					Logger.i(needLog, tag, "AudioRecord  release 释放资源 ");
					record = null;
					break;
				}
				record.startRecording();
				MyLog.e("htag", "group- record startRecording ,time = "+ System.currentTimeMillis());
				micgain = (int) (Settings.getMicGain() * 10);
			}
			// 是否需要重新启动录音
			if (muted || Receiver.call_state == UserAgent.UA_STATE_HOLD) {
				if (Receiver.call_state == UserAgent.UA_STATE_HOLD)
					RtpStreamReceiver_group.restoreMode();
				record.stop();
				while (running
						&& (muted || Receiver.call_state == UserAgent.UA_STATE_HOLD)) {
					try {
						sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						break;
					}
				}
				record.startRecording();
			}
			// DTMF change start
			/** -------- gsm 和 amr 都不走---------- */
			if (dtmf.length() != 0) {
				byte[] dtmfbuf = new byte[dtframesize + 12];
				RtpPacket dt_packet = new RtpPacket(dtmfbuf, 0);
				dt_packet.setPayloadType(dtmf_payload_type);
				dt_packet.setPayloadLength(dtframesize);
				dt_packet.setSscr(rtp_packet.getSscr());
				long dttime = time;
				int duration;

				for (int i = 0; i < 6; i++) {
					time += 160;
					duration = (int) (time - dttime);
					dt_packet.setSequenceNumber(seqn++);
					dt_packet.setTimestamp(dttime);
					dtmfbuf[12] = rtpEventMap.get(dtmf.charAt(0));
					dtmfbuf[13] = (byte) 0x0a;
					dtmfbuf[14] = (byte) (duration >> 8);
					dtmfbuf[15] = (byte) duration;
					try {
						rtp_socket.send(dt_packet);
						Logger.i(needLog, "RtpStreamSender send",
								"rtp_socket.send(dt_packet)");
						sleep(20);
						Logger.i(needLog, "RtpStreamSender send",
								"rtp_socket.send(dt_packet)");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				for (int i = 0; i < 3; i++) {
					duration = (int) (time - dttime);
					dt_packet.setSequenceNumber(seqn);
					dt_packet.setTimestamp(dttime);
					dtmfbuf[12] = rtpEventMap.get(dtmf.charAt(0));
					dtmfbuf[13] = (byte) 0x8a;
					dtmfbuf[14] = (byte) (duration >> 8);
					dtmfbuf[15] = (byte) duration;
					try {
						rtp_socket.send(dt_packet);
					} catch (Exception e1) {
					}
				}
				time += 160;
				seqn++;
				dtmf = dtmf.substring(1);
			}
			// DTMF change end
//			if (frame_size < 480) {
//				now = System.currentTimeMillis();
//				next_tx_delay = frame_period - (now - last_tx_time);
//				last_tx_time = now;
//				if (next_tx_delay > 0) {
//					try {
//						sleep(next_tx_delay);
//					} catch (InterruptedException e1) {
//					}
//					last_tx_time += next_tx_delay - sync_adj;
//				}
//			}
			
			pos = (ring + delay * frame_rate * frame_size)
					% (frame_size * (frame_rate + 1));
//			if(System.currentTimeMillis()%1000 < 200){
//				MyLog.e(HTag, "read record stop --- ");
//				record.stop();
//			}else{
//				if(AudioRecord.RECORDSTATE_STOPPED == record.getState()){
//					record.startRecording();
//					MyLog.e(HTag, "read record start +++");
//				}
//			}
			// 在lin数组（）中，从pos角标开始，读取frame_size（160）个元素，返回有效元素的个数，有可能不等于160；
			num = record.read(lin, pos, frame_size);
			if(num != frame_size){
				MyLog.e(HTag, "readNum error !!!!!!!!! num = "+num+",frame_size ="+frame_size);
			}
			MyLog.e(HTag, "readNum = "+num);
			if (num <= 0)
				continue;
			if (!p_type.codec.isValid())
				continue;

			int d = (MemoryMg.SdpPtime == 0) ? MemoryMg.PTIME
					: MemoryMg.SdpPtime;

			timeCount++;
			boolean isPttMode = Receiver.GetCurUA().IsPttMode();
			if (isPttMode && timeCount == (300 / d)) {
				timeCount = 0;
				short[] lin2 = new short[frame_size];
				System.arraycopy(lin, pos, lin2, 0, frame_size);
				byte[] speechBuffer = Tools.shortArray2ByteArray(lin2);
				if (speechBuffer.length > 0) {
					int t = 0;
					for (int i = 0; i < speechBuffer.length; i++) {
						t += Math.abs(speechBuffer[i]);
					}
					t /= speechBuffer.length;
					MyHandler.sendMessage(t);
				}
			}
			// Logger.i(needLog, tag,
			// "time for read : "
			// + (System.currentTimeMillis() - begin));
			// MyLog.i(tag, "record.read(lin,pos,frame_size)"
			// + "\r\n lin.len = " + lin.length + "\r\n pos = " + pos
			// + "\r\n frame_size = " + frame_size + "\r\n num = "
			// + num);// 160
			//
			// Logger.i(needLog, tag, "debug 707");

			// Call recording: Save the frame to the CallRecorder.
			if (call_recorder != null) {
				Logger.i(needLog, tag, "debug 707");
				call_recorder.writeOutgoing(lin, pos, num);
			}
//			short[] linn = new short[num];
//			System.arraycopy(lin, pos, linn, 0, num);
//			TestTools.write2FileMIC(linn);
			if (RtpStreamReceiver_group.speakermode == AudioManager.MODE_NORMAL) {
				calc(lin, pos, num);
				Logger.i(needLog, tag, "debug 707");
				if (RtpStreamReceiver_group.nearend != 0
						&& RtpStreamReceiver_group.down_time == 0) {
					Logger.i(needLog, tag, "debug 707");
					noise(lin, pos, num, p / 2);
				} else if (nearend == 0)
					p = 0.9 * p + 0.1 * s;
			} else
				switch (micgain) {
				case 1:
					Logger.i(needLog, tag, "debug 728");
					calc1(lin, pos, num);
					break;
				case 2:
					Logger.i(needLog, tag, "debug 732");
					calc2(lin, pos, num);
					break;
				case 10:
					Logger.i(needLog, tag, "debug 736");
					calc10(lin, pos, num);
					break;
				}
			if (Receiver.call_state != UserAgent.UA_STATE_INCALL
					&& Receiver.call_state != UserAgent.UA_STATE_OUTGOING_CALL
					&& alerting != null &&
					// Added by zzhan 2011-11-04
					!UserAgent.ua_ptt_mode) {
				Logger.i(needLog, tag, "debug 744");
				try {
					if (alerting.available() < num / mu)
						// Logger.i(needLog,tag, "debug 747");
						alerting.reset();
					// Logger.i(needLog,tag, "debug 749");
					alerting.read(buffer, 12, num / mu);
				} catch (IOException e) {
					if (!Sipdroid.release)
						e.printStackTrace();
					// Logger.i(needLog,tag, "debug 753");
				}
				if (p_type.codec.number() != 8) {
					// Logger.i(needLog,tag, "debug 765");
					G711.alaw2linear(buffer, lin, num, mu);
					num = p_type.codec.encode(lin, 0, buffer, num);
				}
			} else {
				// Logger.i(needLog,tag, "debug 764");
				
//				short[] linn = new short[num];
//				System.arraycopy(lin, pos, linn, 0, num);
//				TestTools.write2FileMIC(linn);
				num = p_type.codec.encode(lin, ring
						% (frame_size * (frame_rate + 1)), buffer, num);

			}
			if (num == 0) {// slience packet,no need to send;
				if (p_type.codec.number() == 9)
					time += frame_size / 2;
				else
					time += 160 * mframeNumber;
				continue;
			}
			ring += frame_size;

			if (!muted)
				rtp_packet.setSequenceNumber(seqn++);

			rtp_packet.setTimestamp(time);
			rtp_packet.setPayloadLength(num);
			try {
				// Modified by zzhan 2011-10-26
				if (!sndSuspend) {
					SuspendTime = System.currentTimeMillis();
				}
				if (rtp_packet.getPackedTime() <= SuspendTime) {
					if (!muted) {
						synchronized (bLock) {
//							if (storage.size() >= mStorageBufferSize){
//								MyLog.e(HTag, "zzc to clear if mStorageBuffer too big, size = "+storage.size());
//								MyLog.e(HTag, "zzc to clear , mStorageBufferSize = "+mStorageBufferSize);
//								//storage.clear();
//							}
							MyLog.e(HTag, "to offer , rtp_packet.seq = "+rtp_packet.getSequenceNumber());
//							storage.offer(rtp_packet);
							if(rtp_packet != null){
								MyLog.e(HTag, "send pp 1 seqNum = "+rtp_packet.getSequenceNumber());
								rtp_socket.send(rtp_packet);
							}
							MyLog.e("htag", "group- record offer packet ,time = "+ System.currentTimeMillis());
							long tmpTime = System.currentTimeMillis();
							MyLog.e(HTag, "offer  curTime = "+tmpTime);
							if (m == 2) {
								MyLog.e(HTag, "m = 2,offer same package");
//								storage.offer(rtp_packet);
								if(rtp_packet != null){
									MyLog.e(HTag, "send pp 2 seqNum = "+rtp_packet.getSequenceNumber());
									rtp_socket.send(rtp_packet);
								}
							}
						}

						mutedTimeMillion = 0;
					} else {// 静音时

						if (mutedTimeMillion == 0)
							mutedTimeMillion = System.currentTimeMillis();
						// 固定时间间隔发个空包
						if (System.currentTimeMillis() - mutedTimeMillion > /* 20000 */INTERVAL_RTP_SEND_3G) {
							mutedTimeMillion = 0;

							MyLog.e(tag,
									"send audio null packet:"
											+ System.currentTimeMillis());

							byte[] b = "0".getBytes();
							byte[] newbuf = new byte[1 + 12];
							System.arraycopy(b, 0, newbuf, 12, 1);

							RtpPacket dt_packet = new RtpPacket(newbuf, 0);
							dt_packet.setPayloadType(p_type.number);
							dt_packet.setPayloadLength(1);
							dt_packet.setSequenceNumber(seqn);
							dt_packet.setTimestamp(time);
							// dt_packet.setMarker(true);// 单个NAL 不需要分片为true
							length = rtp_socket.send(dt_packet) + 42;

							seqn++;
							// time += (160 * 10 * 10);// 1秒大约10个包
						}
					}
				} else {
					MyLog.e(HTag, "never called!!!!!!!!!!");
					rtp_packet = null;
				}
			} catch (Exception e) {

			}

			if (!muted) {
				if (p_type.codec.number() == 9)
					time += frame_size / 2;
				else
					time += /* 160 */160 * mframeNumber;// frame_size;
			}

			if (RtpStreamReceiver_group.good != 0
					&& RtpStreamReceiver_group.loss
							/ RtpStreamReceiver_group.good > 0.01) {
				if (selectWifi && Receiver.on_wlan
						&& SystemClock.elapsedRealtime() - lastscan > 10000) {
					wm.startScan();
					lastscan = SystemClock.elapsedRealtime();
				}
				if (improve
						&& delay == 0
						&& (p_type.codec.number() == 0
								|| p_type.codec.number() == 8 || p_type.codec
								.number() == 9))
					m = 2;
				else
					m = 1;
			} else
				m = 1;

		}
//		if (timer != null) {
//			timer.cancel();
//			timer = null;
//		}

		if (slientChk != null) // vadcheck release
			if (slientChk.WebRtcVadFree() == 0)
				MyLog.e(tag, "free ok");
			else
				MyLog.e(tag, "free error");
		if (record != null) {
			// AudioRecordUtils.releaseAudioRecord(record);
			AudioRecordUitls.releaseRecord(record);
		}
		m = 0;
		/** --- 注销编解码器 ---- */
		p_type.codec.close();
		rtp_socket.close();
		rtp_socket = null;

		if (DEBUG)
			println("rtp sender terminated");

		MyLog.e(tag, "run terminated.");
		LogUtil.makeLog(tag, "run end");
	}

	/** Debug output */
	private static void println(String str) {
		if (!Sipdroid.release)
			System.out.println("RtpStreamSender: " + str);
	}

	/** Set RTP payload type of outband DTMF packets. **/
	public void setDTMFpayloadType(int payload_type) {
		dtmf_payload_type = payload_type;
	}

	/** Send outband DTMF packets */
	public void sendDTMF(char c) {
		dtmf = dtmf + c; // will be set to 0 after sending tones
	}

	// DTMF change

	private boolean isSocketInvalidArgmentException(Exception e) {
		if (e != null) {

			if (e instanceof SocketException) {

				Zed3Log.debug("testptt",
						"RtpStreamSender#onRtpStreamSenderException is socket exception");

				SocketException sktException = (SocketException) e;

				String message = sktException.getMessage();

				if (!TextUtils.isEmpty(message)) {

					if (message.contains("Invalid argument")) {

						return true;

					}
				}
			}
		}
		return false;
	}

	/**
	 * 处理 java.net.SocketException: sendto failed: EINVAL (Invalid argument)
	 * 当发生以上异常之后，通过{@link UserAgent} 关闭media
	 * 
	 * @param e
	 */
	private void onRtpStreamSenderException(Exception e) {

		Zed3Log.debug("testptt",
				"RtpStreamSender#onRtpStreamSenderException enter exception object = "
						+ e);

		Receiver.engine(SipUAApp.mContext).GetCurUA()
				.onRtpStreamSenderException();

	}

}
