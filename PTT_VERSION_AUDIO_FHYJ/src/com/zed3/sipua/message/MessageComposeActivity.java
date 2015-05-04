package com.zed3.sipua.message;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.toast.MyToast;

public class MessageComposeActivity extends BaseActivity implements OnClickListener {
	private Context context;
//	private MsgManager msgMgr;
//	private ContactManager contactMgr;
//	private TelManager telMgr;
//	private GridView gridViewDisplayBtnContact;// 展示收信人的一个布局
	private Button btnSendMsg;// 发送信息
	public  static  String edtTransfer="";
	private EditText edtInputMsg;// 信息编辑框
	private EditText edtInputMsger;// 联系人编辑框
	private String toValue;
	private String bodyValue;
	private String userName;
	private String userNum ;
	private View btn_home_message2;
	private boolean isContent = false;
	//通过bundle传入的短信
	String mbody;
//	private GridView gridViewRecentContact;// 展示最近发送信息的联系人
//	private List<String> recieverTel;// 收件人电话
//	private List<String> recieverName;// 收件 人姓名
	
	private ImageView imbContact;

	public static final int REQUEST_SELECT_SYS_CONTACT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(  
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);  
		setContentView(R.layout.activity_new_message);
		init();// 初始化
		// 发送信息
		btnSendMsg.setOnClickListener(this);
		
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if(bundle!=null){
			userNum= bundle.getString("number");
			userName = bundle.getString("name");
			if(userNum!=null){
				edtInputMsger.setText(userNum);
			}
		}
		if(edtInputMsger.getText().toString().length()>0&&
				edtInputMsg.getText().length()>0){
			isContent = true;
			btnSendMsg.setTextColor(getResources().getColor(R.color.tab_wihte));
		}else{
			isContent = false;
			btnSendMsg.setTextColor(getResources().getColor(R.color.disable_color));
		}
		btn_home_message2 = findViewById(R.id.btn_home_message2);
		btn_home_message2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btn_home_message2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView photo_sent_home =(TextView)findViewById(R.id.photo_sent_home3);
				TextView li_photo =(TextView)findViewById(R.id.left_photo3);
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
//					btn_home.setBackgroundResource(R.color.red);
					photo_sent_home.setTextColor(Color.WHITE);
					btn_home_message2.setBackgroundResource(R.color.btn_click_bg);
					li_photo.setBackgroundResource(R.drawable.map_back_press);
					break;
				case MotionEvent.ACTION_UP:
//					btn_home.setBackgroundResource(R.color.font_color3);
					photo_sent_home.setTextColor(getResources().getColor(R.color.font_color3));
					btn_home_message2.setBackgroundResource(R.color.whole_bg);
					li_photo.setBackgroundResource(R.drawable.map_back_release);
					break;
				}
				return false;
			}
		});
	}

	/**
	 * Activity的初始化
	 */
	private void init() {
		this.context = this;
		btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
		edtInputMsg = (EditText) findViewById(R.id.edtInputMsg);
		edtInputMsg.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(100)});
		edtInputMsger = (EditText) findViewById(R.id.edtInputMsger);
		String content = this.getIntent().getStringExtra("content");
		if (content != null && !content.equals(""))// 表明是信息转发
		{
			edtInputMsg.setText(content);
			edtTransfer = content;
		}else{
			if(!edtTransfer.equals("")){
				edtInputMsg.setText(edtTransfer);
			}
		}
			
		imbContact = (ImageView) findViewById(R.id.contact);
		imbContact.setOnClickListener(this);
		Intent intent = this.getIntent();
		if(intent.getExtras()!=null){
			mbody = (java.lang.String) intent.getExtras().get("body");
			if(intent.getExtras().get("body")!=null){
				edtInputMsg.setText(mbody);
			}
		}
		edtInputMsg.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(s.length()>0&&edtInputMsger.getText().toString().length()>0){
					isContent = true;
					btnSendMsg.setTextColor(getResources().getColor(R.color.tab_wihte));
				}else{
					isContent = false;
					btnSendMsg.setTextColor(getResources().getColor(R.color.disable_color));
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				edtTransfer = edtInputMsger.getText().toString();
			}
		});
		edtInputMsger.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(s.length()>0&&edtInputMsg.getText().toString().length()>0){
					isContent = true;
					btnSendMsg.setTextColor(getResources().getColor(R.color.tab_wihte));
				}else{
					isContent = false;
					btnSendMsg.setTextColor(getResources().getColor(R.color.disable_color));
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/**
	 * 获取最近联系人的姓名
	 * 
	 * @return
	 */

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btnSendMsg:
			if(!isContent){
				return;
			}
			if(!NetChecker.check(MessageComposeActivity.this, true)){
				break;
			}
			bodyValue = edtInputMsg.getText().toString();
			toValue = edtInputMsger.getText().toString();
			if(toValue.contains("#")||toValue.contains("*")){
				MyToast.showToast(true, context, context.getResources()
						.getString(R.string.invalid_char));
				return;
			}
			Intent intent = new Intent(context, MessageDialogueActivity.class);
			intent.putExtra(MessageDialogueActivity.USER_NAME, toValue);
			intent.putExtra(MessageDialogueActivity.USER_NUMBER, toValue);
			intent.putExtra("0", "compose");
			intent.putExtra("bodyValue", bodyValue);
			intent.putExtra("toValue", toValue);
			edtTransfer = "";
			startActivity(intent);
			this.finish();
			break;
			
		case R.id.contact:
			Intent cIntent = new Intent(this, MessageToContact.class);
//			cIntent.putExtra("message", "message");
			startActivity(cIntent);
			edtTransfer = edtInputMsg.getText().toString();
			finish();
			break;
		}
	}


	// 获取当前系统时间
		public String getCurrentTime() {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(
						" yyyy-MM-dd HH:mm ");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时
				String strTime = formatter.format(curDate);
				return strTime;
			} catch(Exception e) {
				
			}
			return null;
		}
}
