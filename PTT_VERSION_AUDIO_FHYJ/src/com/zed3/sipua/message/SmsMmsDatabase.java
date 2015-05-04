/**
 * houyuchun create 20120802
 */

package com.zed3.sipua.message;

import org.zoolu.tools.MyLog;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zed3.sipua.SipUAApp;

public class SmsMmsDatabase extends SQLiteOpenHelper {

	private static final String TAG = "SmsMmsDatabase";
	private static final String DB_NAME = "message.db";
	public static final String TABLE_MESSAGE_TALK = "message_talk";
	public static final String SMS_MMS_DATABASE_CHANGED = "database_changed";
	private static final int DB_VERSION = 1;
	// SQL_CREATE_SMS_MMS_TABLE 创建收发的所有短彩信表SQL语句
	private static final String SQL_CREATE_MESSAGE_TALK_TABLE = "CREATE TABLE message_talk " +
			"(_id integer PRIMARY KEY AUTOINCREMENT , " +
			"E_id text ,  " +
			"address text , " +//与我会话的人的号码
			"contact_name text , " +//与我会话的人在联系人数据中的名字
			"sip_name text , " +//与我会话的人在Sip的DisplayName
			"body text , " +
			"status integer DEFAULT 0 , " +//1是否读过了 0为未读
			"mark integer DEFAULT 0 , " +//0为收到的短息 1为发出的短信
			"attachment text , " +
			"attachment_name text, " +
			"send integer DEFAULT 2, " +//0为发送成功，1为发送失败2为发送中，3为发送完成
			"type text , " +//短信息类型 彩信为“mms” 短信为“sms”
			"date text)";  
	// SQL_CREATE_SMS_MMS_DRAFT_TABLE 创建短彩信草稿箱SQL语句
	private static final String SQL_CREATE_MESSAGE_DRAFT_TABLE = "CREATE TABLE message_draft " +
			"(_id integer PRIMARY KEY AUTOINCREMENT , " +
			"address text , " +//与我会话的人的号码
			"body text , " +
			"save_time text)";
	protected static final int TYPE_RECEIVE = 0;
	protected static final int TYPE_SEND = 1;
	
    public SmsMmsDatabase(Context context) {
    	super(context, DB_NAME, null, DB_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		MyLog.i(TAG, "begin create table");
		try {
			db.execSQL(SQL_CREATE_MESSAGE_TALK_TABLE);
			db.execSQL(SQL_CREATE_MESSAGE_DRAFT_TABLE);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "create table error: "+e.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
    
	// 查询数据库 by order
	public Cursor query(final String table_name, final String order) {
		SQLiteDatabase database =null;
		Cursor cursor = null;
		try {
			database = this.getReadableDatabase();
			cursor = database.query(table_name, null, null, null, null, null, order);
			MyLog.i(TAG, "cursor.count = " + cursor.getCount());
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "query from " + table_name + "error:"+e.toString());
		} finally {
			if(database != null && database.isOpen()) {
				database.close();
			}
		}
		return cursor;
	}
	//guojunfeng add 查询数据库 by where  
	public synchronized Cursor mQuery(final String table_name, final String where,
			final String GroupBy,final String orderby) {
		SQLiteDatabase database =null;
		Cursor cursor = null;
		try {
			database = this.getReadableDatabase();
			cursor = database.query(table_name, null, where, null, GroupBy, null, orderby);
			MyLog.i(TAG, "cursor.count = " + cursor.getCount());
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "query from " + table_name + "error:"+e.toString());
		} finally {
			if(database != null && database.isOpen()) {
				database.close();
			}
		}
		return cursor;
	}
	
	// 插入数据到数据库
	public void insert(final String table_name, final ContentValues values) {
		SQLiteDatabase database =null;
		try {
			database = this.getWritableDatabase();
			database.insert(table_name, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "insert into " + table_name + "error:"+e.toString());
		} finally {
			if(database != null && database.isOpen()) {
				database.close();
			}
		}
		sendDataBaseChangedBroadCast();
	}
	
	private void sendDataBaseChangedBroadCast() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SMS_MMS_DATABASE_CHANGED);
		SipUAApp.getAppContext().sendBroadcast(intent);
	}

	// 删除对应表中的信息
	public void delete(final String table_name, final String where) {
		SQLiteDatabase database =null;
		try {
			database = this.getWritableDatabase();
			database.delete(table_name, where, null);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "delete from " + table_name + "error:"+e.toString());
		} finally {
			if(database != null && database.isOpen()) {
				database.close();
			}
		}
		sendDataBaseChangedBroadCast();
	}
	
	// 更新对应表信息
	public void update(final String table_name, final String where, final ContentValues values) {
		SQLiteDatabase database =null;
		try {
			database = this.getWritableDatabase();
			database.update(table_name, values, where, null);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "update table " + table_name + "error, where = " + where +" "+e.toString());
		} finally {
			if(database != null && database.isOpen()) {
				database.close();
			}
		}
		sendDataBaseChangedBroadCast();
	}
	public void mUpdate(final String table_name, final String where, final ContentValues values,final String[] wheres) {
		SQLiteDatabase database =null;
		try {
			database = this.getWritableDatabase();
			database.update(table_name, values, where, wheres);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "update table " + table_name + "error, where = " + where +" "+e.toString());
		} finally {
			if(database != null && database.isOpen()) {
				database.close();
			}
		}
		sendDataBaseChangedBroadCast();
	}
	
	// 通过 E_id 获得对应信息ID
	public String getIdByE_id(final String tableName, final String E_id) {
		String _id = null;
		SQLiteDatabase database =null;
		Cursor cursor = null;
		try {
			database = this.getReadableDatabase();
			cursor = database.query(tableName, null, "E_id = ?", new String[]{E_id}, null, null, null);  
			if(cursor != null && cursor.moveToNext()) {
				_id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
			}
		} catch(Exception e) {
			MyLog.e(TAG, "getIdByE_id from " + tableName + "error, E_id = " + E_id+" "+e.toString());
		} finally {
			if(cursor != null) {
				cursor.close();
			}
			if(database != null && database.isOpen()) {
				database.close();
			}
		}
		return _id;
	}
	
	
}
