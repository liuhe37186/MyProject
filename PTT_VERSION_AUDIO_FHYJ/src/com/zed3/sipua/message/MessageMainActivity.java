/**
 * 
 */
package com.zed3.sipua.message;

import java.util.Map;

import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zed3.flow.FlowRefreshService;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.OneShotAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;

public class MessageMainActivity extends BaseActivity implements OnClickListener {
	private IntentFilter mFilter;
	private SmsMmsDatabase mSmsMmsDatabase;//短信数据库
	
	public static final int REQUEST_MSG_EDIT = 1;//编辑界面
	public static final int REQUEST_MSG_NEW_CONTACT = 2;//新建联系人界面
	private ListView lsvMsg;
	private ImageView imbNewMsg;
	private RelativeLayout newMsg;
	private String SMS  = "sms";
	//mogang
	private TextView none_message;
	public Map<String, Object> clickedItem;
	public boolean isEditMode;
	public static final String COLOR_LIGHT = "#FFFFFFF";
    String GET_NEWEST_MESSAGE = "select * from message_talk ";
    //select * from sms group by address order by date desc
    String TABLE_NAME = "message_talk";
	private static Activity mContext ;
	private View mRootView;
	private Cursor mCursor;
	private int mPosition;
	// menu
	private View popupView;
	private PopupWindow popupWindow;
	private ScaleAnimation sa;
	private LinearLayout menuPopupExit;
	private LinearLayout menuPopupSetting;
//	private ImageView imbContact;
			
	private BroadcastReceiver recv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context mContext, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE)) {
				refresh();
			}
		}
	};
	private MessageMainCursorAdapter mMessageCursorAdapter;

	private void exitApp() {
		finish();
		// Can't create handler inside thread that has not called
		// Looper.prepare() modify by liangzhang 2014-10-16
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// // TODO Auto-generated method stub
		Receiver.engine(MessageMainActivity.this).expire(-1);
		// 延迟2秒
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// deleted by hdf 停止
		Receiver.engine(MessageMainActivity.this).halt();
		// 停止服务
		stopService(new Intent(MessageMainActivity.this, RegisterService.class));
		// 取消全局定时器
		Receiver.alarm(0, OneShotAlarm.class);

		// 跳转手机桌面
		Intent intent_ = new Intent(Intent.ACTION_MAIN);
		intent_.addCategory(Intent.CATEGORY_HOME);
		intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent_);
		System.exit(0);
		// }
		// }).start();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		mContext = this;
		mRootView = getLayoutInflater().inflate(R.layout.activity_message,null);
		mRootView.setOnClickListener(this);
		mFilter = new IntentFilter();
		mFilter.addAction(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE);
		mContext.registerReceiver(recv, mFilter);
		
		lsvMsg = (ListView) findViewById(R.id.lsvMessage);
		registerForContextMenu(lsvMsg);// 注册上下文菜单
		imbNewMsg = (ImageView) findViewById(R.id.imbNewMessage);
		newMsg = (RelativeLayout) findViewById(R.id.rlMsgTitleBar);
