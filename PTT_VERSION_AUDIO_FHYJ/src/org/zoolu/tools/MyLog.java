package org.zoolu.tools;

import com.zed3.log.CrashHandler;

public class MyLog {

	public static void e(String tag, String content) {
		android.util.Log.e(tag, content);
		CrashHandler.SaveLog(tag, content);
	}

	//
	public static void v(String tag, String content) {
		android.util.Log.v(tag, content);
		CrashHandler.SaveLog(tag, content);
	}

	//
	public static void i(String tag, String content) {
		android.util.Log.i(tag, content);
		CrashHandler.SaveLog(tag, content);
	}

	public static void d(String tag, String msg) {
		CrashHandler.SaveLog(tag, msg);
	}

}
