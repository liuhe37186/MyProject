package com.zed3.sipua.ui;

import java.net.DatagramSocket;

import org.zoolu.tools.InCallInfo;
import org.zoolu.tools.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.location.MemoryMg;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.phone.Call;
import com.zed3.sipua.phone.Phone;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.window.MyWindowManager;

public class DemoCallScreen extends BaseActivity implements SensorEventListener{

	private final static String TAG = "DemoCallScreen";
	private IntentFilter mFilter;
	public static DemoCallScreen mContext;
	public static boolean started;

	private final String ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
	
	//2014-9-15 add by wlei 视频来电界面动态效果
	private FrameLayout ringA,photo;
	private boolean isRinging = true;
	private ImageView image1, image2, image3;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case 0:
				image1.setAlpha(0);
				image2.setAlpha(250);
				image3.setAlpha(250);
				break;
			case 1:
				image1.setAlpha(250);
				image2.setAlpha(0);
				image3.setAlpha(250);

				break;
			case 2:
				image1.setAlpha(250);
				image2.setAlpha(250);
				image3.setAlpha(0);
				break;
			default:
				break;
			}
		};
	};

	private BroadcastReceiver quitRecv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// quite 
			if (intent.getAction().equalsIgnoreCase(ACTION_CALL_END)) {

				if (Receiver.call_state == UserAgent.UA_STATE_IDLE) {
					Log.e("DemoCallScreen", "ACTION_CALL_END onstop");
					//初始默认值
					Receiver.engine(DemoCallScreen.this).isMakeVideoCall = -1;
					MemoryMg.SdpPtime=0;
					
					finish();
				}
				else
					MyLog.e(TAG, "0000");
			}

			// 通话中页面打开后就通过广播关闭该页面了 //add by hdf0914
			if (intent.getAction().equalsIgnoreCase(
					"android.action.closeDemoCallScreen")) {

				DemoCallScreen.this.finish();

				MyLog.e(TAG, "finish broadcas");
			}
		}
	};
	private DatagramSocket mdsSocket = null;
	// private Audio183Handle audio183 = null;

	TextView calltip = null;
	TextView callNum = null;
	TextView callName = null;
	ImageView btnok=null;
	ImageView btnno=null;
	ImageView btnoutend=null;
	Phone ccPhone;// 很重要

	private InCallInfo info = null;
	// 计时器
	Chronometer mElapsedTime;
//	private ImageView keyboardShowBT;
//	private ImageView keyboardHideBT;
//	
//	private ImageView loudspeakerOnBT;
//	private ImageView loudspeakerOffBT;
//	
//	private ImageView forbidSoundOutOnBT;
//	private ImageView forbidSoundOutOffBT;
//	private View keyBoard;
//	private View controlOverLayView;
	LinearLayout line_outcall=null;
	LinearLayout line_incall=null;
	
	
	public static String mNumber;
	public static String mName;
	
	private boolean rejFlag=false;
	private SensorManager sensorManager;
	private Sensor proximitySensor;
	private String mScreanWakeLockKey = TAG;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		super.onCreate(savedInstanceState);
		
		if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
			this.finish();
			return;
		}
		// 设置窗口属性->无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//唤醒屏幕
		mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen(TAG);
		//来电解锁
		MyWindowManager.getInstance().disableKeyguard(this);
		
		setContentView(R.layout.callscreen);
        screenOffView = findViewById(R.id.screen_off_view);
		calltip = (TextView) findViewById(R.id.calltip);
		callNum = (TextView) findViewById(R.id.callnum);
		callName = (TextView) findViewById(R.id.callname);
		mElapsedTime = (Chronometer) findViewById(R.id.elapsedTime);
		//2014-9-15 add by wlei 
		image1 = (ImageView) findViewById(R.id.image1);
		image2 = (ImageView) findViewById(R.id.image2);
		image3 = (ImageView) findViewById(R.id.image3);
		photo = (FrameLayout) findViewById(R.id.photoUser);
		ringA = (FrameLayout) findViewById(R.id.callOut);
		//呼出挂断
		btnoutend=(ImageView) findViewById(R.id.out_end_call);
		btnoutend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// if call is idle，should close notification and finish current activity。 add by mou 2014-11-05
				if (Receiver.isCallNotificationNeedClose()) {
//					finish();
				}
				if(ringA!=null)isRinging = false;//2014-9-15 add by wlei
				if (!rejFlag) {
					rejFlag = true;
					reject();
				}
				finish();
			}
		});
		//接听
		btnok=(ImageView) findViewById(R.id.accept_call);
		
		btnok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// if call is idle，should close notification and finish current activity。 add by mou 2014-11-05
				if (Receiver.isCallNotificationNeedClose()) {
//					finish();
				}
				if(ringA!=null)isRinging = false;//2014-10-16 add by wlei
				if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
					answer();
				}
				finish();
			}
		});
		//挂断
		btnno=(ImageView) findViewById(R.id.end_call);
		btnno.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// if call is idle，should close notification and finish current activity。 add by mou 2014-11-05
				if (Receiver.isCallNotificationNeedClose()) {
//					finish();
				}
				if(ringA!=null)isRinging = false;//2014-9-15 add by wlei
				if (!rejFlag) {
					rejFlag = true;
					reject();
				}
				finish();
			}
		});
		line_incall = (LinearLayout) findViewById(R.id.line_incall);
		line_outcall = (LinearLayout) findViewById(R.id.line_outcall);
		
