/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2008 Hughes Systique Corporation, USA (http://www.hsc.com)
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
import java.net.UnknownHostException;

import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.UdpTransport;
import org.zoolu.tools.MyLog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zed3.net.KeepAliveSip;
import com.zed3.sipua.UserAgent.GrpCallSetupType;
import com.zed3.sipua.ui.LoopAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.Sipdroid;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Zed3Log;

public class SipdroidEngine implements RegisterAgentListener {

	// Modify by zzhan 2011-9-7
	// public static final int LINES = 2;
	public static final int LINES = 1;

	public int pref;

	public static final int UNINITIALIZED = 0x0;
	public static final int INITIALIZED = 0x2;

	private static final String tag = "SipdroidEngine";

	/** User Agent */
	public UserAgent[] uas;
	public UserAgent ua;

	/** Register Agent */
	public RegisterAgent[] ras;

	private KeepAliveSip[] kas;

	/** UserAgentProfile */
	public UserAgentProfile[] user_profiles;

	public SipProvider[] sip_providers;

	public static PowerManager.WakeLock[] wl, pwl;
	public int isMakeVideoCall = -1;

	UserAgentProfile getUserAgentProfile(String suffix) {
		UserAgentProfile user_profile = new UserAgentProfile(null);

		user_profile.username = PreferenceManager.getDefaultSharedPreferences(
				getUIContext()).getString(Settings.PREF_USERNAME + suffix,
				Settings.DEFAULT_USERNAME); // modified
		user_profile.passwd = PreferenceManager.getDefaultSharedPreferences(
				getUIContext()).getString(Settings.PREF_PASSWORD + suffix,
				Settings.DEFAULT_PASSWORD);
		if (PreferenceManager
				.getDefaultSharedPreferences(getUIContext())
				.getString(Settings.PREF_DOMAIN + suffix,
						Settings.DEFAULT_DOMAIN).length() == 0) {
			user_profile.realm = PreferenceManager.getDefaultSharedPreferences(
					getUIContext()).getString(Settings.PREF_SERVER + suffix,
					Settings.DEFAULT_SERVER);
		} else {
			user_profile.realm = PreferenceManager.getDefaultSharedPreferences(
					getUIContext()).getString(Settings.PREF_DOMAIN + suffix,
					Settings.DEFAULT_DOMAIN);
		}
		user_profile.realm_orig = user_profile.realm;
		if (PreferenceManager
				.getDefaultSharedPreferences(getUIContext())
				.getString(Settings.PREF_FROMUSER + suffix,
						Settings.DEFAULT_FROMUSER).length() == 0) {
			user_profile.from_url = user_profile.username;
		} else {
			user_profile.from_url = PreferenceManager
					.getDefaultSharedPreferences(getUIContext()).getString(
							Settings.PREF_FROMUSER + suffix,
							Settings.DEFAULT_FROMUSER);
		}

		// MMTel configuration (added by mandrajg)
		user_profile.qvalue = PreferenceManager.getDefaultSharedPreferences(
				getUIContext()).getString(Settings.PREF_MMTEL_QVALUE,
				Settings.DEFAULT_MMTEL_QVALUE);
		user_profile.mmtel = PreferenceManager.getDefaultSharedPreferences(
				getUIContext()).getBoolean(Settings.PREF_MMTEL,
				Settings.DEFAULT_MMTEL);

		user_profile.pub = PreferenceManager.getDefaultSharedPreferences(
				getUIContext()).getBoolean(Settings.PREF_EDGE + suffix,
				Settings.DEFAULT_EDGE)
				|| PreferenceManager
						.getDefaultSharedPreferences(getUIContext())
						.getBoolean(Settings.PREF_3G + suffix,
								Settings.DEFAULT_3G);
		return user_profile;
	}

	public boolean StartEngine() {
		LogUtil.makeLog(tag, "StartEngine()");
		PowerManager pm = (PowerManager) getUIContext().getSystemService(
				Context.POWER_SERVICE);
		if (wl == null) {
			if (!PreferenceManager.getDefaultSharedPreferences(getUIContext())
					.contains(com.zed3.sipua.ui.Settings.PREF_KEEPON)) {
				Editor edit = PreferenceManager.getDefaultSharedPreferences(
						getUIContext()).edit();

				edit.putBoolean(
						com.zed3.sipua.ui.Settings.PREF_KEEPON,
						Build.MODEL.equals("Nexus One")
								|| Build.MODEL.equals("Nexus S")
								|| Build.MODEL.equals("Archos5")
								|| Build.MODEL.equals("ADR6300")
								|| Build.MODEL.equals("PC36100")
								|| Build.MODEL.equals("HTC Desire")
								|| Build.MODEL.equals("HTC Incredible S")
								|| Build.MODEL.equals("HTC Wildfire")
								|| Build.MODEL.equals("GT-I9100"));
				edit.commit();
			}
			wl = new PowerManager.WakeLock[LINES];
			pwl = new PowerManager.WakeLock[LINES];
		}

		// pref = ChangeAccount.getPref(Receiver.mContext);

		uas = new UserAgent[LINES];
		ras = new RegisterAgent[LINES];
		kas = new KeepAliveSip[LINES];
		lastmsgs = new String[LINES];
		sip_providers = new SipProvider[LINES];
		user_profiles = new UserAgentProfile[LINES];
		user_profiles[0] = getUserAgentProfile("");
		for (int i = 1; i < LINES; i++)
			user_profiles[1] = getUserAgentProfile("" + i);

		SipStack.init(null);
		int i = 0;
		for (UserAgentProfile user_profile : user_profiles) {
			if (wl[i] == null) {
				wl[i] = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
						"Sipdroid.SipdroidEngine");
				if (PreferenceManager.getDefaultSharedPreferences(
						getUIContext()).getBoolean(
						com.zed3.sipua.ui.Settings.PREF_KEEPON,
						com.zed3.sipua.ui.Settings.DEFAULT_KEEPON))
					pwl[i] = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP,
							"Sipdroid.SipdroidEngine");
			}

			try {
				SipStack.debug_level = 0;
				// SipStack.log_path = "/data/data/com.zed3.sipua";
				SipStack.max_retransmission_timeout = 4000;
				SipStack.default_transport_protocols = new String[1];
				SipStack.default_transport_protocols[0] = PreferenceManager
						.getDefaultSharedPreferences(getUIContext())
						.getString(
								Settings.PREF_PROTOCOL + (i != 0 ? i : ""),
								user_profile.realm
										.equals(Settings.DEFAULT_SERVER) ? "tcp"
										: "udp");

				// String version = "Sipdroid/" + Sipdroid.getVersion() + "/" +
				// Build.MODEL;
				String version = "Sipdroid/" + SipUAApp.getVersion() + "/"
						+ Build.MODEL;
				SipStack.ua_info = version;
				SipStack.server_info = version;

				IpAddress.setLocalIpAddress();
				sip_providers[i] = new SipProvider(IpAddress.localIpAddress, 0);
				UdpTransport.needEncrypt = PreferenceManager.getDefaultSharedPreferences(
						getUIContext()).getBoolean(
						com.zed3.sipua.ui.Settings.PREF_MSG_ENCRYPT,false);
				user_profile.contact_url = getContactURL(user_profile.username,
						sip_providers[i]);

				if (user_profile.from_url.indexOf("@") < 0) {
					user_profile.from_url += "@" + user_profile.realm;
				}

				CheckEngine();

				// added by mandrajg
				String icsi = null;
				if (user_profile.mmtel == true) {
					icsi = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
				}

				uas[i] = ua = new UserAgent(sip_providers[i], user_profile);

				// Set group call configuration
				SharedPreferences share = PreferenceManager
						.getDefaultSharedPreferences(getUIContext());
				uas[i].SetGrpCallConfig(
						getPriority(share.getInt(Settings.HIGH_PRI_KEY, 1)),
						getPriority(share.getInt(Settings.SAME_PRI_KEY, 0)),
						getPriority(share.getInt(Settings.LOW_PRI_KEY, 2)));

				ras[i] = new RegisterAgent(
						sip_providers[i],
						user_profile.from_url, // modified
						user_profile.contact_url, user_profile.username,
						user_profile.realm, user_profile.passwd, this,
						user_profile, user_profile.qvalue, icsi,
						user_profile.pub); // added by mandrajg
				kas[i] = new KeepAliveSip(sip_providers[i], 100000);
			} catch (Exception E) {
				Zed3Log.debug("testgps", "SipdroidEngine#StartEngine exception = " + E.getMessage());
			}
			i++;
		}
		listen();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		register(true);

		return true;
	}


	private GrpCallSetupType getPriority(int intPriority) {
		if (intPriority == 0)
			return GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
		else if (intPriority ==1)
			return GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT;
		else
			return GrpCallSetupType.GRPCALLSETUPTYPE_REJECT;
	}

	private String getContactURL(String username, SipProvider sip_provider) {
		int i = username.indexOf("@");
		if (i != -1) {
			// if the username already contains a @
			// strip it and everthing following it
			username = username.substring(0, i);
		}

		return username
				+ "@"
				+ IpAddress.localIpAddress
				+ (sip_provider.getPort() != 0 ? ":" + sip_provider.getPort()
						: "") + ";transport="
				+ sip_provider.getDefaultTransport();
	}

	/**
	 * 设置服务器地址和端口号
	 * 
	 * @param sip_provider
	 * @param i
	 */
	void setOutboundProxy(SipProvider sip_provider, int i) {
		try {
			if (sip_provider != null) {
//				IpAddress ipAddress = IpAddress.getByName(PreferenceManager
//						.getDefaultSharedPreferences(getUIContext()).getString(
//								Settings.PREF_DNS + i, Settings.DEFAULT_DNS));
				String sharedPrefsFile = "com.zed3.sipua_preferences";
				IpAddress ipAddress = IpAddress.getByName(getUIContext().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE).getString(Settings.PREF_SERVER, ""));
				int integer = Integer.valueOf(PreferenceManager
						.getDefaultSharedPreferences(getUIContext()).getString(
								Settings.PREF_PORT + (i != 0 ? i : ""),
								Settings.DEFAULT_PORT));
				sip_provider.setOutboundProxy(new SocketAddress(ipAddress,
						integer));

				// 重新注册

			}
		} catch (Exception e) {
		}
	}

	public void CheckEngine() {
		int i = 0;
		for (SipProvider sip_provider : sip_providers) {
			if (sip_provider != null && !sip_provider.hasOutboundProxy())
				setOutboundProxy(sip_provider, i);
			i++;
		}
	}

	public Context getUIContext() {
		return Receiver.mContext;
	}

	public int getRemoteVideo() {
		return ua.remote_video_port;
	}

	public int getLocalVideo() {
		return ua.local_video_port;
	}

	public String getRemoteAddr() {
		return ua.remote_media_address;
	}

	public void expire() {
		Receiver.expire_time = 0;
		int i = 0;
		for (RegisterAgent ra : ras) {
			if (ra != null && ra.CurrentState == RegisterAgent.REGISTERED) {
				ra.CurrentState = RegisterAgent.UNREGISTERED;
				Receiver.onText(Receiver.REGISTER_NOTIFICATION + i, null, 0, 0);
			}
			i++;
		}
		// by hdf 该方法会定时注册，会保持注册的连续性，
		// 如果去掉，程序启动后会注册，一段时间后注册状态 会消失，掉线
		register(false);
	}

	// 重载 add by hdf
	public void expire(int time) {
		//add by oumogang 2013-05-23
		//退出程序之前先把来电去电或者通话给结束掉；
		if (Receiver.call_state != UserAgent.UA_STATE_IDLE) {
			Receiver.engine(Receiver.mContext).rejectcall();
		}
		Receiver.expire_time = 0;
		int i = 0;
		for (RegisterAgent ra : ras) {
			if (ra != null && ra.CurrentState == RegisterAgent.REGISTERED) {
				// register(0)
				ra.register(time);

				ra.CurrentState = RegisterAgent.UNREGISTERED;
				Receiver.onText(Receiver.REGISTER_NOTIFICATION + i, null, 0, 0);
			}
			i++;
		}
	}

	public void unregister(int i) {
		// Deleted by zzhan 2011-9-19
		/*
		 * if (user_profiles[i] == null || user_profiles[i].username.equals("")
		 * || user_profiles[i].realm.equals("")) return;
		 * 
		 * RegisterAgent ra = ras[i]; if (ra != null && ra.unregister()) {
		 * Receiver.alarm(0, LoopAlarm.class);
		 * Receiver.onText(Receiver.REGISTER_NOTIFICATION
		 * +i,getUIContext().getString
		 * (R.string.reg),R.drawable.icon64,0); wl[i].acquire(); }
		 * else Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
		 */
	}

	public void registerMore() {
		IpAddress.setLocalIpAddress();
		int i = 0;
		for (RegisterAgent ra : ras) {
			try {
				if (user_profiles[i] == null
						|| user_profiles[i].username.equals("")
						|| user_profiles[i].realm.equals("")) {
					i++;
					continue;
				}
				user_profiles[i].contact_url = getContactURL(
						user_profiles[i].from_url, sip_providers[i]);

				if (ra != null && !ra.isRegistered() && Receiver.isFast(i)
						&& ra.register()) {
					Receiver.onText(Receiver.REGISTER_NOTIFICATION + i,
							getUIContext().getString(R.string.reg),
							R.drawable.icon64, 0);
					wl[i].acquire();
				}
			} catch (Exception ex) {
			}
			i++;
		}
	}

	public void register(boolean hasyellowLight) {
		IpAddress.setLocalIpAddress();
		int i = 0;
		for (RegisterAgent ra : ras) {
			try {
				if (user_profiles[i] == null
						|| user_profiles[i].username.equals("")
						|| user_profiles[i].realm.equals("")) {
					i++;
					continue;
				}
				user_profiles[i].contact_url = getContactURL(
						user_profiles[i].from_url, sip_providers[i]);

				if (!Receiver.isFast(i)) {
					unregister(i);
				} else {
					if (ra != null && ra.register()) {
						if (hasyellowLight) {
							Receiver.onText(Receiver.REGISTER_NOTIFICATION + i,
									getUIContext().getString(R.string.reg),
									R.drawable.icon64, 0);
						}
						wl[i].acquire();
					}
				}
			} catch (Exception ex) {

			}
			i++;
		}
	}

	public void registerUdp() {
		IpAddress.setLocalIpAddress();
		int i = 0;
		for (RegisterAgent ra : ras) {
			try {
				if (user_profiles[i] == null
						|| user_profiles[i].username.equals("")
						|| user_profiles[i].realm.equals("")
						|| sip_providers[i] == null
						|| sip_providers[i].getDefaultTransport() == null
						|| sip_providers[i].getDefaultTransport().equals("tcp")) {
					i++;
					continue;
				}
				user_profiles[i].contact_url = getContactURL(
						user_profiles[i].from_url, sip_providers[i]);

				if (!Receiver.isFast(i)) {
					unregister(i);
				} else {
					if (ra != null && ra.register()) {
						Receiver.onText(Receiver.REGISTER_NOTIFICATION + i,
								getUIContext().getString(R.string.reg),
								R.drawable.icon64, 0);
						wl[i].acquire();
					}
				}
			} catch (Exception ex) {

			}
			i++;
		}
	}

	public void halt() { // modified
		LogUtil.makeLog(tag, "halt()");
		
		StackTraceElement[] els = Thread.currentThread().getStackTrace();
		
		for (int i = 0; i < els.length; i++) {
			StackTraceElement ste = els[i];
			Zed3Log.debug("test", "SipdroidEngine#halt called by:"+ste.getClassName() + " , " + ste.getMethodName());
		}
		
		innnerHalt(true); //close gps
	}
	
	public void haltNotCloseGps(){
		
		StackTraceElement[] els = Thread.currentThread().getStackTrace();
		
		for (int i = 0; i < els.length; i++) {
			StackTraceElement ste = els[i];
			Zed3Log.debug("test", "SipdroidEngine#haltNotCloseGps called by:"+ste.getClassName() + " , " + ste.getMethodName());
		}
		
		innnerHalt(false); //don't close gps
	}
	
	private void innnerHalt(boolean isCloseGps) {
		long time = SystemClock.elapsedRealtime();

		int i = 0;
		for (RegisterAgent ra : ras) {
			unregister(i);
			while (ra != null && ra.CurrentState != RegisterAgent.UNREGISTERED
					&& SystemClock.elapsedRealtime() - time < 2000)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			if (wl[i].isHeld()) {
				wl[i].release();
				if (pwl[i] != null && pwl[i].isHeld())
					pwl[i].release();
			}
			if (kas[i] != null) {
				Receiver.alarm(0, LoopAlarm.class);
				kas[i].halt();
			}
			Receiver.onText(Receiver.REGISTER_NOTIFICATION + i, null, 0, 0);
			if (ra != null)
				ra.halt();
			if (uas[i] != null) {
				uas[i].hangup();
				//Add by zzhan 2013-5-10
				uas[i].HaltGroupCall();
				if(isCloseGps) {
					// Added by zzhan 2011-11-07
					uas[i].haltListen();
				} else {
					uas[i].haltListenNotCloseGps();
				}
			}

			if (sip_providers[i] != null)
				sip_providers[i].halt();
			i++;
		}
		//stop in SipUAApp#exit() instead of here . modify by mou 2014-10-23
//		HeadsetPlugReceiver.stopReceive(SipUAApp.mContext);
	}

	public boolean isRegistered() {
		for (RegisterAgent ra : ras)
			if (ra != null && ra.isRegistered())
				return true;
		return false;
	}
	public boolean isRegistered(boolean login) {
		for (RegisterAgent ra : ras)
			if (ra != null && ra.isRegistered(login))
				return true;
		return false;
	}
	public boolean isRegistered(int i) {
		if(i >= ras.length) return false;//这个调用后期优化  为什么会出现i=1的情况
		if (ras[i] == null) {
			return false;
		}
		return ras[i].isRegistered();
	}

	public void onUaRegistrationSuccess(RegisterAgent reg_ra,
			NameAddress target, NameAddress contact, String result) {
//		if(!DeviceInfo.IS_Autologon){
//		if(registerListener != null){
//			registerListener.registerSuccess();
//		}}
		int i = 0;
		for (RegisterAgent ra : ras) {
			if (ra == reg_ra)
				break;
//			i++; /// 先注释掉  modify by hu
		}
		if (isRegistered(i)) {
			if (Receiver.on_wlan) {
				Receiver.alarm(60, LoopAlarm.class);
				Log.e("2===============================",
						"2===============================");
			}
			Receiver.onText(
					Receiver.REGISTER_NOTIFICATION + i,
					getUIContext().getString(
							i == pref ? R.string.regok/* pref */
									: R.string.regfailed/* regclick */),
					R.drawable.icon64, 0);
			reg_ra.subattempts = 0;
			//Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.login").putExtra("loginstatus", true));
			// Delete by zzhan 2011-9-7
			// reg_ra.startMWI();

			Receiver.registered();

			// Add zzhan 2011-9-6
			uas[i].OnRegisterSuccess(result);
			Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.login").putExtra("loginstatus", true));
			System.out.println("-----SipdroidEngine sendBroadcast loginstatus true");
		} else
			Receiver.onText(Receiver.REGISTER_NOTIFICATION + i, null, 0, 0);
		if (wl[i].isHeld()) {
			wl[i].release();
			if (pwl[i] != null && pwl[i].isHeld())
				pwl[i].release();
		}
	}

	String[] lastmsgs;

	public void onMWIUpdate(RegisterAgent mwi_ra, boolean voicemail,
			int number, String vmacc) {
		int i = 0;
		for (RegisterAgent ra : ras) {
			if (ra == mwi_ra)
				break;
			i++;
		}
		if (i != pref)
			return;

		// Add zzhan 2011-10-11
		uas[i].OnRegisterFailure();
		ras[i].CurrentState = RegisterAgent.UNREGISTERED;

		if (voicemail) {
			String msgs = getUIContext().getString(R.string.voicemail);
			if (number != 0) {
				msgs = msgs + ": " + number;
			}
			Receiver.MWI_account = vmacc;
			if (lastmsgs[i] == null || !msgs.equals(lastmsgs[i])) {
				Receiver.onText(Receiver.MWI_NOTIFICATION, msgs,
						android.R.drawable.stat_notify_voicemail, 0);
				lastmsgs[i] = msgs;
			}
		} else {
			Receiver.onText(Receiver.MWI_NOTIFICATION, null, 0, 0);
			lastmsgs[i] = null;
		}
	}

	static long lasthalt, lastpwl;

	/** When a UA failed on (un)registering. */
	public void onUaRegistrationFailure(RegisterAgent reg_ra,
			NameAddress target, NameAddress contact, String result) {
		MyLog.e("Register", "Register failed reason:"+result);
		//add by hu 2013-7-4
				/*if(registerListener != null){
					registerListener.registerPwdError();
				}*/
		Intent failedIntent = new Intent("com.zed3.sipua.login");
		failedIntent.putExtra("result", result);
		Receiver.mContext.sendBroadcast(failedIntent);
		System.out.println("-----SipdroidEngine sendBroadcast loginresult:"+result);
		boolean retry = false;
		int i = 0;
		for (RegisterAgent ra : ras) {
			if (ra == reg_ra)
				break;
			// i++;修改因网络不好注册失败时因两个账户通知栏同时出现登陆成功和登陆失败两个通知的问题 modify by liangzhang
			// 2014-10-14
		}
		if (isRegistered(i)) {
			reg_ra.CurrentState = RegisterAgent.UNREGISTERED;
			Receiver.onText(Receiver.REGISTER_NOTIFICATION + i, null, 0, 0);
		} else {
			retry = true;
			Receiver.onText(Receiver.REGISTER_NOTIFICATION + i, getUIContext()
					.getString(R.string.regfailed) /*+ " (" + result + ")"*/,// 去掉协议内容eg。403,
					R.drawable.sym_presence_away, 0);
		}
		if (retry && SystemClock.uptimeMillis() > lastpwl + 45000
				&& pwl[i] != null && !pwl[i].isHeld() && Receiver.on_wlan) {
			lastpwl = SystemClock.uptimeMillis();
			if (wl[i].isHeld())
				wl[i].release();
			pwl[i].acquire();
			register(true);
			if (!wl[i].isHeld() && pwl[i].isHeld())
				pwl[i].release();
		} else if (wl[i].isHeld()) {
			wl[i].release();
			if (pwl[i] != null && pwl[i].isHeld())
				pwl[i].release();
		}
		if (SystemClock.uptimeMillis() > lasthalt + 45000) {
			lasthalt = SystemClock.uptimeMillis();
			sip_providers[i].haltConnections();
		}
		if (!Thread.currentThread().getName().equals("main"))
			updateDNS();
		reg_ra.stopMWI();
		WifiManager wm = (WifiManager) Receiver.mContext
				.getSystemService(Context.WIFI_SERVICE);
		wm.startScan();
	}

	/**
	 * 保存最新的服务器地址
	 */
	public void updateDNS() {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(
				getUIContext()).edit();
		int i = 0;
		for (SipProvider sip_provider : sip_providers) {
			try {
				// dnsString 192.168.100.45
				String dnsString = IpAddress.getByName(
						PreferenceManager.getDefaultSharedPreferences(
								getUIContext()).getString(
								Settings.PREF_SERVER + (i != 0 ? i : ""), ""))
						.toString();
				edit.putString(Settings.PREF_DNS + i, dnsString/*
																 * IpAddress.
																 * getByName(
																 * PreferenceManager
																 * .
																 * getDefaultSharedPreferences
																 * (
																 * getUIContext(
																 * )).getString(
																 * Settings
																 * .PREF_SERVER
																 * +(i!=0?i:""),
																 * ""
																 * )).toString()
																 */);
			} catch (UnknownHostException e1) {
				i++;
				continue;
			}
			edit.commit();
			setOutboundProxy(sip_provider, i);
			i++;
		}
	}

	/** Receives incoming calls (auto accept) */
	public void listen() {
		for (UserAgent ua : uas) {
			if (ua != null) {
				ua.printLog("UAS: WAITING FOR INCOMING CALL");

				if (!ua.user_profile.audio && !ua.user_profile.video) {
					ua.printLog("ONLY SIGNALING, NO MEDIA");
				}

				ua.listen();
			}
		}
	}

	public void info(char c, int duration) {
		ua.info(c, duration);
	}

	/** Makes a new call */
	public boolean call(String target_url, boolean force) {
		if (!Receiver.mSipdroidEngine.isRegistered()) {
			DeviceInfo.isEmergency = false;
			return false;
		}

		int p = pref;
		boolean found = false;

		if (isRegistered(p) && Receiver.isFast(p))
			found = true;
		else {
			for (p = 0; p < LINES; p++)
				if (isRegistered(p) && Receiver.isFast(p)) {
					found = true;
					break;
				}
			if (!found && force) {
				p = pref;
				if (Receiver.isFast(p))
					found = true;
				else
					for (p = 0; p < LINES; p++)
						if (Receiver.isFast(p)) {
							found = true;
							break;
						}
			}
		}

		if (!found || (ua = uas[p]) == null) {
			DeviceInfo.isEmergency = false;
			if (PreferenceManager.getDefaultSharedPreferences(getUIContext())
					.getBoolean(Settings.PREF_CALLBACK,
							Settings.DEFAULT_CALLBACK)
					&& PreferenceManager
							.getDefaultSharedPreferences(getUIContext())
							.getString(Settings.PREF_POSURL,
									Settings.DEFAULT_POSURL).length() > 0) {
				Receiver.url("n=" + Uri.decode(target_url));
				return true;
			}
			return false;
		}

		ua.printLog("UAC: CALLING " + target_url);

		if (!ua.user_profile.audio && !ua.user_profile.video) {
			ua.printLog("ONLY SIGNALING, NO MEDIA");

		}
		if (isMakeVideoCall == 1)
			// 启动视频呼叫
			ua.user_profile.video = true;
		else if (isMakeVideoCall == 0)
			// 禁止视频呼叫
			ua.user_profile.video = false;

		MyLog.e("sipdroidEngine", "isMakeVideoCall is:"
				+ (isMakeVideoCall == 1 ? "true" : "false"));

		return ua.call(target_url, false);
	}

	// add by oumogang 2013-04-25 1
	/** Makes a new call */
	public boolean call(String target_url, boolean force,
			boolean isGroupBroadcast, String numbersStr) {

		int p = pref;
		boolean found = false;

		if (isRegistered(p) && Receiver.isFast(p))
			found = true;
		else {
			for (p = 0; p < LINES; p++)
				if (isRegistered(p) && Receiver.isFast(p)) {
					found = true;
					break;
				}
			if (!found && force) {
				p = pref;
				if (Receiver.isFast(p))
					found = true;
				else
					for (p = 0; p < LINES; p++)
						if (Receiver.isFast(p)) {
							found = true;
							break;
						}
			}
		}

		if (!found || (ua = uas[p]) == null) {
			if (PreferenceManager.getDefaultSharedPreferences(getUIContext())
					.getBoolean(Settings.PREF_CALLBACK,
							Settings.DEFAULT_CALLBACK)
					&& PreferenceManager
							.getDefaultSharedPreferences(getUIContext())
							.getString(Settings.PREF_POSURL,
									Settings.DEFAULT_POSURL).length() > 0) {
				Receiver.url("n=" + Uri.decode(target_url));
				return true;
			}
			return false;
		}

		ua.printLog("UAC: CALLING " + target_url);

		if (!ua.user_profile.audio && !ua.user_profile.video) {
			ua.printLog("ONLY SIGNALING, NO MEDIA");
		}
		if (isMakeVideoCall == 1)
			// 启动视频呼叫
			ua.user_profile.video = true;
		else if (isMakeVideoCall == 0)
			// 禁止视频呼叫
			ua.user_profile.video = false;

		MyLog.e("sipdroidEngine", "isMakeVideoCall is:"
				+ (isMakeVideoCall == 1 ? "true" : "false"));
		return ua.call(target_url, false, isGroupBroadcast, numbersStr);
	}

	/** Makes a group call */
	// 由call 函数修改 func by yangjian
	public boolean MakeGroupCall(int pttStatus) {
		int p = pref;
		boolean found = false;

		if (isRegistered(p) && Receiver.isFast(p))
			found = true;
		else {
			for (p = 0; p < LINES; p++) {
				if (isRegistered(p) && Receiver.isFast(p)) {
					found = true;
					break;
				}
			}

			// 这段没明白
			// if (!found && force) {
			// p = pref;
			// if (Receiver.isFast(p))
			// found = true;
			// else for (p = 0; p < LINES; p++)
			// if (Receiver.isFast(p)) {
			// found = true;
			// break;
			// }
			// }
		}

		// add by yangjian 这里表示没找到的话，使用直接拨号？

		// if (!found || (ua = uas[p]) == null) {
		// if
		// (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_CALLBACK,
		// Settings.DEFAULT_CALLBACK) &&
		// PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_POSURL,
		// Settings.DEFAULT_POSURL).length() > 0) {
		// Receiver.url("n="+Uri.decode(target_url));
		// return true;
		// }
		// return false;
		// }
		if (!found) {
			return false;
		} else {
			ua.printLog("UAC: PTT Pressed ");

			if (!ua.user_profile.audio && !ua.user_profile.video) {
				ua.printLog("ONLY SIGNALING, NO MEDIA");
			}
			// interface by yangjian add by yangjian
			// return ua.makeGroupCall(pttStatus);
			return true;
		}
	}

	
	
	/** Makes a new call */
	public boolean antaCall1(String target_url,String numbers,boolean force,boolean isGroupBroadcast) {
		// add by hdf
		if (!Receiver.mSipdroidEngine.isRegistered()) {
			return false;
		}
		
		
		int p = pref;
		boolean found = false;
		
		if (isRegistered(p) && Receiver.isFast(p))
			found = true;
		else {
			for (p = 0; p < LINES; p++)
				if (isRegistered(p) && Receiver.isFast(p)) {
					found = true;
					break;
				}
			if (!found && force) {
				p = pref;
				if (Receiver.isFast(p))
					found = true;
				else for (p = 0; p < LINES; p++)
					if (Receiver.isFast(p)) {
						found = true;
						break;
					}
			}
		}
		
	
		if (!found || (ua = uas[p]) == null) {
			if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_CALLBACK, Settings.DEFAULT_CALLBACK) &&
					PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_POSURL, Settings.DEFAULT_POSURL).length() > 0) {
				Receiver.url("n="+Uri.decode(target_url));
				return true;
			}
			return false;
		}
		
		ua.printLog("UAC: CALLING " + target_url);
		
