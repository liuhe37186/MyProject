package com.zed3.utils;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.contant.Contants;

public class LogUtil {
	private StringBuilder sb;
	private final static byte[] block4MakeLog = new byte[0];
	private FileWriter mFileWriter;
	private File mLogFile;
	private static SimpleDateFormat formatter;
	private static boolean DEBUG = false;
	
	private LogUtil() {
	}
	private static final class InstanceCreater {
		public static LogUtil sInstance = new LogUtil();
	}
	public static LogUtil getInstance() {
		// TODO Auto-generated method stub
		return InstanceCreater.sInstance;
	}
	/**
	 * make log and write to file,add Parameter tag
	 */
	//modify by oumogang 2014-05-08
	public static void makeLog(String tag,String logMsg) {
		// TODO Auto-generated method stub
		synchronized (block4MakeLog) {
			try {
				Log.i(tag, logMsg);
				if (DEBUG ) {
					LogUtil.getInstance().writeLog2File(tag+" "+logMsg);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	private void writeLog2File(String mMessageStr) {
		// TODO Auto-generated method stub
		
		// TODO Auto-generated method stub
//		StringWriter wr = new StringWriter();
//		PrintWriter pw = new PrintWriter(wr);
		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				if (sb == null) {
					sb = new StringBuilder();
				}
//				sb.append("\r\nBluetoothMsgLog:");
//				sb.append("\r\n=========> ");
				sb.append("\r\n"+getDeviceModel());
				String time = "TIME:" + getTimeString();
				sb.append(" "+time);
				sb.append(" Thread:"+Thread.currentThread().getName());
				sb.append(" "+mMessageStr);
//				sb.append("\r\n"+getDeviceInfo());
//				sb.append("\r\n"+getDeviceModel());
//				String time = "\r\nTIME:" + getTimeString();
//				sb.append(time);
//				sb.append(" Thread:"+Thread.currentThread().getName());
//				sb.append("\r\n===============================");
				String log = sb.toString();
				if (mFileWriter == null) {
					initFile();
					mFileWriter = new FileWriter(mLogFile, true);
				}
				if (!mLogFile.exists()) {
					initFile();
					mFileWriter = new FileWriter(mLogFile, true);
				}
				if (mLogFile.length()>1024*1024*50) {
					mFileWriter.close();
					mLogFile.delete();
					saveLastLogFileName(SipUAApp.getAppContext(),"");
					initFile();
					mFileWriter = new FileWriter(mLogFile, true);
				}
				
				mFileWriter.write(log);
				mFileWriter.flush();
//					mFileWriter.close();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (sb != null && sb.length()>0) {
				sb.delete(0, sb.length());
			}
		}
		
	}
	
	
	/**
	 * 如果没有历史文件名，就新建一个文件。
	 */
	private void initFile() {
		// TODO Auto-generated method stub
		String fileName = getLastLogFileName(SipUAApp.getAppContext());
		String dirName = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + "com.zed3.sipua";
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdir();
		
		File[] listFiles = dir.listFiles();
		File file;
		int count = 0;
		boolean isExistsed = false;
		for (int i = 0; i < listFiles.length; i++) {
			file = listFiles[i];
			if (file.getName().equals(fileName)) {
				isExistsed = true;
			}
		}
		if (TextUtils.isEmpty(fileName)||!isExistsed) {
//			File[] listFiles = dir.listFiles();
//			File file;
//			int count = 0;
//			for (int i = 0; i < listFiles.length; i++) {
//				file = listFiles[i];
//				if (file.getName().contains("ZMBluetoothMsgLog")) {
////				count ++;
//					file.delete();
//				}
//			}
			SimpleDateFormat formatter = new SimpleDateFormat(
					"MMdd-hhmm-ss");
			long systemTime = System.currentTimeMillis();
			Date curDate = new Date(systemTime);// 获取当前时
			String time = formatter.format(curDate);
			String filename = "GQT-Log-"+getDeviceModel()+"-"+time+".txt";
			mLogFile = new File(dir, filename);
			saveLastLogFileName(SipUAApp.getAppContext(),filename);
		}else {
			mLogFile = new File(dir, fileName);
		}
	}
	
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
	private String getDeviceInfo() {
		// TODO Auto-generated method stub
		synchronized (LogUtil.this) {
			if (deviceInfoStr == null) {
				// 获取当前手机操作系统的信息.
				String version = null;
				try {
					PackageInfo packinfo = SipUAApp.mContext.getPackageManager().getPackageInfo(
							SipUAApp.mContext.getPackageName(), 0);
					version = packinfo.versionName;
					//add by oumogang 2013-10-28
					PackageInfo packageInfo = SipUAApp.mContext.getPackageManager().getPackageInfo(SipUAApp.mContext.getPackageName(), 0);
					deviceInfoStr = "";
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}finally{
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
							deviceInfoStr += (name + ":" + value);
							break;
						}
					}
					deviceInfoStr += ("\r\nSDK:"+Build.VERSION.SDK_INT+"\r\nRELEASE:"+Build.VERSION.RELEASE+"\r\nVERSION:"+version);
				}
			}
			return deviceInfoStr;
		}
	}
	private String deviceInfoStr;
	private String deviceModelStr;
	private final byte[] block4GetDeviceModel = new byte[0];
	private String getDeviceModel() {
		// TODO Auto-generated method stub
		synchronized (block4GetDeviceModel) {
			if (deviceModelStr == null) {
				// 获取当前手机操作系统的信息.
				String version = null;
				try {
					PackageInfo packinfo = SipUAApp.getAppContext().getPackageManager().getPackageInfo(
							SipUAApp.getAppContext().getPackageName(), 0);
					version = packinfo.versionName;
					//add by oumogang 2013-10-28
					PackageInfo packageInfo = SipUAApp.getAppContext().getPackageManager().getPackageInfo(SipUAApp.getAppContext().getPackageName(), 0);
					deviceModelStr = "";
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
//					deviceModelStr += (name + ":" + value);
						deviceModelStr += value;
						break;
					}
				}
				deviceModelStr += ("["+Build.VERSION.SDK_INT+"]["+Build.VERSION.RELEASE+"]["+version+"]");
			}
		}
		return deviceModelStr;
	}
	private String getLastLogFileName(Context context) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = getSharedPreferences(context);
		return preferences.getString(Contants.KEY_LAST_GQT_MAIN_LOGFILE_NAME, "");
	}
	private void saveLastLogFileName(Context context,String fileName) {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putString(Contants.KEY_LAST_GQT_MAIN_LOGFILE_NAME, fileName);
		edit.commit();
	}

	private SharedPreferences getSharedPreferences(Context context) {
		// TODO Auto-generated method stub
		return context.getSharedPreferences("com.zed3.app", Context.MODE_PRIVATE);
	}

	
}
