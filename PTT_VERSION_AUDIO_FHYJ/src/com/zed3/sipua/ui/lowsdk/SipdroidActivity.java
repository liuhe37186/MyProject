/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 * Nitin Khanna, Hughes Systique Corp. (Reason: Android specific change, optmization, bug fix) 
 */
package com.zed3.sipua.ui.lowsdk;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.dialog.DialogUtil;
import com.zed3.location.MemoryMg;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallHistoryDatabase;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.CallActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.contact.AddContactDialog;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.Tools;
import com.zed3.utils.Zed3Log;

/////////////////////////////////////////////////////////////////////
// this the main activity of Sipdroid
// for modifying it additional terms according to section 7, GPL apply
// see ADDITIONAL_TERMS.txt
/////////////////////////////////////////////////////////////////////
public class SipdroidActivity extends BaseActivity implements
		OnDismissListener, OnClickListener, OnLongClickListener {

	public static final boolean release = true;
	public static final boolean market = false;
	public static final HashMap<Character, Integer> mToneMap = new HashMap<Character, Integer>();

	private static AlertDialog mAlertDlg;

	//
	private EditText numTxt = null;
	private ImageButton btnone = null;
	private ImageButton btntwo = null;
	private ImageButton btnthree = null;
	private ImageButton btnfour = null;
	private ImageButton btnfive = null;
	private ImageButton btnsix = null;
	private ImageButton btnseven = null;
	private ImageButton btnenight = null;
	private ImageButton btnnine = null;
	private ImageButton btn0 = null;
	private ImageButton btnmi = null;
	private ImageButton btnjing = null;

	//
	private ImageButton btnsearch = null;
	private ImageButton btndialy = null;
	private ImageButton btndel = null;

	private String CurGrpID = "";

	// menu
	// private View callHistoryPopupView;
	private PopupWindow callHistoryPopupWindow;
	private ScaleAnimation sa1;

	// longclick
	private PopupWindow popupWindow4Delete;
	private LinearLayout popupDelectCancel;
	private LinearLayout popupDelectDelect;
	private View editModePopupView;
	private View currentClickedView;
	public static final String COLOR_LIGHT = "#FFBDBDBD";
	private ScaleAnimation sa2;

	// keyboard
	private ScaleAnimation keyBoardHideSA;
	private ScaleAnimation keyBoardShowSA;

	// 通话记录 popupwindow view
	private LinearLayout popup_delete;
	private LinearLayout popup_save;
	private LinearLayout popup_message;
	private LinearLayout popup_video;
	private LinearLayout popup_call;
	private static PopupWindow popupWindow;
	private View popupView;
	private View history_message;
	private View history_call;

	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock = new Object();// 监视器对象锁
	private boolean mDTMFToneEnabled; // 按键操作音
	private static final int TONE_LENGTH_MS = 150;// 延迟时间
	public static final String NULLSTR = "--";

	void playTone(Character tone) {
		// TODO 播放按键声音
		if (!mDTMFToneEnabled) {
			return;
		}

		AudioManager audioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		int ringerMode = audioManager.getRingerMode();
		if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
				|| (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {// 静音或震动时不发出按键声音
			return;
		}

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				Log.w("tagdd", "playTone: mToneGenerator == null, tone: "
						+ tone);
				return;
			}
			mToneGenerator.startTone(mToneMap.get(tone), TONE_LENGTH_MS);// 发声TONE_LENGTH_MS
		}
	}

	private static Activity mContext;
	private View mRootView;
	private ImageButton saveNumber;
	private ImageButton videoCall;
	private ListView callHistoryListView;
	private View keyboardView;
	private View callHistoryView;
	private ImageButton btnHideKeyboard;
	private View callHistoryOverView;
	private View deleteFinishView;
	private View editMenuView;

	// 联系人
	private int SelectItemIndex = -1;
	private Cursor mCursor;
	private MyAdapter mAdapter = null;
	private View controlViews;
	private View controlViewsNoKeyboard;

	private static PopupWindow menuPopupWindow = null;

	public String mClickedItemNumber;
	protected String mClickedItemName;
	private boolean isKeyBoardHided;
	private Intent modifyIntent;
	// bug
	protected int scrollCount = 0;
	private boolean isUserHideKeyboard;
	private boolean isUserShowKeyboard;
	protected boolean isResumed;
	protected int mPosition;
	private CallHistoryDatabase db;
	private boolean isEditMode;
	protected String numberViewText;
	private String numberString;
	private boolean needShowKeyBoardOnResume;
	private IntentFilter mFilter;
	protected String mNumber;
	private boolean needLog = true;
	private String tag = "SipdroidActivity";
	private long lastTimeMillis;
	protected boolean isWaitingForNewData;
	private int popupWidth;
	private int startPX;
	protected boolean numTxtCursor;

	MyAdapter adpter = null;

	private void initCallHistoryViews() {
		// TODO Auto-generated method stub

		callHistoryListView = (ListView) mRootView
				.findViewById(R.id.call_history_list);
		callHistoryListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, final int arg2, long arg3) {
						new AlertDialog.Builder(SipdroidActivity.this)
								.setTitle(R.string.options_one)
								.setItems(R.array.calllist_longclick,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												switch (which) {
												case 0:
													new AlertDialog.Builder(
															SipdroidActivity.this)
															.setTitle(
																	R.string.delete_log)
															.setMessage(
																	getResources()
																			.getString(
																					R.string.delete_call_notify))
															.setPositiveButton(
																	getResources()
																			.getString(
																					R.string.delete_ok),
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																			long d = adpter
																					.getItemId(arg2);
																			db.delete(
																					"call_history",
																					"_id"
																							+ "="
																							+ adpter.getItemId(arg2));
																			mHandle.sendMessage(mHandle
																					.obtainMessage(
																							1,
																							GetDataFromDB()));
																		}
																	})
															.setNegativeButton(
																	getResources()
																			.getString(
																					R.string.cancel),
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																		}
																	}).show();
													break;
												case 1:
													new AlertDialog.Builder(
															SipdroidActivity.this)
															.setTitle(
																	R.string.delete_all_log)
															.setMessage(
																	getResources()
																			.getString(
																					R.string.delete_allCall_notify))
															.setPositiveButton(
																	getResources()
																			.getString(
																					R.string.delete_all_ok),
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																			db.delete(
																					"call_history",
																					null);
																			mHandle.sendMessage(mHandle
																					.obtainMessage(
																							1,
																							GetDataFromDB()));
																		}
																	})
															.setNegativeButton(
																	getResources()
																			.getString(
																					R.string.cancel),
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																		}
																	}).show();
													break;
												}

											}
										}).show();
						return false;
					}
				});
		callHistoryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				// mClickedItemNumber = cursor.getString(6);
				// TextView numberView = (TextView) v
				// .findViewById(R.id.call_history_number);
				// TextView nameView = (TextView) v
				// .findViewById(R.id.call_history_name);
				// mNumber = numberView.getText().toString();
				// mClickedItemName = nameView.getText().toString();

				mPosition = position;

				// 清空处理然后设置某一项
				// for (int i = 0; i < parent.getChildCount(); i++) {
				// View child = parent.getChildAt(i);
				// child.setBackgroundResource(R.drawable.tabcell);
				// child.findViewById(R.id.call_video_btn).setVisibility(View.GONE);
				// child.findViewById(R.id.call_history_type).setVisibility(View.VISIBLE);
				// }
				// v.setBackgroundResource(R.drawable.tabcell_sel);
				// v.findViewById(R.id.call_video_btn).setVisibility(View.VISIBLE);
				// v.findViewById(R.id.call_history_type).setVisibility(View.GONE);
			}

		});

	}

	// protected void deletHistory(int position) {
	// // TODO Auto-generated method stub
	// mCursor.moveToPosition(position);
	// String begin_str = mCursor.getString(mCursor
	// .getColumnIndex("begin_str"));
	// if (db == null) {
	// db = CallHistoryDatabase.getInstance();
	// }
	// db.delete("call_history", "begin_str = '" + begin_str + "'");
	// }

	public class MyAdapter extends BaseAdapter {
		private int left;
		private LayoutInflater mInflater;
		ViewHolder vHolder = null;
		String number, name, begin_str, type;
		Long begin, end;
		List<java.util.Map<String, Object>> dbList = null;

		public MyAdapter(Context context,
				List<java.util.Map<String, Object>> dbList) {
			mInflater = LayoutInflater.from(context);
			this.dbList = dbList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (dbList == null)
				return 0;
			return dbList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.parseInt(String.valueOf(dbList.get(position).get(
					"_id")));
		}

		public void refreshListView(List<java.util.Map<String, Object>> dbList) {
			this.dbList = dbList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.call_history_item,
						null);
				vHolder = new ViewHolder();

				vHolder.title = (TextView) convertView
						.findViewById(R.id.call_history_name);
				vHolder.tp = vHolder.title.getPaint();
				vHolder.tp.setFakeBoldText(true);

				// vHolder.call_history_txt = (TextView) convertView
				// .findViewById(R.id.call_history_txt);

				vHolder.time = (TextView) convertView
						.findViewById(R.id.call_history_time);
				vHolder.img = (ImageView) convertView
						.findViewById(R.id.call_history_type);
				vHolder.photoImageView = (ImageView) convertView
						.findViewById(R.id.call_history_photo);
				vHolder.numberTextView = (TextView) convertView
						.findViewById(R.id.call_history_number);
				vHolder.videoBtn = (ImageView) convertView
						.findViewById(R.id.call_video_btn);

				vHolder.voiceBtn = (ImageView) convertView
						.findViewById(R.id.call_voice_btn);
				vHolder.msgBtn = (ImageView) convertView
						.findViewById(R.id.call_msg_btn);
				vHolder.line_sub = (LinearLayout) convertView
						.findViewById(R.id.line_sub);
				vHolder.line_sub2 = (LinearLayout) convertView
						.findViewById(R.id.line_sub2);

				convertView.setTag(vHolder);
			} else {
				// convertView.setBackgroundResource(R.drawable.tabcell);
				// convertView.findViewById(R.id.call_video_btn).setVisibility(View.GONE);
				// convertView.findViewById(R.id.call_history_type).setVisibility(View.VISIBLE);

				vHolder = (ViewHolder) convertView.getTag();
			}
			left = 0;
			if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
				vHolder.line_sub.setVisibility(View.GONE);
				vHolder.videoBtn.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
				vHolder.line_sub2.setVisibility(View.GONE);
				vHolder.voiceBtn.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (!DeviceInfo.CONFIG_SUPPORT_IM) {
				vHolder.msgBtn.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (left == 1) {
				vHolder.line_sub2.setVisibility(View.GONE);
				vHolder.line_sub.setVisibility(View.GONE);
			}
			if (DeviceInfo.CONFIG_SUPPORT_VIDEO
					&& DeviceInfo.CONFIG_SUPPORT_AUDIO
					&& !DeviceInfo.CONFIG_SUPPORT_IM) {
				vHolder.line_sub2.setVisibility(View.GONE);
			}
			/*
			 * guojunfeng
			 * 四种类型CallIn（已接来电）、CallOut（呼出已接通）、CallUnak（未接来电）、CallUnout(呼出未接通)
			 * 根据四种类型匹配相应记录的图标
			 */

			type = (String) dbList.get(position).get("type");
			name = (String) dbList.get(position).get("name");
			if (name == null) {
				name = "null";
			}
			number = (String) dbList.get(position).get("number");
			if (number == null) {
				number = "nullnumber";
			}
			begin_str = (String) dbList.get(position).get("begin_str");
			// begin = (Long) dbList.get(position).get("begin");
			// end = (Long) dbList.get(position).get("end");
			final int pos = position;
			// 视频呼叫按钮
			vHolder.videoBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// Toast.makeText(mContext, (String)
					// dbList.get(pos).get("begin_str")+"|"+(Long)
					// dbList.get(pos).get("end"), Toast.LENGTH_SHORT).show();
					String number = (String) dbList.get(pos).get("number");
					if (number == null) {
						DialogUtil.showCheckDialog(
								SipdroidActivity.mContext,
								getResources().getString(R.string.information),
								getResources().getString(
										R.string.number_not_exist),
								getResources().getString(R.string.ok_know));
					} else {
						CallUtil.makeVideoCall(mContext, /*
														 * (String)
														 * dbList.get(pos
														 * ).get("number")
														 */number, null);
					}
				}
			});
			vHolder.msgBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(mContext,
							MessageDialogueActivity.class);
					intent.putExtra(MessageDialogueActivity.USER_NAME,
							(String) dbList.get(pos).get("name"));
					intent.putExtra(MessageDialogueActivity.USER_NUMBER,
							(String) dbList.get(pos).get("number"));

					startActivity(intent);
					;
				}
			});
			// 语音
			vHolder.voiceBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					String number = (String) dbList.get(pos).get("number");
					if (number == null) {
						DialogUtil.showCheckDialog(
								SipdroidActivity.mContext,
								getResources().getString(R.string.information),
								getResources().getString(
										R.string.number_not_exist),
								getResources().getString(R.string.ok_know));
					} else {
						// if (DeviceInfo.AudioType == 1)
						// CallUtil.makeAudioCall(mContext, number, null);
						// else {
						// Intent intent = new Intent(Intent.ACTION_CALL, Uri
						// .parse("tel:" + number));
						// startActivity(intent);
						// }
						if (MemoryMg.getInstance().PhoneType == -1) {// 自动
							if (DeviceInfo.CONFIG_AUDIO_MODE == 1)
								CallUtil.makeAudioCall(mContext, number, null);
							else {
								Intent intent = new Intent(Intent.ACTION_CALL,
										Uri.parse("tel:" + number));
								startActivity(intent);
							}
						} else {// 手动
							if (MemoryMg.getInstance().PhoneType == 1)
								CallUtil.makeAudioCall(mContext, number, null);
							else {
								Intent intent = new Intent(Intent.ACTION_CALL,
										Uri.parse("tel:" + number));
								startActivity(intent);
							}
						}
					}
				}
			});

			if (type.equals("CallIn")) {
				vHolder.img
						.setImageResource(R.drawable.ic_call_incoming_holo_dark);
				// vHolder.call_history_txt.setText("(已接)");
			} else if (type.equals("CallUnak")) {
				vHolder.img
						.setImageResource(R.drawable.ic_call_missed_holo_dark);
				// vHolder.call_history_txt.setText("   ");
			} else if (type.equals("CallOut")) {
				vHolder.img
						.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
				// vHolder.call_history_txt.setText("(已拨)");
			} else if (type.equals("CallUnout")) {
				vHolder.img
						.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
				// vHolder.call_history_txt.setText("(已拨)");
			}

			// 如果存在联系人，用联系人姓名，否则用号码 ，modify by guojunfeng 2013-05-05
			String contact = ContactUtil.getUserName(number);
			vHolder.title.setText(contact == null ? number : contact);
			vHolder.numberTextView.setText(contact == null ? NULLSTR : number);
			if (contact != null) {
				vHolder.title.setText(contact);
				vHolder.numberTextView.setText(number);
			} else if (!name.equals(number)) {
				vHolder.title.setText(name);
				vHolder.numberTextView.setText(number);
			} else {
				vHolder.title.setText(number);
				vHolder.numberTextView.setText(NULLSTR);
			}
			// vHolder.info.setText(begin_str);
			if (!begin_str.equals("")) {
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date strtodate = formatter.parse(begin_str.trim(),
						new ParsePosition(0));

				vHolder.time.setText(twoDateDistance(strtodate.getTime()));
			}
			return convertView;
		}
	}

	/**
	 * 计算两个日期型的时间相差多少时间
	 * 
	 * @param startDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @return
	 */
	public String twoDateDistance(long startDate/* Date startDate, Date endDate */) {

		if (startDate == 0 /* || endDate == null */) {
			return "";
		}
		long timeLong = System.currentTimeMillis()/* endDate.getTime() */
				- startDate;
		if (timeLong < 60 * 1000){
			if(timeLong/1000 <= 1)
				return timeLong/1000+" "+getResources().getString(R.string.second_ago);
			return timeLong/1000+" "+getResources().getString(R.string.seconds_ago);
		}	
		else if (timeLong < 60 * 60 * 1000) {
			timeLong = timeLong / 1000 / 60;
			if(timeLong <= 1)
				return timeLong+" "+getResources().getString(R.string.minute_ago);
			return timeLong+" "+getResources().getString(R.string.minutes_ago);
		} else if (timeLong < 60 * 60 * 24 * 1000) {
			timeLong = timeLong / 60 / 60 / 1000;
			if(timeLong <= 1)
				return timeLong+" "+getResources().getString(R.string.hour_ago);
			return timeLong+" "+getResources().getString(R.string.hours_ago);
		} else if (timeLong < 60 * 60 * 24 * 1000 * 7) {
			timeLong = timeLong / 1000 / 60 / 60 / 24;
			if(timeLong <= 1)
				return timeLong+" "+getResources().getString(R.string.day_ago);
			return timeLong+" "+getResources().getString(R.string.days_ago);
		} else {
			timeLong = timeLong / 1000 / 60 / 60 / 24 / 7;
			if(timeLong <= 1)
				return timeLong+" "+getResources().getString(R.string.week_ago);
			return timeLong+" "+getResources().getString(R.string.weeks_ago);
		} // if (timeLong < 60 * 60 * 24 * 1000 * 7 * 4)
	}

	private class ViewHolder {
		TextView title;
		TextPaint tp;
		TextView info;
		TextView time;
		TextView call_history_txt;
		ImageView img;
		ImageView photoImageView;
		TextView numberTextView;
		ImageView videoBtn;
		ImageView voiceBtn;
		ImageView msgBtn;
		LinearLayout line_sub;
		LinearLayout line_sub2;
	}

	private synchronized List<java.util.Map<String, Object>> GetDataFromDB() {
		List<java.util.Map<String, Object>> dbList = new ArrayList<Map<String, Object>>();

		mCursor = db.query("call_history", "begin_str desc");
		java.util.Map<String, Object> dbMap = null;
		String number, name, begin_str, type;
		int _id, status;// 获取记录的唯一id

		for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
				.moveToNext()) {
			dbMap = new HashMap<String, Object>();

			number = mCursor.getString(mCursor.getColumnIndex("number"));
			// 查询名称
			name = mCursor.getString(mCursor.getColumnIndex("name"));

			// add by hu 20140404
			_id = mCursor.getInt(mCursor.getColumnIndex("_id"));
			// 空指针异常
			if (name == null) {
				name = number;
			}
			// modify by liangzhang 2014-09-05
			begin_str = mCursor.getString(mCursor.getColumnIndex("begin_str"));
			type = mCursor.getString(mCursor.getColumnIndex("type"));
			status = mCursor.getInt(mCursor.getColumnIndex("status"));
			dbMap.put("number", number);
			dbMap.put("name", name);
			dbMap.put("_id", _id);
			dbMap.put("begin_str", begin_str);
			dbMap.put("type", type);
			dbMap.put("status", status);
			// dbMap.put("begin",
			// mCursor.getLong(mCursor.getColumnIndex("begin")));
			// dbMap.put("end", mCursor.getLong(mCursor.getColumnIndex("end")));
			dbList.add(dbMap);
		}

		return dbList;
	}

	// add by hu 2013-09-24
	ImageButton back_btn;
	ImageView keyboard_img;
	boolean isKeyboard = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		MyLog.e("sipdroidActivity", System.currentTimeMillis() + "");
		// lastTimeMillis = System.currentTimeMillis();
		mContext = this;
		mRootView = getLayoutInflater().inflate(R.layout.sipdroid_lowsdk, null);
		mRootView.setOnClickListener(this);// dismiss menu popupwindow
		setContentView(mRootView);
		// ------------------------
		db = CallHistoryDatabase.getInstance(mContext);
		// 初始化ListView
		initCallHistoryViews();
		// 初始化布局
		InitCallScreen();
		initKeyBoard();
		initMenuViews();
		back_btn = (ImageButton) findViewById(R.id.back_button);
		// contact_btn = (ImageView)findViewById(R.id.contact_button);
		back_btn.setOnClickListener(this);
		// contact_btn.setOnClickListener(this);

		keyboard_img = (ImageView) findViewById(R.id.keyboard_img);
		keyboard_img.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView tv = (ImageView) findViewById(R.id.keyboard_img);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					tv.setBackgroundResource(R.color.btn_click_bg);
					if (isKeyboard) {
						tv.setImageResource(R.drawable.keyboarddown);
					} else {
						tv.setImageResource(R.drawable.keyboardup);
					}
					break;
				case MotionEvent.ACTION_UP:
					tv.setBackgroundResource(R.color.whole_bg);
					if (isKeyboard) {
						tv.setImageResource(R.drawable.keyboarddown_release);
					} else {
						tv.setImageResource(R.drawable.keyboardup_release);
					}
					break;
				}
				return false;
			}
		});
		keyboard_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ImageView pic;
				pic = (ImageView) v;

				if (isKeyboard) {
					isKeyboard = false;
					pic.setImageResource(R.drawable.keyboardup_release);
					// callHistoryOverView.setVisibility(View.INVISIBLE);
					keyboardView.setVisibility(View.INVISIBLE);
				} else {
					isKeyboard = true;
					pic.setImageResource(R.drawable.keyboarddown_release);
					keyboardView.setVisibility(View.VISIBLE);
					// callHistoryOverView.setVisibility(View.VISIBLE);
				}

			}
		});
		registerReceiver(refreshlistReceiver, new IntentFilter(
				"sipdroid.history.fresh"));
	}

	private void initKeyBoard() {
		// TODO Auto-generated method stub
		numTxt = (EditText) findViewById(R.id.p_digits);
		numTxt.setText("");
		numTxt.setEnabled(false);
		numTxt.setInputType(InputType.TYPE_NULL);
		// add by guojunfeng 2013-05-20
		numTxt.setCursorVisible(false);
		numTxtCursor = false;
		numTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 设置光标为可见状态
				numTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(numTxt.getWindowToken(), 0);
				numTxt.setCursorVisible(true);
				numTxtCursor = true;
			}
		});
		// numTxt.setSingleLine(true);

		// add by guojunfeng 2013-05-20
		// 设置文本框输入字数上限；
		numTxt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1000) });
		numTxt.setDrawingCacheEnabled(true);
		//
		btnjing = (ImageButton) mRootView.findViewById(R.id.pjing);
		btnjing.setOnClickListener(this);
		btnone = (ImageButton) mRootView.findViewById(R.id.pone);
		btnone.setOnClickListener(this);
		//
		btntwo = (ImageButton) mRootView.findViewById(R.id.ptwo);
		btntwo.setOnClickListener(this);
		//
		btnthree = (ImageButton) mRootView.findViewById(R.id.pthree);
		btnthree.setOnClickListener(this);
		//
		btnfour = (ImageButton) mRootView.findViewById(R.id.pfour);
		btnfour.setOnClickListener(this);
		//
		btnfive = (ImageButton) mRootView.findViewById(R.id.pfive);
		btnfive.setOnClickListener(this);
		//
		btnsix = (ImageButton) mRootView.findViewById(R.id.psix);
		btnsix.setOnClickListener(this);
		//
		btnseven = (ImageButton) mRootView.findViewById(R.id.pseven);
		btnseven.setOnClickListener(this);
		//
		btnenight = (ImageButton) mRootView.findViewById(R.id.penight);
		btnenight.setOnClickListener(this);
		//
		btnnine = (ImageButton) mRootView.findViewById(R.id.pnine);
		btnnine.setOnClickListener(this);
		//
		btn0 = (ImageButton) mRootView.findViewById(R.id.p0);
		btn0.setOnClickListener(this);
		//
		btnmi = (ImageButton) mRootView.findViewById(R.id.pmi);
		btnmi.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		releaseToneGenerator();
		unregisterReceiver(refreshlistReceiver);
		if (db != null) {// 关闭数据库 add by liangzhang 2014-09-05
			db.close();
		}
		super.onDestroy();
	}

	private void releaseToneGenerator(){
		if(mToneGenerator!=null){
			try {
				mToneGenerator.release();
			} catch(Exception e) {
				if(e!=null) e.printStackTrace();
			} finally {
				mToneGenerator = null;
			}
		}
	}
	
	private boolean isFirstResume = true;
	private String contactName;
	private ImageButton btnShowKeyboard;

	private void InitCallScreen() {
		mToneMap.put('1', ToneGenerator.TONE_DTMF_1);
		mToneMap.put('2', ToneGenerator.TONE_DTMF_2);
		mToneMap.put('3', ToneGenerator.TONE_DTMF_3);
		mToneMap.put('4', ToneGenerator.TONE_DTMF_4);
		mToneMap.put('5', ToneGenerator.TONE_DTMF_5);
		mToneMap.put('6', ToneGenerator.TONE_DTMF_6);
		mToneMap.put('7', ToneGenerator.TONE_DTMF_7);
		mToneMap.put('8', ToneGenerator.TONE_DTMF_8);
		mToneMap.put('9', ToneGenerator.TONE_DTMF_9);
		mToneMap.put('0', ToneGenerator.TONE_DTMF_0);
		mToneMap.put('#', ToneGenerator.TONE_DTMF_P);
		mToneMap.put('*', ToneGenerator.TONE_DTMF_S);
		mToneMap.put('d', ToneGenerator.TONE_DTMF_A);
	}

	private void ShowCurrentGrp() {
		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		if (pttGrp != null) {
			CurGrpID = pttGrp.grpID;
		}
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		scrollCount = 0;
		NotificationManager mNotificationMgr = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationMgr.cancel(Receiver.MISSED_CALL_NOTIFICATION);
		mNotificationMgr.cancel(Receiver.AUTO_ANSWER_NOTIFICATION);
		super.onStart();
	}

	Handler mHandle = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				List<java.util.Map<String, Object>> dbList = (List<java.util.Map<String, Object>>) msg.obj;
				if (adpter == null) {
					adpter = new MyAdapter(mContext, dbList);
					callHistoryListView.setAdapter(adpter);
				} else {
					adpter.refreshListView(dbList);
					adpter.notifyDataSetChanged();
				}

			}
		}
	};

	// 当对话框获得焦点时调用
	@Override
	public void onResume() {
		Zed3Log.debug("testcrash", "SipdroidActivity#onResume() enter");
		super.onResume();
		//reset by Receiver.onState() modify by mou 2014-12-29
//		CallActivity.resetCallParams();
		// add by liangzhang 2014-09-05 进入通话页面后更新数据库信息
		// modify by liangzhang 2014-09-11 直接更新未接电话数据库信息
		ContentValues values = new ContentValues();
		values.put("status", 1);
		db.update("call_history", "type='CallUnak'" + " and status=" + 0,
				values);
		// 更新未接来电信息后发送广播以清除提醒图标
		sendBroadcast(new Intent(Contants.ACTION_CLEAR_MISSEDCALL));

		Thread initList = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					// 获取通话记录里的数据列表
					// List<java.util.Map<String, Object>> dbList =
					// GetDataFromDB();
					// MyLog.e(tag, dbList.size() + "PPPP");

					mHandle.sendMessage(mHandle.obtainMessage(1,
							GetDataFromDB()));

				} catch (Exception e) {
					MyLog.e(tag, e.toString());
					e.printStackTrace();
				}

			}
		});
		initList.start();

		// // add by hu
		// if (Receiver.viewInvisible) {
		// controlViewsNoKeyboard.setVisibility(View.VISIBLE);
		// callHistoryOverView.setVisibility(View.INVISIBLE);
		// keyboardView.setVisibility(View.INVISIBLE);
		// // add by guojunfeng 2013-05-17
		// // 避免从未接来电进入并点击通话记录时键盘一闪而过；
		// isKeyBoardHided = true;
		// Receiver.viewInvisible = false;
		// } else {
		// if (isKeyBoardHided) {
		// controlViewsNoKeyboard.setVisibility(View.VISIBLE);
		// callHistoryOverView.setVisibility(View.INVISIBLE);
		// keyboardView.setVisibility(View.INVISIBLE);
		// }
		// // add by guojunfeng 2013-05-24
		// // 默认显示拨号盘
		// else {
		// controlViewsNoKeyboard.setVisibility(View.INVISIBLE);
		// callHistoryOverView.setVisibility(View.VISIBLE);
		// keyboardView.setVisibility(View.VISIBLE);
		// }
		// }

		Receiver.engine(mContext);
		if (popup_video != null) {
			popup_video.setVisibility(Settings.needVideoCall ? View.VISIBLE
					: View.GONE);
		}

		if (MemoryMg.getInstance().CallNum != "") {
			// 赋值
			numTxt.setText(MemoryMg.getInstance().CallNum);
			MemoryMg.getInstance().CallNum = "";
		}

		mDTMFToneEnabled = android.provider.Settings.System.getInt(
				mContext.getContentResolver(),
				android.provider.Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;// 获取系统参数“按键操作音”是否开启

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				try {
					mToneGenerator = new ToneGenerator(
							AudioManager.STREAM_MUSIC, 80);
					mContext.setVolumeControlStream(AudioManager.STREAM_MUSIC);
				} catch (RuntimeException e) {
					Log.w("tag",
							"Exception caught while creating local tone generator: "
									+ e);
					mToneGenerator = null;
				}
			}
		}

		// bug 单呼界面添加联系人，输入面板会挤压拨号盘。
		// add by guojunfeng 2013-05-05
		if (needShowKeyBoardOnResume) {
			keyboardView.setVisibility(View.VISIBLE);
			needShowKeyBoardOnResume = false;
		}

		MyLog.e("sipdroidActivity", System.currentTimeMillis() + "");
		
		Zed3Log.debug("testcrash", "SipdroidActivity#onResume() exit");
	}

	private void initMenuViews() {
		// TODO Auto-generated method stub
		// 搜索
		// btnsearch = (ImageButton) mRootView.findViewById(R.id.psearch);
		// btnsearch.setOnClickListener(this);
		// btnsearch.setVisibility(View.GONE);
		// 拨打
		btndialy = (ImageButton) mRootView.findViewById(R.id.pphone);

		btndialy.setOnClickListener(this);

		// 拨打
		videoCall = (ImageButton) mRootView.findViewById(R.id.video_call);

		videoCall.setOnClickListener(this);

		// 保存
		saveNumber = (ImageButton) mRootView.findViewById(R.id.num_save);
		saveNumber.setOnClickListener(this);
		// saveNumber.setVisibility(View.GONE);
		// 删除
		btndel = (ImageButton) mRootView.findViewById(R.id.pdel);
		btndel.setOnClickListener(this);
		// add by guojunfeng 2013-07-16
		btndel.setOnLongClickListener(this);
		// 键盘
		// 显示键盘
		// btnShowKeyboard = (ImageButton) mRootView
		// .findViewById(R.id.call_show_keyboard);
		// btnShowKeyboard.setOnClickListener(this);
		// // 隐藏键盘
		// btnHideKeyboard = (ImageButton) mRootView
		// .findViewById(R.id.hide_keyboard);
		// btnHideKeyboard.setOnClickListener(this);
		// // 显示键盘后的控制区
		// controlViews = mRootView.findViewById(R.id.call_controlviews);

		// 隐藏键盘后的控制区
		// controlViewsNoKeyboard = mRootView
		// .findViewById(R.id.call_controlviews_nokeyboard);
		// controlViewsNoKeyboard.setVisibility(View.INVISIBLE);

		keyboardView = mRootView.findViewById(R.id.call_keyboard);
		keyboardView.setOnClickListener(this);
		keyboardView.setVisibility(View.INVISIBLE);
		callHistoryView = mRootView.findViewById(R.id.call_history);
		callHistoryView.setVisibility(View.VISIBLE);

		callHistoryOverView = mRootView
				.findViewById(R.id.call_history_coverview);
		callHistoryOverView.setVisibility(View.INVISIBLE);
		callHistoryOverView.setOnClickListener(this);

		// // 通话记录删除控制
		// editMenuView = mRootView.findViewById(R.id.edit_menu);
		// editMenuView.setOnClickListener(this);
		// editMenuView.setVisibility(View.INVISIBLE);
		// deleteFinishView = mRootView.findViewById(R.id.edit_ok);
		// deleteFinishView.setOnClickListener(this);

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		dismissPopupWindows();
		super.onPause();
	}

	// 点menu键选择退出不起作用，disable by oumogang 2014-01-24
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		// case Menu.FIRST + 1:// 呼叫
		// call_menu(sip_uri_box2);
		// break;
		case 1:
			Tools.exitApp(SipdroidActivity.this);
			break;
		}
		return result;
	}

	// // sendBroadcast(new
	// Intent("com.zed3.sipua.exitActivity").putExtra("exit", true));
	// // SipdroidActivity.this.finish();
	// //
	// // break;
	// case Menu.FIRST + 4:
	// Intent intent_contact = new Intent(mContext,
	// com.zed3.sipua.ui.PrtCallTab.class);
	// intent_contact.putExtra("Tab", "Contact");
	// startActivity(intent_contact);
	// break;
	//
	// case Menu.FIRST + 5:
	// Intent intent_history = new Intent(mContext,
	// com.zed3.sipua.ui.PrtCallTab.class);
	// intent_history.putExtra("Tab", "History");
	// startActivity(intent_history);
	// break;
	// }
	// return result;
	// }

	public static String getVersion() {
		return getVersion(Receiver.mContext);
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
	public void onDismiss(DialogInterface dialog) {
		onResume();
	}

	public static boolean dismissMenuPopupWindows() {
		// TODO Auto-generated method stub
		if (menuPopupWindow != null && menuPopupWindow.isShowing()) {
			menuPopupWindow.dismiss();
			return true;
		} else {
			return false;
		}

	}

	// add by guojunfeng 2013-05-20
	// 按钮事件触发手动调用此方法
	public void downKey(String key) {
		numTxt.setGravity(Gravity.CENTER);//GQT英文版 2014-8-28
		if (!numTxtCursor) {
			numTxt.setCursorVisible(true);
			numTxtCursor = true;
		}
		// 设置一个变量判断是否有光标
		if (numTxtCursor == true) {
			// 获得光标的位置
			int index = numTxt.getSelectionStart();
			// 将字符串转换为StringBuffer
			StringBuffer sb = new StringBuffer(numTxt.getText().toString()
					.trim());
			// 将字符插入光标所在的位置
			sb = sb.insert(index, key);
			numTxt.setText(sb.toString());
			// 设置光标的位置保持不变
			Selection.setSelection(numTxt.getText(), index + 1);
		} else {
			numTxt.setText(numTxt.getText().toString().trim() + key);
		}
		// 手机振动
		toVibrate();
	}

	private void toVibrate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		dismissMenuPopupWindows();
		// dismissPopupWindow();
		String number;
		Bundle extras;
		switch (v.getId()) {
		case R.id.call_keyboard:// 删除联系人

			break;
		// 显示键盘
		case R.id.call_history_coverview:
			isUserHideKeyboard = true;
			hideKeyboard(true);
			break;
		case R.id.pone:
			// numTxt.setText(numTxt.getText() + "1");
			downKey("1");
			playTone('1');
			break;
		case R.id.ptwo:
			// numTxt.setText(numTxt.getText() + "2");
			downKey("2");
			playTone('2');
			break;
		case R.id.pthree:
			// numTxt.setText(numTxt.getText() + "3");
			downKey("3");
			playTone('3');
			break;
		case R.id.pfour:
			// numTxt.setText(numTxt.getText() + "4");
			downKey("4");
			playTone('4');
			break;
		case R.id.pfive:
			// numTxt.setText(numTxt.getText() + "5");
			downKey("5");
			playTone('5');
			break;
		case R.id.psix:
			// numTxt.setText(numTxt.getText() + "6");
			downKey("6");
			playTone('6');
			break;
		case R.id.pseven:
			// numTxt.setText(numTxt.getText() + "7");
			downKey("7");
			playTone('7');
			break;
		case R.id.penight:
			// numTxt.setText(numTxt.getText() + "8");
			downKey("8");
			playTone('8');
			break;
		case R.id.pnine:
			// numTxt.setText(numTxt.getText() + "9");
			downKey("9");
			playTone('9');
			break;

		case R.id.p0:
			// numTxt.setText(numTxt.getText() + "0");
			downKey("0");
			playTone('0');
			break;

		case R.id.pmi:
			// numTxt.setText(numTxt.getText() + "*");
			downKey("*");
			playTone('*');
			break;

		case R.id.pjing:
			// numTxt.setText(numTxt.getText() + "#");
			downKey("#");
			playTone('#');
			// add by oumogang 2014-04-03
			numberString = numTxt.getText().toString().trim();
			if (numberString.equals("999999#")) {
				// ZMBluetoothManager.getInstance().showBluetoothStates();
			}
			break;

		// case R.id.psearch:
		// // ContactUtil.startContactActivity(mContext);
		// SipdroidActivity.this.startActivity(new
		// Intent(SipdroidActivity.this,ContactActivity.class));
		// break;

		case R.id.video_call:
			if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
				Toast.makeText(mContext,getResources().getString(R.string.ve_service_not), Toast.LENGTH_SHORT).show();
				break;
			}
			// modify by guojunfeng 2013-07-18
			numberString = numTxt.getText().toString().trim();
			CallUtil.makeVideoCall(mContext, numberString, null);
			// CallUtil.makeAudioCall(mContext, numberString, null);
			break;
		case R.id.pphone:// 移动电话
			if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
				Toast.makeText(mContext, getResources().getString(R.string.vc_service_not), Toast.LENGTH_SHORT).show();
				break;
			}
			// 0:移动电话 1:voip电话
			numberString = numTxt.getText().toString().trim();

			if (MemoryMg.getInstance().PhoneType == -1) {// 自动
				if (DeviceInfo.CONFIG_AUDIO_MODE == 1)
					CallUtil.makeAudioCall(mContext, numberString, null);
				else {
					intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
							+ numberString));
					startActivity(intent);
				}
			} else {// 手动
				if (MemoryMg.getInstance().PhoneType == 1)
					CallUtil.makeAudioCall(mContext, numberString, null);
				else {
					intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
							+ numberString));
					startActivity(intent);
				}
			}

			break;

		case R.id.num_save:
			// bug 单呼界面添加联系人，输入面板会挤压拨号盘。
			// add by guojunfeng 2013-05-05
			needShowKeyBoardOnResume = true;
			keyboardView.setVisibility(View.INVISIBLE);

			number = numTxt.getText().toString();
			intent = new Intent(mContext, AddContactDialog.class);
			extras = new Bundle();
			extras.putString(AddContactDialog.USER_NUMBER, number);
			// extras.putString(AddContactDialog.USER_NAME, name);
			extras.putInt(AddContactDialog.TYPE, AddContactDialog.SAVE);
			intent.putExtras(extras);
			mContext.startActivity(intent);

			break;

		case R.id.pdel:
			// String str = numTxt.getText().toString();
			// if (str.length() > 1)
			// numTxt.setText(str.substring(0, str.length() - 1));
			// else
			// numTxt.setText("");
			delete();
			playTone('d');
			break;
		case R.id.back_button:
			SipdroidActivity.this.finish();
			break;
		// case R.id.contact_button:
		// startActivity(new
		// Intent(SipdroidActivity.this,ContactActivity.class));
		// break;
		default:
			break;
		}
		// dismissPopupWindow();
	}

	private void delete() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer(numTxt.getText().toString().trim());
		int index = 0;
		if (numTxtCursor == true) {
			index = numTxt.getSelectionStart();
			if (index > 0) {
				sb = sb.delete(index - 1, index);
			}
		} else {
			index = numTxt.length();
			if (index > 0) {
				sb = sb.delete(index - 1, index);
			}
		}
		numTxt.setText(sb.toString());
		if (index > 0) {
			Selection.setSelection(numTxt.getText(), index - 1);
		}
		if (numTxt.getText().toString().trim().length() <= 0) {
			numTxt.setCursorVisible(false);
			numTxtCursor = false;
			numTxt.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);//GQT英文版 2014-8-28
		}
	}

	private boolean chanageEditMode() {
		// dismissPopupWindow();
		if (currentClickedView != null) {
			currentClickedView
					.setBackgroundColor(Color.parseColor(COLOR_LIGHT));
		}
		isEditMode = (!isEditMode);

		return isEditMode;
	}

	private void hideKeyboard(boolean sure) {
		// TODO Auto-generated method stub
		if (sure) {
			if (isKeyBoardHided) {
				return;
			}
			if (keyBoardHideSA == null) {
				// keyboardView.showAtLocation(mRootView, Gravity.LEFT |
				// Gravity.TOP,
				// location[0] + 60, location[1]);
				// keyBoardHideSA = new ScaleAnimation(1.0f, 1.0f, 0.1f, -1f);
				keyBoardHideSA = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.1f);
				keyBoardHideSA.setDuration(200);
			}
			keyboardView.startAnimation(keyBoardHideSA);

			callHistoryOverView.setVisibility(View.INVISIBLE);
			keyboardView.setVisibility(View.INVISIBLE);
			// controlViewsNoKeyboard.setVisibility(View.VISIBLE);
			isKeyBoardHided = true;
		} else {
			if (keyBoardShowSA == null) {
				keyBoardShowSA = new ScaleAnimation(1.0f, 1.0f, 0.1f, 1f);
				keyBoardShowSA.setDuration(200);
			}
			keyboardView.startAnimation(keyBoardShowSA);
			keyboardView.setVisibility(View.VISIBLE);

			callHistoryOverView.setVisibility(View.VISIBLE);
			// controlViewsNoKeyboard.setVisibility(View.INVISIBLE);
			isKeyBoardHided = false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent_ = new Intent(Intent.ACTION_MAIN);
			intent_.addCategory(Intent.CATEGORY_HOME);
			intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent_);
			return false;
		}
		boolean result = false;
		switch (keyCode) {

		// case KeyEvent.KEYCODE_BACK:
		// SipUAApp.startHomeActivity(SipUAApp.mContext);
		case KeyEvent.KEYCODE_HOME:
			break;
		case KeyEvent.KEYCODE_MENU:
			if (dismissMenuPopupWindows()) {
				return true;
			} else {
				break;
			}

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	public static void dismissPopupWindows() {
		dismissMenuPopupWindows();
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// add by guojunfeng 2013-07-16
		case R.id.pdel:
			numTxt.setText("");
			break;
		default:
			break;
		}
		return false;
	}

	BroadcastReceiver refreshlistReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Thread initList = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						// 获取通话记录里的数据列表
						// List<java.util.Map<String, Object>> dbList =
						// GetDataFromDB();
						// MyLog.e(tag, dbList.size() + "PPPP");

						mHandle.sendMessage(mHandle.obtainMessage(1,
								GetDataFromDB()));

					} catch (Exception e) {
						MyLog.e(tag, e.toString());
						e.printStackTrace();
					}

				}
			});
			initList.start();
		};
	};
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // TODO Auto-generated method stub
	// menu.add(0, 1, 0, "退出").setIcon(R.drawable.exit);
	// return super.onCreateOptionsMenu(menu);
	// }
}
