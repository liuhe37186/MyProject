package com.zed3.sipua.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zed3.flow.FlowRefreshService;
import com.zed3.flow.TotalFlowView;
import com.zed3.location.MemoryMg;
import com.zed3.screenhome.BaseActivity;
import com.zed3.settings.AboutActivity;
import com.zed3.settings.AdvancedChoice;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.autoUpdate.UpdateVersionService;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.SwitchButton;
import com.zed3.utils.Tools;

public class SettingNew extends BaseActivity implements OnClickListener{
	private final String sharedPrefsFile = "com.zed3.sipua_preferences";

	boolean flag1 = false,flag2 = false;
//	ImageButton backBtn;
	//ImageView imgBtn = null;
//	SlipButton mSlipButton = null;
	com.zed3.utils.SwitchButton mSlipButton = null;
	ImageView imgVideoBtn = null;
	LinearLayout lineSuper = null;
	LinearLayout lineflow = null;
	
	LinearLayout lineupdate = null;
	LinearLayout lineabout = null;
	LinearLayout exit_app_new = null;
	Button loginout = null;

	SharedPreferences mypre = null;
	ProgressDialog pd;
	private final int CHECKUPDATEOVER = 1;
	Handler hd = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case CHECKUPDATEOVER:
				if(pd != null){
					pd.dismiss();
					pd = null;
				}
				break;
			default:break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingnew);
		//返回
//		backBtn = (ImageButton) findViewById(R.id.back_button);
//		backBtn.setOnClickListener(this);
		// 开机启动
		//imgBtn = (ImageView) findViewById(R.id.imgviewbtn);
		mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
		LinearLayout autoRunLayout = (LinearLayout) findViewById(R.id.autorun);
		if (!DeviceInfo.AUTORUN_REMOTE) {
			if (!DeviceInfo.CONFIG_SUPPORT_AUTORUN) {
				autoRunLayout.setVisibility(View.GONE);
				findViewById(R.id.autorun_line).setVisibility(View.GONE);
				if ("1".equals(mypre.getString(Settings.PREF_AUTORUN, "1"))) {
					SharedPreferences.Editor editor = mypre.edit();
					editor.putString(Settings.PREF_AUTORUN, "0");
					editor.commit();
				}
			} else {
				autoRunLayout.setVisibility(View.VISIBLE);
				findViewById(R.id.autorun_line).setVisibility(View.VISIBLE);
				// mypre = getSharedPreferences(sharedPrefsFile,
				// Activity.MODE_PRIVATE);
				// if("0".equals(mypre.getString(Settings.PREF_AUTORUN, "1"))){
				// SharedPreferences.Editor editor = mypre.edit();
				// editor.putString(Settings.PREF_AUTORUN, "1");
				// editor.commit();
				// }
			}
		} else {
			autoRunLayout.setVisibility(View.GONE);
			findViewById(R.id.autorun_line).setVisibility(View.GONE);
			if ("0".equals(mypre.getString(Settings.PREF_AUTORUN, "1"))) {
				SharedPreferences.Editor editor = mypre.edit();
				editor.putString(Settings.PREF_AUTORUN, "1");
				editor.commit();
			}
		}
		// 修改关闭开机启动后退出再次登录仍显示为打开状态的问题 modify by liangzhang 2014-10-22
	    mSlipButton = (SwitchButton) this.findViewById(R.id.imgviewbtn);
		if (mypre.getString(Settings.PREF_AUTORUN, "1").equals("1")) {
			mSlipButton.setChecked(true);
		} else {
			mSlipButton.setChecked(false);
		}
	    mSlipButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				boolean flag = mypre.getString(Settings.PREF_AUTORUN, "1")
						.equals("1");
				SharedPreferences.Editor editor = mypre.edit();
				editor.putString(Settings.PREF_AUTORUN, flag ? "0" : "1");
				editor.commit();
				if (flag) {
					mSlipButton.setChecked(false);
				} else {
					mSlipButton.setChecked(true);
				}
			}
		});
