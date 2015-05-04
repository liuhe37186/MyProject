package com.zed3.location;

import java.io.Serializable;


public class GpsInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1335431462438445144L;

	public String gps_time = "";
	public double gps_x = 0;
	public double gps_y = 0;
	public float gps_speed = 0;
	public float gps_height = 0;
	public int gps_direction=0;
	public String gps_date = "";
	public String gps_status = "";
	public long UnixTime = 0;
	public String E_id = "";


}
