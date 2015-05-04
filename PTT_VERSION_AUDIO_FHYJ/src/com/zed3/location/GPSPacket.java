package com.zed3.location;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.zoolu.tools.MyLog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.zed3.sipua.R;
import com.zed3.toast.MyToast;
import com.zed3.utils.Zed3Log;

public class GPSPacket {

	private String tag = "GPSPacket";
	private Context context;
	private DatagramSocket socket;
	private InetAddress socketAdd;
	public static ReceiveThread thread = null;
	private String usernum = "";

	private boolean isGpsReg = false;
	private GpsInfo info =null;
	private BDLocation bdloc=null;
	SQLiteDatabase db;
	MyHandlerThread mHandlerThread;
	public static boolean loginFlag = false;
	/*
	 * userName 用户名pwd 密码ipAdd 地址port 端口
	 */
	public GPSPacket(Context context, String userName, String pwd,
			String ipAdd) {
		this.context = context;
		this.usernum = userName;
		// GPS ip地址
		Zed3Log.debug("testgps", "GPSPacket ipAdd = " + ipAdd);
		MemoryMg.getInstance().IPAddress = ipAdd;
		// GPS端口号 固定的。。。
		MemoryMg.getInstance().IPPort = 5070;
		//开关默认是打开的
		MemoryMg.getInstance().GpsLockState=true;
		mHandlerThread = MyHandlerThread.getMHThreadInstance();
		Init();
		// 等待接收服务端数据(在方法里只能执行一次)
		thread = new ReceiveThread(MemoryMg.getInstance().getSocket(), context);
		//gps登录的回调
		thread.setGpsListener(new GpsListener() {
			@Override
			public void LoginResult(int val) {
				// TODO Auto-generated method stub
				Zed3Log.debugE("testgps", "GPSPacket#run isGpsReg = " + isGpsReg + " , val = " + val);
				// TODO Auto-generated method stub
				if (isGpsReg == false) {//如果是第一次服务器响应登录
					if (val == 0)// ok
					{
						isGpsReg = true;
						loginFlag = true;
						
						Zed3Log.debugE("testgps", "GPSPacket#run login success");
						
						MyLog.i("gpsloginok", "----ok");
						// 保存用户名和密码
//						MemoryMg.getInstance().TerminalNum = usernum;
						// 开始频繁发心跳及周期注册
						thread.StartHandler();
					} else {
						isGpsReg = false;
						MyLog.i("gpsloginok", "----failed");
						// 再次注册
						SendLoginUdp();
						// 提示用户
						handler.sendMessage(handler.obtainMessage(1));
					}
				}
			
			}

			@Override
			public void UploadResult(int val, int type,String UnixE_id) {
				// TODO Auto-generated method stub
				if (isGpsReg == true) {
					if (val == 0 && type == 20) {
						// 数据上传成功
						Zed3Log.debug("testgps", "GPSPacket#setGpsListener upload success E_id="+UnixE_id);
						mHandlerThread.sendMessage(Message.obtain(mHandlerThread.mInnerHandler,2,UnixE_id));
					}
				}
			}
		});
		thread.start();// 暂屏蔽

		// 初始化百度定位
		bdloc = new BDLocation(context);
	}

	// 开启gps
	/**----------注册成功，开启GPS-----------*/
	public void StartGPS() {
		Zed3Log.debug("testgps", "GPSPacket#StartGPS enter usernum = " + usernum);
		if (usernum != null && usernum.length() > 0 ) {
			// 先赋值
			MemoryMg.getInstance().TerminalNum = usernum.trim();//1061
			//开启百度定位
			System.out.println("-------开启百度定位-----");
			if(bdloc!=null)
				bdloc.StartBDGPS();
			// 发包登录
			SendLoginUdp();
		}
	}

	
	// 初始化socket
	/**-----------对DatagramSocket和InetAddress进行初始化-----------*/
	private void Init() {
		try {
			socket = MemoryMg.getInstance().getSocket();
			
			Zed3Log.debug("testgps", "GPSPacket#Init ipAdd = " + GpsTools.ServerIP);
			
			socketAdd = InetAddress.getByName(MemoryMg.getInstance().IPAddress);
			
			Zed3Log.debug("testgps", "GPSPacket#Init ipAdd soket add = " + socketAdd);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 发送登录包
	public void SendLoginUdp() {
		Zed3Log.debug("testgps", "GPSPacket#SendLoginUdp enter ");
		try {
			//登录的时候初始化GPS定位吧 
			// SipUAApp.gpsInfo
//			info = GpsManage.getInstance(context).GetValueGpsStr();
//			Cursor cursor = GPSInfoDataBase.getInstance().query(MemoryMg.TABLE_NAME, null);
//			if(cursor != null){	
//				try {
//					if(cursor.moveToLast()){
//						info = new GpsInfo();
//						info.gps_x = cursor.getDouble(cursor.getColumnIndex("gps_x"));
//						info.gps_y = cursor.getDouble(cursor.getColumnIndex("gps_y"));
//						info.gps_speed = cursor.getFloat(cursor.getColumnIndex("gps_speed"));
//						info.gps_height = cursor.getFloat(cursor.getColumnIndex("gps_height"));
//						info.UnixTime = cursor.getLong(cursor.getColumnIndex("UnixTime"));
//					}
//				} finally{
//					cursor.close();
//				}
//			}
//			Zed3Log.debug("testgps", "GPSPacket#SendLoginUdp info =  " + info);
			SendThread send = new SendThread(socket, socketAdd, GpsTools.Port);
//			// gps坐标获取不到
//			if (info != null) {
//				send.SetContent(GpsTools.LoginByte(usernum, info.gps_x,
//						info.gps_y, info.gps_speed, info.gps_height,info.gps_direction));
//				
//				Zed3Log.debug("testgps", "GPSPacket#SendLoginUdp usernum = " + usernum);
//				
//			}
//			else
//			{
				//Toast.makeText(context, R.string.gps_Starfailure, Toast.LENGTH_SHORT).show();
				send.SetContent(GpsTools.LoginByte(usernum, 0, 0, 0, 0,0));
//			}
			send.start();
			MyLog.e("GPSPacket", "SendLoginUdp gps login...");
		} catch (Exception e) {
			Zed3Log.debug("testgps", "GPSPacket#SendLoginUdp exception =  " + e.getMessage());
			// TODO Auto-generated catch block
			MyLog.e("gpsPacket SendLoginUdp error:", e.toString());
		}
	}
	
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what==1)
				MyToast.showToast(true, context, context.getResources().getString(R.string.sis_loginning));
		}
	};

	// 退出gps 暂屏蔽
	public void ExitGPS(boolean isCloseGps) {
		Zed3Log.debug("testgps", "GPSPacket#ExitGPS enter");
		//暂停百度定位
		if(bdloc!=null && isCloseGps){
			MyLog.e("GPSPacket", "----StopBDGPS==");
			
			bdloc.StopBDGPS();
		}
		thread.ExitSys(isCloseGps);
		
	}

}
