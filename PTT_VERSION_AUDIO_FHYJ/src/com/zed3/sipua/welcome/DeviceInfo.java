package com.zed3.sipua.welcome;

public class DeviceInfo {
	public static String IMEI= "";
	public static String IMSI= "";
	public static String SIMID = "";
	public static String TELNUM ="";
	public static String MACADDRESS = "";
	public static String UDID = "";
	public static String PHONENUM ="";
	public static String SIMNUM = "";
	public static String AutoVNoName = "";
	public static boolean isSameSimCard = false;
	public static boolean isSameHandset = false;
	
	public static boolean isEmergency;
	
	public static String svpnumber = "";
	public static String defaultrecnum = "";
	public static String http_port = "";
	public static String https_port = "";
	
	public static String CONFIG_UPDATE_URL = "";//更新服务器地址
	public static String CONFIG_CONFIG_URL = "";//配置服务器地址
	public static boolean CONFIG_SUPPORT_AUTOLOGIN = true ;//自动登录
	public static boolean CONFIG_SUPPORT_UNICOM_PASSWORD = false;//联通密码
	public static boolean CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS = false;//联通流量
	public static boolean CONFIG_SUPPORT_VIDEO = true;//视频通话
	public static boolean CONFIG_SUPPORT_AUDIO = true;//语音通话
	public static int CONFIG_AUDIO_MODE =1;//0:电信移动电话  1:VOIP电话
	public static boolean CONFIG_SUPPORT_AUTORUN = false;//开机启动
	public static boolean CONFIG_CHECK_UPGRADE = true; //程序检查更新
	public static boolean CONFIG_SUPPORT_ENCRYPT = false;//信令加密
	public static boolean CONFIG_SUPPORT_PTTMAP = false;//地图模式
	public static int CONFIG_GPS = 4;// 0:不具备上报功能 1:强制GPS定位上报 2:强制百度智能定位
										// 3：强制百度GPS定位 4：默认从不定位
	public static boolean CONFIG_SUPPORT_AUDIO_CONFERENCE = false; //语音会议
	public static boolean CONFIG_SUPPORT_PICTURE_UPLOAD = true;//图片拍传
	public static boolean CONFIG_SUPPORT_IM = true;//短消息
	public static boolean CONFIG_SUPPORT_EMERGENYCALL = true;//一键告警
	public static boolean CONFIG_SUPPORT_HOMEKEY_BLOCK = false;//home屏蔽
	public static boolean CONFIG_SUPPORT_VAD = false;//静音检测 
	public static boolean CONFIG_SUPPORT_LOG = false;//LOG支持
	public static boolean CONFIG_SUPPORT_RATE_MONITOR = true;//速率悬浮窗
	public static boolean CONFIG_SUPPORT_REGISTER_INTERNAL = true;//注册间隔
	public static boolean CONFIG_SUPPORT_AEC = true;//回声消除
	public static boolean CONFIG_SUPPORT_NS = true;//降噪
	public static boolean CONFIG_SUPPORT_BLUETOOTH = false;//蓝牙支持
	public static boolean ISAlarmShowing;
	
	//remote   get value from server:
	public static int GPS_REMOTE = -1;
	public static boolean ENCRYPT_REMOTE = false;
	public static boolean AUTORUN_REMOTE = false;
	//phone state
		public static boolean isSupportHWChange = false;
}
