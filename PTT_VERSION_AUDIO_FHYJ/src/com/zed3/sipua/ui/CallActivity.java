package com.zed3.sipua.ui;

import java.util.HashMap;

import android.app.Activity;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Selection;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.location.MemoryMg;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.call.CallTimer;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Zed3Log;
import com.zed3.window.MyWindowManager;

public class CallActivity extends BaseActivity implements View.OnClickListener,OnLongClickListener, SensorEventListener {

	//
	private EditText numTxt = null;
	private ImageButton btnone = null;
	private ImageButton btntwo = null;
	private ImageButton btnthree = null;
	private ImageButton btnfour = null;
	private ImageButton btnfive = null;
	private ImageButton btnsix = null;
	private ImageButton btnseven = null;
	private ImageButton btnenight = null;
	private ImageButton btnnine = null;
	private ImageButton btn0 = null;
	private ImageButton btnmi = null;
	private ImageButton btnjing = null;
	//
	private ImageButton btnsearch = null;
	private ImageButton btndialy = null;
	private ImageButton btndel = null;
	
	private View yincang;

	protected static final int HIDECONNECTVIEW = 0;
	private static final String TAG = "CallActivity";
	private static TextView userNameTV;
	private static TextView userNumberTV;
	private static TextView callTimeTV;
	private ImageView userPhotoIV;
	private ImageView endCallBT;
	private ImageView endCallBT2;//GQT英文版
	private TextView endCallTT;//GQT英文版
	
	private ImageView keyboardShowBT;
	private ImageView keyboardHideBT;
	
	private static RelativeLayout loudspeakerOnBT;
	private static RelativeLayout loudspeakerOffBT;
	
	private RelativeLayout forbidSoundOutOnBT;
	private RelativeLayout forbidSoundOutOffBT;
	
	private ImageView newCallBT;
	private static TextView connectStateTV;
	// income
	private ImageView rejectIncomingCall;
	private ImageView acceptIncomingCall;
	//2014-9-16 add by wlei 语音来电界面动态效果
		private LinearLayout bg ;
		private FrameLayout ringA,photo;
		private boolean isRinging = true;
		private boolean isAcceptSelf = false;
		private ImageView image1, image2, image3;
		private Handler handler2 = new Handler() {

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

	private boolean isCallingIn;
	private View incomeControlView;
	public static String userNum = "--";
	public static String userName = "--";
	public static String ACTION_CHANGE_CALL_STATE = "com.zed3.sipua.ui.CallActivity.CALL_STATE";
	public static String NEWSTATE = "callState";
	private final String ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
	public static String ACTION_AMR_RATE_CHANGE = "com.zed3.sipua.ui.AMR_RATE_CHANGE";
	
//	boolean first;
	public static int mState;

	// public static final int UA_STATE_IDLE = 0; 对方挂断 进入空闲状态
	// public static final int UA_STATE_INCOMING_CALL = 1; 对方邀请
	// public static final int UA_STATE_OUTGOING_CALL = 2; 我方邀请
	// public static final int UA_STATE_INCALL = 3; 对方接听 进入通话状态
	// public static final int UA_STATE_HOLD = 4;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (intent.getAction().equals(ACTION_CHANGE_CALL_STATE)) {
				AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
				mState = extras.getInt(NEWSTATE);
				switch (mState) {
				case UserAgent.UA_STATE_INCALL:// 接通
					num = 0;//2014-10-22 modify by wlei
//					if(!isAcceptSelf){
//						closeRinging();
//					}
//					keyBoard2.setVisibility(View.VISIBLE);
//					endCallTT.setText(R.string.declineCall);
//					if(tableShow!=null) tableShow.setVisibility(View.VISIBLE);// add by wlei 2014-9-29 修改语音呼叫界面
					if(callTime != null){
						callTime.setBase(SystemClock.elapsedRealtime());
//						callTime.setVisibility(View.GONE);
					}
					CallUtil.mCallBeginTime = System.currentTimeMillis();
					CallUtil.mCallBeginTime2 = System.currentTimeMillis();
					//Add by zzhan 2013-5-17
					if (RtpStreamReceiver_signal.speakermode == AudioManager.MODE_IN_CALL)
						setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			
					setText4ConnectStateView(mState);
					hideConnectStateView(2000);
					setControlViewsVisible();
//					callTime2.setVisibility(View.VISIBLE);
					
					//Delete by zzhan 2013-5-8
					/*
					// 扬声器是否开启
					// add by oumogang 2013-05-07
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(500);
								if (isLoudspeakerOn) {
									RtpStreamReceiver.mAudioManager.setSpeakerphoneOn(true);
								} else {
//									Receiver.engine(CallActivity.this).speaker(
//											AudioManager.MODE_IN_CALL);
									RtpStreamReceiver.mAudioManager.setSpeakerphoneOn(false);
								}

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}).start();
					*/
					Message obtainMessage = handler.obtainMessage();
					obtainMessage.what = HIDECONNECTVIEW;
//					handler.sendMessageDelayed(obtainMessage, 2000);
                    handler.sendMessage(obtainMessage);
					break;
				case UserAgent.UA_STATE_IDLE:// 挂断
                    keyBoard2.setVisibility(View.INVISIBLE);
                    callTime2.setVisibility(View.INVISIBLE);
					if (callTime != null) {
						callTime.stop();
						callTime = null;
					}
					setText4ConnectStateView(mState);
					if (Receiver.mSipdroidEngine != null) {
						Receiver./*engine(mContext)*/mSipdroidEngine.isMakeVideoCall = -1;
					}
					MemoryMg.SdpPtime=0;
					//callTime = null;
					
					reSetControlStates();
					// 结束当前界面  
					finish();

					break;
				case UserAgent.UA_STATE_HOLD:// 等待

					break;
				case UserAgent.UA_STATE_INCOMING_CALL:

					break;
				case UserAgent.UA_STATE_OUTGOING_CALL:
					
					break;

				default:
					break;
				}

			}
			else if (intent.getAction().equals(ACTION_CALL_END)) {
				if (Receiver.call_state == UserAgent.UA_STATE_IDLE) {
					
					if (callTime != null) {
						callTime.stop();
						callTime = null;
					}
					setText4ConnectStateView(mState);
					
//					Receiver.engine(mContext).isMakeVideoCall = -1;
					if (Receiver.mSipdroidEngine != null) {
						Receiver./*engine(mContext)*/mSipdroidEngine.isMakeVideoCall = -1;
					}
					MemoryMg.SdpPtime=0;
					//callTime = null;
					
					reSetControlStates();
					// 结束当前界面  
					finish();
				}
				
			}else if (intent.getAction().equals(AudioUtil.ACTION_STREAM_CHANGED)) {
				int stream = extras.getInt(AudioUtil.KEY_STREAM_INT);
				switch (stream) {
				case AudioManager.STREAM_MUSIC:
					setVolumeControlStream(AudioManager.STREAM_MUSIC);
					break;
				case AudioManager.STREAM_VOICE_CALL:
					setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
					break;

				default:
					break;
				}
			}else if (intent.getAction().equals(AudioUtil.ACTION_SPEAKERPHONE_STATE_CHANGED)) {
				isLoudspeakerOn = AudioUtil.getInstance().isSpeakerphoneOn();
				loudspeakerOnBT.setVisibility(isLoudspeakerOn?View.VISIBLE:View.INVISIBLE);
				loudspeakerOffBT.setVisibility(isLoudspeakerOn?View.INVISIBLE:View.VISIBLE);
			}
		}
	};

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HIDECONNECTVIEW:
				hideConnectStateView(0);
				break;
			default:
				break;
			}
		}
	};
	private View keyBoard;
	private View keyBoard2;
	private static Context mContext;
	private View screenOffView;
	private View controlOverLayView;
	private View mRootView;
	private LinearLayout tableShow;

	void hideConnectStateView(int time) {
		if (time==0) {
//			connectStateTV.setVisibility(View.GONE);
			connectStateTV.setVisibility(View.INVISIBLE);//2014-9-15 wlei
		}else {
			Message obtainMessage = handler.obtainMessage();
			obtainMessage.what = HIDECONNECTVIEW;
			handler.sendMessageDelayed(obtainMessage, time);

		}
	}

	protected void retstartCallTime() {
		// TODO Auto-generated method stub
		callTime.stop();
		callTime = null;
		callTime = (Chronometer) findViewById(R.id.call_time);
		if (callTime != null) {
			callTime.start();
		}
	}

	public static void resetCallParams(){
		reSetControlStates();
	}
	
	//add by oumogang 2013-05-06
	protected static void reSetControlStates() {
		// TODO Auto-generated method stub
		isKeyBoardShow = false;
		isLoudspeakerOn = false;
		isMuteOn = false;
		AntaCallUtil.reInit();
//		CallUtil.reInit();
		CallUtil.mCallBeginTime = 0;
	}

	protected void hideControlDisplayView() {
		// TODO Auto-generated method stub
		controlOverLayView.setVisibility(View.GONE);
	}

	void showConnectStateView() {
		connectStateTV.setVisibility(View.VISIBLE);
	}

	/**
	 * 
	 */
	private boolean numTxtCursor;
	public  Chronometer callTime;
	public TextView callTime2;
	private int num = 0;
	private KeyguardLock mKeyguardLock;
	
