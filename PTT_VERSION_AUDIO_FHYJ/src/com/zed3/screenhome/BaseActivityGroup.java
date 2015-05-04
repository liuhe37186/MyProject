package com.zed3.screenhome;

import com.zed3.sipua.welcome.DeviceInfo;

import android.app.ActivityGroup;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivityGroup extends ActivityGroup {
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
			return true;}
		}
		return super.onKeyDown(keyCode, event);
	}
}
