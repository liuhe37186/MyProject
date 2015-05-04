package com.zed3.sipua.ui;

import java.io.IOException;
import java.util.Locale;
import java.util.zip.Inflater;

import org.zoolu.tools.MyLog;

import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.codecs.EncodeRate.Mode;
import com.zed3.codecs.NetworkType;
import com.zed3.screenhome.BaseActivityGroup;
import com.zed3.sipua.CallHistoryDatabase;
import com.zed3.sipua.LocalConfigSettings;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.autoUpdate.UpdateVersionService;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.message.MessageMainActivity;
import com.zed3.sipua.message.PhotoTransferActivity;
import com.zed3.sipua.message.PhotoTransferReceiveActivity;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.ui.anta.AntaCallActivity2;
import com.zed3.sipua.ui.lowsdk.ContactActivity;
import com.zed3.sipua.ui.lowsdk.SipdroidActivity;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.AutoLoginService;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.sipua.welcome.LoginActivity;
import com.zed3.utils.DensityUtil;
import com.zed3.utils.LanguageChange;
import com.zed3.utils.LogUtil;
import com.zed3.utils.NetworkListenerService;
import com.zed3.utils.Tools;
import com.zed3.utils.Zed3Log;

public class MainActivity extends BaseActivityGroup implements OnClickListener {
	public static final String TAG = "MainActivity";
	FrameLayout frame_message, frame_singlecall, frame_phototransfer;
	LinearLayout l_contact, l_groupcall, l_meeting, l_setting, l_more;
	View v_contact, v_groupcall, v_meeting, v_singlecall, v_message, v_photo,
			v_setting_pop, v_setting_normal;
	TextView tv_contact, tv_groupcall, tv_meeting, tv_singlecall, tv_message,
			tv_photo, tv_setting_pop, tv_setting_normal;
	LinearLayout actvityarea;
	LinearLayout lll;
	private TextView msgPoint, singlecallpoint;// add by liangzhang 增加未接来电提示信息
	private Context mContext;
	public static String READ_MESSAGE = "read_message_update_count";
	private View PopupView, popup_meeting, popup_setting, mRootView;
	private static PopupWindow Setting_Transfer_View = null;
	static MainActivity gInst;
	private static boolean isShowing = false;
	private ScaleAnimation sa;
	private int functionNum = 0;
	private TextView msgPointPhoto;
	public static Mode mode = Mode.MR475;
	private IntentFilter mFilter;

	public static MainActivity getInstance() {
		return gInst;
	}

