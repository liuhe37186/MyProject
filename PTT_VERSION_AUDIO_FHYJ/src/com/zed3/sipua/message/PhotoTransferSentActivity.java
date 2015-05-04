package com.zed3.sipua.message;

import org.zoolu.tools.MyLog;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.dialog.DialogUtil;
import com.zed3.dialog.DialogUtil.DialogCallBack;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.utils.Zed3Log;

public class PhotoTransferSentActivity extends BaseActivity{
	
	ListView transfer_sent_list;
	private Cursor mCursor;
	SmsMmsDatabase mSmsMmsDatabase;
	Context mContext;
	View btn_home_photo;
	String MMS = "mms";
	private IntentFilter mFilter;
	PhotoTransferCursorAdapter mAdapter;
	TextView none_photo_transfer;
	private BroadcastReceiver recv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(SmsMmsReceiver.MESSAGE_REPORT_RECEIVE_OK_DATABASE)){
			}else if (intent.getAction().equalsIgnoreCase(SmsMmsDatabase.SMS_MMS_DATABASE_CHANGED)) {
//				mSmsMmsDatabase = new SmsMmsDatabase(mContext);
//				mCursor = mSmsMmsDatabase.mQuery("message_talk", "type = '"+MMS+"' and mark = 1", null, "date desc");
//				mAdapter.changeCursor(mCursor);
//				mAdapter.notifyDataSetChanged();
				addDelayTask(ACTION_UPDATE_MESSAGE_LIST,100);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.aa_transfer_sent);
		btn_home_photo  = findViewById(R.id.btn_home_photo);
		btn_home_photo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btn_home_photo.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView photo_sent_home =(TextView)findViewById(R.id.photo_sent_home);
				TextView li_photo =(TextView)findViewById(R.id.left_photo);
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
//					btn_home.setBackgroundResource(R.color.red);
					photo_sent_home.setTextColor(Color.WHITE);
					btn_home_photo.setBackgroundResource(R.color.btn_click_bg);
					li_photo.setBackgroundResource(R.drawable.map_back_press);
					break;
				case MotionEvent.ACTION_UP:
//					btn_home.setBackgroundResource(R.color.font_color3);
					photo_sent_home.setTextColor(getResources().getColor(R.color.font_color3));
					btn_home_photo.setBackgroundResource(R.color.whole_bg);
					li_photo.setBackgroundResource(R.drawable.map_back_release);
					break;
				}
				return false;
			}
		});
		none_photo_transfer = (TextView) findViewById(R.id.none_photo_transfer);
		transfer_sent_list = (ListView) findViewById(R.id.transfer_sent_list);
		none_photo_transfer.setVisibility(View.VISIBLE);
		//delay to load data for list view. modify by mou 2015-01-22
//		mSmsMmsDatabase = new SmsMmsDatabase(mContext);
//		mCursor = mSmsMmsDatabase.mQuery("message_talk", "type = '"+MMS+"' and mark = 1", null, "date desc");
//		mAdapter = new PhotoTransferCursorAdapter(mContext, mCursor);
//		transfer_sent_list.setAdapter(mAdapter);
		addDelayTask(ACTION_INIT_MESSAGE_LIST,100);
		registerForContextMenu(transfer_sent_list);