//		field=(LinearLayout) findViewById(R.id.income_control_layout);
//		field.setVisibility(View.INVISIBLE);
//		// menu
//		keyboardShowBT = (ImageView) findViewById(R.id.keyboard_show);
//		keyboardShowBT.setOnClickListener(this);
//		keyboardHideBT = (ImageView) findViewById(R.id.keyboard_hide);
//		keyboardHideBT.setOnClickListener(this);
//
//		loudspeakerOnBT = (ImageView) findViewById(R.id.loudspeaker_on);
//		loudspeakerOnBT.setOnClickListener(this);
//		loudspeakerOffBT = (ImageView) findViewById(R.id.loudspeaker_off);
//		loudspeakerOffBT.setOnClickListener(this);
//
//		forbidSoundOutOnBT = (ImageView) findViewById(R.id.forbid_sound_out_on);
//		forbidSoundOutOnBT.setOnClickListener(this);
//		forbidSoundOutOffBT = (ImageView) findViewById(R.id.forbid_sound_out_off);
//		forbidSoundOutOffBT.setOnClickListener(this);
//
//		// keboard
//		keyBoard = findViewById(R.id.keyboard_layout);
//		keyBoard.setVisibility(View.INVISIBLE);
		

		// 接受退出的广播
		mFilter = new IntentFilter();
		mFilter.addAction(ACTION_CALL_END);
		mFilter.addAction("android.action.closeDemoCallScreen");
		registerReceiver(quitRecv, mFilter);
		
		// 通话时间
		if (mElapsedTime != null) {
			mElapsedTime.setBase(SystemClock.elapsedRealtime());
			mElapsedTime.start();
		}
		MyLog.e(TAG, "oncreate");

	}