	private BroadcastReceiver recv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context mContext, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					MessageDialogueActivity.RECEIVE_TEXT_MESSAGE)) {
				initMsg();
			}
			if (intent.getAction().equalsIgnoreCase(MainActivity.READ_MESSAGE)) {
				initMsg();
			}
			if (PhotoTransferReceiveActivity.ACTION_READ_MMS
					.equalsIgnoreCase(intent.getAction())) {
				initPhotoMsg();
			}
			if (PhotoTransferReceiveActivity.ACTION_RECEIVE_MMS
					.equalsIgnoreCase(intent.getAction())) {
				initPhotoMsg();
			}
			// 收到清空未接电话信息隐藏UI add by liangzhang 2014-09-12
			if (intent.getAction().equalsIgnoreCase(
					Contants.ACTION_CLEAR_MISSEDCALL)) {
				singlecallpoint.setVisibility(View.GONE);
			}
			// 收到未接电话广播，更新UI add by liangzhang 2014-10-25
			if (intent.getAction().equalsIgnoreCase(
					Contants.ACTION_HISTORY_CHANGED)) {
				initSingleCall();
			}
		}
	};
	public static final String USERNAME = "username";
	public static final String NUMBER = "number";
	public static final String PASSWORD = "password";
	public static final String PROXY = "proxy";
	public static final String PORT = "port";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Zed3Log.debug("testcrash", "MainActivity#onCreate enter");
		LogUtil.makeLog(TAG, "onCreate()");
		mContext = this;
		gInst = this;
		super.onCreate(savedInstanceState);
		IntentFilter inFilter  =new IntentFilter("com.zed3.sipua.login");
		registerReceiver(loginReceiver,inFilter);
		Intent start = this.getIntent();
		if (start != null) {
			Bundle extras = start.getExtras();
			if (extras != null) {
				String username = extras.getString(USERNAME);
				String password = extras.getString(PASSWORD);
				String proxy = extras.getString(PROXY);
				String port = extras.getString(PORT);
				login(username, password, proxy, port);
				System.out.println("------username:" + username + "password:"
						+ password);
			}
		}

		// checkUserData();
		initDeviceInfo();
		functionNum = getfunctionNum();
		// 判断是否需要更新资源配置文件
		Resources resources = getResources();// 获取资源对象
		Configuration config = resources.getConfiguration();// 获取配置对象
		String language = config.locale.getLanguage();
		SharedPreferences sharedPreferences = getSharedPreferences(
				Settings.sharedPrefsFile, Context.MODE_PRIVATE);
		// 自动登陆版本正常成功登陆后停止NetworkListenerService add by liangzhang 2014-11-26
		if (sharedPreferences.getBoolean("NetworkListenerService", false)) {
			sharedPreferences.edit()
					.putBoolean("NetworkListenerService", false).commit();
			Intent intent = new Intent(mContext, NetworkListenerService.class);
			stopService(intent);
		}
		int languageId = sharedPreferences.getInt("languageId", 0);
		if (languageId == 0
				&& !language.equals(Locale.getDefault().getLanguage())
				|| languageId == 1 && !language.equals("zh") || languageId == 2
				&& !language.equals("en")) {
			LanguageChange.upDateLanguage(mContext);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mRootView = getLayoutInflater().inflate(R.layout.tab_item, null);
		setContentView(mRootView);
		mFilter = new IntentFilter();
		mFilter.addAction(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE);
		mFilter.addAction(MainActivity.READ_MESSAGE);
		mFilter.addAction(Contants.ACTION_ALL_GROUP_CHANGE);
		mFilter.addAction(PhotoTransferReceiveActivity.ACTION_READ_MMS);
		mFilter.addAction(PhotoTransferReceiveActivity.ACTION_RECEIVE_MMS);
		// add by liangzhang 2014-09-05
		mFilter.addAction(Contants.ACTION_CLEAR_MISSEDCALL);
		mFilter.addAction(Contants.ACTION_HISTORY_CHANGED);
		mContext.registerReceiver(recv, mFilter);
		actvityarea = (LinearLayout) findViewById(R.id.LinearLayout);
		tabInit();
		msgPoint = (TextView) findViewById(R.id.msgpoint);
		// add by liangzhang 2014-09-05
		msgPointPhoto = (TextView) findViewById(R.id.msgpoint_photo);
		initMsg();
		initPhotoMsg();
		singlecallpoint = (TextView) findViewById(R.id.singlecallpoint);
		initSingleCall();
		lll = (LinearLayout) findViewById(R.id.tab_bottm_size);
		// add by zdx
		if ("Auto".equals(PreferenceManager.getDefaultSharedPreferences(
				Receiver.mContext).getString(Settings.AMR_MODE,
				Settings.DEFAULT_AMR_MODE))) {
			mode = NetworkType.getNetWorkType(getApplicationContext());
		}
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		// SplashActivity.isVideo = sp.getBoolean("isVideo", true);
		if (sp.getBoolean("flowOnOffKey", false)) {
			Intent intent = new Intent();
			intent.setAction("com.zed3.flow.FlowRefreshService");
			startService(intent);
		}
		if (DeviceInfo.CONFIG_CHECK_UPGRADE && !SipUAApp.updateNextTime) {
			new Thread(new Runnable() {
				public void run() {
					Looper.prepare();
					UpdateVersionService service = new UpdateVersionService(
							MainActivity.this, SipUAApp.mContext
									.getSharedPreferences(
											Settings.sharedPrefsFile,
											Context.MODE_PRIVATE).getString(
											Settings.PREF_SERVER,
											Settings.DEFAULT_SERVER));
					service.checkUpdate(false);
					Looper.loop();
				}
			}).start();
		}
		Zed3Log.debug("testcrash", "MainActivity#onCreate exit");
	}

	AutoConfigManager acm = new AutoConfigManager(this);
	ProgressDialog pd;

	private void login(String userName, String pwd, String server, String port) {

		acm.saveSetting(userName, pwd, server, port);

		pd = new ProgressDialog(MainActivity.this);
		pd.setMessage(getResources().getString(R.string.loginning));
		pd.show();
		pd.setCanceledOnTouchOutside(false);

		if (Receiver.mSipdroidEngine == null) {// modify by hu
			// 2014/1/22
			Receiver.engine(this);
		} else {
			if (!Receiver.mSipdroidEngine.isRegistered(true)) {
				Receiver.mSipdroidEngine.StartEngine();
			} else {
				if (pd != null) {
					pd.dismiss();
				}
			}
		}
	}

	BroadcastReceiver loginReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isSuccess = intent.getBooleanExtra("loginstatus", false);
			System.out.println("-------loginReceiver result:" + isSuccess);
			if (isSuccess) {
				pd.dismiss();
			}
		}

	};

	private void checkUserData() {
		if (!AutoLoginService.getDefault().existLoginParams()) {
			Toast.makeText(SipUAApp.getAppContext(),
					getString(R.string.validate_user_data), Toast.LENGTH_LONG)
					.show();

			SipUAApp.getMainThreadHandler().postDelayed(new Runnable() {

				@Override
				public void run() {
					Tools.exitApp(SipUAApp.mContext);
				}
			}, 1 * 1000);

		}
	}

	private void initDeviceInfo() {

		AutoLoginService ALS = AutoLoginService.getDefault();

		if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN && ALS.existLoginParams()) {
			Zed3Log.debug("testcrash", "MainActivity#initDeviceInfo() ALS init");
			ALS.initDeviceInfo();
		} else {
			try {
				Zed3Log.debug("testcrash",
						"MainActivity#initDeviceInfo() use local settings");
				LocalConfigSettings.loadSettings(SipUAApp.getAppContext());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private int getfunctionNum() {
		// TODO Auto-generated method stub
		int a = 3;
		if (DeviceInfo.CONFIG_SUPPORT_VIDEO || DeviceInfo.CONFIG_SUPPORT_AUDIO) {
			a++;
		}
		if (DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE) {
			a++;
		}
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD) {
			a++;
		}
		if (DeviceInfo.CONFIG_SUPPORT_IM) {
			a++;
		}
		return a;
	}

	@Override
	protected void onResume() {
		Zed3Log.debug("testcrash", "MainActivity#onResume enter");
		LanguageChange.upDateLanguage(this);
		super.onResume();
		// AutoConfigManager manager = new AutoConfigManager(mContext);
		// String userName = ContactUtil.getUserName(manager.fetchLocalPwd());
		// Receiver.onText(Receiver.REGISTER_NOTIFICATION, userName,
		// R.drawable.icon64, 0);
		Zed3Log.debug("testcrash", "MainActivity#onResume exit");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent_ = new Intent(Intent.ACTION_MAIN);
			intent_.addCategory(Intent.CATEGORY_HOME);
			intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent_);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void startIntent(String id, Intent intent) {
		LocalActivityManager activitymanager;
		activitymanager = getLocalActivityManager();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Window w = activitymanager.startActivity(id, intent);
		View v = w.getDecorView();
		actvityarea.removeAllViews();
		actvityarea.setPadding(0, 0, 0, 0);
		actvityarea.addView(v, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
	}

	@Override
	protected void onDestroy() {
		LogUtil.makeLog(TAG, "onDestroy()");
		unregisterReceiver(loginReceiver);

		if (mFilter != null) {
			try {
				unregisterReceiver(recv);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				MyLog.e(TAG, "unregister error");
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}

	public void startIntent(Class<?> cls) {
		Intent intent = new Intent(MainActivity.this, cls);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startIntent(cls.getSimpleName(), intent);
	}

	public void tabInit() {
		l_contact = (LinearLayout) findViewById(R.id.tab_contact);
		l_groupcall = (LinearLayout) findViewById(R.id.tab_groupcall);
		frame_singlecall = (FrameLayout) findViewById(R.id.tab_singlecall);
		if (!DeviceInfo.CONFIG_SUPPORT_AUDIO
				&& !DeviceInfo.CONFIG_SUPPORT_VIDEO) {
			frame_singlecall.setVisibility(View.GONE);
		}
		frame_message = (FrameLayout) findViewById(R.id.tab_message);
		if (!DeviceInfo.CONFIG_SUPPORT_IM) {
			frame_message.setVisibility(View.GONE);
		}
		frame_phototransfer = (FrameLayout) findViewById(R.id.tab_photo_transfer);
		if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD) {
			frame_phototransfer.setVisibility(View.GONE);
		}
		l_meeting = (LinearLayout) findViewById(R.id.tab_meeting);
		if (DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE && functionNum != 7)// 是否包含语音会议
			l_meeting.setVisibility(View.VISIBLE);
		else
			l_meeting.setVisibility(View.GONE);
		l_setting = (LinearLayout) findViewById(R.id.tab_setting);
		if (functionNum > 5) {
			l_setting.setVisibility(View.GONE);
		}
		l_more = (LinearLayout) findViewById(R.id.tab_more);
		if (functionNum < 6) {
			l_more.setVisibility(View.GONE);
		}
		l_contact.setOnClickListener(contactlistener);
		l_groupcall.setOnClickListener(groupcalllistener);
		frame_singlecall.setOnClickListener(singlecalllistener);
		frame_message.setOnClickListener(messagelistener);
		frame_phototransfer.setOnClickListener(phototransferlistener);
		l_meeting.setOnClickListener(meetinglistener);
		l_setting.setOnClickListener(settinglistener_normal);
		l_more.setOnClickListener(setting_pop_listener);

		v_contact = findViewById(R.id.icon_contact);
		v_groupcall = findViewById(R.id.icon_groupcall);
		v_meeting = findViewById(R.id.icon_meeting);
		v_singlecall = findViewById(R.id.icon_singlecall);
		v_message = findViewById(R.id.icon_message);
		v_photo = findViewById(R.id.icon_photo_transfer);

		tv_contact = (TextView) findViewById(R.id.tab_1_text);
		tv_groupcall = (TextView) findViewById(R.id.tab_2_text);
		tv_singlecall = (TextView) findViewById(R.id.tab_3_text);
		tv_message = (TextView) findViewById(R.id.tab_4_text);
		tv_photo = (TextView) findViewById(R.id.tab_5_text);
		tv_meeting = (TextView) findViewById(R.id.tab_6_text);
		tv_setting_normal = (TextView) findViewById(R.id.tab_7_text);
		tv_setting_pop = (TextView) findViewById(R.id.tab_8_text);

		l_groupcall.performClick();
	}

	OnClickListener contactlistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			View v_icon = findViewById(R.id.icon_contact);
			l_contact.setBackgroundResource(R.drawable.main_tab_item_select);
			tv_contact.setTextColor(Color.WHITE);
			v_icon.setBackgroundResource(R.drawable.tab_contact_after);
			startIntent(ContactActivity.class);
			dismissPopupWindow();
		}
	};
	OnClickListener groupcalllistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			View v_icon = findViewById(R.id.icon_groupcall);
			tv_groupcall.setTextColor(Color.WHITE);
			l_groupcall.setBackgroundResource(R.drawable.main_tab_item_select);
			v_icon.setBackgroundResource(R.drawable.tab_groupcall_after);
			startIntent(TalkBackNew.class);
			dismissPopupWindow();
		}
	};
	OnClickListener meetinglistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			View v_icon = findViewById(R.id.icon_meeting);
			tv_meeting.setTextColor(Color.WHITE);
			l_meeting.setBackgroundResource(R.drawable.main_tab_item_select);
			v_icon.setBackgroundResource(R.drawable.tab_meeting_after);
			startIntent(AntaCallActivity2.class);
			dismissPopupWindow();
		}
	};
	OnClickListener singlecalllistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			// add by liangzhang 2014-09-04 通话界面清空未接电话提示信息
			singlecallpoint.setVisibility(View.GONE);
			tv_singlecall.setTextColor(Color.WHITE);
			View v_icon = findViewById(R.id.icon_singlecall);
			frame_singlecall
					.setBackgroundResource(R.drawable.main_tab_item_select);
			v_icon.setBackgroundResource(R.drawable.tab_singlecall_after);
			startIntent(SipdroidActivity.class);
			dismissPopupWindow();
		}
	};
	OnClickListener messagelistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_message.setTextColor(Color.WHITE);
			View v_icon = findViewById(R.id.icon_message);
			frame_message
					.setBackgroundResource(R.drawable.main_tab_item_select);
			v_icon.setBackgroundResource(R.drawable.tab_mesage_down);
			startIntent(MessageMainActivity.class);
			dismissPopupWindow();
		}
	};
	OnClickListener phototransferlistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_photo.setTextColor(Color.WHITE);
			View v_icon = findViewById(R.id.icon_photo_transfer);
			frame_phototransfer
					.setBackgroundResource(R.drawable.main_tab_item_select);
			v_icon.setBackgroundResource(R.drawable.tab_photo_down);
			startIntent(PhotoTransferActivity.class);
			dismissPopupWindow();
		}
	};
	OnClickListener setting_pop_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (isShowing) {
				dismissPopupWindow();
				return;
			}
			// resetAll();
			// tv_setting.setTextColor(Color.WHITE);
			// View v_icon = findViewById(R.id.icon_setting);
			// l_setting.setBackgroundResource(R.drawable.main_tab_item_select);
			// v_icon.setBackgroundResource(R.drawable.tab_setting_after);
			// startIntent(SettingNew.class);
			showMenuPopuWindow();

		}
	};
	OnClickListener settinglistener_normal = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_setting_normal.setTextColor(Color.WHITE);
			View v_icon = findViewById(R.id.icon_setting);
			l_setting.setBackgroundResource(R.drawable.main_tab_item_select);
			v_icon.setBackgroundResource(R.drawable.tab_setting_after);
			startIntent(SettingNew.class);
			// showMenuPopuWindow();
		}
	};

	void resetAll() {
		View v_icon = findViewById(R.id.icon_contact);
		l_contact.setBackgroundDrawable(null);
		v_icon.setBackgroundResource(R.drawable.tab_contact_before);
		tv_contact.setTextColor(getResources().getColor(R.color.font_color3));

		l_groupcall.setBackgroundDrawable(null);
		v_icon = findViewById(R.id.icon_groupcall);
		v_icon.setBackgroundResource(R.drawable.tab_groupcall_before);
		tv_groupcall.setTextColor(getResources().getColor(R.color.font_color3));

		l_meeting.setBackgroundDrawable(null);
		v_icon = findViewById(R.id.icon_meeting);
		v_icon.setBackgroundResource(R.drawable.tab_meeting_before);
		tv_meeting.setTextColor(getResources().getColor(R.color.font_color3));

		frame_singlecall.setBackgroundDrawable(null);
		v_icon = findViewById(R.id.icon_singlecall);
		v_icon.setBackgroundResource(R.drawable.tab_singlecall_before);
		tv_singlecall
				.setTextColor(getResources().getColor(R.color.font_color3));

		frame_message.setBackgroundDrawable(null);
		v_message = findViewById(R.id.icon_message);
		v_message.setBackgroundResource(R.drawable.tab_message);
		tv_message.setTextColor(getResources().getColor(R.color.font_color3));

		frame_phototransfer.setBackgroundDrawable(null);
		v_photo = findViewById(R.id.icon_photo_transfer);
		v_photo.setBackgroundResource(R.drawable.tab_photo_up);
		tv_photo.setTextColor(getResources().getColor(R.color.font_color3));

		l_more.setBackgroundResource(R.drawable.setting_meetting_selector);
		v_icon = findViewById(R.id.icon_setting);
		v_icon.setBackgroundResource(R.drawable.tab_setting_before);
		tv_setting_pop.setTextColor(getResources()
				.getColor(R.color.font_color3));

		l_setting.setBackgroundResource(R.drawable.setting_meetting_selector);
		v_icon = findViewById(R.id.icon_setting);
		v_icon.setBackgroundResource(R.drawable.tab_setting_before);
		tv_setting_normal.setTextColor(getResources().getColor(
				R.color.font_color3));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, getResources().getString(R.string.exits)).setIcon(
				R.drawable.exit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Tools.exitApp(MainActivity.this);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void showMenuPopuWindow() {
		// if (Setting_Transfer_View == null) {
		if (functionNum == 6) {
			PopupView = View.inflate(mContext, R.layout.aa_setting_transfer,
					null);
			// popup_meetting = (LinearLayout) PopupView
			// .findViewById(R.id.pop_meetting);
			// popup_meetting.setVisibility(View.GONE);
			// popup_transfer = (LinearLayout) PopupView
			// .findViewById(R.id.popup_transfer);
			// popup_transfer.setOnClickListener(this);
			popup_setting = (LinearLayout) PopupView
					.findViewById(R.id.popup_setting);
			popup_setting.setOnClickListener(this);

			/*
			 * 在代码中 new出来view对象 或者 设置popwindows的时候 里面接受的参数都是 px单位
			 */
			Setting_Transfer_View = new PopupWindow(PopupView,
			// DensityUtil.getDipWidth(mContext)
			// - DensityUtil.dip2px(mContext, 1),
					DensityUtil.dip2px(mContext, 110), DensityUtil.dip2px(
							mContext, 50));
			// 给popupwindow设置一个透明的背景颜色,如果不设置 会导致动画效果没法显示
			Setting_Transfer_View.setBackgroundDrawable(new ColorDrawable(
					Color.TRANSPARENT));
			// sa = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.1f);
			sa = new ScaleAnimation(-0.5f, 1.0f, -0.5f, 0.1f);
			sa.setDuration(200);
		} else if (functionNum == 7) {
			PopupView = View.inflate(mContext, R.layout.aa_setting_transfer2,
					null);
			popup_meeting = (LinearLayout) PopupView
					.findViewById(R.id.popup_meetting);
			popup_meeting.setOnClickListener(this);
			popup_setting = (LinearLayout) PopupView
					.findViewById(R.id.popup_setting);
			popup_setting.setOnClickListener(this);

			/*
			 * 在代码中 new出来view对象 或者 设置popwindows的时候 里面接受的参数都是 px单位
			 */
			Setting_Transfer_View = new PopupWindow(PopupView,
			// DensityUtil.getDipWidth(mContext)
			// - DensityUtil.dip2px(mContext, 1),
					DensityUtil.dip2px(mContext, 110), DensityUtil.dip2px(
							mContext, 100));
			// 给popupwindow设置一个透明的背景颜色,如果不设置 会导致动画效果没法显示
			Setting_Transfer_View.setBackgroundDrawable(new ColorDrawable(
					Color.TRANSPARENT));
			// sa = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.1f);
			sa = new ScaleAnimation(-0.5f, 1.0f, -0.5f, 0.1f);
			sa.setDuration(200);
		}
		// }

		Setting_Transfer_View.showAtLocation(mRootView, Gravity.RIGHT
				| Gravity.BOTTOM,
		/* location[0] *//* 10 + 60 */0, /* location[1] *//* 400 */
				// DensityUtil.dip2px(mContext, 120)+lll.getHeight()+20);
				// DensityUtil.dip2px(mContext, 120)+lll.getHeight()+20);
				lll.getHeight());
		// MyToast.showToast(true, mContext,
		// mContext.getResources().getDisplayMetrics().heightPixels+""
		// +"..."+lll.getHeight()+"..."+DensityUtil.dip2px(mContext, 120));
		// PopupView.startAnimation(sa);//2014-9-26 modify by wlei
		isShowing = true;
	}

	public static void dismissPopupWindow() {
		if (Setting_Transfer_View != null && Setting_Transfer_View.isShowing()) {
			try {
				Setting_Transfer_View.dismiss();
			} catch (Exception e) {
				// TODO: handle exception
			}
			isShowing = false;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.popup_meetting:
			resetAll();
			tv_setting_pop.setTextColor(Color.WHITE);
			View v_icon = findViewById(R.id.icon_more);
			l_more.setBackgroundResource(R.drawable.main_tab_item_select);
			v_icon.setBackgroundResource(R.drawable.tab_setting_after);
			startIntent(AntaCallActivity2.class);
			dismissPopupWindow();
			break;
		case R.id.popup_setting:
			resetAll();
			tv_setting_pop.setTextColor(Color.WHITE);
			l_more.setBackgroundResource(R.drawable.main_tab_item_select);
			startIntent(SettingNew.class);
			dismissPopupWindow();
			break;
		}
	}

	/**
	 * 异步查询SMS数据库，更新UI modify by liangzhang 2014-10-25
	 */
	private void initMsg() {
		MyLog.e(TAG, "initMsg");
		new MessageTask().execute();
	}

	private final class MessageTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			SmsMmsDatabase database = new SmsMmsDatabase(mContext);
			int count = 0;
			Cursor mCursor = null;
			try {
				mCursor = database.mQuery("message_talk", "status= " + 0
						+ " and type='sms' ", null, null);
				count = mCursor.getCount();
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.e("MessageTask",
						"query table message_talk error:" + e.toString());
			} finally {
				if (mCursor != null) {
					mCursor.close();
				}
			}
			return count;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result > 0) {
				msgPoint.setVisibility(View.VISIBLE);
				msgPoint.setText("" + result);
			} else {
				msgPoint.setVisibility(View.INVISIBLE);
			}
		}

	}

	/**
	 * 异步查询通话记录数据库，更新UI modify by liangzhang 2014-10-25
	 */
	private void initSingleCall() {
		MyLog.e(TAG, "initSingleCall");
		new SingleCallTask().execute();
	}

	private final class SingleCallTask extends
			AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			// 查询数据库，获得未读的未接电话信息
			CallHistoryDatabase callHistoryDatabase = CallHistoryDatabase
					.getInstance(mContext);
			int count = 0;
			Cursor cursor = null;
			try {
				cursor = callHistoryDatabase.mQuery("call_history",
						"type='CallUnak'" + " and status=" + 0);
				count = cursor.getCount();
			} catch (Exception e) {
				e.printStackTrace();
				MyLog.e("SingleCallTask",
						"query CallHistoryDatabase error" + e.toString());
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return count;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result > 0) {
				singlecallpoint.setVisibility(View.VISIBLE);
				singlecallpoint.setText(result + "");
			} else {
				singlecallpoint.setVisibility(View.INVISIBLE);
			}
		}
	}

	/**
	 * 异步查询图片拍传数据库，更新UI modify by liangzhang 2014-10-25
	 */
	private void initPhotoMsg() {
		MyLog.e(TAG, "initPhotoMsg");
		new PhotoMsgTask().execute();
	}

	private final class PhotoMsgTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			SmsMmsDatabase database = new SmsMmsDatabase(mContext);
			int count = 0;
			Cursor mCursor = null;
			try {
				mCursor = database.mQuery(SmsMmsDatabase.TABLE_MESSAGE_TALK,
						"type = 'mms' and mark = 0 and status = 0", null, null);
				count = mCursor.getCount();
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.e("PhotoMsgTask",
						"query table message_talk error:" + e.toString());
			} finally {
				if (mCursor != null) {
					mCursor.close();
				}
			}
			return count;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result > 0) {
				msgPointPhoto.setVisibility(View.VISIBLE);
				msgPointPhoto.setText("" + result);
			} else {
				msgPointPhoto.setVisibility(View.INVISIBLE);
			}
		}
	}

}