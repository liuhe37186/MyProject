package com.zed3.sipua.baiduMap;
import java.util.ArrayList;
import java.util.List;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MapTools {

	private static double PI = Math.PI; // Բ����
	private static double R = 6371.004;// 6371.229; // ����İ�?

	/**
	 * ��λkm �����?//�Ժ��Ż�
	 * longitude ����
	 * 
	 * latitude ά��
	 */
	public static double getDistance(double longt1, double lat1, double longt2,
			double lat2) {
		double x, y, distance;
		x = (longt2 - longt1) * PI * R
				* Math.cos(((lat1 + lat2) / 2) * PI / 180) / 180;
		y = (lat2 - lat1) * PI * R / 180;

		distance = Math.hypot(x, y);
		return distance;

	}
	public static double getDistance(GeoPoint ponit1, GeoPoint ponit2) {
		double x, y, distance;
		double longt1,lat1,longt2,lat2;
		longt1 = ponit1.getLongitudeE6()/1e6;
		longt2 = ponit2.getLongitudeE6()/1e6;
		lat1 = ponit1.getLatitudeE6()/1e6;
		lat2 = ponit2.getLatitudeE6()/1e6;
		x = (longt2 - longt1) * PI * R
				* Math.cos(((lat1 + lat2) / 2) * PI / 180) / 180;
		y = (lat2 - lat1) * PI * R / 180;

		distance = Math.hypot(x, y);
		return distance;

	}

//	public static List<GroupMember> getAllMem(){
//		List<GroupMember> list = new ArrayList<GroupMember>();
//		//demo 
//		double mLon1 = 116.400244 ;
//		double mLat1 = 39.963175 ;
//		double mLon2 = 116.369199;
//		double mLat2 = 39.942821;
//		double mLon3 = 116.425541;
//		double mLat3 = 39.939723;
//		double mLon4 = 116.401394;
//		double mLat4 = 39.906965;
//		 GeoPoint p1 = new GeoPoint((int)(mLat1 * 1E6), (int)(mLon1* 1E6));
//		 GeoPoint p2 = new GeoPoint((int)(mLat2 * 1E6), (int)(mLon2* 1E6));
//		 GeoPoint p3 = new GeoPoint((int)(mLat3 * 1E6), (int)(mLon3* 1E6));
//		 GeoPoint p4 = new GeoPoint((int)(mLat4 * 1E6), (int)(mLon4* 1E6));
//		 GroupMember gm1 = new GroupMember();
//		 gm1.setGeo(p1);
//		 gm1.setName("张三");
//		 gm1.setNum("10086");
//		 gm1.setOnline(false);
//		 list.add(gm1);
//		 GroupMember gm2 = new GroupMember();
//		 gm2.setGeo(p2);
//		 gm2.setName("李四");
//		 gm2.setNum("10010");
//		 gm2.setOnline(true);
//		 list.add(gm2);
//		 GroupMember gm3 = new GroupMember();
//		 gm3.setGeo(p3);
//		 gm3.setName("王五");
//		 gm3.setNum("12306");
//		 gm3.setOnline(false);
//		 list.add(gm3);
//		 GroupMember gm4 = new GroupMember();
//		 gm4.setGeo(p4);
//		 gm4.setName("赵六");
//		 gm4.setNum("95555");
//		 gm4.setOnline(true);
//		 list.add(gm4);
//		return list;
//	}
	public static List<GroupMember> getMemInMiles(GeoPoint mypos, int miles,List<GroupMember> alllist) {
		List<GroupMember> list = new ArrayList<GroupMember>();
		if(alllist != null && alllist.size() > 0){
		for(GroupMember gm:alllist){
			if(getDistance(mypos,gm.getGeo()) <= miles){
				list.add(gm);
			}
		}
		}
		return list;
	}
}
