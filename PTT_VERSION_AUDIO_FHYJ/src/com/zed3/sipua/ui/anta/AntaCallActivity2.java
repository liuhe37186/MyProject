package com.zed3.sipua.ui.anta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zoolu.tools.GroupListInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.log.MyLog;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.message.MessageComposeActivity;
import com.zed3.sipua.ui.CallActivity2;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.contact.MeetingCompareTool;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.toast.MyToast;
import com.zed3.utils.DialogMessageTool;
import com.zed3.utils.Tools;
import com.zed3.utils.Zed3Log;

public class AntaCallActivity2 extends Activity implements OnClickListener {

	public class SelectTag {

		private int position;
		private boolean isSelected;

		public void setPosition(int position) {
			// TODO Auto-generated method stub
			this.position = position;
		}

		public boolean isSelected() {
			return isSelected;
		}

		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}

		public int getPosition() {
			return position;
		}

	}

	private static final String TAG = "AntaCallActivity2SS";
	private ViewGroup mRootView;
	private View showContactList;
	private View hideContactList;
	private View makeConferenceCall;
	private View makeGroupBroadcastCall;
	private ListView callList;
	private ListView contactList;

	// 被叫用户 popupwindow界面
	private LinearLayout userList_popup_cancel;
	private LinearLayout userList_popup_move;
	private static PopupWindow userListPopupWindow;
	private View userListPopupView;
	private AntaCallActivity2 mContext;

	// 联系人popupwindow界面
	private LinearLayout contactList_popup_cancel;
	private LinearLayout contactList_popup_added;
	private LinearLayout contactList_popup_added_views;
	private LinearLayout contactList_popup_add2userList;
	private LinearLayout contactList_popup_add2userList_views;
	private static PopupWindow contactListPopupWindow;
	private View contactListPopupView;

	public boolean isEditMode;
	private View currentClickedView;
	private static int currentClickedViewPosition;
	private static boolean isCreated;
	private LinearLayout popupDelectCancel;
	private LinearLayout popupDelectDelect;
	private BroadcastReceiver receiver;
	private View editModePopupView;
	private ScaleAnimation sa1;

	private ScaleAnimation sa2;
	private View userAdd;
	public static int mIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		mRootView = (ViewGroup) getWindow().getLayoutInflater().inflate(
				R.layout.antacall2, null);
		mRootView.setOnClickListener(this);
		setContentView(mRootView);
		// initListViews(mRootView);
		contactList = (ListView) mRootView.findViewById(R.id.contact_list);
		isClicked = false;
		initNewUI();
	}

	ImageButton mCompleteButton;
	TextView mCancelSelectTV;
	TextView mCompleteTv;
	LinearLayout mettingline, cancelline;
	public static boolean isClicked = false;// 标记是否是点击了进入会议按钮进入的会议
	private void initNewUI() {
		// TODO Auto-generated method stub
		// mCompleteButton = (ImageButton)findViewById(R.id.complete_bt);
		// mCompleteButton.setOnClickListener(this);

		mCancelSelectTV = (TextView) findViewById(R.id.cancel_select_tv);
		mCancelSelectTV.setOnClickListener(this);
		cancelline = (LinearLayout) findViewById(R.id.cancel_line);
		cancelline.setOnClickListener(this);
		cancelline.setOnTouchListener(new OnTouchListener() {
			TextView tv = (TextView) findViewById(R.id.cancel_tv);

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// mettingline.setBackgroundResource(R.color.red);
					// mCompleteTv.setTextColor(Color.RED);
					tv.setBackgroundResource(R.color.btn_click_bg);
					tv.setTextColor(Color.WHITE);
					break;
				case MotionEvent.ACTION_UP:
					tv.setBackgroundResource(R.color.whole_bg);
					tv.setTextColor(getResources()
							.getColor(R.color.font_color3));
				}
				return false;
			}
		});

		mettingline = (LinearLayout) findViewById(R.id.mettingline);
		// mCompleteTv.setOnClickListener(this);
		mettingline.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mCompleteTv = (TextView) findViewById(R.id.complete_tv);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// mettingline.setBackgroundResource(R.color.red);
					// mCompleteTv.setTextColor(Color.RED);
					mCompleteTv.setBackgroundResource(R.color.btn_click_bg);
					mCompleteTv.setTextColor(Color.WHITE);
					isClicked = true;
					break;
				case MotionEvent.ACTION_UP:
					mCompleteTv.setBackgroundResource(R.color.whole_bg);
					mCompleteTv.setTextColor(getResources().getColor(
							R.color.font_color3));
					// mettingline.setBackgroundResource(R.color.font_color3);
					// mCompleteTv.setTextColor(getResources().getColor(
					// R.color.font_color3));

					String numberString = getNumbers();
					if (!numberString.equals("")) {
						showMakeMeetingDialog(
								getApplicationContext(),
								getResources().getString(
										R.string.begin_conference),
								getResources().getString(
										R.string.start_conference_notify_1)
										+ " "+mGridData.size()+" "
												+ (mGridData.size()<=1?getResources().getString(
														R.string.start_conference_notify_3):getResources().getString(
																R.string.start_conference_notify_2)),
								numberString);
					} else {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.no_selected),
								0).show();
					}
					break;
				}
				return true;
			}
		});
		mContext = this;
		selected = new ArrayList<String>();
		linkData = ContactUtil.getUsers();
		contactData = new ArrayList<Linkman>();
		for (int i = 0; i < linkData.size(); i++) {
			Linkman link_ = new Linkman();
			link_.name = (String) linkData.get(i).get("title");
			link_.number = (String) linkData.get(i).get("info");
			contactData.add(link_);
		}
		// getGroupData();
		// 发送延迟（100ms）消息，在handler中执行加载任务 modify by liangzhang 2014-07-24
		MyHandler handler = new MyHandler();
		Message message = Message.obtain();
		message.what = 2;
		handler.sendMessageDelayed(message, 100);
		if (contactData.size() > 0) {
			childData.add(contactData);
			groupData.add(getResources().getString(R.string.contact));
		}

		Linkman example = new Linkman();
		// example.name = "名字";
		// example.number="号码";
		mGridData = new ArrayList<Linkman>();
		updateBtn();
		// mGridData.add(example);
		mGridView = (GridView) findViewById(R.id.grid_selected_member);
		MyHandler mh = new MyHandler();
		Message msg = Message.obtain();
		msg.what = 1;
		mh.sendMessage(msg);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			private Linkman linkman;

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				MyHandler mh = new MyHandler();
				Message msg = Message.obtain();
				msg.what = 1;
				mh.sendMessage(msg);
				String number = mGridData.get(position).number;
				for (int i = 0; i < mLinkmans.size(); i++) {
					linkman = mLinkmans.get(i);
					if (linkman.number.equals(number)) {
						linkman.isSelected = false;
					}
				}

				selected.remove(mGridData.get(position).number);
				mGridData.remove(position);
				updateBtn();
				mAdapter.notifyDataSetChanged();
			}
		});
		confirm_select = (TextView) findViewById(R.id.confirm_select);
		confirm_select2 = (TextView) findViewById(R.id.confirm_select2);
		confirm_select2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				// if(xxTag){
				// xxTag = false;
				// MyToast.showToast(true, mContext, "算法处理已经关闭");
				// }else{
				// xxTag = true;
				// MyToast.showToast(true, mContext, "算法处理已经打开");
				// }
			}
		});
		confirm_select.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mGridData.size() < 1) {
					MyToast.showToast(true, mContext,
							getResources().getString(R.string.wrong_notify));
					return;
				}
				String memberNumber = null;
				String memberName = null;
				StringBuffer sb = new StringBuffer("");
				StringBuffer dsb = new StringBuffer("");
				int a = mGridData.size();
				int b = 0;
				for (Linkman man : mGridData) {
					b++;
					sb.append(man.number);
					dsb.append(man.name);
					if (b < a) {
						sb.append(";");
						dsb.append(";");
					}
				}
				memberNumber = sb.toString();
				memberName = dsb.toString();
				Intent intent = new Intent(mContext,
						MessageComposeActivity.class);
				intent.putExtra("name", memberName);
				intent.putExtra("number", memberNumber);
				if (memberName.contains(";")) {
					intent.putExtra("type", "mass");
				}
				startActivity(intent);
				finish();

			}

		});
	}

	private void updateBtn() {
		TextView tv = (TextView) findViewById(R.id.cancel_tv);
		mCompleteTv = (TextView) findViewById(R.id.complete_tv);
		if (mGridData.size() < 1) {
			cancelline.setEnabled(false);
			mettingline.setEnabled(false);
			tv.setTextColor(getResources().getColor(R.color.font_color2));
			mCompleteTv.setTextColor(getResources().getColor(
					R.color.font_color2));
		} else {
			cancelline.setEnabled(true);
			mettingline.setEnabled(true);
			tv.setTextColor(getResources().getColor(R.color.font_color3));
			mCompleteTv.setTextColor(getResources().getColor(
					R.color.font_color3));
		}
	}

	private ContactListAdapter mAdapter;
	public Map<String, Object> userListClickedItem;
	public Map<String, Object> contactListClickedItem;
	private List<Map<String, Object>> mContacts;
	private List<Map<String, Object>> mUsers;
	private int mDataIndex;
	private int mContactListIndex = -1;
	private int mUserListIndex = -1;
	private View contactListViews;
	private ScaleAnimation contactListShowSA;
	private ScaleAnimation contactListHideSA;
	private View userListClickView;
	private View contactListClickView;
	public boolean mExist;
	private boolean needAddContactMenu = true;
	private ImageButton back_btn;
	private ImageButton contact_btn;

	private ArrayList<Linkman> getLinkmans(List<Map<String, Object>> contacts) {
		// TODO Auto-generated method stub
		mLinkmans.clear();
		for (int j = 0; j < contacts.size(); j++) {
			// GroupListInfo groupListInfo = arrayList.get(j);
			Map<String, Object> map = contacts.get(j);
			try {
				// if (groupListInfo != null) {
				// Linkman lin = new Linkman();
				// lin.name = groupListInfo.GrpName;
				// lin.number = groupListInfo.GrpNum;
				// list.add(lin);
				// }
				if (map != null) {
					Linkman lin = new Linkman();
					lin.name = (String) map.get("title");
					lin.number = (String) map.get("info");

					for (int i = 0; i < mGridData.size(); i++) {
						Linkman linkman = mGridData.get(i);
						if (linkman.number.equals(lin.number)) {
							lin.isSelected = true;
						}

					}
					mLinkmans.add(lin);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				MyLog.e(TAG, "getGroupData fail");
				e.printStackTrace();
			}
		}
		return mLinkmans;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.complete_tv:
			// Toast.makeText(getApplicationContext(),"进入会议",0).show();
			// AntaCallUtil.makeAntaCall(false);
			String numberString = getNumbers();
			if (!numberString.equals("")) {
				showMakeMeetingDialog(
						getApplicationContext(),
						getResources().getString(R.string.start_conference),
						getResources().getString(
								R.string.start_conference_notify_1)
								+ " "+mGridData.size()+" "
										+ (mGridData.size()<=1?getResources().getString(
												R.string.start_conference_notify_3):getResources().getString(
														R.string.start_conference_notify_2)),
						numberString);
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.no_selected), 0)
						.show();
			}
			break;
		case R.id.select_iv:
			SelectTag selectTag = (SelectTag) v.getTag();
			int position = selectTag.getPosition();
			boolean isSelected = selectTag.isSelected();

			// String s =
			// childData.get(groupPosition).get(childPosition).number;
			// Linkman linkman =
			// childData.get(groupPosition).get(childPosition);

			Map<String, Object> map = mContacts.get(position);
			Linkman linkman = mLinkmans.get(position);

			// CheckBox cb = (CheckBox) v.findViewById(R.id.custom_checkbox);
			isSelected = linkman.isSelected;
			if (isSelected) {
				selectTag.setSelected(!isSelected);
				linkman.isSelected = !isSelected;
				// cb.setChecked(false);

				/*
				 * for(int i = 0;i<mGridData.size();i++){
				 * if(mGridData.get(i).number.equals(s)){ mGridData.remove(i); }
				 * }
				 */
				v.setBackgroundResource(R.drawable.select_off);
				if (mGridData.contains(linkman)) {
					mGridData.remove(linkman);
					updateBtn();
				}
				MyHandler mh = new MyHandler();
				Message msg = Message.obtain();
				msg.what = 1;
				mh.sendMessage(msg);
			} else {
				selectTag.setSelected(!isSelected);
				linkman.isSelected = !isSelected;
				// cb.setChecked(true);
				v.setBackgroundResource(R.drawable.select_on);
				// mGridData.add(childData.get(groupPosition).get(childPosition));
				mGridData.add(linkman);
				updateBtn();
				MyHandler mh = new MyHandler();
				Message msg = Message.obtain();
				msg.what = 1;
				mh.sendMessage(msg);
			}
			// mExAdapter.notifyDataSetChanged();
			break;
		case R.id.cancel_line:
			if (mGridData != null) {
				mGridData.clear();
				updateBtn();
			}
			MyHandler mh = new MyHandler();
			Message msg = Message.obtain();
			msg.what = 1;
			mh.sendMessage(msg);
			clearListSelect();
			break;
		default:
			break;
		}

	}

	void clearListSelect() {
		if (mLinkmans == null || mLinkmans.size() < 1)
			return;
		for (Linkman lin : mLinkmans) {
			if (lin.isSelected) {
				lin.isSelected = false;
			}
		}
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	//
	// private void initListViews(ViewGroup mRootView) {
	// // TODO Auto-generated method stub
	// contactList = (ListView) mRootView.findViewById(R.id.contact_list);
	// //ready del...
	// // mAdapter = new ContactListAdapter(mContacts);
	// // mContacts = AntaCallUtil.getContacts();
	// // mLinkmans = getLinkmans(mContacts);
	// //
	// // mAdapter.setData(mContacts);
	// // contactList.setAdapter(mAdapter);
	// }

	@Override
	protected void onResume() {
		Zed3Log.debug("testcrash", "AntaCallActivity#onResume() enter");
//		CallActivity2.resetCallParams();
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		// 延时100ms发送获取联系人信息的消息 modify by liangzhang 2014-07-24
		MyHandler handler = new MyHandler();
		Message message = Message.obtain();
		message.what = 3;
		handler.sendMessageDelayed(message, 100);

		MyHandler mh = new MyHandler();
		Message msg = Message.obtain();
		msg.what = 1;
		mh.sendMessage(msg);
		super.onResume();
		Zed3Log.debug("testcrash", "AntaCallActivity#onResume() exit");
	}

	private String getNumbers() {
		// TODO Auto-generated method stub
		String numbers = "";
		for (int i = 0; i < mGridData.size(); i++) {
			numbers += " " + mGridData.get(i).number;
			if (i > 32) {
				break;
			}
			// mNumber = (String)userListData.get(i).get(NUMBER);
			// numbers+=" "+mNumber;
		}
		return numbers;
	}

	class ContactListAdapter extends BaseAdapter {

		private List<Map<String, Object>> mData;
		private LayoutInflater mInflater;
		private String tag = "ContactListAdapter";

		public ContactListAdapter(List<Map<String, Object>> mData) {
			// TODO Auto-generated constructor stub
			this.mData = mData;
			mInflater = LayoutInflater.from(mContext);
		}

		public void setData(List<Map<String, Object>> mData2) {
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
						R.layout.anta_contact_list_item_2, null);
				convertView.setSelected(true);
				convertView.setEnabled(true);
				holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				holder.select = (ImageView) convertView
						.findViewById(R.id.select_iv);
				holder.select.setOnClickListener(AntaCallActivity2.this);
				// SelectTag selectTag = new SelectTag();
				// selectTag.setSelected(false);
				// selectTag.setPosition(position);
				//
				// holder.select.setTag(selectTag);
				// convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// holder.img.setBackgroundResource((Integer)
			// mData.get(position).get(
			// "img"));
			Map<String, Object> map = mData.get(position);
			Log.i(tag, "position = " + position);
			String titleString = (String) map.get("title");
			holder.title.setText(titleString);
			holder.info.setText((String) mData.get(position).get("info"));
			boolean isSelected = mLinkmans.get(position).isSelected;

			SelectTag selectTag = new SelectTag();
			selectTag.setSelected(isSelected);
			selectTag.setPosition(position);
			holder.select.setTag(selectTag);
			convertView.setTag(holder);

			holder.select
					.setBackgroundResource(isSelected ? R.drawable.select_on
							: R.drawable.select_off);

			// if (position == selectItem) {
			// convertView.setBackgroundColor(Color.LTGRAY);
			// } else {
			// convertView.setBackgroundColor(Color.TRANSPARENT);
			// }

			return convertView;
		}

	}

	public final class ViewHolder {
		public View add;
		public View remove;
		public ImageView img;
		public ImageView select;
		public TextView title;
		public TextView info;

	}

	class ContactListOnItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mContactListIndex = position;
			contactListClickView = view;
			contactListClickedItem = mContacts.get(position);
			mExist = AntaCallUtil.checkExist(contactListClickedItem);
			mDataIndex = position;
			// showContactListPopupWindow(view, position);
			// showContactListPopupWindow(view, position);
			// showCallListPopupWindow(view, position);
		}

	}

	class CallListOnItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mUserListIndex = position;
			userListClickView = view;
			userListClickedItem = mUsers.get(position);
			mDataIndex = position;
			// showCallListPopupWindow(view, position);
		}

	}

	public static String getVersion(Context context) {
		final String unknown = "Unknown";

		if (context == null) {
			return unknown;
		}

		try {
			String ret = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
			if (ret.contains(" + "))
				ret = ret.substring(0, ret.indexOf(" + ")) + "b";
			return ret;
		} catch (NameNotFoundException ex) {
		}

		return unknown;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		// menu.add(0, 1, 0, "退出").setIcon(R.drawable.exit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case 1:
			Tools.exitApp(AntaCallActivity2.this);
			break;
		}
		return result;
	}

	private List<Map<String, Object>> linkData;
	private List<Linkman> contactData;
	private List<String> groupData;
	// private MyAdapter mAdapter;
	// public Map<String, Object> userListClickedItem;
	// private Context mContext;
	private GridView mGridView;
	private ArrayList<Linkman> mContactData;
	public static ArrayList<Linkman> mGridData;
	public static ArrayList<Linkman> mLinkmans = new ArrayList<Linkman>();
	private ArrayList<String> selected;
	private MyGridViewAdapter mAdapter_;
	private TextView confirm_select;
	private TextView confirm_select2;
	private String groupName;
	private List<List<Linkman>> childData;

	private class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if ((mGridData == null) || (mGridData.size() == 0)) {
					mAdapter_ = new MyGridViewAdapter(mContext);
					mGridView.setAdapter(mAdapter_);
					return;
				} else {
					LayoutParams params = new LinearLayout.LayoutParams(
							((mGridData.size() * 122) + 10),
							LayoutParams.WRAP_CONTENT);
					mGridView.setLayoutParams(params);
					mGridView.setColumnWidth(/* 148 *//* 100 */120);
					mGridView.setHorizontalSpacing(/* 1 */2);
					mGridView.setStretchMode(GridView.NO_STRETCH);
					mGridView.setNumColumns(mGridData.size());

					mAdapter_ = new MyGridViewAdapter(mContext);
					mGridView.setAdapter(mAdapter_);
				}
				break;
			case 2:
				// 延迟获取对讲组信息 modify by liangzhang 2014-07-24
				getGroupData();
				break;
			case 3:
				// 延迟获取联系人信息 modify by liangzhang 2014-07-24
				mContacts = MeetingCompareTool.getInstance().sortByDefault(
						AntaCallUtil.getContacts());
				mLinkmans = getLinkmans(mContacts);
				if (mAdapter == null) {
					mAdapter = new ContactListAdapter(mContacts);
					mAdapter.setData(mContacts);
					contactList.setAdapter(mAdapter);
				} else {
					mAdapter.setData(mContacts);
					mAdapter.notifyDataSetChanged();
				}
				break;
			}
		}
	}

	// public final class ExViewHolder {
	// public ImageView img;
	// public TextView title;
	// public TextView info;
	// public CheckBox cBox;
	// }

	private class MyGridViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyGridViewAdapter(Context context) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return mGridData.size();
		}

		public Object getItem(int position) {
			return mGridData.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Linkman item = mGridData.get(position);
			CellHolder cellHolder;
			if (convertView == null) {
				cellHolder = new CellHolder();
				convertView = mInflater.inflate(R.layout.custom_gridview_item,
						null);
				cellHolder.name = (TextView) convertView
						.findViewById(R.id.custom_name);
				cellHolder.number = (TextView) convertView
						.findViewById(R.id.custom_number);
				convertView.setTag(cellHolder);
			} else {
				cellHolder = (CellHolder) convertView.getTag();
			}
			cellHolder.name.setText(item.name);
			cellHolder.number.setText(item.number);
			return convertView;
		}
	}

	private class CellHolder {
		TextView name;
		TextView number;
	}

	private void getGroupData() {
		// TODO Auto-generated method stub
		groupData = new ArrayList<String>();
		childData = new ArrayList<List<Linkman>>();
		ArrayList<PttGrp> mGroups = (ArrayList<PttGrp>) GroupListUtil
				.getGroups();
		for (int i = 0; i < mGroups.size(); i++) {
			ArrayList<Linkman> list = new ArrayList<Linkman>();
			PttGrp pttGrp = mGroups.get(i);
			HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap = GroupListUtil
					.getGroupListsMap();
			ArrayList<GroupListInfo> arrayList = mGroupListsMap.get(pttGrp);
			if (arrayList != null && arrayList.size() != 0) {
				for (int j = 0; j < arrayList.size(); j++) {
					GroupListInfo groupListInfo = arrayList.get(j);

					try {
						if (groupListInfo != null) {
							Linkman lin = new Linkman();
							lin.name = groupListInfo.GrpName;
							lin.number = groupListInfo.GrpNum;
							list.add(lin);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						MyLog.e(TAG, "getGroupData fail");
						e.printStackTrace();
					}

				}
			}
			groupData.add(pttGrp.grpName);
			childData.add(list);
		}
	}

	private void showMakeMeetingDialog(Context context, String title,
			String msg, final String numberString) {
		//sim卡电话来电中、去电中或通话中，voip禁止去电，拒接来电；  add by mou 2014-10-08
		if (CallUtil.checkGsmCallInCall()) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
			return;
		}
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(R.layout.shrew_exit_dialog);
		TextView ok = (TextView) window.findViewById(R.id.btn_ok);
		TextView titleTV = (TextView) window
				.findViewById(R.id.contact_user_title);
		TextView msgTV = (TextView) window.findViewById(R.id.msg_tv);
		titleTV.setText(title);
		//add by  wlei 2014-10-20 解决英文提示对话框，英文单词分开
		if(mContext == null){
			mContext = AntaCallActivity2.this;
		}
		int width = (int)(mContext.getResources().getDisplayMetrics().density*296+0.5f);
		String message = DialogMessageTool.getString(width,msgTV.getTextSize(), msg);
		msgTV.setText(message);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.dismiss();
				AntaCallUtil.makeAntaCall(false, numberString);
			}
		});
		TextView cancel = (TextView) window.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});

	}
}
