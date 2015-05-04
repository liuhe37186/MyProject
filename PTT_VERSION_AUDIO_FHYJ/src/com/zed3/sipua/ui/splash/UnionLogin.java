package com.zed3.sipua.ui.splash;

import org.zoolu.tools.MyLog;


import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.DES;
import com.zed3.utils.MD5;
import com.zed3.utils.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UnionLogin extends Activity {
	private final String sharedPrefsFile = "com.zed3.sipua_preferences";
	private Button loginBtn = null;
	private EditText editTxt = null;
	UserAgent ua = null;
	SharedPreferences mypre = null;

	BroadcastReceiver loginReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isSuccess = intent.getBooleanExtra("loginstatus", false);
			if (isSuccess) {
				if (isError) {// ���ݲ�������
					// Ȼ��
					ua = Receiver.GetCurUA();
					// Ӧ�üӵȴ�״̬��
					mypre = getSharedPreferences(sharedPrefsFile,
							Activity.MODE_PRIVATE);
					//
					CalActivity(editTxt.getText().toString());
				} else {
					
					// Ȼ��
					ua = Receiver.GetCurUA();
					// Ӧ�üӵȴ�״̬��
					mypre = getSharedPreferences(sharedPrefsFile,
							Activity.MODE_PRIVATE);
					// ��¼��ť״̬Ϊ�ɱ༭
					loginBtn.setEnabled(true);
					// �Ƿ����пͷ�����
					if (!mypre.getString("unionpassword", "0").equals("0")) {
						// ��info��֤����
						ua.UploadUnionPwd(
								mypre.getString("unionpassword", "0"));
						
						startActivity(new Intent(UnionLogin.this,
								MainActivity.class));
						finish();
						return;//�����⣿������
					}
					CalActivity(editTxt.getText().toString());
				}
			} else {
				Receiver.engine(UnionLogin.this).halt();
				new AlertDialog.Builder(UnionLogin.this)
						.setTitle(R.string.information)
						.setMessage(getResources().getString(R.string.the_wrong_pwd))
						.setPositiveButton(getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										UnionLogin.this.finish();
									}
								}).create().show();
			}
		}

	};

	private boolean isError = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Bundle bund = getIntent().getExtras();
		if (bund != null)
			isError = bund.getBoolean("unionepwderror");

		registerReceiver(loginReceiver,
				new IntentFilter("com.zed3.sipua.login"));

		if (!isError) {// ���ݲ�������
			if (Receiver.mSipdroidEngine != null
					&& Receiver.mSipdroidEngine.isRegistered(true)) {
				CheckLogin();
			} 
			else{
				mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
				// �Ƿ񱣴�ͷ�����
				if (!mypre.getString("unionpassword", "0").equals("0"))
				{
					// Ȼ��
					ua = Receiver.GetCurUA();
					ua.UploadUnionPwd(mypre.getString("unionpassword", "0"));
					startActivity(new Intent(UnionLogin.this,
							MainActivity.class));
					finish();

				}
			}
		}

		//
		setContentView(R.layout.unionlogin);
		
		loginBtn = (Button) findViewById(R.id.loginbtn);
		loginBtn.setEnabled(true);
			
		loginBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String pwd = editTxt.getText().toString();
				if (pwd.length() < 1)
					return;

				// ���ݲ���������������ע��
				//Receiver.engine(UnionLogin.this).registerMore();
				
				if(Receiver.mSipdroidEngine == null){
					Receiver.engine(UnionLogin.this);
				}else{
					Receiver.mSipdroidEngine.StartEngine();
				}
				
				loginBtn.setEnabled(false);
			}
		});
		editTxt = (EditText) findViewById(R.id.editTxt);

	}

	private void CheckLogin() {
		// Ȼ��
		ua = Receiver.GetCurUA();

		// Ӧ�üӵȴ�״̬��
		mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
		// �Ƿ񱣴�ͷ�����
		if (!mypre.getString("unionpassword", "0").equals("0")) {
			// ��info��֤����
			ua.UploadUnionPwd(mypre.getString("unionpassword", "0"));
			startActivity(new Intent(UnionLogin.this,
					MainActivity.class));
			finish();
		}

	}
	
	private String fetchUserName() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"ServerSet", Context.MODE_PRIVATE);
		return sharedPreferences.getString("UserName", "");
	}
	//
	private void CalActivity(String pwd) {
		try {
			// md5ֵ   �û���
			String toMd5 = MD5.toMd5(fetchUserName());
			// ȡMD5��ǰ4λ�ͺ�4λ��Ϊ��Կ
			String keyStr = toMd5.substring(0, 4) + toMd5.substring(28);
			String encVal = DES.encryptDES(pwd, keyStr);

			MyLog.e("unionlogin", "md5:" + toMd5 + " key:" + keyStr
					+ " DesBase64:" + encVal);
			// ��Ϊ�����������ؽ�������Բ���Ҫ�ڴ����ж�
			ua.UploadUnionPwd(encVal);
			if (mypre != null) {
				SharedPreferences.Editor editor = mypre.edit();
				editor.putString("unionpassword", encVal);
				editor.commit();
			}
			startActivity(new Intent(UnionLogin.this,
					MainActivity.class));
			UnionLogin.this.finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(loginReceiver);
	}
}
