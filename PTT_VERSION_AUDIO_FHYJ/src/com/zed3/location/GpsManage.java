package com.zed3.location;

import org.zoolu.tools.MyLog;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class GpsManage {
	private static GpsManage instance = null;

	private static final String TAG = "GpsManage";
	Context context;
	// gps
	private LocationManager mLocationManager;
	private Location location = null;
	private String provider = "";

	private int count = 0;
	private int upTime = 0;
	private int upTimeCycle = 0, upTimeOut = 0;
	private double lstLat = 0, lstLng = 0;

	// ����ģʽ
	public static GpsManage getInstance(Context context) {
		if (instance == null)
			instance = new GpsManage(context);
		return instance;
	}

	public GpsManage(Context context) {
		this.context = context;
		count = 0;
		upTime = 0;// MemoryMg.getInstance().gpsSecond;
		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		// ʹ�ñ�׼���ϣ���ϵͳ�Զ�ѡ����õ����λ���ṩ�����ṩλ��
		Criteria criteria = new Criteria();
		// Criteria.ACCURACY_COARSE ģ�� .ACCURACY_FINE �߾���
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);// �߾���
		criteria.setAltitudeRequired(true);// ��Ҫ�󺣰�
		criteria.setBearingRequired(false);// ��Ҫ��λ
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);// �͹���
		// gps�п���
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| mLocationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			// �ӿ��õ�λ���ṩ���У�ƥ�����ϱ�׼������ṩ��
			provider = mLocationManager.getBestProvider(criteria, true);

			if (upTime == 0) {
				upTime = 15;
				upTimeCycle = 13;
				upTimeOut = 14;
			} else if (upTime == 1 || upTime == 2) {
				upTimeCycle = 1;
				upTimeOut = 1;

				// ����λ�ñ仯��Ĭ��1��һ�Σ�����1������ �ż�����λ�� ֻ�Ǽ���λ�ñ仯
				mLocationManager.requestLocationUpdates(provider, 1000, 1,
						locationListener);
				return;
			} else {
				upTimeCycle = upTime - 2;
				upTimeOut = upTime - 1;
			}

			// ����λ�ñ仯��Ĭ��15��һ�Σ�����1������ �ż�����λ�� ֻ�Ǽ���λ�ñ仯
			mLocationManager
					.requestLocationUpdates(provider,
							GpsTools.GetLocationTimeValByModel(MemoryMg
									.getInstance().GpsSetTimeModel) * 1000, 1,
							locationListener);

			MyLog.e(TAG, "GpsManage 1");
		} else {
			// ���ñ���
			ShowInfo(null);
			MyLog.e(TAG, "GpsManage 2");
		}

	}

	// GPSλ�ü�����
	private final LocationListener locationListener = new LocationListener() {
		// ��λ�ñ仯ʱ����1
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			// ShowInfo(getLastPosition());
			MyLog.e(TAG, "onLocationChanged");
			if (location != null) {
				count = 0;
				MyLog.e(TAG,
						"onLocationChanged Latitude:" + location.getLatitude()
								* 1E6 + " Longitude:" + location.getLongitude()
								* 1E6);
			} else
				MyLog.e(TAG, "onLocationChanged location is null");
		}

		// providerʧЧʱ����
		// �ر�gps
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			ShowInfo(null);
			// ����һ����־λ
			MyLog.e(TAG, "onProviderDisabled");
		}

		// provider����ʱ����
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			MyLog.e(TAG, "onProviderEnabled");
		}

		// 2
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			MyLog.e(TAG, "onStatusChanged");

		}

	};

	// ��ȡ����
	private GpsInfo getLastPosition() {
		MyHandlerThread mHandlerThread = MyHandlerThread.getMHThreadInstance();
		GpsInfo info = new GpsInfo();
		try {
			// ������һ�α仯��λ��
			location = mLocationManager.getLastKnownLocation(provider);
			if (location != null) {
//				info.gps_x = (double) location.getLongitude();
//				info.gps_y = (double) location.getLatitude();
//				info.gps_speed = location.getSpeed();
//				info.gps_height = (float) location.getAltitude();
//				info.gps_direction = 0;
//				info.UnixTime = GpsTools.getUnixTime();
//				info.E_id = GpsTools.getE_id();
//				mHandlerThread.sendMessage(Message.obtain(mHandlerThread.mInnerHandler,1,info));
			} else {
				MyLog.e(TAG, "info is null");
				info = null;
			}
			MyLog.e(TAG, "gpsState is true");

			// ����豸�ر�gps�ϴ�����
		} catch (Exception e) {
			MyLog.e(TAG, "info exception is null " + e.toString());
			info = null;
		}
		return info;
	}

	private GpsInfo ShowInfo(GpsInfo info) {
		return info;
	}

	// �ⲿ���ø÷���
	public GpsInfo GetValueGpsStr() {
		// ��־λ
		// ���ؿ�ֵ
		// ����
		return ShowInfo(getLastPosition());

	}

	// �ر�gps
	public void CloseGPS() {

		if (instance == null)
			return;
		
		count = 0;
		try {// �ر�gps
			mLocationManager.removeUpdates(locationListener);
		} catch (Exception e) {
			Log.e("", "", e);
		}

		if (instance != null)
			instance = null;
		MyLog.e(TAG, "CloseGPS");
	}

}