//		if (!ua.user_profile.audio && !ua.user_profile.video)
//		{
//			 ua.printLog("ONLY SIGNALING, NO MEDIA");
//		}
//		if (isMakeVideoCall)
//			// 启动视频呼叫
//			ua.user_profile.video = true;
//		else
//			// 禁止视频呼叫
		ua.user_profile.video = false;
		
		MyLog.e("sipdroidEngine", "isMakeVideoCall is:"
				+ (isMakeVideoCall /*== true*/== 1 ? "true" : "false"));
		return ua.antaCall2(target_url,numbers, false,isGroupBroadcast);
	}

	
	public void answercall() {
		Receiver.stopRingtone();
		ua.accept();
	}

	public void rejectcall() {
		Receiver.mIsRejectedByUser = true;
		ua.printLog("UA: HANGUP");
		ua.hangup();
	}

	public void togglehold() {
		ua.reInvite(null, 0);
	}

	public void transfer(String number) {
		ua.callTransfer(number, 0);
	}

	public void togglemute() {
		if (ua.muteMediaApplication()){}
//			Receiver.onText(Receiver.CALL_NOTIFICATION, getUIContext()
//					.getString(R.string.menu_mute),
//					android.R.drawable.stat_notify_call_mute,
//					Receiver.ccCall.base);
		else
			Receiver.progress();
	}

	public void togglebluetooth() {
		ua.bluetoothMediaApplication();
		Receiver.progress();
	}

	public int speaker(int mode) {
		int ret = ua.speakerMediaApplication(mode);
		Receiver.progress();
		return ret;
	}

	public void keepAlive() {
		int i = 0;
		for (KeepAliveSip ka : kas) {
			if (ka != null && Receiver.on_wlan && isRegistered(i))
				try {
					ka.sendToken();
					Receiver.alarm(60, LoopAlarm.class);
					Log.e("1===============================",
							"1===============================");
				} catch (IOException e) {
					if (!Sipdroid.release)
						e.printStackTrace();
				}
			i++;
		}
	}

	// Add by zzhan 2011-9-15
	public UserAgent GetCurUA() {
		return ua;
	}
}
