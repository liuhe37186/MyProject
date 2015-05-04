package com.zed3.sipua.ui.lowsdk;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.zoolu.tools.GroupListInfo;
import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.BluetoothSCOStateReceiver;
import com.zed3.bluetooth.OnBluetoothAdapterStateChangedListener;
import com.zed3.bluetooth.OnSppConnectStateChangedListener;
import com.zed3.bluetooth.PTTListener;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.dialog.DialogUtil;
import com.zed3.dialog.DialogUtil.DialogCallBack;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.location.MemoryMg;
import com.zed3.media.mediaButton.MediaButtonReceiver;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrp.E_Grp_State;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.baiduMap.LocationOverlayDemo;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.ActvityNotify;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.BaseVisualizerView;
import com.zed3.utils.LineUpdateListener;
import com.zed3.utils.LoadingAnimation;
import com.zed3.utils.LogUtil;
import com.zed3.utils.MyHandler;
import com.zed3.utils.ReceiveLineListener;
import com.zed3.utils.Tools;

public class TalkBackNew extends BaseActivity implements OnClickListener,
		Comparator<GroupListInfo>, LineUpdateListener, ReceiveLineListener,
		PTTListener, OnSppConnectStateChangedListener,
		OnBluetoothAdapterStateChangedListener {
	private ImageView myphoto;
	private Timer timer1;
	private Timer timer2;
	private LinearLayout new_music;
	private Boolean isNeedMenberList = true;
	private ImageView new_down_up;
	boolean isChangeMemaber = false;
	private String TAG = "TalkBackNew";
	public static TalkBackNew mtContext;
	private int groupBodyMumber;
	private String groupOnlineBodyMumber;
	public static int isRefresh = 1;
	private View group_set_button;
	public static boolean isPttPressing;
	private TextView new_member_text;
	// private TextView tv_group_others;
	private String currentGroupNumber;
	private IntentFilter mFilter;
	private IntentFilter intentfilter2;
	private View new_open_close;
	static boolean mHasPttGrp;
	public boolean isPTTUseful = false;
	private TextView group_name_title;;
	private static ImageView group_button_ptt;
	private static TextView group_button_text;
	private ListView group_name_list;
	private ListView group_member_list;
	private TextView tv_group_status;
	private TextView tv_group_speaker;
	// private ImageView aa_open_list;
	// private ImageView aa_close_list;
	private MyGroupNameAdapter mGroupNameAdapter;
	private MyGroupMemberAdapter mGroupMemberAdapter;
	ArrayList<GroupListInfo> arrayList;
	public static final String ACTION_GETSTATUS_MESSAGE = "com.zed3.sipua.ui_groupstatelist";
	public boolean isStarted;
	// private ImageView group_back_button;
	private final String ACTION_GROUP_STATUS = "com.zed3.sipua.ui_groupcall.group_status";
	private final String ACTION_ALL_GROUP_CHANGE = "com.zed3.sipua.ui_groupcall.all_groups_change";
	// One group to another
	private final String ACTION_GROUP_2_GROUP = "com.zed3.sipua.ui_groupcall.group_2_group";
	// One group to another
	private final String ACTION_SINGLE_2_GROUP = "com.zed3.sipua.ui_groupcall.single_2_group";
	// Receive text message
	private final String ACTION_RECEIVE_TEXT_MESSAGE = "com.zed3.sipua.ui_receive_text_message";
	// Receive text message
	private final String ACTION_SEND_TEXT_MESSAGE_FAIL = "com.zed3.sipua.ui_send_text_message_fail";
	// 接受到强制关闭"摇毙"的广播
	private final String ACTION_SEND_TEXT_MESSAGE_SUCCEED = "com.zed3.sipua.ui_send_text_message_succeed";
	private final String ACTION_SEND_TEXT_MESSAGE_TIMEOUT = "com.zed3.sipua.ui_send_text_message_timeout";
	//英文版
	private  String mStatus ="";//我的状态
	// add by wlei 2014-10-22 解决对讲组人数过多时，调用排序方法会出现异常退出的问题
	private Collator collator = Collator.getInstance(java.util.Locale.CHINA);
	private boolean isGroupChange = true;//add by wlei 2014-10-22 切换对讲组，需要更新成员列表
	// 代表当前对讲组的变量，用于判断当前组是否已经改变   // add by lwang 2014-10-24
	private PttGrp mLastPttGrp = null;
	// 流量预警
	PttGrps pttGrps = null/* Receiver.GetCurUA().GetAllGrps() */;
	public final String ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
	private final String ACTION_DESTORY_MESSAGE = "com.zed3.sipua.ui_destory_message";
	private HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap;
	private int pttkeycode = 0;
	private SharedPreferences mypre = null;
	
	/**
	 * delay to set current group for dalog showing. add by lwang 2014-10-23
	 */
	private Handler setCurrentGroupHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
//			final PttGrp grpByIndex = pttGrps.GetGrpByIndex(position);
			final PttGrp grpByIndex = (PttGrp) msg.obj;
			final UserAgent ua = Receiver.GetCurUA();
			if (ua != null) {
				PttGrp curGrp = ua.GetCurGrp();
				// add by lwang 2014-11-06
				long delay = 0;
				if (curGrp != null && grpByIndex != null
						&& curGrp != grpByIndex) {
//					mSwitchGroupProcessDailog = DialogUtil.showProcessDailog(TalkBackNew.this, "正在切换对讲组  "+grpByIndex.grpName);
					ua.SetCurGrp(grpByIndex);
					delay = 5000;
				}else {
					mSwitchGroupProcessDailog = DialogUtil.showProcessDailog(TalkBackNew.this, getResources().getString(R.string.switch_failed));
					delay = 2000;
				}
				// 切换对讲组的提示对框不消失问题   。 modify by lwang 2014-11-06
				Message disMsg = dismissDialogHandler.obtainMessage();
				msg.what = 1;
				dismissDialogHandler.sendMessageDelayed(disMsg, delay);
			}
		}
	};
	
	//need to sort . add by lwang 2014-10-25
	protected boolean mNeedReSort;
	private LoadingAnimation mLoadingAnimation;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			StringBuilder builder = new StringBuilder("mHandler#handleMessage() what "+msg.what);
			super.handleMessage(msg);
			//网络正常时，直接获取当前组信息。
			if (!NetChecker.check(mtContext, false)) {
				builder.append(" bad network ignore");
				LogUtil.makeLog(TAG, builder.toString());
				return;
			}
			switch (msg.what) {
			case 1:
				//增加时间保护，默认是5秒，少于5秒，延迟发送新消息以便更新。
				long needDelay = 5000;
				if (msg.arg1 != 0) {
					needDelay = msg.arg1;
				}
				builder.append(" needDelay "+needDelay);
				// 解决30S不刷新数据的问题。 modify by lwang 
//				int time = (int) (System.currentTimeMillis() - mLastGetGropMemberMessagesTime);
				long time = System.currentTimeMillis() - mLastGetGropMemberMessagesTime;
				builder.append(" mLastGetGropMemberMessagesTime is "+mLastGetGropMemberMessagesTime+" time "+time);
				time = needDelay - time;
				if (time > 0) {
					sendEmptyMessageDelayed(1, time);
					builder.append(" send delay message to getCurrentGrpMemberMessages delay "+time);
					break;
				} 
				getCurrentGrpMemberMessages();
				break;
			case 2:
				pttGrps = Receiver.GetCurUA().GetAllGrps();
				if (mGroupNameAdapter != null) {
					mGroupNameAdapter.refreshNameList(pttGrps);
					mGroupNameAdapter.notifyDataSetChanged();
					builder.append(" mGroupNameAdapter.notifyDataSetChanged()");
				}
//				if (pttGrps.GetCount() == 0) {
//				}else {
//					builder.append(" pttGrps.GetCount() != 0 ignore");
//				}
				break;
			}
			LogUtil.makeLog(TAG, builder.toString());
		}

	};
	/**
	 * dismissDialog of switch group add by oumogang 2014-03-22
	 */
	Handler dismissDialogHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				dismissMyDialog(mSwitchGroupProcessDailog);
				mSwitchGroupProcessDailog = null;
				break;
			case 2:
				dismissMyDialog(mSppConnectProcessDialog);
				mSppConnectProcessDialog = null;
				break;

			default:
				break;
			}
		};
	};
	//解决多次反注册mReceiver，导致程序异常退出的问题 。add by lwang 2014-11-24
	private boolean isRegister = false;
	public void unregisterPttGroupChangedReceiver(){
		if(isRegister){
			unregisterReceiver(mReceiver);
			isRegister = true;
			LogUtil.makeLog(TAG, " unregisterPttGroupChangedReceiver() ");
		}
	}
	PttGrp.E_Grp_State mLastPttGrpState = null;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		// Receive GroupInfo to Update Activity
		StringBuilder builder = new StringBuilder();
		@Override
		public void onReceive(Context mtContext, Intent intent) {
			if (builder.length()>0) {
				builder.delete(0, builder.length());
			}
			builder.append("mReceiver#onReceive()");
			Bundle extras = intent.getExtras();
			// dismiss dialog of switch group,add by oumogang 2014-03-22
			if (mSwitchGroupProcessDailog != null) {
				builder.append("dissmiss mSwitchGroupProcessDailog");
				Message msg = dismissDialogHandler.obtainMessage();
				msg.what = 1;
				dismissDialogHandler.sendMessageDelayed(msg, 600);
			}
			if (intent.getAction().equalsIgnoreCase(ACTION_GROUP_STATUS)) {
				
				stopCurrentAnimation();
				
				builder.append(" ACTION_GROUP_STATUS");
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
				
				builder.append(" speaker["+userNum+","+speaker+"]");
				PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
				// modify by lwang 2014-10-25
				isGroupChange = getGroupChangeState();
//				ShowCurrentGrp();
				pttGrps = getGrps();
				// if (pttGrps != null) {
				// updateGroupLists();
				// }

				// 是否允许刷新组列表
				mHasPttGrp = pttGrp != null ? true : false;

				if (pttGrp != null) {
					builder.append(" "+pttGrp.toString());
					//need to refresh the memberList . modify lwang 2014-11-06
					if (isGroupChange) {
						LogUtil.makeLog(TAG, " isGroupChange is true");
						onCurrentGrpChanged();
						if (!isNeedMenberList) {
							onCurrentGrpMemberMessagesChanged();
						}
					}
					//修改speaker 信息与对讲状态不一致的问题； add by mou 2014-10-27
					if (pttGrp.state == PttGrp.E_Grp_State.GRP_STATE_IDLE || pttGrp.state == PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN ) {
						userNum = "";
						speaker = "";
					}
					//修改处理多次空闲消息的问题； add by mou 2014-10-27
					if (pttGrp.state == mLastPttGrpState && pttGrp.state == PttGrp.E_Grp_State.GRP_STATE_IDLE && !isGroupChange) {
						builder.append(" GRP_STATE_IDLE pttGrp.state == mLastPttGrpState ignore");
						LogUtil.makeLog(TAG, builder.toString());
						return;
					}
					//如果对讲组列表，没有被选择的，没有当前组，需要更新一下。
					if (mGroupNameAdapter.isCurGrpNull) {
						builder.append(" MyGroupNameAdapter#isCurGrpNull is true");
						Message msg = new Message();
						msg.what = 2;
						mHandler.sendMessage(msg);
					}
					// mTextGroupNum.setText("");
					// tv_group_status.setText("");
					// tv_group_speaker.setText("");
					tv_group_status.setText(mStatus+getResources().getString(R.string.status_none));
					tv_group_speaker.setText(R.string.talking_none);

					tv_group_speaker
							.setText(ShowSpeakerStatus(speaker, userNum));
					
					if(pttGrp.state==E_Grp_State.GRP_STATE_INITIATING) {
						
						tv_group_status.setText(mStatus
								+ ShowPttStatus(pttGrp.state));
						
						stopCurrentAnimation();
						
						mLoadingAnimation = new LoadingAnimation();
						mLoadingAnimation.setAppendCount(3).startAnimation(tv_group_status);
						
					} else {
						stopCurrentAnimation();
						
						tv_group_status.setText(mStatus
								+ ShowPttStatus(pttGrp.state));
					}
					
					
					// // houyuchun modify 20120620 end
					currentGroupNumber = pttGrp.grpID;

					// add by oumogang 2013-10-28
					// need not set it,disable by oumogang 2013-12-03
					if (pttGrp.state == PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN
							|| pttGrp.state == PttGrp.E_Grp_State.GRP_STATE_IDLE) {
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								mBaseVisualizerView.setTimes(-1);
							}
						}).start();
					}
				} else {
					mBaseVisualizerView.setTimes(-1);
					tv_group_status.setText(mStatus+getResources().getString(R.string.status_none));
					tv_group_speaker.setText(R.string.talking_none);
				}
				mLastPttGrpState = mLastPttGrp.state;
			} else if (intent.getAction()
					.equalsIgnoreCase(ACTION_GROUP_2_GROUP)) {
				builder.append(" ACTION_GROUP_2_GROUP");
				Bundle bundle = intent.getExtras();
				GroupCallUtil.setTalkGrp(bundle.getString("0"));
				GroupCallUtil.setActionMode(ACTION_GROUP_2_GROUP);
				Intent startActivity = new Intent();
				startActivity.setClass(Receiver.mContext, ActvityNotify.class);
				startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				SipUAApp.getAppContext().startActivity(startActivity);

			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_SINGLE_2_GROUP)) {
				builder.append(" ACTION_SINGLE_2_GROUP");
				Bundle bundle = intent.getExtras();
				GroupCallUtil.setTalkGrp(bundle.getString("0"));
				GroupCallUtil.setActionMode(ACTION_SINGLE_2_GROUP);

				if (!UserAgent.isCamerPttDialog) {
					Intent startActivity = new Intent();
					startActivity.setClass(Receiver.mContext,
							ActvityNotify.class);
					startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SipUAApp.getAppContext().startActivity(startActivity);
				} else {// 往cameracall页面发送对讲组切换广播
					sendBroadcast(new Intent("com.zed3.sipua.camera_ptt_dialog"));
				}

			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_RECEIVE_TEXT_MESSAGE)) {
				builder.append(" ACTION_RECEIVE_TEXT_MESSAGE");
				// 此处接受消息接受了两次以上
				// 获得消息内容
				Bundle bundle = intent.getExtras();
				// String strFrom = bundle.getString("0");
				// String strTo = bundle.getString("1");
				String strSeq = bundle.getString("2");
				// String strContent = bundle.getString("3");
				// String strSipName = bundle.getString("4");

				if (!strSeq.equals(MemoryMg.getInstance().LastSeq)) {
					Intent broadCast = new Intent(
							MessageDialogueActivity.RECEIVE_TEXT_MESSAGE);
					mtContext.sendBroadcast(broadCast);
					builder.append(" ACTION_SEND_TEXT_MESSAGE_FAIL");
				}

			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_SEND_TEXT_MESSAGE_FAIL)) {
				builder.append(" ACTION_SEND_TEXT_MESSAGE_FAIL");
				String mE_id = intent.getStringExtra("0");
				Intent broadCast = new Intent(
						MessageDialogueActivity.SEND_TEXT_FAIL);
				broadCast.putExtra("0", mE_id);
				mtContext.sendBroadcast(broadCast);
			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_SEND_TEXT_MESSAGE_SUCCEED)) {
				builder.append(" ACTION_SEND_TEXT_MESSAGE_SUCCEED");
				String mE_id = intent.getStringExtra("0");
				// guojunfenging
				Intent broadCast = new Intent(
						MessageDialogueActivity.SEND_TEXT_SUCCEED);
				broadCast.putExtra("0", mE_id);
				mtContext.sendBroadcast(broadCast);
			}// ”摇毙“
			else if (intent.getAction().equalsIgnoreCase(
					ACTION_SEND_TEXT_MESSAGE_TIMEOUT)) {
				builder.append(" ACTION_SEND_TEXT_MESSAGE_TIMEOUT");
				String mE_id = intent.getStringExtra("E_id");
				Intent broadCast = new Intent(
						MessageDialogueActivity.SEND_TEXT_TIMEOUT);
				broadCast.putExtra("0", mE_id);
				mtContext.sendBroadcast(broadCast);

			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_DESTORY_MESSAGE)) {
				builder.append(" ACTION_DESTORY_MESSAGE");
				// 关闭sharedPreference的onchangedListener事件
				MemoryMg.getInstance().IsChangeListener = false;

				// 清空sharedpreference里的用户名和密码
				Editor edit = PreferenceManager.getDefaultSharedPreferences(
						mtContext).edit();
				edit.putString(Settings.PREF_USERNAME, "");// 用户名
				edit.putString(Settings.PREF_PASSWORD, "");// 密码
				edit.putString(Settings.PREF_SERVER, "");// 服务器地址
				edit.commit();

				// 彻底退出
				// GroupCallActivity.QuitMessage();
				Tools.exitApp(mtContext);
			} else if (intent.getAction().equalsIgnoreCase(
					Contants.ACTION_NEWWORK_CHANGED)) {
				builder.append(" ACTION_NEWWORK_CHANGED");
				LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip2);
				if (intent.getIntExtra(Contants.NETWORK_STATE, -1) == Contants.NETWORK_STATE_GOOD) {
					ll.setVisibility(View.GONE);
				} else {
					ll.setVisibility(View.VISIBLE);
					isRefresh++;
				}
			}
			// 确定切换组列表，是否需要更新；
			else if (intent.getAction().equalsIgnoreCase(
					Contants.ACTION_CURRENT_GROUP_CHANGED)) {
				builder.append(" ACTION_CURRENT_GROUP_CHANGED");
				LogUtil.makeLog(TAG, " ACTION_CURRENT_GROUP_CHANGED onCurrentGrpMemberMessagesChanged()");
				//need not reget all groups list after change current group. mofify by mou 2014-11-04
//				GroupListUtil.getData4GroupList();
			} else if (intent.getAction().equalsIgnoreCase(ACTION_3GFlow_ALARM)) {
				builder.append(" ACTION_3GFlow_ALARM");
				Tools.FlowAlertDialog(TalkBackNew.this);
			} else if (intent.getAction().equalsIgnoreCase(
					ACTION_ALL_GROUP_CHANGE)) {
				builder.append(" ACTION_ALL_GROUP_CHANGE");
				isRefresh = 2;
				LogUtil.makeLog(TAG, " ACTION_ALL_GROUP_CHANGE onCurrentGrpMemberMessagesChanged()");
//				ShowCurrentGrp();
				//reget all groups list when groups changed. add by mou 2014-11-04
				GroupListUtil.getData4GroupList();
			}else if (intent.getAction().equals(AudioUtil.ACTION_STREAM_CHANGED)) {
				int stream = extras.getInt(AudioUtil.KEY_STREAM_INT);
				switch (stream) {
				case AudioManager.STREAM_MUSIC:
					setVolumeControlStream(AudioManager.STREAM_MUSIC);
					break;
				case AudioManager.STREAM_VOICE_CALL:
					setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
					break;

				default:
					break;
				}
			}
			LogUtil.makeLog(TAG, builder.toString());
		}
	};
	private BroadcastReceiver groupListReceiver = new BroadcastReceiver() {

		// String tag = "groupListReceiver";

		@Override
		public void onReceive(Context mtContext, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(Contants.ACTION_CLEAR_GROUPLIST)) {
				GroupListUtil.removeDataOfGroupList();
				//有待测试确定
//				ShowCurrentGrp();
				LogUtil.makeLog(TAG, " ShowCurrentGrp() groupListReceiver ACTION_CLEAR_GROUPLIST +1");
			}
			if (!isStarted) {
				return;
			}
			if (action.equals(Contants.ACTION_GROUPLIST_UPDATE_OVER)) {
				// mGroupNameAdapter.notify();
//				ShowCurrentGrp();
				LogUtil.makeLog(TAG, "ACTION_GROUPLIST_UPDATE_OVER  onCurrentGrpMemberMessagesChanged()");
				onCurrentGrpMemberMessagesChanged();
			}

		}
	};
	// should not init handler by static but init in main thread modify by
	// oumogang 2014-04-28
	/* static */Handler noGroupToastHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// mNoGroupDialog.setVisibility(View.GONE);
		};
	};
	public static boolean isResume;
	// 为pttDownRunnable处理事情；
	Handler progressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// hideLoadingProgress();
		};
	};
	// bluetooth for ptt,add by oumogang 2014-03-06
	private Handler mPttHandler;
	private TextView bluetoothOnoffBt;
	private TextView bluetoothModeOnoffBt;
	private TextView hookModeOnoffBt;
	private TextView speakerModeOnoffBt;
	protected ProgressDialog mSwitchGroupProcessDailog;
	public static boolean mIsCreate;

	private View mRootView;
	private long mLastGetGropMemberMessagesTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// GroupListUtil.getData4GroupList();
		lineListener = TalkBackNew.this;
		mtContext = this;
		// setSppConnectStateListener，add by oumogang 2014-03-27
		ZMBluetoothManager.getInstance().setSppConnectStateListener(this);
		BluetoothSCOStateReceiver
				.setOnBluetoothAdapterStateChangedListener(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mRootView = getLayoutInflater().inflate(R.layout.aa_new, null);
		setContentView(mRootView);
		mStatus =  getResources().getString(R.string.my_status);
		mRootView.setOnClickListener(this);
		// tv_group_others = (TextView) findViewById(R.id.tv_group_others);
		// linear_ptt = findViewById(R.id.linear_ptt);
		// linear_list = findViewById(R.id.linear_list);
		myphoto = (ImageView) findViewById(R.id.myphoto);
		// myphoto.setOnClickListener(this);
		group_name_list = (ListView) findViewById(R.id.new_group_name_list);
		group_name_list.setVerticalScrollBarEnabled(false);
		group_member_list = (ListView) findViewById(R.id.new_group_member_list);
		group_member_list.setVerticalScrollBarEnabled(true);
		// group_back_button = (ImageView) findViewById(R.id.group_back_button);
		// group_back_button.setOnClickListener(this);
		// GroupListUtil.addGroupList2Contacts();
		tv_group_status = (TextView) findViewById(R.id.new_tv_group_status);
		tv_group_speaker = (TextView) findViewById(R.id.new_tv_group_speaker);
		group_set_button = findViewById(R.id.new_group_gps);
		if (DeviceInfo.CONFIG_SUPPORT_PTTMAP)// 定位对讲 显示 隐藏
			group_set_button.setVisibility(View.VISIBLE);
		else
			group_set_button.setVisibility(View.INVISIBLE);

		group_set_button.setOnClickListener(this);
		group_set_button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView ImageView = (ImageView) findViewById(R.id.t_add);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					ImageView.setBackgroundResource(R.color.btn_click_bg);
					ImageView.setImageResource(R.drawable.icon_location_press);
					// tv.setTextColor(Color.RED);
					break;
				case MotionEvent.ACTION_UP:
					ImageView.setBackgroundResource(R.color.whole_bg);
					// tv.setTextColor(getResources().getColor(R.color.font_color3));
					ImageView
							.setImageResource(R.drawable.icon_loaction_release);
					break;
				}
				return false;
			}
		});
		new_open_close = findViewById(R.id.new_open_close);
		new_open_close.setOnClickListener(this);
		new_down_up = (ImageView) findViewById(R.id.new_down_up);
		new_member_text = (TextView) findViewById(R.id.new_member_text);
		new_music = (LinearLayout) findViewById(R.id.new_music);
		initMusicLine();
		group_button_ptt = (ImageView) findViewById(R.id.new_group_button_ptt);
