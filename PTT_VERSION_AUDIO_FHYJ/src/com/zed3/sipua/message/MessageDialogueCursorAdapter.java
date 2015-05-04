package com.zed3.sipua.message;

import com.zed3.sipua.R;
import com.zed3.utils.LogUtil;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MessageDialogueCursorAdapter extends CursorAdapter {
	private LayoutInflater mInflater;
	public MessageDialogueCursorAdapter(Context context, Cursor c) {
		// TODO Auto-generated constructor stub
		super(context, c);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		int mark = cursor.getInt(cursor.getColumnIndex("mark"));
		int send = cursor.getInt(cursor.getColumnIndex("send"));
		
		RelativeLayout rl_send =(RelativeLayout)view.findViewById(R.id.send);
		RelativeLayout rl_receive =(RelativeLayout)view.findViewById(R.id.receive);
		if(mark == 0){
			rl_send.setVisibility(View.GONE);
			rl_receive.setVisibility(View.VISIBLE);
			//短信时间
			TextView txtDate_receive = (TextView) view.findViewById(R.id.txtDate_receive);
			//短信内容
			TextView txtMsgContent = (TextView) view
					.findViewById(R.id.txtMsgContent_receive);
			LogUtil.makeLog("MessageDialogueCursorAdapter", "--++>>收到的短信，在DB中得到的数据："+cursor.getString(cursor.getColumnIndex("body")));			
			txtMsgContent.setText(cursor.getString(cursor.getColumnIndex("body")));
			txtDate_receive.setText(cursor.getString(cursor.getColumnIndex("date")));
			ImageView imageDot =(ImageView) view.findViewById(R.id.imgLeftDot);
			imageDot.setBackgroundResource(R.drawable.dot);
//			if(send==2){
//				imageDot.setBackgroundResource(R.drawable.led_ing);
//			}
////				imageDot.setBackgroundResource(R.drawable.icon64)
//			if(send==1&&imageDot!=null){
//				imageDot.setBackgroundResource(R.drawable.dot);
//			}
//			if(send==0){
//				imageDot.setBackgroundResource(R.drawable.led_error);
//			}
		}else{
			rl_send.setVisibility(View.VISIBLE);
			rl_receive.setVisibility(View.GONE);
			//短信内容
			TextView txtMsgContent = (TextView) view
					.findViewById(R.id.txtMsgContent_send);
			txtMsgContent.setText(cursor.getString(cursor.getColumnIndex("body")));
			LogUtil.makeLog("MessageDialogueCursorAdapter", "--++>>发送的短信，在DB中得到的数据："
			+cursor.getString(cursor.getColumnIndex("body"))+"	状态值send:"+send);
			//短信时间
			TextView txtDate = (TextView) view.findViewById(R.id.txtDate_send);
			txtDate.setText(cursor.getString(cursor.getColumnIndex("date")));
			//modify by liangzhang 2014-11-01 修改发送状态显示不对的问题
			//短信发送状态：0-成功 	1-失败	2-发送中
			ImageView imageDot =(ImageView) view.findViewById(R.id.imgRightDot);
			switch (send) {
			case 0:
				imageDot.setImageResource(R.drawable.dot);
				break;
			case 1:
				imageDot.setImageResource(R.drawable.led_error);
				break;
			case 2:
				imageDot.setImageResource(R.drawable.led_ing);
				break;

			default:
				break;
			}
			//短信状态文字
			//TextView txtView = (TextView) view.findViewById(R.id.txtSend);
			/*if(send==2){
				imageDot.setBackgroundResource(R.drawable.led_ing);
				txtView.setText(R.string.sending);
			}
			imageDot.setBackgroundResource(R.drawable.icon64)
			if(send==1&&imageDot!=null){
				imageDot.setBackgroundResource(R.drawable.dot);
				txtView.setText("");
			}
			if(send==0){
				txtView.setText(R.string.failed);
				imageDot.setBackgroundResource(R.drawable.led_error);
			}*/
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
//		if (cursor.getInt(cursor.getColumnIndex("mark")) == 0)// 接收到的信息
//			return  mInflater.inflate(R.layout.vlist_receive_msgs,
//					null);
//		else
//			return mInflater.inflate(R.layout.vlist_send_msgs, null);
		return mInflater.inflate(R.layout.msg_show, null);
	}
	
}
