package com.zed3.sipua.ui.contact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.lowsdk.ContactManager;
import com.zed3.toast.MyToast;

public class AddContactDialog extends BaseActivity implements OnClickListener {
	public static final int CREATE = 1;//创建联系人
	public static final int MODIFY = 2;//修改联系人
	public static final int ADD = 3;//添加到联系人
	public static final int SAVE = 4;//保存到联系人
	public static final String TYPE = "type";
	private static final String TITLE_CREATE = SipUAApp.mContext.getResources().getString(R.string.new_contact);//新建联系人
	private static final String TITLE_MODIFY = SipUAApp.mContext.getResources().getString(R.string.change_contact);//修改联系人
	private static final String TITLE_ADD = SipUAApp.mContext.getResources().getString(R.string.add_contact);//添加联系人
	
	private static final String COMMIT_CREATE = SipUAApp.mContext.getResources().getString(R.string.save);//保存
	private static final String COMMIT_MODIFY = SipUAApp.mContext.getResources().getString(R.string.edit);//修改
	private static final String COMMIT_ADD = SipUAApp.mContext.getResources().getString(R.string.add);//添加
	
	public static final String USER_NAME = "title";
	public static final String USER_NUMBER = "info";
	private TextView titleTextView;
	private EditText nameEditText;
	private EditText numberEditText;
	private String name;
	private String number;
	public static int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_add_dialog);
		
		titleTextView = (TextView)findViewById(R.id.contact_user_title);
		nameEditText = (EditText) findViewById(R.id.contact_user_name);
		numberEditText = (EditText) findViewById(R.id.contact_user_number);
		findViewById(R.id.user_cancel).setOnClickListener(this);
		TextView saveButton = (TextView) findViewById(R.id.user_save);
		saveButton.setOnClickListener(this);
		
		//add by oumogang 2013-05-20
		//设置文本框输入字数上限；              
		nameEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(12)
        });
		numberEditText.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(15)
		});
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			type = extras.getInt(TYPE);
			switch (type) {
			case CREATE:
				titleTextView.setText(TITLE_CREATE);
				saveButton.setText(COMMIT_CREATE);
				break;
			case MODIFY:
				titleTextView.setText(TITLE_MODIFY);
				saveButton.setText(COMMIT_MODIFY);
				break;
			case ADD:
				titleTextView.setText(TITLE_ADD);
				saveButton.setText(COMMIT_ADD);
				break;
			default:
				break;
			}
			if (type != CREATE) {
				name = extras.getString(USER_NAME);
				if (name != null) {
					nameEditText.setText(name);
				}
				number = extras.getString(USER_NUMBER);
				if (number != null) {
					numberEditText.setText(number);
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		type = 0;
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.user_cancel:
			finish();
			break;
		case R.id.user_save:
			name = nameEditText.getText().toString().trim();
			if (name.length()>20) {
				name = name.substring(0, 20);
			}
			if (name.equals("")) {
				MyToast.showToast(true, getApplicationContext(), getResources().getString(R.string.name_is_blank));
				return;
			}
//			String regExs= "^[a-zA-Z0-9\u4E00-\u9FA5]*$";
//			Pattern   p   =   Pattern.compile(regExs);    
//			Matcher   m   =   p.matcher(name); 
//			if(!m.find()){
//				MyToast.showToast(true, getApplicationContext(), "名称中含有无效字符");
//				return;
//			}
			number = numberEditText.getText().toString().trim();
			if (number.length()>20) {
				number = number.substring(0, 20);
			}
			if (number.equals("")) {
				MyToast.showToast(true, getApplicationContext(), getResources().getString(R.string.number_is_blank));
				return;
			}
			
			if (name == null || name.equals("") || number == null
					|| number.equals("")) {
				// houyuchun modify 20120620 begin 
				MyToast.showToast(true, this, R.string.toast_null_name_or_number);
				// houyuchun modify 20120620 end 
			} else {
				//modify by hu 2013-09-26
				ContactManager cm = new ContactManager(AddContactDialog.this);
//				String contact_name = cm.queryNameByNum(number);
				boolean flag = cm.queryNumExsit(number);
				if(flag){
					Toast.makeText(AddContactDialog.this, getResources().getString(R.string.contact_alrady_exist), Toast.LENGTH_SHORT).show();
				}else{
					ContactUtil.addContacts(this,name, number);
					sendBroadcast(new Intent("com.zed3.contactfresh"));
				}
//				Intent intent;
//				Bundle extras;
//				switch (type) {
//				case CREATE:  
//					ContactUtil.addContacts(this,name, number);
//					break;
//				case MODIFY://修改联系人
//					if (SipUAApp.isLowSdk) {
//						ContactUtil.change(getApplicationContext(), ContactActivity4LowSdk.mIndex, name, number);
//					}else {
////						ContactUtil.change(getApplicationContext(), UserListFragment.mIndex, name, number);
//					}
//
//					break;
//				case ADD://从组列表添加到联系人
//					if (ContactUtil.addContacts(this,name, number)) {
////						if (ContactActivity.isActivityCreated) {
////							ContactActivity.setViewPagerCurrentItem(0);
////						}
//					}
//					break;
//					
//				case SAVE://创建联系人
//					ContactUtil.addContacts(getApplicationContext(), name,
//							number);
//					break;
//					
//				default:
//					break;
//				}

				finish();
			}

			break;

		default:
			break;
		}

	}
	
	
	
	
	

}