//		imbContact = (ImageView) findViewById(R.id.contact);
		imbNewMsg.setOnClickListener(this);
		
		mSmsMmsDatabase = new SmsMmsDatabase(mContext);
		none_message = (TextView) findViewById(R.id.none_message);
		refresh();
		lsvMsg.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//guojunfeng TO DO
				Intent intent = new Intent(mContext, MessageDialogueActivity.class);
				intent.putExtra(MessageDialogueActivity.USER_NUMBER, 
						mCursor.getString(mCursor.getColumnIndex("address")));
				startActivity(intent);
				}
				
		});
		if (GroupListUtil.mGroupListsMap.size() == 0) {
			GroupListUtil.getData4GroupList();
		}
		
		
	};
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		CommonUtil.Log("menu", "MsgEditActivity", "onCreateContextMenu", 'i');
		menu.setHeaderTitle(R.string.options);
		menu.add(0, 2, 1, getResources().getString(R.string.delete_message));
		menu.add(0, 3, 2, getResources().getString(R.string.delete_all_message));
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		CommonUtil.Log("menu", "ContactActivity", "onMenuItemSelected", 'i');
		switch (item.getItemId()) {
		case 1:// 转发信息
			Intent it = new Intent(mContext, FlowRefreshService.class);
			mContext.stopService(it);
			exitApp();
			break;
		case 2:// 删除信息
			mDelete();
			break;
		case 3:// 删除信息
			mDeleteAll();
			break;
		}
		return true;
	}
	private void mDelete() {
		// TODO Auto-generated method stub
		String address = mCursor.getString(mCursor.getColumnIndex("address"));
		SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(mContext);
		mSmsMmsDatabase.delete("message_talk", "address = '"+address+"'"+"and type = 'sms'");
//		CommonUtil.ToastLong(mContext, "对话已删除");
		refresh();
		Intent intent = new Intent(MainActivity.READ_MESSAGE);
		mContext.sendBroadcast(intent);
		// mSmsMmsDatabase.update("message_talk", "status = " +1, 0);
	
	}
	private void mDeleteAll() {
		// TODO Auto-generated method stub
		String address = mCursor.getString(mCursor.getColumnIndex("address"));
		SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(mContext);
		mSmsMmsDatabase.delete("message_talk", "type = 'sms'");
//		CommonUtil.ToastLong(mContext, "对话已删除");
		refresh();
		Intent intent = new Intent(MainActivity.READ_MESSAGE);
		mContext.sendBroadcast(intent);
		// mSmsMmsDatabase.update("message_talk", "status = " +1, 0);
		
	}
	private void refresh() {
		// TODO Auto-generated method stub
	mCursor = mSmsMmsDatabase.mQuery("message_talk", "type = 'sms'", "address", "date desc");
	if(mCursor==null||mCursor.getCount()<1){
		none_message.setVisibility(View.VISIBLE);
	}else{
		none_message.setVisibility(View.GONE);
	}
	if (mMessageCursorAdapter == null) {
		/*MessageMainCursorAdapter */mMessageCursorAdapter = new MessageMainCursorAdapter(mContext, mCursor);
		lsvMsg.setAdapter(mMessageCursorAdapter);
	}else {
		mMessageCursorAdapter.changeCursor(mCursor);
	}
	}


//	private void ShowCurrentGrp() {
//		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
//		if (pttGrp != null) {
//			CurGrpID=pttGrp.grpID;
//		}
//	}
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "发送").setIcon(
//				R.drawable.message_send);
//		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "消息记录").setIcon(
//				R.drawable.message_history);
//		return true;
//	}
//
//	@Override

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
//		Receiver.engine(mContext);
//		ShowCurrentGrp();
//		
//		if(MemoryMg.getInstance().CallNum!="")
//		{
//			//赋值
//			mTextNum.setText(MemoryMg.getInstance().CallNum);	
//			MemoryMg.getInstance().CallNum="";
//		}
//		隐藏 对讲界面的panel
		refresh();
		super.onResume();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(mFilter!=null){
			
			try {
				mContext.unregisterReceiver(recv);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				MyLog.e("MessageMainActivity", "unregister error");
				e.printStackTrace();
			}
		}
		if(mCursor!=null)
			mCursor.close();
		super.onDestroy();
	}

	public boolean dismissMenuPopupWindows() {
		// TODO Auto-generated method stub
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			return true;
		} else {
			return false;
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		dismissMenuPopupWindows();
		Intent intent;
		switch (v.getId()) {
		case R.id.contact:
			intent = new Intent(mContext, MessageToContact.class);
//			intent.putExtra("message", "message");
			startActivity(intent);
			break;
		case R.id.imbNewMessage:
			intent = new Intent(mContext, MessageComposeActivity.class);
			mContext.startActivity(intent);
			break;

		default:
			break;
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		
		case KeyEvent.KEYCODE_BACK:
//			event.changeAction(event,KeyEvent.KEYCODE_MENU );
			if (dismissMenuPopupWindows()) {
				return true;
			}else {
				break;
			}
		case KeyEvent.KEYCODE_HOME:
			break;
		case KeyEvent.KEYCODE_MENU:
			if (dismissMenuPopupWindows()) {
				return true;
			}else {
				break;
			}

		default:
//			super.onKeyDown(keyCode, event);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
