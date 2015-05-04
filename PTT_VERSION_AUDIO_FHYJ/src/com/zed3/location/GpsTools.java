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
	 * ������������Ƿ����
	 * 
	 * @param ctx
	 * @return true ����; false ������
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

	// ��unixʱ��ת�����������ڸ�ʽ
	public static String UnixTimeToNormal(int minutes) {

		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new java.util.Date(minutes * 1000L));
	}

	// ����������ת����unixʱ��
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
	/**---------------��̬��GPS--------------------*/
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
	
	//�ж�GPS����״̬
	/**---------------�ж�GPS�Ƿ���----------------*/
	public static boolean GetGspLockState(Context context) {
		boolean isGPSopen = Settings.Secure.isLocationProviderEnabled(
				context.getContentResolver(), LocationManager.GPS_PROVIDER);
		
		return isGPSopen;

	}
	/*
	 * �ο� http://hi.baidu.com/jiaodj/blog/item/0b9e59958bc803067af4803e.html
	 * http://xinklabi.iteye.com/blog/648570 java���ԣ� byte 1���ֽ� short 2���ֽ� int
	 * 4���ֽ� float 4���ֽ� char 2���ֽ� String 16���ֽ�
	 */

	// ���string���
	// temp = terminalNum.getBytes();
	// System.arraycopy(valArr, 4, temp, 0, temp.length);

	/**
	 * ���float ��floatתΪ���ֽ���ǰ�����ֽ��ں��byte����
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

	// �ֽ�ת����
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
	 * ���int�㷨 ��intתΪ���ֽ���ǰ�����ֽ��ں��byte���� ok
	 */
	public static byte[] InttoByte(int n) {
		byte[] b = new byte[4];
		b[3] = (byte) (0xff & n);
		b[2] = (byte) ((0xff00 & n) >> 8);
		b[1] = (byte) ((0xff0000 & n) >> 16);
		b[0] = (byte) ((0xff000000 & n) >> 24);
		return b;
	}

	// ���short�㷨 ok
	public static byte[] ShorttoByte(short n) {
		byte[] b = new byte[2];
		b[1] = (byte) (n & 0xff);
		b[0] = (byte) (n >> 8 & 0xff);
		return b;
	}

	// =======================����c++���ݵ��ַ���============================
	/**
	 * ��byte����ת����String �ӵ�4��λ�ö� ��ȡ����Ϊ16 ok
	 */
	public static String BytetoTerminalNumStr(byte[] valArr) {
		byte[] temp = new byte[16];// �ӵ�4����ʼ��ȡ
		System.arraycopy(valArr, 4, temp, 0, temp.length);
		return new String(temp).trim();
	}

	// ���Ϊshort�㷨 ok
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

	// ���byte 1���ֽ� ���ֵΪ256
	public static byte BytetoResultbyte(byte[] b) {

		byte bLoop = 0;
		for (int i = 20; i < 21; i++) {
			bLoop = b[i];
		}

		return bLoop;
	}

	// ����/�޸��ϴ����ڽ�����նˡ� ok
	/**--------------������װ�����飬�Ա��ϴ�������-----------------*/
	public static byte[] ReplyUploadCycleByte(int openFlag, String terminalNum,
			int flag, int cycletime) {
		byte[] buf = new byte[16];
		String str = "", temp = "";
		int len;
		str += GetVal(1, 2);// ����1
		str += GetVal(5, 4);// ��չ����5
	    str += "1";//
		str += GetVal(flag, 8);// �������0~255

		str += GetVal(openFlag, 8);// λ���ϱ�������־
		str += GetVal(cycletime, 7);// ��С�ϱ����
		str += GetVal(0, 2);// ��������

		str += GetVal(17, 5);
		len = terminalNum.length() * 4 + 4;// ����len
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

	// ��ȡ ����/�޸��ϴ����� �ն�ID
	public static String BitToUploadCycleID(byte[] b) {
		// ��ȡ����28
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

	// �ն���Ӧ���Ȼ�����/�޸Ĵ����������
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

		str += "1";// ����/Ӧ��
		// val ������� 0~255 0�ɹ�
		temp = Integer.toBinaryString(0);
		if (temp.length() != 8) {
			temp = String.format("%08d", Integer.parseInt(temp));
		}
		str += temp + "00" + "10001";// ��������+����17
		len = terminalNum.length() * 4 + 4;// ����len
		// termianlnum
		temp = Integer.toBinaryString(len);
		if (temp.length() != 6) {
			temp = String.format("%06d", Integer.parseInt(temp));
		}
		str += temp;
		str += "1000";// ����8

		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		// ����19
		str += "10011";
		// ����9
		str += "001001";

		temp = Integer.toBinaryString(classType);
		if (temp.length() != 8) {
			temp = String.format("%08d", Integer.parseInt(temp));
		}
		str += temp;

		// ѭ��1 or 0
		str += "" + cycle;

		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// �õ��µ�str
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
		String str = "0000000000000000000000000000000000000000000000000000000000000000";//8���ֽ�
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

	// �ն���Ӧ���Ȼ�����/�޸Ĵ���������� val:������� occurType:�ٷ�����
	public static byte[] ReplyServerDelOccur(String terminalNum, int delType,
			int occurType) {
		String str = "", temp = "";
		int len = 0;
		str += GetVal(1, 2);// ����1
		str += GetVal(7, 4);// ��չ����7
		str += "1";
		str += GetVal(0, 8);// �������0~255
		str += GetVal(17, 5);// �ն˺�������17

		len = terminalNum.length() * 4 + 4;// ����len
		str += GetVal(len, 6);

		str += GetVal(8, 4);// ����8
		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		str += GetVal(20, 5);// ����20
		str += GetVal(9, 6);// ����9
		str += "" + delType;// ɾ������1
		//
		str += GetVal(occurType, 8);// �ٷ�����

		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// �õ��µ�str
		byte[] buf = new byte[14];

		for (int m = 0; m < (str.length() / 8); m++) {
			temp = str.substring(m * 8, m * 8 + 8);
			buf[m] = (byte) ((buf[m] & 0x0) | convertBinaryToInt(temp
					.toCharArray()));
		}
		return buf;

	}

	// ��ȡ ���Ȼ�Ҫ�� �������� || ���رա� �ն�GPS�ϴ� �ն�ID
	public static String BitToOpenCloseGPSID(byte[] b) {
		// ��ȡ����28
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

	// �ն���Ӧ���Ȼ����� �ر� gps����
	public static byte[] ReplyOpenCloseGPS(String terminalNum, int flag) {
		byte[] buf = new byte[14];
		String s1, s2, s3, s4, xStr = "", s5 = "";
		int x1, x2, len, x3;

		buf[0] = (byte) ((buf[0] & 0x3F) | (1 << 6));// pdu ����
		buf[0] = (byte) ((buf[0] & 0xC3) | (10 << 2));// pdu ��չ����
		buf[0] = (byte) ((buf[0] & 0xFC) | 2);// ���� 2

		buf[1] = (byte) ((buf[1] & 0x01) | (flag << 1));// ������־ 1�� 0��
		buf[1] = (byte) ((buf[1] & 0xFE) | 1);//

		s1 = Integer.toBinaryString(17);
		s1 = s1.substring(1);
		buf[2] = (byte) ((buf[2] & 0x0F) | (convertBinaryToInt(s1.toCharArray()) << 4));

		len = terminalNum.length() * 4 + 4;// ����len
		s2 = Integer.toBinaryString(len);
		// ǰ�油0
		if (s2.length() != 6) {
			s2 = String.format("%06d", Integer.parseInt(s2));
		}
		s3 = s2.substring(0, 4);
		x1 = convertBinaryToInt(s3.toCharArray());
		buf[2] = (byte) ((buf[2] & 0xF0) | x1);//

		s3 = s2.substring(4, 6);
		x1 = convertBinaryToInt(s3.toCharArray());
		buf[3] = (byte) ((buf[3] & 0x3F) | x1 << 6);//
		buf[3] = (byte) ((buf[3] & 0xC3) | 8 << 2);// ����8

		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			xStr = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (xStr.length() != 4) {
				xStr = String.format("%04d", Integer.parseInt(xStr));
			}
			s5 += xStr;
		}

		s3 = s5.substring(0, 2);// ȡǰ2��
		x3 = convertBinaryToInt(s3.toCharArray());
		buf[3] = (byte) ((buf[3] & 0xFC) | x3);

		int j = (s5.length() - 2) % 8;
		if (j != 0) {
			for (int c = 0; c < (8 - j); c++) {
				s5 += "1";// ��λ
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

	// ����PDU��չ����
	public static int BitToPDUExtendType(byte[] b) {// & 00111100 ���ƶ���λ�õ���չ����
		int i = (b[0] & 0x3C) >> 2;

		return i;
	}

	public static int BitToSuccess(byte[] b) {// 01 0100 00|000000 11|
		return ((b[0] & 0x03) | ((b[1] & 0xFC) >> 2));
	}
	
	/*
	 * ͨ���������������ݵõ�Ψһ��ʶ��
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
	 * ͨ�����������ص�byte����õ�������ʱ�䣻
	 */
	public static long BitToUnixTime(byte[] src, int offset) {  

	    byte[] b = new byte[8];
	    int i = b.length - 1,j = 11;  
	    for (; i >= 0 ; i--,j--) {//��b��β��(��intֵ�ĵ�λ)��ʼcopy����  
	        if(j >= 4)  
	            b[i] = src[j];  
	        else  
	            b[i] = 0;//���b.length����4,�򽫸�λ��0  
	    }
	    int v0 = (b[0] & 0xff) << 56;//&0xff��byteֵ�޲���ת��int,����Java�Զ�����������,�ᱣ����λ�ķ���λ  
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
	 * �õ���ǰ������ʱ��
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
	 * �õ���ǰ�ն˿���ʱ��
	 */
	public static long getCurrentRealTime(){
		long realtime = SystemClock.elapsedRealtime()/1000;
//		System.out.println("gps----getCurrentRealTime-realtime"+realtime);
		return realtime;
	}
	/*
	 * ��ȡ��ǰϵͳʱ��
	 */
	public static long getCurrentLocalTime1(){
		return System.currentTimeMillis()/1000;
	}
	public static int BitToExtralType(byte[] b) {
		// TODO Auto-generated method stub
		int v = (b[1] & 0x03|(b[2] & 0xE0)>>2);
		return v;
	}
	//�õ��ϴ�����
	public static int BitToUploadType(byte[] b){
		int v = (((b[1] & 0x03)<<1)|(b[2] & 0xE0)>>3);
		return v;
	}
	
	
	// ============��¼ת���ֽ�============================
	// &:��һ��Ϊ0 ��ֵΪ0 |����һ��Ϊ1��ֵΪ1    ok
	public static byte[] LoginByte(String terminalNum, double gpsx,
			double gpsy, float gpsspeed) {
		byte[] buf = new byte[28];
		buf[0] = (byte) ((buf[0] & 0x3F) | (1 << 6));// pdu ����
		buf[0] = (byte) ((buf[0] & 0xC3) | (3 << 2));// pdu ��չ����
		buf[0] = (byte) (buf[0] & 0xFC);// ʱ�� 00

		buf[1] = (byte) ((buf[1] & 0x0F) | (4 << 4));// λ������ 4
		
		// ��¼��ʱ���Ҫ������ֵô��Ĭ��һ������ֵ�Ƿ���ԣ���
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
				// ǰ4λ
				s1 = xStr.substring(0, 4);
				x1 = convertBinaryToInt(s1.toCharArray());
				buf[1] = (byte) ((buf[1] & 0xF0) | x1);
				// 8λ
				s2 = xStr.substring(4, 12);
				x2 = convertBinaryToInt(s2.toCharArray());
				buf[2] = (byte) ((buf[2] & 0x0) | x2);
				// 8λ
				s3 = xStr.substring(12, 20);
				x3 = convertBinaryToInt(s3.toCharArray());
				buf[3] = (byte) ((buf[3] & 0x0) | x3);
				// 5λ
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
			// ǰ3λ
			s1 = xStr.substring(0, 3);
			x1 = convertBinaryToInt(s1.toCharArray());
			buf[4] = (byte) ((buf[4] & 0xF8) | x1);
			// 8λ
			s2 = xStr.substring(3, 11);
			x2 = convertBinaryToInt(s2.toCharArray());
			buf[5] = (byte) ((buf[5] & 0x0) | x2);
			// 8λ
			s3 = xStr.substring(11, 19);
			x3 = convertBinaryToInt(s3.toCharArray());
			buf[6] = (byte) ((buf[6] & 0x0) | x3);
			// 5λ
			s4 = xStr.substring(19, 24);
			x4 = convertBinaryToInt(s4.toCharArray());
			buf[7] = (byte) ((buf[7] & 0x7) | x4 << 3);
		} else {
			MyLog.e("gpsLogin", "length is not enough");
		}
		buf[7] = (byte) (buf[7] & 0xF8);
		buf[8] = (byte) (buf[8] & 0x0);
		buf[9] = (byte) ((buf[9] & 0x0F) | 3 << 4);// �ٶ�����3

		int speed = GetGPSSpeed(gpsspeed);//
		xStr = Integer.toBinaryString(speed);
		// ǰ�油0
		if (xStr.length() != 8) {
			xStr = String.format("%08d", Integer.parseInt(xStr));
		}
		s1 = xStr.substring(0, 4);
		x1 = convertBinaryToInt(s1.toCharArray());
		buf[9] = (byte) ((buf[9] & 0xF0) | x1);// �ٶ�

		s2 = xStr.substring(4, 8);
		x2 = convertBinaryToInt(s2.toCharArray());
		buf[10] = (byte) ((buf[10] & 0x1F) | x2 << 5);// �ٶ�һ���� ǰ3λ
		buf[10] = (byte) (buf[10] & 0xE0);

		buf[11] = (byte) (buf[11] & 0x1F);
		buf[11] = (byte) ((buf[11] & 0xE0) | 0x10);

		len = terminalNum.length() * 4 + 4;// ����len
		// �ն˺��룺����17 ���̶���
		buf[12] = (byte) (buf[12] & 0x07);
		buf[12] = (byte) ((buf[12] & 0xF8) | 0x04);

		buf[13] = (byte) ((buf[13] & 0x3F) | 0x40);
		xStr = Integer.toBinaryString(len);
		// ǰ�油0
		if (xStr.length() != 6) {
			xStr = String.format("%06d", Integer.parseInt(xStr));
		}
		x1 = convertBinaryToInt(xStr.toCharArray());
		buf[13] = (byte) ((buf[13] & 0xC0) | x1);

		buf[14] = (byte) ((buf[14] & 0x0F) | 0x80);// ����8
		char[] cc = terminalNum.toCharArray();
		// cc�ĳ�����ż�������һλҪ��1111������ż�����ò�1111
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
				s5 += "1";// ��λ
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
		str += GetVal(1, 2);// ����1
		str += GetVal(3, 4);// ��չ����3
		str += "00";
		str += GetVal(4, 4);// λ������4
		
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
			str += "000000000000";// ����12
		else{
			str += "0";
			str += GetVal(GetHeight(gpsheight), 11);
		}
		
		str+=GetVal(5, 3);//����3         ���뷽��󽫴����͸�Ϊ5
		int speed = GetGPSSpeed(gpsspeed);//
		str+=GetVal(speed, 7);
		
		str+=GetVal(gpsdirection,8);
		//str+="00000000";//��ֱ�ٶ�  �ĳ� ����Ƕ�
		
		str+="10";
		str+= "00000000"; //����ԭ��
		str+=GetVal(17,5); //����17
		
		len = terminalNum.length() * 4 + 4;// ����len
		str += GetVal(len, 6);
		str+="1000";//����8
		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		// ���Э��汾�� add by liuhe 2014/9/18
		str += "10100";//����20
		str += "001000";//����8
		str += "00000001";//Э��汾��1
		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// �õ��µ�str
		for (int m = 0; m < (str.length() / 8); m++) {
			temp = str.substring(m * 8, m * 8 + 8);
			buf[m] = (byte) ((buf[m] & 0x0) | convertBinaryToInt(temp
					.toCharArray()));
		}
		
		return buf;
	}
	
	
	// x
	public static int GetGPSX(double val) {
		// ��180 + B���� ��2^25��/360 = A
		//2��25�η���33 554 432
		double i = 180 + val;
		double j = Math.pow(2, 25) / 360;
		return (int) (i * j);
	}

	// y
	public static int GetGPSY(double val) {
		// ��90 + B���� ��2^24��/180 = A   //16 777 216
		double i = 90 + val;
		double j = Math.pow(2, 24) / 180;
		return (int) (i * j);
	}

	//
	public static int GetGPSSpeed(float val) {// Ĭ�ϵ�λΪ��/�� ��Ҫת���ɿ�km/h
		float speed = val * 3.6f;
		
		if (speed < 29) {
			return (int) speed;
		} else {
			return (int) (16 * Math.pow(1.038, (speed - 13)));
		}
	}

	// ����Ϊ4 ��֧��С���͸���ת��
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

	// �����Ҫ����gps����--���Ȼ���Ӧ�������ReceiveThread.java��extType==4
	/**---------------������Ҫ���ϱ�λ����Ϣ------------------*/
	public static void OpenGPSByServer(Context context) {
		// ����gps����
		//OpenGPSByCode(context);

		// ������˷�����Ӧ
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
		
		//�����ٶȶ�λ
		setLocationOption(SipUAApp.mLocationClient);
		SipUAApp.mLocationClient.start();
		
		MyLog.e("opengpsbyserver", "opengpsbyserver");
	}
	
	//gps��λ���
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
	
	// ������ز���
	public static void setLocationOption(LocationClient mLocationClient) {
		LocationClientOption option = new LocationClientOption();
//		option.setServiceName("com.zed3.app");
		// ��Ҫ��ַ��Ϣ������Ϊ�����κ�ֵ��string���ͣ��Ҳ���Ϊnull��ʱ������ʾ�޵�ַ��Ϣ��
//		option.setAddrType("all");
		// �����Ƿ񷵻�POI�ĵ绰�͵�ַ����ϸ��Ϣ��Ĭ��ֵΪfalse����������POI�ĵ绰�͵�ַ��Ϣ��
//		option.setPoiExtraInfo(false);
		// ���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
		option.setProdName("zed3app");
		// ����GPS��ʹ��gpsǰ�����û�Ӳ����gps��Ĭ���ǲ���gps�ġ�
		option.setOpenGps(true);
		// ��λ��ʱ��������λ��ms
		// �����������ֵ���ڵ���1000��ms��ʱ����λSDK�ڲ�ʹ�ö�ʱ��λģʽ��
		option.setScanSpan(GetLocationTimeValByModel(MemoryMg.getInstance().GpsSetTimeModel)*1000);// 15��
		// ��ѯ��Χ��Ĭ��ֵΪ500�����Ե�ǰ��λλ��Ϊ���ĵİ뾶��С��
//		option.setPoiDistance(200);
		// �������û��涨λ����
//		option.disableCache(true);
		
		// ����ϵ���ͣ��ٶ��ֻ���ͼ����ӿ��е�����ϵĬ����bd09ll
		option.setCoorType("bd09ll");
		
		if(MemoryMg.getInstance().GpsLocationModel == 1){
			option.setLocationMode(LocationMode.Hight_Accuracy); //�߾���
		}else if(MemoryMg.getInstance().GpsLocationModel == 2){//ֻ��gps
			option.setLocationMode(LocationMode.Device_Sensors);
		}
		// �������ɷ��ص�POI������Ĭ��ֵΪ3������POI��ѯ�ȽϺķ�������������෵�ص�POI�������Ա��ʡ������
//		option.setPoiNumber(2);
		// ���ö�λ��ʽ�����ȼ���
		// ��gps���ã����һ�ȡ�˶�λ���ʱ�����ٷ�����������ֱ�ӷ��ظ��û����ꡣ���ѡ���ʺ�ϣ���õ�׼ȷ����λ�õ��û������gps�����ã��ٷ����������󣬽��ж�λ��
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
		
	// �����Ҫ��ر�gps����
	public static void CloseGPSByServer(Context context) {
		// ������˷�����Ӧ
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
		
		// �ر�gps����
		//CloseGPSByCode(context);
		
		MemoryMg.getInstance().GpsLockState=false;
		
		//�رհٶȶ�λ
		if(SipUAApp.mLocationClient.isStarted())
			SipUAApp.mLocationClient.stop();
		
		MyLog.e("CloseGPSByServer", "CloseGPSByServer");
	}

	// �ն���������GPS�ϴ����նˡ�������������������������������������������
	public static void OpenGPSByMySelf(double gpsx, double gpsy, float gpsspeed,float gpsheight,int gpsdirection,long UnixTime,String E_id) {
		// ������˷���gps����ֵ
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port);
			send.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum, 9,
					gpsx, gpsy, gpsspeed,gpsheight,gpsdirection,UnixTime,E_id));
			send.start();
			//9���ն˿���gps�ϴ�
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MyLog.e("terminal open gps",
				"�ն���������GPS�ϴ� ��������Ӧ���");
		// ���
		//MemoryMg.getInstance().RegisterOk = "";
	}

	// �ն������ر�GPS�ϴ����նˡ���������������������������������������������
	public static void CloseGPSByMySelf(double gpsx, double gpsy, float gpsspeed,float gpsheight,int gpsdirection,long UnixTime,String E_id) {
		// ������˷���gps����ֵ
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(ServerIP), Port);
			send.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum, 10,
					gpsx, gpsy, gpsspeed, gpsheight, gpsdirection,UnixTime,E_id));
			send.start();
			//10���ն˹ر�gps�ϴ�
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MyLog.e("terminal close gps",
				"�ն������ر�GPS�ϴ�  ��������Ӧ���");
		// ���
		//MemoryMg.getInstance().RegisterOk = "";
	}

	// �ն��ϱ�λ����Ϣ���նˡ�***************************
	public static void UploadGPSByTerminal(int sendType, double gpsx,
			double gpsy, float gpsspeed,float gpsheight,int gpsdirection,long UnixTime,String E_id) {
		// �������� �����������͵� gps
		// 12�����ͣ�
		// 21����ʧ��(������)��������ڷ���gps�����л�ȡ��������ֵ��ʱ��������ʧ��
		// 129��ʱ�ϱ���
		// 255����
		// 2��������

		// ������˷���
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
	// �ն��ϱ�λ����Ϣ���նˡ�***************************
	public static void UploadGPSByTerminal(int sendType,List<GpsInfo> list) {
		// �������� �����������͵� gps
		// 12�����ͣ�
		// 21����ʧ��(������)��������ڷ���gps�����л�ȡ��������ֵ��ʱ��������ʧ��
		// 129��ʱ�ϱ���
		// 255����
		// 2��������
		
		// ������˷���
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
	// gps����ֻ�з���ԭ��ͬ
	public static byte[] GpsByte(String terminalNum, int sendType, double gpsx,
			double gpsy, float gpsspeed,float gpsheight, int gpsdirection,long UnixTime,String E_id) {
		byte[] buf = new byte[67];//28
		String str = "", temp = "";
		int len = 0;
		str += GetVal(1, 2);// ����1
		str += GetVal(3, 4);// ��չ����3
		str += "00";
		str += GetVal(4, 4);// λ������4
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
			str += "000000000000";// ����12
		else{
			str += "0";
			str += GetVal(GetHeight(gpsheight), 11);
		}
		
		str += GetVal(5, 3);// ����3             ���뷽��󽫴����͸�Ϊ5
		int speed = (int)gpsspeed;//GetGPSSpeed(gpsspeed);//
		str += GetVal((speed > 127 ? 127 : speed)/*speed*/, 7);//ˮƽ�ٶ�
//		str += "0000000";
		str += GetVal(gpsdirection, 8);
		//str += "00000000";// ��ֱ�ٶ� �ĳ� ����Ƕ�
		
		str+="10";
		str+= GetVal(sendType,8); //����ԭ��
		str+=GetVal(17,5); //����17
		
		len = terminalNum.length() * 4 + 4;// ����len
		str += GetVal(len, 6);
		str+="1000";//����8
		char[] cc = terminalNum.toCharArray();
		int cLen = cc.length, n = 0;
		for (int m = 0; m < cLen; m++) {
			temp = Integer.toBinaryString(Integer.parseInt(cc[m]+""));
			if (temp.length() != 4) {
				temp = String.format("%04d", Integer.parseInt(temp));
			}
			str += temp;
		}
		
		str += "10011";//����19
		str += "000000";//����0
		str += "0101000";// ��չ����40
		str += GetVal(UnixTime, 64);
		str += strToE_id(E_id);
//		str += GetVal(getE_id(), 32*8);
		int j = str.length() % 8;
		if (j != 0) {
			for (int i = 0; i < (8 - j); i++)
				str += "1";
		}
		// �õ��µ�str
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
	//�����ķ���Ƕ��������ط���Ҫ�޸ģ�1��gps�����ϱ� 2��GPS������������ 3��GPS���������ر�  4��GPS��¼ [1 2 3�ɹ�һ��]

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
	 *  * �ж��Ƿ���������	
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
	 * ͨ����γ�Ⱥ�ʱ������ٶ�
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
	 * �����������������(��γ��)���� 
	 *  
	 * @param long1 
	 *            ��һ�㾭�� 
	 * @param lat1 
	 *            ��һ��γ�� 
	 * @param long2 
	 *            �ڶ��㾭�� 
	 * @param lat2 
	 *            �ڶ���γ�� 
	 * @return ���ؾ��� ��λ���� 
	 */  
	public static double Distance(double long1, double lat1, double long2,  
	        double lat2) {  
	    double a, b, R;  
	    R = 6378137; // ����뾶  
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
			gpsInfo.gps_height = (float) location.getAltitude();// ����
			gpsInfo.gps_speed = location.getSpeed();// �ٶ�
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
				//���UnixTime��ΪĬ��ֵ���ȡ
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
			//�����λ�õ����ݾ���Ϊ0���ȡ�ķ�����ʱ��Ϊ0��������������
			if((int)gpsInfo.gps_x == 0 || gpsInfo.UnixTime == 0){
				Zed3Log.debug("testgps", "GPSTools#registerLocationListener gpsInfo.gps_x ="+gpsInfo.gps_x+"gpsInfo.UnixTime"+gpsInfo.UnixTime);
				return;
			}else{
				//�����λ��ʱ���뵱ǰʱ������������λ�����������������
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
			 * �ٶȻ�վ��λ���󷵻���
			 */
			// 61 �� GPS��λ���
			// 62 �� ɨ�����϶�λ����ʧ�ܡ���ʱ��λ�����Ч��
			// 63 �� �����쳣��û�гɹ���������������󡣴�ʱ��λ�����Ч��
			// 65 �� ��λ����Ľ����
			// 66 �� ���߶�λ�����ͨ��requestOfflineLocaiton����ʱ��Ӧ�ķ��ؽ��
			// 67 �� ���߶�λʧ�ܡ�ͨ��requestOfflineLocaiton����ʱ��Ӧ�ķ��ؽ��
			// 68 �� ��������ʧ��ʱ�����ұ������߶�λʱ��Ӧ�ķ��ؽ��
			// 161�� ��ʾ���綨λ���
			// 162~167�� ����˶�λʧ�ܡ�
			int errorCode = location.getLocType();
			if (errorCode == 61)// GPS��λ���
				MemoryMg.getInstance().isGpsLocation = true;
			else
				// ��վ��λ
				MemoryMg.getInstance().isGpsLocation = false;

			MyLog.e("SipUAPP", "LocType:" + errorCode + " x:"
					+ gpsInfo.gps_x + " y:" + gpsInfo.gps_y + " h:"
					+ gpsInfo.gps_height + " s:" + gpsInfo.gps_speed
					+ " satlites:" + location.getSatelliteNumber());

		}
	}
	
	/*
	 * ��ʱ���ľ���ֵ
	 */
	private static long getAbsTime(long getTime){
		return Math.abs(getTime - getCurrentLocalTime1());
	}
	/**
	 * ��ö�λ���
	 * @return
	 */
	private static long getScanSpan(){
		return GetLocationTimeValByModel(MemoryMg.getInstance().GpsSetTimeModel)*1000;
	}
	/**
	 * �ж�GPS�Ƿ��¼�ɹ�����ȡUnixTime
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
	 * ϵͳʱ��ı������¼���ʱ�䣬�൱��ʱ�����³�ʼ��
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
	 * ע�����ϵͳʱ�䣬ʱ�������ڸı�Ĺ㲥
	 */
	public static void registerTimeChangedReceiver(){
	
		IntentFilter timeChangedFilter = new IntentFilter();
		timeChangedFilter.addAction(Intent.ACTION_TIME_CHANGED);
		timeChangedFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		timeChangedFilter.addAction(Intent.ACTION_DATE_CHANGED);
		SipUAApp.getAppContext().registerReceiver(timeChangedReceiver, timeChangedFilter);
	}
	/**
	 * ȡ��ע�����ϵͳʱ�䣬ʱ�������ڸı�Ĺ㲥
	 */
	public static void unRegisterTimeChangedReceiver(){
		SipUAApp.getAppContext().unregisterReceiver(timeChangedReceiver);
	}
}