//	@Override
//	public void onClick(View v) {
//		// TODO Auto-generated method stub
//		switch (v.getId()) {
//		case R.id.keyboard_show:
//
//			keyBoard.setVisibility(View.VISIBLE);
//			keyboardShowBT.setVisibility(View.INVISIBLE);
//			keyboardHideBT.setVisibility(View.VISIBLE);
//
//			break;
//		case R.id.keyboard_hide:
//			keyBoard.setVisibility(View.INVISIBLE);
//			keyboardHideBT.setVisibility(View.INVISIBLE);
//			keyboardShowBT.setVisibility(View.VISIBLE);
//
//			break;
//		case R.id.loudspeaker_on:
//			loudspeakerOnBT.setVisibility(View.INVISIBLE);
//			loudspeakerOffBT.setVisibility(View.VISIBLE);
//
//			Receiver.engine(this).speaker(AudioManager.MODE_IN_CALL);
//
//			break;
//		case R.id.loudspeaker_off:
//			loudspeakerOffBT.setVisibility(View.INVISIBLE);
//			loudspeakerOnBT.setVisibility(View.VISIBLE);
//
//			Receiver.engine(this).speaker(AudioManager.MODE_NORMAL);
//			break;
//		case R.id.forbid_sound_out_on:
//				
//			Receiver.engine(this).togglemute();
//			forbidSoundOutOnBT.setVisibility(View.INVISIBLE);
//			forbidSoundOutOffBT.setVisibility(View.VISIBLE);
//			
//			break;
//		case R.id.forbid_sound_out_off:
//				
//			Receiver.engine(this).togglemute();
//			forbidSoundOutOffBT.setVisibility(View.INVISIBLE);
//			forbidSoundOutOnBT.setVisibility(View.VISIBLE);
//			
//			break;
//		}
//	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		isRinging = true;
		super.onResume();
		screenOffView.setVisibility(View.GONE);
		if (sensorManager == null) {
			sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			//避免可能出现有的手机没有距离传感器 。 modify by lwang 2014-10-31
//			proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//			sensorManager.registerListener(this, proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
		}
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null) {
			Toast.makeText(mContext,
					getResources().getString(R.string.unProximitySensor), 0)
					.show();
		} else {
			sensorManager.registerListener(this, proximitySensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
		try {
			// 显示呼叫人号码、来电人号码
			GetCallNum();
		} catch (Exception e) {
			MyLog.e(TAG, "GetCallNum error " + e.toString());
		}
		
		// 呼入状态
		if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
			// 语音通话
			//if (UserAgent.ServerCallType == 0) {
			//	calltip.setText(R.string.call_voicecome);// 语音来电
			//} else if (UserAgent.ServerCallType == 1)
			calltip.setText(R.string.vedio_incoming);// 视频来电
			//显示接听  按钮
			line_incall.setVisibility(View.VISIBLE);
			line_outcall.setVisibility(View.GONE);
			//2014-9-15 add by wlei 视频来电动态效果
			ringA.setVisibility(View.VISIBLE);
			photo.setVisibility(View.INVISIBLE);
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					int num = 0;
					while(isRinging){
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Message msg = Message.obtain();
					msg.arg1 = num;
					if (num < 2) {
						num++;
					} else {
						num = 0;
					}
					handler.sendMessage(msg);
				}
				}
			}).start();
			
			//MyLog.e(TAG, "ServerCallType:"+UserAgent.ServerCallType);
			
		} else if (Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL) {// 呼出状态
			// if (MemoryMg.getInstance().IsUploadVideo)
			// calltip.setText(R.string.call_videouploading);
			calltip.setText(R.string.video_calling);
			//field.setVisibility(View.INVISIBLE);
			line_incall.setVisibility(View.GONE);
			line_outcall.setVisibility(View.VISIBLE);
			
			
		}
		MyLog.e(TAG, "call state :"+Receiver.call_state);
		// if call is idle before onresume ，should close notification and finish current activity。 add by mou 2014-11-05
		if (Receiver.isCallNotificationNeedClose()) {
			finish();
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		isRinging = false;
		if(sensorManager!=null){
			sensorManager.unregisterListener(this);
		}
		super.onPause();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (Receiver.call_state == UserAgent.UA_STATE_IDLE)

			started = true;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		started = false;
	}

	void moveBack() {
		if (Receiver.ccConn != null && !Receiver.ccConn.isIncoming()) {
			// after an outgoing call don't fall back to the contact
			// or call log because it is too easy to dial accidentally from
			// there
			// added by yangjian 去掉程序最小化
			// startActivity(Receiver.createHomeIntent());
		}
		onStop();
	}
	
	public void reject() {
		if (Receiver.ccCall != null) {		
			//process in Receiver#onState() instead of here. modify by mou 2014-12-30
//			Receiver.stopRingtone();
			Receiver.ccCall.setState(Call.State.DISCONNECTED);

		}
		(new Thread() {
			public void run() {
				Receiver.engine(Receiver.mContext).rejectcall();
				rejFlag = false;
			}
		}).start();
	}

	public void answer() {
		CallUtil.answerCall();
		if (Receiver.ccCall != null) {
			Receiver.ccCall.setState(Call.State.ACTIVE);
			Receiver.ccCall.base = SystemClock.elapsedRealtime();

		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {

		case KeyEvent.KEYCODE_CALL:// 接听键
			if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {// 并且是点呼
				answer();
				this.finish();
			}
			return true;
		case KeyEvent.KEYCODE_ENDCALL:// 挂断键
			reject();

			return true;
		case KeyEvent.KEYCODE_BACK:// 返回键
			reject();

			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	// 显示来电人号码、呼叫人号码
	private void GetCallNum() {

//		Call call = Receiver.ccCall;
//		// Call.State state = call.getState();
//
//		String name, displayNumber = "";
//		// First, see if we need to query.
//
//		PhoneUtils.CallerInfoToken infox = PhoneUtils.startGetCallerInfo(
//				mContext, call, this, callNum);
//		CallerInfo info = infox.currentInfo;

		//add by oumogang 2013-05-05
//		if (Receiver.ccConn != null) {
//			// 通话界面显示，显示名称，本地优先，服务器其次，号码第三；
//			String name;
//			String displayName = Receiver.ccConn.getAddress2();
//			String num = Receiver.ccConn.getAddress();
//			String contactName = ContactUtil.getUserName(num);
//			if (contactName != null) {
//				name = contactName;
//			} else if (displayName != null) {
//				name = displayName;
//			} else {
//				name = Receiver.ccConn.getAddress();
//				num = "";
//			}
//			
//			callName.setText(name);
//			if(!name.equals(num))
//				callNum.setText(num);
//		}
		
		//Toast.makeText(mContext, CallUtil.mName+"--"+CallUtil.mNumber, Toast.LENGTH_LONG).show();
		//modify by oumogang 2013-05-16
		callName.setText(CallUtil.mName);
		if(!CallUtil.mName.equals(CallUtil.mNumber))
		callNum.setText(CallUtil.mNumber);
//		if (info != null) {
//			if (TextUtils.isEmpty(info.name)) {
//				if (TextUtils.isEmpty(info.phoneNumber)) {
//					{
//						name = mContext.getString(R.string.unknown);
//					}
//				} else {
//					name = info.phoneNumber;
//				}
//			} else {
//				name = info.name;
//				displayNumber = info.phoneNumber;
//			}
//
//		} else
//			name = mContext.getString(R.string.unknown);
//
//		callName.setText(name);
//
//		if (displayNumber != null) {
//			callNum.setText(displayNumber);
//		} else {
//			callNum.setText("");
//		}

	}

	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MyLog.e(TAG, "democallscreen ondestory");
		
		try {
			if (mElapsedTime != null) {
				mElapsedTime.stop();
			}
			MyPowerManager.getInstance().releaseScreenWakeLock(mScreanWakeLockKey);
			MyWindowManager.getInstance().reenableKeyguard(this);
			if (mFilter != null)
				unregisterReceiver(quitRecv);
		} catch (Exception e) {
			MyLog.e(TAG, "democallscreen ondestory error:" + e.toString());
		}
		//add by wlei 2014-10-16
		mContext = null;

	}
	
	public static DemoCallScreen getInstance(){
		return mContext;
	}
	private static final int REJECT_CALL = 0;//挂断
	private static final int ANSWER_CALL = 1;//接听
	Handler buttonHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if(ringA!=null)isRinging = false;
				if (!rejFlag) {
					rejFlag = true;
					reject();
				}
				finish();
				break;
			case 1:
				if(ringA!=null)isRinging = false;
				if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
					answer();
					DemoCallScreen.this.finish();
				}
				break;
			default:
				break;
			}
		};
	};
	public static boolean pactive;
	static final float PROXIMITY_THRESHOLD = 5.0f;
	private View screenOffView;
	//拒接
   public void rejectCall(){
	   Message msg = buttonHandler.obtainMessage();
	   msg.what = REJECT_CALL;
	   buttonHandler.sendMessage(msg);
   }
   //接听
   public void answerCall(){
	   Message msg = buttonHandler.obtainMessage();
	   msg.what = ANSWER_CALL;
	   buttonHandler.sendMessage(msg);
   }

@Override
public void onSensorChanged(SensorEvent event) {
	// TODO Auto-generated method stub
	float distance = event.values[0];
    boolean active = (distance >= 0.0 && distance < PROXIMITY_THRESHOLD && distance < event.sensor.getMaximumRange());
    pactive = active;
    screenOffView.setVisibility(active?View.VISIBLE:View.GONE);
}

@Override
public void onAccuracyChanged(Sensor sensor, int accuracy) {
	// TODO Auto-generated method stub
	
}

}
