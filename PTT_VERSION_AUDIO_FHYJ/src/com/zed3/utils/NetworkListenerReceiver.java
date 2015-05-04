package com.zed3.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.zed3.net.util.NetChecker;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.splash.SplashActivity;

/**
 * 该广播用来监听网络变化的广播android.net.conn.CONNECTIVITY_CHANGE，处理自动登陆版本开机启动时因网络异常而登陆失败的问题
 * @author liangzhang 2014-11-26
 *
 */
public class NetworkListenerReceiver extends BroadcastReceiver {

	public NetworkListenerReceiver() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		LogUtil.makeLog("NetworkListenerReceiver", "onReceiver() intent.getAction() = " + intent.getAction());
		if (intent.getAction() != null) {
			if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
				if (NetChecker.isNetworkAvailable(context)) {
					//网络恢复正常后启动集群通 
					SharedPreferences mSharedPreferences = context.getSharedPreferences(Settings.sharedPrefsFile, Context.MODE_PRIVATE);
					if (mSharedPreferences.getBoolean("NetworkListenerService", false)) {
						Intent startIntent = new Intent(context, SplashActivity.class);
						startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(startIntent);
					}
				}
			}
		}
	}

}
