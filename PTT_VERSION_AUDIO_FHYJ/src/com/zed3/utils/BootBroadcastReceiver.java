package com.zed3.utils;

import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.sipua.welcome.DeviceInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		LogUtil.makeLog("BootBroadcastReceiver", "onReceiver() onReceiver() intent.getAction() = " + intent.getAction());
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			if(IsAutoRunConfig(context).equals("1")){
				//自动登陆版本启动检测网络的服务 modified by liangzhang 2014-11-25
				if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
					PreferenceManager.getDefaultSharedPreferences(context).
					edit().putBoolean("NetworkListenerService", true).commit();
					Intent it = new Intent(context, NetworkListenerService.class);
					context.startService(it);
				}
				Intent startSplashIntent = new Intent(context,SplashActivity.class);
				startSplashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(startSplashIntent);
			}
		}
	}
	
	private String IsAutoRunConfig(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("autorunkey", "1");
	}
}
