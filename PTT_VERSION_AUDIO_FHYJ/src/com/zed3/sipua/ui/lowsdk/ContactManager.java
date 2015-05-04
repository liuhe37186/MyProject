package com.zed3.sipua.ui.lowsdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.zoolu.tools.GroupListInfo;

import com.zed3.sipua.PttGrp;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.contact.ContactUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ContactManager {
	public final static int QUERY_TYPE_ALL = 0; // ȫ����ϵ��
	public final static int QUERY_TYPE_COMMON = 1; // ������ϵ��
	private Context mContext;
	private MyContactDatabase db;

	public ContactManager(Context ctx) {
		this.mContext = ctx;
	}

	/**
	 * @param type
	 *            ��ѯ������
	 * */
	public List<ContactPerson> query(int type) {
		List<ContactPerson> list = new ArrayList<ContactPerson>();
		db = new MyContactDatabase(mContext);
		switch(type){
		case QUERY_TYPE_ALL:
			Cursor cursor = null;
			try {
				cursor = db.mQuery(MyContactDatabase.CUR_LOGINER+" = '"+Settings.getUserName()+"'", null, null);
				if(cursor != null && cursor.getCount() > 0){
					cursor.moveToFirst();
					//��ӵ�һ��
					ContactPerson cperson1 = new ContactPerson();
					String name1 = cursor.getString(cursor.getColumnIndex(MyContactDatabase.CONTACT_NAME));
					cperson1.setContact_name(name1);
					String num1 = cursor.getString(cursor.getColumnIndex(MyContactDatabase.CONTACT_NUM));
					cperson1.setContact_num(num1);
					//Ŀǰ��ʵ�ֳ�����ϵ�ˣ�ʹ�ô�������ͳ��
					list.add(cperson1);
					for(cursor.moveToFirst();cursor.moveToNext();){
						ContactPerson cperson = new ContactPerson();
						String name = cursor.getString(cursor.getColumnIndex(MyContactDatabase.CONTACT_NAME));
						cperson.setContact_name(name);
						String num = cursor.getString(cursor.getColumnIndex(MyContactDatabase.CONTACT_NUM));
						cperson.setContact_num(num);
						//Ŀǰ��ʵ�ֳ�����ϵ�ˣ�ʹ�ô�������ͳ��
						list.add(cperson);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				 if(cursor != null){
				        cursor.close();
				    }
			}
			break;
		case QUERY_TYPE_COMMON://Ŀǰ��ʵ��
			break;
		}
		if (db != null) {
			db.close();
		}
		return list;
	}
	/**
	 * ���ݺ����ѯ��ϵ��
	 */
	public String queryNameByNum(String num){
		db = new MyContactDatabase(mContext);
		String name = "";
		Cursor cursor = db.mQuery(MyContactDatabase.CONTACT_NUM+" = '"+num+"' and "+MyContactDatabase.CUR_LOGINER+" = '"+Settings.getUserName()+"'" , null, null);
		try {
			if(cursor != null && cursor.getCount() >0){
				cursor.moveToFirst();
				name = cursor.getString(cursor.getColumnIndex(MyContactDatabase.CONTACT_NAME));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		if (db != null) {
			db.close();
		}
		return name;
	}
	
	/**
	 * ���ݹؼ���������ϵ��
	 * 
	 * @param keyword
	 * @return List<ContactPerson>
	 */
	public List<ContactPerson> queryContactsByKeyword(String keyword) {
		List<ContactPerson> list = new ArrayList<ContactPerson>();
		db = new MyContactDatabase(mContext);
		Cursor cursor = null;
		try {
			cursor = db.mQuery(null, null, null);
			while (cursor.moveToNext()) {
				String name = cursor.getString(cursor
						.getColumnIndex(MyContactDatabase.CONTACT_NAME));
				String num = cursor.getString(cursor
						.getColumnIndex(MyContactDatabase.CONTACT_NUM));
				if(name.toLowerCase().contains(keyword.toLowerCase()) || num.contains(keyword)){
					ContactPerson cperson = new ContactPerson();
					cperson.setContact_name(name);
					cperson.setContact_num(num);
					list.add(cperson);
				} else {
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}
	
	/**
	 * ���ݺ����ѯ��ϵ��
	 */
	public boolean queryNumExsit(String num){
		db = new MyContactDatabase(mContext);
		boolean flag = false;
		Cursor cursor = db.mQuery(MyContactDatabase.CONTACT_NUM+" = '"+num+"' and "+MyContactDatabase.CUR_LOGINER+" = '"+Settings.getUserName()+"'", null, null);
		try {
			if(cursor != null && cursor.getCount() >0){
				flag = true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		if (db != null) {
			db.close();
		}
		return flag;
	}
	/**
	 * ������ϵ�˱�
	 * */
	public boolean insertContact(ContactPerson cp) {
		db = new MyContactDatabase(mContext);
		ContentValues mContentValues = new ContentValues();
		mContentValues.put(MyContactDatabase.CUR_LOGINER, Settings.getUserName());
		mContentValues.put(MyContactDatabase.CONTACT_NAME, cp.getContact_name());
		mContentValues.put(MyContactDatabase.CONTACT_NUM, cp.getContact_num());
		db.insert(mContentValues);
		if (db != null) {
			db.close();
		}
		return true;
	}

	/**
	 * ����ĳһ����ϵ��
	 * 
	 * @param contact_num
	 *            ��ϵ�˺���
	 * @param field_name
	 *            �޸��ֶε�����
	 * @param field_value
	 *            �޸��ֶε�ֵ
	 * */
	public boolean updateContact(String contact_num, String field_name,
			String field_value) {
		db = new MyContactDatabase(mContext);
		ContentValues values = new ContentValues();
		values.put(field_name, field_value);
		db.update(MyContactDatabase.CONTACT_NUM+" = '"+contact_num+"' and "+MyContactDatabase.CUR_LOGINER+" = '"+Settings.getUserName()+"'", values);
		if (db != null) {
			db.close();
		}
		return true;
	}

	/**
	 * ɾ��ĳ����ϵ��
	 * 
	 * @param contact_num ��ϵ�˺���
	 * */
	public boolean deleteContact(String contact_num) {
		ContactUtil.removeContact(contact_num);
		db = new MyContactDatabase(mContext);
		db.delete(MyContactDatabase.CONTACT_NUM+" = '"+contact_num+"' and "+MyContactDatabase.CUR_LOGINER+" = '"+Settings.getUserName()+"'");
		if (db != null) {
			db.close();
		}
		return true;
	}

	/**
	 * delete all contacts,add by oumogang 2014-01-08
	 */
	public void deleteAllContacts() {
		// TODO Auto-generated method stub
		List<ContactPerson> persons = query(ContactManager.QUERY_TYPE_ALL);
		for (int i = 0; i < persons.size(); i++) {
			deleteContact(persons.get(i).getContact_num());
		}
	}
	/**
	 * delete all contacts,add by oumogang 2014-01-08
	 */
	public void importGroupList() {
		// TODO Auto-generated method stub
		HashMap<PttGrp, ArrayList<GroupListInfo>> groupListsMap = GroupListUtil.getGroupListsMap();
		
		List<ContactPerson> persons = query(ContactManager.QUERY_TYPE_ALL);
		for (int i = 0; i < persons.size(); i++) {
			deleteContact(persons.get(i).getContact_num());
		}
	}
}