//	private View mKeyboarControlRootView;
//	private View mShowKeyboarView;
//	private View mHideKeyboarView;
//	private View mKeyboarRootView;
	boolean isIncomingcall;
	private String mScreanWakeLockKey = TAG;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Zed3Log.debug("testcrash", "CallActivity#onCreate() enter");
		super.onCreate(savedInstanceState);
		mContext = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//唤醒屏幕
		mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen(TAG);
		//来电解锁
		MyWindowManager.getInstance().disableKeyguard(this);
		Intent intent = getIntent();
		
		Bundle extras = intent.getExtras();
		 
		isIncomingcall = intent.getBooleanExtra("isCallingIn", true);
		if(!isIncomingcall){
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		}else{
			setVolumeControlStream(AudioManager.STREAM_RING);
		}
		// modify by oumogang 2013-05-16
		// name add by oumogang 2013-05-05
		userNum = CallUtil.mNumber;
		userName = CallUtil.mName;

		mRootView = getLayoutInflater().inflate(R.layout.call_out_ui, null);
		setContentView(mRootView);
		//2014-9-15 add by wlei 
		image1 = (ImageView) findViewById(R.id.image1);
		image2 = (ImageView) findViewById(R.id.image2);
		image3 = (ImageView) findViewById(R.id.image3);
		photo = (FrameLayout) findViewById(R.id.photoUser);
		ringA = (FrameLayout) findViewById(R.id.callOut);
		callTime = (Chronometer) findViewById(R.id.call_time);
		callTime2 = (TextView) findViewById(R.id.call_time2);
		findViewsAndSetListener(mRootView);
		yincang = mRootView.findViewById(R.id.yincang);
		
		yincang.setVisibility(View.GONE);
		Handler callTimeHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (callTimeTV != null) {
					callTimeTV.setText((String) msg.obj);
				}
			};
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CHANGE_CALL_STATE);
		filter.addAction(ACTION_CALL_END);
		filter.addAction(AudioUtil.ACTION_STREAM_CHANGED);
		filter.addAction(AudioUtil.ACTION_SPEAKERPHONE_STATE_CHANGED);
		registerReceiver(receiver, filter);
		// 计时器
		//add by oumogang 2013-05-21
		//CallUtil.mCallBeginTim 为依据，以重新计时
