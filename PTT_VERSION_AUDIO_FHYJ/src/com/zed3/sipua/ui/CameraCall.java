package com.zed3.sipua.ui;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.zoolu.tools.MyLog;

import sinofloat.wvp.core.NativeH264Encoder;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.video.utils.IVideoSizeChange;
import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.h264_fu_process.RtpStack;
import com.zed3.location.MemoryMg;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.net.util.NetChecker;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SyncBufferSendQueue;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.phone.CallerInfo;
import com.zed3.sipua.phone.CallerInfoAsyncQuery;
import com.zed3.sipua.phone.ContactsAsyncHelper;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.LanguageChange;
import com.zed3.utils.Tools;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.EncoderBufferQueue;
import com.zed3.video.PhoneSupportTest;
import com.zed3.video.SensorCheckService;
import com.zed3.video.VideoUtils;
import com.zed3.videocodec.IFrameListener;
import com.zed3.window.MyWindowManager;

public class CameraCall extends BaseActivity implements OnClickListener,
		CallerInfoAsyncQuery.OnQueryCompleteListener,
		ContactsAsyncHelper.OnImageLoadCompleteListener, IFrameListener,
		IVideoSizeChange{
	EncoderBufferQueue equeue = null;
	Thread mEncodeOutThread = null;
	encodeOutSendRunnable runable = null;
	private int defaultValue = 360;//dip hor:width;ver:height
	boolean isLocalRemoteChanged = false;
	long timestamp;
	int iframe = 0;// i帧帧率
	int frame = 0;// 帧率
	int netrate = 0;// 码率
	byte[] byteBuffer1;
	byte[] byteBuffer2;
	int curAngle = 0;
	private static final String TAG = "CameraCall";
	// 关闭通话页面的广播
	private final String ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
	private final String ACTION_SINGLE_2_GROUP = "com.zed3.sipua.ui_groupcall.single_2_group";
	public final String ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
	ToneGenerator toneGenerator = null;
	Timer timer;
	private IntentFilter mFilter;
	private BroadcastReceiver quitRecv2 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (intent.getAction().equalsIgnoreCase(ACTION_CALL_END)) {
				if (Receiver.call_state == UserAgent.UA_STATE_IDLE) {
					endCameraCall();
					MyLog.e(TAG, "broadcast to finish");
				}
			} else if (intent.getAction().equalsIgnoreCase(ACTION_3GFlow_ALARM)) {
				Tools.FlowAlertDialog(CameraCall.this);
			}else if (intent.getAction().equalsIgnoreCase(
					ACTION_SINGLE_2_GROUP/* ACTION_CAMERA_PTT_DIALOG */)) {
				
				Bundle bundle = intent.getExtras();
				GroupCallUtil.setTalkGrp(bundle.getString("0"));
				GroupCallUtil.setActionMode(ACTION_SINGLE_2_GROUP);
				
				GrpCallNotify.startSelf(intent);
				
				//wei.deng 2014-11-25 在视频通话时，有组的呼叫的提示，使用activity来提示，防止调度台挂断视频导致当前对讲组为关闭状态，别人讲话听不到
				// 提示Dialog
//				String grpId = /* SipUAApp.isLowSdk ? */GroupCallUtil
//						.getTalkGrp() /* : GroupCall.getTalkGrp() */;
//				pttGrp = Receiver.GetCurUA().GetGrpByID(grpId);
//				String name = pttGrp.grpName;
//
//				final AlertDialog dlg = new AlertDialog.Builder(CameraCall.this)
//						.create();
//				dlg.show();
//				Window window = dlg.getWindow();
//				window.setContentView(R.layout.shrew_pttdialog);
//				TextView txt = (TextView) window.findViewById(R.id.dialogtxt);
////				txt.setText(name + "正在呼叫，是否切换到对讲组？");
//				//modify by wlei 2014-9-29 修改自动换行的时候，英文单词分割的问题
//				if(mContext == null){
//					mContext = CameraCall.this;
//				}
//				int width = (int)(mContext.getResources().getDisplayMetrics().density*284+0.5f);
//				String message = DialogMessageTool.getString(width,txt.getTextSize(), name+" "+getResources().getString(R.string.notify_message_text));
////				String message = name+" "+getResources().getString(R.string.notify_message_text);
//				txt.setText(message);
//				TextView ok = (TextView) window.findViewById(R.id.btn_ok);
//				ok.setOnClickListener(new View.OnClickListener() {
//					public void onClick(View v) {
//						dlg.dismiss();
//
//						if (pttGrp != null) {
//							Receiver.GetCurUA().hangupWithoutRejoin();
//							Receiver.GetCurUA().answerGroupCall(pttGrp);
//							// 是否需要更新组列表
//							sendBroadcast(new Intent(
//									Contants.ACTION_CURRENT_GROUP_CHANGED));
//
//							if (toneGenerator != null) {
//								toneGenerator.stopTone(); // 停止播放
//								toneGenerator.release();
//								toneGenerator = null;
//							}
//						}
//
//					}
//				});
//				TextView cancel = (TextView) window
//						.findViewById(R.id.btn_cancel);
//				cancel.setOnClickListener(new View.OnClickListener() {
//					public void onClick(View v) {
//						if (pttGrp != null)
//							Receiver.GetCurUA().grouphangup(pttGrp);
//
//						if (toneGenerator != null) {
//							toneGenerator.stopTone(); // 停止播放
//							toneGenerator.release();
//							toneGenerator = null;
//						}
//
//						dlg.cancel();
//					}
//				});
//
//				try {
//					toneGenerator = new ToneGenerator(
//							AudioManager.STREAM_MUSIC, 100);
//				} catch (RuntimeException e) {
//					toneGenerator = null;
//				}
//
//				if (toneGenerator != null) {
//					toneGenerator
//							.startTone(ToneGenerator.TONE_SUP_CALL_WAITING); // 开始播放，呼叫等待类型提示音
//					try {
//						Thread.sleep(1000); // 播放时间控制，1秒钟
//					} catch (InterruptedException e) {
//					}
//				}

			}else if(intent.getAction().equalsIgnoreCase(
					DeviceVideoInfo.ACTION_RESTART_CAMERA)){
				if(DeviceVideoInfo.onlyCameraRotate){
					if(DeviceVideoInfo.isHorizontal){
						if(mCameraDevice != null){
							if(isFrontCamera){
								mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle+90)%360);
							}else{
								if(DeviceVideoInfo.curAngle == 0 ||DeviceVideoInfo.curAngle == 180){
									mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle+270)%360);
								}else{
									mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle+90)%360);
								}
							}
						}
					}else {
						if(mCameraDevice != null){
							if(isFrontCamera){
								mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle+90)%360);
							}else{
								if(DeviceVideoInfo.curAngle == 0 ||DeviceVideoInfo.curAngle == 180){
									mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle+90)%360);
								}else{
									mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle+270)%360);
								}
							}
						}
					}
				}else{
					encoderChanging = true;
					releaseEncoder();
					curAngle = DeviceVideoInfo.curAngle;
					initMediaCodec();
					encoderChanging = false;
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
				isSpeakLoud = AudioUtil.getInstance().isSpeakerphoneOn();
				speakerbtn.setImageResource(isSpeakLoud?R.drawable.call_speaker_pressed:R.drawable.call_speaker_pressed0);
			}

		}
	};
	int width = 0;//
	int height = 0;//
	boolean encoderChanging = false;
	//

	int getEncodeWidth() {
		if(DeviceVideoInfo.isHorizontal){
			return width;
		}
		if (DeviceInfo.isSupportHWChange) {
			if (curAngle == 0 || curAngle == 180) {
				return width;
			}else{
				return height;
			}
		} else {
			if (curAngle == 0 || curAngle == 180) {
				return height;
			} else {
				return width;
			}
		}
	};

	int getEncodeHeight() {
		if(DeviceVideoInfo.isHorizontal){
			return height;
		}
		if (DeviceInfo.isSupportHWChange) {
			if (curAngle == 0
					|| curAngle == 180) {
				return height;
			} else {
				return width;
			}
		} else {
			if (curAngle == 0
					|| curAngle == 180) {
				return width;
			} else {
				return height;
			}
		}
	};

	int getCameraWidth() {
		if (DeviceInfo.isSupportHWChange) {
//			if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
			if(DeviceVideoInfo.isHorizontal){
				return width;
			}else{
				return height;
			}
//			}
		}
		return width;
	};

	int getCameraHeight() {
		if (DeviceInfo.isSupportHWChange) {
//			if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
			if(DeviceVideoInfo.isHorizontal){
				return height;
			}else{
				return width;
			}
//			}
		}
		return height;
	};

	Context mContext = null;
	TextView alarmFlowNum = null;
	ProgressBar proBar = null;
	TextView callName = null;
	TextView callNum = null;
	TextView selTxt = null;
	LinearLayout topLinearLayout = null;
	RelativeLayout relatLayout = null;
	LinearLayout bottomBtnBar = null, closelinear = null, topBoard;// voiceLayout
																	// = null,
	PttGrp pttGrp = null;
	ImageView speakerbtn = null, mutebtn = null, stopvideobtn = null,flowlockbtn=null,
			chgvideobtn = null, rotatebtn = null;
	// 计时器
	// Chronometer mElapsedTime;
	// private int mTextColorConnected;
	// private int mTextColorEnded;
	private Chronometer mCallTime;//2014-9-9 显示通话时间   wlei
	// 本地视频发送 localcamera
	private Camera mCameraDevice;// ***
	private SurfaceHolder mSurfaceHolder = null;
	private SurfaceView localview;// 本地预览video
	private byte[] eData = null;
	private boolean prewRunning = false;
	private String pixTag = "", cameraval = "", videocode = "";
	private boolean recTcpFlag = false;
	private RtpStack rtpStack = null;// ***
	private SurfaceHolder localSurfaceHolder;

	RandomAccessFile raf = null;
	AudioManager audioManager = null;
	private int flagx = -1, widthPix = 0, heightPix = 0,
			cameraCurrLock = -1;
	private boolean farflag = false, whichCameraFlag = true,
			flowflag = false;
	Camera.Parameters localParameters;

	private SurfaceView remoteview;
	private LinearLayout /*mLoadProgress = null,*/ progressbarlinear = null;
	private boolean videoFlag = false,
			isShowViewFlag = false, isSpeakLoud = false, isMute = false,
			isChgVideo = false;
	private int rag = 0;
	private double progressval = 0;
	SyncBufferSendQueue sendSync = null;
	H264EncoderThread encoderThread = null;

	// 编码
	private void InitH264Encoder() {
		// 分辨率 5--QVGA 4--cif(352*288) 3--d1 2--720p
		if (pixTag.equals("5")) {// QVGA
			this.width = 320;
			this.height = 240;
		} else if (pixTag.equals("4")) {// CIF
			this.width = 352;
			this.height = 288;
		} else if (pixTag.equals("6")) {// VGA
			this.width = 640;
			this.height = 480;
		} else if (pixTag.equals("3")) {// d1
			this.width = 720;
			this.height = 480;
		} else if (pixTag.equals("7")) {// qcif
			this.width = 176;
			this.height = 144;
		} else if (pixTag.equals("8")) {// qcif
			this.width = 384;
			this.height = 288;
		}else if (pixTag.equals("9")) {// qcif
			this.width = 480;
			this.height = 320;
		}else {// 720p //2
			this.width = 1280;
			this.height = 720;
		} 

		// NativeH264Encoder.InitEncoder(this.width, this.height, h264EnFrame,
		// this.width * this.height);// 只能是一秒10帧???15帧可以么？？？
		byteBuffer1 = new byte[this.width * this.height * 3 / 2];
		byteBuffer2 = new byte[this.width * this.height * 3 / 2];
	}

	PreviewCallBack callback;
	boolean needChangeUV = false;
	private String mScreanWakeLockKey = TAG;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		SettingVideoSize.setDefaultValue(this);
		needChangeUV = needChangeUVinNV21();
