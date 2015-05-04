package com.zed3.sipua.message;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.zoolu.tools.MyLog;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.SipdroidActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.LogUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class AlarmService extends Service {
	private View view;
	private WindowManager.LayoutParams wmParams;
	private WindowManager mWindowManager;
	private ImageView img;
	private Context mContext;
	private long downtime = 0;
	private long uptime = 0;
	private Timer timer = null;
	private Timer timer1 = null;
//	private boolean isEnglish = false;// 2014-8-26 GQT英文版

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		MyLog.e("AlarmService", "--++>>onCreate()");
//		isEnglish = ("en").equals(Locale.getDefault().getLanguage());// 2014-8-26
																		// GQT英文版
		view = LayoutInflater.from(getApplicationContext()).inflate(
				R.layout.yijian_gaojing, null);
		img = (ImageView) view.findViewById(R.id.a_key_alarm);
//		img.setBackgroundResource(isEnglish ? R.drawable.a_key_alarm_2
//				: R.drawable.a_key_alarm);// 2014-8-26 GQT英文版
		// 获取WindowManager
		mWindowManager = (WindowManager) getApplicationContext()
				.getSystemService("window");
		wmParams = new WindowManager.LayoutParams();
		// 该类型提供与用户交互，置于所有应用程序上方，但是在状态栏后面
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		// 不接受任何按键事件
		wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.x = 250;
		wmParams.y = 250;
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = PixelFormat.RGBA_8888;

		mWindowManager.addView(view, wmParams);
		view.setOnTouchListener(new OnTouchListener() {
			int lastX, lastY;
			int paramX, paramY;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					img.setImageResource(R.drawable.a_key_alarm_down);
//					img.setBackgroundResource(isEnglish ? R.drawable.a_key_alarm_down_2
//							: R.drawable.a_key_alarm_down);// 2014-8-26 GQT英文版
					downtime = System.currentTimeMillis();
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = wmParams.x;
					paramY = wmParams.y;
					timer = new Timer();
					timer1 = new Timer();
					timer1.schedule(new TimerTask() {

						@Override
						public void run() {
							Looper.prepare();
							// TODO Auto-generated method stub
							if (CallUtil.isInCall()) {
								Receiver.engine(SipUAApp.mContext).rejectcall();
							}
							Looper.loop();
						}
					}, 1000);
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							Looper.prepare();
							// TODO Auto-generated method stub
							if (DeviceInfo.svpnumber.equals("")) {
								MyToast.showToast(
										true,
										Receiver.mContext,
										Receiver.mContext
												.getString(R.string.unavailable_cno));
							} else {
								DeviceInfo.isEmergency = true;
								CallUtil.makeAudioCall(Receiver.mContext,
										DeviceInfo.svpnumber, null);
							}
							Looper.loop();
						}
					}, 2000);
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					if (Math.abs(dx) < 2 || Math.abs(dy) < 2) {
						break;
					}
					wmParams.x = paramX + dx;
					wmParams.y = paramY + dy;
					// 更新悬浮窗位置
					mWindowManager.updateViewLayout(v, wmParams);
					break;
				case MotionEvent.ACTION_UP:
					img.setImageResource(R.drawable.a_key_alarm);
//					img.setBackgroundResource(isEnglish ? R.drawable.a_key_alarm_2
//							: R.drawable.a_key_alarm);// 2014-8-26 GQT英文版
					uptime = System.currentTimeMillis();
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					if (timer1 != null) {
						timer1.cancel();
						timer1 = null;
					}
					break;
				}
				return true;
			}
		});
		super.onCreate();

	}

	@Override
	public void onDestroy() {
		MyLog.e("AlarmService", "--++>>onDestroy()");
		mWindowManager.removeView(view);
		super.onDestroy();
	}

}
