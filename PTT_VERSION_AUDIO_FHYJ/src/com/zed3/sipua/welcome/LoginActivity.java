package com.zed3.sipua.welcome;

import java.lang.ref.WeakReference;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

public class LoginActivity extends BaseActivity {

	ProgressDialog pd = null;
	EditText et_userName, et_pwd, et_port;
	AutoConfigManager acm;
//	private ImageView imageTitle,imageBottom;
	boolean  beginNetState = true;
	// for ip edit by hu

	EditText first, second, third, forth;
	EditText[] dd = { first, second, third, forth };
	int cursorIndex = 0;
	String text;

	public void showIp(View v) {
		Toast.makeText(LoginActivity.this, packetIp(), Toast.LENGTH_SHORT)
				.show();
	}

	private String packetIp() {
		String d = "";
		for (int i = 0; i < 4; i++) {
			d += dd[i].getText().toString() + ".";
		}
		return d.substring(0, d.length() - 1);
	}

	private void setIp(String ip) {
		if (!TextUtils.isEmpty(ip.replace(".", ""))) {
			String[] ips = ip.split("\\.");
			if (ips.length == 4) {
				for (int i = 0; i < 4; i++) {
					dd[i].setText(ips[i]);
				}
			}
		}
	}

	LinearLayout ll = null;
	CheckBox chkbtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.login);
//		imageTitle = (ImageView) findViewById(R.id.imageTitle);
//		imageBottom = (ImageView) findViewById(R.id.imageBottom);
//		String langue = Locale.getDefault().getLanguage();
//		if(langue.equals("en")){
//			imageTitle.setBackgroundResource(R.drawable.login_bottom);
//			imageBottom.setBackgroundResource(R.drawable.login_head);
//		}
		// for ip add by hu 0820
		dd[0] = (EditText) findViewById(R.id.first);
		dd[1] = (EditText) findViewById(R.id.second);
		dd[2] = (EditText) findViewById(R.id.third);
		dd[3] = (EditText) findViewById(R.id.forth);
		dd[0].setOnKeyListener(new MyOnKeyListener(0));
		dd[1].setOnKeyListener(new MyOnKeyListener(1));
		dd[2].setOnKeyListener(new MyOnKeyListener(2));
		dd[3].setOnKeyListener(new MyOnKeyListener(3));
		dd[0].addTextChangedListener(new MyTextWatch(0));
		dd[1].addTextChangedListener(new MyTextWatch(1));
		dd[2].addTextChangedListener(new MyTextWatch(2));
		dd[3].addTextChangedListener(new MyTextWatch(3));

		acm = new AutoConfigManager(this);
		// userClear = (ImageView) findViewById(R.id.user_clear);
		// pwdClear = (ImageView) findViewById(R.id.pwd_clear);
		et_pwd = (EditText) findViewById(R.id.pwd);
		et_pwd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });
		et_pwd.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// if (s.length() > 0) {
				// pwdClear.setVisibility(View.VISIBLE);
				// } else {
				// pwdClear.setVisibility(View.INVISIBLE);
				// }
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		et_userName = (EditText) findViewById(R.id.username);
		et_userName.requestFocus();
		et_userName
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });
		et_userName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// if (s.length() > 0) {
				// userClear.setVisibility(View.VISIBLE);
				// } else {
				// userClear.setVisibility(View.INVISIBLE);
				// }
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		// userClear.setOnClickListener(this);
		// pwdClear.setOnClickListener(this);
		et_port = (EditText) findViewById(R.id.port);

		ll = (LinearLayout) findViewById(R.id.advancedPannel);
		chkbtn = (CheckBox) findViewById(R.id.chkbtn);
		chkbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// if(ll.getVisibility() == View.INVISIBLE){
				// ll.setVisibility(View.VISIBLE);
				// }else if(ll.getVisibility() == View.VISIBLE){
				// ll.setVisibility(View.INVISIBLE);
				// }
				if (isChecked){
					ll.setVisibility(View.VISIBLE);
					ll.requestFocus();
				} else
					ll.setVisibility(View.GONE);
			}
		});
		
		// 显示上次保存的ip和端口和账号密码,
		String username = acm.fetchLocalUserName();
		String pwd = acm.fetchLocalPwd();
		String server = acm.fetchLocalServer();
		String port = acm.fetchLocalPort();

		et_userName.setText(username);
		et_pwd.setText(acm.fetchLocalPwd());
		et_port.setText(acm.fetchLocalPort());
		setIp(acm.fetchLocalServer());
