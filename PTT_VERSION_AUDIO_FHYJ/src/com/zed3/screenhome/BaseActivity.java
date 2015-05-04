package com.zed3.screenhome;

import com.zed3.sipua.welcome.DeviceInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK){
		Window localWindow = getWindow();
    	WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
    	localLayoutParams.flags =-2147483648;
    	localLayoutParams.systemUiVisibility = 512;
    	localWindow.setAttributes(localLayoutParams);
    	}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK){
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
				Intent intent_ = new Intent(Intent.ACTION_MAIN);
				intent_.addCategory(Intent.CATEGORY_HOME);
				intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent_);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
