package com.zed3.utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * �÷������������Զ���½�汾��������ʱ�����쳣ʱ�������״̬�仯��
 * 
 * @author liangzhang 2014-11-25
 * 
 */
public class NetworkListenerService extends Service {
	private static final String TAG = "NetworkService";
	private NetworkListenerReceiver networkListenerReceiver;
	private IntentFilter filter;

	public NetworkListenerService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		LogUtil.makeLog(TAG, "--++>>onCreate()");
		super.onCreate();
		networkListenerReceiver = new NetworkListenerReceiver();
		filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		LogUtil.makeLog(TAG, "--++>>onStartCommand() registerReceiver");
		// ע���������״̬�Ĺ㲥
		registerReceiver(networkListenerReceiver, filter);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		LogUtil.makeLog(TAG, "--++>>onDestroy() unregisterReceiver");
		super.onDestroy();
		if (networkListenerReceiver != null && filter != null) {
			// ע����������״̬�Ĺ㲥
			unregisterReceiver(networkListenerReceiver);
		}
	}
}
