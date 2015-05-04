package com.zed3.location;

import org.zoolu.tools.MyLog;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zed3.sipua.ui.Receiver;

public class GPSInfoDataBase extends SQLiteOpenHelper {
	private static final String TAG = "GpsInfoDataBase";
	private static final String DB_NAME = "gpsInfo.db";
	private static final int DB_VERSION = 1;
	private static GPSInfoDataBase instance;

	private static final String SQL_CREATE_GPSINFO_TABLE = "CREATE TABLE "+MemoryMg.TABLE_NAME
			+ "(_id integer PRIMARY KEY AUTOINCREMENT , "
			+ "gps_x text ,  "
			+ "gps_y text , "
			+ "gps_speed text, "
			+ "gps_height text , "
			+ "gps_direction  text ,"
			+ "UnixTime text ,"
			+ "real_time text ,"
			+ "E_id text )";

	public synchronized static GPSInfoDataBase getInstance() {
		if (instance == null) {
			instance = new GPSInfoDataBase(Receiver.mContext);
		}
		return instance;

	}

	public GPSInfoDataBase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try {
			db.execSQL(SQL_CREATE_GPSINFO_TABLE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	// ²éÑ¯Êý¾Ý¿â
		public synchronized Cursor query(final String table_name, final String order) {
			SQLiteDatabase database = this.getReadableDatabase();
			Cursor cursor = null;
			try {
				cursor = database.query(table_name, null, null, null, null, null,
						order);
				MyLog.i(TAG, "cursor.count = " + cursor.getCount());
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.e(TAG, "query from " + table_name + "error:");
				e.printStackTrace();
			} /* need not close the database
			 * finally { if (database != null && database.isOpen()) {
			 * database.close(); } }
			 */
			return cursor;
		}
}
