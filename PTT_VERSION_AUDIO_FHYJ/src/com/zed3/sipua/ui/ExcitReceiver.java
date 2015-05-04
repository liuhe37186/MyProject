package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExcitReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Receiver.engine(context).expire(-1);
		//延迟
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// deleted by hdf 停止
		Receiver.engine(context).halt();
		// 停止服务
		context.stopService(new Intent(context, RegisterService.class));	
		// 取消全局定时器
		Receiver.alarm(0, OneShotAlarm.class);
		
		//跳转手机桌面
		Intent intent2=new Intent(Intent.ACTION_MAIN);
		intent2.addCategory(Intent.CATEGORY_HOME);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent2);
		//
		System.exit(0);
	}

}