//		callTime = (Chronometer) findViewById(R.id.call_time);
		//add by wlei 2014-9-15 修改语音通话界面时间显示
		callTime.setOnChronometerTickListener(new OnChronometerTickListener() {
			
			@Override
			public void onChronometerTick(Chronometer chronometer) {
				// TODO Auto-generated method stub
				int h = 00;
				int m = 00;
				int s = 00;
				s = s+num%60;
				m = m+(num-s)/60%60;
				h = h+(num-s-m*60)/3600;
				String ss = "";
				String mm = "";
				String hh = "";
				if(s<10){
					ss = "0"+s;
				}else{
					ss = ""+s;
				}
				if(m<10){
					mm = "0"+m;
				}else{
					mm = ""+m;
				}
				if(h<10){
					hh = "0"+h;
				}else{
					hh = ""+h;
				}
				callTime2.setText(hh+":"+mm+":"+ss);
				num++;
			}
		});
		callTime.start();
		long time = 0;
		long currentTimeMillis = System.currentTimeMillis();
		if (CallUtil.mCallBeginTime == 0) {
			CallUtil.mCallBeginTime = currentTimeMillis;
		}else {
			time = currentTimeMillis - CallUtil.mCallBeginTime;
		}
		callTime.setBase(SystemClock.elapsedRealtime()-time);
//		setVolumeControlStream(AudioManager.STREAM_RING);
		Zed3Log.debug("testcrash", "CallActivity#onCreate() exit");
	}

	private void findViewsAndSetListener(View mRootView) {
		// TODO Auto-generated method stub
		// message of user
		userNameTV = (TextView) mRootView.findViewById(R.id.user_name);
		userNumberTV = (TextView) mRootView.findViewById(R.id.user_number);
		
//		if (userNum!=null) {
//			if (userNum.split(" ").length>1) {
//				userNumberTV.setText(userNum);
//				userNumberTV.setText(userNum);
//			}else {
//				userNumberTV.setText(userNum+(AntaCallUtil.isAntaCall?"(会议/广播)":""));
//			}
//		}
		if (AntaCallUtil.isAntaCall ) {
			if (Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL) {
//				userNumberTV.setText(AntaCallUtil.isIsGroupBroadcast()?"广播":"会议");
				userNumberTV.setVisibility(View.INVISIBLE);
//				userNameTV.setText(AntaCallUtil.isIsGroupBroadcast()?"广播":"会议");
				userName = AntaCallUtil.isIsGroupBroadcast()?getResources().getString(R.string.broadcast):getResources().getString(R.string.conference);
			}else {
				//(会议/广播) add by oumogang 2014-01-09
				userName += AntaCallUtil.isAntaCall?getResources().getString(R.string.con_bro):"";
			}
			((ImageView) findViewById(R.id.user_photo)).setImageResource(R.drawable.picture_unknown_anta);
		}
		if(userNum.equals(DeviceInfo.svpnumber.trim())){
			userNameTV.setText(R.string.console);
			userNumberTV.setText(userNum);
		}else{
			userNameTV.setText(userName);
			userNumberTV.setText(userNum);
			if (userNum != null && userNum.equals(userName)) {
				userNumberTV.setVisibility(View.INVISIBLE);
			}
		}
		//add by oumogang 2013-05-08
		//隐藏
		
		userPhotoIV = (ImageView) mRootView.findViewById(R.id.user_photo);
		userPhotoIV.setOnClickListener(this);

		connectStateTV = (TextView) mRootView.findViewById(R.id.connect_state);
		setText4ConnectStateView(Receiver.call_state);

		// end call
		endCallBT = (ImageView) findViewById(R.id.end_call);
		endCallBT.setOnClickListener(this);
		endCallBT2 = (ImageView) findViewById(R.id.end_call2);
		endCallBT2.setOnClickListener(this);
		endCallTT = (TextView) findViewById(R.id.end_call2_text);
		// menu
		keyboardShowBT = (ImageView) findViewById(R.id.keyboard_show);
		keyboardShowBT.setOnClickListener(this);
		keyboardHideBT = (ImageView) findViewById(R.id.keyboard_hide);
		keyboardHideBT.setOnClickListener(this);
		
		loudspeakerOnBT = (RelativeLayout) findViewById(R.id.loudspeaker_on);
		loudspeakerOnBT.setOnClickListener(this);
		loudspeakerOffBT = (RelativeLayout) findViewById(R.id.loudspeaker_off);
		loudspeakerOffBT.setOnClickListener(this);
		
		forbidSoundOutOnBT = (RelativeLayout) findViewById(R.id.forbid_sound_out_on);
		forbidSoundOutOnBT.setOnClickListener(this);
		forbidSoundOutOffBT = (RelativeLayout) findViewById(R.id.forbid_sound_out_off);
		forbidSoundOutOffBT.setOnClickListener(this);
		
//		newCallBT = (ImageView) findViewById(R.id.newcall);
//		newCallBT.setOnClickListener(this);

		// income ui
		incomeControlView = findViewById(R.id.income_control_layout);
		if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
			acceptIncomingCall = (ImageView) findViewById(R.id.accept_call);
			incomeControlView.setVisibility(View.VISIBLE);
			acceptIncomingCall.setOnClickListener(this);
		}

		// keboard
		keyBoard = findViewById(R.id.keyboard_layout);
		keyBoard.setVisibility(View.INVISIBLE);
		keyBoard2 = findViewById(R.id.keyboard_layout_1);
		keyBoard.setVisibility(View.INVISIBLE);
		// line for show the control select
		lineKeyboard = findViewById(R.id.line_keyboard);
		lineKeyboard.setVisibility(View.INVISIBLE);

		lineLoudspeaker = findViewById(R.id.line_loudspeaker);
		lineLoudspeaker.setVisibility(View.INVISIBLE);

		lineForbidSoundOut = findViewById(R.id.line_forbid_sound_out);
		lineForbidSoundOut.setVisibility(View.INVISIBLE);

