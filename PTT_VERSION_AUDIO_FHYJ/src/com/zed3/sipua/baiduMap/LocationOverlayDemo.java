package com.zed3.sipua.baiduMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.zoolu.tools.GroupListInfo;
import org.zoolu.tools.MyLog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.zed3.bluetooth.PTTListener;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.location.MemoryMg;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.R;
import com.zed3.sipua.R.drawable;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.ActvityNotify;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

/**
 * 
 */
public class LocationOverlayDemo extends BaseActivity implements PTTListener {
	// add by hu 2014 /1/22
	LinearLayout tab_show1, tab_hide;
	PttGrps pttGrps = Receiver.GetCurUA().GetAllGrps();
	// add by hu
	ArrayAdapter<String> adapter;
	boolean flag = false;
	TextView hide_text;

	// VerticalSeekBar seekbar;
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		isStarted = true;

		// if (groupListStatusViews.getVisibility() == View.VISIBLE) {
		// GroupListUtil.getData4GroupList();
		// }
		if (pttGrps.GetCount() == 0) {
			isRefresh_ = 0;
			ShowCurrentGrp();
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (timer1 != null) {
			timer1.cancel();
			timer1 = null;
		}
		isResume = false;
		isStarted = false;
		super.onStop();
	}

	private Timer timer1;
	// private Timer timer2;
	private String TAG = "TalkBackNew";
	private ArrayList<GroupMember> GrpGisForMap;
	private String jsonTag;
	public static LocationOverlayDemo mContext;
	public static int isRefresh_ = 1;
	public static boolean isPttPressing;
	private IntentFilter mFilter;
	private IntentFilter intentfilter2;
	private String currentGrpNum;
	static boolean mHasPttGrp;
	public boolean isPTTUseful = false;
	ArrayList<GroupListInfo> arrayList;
	public static final String ACTION_GETSTATUS_MESSAGE = "com.zed3.sipua.ui_groupstatelist";
	public boolean isStarted;
	private final String ACTION_GROUP_STATUS = "com.zed3.sipua.ui_groupcall.group_status";
	private final String ACTION_ALL_GROUP_CHANGE = "com.zed3.sipua.ui_groupcall.all_groups_change";
	private final String ACTION_SINGLE_2_GROUP = "com.zed3.sipua.ui_groupcall.single_2_group";
	private final String ACTION_RECEIVE_TEXT_MESSAGE = "com.zed3.sipua.ui_receive_text_message";
	private final String ACTION_SEND_TEXT_MESSAGE_FAIL = "com.zed3.sipua.ui_send_text_message_fail";
	private final String ACTION_SEND_TEXT_MESSAGE_SUCCEED = "com.zed3.sipua.ui_send_text_message_succeed";
	private final String ACTION_SEND_TEXT_MESSAGE_TIMEOUT = "com.zed3.sipua.ui_send_text_message_timeout";
	public final String ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
	private final String ACTION_DESTORY_MESSAGE = "com.zed3.sipua.ui_destory_message";
	private HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap;

	// bluetooth for ptt,add by oumogang 2014-03-06
	private Handler mPttHandler;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				Log.e(TAG, "GroupRefresh timer1 ==> change All");
				isRefresh_ = 0;
				ShowCurrentGrp();
				getAllGrpGisInfo(currentGrpNum);
				break;
			case 2:
				if (pttGrps.GetCount() == 0) {
					pttGrps = Receiver.GetCurUA().GetAllGrps();
				}
				break;
			case 3:
				Log.e("+++====>>>", jsonTag);
				if (GrpGisForMap != null) {
					GrpGisForMap.clear();
					GrpGisForMap = new ArrayList<GroupMember>();
				} else {
					GrpGisForMap = new ArrayList<GroupMember>();
				}
				try {
					JSONObject jsonO = new JSONObject(jsonTag);
					JSONArray jsonArray = jsonO.getJSONArray("Content");
					int x = Integer.parseInt(jsonO.getString("TotalGIS"));
					Log.e("guojunfeng2013", "Json Array size :" + x + "");
					for (int i = 0; i < x; i++) {
						boolean isNeed = true;
						JSONObject curObj = jsonArray.getJSONObject(i);
						Log.e("Info print all==>" + i, curObj.toString());
						GroupMember info = new GroupMember();
						if (curObj.optString("Latitude").equalsIgnoreCase(
								"null")
								|| curObj.optString("Longitude").equals("null")
								|| curObj.optString("Latitude") == null
								|| curObj.optString("Longitude") == null) {
							return;
						}
						if (curObj.optString("Latitude").equals("0.000000")
								&& curObj.optString("Longitude").equals(
										"0.000000")) {
							isNeed = false;
						}
						try {
							info.setGeo(new GeoPoint(
									(int) (Double.parseDouble(curObj
											.optString("Latitude")) * 1E6),
									(int) (Double.parseDouble(curObj
											.optString("Longitude")) * 1E6)));
						} catch (NumberFormatException e) {
							Log.e("=====>",
									"json parse NumberFormatException..." + e);
							isNeed = false;
							e.printStackTrace();
						}
						info.setNum(curObj.optString("User"));
						info.setName(ContactUtil.getUserName(info.getNum()));

						if (arrayList != null && info.getName() == null) {
							for (int a = 0; a < arrayList.size(); a++) {
								if (arrayList.get(a).GrpNum.equals(info
										.getNum())) {
									info.setName(arrayList.get(a).GrpName);
									break;
								}
							}
						}
						if (arrayList != null) {
							for (int a = 0; a < arrayList.size(); a++) {
								if (arrayList.get(a).GrpNum.equals(info
										.getNum())) {
									if (arrayList.get(a).GrpState.equals(SipUAApp.mContext.getResources().getString(R.string.the_status_1))) {
										info.setOnline(false);
									} else {
										info.setOnline(true);
									}

									break;
								}
							}
						}
						if (info.getName() == null) {
							info.setName(info.getNum());
						}
						Log.e("guojunfeng2013",
								"result:" + info.getName() + info.getNum()
										+ info.getGeo() + info.isOnline());
						if (info.getNum().equals(Settings.getUserName()))
							continue;
						if (isNeed) {
							GrpGisForMap.add(info);
						}
					}

				} catch (JSONException e) {
					e.printStackTrace();
					Log.e("=====>", "json parse exception..." + e);
				}
				if (mOverlay != null) {
					mOverlay.removeAll();
				}
				LogUtil.makeLog(" LocationDemo", " Handler mMapView.refresh() ");
				//添加！null判断，解决切换对讲组的时候，退出地图界面，应用崩溃的问题 . add by lwang 2014-12-19
				if (mMapView != null) {
					mMapView.refresh();
					initOverlay(null, -1);
				}

