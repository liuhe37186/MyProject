package com.zed3.codecs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.zed3.codecs.EncodeRate.Mode;

public class NetworkType {
    /** û������ */
    public static final int NETWORKTYPE_INVALID = 0;
    /** 2G���� */
    public static final int NETWORKTYPE_2G = 1;
    /** 3G��3G�������磬��ͳ��Ϊ�������� */
    public static final int NETWORKTYPE_3G = 2;
    /** wifi���� */
    public static final int NETWORKTYPE_WIFI = 3;
   
    /**�ж��Ƿ���FastMobileNetWork����3G����3G���ϵ������Ϊ�������� */
        
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
	 * ��ȡ����״̬��wifi,2g,3g���ͳ�ʼ����ģʽ��Ӧ.
	 *
	 * @param context ������
	 * @return Mode ��ǰ��������ģʽ����״̬
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