//		lineNewCall = findViewById(R.id.line_newcall);
//		lineNewCall.setVisibility(View.INVISIBLE);
		
		initKeyBoard();
		
		screenOffView = findViewById(R.id.screen_off_view);
		screenOffView.setOnClickListener(this);
	
		incomeControlView = findViewById(R.id.income_control_layout);
		acceptIncomingCall = (ImageView) findViewById(R.id.accept_call);
		
		//重新初始化控制界面
		setControlViewsVisible();
	}

	private void setControlViewsVisible() {
		// TODO Auto-generated method stub
		switch (Receiver.call_state) {
		case UserAgent.UA_STATE_INCOMING_CALL:
			incomeControlView.setVisibility(View.VISIBLE);
			acceptIncomingCall = (ImageView) findViewById(R.id.accept_call);
			acceptIncomingCall.setOnClickListener(this);
			//2014-9-16 add by wlei 语音来电呼入动态效果
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
					handler2.sendMessage(msg);
				}
				}
			}).start();
			
			break;
		case UserAgent.UA_STATE_OUTGOING_CALL:
			tableShow = (LinearLayout) this.findViewById(R.id.table_show);//modify by wlei 2014-9-29
			tableShow.setVisibility(View.GONE);
			incomeControlView.setVisibility(View.GONE);//2-14-8-26 GQT英文版
			keyboardShowBT.setVisibility(View.INVISIBLE);//2014-8-26 GQT英文版
			endCallTT.setText(R.string.cancel);//2014-8-26 GQT英文版
			keyboardHideBT.setVisibility(View.INVISIBLE);
			loudspeakerOnBT.setVisibility(View.INVISIBLE);
			loudspeakerOffBT.setVisibility(View.INVISIBLE);
			forbidSoundOutOnBT.setVisibility(View.INVISIBLE);
			forbidSoundOutOffBT.setVisibility(View.INVISIBLE);
			
			break;

		default:
			//hide imcome control view,add by oumogang 2014-05-05
			//修改通过手咪接听电话后接听按键不消失的bug。
			incomeControlView.setVisibility(View.GONE);//2014-8-28 GQT 英文版
			keyboardShowBT.setVisibility(isKeyBoardShow?View.INVISIBLE:View.VISIBLE);
			keyboardHideBT.setVisibility(isKeyBoardShow?View.VISIBLE:View.INVISIBLE);
			loudspeakerOnBT.setVisibility(isLoudspeakerOn?View.VISIBLE:View.INVISIBLE);
			loudspeakerOffBT.setVisibility(isLoudspeakerOn?View.INVISIBLE:View.VISIBLE);
			forbidSoundOutOnBT.setVisibility(isMuteOn?View.VISIBLE:View.INVISIBLE);
			forbidSoundOutOffBT.setVisibility(isMuteOn?View.INVISIBLE:View.VISIBLE);
			
			keyBoard.setVisibility(isKeyBoardShow?View.VISIBLE:View.INVISIBLE);
			//add by wlei 2014-10-20 解决语音通话界面，点击返回键退出，重新进入之后，页面显示不正确的问题
			if(!isAcceptSelf){
				closeRinging();
			}
			keyBoard2.setVisibility(View.VISIBLE);
			endCallTT.setText(R.string.declineCall);
			if(tableShow!=null) tableShow.setVisibility(View.VISIBLE);
			callTime2.setVisibility(View.VISIBLE);
			if(callTime!=null) callTime.setVisibility(View.GONE);
			break;
		}
		
		
		if (AntaCallUtil.isAntaCall) {
			keyboardShowBT.setVisibility(View.INVISIBLE);
			keyboardHideBT.setVisibility(View.INVISIBLE);
		}
//		loudspeakerOnBT.setVisibility(View.INVISIBLE);
//		loudspeakerOffBT.setVisibility(View.INVISIBLE);
		
		
		boolean headSetEnabled = ZMBluetoothManager.getInstance().isHeadSetEnabled();
		if (headSetEnabled) {
			loudspeakerOnBT.setVisibility(View.GONE);
			loudspeakerOffBT.setVisibility(View.GONE);
		}
	}
	
	private void setText4ConnectStateView(int call_state) {
		// TODO Auto-generated method stub
		hideConnectStateView(0);
		switch (call_state) {
		case UserAgent.UA_STATE_HOLD:
			connectStateTV.setText(R.string.audio_hold);
			showConnectStateView();

			break;
		case UserAgent.UA_STATE_IDLE:
			connectStateTV.setText(R.string.audio_ending);
			showConnectStateView();

			break;
		case UserAgent.UA_STATE_INCALL:
			connectStateTV.setText(R.string.audio_incom);
			showConnectStateView();
			hideConnectStateView(2000);

			break;
		case UserAgent.UA_STATE_INCOMING_CALL:
			connectStateTV.setText(R.string.audio_incoming);
			showConnectStateView();

			break;
		case UserAgent.UA_STATE_OUTGOING_CALL:
			connectStateTV.setText(R.string.audio_calling);
			showConnectStateView();

			break;
		default:
			break;
		}
	}

	private void initKeyBoard() {
		// TODO Auto-generated method stub
		numTxt = (EditText) findViewById(R.id.p_digits);
		numTxt.setText("");
		//add by oumogang 2013-05-20
		numTxt.setCursorVisible(false);
		numTxtCursor = false;
		numTxt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 设置光标为可见状态
				numTxt.setGravity(Gravity.CENTER);//GQT英文版
				numTxt.setCursorVisible(true);
				numTxtCursor = true;
			}
		});
		
