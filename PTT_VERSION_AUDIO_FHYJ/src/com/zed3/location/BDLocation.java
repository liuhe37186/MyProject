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

//	// 设置相关参数
//	private void setLocationOption() {
//		LocationClientOption option = new LocationClientOption();
//		option.setServiceName("com.zed3.app");
//		// 需要地址信息，设置为其他任何值（string类型，且不能为null）时，都表示无地址信息。
//		option.setAddrType("all");
//		// 设置是否返回POI的电话和地址等详细信息。默认值为false，即不返回POI的电话和地址信息。
//		option.setPoiExtraInfo(false);
//		// 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
//		option.setProdName("zed3app");
//		// 设置GPS，使用gps前提是用户硬件打开gps。默认是不打开gps的。
//		option.setOpenGps(true);
//		// 定位的时间间隔，单位：ms
//		// 当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。
//		option.setScanSpan(10000);//10秒
//		// 查询范围，默认值为500，即以当前定位位置为中心的半径大小。
//		option.setPoiDistance(200);
//		// 禁用启用缓存定位数据
//		option.disableCache(true);
//		// 坐标系类型，百度手机地图对外接口中的坐标系默认是bd09ll
//		option.setCoorType("bd09ll");
//
//		// 设置最多可返回的POI个数，默认值为3。由于POI查询比较耗费流量，设置最多返回的POI个数，以便节省流量。
//		option.setPoiNumber(2);
//		// 设置定位方式的优先级。
//		// 当gps可用，而且获取了定位结果时，不再发起网络请求，直接返回给用户坐标。这个选项适合希望得到准确坐标位置的用户。如果gps不可用，再发起网络请求，进行定位。
//		option.setPriority(LocationClientOption.GpsFirst);
//		option.setPoiExtraInfo(true);
//		mLocationClient.setLocOption(option);
//	}
	
	private boolean mIsStarted = false;
	
	// 开启定位
	public void StartBDGPS() {
		Zed3Log.debug("testgps", "BDLocation#StartBDGPS enter isStarted = " + (mLocationClient.isStarted()) + " , this ref = " + this + ", location client = " + mLocationClient + " , mIsStarted = " + mIsStarted);
		// 开启定位
		if(mLocationClient.isStarted()){
			Zed3Log.debug("testgps", "BDLocation#StartBDGPS stop ");
			mLocationClient.stop();
		}
		GpsTools.setLocationOption(SipUAApp.mLocationClient);
		mLocationClient.start();
		
		Zed3Log.debug("testgps", "BDLocation#StartBDGPS exit");
		MyLog.i(TAG, "ReStartBDGPS");
	}

	
	// 暂停定位
	public void StopBDGPS() {
		if(mLocationClient != null && mLocationClient.isStarted())
			mLocationClient.stop();
		
		MyLog.i(TAG, "StopBDGPS");
	}

}
