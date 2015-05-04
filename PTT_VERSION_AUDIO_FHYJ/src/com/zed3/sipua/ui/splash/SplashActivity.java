package com.zed3.sipua.ui.splash;

import org.zoolu.tools.MyLog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.OneShotAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.sipua.welcome.IAutoConfigListener;
import com.zed3.sipua.welcome.LoginActivity;
import com.zed3.toast.MyToast;
import com.zed3.utils.NetworkListenerService;
import com.zed3.utils.Tools;

public class SplashActivity extends BaseActivity implements IAutoConfigListener{
	private boolean ISUNLOGIN = true;
	NotificationManager notificationManager;
	ImageButton about_btn_show;
	Context mContext;
	private static final int FETCH_INFO_OK = 0;
	private static final int AutoLogin = 2;
	private static final int ManuLogin = 3;
	private static final int BeginRegister = 4;
	private static final int NOPHONENUM = 5;
	private static final int NOSIMCARD = 6;
	private static final int UNKONWNSTATE = 7;
	private final String TAG = "SplashActivity";
	private boolean isThreadStart = false;
	AutoConfigManager mManager;
	//public static boolean isVideo = true;//移植到DeviceInfo
	public static boolean isConferenceVideo = false;
	public static boolean isGsm = false;
	public static boolean isMessage = false;
	//public static boolean isAudio = false;//移植到DeviceInfo
	private boolean isDialog = false;
	Thread t_fetchInfo;
	TextView tv_version;
	private SharedPreferences sharedPreferences;
	private TelephonyManager manager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.e("SplashActivity", "SplashActivity SplashActivity SplashActivity");
		super.onCreate(savedInstanceState);
		manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		sharedPreferences = getSharedPreferences(Settings.sharedPrefsFile,
				Context.MODE_PRIVATE);
		//2014-9-10 英文环境下实现自动打开信令加密开关
//		if(config.locale.getLanguage().equals("en")){
//			getSharedPreferences(Settings.sharedPrefsFile,
//					MODE_PRIVATE).edit().putBoolean(Settings.PREF_MSG_ENCRYPT, true).commit();
//		}
		mContext = SipUAApp.mContext;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		ISUNLOGIN = DeviceInfo.CONFIG_SUPPORT_UNICOM_PASSWORD;
		if (!ISUNLOGIN) {
			registerReceiver(loginReceiver, new IntentFilter(
					"com.zed3.sipua.login"));
			if (Receiver.mSipdroidEngine != null
					&& Receiver.mSipdroidEngine.isRegistered(true)) {
				MyLog.e("HT", "launch from oncreate" + Receiver.mSipdroidEngine);
				hd.sendEmptyMessage(AutoLogin);
				return;
			}
		}
		setContentView(R.layout.splash);
//		tv_version =(TextView)findViewById(R.id.versionshow);//GQT英文版 修改欢迎界面
//		tv_version.setText(getResources().getString(R.string.app_version)+Tools.getVersionName(this));
		//AutoConfigManager.LoadSettings(this);//初始化配置信息
		//开机动画代码
		RelativeLayout rl_splash_main = (RelativeLayout) this.findViewById(R.id.rl_splash_main);
        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
        aa.setDuration(2000);
//        aa.setRepeatCount(Animation.INFINITE);//无线循环代码
        rl_splash_main.startAnimation(aa);
        rl_splash_main.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				RelativeLayout ll= (RelativeLayout)findViewById(R.id.splash_about);
//				if(ll.getVisibility() == View.VISIBLE){
//					ll.setVisibility(View.GONE);
//				}//GQT英文版，修改了欢迎界面
			}
		});