//		numTxt.setSingleLine(true);
		
		//add by oumogang 2013-05-20
		//设置文本框输入字数上限；              
		numTxt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1000) });
		numTxt.setDrawingCacheEnabled(true);
		//
		btnjing = (ImageButton) mRootView.findViewById(R.id.pjing);
		btnjing.setOnClickListener(this);
		btnone = (ImageButton) mRootView.findViewById(R.id.pone);
		btnone.setOnClickListener(this);
		//
		btntwo = (ImageButton) mRootView.findViewById(R.id.ptwo);
		btntwo.setOnClickListener(this);
		//
		btnthree = (ImageButton) mRootView.findViewById(R.id.pthree);
		btnthree.setOnClickListener(this);
		//
		btnfour = (ImageButton) mRootView.findViewById(R.id.pfour);
		btnfour.setOnClickListener(this);
		//
		btnfive = (ImageButton) mRootView.findViewById(R.id.pfive);
		btnfive.setOnClickListener(this);
		//
		btnsix = (ImageButton) mRootView.findViewById(R.id.psix);
		btnsix.setOnClickListener(this);
		//
		btnseven = (ImageButton) mRootView.findViewById(R.id.pseven);
		btnseven.setOnClickListener(this);
		//
		btnenight = (ImageButton) mRootView.findViewById(R.id.penight);
		btnenight.setOnClickListener(this);
		//
		btnnine = (ImageButton) mRootView.findViewById(R.id.pnine);
		btnnine.setOnClickListener(this);
		//
		btn0 = (ImageButton) mRootView.findViewById(R.id.p0);
		btn0.setOnClickListener(this);
		//
		btnmi = (ImageButton) mRootView.findViewById(R.id.pmi);
		btnmi.setOnClickListener(this);
		
		// 删除
		btndel = (ImageButton) mRootView.findViewById(R.id.pdel);
		btndel.setOnClickListener(this);
		//add by oumogang 2013-07-16
		btndel.setOnLongClickListener(this);
		
		
		//n8000键盘
//		mKeyboarControlRootView = findViewById(R.id.keyboard_control_root);
//		mShowKeyboarView = mKeyboarControlRootView.findViewById(R.id.show_keyboard);
//		mHideKeyboarView  = mKeyboarControlRootView.findViewById(R.id.hide_keyboard);
//		mKeyboarRootView = findViewById(R.id.keyboard_root);
		
//		findViewById(R.id.audio_call).setVisibility(View.GONE);
//		findViewById(R.id.video_call).setVisibility(View.GONE);
//		mKeyboarControlRootView.setVisibility(View.GONE);
		
		
		InitTones();
	}
	
	// 按钮事件触发手动调用此方法
	public void downKey(String key)

	{
		numTxt.setGravity(Gravity.CENTER);
		// 设置一个变量判断是否有光标
		if (numTxtCursor == true) {
			// 获得光标的位置
			int index = numTxt.getSelectionStart();
			// 将字符串转换为StringBuffer
			StringBuffer sb = new StringBuffer(numTxt.getText().toString()
					.trim());
			// 将字符插入光标所在的位置
			sb = sb.insert(index, key);
			numTxt.setText(sb.toString());
			// 设置光标的位置保持不变
			Selection.setSelection(numTxt.getText(), index + 1);
		} else {
			numTxt.setText(numTxt.getText().toString().trim() + key);
		}
		// 手机振动
		toVibrate();
	}

	private void toVibrate() {
		// TODO Auto-generated method stub

	}
	Thread t;
	boolean running=false;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock = new Object();// 监视器对象锁
	private boolean mDTMFToneEnabled; // 按键操作音
	private static final int TONE_LENGTH_MS = 150;// 延迟时间

	private static CallTimer timer4Call;
	private View lineKeyboard;
	private View lineLoudspeaker;
	private View lineForbidSoundOut;
	private View lineNewCall;
	private boolean isResume;
	public static final HashMap<Character, Integer> mToneMap = new HashMap<Character, Integer>();

	private void InitTones() {
		mToneMap.put('1', ToneGenerator.TONE_DTMF_1);
		mToneMap.put('2', ToneGenerator.TONE_DTMF_2);
		mToneMap.put('3', ToneGenerator.TONE_DTMF_3);
		mToneMap.put('4', ToneGenerator.TONE_DTMF_4);
		mToneMap.put('5', ToneGenerator.TONE_DTMF_5);
		mToneMap.put('6', ToneGenerator.TONE_DTMF_6);
		mToneMap.put('7', ToneGenerator.TONE_DTMF_7);
		mToneMap.put('8', ToneGenerator.TONE_DTMF_8);
		mToneMap.put('9', ToneGenerator.TONE_DTMF_9);
		mToneMap.put('0', ToneGenerator.TONE_DTMF_0);
		mToneMap.put('#', ToneGenerator.TONE_DTMF_P);
		mToneMap.put('*', ToneGenerator.TONE_DTMF_S);
		mToneMap.put('d', ToneGenerator.TONE_DTMF_A);
		
		mDTMFToneEnabled = android.provider.Settings.System.getInt(
				mContext.getContentResolver(),
				android.provider.Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;// 获取系统参数“按键操作音”是否开启

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				try {
					mToneGenerator = new ToneGenerator(
							AudioManager.STREAM_MUSIC, 80);
					((Activity)mContext).setVolumeControlStream(AudioManager.STREAM_MUSIC);
				} catch (RuntimeException e) {
					Log.w("tag",
							"Exception caught while creating local tone generator: "
									+ e);
					mToneGenerator = null;
				}
			}
		}
	}

	void playTone(Character tone) {
		// TODO 播放按键声音
		if (!mDTMFToneEnabled) {
			return;
		}

		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int ringerMode = audioManager.getRingerMode();
		if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
				|| (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {// 静音或震动时不发出按键声音
			return;
		}

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				Log.w("tagdd", "playTone: mToneGenerator == null, tone: "
						+ tone);
				return;
			}
			mToneGenerator.startTone(mToneMap.get(tone), TONE_LENGTH_MS);// 发声TONE_LENGTH_MS
		}
	}


	@Override
	protected void onResume() {
		Zed3Log.debug("testcrash", "CallActivity#onResume() enter");
		// TODO Auto-generated method stub
		if(isPause||CallUtil.isDestory){
			int durtation = (int)(System.currentTimeMillis()-CallUtil.mCallBeginTime2);
			num=durtation/1000+1;
			isPause = false;
			CallUtil.isDestory = false;
		}
		// 解决语音呼叫来电，退出之后重新进入语音界面，来电动画显示过快的问题。 add by lwang 2014-10-29
		isRinging = true;
		isResume = true;
		screenOffView.setVisibility(View.GONE);
		if (sensorManager == null) {
			sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		   //避免可能出现有的手机没有距离传感器 。 modify by lwang 2014-10-31
//			proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//			sensorManager.registerListener(this,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
		}
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null) {
			Toast.makeText(mContext,
					getResources().getString(R.string.unProximitySensor), 0)
					.show();
		} else {
			// 进入页面之后需要重新注册传感器。 modify by lwang 2014-10-30
			sensorManager.registerListener(this, proximitySensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
		
//		screenOff(true);
		//add by oumogang 2013-05-05
		//作为主叫，呼出界面的静音是否要去掉？而且扬声器不起作
		
		//add by oumogang 2013-07-17
		Receiver.engine(this);
		super.onResume();
		
		setControlViewsVisible();
		//hide speaker mode control when using bluetooth ,add by oumogang 2014-03-07
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance()!= null) {
			boolean headSetEnabled = ZMBluetoothManager.getInstance().isHeadSetEnabled();
			if (headSetEnabled) {
				loudspeakerOnBT.setVisibility(View.GONE);
				loudspeakerOffBT.setVisibility(View.GONE);
			}
		}
		AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
		if(!isIncomingcall){
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		}else{
			setVolumeControlStream(AudioManager.STREAM_RING);
		}
		
		if (t == null && Receiver.call_state != UserAgent.UA_STATE_IDLE) {
			numTxt.setText("");
			running = true;
			(t = new Thread() {
				public void run() {
					int len = 0;
					long time;
					// ToneGenerator tg = null;
					// if (Settings.System.getInt(getContentResolver(),
					// Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1)
					// tg = new ToneGenerator(AudioManager.STREAM_VOICE_CALL,
					// (int)(ToneGenerator.MAX_VOLUME*2*com.zed3.sipua.ui.Settings.getEarGain()));
					for (;;) {
						if (!running) {
							t = null;
							break;
						}
						if (len != numTxt.getText().length()) {
							time = SystemClock.elapsedRealtime();

							// if (tg != null)
							// tg.startTone(mToneMap.get(mDigits.getText().charAt(len)));
							if(numTxt.getText().length() >(len+1)){
								Receiver.engine(Receiver.mContext).info(
										numTxt.getText().charAt(len++), 250);
							}

							time = 250 - (SystemClock.elapsedRealtime() - time);

							try {
								if (time > 0)
									sleep(time);
							} catch (InterruptedException e) {
							}

							// if (tg != null)
							// tg.stopTone();

							try {
								if (running)
									sleep(250);
							} catch (InterruptedException e) {
							}
							continue;
						}
						// mHandler.sendEmptyMessage(MSG_TICK);
						try {
							sleep(1000);
						} catch (InterruptedException e) {
						}
					}
					// if (tg != null) tg.release();
				}
			}).start();
		}
		Zed3Log.debug("testcrash", "CallActivity#onResume() exit");
		// if call is idle before onresume ，should close notification and finish current activity。 add by mou 2014-11-05
		if (Receiver.isCallNotificationNeedClose()) {
			reSetControlStates();
			finish();
		}
	}
	
	
    private boolean isPause = false;
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
//		screenOff(false);
		LogUtil.makeLog(" CallActivity ", " onPause() "); 
		// 解决语音呼叫来电，退出之后重新进入语音界面，来电动画显示过快的问题。退出时，将动态效果关闭掉。 add by lwang 2014-10-29
		isRinging = false;
		isPause = true;
		isResume = false;