//		group_button_text = (TextView) findViewById(R.id.textView1);
		
		group_name_title = (TextView) findViewById(R.id.new_group_name_title);
		group_button_ptt.setOnTouchListener(new OnTouchListener() {

			// private boolean needLog = true;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (!NetChecker.check(mtContext, true)) {
						break;
					}
					if (!checkHasCurrentGrp(mtContext)) {
						break;
					}
					isPttPressing = true;
//					lineListener = TalkBackNew.this;
					MyLog.e("hst", "onTouch... down ");
					// GroupCallActivity.pressPTT(true);
					setPttBackground(true);
					GroupCallUtil.makeGroupCall(true, false);
					break;
				case MotionEvent.ACTION_UP:
					setPttBackground(false);
					MyLog.e("guojunfeng20140222", "up1");
					if (!NetChecker.check(mtContext, true)) {
						break;
					}
					if (!checkHasCurrentGrp(mtContext)) {
						break;
					}
					isPttPressing = false;
//					lineListener = null;
					MyLog.e("hst", "onTouch... up ");
					// GroupCallActivity.pressPTT(false);
					stopCurrentAnimation();
					PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
					if(pttGrp!=null){
						if(pttGrp.state==E_Grp_State.GRP_STATE_INITIATING){
							tv_group_status.setText(mStatus
									+ ShowPttStatus(E_Grp_State.GRP_STATE_IDLE));
						}
					}
					GroupCallUtil.makeGroupCall(false, false);
					mBaseVisualizerView.setTimes(-1);
					break;
				}

				return true;
			}
		});
		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		// 赋初始值 add by lwang 2014-10-24
		mLastPttGrp = pttGrp;
		// if (pttGrp != null) {
		// if (pttGrp.speaker != null && pttGrp.speaker.length() > 0) {
		// tv_group_speaker.setText(pttGrp.speaker);
		// currentGroupNumber = pttGrp.grpID;
		// } else {
		// tv_group_speaker.setText("无");
		// }
		// // tv_group_status.setText(pttGrp.);
		// arrayList = mGroupListsMap.get(pttGrp);
		// // mGroupMemberAdapter = new MyGroupMemberAdapter(mContext,
		// arrayList);
		// // group_member_list.setAdapter(mGroupMemberAdapter);
		// group_name_title.setText(pttGrp.grpName);
		// }
		if (pttGrp != null) {
			currentGroupNumber = pttGrp.grpID;
			// mTextGroupNum.setText(resources.getString(R.string.current_group)
			// + pttGrp.grpName);
			group_name_title.setText(pttGrp.grpName);
			tv_group_speaker.setText(ShowSpeakerStatus(pttGrp.speaker,
					pttGrp.speakerN));
			tv_group_status.setText(mStatus
					+ ShowPttStatus(pttGrp.state));

			// houyuchun modify 20120620 end
		} else {
			// 不在任何组
			group_name_title.setText(R.string.ptt);
			currentGroupNumber = "";
			tv_group_status.setText(mStatus+getResources().getString(R.string.status_none));
			tv_group_speaker.setText(R.string.talking_none);
		}
		resetGroupNameTitle(pttGrp);
		pttGrps = Receiver.GetCurUA().GetAllGrps();
		 updateGroupLists();
		mGroupNameAdapter = new MyGroupNameAdapter(mtContext, pttGrps);
		group_name_list.setAdapter(mGroupNameAdapter);
		mGroupNameAdapter.notifyDataSetChanged();
		mGroupMemberAdapter = new MyGroupMemberAdapter(mtContext, arrayList);
		group_member_list.setAdapter(mGroupMemberAdapter);
		// group_member_list.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View v, int position,
		// long arg3) {
		// // TODO Auto-generated method stub
		// mGroupMemberAdapter.setPositon(position);
		// mGroupMemberAdapter.notifyDataSetChanged();
		// }
		// });
		group_name_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v,
					final int position, long arg3) {
				if (!NetChecker.check(mtContext, true)) {
					MyToast.showToast(true, mtContext, R.string.network_exception);
					return;
				}
				// if(Receiver.GetCurUA().GetCurGrp()!=pttGrps.GetGrpByIndex(position)){
				// Receiver.GetCurUA().SetCurGrp(pttGrps.GetGrpByIndex(position));
				// }
				/**
				 * android.os.NetworkOnMainThreadException should't use main
				 * thread to send message,modify by oumogang 2014-03-07
				 */
				final PttGrp grpByIndex = pttGrps.GetGrpByIndex(position);
				final UserAgent ua = Receiver.GetCurUA();
				if (ua != null) {
					PttGrp curGrp = ua.GetCurGrp();

					if (curGrp != null && grpByIndex != null
							&& curGrp != grpByIndex) {
						if (isPttPressing) {
							MyToast.showToast(true, mtContext, R.string.release_ptt_and_try_again);
							return;
						}
						isGroupChange = true;//add by wlei 2014-10-22 切换对讲组，需要更新成员列表
						mSwitchGroupProcessDailog = DialogUtil
								.showProcessDailog(TalkBackNew.this,
										getResources().getString(R.string.group_switching) +" "+ grpByIndex.grpName);
//						ua.SetCurGrp(grpByIndex);
						Message msg = setCurrentGroupHandler.obtainMessage();
						msg.obj = grpByIndex;
						setCurrentGroupHandler.sendMessageDelayed(msg, 200);

					}
				}

				// update after group changed.  modify by lwang 2014-10-24
//				mGroupNameAdapter.notifyDataSetChanged();
//				Message msg = new Message();
//				msg.what = 1;
//				mHandler.sendMessage(msg);
//				LogUtil.makeLog(TAG, " mHandler group_name_list 点击对讲组列表切换对讲组");
//				Log.e(TAG, "GroupRefresh timer1 run");
			}
		});

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
		mFilter.addAction(ACTION_DESTORY_MESSAGE);// 摇毙命令
		mFilter.addAction(Contants.ACTION_CURRENT_GROUP_CHANGED);//
		mFilter.addAction(ACTION_3GFlow_ALARM);
		mFilter.addAction(AudioUtil.ACTION_STREAM_CHANGED);
		mtContext.registerReceiver(mReceiver, mFilter);
		// add by lwang 2014-11-24
		isRegister = true;
		if (intentfilter2 == null) {
			intentfilter2 = new IntentFilter();
			intentfilter2.addAction(ACTION_GETSTATUS_MESSAGE);
			intentfilter2.addAction(Contants.ACTION_GROUPLIST_UPDATE_OVER);//

			intentfilter2.addAction(Contants.ACTION_GROUPLIST_CLEAR_OVER);//
			intentfilter2.addAction(Contants.ACTION_CLEAR_GROUPLIST);//
		}
		mtContext.registerReceiver(groupListReceiver, intentfilter2);
		//need not update group message by this timer. modify by lwang 2014-10-25
