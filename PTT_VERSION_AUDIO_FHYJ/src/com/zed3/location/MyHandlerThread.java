package com.zed3.location;

import com.zed3.utils.Zed3Log;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class MyHandlerThread extends HandlerThread {

	public static GPSInfoDataBase gpsDB;
	SQLiteDatabase db;
	private static MyHandlerThread mHandlerThread;

	public MyHandlerThread(String name) {
		super(name);
		gpsDB = GPSInfoDataBase.getInstance();
//		db = gpsDB.getWritableDatabase();
	}

	public MyHandlerThread(String name, int priority) {
		super(name, priority);
	}

	public InnerHandler mInnerHandler;

	private final class InnerHandler extends Handler {

		public InnerHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case 1:
				/*
				 * 将得到GPS数据插入数据库 add by liuhe 2014/9/18
				 */
				GpsInfo gpsInfo = (GpsInfo) msg.obj;
				String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(gpsInfo.UnixTime * 1000));
				ContentValues gpsValues = new ContentValues();
				gpsValues.put("gps_x", gpsInfo.gps_x);
				gpsValues.put("gps_y", gpsInfo.gps_y);
				gpsValues.put("gps_height", gpsInfo.gps_height);
				gpsValues.put("gps_speed", gpsInfo.gps_speed);
				gpsValues.put("UnixTime", gpsInfo.UnixTime);
				gpsValues.put("real_time", date);
				gpsValues.put("E_id", gpsInfo.E_id);
				if ((db == null) || !db.isOpen()) {
						db = gpsDB.getWritableDatabase();
					}
					try {
						db.beginTransaction();
						Zed3Log.debug("testgps", "MyHandlerThread#begin insert E_id="+gpsInfo.E_id);
						db.insert(MemoryMg.TABLE_NAME, null, gpsValues);
						db.setTransactionSuccessful();
						Zed3Log.debug("testgps", "MyHandlerThread#insert success E_id="+gpsInfo.E_id);
					} catch (SQLException e) {
						e.printStackTrace();
						Zed3Log.debug("testgps", "MyHandlerThread#insert exception:"+e.toString());
					} finally {
						db.endTransaction();
						Zed3Log.debug("testgps", "MyHandlerThread#insert end E_id="+gpsInfo.E_id);
					}
				break;
			case 2:
				String UnixE_id = (String) msg.obj;
				if ((db == null) || !db.isOpen()) {
						db = gpsDB.getWritableDatabase();
					}
					if (UnixE_id != null && UnixE_id != "") {
						try {
							db.beginTransaction();
							Zed3Log.debug("testgps", "MyHandlerThread#begin delete E_id="+UnixE_id);
							db.delete(MemoryMg.TABLE_NAME, "E_id = '"
									+ UnixE_id + "'", null);
							
							db.setTransactionSuccessful();
							Zed3Log.debug("testgps", "MyHandlerThread#success delete E_id="+UnixE_id);
						}catch(Exception e){
							Zed3Log.debug("testgps", "MyHandlerThread#delete exception:"+e.toString());
						}
						finally {
							db.endTransaction();
							Zed3Log.debug("testgps", "MyHandlerThread#end delete E_id="+UnixE_id);
						}
					}
				break;
			}
		}

	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		mInnerHandler = new InnerHandler(getLooper());
	}

	public static MyHandlerThread getMHThreadInstance() {
		if (mHandlerThread == null) {
			mHandlerThread = new MyHandlerThread("mHandlerThread");
		}
		return mHandlerThread;
	}

	public void sendMessage(Message msg) {
		if(mHandlerThread.isAlive()){
			mInnerHandler.sendMessage(msg);
		}
	}
/**
 * 退出当前线程，关闭数据库
 */
	public void stopSelf() {
		if(mHandlerThread != null){
			mHandlerThread.quit();
			mHandlerThread = null;
		}
		if(db != null){
			db.close();
			db = null;
		}
	}

}
