package com.zed3.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Screen Wakeup PendingIntent action receiver. add by mou 2014-12-11
 * @author oumogang
 *
 */
public class ScreenWakeupActionReceiver extends BroadcastReceiver{

	private static final String tag = "ScreenWakeupActionReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		MyPowerManager.getInstance().wakeupScreen(tag,5000);
	}
	
}
