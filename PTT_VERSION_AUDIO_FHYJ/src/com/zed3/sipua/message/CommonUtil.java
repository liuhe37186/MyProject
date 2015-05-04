package com.zed3.sipua.message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class CommonUtil {
	public static Boolean isLog=true;
	/**
	 * ��־��Ϣ
	 * 
	 * @param tag
	 *            ��־
	 * @param funcName
	 *            ����־�ĺ�����
	 * @param msg
	 *            ��־��Ϣ����
	 * @param type
	 *            ��־����
	 */
	public static void Log(String tag, String funcName, String msg, char type) {
		if(!isLog){
			return;
		}
		switch (type) {
		case 'e':
			Log.e(tag, funcName + "===>" + msg);
			break;
		case 'v':
			Log.v(tag, funcName + "===>" + msg);
			break;
		case 'i':
			Log.i(tag, funcName + "===>" + msg);
			break;
		case 'd':
			Log.d(tag, funcName + "===>" + msg);
			break;
		default:
			Log.d(tag, funcName + "===>" + msg);
			break;
		}
	}

	/**
	 * ���ص�ǰϵͳ����
	 * 
	 * @return
	 */
	public static String getNowDate() {
		String date = null;
		Calendar calendar = Calendar.getInstance();
		int mYear = calendar.get(Calendar.YEAR);
		int mMonth = calendar.get(Calendar.MONTH);
		int mDay = calendar.get(Calendar.DAY_OF_MONTH);
		mMonth++;
		if (mMonth < 10)
			date = mYear + "-0" + mMonth;
		else
			date = mYear + "-" + mMonth;
		if (mDay < 10)
			date = date + "-0" + mDay;
		else
			date = date + "-" + mDay;
		return date;
	}

	/**
	 * ���ص�ǰϵͳʱ��
	 * 
	 * @return
	 */
	public static String getNowTime() {
		String time = null;
		Calendar calendar = Calendar.getInstance();
		int mHour = calendar.get(Calendar.HOUR_OF_DAY);
		int mMinute = calendar.get(Calendar.MINUTE);
		int mSecond = calendar.get(Calendar.SECOND);

		if (mHour < 10)
			time = "0" + mHour;
		else
			time = mHour + "";
		if (mMinute < 10)
			time = time + ":0" + mMinute;
		else
			time = time + ":" + mMinute;
		if (mSecond < 10)
			time = time + ":0" + mSecond;
		else
			time = time + ":" + mSecond;
		return time;
	}

	// ��ȡ��ǰϵͳʱ��
		public static String getCurrentTime() {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(
						" yyyy-MM-dd HH:mm ");
				Date curDate = new Date(System.currentTimeMillis());
				String strTime = formatter.format(curDate);
				return strTime;
			} catch(Exception e) {
				
			}
			return null;
		}

}
