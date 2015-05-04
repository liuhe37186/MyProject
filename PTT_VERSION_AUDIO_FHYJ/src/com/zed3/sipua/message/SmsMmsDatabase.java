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
	// SQL_CREATE_SMS_MMS_TABLE �����շ������ж̲��ű�SQL���
	private static final String SQL_CREATE_MESSAGE_TALK_TABLE = "CREATE TABLE message_talk " +
			"(_id integer PRIMARY KEY AUTOINCREMENT , " +
			"E_id text ,  " +
			"address text , " +//���һỰ���˵ĺ���
			"contact_name text , " +//���һỰ��������ϵ�������е�����
			"sip_name text , " +//���һỰ������Sip��DisplayName
			"body text , " +
			"status integer DEFAULT 0 , " +//1�Ƿ������ 0Ϊδ��
			"mark integer DEFAULT 0 , " +//0Ϊ�յ��Ķ�Ϣ 1Ϊ�����Ķ���
			"attachment text , " +
			"attachment_name text, " +
			"send integer DEFAULT 2, " +//0Ϊ���ͳɹ���1Ϊ����ʧ��2Ϊ�����У�3Ϊ�������
			"type text , " +//����Ϣ���� ����Ϊ��mms�� ����Ϊ��sms��
			"date text)";  
	// SQL_CREATE_SMS_MMS_DRAFT_TABLE �����̲��Ųݸ���SQL���
	private static final String SQL_CREATE_MESSAGE_DRAFT_TABLE = "CREATE TABLE message_draft " +
			"(_id integer PRIMARY KEY AUTOINCREMENT , " +
			"address text , " +//���һỰ���˵ĺ���
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
    
	// ��ѯ���ݿ� by order
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
	//guojunfeng add ��ѯ���ݿ� by where  
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
	
	// �������ݵ����ݿ�
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

	// ɾ����Ӧ���е���Ϣ
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
	
	// ���¶�Ӧ����Ϣ
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
	
	// ͨ�� E_id ��ö�Ӧ��ϢID
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
