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
		if (lastTime == 0) {// ��һ��
			lastTime = thisTime;
			return true;
		} else if (thisTime - lastTime > 3000) {// ���3������
			lastTime = thisTime;
			return true;
		}
		return false;
	}

	// ---------------------------
	/**
	 * �����Ƿ����
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
	 * wifi�Ƿ��---------------ֻ���ж�wifi�򿪹رգ���ȥ�����Ƿ�wifi�����ӵ����磿��������������
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
	 * �жϵ�ǰ�����Ƿ���wifi����?????????????????????????????�Ƿ�����Ч��wifi,�������ӵ������wifi
	 * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) { //�ж�3G��
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
	 * �жϵ�ǰ�����Ƿ���3G����?????????????????????????????�Ƿ�����Ч��3G,�������ӵ������3G
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
