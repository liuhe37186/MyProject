package com.zed3.sipua.ui.lowsdk;

import org.zoolu.tools.MyLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyContactDatabase extends SQLiteOpenHelper {
	private static final String TAG = "MyContactDatabase";
	private static final String DB_NAME = "pttcontact_login.db";
	private static final String TABLE_NAME = "pttcontact_login";
	private static final int DB_VERSION = 1;
	//表字段
	public static final String CUR_LOGINER = "cur_loginer";
	public static final String CONTACT_NUM = "contact_num";
	public static final String CONTACT_NAME= "contact_name";
	public static final String USED_TIMES = "used_times";
	private static final String SQL_CREATE_CONTACT_TABLE = "CREATE TABLE "+TABLE_NAME +
			" (_id integer PRIMARY KEY AUTOINCREMENT , " +  //primary key 
			"cur_loginer text ,  "+ //当前登陆号码
			"contact_num text ,  " + //联系人号码
			"contact_name text , " + //联系人名称
			"used_times integer DEFAULT 0)"; //使用次数
	public MyContactDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(SQL_CREATE_CONTACT_TABLE);
		} catch (Exception e) {
			MyLog.e(TAG, "create table error: "+e.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
	public Cursor mQuery(final String where,
			final String GroupBy, final String orderby) {
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = this.getReadableDatabase();
			cursor = database.query(TABLE_NAME, null, where, null, GroupBy,
					null, orderby);
//			MyLog.i(TAG, "cursor.count = " + cursor.getCount());
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "query from " + TABLE_NAME + "error:" + e.toString());
		} 
		return cursor;
	}

	// 插入数据到数据库
	public void insert(final ContentValues values) {
		SQLiteDatabase database = null;
		try {
			database = this.getWritableDatabase();
			database.insert(TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "insert into " + TABLE_NAME + "error:" + e.toString());
		} 
	}

	// 删除对应表中的信息
	public void delete(final String where) {
		SQLiteDatabase database = null;
		try {
			database = this.getWritableDatabase();
			database.delete(TABLE_NAME, where, null);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "delete from " + TABLE_NAME + "error:" + e.toString());
		} 
	}

	// 更新对应表信息
	public void update(final String where,
			final ContentValues values) {
		SQLiteDatabase database = null;
		try {
			database = this.getWritableDatabase();
			database.update(TABLE_NAME, values, where, null);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "update table " + TABLE_NAME + "error, where = "
					+ where + " " + e.toString());
		}
	}
}
