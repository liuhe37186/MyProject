package com.zed3.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestoreReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		Tools.bringtoFront(context);
		if(intent.getAction().equals ("com.zed3.restore")){
			if(Tools.isInBg)
			Tools.bringtoFront(context);
			return;
		}
	}

}
