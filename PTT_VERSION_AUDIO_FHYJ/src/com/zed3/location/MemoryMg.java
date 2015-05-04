package com.zed3.location;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MemoryMg {

	private static MemoryMg instance;
	//ptime for ptt
	public static int PTIME = 20;
	//协商后的ptime值
	public static int SdpPtime=0;
	
	// socket
	private DatagramSocket socket;
	// 终端号码 即用户名 配置文件读取
	public String TerminalNum = "";
	// 密码 md5加密
	public String Password = "";
	// 是否登录成功 服务器返回值保存在这里 0表ok 其它失败
	//public String RegisterOk = "";
	//
	public String IsLock = "";
	// 周期上传的cycle
	public int cycle = 0;
	// 地址
	public String IPAddress = "";
	// 端口号
	public int IPPort = /*0*/5070;
    //传递号码
	public String CallNum="";
	//发送消息记录seq编号
	public String LastSeq = "";
	//sharedpreference的监听事件
	public boolean IsChangeListener=true;
	
	//区分是百度定位还是GPS定位
	public boolean isGpsLocation = false;
	
	public boolean GpsLockState=false;
	// gps定位模式
	public int GpsLocationModel = -1;
	// gps定位间隔模式
	public int GpsSetTimeModel = 1;
	// gps上报间隔模式
	public int GpsUploadTimeModel = 1;
	
	// gps搜星失败提示
	public boolean GPSSatelliteFailureTip = false;
	//gvs转码分辨率设置
	public String GvsTransSize="";
	//摄像头所支持的视频分辨率
	public String SupportVideoSizeStr="";
	//视频转发是否携带isSendOnly
	public boolean isSendOnly = false;
	//sdp h264s
	public boolean isSdpH264s = false;
	//静音检测开关
	public boolean isAudioVAD = false;
	//MIC唤醒开关
	public boolean isMicWakeUp = false;
	// 移动电话类型  0:移动电话 1:voip电话
	public int PhoneType = 1;

	//--------------------------------
	// 用户本月套餐总流量
	public double User_3GTotal = 0;
	public double User_3GTotalPTT = 0;
	public double User_3GTotalVideo = 0;
	
	
	// 用户本月已用总流量
	public double User_3GLocalTotal = 0;
	public double User_3GLocalTotalPTT = 0;
	public double User_3GLocalTotalVideo = 0;
	// 用户本月上传时间点
	public String User_3GLocalTime = "";

	
	// 用户本月数据库已用总流量
	public double User_3GDBLocalTotal = 0;
	public double User_3GDBLocalTotalPTT = 0;
	public double User_3GDBLocalTotalVideo = 0;
	
	// 用户本月数据库上传时间点
	public String User_3GDBLocalTime = "";	
	
	// 计算得到的真实数据
	public double User_3GRelTotal=0;
	public double User_3GRelTotalPTT=0;
	public double User_3GRelTotalVideo=0;
	
	// 超额预警值
	public double User_3GFlowOut = 0;
	// 流量条提醒
	public boolean isProgressBarTip = false;
	//存储GPS信息数据表名
	public static final String TABLE_NAME = "gps_info";
	public static final String TABLE_NAME_COPY = "gps_info_copy";
	//--------------------------------
	public static MemoryMg getInstance() {
		if (instance == null)
			instance = new MemoryMg();
		return instance;
	}
    //
	public DatagramSocket getSocket() {
		if (socket == null || socket.isClosed()) {
			try {
				socket = new DatagramSocket();
				//socket = new DatagramSocket(GpsTools.Port);

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return socket;
	}

}