//		if(DeviceVideoInfo.isConsole){
//			if(DeviceInfo.isSupportHWChange){
//				DeviceVideoInfo.isHorizontal = false;
//			}else{
//				DeviceVideoInfo.isHorizontal = true;
//			}
//		}else{
//			DeviceVideoInfo.isHorizontal = false;
//		}
		if(DeviceVideoInfo.isHorizontal){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//			MyToast.showToast(true, mContext, R.string.video_land_tip);
			curAngle = 270;
		}else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			curAngle = 0;
		}
		MyLog.e(TAG, "create begin" + System.currentTimeMillis());

		if (UserAgent.Camera_AudioPort.equals("")
				|| UserAgent.Camera_URL.equals("")
				|| UserAgent.Camera_VideoPort.equals("")) {
			//
			MyLog.e(TAG, "AudioPort Camera_URL VideoPort  null");
			MyToast.showToastInBg(true, this.getApplicationContext(),
					R.string.cameracall_startfail);
			NotificationManager mNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationMgr.cancel(2);
			finish();
			return;
		}
		if(Build.VERSION.SDK_INT >= 16){
			DeviceVideoInfo.supportColor = PhoneSupportTest.getEncodeSupportColor();
		}
		this.recTcpFlag = true;
		this.farflag = false;
		this.videoFlag = false;
		this.isShowViewFlag = false;
		isSpeakLoud = false;
		isMute = false;
		UserAgent.isCamerPttDialog = true;
		whichCameraFlag = true;
		// *********************
		if (flagx == -1)
			flagx = -1;// 前后摄像头
		// *********************
		//唤醒屏幕
		mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen(TAG);
		//来电解锁
		MyWindowManager.getInstance().disableKeyguard(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.cameracall_new);
		startService(new Intent(this,SensorCheckService.class));
		mCallTime = (Chronometer) findViewById(R.id.call_time);
		mCallTime.start();
