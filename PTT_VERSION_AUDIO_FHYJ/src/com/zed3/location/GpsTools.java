package com.zed3.location;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.zoolu.tools.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.Zed3Log;


public class GpsTools {

	//modify by oumogang 2013-06-03
	public static /*final*/ String ServerIP = MemoryMg.getInstance().IPAddress;
	public static final int Port = MemoryMg.getInstance().IPPort;
	private static boolean needLog = false;
	public static List<GpsInfo> gpsList;

	private static final ReentrantLock sLock = new ReentrantLock();
	/**
	 * 检测网络连接是否可用
	 * 
	 * @param ctx
	 * @return true 可用; false 不可用
	 */
	public static boolean CheckNetWork(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo[] netinfo = cm.getAllNetworkInfo();
		if (netinfo == null) {
			return false;
		}
		for (int i = 0; i < netinfo.length; i++) {
			if (netinfo[i].isConnected()) {
				return true;
			}
		}
		return false;
	}

	// 将unix时间转换成正常日期格式
	public static String UnixTimeToNormal(int minutes) {

		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new java.util.Date(minutes * 1000L));
	}

	// 将正常日期转换成unix时间
	public static int TimeToUnix() {
		long epoch = 0;
		try {
			Date currentTime = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String dateString = formatter.format(currentTime);
			epoch = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.parse(dateString).getTime();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (int) (epoch / 1000);
	}
	/**---------------动态打开GPS--------------------*/
	public static void OpenGPSByCode(Context context) {
	    boolean isGPSopen = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(),
	    		LocationManager.GPS_PROVIDER );
	    if(isGPSopen) {
	    	return;
	    }
	    try {
	    	MyLog.e("ffff", "open gps");
	    	Settings.Secure.setLocationProviderEnabled(context.getContentResolver(),
		    		 LocationManager.GPS_PROVIDER, true);
	   
	    } catch (Exception e) {
			// TODO: handle exception
	    	e.printStackTrace();
		}  
	}
	
	public static void CloseGPSByCode(Context context) {
	    boolean isGPSopen = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(),
	    		LocationManager.GPS_PROVIDER );
	    if(!isGPSopen) {
	    	return;
	    }
		 try {
			    MyLog.e("ffff", "close gps");
		    	Settings.Secure.setLocationProviderEnabled(context.getContentResolver(),
			    		 LocationManager.GPS_PROVIDER, false);
		    	
		    } catch (Exception e) {
				// TODO: handle exception
		    	e.printStackTrace();
			}  
	}
	
	//判断GPS开关状态
	/**---------------判断GPS是否开启----------------*/
	public static boolean GetGspLockState(Context context) {
		boolean isGPSopen = Settings.Secure.isLocationProviderEnabled(
				context.getContentResolver(), LocationManager.GPS_PROVIDER);
		
		return isGPSopen;

	}
	/*
	 * 参考 http://hi.baidu.com/jiaodj/blog/item/0b9e59958bc803067af4803e.html
	 * http://xinklabi.iteye.com/blog/648570 java语言： byte 1个字节 short 2个字节 int
	 * 4个字节 float 4个字节 char 2个字节 String 16个字节
	 */

	// 封包string最简单
	// temp = terminalNum.getBytes();
	// System.arraycopy(valArr, 4, temp, 0, temp.length);

	/**
	 * 封包float 将float转为低字节在前，高字节在后的byte数组
	 */
	public static byte[] FloattoByte(float f) {
		return FloatUses(Float.floatToRawIntBits(f));
	}

	public static byte[] doubleToByte(double d) {
		byte[] b = new byte[8];
		long l = Double.doubleToLongBits(d);
		for (int i = 0; i < b.length; i++) {
			b[i] = new Long(l).byteValue();
			l = l >> 8;
		}
		return b;
	}

	// 字节转比特
	public static String byteToBit(byte b) {
		return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
				+ (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
				+ (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
				+ (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
	}

	public static byte[] FloatUses(int n) {
		byte[] b = new byte[4];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}

	/**
	 * 封包int算法 将int转为低字节在前，高字节在后的byte数组 ok
	 */
	public static byte[] InttoByte(int n) {
		byte[] b = new byte[4];
		b[3] = (byte) (0xff & n);
		b[2] = (byte) ((0xff00 & n) >> 8);
		b[1] = (byte) ((0xff0000 & n) >> 16);
		b[0] = (byte) ((0xff000000 & n) >> 24);
		return b;
	}

	// 封包short算法 ok
	public static byte[] ShorttoByte(short n) {
		byte[] b = new byte[2];
		b[1] = (byte) (n & 0xff);
		b[0] = (byte) (n >> 8 & 0xff);
		return b;
	}

	// =======================解析c++传递的字符串============================
	/**
	 * 将byte数组转化成String 从第4个位置读 读取长度为16 ok
	 */
	public static String BytetoTerminalNumStr(byte[] valArr) {
		byte[] temp = new byte[16];// 从第4个开始截取
		System.arraycopy(valArr, 4, temp, 0, temp.length);
		return new String(temp).trim();
	}

	// 解包为short算法 ok
	public static short BytetoMsgTypeShort(byte[] b) {
		int s = 0;
		if (b[0] >= 0) {
			s = s + b[0];
		} else {
			s = s + 256 + b[0];
		}
		s = s * 256;
		if (b[1] >= 0) {
			s = s + b[1];
		} else {
			s = s + 256 + b[1];
		}
		short result = (short) s;
		return result;
	}

	// 解包byte 1个字节 最大值为256
	public static byte BytetoResultbyte(byte[] b) {

		byte bLoop = 0;
		for (int i = 20; i < 21; i++) {
			bLoop = b[i];
		}

		return bLoop;
	}

	// 配置/修改上传周期结果【终端】 ok
	/**--------------把数据装入数组，以备上传服务器-----------------*/
	public static byte[] ReplyUploadCycleByte(int openFlag, String terminalNum,
			int flag, int cycletime) {
		byte[] buf = new byte[16];
		String str = "", temp = "";
		int len;
		str += GetVal(1, 2);// 类型1
		str += GetVal(5, 4);// 扩展类型5
	    str += "1";//
		str += GetVal(flag, 8);// 结果代码0~255

		str += GetVal(openFlag, 8);// 位置上报开启标志
		str += GetVal(cycletime, 7);// 最小上报间隔
		str += GetVal(0, 2);// 报告类型

		str += GetVal(17, 5);
		len = terminalNum.length() * 4 + 4;// 长度len
		str += GetVal(len, 6);
		str += "1000";

		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m] + ""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		
		for (int m = 0; m < (str.length() / 8); m++) {
			temp = str.substring(m * 8, m * 8 + 8);
			buf[m] = (byte) ((buf[m] & 0x0) | convertBinaryToInt(temp
					.toCharArray()));
		}
		
		return buf;
	}

	// 获取 配置/修改上传周期 终端ID
	public static String BitToUploadCycleID(byte[] b) {
		// 获取长度28
		int len = (((b[2] & 0x03) << 4) | (b[3] & 0xF0) >> 4);
		int i = (len - 4) / 8;
		int j = (len - 4) % 8;

		String str = "";
		for (int m = 0; m < i; m++) {
			str += ((b[4 + m] & 0xF0) >> 4) + "";
			str += (b[4 + m] & 0x0F) + "";
		}
		if (j != 0) {
			str += ((b[4 + i] & 0xF0) >> 4) + "";
		}

		return str.trim();
	}

	// 终端响应调度机配置/修改触发条件结果
	public static byte[] ReplyServerSetOrUpdateOccur(String terminalNum,
			int cycle, int classType) {
		String str = "", temp = "";
		int len = 0;
		temp = Integer.toBinaryString(1);
		if (temp.length() != 2) {
			temp = String.format("%02d", Integer.parseInt(temp));
		}
		str += temp;
		temp = Integer.toBinaryString(6);
		if (temp.length() != 4) {
			temp = String.format("%04d", Integer.parseInt(temp));
		}
		str += temp;

		str += "1";// 请求/应答
		// val 结果代码 0~255 0成功
		temp = Integer.toBinaryString(0);
		if (temp.length() != 8) {
			temp = String.format("%08d", Integer.parseInt(temp));
		}
		str += temp + "00" + "10001";// 报告类型+类型17
		len = terminalNum.length() * 4 + 4;// 长度len
		// termianlnum
		temp = Integer.toBinaryString(len);
		if (temp.length() != 6) {
			temp = String.format("%06d", Integer.parseInt(temp));
		}
		str += temp;
		str += "1000";// 类型8

		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		// 类型19
		str += "10011";
		// 长度9
		str += "001001";

		temp = Integer.toBinaryString(classType);
		if (temp.length() != 8) {
			temp = String.format("%08d", Integer.parseInt(temp));
		}
		str += temp;

		// 循环1 or 0
		str += "" + cycle;

		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// 得到新的str
		byte[] buf = new byte[17];

		for (int m = 0; m < (str.length() / 8); m++) {
			temp = str.substring(m * 8, m * 8 + 8);
			buf[m] = (byte) ((buf[m] & 0x0) | convertBinaryToInt(temp
					.toCharArray()));
		}
		return buf;

	}

	private static String GetVal(int val, int len) {
		String temp = "";
		try {
			temp = Integer.toBinaryString(val);
			if (temp.length() != len) {
				temp = String.format("%0" + len + "d", Integer.parseInt(temp));
			}
		} catch (Exception e) {
			MyLog.e("GpsTool GetVal error", e.toString());
			return "";
		}
		return temp;
	}
	private static String GetVal(long val, int len) {
		String temp = "";
		String str = "0000000000000000000000000000000000000000000000000000000000000000";//8个字节
		try {
			temp = Long.toBinaryString(val);
			if (temp.length() != len) {
				temp = str.substring(0, len-temp.length())+temp;
			}
		} catch (Exception e) {
			MyLog.e("GpsTool GetVal error", e.toString());
			return "";
		}
		return temp;
	}

	// 终端响应调度机配置/修改触发条件结果 val:结果代码 occurType:促发类型
	public static byte[] ReplyServerDelOccur(String terminalNum, int delType,
			int occurType) {
		String str = "", temp = "";
		int len = 0;
		str += GetVal(1, 2);// 类型1
		str += GetVal(7, 4);// 扩展类型7
		str += "1";
		str += GetVal(0, 8);// 结果代码0~255
		str += GetVal(17, 5);// 终端号码类型17

		len = terminalNum.length() * 4 + 4;// 长度len
		str += GetVal(len, 6);

		str += GetVal(8, 4);// 类型8
		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		str += GetVal(20, 5);// 类型20
		str += GetVal(9, 6);// 长度9
		str += "" + delType;// 删除类型1
		//
		str += GetVal(occurType, 8);// 促发类型

		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// 得到新的str
		byte[] buf = new byte[14];

		for (int m = 0; m < (str.length() / 8); m++) {
			temp = str.substring(m * 8, m * 8 + 8);
			buf[m] = (byte) ((buf[m] & 0x0) | convertBinaryToInt(temp
					.toCharArray()));
		}
		return buf;

	}

	// 获取 调度机要求 “开启” || “关闭” 终端GPS上传 终端ID
	public static String BitToOpenCloseGPSID(byte[] b) {
		// 获取长度28
		int len = (((b[2] & 0x07) << 3) | (b[3] & 0xE0) >> 5);
		int i = (len - 4) / 8;
		int j = (len - 4) % 8;
		String str = "";

		for (int m = 0; m < i; m++) {
			str += (((b[3 + m] & 0x01) << 3) | (b[3 + m + 1] & 0xE0) >> 5) + "";
			str += ((b[3 + m + 1] & 0x1E) >> 1) + "";
		}
		
		if (j != 0) {
			str += (((b[3 + i] & 0x01) << 3) | (b[3 + i + 1] & 0xE0) >> 5) + "";
		}
		MyLog.e("openclosegpsID", str +" len:"+len+" i:"+i);
		return str.trim();
	}

	// 终端响应调度机开启 关闭 gps请求
	public static byte[] ReplyOpenCloseGPS(String terminalNum, int flag) {
		byte[] buf = new byte[14];
		String s1, s2, s3, s4, xStr = "", s5 = "";
		int x1, x2, len, x3;

		buf[0] = (byte) ((buf[0] & 0x3F) | (1 << 6));// pdu 类型
		buf[0] = (byte) ((buf[0] & 0xC3) | (10 << 2));// pdu 扩展类型
		buf[0] = (byte) ((buf[0] & 0xFC) | 2);// 整数 2

		buf[1] = (byte) ((buf[1] & 0x01) | (flag << 1));// 开启标志 1开 0关
		buf[1] = (byte) ((buf[1] & 0xFE) | 1);//

		s1 = Integer.toBinaryString(17);
		s1 = s1.substring(1);
		buf[2] = (byte) ((buf[2] & 0x0F) | (convertBinaryToInt(s1.toCharArray()) << 4));

		len = terminalNum.length() * 4 + 4;// 长度len
		s2 = Integer.toBinaryString(len);
		// 前面补0
		if (s2.length() != 6) {
			s2 = String.format("%06d", Integer.parseInt(s2));
		}
		s3 = s2.substring(0, 4);
		x1 = convertBinaryToInt(s3.toCharArray());
		buf[2] = (byte) ((buf[2] & 0xF0) | x1);//

		s3 = s2.substring(4, 6);
		x1 = convertBinaryToInt(s3.toCharArray());
		buf[3] = (byte) ((buf[3] & 0x3F) | x1 << 6);//
		buf[3] = (byte) ((buf[3] & 0xC3) | 8 << 2);// 类型8

		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			xStr = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (xStr.length() != 4) {
				xStr = String.format("%04d", Integer.parseInt(xStr));
			}
			s5 += xStr;
		}

		s3 = s5.substring(0, 2);// 取前2个
		x3 = convertBinaryToInt(s3.toCharArray());
		buf[3] = (byte) ((buf[3] & 0xFC) | x3);

		int j = (s5.length() - 2) % 8;
		if (j != 0) {
			for (int c = 0; c < (8 - j); c++) {
				s5 += "1";// 补位
			}
		}
		int i = (s5.length() - 2) / 8;
		for (int a = 0; a < i; a++) {
			s4 = s5.substring(a * 8 + 2, a * 8 + 10);
			buf[4 + a] = (byte) ((buf[4 + a] & 0x0) | convertBinaryToInt(s4
					.toCharArray()));
		}
		return buf;
	}

	// 解析PDU扩展类型
	public static int BitToPDUExtendType(byte[] b) {// & 00111100 右移动两位得到扩展类型
		int i = (b[0] & 0x3C) >> 2;

		return i;
	}

	public static int BitToSuccess(byte[] b) {// 01 0100 00|000000 11|
		return ((b[0] & 0x03) | ((b[1] & 0xFC) >> 2));
	}
	
	/*
	 * 通过服务器返回数据得到唯一标识码
	 */
	public static String BitToE_id(byte[] b){
		StringBuffer str = new StringBuffer();
		for(int i = 4;i < b.length;i++){
			int a = Math.abs(b[i]);
			char[] temp = null;
			if((a >= 48 && a <= 57)||(a >= 97 && a <= 122)){
				temp = Character.toChars(a);
			}
			if(temp != null){
				str.append(temp);
			}
			}
//		System.out.println("----str--:"+str);
		return str.toString();
	}
	
	/*
	 * 通过服务器返回的byte数组得到服务器时间；
	 */
	public static long BitToUnixTime(byte[] src, int offset) {  

	    byte[] b = new byte[8];
	    int i = b.length - 1,j = 11;  
	    for (; i >= 0 ; i--,j--) {//从b的尾部(即int值的低位)开始copy数据  
	        if(j >= 4)  
	            b[i] = src[j];  
	        else  
	            b[i] = 0;//如果b.length不足4,则将高位补0  
	    }
	    int v0 = (b[0] & 0xff) << 56;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位  
	    int v1 = (b[1] & 0xff) << 48;  
	    int v2 = (b[2] & 0xff) << 40;  
	    int v3 = (b[3] & 0xff) << 32; 
	    int v4 = (b[4] & 0xff) << 24; 
	    int v5 = (b[5] & 0xff) << 16; 
	    int v6 = (b[6] & 0xff) << 8; 
	    int v7 = (b[7] & 0xff) ; 
	    return v0+v1+v2+v3+v4+v5+v6+v7;  
	}  
	
	/*
	 * 得到当前服务器时间
	 */
	public static long getCurrentUnixTime(long lastLocalTime ,long lastUnixTime){
		long currentLocal = getCurrentRealTime();
		if(currentLocal > lastLocalTime){
			return (currentLocal - lastLocalTime)+lastUnixTime;
		}
		return -1;
	}
	
	public static long getRealUnixTime(){
		Zed3Log.debug("testgps", "GPSTools#getUnixTime lock enter");
		try{
			boolean result = sLock.tryLock(1, TimeUnit.SECONDS);
			Zed3Log.debug("testgps", "GPSTools#getUnixTime result:"+result);
			if(result){
				String sharedPrefsFile = "com.zed3.sipua_preferences";
				SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
						Context.MODE_PRIVATE);
				
				long unixTime = settings.getLong(com.zed3.sipua.ui.Settings.UNIX_TIME, com.zed3.sipua.ui.Settings.DEFAULT_UNIX_TIME);
				long localTime = settings.getLong(com.zed3.sipua.ui.Settings.REALTIME, com.zed3.sipua.ui.Settings.DEFAULT_REALTIME);
//				System.out.println("gps----getRealUnixTime-unixTime:"+unixTime);
//				System.out.println("gps----getRealUnixTime-localTime:"+localTime);
				return getCurrentUnixTime(localTime, unixTime);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			sLock.unlock();
		}
		Zed3Log.debug("testgps", "GPSTools#savuUnixTime lock exit");
		return -1;
	}
	public static long getUnixTime(long time){
		Zed3Log.debug("testgps", "GPSTools#getUnixTime lock enter");
		try{
			boolean result = sLock.tryLock(1, TimeUnit.SECONDS);
			Zed3Log.debug("testgps", "GPSTools#getUnixTime result:"+result);
			if(result){
				String sharedPrefsFile = "com.zed3.sipua_preferences";
				SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
						Context.MODE_PRIVATE);
				
				long unixTime = settings.getLong(com.zed3.sipua.ui.Settings.UNIX_TIME, com.zed3.sipua.ui.Settings.DEFAULT_UNIX_TIME);
				long localTime = settings.getLong(com.zed3.sipua.ui.Settings.LOCAL_TIME, com.zed3.sipua.ui.Settings.DEFAULT_LOCAL_TIME);
//				System.out.println("gps----getUnixTime1-unixTime:"+unixTime);
//				System.out.println("gps----getUnixTime1-localTime:"+localTime);
				return time-localTime+unixTime;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sLock.unlock();
		}
		Zed3Log.debug("testgps", "GPSTools#savuUnixTime lock exit");
		return 0;
	}
	/*
	 * 得到当前终端开机时间
	 */
	public static long getCurrentRealTime(){
		long realtime = SystemClock.elapsedRealtime()/1000;
//		System.out.println("gps----getCurrentRealTime-realtime"+realtime);
		return realtime;
	}
	/*
	 * 获取当前系统时间
	 */
	public static long getCurrentLocalTime1(){
		return System.currentTimeMillis()/1000;
	}
	public static int BitToExtralType(byte[] b) {
		// TODO Auto-generated method stub
		int v = (b[1] & 0x03|(b[2] & 0xE0)>>2);
		return v;
	}
	//得到上传类型
	public static int BitToUploadType(byte[] b){
		int v = (((b[1] & 0x03)<<1)|(b[2] & 0xE0)>>3);
		return v;
	}
	
	
	// ============登录转换字节============================
	// &:有一个为0 则值为0 |：有一个为1则值为1    ok
	public static byte[] LoginByte(String terminalNum, double gpsx,
			double gpsy, float gpsspeed) {
		byte[] buf = new byte[28];
		buf[0] = (byte) ((buf[0] & 0x3F) | (1 << 6));// pdu 类型
		buf[0] = (byte) ((buf[0] & 0xC3) | (3 << 2));// pdu 扩展类型
		buf[0] = (byte) (buf[0] & 0xFC);// 时间 00

		buf[1] = (byte) ((buf[1] & 0x0F) | (4 << 4));// 位置类型 4
		
		// 登录的时候非要传坐标值么，默认一个坐标值是否可以？？
		String s1, s2, s3, s4,xStr;
		int x,x1, x2, x3, x4, len;
	
		if (gpsx == 0) {
			buf[1] = (byte) (buf[1] & 0xF0);
			buf[2] = (byte) (buf[2] & 0x0);
			buf[3] = (byte) (buf[3] & 0x0);
			buf[4] = (byte) (buf[4] & 0x0);
		} else {
			x = GetGPSX(gpsx);// gpsx
			xStr = Integer.toBinaryString(x);
			if (xStr.length() == 24) {
				xStr = "0" + xStr;
			}
			if (xStr.length() == 25) {
				// 前4位
				s1 = xStr.substring(0, 4);
				x1 = convertBinaryToInt(s1.toCharArray());
				buf[1] = (byte) ((buf[1] & 0xF0) | x1);
				// 8位
				s2 = xStr.substring(4, 12);
				x2 = convertBinaryToInt(s2.toCharArray());
				buf[2] = (byte) ((buf[2] & 0x0) | x2);
				// 8位
				s3 = xStr.substring(12, 20);
				x3 = convertBinaryToInt(s3.toCharArray());
				buf[3] = (byte) ((buf[3] & 0x0) | x3);
				// 5位
				s4 = xStr.substring(20, 25);
				x4 = convertBinaryToInt(s4.toCharArray());
				buf[4] = (byte) ((buf[4] & 0x7) | x4 << 3);
			} else {
				MyLog.e("gpsLogin", "length is not enough");
			}
		}
		int y = GetGPSY(42.1);// gpsy
		xStr = Integer.toBinaryString(y);
		if (xStr.length() == 23)
			xStr = "0" + xStr;
		if (xStr.length() == 24) {
			// 前3位
			s1 = xStr.substring(0, 3);
			x1 = convertBinaryToInt(s1.toCharArray());
			buf[4] = (byte) ((buf[4] & 0xF8) | x1);
			// 8位
			s2 = xStr.substring(3, 11);
			x2 = convertBinaryToInt(s2.toCharArray());
			buf[5] = (byte) ((buf[5] & 0x0) | x2);
			// 8位
			s3 = xStr.substring(11, 19);
			x3 = convertBinaryToInt(s3.toCharArray());
			buf[6] = (byte) ((buf[6] & 0x0) | x3);
			// 5位
			s4 = xStr.substring(19, 24);
			x4 = convertBinaryToInt(s4.toCharArray());
			buf[7] = (byte) ((buf[7] & 0x7) | x4 << 3);
		} else {
			MyLog.e("gpsLogin", "length is not enough");
		}
		buf[7] = (byte) (buf[7] & 0xF8);
		buf[8] = (byte) (buf[8] & 0x0);
		buf[9] = (byte) ((buf[9] & 0x0F) | 3 << 4);// 速度类型3

		int speed = GetGPSSpeed(gpsspeed);//
		xStr = Integer.toBinaryString(speed);
		// 前面补0
		if (xStr.length() != 8) {
			xStr = String.format("%08d", Integer.parseInt(xStr));
		}
		s1 = xStr.substring(0, 4);
		x1 = convertBinaryToInt(s1.toCharArray());
		buf[9] = (byte) ((buf[9] & 0xF0) | x1);// 速度

		s2 = xStr.substring(4, 8);
		x2 = convertBinaryToInt(s2.toCharArray());
		buf[10] = (byte) ((buf[10] & 0x1F) | x2 << 5);// 速度一部分 前3位
		buf[10] = (byte) (buf[10] & 0xE0);

		buf[11] = (byte) (buf[11] & 0x1F);
		buf[11] = (byte) ((buf[11] & 0xE0) | 0x10);

		len = terminalNum.length() * 4 + 4;// 长度len
		// 终端号码：类型17 【固定】
		buf[12] = (byte) (buf[12] & 0x07);
		buf[12] = (byte) ((buf[12] & 0xF8) | 0x04);

		buf[13] = (byte) ((buf[13] & 0x3F) | 0x40);
		xStr = Integer.toBinaryString(len);
		// 前面补0
		if (xStr.length() != 6) {
			xStr = String.format("%06d", Integer.parseInt(xStr));
		}
		x1 = convertBinaryToInt(xStr.toCharArray());
		buf[13] = (byte) ((buf[13] & 0xC0) | x1);

		buf[14] = (byte) ((buf[14] & 0x0F) | 0x80);// 类型8
		char[] cc = terminalNum.toCharArray();
		// cc的长度是偶数，最后一位要补1111，不是偶数不用补1111
		int cLen = cc.length;
		String s5="";
		for (int m = 0; m < cLen; m++) {
			xStr = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (xStr.length() != 4) {
				xStr = String.format("%04d", Integer.parseInt(xStr));
			}
			s5 += xStr;
		}
		buf[14] = (byte) ((buf[14] & 0xF0) | convertBinaryToInt(s5.substring(0,4).toCharArray()));
		
		int j = (s5.length()-4) % 8;
		if (j != 0) {
			for (int c = 0; c < (8 - j); c++) {
				s5 += "1";// 补位
			}
		}
		int i = (s5.length()-4)/ 8;
		
		int count = 0;
		for (int a = 1; a <= i; a++) {
			s4 = s5.substring((2 * count + 1) * 4, (2 * count + 1) * 4 + 8);
			buf[14 + a] = (byte) ((buf[4 + a] & 0x0) | convertBinaryToInt(s4
					.toCharArray()));
			count++;
		}
		
		return buf;
	}

	public static byte[] LoginByte(String terminalNum, double gpsx,
			double gpsy, float gpsspeed,float gpsheight,int gpsdirection) {

		byte[] buf = new byte[32];
		String str = "", temp = "";
		int len = 0;
		str += GetVal(1, 2);// 类型1
		str += GetVal(3, 4);// 扩展类型3
		str += "00";
		str += GetVal(4, 4);// 位置类型4
		
		if (gpsx == 0) {
			str+="0000000000000000000000000";
		} else {
			int x = GetGPSX(gpsx);
			str += GetVal(x, 25);//
		}
		
		if (gpsy == 0) {
			str+="000000000000000000000000";
		} else {
			int y = GetGPSY(gpsy);//
			str += GetVal(y, 24);//
		}
		//height
		if (gpsheight == 0)
			str += "000000000000";// 海拔12
		else{
			str += "0";
			str += GetVal(GetHeight(gpsheight), 11);
		}
		
		str+=GetVal(5, 3);//类型3         加入方向后将此类型改为5
		int speed = GetGPSSpeed(gpsspeed);//
		str+=GetVal(speed, 7);
		
		str+=GetVal(gpsdirection,8);
		//str+="00000000";//垂直速度  改成 方向角度
		
		str+="10";
		str+= "00000000"; //发送原因
		str+=GetVal(17,5); //类型17
		
		len = terminalNum.length() * 4 + 4;// 长度len
		str += GetVal(len, 6);
		str+="1000";//类型8
		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		// 添加协议版本号 add by liuhe 2014/9/18
		str += "10100";//类型20
		str += "001000";//长度8
		str += "00000001";//协议版本号1
		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// 得到新的str
		for (int m = 0; m < (str.length() / 8); m++) {
			temp = str.substring(m * 8, m * 8 + 8);
			buf[m] = (byte) ((buf[m] & 0x0) | convertBinaryToInt(temp
					.toCharArray()));
		}
		
		return buf;
	}
	
	
	// x
	public static int GetGPSX(double val) {
		// （180 + B）× （2^25）/360 = A
		//2的25次方：33 554 432
		double i = 180 + val;
		double j = Math.pow(2, 25) / 360;
		return (int) (i * j);
	}

	// y
	public static int GetGPSY(double val) {
		// （90 + B）× （2^24）/180 = A   //16 777 216
		double i = 90 + val;
		double j = Math.pow(2, 24) / 180;
		return (int) (i * j);
	}

	//
	public static int GetGPSSpeed(float val) {// 默认单位为米/秒 需要转换成开km/h
		float speed = val * 3.6f;
		
		if (speed < 29) {
			return (int) speed;
		} else {
			return (int) (16 * Math.pow(1.038, (speed - 13)));
		}
	}

	// 长度为4 不支持小数和负数转换
	private static int convertBinaryToInt(char[] cars) {
		int result = 0;
		int num = 0;
		for (int i = cars.length - 1; 0 <= i; i--) {
			int temp = 2;
			if (num == 0) {
				temp = 1;
			} else if (num == 1) {
				temp = 2;
			} else {
				for (int j = 1; j < num; j++) {
					temp = temp * 2;
				}
			}
			int sum = Integer.parseInt(cars[i]+"");
			result = result + (sum * temp);
			num++;
		}
		return result;
	}

	// 服务端要求开启gps开关--调度机响应结果都在ReceiveThread.java的extType==4
	/**---------------服务器要求上报位置信息------------------*/
	public static void OpenGPSByServer(Context context) {
		// 开启gps代码
		//OpenGPSByCode(context);

		// 给服务端发送响应
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port);
			send.SetContent(GpsTools.ReplyOpenCloseGPS(
					MemoryMg.getInstance().TerminalNum, 1));
			send.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//
		MemoryMg.getInstance().GpsLockState=true;
		
		//开启百度定位
		setLocationOption(SipUAApp.mLocationClient);
		SipUAApp.mLocationClient.start();
		
		MyLog.e("opengpsbyserver", "opengpsbyserver");
	}
	
	//gps定位间隔
	public static int GetLocationTimeValByModel(int model) {
		if (model == 0)
			return 5;
		else if (model == 1)
			return 15;
		else if (model == 2)
			return 30;
		else if (model == 3)
			return 60;
		else
			return 15;
	}
	
	// 设置相关参数
	public static void setLocationOption(LocationClient mLocationClient) {
		LocationClientOption option = new LocationClientOption();
//		option.setServiceName("com.zed3.app");
		// 需要地址信息，设置为其他任何值（string类型，且不能为null）时，都表示无地址信息。
//		option.setAddrType("all");
		// 设置是否返回POI的电话和地址等详细信息。默认值为false，即不返回POI的电话和地址信息。
//		option.setPoiExtraInfo(false);
		// 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
		option.setProdName("zed3app");
		// 设置GPS，使用gps前提是用户硬件打开gps。默认是不打开gps的。
		option.setOpenGps(true);
		// 定位的时间间隔，单位：ms
		// 当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。
		option.setScanSpan(GetLocationTimeValByModel(MemoryMg.getInstance().GpsSetTimeModel)*1000);// 15秒
		// 查询范围，默认值为500，即以当前定位位置为中心的半径大小。
//		option.setPoiDistance(200);
		// 禁用启用缓存定位数据
//		option.disableCache(true);
		
		// 坐标系类型，百度手机地图对外接口中的坐标系默认是bd09ll
		option.setCoorType("bd09ll");
		
		if(MemoryMg.getInstance().GpsLocationModel == 1){
			option.setLocationMode(LocationMode.Hight_Accuracy); //高精度
		}else if(MemoryMg.getInstance().GpsLocationModel == 2){//只有gps
			option.setLocationMode(LocationMode.Device_Sensors);
		}
		// 设置最多可返回的POI个数，默认值为3。由于POI查询比较耗费流量，设置最多返回的POI个数，以便节省流量。
//		option.setPoiNumber(2);
		// 设置定位方式的优先级。
		// 当gps可用，而且获取了定位结果时，不再发起网络请求，直接返回给用户坐标。这个选项适合希望得到准确坐标位置的用户。如果gps不可用，再发起网络请求，进行定位。
//		option.setPriority(LocationClientOption.GpsFirst);
//		option.setPoiExtraInfo(true);
		mLocationClient.setLocOption(option);
	}
	public static List<GpsInfo> getInfo(){
		gpsList = new ArrayList<GpsInfo>();
		Cursor cursor = GPSInfoDataBase.getInstance().query(MemoryMg.TABLE_NAME, null);
		GpsInfo info = null;
		if(cursor != null){
			try {
				while(cursor.moveToNext()){
					info = new GpsInfo();
					info.gps_x = cursor.getDouble(cursor.getColumnIndex("gps_x"));
					info.gps_y = cursor.getDouble(cursor.getColumnIndex("gps_y"));
					info.gps_speed = cursor.getFloat(cursor.getColumnIndex("gps_speed"));
					info.gps_height = cursor.getFloat(cursor.getColumnIndex("gps_height"));
					info.UnixTime = cursor.getLong(cursor.getColumnIndex("UnixTime"));
					info.E_id = cursor.getString(cursor.getColumnIndex("E_id"));
					gpsList.add(info);
				}
			} finally{
				cursor.close();
			}
			
		}
		
		return gpsList;
	}
		
	// 服务端要求关闭gps开关
	public static void CloseGPSByServer(Context context) {
		// 给服务端发送响应
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port);
			send.SetContent(GpsTools.ReplyOpenCloseGPS(
					MemoryMg.getInstance().TerminalNum, 0));
			send.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 关闭gps代码
		//CloseGPSByCode(context);
		
		MemoryMg.getInstance().GpsLockState=false;
		
		//关闭百度定位
		if(SipUAApp.mLocationClient.isStarted())
			SipUAApp.mLocationClient.stop();
		
		MyLog.e("CloseGPSByServer", "CloseGPSByServer");
	}

	// 终端主动开启GPS上传【终端】》》》》》》》》》》》》》》》》》》》》》
	public static void OpenGPSByMySelf(double gpsx, double gpsy, float gpsspeed,float gpsheight,int gpsdirection,long UnixTime,String E_id) {
		// 给服务端发送gps坐标值
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port);
			send.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum, 9,
					gpsx, gpsy, gpsspeed,gpsheight,gpsdirection,UnixTime,E_id));
			send.start();
			//9是终端开启gps上传
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MyLog.e("terminal open gps",
				"终端主动开启GPS上传 服务器回应结果");
		// 清空
		//MemoryMg.getInstance().RegisterOk = "";
	}

	// 终端主动关闭GPS上传【终端】》》》》》》》》》》》》》》》》》》》》》》
	public static void CloseGPSByMySelf(double gpsx, double gpsy, float gpsspeed,float gpsheight,int gpsdirection,long UnixTime,String E_id) {
		// 给服务端发送gps坐标值
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port);
			send.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum, 10,
					gpsx, gpsy, gpsspeed, gpsheight, gpsdirection,UnixTime,E_id));
			send.start();
			//10是终端关闭gps上传
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MyLog.e("terminal close gps",
				"终端主动关闭GPS上传  服务器回应结果");
		// 清空
		//MemoryMg.getInstance().RegisterOk = "";
	}

	// 终端上报位置信息【终端】***************************
	public static void UploadGPSByTerminal(int sendType, double gpsx,
			double gpsy, float gpsspeed,float gpsheight,int gpsdirection,long UnixTime,String E_id) {
		// 以下类型 都是主动发送的 gps
		// 12电量低；
		// 21搜星失败(脱网？)；这个是在发送gps过程中获取不到坐标值的时候发送搜星失败
		// 129定时上报；
		// 255倒地
		// 2紧急呼叫

		// 给服务端发送
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port);
			send.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum,
					sendType, gpsx, gpsy, gpsspeed, gpsheight,gpsdirection,UnixTime,E_id));
			
			send.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	// 终端上报位置信息【终端】***************************
	public static void UploadGPSByTerminal(int sendType,List<GpsInfo> list) {
		// 以下类型 都是主动发送的 gps
		// 12电量低；
		// 21搜星失败(脱网？)；这个是在发送gps过程中获取不到坐标值的时候发送搜星失败
		// 129定时上报；
		// 255倒地
		// 2紧急呼叫
		
		// 给服务端发送
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port,sendType,list);
//			send.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum,
//					sendType, gpsx, gpsy, gpsspeed, gpsheight,gpsdirection,UnixTime,E_id));
			
			send.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private  static int GetHeight(float height)
	{
//		val<=1000:val+201;
//		val<=2450:(val-1000+1)/2+1201;
//		val>2450:(val-2450+74)/75+1926;
		int val = (int) height;
		if (val <= 1000)
			return val + 201;
		else if (val <= 2450 && val > 1000)
			return (val - 1000 + 1) / 2 + 1201;
		else if (val > 2450)
			return (val - 2450 + 74) / 75 + 1926;
		return 0;
	}
	// gps发送只有发送原因不同
	public static byte[] GpsByte(String terminalNum, int sendType, double gpsx,
			double gpsy, float gpsspeed,float gpsheight, int gpsdirection,long UnixTime,String E_id) {
		byte[] buf = new byte[67];//28
		String str = "", temp = "";
		int len = 0;
		str += GetVal(1, 2);// 类型1
		str += GetVal(3, 4);// 扩展类型3
		str += "00";
		str += GetVal(4, 4);// 位置类型4
		if (gpsx == 0) {
			str += "0000000000000000000000000";
		} else {
			int x = GetGPSX(gpsx);
			str += GetVal(x, 25);//
		}
		if (gpsy == 0) {
			str += "000000000000000000000000";
		} else {
			int y = GetGPSY(gpsy);//
			str += GetVal(y, 24);//
		}
		if (gpsheight == 0)
			str += "000000000000";// 海拔12
		else{
			str += "0";
			str += GetVal(GetHeight(gpsheight), 11);
		}
		
		str += GetVal(5, 3);// 类型3             加入方向后将此类型改为5
		int speed = (int)gpsspeed;//GetGPSSpeed(gpsspeed);//
		str += GetVal((speed > 127 ? 127 : speed)/*speed*/, 7);//水平速度
//		str += "0000000";
		str += GetVal(gpsdirection, 8);
		//str += "00000000";// 垂直速度 改成 方向角度
		
		str+="10";
		str+= GetVal(sendType,8); //发送原因
		str+=GetVal(17,5); //类型17
		
		len = terminalNum.length() * 4 + 4;// 长度len
		str += GetVal(len, 6);
		str+="1000";//类型8
		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		
		str += "10011";//类型19
		str += "000000";//长度0
		str += "0101000";// 扩展长度40
		str += GetVal(UnixTime, 64);
		str += strToE_id(E_id);
//		str += GetVal(getE_id(), 32*8);
		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// 得到新的str
		for (int m = 0; m < (str.length() / 8); m++) {
			temp = str.substring(m * 8, m * 8 + 8);
			buf[m] = (byte) ((buf[m] & 0x0) | convertBinaryToInt(temp
					.toCharArray()));
		}
		
		return buf;
	}

	//add by oumogang 2013-06-03
	public static void setServer(String address) {
		// TODO Auto-generated method stub
		MemoryMg.getInstance().IPAddress = address;
		ServerIP = address;
	}
	//新增的方向角度有三个地方需要修改：1、gps正常上报 2、GPS开关主动开启 3、GPS开关主动关闭  4、GPS登录 [1 2 3可归一类]

	public static String getE_id() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}

	public static String strToE_id(String str){
		StringBuffer stringBuffer = new StringBuffer();
		for(int i = 0;i < str.length();i++){
			char ch = str.charAt(i);
			String temp = Integer.toBinaryString(ch);
			if (temp.length() != 8) {
				temp = String.format("%0" + 8 + "d", Integer.parseInt(temp));
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}

	public static void saveServerUnixTime(long unixTime) {
		
		Zed3Log.debug("testgps", "RegisterService#saveServerUnixTime enter param unixTime = " + unixTime);
		
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
    	
		Editor editor = settings.edit();
		
		editor.putLong(com.zed3.sipua.ui.Settings.SERVER_UNIX_TIME, unixTime);
		
		editor.commit();
		
		Zed3Log.debug("testgps", "RegisterService#saveServerUnixTime exit");
	}
	public static long getServerUnixTime(){
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
		long unixTime = settings.getLong(com.zed3.sipua.ui.Settings.SERVER_UNIX_TIME, 0);
		return unixTime;
	}
	public static void saveUnixTime(long unixTime) {
			Zed3Log.debug("testgps", "GPSTools#saveUnixTime enter param unixTime = " + unixTime);
			
			String sharedPrefsFile = "com.zed3.sipua_preferences";
			SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
					Context.MODE_PRIVATE);
			
			Editor editor = settings.edit();
			
			editor.putLong(com.zed3.sipua.ui.Settings.UNIX_TIME, unixTime);
			
			editor.commit();
			
			Zed3Log.debug("testgps", "GPSTools#saveUnixTime exit");
	}
	public static void saveLocalTime(long localtime) {
		
		Zed3Log.debug("testgps", "RegisterService#saveLocalTime enter param localtime = " + localtime);
		
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
		
		Editor editor = settings.edit();
		
		editor.putLong(com.zed3.sipua.ui.Settings.LOCAL_TIME, localtime);
		
		editor.commit();
		
		Zed3Log.debug("testgps", "RegisterService#saveLocalTime exit");
	}
	public static void saveRealTime(long realtime) {
		
		Zed3Log.debug("testgps", "RegisterService#saveRealTime enter param realtime = " + realtime);
		
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
		
		Editor editor = settings.edit();
		
		editor.putLong(com.zed3.sipua.ui.Settings.REALTIME, realtime);
		
		editor.commit();
		
		Zed3Log.debug("testgps", "RegisterService#saveLocalTime exit");
	}
	
	public static void saveTime(long UnixTime,long LocalTime,long realTime){
		Zed3Log.debug("testgps", "RegisterService#saveTime enter");
		try{
			sLock.lock();
			saveUnixTime(UnixTime);
			saveLocalTime(LocalTime);
			saveRealTime(realTime);
		}finally{
			sLock.unlock();
		}
		Zed3Log.debug("testgps", "RegisterService#saveTime exit");
	}
	/**
	 *  * 判断是否是脏数据	
	 * @param Previous_gps_x
	 * @param Previous_gps_y
	 * @param Previous_UnixTime
	 * @param gps_x
	 * @param gps_y
	 * @param UnixTime
	 * @return
	 */
	
	public static boolean isDirtyData(double Previous_gps_x, double Previous_gps_y, long Previous_UnixTime, double gps_x, double gps_y, long UnixTime){
		double speed =  getSpeed(Previous_gps_x, Previous_gps_y, Previous_UnixTime, gps_x,gps_y, UnixTime);
		return (speed > 42) ? true : false;
	}
	/**
	 * 通过经纬度和时间计算速度
	 * @param Previous_gps_x
	 * @param Previous_gps_y
	 * @param Previous_UnixTime
	 * @param gps_x
	 * @param gps_y
	 * @param UnixTime
	 * @return
	 */
	public static double getSpeed(double Previous_gps_x, double Previous_gps_y, long Previous_UnixTime, double gps_x, double gps_y, long UnixTime){
		double speed = 0;
		try {
			double distance = Distance(Previous_gps_x, Previous_gps_y,gps_x, gps_y);
			speed = distance / (UnixTime - Previous_UnixTime);
		} catch(Exception e){}
		return Math.abs(speed);
	}
	/** 
	 * 计算地球上任意两点(经纬度)距离 
	 *  
	 * @param long1 
	 *            第一点经度 
	 * @param lat1 
	 *            第一点纬度 
	 * @param long2 
	 *            第二点经度 
	 * @param lat2 
	 *            第二点纬度 
	 * @return 返回距离 单位：米 
	 */  
	public static double Distance(double long1, double lat1, double long2,  
	        double lat2) {  
	    double a, b, R;  
	    R = 6378137; // 地球半径  
	    lat1 = lat1 * Math.PI / 180.0;  
	    lat2 = lat2 * Math.PI / 180.0;  
	    a = lat1 - lat2;  
	    b = (long1 - long2) * Math.PI / 180.0;  
	    double d;  
	    double sa2, sb2;  
	    sa2 = Math.sin(a / 2.0);  
	    sb2 = Math.sin(b / 2.0);  
	    d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));  
	    return d;  
	}  
	public static double Previous_gps_x = 0;
	public static double Previous_gps_y = 0;
	public static long Previous_UnixTime = 0;
	public static long D_UnixTime = 0;
	private static GpsInfo gpsInfo = null;
	public static synchronized void IsUploadGpsInfo(BDLocation location,MyHandlerThread mHandlerThread) {
		
		long currentTime = 0;
		if (location == null) {
			Zed3Log.debug("testgps", "GPSTools#registerLocationListener location == null");
			return;
		} else {
			gpsInfo = new GpsInfo();
			gpsInfo.gps_x = location.getLongitude();
			gpsInfo.gps_y = location.getLatitude();
			gpsInfo.gps_height = (float) location.getAltitude();// 海拔
			gpsInfo.gps_speed = location.getSpeed();// 速度
			gpsInfo.E_id = GpsTools.getE_id();
			String time = location.getTime();
			int locType = location.getLocType();
			int satelliteNumber = location.getSatelliteNumber();
			 SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			try {
				if(time != null){
					Date date = formatDate.parse(time);
					currentTime = date.getTime()/1000;
				}else{
					return;
				}
				//如果UnixTime不为默认值则获取
				if(isGetUnixTime()){
					Zed3Log.debug("testgps", "GPSTools#registerLocationListener isGetUnixTime ="+isGetUnixTime());
					gpsInfo.UnixTime =  GpsTools.getUnixTime(currentTime);
				}else{
					return;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Zed3Log.debug("testgps", "GpsTools#registerLocationListener gpsInfo:gps_x: "+ gpsInfo.gps_x+",gps_y:"+gpsInfo.gps_y+",UnixTime:"+gpsInfo.UnixTime+",getTime:"+time+",locType:"+locType+",satelliteNumber"+satelliteNumber);
			//如果定位得到数据经度为0或获取的服务器时间为0则舍弃该条数据
			if((int)gpsInfo.gps_x == 0 || gpsInfo.UnixTime == 0){
				Zed3Log.debug("testgps", "GPSTools#registerLocationListener gpsInfo.gps_x ="+gpsInfo.gps_x+"gpsInfo.UnixTime"+gpsInfo.UnixTime);
				return;
			}else{
				//如果定位的时间与当前时间相差超过两倍定位间隔则舍弃该条数据
				if(getAbsTime(currentTime) > 2 * getScanSpan()){
					Zed3Log.debug("testgps", "GPSTools#registerLocationListener getAbsTime(currentTime) > 2 * getScanSpan(),currentTime="+currentTime);
					return;
				}
				GpsTools.saveServerUnixTime(gpsInfo.UnixTime);
				if (Previous_gps_x == 0) {
					Zed3Log.debug("testgps", "GPSTools#registerLocationListener Previous_gps_x == 0");
					mHandlerThread.sendMessage(Message.obtain(mHandlerThread.mInnerHandler,1,gpsInfo));
					Previous_gps_x = gpsInfo.gps_x;
					Previous_gps_y = gpsInfo.gps_y;
					Previous_UnixTime = gpsInfo.UnixTime;
					D_UnixTime = 0;
				} else {
					if(Previous_UnixTime >= gpsInfo.UnixTime){
						Zed3Log.debug("testgps", "GpsTools#registerLocationListener Previous_UnixTime >= gpsInfo.UnixTime ,time:"+gpsInfo.UnixTime+",Previous_UnixTime"+Previous_UnixTime+",locType:"+locType+",satelliteNumber"+satelliteNumber);
						return;
					}
					if(GpsTools.isDirtyData(Previous_gps_x, Previous_gps_y, Previous_UnixTime, gpsInfo.gps_x, gpsInfo.gps_y, gpsInfo.UnixTime)){
						Zed3Log.debug("testgps", "GpsTools#isDirtyData gpsInfo.UnixTime:"+gpsInfo.UnixTime);
						if (D_UnixTime == 0){
							D_UnixTime = gpsInfo.UnixTime;
							Previous_UnixTime = gpsInfo.UnixTime;
						} else {
							if(gpsInfo.UnixTime - D_UnixTime > 60){
								mHandlerThread.sendMessage(Message.obtain(mHandlerThread.mInnerHandler,1,gpsInfo));
								Previous_gps_x = gpsInfo.gps_x;
								Previous_gps_y = gpsInfo.gps_y;
								Previous_UnixTime = gpsInfo.UnixTime;
								D_UnixTime = 0;
							}else{
								Previous_UnixTime = gpsInfo.UnixTime;
							}
						}
					}else{
						mHandlerThread.sendMessage(Message.obtain(mHandlerThread.mInnerHandler,1,gpsInfo));
						Previous_gps_x = gpsInfo.gps_x;
						Previous_gps_y = gpsInfo.gps_y;
						Previous_UnixTime = gpsInfo.UnixTime;
						D_UnixTime = 0;
					}
				}
			}
			
			/**
			 * 百度基站定位错误返回码
			 */
			// 61 ： GPS定位结果
			// 62 ： 扫描整合定位依据失败。此时定位结果无效。
			// 63 ： 网络异常，没有成功向服务器发起请求。此时定位结果无效。
			// 65 ： 定位缓存的结果。
			// 66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果
			// 67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果
			// 68 ： 网络连接失败时，查找本地离线定位时对应的返回结果
			// 161： 表示网络定位结果
			// 162~167： 服务端定位失败。
			int errorCode = location.getLocType();
			if (errorCode == 61)// GPS定位结果
				MemoryMg.getInstance().isGpsLocation = true;
			else
				// 基站定位
				MemoryMg.getInstance().isGpsLocation = false;

			MyLog.e("SipUAPP", "LocType:" + errorCode + " x:"
					+ gpsInfo.gps_x + " y:" + gpsInfo.gps_y + " h:"
					+ gpsInfo.gps_height + " s:" + gpsInfo.gps_speed
					+ " satlites:" + location.getSatelliteNumber());

		}
	}
	
	/*
	 * 求时间差的绝对值
	 */
	private static long getAbsTime(long getTime){
		return Math.abs(getTime - getCurrentLocalTime1());
	}
	/**
	 * 获得定位间隔
	 * @return
	 */
	private static long getScanSpan(){
		return GetLocationTimeValByModel(MemoryMg.getInstance().GpsSetTimeModel)*1000;
	}
	/**
	 * 判断GPS是否登录成功并获取UnixTime
	 */
	private static boolean isGetUnixTime(){
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences settings = SipUAApp.getAppContext().getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
		
		long preUnixTime = settings.getLong(com.zed3.sipua.ui.Settings.UNIX_TIME, com.zed3.sipua.ui.Settings.DEFAULT_UNIX_TIME);
		return preUnixTime == -1 ? false:true;
	}
	static BroadcastReceiver timeChangedReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, android.content.Intent intent) {
			Zed3Log.debug("testgps", "GPSTools#timeChangedReceiver intent:"+intent.toString());
			reInitTime();
		};
	};
	/**
	 * 系统时间改变需重新计算时间，相当于时间重新初始化
	 */
	private static void reInitTime(){
		try{
			sLock.lock();
			if(isGetUnixTime()){
				long localtime = getCurrentLocalTime1();
				long realTime = getCurrentRealTime();
				long unixTime = getRealUnixTime();
				if(unixTime != -1){
					saveLocalTime(localtime);
					saveRealTime(realTime);
					saveUnixTime(unixTime);
//					System.out.println("gps-----timeChangedReceiver unixTime:"+unixTime);
//					System.out.println("gps-----timeChangedReceiver localtime:"+localtime);
				}
			}
		}finally{
			sLock.unlock();
		}
	}
	/**
	 * 注册监听系统时间，时区，日期改变的广播
	 */
	public static void registerTimeChangedReceiver(){
	
		IntentFilter timeChangedFilter = new IntentFilter();
		timeChangedFilter.addAction(Intent.ACTION_TIME_CHANGED);
		timeChangedFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		timeChangedFilter.addAction(Intent.ACTION_DATE_CHANGED);
		SipUAApp.getAppContext().registerReceiver(timeChangedReceiver, timeChangedFilter);
	}
	/**
	 * 取消注册监听系统时间，时区，日期改变的广播
	 */
	public static void unRegisterTimeChangedReceiver(){
		SipUAApp.getAppContext().unregisterReceiver(timeChangedReceiver);
	}
}