//		timer2 = new Timer();
//		TimerTask tt = new TimerTask() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				Message msg = new Message();
//				msg.what = 2;
//				mHandler.sendMessage(msg);
//			}
//		};
//		timer2.schedule(tt, 3000, 3000);

		// bluetooth for ptt,add by oumogang 2014-03-06
		bluetoothOnoffBt = (TextView) findViewById(R.id.bluetooth_onoff_bt);
		bluetoothOnoffBt.setOnClickListener(this);
		bluetoothOnoffBt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(
						android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, 4);
				intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, 3);
				return true;
			}
		});
		// bluetoothMode for ptt,add by oumogang 2014-03-06
		bluetoothModeOnoffBt = (TextView) findViewById(R.id.mode_bluetooth_onoff_bt);
		bluetoothModeOnoffBt.setOnClickListener(this);
		bluetoothModeOnoffBt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(
						android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, 4);
				intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, 3);
				return true;
			}
		});
		bluetoothModeOnoffBt.setVisibility(View.GONE);
		// hookMode for ptt,add by oumogang 2014-03-06
		hookModeOnoffBt = (TextView) findViewById(R.id.mode_hook_onoff_bt);
		hookModeOnoffBt.setOnClickListener(this);
		hookModeOnoffBt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		// speakerMode for ptt,add by oumogang 2014-03-06
		speakerModeOnoffBt = (TextView) findViewById(R.id.mode_speaker_onoff_bt);
		speakerModeOnoffBt.setOnClickListener(this);
		speakerModeOnoffBt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		String t = Settings.PREF_BLUETOOTH_ONOFF;
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			if (!ZMBluetoothManager.getInstance().isDeviceSupportBluetooth()) {
				DialogUtil.showCheckDialog(TalkBackNew.this, getResources().getString(R.string.information),
						getResources().getString(R.string.bluetooth_notify), getResources().getString(R.string.ok_know));
				Settings.mNeedBlueTooth = false;
				bluetoothOnoffBt.setVisibility(View.GONE);
			}
			boolean on = ZMBluetoothManager.getInstance()
					.getZMBluetoothOnOffState(mtContext);
			reInitBluetoothButton(on);
			if (on) {
				ZMBluetoothManager.getInstance().connectZMBluetooth(
						getApplicationContext());
			}
		}
		// 创建属于主线程的handler
		mPttHandler = new Handler();

		pttGrps = getGrps();
		if (pttGrps != null) {
			updateGroupLists();
		}
		if (groupOnlineBodyMumber != null) {
			if (isChangeMemaber) {
				new_member_text.setText("(" + groupOnlineBodyMumber + "/"
						+ groupBodyMumber + ")");
			} else {
				new_member_text.setText("(" + groupBodyMumber + ")");
			}
		}
		MyHandler.setHandler(myHandler);
		receiveListener = this;
		super.onCreate(savedInstanceState);
		mIsCreate = true;
