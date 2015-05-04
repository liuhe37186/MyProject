package com.zed3.video;

public class DeviceVideoInfo {
	public static final String ACTION_RESTART_CAMERA ="com.zed3.siupa.ui.restartcamera";
	public static int curAngle = 0;
	public static int supportColor = -1;
	public static boolean supportRotate = false;
	public static boolean supportFullScreen = false;
	public static boolean color_correct = false;
	public static boolean isHorizontal  = false;
	public static boolean supportAudioFec = false;
	public static boolean AudioFecOPen = false;
	public static final String AUDIO_FEC_SWITCH = "audio_fec_switch";
	public static final boolean DEAULT_AUDIO_FEC_SWITCH = true;
//	public static int fecLevel = 1;
	public static final String VIDEO_COLOR_CORRECT = "color_correct";
	public static final boolean DEFAULT_VIDEO_COLOR_CORRECT = false;
	public static final String VIDEO_SUPPORT_LAND = "support_land";
	public static final boolean DEFAULT_VIDEO_SUPPORT_LAND = false;
	
	public static final String VIDEO_SUPPORT_ROTATE = "rotate";
	public static final String VIDEO_SUPPORT_FULLSCREEN = "full_screen";
	public static final boolean DEFAULT_VIDEO_SUPPORT_ROTATE = false;
	public static final boolean DEFAULT_VIDEO_SUPPORT_FULLSCREEN = false;
	
	public static final String SCREEN_TYPE = "screen_type";
	public static final String DEFAULT_SCREEN_TYPE = "hor";
	public static String screen_type = "hor";
	
	public static boolean isConsole = true;
	public static boolean onlyCameraRotate = true;
	
	public static int MaxIFrameLostLimited = 0;//允许I帧丢包个数
	public static int MaxPFrameLostLimited = 0;//允许p帧丢包个数
	
	public static final int DEFAULT_PACKET_LOST_LEVEL = 1;//不允许丢包
	public static final String PACKET_LOST_LEVEL = "lost_level";
	public static int lostLevel = 1;
	public static int allow_audio_MaxDelay = 200;
	public static  int MaxVideoJitterbufferDelay=2000;//ms 最大容许延迟;
	public static  int MinVideoJitterbufferDelay=500;//ms 最小容许延迟;
	public static  int MidVideoJitterbufferDelay=1000;//ms 视频缓冲中间值;
	public static boolean isCodecK3 = false;
	
	public static final String MaxJitterDelay = "Max_Jitter_Delay";
	public static final String MinJitterDelay = "Min_Jitter_Delay";
	
	public static final String AUDIO_AEC_SWITCH = "AEC_SWITCH";
	public static final boolean DEFAULT_AUDIO_AEC_SWITCH = true;
}