//		//当语音界面暂停的时候，将距离传感器注销。 add by lwang 2014-10-30
		if(sensorManager!=null){
			sensorManager.unregisterListener(this);
		}
		super.onPause();
	}

	final int SCREEN_OFF_TIMEOUT = 12000;

	int oldtimeout;
	SensorManager sensorManager;
	Sensor proximitySensor;
	private String CALL_STATE = "callstate";
	private boolean misAntaCall;
	static boolean isKeyBoardShow;
	static boolean isLoudspeakerOn;
	static boolean isMuteOn;
	
	void screenOff(boolean off) {
        ContentResolver cr = getContentResolver();
        
        if (proximitySensor != null)
        	return;
        if (off) {
        	if (oldtimeout == 0) {
        		oldtimeout = Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, 60000);
	        	Settings.System.putInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIMEOUT);
        	}
        } else {
        	if (oldtimeout == 0 && Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, 60000) == SCREEN_OFF_TIMEOUT)
        		oldtimeout = 60000;
        	if (oldtimeout != 0) {
	        	Settings.System.putInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, oldtimeout);
        		oldtimeout = 0;
        	}
        }
	}
	@Override
	public void onStop() {
		super.onStop();
		
//		mHandler.removeMessages(MSG_BACK);
		if (Receiver.call_state == UserAgent.UA_STATE_IDLE)
			finish();
		//dissable by houdongfeng 2013-11-28
//		sensorManager.unregisterListener(this);
//		started = false;
	}
	
	@Override
	public void onStart() {
		super.onStart();
//	    first = true;
	    pactive = false;
//	    sensorManager.registerListener(this,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
//	    started = true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Zed3Log.debug("testcrash", "CallActivity#onDestroy() enter");
		CallUtil.isDestory = true;
		running=false;
		releaseToneGenerator();
		//unregisterReceiver before mContext = null;
		unregisterReceiver(receiver);
		MyPowerManager.getInstance().releaseScreenWakeLock(mScreanWakeLockKey);
		MyWindowManager.getInstance().reenableKeyguard(this);
		mContext = null;
		super.onDestroy();
		Zed3Log.debug("testcrash", "CallActivity#onDestroy() exit");
	}
	
	private void releaseToneGenerator(){
		if(mToneGenerator!=null){
			try {
				mToneGenerator.release();
			} catch(Exception e) {
				if(e!=null) e.printStackTrace();
			} finally {
				mToneGenerator = null;
			}
		}
	}
	
	private boolean rejFlag=false;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// if call is idle , should close notification and finish current activity。 add by mou 2014-11-05
		if (Receiver.isCallNotificationNeedClose()) {
			reSetControlStates();
			finish();
		}
		switch (v.getId()) {
		case R.id.screen_off_view:
			return;
		case R.id.end_call:
			//add by oumogang 2013-05-10
			//快速拨打不在线的的用户，导致timer不停止；
//			timer4Call.stop();
			if (callTime != null) {
				callTime.stop();
				callTime = null;
			}
			
			if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
				callTime2.setVisibility(View.INVISIBLE);
				connectStateTV.setText(R.string.audio_ending);
				showConnectStateView();
			}
			if (Receiver.call_state == UserAgent.UA_STATE_INCALL
					|| Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL
					|| Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL
					) {
				if (!rejFlag) {
					rejFlag = true;
					CallUtil.rejectCall();
				}
			} 
			AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
			if(!isIncomingcall){
				setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			}else{
				setVolumeControlStream(AudioManager.STREAM_RING);
			}

			//add by oumogang 2013-05-17
			//主动挂断也需要及时恢复控制键默认状态；
			reSetControlStates();
			//如果是用户主动挂断，是可以直接结束当前activity的
			finish();
			break;
		case R.id.end_call2:
			//add by oumogang 2013-05-10
			//快速拨打不在线的的用户，导致timer不停止；
