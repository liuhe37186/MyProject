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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.zed3.location.GpsTools;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.utils.Zed3Log;

public class RegisterService extends Service {
	Receiver m_receiver;
	Caller m_caller;
	private Context mContext;
	
    public void onDestroy() {
		super.onDestroy();
		if (m_receiver != null) {
			unregisterReceiver(m_receiver);
			m_receiver = null;
		}
		if (m_caller != null) {
			unregisterReceiver(m_caller);
			m_caller = null;
		}
		//该方法 停止OneShotAlarm2的AlarmManager全局定时器
		Receiver.alarm(0, OneShotAlarm2.class);
	}
    
    @Override
    public void onCreate() {
    	Zed3Log.debug("testgps", "RegisterService#onCreate enter");
    	super.onCreate();
    	mContext = getApplicationContext();
//    	Log.e("sipservice", "sipservice");
//    	if (Receiver.mContext == null) Receiver.mContext = this;
        if (m_receiver == null) {
			 IntentFilter intentfilter = new IntentFilter();
			 intentfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			 intentfilter.addAction(Receiver.ACTION_DATA_STATE_CHANGED);
			 intentfilter.addAction(Receiver.ACTION_PHONE_STATE_CHANGED);
			 intentfilter.addAction(Receiver.ACTION_DOCK_EVENT);
			 intentfilter.addAction(Intent.ACTION_HEADSET_PLUG);
			 intentfilter.addAction(Intent.ACTION_USER_PRESENT);
//			 intentfilter.addAction(Intent.ACTION_SCREEN_OFF);
//			 intentfilter.addAction(Intent.ACTION_SCREEN_ON);
			 intentfilter.addAction(Receiver.ACTION_VPN_CONNECTIVITY);
			 intentfilter.addAction(Receiver.ACTION_SCO_AUDIO_STATE_CHANGED);
			 intentfilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			 intentfilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			 
			 //added by yangjian 接收PTT广播信息
			 intentfilter.addAction(Receiver.ACTION_PTT_DOWN);
			 intentfilter.addAction(Receiver.ACTION_PTT_UP);
			 
	         registerReceiver(m_receiver = new Receiver(), intentfilter);      
			// 是否重复？？delete by liangzhang 2014-07-30
			// intentfilter = new IntentFilter();
			// registerReceiver(m_receiver = new Receiver(), intentfilter);
        }
        if (m_caller == null) {
        	IntentFilter intentfilter = new IntentFilter();
        	intentfilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        	registerReceiver(m_caller = new Caller(), intentfilter);      
        }
        
        Receiver.engine(this).isRegistered();
        
        final UserAgent currentUserAgent = Receiver.engine(this).GetCurUA();
        Zed3Log.debug("testgps", "RegisterService#onCreate currentUserAgent = " + currentUserAgent);
        if(currentUserAgent!=null){
        	
        	//1.判断是否需要启动GPS
        	boolean enableGps = isEnableGps();
        	Zed3Log.debug("testgps", "RegisterService#onCreate enableGps = " + enableGps);
        	
        	//2. SharedPreferences中存在服务器时间
        	boolean existUnixTime = existUnixTime();
        	Zed3Log.debug("testgps", "RegisterService#onCreate existUnixTime = " + existUnixTime);
        	
        	//3. 1和2都成立的情况下 则打开GPS
        	if(enableGps && existUnixTime) {
        		Zed3Log.debug("testgps", "RegisterService#onCreate is open gps = " + (currentUserAgent.isOpenGps()));
        		//4. 如果没有打开GPS,则打开
        		if(!currentUserAgent.isOpenGps()){
        			Zed3Log.debug("testgps", "RegisterService#onCreate prepare open gps");
        			currentUserAgent.GPSOpenLock();
        		}
        		
        	} 
        	
        } 
        
        Zed3Log.debug("testgps", "RegisterService#onCreate exit");
    }
    
    private boolean existUnixTime() {
    	String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
		
		long unixTime = settings.getLong(Settings.SERVER_UNIX_TIME, Settings.DEFAULT_SERVER_UNIX_TIME);
		
		
    	Zed3Log.debug("testgps", "RegisterService#existUnixTime() unixTime = " + unixTime);
    	return (unixTime > 0);
	}

	private boolean isEnableGps(){
    	
    	String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
    	
		int mode = settings.getInt(Settings.PREF_LOCATEMODE, Settings.DEFAULT_PREF_LOCATEMODE);
		
		Zed3Log.debug("testgps", "RegisterService#isEnableGps mode = " + mode);
		
		if (mode < 3 && mode > -1) {
			return true;
		}
		
		return false;
    }
    
    @Override
    public void onStart(Intent intent, int id) {
         super.onStart(intent,id);
       //  Receiver.alarm(10*60, OneShotAlarm2.class);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
