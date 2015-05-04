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
		super(context, c, true);// 设置第三个参数为true,表示每当数据库数据变化时都会自动更新数据
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
		//默认不可见 
		// ImageView qcbImage = (ImageView) convertView
		// .findViewById(R.id.thread_list_item_photoview);
		//默认不可见 
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
		// 显示未读短信 || 显示联系人的名字
		if (cursor.getInt(cursor.getColumnIndex("status")) == 0) {
			txtMsgName.setText(mUserName
					+ SipUAApp.mContext.getResources().getString(
							R.string.unread));
		} else {
			txtMsgName.setText(mUserName);
		}
		// 显示短信 modify by liangzhang 2014-09-28 当短信内容过长时显示省略号
		txtMsgContent.setText(cursor.getString(cursor.getColumnIndex("body")));
		//显示日期
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
