package com.zed3.sipua.baiduMap;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public class GroupMember {
	private GeoPoint geo;
	private String name;
	private String num;
	private boolean isOnline = false;
	public GeoPoint getGeo() {
		return geo;
	}
	public void setGeo(GeoPoint geo) {
		this.geo = geo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	
}
