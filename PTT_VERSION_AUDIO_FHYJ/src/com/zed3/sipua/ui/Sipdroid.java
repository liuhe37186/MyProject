/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2008 Hughes Systique Corporation, USA (http://www.hsc.com)
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.zed3.sipua.ui;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;

/////////////////////////////////////////////////////////////////////
// this the main activity of Sipdroid
// for modifying it additional terms according to section 7, GPL apply
// see ADDITIONAL_TERMS.txt
/////////////////////////////////////////////////////////////////////
public class Sipdroid/* extends Fragment*//* implements OnDismissListener ,OnClickListener*/{

	public static final boolean release = true;
	public static final boolean market = false;
	public static final HashMap<Character, Integer> mToneMap = new HashMap<Character, Integer>();

	// AutoCompleteTextView sip_uri_box2;
	public static Context mContext; 

	//
//	private MultiAutoCompleteTextView numTxt = null;
//	private ImageButton btnone = null;
//	private ImageButton btntwo = null;
//	private ImageButton btnthree = null;
//	private ImageButton btnfour = null;
//	private ImageButton btnfive = null;
//	private ImageButton btnsix = null;
//	private ImageButton btnseven = null;
//	private ImageButton btnenight = null;
//	private ImageButton btnnine = null;
//	private ImageButton btn0 = null;
//	private ImageButton btnmi = null;
//	private ImageButton btnjing = null;
//
//	//
//	private ImageButton btnsearch = null;
//	private ImageButton btndialy = null;
//	private ImageButton btndel = null;
//
//	private String CurGrpID = "";
//	
//	// menu
//	private View menuPopupView;
//	private ScaleAnimation sa;
//	private LinearLayout menuPopupExit;
//	private LinearLayout menuPopupSetting;
//	private LinearLayout menuPopupCallHistory;
//
//	// menu
//	private View callHistoryPopupView;
//	private PopupWindow callHistoryPopupWindow;
//	private ScaleAnimation sa1;
//	private LinearLayout menuPopupExit1;
//	private LinearLayout menuPopupSetting1;
//	private LinearLayout menuPopupCallHistory1;
//	
//	//longclick
//	private PopupWindow popupWindow4Delete;
//	private LinearLayout popupDelectCancel;
//	private LinearLayout popupDelectDelect;
//	private View editModePopupView;
//	private View currentClickedView;
//	public static final String COLOR_LIGHT = "#FFBDBDBD";
//	private ScaleAnimation sa2;
//
//	
//	//keyboard
//	private ScaleAnimation keyBoardHideSA;
//	private ScaleAnimation keyBoardShowSA;
//	
//	//通话记录 popupwindow view
//	private LinearLayout popup_delete;
//	private LinearLayout popup_save;
////	private LinearLayout popup_message;
//	private LinearLayout popup_video;
////	private LinearLayout popup_call;
//	private PopupWindow popupWindow;
//	private View popupView; 
//
//
//	// @Override
//	// public void onStart() {
//	// super.onStart();
//	// Receiver.engine(this).registerMore();
//	// ContentResolver content = getContentResolver();
//	// Cursor cursor = content.query(Calls.CONTENT_URI, PROJECTION,
//	// Calls.NUMBER + " like ?", new String[] { "%@%" },
//	// Calls.DEFAULT_SORT_ORDER);
//	// CallsAdapter adapter = new CallsAdapter(this, cursor);
//	// sip_uri_box2.setAdapter(adapter);
//	// }
//
//	// public static class CallsCursor extends CursorWrapper {
//	// List<String> list;
//	//
//	// public int getCount() {
//	// return list.size();
//	// }
//	//
//	// public String getString(int i) {
//	// return list.get(getPosition());
//	// }
//	//
//	// public CallsCursor(Cursor cursor) {
//	// super(cursor);
//	// list = new ArrayList<String>();
//	// for (int i = 0; i < cursor.getCount(); i++) {
//	// moveToPosition(i);
//	// String phoneNumber = super.getString(1);
//	// String cachedName = super.getString(2);
//	// if (cachedName != null && cachedName.trim().length() > 0)
//	// phoneNumber += " <" + cachedName + ">";
//	// if (list.contains(phoneNumber))
//	// continue;
//	// list.add(phoneNumber);
//	// }
//	// moveToFirst();
//	// }
//	//
//	// }
//
//	// public static class CallsAdapter extends CursorAdapter implements
//	// Filterable {
//	// public CallsAdapter(Context context, Cursor c) {
//	// super(context, c);
//	// mContent = context.getContentResolver();
//	// }
//	//
//	// public View newView(Context context, Cursor cursor, ViewGroup parent) {
//	// final LayoutInflater inflater = LayoutInflater.from(context);
//	// final TextView view = (TextView) inflater.inflate(
//	// android.R.layout.simple_dropdown_item_1line, parent, false);
//	// String phoneNumber = cursor.getString(1);
//	// view.setText(phoneNumber);
//	// return view;
//	// }
//	//
//	// @Override
//	// public void bindView(View view, Context context, Cursor cursor) {
//	// String phoneNumber = cursor.getString(1);
//	// ((TextView) view).setText(phoneNumber);
//	// }
//	//
//	// @Override
//	// public String convertToString(Cursor cursor) {
//	// String phoneNumber = cursor.getString(1);
//	// if (phoneNumber.contains(" <"))
//	// phoneNumber = phoneNumber.substring(0,
//	// phoneNumber.indexOf(" <"));
//	// return phoneNumber;
//	// }
//	//
//	// @Override
//	// public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
//	// if (getFilterQueryProvider() != null) {
//	// return new CallsCursor(getFilterQueryProvider().runQuery(
//	// constraint));
//	// }
//	//
//	// StringBuilder buffer;
//	// String[] args;
//	// buffer = new StringBuilder();
//	// buffer.append(Calls.NUMBER);
//	// buffer.append(" LIKE ? OR ");
//	// buffer.append(Calls.CACHED_NAME);
//	// buffer.append(" LIKE ?");
//	// String arg = "%"
//	// + (constraint != null && constraint.length() > 0 ? constraint
//	// .toString() : "@") + "%";
//	// args = new String[] { arg, arg };
//	//
//	// return new CallsCursor(mContent.query(Calls.CONTENT_URI,
//	// PROJECTION, buffer.toString(), args, Calls.NUMBER + " asc"));
//	// }
//	//
//	// private ContentResolver mContent;
//	// }
//	//
//	// private static final String[] PROJECTION = new String[] { Calls._ID,
//	// Calls.NUMBER, Calls.CACHED_NAME };
//	private ToneGenerator mToneGenerator;
//	private Object mToneGeneratorLock = new Object();// 监视器对象锁
//	private boolean mDTMFToneEnabled; // 按键操作音
//	private static final int TONE_LENGTH_MS = 150;// 延迟时间
//	public static final String NULLSTR = "--";
//
//	void playTone(Character tone) {
//		// TODO 播放按键声音
//		if (!mDTMFToneEnabled) {
//			return;
//		}
//
//		AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
//		int ringerMode = audioManager.getRingerMode();
//		if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
//				|| (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {// 静音或震动时不发出按键声音
//			return;
//		}
//
//		synchronized (mToneGeneratorLock) {
//			if (mToneGenerator == null) {
//				Log.w("tagdd", "playTone: mToneGenerator == null, tone: "
//						+ tone);
//				return;
//			}
//			mToneGenerator.startTone(mToneMap.get(tone), TONE_LENGTH_MS);// 发声TONE_LENGTH_MS
//		}
//	}
////
////	@Override
////	public boolean onKeyUp(int keyCode, KeyEvent event) {
////		// TODO Auto-generated method stub
////		if(keyCode == KeyEvent.KEYCODE_CALL){
////			call_menu(numTxt);
////			return true;  
////		}else{
////			return super.onKeyUp(keyCode, event);
////		}
////	}
//
//	private static PopupWindow mMenuPopuWindow;
//	private static View mMenuView;
//	private static GridView mMenuGrid;
//
//	private static Activity mActivity;
//	private View mRootView;
//	private ImageButton saveNumber;
//	private ImageButton videoCall;
//	private ListView callHistoryListView;
//	private View keyboardView;
//	private View callHistoryView;
//	private ImageButton btnHideKeyboard;
//	private View callHistoryOverView;
//	private View deleteFinishView;
//	private View editMenuView;
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		
//		/*
//		 * if (mRootView != null) { return mRootView; }
//		 * 会导致其他view 空指针
//		 */
//		
//		mContext = SipUAApp.mContext;
//		mActivity = getActivity();
//		mRootView = inflater.inflate(R.layout.sipdroid, container, false);
//		
//		mRootView.setOnClickListener(this);//dismiss menu popupwindow
//		
//		ShowCurrentGrp();
//		//
//		InitCallScreen();
//		numTxt = (MultiAutoCompleteTextView) mRootView.findViewById(R.id.p_digits);
//		numTxt.setText("");
//		numTxt.setSingleLine(true);
//		btnone = (ImageButton) mRootView.findViewById(R.id.pone);
//		btnone.setOnClickListener(this);
//		//
//		btntwo = (ImageButton) mRootView.findViewById(R.id.ptwo);
//		btntwo.setOnClickListener(this);
//		//
//		btnthree = (ImageButton) mRootView.findViewById(R.id.pthree);
//		btnthree.setOnClickListener(this);
//		//
//		btnfour = (ImageButton) mRootView.findViewById(R.id.pfour);
//		btnfour.setOnClickListener(this);
//		//
//		btnfive = (ImageButton) mRootView.findViewById(R.id.pfive);
//		btnfive.setOnClickListener(this);
//		//
//		btnsix = (ImageButton) mRootView.findViewById(R.id.psix);
//		btnsix.setOnClickListener(this);
//		//
//		btnseven = (ImageButton) mRootView.findViewById(R.id.pseven);
//		btnseven.setOnClickListener(this);
//		//
//		btnenight = (ImageButton) mRootView.findViewById(R.id.penight);
//		btnenight.setOnClickListener(this);
//		//
//		btnnine = (ImageButton) mRootView.findViewById(R.id.pnine);
//		btnnine.setOnClickListener(this);
//		//
//		btn0 = (ImageButton) mRootView.findViewById(R.id.p0);
//		btn0.setOnClickListener(this);
//		//
//		btnmi = (ImageButton) mRootView.findViewById(R.id.pmi);
//		btnmi.setOnClickListener(this);
//		//
//		btnjing = (ImageButton) mRootView.findViewById(R.id.pjing);
//		btnjing.setOnClickListener(this);
//		// 搜索
//		btnsearch = (ImageButton) mRootView.findViewById(R.id.psearch);
//		btnsearch.setOnClickListener(this);
//		// 拨打
//		btndialy = (ImageButton) mRootView.findViewById(R.id.pphone);
//		btndialy.setOnClickListener(this);
//		// 拨打
//		videoCall = (ImageButton) mRootView.findViewById(R.id.video_call);
//		btndialy.setOnClickListener(this);
//		videoCall.setVisibility(View.GONE);
//		// 保存
//		saveNumber = (ImageButton) mRootView.findViewById(R.id.num_save);
//		saveNumber.setOnClickListener(this);
////		saveNumber.setVisibility(View.GONE);
//		// 删除
//		btndel = (ImageButton) mRootView.findViewById(R.id.pdel);
//		btndel.setOnClickListener(this);
//		//键盘
//		// 显示键盘
//		btndel = (ImageButton) mRootView.findViewById(R.id.call_show_keyboard);
//		btndel.setOnClickListener(this);
//		// 隐藏键盘
//		btnHideKeyboard = (ImageButton) mRootView.findViewById(R.id.hide_keyboard);
//		btnHideKeyboard.setOnClickListener(this);
//		//显示键盘后的控制区
//		controlViews = mRootView.findViewById(R.id.call_controlviews);
//		
//		//隐藏键盘后的控制区
//		controlViewsNoKeyboard = mRootView.findViewById(R.id.call_controlviews_nokeyboard);
//		controlViewsNoKeyboard.setVisibility(View.INVISIBLE);
//		keyboardView = mRootView.findViewById(R.id.call_keyboard);
//		keyboardView.setOnClickListener(this);
//		keyboardView.setVisibility(View.VISIBLE);
//		callHistoryView = mRootView.findViewById(R.id.call_history);
//		callHistoryView.setVisibility(View.VISIBLE);
//		callHistoryOverView = mRootView.findViewById(R.id.call_history_coverview);
//		callHistoryOverView.setVisibility(View.VISIBLE);
//		callHistoryOverView.setOnClickListener(this);
//		
//		//通话记录删除控制
//		editMenuView = mRootView.findViewById(R.id.edit_menu);
//		editMenuView.setOnClickListener(this);
//		editMenuView.setVisibility(View.INVISIBLE);
//		deleteFinishView = mRootView.findViewById(R.id.edit_ok);
//		deleteFinishView.setOnClickListener(this);
//		
//		SipUAApp.on(mActivity, true);
//		initCallHistoryViews();
//		initContacts();
//		return mRootView;
//	}
//	
//	// 联系人
//	private static List<Map<String, Object>> mContact = new ArrayList<Map<String, Object>>();
//	private int SelectItemIndex = -1;
//	private Cursor mCursor;
//	private MyAdapter mAdapter = null;
//	private View controlViews;
//	private View controlViewsNoKeyboard;
//	
//	private void initContacts(){
//		mContact.clear();
//		PrtCallContacts.ReadContact(mContact);
//	}
//	
//	private PopupWindow menuPopupWindow = null;
//	
//	private LinearLayout popupSave2Contact;
//	private LinearLayout popupSendMessge;
//	private LinearLayout popupVideoCall;
//	private LinearLayout popupAudioCall;
////	public GroupListInfo clickedItem;
//	public String clickedItemNumber;
//	private boolean isKeyBoardHided;
//	private Intent modifyIntent;
//	//bug 
//	protected int scrollCount = 0;
//	private boolean isUserHideKeyboard;
//	private boolean isUserShowKeyboard;
//	protected boolean isResumed;
//	protected int mPosition;
//	private CallHistoryDatabase  db;
//	private boolean isEditMode;
//	protected String numberViewText;
//	
//	private void initCallHistoryViews() {
//		// TODO Auto-generated method stub
//		
//		callHistoryListView = (ListView)mRootView.findViewById(R.id.call_history_list);
//		callHistoryListView.setOnScrollListener(new OnScrollListener() {
//			
//
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				// TODO Auto-generated method stub
//			}
//			
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				// TODO Auto-generated method stub
//				dismissPopupWindow();
////				if (isResumed&&!isKeyBoardHided) {
////					if (scrollCount < 2 ) {
////						scrollCount++;
////						return;
////					}
////					keyboardView.setVisibility(View.INVISIBLE);
////					controlViewsNoKeyboard.setVisibility(View.VISIBLE);
////					callHistoryOverView.setVisibility(View.INVISIBLE);
////					isKeyBoardHided = true;
////				}
//				
//			}
//		});
//		callHistoryListView.setOnItemClickListener(new OnItemClickListener() {
//
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View v,
//					int position, long id) {
//				// TODO Auto-generated method stub
//				dismissPopupWindow();
//				hideKeyboard(true);
////				SelectItemIndex = (Integer) mAdapter.getItem(position);
//				Cursor cursor = (Cursor) mAdapter.getItem(position);
//				clickedItemNumber = cursor.getString(6);
//				TextView numberView  = (TextView) v.findViewById(R.id.call_history_number);
//				numberViewText = numberView.getText().toString();
//				
//				if (isEditMode) {
//					mPosition = position;
//					currentClickedView = v;
//					showPopupWindow4Delete(v,mPosition);
//				}else {
//					showCallHistoryPopuWindow(v);
//				}
//			}
//
//			
//		});
//		
////		callHistoryListView.setOnItemLongClickListener(new OnItemLongClickListener() {
////
////			@Override
////			public boolean onItemLongClick(AdapterView<?> parent, View view,
////					int position, long id) {
////				// TODO Auto-generated method stub
////				dismissPopupWindow();
////				mPosition = position;
////				currentClickedView = view;
//////				showPopupWindow4Delete(view,mPosition);
////				if (!chanageEditMode()) {
////					editMenuView.setVisibility(View.GONE);
////				}else {
////					editMenuView.setVisibility(View.VISIBLE);
////				}
////				return true;
////			}
////		});
//		
//	}
//	
//	public void showPopupWindow4Delete(View view, int position) {
//		// TODO Auto-generated method stub
//		// 判断 当前界面里面是否存在 popupwindow
//		
////		clickedItem = mData.get(position);
////		mDataIndex = position;
//
//		int l = view.getLeft();
//		int r = view.getRight();
//		int t = view.getTop();
//		int b = view.getBottom();
//		if (currentClickedView != null) {
//			currentClickedView
//					.setBackgroundColor(Color.parseColor(COLOR_LIGHT));
//		}
//		view.setBackgroundColor(Color.BLACK);
////		currentClickedViewPosition = position;
//
//		if (editModePopupView == null) {
//
//			editModePopupView = View.inflate(mActivity,
//					R.layout.contact_user_popup_item_edit, null);
//
//			popupDelectDelect = (LinearLayout) editModePopupView
//					.findViewById(R.id.contact_user_popup_edit_delete);
//			popupDelectDelect.setOnClickListener(this);
//
//			popupDelectCancel = (LinearLayout) editModePopupView
//					.findViewById(R.id.contact_user_popup_edit_cancel);
//			popupDelectCancel.setOnClickListener(this);
//
//			/*
//			 * 在代码中 new出来view对象 或者 设置popwindows的时候 里面接受的参数都是 px单位
//			 */
//			popupWindow4Delete = new PopupWindow(editModePopupView, r - l
//					- DensityUtil.dip2px(mActivity, 60), b - t
//					- DensityUtil.dip2px(mActivity, 5));
//			// 给popupwindow设置一个透明的背景颜色,如果不设置 会导致动画效果没法显示
//			popupWindow4Delete.setBackgroundDrawable(new ColorDrawable(
//					Color.TRANSPARENT));
//			sa2 = new ScaleAnimation(0.2f, 1.0f, 0.2f, 1.0f);
//			sa2.setDuration(120);
//		}
//		int[] location = new int[2];
//		view.getLocationInWindow(location);
//		// 指定popupwindow在窗体中显示的位置
//		popupWindow4Delete.showAtLocation(view, Gravity.LEFT | Gravity.TOP,
//				location[0] + 60, location[1]);
//		// 注意popupwindow 默认是没有指定背景颜色的, 悬浮在activity的上面.
//
//		editModePopupView.startAnimation(sa2);
//		currentClickedView = view;
//		
//		
//	}
//	protected void deletHistory(int position) {
//		// TODO Auto-generated method stub
//		mCursor.moveToPosition(position);
//		String begin_str = mCursor.getString(mCursor.getColumnIndex("begin_str"));
////		CallHistoryDatabase db = new CallHistoryDatabase(mActivity);
//		db.delete("call_history", "begin_str = '"+begin_str+"'");
//	}
//	private void showCallHistoryPopuWindow(View view) {
//		// TODO Auto-generated method stub
//		int l = view.getLeft();
//		int r = view.getRight();
//		int t = view.getTop();
//		int b = view.getBottom();
//		if (popupView == null) {
//			
//			popupView = View.inflate(mActivity,
//					R.layout.call_history_click_popu, null);
//			popup_delete = (LinearLayout) popupView
//					.findViewById(R.id.contact_user_popup_delete);
//			popup_delete.setOnClickListener( this);
//			popup_save = (LinearLayout) popupView
//					.findViewById(R.id.contact_user_popup_save);
//			popup_save.setOnClickListener( this);
////			popup_message = (LinearLayout) popupView
////					.findViewById(R.id.contact_user_popup_message);
////			popup_message.setOnClickListener( this);
//			popup_video = (LinearLayout) popupView
//					.findViewById(R.id.contact_user_popup_video);
//			popup_video.setOnClickListener(this);
////			popup_call = (LinearLayout) popupView
////					.findViewById(R.id.contact_user_popup_call);
////			popup_call.setOnClickListener( this);
//			
//			/*
//			 * 在代码中 new出来view对象 或者 设置popwindows的时候 里面接受的参数都是 px单位
//			 */
//			popupWindow = new PopupWindow(popupView, r - l
//					- DensityUtil.dip2px(mActivity, 60), b - t
//					- DensityUtil.dip2px(mActivity, 5));
//			// 给popupwindow设置一个透明的背景颜色,如果不设置 会导致动画效果没法显示
//			popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//			sa2 = new ScaleAnimation(0.2f, 1.0f, 0.2f, 1.0f);
//			sa2.setDuration(300);
//		}
//		if (numberViewText.equals(NULLSTR)) {
//			popup_save.setVisibility(View.VISIBLE);
//		}else {
//			popup_save.setVisibility(View.GONE);
//		}
//		
//		int[] location = new int[2];
//		view.getLocationInWindow(location);
//		// 指定popupwindow在窗体中显示的位置
//		popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.TOP,
//				location[0] + 60, location[1]);
//		// 注意popupwindow 默认是没有指定背景颜色的, 悬浮在activity的上面.
//
//		popupView.startAnimation(sa2);
//	}
//	public void dismissPopupWindow() {
//		dismissMenuPopupWindows();
//		if (popupWindow != null && popupWindow.isShowing()) {
//			popupWindow.dismiss();
//		}
//		if (callHistoryPopupWindow != null && callHistoryPopupWindow.isShowing()) {
//			callHistoryPopupWindow.dismiss();
//		}
//		if (popupWindow4Delete != null && popupWindow4Delete.isShowing()) {
//			popupWindow4Delete.dismiss();
//		}
//		if (currentClickedView != null) {
//			currentClickedView.setBackgroundColor(Color.parseColor(COLOR_LIGHT));
//		}
//	}
//	
//	private Cursor getData() {
//		// TODO Auto-generated method stub
//		if (db == null) {
//			db = new CallHistoryDatabase(mActivity);
//		}
////		String where = "number= " + "";
////		String where_ = "type= " + "";
////		db.delete("call_history", where);
////		db.delete("call_history", where_);
//		mCursor = db.query("call_history", null);
//		return mCursor;
//	}
//	
//
//	public class MyAdapter extends CursorAdapter {
//
//		
//		private LayoutInflater mInflater;
//		private Context mContext;
//
//		public MyAdapter(Context context, Cursor c) {
//			super(context, c);
//			mContext = context;
//			mInflater = LayoutInflater.from(context);
//			// TODO Auto-generated constructor stub
//		}
//
//		public void setSelectItem(int position) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void bindView(View view, Context context, Cursor cursor) {
//
//			// TODO Auto-generated method stub
//			String number = cursor.getString(cursor.getColumnIndex("number"));
//			long begin = cursor.getLong(cursor.getColumnIndex("begin"));
//			String begin_str = cursor.getString(cursor
//					.getColumnIndex("begin_str"));
//			String type = cursor.getString(cursor.getColumnIndex("type"));
////			TextView title = (TextView) view.findViewById(R.id.title);
////			TextView info = (TextView) view.findViewById(R.id.info);
////			TextView time = (TextView) view.findViewById(R.id.time);
////			ImageView img = (ImageView) view.findViewById(R.id.img);
//			TextView title = (TextView) view.findViewById(R.id.call_history_name);
//			TextView info = (TextView) view.findViewById(R.id.call_history_date);
//			TextView time = (TextView) view.findViewById(R.id.call_history_time);
//			ImageView img = (ImageView) view.findViewById(R.id.call_history_type);
//			
//			ImageView photoImageView = (ImageView) view.findViewById(R.id.call_history_photo);
//			TextView numberTextView = (TextView) view.findViewById(R.id.call_history_number);
//			
//			/*
//			 * guojunfeng
//			 * 四种类型CallIn（已接来电）、CallOut（呼出已接通）、CallUnak（未接来电）、CallUnout(呼出未接通)
//			 * 根据四种类型匹配相应记录的图标
//			 */
//			if (type.equals("CallIn")
//					&& cursor.getLong(cursor.getColumnIndex("end")) != 0) {
//				long end = cursor.getLong(cursor.getColumnIndex("end"));
//				time.setText((end - begin) / 1000
//						+ context.getResources().getString(R.string.second));
////				img.setImageResource(R.drawable.icon_callin);
//				img.setImageResource(R.drawable.ic_call_incoming_holo_dark);
//			} else if (type.equals("CallUnak")) {
//				time.setText("");
////				img.setImageResource(R.drawable.icon_callunac);
//				img.setImageResource(R.drawable.ic_call_missed_holo_dark);
//			} else if (type.equals("CallOut")
//					&& cursor.getLong(cursor.getColumnIndex("end")) != 0) {
//				long end = cursor.getLong(cursor.getColumnIndex("end"));
//				time.setText((end - begin) / 1000
//						+ context.getResources().getString(R.string.second));
////				img.setImageResource(R.drawable.icon_callout);
//				img.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
//			} else if (type.equals("CallUnout")) {
////				img.setImageResource(R.drawable.icon_callout);
//				time.setText("");
//				img.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
//			}
////			String contact = getContact(number);
//			String contact = ContactUtil.getUserName(number);
//			
//			title.setText(contact == null?number:contact);
//			numberTextView.setText(contact == null?NULLSTR:number);
//			info.setText(begin_str);
//
//		}
//
//		@Override
//		public View newView(Context context, Cursor cursor, ViewGroup parent) {
//			// TODO Auto-generated method stub
////			return mInflater.inflate(R.layout.prtcall_history, parent, false);
//			return mInflater.inflate(R.layout.call_history_item, parent, false);
//		}
//
//	}
//
//
//	@Override
//	public void onCreate(Bundle icicle) {
//		super.onCreate(icicle);
////		context = this;
////		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置窗口属性->无标题
////
////		// 加载XML 界面
////		setContentView(R.layout.sipdroid);
//		// 获取当前组
////		ShowCurrentGrp();
////		//
////		InitCallScreen();
//		
//
//		
//
//		// 呼叫号码
//		// sip_uri_box2 = (AutoCompleteTextView) findViewById(R.id.txt_callee2);
//		//
//		// sip_uri_box2.setOnKeyListener(new OnKeyListener() {
//		// public boolean onKey(View v, int keyCode, KeyEvent event) {
//		// if (event.getAction() == KeyEvent.ACTION_DOWN
//		// /*
//		// * && (keyCode == KeyEvent.KEYCODE_CALL || keyCode ==
//		// * KeyEvent.KEYCODE_DPAD_CENTER)) {
//		// */
//		// && (keyCode == KeyEvent.KEYCODE_CALL || keyCode ==
//		// KeyEvent.KEYCODE_ENTER)) {
//		// call_menu(sip_uri_box2);
//		// return true;
//		// }
//		// return false;
//		// }
//		// });
//		// sip_uri_box2.setOnItemClickListener(new OnItemClickListener() {
//		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//		// long arg3) {
//		// call_menu(sip_uri_box2);
//		// }
//		// });
////		on(this, true);
//		//
//		// Button contactsButton = (Button) findViewById(R.id.contacts_button);
//		// contactsButton.setFocusable(false);
//		// contactsButton.setOnClickListener(new Button.OnClickListener() {
//		// public void onClick(View v) {
//		// Intent myIntent = new Intent(Intent.ACTION_DIAL);
//		// startActivity(myIntent);
//		// }
//		// });
//
//		// 通讯录
//		// btnSearch = (Button) findViewById(R.id.btnbook);
//		// btnSearch.setOnClickListener(new OnClickListener() {
//		// @Override
//		// public void onClick(View v) {
//		// // TODO Auto-generated method stub
//		// if (CurGrpID != "") {
//		// Intent intent2 = new Intent(Sipdroid.this,
//		// com.zed3.sipua.ui.BookListTab.class);
//		// intent2.putExtra("curgrpid", CurGrpID);
//		// intent2.putExtra("flag", "1");// 标识是否是通讯录
//		// startActivity(intent2);
//		// }
//		// }
//		// });
//
//		// 拨打
//		// btnCall = (Button) findViewById(R.id.btncall);
//		// btnCall.setOnClickListener(new OnClickListener() {
//		// @Override
//		// public void onClick(View v) {
//		// // TODO Auto-generated method stub
//		// call_menu(sip_uri_box2);
//		// }
//		// });
//		// 联系人
//		// btnContact = (Button) findViewById(R.id.btncontact);
//		// btnContact.setOnClickListener(new OnClickListener() {
//		//
//		// @Override
//		// public void onClick(View v) {
//		// // TODO Auto-generated method stub
//		// Intent intent_contact = new Intent(Sipdroid.this,
//		// com.zed3.sipua.ui.PrtCallTab.class);
//		// intent_contact.putExtra("Tab", "Contact");
//		// startActivity(intent_contact);
//		// }
//		// });
//		// 呼叫记录
//		// btnContactHistory = (Button) findViewById(R.id.btnhistory);
//		// btnContactHistory.setOnClickListener(new OnClickListener() {
//		//
//		// @Override
//		// public void onClick(View v) {
//		// // TODO Auto-generated method stub
//		// Intent intent_history = new Intent(Sipdroid.this,
//		// com.zed3.sipua.ui.PrtCallTab.class);
//		// intent_history.putExtra("Tab", "History");
//		// startActivity(intent_history);
//		// }
//		// });
//
//	}
//
//	private void InitCallScreen() {
//		mToneMap.put('1', ToneGenerator.TONE_DTMF_1);
//		mToneMap.put('2', ToneGenerator.TONE_DTMF_2);
//		mToneMap.put('3', ToneGenerator.TONE_DTMF_3);
//		mToneMap.put('4', ToneGenerator.TONE_DTMF_4);
//		mToneMap.put('5', ToneGenerator.TONE_DTMF_5);
//		mToneMap.put('6', ToneGenerator.TONE_DTMF_6);
//		mToneMap.put('7', ToneGenerator.TONE_DTMF_7);
//		mToneMap.put('8', ToneGenerator.TONE_DTMF_8);
//		mToneMap.put('9', ToneGenerator.TONE_DTMF_9);
//		mToneMap.put('0', ToneGenerator.TONE_DTMF_0);
//		mToneMap.put('#', ToneGenerator.TONE_DTMF_P);
//		mToneMap.put('*', ToneGenerator.TONE_DTMF_S);
//		mToneMap.put('d', ToneGenerator.TONE_DTMF_A);
//	}
//
//	private void ShowCurrentGrp() {
//		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
//		if (pttGrp != null) {
//			CurGrpID = pttGrp.grpID;
//		}
//	}
//
//	
//	@Override
//	public void onStart() {
//		// TODO Auto-generated method stub
//		scrollCount = 0;
//		addDatas();
//		if (mAdapter == null) {
//			mAdapter = new MyAdapter(mActivity, mCursor);
//			callHistoryListView.setAdapter(mAdapter);
//		}else {
//			mAdapter.notifyDataSetChanged();
//		}
//		NotificationManager mNotificationMgr = (NotificationManager) mActivity
//				.getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationMgr.cancel(Receiver.MISSED_CALL_NOTIFICATION);
//		mNotificationMgr.cancel(Receiver.AUTO_ANSWER_NOTIFICATION);
//		super.onStart();
//	}
//	// 当对话框获得焦点时调用
//	@Override
//	public void onResume() {
//		Receiver.engine(mActivity);
//
//		ShowCurrentGrp();
//
//		if (MemoryMg.getInstance().CallNum != "") {
//			// 赋值
//			numTxt.setText(MemoryMg.getInstance().CallNum);
//			MemoryMg.getInstance().CallNum = "";
//		}
//
//		mDTMFToneEnabled = android.provider.Settings.System.getInt(
//				mActivity.getContentResolver(),
//				android.provider.Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;// 获取系统参数“按键操作音”是否开启
//
//		synchronized (mToneGeneratorLock) {
//			if (mToneGenerator == null) {
//				try {
//					mToneGenerator = new ToneGenerator(
//							AudioManager.STREAM_MUSIC, 80);
//					mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//				} catch (RuntimeException e) {
//					Log.w("tag",
//							"Exception caught while creating local tone generator: "
//									+ e);
//					mToneGenerator = null;
//				}
//			}
//		}
//		
//		
//		//隐藏 对讲界面的panel
////		MainSipDroid.closePanels();
//		
//		
//		
////		Receiver.engine(this);
//		
//		// 刷新List显示
//		//mData.clear();
//		
////		SetOutListData();
////		SetInListData();
////		SetUnAcListData();
//		//Added by zzhan 2011-11-07
//		
//		
//		refesh();
//		
//		super.onResume();
//
//	}
//	
//	private void refesh() {
//		// TODO Auto-generated method stub
//		if (db == null) {
////			String where = "number= " + "";
////			String where_ = "type= " + "";
////			db.delete("call_history", where);
////			db.delete("call_history", where_);
//		}
//		db = new CallHistoryDatabase(mActivity);
//		mCursor = db.query("call_history", null);
//		mAdapter = new MyAdapter(mActivity, mCursor);
//		callHistoryListView.setAdapter(mAdapter);
//		
//		mAdapter.notifyDataSetChanged();
//		
//	}
//	@Override
//	public void onPause() {
//		// TODO Auto-generated method stub
//		super.onPause();
//	}
//	
//	private void addDatas(){
//		CallHistoryDatabase db = new CallHistoryDatabase(mActivity);
////		String where = "number= " + "";
////		String where_ = "type= " + "";
////		db.delete("call_history", where);
////		db.delete("call_history", where_);
//		mCursor = db.query("call_history", null);
//	}
//
////	@Override
////	public boolean onCreateOptionsMenu(Menu menu) {
////		// menu.add(Menu.NONE, Menu.FIRST + 1, 0, "呼叫").setIcon(
////		// R.drawable.contact_call);
////		// houyuchun modify 20120620 begin 
////		menu.add(Menu.NONE, Menu.FIRST + 4, 1, this.getResources().getString(R.string.contactor)).setIcon(
////				R.drawable.mm_contact);
////		menu.add(Menu.NONE, Menu.FIRST + 5, 2, this.getResources().getString(R.string.contact_history)).setIcon(
////				R.drawable.mm_msghistory);
////		// houyuchun modify 20120620 end
////		return true;
////	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		boolean result = super.onOptionsItemSelected(item);
//
//		switch (item.getItemId()) {
//		// case Menu.FIRST + 1:// 呼叫
//		// call_menu(sip_uri_box2);
//		// break;
//		case Menu.FIRST + 4:
//			Intent intent_contact = new Intent(mActivity,
//					com.zed3.sipua.ui.PrtCallTab.class);
//			intent_contact.putExtra("Tab", "Contact");
//			startActivity(intent_contact);
//			break;
//
//		case Menu.FIRST + 5:
//			Intent intent_history = new Intent(mActivity,
//					com.zed3.sipua.ui.PrtCallTab.class);
//			intent_history.putExtra("Tab", "History");
//			startActivity(intent_history);
//			break;
//		}
//		return result;
//	}
//
//	public static String getVersion() {
//		return getVersion(Receiver.mContext);
//	}
//
//	public static String getVersion(Context context) {
//		final String unknown = "Unknown";
//
//		if (context == null) {
//			return unknown;
//		}
//
//		try {
//			String ret = context.getPackageManager().getPackageInfo(
//					context.getPackageName(), 0).versionName;
//			if (ret.contains(" + "))
//				ret = ret.substring(0, ret.indexOf(" + ")) + "b";
//			return ret;
//		} catch (NameNotFoundException ex) {
//		}
//
//		return unknown;
//	}
//
//	
//	@Override
//	public void onDismiss(DialogInterface dialog) {
//		onResume();
//	}
//
//
//	public void showMenuPopuWindow() {
//		if (menuPopupWindow == null) {
//
//			menuPopupView = View.inflate(mActivity, R.layout.call_menu_popup,
//					null);
//			menuPopupExit = (LinearLayout) menuPopupView
//					.findViewById(R.id.menu_popup_exit);
//			menuPopupExit.setOnClickListener(this);
//			menuPopupSetting = (LinearLayout) menuPopupView
//					.findViewById(R.id.menu_popup_setting);
//			menuPopupSetting.setOnClickListener(this);
//			
//			menuPopupCallHistory = (LinearLayout) menuPopupView
//					.findViewById(R.id.call_menu_popup_history);
//			menuPopupCallHistory.setOnClickListener(this);
//			menuPopupCallHistory.setVisibility(View.GONE);
//			/*
//			 * 在代码中 new出来view对象 或者 设置popwindows的时候 里面接受的参数都是 px单位
//			 */
//			menuPopupWindow = new PopupWindow(menuPopupView, /* r - l */
//			DensityUtil.getDipWidth(mActivity)
//					- DensityUtil.dip2px(mActivity, 1), /* b - t */
//			/*- */DensityUtil.dip2px(mActivity, 70));
//			// 给popupwindow设置一个透明的背景颜色,如果不设置 会导致动画效果没法显示
//			menuPopupWindow.setBackgroundDrawable(new ColorDrawable(
//					Color.TRANSPARENT));
////			sa = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.1f);
//			sa = new ScaleAnimation(-0.5f, 1.0f, -0.5f, 0.1f);
//			sa.setDuration(150);
//		}
//
//		menuPopupWindow.showAtLocation(mRootView, Gravity.BOTTOM,
//		/* location[0] *//* 10 + 60 */0, /* location[1] *//* 400 */
//				DensityUtil.dip2px(mActivity, 1));
//		// 注意popupwindow 默认是没有指定背景颜色的, 悬浮在activity的上面.
//
//		menuPopupView.startAnimation(sa);
//	}
//	private static void initMenuPopuWindow() {
//		// TODO Auto-generated method stub
//		LayoutInflater inflater = mActivity.getLayoutInflater();
//    	mMenuView = inflater.inflate(R.layout.menu, null);
//		mMenuGrid = (GridView)mMenuView.findViewById(R.id.menuGridView);
//    	String [] menuItemNames = mActivity.getResources().getStringArray(R.array.siglecall_menu_item_name_array);
//		mMenuGrid.setAdapter(new MenuGridAdapter(mActivity,menuItemNames));
////		mMenuPopuWindow = new PopupWindow(mMenuView);
//		
//		mMenuPopuWindow = new PopupWindow(mMenuView, LayoutParams.FILL_PARENT,  LayoutParams.WRAP_CONTENT);
//		mMenuPopuWindow.setFocusable(true);
////		以下两行加上去后就可以使用BACK键关闭POPWINDOW
//		ColorDrawable dw = new ColorDrawable(0x00);
//		mMenuPopuWindow.setBackgroundDrawable(dw);
//		
//		mMenuPopuWindow.setAnimationStyle(android.R.style.Animation_Toast);  
////		mMenuPopuWindow.setAnimationStyle(android.R.style.Animation_Translucent);  
//	}
//
//
//	public boolean dismissMenuPopupWindows() {
//		// TODO Auto-generated method stub
//		if (menuPopupWindow != null && menuPopupWindow.isShowing()) {
//			menuPopupWindow.dismiss();
//			return true;
//		} else {
//			return false;
//		}
//		
//	}
//
//
//	@Override
//	public void onClick(View v) {
//		// TODO Auto-generated method stub
//		Intent intent;
//		dismissMenuPopupWindows();
//		dismissPopupWindow();
//		String number;
//		Bundle extras;
//		switch (v.getId()) {
//		case R.id.edit_menu:// 编辑结束
//			isEditMode = false;
//			editMenuView.setVisibility(View.GONE);
//			break;
//		case R.id.call_keyboard:// 删除联系人
//			
//			break;
//		case R.id.contact_user_popup_edit_delete:// 删除联系人
//			deletHistory(mPosition);
//			refesh();
//			break;
//		case R.id.contact_user_popup_edit_cancel:// 取消
//			dismissPopupWindow();
//			break;
//		//显示键盘
//		case R.id.call_history_coverview:
//			isUserHideKeyboard = true;
//			hideKeyboard(true);
//			break;
//		//显示键盘
//		case R.id.call_show_keyboard:
//			isUserShowKeyboard = true;
//			hideKeyboard(false);
//			break;
//		//隐藏键盘
//		case R.id.hide_keyboard:
//			isUserHideKeyboard = true;
//			hideKeyboard(true);
//			break;
//		case R.id.menu_popup_exit:
//			Intent it = new Intent(mActivity, FlowRefreshService.class);
//			mActivity.stopService(it);
//			intent = new Intent();
//			intent.setAction(MainSipDroid.ACTION_EXIT_APPLICATION);
//			mActivity.sendBroadcast(intent);
//
//			break;
//		case R.id.call_menu_popup_history:
//			
//			intent = new Intent(mActivity,PrtCallHistory.class);
//			mActivity.startActivity(intent);
//			
//			break;
//			
//		case R.id.menu_popup_setting:
//			intent = new Intent(mActivity, Settings.class);
//			mActivity.startActivity(intent);
//
//			break;
//			
//		case R.id.pone :
//			numTxt.setText(numTxt.getText() + "1");
//		
//			playTone('1');
//			break;
//		case R.id.ptwo :
//			numTxt.setText(numTxt.getText() + "2");
//			playTone('2');
//			break;
//		case R.id.pthree :
//			numTxt.setText(numTxt.getText() + "3");
//			playTone('3');
//			break;
//		case R.id.pfour :
//			numTxt.setText(numTxt.getText() + "4");
//			playTone('4');
//			break;
//		case R.id.pfive :
//			numTxt.setText(numTxt.getText() + "5");
//			playTone('5');
//			break;
//		case R.id.psix :
//			numTxt.setText(numTxt.getText() + "6");
//			playTone('6');
//			break;
//		case R.id.pseven :
//			numTxt.setText(numTxt.getText() + "7");
//			playTone('7');
//			break;
//		case R.id.penight :
//			numTxt.setText(numTxt.getText() + "8");
//			playTone('8');
//			break;
//		case R.id.pnine :
//			numTxt.setText(numTxt.getText() + "9");
//			
//			playTone('9');
//			break;
//			
//		case R.id.p0 :
//			numTxt.setText(numTxt.getText() + "0");
//			playTone('0');
//			break;
//			
//		case R.id.pmi :
//			numTxt.setText(numTxt.getText() + "*");
//			playTone('*');
//			break;
//			
//		case R.id.pjing :
//			numTxt.setText(numTxt.getText() + "#");
//			playTone('#');
//			break;
//			
//		case R.id.psearch :
//			ContactUtil.startContactActivity(mActivity);
//			break;
//			
//		case R.id.video_call :
//			
//			
//			break;
//		case R.id.pphone :
//			call_menu(numTxt,false);
//			break;
//			
//		case R.id.num_save :
//			number = numTxt.getText().toString();
//			if (modifyIntent == null) {
//				modifyIntent = new Intent(mActivity,
//						AddContactDialog.class);
//			}
//			extras = new Bundle();
//			extras.putString(AddContactDialog.USER_NUMBER, number);
////			extras.putString(AddContactDialog.USER_NAME, name);
//			extras.putInt(AddContactDialog.TYPE, AddContactDialog.SAVE);
//			modifyIntent.putExtras(extras );
//			mActivity.startActivity(modifyIntent);
//			
//			break;
//			
//		case R.id.pdel :
//			String str = numTxt.getText().toString();
//			if (str.length() > 1)
//				numTxt.setText(str.substring(0, str.length() - 1));
//			else
//				numTxt.setText("");
//			playTone('d');
//			break;
//			
//		//通话记录点击事件
//		case R.id.contact_user_popup_delete: // 删除本条记录
//			deletHistory(mPosition);
//			refesh();
//			break;
//		case R.id.contact_user_popup_save: // 添加到联系人
//			if (modifyIntent == null) {
//				modifyIntent = new Intent(mActivity,
//						AddContactDialog.class);
//			}
//			extras = new Bundle();
//			extras.putString(AddContactDialog.USER_NUMBER, clickedItemNumber);
////			extras.putString(AddContactDialog.USER_NAME, name);
//			extras.putInt(AddContactDialog.TYPE, AddContactDialog.SAVE);
//			modifyIntent.putExtras(extras );
//			mActivity.startActivity(modifyIntent);
//		
//			break;
////		case R.id.contact_user_popup_message:// 发短信
////
////			intent = new Intent(mContext, MessageDialogueActivity.class);
////			intent.putExtra(MessageDialogueActivity.USER_NUMBER, 
////					clickedItemNumber);
////			startActivity(intent);
////			
////			break;
////		case R.id.contact_user_popup_video:// 视频通话
////				//是否注册成功
////				if(!Receiver.mSipdroidEngine.isRegistered())
////				{
////					MyToast.showToast(true, mContext, R.string.notfast);
////					return ;
////				}
////				
////				if(Receiver.call_state == UserAgent.UA_STATE_INCALL || Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL)
////				{
////					MyToast.showToast(true, mContext, R.string.sipdroid_existline);
////					return ;
////				}
////				//号码不能为空
////				if (numTxt.getText().toString().trim().length() == 0) {
////					MyToast.showToast(true, mContext, R.string.sipdroid_numnull);
////					return ;
////				}
////				//判断不能呼叫本机号码
////				if (numTxt.getText().toString().trim().equals(MemoryMg.getInstance().TerminalNum))// 或从pctool里读取值
////				{
////					MyToast.showToast(true, mContext, R.string.sipdroid_callmyself);
////					return ;
////				}
////				call_menu(numTxt,true);
////
////			break;
//			
////		case R.id.contact_user_popup_call:// 语音通话
////			//是否注册成功
////			if(!Receiver.mSipdroidEngine.isRegistered())
////			{
////				MyToast.showToast(true, mContext, R.string.notfast);
////				return ;
////			}
////			
////			if(Receiver.call_state == UserAgent.UA_STATE_INCALL || Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL)
////			{
////				MyToast.showToast(true, mContext, R.string.sipdroid_existline);
////				return ;
////			}
////			//号码不能为空
////			if (numTxt.getText().toString().trim().length() == 0) {
////				MyToast.showToast(true, mContext, R.string.sipdroid_numnull);
////				return ;
////			}
////			//判断不能呼叫本机号码
////			if (numTxt.getText().toString().trim().equals(MemoryMg.getInstance().TerminalNum))// 或从pctool里读取值
////			{
////				MyToast.showToast(true, mContext, R.string.sipdroid_callmyself);
////				return ;
////			}
////			
////			
////			
////			call_menu(numTxt,false);
////			break;
//		default:
//			break;
//		}
//		dismissPopupWindow();
//	}
//	private boolean chanageEditMode() {
//		dismissPopupWindow();
//		if (currentClickedView != null) {
//			currentClickedView.setBackgroundColor(Color.parseColor(COLOR_LIGHT));
//		}
//		isEditMode = (!isEditMode);
//		
////		if (isEditMode) {
////			mUserList.setBackgroundColor(Color.WHITE);
////		} else {
////			mUserList.setBackgroundColor(Color.BLUE);
////		}
//		return isEditMode;
//	}
//	private void hideKeyboard(boolean sure) {
//		// TODO Auto-generated method stub
//		if (sure) {
//			if (isKeyBoardHided) {
//				return;
//			}
//			if (keyBoardHideSA == null) {
////				keyboardView.showAtLocation(mRootView, Gravity.LEFT | Gravity.TOP,
////						location[0] + 60, location[1]);
////				keyBoardHideSA = new ScaleAnimation(1.0f, 1.0f, 0.1f, -1f);
//				keyBoardHideSA = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.1f);
//				keyBoardHideSA.setDuration(200);
//			}
//			keyboardView.startAnimation(keyBoardHideSA);
////			callHistoryListView.setVisibility(View.VISIBLE);
//			callHistoryOverView.setVisibility(View.INVISIBLE);
//			keyboardView.setVisibility(View.INVISIBLE);
//			controlViewsNoKeyboard.setVisibility(View.VISIBLE);
//			isKeyBoardHided = true;
//		}else {
//			if (keyBoardShowSA == null) {
//				keyBoardShowSA = new ScaleAnimation(1.0f, 1.0f, 0.1f, 1f);
//				keyBoardShowSA.setDuration(200);
//			}
//			keyboardView.startAnimation(keyBoardShowSA);
//			keyboardView.setVisibility(View.VISIBLE);
////			callHistoryListView.setVisibility(View.INVISIBLE);
//			callHistoryOverView.setVisibility(View.VISIBLE);
//			controlViewsNoKeyboard.setVisibility(View.INVISIBLE);
//			isKeyBoardHided = false;
//		}
//	}
//
//
//
//    //单呼
//	void call_menu(EditText view,boolean flag) {
//		String target = view.getText().toString();
//		if (mAlertDlg != null) {
//			mAlertDlg.cancel();
//		}
//		
//		if (flag)
//			Receiver.engine(mContext).isMakeVideoCall = 1;
//		else
//			Receiver.engine(mContext).isMakeVideoCall = 0;
//		
//		if (target.length() == 0)
//			mAlertDlg = new AlertDialog.Builder(mContext)
//					.setMessage(R.string.empty).setTitle(R.string.app_name)
//					.setIcon(R.drawable.icon22).setCancelable(true).show();
//		else if (!Receiver.engine(mContext).call(target, true))
//			mAlertDlg = new AlertDialog.Builder(mContext)
//					.setMessage(R.string.notfast).setTitle(R.string.app_name)
//					.setIcon(R.drawable.icon22).setCancelable(true).show();
//	}
	
}