				break;
			case 4:
				break;
			case 5:
				if (mMapView != null) {
					mMapView.getController().setZoom(
							msg.getData().getInt("zoom"));
				}
				break;
			}
		}

	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		// Receive GroupInfo to Update Activity
		@Override
		public void onReceive(Context mContext, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(ACTION_GROUP_STATUS)) {
				Bundle bundle = intent.getExtras();

				String speaker = bundle.getString("1");
				String userNum = null;
				if (speaker != null) {
					String[] arr = speaker.split(" ");
					if (arr.length == 1) {
						userNum = arr[0];
						// speaker = arr[0];
					} else {
						userNum = arr[0];
						speaker = arr[1];
					}
				}

				// ShowCurrentGrp();
				// updateGroupLists();

				// use ShowCurrentGrp() instead
				PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
				mHasPttGrp = pttGrp != null ? true : false;

			}
			// else if (intent.getAction()
			// .equalsIgnoreCase(ACTION_GROUP_2_GROUP)) {
			//
			// Bundle bundle = intent.getExtras();
			// GroupCallActivity.setTalkGrp(bundle.getString("0"));
			// GroupCallActivity.setActionMode(ACTION_GROUP_2_GROUP);
			// Intent startActivity = new Intent();
			// startActivity.setClass(Receiver.mContext, ActvityNotify.class);
			// startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// Receiver.mContext.startActivity(startActivity);
			//
			// }
			else if (intent.getAction().equalsIgnoreCase(ACTION_SINGLE_2_GROUP)) {
				Bundle bundle = intent.getExtras();
				GroupCallUtil.setTalkGrp(bundle.getString("0"));
				GroupCallUtil.setActionMode(ACTION_SINGLE_2_GROUP);

				if (!UserAgent.isCamerPttDialog) {
					Intent startActivity = new Intent();
					startActivity.setClass(Receiver.mContext,
							ActvityNotify.class);
					startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SipUAApp.getAppContext().startActivity(startActivity);
				} else {
					sendBroadcast(new Intent("com.zed3.sipua.camera_ptt_dialog"));
				}

			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_RECEIVE_TEXT_MESSAGE)) {

				Bundle bundle = intent.getExtras();
				// String strFrom = bundle.getString("0");
				// String strTo = bundle.getString("1");
				String strSeq = bundle.getString("2");
				// String strContent = bundle.getString("3");
				// String strSipName = bundle.getString("4");

				if (!strSeq.equals(MemoryMg.getInstance().LastSeq)) {
					Intent broadCast = new Intent(
							MessageDialogueActivity.RECEIVE_TEXT_MESSAGE);
					mContext.sendBroadcast(broadCast);
				}

			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_SEND_TEXT_MESSAGE_FAIL)) {
				String mE_id = intent.getStringExtra("0");
				Intent broadCast = new Intent(
						MessageDialogueActivity.SEND_TEXT_FAIL);
				broadCast.putExtra("0", mE_id);
				mContext.sendBroadcast(broadCast);
			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_SEND_TEXT_MESSAGE_SUCCEED)) {
				String mE_id = intent.getStringExtra("0");
				// guojunfenging
				Intent broadCast = new Intent(
						MessageDialogueActivity.SEND_TEXT_SUCCEED);
				broadCast.putExtra("0", mE_id);
				mContext.sendBroadcast(broadCast);
			}//
			else if (intent.getAction().equalsIgnoreCase(
					ACTION_SEND_TEXT_MESSAGE_TIMEOUT)) {
				String mE_id = intent.getStringExtra("E_id");
				Intent broadCast = new Intent(
						MessageDialogueActivity.SEND_TEXT_TIMEOUT);
				broadCast.putExtra("0", mE_id);
				mContext.sendBroadcast(broadCast);

			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_DESTORY_MESSAGE)) {

				MemoryMg.getInstance().IsChangeListener = false;

				Editor edit = PreferenceManager.getDefaultSharedPreferences(
						mContext).edit();
				edit.putString(Settings.PREF_USERNAME, "");//
				edit.putString(Settings.PREF_PASSWORD, "");//
				edit.putString(Settings.PREF_SERVER, "");//
				edit.commit();

				// GroupCallActivity.QuitMessage();
			} else if (intent.getAction().equalsIgnoreCase(
					Contants.ACTION_NEWWORK_CHANGED)) {
				LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip3);
				if (intent.getIntExtra(Contants.NETWORK_STATE, -1) == Contants.NETWORK_STATE_GOOD) {
					ll.setVisibility(View.GONE);
				} else {
					ll.setVisibility(View.VISIBLE);
					isRefresh_++;
				}
			} else if (intent.getAction().equalsIgnoreCase(
					Contants.ACTION_CURRENT_GROUP_CHANGED)) {
				ShowCurrentGrp();
				getAllGrpGisInfo(currentGrpNum);
			} else if (intent.getAction().equalsIgnoreCase(ACTION_3GFlow_ALARM)) {
				Tools.FlowAlertDialog(LocationOverlayDemo.this);
			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_ALL_GROUP_CHANGE)) {
				isRefresh_ = 0;
				ShowCurrentGrp();
			}
		}
	};
	private BroadcastReceiver groupListReceiver = new BroadcastReceiver() {

		// String tag = "groupListReceiver";

		@Override
		public void onReceive(Context mContext, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(Contants.ACTION_CLEAR_GROUPLIST)) {
				GroupListUtil.removeDataOfGroupList();
			}
			if (!isStarted) {
				return;
			}
			if (action.equals(Contants.ACTION_GROUPLIST_UPDATE_OVER)) {
				// mGroupNameAdapter.notify();
				ShowCurrentGrp();
			}

		}
	};
	public static boolean isResume;
	Handler progressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// hideLoadingProgress();
		};
	};

	// add by hu 2014/2/11
	ImageButton ib_zoom_in, ib_zoom_out, ib_position;

	LinearLayout btn_home, btn_changegroup;
	TextView title;
	// Spinner mySpin;
	static View pttkeyMap;
	static TextView pttkeyMap_text;
	boolean hideMyView = false;
	int kmarea = 5;
	ImageButton position_hide;

	LocationClient mLocClient;
	LocationData locData = null;
	public MyLocationListenner myListener = new MyLocationListenner();

	locationOverlay myLocationOverlay = null;
	private PopupOverlay pop = null;//
	private TextView popupText = null;//
	private View viewCache = null;

	MapView mMapView = null; //
	private MapController mMapController = null;

	OnCheckedChangeListener radioButtonListener = null;
	boolean isRequest = false;//
	boolean isFirstLoc = true;//
	int lastnum = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_locationoverlay);
		GroupListUtil.getData4GroupList();
		// add by hu 2014/2/11
		ib_zoom_in = (ImageButton) findViewById(R.id.zoom_in);
		ib_zoom_out = (ImageButton) findViewById(R.id.zoom_out);
		ib_position = (ImageButton) findViewById(R.id.position);

		tab_show1 = (LinearLayout) findViewById(R.id.map_tab_show1);
		tab_hide = (LinearLayout) findViewById(R.id.map_tab_hide);
		tab_hide.setLongClickable(true);
		tab_hide.setClickable(true);
		mContext = this;
		// GroupListUtil.getData4GroupList();
		// GroupListUtil.addGroupList2Contacts();
		mGroupListsMap = GroupListUtil.getGroupListsMap();
		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		if (pttGrp != null) {
			currentGrpNum = pttGrp.grpID;
			getAllGrpGisInfo(currentGrpNum);
		}
		mFilter = new IntentFilter();
		mFilter.addAction(Contants.ACTION_NEWWORK_CHANGED);
		mFilter.addAction(ACTION_GROUP_STATUS);
		mFilter.addAction(ACTION_ALL_GROUP_CHANGE);
		// mFilter.addAction(ACTION_GROUP_2_GROUP);
		mFilter.addAction(ACTION_SINGLE_2_GROUP);
		mFilter.addAction(ACTION_RECEIVE_TEXT_MESSAGE);
		mFilter.addAction(ACTION_SEND_TEXT_MESSAGE_FAIL);
		mFilter.addAction(ACTION_SEND_TEXT_MESSAGE_SUCCEED);
		mFilter.addAction(ACTION_SEND_TEXT_MESSAGE_TIMEOUT);
		mFilter.addAction(ACTION_DESTORY_MESSAGE);//
		mFilter.addAction(Contants.ACTION_CURRENT_GROUP_CHANGED);//
		mFilter.addAction(ACTION_3GFlow_ALARM);
		mContext.registerReceiver(mReceiver, mFilter);
		if (intentfilter2 == null) {
			intentfilter2 = new IntentFilter();
			intentfilter2.addAction(ACTION_GETSTATUS_MESSAGE);
			intentfilter2.addAction(Contants.ACTION_GROUPLIST_UPDATE_OVER);//

			intentfilter2.addAction(Contants.ACTION_GROUPLIST_CLEAR_OVER);//
			intentfilter2.addAction(Contants.ACTION_CLEAR_GROUPLIST);//
		}
		mContext.registerReceiver(groupListReceiver, intentfilter2);
		// timer2 = new Timer();
		// TimerTask tt2 = new TimerTask() {
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// Message msg = new Message();
		// msg.what = 2;
		// mHandler.sendMessage(msg);
		// }
		// };
		// timer2.schedule(tt2, 5000, 5000);

		timer1 = new Timer();
		TimerTask tt1 = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
			}
		};
		timer1.schedule(tt1, 0, 30000);
		// requestLocButton = (Button) findViewById(R.id.button1);
		// OnClickListener btnClickListener = new OnClickListener() {
		// public void onClick(View v) {
		// requestLocClick();
		// }
		// };
		// requestLocButton.setOnClickListener(btnClickListener); //

		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapView.getController().setZoom(14);
		ib_zoom_in.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int zoomlevel = Math.round(mMapView.getZoomLevel()) + 1;
				if (zoomlevel > 19) {
				} else {
					mMapView.getController().setZoom(zoomlevel);
				}
			}
		});
		ib_zoom_out.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int zoomlevel = Math.round(mMapView.getZoomLevel()) - 1;
				if (zoomlevel < 3) {
				} else {
					mMapView.getController().setZoom(zoomlevel);
				}
			}
		});
		ib_position.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				requestLocClick();
			}
		});
		// seekbar.setProgress(((14-3)*100)/15);
		mMapView.getController().enableClick(true);
		// mMapView.getController().setZoomGesturesEnabled(false);
		// mMapView.setBuiltInZoomControls(true);
		createPaopao();
		mOverlay = new MyOverlay(getResources().getDrawable(
				R.drawable.icon_gcoding), mMapView);
		mLocClient = new LocationClient(this);
		locData = new LocationData();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);//
		option.setCoorType("bd09ll"); //
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		myLocationOverlay = new locationOverlay(mMapView);
		myLocationOverlay.setData(locData);
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		mMapView.refresh();

		btn_home = (LinearLayout) findViewById(R.id.btn_home);
		btn_home.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
