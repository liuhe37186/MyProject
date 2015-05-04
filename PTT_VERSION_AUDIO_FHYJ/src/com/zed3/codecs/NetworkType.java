package com.zed3.codecs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.zed3.codecs.EncodeRate.Mode;

public class NetworkType {
    /** 没有网络 */
    public static final int NETWORKTYPE_INVALID = 0;
    /** 2G网络 */
    public static final int NETWORKTYPE_2G = 1;
    /** 3G和3G以上网络，或统称为快速网络 */
    public static final int NETWORKTYPE_3G = 2;
    /** wifi网络 */
    public static final int NETWORKTYPE_WIFI = 3;
   
    /**判断是否是FastMobileNetWork，将3G或者3G以上的网络称为快速网络 */
        
    private  static boolean isFastMobileNetwork(Context context) {
    	TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    		switch (telephonyManager.getNetworkType()) {
    	        case TelephonyManager.NETWORK_TYPE_1xRTT:
    	            return false; // ~ 50-100 kbps
    	        case TelephonyManager.NETWORK_TYPE_CDMA:
    	            return false; // ~ 14-64 kbps
    	        case TelephonyManager.NETWORK_TYPE_EDGE:
    	            return false; // ~ 50-100 kbps
    	        case TelephonyManager.NETWORK_TYPE_EVDO_0:
    	            return true; // ~ 400-1000 kbps
    	        case TelephonyManager.NETWORK_TYPE_EVDO_A:
    	            return true; // ~ 600-1400 kbps
    	        case TelephonyManager.NETWORK_TYPE_GPRS:
    	            return false; // ~ 100 kbps
    	        case TelephonyManager.NETWORK_TYPE_HSDPA:
    	            return true; // ~ 2-14 Mbps
    	        case TelephonyManager.NETWORK_TYPE_HSPA:
    	            return true; // ~ 700-1700 kbps
    	        case TelephonyManager.NETWORK_TYPE_HSUPA:
    	            return true; // ~ 1-23 Mbps
    	        case TelephonyManager.NETWORK_TYPE_UMTS:
    	            return true; // ~ 400-7000 kbps
    	        case TelephonyManager.NETWORK_TYPE_EHRPD:
    	            return true; // ~ 1-2 Mbps
    	        case TelephonyManager.NETWORK_TYPE_EVDO_B:
    	            return true; // ~ 5 Mbps
    	        case TelephonyManager.NETWORK_TYPE_HSPAP:
    	            return true; // ~ 10-20 Mbps
    	        case TelephonyManager.NETWORK_TYPE_IDEN:
    	            return false; // ~25 kbps
    	        case TelephonyManager.NETWORK_TYPE_LTE:
    	            return true; // ~ 10+ Mbps
    	        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
    	            return false;
    	        default:
    	            return false;
    			}
    		}

    /**
	 * 获取网络状态，wifi,2g,3g，和初始编码模式对应.
	 *
	 * @param context 上下文
	 * @return Mode 当前编码速率模式网络状态
	 */

	public  static Mode getNetWorkType(Context context) {

		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();

		int mNetWorkType = NETWORKTYPE_INVALID;
		Mode mode = Mode.MR475;
		if (networkInfo != null && networkInfo.isConnected()) {
			String type = networkInfo.getTypeName();

			if (type.equalsIgnoreCase("WIFI")) {
				mNetWorkType = NETWORKTYPE_WIFI;
				mode = Mode.MR122;
            } else if (type.equalsIgnoreCase("MOBILE")) {
            	
            	mNetWorkType =  (isFastMobileNetwork(context) ? NETWORKTYPE_3G : NETWORKTYPE_2G);
            	
            	if (mNetWorkType == 1)
            		mode = Mode.MR475;
            	else
            		mode = Mode.MR122;
            			
            }
		} else {
			mNetWorkType = NETWORKTYPE_INVALID;
		}
		return mode;
	} 
}
