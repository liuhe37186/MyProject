package com.zed3.utils;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.zed3.sipua.SipUAApp;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
/**
 * 向logcat统一控制输出log
 * 发布生产{@link Zed3Log#DEBUG} 置为false,不输出调试log
 * 输出某个tag的log需要向{@link Zed3Log#sTagList} 注册
 * @author wei.deng 2014-05-29
 */
public final class Zed3Log {

	public static boolean DEBUG = false;
	private static boolean DEBUG_FILE = false;
	
	private static ArrayList<String> sTagList = new ArrayList<String>();
	public static String CURRENT_LOG_TAG = "";
	
	
	private static final String LOG_PREFIX = "GQT==>";
	
	private static final String LOG_FILE_NAME = "ptt_trace.log";
	
	
	static {
		sTagList.add("heatbeat");
		sTagList.add("pttkey");
		sTagList.add("processtime");
	}
	
	public static void setTagList(ArrayList<String> list){
		sTagList = list;
	}
	
	public static void i(String tag, String log){
		if(DEBUG) {
			Log.i("xxxx", log);
		}
	}
	
	public static void i(String log){
		if(DEBUG) {
			Log.i("xxxx", log);
		}
	}
	
	public static void e(String log){
		if(DEBUG) {
			Log.e("xxxx", log);
		}
	}
	
	public static void e(String tag,String log){
		if(DEBUG) {
			Log.e(tag, log);
		}
	}
	
	public static void writeLog(String log){
		if(DEBUG) {
			Log.e("xxxx", LOG_PREFIX+log);
			writeLog2File(log);
		}
	}
	
	private static String getLogPrefix(){
		return "Thread name = " + Thread.currentThread().getName() + " , id = " + Thread.currentThread().getId();
	}
	
	public static void debug(String tag,String log){
		if(DEBUG) {
			String result = LOG_PREFIX + log;
			String logPrefix = getLogPrefix();
			Log.i(tag, result);
//			Log.w(tag,logPrefix);
			writeLog2File(result  + " , " +  logPrefix);
		}
	}
	
	public static void debugE(String tag,String log){
		if(DEBUG) {
			String result = LOG_PREFIX + log;
			String logPrefix = getLogPrefix();
			Log.e(tag, result);
//			Log.w(tag,logPrefix);
			writeLog2File(result + " , " + logPrefix);
		}
	}
	
	private static FileWriter fileWriter;
	private static String deviceInfoStr;
	private static StringBuilder sb;
	
	private static void writeLog2File(String mMessageStr) {
		// TODO Auto-generated method stub
		if(!DEBUG_FILE) return;
		// TODO Auto-generated method stub
//		StringWriter wr = new StringWriter();
//		PrintWriter pw = new PrintWriter(wr);
		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				
				sb = new StringBuilder();
				sb.append("\r\n"+mMessageStr);
				String time = " TIME:" + getTimeString();
				sb.append(time);
				String log = sb.toString();
				
				if (fileWriter == null) {
					String dirName = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + File.separator + "com.zed3.sipua.3GPTT";
					File dir = new File(dirName);
					if (!dir.exists())
						dir.mkdir();
//					File file = new File(Environment.getExternalStorageDirectory(),
//						"com.zed3.sipua" +
//						".log");
					File file = new File(dir, LOG_FILE_NAME);
					//add by oumogang 2013-09-11 
					if (!file.exists()){
						file.createNewFile();
					}
					if (file.length()>1024*1024*5) {
						file.delete();
						file = new File(dir, LOG_FILE_NAME);
					}
					fileWriter = new FileWriter(file, true);
				}
				fileWriter.write(log);
				fileWriter.flush();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static SimpleDateFormat formatter;
	private static String getTimeString() {
		// TODO Auto-generated method stub
		if (formatter == null) {
			formatter = new SimpleDateFormat(
					" yyyy-MM-dd hh:mm:ss SSS ");
		}
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// 获取当前时
		return formatter.format(curDate);
	}
	
	private static String getDeviceInfo() {
		// TODO Auto-generated method stub
		if (deviceInfoStr == null) {
			// 获取当前手机操作系统的信息.
			String version = null;
			try {
				PackageInfo packinfo = SipUAApp.mContext.getPackageManager().getPackageInfo(
						SipUAApp.mContext.getPackageName(), 0);
				version = packinfo.versionName;
				//add by oumogang 2013-10-28
				PackageInfo packageInfo = SipUAApp.mContext.getPackageManager().getPackageInfo(SipUAApp.mContext.getPackageName(), 0);
				deviceInfoStr = ("\r\n"+packageInfo.versionName);
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);// 暴力反射,可以获取私有成员变量的信息
				String name = field.getName();
				String value = "";
				try {
					value = field.get(null).toString();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (/*name.equalsIgnoreCase("PRODUCT")||*/name.equalsIgnoreCase("MODEL")) {
					deviceInfoStr+=("\r\n"+name + "=" + value);
					break;
				}
			}
			deviceInfoStr+=("\r\nSDK:"+Build.VERSION.SDK_INT+"  RELEASE:"+Build.VERSION.RELEASE+" VERSION:"+version);
		}
		return deviceInfoStr;
	}
	
}
