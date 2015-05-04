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
	//public static boolean isVideo = true;//��ֲ��DeviceInfo
	public static boolean isConferenceVideo = false;
	public static boolean isGsm = false;
	public static boolean isMessage = false;
	//public static boolean isAudio = false;//��ֲ��DeviceInfo
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
		//2014-9-10 Ӣ�Ļ�����ʵ���Զ���������ܿ���
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
//		tv_version =(TextView)findViewById(R.id.versionshow);//GQTӢ�İ� �޸Ļ�ӭ����
//		tv_version.setText(getResources().getString(R.string.app_version)+Tools.getVersionName(this));
		//AutoConfigManager.LoadSettings(this);//��ʼ��������Ϣ
		//������������
		RelativeLayout rl_splash_main = (RelativeLayout) this.findViewById(R.id.rl_splash_main);
        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
        aa.setDuration(2000);
//        aa.setRepeatCount(Animation.INFINITE);//����ѭ������
        rl_splash_main.startAnimation(aa);
        rl_splash_main.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				RelativeLayout ll= (RelativeLayout)findViewById(R.id.splash_about);
//				if(ll.getVisibility() == View.VISIBLE){
//					ll.setVisibility(View.GONE);
//				}//GQTӢ�İ棬�޸��˻�ӭ����
			}
		});
