package com.zed3.sipua.contant;


public class Contants {

	/**
	 * actions
	 */
	public static final String ACTION_HISTORY_CHANGED = "com.zed3.sipua_callhistory_changed";
	public static final String ACTION_CLEAR_MISSEDCALL = "com.zed3.sipua_clear_missedcall";
	public static final String ACTION_CONTACT_CHANGED = "com.zed3.sipua_contact_changed";
	public static final String ACTION_CURRENT_GROUP_CHANGED = "com.zed3.sipua_currentgroup_changed";
	//网络状态发生改变    add by oumogang 2013-05-10
	public static final String ACTION_NEWWORK_CHANGED = "com.zed3.sipua_network_changed";
	//注册成功后，注册失败后，更新组列表   add by oumogang 2013-05-27
	public static final String ACTION_ALL_GROUP_CHANGE = "com.zed3.sipua.ui_groupcall.all_groups_change";
	public static final String ACTION_GROUPLIST_CLEAR_OVER = "com.zed3.sipua.ui_groupcall.all_groups_clear_over";
	public static final String ACTION_CLEAR_GROUPLIST = "com.zed3.sipua.ui_groupcall.clear_grouplist";

	public static final int NETWORK_STATE_BAD = 0;
	public static final int NETWORK_STATE_GOOD = 1;
	public static final String NETWORK_STATE = "network_state";
	public static final String ACTION_GROUPLIST_UPDATE_OVER = "com.zed3.sipua_grouplist_update_over";
	public static final String ACTION_GETSTATUS_MESSAGE = "com.zed3.sipua.ui_groupstatelist";
	
	//add by oumogang 2013-05-28
	public static final String ACTION_LOCATE_GPS_INITED = "com.zed3.sipua_gps_inited";
	public static final String ACTION_LOCATE_GPS_EXITED = "com.zed3.sipua_gps_exited";
	public static final String ACTION_LOCATE_GPS_LOCATE_STARTED = "com.zed3.sipua_locate_gps_locate_started";
	public static final String ACTION_LOCATE_GPS_LOCATE_STOPED = "com.zed3.sipua_locate_gps_locate_stoped";
	public static final String ACTION_LOCATE_GPS_ENABLED = "com.zed3.sipua_locate_gps_enabled";
	public static final String ACTION_LOCATE_GPS_DISABELED = "com.zed3.sipua_gps_disabled";
	public static final String ACTION_LOCATE_GPS_LOCATE_SUCCESSED = "com.zed3.sipua_locate_successed";
	public static final String ACTION_LOCATE_GPS_LOCATE_FAILED = "com.zed3.sipua_gps_locate_failed";
	
	public static final String ACTION_LOCATE_BDGPS_INITED = "com.zed3.sipua_bdgps_inited";
	public static final String ACTION_LOCATE_BDGPS_EXITED = "com.zed3.sipua_bdgps_exited";
	public static final String ACTION_LOCATE_BDGPS_LOCATE_STARTED = "com.zed3.sipua_bdgps_locate_started";
	public static final String ACTION_LOCATE_BDGPS_LOCATE_STOPED = "com.zed3.sipua_bdgps_locate_stoped";
	public static final String ACTION_LOCATE_BDGPS_LOCATE_SUCCESSED = "com.zed3.sipua_bdgps_locate_succsessed";
	public static final String ACTION_LOCATE_BDGPS_LOCATE_FAILED = "com.zed3.sipua_bdgps_locate_failed";
	
	public static final String ACTION_LOCATE_UPLOAD_SUCCESSED = "com.zed3.sipua_upload_successed";
	public static final String ACTION_LOCATE_UPLOAD_FAILED = "com.zed3.sipua_upload_failed";
	public static final String ACTION_NOT_LOCATE_UPLOAD = "com.zed3.sipua_upload";
	public static final String ACTION_LOCATE_UPLOAD_SENDED = "com.zed3.sipua_upload_sended";
	
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LOCATE_UPLOAD_MODE = "locate_upload_mode";
	public static final String KEY_TIME = "time";
	public static final String KEY_RATE = "rate";
	
	public static final String KEY_LAST_ZMBLUETOOTH_SPP_ADDRESS = "last_spp_ZMBluetoothAddress";
	public static final String KEY_LAST_ZMBLUETOOTH_LOGFILE_NAME = "last_spp_ZMBluetooth_logfile_name";
	public static final String KEY_LAST_GQT_MAIN_LOGFILE_NAME = "last_GQT_MAIN_logfile_name";
	public static final String KEY_LAST_ZMBLUETOOTH_SPP_ONOFF_STATE = "last_spp_ZMBluetooth_spp_onoff_state";

	public static final String KEY_LOCATE_UPLOAD_SEND_ADDRESS = "server";
	public static final String KEY_LOCATE_UPLOAD_SEND_PORT = "port";
	
	// One group to another
	public static final String ACTION_GROUP_2_GROUP = "com.zed3.sipua.ui_groupcall.group_2_group";
	// One group to another
	public static final String ACTION_SINGLE_2_GROUP = "com.zed3.sipua.ui_groupcall.single_2_group";
	public static final String REGISTER_TRACES = " register traces 1220 ";
	
	public static final String MODEL_HUAWEI_G7 = "HUAWEI G7";
	public static final String MODEL_ZTE_V5 = "N918St";
	public static final String MODEL_HUAWEI_MATE7 = "HUAWEI MT7";
	
	
}
