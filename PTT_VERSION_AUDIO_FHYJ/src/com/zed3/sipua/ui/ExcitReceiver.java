package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExcitReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Receiver.engine(context).expire(-1);
		//�ӳ�
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// deleted by hdf ֹͣ
		Receiver.engine(context).halt();
		// ֹͣ����
		context.stopService(new Intent(context, RegisterService.class));	
		// ȡ��ȫ�ֶ�ʱ��
		Receiver.alarm(0, OneShotAlarm.class);
		
		//��ת�ֻ�����
		Intent intent2=new Intent(Intent.ACTION_MAIN);
		intent2.addCategory(Intent.CATEGORY_HOME);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent2);
		//
		System.exit(0);
	}

}