//        about_btn_show = (ImageButton)findViewById(R.id.splash_about_btn);
//        about_btn_show.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				RelativeLayout ll= (RelativeLayout)findViewById(R.id.splash_about);
//				ll.setVisibility(View.VISIBLE);
//			}
//		});//GQTӢ�İ棬�޸��˻�ӭ����
        //ͨ���㲥���յ�¼
        //�ж��Ƿ�Ϊ�Զ����ð汾���ֶ����ð汾
        if(!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN){
        	hd.sendEmptyMessageDelayed(ManuLogin, 2000); //�ֶ��汾2s������ֱ����ת���ֶ���½ҳ�档
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

			mManager = new AutoConfigManager(SplashActivity.this); //��ʼ��
			mManager.setOnFetchListener(this);
			//�߳̿���ȥ��ȡ������Ϣ��
			t_fetchInfo = new Thread(new Runnable() {
				
				@Override
				public void run() {
					//��������ʱ
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
												//exitApp();���ø÷�����ִ��Receiver.engine()�����������˳���Ⱥͨ���̨����
												// ���������Զ���½ʧ��ʱֹͣNetworkListenerService add by liangzhang 2014-11-26
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
	 * ��ȡ�ֻ�����������Ϣ modify by liangzhang 2014-10-14
	 * 
	 */
	private void fetchInfo() {
		// ��ȡ�ֻ���Ϣ
		fetchLocalInfo(0);
		// �ж��Ƿ���Ҫ���»�ȡ�ֻ�������Ϣ
		if (DeviceInfo.isSameSimCard) {// ͬһ��sim��
			if (DeviceInfo.isSameHandset) {// ͬһ���ֻ�
				// ͬһ��sim��װ��ͬһ���ֻ��ϣ�ֱ�Ӷ�ȡ�ϴε�½ʱ���浽sharedPreferences�е���Ϣ
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
				MyLog.i(TAG, "���״ε�½��ͬһ��sim��װ��ͬһ���ֻ��ϣ�" + "\r\n"
						+ "DeviceInfo.PHONENUM>>" + DeviceInfo.PHONENUM
						+ "\r\n" + "DeviceInfo.SIMNUM ICCId>>"
						+ DeviceInfo.SIMNUM + "\r\n" + "DeviceInfo.IMSI IMSI>>"
						+ DeviceInfo.IMSI + "\r\n" + "DeviceInfo.IMEI IMEI >>"
						+ DeviceInfo.IMEI + "\r\n"
						+ "DeviceInfo.MACADDRESS MACADDRESS >>"
						+ DeviceInfo.MACADDRESS);
			} else {// ͬһ��sim��װ����һ���ֻ��ϣ����»�ȡ�ֻ�������Ϣ
				// ��ȡ�ֻ���Ϣ
				fetchLocalInfo(60);
			}
		} else {// ����sim����sim��������
			// ���ϴε�½����ͬһ���ֻ���ֱ�ӻ�ȡ�ϴε�¼ʱ���浽sharedPreferences�е���Ϣ
			if (DeviceInfo.isSameHandset) {
				DeviceInfo.IMEI = sharedPreferences.getString(
						AutoConfigManager.LC_IMEI, "");
				DeviceInfo.MACADDRESS = sharedPreferences.getString(
						AutoConfigManager.LC_MACADDRESS, "");
				MyLog.i(TAG, "���״ε�½��ͬһ���ֻ���" + "\r\n"
						+ "DeviceInfo.IMEI IMEI>>" + DeviceInfo.IMEI + "\r\n"
						+ "DeviceInfo.MACADDRESS MACADDRESS>>"
						+ DeviceInfo.MACADDRESS);
			} else {// �״ε�½���ֻ���sim�������������»�ȡ�ֻ�������Ϣ
				// ��ȡ�ֻ���Ϣ
				fetchLocalInfo(60);
			}
		}
		if (Tools.isConnect(mContext)) {// ������������ʱ����������������ȡDPMP�Զ���½������Ϣ
			mManager.fetchConfig();
		} else {
			TimeOut();// û�����磬���˳�
			mManager.setOnFetchListener(null);
		}
	}

	/**
	 * ��ȡ�ֻ�sim�����豸��Ϣ��������sim���ҿ��ã�����60sʱ���ڻ�ȡsim����Ϣ������ֻ��ȡ�ֻ��豸��Ϣ
	 */
	private void fetchLocalInfo(int time) {
		// ���sim��״̬
		if (checkSimCardState(manager)) {// sim�������ҿ���
			if (time == 0) {
				// ��ȡsim��Ϣ
				getSimCardInfo();
			} else {
				// 60sʱ���ȡ�ֻ�sim����Ϣ
				for (int i = 0; i < time; i++) {
					try {
						Thread.sleep(1000);
						// ��ȡsim��Ϣ
						getSimCardInfo();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// ��ȡ�ֻ�����ʧ��ʱ������˾��ʾ�û�
				if (DeviceInfo.PHONENUM == null
						|| DeviceInfo.PHONENUM.equals("")
						|| DeviceInfo.PHONENUM.equalsIgnoreCase("null")) {
					hd.sendEmptyMessage(NOPHONENUM);
				}
			}
			// �жϴ�sim���Ƿ����ϴε�½����ͬһ��Sim��
			if (mManager.isTheSameSimCard(DeviceInfo.SIMNUM, DeviceInfo.IMSI)) {
				DeviceInfo.isSameSimCard = true;
			} else {
				DeviceInfo.isSameSimCard = false;
			}
			MyLog.i(TAG, "��ǰ�ֻ�sim����Ϣ��DeviceInfo.PHONENUM >>"
					+ DeviceInfo.PHONENUM + "\r\n"
					+ "DeviceInfo.SIMNUM ICCId >>" + DeviceInfo.SIMNUM + "\r\n"
					+ "DeviceInfo.IMSI IMSI >>" + DeviceInfo.IMSI);
		} else {// ��sim����sim��������
			DeviceInfo.isSameSimCard = false;
		}
		// ��ȡ�ֻ�������Ϣ���ж��Ƿ����ϴε�½��Ϊͬһ���ֻ��豸
		getHandsetInfo();
		if (mManager.isTheSameHandset()) {
			DeviceInfo.isSameHandset = true;
		} else {
			DeviceInfo.isSameHandset = false;
		}
	}

	/**
	 * ���sim��״̬
	 * 
	 * @param manager
	 *            TelephonyManager manager
	 * @return sim������״̬����ʱ����true,���򷵻�false
	 */
	private boolean checkSimCardState(TelephonyManager manager) {
		boolean flag = false;
		switch (manager.getSimState()) {
		case TelephonyManager.SIM_STATE_READY:// sim��״̬����
			flag = true;
			break;

		case TelephonyManager.SIM_STATE_ABSENT:// ��sim��
			flag = false;
			hd.sendEmptyMessage(NOSIMCARD);
			break;

		default:// sim��δ֪״̬
			flag = false;
			hd.sendEmptyMessage(UNKONWNSTATE);
			break;
		}
		return flag;
	}

	/**
	 * ��ȡsim����Ϣ���ֻ�����\ICCID\IMSI��
	 */
	private void getSimCardInfo() {
		// ��ȡ�ֻ�����
		DeviceInfo.PHONENUM = manager.getLine1Number();
		if (DeviceInfo.PHONENUM != null && DeviceInfo.PHONENUM != ""
				&& DeviceInfo.PHONENUM.startsWith("+86")) {
			DeviceInfo.PHONENUM = DeviceInfo.PHONENUM.replace("+86", "");
		}
		// ��ȡsim�����к�ICCID
		DeviceInfo.SIMNUM = manager.getSimSerialNumber();
		// ��ȡsim��IMSI
		DeviceInfo.IMSI = manager.getSubscriberId();
	}

	/**
	 * ��ȡ�ֻ��豸��Ϣ��IMEI\MAC��ַ��
	 */
	private void getHandsetInfo() {
		DeviceInfo.IMEI = manager.getDeviceId();
		DeviceInfo.MACADDRESS = getLocalMacAddress();
		MyLog.i(TAG, "��ǰ�ֻ��豸��Ϣ�� DeviceInfo.IMEI IMEI >>" + DeviceInfo.IMEI
				+ "\r\n" + " DeviceInfo.MACADDRESS MACADDRESS >>"
				+ DeviceInfo.MACADDRESS);
	}

	/**
	 * ��ȡwifi MAC��ַ
	 * 
	 * @return ��MAC��ַ������ʱ���ؿգ�����MAC��ַ�е�ð��ȥ�����ٷ���
	 */
	private String getLocalMacAddress() {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		// modify by liangzhang 2014-09-15 ��ȡMac��ַ��ȥ��ð��
		String macAddress = info.getMacAddress();
		// modify by liangzhang 2014-09-25
		// NullPointerExeception ����MAC��ַ�����õ��ж�
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
	 * ��������������ʧ�� modify by liangzhang 2014-10-14
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
