package com.zed3.log;
import android.util.Log;
/**
 * 一个具有开关的日志工具 代替系统的Log类
 * @author oumogang
 *
 */
public class Logger {
	private static int LOGLEVEL = 7;
	private static int VERBOSE = 1;
	private static int DEBUG = 2;
	private static int INFO = 3;
	private static int WARN = 4;
	private static int ERROR = 5;
	public static boolean isDebug = true;
	
	
	public static void v(boolean needLog, String tag,String msg){
		if(needLog&&LOGLEVEL>VERBOSE)
		Log.v(tag, msg);
	}
	public static void d(boolean needLog, String tag,String msg){
		if(needLog&&LOGLEVEL>DEBUG)
		Log.d(tag, msg);
	}
	public static void i(boolean needLog, String tag,String msg){
		if(needLog&&LOGLEVEL>INFO)
		Log.i(tag, msg);
	}
	public static void w(boolean needLog, String tag,String msg){
		if(needLog&&LOGLEVEL>WARN)
		Log.w(tag, msg);
	}
	public static void e(boolean needLog, String tag,String msg){
		if(needLog&&LOGLEVEL>ERROR)
		Log.e(tag, msg);
	}
	public static void e(String tag,String msg){
			Log.e(tag, msg);
	}
	public static void i(String tag,String msg){
		Log.e(tag, msg);
	}
	public static void w(String tag,String msg){
		Log.e(tag, msg);
	}
	public static void v(String tag,String msg){
		Log.e(tag, msg);
	}
}
