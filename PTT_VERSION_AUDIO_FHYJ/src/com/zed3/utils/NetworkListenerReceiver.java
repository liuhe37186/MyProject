package com.zed3.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.zed3.net.util.NetChecker;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.splash.SplashActivity;

/**
 * �ù㲥������������仯�Ĺ㲥android.net.conn.CONNECTIVITY_CHANGE�������Զ���½�汾��������ʱ�������쳣����½ʧ�ܵ�����
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
					//����ָ�������������Ⱥͨ 
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
