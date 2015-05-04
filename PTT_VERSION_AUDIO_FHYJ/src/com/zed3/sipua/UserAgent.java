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

package com.zed3.sipua;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import org.zoolu.net.IpAddress;
import org.zoolu.sdp.AttributeField;
import org.zoolu.sdp.ConnectionField;
import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sdp.TimeField;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.CallListenerAdapter;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.call.SdpTools;
import org.zoolu.sip.header.ContentLengthHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.SipHeaders;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;
import org.zoolu.tools.MyLog;
import org.zoolu.tools.Parser;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.BluetoothPaControlUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.codecs.Codec;
import com.zed3.codecs.Codecs;
import com.zed3.flow.TotalFlowThread;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.location.GPSPacket;
import com.zed3.location.GpsTools;
import com.zed3.location.MemoryMg;
import com.zed3.location.MyHandlerThread;
import com.zed3.log.CrashHandler;
import com.zed3.media.JAudioLauncher;
import com.zed3.media.RtpStreamReceiver_group;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.media.TipSoundPlayer;
import com.zed3.media.TipSoundPlayer.Sound;
import com.zed3.media.mediaButton.MediaButtonReceiver;
import com.zed3.sipua.PttGrp.E_Grp_State;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.message.CommonUtil;
import com.zed3.sipua.message.MessageSender;
import com.zed3.sipua.message.MmsMessageService;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.message.SmsMmsReceiver;
import com.zed3.sipua.ui.ActvityNotify;
import com.zed3.sipua.ui.MyHeartBeatReceiver;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.Sipdroid;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.HeartBeatGrpState;
import com.zed3.utils.HeartBeatPacket;
import com.zed3.utils.HeartBeatParser;
import com.zed3.utils.IHeartBeatListener;
import com.zed3.utils.LogUtil;
import com.zed3.utils.NetChangedReceiver;
import com.zed3.utils.Systems;
import com.zed3.utils.Tools;
import com.zed3.utils.Zed3Log;

/**
 * Simple SIP user agent (UA). It includes audio/video applications.
 * <p>
 * It can use external audio/video tools as media applications. Currently only
 * RAT (Robust Audio Tool) and VIC are supported as external applications.
 */