//	    mSlipButton.SetOnChangedListener(new OnSlipChangedListener() {
//			@Override
//			public void OnChanged(boolean CheckState) {
//				// TODO Auto-generated method stub
//				mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
//				SharedPreferences.Editor editor = mypre.edit();
//				if (CheckState) {
//					editor.putString(Settings.PREF_AUTORUN, "1");
//					//Toast.makeText(SettingNew.this, "打开了", Toast.LENGTH_SHORT).show();
//				} else {
//					editor.putString(Settings.PREF_AUTORUN, "0");
//					//Toast.makeText(SettingNew.this, "关闭了", Toast.LENGTH_SHORT).show();
//				}
//				editor.commit();
//			}
//		});
	    
		// 流量监控
		lineflow = (LinearLayout) findViewById(R.id.lineflow);
		if (!DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS) {
			lineflow.setVisibility(View.GONE);
			LinearLayout flowline = (LinearLayout)findViewById(R.id.lineflow_underline);
			flowline.setVisibility(View.GONE);
		}
		lineflow.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (MemoryMg.getInstance().User_3GTotal > 0)
					startActivity(new Intent(SettingNew.this,
							TotalFlowView.class));
				else
					Toast.makeText(SettingNew.this, getResources().getString(R.string.monitoring_notify),
							Toast.LENGTH_LONG).show();
			}
		});
		// 高级
		lineSuper = (LinearLayout) findViewById(R.id.linesuper);
		lineSuper.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(SettingNew.this,AdvancedChoice.class));
				// TODO LLL
			}
		});
		// 更新
		lineupdate = (LinearLayout) findViewById(R.id.lineupdate);
		if(DeviceInfo.CONFIG_CHECK_UPGRADE){
			lineupdate.setVisibility(View.VISIBLE);
			lineupdate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					final String serverIp =mypre.getString(Settings.PREF_SERVER, Settings.DEFAULT_SERVER);
					if(!TextUtils.isEmpty(serverIp) && serverIp.split("\\.").length == 4){
						if(pd == null){
							//更新提示框
							pd = new ProgressDialog(SettingNew.this);
							pd.setMessage(getResources().getString(R.string.setting_updating));
							pd.setCancelable(false);
						}
						if(pd != null){
							pd.show();
						}
						new Thread(new Runnable() {
							public void run() {
								Looper.prepare();
								UpdateVersionService service = new UpdateVersionService(SettingNew.this,serverIp);
								service.checkUpdate(true);
								hd.sendEmptyMessage(CHECKUPDATEOVER);
								Looper.loop();
							}
						}).start();
					}else{
						MyToast.showToast(true, SettingNew.this, getResources().getString(R.string.ip_wrong));//应该不会执行到这里
					}
				}
			});
		}
		else{
			findViewById(R.id.linesuper_line).setVisibility(View.GONE);
			lineupdate.setVisibility(View.GONE);
		}
		
		// 关于
		lineabout = (LinearLayout) findViewById(R.id.lineabout);
		lineabout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(SettingNew.this,AboutActivity.class));
			}
		});
		// 注销
		
		loginout = (Button) findViewById(R.id.loginout);
		if(DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN)
			loginout.setVisibility(View.GONE);
		loginout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TalkBackNew.getInstance().unregisterPttGroupChangedReceiver();
				Tools.cleanGrpID();
				DeviceInfo.svpnumber="";
				DeviceInfo.https_port="";
				DeviceInfo.http_port="";
				DeviceInfo.defaultrecnum="";
				SharedPreferences.Editor editor = mypre.edit();
//				editor.putString(Settings.PREF_USERNAME, Settings.DEFAULT_USERNAME);//modify by hu 保留账号
				editor.putString(Settings.PREF_PASSWORD, Settings.DEFAULT_PASSWORD);
				editor.commit();
				//invoke Tools#onPreLogOut() instead of exit application. modify by mou 2104-11-02
//				SipUAApp.exit();
				Tools.onPreLogOut();
				//clear username pwd add by hu 2014/3/21
				Settings.mUserName = null;
				Settings.mPassword = null;
				//exit app
				Intent it = new Intent(SettingNew.this, FlowRefreshService.class);
				SettingNew.this.stopService(it);
				Receiver.engine(SettingNew.this).expire(-1);
				//解决注销后未清除通知栏导致点击通知栏进入集群通主页面的问题 modify by liangzhang 2014-10-28
				Receiver.onText(Receiver.MISSED_CALL_NOTIFICATION, null, 0, 0);
				// 清空保存的通知栏数据，解决注销后，切换系统语言时，出现通知栏更新的问题。 add by lwang 2014-11-13
				SharedPreferences sharedPreferences = SettingNew.this
						.getSharedPreferences("notifyInfo", Context.MODE_PRIVATE);
				sharedPreferences.edit().clear().commit();
				// 延迟
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// deleted by hdf 停止
				Receiver.engine(SettingNew.this).halt();
				// 停止服务
				stopService(new Intent(SettingNew.this, RegisterService.class));
				stopService(new Intent(SettingNew.this, AlarmService.class));
				DeviceInfo.ISAlarmShowing = false;
				// 取消全局定时器
				Receiver.alarm(0, OneShotAlarm.class);
				Receiver.alarm(0, MyHeartBeatReceiver.class); //停止上报
				
				sendBroadcast(new Intent("com.zed3.sipua.exitActivity"));
				Intent loginoutIntent = new Intent(SettingNew.this,SplashActivity.class);
				loginoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(loginoutIntent);
				SettingNew.this.finish();
			}
		});
		InitRadioButton();
		
	}
	
	
	// 初始化
	private void InitRadioButton() {
		//开机自启
		String autorun = mypre.getString(Settings.PREF_AUTORUN, Settings.DEFAULT_PREF_AUTORUN);//
		if(autorun.equals(Settings.DEFAULT_PREF_AUTORUN)) 
		{
			if(mSlipButton!=null)
				mSlipButton.setChecked(true);
		}
		else
		{	
			if(mSlipButton!=null)
				mSlipButton.setChecked(false);
		}
		
	}


	@Override
	public void onClick(View v) {
//		if(v == backBtn){
//			SettingNew.this.finish();
//			SipUAApp.mContext.sendBroadcast(new Intent(
//					"android.intent.action.RestartUnionLogin"));
//		}
	}
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// TODO Auto-generated method stub
//		menu.add(0, 1, 0, "退出").setIcon(R.drawable.exit);
//		return super.onCreateOptionsMenu(menu);
//	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
//			SettingNew.this.finish();
//			sendBroadcast(new Intent("com.zed3.sipua.exitActivity").putExtra("exit", true));
			Tools.exitApp(SettingNew.this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