//		registerReceiver(loginReceiver,
//				new IntentFilter("com.zed3.sipua.login"));
		beginNetState = NetChecker.check(this, false);
		// 检测不为空，则自动登陆，登陆失败停留在登陆页面
		if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)
				&& !TextUtils.isEmpty(server) && !TextUtils.isEmpty(port)) {
			login(true);
		}
		registerReceiver(br, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		
		// delete by liangzhang 2014-09-09 need not check here
		//add by hu 2014/5/28
		// int val = DeviceInfo.CONFIG_GPS;
		// if (val > 3 || val < 0) {
		// val = 3;
		// }
		// if(PreferenceManager
		// .getDefaultSharedPreferences(this).getInt(Settings.PREF_LOCATEMODE,
		// -1) == -1){
		// MemoryMg.getInstance().GpsLocationModel = val;
		// Editor it = PreferenceManager
		// .getDefaultSharedPreferences(this)
		// .edit();
		// it.putInt(Settings.PREF_LOCATEMODE, val);//0为gps 1为基站 2为基站GPS 3 从不定位
		// it.commit();
		// }
		finish();
	}

	public void login(View v) {
		login(true);
	}
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	//解决登陆界面点击HOME按钮退出，此时切换系统语言，CheckBox的Text不随系统语言改变的问题。 add by lwang 2014-11-14
    	chkbtn.setText(R.string.server_ip);
    	super.onResume();
    }
	private void login(boolean flag) {// 是否显示进度对话框
		if (checkEditText(et_userName)) {
			String userName = et_userName.getText().toString();
			if (checkEditText(et_pwd)) {
				String pwd = et_pwd.getText().toString();
				String server = packetIp();
				if (!TextUtils.isEmpty(server.replace(".", ""))
						&& !server.contains("..") && !server.endsWith(".")
						&& !server.startsWith(".")) {
					if (checkEditText(et_port)) {
						if (NetChecker.check(LoginActivity.this, false)) {
							if (flag) {
								if (pd == null) {
									pd = new ProgressDialog(LoginActivity.this);
									pd.setMessage(getResources().getString(R.string.loginning));
									pd.show();
									pd.setOnCancelListener(new OnCancelListener() {

										@Override
										public void onCancel(
												DialogInterface dialog) {
											Tools.exitApp(LoginActivity.this);
										}
									});
									pd.setCanceledOnTouchOutside(false);
								}
							}
						} else {
							Message msg = new Message();
							msg.what = 0;
							Bundle data = new Bundle();
							data.putString("result", "netbroken");
							msg.setData(data);
							hd.sendMessage(msg);
							return;
						}

						String port = et_port.getText().toString();
						acm.saveSetting(userName, pwd, server, port);
						if (Receiver.mSipdroidEngine == null) {// modify by hu
																// 2014/1/22
							Receiver.engine(this);
						} else {
							if (!Receiver.mSipdroidEngine.isRegistered(true)){
								Receiver.mSipdroidEngine.StartEngine();
							}else{
								if (pd != null) {
									pd.dismiss();
								}
								LoginActivity.this.finish();
							}
						}
					} else {
						MyToast.showToast(true, LoginActivity.this, getResources().getString(R.string.port_is_blank));
					}
				} else {
					MyToast.showToast(true, LoginActivity.this, getResources().getString(R.string.server_ip_wrong));
				}
			} else {
				MyToast.showToast(true, LoginActivity.this, getResources().getString(R.string.pwd_is_blank));
			}
		} else {
			MyToast.showToast(true, LoginActivity.this, getResources().getString(R.string.userName_is_blank));
		}
	}

	public void save(View v) {

	}

	public boolean checkEditText(EditText et) {
		if (et.getText().length() < 1) {
			return false;
		}
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		unregisterReceiver(loginReceiver);
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
		unregisterReceiver(br);
	}

	public class MyTextWatch implements TextWatcher {
		int editNum;

		public MyTextWatch(int editNum) {
			this.editNum = editNum;
		}

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		// 输入字符串监听
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			text = s.toString();
			int length = text.length();
			if (length > 0) {
				char c = text.charAt(length - 1);
				if (String.valueOf(c).equals(".")) {
					if (length == 1) {
						charHandler.sendEmptyMessage(editNum);
					} else if (editNum < 3) {
						toStep(editNum + 1);
						charHandler.sendEmptyMessage(editNum);
					} else if (editNum == 3) {
						charHandler.sendEmptyMessage(editNum);
					}
					// if(editNum < 3 && length != 1)
					// hd.sendEmptyMessage(editNum);
				}
			}
			text = text.replace(".", "");
			if (text.length() > 3
					|| (!TextUtils.isEmpty(text) && Integer.parseInt(text) > 255)) {
				if (editNum < 3)
					toStep(editNum + 1);
				charHandler.sendEmptyMessage(editNum);
			} else if (text.length() == 3) {
				if (editNum < 3)
					toStep(editNum + 1);
				else {
					toStep(editNum);
				}
			}
			// if(editNum > 0 && length == 0){
			// toStep(editNum-1, true);
			// }
		}

	}

	// 删除键监听
	public class MyOnKeyListener implements OnKeyListener {
		int editNum = -1;

		public MyOnKeyListener(int editNum) {
			this.editNum = editNum;
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (KeyEvent.KEYCODE_DEL == keyCode) {
				if (dd[editNum].length() == 0) {
					if (editNum > 0)
						toStep(editNum - 1);
				}
			}
			return false;
		}

	}

	// 跳转到指定的文本框
	void toStep(int next) {
		dd[next].requestFocus();
		//modify by zdx 文本框设置焦点后要全选文本，故不能定位光标位置
//		dd[next].setSelection(dd[next].getText().length());
	}

	BroadcastReceiver loginReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isSuccess = intent.getBooleanExtra("loginstatus", false);
			System.out.println("-------loginReceiver result:"+isSuccess);
			if (isSuccess) {
				Intent loginIntent = new Intent();
//				loginIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				loginIntent.setClass(LoginActivity.this, MainActivity.class);
				startActivity(loginIntent);
				LoginActivity.this.finish();
			} else {
				Message msg = new Message();
				msg.what = 0;
				Bundle data = new Bundle();
				data.putString("result", intent.getStringExtra("result"));
				msg.setData(data);
				hd.sendMessage(msg);
			}
		}

	};
	// 收到消息，删除刚才输入的字符串
	Handler charHandler = new MyHandler(LoginActivity.this);

	private static class MyHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		public MyHandler(LoginActivity activity) {
			this.mActivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			EditText temp = mActivity.get().dd[msg.what];
			temp.setText(temp.getText().toString()
					.substring(0, temp.length() - 1));
//			temp.setSelection(mActivity.get().dd[msg.what].getText().length());
		}
	}

	Handler hd = new LoginHandler(LoginActivity.this);

	private static class LoginHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		public LoginHandler(LoginActivity activity) {
			this.mActivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				if (mActivity.get().pd != null) {
					mActivity.get().pd.dismiss();
					mActivity.get().pd = null;
				}
				String result = msg.getData().getString("result");
				if (!"netbroken".equalsIgnoreCase(result)) { // 网络异常 不需要halt
					Receiver.engine(mActivity.get()).halt();
				}
				if (result.contains("passwdIncorrect")) {
					result = SipUAApp.mContext.getResources().getString(R.string.wrong_password);
				} else if (result.contains("userNotExist")) {
					result = SipUAApp.mContext.getResources().getString(R.string.account_not_exist);
				} else if (result.contains("Timeout")) {
					result = SipUAApp.mContext.getResources().getString(R.string.timeout);
				} else if (result.contains("netbroken")) {
					result = SipUAApp.mContext.getResources().getString(R.string.network_exception);
				} else if (result.contains("userOrpwderror")) {
					result = SipUAApp.mContext.getResources().getString(R.string.wrong_name_or_pwd);
				} else if(result.contains("versionTooLow")){
					result = SipUAApp.mContext.getResources().getString(R.string.version_too_low);
				} else if(result.contains("Already Login")){
					result = SipUAApp.mContext.getResources().getString(R.string.logged_in_another_place);
				} else if(result.contains("Temporarily Unavailable")){
					result = SipUAApp.mContext.getResources().getString(R.string.service_unavailable);
				}
				MyToast.showToast(true, mActivity.get(), result);
				break;
			}

		}
	}
	BroadcastReceiver br = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			boolean isConnected = NetChecker.check(LoginActivity.this, false);
			if(beginNetState != isConnected && !beginNetState){
				login(true);
			} 
			beginNetState = isConnected;
		};
	};
}