//		long time = 0;
//		long currentTimeMillis = System.currentTimeMillis();
//		if (CallUtil.mCallBeginTime == 0) {
//			CallUtil.mCallBeginTime = currentTimeMillis;
//		}else {
//			time = currentTimeMillis - CallUtil.mCallBeginTime;
//		}
//		mCallTime.setBase(SystemClock.elapsedRealtime()-time);
		mContext = this;
		// ---视频界面---
		timer = new Timer();
		// 初始化视频界面组件...
		callName = (TextView) findViewById(R.id.vcallname);
		callNum = (TextView) findViewById(R.id.vcallnum);
		selTxt = (TextView) findViewById(R.id.selecttxt);
		// topLinearLayout = (LinearLayout) findViewById(R.id.toplinear);
		bottomBtnBar = (LinearLayout) findViewById(R.id.bottomBoard);
		topBoard = (LinearLayout) findViewById(R.id.topboard);
		speakerbtn = (ImageView) findViewById(R.id.speakerbtn);
		speakerbtn.setOnClickListener(this);
		mutebtn = (ImageView) findViewById(R.id.mutebtn);
		mutebtn.setOnClickListener(this);
		stopvideobtn = (ImageView) findViewById(R.id.stopvideobtn);
		stopvideobtn.setOnClickListener(this);
		chgvideobtn = (ImageView) findViewById(R.id.chgvideobtn);
		chgvideobtn.setOnClickListener(this);
		rotatebtn = (ImageView) findViewById(R.id.rotatebtn);
		rotatebtn.setOnClickListener(this);
		rotatebtn.setVisibility(View.GONE);
		flowlockbtn= (ImageView) findViewById(R.id.flowlockbtn);
		if(!DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS)
			flowlockbtn.setVisibility(View.GONE);
		flowlockbtn.setOnClickListener(this);
		closelinear = (LinearLayout) findViewById(R.id.closelinear);
		closelinear.setOnClickListener(this);

		cameraval = PreferenceManager.getDefaultSharedPreferences(this)
				.getString("usevideokey", "0");// 默认后置

		videocode = PreferenceManager.getDefaultSharedPreferences(this)
				.getString("videocode", "0");
		// 先判断前后置摄像头
		if (cameraval.equals("0")){
			pixTag = PreferenceManager.getDefaultSharedPreferences(this)// 后置
					.getString("videoresolutionkey0", "5");
			if((pixTag.equals("5")) && !MemoryMg.getInstance().SupportVideoSizeStr.contains("320*240")){
				pixTag = "6";
			}
			}
		else
			// 分辨率 5--QVGA 4--cif(352*288) 3--d1 2--720p
		{
			pixTag = PreferenceManager.getDefaultSharedPreferences(this)// 前置
					.getString("videoresolutionkey", "5");
			if((pixTag.equals("5")) && !MemoryMg.getInstance().SupportVideoSizeStr.contains("320*240")){
				pixTag = "6";
			}
		}
		boolean is3G = NetChecker.is3G(CameraCall.this);
		String add = is3G ? "3g" : "wifi";
		String[] info = PreferenceManager
				.getDefaultSharedPreferences(this)
				.getString(SettingVideoSize.getCurVideoSize(pixTag) + add,
						"1,10,300000").split(",");
		if (info.length == 3) {
			iframe = Integer.parseInt(info[0]);
			frame = Integer.parseInt(info[1]);
			netrate = Integer.parseInt(info[2]) * 1000;
		} else {
			iframe = 1;
			frame = 10;
			netrate = 300000;
		}
		if (iframe == 0 || frame == 0 || netrate == 0) {
			iframe = 1;
			frame = 10;
			netrate = 300000;
		}
		MyLog.e(TAG, "consult pix:" + pixTag + " videocode:" + videocode);

		remoteview = (SurfaceView) findViewById(R.id.bigvideoView);
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			widthPix = dm.widthPixels;
			heightPix = dm.heightPixels;
			if(DeviceVideoInfo.supportFullScreen){
			sizeChange(dm.widthPixels, dm.heightPixels);
			}
			RelativeLayout.LayoutParams remoteparam = (RelativeLayout.LayoutParams) remoteview.getLayoutParams();
			if(DeviceVideoInfo.isHorizontal){
				remoteparam.leftMargin = dm.widthPixels - (dm.widthPixels - dip2px(defaultValue))/2 -  dip2px(defaultValue);
				remoteparam.width = dip2px(defaultValue);
			}else{
				remoteparam.topMargin = dm.heightPixels - (dm.heightPixels - dip2px(defaultValue))/2 -  dip2px(defaultValue);
				remoteparam.height = dip2px(defaultValue);
			}
			
			remoteview.setLayoutParams(remoteparam);
			remoteview.setOnClickListener(this);
		remoteview.getHolder().addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				MyLog.e("surface", "show view destroyed 2222222222222");
				isSurfaceDestroyed = true;
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				MyLog.e("surface", "show view created 2222222222222");
				if (rtpStack != null && isSurfaceDestroyed) {
					rtpStack.resetDecode();
					isSurfaceDestroyed = false;
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				MyLog.e("surface", "show view changed. 2222222222222" + width
						+ " height = " + height);
				holder.setFixedSize(width, height);
			}
		});
