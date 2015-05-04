package com.zed3.sipua;

import org.zoolu.tools.MyLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CallHistoryDatabase extends SQLiteOpenHelper {
	private static final String TAG = "guojunfeng-CallHistoryDatabase";
	private static final String DB_NAME = "callhistory.db";
	private static final int DB_VERSION = 2;
	// SQL_CREATE_CALL_HISTORY_TABLE 创建通话记录表SQL语句
	private static final String SQL_CREATE_CALL_HISTORY_TABLE = "CREATE TABLE call_history"
			+ "(_id integer PRIMARY KEY AUTOINCREMENT , "
			+ "type text ,  "
			+ "begin integer , "
			+ "end integer , "
			+ "begin_str text , "
			+ "name text , " + "number text ," + "status integer DEFAULT 0)";
	// add by liangzhang 2014-09-05 增加status字段
	// 0：未接电话未读 1：未接电话已读
	// add by liangzhang 2014-09-29 数据库升级并保留之前的数据
	private static final String CREATE_TEMP_TABLE = "alter table call_history rename to temp_call_history";
	private static final String INSERT_DATA = "insert into call_history(_id,type,begin,end,begin_str,name,number) select _id,type,begin,end,begin_str,name,number from temp_call_history";
	private static final String DROP_TEMP_TABLE = "drop table temp_call_history";
    private static CallHistoryDatabase mCallHistroy;
	// delete by liangzhang 2014-09-05
	// private Intent historyChangeIntent = new Intent(
	// Contants.ACTION_HISTORY_CHANGED);

	private CallHistoryDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	
	public static CallHistoryDatabase getInstance(Context context){
		if(mCallHistroy == null){
			mCallHistroy = new CallHistoryDatabase(context);
		}
		return mCallHistroy;
	}
//	private static CallHistoryDatabase callHistoryDatabase;
//	static{
//		callHistoryDatabase = new CallHistoryDatabase(SipUAApp.mContext);
//	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try {
			db.execSQL(SQL_CREATE_CALL_HISTORY_TABLE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MyLog.v(TAG, "create table error");
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		if (newVersion > oldVersion) {// 当新数据库版本号大于旧数据库版本号时升级数据库
			switch (newVersion) {// switch结构方便以后数据库升级使用
			case 2:
				// 数据库事物保证升级数据库时以前的数据库数据不会丢失
				db.beginTransaction();
				// 复制旧数据库中数据表
				db.execSQL(CREATE_TEMP_TABLE);
				// 创建新数据库表，新增字段status
				db.execSQL(SQL_CREATE_CALL_HISTORY_TABLE);
				// 将旧数据表中数据复制到包含新字段的数据表中
				db.execSQL(INSERT_DATA);
				// 删除复制的旧数据表
				db.execSQL(DROP_TEMP_TABLE);
				db.setTransactionSuccessful();
				db.endTransaction();
				break;
			default:
				break;
			}
		}
	}

	// 查询数据库
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

	/**
	 * 按条件查询数据库 add by liangzhang 2014-09-05
	 * 
	 * @param table_name
	 * @param selection
	 * @return cursor
	 */
	public synchronized Cursor mQuery(final String table_name,
			final String selection) {
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = null;
		try {
			cursor = database.query(table_name, null, selection, null, null,
					null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	// 插入数据到数据库
	public synchronized void insert(final String table_name, final ContentValues values) {
		SQLiteDatabase database = this.getWritableDatabase();
		try {
			database.insert(table_name, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "insert into " + table_name + "error:");
			e.printStackTrace();
		} finally {
			if (database != null && database.isOpen()) {
				database.close();
			}
		}
		// SipUAApp.mContext.sendBroadcast(historyChangeIntent);
	}

	// 删除对应表中的信息
	public synchronized void delete(final String table_name, final String where) {
		SQLiteDatabase database = null;
		try {
			database = this.getWritableDatabase();
			database.delete(table_name, where, null);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "delete from " + table_name + "error:");
			// e.printStackTrace();
		} finally {
			if (database != null && database.isOpen()) {
				database.close();
			}
		}
		// SipUAApp.mContext.sendBroadcast(historyChangeIntent);
	}

	// 更新对应表信息
	public synchronized void update(final String table_name, final String where,
			final ContentValues values) {
		SQLiteDatabase database = this.getWritableDatabase();
		try {
			database.update(table_name, values, where, null);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "update table " + table_name + "error, where = "
					+ where);
			e.printStackTrace();
		} finally {
			if (database != null && database.isOpen()) {
				database.close();
			}
		}
		// SipUAApp.mContext.sendBroadcast(historyChangeIntent);
	}

	

}