//		mAudioMode = AudioUtil.getInstance().getCustomMode(
//				AudioUtil.TYPE_GROUPCALL);
//		if (mAudioMode == AudioUtil.MODE_BLUETOOTH) {
//			if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
//				ZMBluetoothManager.getInstance()
//						.askUserToSelectHeadSetBluetooth();
//			}
//		} else {
//			AudioUtil.getInstance().setAudioConnectMode(mAudioMode);
//		}

		speakerModeOnoffBt.setVisibility(View.GONE);
		hookModeOnoffBt.setVisibility(View.GONE);

		String grpID = PreferenceManager
				.getDefaultSharedPreferences(mtContext).getString("grpID", "");
		
		/*
		 * 检测文件中是否存在对讲组信息，
		 * 若有则设置当前对讲组为文件中保存的对讲组，若无则为默认
		 */
		if(!TextUtils.isEmpty(grpID)){
			
			UserAgent userAgent = Receiver.GetCurUA();
			
			PttGrp targetPttGrp = userAgent.GetGrpByID(grpID);
			
			PttGrp curGrp = userAgent.GetCurGrp();
			
			if (curGrp != null && targetPttGrp != null
					&& curGrp != targetPttGrp) {
				userAgent.SetCurGrp(targetPttGrp);
			}
			
		}
		
		if (!NetChecker.check(this, false)) {
			LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip2);
			ll.setVisibility(View.VISIBLE);
		} else {
			LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip2);
			ll.setVisibility(View.GONE);
		}
	}

	private void resetGroupNameTitle(PttGrp pttGrp) {
		// TODO Auto-generated method stub
		if (pttGrp != null) {
			group_name_title.setText(pttGrp.grpName);
		} else {
			group_name_title.setText(R.string.ptt);
		}
	}

	/**
	 * 请求当前组成员信息。 add by lwang 2014-10-25
	 */
	protected  void getCurrentGrpMemberMessages() {
		// TODO Auto-generated method stub
		LogUtil.makeLog("TalkBackNew ", " getCurrentGrpMemberMessages()");
		mLastGetGropMemberMessagesTime = System.currentTimeMillis();
		isGroupChange = true;
		GroupListUtil.getDataCurrentGroupList();
	}
	/**
	 * 组切换后，更新组名等信息。 add by lwang 2014-10-25
	 */
	protected  void onCurrentGrpChanged() {
		// TODO Auto-generated method stub
//		if(!isGroupChange) return;
//		isGroupChange = false;
		PttGrp pttGrp = mLastPttGrp;
		Tools.saveGrpID(mLastPttGrp.grpID);
		if (pttGrp != null) {
			mGroupListsMap = GroupListUtil.getGroupListsMap();
			group_name_title.setText(pttGrp.grpName);
			updateOnlineGroups(pttGrp);
		} else {
			group_name_title.setText(R.string.ptt);
		}
		if (mGroupNameAdapter != null) {
			mGroupNameAdapter.refreshNameList(pttGrps);
			mGroupNameAdapter.notifyDataSetChanged();
		}
		
		//重新请求当前组成员信息。
//		mHandler.sendEmptyMessage(1);
		Message msg = mHandler.obtainMessage();
		msg.what = 1;
		msg.arg1 = 500;
		mHandler.sendMessage(msg);
	}
	/**
	 * 接收到服务器返回的当前组信息后，对成员列表排序。 add by lwang 2014-10-25
	 */
	protected void onCurrentGrpMemberMessagesChanged() {
		// TODO Auto-generated method stub
//		if(!isGroupChange) return;
//		isGroupChange = false;
		StringBuilder builder = new StringBuilder(" onCurrentGrpMemberMessagesChanged() ");
		PttGrp pttGrp = mLastPttGrp;
		if (pttGrp != null) {
			updateOnlineGroups(pttGrp);
			arrayList = mGroupListsMap.get(pttGrp);
			//解决arrayList为空时的异常退出  add by lwang 2014-10-30
			if(arrayList == null){
				builder.append(" arrayList is null return");
				LogUtil.makeLog(TAG, builder.toString());
				return ;
			}
			// 因为排序是在子线程中进行的，所以可能会出现arrayList被置为空的情况。 add by lwang 2014-10-29
			try {
				final ArrayList<GroupListInfo> list = (ArrayList<GroupListInfo>) arrayList.clone();
				LogUtil.makeLog(TAG, builder.toString());
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						sort(list);
					}
				}).start();
			} catch (Exception e) {
				// TODO: handle exception
				builder.append(" Exceptioned when arrayList.clone()!");
				LogUtil.makeLog(TAG, builder.toString());
			}
		} else {
			group_name_title.setText(R.string.ptt);
		}
	}

	/**
	 * dismiss dialog ,add by oumogang 2014-03-22
	 */
	protected void dismissMyDialog(ProgressDialog dialog) {
		// TODO Auto-generated method stub
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	BaseVisualizerView mBaseVisualizerView;
	private boolean flag;
	public static boolean mIsBTServiceStarted;

	private void initMusicLine() {
		mBaseVisualizerView = new BaseVisualizerView(this);
		mBaseVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		new_music.addView(mBaseVisualizerView);
	}

	/**
	 * check HasCurrentGrp and show state add by oumogang 2013-12-03
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkHasCurrentGrp(Context context) {
		// TODO Auto-generated method stub
		if (!mHasPttGrp) {
			MyToast.showToast(true, context, R.string.no_groups);
		}
		return mHasPttGrp;
	}

	@Override
	public void onStart() {
		// Receiver.engine(mContext).registerMore();
		isStarted = true;

		mypre = getSharedPreferences("com.zed3.sipua_preferences",
				Activity.MODE_PRIVATE);
		String pttkey = mypre.getString("pttkey", "140");
		if (!pttkey.equals(""))
			pttkeycode = Integer.parseInt(pttkey);

		// if (groupListStatusViews.getVisibility() == View.VISIBLE) {
		// GroupListUtil.getData4GroupList();
		// }
		MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
		super.onStart();
//		ShowCurrentGrp();
//		LogUtil.makeLog(TAG, " ShowCurrentGrp() onStart +1");
		// add by hdf336
		// if(Receiver.engine(this).isRegistered())
		// Receiver.engine(this).register();

	}

	@Override
	public void onResume() {
		StringBuilder builder = new StringBuilder("TalkBackNew.onResume()");
		// mContext.registerReceiver(recv, mFilter);
		isResume = true;
		Receiver.engine(mtContext);
		if (timer1 == null && isChangeMemaber) {
			timer1 = new Timer();
			Log.e(TAG, "GroupRefresh timer1 start");
			TimerTask timerT1 = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					pttGrps = getGrps();
					if (pttGrps != null) {
						Message msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
						LogUtil.makeLog(TAG, " mHandler onResume Timer1");
					}
					Log.e(TAG, "GroupRefresh timer1 run");
				}
			};
			timer1.schedule(timerT1, 0, 30000);

		}
		//need not update group message by this timer. modify by lwang 2014-10-25
//		if (timer2 == null) {
//			timer2 = new Timer();
//			TimerTask tt = new TimerTask() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					Message msg = new Message();
//					msg.what = 2;
//					mHandler.sendMessage(msg);
//				}
//			};
//			timer2.schedule(tt, 3000, 3000);
//		}
		super.onResume();
//		ShowCurrentGrp();
//		builder.append(" ShowCurrentGrp()");
		
		//control by ACTION_NEWWORK_CHANGED broadcast recever. modify by mou 2014-12-24
//		if (!NetChecker.check(this, false)) {
//			builder.append(" NetChecker.check() needwork disconnected");
//			LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip2);
//			ll.setVisibility(View.VISIBLE);
//		} else {
//			builder.append(" NetChecker.check() needwork connected");
//			LinearLayout ll = (LinearLayout) findViewById(R.id.net_tip2);
//			ll.setVisibility(View.GONE);
//		}
		// getData4GroupList();

		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		// add by oumogang 2013-05-24
		// 是否允许刷新组列表
		mHasPttGrp = pttGrp != null ? true : false;
		// isResume = true;
		setPttBackground(isPttPressing);

		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			mIsBTServiceStarted = ZMBluetoothManager.getInstance()
					.isSPPConnected();
			reInitBluetoothButton(mIsBTServiceStarted);
		}
		bluetoothOnoffBt.setVisibility(Settings.mNeedBlueTooth ? View.VISIBLE
				: View.GONE);

		bluetoothModeOnoffBt
				.setVisibility((!mIsBTServiceStarted && BluetoothSCOStateReceiver.isBluetoothAdapterEnabled) ? View./* VISIBLE */GONE
						: View.GONE);
		LogUtil.makeLog(TAG, builder.toString());
	}

	// bluetooth for ptt,add by oumogang 2014-03-06
	public void reInitBluetoothButton(boolean isBTServiceStarted) {
		// TODO Auto-generated method stub
		Log.i(TAG, "isBTServiceStarted = " + isBTServiceStarted);
		mIsBTServiceStarted = isBTServiceStarted;
		bluetoothOnoffBt.setBackgroundColor(Color
				.parseColor(isBTServiceStarted ? "#FFbe0a0b" : "#FF565759"));
		bluetoothOnoffBt.setText(isBTServiceStarted ? getResources().getString(R.string.disconnect_hm)
				: getResources().getString(R.string.connect_hm));
	}

	// audio mode for ptt,add by oumogang 2014-03-20