//			timer4Call.stop();
			if (callTime != null) {
				callTime.stop();
				callTime = null;
			}
			
			if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
				callTime2.setVisibility(View.INVISIBLE);
				connectStateTV.setText(R.string.audio_ending);
				showConnectStateView();
			}
			if (Receiver.call_state == UserAgent.UA_STATE_INCALL
					|| Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL
					|| Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL
					) {
				if (!rejFlag) {
					rejFlag = true;
					CallUtil.rejectCall();
				}
			} 
			AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
			if(!isIncomingcall){
				setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			}else{
				setVolumeControlStream(AudioManager.STREAM_RING);
			}

			//add by oumogang 2013-05-17
			//主动挂断也需要及时恢复控制键默认状态；
			reSetControlStates();
			//如果是用户主动挂断，是可以直接结束当前activity的
			finish();
			break;
		case R.id.accept_call:
			//2014-9-16 add by wlei
			isAcceptSelf = true;
			closeRinging();
			keyBoard2.setVisibility(View.VISIBLE);
			CallUtil.answerCall();
			AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
			if(!isIncomingcall){
				setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			}else{
				setVolumeControlStream(AudioManager.STREAM_RING);
			}
			incomeControlView.setVisibility(View.GONE);//2014-8-28 GQT英文版
			break;

		case R.id.keyboard_show:
			
			keyBoard.setVisibility(View.VISIBLE);
			keyBoard2.setVisibility(View.INVISIBLE);//2014-9-16 add by wlei
//			lineKeyboard.setVisibility(View.VISIBLE);
			keyboardShowBT.setVisibility(View.INVISIBLE);
			keyboardHideBT.setVisibility(View.VISIBLE);
			isKeyBoardShow = true;

			break;
		case R.id.keyboard_hide:
			keyBoard.setVisibility(View.INVISIBLE);
			keyBoard2.setVisibility(View.VISIBLE);//2014-9-16 add by wlei
//			lineKeyboard.setVisibility(View.INVISIBLE);
			keyboardHideBT.setVisibility(View.INVISIBLE);
			keyboardShowBT.setVisibility(View.VISIBLE);
			isKeyBoardShow = false;
			
			break;
		case R.id.loudspeaker_on:
			//只在通话中时处理扬声器 modify by liangzhang 2014-11-20
			if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
				// lineLoudspeaker.setVisibility(View.VISIBLE);
				loudspeakerOnBT.setVisibility(View.INVISIBLE);
				loudspeakerOffBT.setVisibility(View.VISIBLE);
				// use setAudioConnectMode() instead of speaker(),modify by oumoang 2014-03-07
				// Receiver.engine(this)
				// .speaker(AudioManager.MODE_IN_CALL);
				// AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
				// // setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
				// AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
				Receiver.engine(this).speaker(AudioManager.MODE_IN_CALL);
				isLoudspeakerOn = false;
				AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
			}
			break;
		case R.id.loudspeaker_off:
			//只在通话中时处理扬声器 modify by liangzhang 2014-11-20
			if (Receiver.call_state == UserAgent.UA_STATE_INCALL) {
				// lineLoudspeaker.setVisibility(View.INVISIBLE);
				loudspeakerOffBT.setVisibility(View.INVISIBLE);
				loudspeakerOnBT.setVisibility(View.VISIBLE);
				// use setAudioConnectMode() instead of speaker(),modify by oumoang 2014-03-07
				// Receiver.engine(this)
				// .speaker(AudioManager.MODE_NORMAL);
				// AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_SPEAKER);
				// AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
				Receiver.engine(this).speaker(AudioManager.MODE_NORMAL);
				isLoudspeakerOn = true;
				AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
			}
			break;
		case R.id.forbid_sound_out_on:
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Receiver.engine(mContext).togglemute();
				}
			}).start();
//			lineForbidSoundOut.setVisibility(View.VISIBLE);
			forbidSoundOutOnBT.setVisibility(View.INVISIBLE);
			forbidSoundOutOffBT.setVisibility(View.VISIBLE);
			isMuteOn = false;
			break;
		case R.id.forbid_sound_out_off:
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Receiver.engine(mContext).togglemute();
				}
			}).start();
