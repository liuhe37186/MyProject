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
	// SQL_CREATE_CALL_HISTORY_TABLE ����ͨ����¼��SQL���
	private static final String SQL_CREATE_CALL_HISTORY_TABLE = "CREATE TABLE call_history"
			+ "(_id integer PRIMARY KEY AUTOINCREMENT , "
			+ "type text ,  "
			+ "begin integer , "
			+ "end integer , "
			+ "begin_str text , "
			+ "name text , " + "number text ," + "status integer DEFAULT 0)";
	// add by liangzhang 2014-09-05 ����status�ֶ�
	// 0��δ�ӵ绰δ�� 1��δ�ӵ绰�Ѷ�
	// add by liangzhang 2014-09-29 ���ݿ�����������֮ǰ������
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
		if (newVersion > oldVersion) {// �������ݿ�汾�Ŵ��ھ����ݿ�汾��ʱ�������ݿ�
			switch (newVersion) {// switch�ṹ�����Ժ����ݿ�����ʹ��
			case 2:
				// ���ݿ����ﱣ֤�������ݿ�ʱ��ǰ�����ݿ����ݲ��ᶪʧ
				db.beginTransaction();
				// ���ƾ����ݿ������ݱ�
				db.execSQL(CREATE_TEMP_TABLE);
				// ���������ݿ�������ֶ�status
				db.execSQL(SQL_CREATE_CALL_HISTORY_TABLE);
				// �������ݱ������ݸ��Ƶ��������ֶε����ݱ���
				db.execSQL(INSERT_DATA);
				// ɾ�����Ƶľ����ݱ�
				db.execSQL(DROP_TEMP_TABLE);
				db.setTransactionSuccessful();
				db.endTransaction();
				break;
			default:
				break;
			}
		}
	}

	// ��ѯ���ݿ�
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
	 * ��������ѯ���ݿ� add by liangzhang 2014-09-05
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

	// �������ݵ����ݿ�
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

	// ɾ����Ӧ���е���Ϣ
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

	// ���¶�Ӧ����Ϣ
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