//	public void reInitModeButtons(int mode) {
//		// TODO Auto-generated method stub
//		mAudioMode = mode;
//		switch (mode) {
//		case AudioUtil.MODE_BLUETOOTH:
//			// Toast.makeText(getApplicationContext(),"蓝牙模式已启动",0).show();
//			break;
//		case AudioUtil.MODE_HOOK:
//			// Toast.makeText(getApplicationContext(),"听筒模式已启动",0).show();
//			break;
//		case AudioUtil.MODE_SPEAKER:
//			// Toast.makeText(getApplicationContext(),"扬声器模式已启动",0).show();
//			break;
//
//		default:
//			return;
//		}
//		setAudioMode(mode);
//		bluetoothModeOnoffBt.setBackgroundColor(Color
//				.parseColor((mode == AudioUtil.MODE_BLUETOOTH) ? "#FFbe0a0b"
//						: "#FF565759"));
//		hookModeOnoffBt.setBackgroundColor(Color
//				.parseColor((mode == AudioUtil.MODE_HOOK) ? "#FFbe0a0b"
//						: "#FF565759"));
//		speakerModeOnoffBt.setBackgroundColor(Color
//				.parseColor((mode == AudioUtil.MODE_SPEAKER) ? "#FFbe0a0b"
//						: "#FF565759"));
//	}

	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		if (timer1 != null) {
			timer1.cancel();
			timer1 = null;
		}
		if (timer2 != null) {
			timer2.cancel();
			timer2 = null;
		}
		isResume = false;
		isStarted = false;
		LogUtil.makeLog(TAG, "onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (timer1 != null) {
			timer1.cancel();
			timer1 = null;
		}
		if (timer2 != null) {
			timer2.cancel();
			timer2 = null;
		}
		try {
			if (mFilter != null){
				mtContext.unregisterReceiver(mReceiver);
				// add by lwang 2014-11-24
				isRegister = false;
				LogUtil.makeLog(TAG, " onDestroy() and mtContext.unregisterReceiver(mReceiver)");
			}else
				MyLog.i("GroupCallActivity",
						"recv unregister fail! mFilter is null. ");

			if (intentfilter2 != null)
				mtContext.unregisterReceiver(groupListReceiver);
			else
				MyLog.i("GroupCallActivity",
						"groupListReceiver unregister fail! intentfilter2 is null. ");

		} catch (Exception e) {
			MyLog.i("GroupCallActivity",
					"unregisterReceiver fail: " + e.toString());
		}
		MyHandler.setHandler(null);
		lineListener = null;
		receiveListener = null;
		mtContext = null;
		mIsCreate = false;
		ZMBluetoothManager.getInstance().removeSppConnectStateListener(this);
		BluetoothSCOStateReceiver
				.reMoveOnBluetoothAdapterStateChangedListener(this);
		super.onDestroy();
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
		if (pttkeycode != 0 && keyCode == pttkeycode) {
			if (!NetChecker.check(mtContext, true)) {
				return false;
			}
			if (!checkHasCurrentGrp(mtContext)) {
				return false;
			}
			isPttPressing = true;
			lineListener = TalkBackNew.this;
			GroupCallUtil.makeGroupCall(true, false);
			setPttBackground(true);
		} else if (keyCode == 181) {
			if (!NetChecker.check(mtContext, true)) {
				return false;
			}
			if (!checkHasCurrentGrp(mtContext)) {
				return false;
			}
			setPttBackground(true);
			isPttPressing = true;
			lineListener = TalkBackNew.this;
			GroupCallUtil.makeGroupCall(true, false);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (pttkeycode != 0 && keyCode == pttkeycode) {
			if (!NetChecker.check(mtContext, true)) {
				return false;
			}
			if (!checkHasCurrentGrp(mtContext)) {
				return false;
			}
			isPttPressing = false;
//			lineListener = null;
			GroupCallUtil.makeGroupCall(false, false);
			setPttBackground(false);
			mBaseVisualizerView.setTimes(-1);
		} else if (keyCode == 181) {
			if (!NetChecker.check(mtContext, true)) {
				return false;
			}
			if (!checkHasCurrentGrp(mtContext)) {
				return false;
			}
			isPttPressing = false;
//			lineListener = null;
			setPttBackground(false);
			GroupCallUtil.makeGroupCall(false, false);
			mBaseVisualizerView.setTimes(-1);
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.new_open_close:
			if (isNeedMenberList) {

				new_member_text.setVisibility(View.VISIBLE);
				new_down_up.setImageDrawable(getResources().getDrawable(
						R.drawable.new_up));
				group_member_list.setVisibility(View.VISIBLE);
				isNeedMenberList = false;
				isChangeMemaber = true;
				//need to refresh memberList . add by lwang 2014-11-06
				onCurrentGrpMemberMessagesChanged();
				if (timer1 == null) {
					timer1 = new Timer();
					TimerTask timerT1 = new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Message msg = new Message();
							msg.what = 1;
							mHandler.sendMessage(msg);
							LogUtil.makeLog(TAG, "GroupRefresh timer1 start");
						}
					};
					timer1.schedule(timerT1, 0, 30000);
				}
				if (groupOnlineBodyMumber != null) {
					new_member_text.setText("(" + groupOnlineBodyMumber + "/"
							+ groupBodyMumber + ")");
				}
			} else {
				if (groupOnlineBodyMumber != null) {
					new_member_text.setText("(" + groupBodyMumber + ")");
				}
				isChangeMemaber = false;
				if (timer1 != null) {
					timer1.cancel();
					timer1 = null;
				}
				isNeedMenberList = true;
				new_down_up.setImageDrawable(getResources().getDrawable(
						R.drawable.new_down));
				group_member_list.setVisibility(View.GONE);
			}
			break;
		case R.id.new_group_gps:
			// ShowCurrentGrp();
			
		/*	if (pttGrps == null || pttGrps.GetCount() == 0) {
				MyToast.showToast(true, mtContext, R.string.no_groups);
				return;
			}
			Intent intent = new Intent(mtContext, LocationOverlayDemo.class);
			startActivity(intent);*/
			ComponentName componentName = new ComponentName("com.wrxb.plot",
					"com.wrxb.plot.MainActivity");
			Intent intent = new Intent("start");
			intent.setComponent(componentName);
			startActivity(intent);
			// if(!SplashActivity.isAudio){
			// finish();
			// }
			break;

		// bluetooth for ptt,add by oumogang 2014-03-06
		case R.id.bluetooth_onoff_bt:
			if (!mIsBTServiceStarted) {
				reInitBluetoothButton(true);
				// 开启蓝牙手咪
				ZMBluetoothManager.getInstance().connectZMBluetooth(
						SipUAApp.mContext);
				ZMBluetoothManager.getInstance()
						.saveZMBluetoothOnOffState(true);
			} else {
				DialogCallBack callBack;
				DialogUtil.showSelectDialog(TalkBackNew.this, getResources().getString(R.string.disconnect_hm),
						getResources().getString(R.string.disconnect_hm_notify), getResources().getString(R.string.disconnect),
						new DialogCallBack() {

							@Override
							public void onPositiveButtonClick() {
								// TODO Auto-generated method stub
								ZMBluetoothManager.getInstance()
										.disConnectZMBluetooth(
												SipUAApp.mContext);
								if (ZMBluetoothManager.getInstance()
										.isBluetoothAdapterEnabled()) {
									ZMBluetoothManager.getInstance()
											.askUserToDisableBluetooth();
								}
								// ZMBluetoothManager.getInstance().disConnectSPP();
								reInitBluetoothButton(false);
								ZMBluetoothManager.getInstance().mNeedAskUserToReconnectSpp = false;
								ZMBluetoothManager.getInstance()
										.saveZMBluetoothOnOffState(false);
							}

							@Override
							public void onNegativeButtonClick() {
								// TODO Auto-generated method stub

							}
						});

			}
			break;
		// hook mode for ptt,add by oumogang 2014-03-20
		case R.id.mode_hook_onoff_bt:
//			if (mAudioMode == AudioUtil.MODE_HOOK) {
//				return;
//			}
//			mAudioMode = AudioUtil.MODE_HOOK;
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.opening_re_mode), 0).show();
//			reInitModeButtons(mAudioMode);
//			AudioUtil.getInstance().setAudioConnectMode(mAudioMode);
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.re_mode_opened), 0).show();
//			AudioUtil.getInstance().setVolumeControlStream(TalkBackNew.this);
			break;
		// speaker mode for ptt,add by oumogang 2014-03-20
		case R.id.mode_speaker_onoff_bt:
//			if (mAudioMode == AudioUtil.MODE_SPEAKER) {
//				return;
//			}
//			mAudioMode = AudioUtil.MODE_SPEAKER;
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.opening_sp_mode), 0).show();
//			reInitModeButtons(mAudioMode);
//			AudioUtil.getInstance().setAudioConnectMode(mAudioMode);
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.sp_mode_opened), 0).show();
//			AudioUtil.getInstance().setVolumeControlStream(TalkBackNew.this);
			break;
		// bluetooth mode for ptt,add by oumogang 2014-03-20
		case R.id.mode_bluetooth_onoff_bt:
			// if (mAudioMode == AudioUtil.MODE_BLUETOOTH) {
			// return;
			// }
			//
			// startBluetoothAudioMode();
//			if (mAudioMode == AudioUtil.MODE_BLUETOOTH) {
//				stopBluetoothAudioMode();
//			} else {
//				startBluetoothAudioMode();
//			}
//			AudioUtil.getInstance().setVolumeControlStream(TalkBackNew.this);
			break;
		}

	}

//	public void startBluetoothAudioMode() {
//		// TODO Auto-generated method stub
//		mLastAudioMode = mAudioMode;
//		mAudioMode = AudioUtil.MODE_BLUETOOTH;
//		if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
//			ZMBluetoothManager.getInstance().enableAdapter();
//			ZMBluetoothManager.getInstance().askUserToSelectHeadSetBluetooth();
//		} else {
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.opening_bl_mode), 0).show();
//			reInitModeButtons(mAudioMode);
//			AudioUtil.getInstance().setAudioConnectMode(mAudioMode);
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.bl_mode_opened), 0).show();
//			AudioUtil.getInstance().setVolumeControlStream(TalkBackNew.this);
//		}
//	}