//				MainActivity.getInstance().startIntent(TalkBackNew.class);
			}
		});
		btn_home.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView tv = (TextView) findViewById(R.id.t_home);
				TextView tv_left = (TextView) findViewById(R.id.left_icon);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// btn_home.setBackgroundResource(R.color.red);
					tv.setTextColor(Color.WHITE);
					btn_home.setBackgroundResource(R.color.btn_click_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_press);
					break;
				case MotionEvent.ACTION_UP:
					// btn_home.setBackgroundResource(R.color.font_color3);
					tv.setTextColor(getResources()
							.getColor(R.color.font_color3));
					btn_home.setBackgroundResource(R.color.whole_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_release);
					break;
				}
				return false;
			}
		});
		title = (TextView) findViewById(R.id.map_title);
		// int position = findPos(Receiver.GetCurUA().GetCurGrp(),pttGrps);
		if (Receiver.GetCurUA().GetCurGrp() != null) {
			title.setText(Receiver.GetCurUA().GetCurGrp().grpName);
		}
		btn_changegroup = (LinearLayout) findViewById(R.id.btn_changegroup);
		btn_changegroup.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView tv = (TextView) findViewById(R.id.t_spin);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// btn_home.setBackgroundResource(R.color.red);
					tv.setTextColor(Color.WHITE);
					tv.setBackgroundResource(R.color.btn_click_bg);
					break;
				case MotionEvent.ACTION_UP:
					// btn_home.setBackgroundResource(R.color.font_color3);
					tv.setTextColor(getResources()
							.getColor(R.color.font_color3));
					tv.setBackgroundResource(R.color.whole_bg);
					break;
				}
				return false;
			}
		});
		btn_changegroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder b = new AlertDialog.Builder(
						LocationOverlayDemo.this).setAdapter(new MyAdapter(
						LocationOverlayDemo.this, pttGrps),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (!NetChecker.check(mContext, true)) {
									MyToast.showToast(
											true,
											mContext,
											getResources().getString(
													R.string.group_notify));
									return;
								}
								if (Receiver.GetCurUA().GetCurGrp() != pttGrps
										.GetGrpByIndex(which)) {
									if (isPttPressing) {
										MyToast.showToast(true, mContext, R.string.release_ptt_and_try_again);
										return;
									}
									Receiver.GetCurUA().SetCurGrp(
											pttGrps.GetGrpByIndex(which));
									currentGrpNum = pttGrps
											.GetGrpByIndex(which).grpID;
									getAllGrpGisInfo(currentGrpNum);
								}
								title.setText(pttGrps.GetGrpByIndex(which).grpName);
							}
						});
				Dialog d = b.create();
				d.show();
				d.setCanceledOnTouchOutside(true);

			}
		});
		// mySpin = (Spinner) findViewById(R.id.myspin);
		pttkeyMap = findViewById(R.id.pttkeymap);
		//pttkeyMap_text = (TextView) findViewById(R.id.textView1);
		pttkeyMap.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (!NetChecker.check(mContext, true)) {
						break;
					}
					pttkeyMap.setBackgroundResource(R.drawable.ptt_down_map);
					isPttPressing = true;