public class UserAgent extends CallListenerAdapter implements
		TransactionClientListener, SipProviderListener, Runnable,
		IHeartBeatListener {

	// current group status change
	private final String ACTION_GROUP_STATUS = "com.zed3.sipua.ui_groupcall.group_status";

	// All groups change
	private final String ACTION_ALL_GROUP_CHANGE = "com.zed3.sipua.ui_groupcall.all_groups_change";

	// One group to another
	private final String ACTION_GROUP_2_GROUP = "com.zed3.sipua.ui_groupcall.group_2_group";

	// One group to another
	private final String ACTION_SINGLE_2_GROUP = "com.zed3.sipua.ui_groupcall.single_2_group";

	// Receive text message
	private final String ACTION_RECEIVE_TEXT_MESSAGE = "com.zed3.sipua.ui_receive_text_message";

	// Receive text message
	private final String ACTION_SEND_TEXT_MESSAGE_FAIL = "com.zed3.sipua.ui_send_text_message_fail";
	private final String ACTION_SEND_TEXT_MESSAGE_SUCCEED = "com.zed3.sipua.ui_send_text_message_succeed";
	private final String ACTION_SEND_TEXT_MESSAGE_TIMEOUT = "com.zed3.sipua.ui_send_text_message_timeout";
	// 获取成员列表状态广播
	private final String ACTION_GETSTATUS_MESSAGE = "com.zed3.sipua.ui_groupstatelist";
	// 接受到强制关闭"摇毙"的广播
	private final String ACTION_DESTORY_MESSAGE = "com.zed3.sipua.ui_destory_message";
	// 流量预警广播
	public final String ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";

	/** Event logger. */
	Log log;

	/** UserAgentProfile */
	public UserAgentProfile user_profile;

	/** SipProvider */
	protected SipProvider sip_provider;

	/** Call */
	// Call call;
	protected ExtendedCall call = null;

	// Added by zzhan 2011-10-25
	protected Vector<ExtendedCall> calls = null;
	public static final int CALL_LINES = 4;

	/** Call transfer */
	protected ExtendedCall call_transfer;

	/** Audio application */
	// public MediaLauncher audio_app = null;
	public JAudioLauncher audio_app = null;

	/** Local sdp */
	protected String local_session = null;

	public static final int UA_STATE_IDLE = 0;
	public static final int UA_STATE_INCOMING_CALL = 1;
	public static final int UA_STATE_OUTGOING_CALL = 2;
	public static final int UA_STATE_INCALL = 3;
	public static final int UA_STATE_HOLD = 4;

	int call_state = UA_STATE_IDLE;
	String remote_media_address;
	int remote_video_port, local_video_port;
	private boolean isInitGroupData = false;

	// Add by zzhan 2011-9
	enum ProcessCmdType {
		PROCESS_TYPE_SIP_CMD, PROCESS_TYPE_PTT_KEY_CMD, PROCESS_TYPE_RTP_SENDER_EXCEPTION_CMD, PROCESS_TYPE_HEATBEAT_MESSAGE_CMD, 
		PROCESS_TYPE_CALL_CMD
	};

	/**
	 * 语音/视频单呼叫状态
	 * @author wei.deng 
	 */
	enum CallStatus {
		/**
		 * 接听
		 */
		CALL_ACCEPT,
		/**
		 * 主叫 取消单呼
		 */
		CALL_CANCELING,
		/**
		 * 接受组呼叫
		 */
		ACCEPT_GROUP_CALL,
		
		CALL_HANGUP_WITHOUT_REJOIN;
		
		private Call mCall;
		/**
		 * sip message
		 */
		private Message mMessage;
		private android.os.Message mWorkArgs = new android.os.Message();

		public CallStatus setCall(Call call) {
			this.mCall = call;
			return this;
		}

		public CallStatus setMessage(Message message) {
			this.mMessage = message;
			return this;
		}

		public CallStatus setWorkArgs(android.os.Message args) {
			this.mWorkArgs = args;
			return this;
		}
		
		public CallStatus addWorkArg(Object obj){
			mWorkArgs.obj = obj;
			return this;
		}

		public android.os.Message getWorkArgs() {
			return mWorkArgs;
		}

		public Message getMessage() {
			return this.mMessage;
		}

		public Call getCall() {
			return this.mCall;
		}

	}

	enum ExtendedSipCallbackType {
		TYPE_REQUEST_ACCEPT_PHONE, TYPE_REQUEST_ACCEPT_LINE, TYPE_REQUEST_REJECT_PHONE, TYPE_REQUEST_WAITING_PHONE, TYPE_REQUEST_WAITING_LINE, TYPE_REQUEST_CANCEL_WAITING_OK_PHONE, TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE, TYPE_SERVER_FORCECANCEL_PHONE, TYPE_REQUEST_CANCEL_OK_PHONE, TYPE_RECEIVE_TEXT_MESSAGE_PHONE, TYPE_SEND_TEXT_MESSAGE_FAIL_PHONE, TYPE_SEND_TEXT_MESSAGE_SUCCEED_PHONE, TYPE_PTT_STATUS_PHONE, TYPE_PEER_INVITE_LINE, TYPE_REQUEST_LISTEN_LINE, TYPE_REQUEST_REJECT_LINE, TYPE_LOCAL_HANGUP_LINE, TYPE_PEER_HANGUP_LINE, TYPE_TALKING, TYPE_REQUEST_GETSTATUS_PHONE, TYPE_REGISTER_SUCCESS, TYPE_REQUEST_403, TYPE_FLOWVIEWSCANNER_START, TYPE_UNIONPASSWORDLOGIN_STATE
	};

	public enum GrpCallSetupType {
		GRPCALLSETUPTYPE_TIP, GRPCALLSETUPTYPE_ACCEPT, GRPCALLSETUPTYPE_REJECT
	};

	public static String Camera_URL = "";
	public static String Camera_AudioPort = "";
	public static String Camera_VideoPort = "";
	public static String camera_PayLoadType = "";// 主叫和被叫协商后的值

	public static boolean ua_ptt_mode = true; // Describe current mode, Ptt mode

	public static int ServerCallType = -1;
	public static int VideoEncodeType = -1;// 编码类型：H264 H264S
											// or single call mode
	public static boolean isCamerPttDialog = false;// ptt对讲组切换提示

	private boolean ptt_key_down = false; // Record ptt key status, down or up

	private int ua_ptt_state = PTT_UNREG; // Describe current ptt state

	public static final int PTT_IDLE = 5;
	public static final int PTT_TALKING = 6; // modify by hu 2014/2/24
	private static final int PTT_LISTENING = 7;
	private static final int PTT_QUEUE = 8;
	private static final int PTT_UNREG = 9;

	private String speaker = "";
	private String speakerN = "";
	private String preGrpBeforeEmergencyCall = "";
	private String preGroup = "";
	private static PttGrps pttGrps = new PttGrps();
	private Handler cmdHandler = null;
	//Modify by zzhan 2014-9-11
	//Can't create handler inside thread that has not called Looper.prepare()
	private Handler beatHandler = null;//new Handler();

	private GrpCallSetupType grpCallSetupHigh = GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
	private GrpCallSetupType grpCallSetupSame = GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
	private GrpCallSetupType grpCallSetupLow = GrpCallSetupType.GRPCALLSETUPTYPE_TIP;

	private Thread cmdProcThread = null;
	private String lastIMContent = "";
	private String lastIMSeq = "";
	private boolean isStartedGPS = false;
	private GPSPacket gpsPacket = null;
	private TotalFlowThread flowThread = null;
	private IntentFilter mGrpFilter = null;

	private String tag = "UserAgent";

	private boolean needLog;

	public class ExtendedSipCallbackPara {
		ExtendedSipCallbackType type;
		String para1;
		Object para2;
	}

	public class TextMessage {
		String from;
		String to;
		String seq;
		String content;
		String sipName;
	}

	// *************************** Basic methods ***************************

	/** Changes the call state */
	protected synchronized void changeStatus(int state, String caller) {
		call_state = state;
		Receiver.onState(state, caller);
	}

	protected void changeStatus(int state) {
		changeStatus(state, null);
	}

	/** Checks the call state */
	protected boolean statusIs(int state) {
		return (call_state == state);
	}

	/**
	 * Sets the automatic answer time (default is -1 that means no auto accept
	 * mode)
	 */
	public void setAcceptTime(int accept_time) {
		user_profile.accept_time = accept_time;
	}

	/**
	 * Sets the automatic hangup time (default is 0, that corresponds to manual
	 * hangup mode)
	 */
	public void setHangupTime(int time) {
		user_profile.hangup_time = time;
	}

	/** Sets the redirection url (default is null, that is no redircetion) */
	public void setRedirection(String url) {
		user_profile.redirect_to = url;
	}

	/** Sets the no offer mode for the invite (default is false) */
	public void setNoOfferMode(boolean nooffer) {
		user_profile.no_offer = nooffer;
	}

	/** Enables audio */
	public void setAudio(boolean enable) {
		user_profile.audio = enable;
	}

	/** Sets the receive only mode */
	public void setReceiveOnlyMode(boolean r_only) {
		user_profile.recv_only = r_only;
	}

	/** Sets the send only mode */
	public void setSendOnlyMode(boolean s_only) {
		user_profile.send_only = s_only;
	}

	/** Sets the send tone mode */
	public void setSendToneMode(boolean s_tone) {
		user_profile.send_tone = s_tone;
	}

	/** Sets the send file */

	public void setSendFile(String file_name) {
		user_profile.send_file = file_name;
	}

	/** Sets the recv file */

	public void setRecvFile(String file_name) {
		user_profile.recv_file = file_name;
	}

	/** Gets the local SDP */
	public String getSessionDescriptor() {
		return local_session;
	}

	// change start (multi codecs)
	/** Inits the local SDP (no media spec) */
	public void initSessionDescriptor(Codecs.Map c) {
		SessionDescriptor sdp = new SessionDescriptor(user_profile.from_url,
				sip_provider.getViaAddress());

		local_session = sdp.toString();

		// We will have at least one media line, and it will be
		// audio
		if (user_profile.audio) {// || !user_profile.video
			// addMediaDescriptor("audio", user_profile.audio_port, c,
			// user_profile.audio_sample_rate);
			addMediaDescriptor("audio", user_profile.audio_port, c);
		}

		if (user_profile.video) {
			String videocode = PreferenceManager.getDefaultSharedPreferences(
					Receiver.mContext).getString("videocode", "0");
			if (videocode.equals("1"))
				// add by hdf
				addMediaDescriptor("video", user_profile.video_port,
						user_profile.video_avps, "H264S", 90000);
			else
				// add by hdf
				addMediaDescriptor("video", user_profile.video_port,
						user_profile.video_avp, "H264", 90000);

			MyLog.e("cccc", "initsession videocode:" + videocode);
		}
		// 恢复视频默认初始值
		user_profile.video = true;
		MyLog.e("xxx", "user_profile.video recover true");
	}

	// change end

	// Add by zzhan 2013-5-5
	/** Inits the local SDP (no media spec) */
	public void initSessionDescriptorForGroupCall(Codecs.Map c) {
		SessionDescriptor sdp = new SessionDescriptor(user_profile.from_url,
				sip_provider.getViaAddress());

		local_session = sdp.toString();

		// We will have at least one media line, and it will be
		// audio
		if (user_profile.audio) {// || !user_profile.video
			// addMediaDescriptor("audio", user_profile.audio_port, c,
			// user_profile.audio_sample_rate);
			addMediaDescriptor("audio", user_profile.audio_port, c);
		}
		/*
		 * //不为组呼 if (!GetPttMode()) { if (user_profile.video) { // add by hdf
		 * addMediaDescriptor("video", user_profile.video_port,
		 * user_profile.video_avp, "H264", 90000); MyLog.e("cccc",
		 * "initsession"); } // 恢复视频默认初始值 user_profile.video = true;
		 * MyLog.e("xxx", "user_profile.video recover true"); }
		 */
	}

	/** Adds a single media to the SDP */
	private void addMediaDescriptor(String media, int port, int avp,
			String codec, int rate) {
		SessionDescriptor sdp = new SessionDescriptor(local_session);

		String attr_param = String.valueOf(avp);

		if (codec != null) {
			attr_param += " " + codec + "/" + rate;
		}
		// add by hdf
		Vector<AttributeField> afvec = new Vector<AttributeField>();
		afvec.add(new AttributeField("rtpmap", attr_param));

		// if (codec.equals("H264")) {
		if (MemoryMg.getInstance().GvsTransSize.equals("3"))// qcif
			afvec.add(new AttributeField("fmtp", String.valueOf(avp) + " "
					+ "profile-level-id=42e00a;qcif=1;fps=10"));

		else if (MemoryMg.getInstance().GvsTransSize.equals("4"))// 4cif
			afvec.add(new AttributeField("fmtp", String.valueOf(avp) + " "
					+ "profile-level-id=42e016;4cif=1;fps=10"));// 10

		else if (MemoryMg.getInstance().GvsTransSize.equals("5"))// cif
			afvec.add(new AttributeField("fmtp", String.valueOf(avp) + " "
					+ "profile-level-id=42e00b;cif=1;fps=10"));
		else
			// 720p
			afvec.add(new AttributeField("fmtp", String.valueOf(avp) + " "
					+ "profile-level-id=42e01f;720p=1;fps=6"));// 6
		// }

		sdp.addMedia(
				new MediaField(media, port, 0, "RTP/AVP", String.valueOf(avp)),
				afvec);

		local_session = sdp.toString();
	}

	/** Adds a set of media to the SDP */
	// private void addMediaDescriptor(String media, int port, Codecs.Map c,int
	// rate) {
	private void addMediaDescriptor(String media, int port, Codecs.Map c) {
		SessionDescriptor sdp = new SessionDescriptor(local_session);

		Vector<String> avpvec = new Vector<String>();
		Vector<AttributeField> afvec = new Vector<AttributeField>();
		if (c == null) {
			// offer all known codecs
			for (int i : Codecs.getCodecs()) {
				Codec codec = Codecs.get(i);
				if (i == 0)
					codec.init();
				avpvec.add(String.valueOf(i));
				if (codec.number() == 9)
					afvec.add(new AttributeField("rtpmap", String.format(
							"%d %s/%d", i, codec.userName(), 8000))); // kludge
																		// for
																		// G722.
																		// See
																		// RFC3551.
				else {
					afvec.add(new AttributeField("rtpmap", String.format(
							"%d %s/%d", i, codec.userName(), codec.samp_rate())));
					// amr add mode add by oumogang 2012-12-27
					// a=fmtp:114 mode-set=0
				}
			}
		} else {
			c.codec.init();
			avpvec.add(String.valueOf(c.number));
			if (c.codec.number() == 9) {
				afvec.add(new AttributeField("rtpmap", String.format(
						"%d %s/%d", c.number, c.codec.userName(), 8000))); // kludge
																			// for
																			// G722.
																			// See
																			// RFC3551.

			} else {
				afvec.add(new AttributeField("rtpmap", String.format(
						"%d %s/%d", c.number, c.codec.userName(),
						c.codec.samp_rate())));
				// amr add mode add by oumogang 2012-12-27
				// a=fmtp:114 mode-set=0
			}
		}

		if (user_profile.dtmf_avp != 0) {
			avpvec.add(String.valueOf(user_profile.dtmf_avp));
			afvec.add(new AttributeField("rtpmap", String.format(
					"%d telephone-event/%d", user_profile.dtmf_avp,
					user_profile.audio_sample_rate)));
			afvec.add(new AttributeField("fmtp", String.format("%d 0-15",
					user_profile.dtmf_avp)));
		}

		// String attr_param = String.valueOf(avp);

		sdp.addMedia(new MediaField(media, port, 0, "RTP/AVP", avpvec), afvec);

		// 仅组呼 才加入（加到3G语音版本里）
		// Modify by zzhan 2012-12-24
		// if(GetPttMode()){

		// 终端主叫 invite 加ptime
		MediaDescriptor audio = sdp.getMediaDescriptor("audio");
		if (audio != null) {
			audio.addAttribute(new AttributeField("ptime", MemoryMg.PTIME + ""));
			needLog = false;
			if (needLog) {
				MyLog.e("useragent", "addMediaDescriptor ptime:"
						+ MemoryMg.PTIME);
				MyLog.e("useragent", "addMediaDescriptor ptime:" + sdp);
			}
		}

		// }

		local_session = sdp.toString();
	}

	// *************************** Public Methods **************************

	/** Costructs a UA with a default media port */
	public UserAgent(SipProvider sip_provider, UserAgentProfile user_profile) {
		this.sip_provider = sip_provider;
		log = sip_provider.getLog();
		this.user_profile = user_profile;
		realm = user_profile.realm;

		// Add by zzhan 2011-9-6
		// pttGrps = new PttGrps(); //delete by hu 2014/4/9

		// if no contact_url and/or from_url has been set, create it now
		user_profile.initContactAddress(sip_provider);
		LogUtil.makeLog(tag, "new UserAgent()");
	}

	String realm;

	// Modify by zzhan 2011-9-7
	/** Makes a new single call (acting as UAC). */
	public boolean call(String target_url, boolean send_anonymous) {
		if (GetPttStatus() == PTT_UNREG) {
			DeviceInfo.isEmergency = false;
			return false;
		}

		// guojunfeng 暂时注释
		// Receiver.savePrtCallOut(target_url);

		// Receiver.savePrtCallIn(target_url);

		if (!isIdleOfPttLines()) {
			PttGrp curGrp = GetCurGrp();
			if (curGrp != null) {
				grouphangup(curGrp);
				// Add by zzhan 2013-5-16
				MyLog.e(tag,
						"SetPttStatus  PTT_IDLE call(String target_url, boolean send_anonymous).");
				SetPttStatus(PTT_IDLE);

				/*
				 * ExtendedCall ec = (ExtendedCall)curGrp.oVoid; if (ec != null)
				 * grouphangup(ec);
				 */
				// Add by zzhan 2013-5-9
				// Notify UI of group state
				// Add UI notify of Group shutdown(because server will not send
				// origianl group status to me)
				curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
				Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
				broadcastIntent.putExtra("0", curGrp.grpID);
				broadcastIntent.putExtra("1", "");
				speaker = "";
				curGrp.speaker = speaker;
				Receiver.mContext.sendBroadcast(broadcastIntent);

				// add by oumogang 2013-10-28
				MediaButtonReceiver.releasePTT();
			}
		}

		return call(target_url, send_anonymous, false, null, false);
	}

	// add by oumogang 2013-04-25 2
	/** Makes a new single call (acting as UAC). */
	public boolean call(String target_url, boolean send_anonymous,
			boolean isGroupBroadcast, String numbersStr) {
		if (GetPttStatus() == PTT_UNREG)
			return false;
		// modify by oumogang 2013-05-21
		// 存储呼出记录到SharedPreferences
		// guojunfeng 暂时注释
		// Receiver.savePrtCallOut(target_url);

		// Receiver.savePrtCallIn(target_url);

		if (!isIdleOfPttLines()) {
			PttGrp curGrp = GetCurGrp();
			if (curGrp != null) {
				grouphangup(curGrp);
				/*
				 * ExtendedCall ec = (ExtendedCall)curGrp.oVoid; if (ec != null)
				 * grouphangup(ec);
				 */
			}
		}

		// return call(target_url, send_anonymous, false, null, false);
		return call(target_url, send_anonymous, null, isGroupBroadcast,
				numbersStr);
	}

	// Modify by zzhan 2011-9-7
	/**
	 * Makes a new single call (acting as UAC).
	 * 
	 * @param isGroupBroadcast
	 * @param numbers
	 */
	public boolean antaCall2(String target_url, String numbers,
			boolean send_anonymous, boolean isGroupBroadcast) {
		if (GetPttStatus() == PTT_UNREG)
			return false;
		// 恢复初始默认值
		ServerCallType = -1;
		MemoryMg.getInstance().isSendOnly = false;

		// 存储呼出记录
		// Receiver.savePrtCallOut(target_url);
		// Receiver.savePrtCallIn(target_url);

		if (!isIdleOfPttLines()) {
			PttGrp curGrp = GetCurGrp();
			if (curGrp != null) {
				grouphangup(curGrp);
				/*
				 * ExtendedCall ec = (ExtendedCall)curGrp.oVoid; if (ec != null)
				 * grouphangup(ec);
				 */
			}
		}

		// return call(target_url, send_anonymous, false, null, false);
		return antaCall3(target_url, numbers, send_anonymous, isGroupBroadcast,
				null);
	}

	private boolean isIdleOfPttLines() {
		for (int i = 0; i < calls.size(); i++) {
			ExtendedCall c = (ExtendedCall) calls.get(i);
			if (c.isOnCall())
				return false;
		}

		return true;
	}

	// Add by zzhan 2011-9-7
	/** Makes a new group call (acting as UAC). */
	private boolean groupcall(String target_url, boolean send_anonymous,
			ExtendedCall ec, boolean join_call) {
		if (GetPttStatus() == PTT_UNREG)
			return false;

		return call(target_url, send_anonymous, true, ec, join_call);
	}

	// Add by oumogang 2013-04-25
	/** Makes a new group call (acting as UAC). */
	private boolean antaCall(String target_url, boolean send_anonymous,
			ExtendedCall ec, boolean isGroupBroadcast, String numbersStr) {
		if (GetPttStatus() == PTT_UNREG)
			return false;

		// return call(target_url, send_anonymous, true, ec, join_call);
		return call(target_url, send_anonymous, ec, isGroupBroadcast,
				numbersStr);
	}

	// Modify by zzhan 2011-9-7
	/** Makes a new call (acting as UAC). */
	// public boolean call(String target_url, boolean send_anonymous)
	public boolean call(String target_url, boolean send_anonymous,
			boolean is_group_call, ExtendedCall ec, boolean join_call) {
		if (Receiver.call_state != UA_STATE_IDLE) {
			// We can initiate or terminate a call only when
			// we are in an idle state
			printLog("Call attempted in state" + this.getSessionDescriptor()
					+ " : Failing Request", LogLevel.HIGH);
			DeviceInfo.isEmergency = false;
			return false;

		}

		MemoryMg.getInstance().isSendOnly = false;
		MemoryMg.getInstance().isSdpH264s = false;
		MemoryMg.SdpPtime = 0;

		// hangup(); // modified
		String from_url;

		if (!send_anonymous) {
			from_url = user_profile.from_url;
		} else {
			from_url = "sip:anonymous@anonymous.com";
		}

		SetPttMode(is_group_call);

		// change start multi codecs
		if (is_group_call)
			createOfferForGroupCall();
		else
			createOffer();

		if (!is_group_call) {
			if (call == null)
				call = getIdlePttLine();

			if (call != null) {
				call.hangup();
				removeCall(call);
			}

			call = new ExtendedCall(sip_provider, from_url,
					user_profile.contact_url, user_profile.username,
					user_profile.realm, user_profile.passwd, this);

			((ExtendedCall) call).isGroupCall = is_group_call;

			addCall(call);

			// this.call = call;

			changeStatus(UA_STATE_OUTGOING_CALL, target_url);
		} else {
			ec.hangup();
			removeCall(ec);

			ec = new ExtendedCall(sip_provider, from_url,
					user_profile.contact_url, user_profile.username,
					user_profile.realm, user_profile.passwd, this);

			((ExtendedCall) ec).isGroupCall = is_group_call;
			addCall(ec);
			GetCurGrp().oVoid = ec;
		}

		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (target_url.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				target_url = "&" + target_url;
			target_url = target_url + "@" + realm; // modified
		}

		// MMTel addition to define MMTel ICSI to be included in INVITE (added
		// by mandrajg)
		String icsi = null;
		if (user_profile.mmtel == true) {
			icsi = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
		}

		target_url = sip_provider.completeNameAddress(target_url).toString();

		if (user_profile.no_offer) {
			if (!is_group_call)
				// call.call(target_url);
				call.call(target_url, null, null, null, null); // modified by
																// zzhan
			else
				ec.groupcall(target_url, null, null, null, null, join_call); // modified
																				// by
																				// zzhan
		} else {
			if (!is_group_call)
				// call.call(target_url, local_session, icsi); // modified by
				// mandrajg
				call.call(target_url, null, null, local_session, icsi); // modified
																		// by
																		// zzhan
			else
				ec.groupcall(target_url, null, null, local_session, icsi,
						join_call); // modified by zzhan
		}

		return true;
	}

	// Modify by zzhan 2011-9-7
	/** Makes a new call (acting as UAC). */
	// public boolean call(String target_url, boolean send_anonymous)
	public boolean antaCall3(String target_url, String numbers,
			boolean send_anonymous, boolean isGroupBroadcast, ExtendedCall ec) {
		if (Receiver.call_state != UA_STATE_IDLE) {
			// We can initiate or terminate a call only when
			// we are in an idle state
			printLog("Call attempted in state" + this.getSessionDescriptor()
					+ " : Failing Request", LogLevel.HIGH);
			return false;
		}

		// hangup(); // modified
		String from_url;

		if (!send_anonymous) {
			from_url = user_profile.from_url;
		} else {
			from_url = "sip:anonymous@anonymous.com";
		}

		SetPttMode(false);

		// change start multi codecs
		createOffer();

		// if (!is_group_call) {
		// if (call == null)
		// call = getIdlePttLine();
		//
		// if (call != null) {
		// call.hangup();
		// removeCall(call);
		// }
		//
		// call = new ExtendedCall(sip_provider, from_url,
		// user_profile.contact_url, user_profile.username,
		// user_profile.realm, user_profile.passwd, this);
		//
		// ((ExtendedCall) call).isGroupCall = is_group_call;
		//
		// addCall(call);
		//
		// // this.call = call;
		//
		// changeStatus(UA_STATE_OUTGOING_CALL, target_url);
		// } else {
		// ec.hangup();
		// removeCall(ec);
		//
		// ec = new ExtendedCall(sip_provider, from_url,
		// user_profile.contact_url, user_profile.username,
		// user_profile.realm, user_profile.passwd, this);
		//
		// ((ExtendedCall) ec).isGroupCall = is_group_call;
		// addCall(ec);
		// GetCurGrp().oVoid = ec;
		// }

		if (call == null) {
			call = getIdlePttLine();
		}
		if (call != null) {
			call.hangup();
			removeCall(call);
		}

		call = new ExtendedCall(sip_provider, from_url,
				user_profile.contact_url, user_profile.username,
				user_profile.realm, user_profile.passwd, this);
		addCall(call);
		changeStatus(UA_STATE_OUTGOING_CALL, target_url);

		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (target_url.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				target_url = "&" + target_url;
			target_url = target_url + "@" + realm; // modified
		}

		// MMTel addition to define MMTel ICSI to be included in INVITE (added
		// by mandrajg)
		String icsi = null;
		if (user_profile.mmtel == true) {
			icsi = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
		}

		target_url = sip_provider.completeNameAddress(target_url).toString();

		if (user_profile.no_offer) {
			// if (!is_group_call)
			// // call.call(target_url);
			// call.call(target_url, null, null, null, null); // modified by
			// // zzhan
			// else
			// ec.groupcall(target_url, null, null, null, null, join_call); //
			// modified
			call.antaCall4(target_url, numbers, null, null, null, null,
					isGroupBroadcast);
			// by
			// zzhan
		} else {
			// if (!is_group_call)
			// // call.call(target_url, local_session, icsi); // modified by
			// // mandrajg
			// call.call(target_url, null, null, local_session, icsi); //
			// modified
			// // by
			// // zzhan
			// else
			// ec.groupcall(target_url, null, null, local_session, icsi,
			// join_call); // modified by zzhan

			call.antaCall4(target_url, numbers, null, null, local_session,
					icsi, isGroupBroadcast);
		}

		return true;
	}

	// Add by oumogang 2013-04-25 3
	/** Makes a new call (acting as UAC). */
	// public boolean call(String target_url, boolean send_anonymous)
	public boolean call(String target_url, boolean send_anonymous,
			ExtendedCall ec, boolean is_groupbroadcast, String numbersStr) {
		if (Receiver.call_state != UA_STATE_IDLE) {
			// We can initiate or terminate a call only when
			// we are in an idle state
			printLog("Call attempted in state" + this.getSessionDescriptor()
					+ " : Failing Request", LogLevel.HIGH);
			return false;
		}

		// hangup(); // modified
		String from_url;

		if (!send_anonymous) {
			from_url = user_profile.from_url;
		} else {
			from_url = "sip:anonymous@anonymous.com";
		}

		// change start multi codecs
		createOffer();

		if (call == null)
			call = getIdlePttLine();

		if (call != null) {
			call.hangup();
			removeCall(call);
		}

		call = new ExtendedCall(sip_provider, from_url,
				user_profile.contact_url, user_profile.username,
				user_profile.realm, user_profile.passwd, this);

		addCall(call);

		// this.call = call;

		changeStatus(UA_STATE_OUTGOING_CALL, target_url);
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (target_url.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				target_url = "&" + target_url;
			target_url = target_url + "@" + realm; // modified
		}

		// MMTel addition to define MMTel ICSI to be included in INVITE (added
		// by mandrajg)
		String icsi = null;
		if (user_profile.mmtel == true) {
			icsi = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
		}

		target_url = sip_provider.completeNameAddress(target_url).toString();

		if (user_profile.no_offer) {
			// call.call(target_url);
			call.call(target_url, null, null, null, null); // modified by

			// by
			// zzhan
		} else {
			// call.call(target_url, local_session, icsi); // modified by
			// mandrajg
			call.call(target_url, null, null, local_session, icsi); // modified
		}

		return true;
	}

	public void info(char c, int duration) {
		boolean use2833 = audio_app != null && audio_app.sendDTMF(c); // send
																		// out-band
																		// DTMF
																		// (rfc2833)
																		// if
																		// supported

		if (!use2833 && call != null)
			call.info(c, duration);
	}

	/** Halt listen */
	public void haltListen() {
		innerHaltListen(true);
	}
	
	public void haltListenNotCloseGps() {
		innerHaltListen(false);
	}
	
	private void innerHaltListen(boolean isCloseGps){
		// Delete by zzhan 2013-5-10
		// closeMediaApplication();
		if (cmdProcThread != null) {
			cmdHandler.getLooper().quit();
			// cmdHandler.removeCallbacks(cmdProcThread);
			// cmdProcThread.stop();

			if (beatHandler.hasMessages(1))
				beatHandler.removeMessages(1);
			cmdHandler = null;
			cmdProcThread = null;
		}
		if (calls != null)
			calls.clear();

		// Close gps
		if (user_profile.gps) {
			if (isStartedGPS) {
				// Close gps
				// if (gpsPacket != null)
				// gpsPacket.ExitGPS();
				GPSCloseLock(isCloseGps);
				GPSPacket.loginFlag = false;
				isStartedGPS = false;
				if(isCloseGps){
					MyHandlerThread.getMHThreadInstance().stopSelf();
				}
			}
		}
		isInitGroupData = false;

		// 总流量统计
		if (flowThread != null) {
			MyLog.e(tag, "haltListen StopFlow");
			flowThread.StopFlow();
			flowThread = null;
		}
		// GroupListUtil.removeDataOfGroupList();
		try {
			// 切换组广播注销
			if (SetGrpRecv != null && mGrpFilter != null)
				SipUAApp.mContext.unregisterReceiver(SetGrpRecv);
		} catch (Exception e) {
			MyLog.e("UserAgent batterylow", e.toString());
		}
	}

	/** Waits for an incoming call (acting as UAS). */
	public boolean listen() {

		if (Receiver.call_state != UA_STATE_IDLE) {
			// We can listen for a call only when
			// we are in an idle state
			printLog(
					"Call listening mode initiated in "
							+ this.getSessionDescriptor()
							+ " : Failing Request", LogLevel.HIGH);
			return false;
		}

		// hangup();

		// Modified by zzhan 2011-10-25
		if (calls != null)
			return true;

		calls = new Vector<ExtendedCall>();
		for (int i = 0; i < CALL_LINES; i++) {
			ExtendedCall subCall = new ExtendedCall(sip_provider,
					user_profile.from_url, user_profile.contact_url,
					user_profile.username, user_profile.realm,
					user_profile.passwd, this);
			subCall.listen();
			calls.add(subCall);
		}

		/*
		 * call = new ExtendedCall(sip_provider, user_profile.from_url,
		 * user_profile.contact_url, user_profile.username, user_profile.realm,
		 * user_profile.passwd, this); call.listen();
		 */

		// Add by zzhan 2011-9-8
		// Start command process thread
		if (cmdProcThread == null) {

			// Add transaction listener to process MDS MESSAGE etc. SIP request
			sip_provider.addSipProviderListener(new TransactionIdentifier(
					SipMethods.MESSAGE), this);
			sip_provider.addSipProviderListener(new TransactionIdentifier(
					SipMethods.INFO), this);
			// add by hdf 增加遥毙 销毁功能
			sip_provider.addSipProviderListener(new TransactionIdentifier(
					SipMethods.NOTIFY), this);
			sip_provider.addSipProviderListener(new TransactionIdentifier(
					SipMethods.OPTIONS), this);

			cmdProcThread = new Thread(this);
			cmdProcThread.start();
		}

		return true;
	}

	// Added by zzhan 2011-11-03
	synchronized private void addCall() {
		ExtendedCall subCall = new ExtendedCall(sip_provider,
				user_profile.from_url, user_profile.contact_url,
				user_profile.username, user_profile.realm, user_profile.passwd,
				this);
		subCall.listen();
		calls.add(subCall);
	}

	synchronized private void addCall(ExtendedCall call) {
		calls.add(call);
	}

	// Added by zzhan 2011-11-03
	synchronized private void removeCall(ExtendedCall call) {
		calls.remove(call);
	}

	// Added by zzhan 2011-11-03
	synchronized private boolean isInCalls(ExtendedCall call) {
		return calls.contains(call);
	}

	// Add by zzhan 2011-9-9
	/** Closes an ongoing, incoming, or pending call */
	public void hangup() {
		// Modify by zzhan 2013-5-27
		// hangup(false);
		hangup(false, true);
	}

	// Add by zzhan 2013-5-27
	public void hangupWithoutRejoin() {
		handleCallStatus(CallStatus.CALL_HANGUP_WITHOUT_REJOIN);
	}
	
	private void hangupWithoutRejoinInner() {
		hangup(false, false);
	}
	
	/** Closes an ongoing, incoming, or pending group call */
	public void grouphangup(ExtendedCall ec) {
		hangup(true, ec);
	}

	public void grouphangup(PttGrp grp) {
		if (grp.oVoid != null) {
			ExtendedCall call = (ExtendedCall) grp.oVoid;
			grouphangup(call);

			grp.isCreateSession = false;
			grp.oVoid = null;
		}
	}

	/** Closes an ongoing, incoming, or pending call */
	// Modify by zzhan 2013-5-27
	// private void hangup(boolean is_group_call) {
	private void hangup(boolean is_group_call, boolean rejoin) {
		printLog("HANGUP");
		// Modify by zzhan 2013-5-10
		// Do not close media when incoming single call(directly hangup) in
		// group call
		if (!IsPttMode() && !is_group_call) {
			MyLog.e(tag, "closeMediaApplication hangup "
					+ (is_group_call ? "goupcall" : "single call"));
			closeMediaApplication();
		}

		if (call != null) {
			removeCall(call);

			// Modified by zzhan 2011-9-9
			if (!is_group_call)
				call.hangup();
			else
				call.grouphangup();

			// listen
			addCall();
		}

		// Modified by zzhan 2011-9-20
		if (!is_group_call) {
			changeStatus(UA_STATE_IDLE);

			// Add by zzhan 2013-5-10
			if (!IsPttMode()) {
				SetPttMode(true);
				// Modify by zhan 2013-5-27
				if (rejoin)
					pttGroupJoin();
			}
		}
	}

	/** Closes an ongoing, incoming, or pending call */
	private void hangup(boolean is_group_call, ExtendedCall ec) {
		printLog("HANGUP");
		if (is_group_call == IsPttMode()) { // add by zzhan 20121112
			MyLog.e(tag, "closeMediaApplication hangup "
					+ (is_group_call ? "goupcall" : "single call"));
			// closeMediaApplication();
			PttGrp grp = GetCurGrp();
			if (grp != null) {
				if (ec == grp.oVoid)
					closeMediaApplication();
			}
		}

		if (ec != null) {
			removeCall(ec);

			// Modified by zzhan 2011-9-9
			if (!is_group_call)
				ec.hangup();
			else
				ec.grouphangup();

			addCall();
		}

		// Modified by zzhan 2011-9-20
		if (!IsPttMode() && !is_group_call) {
			changeStatus(UA_STATE_IDLE);
			SetPttMode(true);
		}

		ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
		escp.type = ExtendedSipCallbackType.TYPE_LOCAL_HANGUP_LINE;
		escp.para1 = GetCurGrp().grpID;
		escp.para2 = ec;

		android.os.Message msg = cmdHandler.obtainMessage();
		msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
		msg.obj = escp;
		cmdHandler.sendMessage(msg);
	}

	/** Accepts an incoming call */
	public boolean accept() {

		Zed3Log.debugE("testptt", "UserAgent#accept() enter");

		handleCallStatus(CallStatus.CALL_ACCEPT);

		return true;
	}

	/** Accepts an incoming call */
	private boolean acceptInner() {
		if (call == null) {
			return false;
		}

		// Added by zzhan 2011-11-05
		PttGrp curGrp = GetCurGrp();
		if (curGrp != null && curGrp.isCreateSession) {
			ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
			// Add by zzhan 2013-5-6
			/*
			 * if (ec != null) pttGroupRelease(false, ec);
			 */

			if (ec != null) {
				pttGroupRelease(false, ec);

				// Add UI notify of Group shutdown(because server will not send
				// origianl group status to me)
				curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
				Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
				broadcastIntent.putExtra("0", curGrp.grpID);
				broadcastIntent.putExtra("1", "");
				speaker = "";
				curGrp.speaker = speaker;
				Receiver.mContext.sendBroadcast(broadcastIntent);

				// add by oumogang 2013-10-28
				MediaButtonReceiver.releasePTT();
			}

			curGrp.isCreateSession = false;
			curGrp.oVoid = null;
		}
		Zed3Log.debug("pttTrace","UsrAgent#acceptInner() enter SetPttMode(@param false)");
		SetPttMode(false);

		printLog("ACCEPT");
		startMediaApplication(call, 0);// edit by hdf 提到前面
		changeStatus(UA_STATE_INCALL); // modified
		// startMediaApplication(call, 0);
		call.accept(local_session);

		return true;
	}

	// Added by zzhan 2011-10-25
	/** Accept group call */
	private boolean groupAccept(ExtendedCall ec) {
		if (ec == null) {
			return false;
		}

		ec.accept(local_session);

		ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
		escp.type = ExtendedSipCallbackType.TYPE_TALKING;

		android.os.Message msg = cmdHandler.obtainMessage();
		msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
		msg.obj = escp;
		cmdHandler.sendMessage(msg);

		return true;
	}

	/** Redirects an incoming call */
	public void redirect(String redirection) {
		if (call != null) {
			call.redirect(redirection);
		}
	}

	// Modified by zzhan 2011-10-26
	/** Launches the Media Application (currently, the RAT audio tool) */
	protected boolean launchMediaApplication(ExtendedCall ec, int mode) {
		// exit if the Media Application is already running
		Zed3Log.debug("pttTrace","UserAgent#launchMediaApplication() enter audio_app = "+ audio_app);
		if (audio_app != null) {
			printLog("DEBUG: media application is already running",
					LogLevel.HIGH);
			return true;
			// closeMediaApplication();
		}

		Codecs.Map c;
		// parse local sdp
		SessionDescriptor local_sdp = new SessionDescriptor(
				ec.getLocalSessionDescriptor());
		int local_audio_port = 0;
		local_video_port = 0;
		int dtmf_pt = 0;
		c = Codecs.getCodec(local_sdp);
		if (c == null) {
			Receiver.call_end_reason = R.string.card_title_ended_no_codec;
			hangup();
			return false;
		}
		MediaDescriptor m = local_sdp.getMediaDescriptor("video");
		if (m != null) {
			local_video_port = m.getMedia().getPort();
			// new add hdf
			Vector<AttributeField> arr = m.getAttributes();
			for (AttributeField field : arr) {
				String str = field.getValue();
				if (str.contains("rtpmap")) {
					this.camera_PayLoadType = str.substring(7,
							str.lastIndexOf(" "));

					MyLog.e("camera_PayLoadType", camera_PayLoadType);
				}
			}
		}
		m = local_sdp.getMediaDescriptor("audio");
		if (m != null) {
			local_audio_port = m.getMedia().getPort();
			if (m.getMedia().getFormatList()
					.contains(String.valueOf(user_profile.dtmf_avp)))
				dtmf_pt = user_profile.dtmf_avp;
		}
		// parse remote sdp
		SessionDescriptor remote_sdp = new SessionDescriptor(
				ec.getRemoteSessionDescriptor());
		remote_media_address = (new Parser(remote_sdp.getConnection()
				.toString())).skipString().skipString().getString();
		int remote_audio_port = 0;
		remote_video_port = 0;
		for (Enumeration<MediaDescriptor> e = remote_sdp.getMediaDescriptors()
				.elements(); e.hasMoreElements();) {
			MediaField media = e.nextElement().getMedia();
			if (media.getMedia().equals("audio"))
				remote_audio_port = media.getPort();
			if (media.getMedia().equals("video"))
				remote_video_port = media.getPort();
		}

		// select the media direction (send_only, recv_ony, fullduplex)
		int dir = 0;
		if (user_profile.recv_only)
			dir = -1;
		else if (user_profile.send_only)
			dir = 1;

		Zed3Log.debug("pttTrace","UserAgent#launchMediaApplication() init prepare ");
		if (user_profile.audio && local_audio_port != 0) { // &&
															// remote_audio_port
															// != 0 create an
															// audio_app and
															// start
															// it

			Zed3Log.debug("pttTrace","UserAgent#launchMediaApplication() init start ");
			if (audio_app == null) { // for testing..
				String audio_in = null;
				if (user_profile.send_tone) {
					audio_in = JAudioLauncher.TONE;
				} else if (user_profile.send_file != null) {
					audio_in = user_profile.send_file;
				}
				String audio_out = null;
				if (user_profile.recv_file != null) {
					audio_out = user_profile.recv_file;
				}
				this.Camera_URL = remote_media_address;
				this.Camera_AudioPort = remote_audio_port + "";
				this.Camera_VideoPort = remote_video_port + "";

				MyLog.e("88888888", "url:" + Camera_URL + "audio:"
						+ Camera_AudioPort + "video:" + Camera_VideoPort);
				if (mode != 0) {
					audio_app = new JAudioLauncher(local_audio_port,
							//modify by zzhan 2014-11-04
							//remote_media_address, remote_audio_port, dir,
							remote_media_address, remote_audio_port, mode,
							audio_in, audio_out, c.codec.samp_rate(),
							user_profile.audio_sample_size,
							c.codec.frame_size()
									* ((MemoryMg.SdpPtime == 0 ? MemoryMg.PTIME
											: MemoryMg.SdpPtime) / 20), log, c,
							dtmf_pt);
				} else {
					audio_app = new JAudioLauncher(local_audio_port,
							//modify by zzhan 2014-11-04
							//remote_media_address, remote_audio_port, dir,
							remote_media_address, remote_audio_port, mode,
							audio_in, audio_out, c.codec.samp_rate(),
							user_profile.audio_sample_size,
							c.codec.frame_size()
									* ((MemoryMg.SdpPtime == 0 ? MemoryMg.PTIME
											: MemoryMg.SdpPtime) / 20), log, c,
							dtmf_pt);
				}

			}
			// audio_app.startMedia();
		}
		return true;
	}

	// mode : duplex= 0, recv-only= -1, send-only= +1;
	private boolean startMediaApplication(ExtendedCall ec, int mode) {

		Zed3Log.debug("pttTrace","UserAgent#startMediaApplication() enter audio_app = "+ audio_app);

		boolean bRet = launchMediaApplication(ec, mode);
		Zed3Log.debug("pttTrace","UserAgent#startMediaApplication() enter launch media reuslt = "+ bRet);
		if (!bRet)
			return false;
		
		if (audio_app != null) {
			// Modify by zzhan 2013-5-15
			// Add by zzhan 2013-5-8
			// mode : duplex= 0, recv-only= -1, send-only= +1;
			if (mode == -1 || mode == 1) {
				pttSpeakerControl();
			} else {
				if(!Build.MODEL.toLowerCase().contains("g716-l070")){
					audio_app.speakerMedia(AudioManager.MODE_IN_CALL);
				}
			}

			audio_app.startMedia(mode);
		}
		return true;
	}

	/** Close the Media Application */
	protected synchronized void closeMediaApplication() {
		Zed3Log.debug("pttTrace","UserAgent#closeMediaApplication() enter audio_app = "+ audio_app);
		if (audio_app != null) {
			audio_app.stopMedia();
			audio_app = null;
			MyLog.e("UserAgent", "closeMediaApplication.");
		}
	}

	public boolean muteMediaApplication() {
		if (audio_app != null)
			return audio_app.muteMedia();
		return false;
	}

	public int speakerMediaApplication(int mode) {
		int old;

		if (audio_app != null)
			return audio_app.speakerMedia(mode);
		if(Receiver.GetCurUA().IsPttMode()){
			old = RtpStreamReceiver_group.speakermode;
			RtpStreamReceiver_group.speakermode = mode;
		}else{
			old = RtpStreamReceiver_signal.speakermode;
			RtpStreamReceiver_signal.speakermode = mode;
		}
		return old;
	}

	public void bluetoothMediaApplication() {
		if (audio_app != null)
			audio_app.bluetoothMedia();
	}

	private void createOffer() {
		initSessionDescriptor(null);
	}

	// Add by zzhan 2013-5-5
	private void createOfferForGroupCall() {
		initSessionDescriptorForGroupCall(null);
	}

	// Modify by zzhan 2012-12-24
	// private void createAnswer(SessionDescriptor remote_sdp) {
	private void createAnswer(SessionDescriptor remote_sdp, boolean isGroupCall) {

		Codecs.Map c = Codecs.getCodec(remote_sdp);
		if (c == null)
			throw new RuntimeException("Failed to get CODEC: AVAILABLE : "
					+ remote_sdp);
		initSessionDescriptor(c);
		// Modify by zzhan 2012-12-24
		// sessionProduct(remote_sdp);
		sessionProduct(remote_sdp, isGroupCall);
	}

	// Modify by zzhan 2012-12-24
	// private void sessionProduct(SessionDescriptor remote_sdp) {
	private void sessionProduct(SessionDescriptor remote_sdp,
			boolean isGroupCall) {
		SessionDescriptor local_sdp = new SessionDescriptor(local_session);
		SessionDescriptor new_sdp = new SessionDescriptor(
				local_sdp.getOrigin(), local_sdp.getSessionName(),
				local_sdp.getConnection(), local_sdp.getTime());
		new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());

		MyLog.e("hdf336", remote_sdp.toString());
		if (remote_sdp.toString().contains("video")) {
			ServerCallType = 1;// 视频呼叫
		} else {
			// remotesdp里不包含视频分两种情况：
			// 1.主叫发起的是语音，被叫回复的也是语音（正常）
			// 2.主叫发起的视频，被叫回复的仅语音，视频协商失败
			if (Receiver.mSipdroidEngine.isMakeVideoCall == 1) {
				ServerCallType = 1;// 还是视频呼叫,只不过。。。
				MemoryMg.getInstance().isSdpH264s = true;
			} else
				ServerCallType = 0;// 语音呼叫
		}
		new_sdp = SdpTools.sdpMediaProduct(new_sdp,
				remote_sdp.getMediaDescriptors());

		// Add by zzhan 2012-12-20
		// if (Receiver.call_state == UA_STATE_INCOMING_CALL) {
		// 仅终端为被叫，200Ok后sdp协商加入ptime
		MediaDescriptor audio = new_sdp.getMediaDescriptor("audio");
		if (audio != null) {
			audio.addAttribute(new AttributeField("ptime",
					(MemoryMg.SdpPtime == 0 ? MemoryMg.PTIME
							: MemoryMg.SdpPtime) + ""));
			MyLog.e("useragent", "audio addAttribute");

		}
		// }
		MyLog.e("useragent", "sessionProduct sdpptime:" + MemoryMg.SdpPtime
				+ " ptime:" + MemoryMg.PTIME + " last ptime:"
				+ (MemoryMg.SdpPtime == 0 ? MemoryMg.PTIME : MemoryMg.SdpPtime));
		MyLog.e("useragent", "sessionProduct ptime:" + new_sdp);

		// ////change multi codecs
		local_session = new_sdp.toString();
		// {
		// ServerCallType=1;//视频呼叫
		if (ServerCallType == 1) {
			if (!local_session.contains("video"))// 最终协商出来没有video则
			{
				MemoryMg.getInstance().isSdpH264s = true;
			}
			// 来区分h264与h264s
			if (local_session.contains("H264S")) {
				VideoEncodeType = 1;
			} else if (local_session.contains("H264")) {
				VideoEncodeType = 0;
			}
		}
		MyLog.e(tag, "VideoEncodeType is:" + VideoEncodeType);
		// }
		// else
		// {
		// ServerCallType=0;//语音呼叫
		// }
		MyLog.e("UserAgent", "sessionProduct:" + local_session + " callType:"
				+ ServerCallType);

		// Modified by zzhan 2011-11-04
		// When single call
		if (!isGroupCall) {
			if (call != null)
				call.setLocalSessionDescriptor(local_session);
		}
		// Group call
		else {
			PttGrp curGrp = GetCurGrp();
			if (curGrp != null) {
				if (curGrp.oVoid != null) {
					ExtendedCall c = (ExtendedCall) curGrp.oVoid;
					c.setLocalSessionDescriptor(local_session);
				}
			}
		}

	}

	// ********************** Call callback functions **********************

	/**
	 * Callback function called when arriving a new INVITE method (incoming
	 * call)
	 */
	public void onCallIncoming(Call call, NameAddress callee,
			NameAddress caller, String sdp, Message invite) {
		printLog("onCallIncoming()", LogLevel.LOW);
		MyLog.e("UserAgent", "onCallIncoming()");

		// Modified by zzhan 2011-11-03
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}

		printLog("INCOMING", LogLevel.HIGH);
		int i = 0;
		for (UserAgent ua : Receiver.mSipdroidEngine.uas) {
			if (ua == this)
				break;
			i++;
		}

		// 暂不支持会议和广播
		// if (invite.hasAntaExtensionHeader()) {// 会议
		// Header h = invite.getHeader(SipHeaders.Anta_Extension);
		// if (h == null)
		// return;
		// String str = h.getValue();// 获取value值
		// MyLog.e("type_Anta-Extension", str);
		// MemoryMg.SdpPtime = 20;
		//
		// }
		// Group call incoming
		if (invite.hasPttExtensionHeader()) {
			MemoryMg.SdpPtime = 0;
			((ExtendedCall) call).isGroupCall = true;
			if (sdp == null) {
				createOfferForGroupCall();
			} else {
				createOfferForGroupCall();
				SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
				try {
					createAnswer(remote_sdp, true);
				} catch (Exception e) {
					removeCall((ExtendedCall) call);
					addCall();
					return;
				}
			}

			call.ring(local_session);

			ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
			escp.type = ExtendedSipCallbackType.TYPE_PEER_INVITE_LINE;
			escp.para1 = caller.getAddress().getUserName();
			escp.para2 = call;

			android.os.Message msg = cmdHandler.obtainMessage();
			msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
			msg.obj = escp;
			cmdHandler.sendMessage(msg);
		}
		// Single call incoming
		else {
			// 正在通话中
			if (Receiver.call_state != UA_STATE_IDLE || !Receiver.isFast(i)) {
				call.busy();
				// listen();
				call = new ExtendedCall(sip_provider, user_profile.from_url,
						user_profile.contact_url, user_profile.username,
						user_profile.realm, user_profile.passwd, this);
				call.listen();
				return;
			}
			//判断是否是调度台号码 add by hu2014/10/29
//			if(invite.hasUserAgentHeader()){
//				Header h = invite.getHeader(SipHeaders.User_Agent);
//				if (h == null)
//					return;
//				String str = h.getValue();// 获取value值
//				if(str.equals("Zed-3-SVP-MDS")){
//					DeviceVideoInfo.isConsole = true;
//				}else if(str.equals("Zed-3-PDA-MDS")){
//					DeviceVideoInfo.isConsole = false;
//				}else{
//					DeviceVideoInfo.isConsole = false;
//				}
//			}
			// 会议和广播 ptime 为20ms，其他跟单呼一样。
			if (invite.hasAntaExtensionHeader()) {// 会议
				Header h = invite.getHeader(SipHeaders.Anta_Extension);
				if (h == null)
					return;
				String str = h.getValue();// 获取value值
				MyLog.e("type_Anta-Extension", str);
				MemoryMg.SdpPtime = 20;
				AntaCallUtil.isAntaCall = true;

			} else {
				MemoryMg.SdpPtime = 0;
				AntaCallUtil.isAntaCall = false;
			}

			// 仅单呼时恢复
			MemoryMg.getInstance().isSendOnly = false;
			MemoryMg.getInstance().isSdpH264s = false;

			if (Receiver.mSipdroidEngine != null)
				Receiver.mSipdroidEngine.ua = this;

			// changeStatus(UA_STATE_INCOMING_CALL, caller.toString());

			if (sdp == null) {
				createOffer();
			} else {
				createOffer();
				SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
				try {
					createAnswer(remote_sdp, false);
				} catch (Exception e) {
					e.printStackTrace();
					MyLog.e("UserAgent",
							"oncallincoming exception:" + e.toString());
					// only known exception is no codec
					Receiver.call_end_reason = R.string.card_title_ended_no_codec;
					changeStatus(UA_STATE_IDLE);
					removeCall((ExtendedCall) call);
					addCall();
					return;
				}
			}
//			// 协商后在弹出来电界面
//			changeStatus(UA_STATE_INCOMING_CALL, caller.toString());

			call.ring(local_session);

			// record single call
			this.call = (ExtendedCall) call;
			
			// changeStatus after reinit the call instance； modify by mou 2014-10-10 
			// 协商后在弹出来电界面
			changeStatus(UA_STATE_INCOMING_CALL, caller.toString());
		}
	}

	/**
	 * Callback function called when arriving a new Re-INVITE method
	 * (re-inviting/call modify)
	 */
	public void onCallModifying(Call call, String sdp, Message invite) {
		printLog("onCallModifying()", LogLevel.LOW);
		// Modified by zzhan 2011-11-04
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("RE-INVITE/MODIFY", LogLevel.HIGH);

		// to be implemented.
		// currently it simply accepts the session changes (see method
		// onCallModifying() in CallListenerAdapter)
		super.onCallModifying(call, sdp, invite);
	}

	/**
	 * Callback function that may be overloaded (extended). Called when arriving
	 * a 180 Ringing or a 183 Session progress with SDP
	 */
	public void onCallRinging(Call call, Message resp) {
		printLog("onCallRinging()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call) && call != call_transfer) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		// Modified by zzhan 2011-9-14
		if (IsPttMode()) {
			// Do nothing
		} else {
			String remote_sdp = call.getRemoteSessionDescriptor();
			if (remote_sdp == null || remote_sdp.length() == 0) {
				printLog("RINGING", LogLevel.HIGH);
				if(Receiver.GetCurUA().IsPttMode()){
					RtpStreamReceiver_group.ringback(true);
				}else{
					RtpStreamReceiver_signal.ringback(true);
				}
			} else {
				printLog("RINGING(with SDP)", LogLevel.HIGH);
				if (!user_profile.no_offer) {
					if(Receiver.GetCurUA().IsPttMode()){
						RtpStreamReceiver_group.ringback(false);
					}else{
						RtpStreamReceiver_signal.ringback(false);
					}
					// Update the local SDP along with offer/answer
					// Modify by zzhan 2012-12-24
					// sessionProduct(new SessionDescriptor(remote_sdp));
					sessionProduct(new SessionDescriptor(remote_sdp), false);

					// Modified by zzhan 2011-9-13
					if (resp.getStatusLine().getCode() == 183) {
						startMediaApplication(this.call, 0);
						// disable by oumogang 2013-07-23
						// // Modified by zzhan 2013-7-17
						// changeStatus(UA_STATE_INCALL);
					}
				}
			}
		}
	}

	/** Callback function called when arriving a 2xx (call accepted) */
	public void onCallAccepted(Call call, String sdp, Message resp) {
		printLog("onCallAccepted()", LogLevel.LOW);

		if (!isInCalls((ExtendedCall) call) && call != call_transfer) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}

		printLog("ACCEPTED/CALL", LogLevel.HIGH);

		// When single call
		if (!IsPttMode()) {
			if (!statusIs(UA_STATE_OUTGOING_CALL)) { // modified
				hangup();
				return;
			}
			// changeStatus(UA_STATE_INCALL);//edit by hdf
			// 放到startMediaApplication的后面
		}

		SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
		/*
		 * if (user_profile.no_offer) { // answer with the local sdp //Modify by
		 * zzhan 2012-12-24 //createAnswer(remote_sdp); createAnswer(remote_sdp,
		 * false); call.ackWithAnswer(local_session); } else { // Update the
		 * local SDP along with offer/answer //createAnswer(remote_sdp);
		 * //sessionProduct(remote_sdp); sessionProduct(remote_sdp, false); }
		 */
		// Modify by zzhan 2011-9-13
		// MDS accept invite request with header "Ptt-Extension: XXXX"
		if (resp.hasPttExtensionHeader()) {
			sessionProduct(remote_sdp, true);
			Header h = resp.getHeader(SipHeaders.Ptt_Extension);
			if (h == null)
				return;

			// Send message to extended sip thread process
			ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();

			String pttExtensionValue = h.getValue();
			if (pttExtensionValue.equalsIgnoreCase("3ghandset listen"))
				escp.type = ExtendedSipCallbackType.TYPE_REQUEST_LISTEN_LINE;
			else
				escp.type = ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_LINE;

			escp.para2 = call;

			android.os.Message msg = cmdHandler.obtainMessage();
			msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
			msg.obj = escp;
			cmdHandler.sendMessage(msg);
		} else {
			//判断是否是调度台号码 add by hu2014/10/29
//			if(resp.hasUserAgentHeader()){
//				Header h = resp.getHeader(SipHeaders.User_Agent);
//				if (h == null)
//					return;
//				String str = h.getValue();// 获取value值
//				if(str.equals("Zed-3-SVP-MDS")){
//					DeviceVideoInfo.isConsole = true;
//				}else if(str.equals("Zed-3-PDA-MDS")){
//					DeviceVideoInfo.isConsole = false;
//				}else{
//					DeviceVideoInfo.isConsole = false;
//				}
//			}
			sessionProduct(remote_sdp, false);
			boolean bRet = startMediaApplication(this.call, 0);
			//modify by zzhan 2014-10-28
			//changeStatus(UA_STATE_INCALL);
			if (bRet)
				changeStatus(UA_STATE_INCALL);
		}

		if (call == call_transfer) {
			StatusLine status_line = resp.getStatusLine();
			int code = status_line.getCode();
			String reason = status_line.getReason();
			this.call.notify(code, reason);
		}
	}

	/** Callback function called when arriving an ACK method (call confirmed) */
	public void onCallConfirmed(Call call, String sdp, Message ack) {
		printLog("onCallConfirmed()", LogLevel.LOW);

		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}

		printLog("CONFIRMED/CALL", LogLevel.HIGH);

		// changeStatus(UA_STATE_INCALL); modified

		if (!IsPttMode()) {
			if (user_profile.hangup_time > 0) {
				this.automaticHangup(user_profile.hangup_time);
			}
		}
	}

	/** Callback function called when arriving a 2xx (re-invite/modify accepted) */
	public void onCallReInviteAccepted(Call call, String sdp, Message resp) {
		printLog("onCallReInviteAccepted()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("RE-INVITE-ACCEPTED/CALL", LogLevel.HIGH);

		if (!IsPttMode()) {
			if (statusIs(UA_STATE_HOLD))
				changeStatus(UA_STATE_INCALL);
			else
				changeStatus(UA_STATE_HOLD);
		}
	}

	/** Callback function called when arriving a 4xx (re-invite/modify failure) */
	public void onCallReInviteRefused(Call call, String reason, Message resp) {
		printLog("onCallReInviteRefused()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("RE-INVITE-REFUSED (" + reason + ")/CALL", LogLevel.HIGH);
	}

	/** Callback function called when arriving a 4xx (call failure) */
	public void onCallRefused(Call call, String reason, Message resp) {
		printLog("onCallRefused()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("REFUSED (" + reason + ")", LogLevel.HIGH);
		if (reason.equalsIgnoreCase("not acceptable here")) {
			// bummer we have to string compare, this is sdp 488
			Receiver.call_end_reason = R.string.card_title_ended_no_codec;
		}
		// When single call
		if (!IsPttMode()) {
			// add by hdf
			if (resp.toString().contains("403 Forbidden")) {
				MyLog.e(tag, "No videoCall 403 Forbidden");
				beatHandler.sendEmptyMessage(2);
			}
			MyLog.e(tag, "closeMediaApplication onCallRefused."
					+ resp.getStatusLine().getCode());
			closeMediaApplication();
			changeStatus(UA_STATE_IDLE);
			if (call == call_transfer) {
				StatusLine status_line = resp.getStatusLine();
				int code = status_line.getCode();
				// String reason=status_line.getReason();
				this.call.notify(code, reason);
				call_transfer = null;
			}
		}

		// Add by zzhan 2011-9-13
		// Response 403 Forbidden(INVITE) by MDS
		if (resp.hasPttExtensionHeader()) {
			android.os.Message msg = cmdHandler.obtainMessage();
			msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();

			ExtendedSipCallbackPara para = new ExtendedSipCallbackPara();

			if (resp.getStatusLine().getCode() == 403)
				para.type = ExtendedSipCallbackType.TYPE_PEER_HANGUP_LINE;
			else if (resp.getStatusLine().getCode() == 486)
				para.type = ExtendedSipCallbackType.TYPE_REQUEST_REJECT_LINE;

			msg.obj = para;
			// Post message to command process thread
			cmdHandler.sendMessage(msg);
		}

		Zed3Log.debug("pttTrace","UsrAgent#onCallRefused() enter IsPttMode() = " + (IsPttMode()));

		if (!IsPttMode()) {
			Zed3Log.debug("pttTrace","UsrAgent#onCallRefused() enter SetPttMode(@param true) ");
			SetPttMode(true);
			// Add by zzhan 2013-5-14
			pttGroupJoin();
		}

		removeCall((ExtendedCall) call);
		addCall();
	}

	/** Callback function called when arriving a 3xx (call redirection) */
	public void onCallRedirection(Call call, String reason,
			Vector<String> contact_list, Message resp) {
		printLog("onCallRedirection()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("REDIRECTION (" + reason + ")", LogLevel.HIGH);

		// When single call
		if (!IsPttMode())
			call.call(((String) contact_list.elementAt(0)));
	}

	/**
	 * Callback function that may be overloaded (extended). Called when arriving
	 * a CANCEL request
	 */
	public void onCallCanceling(Call call, Message cancel) {
		handleCallStatus(
				CallStatus.CALL_CANCELING
						  .setCall(call)
						  .setMessage(cancel)
					);
	}

	private void onCallCancelingInner(Call call, Message cancel){
		
		printLog("onCallCanceling()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("CANCEL", LogLevel.HIGH);

		Zed3Log.debug("pttTrace", "UserAgent#onCallCanceling() enter IsPttMode() = " + (IsPttMode()));
		
		//if server cancel group call
		PttGrps grps = GetAllGrps();
		if (grps != null) {
			PttGrp grp = null;
			for (int i = 0; i < grps.GetCount(); i++){
				grp = grps.GetGrpByIndex(i);
				if (grp.oVoid != null){
					ExtendedCall ec = (ExtendedCall) grp.oVoid;
					if (cancel.getCallIdHeader().getCallId().equalsIgnoreCase(ec.getDialog().getCallID())) {
						grp.oVoid= null;
						grp.isCreateSession = false;
						removeCall((ExtendedCall) call);
						addCall();
						return;
					}
				}
			}
		}
		
		// When single call
		if (!IsPttMode() || !cancel.hasPttExtensionHeader()) {
			
			if(CallUtil.isInCallState()) {
				Zed3Log.debug("pttTrace", "UserAgent#onCallCanceling() enter IsPttMode() = " + (IsPttMode()));
				hangup();
			} else {
				changeStatus(UA_STATE_IDLE);
			}
			this.call = null;
		}
		
		Zed3Log.debug("pttTrace", "UserAgent#onCallCanceling() enter set ptt mode true ");
		// Add by zzhan 2011-9-14
		SetPttMode(true);

		removeCall((ExtendedCall) call);
		addCall();
	}

	/** Callback function called when arriving a BYE request */
	public void onCallClosing(Call call, Message bye) {
		printLog("onCallClosing()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call) && call != call_transfer) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}

		if (call != call_transfer && call_transfer != null) {
			printLog("CLOSE PREVIOUS CALL", LogLevel.HIGH);
			this.call = call_transfer;
			call_transfer = null;
			return;
		}

		// else
		printLog("CLOSE", LogLevel.HIGH);
		// closeMediaApplication();
		if (!((ExtendedCall) call).isGroupCall) {
			boolean bNeedCloseMedia = (call_state == UA_STATE_INCALL);
			changeStatus(UA_STATE_IDLE);
			if (bNeedCloseMedia) {
				MyLog.e(tag, "closeMediaApplication onCallClosing "
						+ (((ExtendedCall) call).isGroupCall ? "goupcall"
								: "single call"));
				closeMediaApplication();
			}
		}

		removeCall((ExtendedCall) call);
		addCall();

		ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
		escp.type = ExtendedSipCallbackType.TYPE_PEER_HANGUP_LINE;
		escp.para2 = call;

		android.os.Message msg = cmdHandler.obtainMessage();
		msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
		msg.obj = escp;
		cmdHandler.sendMessage(msg);
	}

	/**
	 * Callback function called when arriving a response after a BYE request
	 * (call closed)
	 */
	public void onCallClosed(Call call, Message resp) {
		printLog("onCallClosed()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("CLOSE/OK", LogLevel.HIGH);

		if (!IsPttMode())
			changeStatus(UA_STATE_IDLE);

		// Modify by zzhan 2013-5-10
		/*
		 * // Add by zzhan 2011-9-14 if (!IsPttMode()) SetPttMode(true);
		 */
		if (!IsPttMode()) {
			SetPttMode(true);
			pttGroupJoin();
		}
	}

	/** Callback function called when the invite expires */
	public void onCallTimeout(Call call) {
		printLog("onCallTimeout()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call)) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("NOT FOUND/TIMEOUT", LogLevel.HIGH);

		if (!IsPttMode())
			changeStatus(UA_STATE_IDLE);

		if (call == call_transfer) {
			int code = 408;
			String reason = "Request Timeout";
			this.call.notify(code, reason);
			call_transfer = null;
		}

		// Add by zzhan 2011-9-14
		if (!IsPttMode()) {
			SetPttMode(true);
			// Add by zzhan 2013-5-16
			pttGroupJoin();
		}

		removeCall((ExtendedCall) call);
		addCall();
	}

	// ****************** ExtendedCall callback functions ******************

	/**
	 * Callback function called when arriving a new REFER method (transfer
	 * request)
	 */
	public void onCallTransfer(ExtendedCall call, NameAddress refer_to,
			NameAddress refered_by, Message refer) {
		printLog("onCallTransfer()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call) || IsPttMode()) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer to " + refer_to.toString(), LogLevel.HIGH);
		call.acceptTransfer();
		call_transfer = new ExtendedCall(sip_provider, user_profile.from_url,
				user_profile.contact_url, this);
		call_transfer.call(refer_to.toString(), local_session, null); // modified
																		// by
																		// mandrajg
	}

	/** Callback function called when a call transfer is accepted. */
	public void onCallTransferAccepted(ExtendedCall call, Message resp) {
		printLog("onCallTransferAccepted()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call) || IsPttMode()) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer accepted", LogLevel.HIGH);
	}

	/** Callback function called when a call transfer is refused. */
	public void onCallTransferRefused(ExtendedCall call, String reason,
			Message resp) {
		printLog("onCallTransferRefused()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call) || IsPttMode()) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer refused", LogLevel.HIGH);
	}

	/** Callback function called when a call transfer is successfully completed */
	public void onCallTransferSuccess(ExtendedCall call, Message notify) {
		printLog("onCallTransferSuccess()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call) || IsPttMode()) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer successed", LogLevel.HIGH);
		call.hangup();
	}

	/**
	 * Callback function called when a call transfer is NOT sucessfully
	 * completed
	 */
	public void onCallTransferFailure(ExtendedCall call, String reason,
			Message notify) {
		printLog("onCallTransferFailure()", LogLevel.LOW);
		if (!isInCalls((ExtendedCall) call) || IsPttMode()) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer failed", LogLevel.HIGH);
	}

	// ************************* Schedule events ***********************

	/** Schedules a re-inviting event after <i>delay_time</i> secs. */
	void reInvite(final String contact_url, final int delay_time) {
		SessionDescriptor sdp = new SessionDescriptor(local_session);
		sdp.IncrementOLine();
		final SessionDescriptor new_sdp;
		if (statusIs(UserAgent.UA_STATE_INCALL)) { // modified
			new_sdp = new SessionDescriptor(sdp.getOrigin(),
					sdp.getSessionName(),
					new ConnectionField("IP4", "0.0.0.0"), new TimeField());
		} else {
			new_sdp = new SessionDescriptor(sdp.getOrigin(),
					sdp.getSessionName(), new ConnectionField("IP4",
							IpAddress.localIpAddress), new TimeField());
		}
		new_sdp.addMediaDescriptors(sdp.getMediaDescriptors());
		local_session = sdp.toString();
		(new Thread() {
			public void run() {
				runReInvite(contact_url, new_sdp.toString(), delay_time);
			}
		}).start();
	}

	/** Re-invite. */
	private void runReInvite(String contact, String body, int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		printLog("RE-INVITING/MODIFYING");
		if (call != null && call.isOnCall()) {
			printLog("REFER/TRANSFER");
			call.modify(contact, body);
		}
	}

	/** Schedules a call-transfer event after <i>delay_time</i> secs. */
	void callTransfer(final String transfer_to, final int delay_time) {
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		final String target_url;
		if (transfer_to.indexOf("@") < 0)
			target_url = transfer_to + "@" + realm; // modified
		else
			target_url = transfer_to;
		(new Thread() {
			public void run() {
				runCallTransfer(target_url, delay_time);
			}
		}).start();
	}

	/** Call-transfer. */
	private void runCallTransfer(String transfer_to, int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (call != null && call.isOnCall()) {
			printLog("REFER/TRANSFER");
			call.transfer(transfer_to);
		}
	}

	/** Schedules an automatic answer event after <i>delay_time</i> secs. */
	void automaticAccept(final int delay_time) {
		(new Thread() {
			public void run() {
				runAutomaticAccept(delay_time);
			}
		}).start();
	}

	/** Automatic answer. */
	private void runAutomaticAccept(int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (call != null) {
			printLog("AUTOMATIC-ANSWER");
			accept();
		}
	}

	/** Schedules an automatic hangup event after <i>delay_time</i> secs. */
	void automaticHangup(final int delay_time) {
		(new Thread() {
			public void run() {
				runAutomaticHangup(delay_time);
			}
		}).start();
	}

	/** Automatic hangup. */
	private void runAutomaticHangup(int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (call != null && call.isOnCall()) {
			printLog("AUTOMATIC-HANGUP");
			hangup();
		}

	}

	// ****************************** Logs *****************************

	/** Adds a new string to the default Log */
	void printLog(String str) {
		printLog(str, LogLevel.HIGH);
	}

	/** Adds a new string to the default Log */
	@SuppressWarnings("unused")
	void printLog(String str, int level) {
		if (Sipdroid.release)
			return;
		if (log != null)
			log.println("UA: " + str, level + SipStack.LOG_LEVEL_UA);
		if ((user_profile == null || !user_profile.no_prompt)
				&& level <= LogLevel.HIGH)
			System.out.println("UA: " + str);
	}

	/** Adds the Exception message to the default Log */
	@SuppressWarnings("unused")
	void printException(Exception e, int level) {
		if (Sipdroid.release)
			return;
		if (log != null)
			log.printException(e, level + SipStack.LOG_LEVEL_UA);
	}

	// Add by zzhan 2011-9-7
	// ****************************** PTT *****************************
	/* Set ptt mode. Mode: ptt mode or single call mode. */
	public void SetPttMode(boolean isPttMode) {
		// Set audio play mode
		ua_ptt_mode = isPttMode;

		// Set up Speaker
		AudioManager am = (AudioManager) Receiver.mContext
				.getSystemService(Context.AUDIO_SERVICE);
		if (isPttMode) {
			if (Integer.parseInt(Build.VERSION.SDK) >= 5) {
				// zzhan 2012-12-13
				// am.setMode(AudioManager.MODE_NORMAL);//呼叫之前强制将通话模式重置
				// Delete by zzhan 2013-5-8
				// am.setSpeakerphoneOn(true);
			}
			// guojunfeng 注释
			// am.setStreamVolume(AudioManager.STREAM_MUSIC,
			// am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 3 / 4, 0);
		} else {
			// am.setMode(AudioManager.MODE_NORMAL);
			// am.setSpeakerphoneOn(true);
			// guojunfeng 注释
			// am.setStreamVolume(AudioManager.STREAM_MUSIC,
			// am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
		}
	}

	/* Get ptt mode. Mode: ptt mode or single call mode. */
	public boolean GetPttMode() {
		return ua_ptt_mode;
	}

	/* Get current group */
	public PttGrp GetCurGrp() {
		return pttGrps.GetCurGrp();
	}

	/* Get all groups */
	public PttGrps GetAllGrps() {
		return pttGrps;
	}

	/* Set up Group call accept configuration */
	public void SetGrpCallConfig(GrpCallSetupType high, GrpCallSetupType same,
			GrpCallSetupType low) {
		grpCallSetupHigh = high;
		grpCallSetupSame = same;
		grpCallSetupLow = low;
	}

	public GrpCallSetupType GetCrpCallConfigOfHigh() {
		return grpCallSetupHigh;
	}

	public GrpCallSetupType GetCrpCallConfigOfSame() {
		return grpCallSetupSame;
	}

	public GrpCallSetupType GetCrpCallConfigOfLow() {
		return grpCallSetupLow;
	}

	/* Invoke by UI. */
	public boolean OnPttKey(boolean keyDown) {
		// Post message to command process thread
		if (!GetPttMode() || ptt_key_down == keyDown || GetCurGrp() == null)
			return false;
		boolean result = false;
		// ptt_key_down = keyDown;
		if (keyDown) {
			if (SystemClock.uptimeMillis() - intervalDown < PTT_CLICK_INTERVAL_LIMIT) {
				// Do nothing
			} else {
				if (cmdHandler != null) {
					ptt_key_down = keyDown;
					android.os.Message msg = cmdHandler.obtainMessage();
					msg.arg1 = ProcessCmdType.PROCESS_TYPE_PTT_KEY_CMD
							.ordinal();
					msg.arg2 = 1;
					cmdHandler.sendMessage(msg);
					result = true;
				} else {
					MyLog.e(tag, "OnPttKey() cmdHandler is null");
				}
			}
			intervalDown = SystemClock.uptimeMillis();
		} else {
			if (ptt_key_down) {
				if (cmdHandler != null) {
					ptt_key_down = keyDown;
					android.os.Message msg = cmdHandler.obtainMessage();
					msg.arg1 = ProcessCmdType.PROCESS_TYPE_PTT_KEY_CMD
							.ordinal();
					msg.arg2 = 0;
					cmdHandler.sendMessage(msg);
					result = true;
				} else {
					MyLog.e(tag, "OnPttKey() cmdHandler is null");
				}
			}
			/*
			 * if (SystemClock.uptimeMillis() - intervalUp <
			 * PTT_CLICK_INTERVAL_LIMIT) { // Do nothing }else{ if (cmdHandler
			 * != null) { ptt_key_down = keyDown; android.os.Message msg =
			 * cmdHandler.obtainMessage(); msg.arg1 =
			 * ProcessCmdType.PROCESS_TYPE_PTT_KEY_CMD.ordinal(); msg.arg2 = 0;
			 * cmdHandler.sendMessage(msg); result = true; }else { MyLog.e(tag,
			 * "OnPttKey() cmdHandler is null"); } } intervalUp =
			 * SystemClock.uptimeMillis();
			 */
		}
		return result;
	}
	/*
	 * Join group call using INVITE with ptt-extension
	 * "Ptt-Extension: 3ghandset rejoin".
	 */
	private boolean pttGroupJoin() {
		if (!IsPttMode())
			return false;

		PttGrp curGrp = GetCurGrp();
		if (curGrp != null) {
			if (curGrp.oVoid != null) {
				
				ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
				//Add by zzhan 2014-3-28
				if (ec.isGroupCall) {
					MyLog.e(tag, "zzhan-debugrejoin-already rejoin.");
					return true;
				}
			} else {
				ExtendedCall ec = getIdlePttLine();
				
				if (ec != null) {
					curGrp.oVoid = ec;
					SetPttMode(true);
					return groupcall(curGrp.grpID, false, ec, true);
				}
			}
		}

		return false;
	}

	/* Launch group call using INVITE with ptt-extension. */
	private boolean pttGroupCall() {
		if (!IsPttMode())
			return false;

		PttGrp curGrp = GetCurGrp();
		if (curGrp != null) {
			ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
			if (ec == null)
				ec = getIdlePttLine();

			if (ec != null) {
				curGrp.oVoid = ec;
				curGrp.isCreateSession = true;
				SetPttMode(true);
				return groupcall(curGrp.grpID, false, ec, false);
			}
		}

		return false;
	}

	/* Hang up group call using BYE. */
	private boolean pttGroupRelease(boolean bPlayTipSound, ExtendedCall ec) {
		PttGrp curGrp = GetCurGrp();
		if (curGrp != null) {
			ExtendedCall c = (ec == null) ? (ExtendedCall) curGrp.oVoid : ec;
			if (c != null)
				// grouphangup(c);
				grouphangup(curGrp);

			curGrp.isCreateSession = false;
			curGrp.oVoid = null;
			MyLog.e(tag, "SetPttStatus  PTT_IDLE pttGroupRelease.");
			SetPttStatus(PTT_IDLE);

			// Playback ptt release tip sound
			if (bPlayTipSound)
				pttReleaseTipSound();

			return true;
		}

		return false;
	}

	private long intervalDown = 0;
	private long intervalUp = 0;
	private static final int PTT_CLICK_INTERVAL_LIMIT = 500;// 900;广播情况下 不同机型 不同

	/* Invoke by UA thread. */
	private void OnPttKey2(boolean keyDown) {
		
		if(keyDown) {
			if (PTT_IDLE == ua_ptt_state) {
				onPttKeyDown();
				PttGrp curGrp = GetCurGrp();
				// Session existed
				if (curGrp.isCreateSession && curGrp.oVoid != null)
					PttGroupRequestSpeak();
				// Session not created
				else
					pttGroupCall();
			} else if (PTT_LISTENING == ua_ptt_state) {
				onPttKeyDown();
				PttGroupRequestSpeak();
			} 
		} else {
			if (PTT_TALKING == ua_ptt_state) {
				// INFO request to release talk right
				PttGroupReleaseSpeak();
				MyLog.e("hst", "onpttkey2");
			} else if (PTT_QUEUE == ua_ptt_state) {
				// cancel request
				PttGroupReleaseQueue();
			}
		}
	}
	
	private void onPttKeyDown(){
		dispatchPttGroupState(E_Grp_State.GRP_STATE_INITIATING);
	}

	private void dispatchPttGroupState(E_Grp_State grpStateInitiating) {
		PttGrp curGrp = GetCurGrp();
		
		if (curGrp != null) {
			if(curGrp.state==E_Grp_State.GRP_STATE_LISTENING){
				return ;
			}
			curGrp.state = grpStateInitiating;
			Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
			broadcastIntent.putExtra("0", curGrp.grpID);
			broadcastIntent.putExtra("1", "");
			Receiver.mContext.sendBroadcast(broadcastIntent);
		}
	}

	/* Switch ptt group */
	public void SetCurGrp(PttGrp grp) {
		LogUtil.makeLog(tag, "SetCurGrp("+(grp==null?"null":grp.toString())+")");
		if (pttGrps == null || grp == null)
			return;

		PttGrp curGrp = GetCurGrp();
		if (curGrp != null && curGrp.isCreateSession) {
			ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
			if (ec != null
					&& (PTT_TALKING == GetPttStatus() || PTT_LISTENING == GetPttStatus()))
				pttGroupRelease(false, ec);

			curGrp.isCreateSession = false;
			curGrp.oVoid = null;
		}

		pttGrps.SetCurGrp(grp);

		// Add by zzhan 2013-5-2
		// pttHeartBeatEx();
		// modify by hu 2014/2/12
		// sendHeartBeat();

		// Join new group
		curGrp = GetCurGrp();
		if (curGrp.oVoid == null) {
//			curGrp.oVoid = getIdlePttLine();
			if (!curGrp.isCreateSession) {
				pttGroupJoin();

				// Send terminal status to MDS
				if (curGrp.report_heartbeat > 0)
					StartHeartbeat(curGrp.report_heartbeat);
			}
		}

		// Adjust volume
		AudioManager am = (AudioManager) Receiver.mContext
				.getSystemService(Context.AUDIO_SERVICE);
		if (IsPttMode()) {
			if (Integer.parseInt(Build.VERSION.SDK) >= 5) {
				// zzhan 2012-11-13
				// am.setMode(AudioManager.MODE_NORMAL);
				// Delete by zzhan 2013-5-8
				// am.setSpeakerphoneOn(true);
			}
			// guojunfeng 注释
			// am.setStreamVolume(AudioManager.STREAM_MUSIC,
			// am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 3 / 4, 0);
		}

		// Notify UI ????
		Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
		broadcastIntent.putExtra("0", grp.grpID);
		broadcastIntent.putExtra("1", "");
		grp.speaker = "";
		Receiver.mContext.sendBroadcast(broadcastIntent);
		Receiver.onText(Receiver.REGISTER_NOTIFICATION, SipUAApp.mContext
				.getResources().getString(R.string.regok), R.drawable.icon64, 0);
	}

	private void StartHeartbeat(int time) {

	}

	private void StopHearbeat() {

	}

	private ExtendedCall getIdlePttLine() {
		for (int i = 0; i < calls.size(); i++) {
			ExtendedCall c = (ExtendedCall) calls.get(i);
			if (!c.isOnCall())
				return c;
		}

		return null;
	}

	public void OnRegisterSuccess(String groupsInfo) {
		LogUtil.makeLog(tag, "pttGroupParse? OnRegisterSuccess()");
		if (PTT_UNREG == GetPttStatus()) {
			MyLog.e("UserAgent", "OnRegisterSuccess run");
			MyLog.e(tag, "SetPttStatus  PTT_IDLE OnRegisterSuccess.");
			SetPttStatus(PTT_IDLE);

			pttGroupParse(groupsInfo);

			/*
			 * //GPS process if (user_profile.gps){ if (!isStartedGPS){
			 * //Started gps gpsPacket = new GPSPacket(Receiver.mContext,
			 * user_profile.username, user_profile.passwd,
			 * user_profile.realm_orig, user_profile.gps_port);
			 * 
			 * gpsPacket.StartGPS();
			 * 
			 * isStartedGPS = true; } }
			 */

			ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
			escp.type = ExtendedSipCallbackType.TYPE_REGISTER_SUCCESS;
			escp.para1 = groupsInfo;

			android.os.Message msg = cmdHandler.obtainMessage();
			msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
			msg.obj = escp;
			cmdHandler.sendMessage(msg);

			// Delete by zzhan 2013-5-2
			// report-heartbeat：终端状态上报周期，单位秒，取值范围-1到3600
			// 值为-1时，完全不上报
			// if (beatHandler.hasMessages(1)) {
			// beatHandler.removeMessages(1);
			// }
			// beatHandler.sendMessage(beatHandler.obtainMessage(1));
			//
		}

		// add by oumogang 2013-05-27
		// 注册成功后加载组列表
		// GroupListUtil.getData4GroupList();
		if (sip_provider != null && Receiver.mSipdroidEngine.isRegistered()) {
			sip_provider.setHeartBeatListner(UserAgent.this);
		}
		// sendHeartBeat();//modify by hu 2014/3/19
		// modify by hu 2014/3/28
		Receiver.alarm(SipStack.heartBeatCircle, MyHeartBeatReceiver.class);// 只启动计时器
	}

	public void OnRegisterFailure() {
		if (PTT_UNREG != GetPttStatus()) {
			MyLog.e(tag, "SetPttStatus  PTT_UNREG OnRegisterFailure.");
			SetPttStatus(PTT_UNREG);
		}
		// try {
		//
		// // Close gps
		// if (user_profile.gps) {
		// if (isStartedGPS) {
		// // Close gps
		// if (gpsPacket != null)
		// gpsPacket.ExitGPS();
		//
		// isStartedGPS = false;
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// Contants.ACTION_ALL_GROUP_CHANGE
		// add by oumogang 2013-05-27
		// 把组列表清空
		// Receiver.mContext.sendBroadcast(new
		// Intent(Contants.ACTION_ALL_GROUP_CHANGE));
		// if (GroupListUtil.mGroupListsMap.size() != 0) {
		// GroupListUtil.getData4GroupList();
		// }
	}

	// group change
	private boolean groupsChange = true;

	public static long timeOfpttAcceptTipSoundEnd;

	/* Parse ptt group information */
	private void pttGroupParse(String info) {
		LogUtil.makeLog(tag, "pttGroupParse? pttGroupParse(info)");
		if (groupsChange) {
			// Close original session
			if (PTT_TALKING == GetPttStatus()
					|| PTT_LISTENING == GetPttStatus())
				pttGroupRelease(true, null);

			pttGrps.ParseGrpInfo(info);

			if (pttGrps.GetCount() > 0) {
				if (!TextUtils.isEmpty(NetChangedReceiver.lastGrpID)) {
					LogUtil.makeLog(tag, "pttGroupParse() NetChangedReceiver.lastGrpID");
					PttGrp lastGrp = pttGrps
							.GetGrpByID(NetChangedReceiver.lastGrpID);
					if (lastGrp != null) {
						LogUtil.makeLog(tag, "pttGrps.GetGrpByID(NetChangedReceiver.lastGrpID)!=null");
						SetCurGrp(lastGrp);
					}else{
						//解决账号A登陆时，网络状态发生改变，账号A注销之后登陆账号B，当前组显示账号A对讲组的问题。。add by lwang 2014-01-05
						LogUtil.makeLog(tag, "pttGroupParse() SetCurGrp()");
						SetCurGrp(pttGrps.FirstGrp());
					}
				} else {
					LogUtil.makeLog(tag, "pttGroupParse() SetCurGrp()");
					SetCurGrp(pttGrps.FirstGrp());
				}

				// Join group
				// pttGroupJoin();

				if (IsPttMode()) {
					MyLog.e(tag, "SetPttStatus  PTT_IDLE pttGroupParse.");
					SetPttStatus(PTT_IDLE);
				}

				// Notify UI(Only notify once, because if group change, MDS will
				// send MESSAGE request) ????
				Intent broadcastIntent = new Intent(ACTION_ALL_GROUP_CHANGE);
				Receiver.mContext.sendBroadcast(broadcastIntent);

			}
			groupsChange = false;
		}
	}

	/*
	 * Send SIP INFO request to MDS, with extended header
	 * "Ptt-Extension: 3ghandset request\r\n"
	 */
	private void PttGroupRequestSpeak() {
		Zed3Log.debug("pttreqeustTrace", "UserAgent#PttGroupRequestSpeak() enter");
		PttGrp curGrp = GetCurGrp();
		if (curGrp == null)
			return;

		// If emergency group, Must not INFO request for talk
		if (curGrp.level == 0)
			return;

		String to = curGrp.grpID;
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}

		Message req;
		ExtendedCall c = (ExtendedCall) curGrp.oVoid;
		if (curGrp.isCreateSession && c != null) {
			req = MessageFactory.createRequest(c.getDialog(), SipMethods.INFO,
					null);
		} else {
			req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
					new NameAddress(to),
					new NameAddress(user_profile.from_url), null);
		}

		req.setHeader(new Header(SipHeaders.Ptt_Extension, "3ghandset request"));

		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();
	}

	/*
	 * Send SIP INFO request to MDS, with extended header
	 * "Ptt-Extension: 3ghandset cancel\r\n"
	 */
	private void PttGroupReleaseSpeak() {
		PttGrp curGrp = GetCurGrp();
		if (curGrp == null)
			return;

		String to = curGrp.grpID;
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}

		Message req;
		ExtendedCall c = (ExtendedCall) curGrp.oVoid;
		if (curGrp.isCreateSession && c != null) {
			req = MessageFactory.createRequest(c.getDialog(), SipMethods.INFO,
					null);
		} else {
			req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
					new NameAddress(to),
					new NameAddress(user_profile.from_url), null);
		}
		req.setHeader(new Header(SipHeaders.Ptt_Extension, "3ghandset cancel"));

		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

		// post cancel speak ok message to command process thread
		android.os.Message msg = cmdHandler.obtainMessage();
		msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
		ExtendedSipCallbackPara para = new ExtendedSipCallbackPara();
		para.type = ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_OK_PHONE;
		msg.obj = para;
		cmdHandler.sendMessage(msg);
	}

	/*
	 * Send SIP INFO request to MDS, with extended header
	 * "Ptt-Extension: 3ghandset cancelwaiting\r\n"
	 */
	private void PttGroupReleaseQueue() {
		PttGrp curGrp = GetCurGrp();
		if (curGrp == null)
			return;

		String to = curGrp.grpID;
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}

		Message req;
		ExtendedCall c = (ExtendedCall) curGrp.oVoid;
		if (curGrp.isCreateSession && c != null) {
			req = MessageFactory.createRequest(c.getDialog(), SipMethods.INFO,
					null);
		} else {
			req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
					new NameAddress(to),
					new NameAddress(user_profile.from_url), null);
		}

		req.setHeader(new Header(SipHeaders.Ptt_Extension,
				"3ghandset cancelwaiting"));

		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

		// post cancel speak ok message to command process thread
		android.os.Message msg = cmdHandler.obtainMessage();
		msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
		ExtendedSipCallbackPara para = new ExtendedSipCallbackPara();
		para.type = ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_WAITING_OK_PHONE;
		msg.obj = para;
		cmdHandler.sendMessage(msg);
	}

	public void ByeGroupCall(PttGrp grp) {
		ExtendedCall ec = (ExtendedCall) grp.oVoid;
		hangup(true, ec);
	}

	// Add by zzhan 2013-5-10
	public void HaltGroupCall() {
		MyLog.e("UserAgent", "HaltGroupCall");
		pttGroupRelease(false, null);
		PttGrp curGrp = GetCurGrp();
		if (curGrp != null) {
			curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
			Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
			broadcastIntent.putExtra("0", curGrp.grpID);
			broadcastIntent.putExtra("1", "");
			speaker = "";
			curGrp.speaker = speaker;
			Receiver.mContext.sendBroadcast(broadcastIntent);

			// add by oumogang 2013-10-28
			MediaButtonReceiver.releasePTT();
		}
	}

	/**
	 * When the TransactionClient is (or goes) in "Proceeding" state and
	 * receives a new 1xx provisional response
	 */
	public void onTransProvisionalResponse(TransactionClient tc, Message resp) {

	}

	/**
	 * When the TransactionClient goes into the "Completed" state receiving a
	 * 2xx response
	 */
	public void onTransSuccessResponse(TransactionClient tc, Message resp) {
		if (tc.getTransactionMethod().equals(SipMethods.INFO)) {
			StatusLine statusline = resp.getStatusLine();
			int code = statusline.getCode();
			if (!((code == 200 || code == 403) && resp.getTransactionMethod()
					.equals(SipMethods.INFO)))
				return;

			Header h = resp.getHeader(SipHeaders.Ptt_Extension);
			if (h == null)
				return;

			// Post message to command process thread
			android.os.Message msg = cmdHandler.obtainMessage();
			msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
			ExtendedSipCallbackPara para = new ExtendedSipCallbackPara();

			if (code == 200) {
				String pttExtensionValue = h.getValue();
				// 200 OK Ptt-Extension: 3ghandset accept
				if (pttExtensionValue.equalsIgnoreCase("3ghandset accept")) {
					printLog("3ghandset accept");
					para.type = ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_PHONE;
				}
				// 200 OK Ptt-Extension: 3ghandset waiting
				else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset waiting")) {
					printLog("3ghandset waiting");
					para.type = ExtendedSipCallbackType.TYPE_REQUEST_WAITING_PHONE;
				}
				// 200 OK Ptt-Extension: 3ghandset reject
				else if (pttExtensionValue.equalsIgnoreCase("3ghandset reject")) {
					printLog("3ghandset reject");
					para.type = ExtendedSipCallbackType.TYPE_REQUEST_REJECT_PHONE;
				}
				// 200 OK --for Ptt-Extension: 3ghandset cancel
				else if (pttExtensionValue.equalsIgnoreCase("3ghandset cancel")) {
					printLog("3ghandset cancel");
					para.type = ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_OK_PHONE;
				}
				// 200 OK--for Ptt-Extension: 3ghandset cancelwaiting
				else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset cancelwaiting")) {
					printLog("3ghandset cancelwaiting");
					para.type = ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_WAITING_OK_PHONE;
				} else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset getdatatotal"))// 获取套餐类型
				{

					// datatotal：XXXXXX:ptt:XXX:video:XXXX
					String str = resp.getBody().trim().replace("\r\n", ":");
					if (str != null && str.contains(":")) {
						String[] arr = str.split(":");
						// 获取用户总的套餐流量
						MemoryMg.getInstance().User_3GTotal = Double
								.parseDouble(arr[1]);
						// PTT套餐流量
						MemoryMg.getInstance().User_3GTotalPTT = Double
								.parseDouble(arr[3]);
						// Vidoe套餐流量
						MemoryMg.getInstance().User_3GTotalVideo = Double
								.parseDouble(arr[5]);
					} else
						MemoryMg.getInstance().User_3GTotal = -1;
					// MemoryMg.getInstance().User_3GTotal=30*1024*1024;
					// MemoryMg.getInstance().User_3GTotalPTT=10*1024*1024;
					// MemoryMg.getInstance().User_3GTotalVideo=20*1024*1024;
					MyLog.e(tag, "3ghandset getdatatotal recv");

					para.type = ExtendedSipCallbackType.TYPE_FLOWVIEWSCANNER_START;
				} else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset getdatastatistics"))// 获取服务器流量
				{
					MyLog.e(tag, "3ghandset getdatastatistics recv");
					// datastatistics：XXXXXX,ptt:XXX,video:XXXX,time:2013-04-12
					// 13:02:01
					String str = resp.getBody().trim().replace("\r\n", ",");
					if (str != null && str.contains(",")) {
						String[] arr = str.split(",");
						if (!TextUtils.isEmpty(arr[0])) {
							// 获取流量
							MemoryMg.getInstance().User_3GLocalTotal = Double
									.parseDouble(arr[0].split(":")[1]);
							MemoryMg.getInstance().User_3GLocalTotalPTT = Double
									.parseDouble(arr[1].split(":")[1]);
							MemoryMg.getInstance().User_3GLocalTotalVideo = Double
									.parseDouble(arr[2].split(":")[1]);
						}

						str = arr[3].substring(5);
						if (!TextUtils.isEmpty(str))
							MemoryMg.getInstance().User_3GLocalTime = str;
						else
							// 如果为空则赋值当前系统默认的时间点
							MemoryMg.getInstance().User_3GLocalTime = GetCurrentMouth(false);
					}

				}// 流量统计上报成功回复200ok
				else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset reportdatastatistics")) {
					MyLog.e(tag, "reportdatastatistics " + resp.getBody());
				}
				// guojunfeng add 2014 02 19
				else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset OfflineDataSend")) {
					Header ptt_ext_header = resp
							.getHeader(SipHeaders.Ptt_Extension);
					if (ptt_ext_header == null) {
						return;
					}
					Header id_header = resp
							.getHeader(SipHeaders.OFFLINE_DATA_ID);
					Header num_type_header = resp
							.getHeader(SipHeaders.OFFLINE_DATA_NUM_TYPE);
					Header check_id_header = resp
							.getHeader(SipHeaders.OFFLINE_DATA_CLIENT_CHECK_ID);
					Header conn_header = resp
							.getHeader(SipHeaders.OFFLINE_DATA_CONNECTION);
					if (id_header == null || num_type_header == null
							|| check_id_header == null || conn_header == null) {
						return;
					}
					// guojunfeng modify 20120924 begin
					String E_id = id_header.getValue();
					String check_id = check_id_header.getValue();
					MmsMessageService message_service = new MmsMessageService(
							conn_header.getValue(), 0, 0, E_id, check_id, null,
							null);
					// guojunfeng modify 20120924 end
					message_service.initSocket();
					// guojunfeng modify 20120930 end
				}
				// 200 OK--for Ptt-Extension: 3ghandset getstatus
				else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset getstatus")) {

					// Not complete temporarily
					printLog("3ghandset getstatus");
					para.type = ExtendedSipCallbackType.TYPE_REQUEST_GETSTATUS_PHONE;
					// 获取body体信息 3ghandset: getstatus
					// 7011(3021,3021,0;3022,3022,1;3023,3023,3;3024,3024,3)
					String str2 = resp.getBody();

					Intent broadcastIntent2 = new Intent(
							ACTION_GETSTATUS_MESSAGE);
					broadcastIntent2.putExtra("statusbody", str2);
					/* Receiver.mContext */SipUAApp.mContext
							.sendBroadcast(broadcastIntent2);

				} else {
					return;
				}

			}

			else
				return;

			msg.obj = para;
			cmdHandler.sendMessage(msg);
		} else if (tc.getTransactionMethod().equals(SipMethods.MESSAGE)) {
			StatusLine statusline = resp.getStatusLine();
			int code = statusline.getCode();
			if (code == 200) {
				// Send message to extended sip thread process
				ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
				escp.type = ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_SUCCEED_PHONE;
				// escp.para1 = String.valueOf(resp.getCSeqHeader()
				// .getSequenceNumber());
				escp.para1 = String.valueOf(resp.getCallIdHeader().getCallId());

				android.os.Message msg = cmdHandler.obtainMessage();
				msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
				msg.obj = escp;
				cmdHandler.sendMessage(msg);
			}
		}
	}

	/**
	 * When the TransactionClient goes into the "Completed" state receiving a
	 * 300-699 response
	 */
	public void onTransFailureResponse(TransactionClient tc, Message resp) {
		// Process send text message failure
		if (tc.getTransactionMethod().equals(SipMethods.MESSAGE)) {
			StatusLine statusline = resp.getStatusLine();
			int code = statusline.getCode();
			if (code == 404) {
				// Send message to extended sip thread process
				ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
				escp.type = ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_FAIL_PHONE;
				// escp.para1 = String.valueOf(resp.getCSeqHeader()
				// .getSequenceNumber());
				escp.para1 = String.valueOf(resp.getCallIdHeader().getCallId());

				android.os.Message msg = cmdHandler.obtainMessage();
				msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
				msg.obj = escp;
				cmdHandler.sendMessage(msg);
			}
		} else if (tc.getTransactionMethod().equals(SipMethods.INFO)) {
			StatusLine statusline = resp.getStatusLine();
			int code = statusline.getCode();
			if (code == 403) {
				// Post message to command process thread
				android.os.Message msg = cmdHandler.obtainMessage();
				msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
				ExtendedSipCallbackPara para = new ExtendedSipCallbackPara();
				para.type = ExtendedSipCallbackType.TYPE_REQUEST_403;
				para.para1 = String.valueOf(resp.getCallIdHeader()
						.getCallId());
				msg.obj = para;
				cmdHandler.sendMessage(msg);
			}
			
			Header h = resp.getHeader(SipHeaders.Ptt_Extension);
			if (h != null) {
				//trans failed, set the state to failed. add by mou 2015-02-02
				String headValue = h.getValue();
				if ("3ghandset OfflineDataSend".equalsIgnoreCase(headValue)) {
					Header id_header = resp
							.getHeader(SipHeaders.OFFLINE_DATA_ID);
					if (id_header == null) {
						return;
					}
					String E_id = id_header.getValue();
					if (code == 405) {
						MessageSender.updateMmsState(E_id, MessageSender.PHOTO_UPLOAD_STATE_OFFLINE_SPACE_FULL);
						String recipient_num = resp.getToHeader().getNameAddress().getAddress().getUserName();
						Intent intent = new Intent();
						intent.setAction(SmsMmsReceiver.ACTION_OFFLINE_SPACE_FULL);
						intent.putExtra("recipient_num", recipient_num);
						Receiver.mContext.sendBroadcast(intent);
					} else {
						MessageSender.updateMmsState(E_id, MessageSender.PHOTO_UPLOAD_STATE_FAILED);
					}
				}
			}
		}

	}

	/**
	 * When the TransactionClient goes into the "Terminated" state, caused by
	 * transaction timeout
	 */
	public void onTransTimeout(TransactionClient tc) {
		if (tc != null) {
			MyLog.e("guojunfengtimeout",
					"服务器连接超!..==>"
							+ tc.getTransactionMethod()
							+ "...E_id = "
							+ tc.getRequestMessage().getCallIdHeader()
									.getCallId());
		}

		if (tc == null)
			return;
		
		if(tc!=null){
			MyLog.e("guojunfengtimeout", "服务器连接超!..==>"+tc.getTransactionMethod()
					+"...E_id = "+tc.getRequestMessage().getCallIdHeader().getCallId());
			
			Systems.log.print("testptt", "UserAgent#onTransTimeout method = "+tc.getTransactionMethod()
					+", callId = "+tc.getRequestMessage().getCallIdHeader().getCallId() + " *********** request message = " + tc.getRequestMessage().toString());
			
			Message message = tc.getRequestMessage();
			
			Header h = message.getHeader(SipHeaders.Ptt_Extension);
			if (h != null) {
				
				String headValue = h.getValue();
				
				Systems.log.print("testptt", "UserAgent#onTransTimeout request message header = " + headValue);
				//申请话权请求
				if("3ghandset request".equals(headValue)) {
					
					PttGrp curGrp = GetCurGrp();
					if(curGrp!=null){
						
						Systems.log.print("testptt", "UserAgent#onTransTimeout curGrp.state = " + curGrp.state);
						
						Zed3Log.debug("pttreqeustTrace", "UserAgent#onTransTimeout() enter curGrp.state = " + curGrp.state);
						
						if(curGrp.state == E_Grp_State.GRP_STATE_IDLE || curGrp.state == E_Grp_State.GRP_STATE_LISTENING || curGrp.state == E_Grp_State.GRP_STATE_SHOUDOWN) {
							
							if(isPttKeyDown()){
								
								onPttGroupRequestTiemout(message);
								
							}
							
						} else {
							Systems.log.print("testptt", "UserAgent#onTransTimeout ptt group state = " + curGrp.state);
						}
					}
					
				} 
				//trans timeout, set the state to failed. add by mou 2015-02-02
				else if ("3ghandset OfflineDataSend".equalsIgnoreCase(headValue)) {
					Header id_header = message
							.getHeader(SipHeaders.OFFLINE_DATA_ID);
					if (id_header == null) {
						return;
					}
					String E_id = id_header.getValue();
					MessageSender.updateMmsState(E_id, MessageSender.PHOTO_UPLOAD_STATE_FAILED);
				}
				else {
					Systems.log.print("testptt", "UserAgent#onTransTimeout request message header = " + headValue);
				}
				
			} else {
				Systems.log.print("testptt", "UserAgent#onTransTimeout ptt extension header is null ");
			}
			
		}
		
		
		if (tc.getTransactionMethod().equals(SipMethods.MESSAGE)) {
			String mE_id = tc.getRequestMessage().getCallIdHeader().getCallId();
			Intent broadcastIntent = new Intent(
					ACTION_SEND_TEXT_MESSAGE_TIMEOUT);
			broadcastIntent.putExtra("E_id", mE_id);
			Receiver.mContext.sendBroadcast(broadcastIntent);
		}
	}

	private void onPttGroupRequestTiemout(Message message) {
		SipUAApp.getMainThreadHandler().post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(SipUAApp.getAppContext(), R.string.ptt_group_request_timeout, Toast.LENGTH_LONG).show();
			}
		});
	}

	/** When a new Message is received by the SipProvider. */
	public void onReceivedMessage(SipProvider sip_provider, Message message) {
		LogUtil.makeLog(tag, "pttGroupParse? onReceivedMessage()");
		if (message.isRequest()) {
			// Response 200OK
			TransactionServer ts = new TransactionServer(sip_provider, message,
					null);
			Message responseMessage = MessageFactory.createResponse(message, 200,
					SipResponses.reasonOf(200), null);
			
			Header pttExtHeader = message.getHeader(SipHeaders.Ptt_Extension);
			if (pttExtHeader != null) {
				String pttExtensionValue = pttExtHeader.getValue();
				// Queue status switch to talk
				// Ptt-Extension: 3ghandset accept
				if (pttExtensionValue.equalsIgnoreCase("3ghandset OfflineDataSend")) {
					Header id_header = message
							.getHeader(SipHeaders.OFFLINE_DATA_ID);
					
					Header check_id_header = message
							.getHeader(SipHeaders.OFFLINE_DATA_CLIENT_CHECK_ID);
					
					responseMessage.setHeader(new Header(SipHeaders.Ptt_Extension,"3ghandset OfflineDataSend"));
					responseMessage.setHeader(new Header(SipHeaders.OFFLINE_DATA_ID, id_header.getValue()));
					responseMessage.setHeader(new Header(SipHeaders.OFFLINE_DATA_CLIENT_CHECK_ID, check_id_header.getValue()));
					
				}
			}
			ts.respondWith(responseMessage);
			
			// 消息类型为Notify add by hdf 增加遥毙 销毁功能
			if (message.isRequest(SipMethods.NOTIFY)) {
				Header h = message.getHeader("Anta-Extension");
				if (h != null) {
					String Extvalue = h.getValue();
					if (Extvalue.equalsIgnoreCase("destory")) {
						// modify by hu 取消广播发送
						// 发送一个“摇毙”广播
						// Intent broadcastIntent = new
						// Intent(ACTION_DESTORY_MESSAGE);
						// Receiver.mContext.sendBroadcast(broadcastIntent);
					}
				}
				Header mms_id_header = message
						.getHeader(SipHeaders.OFFLINE_DATA_ID);
				// houyuchun add 20120606 beign
				// recipient_num
				// String recipient_num =
				// message.getFromHeader().getNameAddress().getDisplayName();
				String recipient_num = message.getFromHeader().getNameAddress()
						.getAddress().getUserName();
				// houyuchun add 20120606 end
				// sms_id_header
				if (mms_id_header != null) {
					// OfflineData-Reply header
					Header reply_header = message
							.getHeader(SipHeaders.OFFLINE_DATA_REPLY);
					if (reply_header == null) {
						return;
					}
					String reply_value = reply_header.getValue();
					Intent intent = new Intent();
					intent.setAction(SmsMmsReceiver.ACTION_DELIVERY_REPORT_REPLY);
					String id = mms_id_header.getValue();
					intent.putExtra("E_id", id);
					intent.putExtra("type", "mms");
					intent.putExtra("reply", reply_value);
					intent.putExtra("recipient_num", recipient_num);
					Receiver.mContext.sendBroadcast(intent);
				}
			}
			if (message.isRequest(SipMethods.OPTIONS)) {
				// Receiver.reRegister(60);
			}
			// 消息类型为Message
			if (message.isRequest(SipMethods.MESSAGE)) {

				// Get body which describe group information
				ContentLengthHeader clh = message.getContentLengthHeader();
				if (clh == null)
					return;

				if (Integer.valueOf(clh.getContentLength()) <= 0)
					return;

				String body = message.getBody();

				// MDS send SIP MESSAGE request when group configuration is
				// changed
				String contentType = message.getContentTypeHeader().getValue();
				if (contentType.equalsIgnoreCase("text/3ghandset")) {
					groupsChange = true;
					pttGroupParse(body);

					// Add by zzhan 2013-5-13
					pttGroupJoin();

					//do it in pttGroupParse() already. mofiy by mou 2014-11-04
					// Notify UI ????
//					Intent broadcastIntent = new Intent(ACTION_ALL_GROUP_CHANGE);
//					Receiver.mContext.sendBroadcast(broadcastIntent);
				}
				// Receive text message
				else if (contentType.equalsIgnoreCase("text/plain")) {
					// Delete last \r\n
					String end = "\r\n";
					if (body.endsWith(end)) {
						body = body.substring(0, body.length() - end.length());
					}

					// Delete "\r\n" twice
					if (body.endsWith(end)) {
						body = body.substring(0, body.length() - end.length());
					}

					TextMessage textMsg = new TextMessage();
					textMsg.from = message.getFromHeader().getNameAddress()
							.getAddress().getUserName();
					textMsg.to = user_profile.username;
					textMsg.content = body;
					textMsg.seq = String.valueOf(message.getCSeqHeader()
							.getSequenceNumber());
					if (message.getFromHeader().getNameAddress()
							.getDisplayName() != null) {
						textMsg.sipName = message.getFromHeader()
								.getNameAddress().getDisplayName();
						MyLog.e("sipname", "sipname=" + textMsg.sipName);
					}
					MyLog.e("MESSAGE==>", body);
					// android.util.Log.e("useragent", body);
					//
					if ((!lastIMContent.equalsIgnoreCase(textMsg.content))
							|| (!lastIMSeq.equalsIgnoreCase(textMsg.seq))) {
						MyLog.e("MESSAGE==><", body);
						// Send message to extended sip thread process
						ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();
						escp.type = ExtendedSipCallbackType.TYPE_RECEIVE_TEXT_MESSAGE_PHONE;
						escp.para2 = textMsg;

						android.os.Message msg = cmdHandler.obtainMessage();
						msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD
								.ordinal();
						msg.obj = escp;
						cmdHandler.sendMessageDelayed(msg, 1500);// .sendMessage(msg);

						lastIMContent = textMsg.content;
						lastIMSeq = textMsg.seq;
					}

				}// 消息类型为info
			} else if (message.isRequest(SipMethods.INFO)) {
				Header h = message.getHeader(SipHeaders.Ptt_Extension);
				if (h == null)
					return;
				String pttExtensionValue = h.getValue();

				ExtendedSipCallbackPara escp = new ExtendedSipCallbackPara();

				// Queue status switch to talk
				// Ptt-Extension: 3ghandset accept
				if (pttExtensionValue.equalsIgnoreCase("3ghandset accept")) {
					escp.type = ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_PHONE;
				}
				// Server release talk right
				// Ptt-Extension: 3ghandset forcecancel
				else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset forcecancel")) {
					escp.type = ExtendedSipCallbackType.TYPE_SERVER_FORCECANCEL_PHONE;
				}
				// Server release queue right
				// Ptt-Extension: 3ghandset forcecancelwaiting
				else if (pttExtensionValue
						.equalsIgnoreCase("3ghandset forcecancelwaiting")) {
					escp.type = ExtendedSipCallbackType.TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE;
				}
				else if(pttExtensionValue.equalsIgnoreCase("3ghandset OfflineDataSend")) {
					Header ptt_ext_header = message
							.getHeader(SipHeaders.Ptt_Extension);
					if (ptt_ext_header == null) {
						return;
					}
					Header id_header = message
							.getHeader(SipHeaders.OFFLINE_DATA_ID);
					Header check_id_header = message
							.getHeader(SipHeaders.OFFLINE_DATA_CLIENT_CHECK_ID);
					Header conn_header = message
							.getHeader(SipHeaders.OFFLINE_DATA_CONNECTION);
					
					Header size_header = message.getHeader(SipHeaders.OFFLINE_DATA_SIZE);
					String sizeString = size_header.getValue();
					
					if (id_header == null || size_header == null
							|| check_id_header == null || conn_header == null) {
						return;
					}
					
					String recipient_num = message.getFromHeader().getNameAddress()
							.getAddress().getUserName();
					// guojunfeng modify 20120924 begin
					String E_id = id_header.getValue();
					String check_id = check_id_header.getValue();
					
					int size = !TextUtils.isEmpty(sizeString) ? Integer.parseInt(sizeString) : 0;
					
					MmsMessageService message_service = new MmsMessageService(
							conn_header.getValue(), 1, size, E_id, check_id, recipient_num,
							null);
					// guojunfeng modify 20120924 end
					message_service.initSocket();
					// guojunfeng modify 20120930 end
				}
				// Group info
				// Ptt-Extension: 3ghandset status 0
				// Ptt-Extension: 3ghandset status 1 speakernum
				else if (pttExtensionValue.startsWith("3ghandset status")) {
					escp.type = ExtendedSipCallbackType.TYPE_PTT_STATUS_PHONE;
					pttExtensionValue = pttExtensionValue.replace(
							"3ghandset status ", "");
					if (pttExtensionValue.startsWith("1"))
						escp.para1 = pttExtensionValue.substring(2);
					else
						escp.para1 = "";
					// 未完，需要等待服务器修改后测试
					// PttGrp curGrp = GetCurGrp();
					// String str = message.getHeader("From").getValue();
					// if (!str.equals("")) {
					// str = str.substring(str.indexOf(":") + 1,
					// str.indexOf("@")).trim();
					// MyLog.e(tag,
					// "sip from mds groupID:"+str+" curGrpID:"+curGrp.grpID);
					//
					// if (curGrp !=
					// null//服务器要向所有的组发送给本机，所以要在此判断服务器发来的info组是否是当前组
					// && !curGrp.grpID.equalsIgnoreCase(str)) {
					// if (!curGrp.isCreateSession)//判断当前组是否有通话
					// pttGroupJoin();
					// }
					// }

				}
				// 从mds上判断联通客服密码是否正确
				else if (pttExtensionValue.contains("3ghandset auth")) {
					escp.type = ExtendedSipCallbackType.TYPE_UNIONPASSWORDLOGIN_STATE;
					MyLog.e(tag, "3ghandset auth " + pttExtensionValue);
					if (pttExtensionValue.contains("fail"))
						// 失败
						escp.para1 = "fail";
					else
						// 成功
						// 不做任何操作
						escp.para1 = "ok";
				} else
					return;

				// Send message to extended sip thread process
				android.os.Message msg = cmdHandler.obtainMessage();
				msg.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
				msg.obj = escp;
				cmdHandler.sendMessage(msg);
			}
		}
	}

	public PttGrp GetGrpByID(String ID) {
		return pttGrps.GetGrpByID(ID);
	}

	private int GetPttStatus() {
		return ua_ptt_state;
	}

	public int GetPttStatusForLine() {
		return ua_ptt_state;
	}

	private void SetPttStatus(int state) {
		if (state == ua_ptt_state)
			return;

		ua_ptt_state = state;

		// Notify UI ????
		PttGrp grp = GetCurGrp();
		if (grp == null)
			return;

		// If unregistration, return
		if (PTT_UNREG == GetPttStatus())
			return;

		Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
		broadcastIntent.putExtra("0", grp.grpID);
		// Modify by zzhan 2014-1-26
		// broadcastIntent.putExtra("1", speaker);
		broadcastIntent.putExtra("1", speaker.equals("") ? ""
				: (speakerN + " " + speaker));
		grp.speaker = speaker;
		grp.speakerN = speakerN;
		Receiver.mContext.sendBroadcast(broadcastIntent);
	}

	public boolean IsPttMode() {
		return ua_ptt_mode;
	}

	// Add by zzhan 2013-5-15
	public void pttSpeakerControl() {
		if (audio_app == null)
			return;
		// use AudioUtil to set mode , modify by oumogang 2014-03-30
		// if (true) {
		// audio_app.speakerMedia(AudioManager.MODE_NORMAL);
		// } else {
		// audio_app.speakerMedia(AudioManager.MODE_IN_CALL);
		// }
//		AudioUtil.getInstance().setAudioConnectMode(TalkBackNew.mAudioMode);
	}
	
	/*
	 * Answer another incoming group call when current group is different or
	 * current mode is single call
	 */
	public void answerGroupCall(PttGrp grp) {
		handleCallStatus(
					CallStatus.ACCEPT_GROUP_CALL
							  .addWorkArg(grp)
				);
	}
	
	private boolean answerGroupCallInner(PttGrp grp) {

		// Single call state
		if (!IsPttMode()) {
			// Emergency group call
			if (grp.level == 0) {
				hangup();
			}
			// Pop msg to tip user
			else {
				// Pop msg to tip user ????
				Intent broadcastIntent = new Intent(ACTION_SINGLE_2_GROUP);
				broadcastIntent.putExtra("0", grp.grpID);
				Receiver.mContext.sendBroadcast(broadcastIntent);
				return false;
			}
		}

		// If another goup call, Bye original dialog first
		PttGrp curGrp = GetCurGrp();
		if (curGrp != null && !curGrp.grpID.equalsIgnoreCase(grp.grpID)) {
			// if (PTT_TALKING == GetPttStatus() || PTT_LISTENING ==
			// GetPttStatus()){
			pttGroupRelease(false, null);
			// }

			curGrp.isCreateSession = false;
			curGrp.oVoid = null;
		}

		MyLog.e(tag, "SetPttStatus  PTT_LISTENING answerGroupCall.");
		SetPttStatus(PTT_LISTENING);
		SetPttMode(true);

		if (curGrp != null && !curGrp.grpID.equalsIgnoreCase(grp.grpID)) {
			if (grp.level == 0) {
				preGrpBeforeEmergencyCall = curGrp.grpID;
			}
			if(automaticAnswer && "".equals(preGroup)){
				preGroup = curGrp.grpID;
			}
			SetCurGrp(grp);

			Intent broadcastIntent = new Intent(Contants.ACTION_CURRENT_GROUP_CHANGED);
			Receiver.mContext.sendBroadcast(broadcastIntent);
		}

		// Answer group call
		Object oVoid = grp.oVoid;
		if(oVoid!=null){
			ExtendedCall ec = (ExtendedCall) grp.oVoid;
			groupAccept(ec);
			grp.isCreateSession = true;
		}

		return true;
	
	}

	/* Send text message. Return value : text message seq */
	public String SendTextMessage(String to, String content) {
		// if (PTT_UNREG == GetPttStatus())
		// return null;

		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}

		Message req = MessageFactory.createRequest(sip_provider,
				SipMethods.MESSAGE, new NameAddress(to), new NameAddress(
						user_profile.from_url), content);
		// req.setHeader(new Header(SipHeaders.Ptt_Extension, "ipm"));
		req.setHeader(new Header(SipHeaders.Content_Type, "text/plain"));

		String callId = String.valueOf(req.getCallIdHeader().getCallId());

		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

		return callId;
	}

