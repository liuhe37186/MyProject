package com.zed3.sipua.message;

import java.util.List;
import java.util.Map;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.contact.AddContactDialog;
import com.zed3.sipua.ui.contact.CompareTool;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.ContactManager;
import com.zed3.sipua.ui.lowsdk.ContactPerson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MessageToContact extends BaseActivity {
	ListView mUserList;
//	private List<Map<String, Object>> mData;
	private List<ContactPerson> adapterlist;
//	public Map<String, Object> userListClickedItem;
	private Context mContext;
	private MyAdapter mAdapter;
	private View btn_home_message;
	private ContactPerson mContactPerson;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contact_from_message);
		mContext  = this;
		mUserList = (ListView) this.findViewById(R.id.contact_message);
//		mData = ContactUtil.getUsers();
		adapterlist = CompareTool.getInstance().sortByDefault(new ContactManager(mContext).query(ContactManager.QUERY_TYPE_ALL));
		mAdapter = new MyAdapter(adapterlist);
		mUserList.setAdapter(mAdapter);
		mUserList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				if(adapterlist!= null)
					mContactPerson = adapterlist.get(position);
				if(mContactPerson!=null){
					Intent intent = new Intent(mContext, MessageComposeActivity.class);
					intent.putExtra("number", 
							(String) mContactPerson.getContact_num());
					intent.putExtra("name", (String)mContactPerson.getContact_name());
					startActivity(intent);
					finish();
				}
				
				
			}
		});
		btn_home_message = findViewById(R.id.btn_home_message);
		btn_home_message.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, MessageComposeActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		btn_home_message.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView photo_sent_home =(TextView)findViewById(R.id.photo_sent_home2);
				TextView li_photo =(TextView)findViewById(R.id.left_photo2);
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
//					btn_home.setBackgroundResource(R.color.red);
					photo_sent_home.setTextColor(Color.WHITE);
					btn_home_message.setBackgroundResource(R.color.btn_click_bg);
					li_photo.setBackgroundResource(R.drawable.map_back_press);
					break;
				case MotionEvent.ACTION_UP:
//					btn_home.setBackgroundResource(R.color.font_color3);
					photo_sent_home.setTextColor(getResources().getColor(R.color.font_color3));
					btn_home_message.setBackgroundResource(R.color.whole_bg);
					li_photo.setBackgroundResource(R.drawable.map_back_release);
					break;
				}
				return false;
			}
		});
		super.onCreate(savedInstanceState);
	}
	class MyAdapter extends BaseAdapter {

//		private List<Map<String, Object>> mData;
		private LayoutInflater mInflater;
		private  List<ContactPerson> mData;

		public MyAdapter(List<ContactPerson> mData) {
			// TODO Auto-generated constructor stub
			this.mData = mData;
			mInflater = LayoutInflater.from(mContext);
		}

		public void setData(List<ContactPerson> mData2) {
			// TODO Auto-generated method stub
			mData = mData2;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mData.get(position);
		}

		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return super.isEnabled(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(
						R.layout.contact_user_list_item, null);
				convertView.setSelected(true);
				convertView.setEnabled(true);
				holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.info = (TextView) convertView.findViewById(R.id.info);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// holder.img.setBackgroundResource((Integer)
			// mData.get(position).get(
			// "img"));
			holder.title.setText((String) mData.get(position).getContact_name());
			holder.info.setText((String) mData.get(position).getContact_num());


			return convertView;
		}

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	public final class ViewHolder {
		public ImageView img;
		public TextView title;
		public TextView info;
	}
	
}
