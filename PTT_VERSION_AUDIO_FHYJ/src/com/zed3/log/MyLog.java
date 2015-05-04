package com.zed3.log;

import android.util.Log;

public class MyLog {
	/**���ߺ󣬰�log�ص�������Ϊtrue*/
	private static boolean isClosed = false;

	public static void e(String tag, String msg) {
		if (!isClosed ) {
			Log.e(tag, msg);
			CrashHandler.SaveLog(tag, msg);
		}
	}
	public static void d(String tag, String msg) {
		if (!isClosed ) {
			Log.d(tag, msg);
			CrashHandler.SaveLog(tag, msg);
		}
	}	
	
	public static void i(String tag, String msg) {
		if (!isClosed ) {
			Log.i(tag, msg);
			CrashHandler.SaveLog(tag, msg);
		}
	}	
	public static void v(String tag, String content) {
		if (!isClosed )
			return;
		CrashHandler.SaveLog(tag, content);
	}
}