//	public void playsound(int id) {
//		if (Settings.mNeedBlueTooth
//				&& ZMBluetoothManager.getInstance().isSPPConnected()) {
//			BluetoothPaControlUtil.setPaOn(SipUAApp.mContext, true);
//		}
//
//		AudioTrack m_out_trk;
//		int m_out_buf_size;
//		byte[] m_out_bytes;
//		int channel = AudioFormat.ENCODING_PCM_16BIT;
//		int sampleRateInHz = 44100;
//		if (id == R.raw.pttaccept) {
//			channel = AudioFormat.ENCODING_PCM_8BIT;
//			sampleRateInHz = 44100 / /* 4 */4;
//		} else if (id == R.raw.pttrelease) {
//			channel = AudioFormat.ENCODING_PCM_16BIT;
//			sampleRateInHz = 44100 / /* 4 */4;
//		} else if (id == R.raw.imreceive) {
//			channel = AudioFormat.ENCODING_PCM_16BIT;
//			sampleRateInHz = 44100 / 2;
//		}
//
//		if (id == R.raw.pttaccept) {
//			channel = AudioFormat.ENCODING_PCM_8BIT;
//			sampleRateInHz = 44100 / /* 4 */4;
//		} else if (id == R.raw.pttrelease) {
//			channel = AudioFormat.ENCODING_PCM_16BIT;
//			sampleRateInHz = 44100 / /* 4 */4;
//		}
//
//		else if (id == R.raw.pttaccept8k16bit || id == R.raw.pttrelease8k16bit) {
//			channel = AudioFormat.ENCODING_PCM_16BIT;
//			sampleRateInHz = 8000;
//		}
//
//		else if (id == R.raw.pttaccept16k16bit
//				|| id == R.raw.pttrelease16k16bit) {
//			channel = AudioFormat.ENCODING_PCM_16BIT;
//			sampleRateInHz = 16000;
//		}
//
//		m_out_buf_size = AudioTrack.getMinBufferSize(sampleRateInHz,
//				AudioFormat.CHANNEL_CONFIGURATION_MONO, // CHANNEL_CONFIGURATION_MONO,
//				// CHANNEL_CONFIGURATION_STEREO
//				channel);
//		// if (JAudioLauncher.receiver != null &&
//		// JAudioLauncher.receiver.getAudioTrack() != null && sampleRateInHz ==
//		// 8000 && channel == AudioFormat.ENCODING_PCM_16BIT) {
//		// m_out_trk = JAudioLauncher.receiver.getAudioTrack();
//		// isReceiverAudioTrack = true;
//		// }else {
//		// int streamType = TalkBackNew.mAudioMode ==
//		// AudioUtil.MODE_SPEAKER?AudioManager.STREAM_MUSIC:AudioManager.STREAM_VOICE_CALL;
//		// m_out_trk = new AudioTrack(streamType, sampleRateInHz,
//		// AudioFormat.CHANNEL_CONFIGURATION_STEREO, //
//		// CHANNEL_CONFIGURATION_MONO,
//		// // CHANNEL_CONFIGURATION_STEREO
//		// channel, m_out_buf_size, AudioTrack.MODE_STREAM);
//		// isReceiverAudioTrack = false;
//		// }
//		int streamType = TalkBackNew.mAudioMode == AudioUtil.MODE_SPEAKER ? AudioManager.STREAM_MUSIC
//				: AudioManager.STREAM_VOICE_CALL;
//		m_out_trk = new AudioTrack(streamType, sampleRateInHz,
//				AudioFormat.CHANNEL_CONFIGURATION_MONO, // CHANNEL_CONFIGURATION_MONO,
//				// CHANNEL_CONFIGURATION_STEREO
//				channel, m_out_buf_size, AudioTrack.MODE_STREAM);
//
//		m_out_bytes = new byte[m_out_buf_size];
//		InputStream is = Receiver.mContext.getResources().openRawResource(id);
//		try {
//			m_out_trk.play();
//			int len;
//			byte[] speechBuffer = new byte[320];
//			while (true) {
//				// len = is.read(m_out_bytes);
//				len = is.read(speechBuffer, 0, 320);
//				if (len == -1)
//					break;
//
//				// m_out_trk.write(m_out_bytes, 0, len);
//				m_out_trk.write(speechBuffer, 0, len);
//				// Thread.sleep(20);
//			}
//			m_out_trk.stop();
//			m_out_trk = null;
//			is.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (Settings.mNeedBlueTooth
//					&& ZMBluetoothManager.getInstance().isSPPConnected()) {
//				BluetoothPaControlUtil.setPaOn(SipUAApp.mContext, false);
//			}
//		}
//
//	}

	static byte[] buffer = null;
	AudioTrack at = null;
	int pcmlen = 0;
	int sampleRateInHz = 0;
	int channel = 0;
	int audioFormat = 0;

	/**
	 * use MODE_STATIC instead of MODE_STREAM to play shout audio data modify by
	 * oumgoang 2014-03-12
	 * 
	 * @param id
	 */
	private void playSoundByStatic(int id) {
		/*
		 * AudioManager am = (AudioManager)
		 * Receiver.mContext.getSystemService(Context.AUDIO_SERVICE); if
		 * (IsPttMode()){ if (Integer.parseInt(Build.VERSION.SDK) >= 5)
		 * am.setSpeakerphoneOn(false);
		 * 
		 * //am.setStreamVolume(AudioManager.STREAM_MUSIC,
		 * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0); }
		 */

		if (Settings.mNeedBlueTooth
				&& ZMBluetoothManager.getInstance().isSPPConnected()) {
			BluetoothPaControlUtil.setPaOn(SipUAApp.mContext, true);
		}

		if (id == R.raw.pttaccept) {
			audioFormat = AudioFormat.ENCODING_PCM_8BIT;
		} else if (id == R.raw.pttrelease) {
			audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		} else if (id == R.raw.imreceive) {
			audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		}

		int streamType = AudioManager.STREAM_MUSIC;
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth
				&& ZMBluetoothManager.getInstance() != null) {
			streamType = ZMBluetoothManager.getInstance().isHeadSetEnabled() ? AudioManager.STREAM_VOICE_CALL
					: AudioManager.STREAM_MUSIC;
		}
		try {
			InputStream is = Receiver.mContext.getResources().openRawResource(
					id);
			buffer = new byte[is.available()];// 2M
			is.read(buffer/* ,0,buffer.length */);
			pcmlen = 0;
			pcmlen += buffer[0x2b];
			pcmlen = pcmlen * 256 + buffer[0x2a];
			pcmlen = pcmlen * 256 + buffer[0x29];
			pcmlen = pcmlen * 256 + buffer[0x28];

			sampleRateInHz = 0;
			sampleRateInHz += buffer[0x1b];
			sampleRateInHz = sampleRateInHz * 256 + buffer[0x1a];
			sampleRateInHz = sampleRateInHz * 256 + buffer[0x19];
			sampleRateInHz = sampleRateInHz * 256 + buffer[0x18];

			channel = 0;
			channel = buffer[0x17];
			channel = channel * 256 + buffer[0x16];

			// audioFormat=0;
			// audioFormat=buffer[0x15];
			// audioFormat=audioFormat*256+buffer[0x14];

			// at = new AudioTrack(AudioManager.STREAM_MUSIC, /*44100*/8000,
			// channel,
			// AudioFormat.ENCODING_PCM_16BIT,
			// pcmlen,
			// AudioTrack.MODE_STATIC);

			at = new AudioTrack(streamType, /* 44100 */sampleRateInHz, channel,
					audioFormat, pcmlen, AudioTrack.MODE_STATIC);
			at.write(buffer, 0x2C, pcmlen);
			at.play();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (Settings.mNeedBlueTooth
					&& ZMBluetoothManager.getInstance().isSPPConnected()) {
				BluetoothPaControlUtil.setPaOn(SipUAApp.mContext, false);
			}
		}

	}

	private void pttReleaseTipSound() {
		if (IsPttMode()) {
			// Stop media
			// closeMediaApplication();

			// Play ptt release sound
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					// playsound(R.raw.pttrelease);
//					playsound(R.raw.pttrelease8k16bit);
//					// playsound(R.raw.pttrelease16k16bit);
//				}
//			}).start();
			TipSoundPlayer.getInstance().play(Sound.PTT_RELEASE);
			// Avoid ptt tip sound bring some echo
			// Thread.sleep(200);
		}
	}

	private void pttAcceptTipSound() {
		if (IsPttMode()) {
			// Stop media
			// closeMediaApplication();
			// Logger.i(true, tag,
			// "time--pttAcceptTipSound()  begin:"+System.currentTimeMillis());
			// Play ptt release sound
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					// playsound(R.raw.pttaccept);
//					playsound(R.raw.pttaccept8k16bit);
//					// playsound(R.raw.pttaccept16k16bit)
//				}
//			}).start();
			TipSoundPlayer.getInstance().play(Sound.PTT_ACCEPT);
			// Avoid ptt tip sound bring some echo
			SystemClock.sleep(500);
			// timeOfpttAcceptTipSoundEnd = System.currentTimeMillis();
			// Logger.i(true, tag,
			// "time--pttAcceptTipSound() end:"+timeOfpttAcceptTipSoundEnd);
		}
	}
	
	private void pttTextMessageTipSound() {
		// Play ptt release sound
		// playsound(R.raw.imreceive);
//		playMessageAcceptSound(R.raw.imreceive);
		TipSoundPlayer.getInstance().play(Sound.MESSAGE_ACCEPT);
		
	}

	private void playMessageAcceptSound(int id) {
		MediaPlayer mp = null;
		mp = MediaPlayer.create(SipUAApp.mContext, id);
		if (mp != null) {
			mp.start();
		}
	}
	
	/*
	 * 紧急对讲组与高优先级对讲组来电关闭后恢复原对讲组
	 * edit by zdx
	 */
	private void restorePreGrp() {
		if (preGrpBeforeEmergencyCall.length() <= 0 && preGroup.length() <= 0)
			return;

		PttGrp curGrp = GetCurGrp();
		if (curGrp != null) {
			if (0 == curGrp.level) {
				LogUtil.makeLog(tag, "restorePreGrp() 0 == curGrp.level  SetCurGrp()");
				SetCurGrp(GetGrpByID(preGrpBeforeEmergencyCall));
				preGrpBeforeEmergencyCall = "";
			}
		}
		SharedPreferences preferences = Receiver.mContext.getSharedPreferences(
				"com.zed3.sipua_preferences", Activity.MODE_PRIVATE);
		boolean isRestore = preferences.getBoolean(Settings.RESTORE_AFTER_OTHER_GROUP, true);
		if(isRestore && automaticAnswer) {
			LogUtil.makeLog(tag, "restorePreGrp() isRestore && automaticAnswer SetCurGrp()");
			SetCurGrp(GetGrpByID(preGroup));
			preGroup = "";
			automaticAnswer = false;
		}
		Intent broadcastIntent = new Intent(Contants.ACTION_CURRENT_GROUP_CHANGED);
		Receiver.mContext.sendBroadcast(broadcastIntent);
		
		MyLog.e(tag, "SetPttStatus  PTT_IDLE restorePreGrp.");
		SetPttStatus(PTT_IDLE);
	}

	private boolean isPttKeyDown() {
		return ptt_key_down;
	}

	private int statusBeforeQueue = PTT_IDLE;
	private E_Grp_State grpStateBeforeQueue = E_Grp_State.GRP_STATE_IDLE;

	/* 自动接听 */ 
	private boolean automaticAnswer = false;

	private void extendedSipProcess(ExtendedSipCallbackPara para) {
		// INFO for talk, response is ok. Or INVITE create group call, response
		// is ok.
		if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_PHONE
				|| para.type == ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_LINE) {
			PttGrp curGrp = GetCurGrp();
			if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_LINE) {
				// Current line is not equal to param line, hangup this call
				if (curGrp.oVoid != para.para2) {
					pttGroupRelease(false, (ExtendedCall) para.para2);
					return;
				}

				curGrp.isCreateSession = true;
			}

			if (GetPttStatus() != PTT_TALKING) {
				// If key is up state, hang up.
				if (!isPttKeyDown()) {
					// Add by zzhan 2014-5-5
					// Fix bug:Media session do not start when receive
					// 200OK-Accept and ptt key is keyup status
					ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
					startMediaApplication(ec, -1);// mode : duplex= 0,
													// recv-only=
													// -1, send-only= +1;

					PttGroupReleaseSpeak();
					MyLog.e("hst", "extendsSipProcess");
				} else if (/* GetPttStatus() != PTT_QUEUE && */PTT_TALKING != GetPttStatus()) {
					ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
					SetPttStatus(PTT_TALKING);
					MyLog.e(tag,
							"SetPttStatus  PTT_TALKING TYPE_REQUEST_ACCEPT_PHONE.");
					pttAcceptTipSound();
					startMediaApplication(ec, 1);// mode : duplex= 0, recv-only=
													// -1, send-only= +1;
				}
			}
		}
		// INFO for talk response is reject
		else if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_REJECT_PHONE) {
			PttGrp curGrp = GetCurGrp();
			if (curGrp != null) {
				if (!curGrp.isCreateSession) {
					// Current grp ui nofify???
					curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
					Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
					broadcastIntent.putExtra("0", curGrp.grpID);
					broadcastIntent.putExtra("1", "");
					curGrp.speaker = speaker;
					Receiver.mContext.sendBroadcast(broadcastIntent);

					// add by oumogang 2013-10-28
					MediaButtonReceiver.releasePTT();
					// All groups ui notify???
				}
			}
		}
		// INFO for talk success, response is waiting. Or INVITE create group
		// call, response is waiting
		else if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_WAITING_LINE
				|| para.type == ExtendedSipCallbackType.TYPE_REQUEST_WAITING_PHONE) {
			if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_WAITING_LINE) {
				// Current line is not equal to param line, hangup this call
				PttGrp curGrp = GetCurGrp();
				if (curGrp.oVoid != para.para2) {
					pttGroupRelease(false, (ExtendedCall) para.para2);
					return;
				}
			}

			// If ptt key is up state, cancel queue
			if (!isPttKeyDown()) {
				PttGroupReleaseQueue();
			} else if (PTT_QUEUE != GetPttStatus()) {
				// Record status before queue status
				statusBeforeQueue = GetPttStatus();

				// Record grp state before queue status
				PttGrp curGrp = GetCurGrp();
				grpStateBeforeQueue = curGrp.state;

				// Current grp ui nofify???
				curGrp.state = E_Grp_State.GRP_STATE_QUEUE;
				Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
				broadcastIntent.putExtra("0", curGrp.grpID);
				// Modify by zzhan 2014-1-26
				// broadcastIntent.putExtra("1", "");
				broadcastIntent.putExtra("1", speaker.equals("") ? ""
						: (speakerN + " " + speaker));

				curGrp.speaker = speaker;
				Receiver.mContext.sendBroadcast(broadcastIntent);

				// All groups ui notify???
				MyLog.e(tag,
						"SetPttStatus  PTT_QUEUE TYPE_REQUEST_WAITING_LINE.");
				SetPttStatus(PTT_QUEUE);
			}
		}
		// INFO for cancel waiting, response is ok. Or Server force cancel
		// waiting
		else if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_WAITING_OK_PHONE
				|| para.type == ExtendedSipCallbackType.TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE) {
			// Restore status before queue status
			if (PTT_QUEUE == GetPttStatus()) {
				PttGrp curGrp = GetCurGrp();
				if (statusBeforeQueue == PTT_IDLE) {
					statusBeforeQueue = PTT_LISTENING;
					grpStateBeforeQueue = E_Grp_State.GRP_STATE_LISTENING;
					ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
					startMediaApplication(ec, -1);// mode : duplex= 0,
													// recv-only= -1, send-only=
													// +1;
				}
				MyLog.e(tag, "SetPttStatus  " + statusBeforeQueue
						+ " TYPE_REQUEST_CANCEL_WAITING_OK_PHONE.");
				SetPttStatus(statusBeforeQueue);

				// Current grp ui nofify???
				curGrp.state = grpStateBeforeQueue;
				Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
				broadcastIntent.putExtra("0", curGrp.grpID);
				// Modify by zzhan 2014-1-26
				// broadcastIntent.putExtra("1", speaker/*""*/);//edit by hdf
				// 20130106
				broadcastIntent.putExtra("1", speaker.equals("") ? ""
						: (speakerN + " " + speaker));
				curGrp.speaker = speaker;
				Receiver.mContext.sendBroadcast(broadcastIntent);

				// All groups ui notify???
			}
		}
		// Server force cancel client talking right. Or Client cancel talking
		// right initiatively.
		else if (para.type == ExtendedSipCallbackType.TYPE_SERVER_FORCECANCEL_PHONE
				|| para.type == ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_OK_PHONE) {
			if (PTT_TALKING == GetPttStatus()) {
				PttGrp curGrp = GetCurGrp();
				ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
				startMediaApplication(ec, -1);// mode : duplex= 0, recv-only=
												// -1, send-only= +1;

				if (IsPttMode() && PTT_TALKING == GetPttStatus())
					pttReleaseTipSound();

				// Switch to listen mode
				MyLog.e(tag,
						"SetPttStatus  PTT_LISTENING TYPE_REQUEST_CANCEL_WAITING_OK_PHONE.");
				SetPttStatus(PTT_LISTENING);

				// Current grp ui nofify???
				curGrp.state = E_Grp_State.GRP_STATE_IDLE;
				Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
				broadcastIntent.putExtra("0", curGrp.grpID);
				broadcastIntent.putExtra("1", "");
				curGrp.speaker = "";
				Receiver.mContext.sendBroadcast(broadcastIntent);

				// All groups ui notify???
				MediaButtonReceiver.releasePTT();
			}
		} else if (para.type == ExtendedSipCallbackType.TYPE_RECEIVE_TEXT_MESSAGE_PHONE) {
			TextMessage msg = (TextMessage) para.para2;
			// 收到多条消息？？？？
			// android.util.Log.e("recvMSG+++++++++++++++", "+++++++++++");
			// Notify UI ????
			Intent broadcastIntent = new Intent(ACTION_RECEIVE_TEXT_MESSAGE);
			// broadcastIntent.putExtra("0", msg.from);
			// broadcastIntent.putExtra("1", msg.to);
			broadcastIntent.putExtra("2", msg.seq);
			// broadcastIntent.putExtra("3", msg.content);
			// broadcastIntent.putExtra("4", msg.sipName);
			SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(
					Receiver.mContext);
			ContentValues mContentValues = new ContentValues();
			mContentValues.put("body", msg.content);
			mContentValues.put("mark", 0);
			mContentValues.put("address", msg.from);
			mContentValues.put("status", 0);
			mContentValues.put("sip_name", msg.sipName);
			mContentValues.put("type", "sms");
			if (msg.from.equals(msg.to))
				mContentValues.put("status", 1);
			mContentValues.put("date", CommonUtil.getCurrentTime());
			int E_id = (new Random()).nextInt(9999999);
			MyLog.e("guojunfeng-random-E_id", E_id + "");
			mContentValues.put("E_id", E_id);
			mSmsMmsDatabase.insert("message_talk", mContentValues);
			MyLog.e("message-->", msg.content);
			Receiver.mContext.sendBroadcast(broadcastIntent);

			// Play tip sound收到短消息的声音
			pttTextMessageTipSound();
		} else if (para.type == ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_FAIL_PHONE) {
			// Notify UI ????
			Intent broadcastIntent = new Intent(ACTION_SEND_TEXT_MESSAGE_FAIL);
			// Text message seq
			broadcastIntent.putExtra("0", para.para1);
			Receiver.mContext.sendBroadcast(broadcastIntent);
		} else if (para.type == ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_SUCCEED_PHONE) {
			// guojunfenging
			Intent broadcastIntent = new Intent(
					ACTION_SEND_TEXT_MESSAGE_SUCCEED);
			broadcastIntent.putExtra("0", para.para1);
			Receiver.mContext.sendBroadcast(broadcastIntent);
			// CommonUtil.ToastLong(Receiver.mContext, "TYPE_SEND"+para.para1);

		} else if (para.type == ExtendedSipCallbackType.TYPE_PTT_STATUS_PHONE) {
			/*
			 * Server only send current group status Format: Ptt-Extension:
			 * 3ghandset status 0 Ptt-Extension: 3ghandset status 1 speakernum
			 */
			PttGrp curGrp = GetCurGrp();
			if (curGrp != null) {
				// Record receive grp time
				curGrp.lastRcvTime = SystemClock.currentThreadTimeMillis();
				String speakerNum = para.para1.toString();

				// Delete by zzhan 2014-1-26
				// name number
				// speaker = speakerNum;

				// Modify by zzhan 2014-1-26
				// No speaker
				// if (speaker.length() == 0) {
				if (speakerNum.length() == 0) {
					// If current state is queue state, keep queue state
					if (GetCurGrp().state == E_Grp_State.GRP_STATE_QUEUE)
						curGrp.state = E_Grp_State.GRP_STATE_QUEUE;
					else {
						speaker = "";
						speakerN = "";
						curGrp.state = E_Grp_State.GRP_STATE_IDLE;
					}
				} else {
					// String[] arr = speaker.split(" ");
					String[] arr = speakerNum.split(" ");
					speakerN = arr[0];
					if (arr.length > 1) {
						speaker = arr[1];
					}
					// speaker is not me
					// num
					if (!speakerN.equalsIgnoreCase(user_profile.username)) {
						if (curGrp.state != E_Grp_State.GRP_STATE_QUEUE)
							curGrp.state = E_Grp_State.GRP_STATE_LISTENING;
						// When queue state and speaker is not me, do not
						// process talking info
						else
							curGrp.state = E_Grp_State.GRP_STATE_QUEUE;
					} else
						curGrp.state = E_Grp_State.GRP_STATE_TALKING;
				}

				// When in single call mode, set as idle state
				if (!IsPttMode())
					curGrp.state = E_Grp_State.GRP_STATE_IDLE;

				// Current grp ui nofify???
				Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
				broadcastIntent.putExtra("0", curGrp.grpID);
				// Modify by zzhan 2014-1-26
				// broadcastIntent.putExtra("1", speakerNum);
				broadcastIntent.putExtra("1", speaker.equals("") ? ""
						: (speakerN + " " + speaker));
				curGrp.speaker = speaker;
				Receiver.mContext.sendBroadcast(broadcastIntent);

				// All groups ui notify???
			}
		} else if (para.type == ExtendedSipCallbackType.TYPE_PEER_INVITE_LINE) {
			String callGrpID = para.para1.toString();
			PttGrp curGrp = GetCurGrp();
			PttGrp paraGrp = GetGrpByID(callGrpID);
			if (paraGrp != null) {
				// Delete by zzhan 2013-5-6
				/*
				 * // Save group call relative phone line paraGrp.oVoid =
				 * (ExtendedCall) para.para2;
				 */
				int pttStatus = GetPttStatus();

				// Invite of the same group
				if (curGrp.grpID.equalsIgnoreCase(callGrpID)) {
					// Current group has created session, this invite is no
					// possible(exception)
					if (curGrp.isCreateSession) {
						// grouphangup((ExtendedCall)para.para2);
						grouphangup(curGrp);
					}

					// Add by zzhan 2012-5-6
					// grouphangup function will close this call
					paraGrp.oVoid = (ExtendedCall) para.para2;

					// When queue status and invite by the same grp, do not
					// process
					if (pttStatus == PTT_QUEUE) {
						// do nothing
					} else if (!curGrp.isCreateSession)
						answerGroupCall(paraGrp);
				}
				// Invite of other group
				else {
					PttGrp callingInGrp = GetGrpByID(callGrpID);
					if (callingInGrp == null)
						return;

					// Add by zzhan 2013-5-6
					// Save group call relative phone line
					paraGrp.oVoid = (ExtendedCall) para.para2;

					// When other group call use current group line, set current
					// group call line to null
					if (para.para2 == curGrp.oVoid)
						curGrp.oVoid = null;

					// IF emergency call(level 0), answer directly
					if (paraGrp.level == 0)
						answerGroupCall(paraGrp);

					//TODO Normal call(level > 0)
					else {
						// Pop msgbox notify user when grp change(including
						// higher level)
						if (0 != curGrp.level || pttStatus == PTT_IDLE) {
							// Tip
							if (((curGrp.level > callingInGrp.level) && (grpCallSetupHigh == GrpCallSetupType.GRPCALLSETUPTYPE_TIP))
									|| ((curGrp.level == callingInGrp.level) && (grpCallSetupSame == GrpCallSetupType.GRPCALLSETUPTYPE_TIP))
									|| ((curGrp.level < callingInGrp.level) && (grpCallSetupLow == GrpCallSetupType.GRPCALLSETUPTYPE_TIP))) {

								// If in single call mode, process as single to
								// grp though grp id is different
								if (IsPttMode()) {
									// Notify UI ????
									// msg.message = WIN_MSG_GRP_CALL_CHANGE;
									Intent broadcastIntent = new Intent(
											ACTION_GROUP_2_GROUP);
									broadcastIntent
											.putExtra("0", paraGrp.grpID);
									SipUAApp.getAppContext()
											.sendBroadcast(broadcastIntent);
								} else {
									// Notify UI ????
									// msg.message =
									// WIN_MSG_GRP_CALLIN_WHEN_SINGLE;
									Intent broadcastIntent = new Intent(
											ACTION_SINGLE_2_GROUP);
									broadcastIntent
											.putExtra("0", paraGrp.grpID);
									Receiver.mContext
											.sendBroadcast(broadcastIntent);
								}

							}
							// Accept
							else if ((curGrp.level > callingInGrp.level && grpCallSetupHigh == GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT)
									|| (curGrp.level == callingInGrp.level && grpCallSetupSame == GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT)
									|| (curGrp.level < callingInGrp.level && grpCallSetupLow == GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT)) {
								SharedPreferences preferences = Receiver.mContext.getSharedPreferences(
										"com.zed3.sipua_preferences", Activity.MODE_PRIVATE);
								boolean isRestore = preferences.getBoolean(Settings.RESTORE_AFTER_OTHER_GROUP, true);
								if(isRestore){
									automaticAnswer  = true;
								}
								answerGroupCall(paraGrp);
							}
							// Reject
							else {
								// Modify by zzhan 2014-3-30
								// grouphangup((ExtendedCall) para.para2);
								grouphangup(paraGrp);
								return;
							}
						}
					}
				}
			}
		}
		// INVITE for create group call, response is listen
		else if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_LISTEN_LINE) {
			// Current line is not equal to param line, hangup this call
			PttGrp curGrp = GetCurGrp();
			if (curGrp.oVoid != para.para2) {
				pttGroupRelease(false, (ExtendedCall) para.para2);
			}
			curGrp.isCreateSession = true;

			MyLog.e(tag, "TYPE_REQUEST_LISTEN_LINE 0.");
			if (PTT_LISTENING != GetPttStatus()) {
				MyLog.e(tag, "TYPE_REQUEST_LISTEN_LINE 1.");
				ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
				startMediaApplication(ec, -1);// mode : duplex= 0, recv-only=
												// -1, send-only= +1;
				MyLog.e(tag,
						"SetPttStatus  PTT_LISTENING TYPE_REQUEST_LISTEN_LINE.");
				SetPttStatus(PTT_LISTENING);
			}
		}
		// INVITE for create or rejoin group call, response is reject
		else if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_REJECT_LINE) {
			// Current line is not equal to param line, hangup this call
			PttGrp curGrp = GetCurGrp();
			if (curGrp.oVoid != para.para2) {
				pttGroupRelease(false, (ExtendedCall) para.para2);
			}
			curGrp.isCreateSession = false;
			MyLog.e(tag, "SetPttStatus  PTT_IDLE TYPE_REQUEST_REJECT_LINE.");
			SetPttStatus(PTT_IDLE);

			// Current grp ui nofify???
			curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
			Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
			broadcastIntent.putExtra("0", curGrp.grpID);
			broadcastIntent.putExtra("1", "");
			curGrp.speaker = speaker;
			Receiver.mContext.sendBroadcast(broadcastIntent);

			// add by oumogang 2013-10-28
			MediaButtonReceiver.releasePTT();

			// All groups ui notify???
		}
		// BYE to hang up Group call or Single call
		else if (para.type == ExtendedSipCallbackType.TYPE_LOCAL_HANGUP_LINE
				|| para.type == ExtendedSipCallbackType.TYPE_PEER_HANGUP_LINE) {
			ExtendedCall ec = (ExtendedCall) para.para2;

			// Single mode
			if (!ec.isGroupCall && !IsPttMode()
					&& Receiver.call_state == UserAgent.UA_STATE_IDLE) {
				SetPttMode(true);
				if (PTT_LISTENING != GetPttStatus()
						&& PTT_TALKING != GetPttStatus()) {
					MyLog.e(tag,
							"SetPttStatus  PTT_IDLE TYPE_LOCAL_HANGUP_LINE.");
					SetPttStatus(PTT_IDLE);
					pttGroupJoin();
				}
			} else if(!IsPttMode()) {
				return ;
			}
			// Group mode
			else if (ec.isGroupCall) {
				PttGrp curGrp = GetCurGrp();
				if (curGrp.oVoid != para.para2)
					return;

				MyLog.e(tag, "closeMediaApplication TYPE_LOCAL_HANGUP_LINE");
				closeMediaApplication();
				// When talking mode, play release tip sound
				if (PTT_TALKING == GetPttStatus())
					pttReleaseTipSound();

				curGrp.isCreateSession = false;
				curGrp.oVoid = null;
				MyLog.e(tag, "SetPttStatus  PTT_IDLE TYPE_LOCAL_HANGUP_LINE.");
				SetPttStatus(PTT_IDLE);

				// Current grp ui nofify???
				curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
				Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
				broadcastIntent.putExtra("0", curGrp.grpID);
				broadcastIntent.putExtra("1", "");
				curGrp.speaker = "";
				Receiver.mContext.sendBroadcast(broadcastIntent);

				// add by oumogang 2013-10-28
				MediaButtonReceiver.releasePTT();

				// All groups ui notify???

				// restore previous group if current group is emergency group
				restorePreGrp();
			}
		} else if (para.type == ExtendedSipCallbackType.TYPE_TALKING) {
			if (PTT_LISTENING == GetPttStatus()) {
				startMediaApplication((ExtendedCall) (GetCurGrp().oVoid), -1);
			}
		} else if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_GETSTATUS_PHONE) {
			// Not complete temporarily
		} else if (para.type == ExtendedSipCallbackType.TYPE_REGISTER_SUCCESS) {
			/*
			 * pttGroupParse(para.para1); if (PTT_UNREG == GetPttStatus())
			 * SetPttStatus(PTT_IDLE);
			 */
			if (!isInitGroupData) {
				GroupListUtil.getData4GroupList();
				isInitGroupData = true;
			}
			// GPS process
			if (user_profile.gps) {
				if (!isStartedGPS) {
					isStartedGPS = true;
					try {
						
						Tools.onRegisterSuccess();
						
						if (DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS) {
							// 流量统计1
							// 获取服务器上的流量统计
							Get3GTotalFromServer();
							// 获取套餐类型及总流量
							Get3GNetWorkType();
						}
						// // 读取gvs转码分辨率设置---del by hdf
						// MemoryMg.getInstance().GvsTransSize =
						// PreferenceManager
						// .getDefaultSharedPreferences(Receiver.mContext)
						// .getString("gvstransvideosizekey", "5");

						// log的开关
						if (PreferenceManager.getDefaultSharedPreferences(
								Receiver.mContext).getBoolean("logOnOffKey",
								false)) {

							CrashHandler.getInstance().init(Receiver.mContext,
									true);
						}

						// //gps开关---del by hdf
						// MemoryMg.getInstance().GpsLocationModel =
						// PreferenceManager
						// .getDefaultSharedPreferences(Receiver.mContext)
						// .getInt(Settings.PREF_LOCATEMODE,
						// Settings.DEFAULT_PREF_LOCATEMODE);

						// gps开关 3为从不定位
						if (MemoryMg.getInstance().GpsLocationModel < 3
								&& MemoryMg.getInstance().GpsLocationModel > -1)
							GPSOpenLock();

						// 从数据库读取的已用总流量
						MemoryMg.getInstance().User_3GDBLocalTotal = Double
								.parseDouble(PreferenceManager
										.getDefaultSharedPreferences(
												Receiver.mContext).getString(
												"User_3GDBLocalTotal", "0"));
						MemoryMg.getInstance().User_3GDBLocalTotalPTT = Double
								.parseDouble(PreferenceManager
										.getDefaultSharedPreferences(
												Receiver.mContext).getString(
												"User_3GDBLocalTotalPTT", "0"));
						MemoryMg.getInstance().User_3GDBLocalTotalVideo = Double
								.parseDouble(PreferenceManager
										.getDefaultSharedPreferences(
												Receiver.mContext)
										.getString("User_3GDBLocalTotalVideo",
												"0"));
						// 从数据库读取的上传时间点
						MemoryMg.getInstance().User_3GDBLocalTime = PreferenceManager
								.getDefaultSharedPreferences(Receiver.mContext)
								.getString("User_3GDBLocalTime",
										GetCurrentMouth(false));// 默认为系统时间
						// 预警值
						MemoryMg.getInstance().User_3GFlowOut = Double
								.parseDouble(PreferenceManager
										.getDefaultSharedPreferences(
												Receiver.mContext).getString(
												"3gflowoutval", "0"));
						// 流量条提醒
						MemoryMg.getInstance().isProgressBarTip = PreferenceManager
								.getDefaultSharedPreferences(Receiver.mContext)
								.getBoolean("flowtooltip", true);
						MemoryMg.getInstance().TerminalNum = Settings.getUserName();
						// 切换对讲组提示广播
						mGrpFilter = new IntentFilter();
						mGrpFilter.addAction(ACTION_GROUP_2_GROUP);
						SipUAApp.mContext.registerReceiver(SetGrpRecv,
								mGrpFilter);

					} catch (Exception e) {
						MyLog.e(tag, "" + e.toString());
						e.printStackTrace();
					}
				}

			}
		} else if (para.type == ExtendedSipCallbackType.TYPE_REQUEST_403) {
			String responseMessageCallId = para.para1;
			
			String currentCallId = getCurrentGroupCallId();
			
			if(!TextUtils.isEmpty(responseMessageCallId) && responseMessageCallId.equals(currentCallId)) {
				PttGrp curGrp = GetCurGrp();
				
				pttGroupRelease(false, null);
				
				//Add by zzhan 2013-5-6
				if (curGrp != null) {
					if (curGrp.isCreateSession && curGrp.oVoid!=null)
						grouphangup(curGrp);
				}
			}
			// If receive 403 for INFO request, try to send invite.
			pttGroupCall();
		}// 流量统计3
		else if (para.type == ExtendedSipCallbackType.TYPE_FLOWVIEWSCANNER_START)// 获取流量统计后启动流量统计线程
		{
			// ---
			if (!TextUtils.isEmpty(MemoryMg.getInstance().User_3GDBLocalTime)) {// 如果时间点非当月[以服务器时间为准]，本地流量统计清零，并写回本地存储
				if (!GetCurrentMouth(true).equals(
						MemoryMg.getInstance().User_3GDBLocalTime.substring(0,
								7))) {

					MemoryMg.getInstance().User_3GDBLocalTotal = 0;
					MemoryMg.getInstance().User_3GDBLocalTotalPTT = 0;
					MemoryMg.getInstance().User_3GDBLocalTotalVideo = 0;
					MemoryMg.getInstance().User_3GDBLocalTime = GetCurrentMouth(false);
					// 本地数据库清空
					NetFlowPreferenceEdit("0", "0", "0", GetCurrentMouth(false));
				}
			}
			String test = MemoryMg.getInstance().User_3GLocalTime + "|"
					+ MemoryMg.getInstance().User_3GLocalTotal + "|"
					+ MemoryMg.getInstance().User_3GLocalTotalPTT + "|"
					+ MemoryMg.getInstance().User_3GLocalTotalVideo;

			if (!TextUtils.isEmpty(MemoryMg.getInstance().User_3GLocalTime)) {
				// 如果服务器提供的时间点是当月
				if (GetCurrentMouth(true)
						.equals(MemoryMg.getInstance().User_3GLocalTime
								.substring(0, 7))) {

					if (MemoryMg.getInstance().User_3GLocalTotal > MemoryMg
							.getInstance().User_3GDBLocalTotal) {

						MemoryMg.getInstance().User_3GRelTotal = MemoryMg
								.getInstance().User_3GLocalTotal;
						MemoryMg.getInstance().User_3GRelTotalPTT = MemoryMg
								.getInstance().User_3GLocalTotalPTT;
						MemoryMg.getInstance().User_3GRelTotalVideo = MemoryMg
								.getInstance().User_3GLocalTotalVideo;

						// 服务器同步到本地
						NetFlowPreferenceEdit(
								MemoryMg.getInstance().User_3GLocalTotal + "",
								MemoryMg.getInstance().User_3GLocalTotalPTT
										+ "",
								MemoryMg.getInstance().User_3GLocalTotalVideo
										+ "", GetCurrentMouth(false));

					} else {
						// 如果服务器数值<=本地数值，则本地为准
						MemoryMg.getInstance().User_3GRelTotal = MemoryMg
								.getInstance().User_3GDBLocalTotal;
						MemoryMg.getInstance().User_3GRelTotalPTT = MemoryMg
								.getInstance().User_3GDBLocalTotalPTT;
						MemoryMg.getInstance().User_3GRelTotalVideo = MemoryMg
								.getInstance().User_3GDBLocalTotalVideo;

						// 同时将本地数值同步到服务器
						Upload3GTotal(
								MemoryMg.getInstance().User_3GDBLocalTotal + "",
								MemoryMg.getInstance().User_3GDBLocalTotalPTT
										+ "",
								MemoryMg.getInstance().User_3GDBLocalTotalVideo
										+ "");
					}

				} else// 如果服务器提供的时间点非当月，上面获取的服务器的变量应该都清零
				{
					MemoryMg.getInstance().User_3GRelTotal = 0;
					MemoryMg.getInstance().User_3GRelTotalPTT = 0;
					MemoryMg.getInstance().User_3GRelTotalVideo = 0;
					// 本地同步到服务器
					Upload3GTotal("0", "0", "0");

					MemoryMg.getInstance().User_3GLocalTotal = 0;
					MemoryMg.getInstance().User_3GLocalTotalPTT = 0;
					MemoryMg.getInstance().User_3GLocalTotalVideo = 0;
					MemoryMg.getInstance().User_3GLocalTime = GetCurrentMouth(false);
				}
			}

			String test2 = MemoryMg.getInstance().User_3GRelTotal + "|"
					+ MemoryMg.getInstance().User_3GRelTotalPTT + "|"
					+ MemoryMg.getInstance().User_3GRelTotalVideo;

			String test3 = MemoryMg.getInstance().User_3GDBLocalTotal + "|"
					+ MemoryMg.getInstance().User_3GDBLocalTotalPTT + "|"
					+ MemoryMg.getInstance().User_3GDBLocalTotalVideo;

			MyLog.e(tag, "test1test2test3" + test + " " + test2 + " " + test3);

			// 网络获取的流量统计及服务失败，全局变量为-1，则不执行
			if (MemoryMg.getInstance().User_3GTotal > 0) {
				if (MemoryMg.getInstance().User_3GTotal != -1) {
					// 最后启动流量统计服务。。。
					flowThread = new TotalFlowThread(Receiver.mContext);
					flowThread.start();
				}
			}

		}// 客服密码状态
		else if (para.type == ExtendedSipCallbackType.TYPE_UNIONPASSWORDLOGIN_STATE) {
			// 注销客服密码，提示用户客服密码输入错误，停掉sipdroidEngine，跳转到客服密码登录页面
			if (para.para1.equals("fail"))
				SipUAApp.mContext.sendBroadcast(new Intent(
						"android.intent.action.RestartUnionLogin"));

		}

	}

	// 开启gps+百度
	public void GPSOpenLock() {
		Zed3Log.debug("testgps", "UserAgent#GPSOpenLock is openGps = " + (isOpenGps()));
		if(!isOpenGps()){
			openGps();
		}
	}
	private synchronized void openGps() {
		Zed3Log.debug("testgps", "UserAgent#openGps gpsPacket = " + gpsPacket + " , user_profile.realm_orig = " + user_profile.realm_orig);
		if (gpsPacket == null) {
			gpsPacket = new GPSPacket(Receiver.mContext, user_profile.username,
					user_profile.passwd, user_profile.realm_orig);
			GpsTools.setServer(SipUAApp.mContext.getSharedPreferences(
					Settings.sharedPrefsFile, Context.MODE_PRIVATE).getString(
					Settings.PREF_SERVER, Settings.DEFAULT_SERVER));
			Receiver.engine(Receiver.mContext).updateDNS();
			MyLog.e("GPSSend", "userAgent  new gpspacket  "
					+ user_profile.username + user_profile.realm_orig);
		} else {
			MyLog.e("GPSSend", "userAgent  gpspacket  != null "
					+ user_profile.username + user_profile.realm_orig);
		}
		gpsPacket.StartGPS();
	}
	
	public boolean isOpenGps(){
		return (gpsPacket!=null);
	}

	// 关闭gps+百度
	public synchronized void GPSCloseLock() {
		Zed3Log.debug("testgps", "UserAgent#GPSCloseLock enter");
		if (gpsPacket != null) {
			MyLog.e("GPSSend", "userAgent GPSCloseLock ");
			Zed3Log.debug("testgps", "UserAgent#GPSCloseLock enter gpsPacket:"+gpsPacket);
			gpsPacket.ExitGPS(true);
			gpsPacket = null;
		}
	}
	public synchronized void GPSCloseLock(boolean isCloseGps) {
		if (gpsPacket != null) {
			gpsPacket.ExitGPS(isCloseGps);
			gpsPacket = null;
		}
	}
	// 切换组广播
	private BroadcastReceiver SetGrpRecv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_GROUP_2_GROUP)) {

				Bundle bundle = intent.getExtras();
				GroupCallUtil.setTalkGrp(bundle.getString("0"));
				GroupCallUtil.setActionMode(ACTION_GROUP_2_GROUP);
				Intent startActivity = new Intent();
				startActivity.setClass(Receiver.mContext, ActvityNotify.class);
				startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Receiver.mContext.startActivity(startActivity);
			}
		}
	};

	public String GetCurrentMouth(boolean flag) {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (flag)
			return formatter.format(currentTime).substring(0, 7);
		else
			return formatter.format(currentTime);
	}

	public void NetFlowPreferenceEdit(String total, String ptt, String video,
			String time) {
		// 本地数据库清空
		SharedPreferences mypre = Receiver.mContext.getSharedPreferences(
				"com.zed3.sipua_preferences", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mypre.edit();
		editor.putString("User_3GDBLocalTotal", total);
		editor.putString("User_3GDBLocalTotalPTT", ptt);
		editor.putString("User_3GDBLocalTotalVideo", video);
		editor.putString("User_3GDBLocalTime", time);
		editor.commit();
	}

	// Runnable--command process
	public void run() {
		// Initialize message queue before create handler
		Looper.prepare();

		// Define handler
		cmdHandler = new Handler() {
			// @Override
			public void handleMessage(android.os.Message msg) {
				if (PTT_UNREG == GetPttStatus())
					return;

				try {
					// PTT key down or up
					if (ProcessCmdType.values()[msg.arg1] == ProcessCmdType.PROCESS_TYPE_PTT_KEY_CMD) {
						OnPttKey2((msg.arg2 == 1) ? true : false);
					}
					// Extended Sip
					else if (ProcessCmdType.values()[msg.arg1] == ProcessCmdType.PROCESS_TYPE_SIP_CMD) {
						if (msg.obj != null)
							extendedSipProcess((ExtendedSipCallbackPara) msg.obj);

					} else if (ProcessCmdType.values()[msg.arg1] == ProcessCmdType.PROCESS_TYPE_RTP_SENDER_EXCEPTION_CMD) {
						processRtpSenderException();
					}else if(ProcessCmdType.values()[msg.arg1] == ProcessCmdType.PROCESS_TYPE_HEATBEAT_MESSAGE_CMD) {
						processHeadBeatMessage(msg);
					} else if (ProcessCmdType.values()[msg.arg1] == ProcessCmdType.PROCESS_TYPE_CALL_CMD) {
						processCallMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		// 终端上报状态 根据report-heartbeat：终端状态上报周期，单位秒，取值范围-1到3600
		// 值为-1时，完全不上报
		beatHandler = new Handler() {
			// @Override
			public void handleMessage(android.os.Message msg) {
				if (msg.what == 1) {
					/*
					 * Delete by zzhan 2013-5-2 int deytime = PttHeartBeat();
					 * android.util.Log.e("tagtag", "looper" + deytime);
					 * 
					 * if (deytime > 0) beatHandler.sendMessageDelayed(
					 * beatHandler.obtainMessage(1), deytime * 1000);
					 */
					pttHeartBeatEx();
				}
				if (msg.what == 2) {
					if(DeviceInfo.CONFIG_SUPPORT_UNICOM_PASSWORD) {
						MyToast.showToast(true, Receiver.mContext,
								SipUAApp.getInstance().getString(R.string.not_support_video_function));
					}
				}
				// if(msg.what == 3){//heartbeat add by hu 2014/2/12
				// sendHeartBeat(); //50s后重新发送
				// }
			}
		};

		// Start message loop
		Looper.loop();
	}

	protected void processCallMessage(android.os.Message msg) {
		Object callStatusObj = msg.obj;
		if (callStatusObj != null) {
			CallStatus callStatus = (CallStatus) callStatusObj;
			dispatchCallStatus(callStatus);
		}
	}

	private void handleCallStatus(CallStatus callStatus) {
		Zed3Log.debugE("pttTrace", "UserAgent#handleCallStatus() call status = "+ callStatus);
		Handler handler = cmdHandler;

		if (handler != null) {
			android.os.Message msg = handler.obtainMessage();
			msg.arg1 = ProcessCmdType.PROCESS_TYPE_CALL_CMD.ordinal();
			msg.arg2 = 4;
			msg.obj = callStatus;
			handler.sendMessage(msg);
		}
	}
	
	private void dispatchCallStatus(CallStatus callStatus) {
		Zed3Log.debug("pttTrace","UserAgent#dispatchCallStatus() enter call status = "+ callStatus);

		if (callStatus == null) {
			return;
		}

		switch (callStatus) {

		case CALL_ACCEPT:
			acceptInner();
			break;
		case CALL_CANCELING:
			onCallCancelingInner(callStatus.getCall(), callStatus.getMessage());
			break;
		case ACCEPT_GROUP_CALL:
			Object arg = callStatus.getWorkArgs().obj;
			if(arg!=null){
				PttGrp pttGrp = (PttGrp) arg;
				answerGroupCallInner(pttGrp);
			}
			break;
		case CALL_HANGUP_WITHOUT_REJOIN:
			hangupWithoutRejoinInner();
			break;
		}
	}

	// Add by zzhan 2013-5-2
	private void pttHeartBeatEx() {
		int deytime = PttHeartBeat();
		if (beatHandler.hasMessages(1)) {
			beatHandler.removeMessages(1);
		}
		if (deytime > 0) {
			beatHandler.sendMessageDelayed(beatHandler.obtainMessage(1),
					deytime * 1000);
		}
	}

	// add by hdf
	// 注册成功后 根据返回时间 定时发心跳
	public int PttHeartBeat() {

		PttGrp curGrp = GetCurGrp();
		if (curGrp == null)
			return -1;

		// If emergency group, Must not INFO request for talk
		if (curGrp.level == 0)
			return -1;
		// 如果为-1则不在往下执行 直接返回
		if (curGrp.report_heartbeat == -1) {
			return -1;
		}

		String to = curGrp.grpID;
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}
		Message req;
		ExtendedCall c = (ExtendedCall) curGrp.oVoid;
		if (curGrp.isCreateSession && c != null) {
			req = MessageFactory.createRequest(c.getDialog(), SipMethods.INFO,
					null);
		} else {
			req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
					new NameAddress(to),
					new NameAddress(user_profile.from_url), null);
		}
		req.setHeader(new Header(SipHeaders.Ptt_Extension,
				"3ghandset heartbeat"));
		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

		return curGrp.report_heartbeat;

	}

	// 发送 获得成员列表 消息信息 add by hdf
	public void PttGetGroupList(String groudID) {
		// PttGrp curGrp = GetCurGrp();
		// if (curGrp == null)
		// return;
		// //If emergency group, Must not INFO request for talk
		// if (curGrp.level == 0)
		// return;
		String to = groudID;
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}

		Message req;
		// if (curGrp.isCreateSession){
		// ExtendedCall c = (ExtendedCall)curGrp.oVoid;
		// req = MessageFactory.createRequest(c.getDialog(), SipMethods.INFO,
		// null);
		// }
		// else {
		req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
				new NameAddress(to), new NameAddress(user_profile.from_url),
				null);
		// }

		req.setHeader(new Header(SipHeaders.Ptt_Extension,
				"3ghandset getstatus"));

		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

	}

	// 获取套餐类型
	public void Get3GNetWorkType() {
		String to = user_profile.username;
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}

		Message req;
		req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
				new NameAddress(to), new NameAddress(user_profile.from_url),
				null);
		req.setHeader(new Header(SipHeaders.Ptt_Extension,
				"3ghandset getdatatotal"));
		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

	}

	// 获取服务器上的流量统计
	public void Get3GTotalFromServer() {
		String to = user_profile.username;
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}
		Message req;
		req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
				new NameAddress(to), new NameAddress(user_profile.from_url),
				null);
		req.setHeader(new Header(SipHeaders.Ptt_Extension,
				"3ghandset getdatastatistics"));
		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

	}

	// 上报流量到服务器
	public void Upload3GTotal(String data, String dataptt, String datavideo) {
		String to = user_profile.username;
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}
		Message req;
		req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
				new NameAddress(to), new NameAddress(user_profile.from_url),
				null);
		req.setHeader(new Header(SipHeaders.Ptt_Extension,
				"3ghandset reportdatastatistics"));
		// datastatistics：XXXXXX
		// ptt:xxx
		// video:xxx
		// time:YYYYYY 应该带：Content-Type: text/3ghandset
		String str = "datastatistics:" + data + "\r\n" + "pttstatistics:"
				+ dataptt + "\r\n" + "videostatistics:" + datavideo + "\r\n"
				+ "time:" + GetCurrentMouth(false) + "\r\n";
		req.setBody(str);

		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();
	}

	// 上传联通客服密码
	public void UploadUnionPwd(String str) {

		String to = user_profile.username;
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}
		Message req;
		req = MessageFactory.createRequest(sip_provider, SipMethods.INFO,
				new NameAddress(to), new NameAddress(user_profile.from_url),
				null);
		req.setHeader(new Header(SipHeaders.Ptt_Extension, "3ghandset auth "
				+ str));

		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();

	}

	// heartbeat add by hu 2014/2/12
	public void sendHeartBeat() {
		if (sip_provider != null && Receiver.mSipdroidEngine.isRegistered()) {
			// new thread add by oumogang 2014-03-11
			new Thread(new Runnable() {

				@Override
				public void run() {
					
					PttGrp currentGroup = GetCurGrp();
					if(currentGroup!=null && currentGroup.oVoid!=null && !currentGroup.isCreateSession){
						Zed3Log.debug(tag, "UserAgent#sendHeartBeat() break send empty pkg");
						sip_provider.sendMessage("0");
						return ;
					}
					
					String callID = ""; 
					String grpNum = "";
					if (GetCurGrp() != null) {
						grpNum = GetCurGrp().grpID;
						// NullPointerException modify by liangzhang 2014-09-10
						ExtendedCall call = (ExtendedCall) GetCurGrp().oVoid;
						if (call != null) {
							callID = call.getDialog().getCallID();
							if (!TextUtils.isEmpty(callID)) {
								// 2014-10-24 add by zdx
								if (callID.length() > 24) {
									callID = callID.substring(callID.length() - 24,callID.length());
								}
							}
						}
						android.util.Log.i("xxxx", "UserAgent#sendHeartBeat send message");
						sip_provider.sendMessage(new HeartBeatPacket(Settings
								.getUserName(), Settings.getPassword(), grpNum,
								getCurGrpState(), callID).toString());
					} else {// 无对讲组时，状态为OFF modify by liangzhang 2014-09-24
						sip_provider.sendMessage(new HeartBeatPacket(Settings
								.getUserName(), Settings.getPassword(), grpNum,
								"OFF", callID).toString());
					}
				}
			}).start();
			// beatHandler.sendMessageDelayed(
			// beatHandler.obtainMessage(3), 50 * 1000);
			Receiver.alarm(SipStack.heartBeatCircle, MyHeartBeatReceiver.class);// for
																				// test
																				// should
																				// be
																				// 50;
		}
	}

	String getCurGrpState() {
		PttGrp curGrp = GetCurGrp();
		if (curGrp != null) {
			if (curGrp.oVoid == null || audio_app == null) {
				return "OFF";
			} else {
				return "ON";
			}
//			if (curGrp.state == E_Grp_State.GRP_STATE_SHOUDOWN) {
//				return "OFF";
//			} else if (curGrp.state == E_Grp_State.GRP_STATE_IDLE
//					|| curGrp.state == E_Grp_State.GRP_STATE_LISTENING
//					|| curGrp.state == E_Grp_State.GRP_STATE_QUEUE
//					|| curGrp.state == E_Grp_State.GRP_STATE_TALKING) {
//				return "ON";
//			}
		}
		return "";
	}

	public String sendMultiMessage(String to, String content,
			String message_id, String report_attr, String type, int size) {
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		// houyuchun add 20120526 begin
		// 判断当前彩信大小是否达到2M上限
		// if(size > 2097152) {
		// Toast.makeText(Receiver.mContext,
		// Receiver.mContext.getResources().getString(R.string.message_toast_over_size),
		// Toast.LENGTH_LONG).show();
		// return null;
		// }
		// houyuchun add 20120526 end
		if (to.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				to = "&" + to;
			to = to + "@" + realm;
		}
		Message req = MessageFactory.createRequest(sip_provider,
				SipMethods.INFO, new NameAddress(to), new NameAddress(
						user_profile.from_url), content);
		req.setHeader(new Header(SipHeaders.Ptt_Extension,
				"3ghandset OfflineDataSend"));
		req.setHeader(new Header(SipHeaders.OFFLINE_DATA_ID, message_id));
		req.setHeader(new Header(SipHeaders.OFFLINE_DATA_ATTRIBUTE, report_attr));
		req.setHeader(new Header(SipHeaders.OFFLINE_DATA_TYPE, type));
		req.setHeader(new Header(SipHeaders.OFFLINE_DATA_SIZE, size + ""));

		String seq = String.valueOf(req.getCSeqHeader().getSequenceNumber());
		TransactionClient info_transaction = new TransactionClient(
				sip_provider, req, this);
		info_transaction.request();
		return seq;
	}

	@Override
	public void onReceiveHeatBeatMsg(String msg) {
		MyLog.e(tag, "receive:--" + msg);
		HeartBeatGrpState state = HeartBeatParser.parser(msg);
		if (state == null)
			return;
		
		if(cmdHandler!=null){
			
			Zed3Log.debug("pttTrace","UserAgent#onReceiveHeatBeatMsg() cmd handler send message");
			
			android.os.Message cmdMessage = cmdHandler.obtainMessage();
			cmdMessage.arg1 = ProcessCmdType.PROCESS_TYPE_HEATBEAT_MESSAGE_CMD.ordinal();
			cmdMessage.arg2 = 3;
			cmdMessage.obj = state;
			cmdHandler.sendMessage(cmdMessage);
		}
		
	}
	
	private void processHeadBeatMessage(android.os.Message message){
		
		Zed3Log.debug("pttTrace", "UserAgent#processHeadBeatMessage() enter");
		
		PttGrp curGrp = GetCurGrp();
		if(curGrp == null) {
			Zed3Log.debug("pttTrace", "UserAgent#processHeadBeatMessage() current group is null");
			return;
		}
		
		Object object = message.obj;
		
		if(object==null) {
			Zed3Log.debug("pttTrace", "UserAgent#processHeadBeatMessage() message.obj is null");
			return ;
		}
		
		HeartBeatGrpState state = (HeartBeatGrpState) object;
		
		if(state.getGrpName().equals(curGrp.grpID)){
			if(!getCurGrpState().equalsIgnoreCase(state.getGrpState())){
				if(getCurGrpState().equalsIgnoreCase("ON") && state.getGrpState().equalsIgnoreCase("OFF")){
					MyLog.e(tag, "do ...pttGroupRelease");
					//TODO grouprelese
					// Added by zzhan 2011-11-05
					if (curGrp.oVoid!=null) {
						ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
						if (ec != null) {
							
							Zed3Log.debug("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
							
							pttGroupRelease(false, ec);
							
							//Add UI notify of Group shutdown(because server will not send origianl group status to me)
							curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
							Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
							broadcastIntent.putExtra("0", curGrp.grpID);
							broadcastIntent.putExtra("1", "");
							speaker = "";
							curGrp.speaker = speaker;
							Receiver.mContext.sendBroadcast(broadcastIntent);
							
							//add by oumogang 2013-10-28
							MediaButtonReceiver.releasePTT();
						}

						curGrp.isCreateSession = false;
						curGrp.oVoid = null;
					}
				}else if(getCurGrpState().equalsIgnoreCase("ON") && state.getGrpState().equalsIgnoreCase("OUT")){
					//TODO grouprelese
					// Added by zzhan 2011-11-05
					MyLog.e(tag, "do ...pttGroupRelease  then rejoin");
					if (curGrp.oVoid!=null) {
						ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
						if (ec != null) {
							Zed3Log.debug("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
							pttGroupRelease(false, ec);
							
							//Add UI notify of Group shutdown(because server will not send origianl group status to me)
							curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
							Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
							broadcastIntent.putExtra("0", curGrp.grpID);
							broadcastIntent.putExtra("1", "");
							speaker = "";
							curGrp.speaker = speaker;
							Receiver.mContext.sendBroadcast(broadcastIntent);
							
							//add by oumogang 2013-10-28
							MediaButtonReceiver.releasePTT();
						}

						curGrp.isCreateSession = false;
						curGrp.oVoid = null;
						//rejoin
						pttGroupJoin();
					}
				}else if(getCurGrpState().equalsIgnoreCase("OFF") && (state.getGrpState().equalsIgnoreCase("ON") || state.getGrpState().equalsIgnoreCase("OUT"))){
					//rejoin
					
					if (curGrp.oVoid!=null && audio_app==null) {
						ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
						if (ec != null) {
							Zed3Log.debug("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
							pttGroupRelease(false, ec);
							
							//Add UI notify of Group shutdown(because server will not send origianl group status to me)
							curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
							Intent broadcastIntent = new Intent(ACTION_GROUP_STATUS);
							broadcastIntent.putExtra("0", curGrp.grpID);
							broadcastIntent.putExtra("1", "");
							speaker = "";
							curGrp.speaker = speaker;
							Receiver.mContext.sendBroadcast(broadcastIntent);
							
							//add by oumogang 2013-10-28
							MediaButtonReceiver.releasePTT();
						}

						curGrp.isCreateSession = false;
						curGrp.oVoid = null;
						//rejoin
						pttGroupJoin();
					} else {
						//rejoin
						pttGroupJoin();
					}
					
				}else if (getCurGrpState().equalsIgnoreCase("ON")
						&& state.getGrpState().equalsIgnoreCase("ERR")) {
					// TODO grouprelese
					// Added by zzhan 2011-11-05
					MyLog.e(tag, "do ...pttGroupRelease  then rejoin");
					if (curGrp.oVoid!=null) {
						ExtendedCall ec = (ExtendedCall) curGrp.oVoid;
						if (ec != null) {
							Zed3Log.debug("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
							pttGroupRelease(false, ec);

							// Add UI notify of Group shutdown(because server
							// will not send origianl group status to me)
							curGrp.state = E_Grp_State.GRP_STATE_SHOUDOWN;
							Intent broadcastIntent = new Intent(
									ACTION_GROUP_STATUS);
							broadcastIntent.putExtra("0", curGrp.grpID);
							broadcastIntent.putExtra("1", "");
							speaker = "";
							curGrp.speaker = speaker;
							Receiver.mContext.sendBroadcast(broadcastIntent);

							// add by oumogang 2013-10-28
							MediaButtonReceiver.releasePTT();
						}

						curGrp.isCreateSession = false;
						curGrp.oVoid = null;
						// rejoin
						pttGroupJoin();
					}
				} else if(getCurGrpState().equalsIgnoreCase("OFF")
						&& state.getGrpState().equalsIgnoreCase("ERR")){
					Zed3Log.debug("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
					pttGroupJoin();
				}
			}
		}
		
		Zed3Log.debug("pttTrace", "UserAgent#processHeadBeatMessage() exit");
	}
	
	private String getCurrentGroupCallId(){
		String callID = "";
		PttGrp curGrp = GetCurGrp();
		if(curGrp!=null){
			Object oVoid = curGrp.oVoid;
			if(oVoid!=null){
				ExtendedCall call = (ExtendedCall)oVoid;
				if(call != null){
					callID = call.getDialog() != null ? call.getDialog().getCallID() : "";
				}
			}
		}
		return callID;
	}
	
	public void onRtpStreamSenderException(){
		//PROCESS_TYPE_RTP_SENDER_EXCEPTION
		Zed3Log.debugE("testptt","UserAgent#onRtpStreamSenderException enter");
		android.os.Message msg = cmdHandler.obtainMessage();
		msg.arg1 = ProcessCmdType.PROCESS_TYPE_RTP_SENDER_EXCEPTION_CMD.ordinal();
		msg.arg2 = 2;
		cmdHandler.sendMessage(msg);
	}
	
	protected void processRtpSenderException() {
		Zed3Log.debugE("testptt","UserAgent#processRtpSenderException enter");
		
		closeMediaApplication();
		
		PttGrp currentPttGroup = GetCurGrp();
		Zed3Log.debug("testptt","UserAgent#processRtpSenderException currentPttGroup = " + currentPttGroup);
		if(currentPttGroup!=null){
			
			Object oVoid = currentPttGroup.oVoid;
			
			Zed3Log.debug("testptt","UserAgent#processRtpSenderException oVoid = " + oVoid);
			
			if(oVoid!=null){
				
				ExtendedCall ec = (ExtendedCall) oVoid;
				
				Zed3Log.debug("testptt","UserAgent#processRtpSenderException oVoid = " + oVoid);
				
				startMediaApplication(ec, -1);
			}
			
		}
		
	}
	
}
