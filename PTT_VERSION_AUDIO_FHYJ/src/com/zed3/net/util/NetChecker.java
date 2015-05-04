package com.zed3.net.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.zed3.sipua.R;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

public class NetChecker {

	private static long lastTime;

	public static boolean check(Context mContext, boolean needToast) {
		// TODO Auto-generated method stub
		if (!Tools.isConnect(mContext)) {
			if (checkTime()) {
				TalkBackNew.isRefresh=2;
				MyToast.showToast(needToast, mContext, R.string.network_exception);
				lastTime = System.currentTimeMillis();
			}
			return false;
		}
		return true;
	}

	private static boolean checkTime() {
		long thisTime = System.currentTimeMillis();
		// TODO Auto-generated method stub
		if (lastTime == 0) {// 第一次
			lastTime = thisTime;
			return true;
		} else if (thisTime - lastTime > 3000) {// 间隔3秒以上
			lastTime = thisTime;
			return true;
		}
		return false;
	}

	// ---------------------------
	/**
	 * 网络是否可用
	 * 
	 * @param activity
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {

					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * wifi是否打开---------------只是判断wifi打开关闭，不去关心是否wifi已连接到网络？？？？？？？？
	 */
	public static boolean isWifiEnabled(Context context) {
		ConnectivityManager mgrConn = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mgrTel = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return ((mgrConn.getActiveNetworkInfo() != null && mgrConn
				.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel
				.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
	}

	/**
	 * 判断当前网络是否是wifi网络?????????????????????????????是否是有效地wifi,即已连接到网络的wifi
	 * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) { //判断3G网
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean isWifi(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	/**
	 * 判断当前网络是否是3G网络?????????????????????????????是否是有效地3G,即已连接到网络的3G
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean is3G(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}
		return false;
	}

}