//			lineForbidSoundOut.setVisibility(View.INVISIBLE);
			forbidSoundOutOffBT.setVisibility(View.INVISIBLE);
			forbidSoundOutOnBT.setVisibility(View.VISIBLE);
			isMuteOn = true;
			break;
			
		case R.id.pone :
//			numTxt.setText(numTxt.getText() + "1");
			downKey("1");
			playTone('1');
			break;
		case R.id.ptwo :
//			numTxt.setText(numTxt.getText() + "2");
			downKey("2");
			playTone('2');
			break;
		case R.id.pthree :
//			numTxt.setText(numTxt.getText() + "3");
			downKey("3");
			playTone('3');
			break;
		case R.id.pfour :
//			numTxt.setText(numTxt.getText() + "4");
			downKey("4");
			playTone('4');
			break;
		case R.id.pfive :
//			numTxt.setText(numTxt.getText() + "5");
			downKey("5");
			playTone('5');
			break;
		case R.id.psix :
//			numTxt.setText(numTxt.getText() + "6");
			downKey("6");
			playTone('6');
			break;
		case R.id.pseven :
//			numTxt.setText(numTxt.getText() + "7");
			downKey("7");
			playTone('7');
			break;
		case R.id.penight :
//			numTxt.setText(numTxt.getText() + "8");
			downKey("8");
			playTone('8');
			break;
		case R.id.pnine :
//			numTxt.setText(numTxt.getText() + "9");
			downKey("9");
			playTone('9');
			break;
			
		case R.id.p0 :
//			numTxt.setText(numTxt.getText() + "0");
			downKey("0");
			playTone('0');
			break;
			
		case R.id.pmi :
//			numTxt.setText(numTxt.getText() + "*");
			downKey("*");
			playTone('*');
			break;
			
		case R.id.pjing :
//			numTxt.setText(numTxt.getText() + "#");
			downKey("#");
			playTone('#');
			break;
		case R.id.pdel :
//			String str = numTxt.getText().toString();
//			if (str.length() > 1)
//				numTxt.setText(str.substring(0, str.length() - 1));
//			else
//				numTxt.setText("");
	 		delete();
			playTone('d');
			break;
			
		default:
			break;
		}

	}
	
	private void delete() {
		// TODO Auto-generated method stub
	
		 
		StringBuffer sb = new StringBuffer(numTxt.getText()
				.toString().trim());
		int index = 0;
		if (numTxtCursor == true) {
			index = numTxt.getSelectionStart();
			if (index > 0) {
				sb = sb.delete(index - 1, index);
			}
		} else {
			index = numTxt.length();
			if (index > 0) {
				sb = sb.delete(index - 1, index);
			}
		}
		numTxt.setText(sb.toString());
		if (index > 0) {
			Selection.setSelection(numTxt.getText(), index - 1);
		}
		if (numTxt.getText().toString().trim().length() <= 0) {
			numTxt.setCursorVisible(false);
			numTxtCursor = false;
			numTxt.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);//GQT英文版 2014-8-28
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	void setScreenBacklight(float a) {
        WindowManager.LayoutParams lp = getWindow().getAttributes(); 
        lp.screenBrightness = a; 
        getWindow().setAttributes(lp);		
	}

	static final float PROXIMITY_THRESHOLD = 5.0f;
	public static boolean pactive;
	
	@Override
	public void onSensorChanged(SensorEvent event) {
//		if (first) {
//			first = false;
//			return;
//		}
		LogUtil.makeLog(" CallActivity ", " onSensorChanged is happened !..");
		float distance = event.values[0];
        boolean active = (distance >= 0.0 && distance < PROXIMITY_THRESHOLD && distance < event.sensor.getMaximumRange());
        pactive = active;
//        setScreenBacklight((float) (active?0.1:-1));
        screenOffView.setVisibility(active?View.VISIBLE:View.GONE);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt(CALL_STATE , mState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		//add by oumogang 2013-07-16
		case R.id.pdel :
			numTxt.setText("");
			break;
		default:
			break;
		}
		return false;
	}

	public static void setSpeakerPhoneON() {
		// TODO Auto-generated method stub
		if (true) {//切换不了扬声器。
			isLoudspeakerOn = false;
			if (mContext != null) {
				loudspeakerOffBT.setVisibility(View.VISIBLE);
				loudspeakerOnBT.setVisibility(View.INVISIBLE);
			}
			return;
		}
		if (CallUtil.isInCall()) {
			if (!isLoudspeakerOn) {
//				if (Receiver.mSipdroidEngine == null) {
//				}
//				Receiver.engine(mContext==null?SipUAApp.mContext:mContext);
//				Receiver.mSipdroidEngine.speaker(AudioManager.MODE_NORMAL);
				Receiver.engine(mContext==null?SipUAApp.mContext:mContext)
				.speaker(AudioManager.MODE_NORMAL);
				isLoudspeakerOn = true;
				if (mContext != null) {
					loudspeakerOffBT.setVisibility(View.INVISIBLE);
					loudspeakerOnBT.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public static void setSpeakerPhoneOFF() {
		// TODO Auto-generated method stub
		if (CallUtil.isInCall()) {
			if (isLoudspeakerOn) {
//				if (Receiver.mSipdroidEngine == null) {
//				}
				Receiver.engine(mContext==null?SipUAApp.mContext:mContext);
				Receiver.mSipdroidEngine.speaker(AudioManager.MODE_IN_CALL);
				isLoudspeakerOn = false;
				if (mContext != null) {
					loudspeakerOffBT.setVisibility(View.VISIBLE);
					loudspeakerOnBT.setVisibility(View.INVISIBLE);
				}
			}
		}
	}
    //add by wlei 2014-10-16 关闭语音来电动画
	private void closeRinging(){
		isRinging = false;
		ringA.setVisibility(View.GONE);
		photo.setVisibility(View.VISIBLE);
	}
	
	
}