//		mLoadProgress = (LinearLayout) findViewById(R.id.linear_loadprogress);// 加载布局
		alarmFlowNum = (TextView) findViewById(R.id.alarmnum);
		proBar = (ProgressBar) findViewById(R.id.probar);
		// 启动视频线程
		if (!UserAgent.Camera_VideoPort.equals("0")
		/* && MemoryMg.getInstance().isSdpH264s == false */) {
			if (rtpStack == null) {
				rtpStack = new RtpStack(remoteview, /*mLoadProgress*/null,
						CameraCall.this, this);
			}

			if (videocode.equals("0"))// h264
				InitH264Encoder();

			// if (encoderThread == null
			// && MemoryMg.getInstance().isSendOnly == false) {
			// 双线程：编码线程和发送线程
			// encoderThread = new H264EncoderThread();
			// // 视频编码
			// t1 = new Thread(encoderThread);
			// }
		}
		// if (cameraval.equals("0"))
		// 横向 后置
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// byteBuffer1 = new byte[this.width * this.height * 3 / 2];
		// byteBuffer2 = new byte[this.width * this.height * 3 / 2];

		MyLog.e(TAG, "widthPix:" + widthPix + " heightPix:" + heightPix);
		// 流量进度条
		progressbarlinear = (LinearLayout) findViewById(R.id.progressbarlinear);

		// 本地视频发送
		localview = (SurfaceView) findViewById(R.id.localvideoView);
		localview.getParent().bringChildToFront(localview);
		localview.setOnClickListener(this);
		if (!MemoryMg.getInstance().isSendOnly) {
			// 设置本地录制视频的宽度和高度
			int localWidth = 0;
			int tmpHeight = 0;//会重新计算
//			if(DeviceVideoInfo.isConsole){
//				localWidth = widthPix;
//				tmpHeight = heightPix;
//				mVideoPreview.getBackground().setAlpha(0);
//			}else{
				localWidth = px2dip(320);
				tmpHeight = px2dip(240);
				if (widthPix >= 1080 && heightPix >= 1080) {
					localWidth = 400;
					tmpHeight = 300;
				}
//				float rate = (getEncodeHeight() * 1.0f) / (getEncodeWidth() * 1.0f);
//				tmpHeight = (int) (localWidth * rate);
//			}
			// 设置本地录制视频的宽度和高度
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) localview
					.getLayoutParams();
			lp.width = localWidth;
			lp.height = tmpHeight;
			lp.leftMargin = widthPix - localWidth -dip2px(20);
			lp.topMargin = heightPix -tmpHeight - dip2px(20);
			/*if(DeviceVideoInfo.isHorizontal){
				lp.bottomMargin =60;
			}*/
			localview.setLayoutParams(lp);
			remoteview.getBackground().setAlpha(0);
			saveFrame();
			setRemoteSize();
			if(checkWindowSize()){
				localview.setLayoutParams(localLp);//改变本地窗口大小
			}
			// if(!TextUtils.isEmpty(MemoryMg.remoteResolution)){//mds 不支持 先不调整
			// double rate = Tools.getWidthHeightRate(CameraCall.this);
			// double picRate = getRate(MemoryMg.remoteResolution);
			// int newWidth = 0;
			// int newHeight = 0;
			// if(picRate>rate){
			// newWidth = Tools.getWidthHeight(CameraCall.this)[0];
			// newHeight = (int)(newWidth*1.0f/picRate);
			// }else{
			// newWidth
			// =(int)rate*getPicWidthHeight(MemoryMg.remoteResolution)[1];
			// newHeight = Tools.getWidthHeight(CameraCall.this)[1];
			// }
			// FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams)
			// mSingleView
			// .getLayoutParams();
			// lp2.width = newWidth;
			// lp2.height = newHeight;
			// mSingleView.setLayoutParams(lp2);
			// mSingleView.measure(newWidth, newHeight);
			// }

			localSurfaceHolder = localview.getHolder();
			// 屏蔽
			// mVideoPreview.setZOrderOnTop(true);
			callback = new PreviewCallBack();
			localSurfaceHolder.addCallback(callback);
		}// end if
		else// 视频转发需要向mds发空包
		{
			localview.setVisibility(View.GONE);
			saveFrame();
			remoteview.getBackground().setAlpha(0);
			if (rtpStack != null) {
				prewRunning = true;
				new Thread(new Runnable() {

					@Override
					public void run() {
						
						while (prewRunning) {
							rtpStack.SendEmptyPacket();

							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}).start();
			}
		}

		// 收听退出的广播
		mFilter = new IntentFilter();
		mFilter.addAction(ACTION_CALL_END);
		// mFilter.addAction(ACTION_CAMERA_PTT_DIALOG);
		mFilter.addAction(ACTION_SINGLE_2_GROUP);
		mFilter.addAction("com.zed3.siupa.ui.restartcamera");
		mFilter.addAction(AudioUtil.ACTION_STREAM_CHANGED);
		mFilter.addAction(AudioUtil.ACTION_SPEAKERPHONE_STATE_CHANGED);
		registerReceiver(quitRecv2, mFilter);
		if (MemoryMg.getInstance().isProgressBarTip) {
			progressbarlinear.setVisibility(View.GONE);
			if (MemoryMg.getInstance().User_3GTotal != 0) {
				if (hd.hasMessages(1))
					hd.removeMessages(1);
				hd.sendEmptyMessage(1);
			}
		} 
		else
			flowlockbtn.setVisibility(View.GONE);//流量按钮不可见
		if (RtpStreamReceiver_signal.speakermode == AudioManager.MODE_IN_CALL)
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

		MyLog.e(TAG, "create end:" + System.currentTimeMillis());
		MyLog.e(TAG, "oncreate");
		try {
			File file = new File("/sdcard/recordyuvcodec.3gp");
			raf = new RandomAccessFile(file, "rw");
		} catch (Exception ex) {
			Log.v("System.out", ex.toString());
		}
		// //init
		equeue = new EncoderBufferQueue();
		initMediaCodec();
//		mEncodeOutThread.start();
		localview.performClick();
		Receiver.engine(this).speaker(AudioManager.MODE_NORMAL);
		isSpeakLoud = true;
		speakerbtn.setImageResource(R.drawable.call_speaker_pressed);
	}


	private void setRemoteSize() {
		remoteLp.leftMargin = 0;
		remoteLp.topMargin = 0;
		if(DeviceVideoInfo.isHorizontal){
			remoteLp.height = heightPix;
			remoteLp.width = localLp.leftMargin;
		}else{
			remoteLp.width = widthPix;
			remoteLp.height = localLp.topMargin;
		}
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		MyLog.e("debug_h", "onresume called");
		MyLog.e(TAG, "onResume  begin " + System.currentTimeMillis());
		// 显示呼叫人及通话时间
		ShowCallNumAndTime();
		// hide speaker mode control when using bluetooth ,add by oumogang
		// 2014-03-07
		if (com.zed3.sipua.ui.Settings.mNeedBlueTooth
				&& ZMBluetoothManager.getInstance() != null) {
			boolean headSetEnabled = ZMBluetoothManager.getInstance()
					.isHeadSetEnabled();
			if (headSetEnabled) {
				speakerbtn.setVisibility(View.GONE);
			}
		} else
			speakerbtn.setVisibility(View.VISIBLE);

		MyLog.e(TAG, "onResume end" + System.currentTimeMillis());
		// setVolumeControlStream();
		AudioUtil.getInstance().setVolumeControlStream(CameraCall.this);
		// if call is idle before onresume ，should close notification and finish current activity。 add by mou 2014-11-05
		if (Receiver.isCallNotificationNeedClose()) {
			finish();
		}
	}

	/**
	 * setVolumeControlStream,back to the calling UI,reset VolumeControlStream
	 * add by oumogang 2014-03-07
	 */
	private void setVolumeControlStream() {
		
		switch (Receiver.call_state) {

		case UserAgent.UA_STATE_HOLD:

			break;
		case UserAgent.UA_STATE_IDLE:

			break;
		case UserAgent.UA_STATE_INCALL:
			// setVolumeControlStream(isLoudspeakerOn?AudioManager.STREAM_MUSIC:AudioManager.STREAM_VOICE_CALL);
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			break;
		case UserAgent.UA_STATE_INCOMING_CALL:
			setVolumeControlStream(AudioManager.STREAM_RING);
			break;
		case UserAgent.UA_STATE_OUTGOING_CALL:
			setVolumeControlStream(AudioManager.STREAM_SYSTEM);
			break;
		default:
			break;
		}
	}

	boolean eflag = false;

	@Override
	public void onClick(View v) {
		// if call is idle before onresume ，should close notification and finish current activity。 add by mou 2014-11-05
		if (Receiver.isCallNotificationNeedClose()) {
			finish();
		}
		ImageView pic;
		switch (v.getId()) {
		case R.id.speakerbtn:
			pic = (ImageView) v;
			if (isSpeakLoud) {
				Receiver.engine(this).speaker(AudioManager.MODE_IN_CALL);
				isSpeakLoud = false;
				pic.setImageResource(R.drawable.call_speaker_pressed0);
			} else {
				Receiver.engine(this).speaker(AudioManager.MODE_NORMAL);
				isSpeakLoud = true;
				pic.setImageResource(R.drawable.call_speaker_pressed);
			}

			if (getVolumeControlStream() != AudioManager.STREAM_VOICE_CALL) {
				setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			}
			break;
		case R.id.mutebtn:
			pic = (ImageView) v;
			Receiver.engine(CameraCall.this).togglemute();
			if (isMute) {
				isMute = false;
				pic.setImageResource(R.drawable.call_unmute0);
			} else {
				isMute = true;
				pic.setImageResource(R.drawable.call_unmute);
			}
			break;
		case R.id.chgvideobtn:
			pic = (ImageView) v;
			if (isChgVideo)// start
			{
				isChgVideo = false;
				pic.setImageResource(R.drawable.call_chgcamera0);
			} else// stop
			{
				isChgVideo = true;
				pic.setImageResource(R.drawable.call_chgcamera);
			}
			
			closeCamera();
			setCameraDataParas(true);
			startPreview(false);
			break;
		case R.id.stopvideobtn:
			pic = (ImageView) v;
			if (videoFlag)// start
			{
				videoFlag = false;
				farflag = false;
				pic.setImageResource(R.drawable.call_video_no0);
			} else// stop
			{
				videoFlag = true;
				farflag = true;
				pic.setImageResource(R.drawable.call_video_no);
			}
			break;
		case R.id.rotatebtn:
			pic = (ImageView) v;
			rag = rag + 90;
			switch (rag % 360) {
			case 0:
				pic.setImageResource(R.drawable.camera0);
				break;
			case 90:
				pic.setImageResource(R.drawable.camera90);
				break;
			case 180:
				pic.setImageResource(R.drawable.camera180);
				break;
			case 270:
				pic.setImageResource(R.drawable.camera270);
				break;
			}

			break;
		case R.id.flowlockbtn:
			if (flowflag == false) {
				flowflag = true;
			} else {
				flowflag = false;
			}
			break;
		case R.id.closelinear:
			exitDialog(CameraCall.this,
					getResources().getString(R.string.information),
					getResources().getString(R.string.end_vedio_notify));
			break;
		case R.id.localvideoView:
			if(!isLocalRemoteChanged){
				checkWindowSize();
				viewResize(remoteview/*, localL,localT,localR,localB*/);
				viewResize(localview/*,remoteL,remoteT,remoteR,remoteB*/);
				if(!MemoryMg.getInstance().isSendOnly){
					remoteview.getBackground().setAlpha(100);
				}
				localview.getBackground().setAlpha(0);
				remoteview.getParent().bringChildToFront(remoteview);
				isLocalRemoteChanged= !isLocalRemoteChanged;
			}else{
				onBigWindowClickedEvent();
			}
			break;
		case R.id.bigvideoView:
			if(isLocalRemoteChanged && !MemoryMg.getInstance().isSendOnly){
				checkWindowSize();
				viewResize(localview/*, localL,localT,localR,localB*/);
				viewResize(remoteview/*,remoteL,remoteT,remoteR,remoteB*/);
				localview.getBackground().setAlpha(100);
				remoteview.getBackground().setAlpha(0);
				localview.getParent().bringChildToFront(localview);
				isLocalRemoteChanged= !isLocalRemoteChanged;
			}else{
				onBigWindowClickedEvent();
			}
			break;
		}
	}
	RelativeLayout.LayoutParams localLp,remoteLp;
	private void saveFrame() {
		//lp
		localLp = (RelativeLayout.LayoutParams) localview.getLayoutParams();
		remoteLp = (RelativeLayout.LayoutParams) remoteview.getLayoutParams();
	}
	public void viewResize(SurfaceView v){
		if(v == localview ){
			if(!isLocalRemoteChanged){
				v.setLayoutParams(remoteLp);
			}else{
				v.setLayoutParams(localLp);
			}
		}else{
			if(!isLocalRemoteChanged){
				v.setLayoutParams(localLp);
			}else{
				v.setLayoutParams(remoteLp);
			}
		}
	}
	boolean isFrontCamera = false;
	boolean isPaused = false;
	boolean isSurfaceDestroyed = false;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		LanguageChange.upDateLanguage(this);
		super.onConfigurationChanged(newConfig);
	}
	// -------------------begin开启摄像头----------------------------
	private void startPreview(boolean cameralock) {
		if (mCameraDevice != null && (isPaused)) {
			closeCamera();
			isPaused = false;
		}
		if (mCameraDevice == null) {
			MyLog.v(TAG, "startPreview");
			try {
				if (Camera.getNumberOfCameras() == 2)// 获取摄像头的个数
				{
					whichCameraFlag = cameralock;
					if (cameralock) {
						if (cameraval.equals("0")) {
							if(!isChgVideo){
								cameraCurrLock = 0;
								mCameraDevice = Camera.open(0);// ????
								flagx = 0;
								isFrontCamera = false;
							}else{
								cameraCurrLock = 1;
								mCameraDevice = Camera.open(1);// 前置
								// mCameraDevice.setDisplayOrientation(90);
								flagx = 1;
								isFrontCamera = true;
							}
						}else{
							if(!isChgVideo){
								cameraCurrLock = 1;
								mCameraDevice = Camera.open(1);// 前置
								// mCameraDevice.setDisplayOrientation(90);
								flagx = 1;
								isFrontCamera = true;
							}else{
								cameraCurrLock = 0;
								mCameraDevice = Camera.open(0);// ????
								flagx = 0;
								isFrontCamera = false;
							}
						}
					} else// 前后置摄像头切换
					{
						int x = (cameraCurrLock + 1) % 2;
						mCameraDevice = Camera.open(x);
						flagx = x;
						isFrontCamera = (x == 0 ? false : true);
						cameraCurrLock++;
					}
				} else {
					mCameraDevice = Camera.open(0);// ????
					flagx = 0;
					isFrontCamera = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				MyToast.showToastInBg(true, this.getApplicationContext(),
						R.string.cameracall_camerarestart);
				return;
			}
			if(DeviceVideoInfo.isHorizontal){
				mCameraDevice.setDisplayOrientation(0);
			}else{
				mCameraDevice.setDisplayOrientation(90);
			}
			
			try {
				localParameters = mCameraDevice.getParameters();
				localParameters
						.setPreviewFormat(/* PixelFormat.YCbCr_420_SP */ImageFormat.NV21);
				// 摄像头支持的帧率范围
				for (int[] s : localParameters.getSupportedPreviewFpsRange()) {
					if (s[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] >= frame * 1000
							&& s[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] <= frame * 1000) {
					} else {
						frame = s[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] / 1000;
						if(frame>30) frame = 30;
					}
					break;
				}
				//auto focus
				List<String> allFocus = localParameters.getSupportedFocusModes();
				if(allFocus.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
					localParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					MyLog.e("LocalSurface", "suppport MODE_CONTINUOUS_VIDEO");
				}else{
					MyLog.e("LocalSurface", "don't suppport MODE_CONTINUOUS_VIDEO");
				}
				localParameters.setPreviewFrameRate(/* cameraFrame */frame);// 10/*25*/

//				// 摄像头所支持的分辨率
//				List<Camera.Size> arr = localParameters
//						.getSupportedPreviewSizes();
//				if (arr != null) {
//
//					if (MemoryMg.getInstance().SupportVideoSizeStr.equals("")) {
//						String[] videoarr = { /* "176*144", */"320*240",
//								"352*288", "640*480", "720*480", "1280*720" };
//						String tempStr = "";
//						for (Camera.Size t : arr) {
//							MyLog.e(TAG, "support :" + t.width + " " + t.height);
//
//							tempStr = t.width + "*" + t.height;
//							for (String v : videoarr) {
//								if (v.equals(tempStr)) {
//									// 记住videostr里的元素
//									MemoryMg.getInstance().SupportVideoSizeStr += (v + ",");
//									break;
//								}
//							}
//						}
//					}
//				}
				MyLog.e("pixTest", "setPreviewSize width : " + getCameraWidth()
						+ "height:" + getCameraHeight());
				localParameters.setPreviewSize(getCameraWidth(),
						getCameraHeight());

				mCameraDevice.setParameters(localParameters);
				mCameraDevice.setPreviewCallbackWithBuffer(null);
				this.mCameraDevice.addCallbackBuffer(byteBuffer1);
				this.mCameraDevice.addCallbackBuffer(byteBuffer2);
				mCameraDevice
						.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
							@Override
							public void onPreviewFrame(byte[] data,
									Camera camera) {
								if (data == null)
									return;
								if(encoderChanging){
									return;
								}
								timestamp += 90000/frame;
								byte[] dst = new byte[data.length];
								switch (color_fmt) {
								case PhoneSupportTest.ColorFormat_I420:
									if (isFrontCamera) {
										switch (curAngle) {
										case 0:
											VideoUtils
													.NV21ToI420pWithRotate90DegreeLeftwise(
															getCameraWidth(),
															getCameraHeight(),
															data, dst);
											break;
										case 90:
											VideoUtils
													.NV21ToI420pWithRotate180Degree(
															getCameraWidth(),
															getCameraHeight(),
															data, dst);

											break;
										case 180:
											VideoUtils
													.NV21ToI420pWithRotate90DegreeRightwise(
															getCameraWidth(),
															getCameraHeight(),
															data, dst);
											break;
										case 270:
											VideoUtils.NV21ToI420p(
													getCameraWidth(),
													getCameraHeight(), data,
													dst);
											break;
										}
									} else {
										switch (curAngle) {
										case 0:
											VideoUtils
													.NV21ToI420pWithRotate90DegreeRightwise(
															getCameraWidth(),
															getCameraHeight(),
															data, dst);
											break;
										case 90:
											VideoUtils
											.NV21ToI420pWithRotate180Degree(
													getCameraWidth(),
													getCameraHeight(),
													data, dst);
											break;
										case 180:
											VideoUtils
													.NV21ToI420pWithRotate90DegreeLeftwise(
															getCameraWidth(),
															getCameraHeight(),
															data, dst);
											break;
										case 270:
											VideoUtils.NV21ToI420p(
													getCameraWidth(),
													getCameraHeight(), data,
													dst);
											break;
										}

									}
									// } else {
									// yuv420spToyuv420p(getCameraWidth(),
									// getCameraHeight(), data);
									// dst = data.clone();
									// }
									// data = dst.clone();
									break;
								case PhoneSupportTest.ColorFormat_NV21:
									// if (needSwitchWH) {
									if (isFrontCamera) {
										switch (curAngle) {
										case 0:
											if (needChangeUV) {
												VideoUtils
														.NV21Rotate90DegreeLeftwiseMi(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											} else {
												VideoUtils
														.NV21Rotate90DegreeLeftwise(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											}
											break;
										case 90:
											if(needChangeUV){
												VideoUtils
												.NV21Rotate180DegreeMi(getCameraWidth(),
														getCameraHeight(),
														data, dst);
											}else{
												VideoUtils
												.NV21Rotate180Degree(getCameraWidth(),
														getCameraHeight(),
														data, dst);
											}
											break;
										case 180:
											if (needChangeUV) {
												VideoUtils
														.NV21Rotate90DegreeRightwiseMi(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											} else {
												VideoUtils
														.NV21Rotate90DegreeRightwise(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											}
											break;
										case 270:
											if(needChangeUV){
												VideoUtils.changeUV(getCameraWidth(),
																getCameraHeight(),
																data);}
											dst = data.clone();
											break;
										}
									} else {
										switch (curAngle) {
										case 0:
											if (needChangeUV) {
												VideoUtils
														.NV21Rotate90DegreeRightwiseMi(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											} else {
												VideoUtils
														.NV21Rotate90DegreeRightwise(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											}
											break;
										case 90:
											if(needChangeUV){
												VideoUtils
												.NV21Rotate180DegreeMi(getCameraWidth(),
														getCameraHeight(),
														data, dst);
											}else{
												VideoUtils
												.NV21Rotate180Degree(getCameraWidth(),
														getCameraHeight(),
														data, dst);
											}
											break;
										case 180:
											if (needChangeUV) {
												VideoUtils
														.NV21Rotate90DegreeLeftwiseMi(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											} else {
												VideoUtils
														.NV21Rotate90DegreeLeftwise(
																getCameraWidth(),
																getCameraHeight(),
																data, dst);
											}
											break;
										case 270:
											if(needChangeUV){
												VideoUtils.changeUV(getCameraWidth(),
													getCameraHeight(),
													data);
											}
											dst = data.clone();
											break;
										}
										
									}
									break;
								default:
									break;
								}
								camera.addCallbackBuffer(data);
								try {
									equeue.push(dst);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						});
				mCameraDevice.setPreviewDisplay(localSurfaceHolder);
				this.prewRunning = true;
				this.mCameraDevice.startPreview();

				// 聚焦
				this.mCameraDevice.autoFocus(new Camera.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						
						if (success) {
							// mCameraDevice.setOneShotPreviewCallback(null);
							MyLog.e(TAG, "camera autoFocus success");
						} else
							MyLog.e(TAG, "camera autoFocus failure");

					}
				});
				//auto focus
				this.mCameraDevice.setAutoFocusMoveCallback(new AutoFocusMoveCallback() {
					@Override
					public void onAutoFocusMoving(boolean start, Camera camera) {
					}
				});

				MyLog.e(TAG, "startpreview end:" + System.currentTimeMillis());
			} catch (Exception localThrowable) {
				MyLog.e(TAG, "startPreview failed" + localThrowable.toString());
				localThrowable.printStackTrace();
				MyToast.showToastInBg(true, this.getApplicationContext(),
						R.string.cameracall_pixsupport);
				reject();
				endCameraCall();
			}

		} else {
			MyLog.e(TAG, "startpreview mCameraDevice is not null");
		}

	}

	//
	private void closeCamera() {
		if (this.mCameraDevice == null) {
			return;
		}
		MyLog.v(TAG, "closeCamera");
		mCameraDevice.setPreviewCallback(null);
		mCameraDevice.stopPreview();
		mCameraDevice.release();
		mCameraDevice = null;
	}

	// ---------end camera-----------------
	//
	class H264EncoderThread implements Runnable {

		byte[] prewframe = null, tempframe = null;
		long a = 0;
		Queue<byte[]> storage = null;
		int type = 0;

		public H264EncoderThread(/* SyncBufferSendQueue s */) {
			// this.s = s;
			storage = new LinkedList<byte[]>();
		}

		public synchronized void setFrameBuffer(byte[] prewframe) {

			storage.offer(prewframe);// 入列

			MyLog.e("cameraThread", "setFrameBuffer frame:" + storage.size());
		}

		private synchronized byte[] getFrameBuffer() {
			if (storage.size() > 0) {
				tempframe = storage.poll();
				storage.clear();// 因为编码耗时太长，导致线程将放入队列的流取出来的时候慢，为避免延迟只取第一帧，其它的删除掉
			} else
				tempframe = null;

			MyLog.e("cameraThread", "getFrameBuffer frame:" + storage.size());
			return tempframe;
		}

		@Override
		public void run() {
			
			// android.os.Process
			// .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			while (prewRunning) {

				prewframe = getFrameBuffer();

				if (prewframe != null && farflag == false) {
					try {
						a = System.currentTimeMillis();

						if (videocode.equals("1")) {
						}// H264S
						else {// H264
								// 编码**prewframe
								// eData =
								// NativeH264Encoder.EncodeFrame(prewframe,
								// tempSacle += h264Frame);
								//
							if (NativeH264Encoder.getLastEncodeStatus() == 0
									&& eData.length > 0) {

								// 保存本地文件勿删
								// byte[] newbuf=new byte[eData.length+4];
								// System.arraycopy(eData, 0, newbuf, 4,
								// eData.length);
								// System.arraycopy(head, 0, newbuf, 0, 4);
								// raf.write(newbuf);

								MyLog.e("cameraThread H264",
										"H264EncoderThread Encode and Send:"
												+ eData.length);

								rtpStack.transmitH264FU(eData, eData.length);

								MyLog.e("cameraThread",
										"waste time:"
												+ (System.currentTimeMillis() - a));
							}
						}

					} catch (Exception ex) {
						Log.e(TAG, "cameraThread " + ex.toString());
						ex.printStackTrace();
					}
				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}// end while
			if (storage != null) {
				storage.clear();
				storage = null;
			}
		}

	}

	// 显示来电人号码、呼叫人号码
	private void ShowCallNumAndTime() {
		try {
			if (TextUtils.isEmpty(CallUtil.mName)) {
				callName.setText(CallUtil.mNumber);
			} else {
				callName.setText(CallUtil.mName);
			}
			// callName.setText(CallUtil.mName);
			// if (!CallUtil.mName.equals(CallUtil.mNumber))
			// callNum.setText(CallUtil.mNumber);
			selTxt.setText(R.string.vedio_incom);

		} catch (Exception e) {

			MyLog.e("CameraCall ShowCallNumAndTime error", e.toString());
		}
	}
	// 挂断电话
	public void reject() {
		//process in Receiver#onState() instead of here. modify by mou 2014-12-30
//		Receiver.stopRingtone();
		Receiver.engine(Receiver.mContext).rejectcall();
	}

	private void exitDialog(Context context, String title, String msg) {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(R.layout.shrew_exit_dialog);
		TextView ok = (TextView) window.findViewById(R.id.btn_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.dismiss();
				reject();
				endCameraCall();

			}
		});
		TextView cancel = (TextView) window.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});

	}

	@Override
	public void onImageLoadComplete(int token, Object cookie, ImageView iView,
			boolean imagePresent) {
		
	}

	@Override
	public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
		
	}

	// 结束视频通话
	private void endCameraCall() {
		if (!this.recTcpFlag)
			return;
		this.recTcpFlag = false;//
		MyLog.e("endCameraCall", "endCameraCall");

		MyLog.e(TAG, "endcameracall begin:" + System.currentTimeMillis());
		
		this.prewRunning = false;
		MemoryMg.getInstance().isSendOnly = false;
		MemoryMg.getInstance().isSdpH264s = false;
		if(runable != null){
			runable.stop();
		}
		if(mEncodeOutThread != null){
			mEncodeOutThread.interrupt();
		}
		if (rtpStack != null)
			rtpStack.CloseUdpSocket();

		closeCamera();
		releaseEncoder();
		MyLog.e(TAG, "endcameracall end:" + System.currentTimeMillis());

		// 初始默认值
		Receiver.engine(this).isMakeVideoCall = -1;
		MemoryMg.SdpPtime = 0;

		hd.sendEmptyMessageDelayed(0, 500);

	}

	Handler hd = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if (msg.what == 0)
				CameraCall.this.finish();
			else if (msg.what == 1) {
				// 已使用15M绿色 满格
				alarmFlowNum.setText(Tools.calculateTotal(MemoryMg
						.getInstance().User_3GRelTotal) + "M");

				// 根据百分比，在做颜色选择
				progressval = Tools.calculatePercent(
						MemoryMg.getInstance().User_3GRelTotal,
						MemoryMg.getInstance().User_3GTotal);

				if (progressval < 0.6 && progressval >= 0) {
					proBar.setProgress((int) (100 - progressval * 100));
					proBar.setProgressDrawable(CameraCall.this.getResources()
							.getDrawable(R.drawable.progressblue));
				} else if (progressval < 0.9 && progressval >= 0.6) {
					proBar.setProgress((int) (100 - progressval * 100));
					proBar.setProgressDrawable(CameraCall.this.getResources()
							.getDrawable(R.drawable.progressyellow));
				} else if (progressval >= 0.9 && progressval <= 1) {
					proBar.setProgress((int) (100 - progressval * 100));
					proBar.setProgressDrawable(CameraCall.this.getResources()
							.getDrawable(R.drawable.progressred));
				}
				hd.sendMessageDelayed(hd.obtainMessage(1), 8000);
			} else if (msg.what == 2)// 8秒消失计时器
			{
				//
				hd.sendMessageDelayed(hd.obtainMessage(2), 1000);
			}

		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};

	@Override
	protected void onPause() {
		
		super.onPause();
		isPaused = true;
		// Receiver.mDialog.dismiss();
		MyLog.e("debug_h", "onPause called");
		MyLog.e(TAG, "onPaues()");
	}
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		stopService(new Intent(this,SensorCheckService.class));
		if(runable  != null){
			runable.stop();
		}
		if(mEncodeOutThread != null){
			mEncodeOutThread.interrupt();
		}
		if(sizeChangeHandler.hasMessages(0)) sizeChangeHandler.removeMessages(0);
		if(sizeChangeHandler.hasMessages(1)) sizeChangeHandler.removeMessages(1);
		sizeChangeHandler = null;
		releaseEncoder();
		closeCamera();
		UserAgent.isCamerPttDialog = false;
		if (hd.hasMessages(1))
			hd.removeMessages(1);
		if (hd.hasMessages(2))
			hd.removeMessages(2);

		if (toneGenerator != null) {
			toneGenerator.stopTone(); // 停止播放
			toneGenerator.release();
			toneGenerator = null;
		}

		// 关闭DemocallScreen的广播
		sendBroadcast(new Intent("android.action.closeDemoCallScreen"));
		//视频通话界面默认是横屏的，视频通话结束返回之前的界面是竖屏的，所以需要更新竖屏的资源
        LanguageChange.upDateLanguage(SipUAApp.mContext);
        MyPowerManager.getInstance().releaseScreenWakeLock(mScreanWakeLockKey);
		MyWindowManager.getInstance().reenableKeyguard(this);
		if (mFilter != null)
			unregisterReceiver(quitRecv2);
	}

	public void onFrame(byte[] buffer) {
		MyLog.e("video_codec", "receive - buffer:" + buffer.length);
		if (buffer.length > 0) {
			if (buffer.length > 3 && buffer[0] == 0 && buffer[1] == 0 && buffer[2] == 0
					&& buffer[3] == 1) {
				byte[] toSend1 = new byte[buffer.length - 4];
				System.arraycopy(buffer, 4, toSend1, 0, toSend1.length);
				int firstValue = toSend1[0] & 0x1f;
				if (firstValue == 7) {
					byte[] sps = findSPS(toSend1);
					byte temp[] = new byte[toSend1.length - sps.length];
					System.arraycopy(toSend1, sps.length, temp, 0, temp.length);
					rtpStack.transmitH264FU(sps, sps.length);
					// saveFile(sps);
					byte[] pps = findPPS(temp);
					byte temp2[] = new byte[temp.length - pps.length];
					System.arraycopy(temp, pps.length, temp2, 0, temp2.length);
					rtpStack.transmitH264FU(pps, pps.length);
					// saveFile(pps);
					// byte[] i = findI(temp2);
					// rtpStack.transmitH264FU(i, i.length);
					// saveFile(i);
				} else {
					rtpStack.transmitH264FU(toSend1, toSend1.length);
					// saveFile(toSend1);
				}
			} else {
				// 0001
				int firstValue = buffer[0] & 0x1f;
				if (firstValue == 7) {
					byte[] sps = findSPS(buffer);
					byte temp[] = new byte[buffer.length - sps.length];
					System.arraycopy(buffer, sps.length, temp, 0, temp.length);
					rtpStack.transmitH264FU(sps, sps.length);
					byte[] pps = findPPS(temp);
					byte temp2[] = new byte[temp.length - pps.length];
					System.arraycopy(temp, pps.length, temp2, 0, temp2.length);
					rtpStack.transmitH264FU(pps, pps.length);
					byte[] i = findI(temp2);
					// saveFile(i);
				} else {
					rtpStack.transmitH264FU(buffer, buffer.length);
					// saveFile(d);
				}
			}
		}

	}

	byte[] findSPS(byte[] d) {
		byte[] result = new byte[0];
		int end = -1;
		for (int i = 0; i < d.length; i++) {
			if (i < d.length - 3 && d[i] == 0 && d[i + 1] == 0 && d[i + 2] == 0
					&& d[i + 3] == 1 && (d[i + 4] & 0x1F) == 8) {
				end = i;
				break;
			}
		}
		result = new byte[end];
		System.arraycopy(d, 0, result, 0, end);
		return result;
	}

	byte[] findPPS(byte[] d) {
		byte[] result = new byte[0];
		for (int i = 0; i < d.length; i++) {
			if (i < d.length - 3 && d[i] == 0 && d[i + 1] == 0 && d[i + 2] == 0
					&& d[i + 3] == 1 /* && (d[i+4] & 0x1F) == 5 */) {
				break;
			}
		}
		result = new byte[d.length - 4];
		System.arraycopy(d, 4, result, 0, result.length);
		return result;
	}

	byte[] findI(byte[] d) {
		byte[] result = new byte[0];
		int start = -1;
		for (int i = 0; i < d.length; i++) {
			if (i < d.length - 3 && d[i] == 0 && d[i + 1] == 0 && d[i + 2] == 0
					&& d[i + 3] == 1 && (d[i + 4] & 0x1F) == 5) {
				start = i + 4;
				break;
			}
		}
		result = new byte[d.length - start];
		System.arraycopy(d, start, result, 0, result.length);
		return result;
	}

	private int color_fmt = -1;

	void initMediaCodec() {
		if(Build.VERSION.SDK_INT < 16){
			MyToast.showToast(true, mContext,
					mContext.getString(R.string.version_unsupported));
			reject();
			endCameraCall();
			return;
		}
		mMediaCodec = MediaCodec.createEncoderByType("video/avc");
		MyLog.e("pixTest", "encode width = " + getEncodeWidth() + " height = "
				+ getEncodeHeight());
		MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", /* width */
				getEncodeWidth(), /* width */getEncodeHeight());
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, netrate);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frame);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframe);
		color_fmt = DeviceVideoInfo.supportColor;
		int d = PhoneSupportTest.ColorFormatList[DeviceVideoInfo.supportColor][1];
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
				d);
		mMediaCodec.configure(mediaFormat, null, null,
				MediaCodec.CONFIGURE_FLAG_ENCODE);
		mMediaCodec.start();
		runable = new encodeOutSendRunnable();
		mEncodeOutThread = new Thread(runable);
		mEncodeOutThread.start();
	}

	private void releaseEncoder() {
		
		if(runable != null){
			runable.stop();
		}
		if(mEncodeOutThread != null && mEncodeOutThread.isAlive()){
			mEncodeOutThread.interrupt();
		}
		if(equeue != null){
			equeue.clear();
		}
		if (mMediaCodec != null) {
			mMediaCodec.stop();
			mMediaCodec.release();
			mMediaCodec = null;
		}
	}

	MediaCodec mMediaCodec;

	


	private void setCameraDataParas(boolean isChanged) {
		if (isChanged && !DeviceVideoInfo.isCodecK3) {
			releaseEncoder();
		}
		// 先判断前后置摄像头
		// boolean isback = (cameraId == 0);
		if (isFrontCamera){
			pixTag = PreferenceManager.getDefaultSharedPreferences(this)// 后置
					.getString("videoresolutionkey0", "5");
			if((pixTag.equals("5")) && !MemoryMg.getInstance().SupportVideoSizeStr.contains("320*240")){
				pixTag = "6";
			}
		}
		else{
			pixTag = PreferenceManager.getDefaultSharedPreferences(this)// 前置
					.getString("videoresolutionkey", "5");
			if((pixTag.equals("5")) && !MemoryMg.getInstance().SupportVideoSizeStr.contains("320*240")){
				pixTag = "6";
			}
		}
		if(!DeviceVideoInfo.isCodecK3){
			InitH264Encoder();
			initMediaCodec();
		}
	}
	public boolean needChangeUVinNV21() {
		String mode = Build.MODEL.toLowerCase();
		if(DeviceVideoInfo.isCodecK3 && !mode.contains("honor")) //判断是否是k3机型
		{
			if(DeviceVideoInfo.color_correct){//判断是否开启色彩校正
				return true;
			}else
				return false;
				
		}else
		{
			if(DeviceVideoInfo.color_correct){
				return false;
			}else{
				return true;
			}
		}
	}

	class PreviewCallBack implements SurfaceHolder.Callback {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			MyLog.e("surface", "preview create!!!");
			closeCamera();
			startPreview(whichCameraFlag);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			MyLog.e("surface", "preview changed!!!");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			MyLog.e("surface", "preview destroyed!!!");
		}

	}

	@Override
	public void sizeChanged(int width, int height) {
//		if(sizeChangeHandler == null) return;
//		sizeChangeHandler.sendMessage(sizeChangeHandler.obtainMessage(0, width,
//				height));
	}

	private void sizeChange(int width, int height) {
//		MyLog.e(TAG, "receive pix = width:"+width+"*height = "+height);
//		resetRemoteViewParam(remoteLp,width,height);
//		checkWindowSize();
//		if (remoteview != null && !isLocalRemoteChanged) {
//			remoteview.setLayoutParams(remoteLp);
//		}
	}
	Handler sizeChangeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				if(!DeviceVideoInfo.supportFullScreen){
				int width = msg.arg1;
				int height = msg.arg2;
				if (width < 1 || height < 1)
					return;
				sizeChange(width, height);
				}
				break;
			case 1:
				if (isShowViewFlag) {
					isShowViewFlag = false;
					if (bottomBtnBar != null)
						bottomBtnBar.setVisibility(View.GONE);
					if (topBoard != null) {
						topBoard.setVisibility(View.GONE);
					}
					if(closelinear != null){
						closelinear.setVisibility(View.GONE);
					}
				}
				break;
			}
		}
	};
	//reset remoteview
	/**
	 * @param width  解码收到的分辨率width
	 * @param height 解码收到的分辨率height
	 * @param param 其中包含的宽度高度 为屏幕显示的像素大小
	 * */
	private void  resetRemoteViewParam(RelativeLayout.LayoutParams param ,int width,int height){
		if(width == 0 || height == 0){
			MyLog.e(TAG, "decode pix width = 0 or height = 0");
			return;
		}
		if(param == null){
			MyLog.e(TAG, "localview param is null");
			return ;
		}
		double deviceRate = 1.0;
		double pixRate = 1.0;
		deviceRate = (widthPix*1.0)/heightPix;
		pixRate = (width*1.0)/height;
		if(DeviceVideoInfo.isHorizontal){
			//1280*720
			if(width > height){
				if(deviceRate > pixRate){
					param.height = heightPix;
					param.width = (int)(widthPix/pixRate);
					param.leftMargin = 0;
					param.topMargin = 0;
				}else if(deviceRate < pixRate){
					param.width = widthPix;
					param.height = (int)(heightPix/pixRate);
					param.leftMargin = 0;
					param.topMargin = 0;
				}else{//相等
					param.width = widthPix;
					param.height = heightPix;
					param.leftMargin = 0;
					param.topMargin = 0;
				}
			}else{
				param.height = heightPix;
				param.width = (int)(heightPix*pixRate);
				param.leftMargin = widthPix -(widthPix -param.width)/2 -param.width;
				param.topMargin = 0;
			}
			
		}else{
			if(width > height){
				param.width = widthPix;
				param.height = (int)(widthPix/pixRate);
				param.leftMargin = 0;
				if(height == 720){
					param.topMargin = heightPix -(heightPix -param.height)/2 -param.height;
				}else{
					param.topMargin = 0;
				}
			}else{
				if(deviceRate > pixRate){
					param.height = heightPix;
					param.width = (int)(widthPix*pixRate);
					param.topMargin = 0;
					param.leftMargin = 0;
				}else if(deviceRate < pixRate){
					param.width = widthPix;
					param.height = (int)(widthPix*pixRate);
					param.leftMargin = 0;
					param.topMargin = heightPix -(heightPix -param.height)/2 -param.height;
				}else{
					param.width = widthPix;
					param.height = heightPix;
					param.leftMargin = 0;
					param.topMargin = 0;
				}
			}
		}
	}
	
	//检查setframe保存的参数是否符合不覆盖的原则
	public boolean checkWindowSize(){
		return false;
		/*
		//相等则不重算
		if((widthPix*1.0/heightPix) == ((remoteLp.width*1.0) /remoteLp.height )){
			return false;
		}
		if(DeviceVideoInfo.isHorizontal){
			if(!isLocalRemoteChanged){//只需没有改变时判断，改变后不需再判断
				if(remoteLp.leftMargin +remoteLp.width > localLp.leftMargin){
					int d = remoteLp.leftMargin +remoteLp.width - localLp.leftMargin;
					localLp.leftMargin = remoteLp.leftMargin +remoteLp.width;
					localLp.width = localLp.width -d;
					return true;
				}
			}
		}else{
			if(!isLocalRemoteChanged){
				if(remoteLp.topMargin+remoteLp.height > localLp.topMargin){
					int d = remoteLp.topMargin+remoteLp.height - localLp.topMargin;
					localLp.topMargin = remoteLp.topMargin+remoteLp.height;
					localLp.height = localLp.height -d;
					return true;
				}
			}
		}
		return false;
	*/}
	//大窗口点击事件
	public void onBigWindowClickedEvent(){
		if (isShowViewFlag) {
			isShowViewFlag = false;
			if (bottomBtnBar != null)
				bottomBtnBar.setVisibility(View.GONE);
			if (topBoard != null) {
				topBoard.setVisibility(View.GONE);
			}
			if(closelinear != null){
				closelinear.setVisibility(View.GONE);
			}
		} else {
			bottomBtnBar.getParent().bringChildToFront(bottomBtnBar);
			topBoard.getParent().bringChildToFront(topBoard);
			closelinear.getParent().bringChildToFront(closelinear);
			isShowViewFlag = true;
			if (bottomBtnBar != null)
				bottomBtnBar.setVisibility(View.VISIBLE);
			if (topBoard != null) {
				topBoard.setVisibility(View.VISIBLE);
			}
			if(closelinear != null){
				closelinear.setVisibility(View.VISIBLE);
			}
			// if (topLinearLayout != null)
			// topLinearLayout.setVisibility(View.VISIBLE);
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					if(sizeChangeHandler == null) return;
					sizeChangeHandler.sendEmptyMessage(1);
				}
			}, 3000);
		}
	}
	// 转换dip为px
	public int dip2px(int dip) {
		float scale = getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	// 转换px为dip
	public int px2dip(int px) {
		float scale = getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f * (px >= 0 ? 1 : -1));
	}
	
	class encodeOutSendRunnable implements Runnable {
		ByteBuffer[] oBufs;
		ByteBuffer[] iBufs;
		MediaCodec.BufferInfo bufInfo;
		boolean flag = true;
		byte[] dst = null;
		public encodeOutSendRunnable() {
			oBufs = mMediaCodec.getOutputBuffers();
			bufInfo = new MediaCodec.BufferInfo();
		}

		public void stop() {
			flag = false;
		}
		@Override
		public void run() {
			int oIdx = 0;
			while (flag) {
				try {
					dst = equeue.pop();
					iBufs = mMediaCodec.getInputBuffers();

					int iIdx = mMediaCodec.dequeueInputBuffer(0);
					if (iIdx >= 0) {
						ByteBuffer iBuf = iBufs[iIdx];
						iBuf.clear();
						int capacity = iBuf.capacity();
						if (capacity < dst.length) {
							MyLog.e("EncodeTest", "capacity is not enough");
							mMediaCodec.queueInputBuffer(iIdx, 0, 0, timestamp,
									0);
						}
						iBuf.put(dst);
						mMediaCodec.queueInputBuffer(iIdx, 0, dst.length,
								timestamp, 0);
					}

					MediaCodec.BufferInfo bufInfo = new MediaCodec.BufferInfo();
					while (true) {
						oIdx = mMediaCodec.dequeueOutputBuffer(bufInfo, 0);
						if (oIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
							// errorInfo ="try later";
							break;
						} else if (oIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
							// errorInfo ="format changed";
						} else if (oIdx == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
							// errorInfo ="buffer changed";
							oBufs = mMediaCodec.getOutputBuffers();
						} else if (oIdx >= 0) {
							oBufs[oIdx].position(bufInfo.offset);
							oBufs[oIdx].limit(bufInfo.offset + bufInfo.size);
							ByteBuffer oBuf = oBufs[oIdx];
							byte[] out = new byte[bufInfo.size];
							oBuf.get(out, 0, bufInfo.size);
							CameraCall.this.onFrame(out);
							mMediaCodec.releaseOutputBuffer(oIdx, false);
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}

	}
}