//        about_btn_show = (ImageButton)findViewById(R.id.splash_about_btn);
//        about_btn_show.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				RelativeLayout ll= (RelativeLayout)findViewById(R.id.splash_about);
//				ll.setVisibility(View.VISIBLE);
//			}
//		});//GQT英文版，修改了欢迎界面
        //通过广播接收登录
        //判断是否为自动配置版本和手动配置版本
        if(!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN){
        	hd.sendEmptyMessageDelayed(ManuLogin, 2000); //手动版本2s结束后直接跳转到手动登陆页面。
        }else{
        	if(!NetChecker.check(SplashActivity.this, true)){
        		new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						SplashActivity.this.finish();
					}
				}).start();
        		return;
        	}

			mManager = new AutoConfigManager(SplashActivity.this); //初始化
			mManager.setOnFetchListener(this);
			//线程开启去获取配置信息。
			t_fetchInfo = new Thread(new Runnable() {
				
				@Override
				public void run() {
					//在这里延时
					Looper.prepare();
					mLooper = Looper.myLooper();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fetchInfo();
					Looper.loop();
				}
			});
			t_fetchInfo.setName("AutoLogin_Thread");
			t_fetchInfo.start();
			isThreadStart = true;
		}
        finish();
	}
	
	private Looper mLooper;
	
	private void quitFetchTask(){
		if(mLooper!=null){
			try {
				mLooper.quit();
			} finally {
				mLooper = null;
			}
		}
	}
	
	Handler hd = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 110:
				SplashActivity.this.finish();
				Tools.exitApp2(SplashActivity.this);
				break;
			case BeginRegister:
				if (!ISUNLOGIN) {
					Receiver.engine(mContext).registerMore();
//					startActivity(new Intent(SplashActivity.this,
//							MainActivity.class)
//							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				} else{
					startActivity(new Intent(SplashActivity.this,
							UnionLogin.class)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					SplashActivity.this.finish();
				}
				
				break;
			case AutoLogin:
				MyLog.e("Hu.", "enter by auto login************************************");
				if(!ISUNLOGIN)
					startActivity(new Intent(SplashActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				else
					startActivity(new Intent(SplashActivity.this,/*MainDirectoryActivity.class*/UnionLogin.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				SplashActivity.this.finish();
				
				break;

			case NOSIMCARD:
				MyToast.showToast(true, mContext, R.string.no_simcard);
				break;

			case UNKONWNSTATE:
				MyToast.showToast(true, mContext, R.string.unknown_state);
				break;

			case NOPHONENUM:
				MyToast.showToast(true, mContext,
						getResources().getString(R.string.fail_to_telnumber));
				break;

			case ManuLogin:
				Bundle bd = msg.getData();
				isDialog = true;
				if(bd != null){
					String reason = bd.getString("reason");
					if(reason != null && !reason.equals("")){
						Dialog dialog = new AlertDialog.Builder(SplashActivity.this).setTitle(R.string.information).setMessage(reason)
								.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
												SplashActivity.this.finish();
												//exitApp();调用该方法会执行Receiver.engine()方法，导致退出集群通后后台自启
												// 开机启动自动登陆失败时停止NetworkListenerService add by liangzhang 2014-11-26
												if (sharedPreferences.getBoolean("NetworkListenerService", false)) {
													sharedPreferences.edit().putBoolean("NetworkListenerService", false).commit();
													Intent intent = new Intent(mContext, NetworkListenerService.class);
													stopService(intent);
												}
												System.exit(0);
												isDialog = false;
									}
								}).create();
						dialog.setCanceledOnTouchOutside(false);
						dialog.show();
					}
				}
				if(!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN){
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
					SplashActivity.this.finish();
				}
				break;
			case 0:
				String result = msg.getData().getString("result");
				
				if (result.contains("passwdIncorrect")) {
					result = SplashActivity.this.getResources().getString(R.string.wrong_password);
				} else if (result.contains("userNotExist")) {
					result = SplashActivity.this.getResources().getString(R.string.account_not_exist);
				} else if (result.contains("Timeout")) {
					result = SplashActivity.this.getResources().getString(R.string.timeout);
				} else if (result.contains("netbroken")) {
					result = SplashActivity.this.getResources().getString(R.string.network_exception);
				} else if (result.contains("userOrpwderror")) {
					result = SplashActivity.this.getResources().getString(R.string.wrong_name_or_pwd);
				} else if(result.contains("versionTooLow")){
					result = SplashActivity.this.getResources().getString(R.string.version_too_low);
				}else if(result.contains("Already Login")){
					result =  SplashActivity.this.getResources().getString(R.string.logged_in_another_place);
				} else if(result.contains("Temporarily Unavailable")){
					result = SplashActivity.this.getResources().getString(R.string.service_unavailable);
				}
				MyToast.showToast(true,SplashActivity.this ,result);
				hd.sendEmptyMessageDelayed(110,1000);
				break;
				
			}
		}

	};

	/**
	 * 获取手机本地配置信息 modify by liangzhang 2014-10-14
	 * 
	 */
	private void fetchInfo() {
		// 获取手机信息
		fetchLocalInfo(0);
		// 判断是否需要重新获取手机配置信息
		if (DeviceInfo.isSameSimCard) {// 同一张sim卡
			if (DeviceInfo.isSameHandset) {// 同一部手机
				// 同一张sim卡装在同一部手机上，直接读取上次登陆时保存到sharedPreferences中的信息
				DeviceInfo.PHONENUM = sharedPreferences.getString(
						AutoConfigManager.LC_PHONENUM, "");
				DeviceInfo.SIMNUM = sharedPreferences.getString(
						AutoConfigManager.LC_SIMNUM, "");
				DeviceInfo.IMSI = sharedPreferences.getString(
						AutoConfigManager.LC_IMSI, "");
				DeviceInfo.IMEI = sharedPreferences.getString(
						AutoConfigManager.LC_IMEI, "");
				DeviceInfo.MACADDRESS = sharedPreferences.getString(
						AutoConfigManager.LC_MACADDRESS, "");
				MyLog.i(TAG, "非首次登陆（同一张sim卡装在同一部手机上）" + "\r\n"
						+ "DeviceInfo.PHONENUM>>" + DeviceInfo.PHONENUM
						+ "\r\n" + "DeviceInfo.SIMNUM ICCId>>"
						+ DeviceInfo.SIMNUM + "\r\n" + "DeviceInfo.IMSI IMSI>>"
						+ DeviceInfo.IMSI + "\r\n" + "DeviceInfo.IMEI IMEI >>"
						+ DeviceInfo.IMEI + "\r\n"
						+ "DeviceInfo.MACADDRESS MACADDRESS >>"
						+ DeviceInfo.MACADDRESS);
			} else {// 同一张sim卡装到另一部手机上，重新获取手机配置信息
				// 获取手机信息
				fetchLocalInfo(60);
			}
		} else {// 换了sim卡或sim卡不可用
			// 与上次登陆的是同一部手机，直接获取上次登录时保存到sharedPreferences中的信息
			if (DeviceInfo.isSameHandset) {
				DeviceInfo.IMEI = sharedPreferences.getString(
						AutoConfigManager.LC_IMEI, "");
				DeviceInfo.MACADDRESS = sharedPreferences.getString(
						AutoConfigManager.LC_MACADDRESS, "");
				MyLog.i(TAG, "非首次登陆（同一部手机）" + "\r\n"
						+ "DeviceInfo.IMEI IMEI>>" + DeviceInfo.IMEI + "\r\n"
						+ "DeviceInfo.MACADDRESS MACADDRESS>>"
						+ DeviceInfo.MACADDRESS);
			} else {// 首次登陆或手机和sim卡都更换，重新获取手机配置信息
				// 获取手机信息
				fetchLocalInfo(60);
			}
		}
		if (Tools.isConnect(mContext)) {// 网络连接正常时向服务器发送请求获取DPMP自动登陆配置信息
			mManager.fetchConfig();
		} else {
			TimeOut();// 没有网络，则退出
			mManager.setOnFetchListener(null);
		}
	}

	/**
	 * 获取手机sim卡和设备信息。若存在sim卡且可用，则在60s时间内获取sim卡信息；否则只获取手机设备信息
	 */
	private void fetchLocalInfo(int time) {
		// 检查sim卡状态
		if (checkSimCardState(manager)) {// sim卡存在且可用
			if (time == 0) {
				// 获取sim信息
				getSimCardInfo();
			} else {
				// 60s时间获取手机sim卡信息
				for (int i = 0; i < time; i++) {
					try {
						Thread.sleep(1000);
						// 获取sim信息
						getSimCardInfo();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// 获取手机号码失败时弹出吐司提示用户
				if (DeviceInfo.PHONENUM == null
						|| DeviceInfo.PHONENUM.equals("")
						|| DeviceInfo.PHONENUM.equalsIgnoreCase("null")) {
					hd.sendEmptyMessage(NOPHONENUM);
				}
			}
			// 判断此sim卡是否与上次登陆的是同一张Sim卡
			if (mManager.isTheSameSimCard(DeviceInfo.SIMNUM, DeviceInfo.IMSI)) {
				DeviceInfo.isSameSimCard = true;
			} else {
				DeviceInfo.isSameSimCard = false;
			}
			MyLog.i(TAG, "当前手机sim卡信息：DeviceInfo.PHONENUM >>"
					+ DeviceInfo.PHONENUM + "\r\n"
					+ "DeviceInfo.SIMNUM ICCId >>" + DeviceInfo.SIMNUM + "\r\n"
					+ "DeviceInfo.IMSI IMSI >>" + DeviceInfo.IMSI);
		} else {// 无sim卡或sim卡不可用
			DeviceInfo.isSameSimCard = false;
		}
		// 获取手机配置信息并判断是否与上次登陆的为同一部手机设备
		getHandsetInfo();
		if (mManager.isTheSameHandset()) {
			DeviceInfo.isSameHandset = true;
		} else {
			DeviceInfo.isSameHandset = false;
		}
	}

	/**
	 * 检查sim卡状态
	 * 
	 * @param manager
	 *            TelephonyManager manager
	 * @return sim存在且状态正常时返回true,否则返回false
	 */
	private boolean checkSimCardState(TelephonyManager manager) {
		boolean flag = false;
		switch (manager.getSimState()) {
		case TelephonyManager.SIM_STATE_READY:// sim卡状态正常
			flag = true;
			break;

		case TelephonyManager.SIM_STATE_ABSENT:// 无sim卡
			flag = false;
			hd.sendEmptyMessage(NOSIMCARD);
			break;

		default:// sim卡未知状态
			flag = false;
			hd.sendEmptyMessage(UNKONWNSTATE);
			break;
		}
		return flag;
	}

	/**
	 * 获取sim卡信息（手机号码\ICCID\IMSI）
	 */
	private void getSimCardInfo() {
		// 获取手机号码
		DeviceInfo.PHONENUM = manager.getLine1Number();
		if (DeviceInfo.PHONENUM != null && DeviceInfo.PHONENUM != ""
				&& DeviceInfo.PHONENUM.startsWith("+86")) {
			DeviceInfo.PHONENUM = DeviceInfo.PHONENUM.replace("+86", "");
		}
		// 获取sim卡序列号ICCID
		DeviceInfo.SIMNUM = manager.getSimSerialNumber();
		// 获取sim卡IMSI
		DeviceInfo.IMSI = manager.getSubscriberId();
	}

	/**
	 * 获取手机设备信息（IMEI\MAC地址）
	 */
	private void getHandsetInfo() {
		DeviceInfo.IMEI = manager.getDeviceId();
		DeviceInfo.MACADDRESS = getLocalMacAddress();
		MyLog.i(TAG, "当前手机设备信息： DeviceInfo.IMEI IMEI >>" + DeviceInfo.IMEI
				+ "\r\n" + " DeviceInfo.MACADDRESS MACADDRESS >>"
				+ DeviceInfo.MACADDRESS);
	}

	/**
	 * 获取wifi MAC地址
	 * 
	 * @return 当MAC地址不可用时返回空，否则将MAC地址中的冒号去掉后再返回
	 */
	private String getLocalMacAddress() {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		// modify by liangzhang 2014-09-15 获取Mac地址后去掉冒号
		String macAddress = info.getMacAddress();
		// modify by liangzhang 2014-09-25
		// NullPointerExeception 加入MAC地址不可用的判断
		if (macAddress != null && !macAddress.equals("")) {
			if (macAddress.contains(":")) {
				return macAddress.replaceAll(":", "");
			} else {
				return macAddress;
			}
		}
		return "";
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(hd != null){
			hd.removeMessages(110);
		}
		if(!ISUNLOGIN){
			unregisterReceiver(loginReceiver);
		}
		if(mManager != null){
			mManager.setOnFetchListener(null);
		}
		isThreadStart = false;
	}

	@Override
	public void TimeOut() {
		Message msg = new Message();
		msg.what = ManuLogin;
		Bundle bd = new Bundle();
		bd.putString("reason", getResources().getString(R.string.netvork_connecttion_timeout));
		msg.setData(bd);
		hd.sendMessage(msg);
	}

	@Override
	public void FetchConfigFailed() {
		Message msg = new Message();
		msg.what = ManuLogin;
		Bundle bd = new Bundle();
		bd.putString("reason", getResources().getString(R.string.account_not_exist));
		msg.setData(bd);
		hd.sendMessage(msg);
	}

	/**
	 * 解析服务器数据失败 modify by liangzhang 2014-10-14
	 */
	@Override
	public void parseFailed() {
		Message message = new Message();
		message.what = ManuLogin;
		Bundle bundle = new Bundle();
		bundle.putString("reason",
				getResources().getString(R.string.error_parse));
		message.setData(bundle);
		hd.sendMessage(message);
	}

	public boolean checkConfig(){
//		if(TextUtils.isEmpty(mManager.fetchLocalUserName()) || (TextUtils.isEmpty(mManager.fetchLocalServer())) || (TextUtils.isEmpty(mManager.fetchLocalPwd()))){
//			return false;
//		}else{
//			return true;
//		}
		return false;
	}

	private void exitApp() {
		Receiver.engine(SplashActivity.this).expire(-1);
		Receiver.engine(SplashActivity.this).halt();
		stopService(new Intent(SplashActivity.this, RegisterService.class));
		Receiver.alarm(0, OneShotAlarm.class);
		System.exit(0);
	}
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		
//		switch (keyCode) {
//		
//		case KeyEvent.KEYCODE_BACK:
//			SplashActivity.this.finish();
//			exitApp();
//		default:
//			break;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
	@Override
	public void ParseConfigOK() {
		
		quitFetchTask();
		
		mManager.saveSetting();
		if(mManager != null){
			mManager.saveLocalconfig();
		}
		hd.sendEmptyMessage(BeginRegister);
	}
	
	BroadcastReceiver loginReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isSuccess = intent.getBooleanExtra("loginstatus", false);
			if(isSuccess){
			Intent loginIntent = new Intent();
			loginIntent.setClass(SplashActivity.this,MainActivity.class);
			startActivity(loginIntent);
			SplashActivity.this.finish();
			}else{ 
//				Message msg = new Message();
//				msg.what = ManuLogin;
//				Bundle bd = new Bundle();
//				bd.putString("reason", getResources().getString(R.string.wrong_password));
//				msg.setData(bd);
//				Receiver.engine(SplashActivity.this).halt();
//				hd.sendMessage(msg);
				Message msg = new Message();
				msg.what = 0;
				Bundle data = new Bundle();
				data.putString("result", intent.getStringExtra("result"));
				msg.setData(data);
				hd.sendMessage(msg);
			}
		}
		
	};
	
}
