package com.zed3.location;

import org.zoolu.tools.MyLog;

import android.content.Context;

import com.baidu.location.LocationClient;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.Zed3Log;

public class BDLocation {
	private final String TAG = "BDLocation";
	LocationClient mLocationClient = null;
	Context context;

	//
	public BDLocation(Context context) {
		this.context = context;
		mLocationClient = SipUAApp.mLocationClient;
		GpsTools.Previous_gps_x = 0;
		GpsTools.Previous_gps_y = 0;
		GpsTools.Previous_UnixTime = 0;
		GpsTools.D_UnixTime = 0;
		MyLog.i(TAG, "BDLocation init");
	}     

//	// ������ز���
//	private void setLocationOption() {
//		LocationClientOption option = new LocationClientOption();
//		option.setServiceName("com.zed3.app");
//		// ��Ҫ��ַ��Ϣ������Ϊ�����κ�ֵ��string���ͣ��Ҳ���Ϊnull��ʱ������ʾ�޵�ַ��Ϣ��
//		option.setAddrType("all");
//		// �����Ƿ񷵻�POI�ĵ绰�͵�ַ����ϸ��Ϣ��Ĭ��ֵΪfalse����������POI�ĵ绰�͵�ַ��Ϣ��
//		option.setPoiExtraInfo(false);
//		// ���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
//		option.setProdName("zed3app");
//		// ����GPS��ʹ��gpsǰ�����û�Ӳ����gps��Ĭ���ǲ���gps�ġ�
//		option.setOpenGps(true);
//		// ��λ��ʱ��������λ��ms
//		// �����������ֵ���ڵ���1000��ms��ʱ����λSDK�ڲ�ʹ�ö�ʱ��λģʽ��
//		option.setScanSpan(10000);//10��
//		// ��ѯ��Χ��Ĭ��ֵΪ500�����Ե�ǰ��λλ��Ϊ���ĵİ뾶��С��
//		option.setPoiDistance(200);
//		// �������û��涨λ����
//		option.disableCache(true);
//		// ����ϵ���ͣ��ٶ��ֻ���ͼ����ӿ��е�����ϵĬ����bd09ll
//		option.setCoorType("bd09ll");
//
//		// �������ɷ��ص�POI������Ĭ��ֵΪ3������POI��ѯ�ȽϺķ�������������෵�ص�POI�������Ա��ʡ������
//		option.setPoiNumber(2);
//		// ���ö�λ��ʽ�����ȼ���
//		// ��gps���ã����һ�ȡ�˶�λ���ʱ�����ٷ�����������ֱ�ӷ��ظ��û����ꡣ���ѡ���ʺ�ϣ���õ�׼ȷ����λ�õ��û������gps�����ã��ٷ����������󣬽��ж�λ��
//		option.setPriority(LocationClientOption.GpsFirst);
//		option.setPoiExtraInfo(true);
//		mLocationClient.setLocOption(option);
//	}
	
	private boolean mIsStarted = false;
	
	// ������λ
	public void StartBDGPS() {
		Zed3Log.debug("testgps", "BDLocation#StartBDGPS enter isStarted = " + (mLocationClient.isStarted()) + " , this ref = " + this + ", location client = " + mLocationClient + " , mIsStarted = " + mIsStarted);
		// ������λ
		if(mLocationClient.isStarted()){
			Zed3Log.debug("testgps", "BDLocation#StartBDGPS stop ");
			mLocationClient.stop();
		}
		GpsTools.setLocationOption(SipUAApp.mLocationClient);
		mLocationClient.start();
		
		Zed3Log.debug("testgps", "BDLocation#StartBDGPS exit");
		MyLog.i(TAG, "ReStartBDGPS");
	}

	
	// ��ͣ��λ
	public void StopBDGPS() {
		if(mLocationClient != null && mLocationClient.isStarted())
			mLocationClient.stop();
		
		MyLog.i(TAG, "StopBDGPS");
	}

}
