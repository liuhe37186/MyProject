package com.zed3.sipua.message;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.net.util.NetChecker;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.toast.MyToast;
import com.zed3.utils.LogUtil;

public class MessageDialogueActivity extends Activity implements
		OnClickListener {
//	private Timer timer ;
	private String TAG = "MessageDialogueActivity";
	public static String RECEIVE_TEXT_MESSAGE = "TEXT_MESSAGE_CHANGED";
	public static String SEND_TEXT_FAIL = "SEND_MESSAGE_FAIL";
	public static String SEND_TEXT_SUCCEED = "SEND_MESSAGE_SUCCEED";
	public static String SEND_TEXT_TIMEOUT = "SEND_MESSAGE_TIMEOUT";
	private IntentFilter mFilter;
	AlertDialog mAlertDlg;
	private Context context;
	private Cursor mCursor;
	private TextView none_message_dialog;
	private ListView lsvItemsMsg;
	private TextView txtMsgName;// ��ϵ������,���Ϊİ��������ʾ�绰����
	private ImageButton imbMsgCall;// ��绰��ť
	private ImageButton imbMsgCall2;// ��绰��ť
	private Button btnSendMsg;// ������Ϣ
	private EditText edtInputMsg;// ��Ϣ�༭��
	private String msgContent;// ���ڴ洢��Ϣ����
	public static final int RESULT_MSG_CHANGED = 1;
	public static final int REQUEST_MSG_EDIT_NEW_CONTACT = 2;
	private SmsMmsDatabase mSmsMmsDatabase;
	public static final String USER_NAME = "userName";
	public static final String USER_NUMBER = "address";
	private View btn_home_message;
	private String draft;
	private String mAddress;
	private String mUserName;
	private Context mContext;
	private boolean isContent = true;//�Ƿ�����÷��ͼ�����
	private BroadcastReceiver recv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(RECEIVE_TEXT_MESSAGE)) {
				LogUtil.makeLog(TAG, "--++>>onReceive RECEIVE_TEXT_MESSAGE:" + msgContent);
				mRefresh();
			}else if(intent.getAction().equalsIgnoreCase(SEND_TEXT_FAIL)){
				LogUtil.makeLog(TAG, "--++>>onReceive SEND_TEXT_FAIL:" + msgContent);
				String mE_id = intent.getStringExtra("0");
				mSmsMmsDatabase = new SmsMmsDatabase(context);
				ContentValues cvs = new ContentValues();
				cvs.put("send", 1);
				mSmsMmsDatabase.update("message_talk", "E_id = '"+mE_id+"'", cvs);
				mRefresh();
			}else if(intent.getAction().equalsIgnoreCase(SEND_TEXT_SUCCEED)){
				LogUtil.makeLog(TAG, "--++>>onReceive SEND_TEXT_SUCCEED:" + msgContent);
				String mE_id = intent.getStringExtra("0");
				mSmsMmsDatabase = new SmsMmsDatabase(context);
				ContentValues cvs = new ContentValues();
				cvs.put("send", 0);
				mSmsMmsDatabase.update("message_talk", "E_id = '"+mE_id+"'", cvs);
				mRefresh();
			}else if(intent.getAction().equalsIgnoreCase(SEND_TEXT_TIMEOUT)){
				LogUtil.makeLog(TAG, "--++>>onReceive SEND_TEXT_TIMEOUT:" + msgContent);
				String mE_id = intent.getStringExtra("0"); 
				mSmsMmsDatabase = new SmsMmsDatabase(context);
				ContentValues cvs = new ContentValues();
				cvs.put("send", 1);
				mSmsMmsDatabase.update("message_talk", "E_id = '"+mE_id+"'", cvs);
				mRefresh();
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_msg_edit);
		mContext = this;
		mFilter = new IntentFilter();
		mFilter.addAction(RECEIVE_TEXT_MESSAGE);
		mFilter.addAction(SEND_TEXT_FAIL);
		mFilter.addAction(SEND_TEXT_SUCCEED);
		mFilter.addAction(SEND_TEXT_TIMEOUT);
		registerReceiver(recv, mFilter);

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		mAddress = bundle.getString(USER_NUMBER);
		// modify by guojunfeng 2013-07-17
		mUserName = /*bundle.getString(USER_NAME)*/null;
		if(ContactUtil.getUserName(mAddress)!=null){
			mUserName = ContactUtil.getUserName(mAddress);
		}
		if(mUserName==null){
			mUserName = GroupListUtil.getUserName(mAddress);
			if(mUserName==null)
				mUserName = mAddress;
		}
		init();// ��ʼ��
		mRefresh();
		Intent intent_ = new Intent();
		intent_.setAction(MainActivity.READ_MESSAGE);
		mContext.sendBroadcast(intent_);
		txtMsgName.setText(mUserName);
		btn_home_message = findViewById(R.id.btn_home_message);
		btn_home_message.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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

		
		
		
		registerForContextMenu(lsvItemsMsg);// ע�������Ĳ˵�
		//����Ǵ�д����ҳ����ת���� ��Ҫ���Ͷ���
		if(bundle.getString("0")!=null&&bundle.getString("0").equals("compose")){
			sendMessage(bundle.getString("toValue"), bundle.getString("bodyValue"));
			mRefresh();
		}
		
	}

	/**
	 * Activity�ĳ�ʼ��
	 */
	private void init() {
		this.context = this;
		lsvItemsMsg = (ListView) findViewById(R.id.lsvItemsMsg);
		txtMsgName = (TextView) findViewById(R.id.txtMsgName);
		imbMsgCall = (ImageButton) findViewById(R.id.imbMsgCall);
		imbMsgCall2 = (ImageButton) findViewById(R.id.imbMsgCall2);
		btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
		edtInputMsg = (EditText) findViewById(R.id.edtInputMsg);
		if(edtInputMsg.getText().toString().length()==0){
			btnSendMsg.setTextColor(getResources().getColor(R.color.disable_color));
			isContent = false;
			
		}
		none_message_dialog = (TextView) findViewById(R.id.none_message_dialog);
		
		
		edtInputMsg.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(100)});

		edtInputMsg.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(s.length()>0){
					btnSendMsg.setTextColor(getResources().getColor(R.color.tab_wihte));
					isContent = true;
				}else{
					btnSendMsg.setTextColor(getResources().getColor(R.color.disable_color));
					isContent = false;
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
		//����вݸ� ��ʾ����
		mSmsMmsDatabase = new SmsMmsDatabase(context);
		Cursor cr = mSmsMmsDatabase.mQuery("message_draft",  "address = '"+mAddress+"'", null, null);
		
		if(cr!=null&&cr.getCount()==1){
			cr.moveToFirst();
			String df =cr.getString(cr.getColumnIndex("body"));
			edtInputMsg.setText(df);
		}
		imbMsgCall.setOnClickListener(this);
		btnSendMsg.setOnClickListener(this);
		imbMsgCall2.setOnClickListener(this);
	}

	/*
	 * ���������Ĳ˵�
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		CommonUtil.Log("menu", "MsgEditActivity", "onCreateContextMenu", 'i');
		menu.setHeaderTitle(this.getResources().getString(R.string.options));
		menu.add(0, 1, 0, this.getResources().getString(R.string.forward));
		menu.add(0, 2, 1, this.getResources().getString(R.string.delete_message_one));
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		CommonUtil.Log("menu", "ContactActivity", "onMenuItemSelected", 'i');
		switch (item.getItemId()) {
		case 1:// ת����Ϣ
			Intent intent = new Intent(context, MessageComposeActivity.class);
			mCursor.getString(mCursor.getColumnIndex("body"));
			intent.putExtra("body", mCursor.getString(mCursor.getColumnIndex("body")));
			startActivity(intent);
			finish();
			break;
		case 2:// ɾ����Ϣ
			mDelete();
			break;
		}
		return true;
	}
	/**
	 * ˢ�½���
	 */
	private void mRefresh() {
		LogUtil.makeLog(TAG, "--++>>mRefresh()");
		//�ҵ�address�����ж��ţ���ʾ����
		mSmsMmsDatabase = new SmsMmsDatabase(context);
		mCursor = mSmsMmsDatabase.mQuery("message_talk","address = '"+mAddress+"'"+" and type = 'sms'"
				, null, null);
		LogUtil.makeLog(TAG, "--++>>mRefresh() cursor length:"+mCursor.getCount());
		if(mCursor==null||mCursor.getCount()<1){
			none_message_dialog.setVisibility(View.VISIBLE);
		}else{
			none_message_dialog.setVisibility(View.GONE);
		}
		MessageDialogueCursorAdapter mAdapter = new MessageDialogueCursorAdapter(
				context, mCursor);
		lsvItemsMsg.setAdapter(mAdapter);
		ContentValues values = new ContentValues();
		values.put("status", 1);
		mSmsMmsDatabase.update("message_talk", "address = '"+mAddress+"'", values);
		if (mSmsMmsDatabase != null) {
			mSmsMmsDatabase.close();
		}
	}
	private void mDelete() {
		String mE_id = mCursor.getString(mCursor.getColumnIndex("E_id"));
		SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(context);
		mSmsMmsDatabase.delete("message_talk", "E_id = '"+mE_id+"'");
		mCursor = mSmsMmsDatabase.mQuery("message_talk", "address = '"+mAddress+"'"+" and type = 'sms'", null, null);
		MyLog.e(TAG+"---cursor length", mCursor.getCount() + "");
		MessageDialogueCursorAdapter mAdapter = new MessageDialogueCursorAdapter(
				context, mCursor);
		lsvItemsMsg.setAdapter(mAdapter);
//		ContentValues values = new ContentValues();
//		values.put("status", 1);
//		mSmsMmsDatabase.update("message_talk", "address = '"+mAddress+"'", values);
		if(mSmsMmsDatabase!=null){
			mSmsMmsDatabase.close();
		}
		// mSmsMmsDatabase.update("message_talk", "status = " +1, 0);
	}
	/**
	 * onClickListener�¼�
	 */
	@Override
	public void onClick(View v) {
		if(!NetChecker.check(MessageDialogueActivity.this, true)){
			return;
		}
		switch (v.getId()) {
		case R.id.imbMsgCall:// ��绰
		{
			//modify by oumogang 2013-07-18
			CallUtil.makeAudioCall(mContext,mAddress,null);
			break;
		}
		case R.id.btnSendMsg:// ���Ͷ���
			if(!isContent){
				return;
			}
			if(mAddress.contains("#")||mAddress.contains("*")){
				MyToast.showToast(true, mContext, getResources().getString(R.string.invalid_char));
				return;
			}
			msgContent = edtInputMsg.getText().toString();
			if (msgContent.equals("")) {
				MyToast.showToast(true, mContext, getResources().getString(R.string.input_message_text));
				return;
			}
			sendMessage(mAddress, msgContent);
			edtInputMsg.setText("");
			mRefresh();
			break;
		case R.id.imbMsgCall2:
			//modify by oumogang 2013-07-18
			// ����Ƶ�绰
			CallUtil.makeVideoCall(mContext,mAddress,null);
			break;
		}
	}
	private void sendMessage(String toValue, String bodyValue) {
		// TODO Auto-generated method stub
		String E_id = Receiver.GetCurUA().SendTextMessage(toValue, bodyValue);
		SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(context);
		ContentValues mContentValues = new ContentValues();
		mContentValues.put("body", bodyValue);
		mContentValues.put("mark", 1);
		mContentValues.put("address", toValue);
		mContentValues.put("status", 1);
		mContentValues.put("date", getCurrentTime());
		mContentValues.put("E_id", E_id);
		mContentValues.put("send", 2);
		mContentValues.put("type", "sms");
		mSmsMmsDatabase.insert("message_talk", mContentValues);
		LogUtil.makeLog(TAG, "--++>>sendMessage()->body:" + bodyValue);
	}

	// ��ȡ��ǰϵͳʱ��
	public String getCurrentTime() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					" yyyy-MM-dd HH:mm ");
			Date curDate = new Date(System.currentTimeMillis());
			String strTime = formatter.format(curDate);
			return strTime;
		} catch (Exception e) {
			
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		draft = edtInputMsg.getText().toString();
		if(draft!=null&&draft.length()>0){
//			CommonUtil.ToastLong(context, "��Ϣ�Ѵ�Ϊ�ݸ�");
			mSmsMmsDatabase = new SmsMmsDatabase(context);
			ContentValues cvs = new ContentValues();
			cvs.put("address", mAddress);
			cvs.put("body", draft);
			mSmsMmsDatabase.delete("message_draft", "address = '"+mAddress+"'");
			
			mSmsMmsDatabase.insert("message_draft", cvs);
		}else{
			mSmsMmsDatabase = new SmsMmsDatabase(context);
			mSmsMmsDatabase.delete("message_draft", "address = '"+mAddress+"'");
		}
		Intent intent_o = new Intent();
		intent_o.setAction(MainActivity.READ_MESSAGE);
		sendBroadcast(intent_o);
		if(mFilter!=null){
			try {
				unregisterReceiver(recv);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				MyLog.e("MessageDialogueActivity", "unregister error");
				e.printStackTrace();
			}
		}
		
		if(mSmsMmsDatabase!=null){
			mSmsMmsDatabase.close();
		}
		if(mCursor!=null){
			mCursor.close();
		}
	}
}
