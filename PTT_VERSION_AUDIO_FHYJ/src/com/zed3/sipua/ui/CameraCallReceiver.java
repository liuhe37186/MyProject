package com.zed3.sipua.ui;

import org.zoolu.tools.MyLog;

import com.zed3.sipua.R;
import com.zed3.sipua.ui.splash.UnionLogin;
import com.zed3.toast.MyToast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

public class CameraCallReceiver extends BroadcastReceiver {

	Context mContext = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.mContext = context;
		if (intent.getAction().toString()
				.equals("android.intent.action.StartDemoCallScreen")) {

			MyLog.e("CameraCallReceiver",
					"android.intent.action.StartDemoCallScreen");
			handle.sendMessage(handle.obtainMessage(1));
		} else if (intent.getAction().toString()
				.equals("android.intent.action.RestartUnionLogin")) {
			//客服密码输入错误后需要跳转
			handle.sendMessage(handle.obtainMessage(2));
		}

	}

	final Handler handle = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				Intent it = new Intent(mContext, DemoCallScreen.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(it);
			} else if (msg.what == 2) {
				MyToast.showToast(true, mContext, mContext.getResources().getString(R.string.wrong_service_pwd));
				//客服密码输入错误后需要跳转
				SharedPreferences mypre = null;
				mypre = mContext.getSharedPreferences("com.zed3.sipua_preferences", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = mypre.edit();
				editor.putString("unionpassword", "0");
				editor.commit();
				
//				//
//				Receiver.engine(mContext).expire(-1);
//				// 延迟
//				try {
//					Thread.sleep(800);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				//退出sipdroidEngine引擎
//				Receiver.engine(mContext).halt();
//				Receiver.mSipdroidEngine=null;//必须去掉 
//				// 取消全局定时器
//				Receiver.alarm(0, OneShotAlarm.class);
//				mContext.sendBroadcast(new Intent("com.zed3.sipua.exitActivity"));
	
				Receiver.engine(mContext).expire(-1);
				Receiver.onText(Receiver.MISSED_CALL_NOTIFICATION, null, 0, 0);
				// 延迟
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// deleted by hdf 停止
				Receiver.engine(mContext).halt();
				
				// 停止服务
				mContext.stopService(new Intent(mContext, RegisterService.class));
				// 取消全局定时器
				Receiver.alarm(0, OneShotAlarm.class);
				Receiver.alarm(0, MyHeartBeatReceiver.class);
				//登录页面
				Intent it = new Intent(mContext, UnionLogin.class);
				it.putExtra("unionepwderror", true);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(it);
			}

		};
	};

}