//	public void stopBluetoothAudioMode() {
//		// TODO Auto-generated method stub
//		mLastAudioMode = mAudioMode;
//		mAudioMode = AudioUtil.MODE_SPEAKER;
//		Toast.makeText(getApplicationContext(), getResources().getString(R.string.closing_bl_mode), 0).show();
//		reInitModeButtons(mAudioMode);
//		AudioUtil.getInstance().setAudioConnectMode(mAudioMode);
//		Toast.makeText(getApplicationContext(), getResources().getString(R.string.bl_mode_off), 0).show();
//		AudioUtil.getInstance().setVolumeControlStream(TalkBackNew.this);
//	}

	public class MyGroupNameAdapter extends BaseAdapter {
		private Context context_;

		private String groupName;
		private LayoutInflater layoutInflater;

		private PttGrps pttGrps;

		private int length;

		private boolean isCurGrpNull = true;

		public MyGroupNameAdapter(Context context, PttGrps pttGrps) {
			this.context_ = context;

			layoutInflater = LayoutInflater.from(context_);

			this.pttGrps = pttGrps;
		}

		public void refreshNameList(PttGrps pttGrps) {
			this.pttGrps = pttGrps;
		}

		// 得到总的数量
		public int getCount() {
			// TODO Auto-generated method stub
			return this.pttGrps != null ? this.pttGrps.GetCount() : 0;
		}

		// 根据ListView位置返回View
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return this.pttGrps.GetGrpByIndex(position);
		}

		// 根据ListView位置得到List中的ID
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			super.notifyDataSetChanged();
			StringBuilder builder = new StringBuilder("MyGroupNameAdapter#notifyDataSetChanged()");
			UserAgent ua = Receiver.GetCurUA();
			if (ua == null) {
				builder.append(" GetCurUA() is null");
				LogUtil.makeLog(TAG, builder.toString());
				return;
			}
			PttGrp curGrp = ua.GetCurGrp();
			if (curGrp == null) {
				isCurGrpNull  = true;
				builder.append(" GetCurGrp is null");
				LogUtil.makeLog(TAG, builder.toString());
				return;
			}
			// 防止当前组为空的时候，一直执行notifyDataSetChanged()。add by lwang 2014-11-04
			isCurGrpNull  = false;
			resetGroupNameTitle(curGrp);
			LogUtil.makeLog(TAG, builder.toString());
		}

		// 根据位置得到View对象
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = layoutInflater.inflate(
						R.layout.aa_list_item_group_name, null);
			}
			TextView tv1 = (TextView) convertView
					.findViewById(R.id.aa_list_item_groupname);
			if (pttGrps == null) {
				length = 0;
				return null;
			} else {
				
				length = pttGrps.GetCount();
			}
			int a = group_name_list.getHeight();
			if (length >= 3) {
				tv1.setHeight(a / 3);
			} else if (length == 2) {
				tv1.setHeight(a / 2);
			} else if (length == 1) {
				tv1.setHeight(a);
			}
			if (pttGrps != null
					&& Receiver.GetCurUA() != null
					&& Receiver.GetCurUA().GetCurGrp() != null
					&& Receiver.GetCurUA().GetCurGrp().grpID == pttGrps
							.GetGrpByIndex(position).grpID) {
				// convertView.setBackgroundColor(R.color.font_color);
				convertView.setBackgroundResource(R.color.font_color);
				tv1.setTextColor(getResources().getColor(R.color.black));
			} else {
				convertView.setBackgroundResource(R.color.font_color2);
				tv1.setTextColor(getResources().getColor(R.color.white));
			}

			// int a = mlist.size();
			// for(int i=0;i<mlist.size();i++){
			// if(mlist.get(i).GrpState.trim().equals("离线")){
			// a--;
			// }
			// }
			groupName = pttGrps.GetGrpByIndex(position).grpName;
			if (length > 2) {
				if (groupName.length() > 4) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + "...";
					// im1.setVisibility(View.VISIBLE);
				} else if (groupName.length() == 4) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3);
				} else if (groupName.length() == 3) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2);
				} else if (groupName.length() == 2) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1);
				}
			} else if (length == 2) {
				if (groupName.length() > 6) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + "...";
					// im1.setVisibility(View.VISIBLE);
				} else if (groupName.length() == 6) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5);
				} else if (groupName.length() == 5) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4);
				} else if (groupName.length() == 4) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3);
				} else if (groupName.length() == 3) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2);
				} else if (groupName.length() == 2) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1);
				}
			} else if (length == 1) {
				if (groupName.length() > 12) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + groupName.charAt(6) + "\n"
							+ groupName.charAt(7) + "\n" + groupName.charAt(8)
							+ "\n" + groupName.charAt(9) + "\n"
							+ groupName.charAt(10) + "\n"
							+ groupName.charAt(11) + "\n" + "...";
					// im1.setVisibility(View.VISIBLE);
				} else if (groupName.length() == 12) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + groupName.charAt(6) + "\n"
							+ groupName.charAt(7) + "\n" + groupName.charAt(8)
							+ "\n" + groupName.charAt(9) + "\n"
							+ groupName.charAt(10) + "\n"
							+ groupName.charAt(11);
				} else if (groupName.length() == 11) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + groupName.charAt(6) + "\n"
							+ groupName.charAt(7) + "\n" + groupName.charAt(8)
							+ "\n" + groupName.charAt(9) + "\n"
							+ groupName.charAt(10);
				}

				else if (groupName.length() == 10) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + groupName.charAt(6) + "\n"
							+ groupName.charAt(7) + "\n" + groupName.charAt(8)
							+ "\n" + groupName.charAt(9);
				} else if (groupName.length() == 9) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + groupName.charAt(6) + "\n"
							+ groupName.charAt(7) + "\n" + groupName.charAt(8);
				} else if (groupName.length() == 8) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + groupName.charAt(6) + "\n"
							+ groupName.charAt(7);
				} else if (groupName.length() == 7) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5)
							+ "\n" + groupName.charAt(6);
				} else if (groupName.length() == 6) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4) + "\n" + groupName.charAt(5);
				} else if (groupName.length() == 5) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3) + "\n"
							+ groupName.charAt(4);
				} else if (groupName.length() == 4) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2)
							+ "\n" + groupName.charAt(3);
				} else if (groupName.length() == 3) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1) + "\n" + groupName.charAt(2);
				} else if (groupName.length() == 2) {
					groupName = groupName.charAt(0) + "\n"
							+ groupName.charAt(1);
				}
			}
			tv1.setText(groupName);
			// if(mPosition==position&&Receiver.GetCurUA().GetCurGrp().grpID==list.get(position).grpID){
			// convertView.setBackgroundResource(R.drawable.background_sytle3);
			// }else{
			// convertView.setBackgroundResource(R.drawable.background_sytle2);
			// }
			return convertView;
		}
	}

	public class MyGroupMemberAdapter extends BaseAdapter implements
			View.OnClickListener {
		private Context context_;
		private String mAddress;
		private LayoutInflater layoutInflater;

		ArrayList<GroupListInfo> list;

		private int mPosition = -1;
		private int left;

		public MyGroupMemberAdapter(Context context,
				ArrayList<GroupListInfo> list) {
			this.context_ = context;

			layoutInflater = LayoutInflater.from(context_);

			this.list = list;
		}

		public void refreshList(ArrayList<GroupListInfo> list) {
			this.list = list;
		}

		public void setPositon(int pos) {
			mPosition = pos;
		}

		// 得到总的数量
		public int getCount() {
			// TODO Auto-generated method stub
			return this.list != null ? this.list.size() : 0;
		}

		// 根据ListView位置返回View
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return this.list.get(position);
		}

		// 根据ListView位置得到List中的ID
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		// 根据位置得到View对象
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				convertView = layoutInflater.inflate(
						R.layout.aa_list_item_group_member, null);
			}
			convertView.setBackgroundColor(getResources().getColor(
					R.color.black_));
			TextView tv1 = (TextView) convertView
					.findViewById(R.id.member_list_name);
			// TextView tv2 = (TextView) convertView
			// .findViewById(R.id.member_list_status);
			ImageView im = (ImageView) convertView
					.findViewById(R.id.member_list_video_call);
			im.setTag(position);

			ImageView voiceBtn = (ImageView) convertView
					.findViewById(R.id.call_voice_btn);
			ImageView msgBtn = (ImageView) convertView
					.findViewById(R.id.call_msg_btn);
			voiceBtn.setTag(position);
			msgBtn.setTag(position);
			LinearLayout line_sub = (LinearLayout) convertView
					.findViewById(R.id.line_sub);
			LinearLayout line_sub2 = (LinearLayout) convertView
					.findViewById(R.id.line_sub2);
			// ImageView im_ = (ImageView)
			// convertView.findViewById(R.id.member_list_audio_call);
			if (list.get(position).GrpState.equals(getResources().getString(R.string.the_status_1))) {
				im.setImageDrawable(getResources().getDrawable(
						R.drawable.ptt_videonormal));
				voiceBtn.setImageDrawable(getResources().getDrawable(
						R.drawable.ptt_voice));
				msgBtn.setImageDrawable(getResources().getDrawable(
						R.drawable.list_message_btn));
				//成员在线状态改变时，改变成员名字体颜色 。 add by lwang 2014-11-25
				tv1.setTextColor(getResources().getColor(R.color.notOnLine));
			} else {
				im.setImageDrawable(getResources().getDrawable(
						R.drawable.ptt_video));
				voiceBtn.setImageDrawable(getResources().getDrawable(
						R.drawable.ptt_voicepress));
				msgBtn.setImageDrawable(getResources().getDrawable(
						R.drawable.list_message_btn_down));
				//成员在线状态改变时，改变成员名字体颜色 。 add by lwang 2014-11-25
				tv1.setTextColor(getResources().getColor(R.color.onLine));
			}
			if (list.get(position).GrpName.trim().length() == 0) {
				tv1.setText(list.get(position).GrpNum);
			} else {
				tv1.setText(list.get(position).GrpName);
			}
			// tv1.setText(list.get(position).GrpName);
			// tv2.setText(list.get(position).GrpState);
			im.setOnClickListener(this);
			msgBtn.setOnClickListener(this);
			// im_.setOnClickListener(this);
			// if(mPosition == position){
			// convertView.setBackgroundResource(R.drawable.group_member_selected);
			// if(SplashActivity.isVideo){
			// tv2.setVisibility(View.GONE);
			// im.setVisibility(View.VISIBLE);
			// }else{
			// im.setVisibility(View.GONE);
			// tv2.setVisibility(View.VISIBLE);
			// }
			// if(SplashActivity.isGsm){
			// im_.setVisibility(View.VISIBLE);
			// }else{
			// im_.setVisibility(View.GONE);
			// }
			// } else {
			// convertView.setBackgroundColor(0);
			// im.setVisibility(View.GONE);
			// im_.setVisibility(View.GONE);
			// tv2.setVisibility(View.VISIBLE);
			// }
			left = 0;
			if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
				line_sub.setVisibility(View.GONE);
				im.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
				line_sub2.setVisibility(View.GONE);
				voiceBtn.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (!DeviceInfo.CONFIG_SUPPORT_IM) {
				msgBtn.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (left == 1) {
				line_sub2.setVisibility(View.GONE);
				line_sub.setVisibility(View.GONE);
			}
			if (DeviceInfo.CONFIG_SUPPORT_VIDEO
					&& DeviceInfo.CONFIG_SUPPORT_AUDIO
					&& !DeviceInfo.CONFIG_SUPPORT_IM) {
				line_sub2.setVisibility(View.GONE);
			}
			// 语音
			voiceBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String number = list.get((Integer) v.getTag()).GrpNum;
					if (number == null) {
						DialogUtil.showCheckDialog(TalkBackNew.this, getResources().getString(R.string.information),
								getResources().getString(R.string.number_not_exist), getString(R.string.ok_know));
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
								CallUtil.makeAudioCall(mtContext, number, null);
							else {
								Intent intent = new Intent(Intent.ACTION_CALL,
										Uri.parse("tel:" + number));
								startActivity(intent);
							}
						} else {// 手动
							if (MemoryMg.getInstance().PhoneType == 1)
								CallUtil.makeAudioCall(mtContext, number, null);
							else {
								Intent intent = new Intent(Intent.ACTION_CALL,
										Uri.parse("tel:" + number));
								startActivity(intent);
							}
						}

					}
				}
			});

			// im_.setTag(position);
			return convertView;

		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.member_list_video_call) {
				int a = (Integer) v.getTag();
				mAddress = list.get(a).GrpNum;
				CallUtil.makeVideoCall(mtContext, mAddress, null);
			} else if (v.getId() == R.id.call_msg_btn) {
				Intent intent = new Intent(mtContext,
						MessageDialogueActivity.class);
				intent.putExtra(MessageDialogueActivity.USER_NAME,
						list.get((Integer) v.getTag()).GrpName);
				intent.putExtra(MessageDialogueActivity.USER_NUMBER,
						list.get((Integer) v.getTag()).GrpNum);

				startActivity(intent);

			}
			// if(v.getId()==R.id.member_list_audio_call){
			// int a = (Integer) v.getTag();
			// mAddress = list.get(a).GrpNum;
			// Intent intent = new
			// Intent(Intent.ACTION_CALL,Uri.parse("tel:"+mAddress));
			// startActivity(intent);
			// }
		}
	}

	public String ShowPttStatus(PttGrp.E_Grp_State pttState) {
		switch (pttState) {
		// houyuchun modify 20120620 begin
		case GRP_STATE_SHOUDOWN:
			return this.getResources().getString(R.string.close);
		case GRP_STATE_IDLE:
			return this.getResources().getString(R.string.idle);
		case GRP_STATE_TALKING:
			return this.getResources().getString(R.string.talking);
		case GRP_STATE_LISTENING:
			return this.getResources().getString(R.string.listening);
		case GRP_STATE_QUEUE:
			return this.getResources().getString(R.string.queueing);
		case GRP_STATE_INITIATING:
			return this.getResources().getString(R.string.ptt_requesting);
			
		}
		return this.getResources().getString(R.string.error);
	}

	public String ShowSpeakerStatus(String str, String userNum) {
		if ((str == null) || str.equals("")) {
			return getResources().getString(R.string.talking_none);
		} else if (userNum.equals(Settings.getUserName())/* &&isPttPressing */) {
			return getResources().getString(R.string.talking_me);
			/*
			 * Receiver.GetCurUA(). user_profile.username
			 */

		} else {
			return getResources().getString(R.string.talking_someOne) + "（" + str + "）";
		}
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // TODO Auto-generated method stub
	// menu.add(0, 1, 0, "退出").setIcon(R.drawable.exit);
	// return super.onCreateOptionsMenu(menu);
	// }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Tools.exitApp(this);
			TalkBackNew.this.finish();
			// sendBroadcast(new
			// Intent("com.zed3.sipua.exitActivity").putExtra("exit", true));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//add by wlei 2014-10-15 单独开了一个线程，对对讲组列表进行排序，解决对讲组成员过多时，出现卡顿现象的问题
    private Handler sortHandler = new Handler(){
    	
    	
    	@Override
    	public void handleMessage(Message msg) {
    		if(msg.what == 1){
    			//refresh in updateMemberList() . modify by wlei 
//    			if (mGroupMemberAdapter != null) {
//    				mGroupMemberAdapter.refreshList(arrayList);
//    				mGroupMemberAdapter.notifyDataSetChanged();
//    			}
    			ArrayList<GroupListInfo> list = (ArrayList<GroupListInfo>) msg.obj;
    			updateMemberList(list);
    		}
    		
    	};
    };
    
    /**
     * 判断当前对讲组，是否改变 。 add by lwang 2014-10-25
     */
    private boolean getGroupChangeState(){
    	PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		if(pttGrp!=null&&mLastPttGrp!=pttGrp){
			mLastPttGrp = pttGrp;
			return true;
		}
		return false;
    }
    
    /**
	 * 刷新当前组成员列表。 add by wlei 2014-10-23
	 */
	private synchronized void updateMemberList(ArrayList<GroupListInfo> list){
    	if (mGroupMemberAdapter != null) {
			mGroupMemberAdapter.refreshList(list);
			mGroupMemberAdapter.notifyDataSetChanged();
		}
    }
    /**
	 * 当前组成员列表重新排序。 add by wlei 2014-10-23
	 */
    private synchronized void sort(ArrayList<GroupListInfo> list){
    	if(list==null) {
    		LogUtil.makeLog(TAG, "sort() list = null return   ");
    		return;
    	}
    	StringBuffer builder = new StringBuffer("sort(list.size "+list.size()+") begin");
    	LogUtil.makeLog(TAG, builder.toString());
		long time2 = System.currentTimeMillis();
		ArrayList<GroupListInfo> arrayList3 = null;
		ArrayList<GroupListInfo> arrayList2 = sortArrayList(list);
	    arrayList3 = sortOnline(arrayList2);
	    builder.append(" needtime "+(System.currentTimeMillis()-time2));
		Message message = new Message();
		message.what = 1;
		message.obj = arrayList3;
		sortHandler.sendMessage(message);
		builder.append(" end");
		LogUtil.makeLog(TAG, builder.toString());
    }
	/**
	 * 获取当前组成员列表，计算组人数及在线人数。 add by wlei 2014-10-23
	 */
	private synchronized void updateOnlineGroups(PttGrp pttGrp) {
		StringBuffer builder = new StringBuffer("updateOnlineGroups()");
		long time2 = System.currentTimeMillis();
		builder.append(" begin");
		LogUtil.makeLog(TAG, builder.toString());
		mGroupListsMap = GroupListUtil.getGroupListsMap();
		arrayList = mGroupListsMap.get(pttGrp);
		if (arrayList != null && arrayList.size() > 0) {
			groupBodyMumber = arrayList.size();
			int x = arrayList.size();
			for (int i = 0; i < arrayList.size(); i++) {
				if (arrayList.get(i).GrpState.equals(getResources().getString(
						R.string.the_status_1))) {
					x = x - 1;
				}
			}
			groupOnlineBodyMumber = x + "";
		}
		if (groupOnlineBodyMumber != null) {
			if (isChangeMemaber) {
				new_member_text.setText("(" + groupOnlineBodyMumber + "/"
						+ groupBodyMumber + ")");
			} else {
				new_member_text.setText("(" + groupBodyMumber + ")");
			}
		}
		builder.append(" groupOnlineBodyMumber  "+groupOnlineBodyMumber);
		builder.append(" need time "+(System.currentTimeMillis()-time2));
		LogUtil.makeLog(TAG, builder.toString());
	}
	
	@Deprecated
	private void ShowCurrentGrp() {
		// mTextGroupNum.setText("");
		LogUtil.makeLog(TAG, "  ShowCurrentGrp()  is need ReSort "+mNeedReSort);
		final boolean isFlag = isGroupChange;
		if (!NetChecker.check(mtContext, false)) {
			return;
		}
		//用mLastPttGrp代替，解决对讲组切换之后，未设置当前对讲组（SetCurGrp方法尚未执行完成）的时候，就进行UI更新操作  mofiy by lwang 2014-10-24
//		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		PttGrp pttGrp = mLastPttGrp;
		if (pttGrp != null) {
			if (isRefresh != 1&&!isNeedMenberList) {//modify by wlei 只有在成员列表展开的情况下，才向服务端发送请求
				GroupListUtil.getDataCurrentGroupList();
			}
			mGroupListsMap = GroupListUtil.getGroupListsMap();
			group_name_title.setText(pttGrp.grpName);
			//process in updateOnlineGroups() . modify by wlei 2014-10-23 
//			arrayList = mGroupListsMap.get(pttGrp);
//			if (arrayList != null && arrayList.size() > 0) {
//				groupBodyMumber = arrayList.size();
//				int x = arrayList.size();
//				for (int i = 0; i < arrayList.size(); i++) {
//					if (arrayList.get(i).GrpState.equals(getResources().getString(R.string.the_status_1))) {
//						x = x - 1;
//					}
//				}
//				groupOnlineBodyMumber = x + "";
//			}
			updateOnlineGroups(pttGrp);
			//modify by wlei 2014-10-15 单独开一个线程处理排序
//			sortArrayList(arrayList);
//			sortOnline(arrayList);
			if(isGroupChange){//add by wlei 2014-10-22 切换对讲组，需要更新成员列表
				//refresh in updateMemberList() . modify by wlei
//				if (mGroupMemberAdapter != null) {
//						mGroupMemberAdapter.refreshList(arrayList);
//						mGroupMemberAdapter.notifyDataSetChanged();
//					isGroupChange = false;
//				}
				updateMemberList(arrayList);
				isGroupChange = false;
			}
			//当成员列表处于打开状态的时候才进行排序 true,需要打开但未打开， 反之，已经打开. add by wlei 2014-10-23
			if (!isNeedMenberList) {
				final ArrayList<GroupListInfo> list = arrayList;
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						sort(list);
					}
				}).start();
			}
			/*
			 * ArrayList<GroupListInfo> arrayList; 根据GrpName排序 public class
			 * GroupListInfo { public String GrpName=""; public String
			 * GrpNum=""; public String GrpState=""; }
			 * 
			 * 规则：中文最前且按A-Z排序 紧接着字母 最后数字
			 */
			// tv_group_status.setText("我的状态"+" ： "+ShowPttStatus(pttGrp.state));
			// tv_group_speaker.setText(ShowSpeakerStatus(pttGrp.speaker,
			// pttGrp.speakerN));

		} else {
			group_name_title.setText(R.string.ptt);
		}

		// 1021
		// mGroupMemberAdapter= new MyGroupMemberAdapter(mContext, arrayList);
		// mGroupNameAdapter= new MyGroupNameAdapter(mContext, mGroups);
		// group_member_list.setAdapter(mGroupMemberAdapter);
		// group_name_list.setAdapter(mGroupNameAdapter);
		if (mGroupNameAdapter != null) {
			mGroupNameAdapter.refreshNameList(pttGrps);
			mGroupNameAdapter.notifyDataSetChanged();
		}
		// if (pttGrp != null) {
		// currentGroupNumber = pttGrp.grpID;
		// // mTextGroupNum.setText(resources.getString(R.string.current_group)
		// // + pttGrp.grpName);
		// tv_group_status.setText("我的状态"+" ： "+ShowPttStatus(pttGrp.state));
		// String speakerString = ShowSpeakerStatus(pttGrp.speaker,
		// pttGrp.speakerN);
		// tv_group_speaker.setText(speakerString);
		//
		// Log.i(TAG,
		// "speaker   pttGrp.speaker = "+pttGrp.speaker+",pttGrp.speakerN = "+pttGrp.speakerN);
		// Log.i(TAG, "speaker   speakerString = "+speakerString);
		//
		// // houyuchun modify 20120620 end
		// } else {
		// // 不在任何组
		// currentGroupNumber = "";
		// tv_group_status.setText("我的状态 ： 空闲");
		// tv_group_speaker.setText("正在讲话（无）");
		// }
		if (groupOnlineBodyMumber != null) {
			if (isChangeMemaber) {
				new_member_text.setText("(" + groupOnlineBodyMumber + "/"
						+ groupBodyMumber + ")");
			} else {
				new_member_text.setText("(" + groupBodyMumber + ")");
			}
		}
		isRefresh = 1;
	}

	private PttGrps getGrps() {
		// TODO Auto-generated method stub
		UserAgent ua = Receiver.GetCurUA();
		if (ua != null) {
			pttGrps = ua.GetAllGrps();
		}
		return pttGrps;
	}

	private ArrayList<GroupListInfo>  sortOnline(ArrayList<GroupListInfo> arrayList2) {
		// TODO Auto-generated method stub
		ArrayList<GroupListInfo> list = new ArrayList<GroupListInfo>(); 
		if (arrayList2 != null && arrayList2.size() > 1) {
			ArrayList<GroupListInfo> arrayList11 = new ArrayList<GroupListInfo>();
			ArrayList<GroupListInfo> arrayList22 = new ArrayList<GroupListInfo>();
			for (int i = 0; i < arrayList2.size(); i++) {
				if (arrayList2.get(i).GrpState.equals(getResources().getString(R.string.the_status_1))) {
					arrayList22.add(arrayList2.get(i));
				} else {
					arrayList11.add(arrayList2.get(i));
				}
			}
			if (arrayList22.size() > 0) {
				for (int i = 0; i < arrayList22.size(); i++) {
					arrayList11.add(arrayList22.get(i));
				}
			}
			list = arrayList11;
		}
		if (arrayList2.size() == 1) {
			list = arrayList2;
		}
		return list;
	}

	private ArrayList<GroupListInfo> sortArrayList(ArrayList<GroupListInfo> arrayList2) {
		// TODO Auto-generated method stub
		ArrayList<GroupListInfo> list = new ArrayList<GroupListInfo>();
		if (arrayList2 != null && arrayList2.size() > 1) {
			list = (ArrayList<GroupListInfo>) sequence(arrayList2);
			// arrayList = (ArrayList<GroupListInfo>) findPos_(arrayList);
			// arrayList = (ArrayList<GroupListInfo>) changePositon(arrayList);
		}
		if (arrayList2.size() == 1) {
			list = arrayList2;
		}
		return list;
	}

	// 更新组列表
	private void updateGroupLists() {
		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		if (pttGrp != null) {
			updateOnlineGroups(pttGrp);
		}
        //need not getCurrentGrpMemberMessages here. modify by lwang 2014-10-25
//		Message msg = new Message();
//		msg.what = 1;
//		mHandler.sendMessage(msg);
	}

	public static void setPttBackground(boolean pressed) {
		// TODO Auto-generated method stub
		if (group_button_ptt != null) {
			group_button_ptt
					.setImageResource(pressed ? R.drawable.group_list_ptt_down
							: R.drawable.group_list_ptt_up);
//			group_button_text.setText(pressed?R.string.releaseEnd:R.string.pushTalk);
			
		}

	}

	@Override
	public int compare(GroupListInfo lhs, GroupListInfo rhs) {
		// TODO Auto-generated method stub
		// Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);

		int flag = 0;
		if (collator.compare(lhs.GrpName,
				rhs.GrpName) < 0) {
			flag = -1;
		} else if (collator.compare(
				lhs.GrpName, rhs.GrpName) > 0) {
			flag = 1;
		}
		// else {
		// if (lhs.verCode > rhs.verCode) {
		// flag = -1;
		// } else if (lhs.verCode < rhs.verCode) {
		// flag = 1;
		// }
		// }
		return flag;
	}

	public List<GroupListInfo> sequence(List<GroupListInfo> list) {
		List<GroupListInfo> tolist = new ArrayList<GroupListInfo>();
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				tolist.add(list.get(i));
			} else {
				tolist.add(findPos(tolist, list.get(i)), list.get(i));
			}
		}

		return tolist;
	}

	private int findPos(List<GroupListInfo> list, GroupListInfo gli) {
		for (int i = 0; i < list.size(); i++) {
			if (compare(list.get(i), gli) > 0) {
				return i;
			}
		}
		return list.size();
	}

	// private List<GroupListInfo> findPos_(List<GroupListInfo> tolist){
	// GroupListInfo info = tolist.get(tolist.size()-1);
	// tolist.remove(tolist.size()-1);
	// for(int i = 0;i<tolist.size();i++){
	// if(compare(tolist.get(i), info)>0){
	// tolist.add(i, info);
	// return tolist;
	// }
	// }
	// tolist.add(tolist.size()-1, info);
	// return tolist;
	// }
	// private int StringType(char c){ //1数字，2为字母 ,3 为汉字 ,-1 error
	// int t = (int)c;
	// if(t > 0){
	// if(t < 58) return 1;
	// if(t < 127) return 2;
	// else return 3;
	// }
	// return -1;
	// }
	// private List<GroupListInfo> changePositon (List<GroupListInfo> list){
	// List<GroupListInfo> tolist = new ArrayList<GroupListInfo>();
	// //寻找位置
	// int numpos = findpos(list,1);
	// int strpos = findpos(list,2);
	// int chinesepos = findpos(list,3);
	// //添加
	// //汉字添加
	// int ddd = list.size() - chinesepos;
	// if(chinesepos != -1)
	// for(int i = 0;i< ddd;i++){
	// if(tolist.size() > list.size()-1) return tolist;
	// tolist.add(list.get(chinesepos+i));
	// }
	// if(chinesepos == -1) chinesepos = list.size();
	// int ddd1 = chinesepos -strpos;
	// //字母添加
	// if(strpos != -1)
	// for(int i = 0;i< ddd1;i++){
	// if(tolist.size() > list.size()-1) return tolist;
	// tolist.add(list.get(strpos+i));
	// }
	// if(strpos == -1) strpos = chinesepos;
	// int ddd2 = strpos -numpos;
	// //数字添加
	// if(numpos != -1)
	// for(int i = 0;i< ddd2;i++){
	// if(tolist.size() > list.size()-1) return tolist;
	// tolist.add(list.get(numpos+i));
	// }
	//
	// return tolist;
	// }
	// private int findpos(List<GroupListInfo> list,int flag){////1数字，2为字母 ,3
	// 为汉字
	// int pos = -1;
	// for(int i = 0;i<list.size();i++){
	// if(list.get(i).GrpName.length()>0){
	// if(StringType(list.get(i).GrpName.charAt(0)) == flag){
	// return i;
	// }
	// }
	// }
	// return pos;
	// }

	@Override
	public void showCurrentVolume(int volume) {
		mBaseVisualizerView.setTimes(volume);
	}

	@Override
	public void showCurrentReceiveVolume(int volume) {
		mBaseVisualizerView.setTimes(volume);
	}

	public static TalkBackNew getInstance() {
		// TODO Auto-generated method stub
		return mtContext;
	}

	// bluetooth for ptt,add by oumogang 2014-03-06
	@Override
	public void pressPTT(boolean down) {
		// TODO Auto-generated method stub
		// mPttHandler.post(down?pttDownRunable:pttUpRunable);
		// 更新界面
		TalkBackNew instance = getInstance();
		if (instance != null) {
			Message msg = instance.pttPressHandler.obtainMessage();
			msg.what = down ? 1 : 0;
			instance.pttPressHandler.sendMessage(msg);
			// 设置波形和发送信令
//			instance.lineListener = down ? TalkBackNew.this : null;
		}
		// 检查网络和对讲组
		if (!NetChecker.check(mtContext, true)) {
			return;
		}
		if (!checkHasCurrentGrp(mtContext)) {
			return;
		}
		UserAgent ua = Receiver.GetCurUA();
		if (ua != null) {
			ua.OnPttKey(down);
		} else {
			com.zed3.log.MyLog.e(TAG, "pressPTT(" + down + ") ,ua = null");
		}
	}
    
	
	public Handler pttPressHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				com.zed3.log.MyLog
						.e(TAG,
								"pttPressHandler setPttBackground isPttPressing = false;");
				isPttPressing = false;
				setPttBackground(false);
				break;
			case 1:
				com.zed3.log.MyLog
						.e(TAG,
								"pttPressHandler setPttBackground isPttPressing = true;");
				isPttPressing = true;
				setPttBackground(true);
				break;

			default:
				break;
			}
		};
	};

	// 构建Runnable对象，在runnable中更新界面
	Runnable pttDownRunable = new Runnable() {
		@Override
		public void run() {
			// 更新界面
			isPttPressing = true;
			setPttBackground(true);

			if (!NetChecker.check(mtContext, true)) {
				return;
			}
			if (!checkHasCurrentGrp(mtContext)) {
				return;
			}

//			lineListener = TalkBackNew.this;
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
//			lineListener = null;
			UserAgent ua = Receiver.GetCurUA();
			if (ua != null) {
				ua.OnPttKey(false);
			} else {
				com.zed3.log.MyLog.e(TAG, "pttUpRunable ,ua = null");
			}
		}

	};
	public static LineUpdateListener lineListener;
	private static ReceiveLineListener receiveListener;
	public/* static */Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				int volume = msg.arg1;
				//对讲界面PTT按键按下或其他PTT按键按下才显示语音波形 modify by mou 2014-10-10
				if (lineListener != null && (isPttPressing || GroupCallUtil.mIsPttDown)) {
					lineListener.showCurrentVolume(volume);
				}
				break;
			case 2:
				int volume1 = msg.arg1;
				if (receiveListener != null) {
					receiveListener.showCurrentReceiveVolume(volume1);
				}
				break;
			}
		}
	};
	private ProgressDialog mSppConnectProcessDialog;
