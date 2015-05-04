package com.zed3.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.net.util.NetChecker;

public final class Systems {

	public static final String EMPTY = "";
	
	public static final Logger log = new Logger();

	public static boolean isConnectedNetwork(Context context) {
		return NetChecker.isNetworkAvailable(context);
	}

	public static int parseInt(String value) {
		int defaultValue = -1;
		if (!TextUtils.isEmpty(value)) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				e.printStackTrace();
				return defaultValue;
			}
		}
		return defaultValue;
	}

	public static String getLocalMacAddress(Context context) {
		if (context == null) {
			return EMPTY;
		}
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = wifi.getConnectionInfo();

		return info.getMacAddress();

	}

	public static String getImei(Context context) {
		if (context == null) {
			return EMPTY;
		}
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceid = tm.getDeviceId();
		return deviceid;
	}

	/**
	 * 获取Imei或macAddress
	 * 
	 * @param context
	 * @return
	 */
	public static String getThid(Context context) {
		String Imei = Systems.getImei(context);
		return (Imei != null ? Imei : Systems.getLocalMacAddress(context));
	}

	/**
	 * 获取版本号
	 * 
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static final class Logger {
		private static final boolean DEBUG = true; 
		public void print(String tag,String log){
			if(DEBUG) {
				Log.i(tag, log);
			}
		}
	}
	
}
