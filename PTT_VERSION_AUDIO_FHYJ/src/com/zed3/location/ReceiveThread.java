package com.zed3.location;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zed3.flow.FlowStatistics;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.Zed3Log;

/*
 * 等待接收
 */
public class ReceiveThread extends Thread {

	private final static int EXITSYSTEM = 0;// 退出系统
	private final static int CYCLEUP = 2;// 周期上传
	private final static int NORMALSTATE = 3;// 周期发送空包
	private final static int MESSAGE_CHECK_NETWORK = 1001; // 检查网络
	private final static int SAVEVALUE=4;
	private int TIMEOUT = 500;
	int flow = 0;
	private boolean returnFlag = true;
	DatagramSocket socket;
	Context context;
	public static boolean flag = true;
	int cyclex = 0;
	int occurType = 0, occurCircle = -1, delType = -1;// 促发定义类型 促发定义循环 删除类型
	public static List<GpsInfo> infoList;
	boolean tipFlag = false;
	private long UnixTime = 0L;
	private long RealTime = 0L;
	private long LocalTime = 0L;
	private GpsListener regListener = null;
	int count = 0;
	Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					Zed3Log.debug("testgps","ReceiverThread#handleMessage message what = " + msg.what);
					try {
						switch (msg.what) {

						case CYCLEUP:// 请求GPS终端周期上传GPS数据
							cyclex = (Integer) msg.obj;

							if (cyclex != 0) {
								// 上报gps  
								if (MemoryMg.getInstance().GpsLockState) {
//									//自动模式---不用
//									if (MemoryMg.getInstance().GpsLocationModel == 0) {
//										// ---自动切换上报模式
//										if (MemoryMg.getInstance().isGpsLocation)
//											info = GpsManage.getInstance(context)
//													.GetValueGpsStr();
//										else
//											info = SipUAApp.gpsInfo;
//									}
//									// gps模式
//									if (MemoryMg.getInstance().GpsLocationModel == 0) {
////										GpsManage.getInstance(context).GetValueGpsStr();
//										infoList = GpsTools.getInfo();
//										MyLog.i("ReceiverThread", "ReceiverThread当前定位方式为GPS模式");
////										Toast.makeText(context, "当前定位方式为GPS模式", Toast.LENGTH_LONG).show();								
//									}
//									// 百度模式
//									else if (MemoryMg.getInstance().GpsLocationModel == 1) {
//										infoList = GpsTools.getInfo();
//										MyLog.i("ReceiverThread", "ReceiverThread当前定位方式为百度模式");
////										Toast.makeText(context, "当前定位方式为百度模式", Toast.LENGTH_LONG).show();
//									}//百度GPS
//									else if (MemoryMg.getInstance().GpsLocationModel == 2) {
//										infoList = GpsTools.getInfo();
//										MyLog.i("ReceiverThread", "ReceiverThread当前定位方式为百度GPS模式");
////										Toast.makeText(context, "当前定位方式为百度GPS模式", Toast.LENGTH_LONG).show();
//									}
									infoList = GpsTools.getInfo();
									UploadGPSInfo(infoList);
								}
								MyLog.e("GPSCycle", "gps cycle is run");
								// 发送完后等待一段时间继续发送gps数据
								handler.sendMessageDelayed(// 传递参数 间隔时间
										handler.obtainMessage(CYCLEUP, cyclex), cyclex);
							} else
								MyLog.i("gpsReceiveThread", "stop message");

							break;
						case NORMALSTATE:// 每隔45秒 向服务器发送一个空包，如此不断循环
							// 发送空包4个字节
							MyLog.i("send null packet", "gps send null packet");
							SendGPS(GetEmptyByte());
							handler.sendMessageDelayed(
									handler.obtainMessage(NORMALSTATE), 45000);
							break;
//						case EXITSYSTEM:// 退出系统
//							DestoryAll();
//							break;
						case SAVEVALUE:
							int sec = (Integer) msg.obj;
							int model = 0;
							if (sec == 5)
								model = 0;
							else if (sec == 15)
								model = 1;
							else if (sec == 30)
								model = 2;
							else// 80S
								model = 3;
							
							SharedPreferences  mSharedPreferences = context.getSharedPreferences(Settings.sharedPrefsFile,
									Activity.MODE_PRIVATE);
							Editor edit = mSharedPreferences.edit();
							edit.putInt(Settings.PREF_LOCUPLOADTIME, model);
							edit.commit();
								
							break;
						}
					} catch (Exception e) {
						Log.e("", "", e);
					}
				}

			};
	
		
	
	
	public ReceiveThread(DatagramSocket socket, Context context) {
		this.socket = socket;
		this.context = context;
	}
	
	
	/**
	 * gps登录回调
	 */
	public void setGpsListener(GpsListener regListener) {
		this.regListener = regListener;
	}
	
	private Looper myLooper = null;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.i("xxxx","ReceiverThread#run enter");
		Looper.prepare();
		
		myLooper = Looper.myLooper();
		
		onLooperPrepared();
		
		Looper.loop();
		
		Log.i("xxxx","ReceiverThread#run exit");
		
	}
	

	private void onLooperPrepared() {
		Log.i("xxxx","ReceiverThread#onLooperPrepared enter");
		startRun();
		Log.i("xxxx","ReceiverThread#onLooperPrepared exit");
	}


	private void startRun() {
		Log.i("xxxx","ReceiverThread#startRun enter");
		try {
			//
//			String arr = "", str = "";
			int cycle = 0,upTime=0;
			int extType = 0;//pdu扩展类型
			byte[] buffer = new byte[1024];
			DatagramPacket packet = null;
			
			while (GetReturnFlag()) {
				packet = new DatagramPacket(buffer, buffer.length);
				Log.i("xxxx","ReceiverThread#receive enter");
				if(socket != null){
					try {
						socket.setSoTimeout(TIMEOUT);
						socket.receive(packet);
					} catch (Exception e) {
						if((e != null) && !(e instanceof SocketTimeoutException)){
							e.printStackTrace();
						}
						packet = null;
						/**
						 * 打开GPS时loginFlag为FALSE，GPS登录成功后loginFlag置为TRUE
						 * 如果GPS登录超时会走到这里，重新发送一个登陆包直到登录成功
						 */
						if(!GPSPacket.loginFlag){
							count++;
							if(count > 20){
								GpsTools.UploadGPSByTerminal(129, 0, 0, 0, 0, 0, 0, GpsTools.getE_id());
								Log.i("xxxx", "ReceiveThread#GPSPacket.loginFlag"+GPSPacket.loginFlag);
								count = 0;
							}
							continue;
						}
						
					}
				}
				Log.i("xxxx","ReceiverThread#receive exit");
				
				if(packet == null){
					continue;
				}
				
				//流量
				if(packet.getLength()+42>60){
					flow = packet.getLength()+42;
				}else{
					flow = 60;
				}
				FlowStatistics.Gps_Receive_Data = FlowStatistics.Gps_Receive_Data+flow;
				
				
				// 获取扩展类型
				extType = GpsTools.BitToPDUExtendType(buffer);
				MyLog.e("gpsReceiveThread", "msgtype:" + extType);
				
				// 调度机响应 配置/修改上传周期
				if (extType == 5) {
					Zed3Log.debug("testgps", "ReceiveThread#startRun extType=5");
					//获取调度机响应的终端号码或ID
					if (MemoryMg.getInstance().TerminalNum.equals(GpsTools
							.BitToUploadCycleID(buffer))) {
						// 最小上报间隔不在从服务端获取，而是直接读取pctool
						cycle = ((buffer[1] & 0xFE) >> 1);
						MyLog.e("gps cycle up cycle:", cycle + "**");

						if (cycle == 127)// 说明服务端没有上报间隔，需要读取本机PCTool的上报间隔时间
						{
							upTime = GpsTools
									.GetLocationTimeValByModel(MemoryMg
											.getInstance().GpsUploadTimeModel);
							if (upTime > 0)
								// 读取pctool里的上报时间间隔
								cycle = ChangeTimeToLip(upTime);
							else
								cycle = 4;
							
							SetGPSReCycle(cycle);			

						} else {//服务器要求本手机端按照传递的时间间隔上报
							SetGPSReCycle(cycle);
						}
					}
				}//配置/修改触发条件[调度机]
				else if (extType == 6) {
					// 解析触发定义【未完成】》》19(代表触发定义)长度，9 （单位bit）类型，
					// 获取类型：12 电量低，21 不能确定位置(脱网)，255 倒地 occurType  2是紧急呼叫
					// 获取循环：0 一次/ 1 循环 occurCircle
					this.BitToSetOrUpdateOccurID(buffer);
					
				}//删除触发条件[调度机]
				else if(extType==7)
				{
					//获取调度机响应的终端号码或ID
					//类型：12 电量低，21 不能确定位置(搜星失败)(脱网)，255 倒地  2是紧急呼叫
					this.BitToDelOccurID(buffer);
					
				}//调度机要求   “开启” || “关闭”  终端GPS上传
				else if(extType==10)
				{
					//获取调度机响应的终端号码或ID
					if (MemoryMg.getInstance().TerminalNum.equals(GpsTools
							.BitToOpenCloseGPSID(buffer))) {
						if (buffer[1] == 1) {// 开启标志
							// 代码开启gps+百度   开启后向调度机发送开启结果
							GpsTools.OpenGPSByServer(context);
							
						} else {
							// 代码关闭gps+百度
							GpsTools.CloseGPSByServer(context);
						
						}
					}
				}
				// 
				else if (extType == 4) {
					
					// 调度机响应通用应答，不仅是登录
					int result = GpsTools.BitToSuccess(buffer);
//					System.out.println("---------buffer.size--"+buffer.length);
//					for(int i=0;i<36;i++){
//						System.out.println("----buffer----"+i+"----="+buffer[i]);
//					}
					int type = GpsTools.BitToExtralType(buffer);
					String UnixE_id = GpsTools.BitToE_id(buffer);
					if(type == 18){
						UnixTime = GpsTools.BitToUnixTime(buffer,4);
						RealTime = GpsTools.getCurrentRealTime();
						LocalTime = GpsTools.getCurrentLocalTime1();
//						GpsTools.saveUnixTime(UnixTime);
//						GpsTools.saveLocalTime(LocalTime);
//						GpsTools.saveRealTime(RealTime);
						GpsTools.saveTime(UnixTime, LocalTime, RealTime);
						System.out.println("gps--ReceiverThread--unixtime--="+UnixTime);
						System.out.println("gps--ReceiverThread--LocalTime--="+LocalTime);
						System.out.println("gps--ReceiverThread---RealTime ="+RealTime);
					}
					int uploadType = GpsTools.BitToUploadType(buffer);
					MyLog.e("GpsRecv", "Recv Server Respond:" + result + "");
					if (result == 0){
						regListener.LoginResult(0);
						regListener.UploadResult(0, uploadType,UnixE_id);
					}
					else{
						
						regListener.LoginResult(1);
						regListener.UploadResult(1, uploadType,UnixE_id);
					}
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i("xxxx","ReceiverThread#run exception = " + e.getMessage());
		}
	}


	private int ChangeTimeToLip(int second) {
		int cycle = 0;
		if (second == 1)
			cycle = 0;// 1s
		else if (second == 2)
			cycle = 1;// 2s
		else if (second > 2 && second <= 5)
			cycle = 2;// 5s
		else if (second > 5 && second <= 10)
			cycle = 3;// 10s
		else if (second > 10 && second <= 15)
			cycle = 4;// 15s
		else if (second > 15 && second <= 30)
			cycle = 5;// 30s
		else if (second > 30 && second <= 40)
			cycle = 6;// 40s
		else if (second > 40 && second <= 80)
			cycle = 7;
		else
			cycle = 4;

		return cycle;
	}

	private void SetGPSReCycle(int cycle) {
		int second = 0;
		if (cycle >= 0) {
			if (cycle <= 6) {
				if (cycle == 0)
					second = 1;// 1s
				else if (cycle == 1)
					second = 2;// 2s
				else if (cycle == 2)
					second = 5;// 5s
				else if (cycle == 3)
					second = 10;// 10s
				else if (cycle == 4)
					second = 15;// 15s
				else if (cycle == 5)
					second = 30;// 30s
				else if (cycle == 6)
					second = 40;// 40s
			} else {
				if (cycle > 6 && cycle <= 29) {
					second = (cycle + 1) * 10;
				} else if (cycle >= 30 && cycle <= 59) {
					second = (int) (5.5 + 0.5 * (cycle - 30)) * 60;
				} else if (cycle >= 60 && cycle <= 127) {
					second = (21 + 1 * (cycle - 60)) * 60;
				} else// 默认1分钟上传一次
				{
					second = 60;
				}
			}
			// /配置/修改上传周期结果--成功 flag为结果代码
			SendGPS(GpsTools.ReplyUploadCycleByte(CheckGpsEnabled(),
					MemoryMg.getInstance().TerminalNum, 0, cycle));

			// 将second保存到全局变量里面去
			// MemoryMg.getInstance().gpsSecond = second;

			second = second * 1000;// 转换成毫秒
			tipFlag = true;
			
			// 先关闭gps
			//GpsManage.getInstance(context).CloseGPS();

			if (handler.hasMessages(CYCLEUP)) {
				handler.removeMessages(CYCLEUP);
			}
			handler.sendMessage(handler.obtainMessage(CYCLEUP, second));
			
			//保存上报时间到sharedpreference配置文件
			handler.sendMessage(handler.obtainMessage(SAVEVALUE, second / 1000));
			
		} else// 配置/修改上传周期结果---失败【终端】 结果代码0~255
		{
			SendGPS(GpsTools.ReplyUploadCycleByte(CheckGpsEnabled(),
					MemoryMg.getInstance().TerminalNum, 1, 0));
		}
	}

	// 获取配置/修改触发条件[调度机] 终端ID
	public void BitToSetOrUpdateOccurID(byte[] b) {
		// 获取长度
		int len = (((b[1] & 0x01) << 7) | (b[2] & 0xF8) >> 3);
		int i = (len - 4) / 8;
		int j = (len - 4) % 8;

		String str = "";
		int m = 0;
		for (m = 0; m < i; m++) {
			str += ((b[3 + m] & 0x78) >> 3) + "";
			str += ((b[3 + m] & 0x07) << 1 | (b[3 + m + 1] & 0x80) >> 7) + "";
		}

		if (j != 0) {// 奇数
			str += ((b[3 + i] & 0x78) >> 3) + "";
			// 最后两个字节数组分别是：[3+m+2] 和 [3+m+3]
			occurType = (b[3 + m + 2] & 0xFF);
			occurCircle = ((b[3 + m + 3] & 0x80) >> 7);

		} else// 偶数
		{
			// 最后两个字节数组分别是：[3+m+1] 和 [3+m+2]
			occurType = ((b[3 + m + 1] & 0x0F) << 4 | (b[3 + m + 2] & 0xF0) >> 4);
			occurCircle = ((b[3 + m + 2] & 0x08) >> 3);
		}

		// 获取调度机响应的终端号码或ID
		if (MemoryMg.getInstance().TerminalNum.equals(str)) {
			if (occurType == 12) {
				// MemoryMg.getInstance().IsBatteryLower=true;
				MyLog.e("GPS ", "设置电池电量低");//
			} else if (occurType == 21) {
				// MemoryMg.getInstance().IsGPSLoaction=true;
				MyLog.e("GPS ", "设置 脱网");//
			} else if (occurType == 255) {
				// MemoryMg.getInstance().IsLand=true;
				MyLog.e("GPS ", "设置 倒地告警");// 恢复值为254
			} else if (occurType == 2) {
				// MemoryMg.getInstance().IsUrgentCall=true;
				MyLog.e("GPS ", "紧急呼叫");
			}
			// 解析完后要向服务端发送应答。。。。
			SendGPS(GpsTools.ReplyServerSetOrUpdateOccur(
					MemoryMg.getInstance().TerminalNum, occurCircle, occurType));
		}

		MyLog.e(">>>>>SetOrUpdateOccur", occurType + " " + occurCircle
				+ "  name:" + str);
	}

	// 获取 删除触发条件 终端ID
	public void BitToDelOccurID(byte[] b) {
		// 获取长度28
		int len = (((b[1] & 0x07) << 3) | (b[2] & 0xE0) >> 5);
		int i = (len - 4) / 8;
		int j = (len - 4) % 8;

		String str = "";
		int m = 0;
		for (m = 0; m < i; m++) {
			str += (((b[2 + m] & 0x01) << 3) | ((b[2 + m + 1] & 0xE0) >> 5))
					+ "";
			str += ((b[2 + m + 1] & 0x1E) >> 1) + "";
		}

		if (j != 0) {// 奇数
			str += (((b[2 + i] & 0x01) << 3) | ((b[2 + i + 1] & 0xE0) >> 5))
					+ "";

			// 最后两个字节数组分别是：[2+m+1] 和 [2+m+2]
			occurType = ((b[2 + m + 1] & 0x01) << 7 | (b[2 + m + 2] & 0xFE) >> 1);
			delType = ((b[2 + m + 1] & 0x02) >> 2);
		} else// 偶数
		{
			// 最后两个字节数组分别是：[2+m+2] 和 [2+m+3]
			occurType = ((b[2 + m + 2] & 0x1F) << 3 | (b[2 + m + 3] & 0xE0) >> 5);
			delType = ((b[2 + m + 2] & 0x20) >> 5);
		}

		if (MemoryMg.getInstance().TerminalNum.equals(str)) {
			// if (occurType == 12)
			// MemoryMg.getInstance().IsBatteryLower = false;
			// else if (occurType == 21)
			// MemoryMg.getInstance().IsGPSLoaction = false;
			// else if (occurType == 255)
			// MemoryMg.getInstance().IsLand = false;
			// else if (occurType == 2)
			// MemoryMg.getInstance().IsUrgentCall = false;
			// 解析完后要向服务端发送应答。。。。
			SendGPS(GpsTools.ReplyServerDelOccur(
					MemoryMg.getInstance().TerminalNum, delType, occurType));
		}
		MyLog.e(">>>>>DelOccur", occurType + " " + delType + "  name:" + str);
	}

	// 外部调用 终端主动发送handler
	public void StartHandler() {
		// 将原来的清除掉
		if (handler.hasMessages(NORMALSTATE)) {
			handler.removeMessages(NORMALSTATE);
		}
		// 终端 向服务端 发空包
		handler.sendMessage(handler.obtainMessage(NORMALSTATE));
	}

	// 退出
	public void ExitSys(boolean isCloseGps) {

		// 三秒空包 心跳
		if (handler.hasMessages(NORMALSTATE)) {
			handler.removeMessages(NORMALSTATE);
		}
		// 定时上传gps的去掉
		if (handler.hasMessages(CYCLEUP)) {
			handler.removeMessages(CYCLEUP);
		}
		DestoryAll(isCloseGps);
		
		//handler.sendMessage(handler.obtainMessage(EXITSYSTEM));
	}

	// 获取标识
	public boolean GetReturnFlag() {
		return returnFlag;
	}

	// 停止线程
	public void StopRunning() {
		if(myLooper != null){
			try{
				myLooper.quit();
			}finally {
				myLooper = null;
			}
		}
		returnFlag = false;
		handler = null;
	}

//	// 注册 标识
//	public String GetRegisterOK() {
//		return MemoryMg.getInstance().RegisterOk;
//	}
//	// 注册 标识
//	public void SetRegisterOK(String str) {
//		MemoryMg.getInstance().RegisterOk = str;
//	}

	// private double test_x=116.46;//经度
	// private double test_y=39.92;//纬度


	private void UploadGPSInfo(List<GpsInfo> infoList){
		if (infoList != null && infoList.size() > 0) {
			
			GpsTools.UploadGPSByTerminal(129,infoList);
//			for(int i = 0;i < infoList.size();i++){
//				GpsTools.UploadGPSByTerminal(129, infoList.get(i).gps_x,
//						infoList.get(i).gps_y, infoList.get(i).gps_speed,
//						infoList.get(i).gps_height, infoList.get(i).gps_direction,infoList.get(i).UnixTime,infoList.get(i).E_id);// 定时上报
//				MyLog.i("GPS upload by seconds:" + cyclex, "----x:"
//						+ infoList.get(i).gps_x + "y:" + infoList.get(i).gps_y
//						+ "speed:" + infoList.get(i).gps_speed + "height:"
//						+ infoList.get(i).gps_height + " direction:"
//						+ infoList.get(i).gps_direction+" UnixTime:"+infoList.get(i).UnixTime+" E_id:"+infoList.get(i).E_id);
//			}
		} else {
			if (!tipFlag) {
				// Toast.makeText(context,R.string.gps_Starfailure,Toast.LENGTH_SHORT).show();
				MyLog.e("GPS upload by seconds: fail"
						+ cyclex, "null");
				// 搜星失败
				GpsTools.UploadGPSByTerminal(21, 0, 0, 0,
						0, 0,0,"");
			} else
				tipFlag = false;
		} // 经度 纬度
	}
	
	// 监测gps是否开启
	private int CheckGpsEnabled() {
		boolean gpsEnabled = android.provider.Settings.Secure.isLocationProviderEnabled(
				context.getContentResolver(), LocationManager.GPS_PROVIDER);
		if (gpsEnabled)
			return 1;// 开启
		else
			return 0;
	}

	// 空包
	private byte[] GetEmptyByte() {
		byte[] buf = new byte[4];
		String str = "\r\n\r\n";
		byte[] temp = str.getBytes();
		System.arraycopy(temp, 0, buf, 0, temp.length);
		return buf;
	}

	// 发送gps
	public void SendGPS(byte[] bytestr) {
		// GpsTools.CheckNetWork()
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
			// 最终需要改成byte[]字节型
			send.SetContent(bytestr);
			send.start();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 登录注册
//	public void SendLoginUdp() {
//		String usernum = MemoryMg.getInstance().TerminalNum;
//
//		//info = GpsManage.getInstance(context).GetValueGpsStr();
//		info = SipUAApp.gpsInfo;
//		try {
//			SendThread send = new SendThread(socket,
//					InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
//			if (info != null)
//				send.SetContent(GpsTools.LoginByte(usernum, info.gps_x,
//						info.gps_y, info.gps_speed));
//			else {
//
//				send.SetContent(GpsTools.LoginByte(usernum, 0, 0, 0));
//			}
//			send.start();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	

	// 退出程序
	public void DestoryAll(boolean isCloseGps) {
		System.out.println("--------停止线程------" + isCloseGps);
		// 停止线程
		StopRunning();
		
		if(isCloseGps) {
			// 停止gps
			GpsManage.getInstance(context).CloseGPS();
		}
		
		// 关闭socket
		if (socket != null) {
			try {
				socket.close();
				socket = null;
				MyLog.e("socket", "---socket close");
			} catch (Exception e) {
				MyLog.e("socket", "stop sc error.");
			}
		}
		MyLog.e("GPSPacket", "GPS DestoryAll==");
	}
	

}