//		if(mCursor==null||mCursor.getCount()==0){
//			none_photo_transfer.setVisibility(View.VISIBLE);
//		}
		
		mFilter = new IntentFilter();
		mFilter.addAction(SmsMmsReceiver.MESSAGE_REPORT_RECEIVE_OK_DATABASE);
		mFilter.addAction(SmsMmsDatabase.SMS_MMS_DATABASE_CHANGED);
		registerReceiver(recv, mFilter);
		super.onCreate(savedInstanceState);
		
		addDelayTask(ACTION_INIT_MESSAGE_LIST,100);
	}
	
	private void addDelayTask(int action,long delay) {
		// TODO Auto-generated method stub
		Message message = delayTaskHandler.obtainMessage();
		message.what = action;
		delayTaskHandler.sendMessageDelayed(message, delay);
	}

	final int ACTION_INIT_MESSAGE_LIST = 1;
	final int ACTION_UPDATE_MESSAGE_LIST = 2;
	final int ACTION_UPDATE_CURSOR = 3;
	final int ACTION_SHOW_LOADING_DIALOG = 4;
	final int ACTION_DISSMISS_LOADING_DIALOG = 5;
	
	Handler delayTaskHandler = new Handler(){
		ProgressDialog showProcessDailog;
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ACTION_INIT_MESSAGE_LIST:
				DialogUtil.dismissProcessDailog(showProcessDailog);
				showProcessDailog = DialogUtil.showProcessDailog(PhotoTransferSentActivity.this, getResources().getString(R.string.loading));
				addDelayTask(ACTION_UPDATE_CURSOR,100);
				break;
			case ACTION_UPDATE_MESSAGE_LIST:
				DialogUtil.dismissProcessDailog(showProcessDailog);
				showProcessDailog = DialogUtil.showProcessDailog(PhotoTransferSentActivity.this, getResources().getString(R.string.loading));
				addDelayTask(ACTION_UPDATE_CURSOR,100);
				break;
			case ACTION_UPDATE_CURSOR:
				updateMessageList();
				addDelayTask(ACTION_DISSMISS_LOADING_DIALOG,100);
				break;
			case ACTION_DISSMISS_LOADING_DIALOG:
				DialogUtil.dismissProcessDailog(showProcessDailog);
				break;

			default:
				break;
			}
		}
		public void updateMessageList() {
			// TODO Auto-generated method stub
			if (mSmsMmsDatabase == null) {
				mSmsMmsDatabase = new SmsMmsDatabase(mContext);
			}
			mCursor = mSmsMmsDatabase.mQuery(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = '"+MMS+"' and mark = "+SmsMmsDatabase.TYPE_SEND, null, "date desc");
			if (mAdapter == null) {
				mAdapter = new PhotoTransferCursorAdapter(mContext, mCursor,SmsMmsDatabase.TYPE_SEND);
				transfer_sent_list.setAdapter(mAdapter);
				transfer_sent_list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position,
							long arg3) {
						String imageFilePath = mCursor.getString(mCursor.getColumnIndex("attachment"));
						imageFilePath = Uri.parse(imageFilePath).getPath();
						
						String body = mCursor.getString(mCursor.getColumnIndex("body"));
						
						Intent intent = new Intent(PhotoTransferSentActivity.this,MmsMessageDetailActivity.class);
						intent.putExtra(MmsMessageDetailActivity.MESSAGE_BODY, body);
						intent.putExtra(MmsMessageDetailActivity.MESSAGE_PIC_PATH, imageFilePath);
						
						startActivity(intent);
					}
					
				});
				transfer_sent_list.setOnScrollListener(mAdapter.getOnScrollListener());
			}else {
				mAdapter.changeCursor(mCursor);
			}
			if(mCursor==null||mCursor.getCount()==0){
				none_photo_transfer.setVisibility(View.VISIBLE);
			}else {
				none_photo_transfer.setVisibility(View.GONE);
			}
		}
	};
	
	
	@Override
	protected void onDestroy() {
		if(mSmsMmsDatabase!=null){
			mSmsMmsDatabase.close();
		}
		if(mCursor!=null){
			mCursor.close();
		}
		if(mFilter!=null){
			try {
				unregisterReceiver(recv);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				MyLog.e("PhotoTransferSentActivity", "unregister error");
				e.printStackTrace();
			}
		}
		unregisterForContextMenu(transfer_sent_list);
		super.onDestroy();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		CommonUtil.Log("menu", "MsgEditActivity", "onCreateContextMenu", 'i');
		menu.setHeaderTitle(R.string.options);
		menu.add(0, 2, 2, getResources().getString(R.string.photo_transfer_delete_all));
		menu.add(0, 1, 1, getResources().getString(R.string.photo_transfer_delete));
		menu.add(0, 3, 3, getResources().getString(R.string.reupload));
//		menu.add(0, 3, 0, "ÖØÐÂ·¢ËÍ");
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		CommonUtil.Log("menu", "ContactActivity", "onMenuItemSelected", 'i');
		Resources resources = getResources();
		switch (item.getItemId()) {
		case 2:
			String title = resources.getString(R.string.photo_transfer_delete_all_title);
			String message = resources.getString(R.string.photo_transfer_delete_all_message);
			String check = resources.getString(R.string.photo_transfer_delete_all_ack);
			DialogUtil.showSelectDialog(PhotoTransferSentActivity.this, title, message, check, new DialogCallBack() {
				@Override
				public void onPositiveButtonClick() {
					// TODO Auto-generated method stub
					SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(mContext);
					mSmsMmsDatabase.delete("message_talk", "type = '"+MMS+"' and mark = 1");
					mCursor = mSmsMmsDatabase.mQuery("message_talk", "type = '"+MMS+"' and mark = 1", null, null);
					mAdapter.changeCursor(mCursor);
					if(mCursor==null||mCursor.getCount()==0){
						none_photo_transfer.setVisibility(View.VISIBLE);
					}
					if(mSmsMmsDatabase!=null){
						mSmsMmsDatabase.close();
					}
				}
				@Override
				public void onNegativeButtonClick() {
					// TODO Auto-generated method stub
				}
			});
			
			break;
		case 1:
			title = resources.getString(R.string.photo_transfer_delete_title);
			message = resources.getString(R.string.photo_transfer_delete_message);
			check = resources.getString(R.string.photo_transfer_delete_ack);
			DialogUtil.showSelectDialog(PhotoTransferSentActivity.this, title, message, check, new DialogCallBack() {
				@Override
				public void onPositiveButtonClick() {
					// TODO Auto-generated method stub
					String mE_id = mCursor.getString(mCursor.getColumnIndex("E_id"));
					SmsMmsDatabase mSmsMmsDatabase_ = new SmsMmsDatabase(mContext);
					mSmsMmsDatabase_.delete("message_talk", "E_id = '"+mE_id+"'");
//					mCursor = mSmsMmsDatabase_.mQuery("message_talk", "type = '"+MMS+"'", null, null);
					mCursor = mSmsMmsDatabase_.mQuery("message_talk", "type = '"+MMS+"' and mark = 1", null, "date desc");
					mAdapter.changeCursor(mCursor);
					if(mCursor==null||mCursor.getCount()==0){
						none_photo_transfer.setVisibility(View.VISIBLE);
					}
					if(mSmsMmsDatabase_!=null){
						mSmsMmsDatabase_.close();
					}
					// mSmsMmsDatabase.update("message_talk", "status = " +1, 0);
				}
				@Override
				public void onNegativeButtonClick() {
					// TODO Auto-generated method stub
				}
			});
			break;
		case 3:
//			Intent intent = new Intent(mContext, PhotoTransferActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			intent.putExtra("body", mCursor.getString(mCursor.getColumnIndex("body")));
//			intent.putExtra("address", mCursor.getString(mCursor.getColumnIndex("address")));
//			intent.putExtra("attachment", mCursor.getString(mCursor.getColumnIndex("attachment")));
//			intent.putExtra("action", "resend");
//			startActivity(intent);
//			finish();
			title = resources.getString(R.string.reupload_title);
			message = resources.getString(R.string.reupload_message);
			check = resources.getString(R.string.reupload_ack);
			DialogUtil.showSelectDialog(PhotoTransferSentActivity.this, title, message, check, new DialogCallBack() {
				@Override
				public void onPositiveButtonClick() {
					// TODO Auto-generated method stub
					new Thread(new Runnable() {
						@Override
						public void run() {
							String E_id = mCursor.getString(mCursor.getColumnIndex("E_id"));
							String toValue = mCursor.getString(mCursor.getColumnIndex("address"));
							String bodyValue = mCursor.getString(mCursor.getColumnIndex("body"));
							Uri imageFileUri = Uri.parse(mCursor.getString(mCursor.getColumnIndex("attachment")));
							// TODO Auto-generated method stub
							MessageSender.setSendDataId(E_id );
							MessageSender sender = new MessageSender(mContext,
									toValue, bodyValue, imageFileUri, "image/jpg",
									E_id.substring(3, 12) + ".jpg", E_id);
							Zed3Log.debug("mmsTrace", "PhotoTransfterActivity#run enter data id = " + E_id);
							sender.reUploadPhoto(E_id);
						}
					}).start();
				}
				@Override
				public void onNegativeButtonClick() {
					// TODO Auto-generated method stub
				}
			});
			break;
		}
		return true;
	}
}
