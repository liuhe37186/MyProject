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

	// 单例模式
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
		// 使用标准集合，让系统自动选择可用的最佳位置提供器，提供位置
		Criteria criteria = new Criteria();
		// Criteria.ACCURACY_COARSE 模糊 .ACCURACY_FINE 高精度
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);// 高精度
		criteria.setAltitudeRequired(true);// 不要求海拔
		criteria.setBearingRequired(false);// 不要求方位
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗
		// gps有开启
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| mLocationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			// 从可用的位置提供器中，匹配以上标准的最佳提供器
			provider = mLocationManager.getBestProvider(criteria, true);

			if (upTime == 0) {
				upTime = 15;
				upTimeCycle = 13;
				upTimeOut = 14;
			} else if (upTime == 1 || upTime == 2) {
				upTimeCycle = 1;
				upTimeOut = 1;

				// 监听位置变化，默认1秒一次，距离1米以上 才监听到位置 只是监听位置变化
				mLocationManager.requestLocationUpdates(provider, 1000, 1,
						locationListener);
				return;
			} else {
				upTimeCycle = upTime - 2;
				upTimeOut = upTime - 1;
			}

			// 监听位置变化，默认15秒一次，距离1米以上 才监听到位置 只是监听位置变化
			mLocationManager
					.requestLocationUpdates(provider,
							GpsTools.GetLocationTimeValByModel(MemoryMg
									.getInstance().GpsSetTimeModel) * 1000, 1,
							locationListener);

			MyLog.e(TAG, "GpsManage 1");
		} else {
			// 设置变量
			ShowInfo(null);
			MyLog.e(TAG, "GpsManage 2");
		}

	}

	// GPS位置监听器
	private final LocationListener locationListener = new LocationListener() {
		// 当位置变化时触发1
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

		// provider失效时调用
		// 关闭gps
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			ShowInfo(null);
			// 设置一个标志位
			MyLog.e(TAG, "onProviderDisabled");
		}

		// provider启用时调用
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

	// 获取坐标
	private GpsInfo getLastPosition() {
		MyHandlerThread mHandlerThread = MyHandlerThread.getMHThreadInstance();
		GpsInfo info = new GpsInfo();
		try {
			// 获得最后一次变化的位置
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

			// 如果设备关闭gps上传功能
		} catch (Exception e) {
			MyLog.e(TAG, "info exception is null " + e.toString());
			info = null;
		}
		return info;
	}

	private GpsInfo ShowInfo(GpsInfo info) {
		return info;
	}

	// 外部调用该方法
	public GpsInfo GetValueGpsStr() {
		// 标志位
		// 返回空值
		// 否则
		return ShowInfo(getLastPosition());

	}

	// 关闭gps
	public void CloseGPS() {

		if (instance == null)
			return;
		
		count = 0;
		try {// 关闭gps
			mLocationManager.removeUpdates(locationListener);
		} catch (Exception e) {
			Log.e("", "", e);
		}

		if (instance != null)
			instance = null;
		MyLog.e(TAG, "CloseGPS");
	}

}