//					pttkeyMap_text.setText(getResources().getString(
//							R.string.releaseEnd));
//					GroupCallUtil.makeGroupCall(true, true);//2014-9-25 modify by wlei
					GroupCallUtil.makeGroupCall(true, false);
//					isPttPressing = true;
					break;
				case MotionEvent.ACTION_UP:
					if (!NetChecker.check(mContext, true)) {
						break;
					}
					if (isPttPressing) {
						isPttPressing = false;
						GroupCallUtil.makeGroupCall(false, false);
//						GroupCallUtil.PressPTT(false);//2014-9-25 modify by wlei
//						isPttPressing = false;
						pttkeyMap
								.setBackgroundResource(R.drawable.ptt_up_map);
//						pttkeyMap_text.setText(getResources().getString(
//								R.string.pushTalk));
						// group_button_ptt.setBackgroundResource(R.drawable.group_list_ptt_up);
					}
					break;
				}
				return true;
			}
		});
		position_hide = (ImageButton) findViewById(R.id.imageposition_hide);
		// position_show =(ImageButton)findViewById(R.id.imageposition_show);
		hide_text = (TextView) findViewById(R.id.hide_text);
		position_hide.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				requestLocClick();
			}
		});

		hide_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tab_show1.setVisibility(View.VISIBLE);
				tab_hide.setVisibility(View.GONE);
			}
		});
		requestLocClick();
		// adapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item);
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

		// if (mGroups != null && mGroups.size() > 0) {
		// mySpin.setAdapter(new MyAdapter(this,mGroups));
		// }else{
		// mySpin.setAdapter(new MyAdapter(this,null));
		// }
		// PttGrp curGrp = Receiver.GetCurUA().GetCurGrp();
		// int position = findPos(curGrp,mGroups);
		// if(position != -1){
		// mySpin.setSelection(position);
		// }
		// mySpin.setOnItemSelectedListener(new OnItemSelectedListener() {
		// @Override
		// public void onItemSelected(AdapterView<?> arg0, View arg1,
		// int position, long arg3) {
		// if (!NetChecker.check(mContext, true)) {
		// MyToast.showToast(true, mContext, "您不在任何对讲组");
		// return;
		// }
		// if(Receiver.GetCurUA().GetCurGrp()!=mGroups.get(position)){
		// Receiver.GetCurUA().SetCurGrp(mGroups.get(position));
		// }
		// Receiver.GetCurUA().SetCurGrp(mGroups.get(position));
		// if(position != -1){
		// mySpin.setSelection(position);
		// }
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> arg0) {
		// }
		// });

		// 创建属于主线程的handler
		mPttHandler = new Handler();
	}

	private int findPos(PttGrp curGrp, List<PttGrp> mGroups2) {
		if (pttGrps != null && pttGrps.GetCount() > 0 && curGrp != null) {
			int i = 0;
			for (PttGrp grp : mGroups2) {
				if (curGrp.grpID.equals(grp.grpID)) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}

	/**
	 */
	public void requestLocClick() {
		isRequest = true;
		mLocClient.requestLocation();
	}

	/**
	 * 
	 * @param marker
	 */
	// public void modifyLocationOverlayIcon(Drawable marker){
	// myLocationOverlay.setMarker(marker);
	// mMapView.refresh();
	// }
	/**
	 */
	public void createPaopao() {
		viewCache = getLayoutInflater()
				.inflate(R.layout.custom_text_view, null);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				pop.hidePop();
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
	}

	// overlay
	private MyOverlay mOverlay = null;
	private PopupOverlay overlayPop = null;
	private TextView overlayPopupText = null;
	private View viewCacheOverlay = null;
	private View popupInfo = null;
	private View popupLeft = null;
	private View popupRight = null;
	private OverlayItem mCurItem = null;
	private boolean isFirstTime = true;

	public void initOverlay(GeoPoint mypos, int miles) {
		List<GroupMember> list = null;
		if (miles < 0 || mypos == null) {
			// list = MapTools.getAllMem();
			list = GrpGisForMap;
		} else {
			list = MapTools.getMemInMiles(mypos, miles, GrpGisForMap);
		}
		if (list == null || list.size() < 1) {
			return;
		}
		for (GroupMember mem : list) {
			OverlayItem item = new OverlayItem(mem.getGeo(), mem.getName(),
					mem.getNum());
			if (!mem.isOnline()) {
				item.setMarker(getResources().getDrawable(
						R.drawable.icon_notonline));
			}
			if (mOverlay != null) {
				mOverlay.addItem(item);
			}
		}
		if (mypos == null && miles < 0 && isFirstTime) {
			if (mMapView != null && mMapView.getOverlays() != null) {
				mMapView.getOverlays().add(mOverlay);
				isFirstTime = false;
			}
		}
		mMapView.refresh();

		viewCacheOverlay = getLayoutInflater().inflate(
				R.layout.custom_text_view, null);
		popupInfo = (View) viewCacheOverlay.findViewById(R.id.popinfo);
		popupLeft = (View) viewCacheOverlay.findViewById(R.id.popleft);
		popupRight = (View) viewCacheOverlay.findViewById(R.id.popright);
		overlayPopupText = (TextView) viewCacheOverlay
				.findViewById(R.id.textcache);

		overlayPop = new PopupOverlay(mMapView, mypopListener);
	}

	/**
	 */
	PopupClickListener mypopListener = new PopupClickListener() {
		@Override
		public void onClickedPopup(int index) {
			String title = mCurItem.getTitle();
			String snippet = mCurItem.getSnippet();
			if (index == 0) {
				overlayPop.hidePop();
				if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
					MyToast.showToast(true, mContext, mContext.getResources()
							.getString(R.string.ve_service_not));
				} else {
					CallUtil.makeVideoCall(mContext, snippet, null);
				}
			} else if (index == 2) {
				overlayPop.hidePop();
				if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
					MyToast.showToast(true, mContext, mContext.getResources()
							.getString(R.string.vc_service_not));
				} else {
					if (MemoryMg.getInstance().PhoneType == -1) {// 自动
						if (DeviceInfo.CONFIG_AUDIO_MODE == 1)
							CallUtil.makeAudioCall(mContext, snippet, null);
						else {
							Intent intent = new Intent(Intent.ACTION_CALL,
									Uri.parse("tel:" + snippet));
							startActivity(intent);
						}
					} else {// 手动
						if (MemoryMg.getInstance().PhoneType == 1)
							CallUtil.makeAudioCall(mContext, snippet, null);
						else {
							Intent intent = new Intent(Intent.ACTION_CALL,
									Uri.parse("tel:" + snippet));
							startActivity(intent);
						}
					}
				}
			}
		}
	};

	public class MyOverlay extends ItemizedOverlay {

		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}

		@Override
		public boolean onTap(int index) {
			OverlayItem item = getItem(index);
			mCurItem = item;
			// if (index == 3){
			// GeoPoint pt = new GeoPoint((int) (mLat4 * 1E6),
			// (int) (mLon4 * 1E6));
			// pop.showPopup(button, pt, 32);
			// }
			// else{
			overlayPopupText.setText(getItem(index).getTitle());
			Bitmap[] bitMaps = { BMapUtil.getBitmapFromView(popupLeft),
					BMapUtil.getBitmapFromView(popupInfo),
					BMapUtil.getBitmapFromView(popupRight) };
			overlayPop.showPopup(bitMaps, item.getPoint(), 32);
			// }
			return true;
		}

		@Override
		public boolean onTap(GeoPoint pt, MapView mMapView) {
			if (overlayPop != null) {
				overlayPop.hidePop();
				// mMapView.removeView(button);
			}
			return false;
		}

	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.accuracy = location.getRadius();
			locData.direction = location.getDirection();
			myLocationOverlay.setData(locData);
			mMapView.refresh();
			if (isRequest || isFirstLoc) {
				Log.d("LocationOverlay", "receive location, animate to it");
				mMapController.animateTo(new GeoPoint(
						(int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6)));
				isRequest = false;
				// myLocationOverlay.setLocationMode(LocationMode.FOLLOWING);
				// requestLocButton.setText("跟随");
				// mCurBtnType = E_BUTTON_TYPE.FOLLOW;
			}
			isFirstLoc = false;
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public class locationOverlay extends MyLocationOverlay {

		public locationOverlay(MapView mapView) {
			super(mapView);
		}

		@Override
		protected boolean dispatchTap() {
			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText("我的位置");
			if (overlayPop != null)
				overlayPop.hidePop(); // pop会混淆 modify by hu 2014/1/6
			// modify by hu 2014/1/7 先不弹出
			// pop.showPopup(BMapUtil.getBitmapFromView(popupText), new
			// GeoPoint(
			// (int) (locData.latitude * 1e6),
			// (int) (locData.longitude * 1e6)), 8);
			return true;
		}

	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		isResume = true;
		if (timer1 == null) {
			timer1 = new Timer();
			TimerTask tt1 = new TimerTask() {
				@Override
				public void run() {
					Message msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
				}
			};
			timer1.schedule(tt1, 0, 30000);
		}
		Receiver.engine(mContext);
		if (!NetChecker.check(this, false)) {
			LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip3);
			ll.setVisibility(View.VISIBLE);
		} else {
			LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip3);
			ll.setVisibility(View.GONE);
		}
		// getData4GroupList();

		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		// add by oumogang 2013-05-24
		mHasPttGrp = pttGrp != null ? true : false;
		setPttBackground(isPttPressing);

		super.onResume();

	}

	@Override
	protected void onDestroy() {
		if (timer1 != null) {
			timer1.cancel();
			timer1 = null;
		}
		// if (timer2 != null) {
		// timer2.cancel();
		// timer2 = null;
		// }
		try {
			if (mFilter != null)
				mContext.unregisterReceiver(mReceiver);
			else
				MyLog.i("GroupCallActivity",
						"recv unregister fail! mFilter is null. ");

			if (intentfilter2 != null)
				mContext.unregisterReceiver(groupListReceiver);
			else
				MyLog.i("GroupCallActivity",
						"groupListReceiver unregister fail! intentfilter2 is null. ");

		} catch (Exception e) {
			MyLog.i("GroupCallActivity",
					"unregisterReceiver fail: " + e.toString());
		}
		if (mLocClient != null)
			mLocClient.stop();
		if (mOverlay != null) {
			mOverlay.removeAll();
			mOverlay = null;
		}
		LogUtil.makeLog(" LocationDemo", " onDestory beging ");
		mMapView.destroy();
		// MapView对象调用destory（）方法之后，手动将MapView的实例对象置为空，解决切换对讲组的时候，退出地图界面，应用崩溃的问题。add by lwang 2014-12-19
		mMapView = null;
		LogUtil.makeLog(" LocationDemo", " onDestory end.. "+(mMapView == null));
		mContext = null;
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	public static boolean checkHasCurrentGrp(Context context) {
		if (!mHasPttGrp) {
			// MyToast.showToast(true, context, "您不在任何组");
		}
		return mHasPttGrp;
	}

	public String ShowPttStatus(PttGrp.E_Grp_State pttState) {
		switch (pttState) {
		// houyuchun modify 20120620 begin
		case GRP_STATE_SHOUDOWN:
			return this.getResources().getString(R.string.status_close);
		case GRP_STATE_IDLE:
			return this.getResources().getString(R.string.status_free);
		case GRP_STATE_TALKING:
			return this.getResources().getString(R.string.status_speaking);
		case GRP_STATE_LISTENING:
			return this.getResources().getString(R.string.status_listening);
		case GRP_STATE_QUEUE:
			return this.getResources().getString(R.string.status_waiting);
		}
		return this.getResources().getString(R.string.status_error);
	}

	public String ShowSpeakerStatus(String str, String userNum) {
		if ((str == null) || str.equals("")) {
			return this.getResources().getString(R.string.none_speaker);
		} else if (userNum.equals(Settings.getUserName()) && isPttPressing) {
			return str + this.getResources().getString(R.string.self_speaker);
			/*
			 * Receiver.GetCurUA(). user_profile.username
			 */

		} else {
			return str;
		}
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // TODO Auto-generated method stub
	// menu.add(0, 1, 0, "退出").setIcon(R.drawable.exit);
	// return super.onCreateOptionsMenu(menu);
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case 1:
	// // sendBroadcast(new Intent("com.zed3.sipua.exitActivity").putExtra(
	// // "exit", true));
	// LocationOverlayDemo.this.finish();
	// Tools.exitApp(SipUAApp.mContext);
	// break;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	private void ShowCurrentGrp() {
		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		if (pttGrp != null) {

			currentGrpNum = pttGrp.grpID;
			// if(mySpin!=null)
			// mySpin.setSelection(findPos(pttGrp, mGroups));
			if (Receiver.GetCurUA().GetCurGrp() != null) {
				title.setText(Receiver.GetCurUA().GetCurGrp().grpName);
			}
			if (isRefresh_ != 1) {
				GroupListUtil.getDataCurrentGroupList();
			}
			mGroupListsMap = GroupListUtil.getGroupListsMap();
			arrayList = mGroupListsMap.get(pttGrp);
		}
		isRefresh_ = 1;
	}

	private void getAllGrpGisInfo(final String groupNumber) {
		if (!NetChecker.check(mContext, false))
			return;
		new Thread() {
			@Override
			public void run() {
				Message msg = new Message();
				String result = null;
				// Log.e("guojunfeng2013", "thread running");
				String nameSpace = "http://schemas.xmlsoap.org/soap/encoding/";
				String methodName = "QueryPttGis";
				String ip = SipUAApp.mContext.getSharedPreferences(
						Settings.sharedPrefsFile, Context.MODE_PRIVATE)
						.getString(Settings.PREF_SERVER,
								Settings.DEFAULT_SERVER);
				// String url = "http://"+ip+":81/nusoap/IGis.php";//
				String url;
				if (DeviceInfo.http_port.equals("")) {
					url = "http://" + ip + "/nusoap/IGis.php";
				} else {
					url = "http://" + ip + ":" + DeviceInfo.http_port
							+ "/nusoap/IGis.php";
				}
				MyLog.e("LocationOverlayDemo", "url=" + url);
				SoapObject rpc = new SoapObject(nameSpace, methodName);

				rpc.addProperty("AuthUser", "admin");
				rpc.addProperty("AuthPwd", "admin");
				rpc.addProperty("PttGroupNum", groupNumber);
				rpc.addProperty("PageSize", 100);
				rpc.addProperty("PageNum", 1);

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);

				envelope.bodyOut = rpc;
				envelope.dotNet = true;
				envelope.setOutputSoapObject(rpc);
				HttpTransportSE transport = new HttpTransportSE(url);
				try {
					transport.call(null, envelope);
					Object object = envelope.getResponse();
					result = object.toString();
					Log.e("guojunfeng2013", "result is..." + result);
				} catch (Exception e) {
					// Log.e("guojunfeng2013", "webService exception..." + e);
					// e.printStackTrace();
				}
				Log.e("guojunfeng2013", "end");
				if (result == null || result.length() == 0) {
					msg.what = 4;
					mHandler.sendMessage(msg);
					Log.e("guojunfeng2013", "result is null or empty...");
				} else if (result.contains("{")) {
					jsonTag = result;
					msg.what = 3;
					mHandler.sendMessage(msg);
					Log.e("guojunfeng2013", "AuthFailed");
				} else if (result.equals("AuthFailed")) {
					msg.what = 4;
					mHandler.sendMessage(msg);
					Log.e("guojunfeng2013", "AuthFailed");
				} else if (result.equals("NoSuchGroup")) {
					msg.what = 4;
					mHandler.sendMessage(msg);
					Log.e("guojunfeng2013", "NoSuchGroup");
				} else if (result.equals("NoData")) {
					msg.what = 4;
					mHandler.sendMessage(msg);
					Log.e("guojunfeng2013", "NoData");
				} else if (result.equals("OtherReason")) {
					msg.what = 4;
					mHandler.sendMessage(msg);
					Log.e("guojunfeng2013", "OtherReason");
				}
			}
		}.start();
	}

	public class MyAdapter extends BaseAdapter {
		private PttGrps pttGrps;
		private Context mContext;

		@Override
		public int getCount() {
			if (pttGrps.GetCount() == 0) {
				return 1;
			}
			return pttGrps.GetCount();
		}

		public MyAdapter(Context ctx, PttGrps pttGrps) {
			this.pttGrps = pttGrps;
			this.mContext = ctx;
			if (pttGrps == null) {

			}
		}

		@Override
		public Object getItem(int position) {
			if (pttGrps == null || pttGrps.GetCount() < 1) {
				return "不在任何组";
			}
			return "切换";
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
			convertView = _LayoutInflater.inflate(R.layout.spinner_item, null);
			if (convertView != null) {
				TextView _TextView1 = (TextView) convertView
						.findViewById(R.id.item);
				_TextView1.setText(pttGrps.GetGrpByIndex(position).grpName);
			}
			return convertView;

		}

	}

	@Override
	public void pressPTT(boolean down) {
		if (down) {
			if (!NetChecker.check(mContext, true)) {
				return;
			}
			if (!checkHasCurrentGrp(mContext)) {
				return;
			}
		}
		mPttHandler.post(down ? pttDownRunable : pttUpRunable);
	}

	// 构建Runnable对象，在runnable中更新界面
	Runnable pttDownRunable = new Runnable() {
		@Override
		public void run() {
			// 更新界面
			isPttPressing = true;
			setPttBackground(true);
			UserAgent ua = Receiver.GetCurUA();
			if (ua != null) {
				ua.OnPttKey(true);
			} else {
				com.zed3.log.MyLog.e(TAG, "pttDownRunable ,ua = null");
			}
		}

	};
	// 构建Runnable对象，在runnable中更新界面
	Runnable pttUpRunable = new Runnable() {
		@Override
		public void run() {
			// 更新界面
			isPttPressing = false;
			setPttBackground(false);
			UserAgent ua = Receiver.GetCurUA();
			if (ua != null) {
				ua.OnPttKey(false);
			} else {
				com.zed3.log.MyLog.e(TAG, "pttUpRunable ,ua = null");
			}
		}

	};

	public static void setPttBackground(boolean pressed) {
		pttkeyMap.setBackgroundResource(pressed ? R.drawable.ptt_down_map
				: drawable.ptt_up_map);
	}

	public static LocationOverlayDemo getInstance() {
		return mContext;
	}

	public Handler pttPressHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				isPttPressing = false;
				setPttBackground(false);
				break;
			case 1:
				isPttPressing = true;
				setPttBackground(true);
				break;

			default:
				break;
			}
		};
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == 181) {
			if (!NetChecker.check(mContext, true)) {
				return false;
			}
			pttkeyMap.setBackgroundResource(R.drawable.ptt_down_map);
			GroupCallUtil.makeGroupCall(true, true);
			isPttPressing = true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == 181) {
			if (!NetChecker.check(mContext, true)) {
				return false;
			}
			if (isPttPressing) {
				GroupCallUtil.makeGroupCall(false, true);
				isPttPressing = false;
				pttkeyMap.setBackgroundResource(R.drawable.ptt_up_map);
			}
		}

		return super.onKeyUp(keyCode, event);
	}
}
/**
 * 
 * 
 * @author hejin
 * 
 */
// class MyLocationMapView extends MapView{
// static PopupOverlay pop = null;//
// Context ctx;
// public MyLocationMapView(Context context) {
// super(context);
// ctx = context;
// }
// public MyLocationMapView(Context context, AttributeSet attrs){
// super(context,attrs);
// ctx = context;
// }
// public MyLocationMapView(Context context, AttributeSet attrs, int defStyle){
// super(context, attrs, defStyle);
// ctx = context;
// }
// @Override
// public boolean onTouchEvent(MotionEvent event){
// if (!super.onTouchEvent(event)){
// //消隐泡泡
// if (pop != null && event.getAction() == MotionEvent.ACTION_UP){
// pop.hidePop();
// Toast.makeText(ctx,"test", Toast.LENGTH_SHORT).show();
// }
// }
// return true;
// }
// }

