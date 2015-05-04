package com.zed3.sipua.message;


import org.zoolu.tools.MyLog;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;


public class MessageMainCursorAdapter extends CursorAdapter{
	private LayoutInflater mInflater;
	private String mAddress;
	private String mUserName;
	private String sipName;

	public MessageMainCursorAdapter(Context context, Cursor c) {
		super(context, c, true);// ���õ���������Ϊtrue,��ʾÿ�����ݿ����ݱ仯ʱ�����Զ���������
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	// changeCursor() add by oumogang 2013-07-16
	@Override
	public void changeCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		super.changeCursor(cursor);
	}
	public void setSelectItem(int position) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		mAddress= cursor.getString(cursor.getColumnIndex("address"));
		sipName = cursor.getString(cursor.getColumnIndex("sip_name"));
		MyLog.e("sipname Main", "sipname = "+sipName);
		mUserName = ContactUtil.getUserName(mAddress);
		if (mUserName == null) {
			if (sipName != null && sipName.equals("")) {
				mUserName = sipName;
			} else {
				mUserName = GroupListUtil.getUserName(mAddress);
				if (mUserName == null) {
					mUserName = mAddress;
				}
			}
		}
		//Ĭ�ϲ��ɼ� 
		// ImageView qcbImage = (ImageView) convertView
		// .findViewById(R.id.thread_list_item_photoview);
		//Ĭ�ϲ��ɼ� 
		// CheckBox ckbMsg = (CheckBox) convertView
		// .findViewById(R.id.thread_list_item_message_checkbox);
		// TextView txtUnReadMsgCount = (TextView) convertView
		// .findViewById(R.id.thread_list_item_message_count);
		TextView txtMsgName = (TextView) convertView
				.findViewById(R.id.thread_list_item_message_name);
		TextView txtMsgContent = (TextView) convertView
				.findViewById(R.id.thread_list_item_message_summary);
		TextView txtMsgTime = (TextView) convertView
				.findViewById(R.id.thread_list_item_time);
		// ��ʾδ������ || ��ʾ��ϵ�˵�����
		if (cursor.getInt(cursor.getColumnIndex("status")) == 0) {
			txtMsgName.setText(mUserName
					+ SipUAApp.mContext.getResources().getString(
							R.string.unread));
		} else {
			txtMsgName.setText(mUserName);
		}
		// ��ʾ���� modify by liangzhang 2014-09-28 ���������ݹ���ʱ��ʾʡ�Ժ�
		txtMsgContent.setText(cursor.getString(cursor.getColumnIndex("body")));
		//��ʾ����
		txtMsgTime.setText(cursor.getString(cursor.getColumnIndex("date")));
	}
//	private String getContact(String number){
//		String contact = number;
//		for(int i = 0; i < mContact.size(); i++)
//		{
//			String info = (String) mContact.get(i).get("info");
//			
//			if (info.equals(number)) {
//				contact = (String) mContact.get(i).get("title");
//				break;
//			}
//		}
//		return contact;
//	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		return mInflater.inflate(R.layout.vlist_message,parent,false);  
	}
}