//	public static int mAudioMode = AudioUtil.MODE_SPEAKER;
//	public static int mLastAudioMode = AudioUtil.MODE_SPEAKER;

	/**
	 * check SipUAApp.isHeadsetConnected 修改按返回键离开通话界面后声音自动切换到扬声器的问题
	 */
//	public void setAudioMode(int mode) {
//		// TODO Auto-generated method stub
//		if (SipUAApp.isHeadsetConnected) {
//			AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_HOOK);
//		} else {
//			AudioUtil.getInstance().setCustomMode(AudioUtil.TYPE_GROUPCALL,
//					mode);
//			AudioUtil.getInstance().setAudioConnectMode(mode);
//		}
//	}

	@Override
	public void onDeviceConnectting(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub
		mSppConnectProcessDialog = DialogUtil.showProcessDailog(
				TalkBackNew.this, getResources().getString(R.string.connecting_hm) + device.getDeviceName());
		// Toast.makeText(getApplicationContext(),"正在连接手咪  "+device.getDeviceName(),0).show();
	}

	@Override
	public void onDeviceConnectFailed(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub
		dismissMyDialog(mSppConnectProcessDialog);
		mSppConnectProcessDialog = DialogUtil.showProcessDailog(
				TalkBackNew.this, getResources().getString(R.string.connecting_failed) + device.getDeviceName());
		// Toast.makeText(getApplicationContext(),"蓝牙手咪连接失败",0).show();
		if (mSppConnectProcessDialog != null) {
			Message msg = dismissDialogHandler.obtainMessage();
			msg.what = 2;
			dismissDialogHandler.sendMessageDelayed(msg, 2000);
		}
	}

	@Override
	public void onDeviceConnected(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub
		dismissMyDialog(mSppConnectProcessDialog);
		mSppConnectProcessDialog = DialogUtil.showProcessDailog(
				TalkBackNew.this, getResources().getString(R.string.hm_connected) + device.getDeviceName());
		// Toast.makeText(getApplicationContext(),"蓝牙手咪已连接",0).show();

		if (mSppConnectProcessDialog != null) {
			Message msg = dismissDialogHandler.obtainMessage();
			msg.what = 2;
			dismissDialogHandler.sendMessageDelayed(msg, 2000);
		}
	}

	@Override
	public void onDeviceDisconnectting(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub
		dismissMyDialog(mSppConnectProcessDialog);
		mSppConnectProcessDialog = DialogUtil.showProcessDailog(
				TalkBackNew.this,getResources().getString(R.string.disconnecting_hm) + device.getDeviceName());
		// Toast.makeText(getApplicationContext(),"蓝牙手咪已连接",0).show();
	}

	@Override
	public void onDeviceDisconnected(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub
		dismissMyDialog(mSppConnectProcessDialog);
		mSppConnectProcessDialog = DialogUtil.showProcessDailog(
				TalkBackNew.this, getResources().getString(R.string.hm_disconnected) + device.getDeviceName());
		// Toast.makeText(getApplicationContext(),"蓝牙手咪已断开",0).show();

		if (mSppConnectProcessDialog != null) {
			Message msg = dismissDialogHandler.obtainMessage();
			msg.what = 2;
			dismissDialogHandler.sendMessageDelayed(msg, 2000);
		}

	}

	@Override
	public void onDeviceFound(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStateTurnningOn() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStateOn() {
		// TODO Auto-generated method stub
		bluetoothModeOnoffBt
				.setVisibility((!mIsBTServiceStarted/*
													 * &&BluetoothSCOStateReceiver
													 * .
													 * isBluetoothAdapterEnabled
													 */) ? View./* VISIBLE */GONE
						: View.GONE);
	}

	@Override
	public void onStateTurnningOff() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStateOff() {
		// TODO Auto-generated method stub
		bluetoothModeOnoffBt.setVisibility(View.GONE);
		ZMBluetoothManager.getInstance().disConnectZMBluetooth(mtContext);
	}
	
	private void stopCurrentAnimation(){
		if(mLoadingAnimation != null){
			mLoadingAnimation.stopAnimation();
		}
	}
	
}
