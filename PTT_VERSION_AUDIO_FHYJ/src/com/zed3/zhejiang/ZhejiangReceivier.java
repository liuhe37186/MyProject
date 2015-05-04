package com.zed3.zhejiang;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zoolu.tools.GroupListInfo;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;

import com.google.gson.Gson;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.SettingNew;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.utils.Tools;
import com.zed3.zhejiang.GroupInfo.GroupMember;

public class ZhejiangReceivier extends BroadcastReceiver {

	/**
	 * 登录
	 */
	public static final String ACTION_LOGIN = "com.zed3.sipua.login_gqt";
	public static final String USERNAME = "username";
	public static final String NUMBER = "number";
	public static final String PASSWORD = "password";
	public static final String PROXY = "proxy";
	public static final String PORT = "port";
	/**
	 * 登录状态
	 */
	public static final String ACTION_LOGIN_SUCCESS = "com.zed3.sipua.login_success";
	public static final String LOGIN_STATUS= "loginstatus";
	/**
	 * 退出
	 */
	public static final String ACTION_LOGOUT = "com.zed3.sipua.logout";

	/**
	 * 视频和语音呼叫
	 */
	public static final String ACTION_CALL = "com.zed3.sipua.call";
	public static final String CALL_TYPE = "call_type";
	public static final int AUDIO_CALL = 1;
	public static final int VIDEO_CALL = 2;

	// public static final String ACTION_SETTING = "com.zed3.sipua.setting";
	/**
	 * 获取对讲组列表
	 */
	public static final String ACTION_GROUP_GET = "com.zed3.sipua.group_get";

	/**
	 * 切换对讲组
	 */
	public static final String ACTION_GROUP_CHANGE = "com.zed3.sipua.group_change";
	public static final String GROUPNUMBER = "groupnumber";

	/**
	 * 获取当前讲话者和自身状态
	 */
	public static final String ACTION_GROUP_STATUS_GET = "com.zed3.sipua.group_status_get";

	/**
	 * PTT按下弹起
	 * 
	 */
	public static final String ACTION_PTT = "com.zed3.sipua.ptt";
	public static final String PTT_STATUS = "ptt_status";
	public static final int PTT_DOWN = 0;
	public static final int PTT_UP = 1;

	/**
	 * 获取当前组成员列表
	 */
	public static final String ACTION_GROUP_MEMBER_GET = "com.zed3.sipua.group_member_get";

	/**
	 * 发送当前组成员列表
	 */
	public static final String ACTION_GROUP_MEMBER_SENT = "com.zed3.sipua.group_member_sent";
	public static final String GROUP_MEMBER = "groupmember";
	/**
	 * 发送对讲组列表
	 */
	public static final String ACTION_GROUP_SENT = "com.zed3.sipua.group_sent";
	public static final String GROUPNAME = "groupname";
	/**
	 * 发送我的状态
	 */
	public static final String ACTION_GROUP_STATUS_SENT = "com.zed3.sipua.group_status_sent";
	public static final String CurrentSpeaker = "current_speaker";
	public static final String STATUS = "status";

	/**
	 * 退出
	 */
	public static final String ACTION_LOGINOUT_SUCCESS = "com.zed3.sipua.loginout_success";
	public static final String LOGINOUT_STATUS = "loginoutstatus";
	/**
	 * 发送对讲组切换状态
	 */
	public static final String ACTION_GROUP_CHANGE_SUCCESS = "com.zed3.sipua.Group_change_success";
	public static final String GROUP_CHANGE_STATUS = "groupchangestatus";

	private final String ACTION_GROUP_STATUS = "com.zed3.sipua.ui_groupcall.group_status";
	private static String currentSpeaker = null;
	private static String myStatus = null;

	/**
	 * 短信
	 */
	private final String ACTION_SMS_SENT = "com.zed3.sipua.sms_sent";
	private static String SMS_SENT_NUM = "";
	private static String SMS_SENT_BODY = "";
	public static String SEND_TEXT_SUCCEED = "SEND_MESSAGE_SUCCEED";
	public static String SEND_TEXT_FAIL = "SEND_MESSAGE_FAIL";
	public static String ACTIONG_SMS_SENT_SUCCESS = "com.zed3.sipua.sms_sent_success";
	public static final String ACTION_SETTING = "com.zed3.sipua.setting";

	public static final String ACTIONG_RECEIVER_SMS = "TEXT_MESSAGE_CHANGED";
	public static String ACTIONG_SMS_GET = "com.zed3.sipua.sms_get";
	private final String ACTION_RECEIVE_TEXT_MESSAGE = "com.zed3.sipua.ui_receive_text_message";

	private final String ACTION_SEND_TEXT_MESSAGE_FAIL = "com.zed3.sipua.ui_send_text_message_fail";
	private final String ACTION_SEND_TEXT_MESSAGE_SUCCEED = "com.zed3.sipua.ui_send_text_message_succeed";
	
	/**
	 * 所有对讲组列表
	 */
	public static final String ACTION_ALL_GET = "com.zed3.sipua.all_get";
	public static final String ACTION_ALL_SENT = "com.zed3.sipua.all_sent";

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("-----intent.Action:"+intent.getAction());
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION_LOGIN)) {
			System.out.println("--------收到登陆广播----");
			// 登录
			Bundle extras = intent.getExtras();
			String username = extras.getString(USERNAME);
			String password = extras.getString(PASSWORD);
			String proxy = extras.getString(PROXY);
			String port = extras.getString(PORT);
			String sharedPrefsFile = "com.zed3.sipua_preferences";
			SharedPreferences sharedPreferences = context.getSharedPreferences(
					sharedPrefsFile, Context.MODE_PRIVATE);
			String mUserName = sharedPreferences.getString(USERNAME, "");
			String mPassWord = sharedPreferences.getString(PASSWORD, "");
			if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
//				System.out.println("-----mSipdroidEngine!=null");
					System.out.println("-----mSipdroidEngine isRegistered");
					if (username.equals(mUserName)
							&& password.equals(mPassWord)) {
						System.out.println("-----username and password same");
						Intent sendLoginStatus = new Intent(
								ACTION_LOGIN_SUCCESS);
						Bundle isLoginStatus = new Bundle();
						isLoginStatus.putBoolean(LOGIN_STATUS, true);
						sendLoginStatus.putExtras(isLoginStatus);
						Receiver.mContext.sendBroadcast(sendLoginStatus);
						System.out.println("-----已经登录了--");
					} else {
						System.out.println("-----username and password not same");
						String sf = "com.zed3.sipua_preferences";
						SharedPreferences sp = context
								.getSharedPreferences(sf,
										Context.MODE_PRIVATE);
						Editor edit = sp.edit();
						edit.putString(Settings.PREF_USERNAME, "");
						edit.putString(Settings.PREF_PASSWORD, "");
						edit.putString(Settings.PREF_SERVER, "");
						edit.putString(Settings.PREF_PORT, "");
						edit.commit();
						Tools.exitApp(context);
						login(username,password,port,proxy);
						Intent sendLoginStatus = new Intent(
								ACTION_LOGIN_SUCCESS);
						Bundle isLoginStatus = new Bundle();
						isLoginStatus.putBoolean(LOGIN_STATUS, true);
						sendLoginStatus.putExtras(isLoginStatus);
						Receiver.mContext.sendBroadcast(sendLoginStatus);
					}
			} else {
				System.out.println("-----开始登录");
				login(username,password,port,proxy);
				Intent sendLoginStatus = new Intent(
						ACTION_LOGIN_SUCCESS);
				Bundle isLoginStatus = new Bundle();
				isLoginStatus.putBoolean(LOGIN_STATUS, true);
				sendLoginStatus.putExtras(isLoginStatus);
				Receiver.mContext.sendBroadcast(sendLoginStatus);
			}

		} else if (intent.getAction().equals(ACTION_LOGOUT)) {
			// 退出
			Intent loginout = new Intent(ACTION_LOGINOUT_SUCCESS);
			Bundle extras = new Bundle();
			extras.putBoolean(LOGINOUT_STATUS, true);
			loginout.putExtras(extras);
			Receiver.mContext.sendBroadcast(loginout);
			String sharedPrefsFile = "com.zed3.sipua_preferences";
			SharedPreferences sharedPreferences = context
					.getSharedPreferences(sharedPrefsFile,
							Context.MODE_PRIVATE);
			Editor edit = sharedPreferences.edit();
			edit.putString(Settings.PREF_USERNAME, "");
			edit.putString(Settings.PREF_PASSWORD, "");
			edit.putString(Settings.PREF_SERVER, "");
			edit.putString(Settings.PREF_PORT, "");
			edit.commit();
			Tools.exitApp(context);

		} else if (intent.getAction().equals(ACTION_CALL)) {
			Bundle data = intent.getExtras();
			String number = data.getString(NUMBER);
			int type = data.getInt(CALL_TYPE);
			if (type == 1) {
				// 语音呼叫
				CallUtil.makeAudioCall(context, number, null);
			} else if (type == 2) {
				// 视频呼叫
				CallUtil.makeVideoCall(context, number, null);
			}
		} else if (intent.getAction().equals(ACTION_GROUP_GET)) {
			// 获取对讲组列表
			sentGroupName();

		} else if (intent.getAction().equals(ACTION_GROUP_CHANGE)) {
			// 切换对讲组
			Bundle data = intent.getExtras();
			String groupNum = data.getString(GROUPNUMBER);
			System.out.println("------groupNum----" + groupNum);
			UserAgent ua = Receiver.GetCurUA();
			PttGrps pttGrps = Receiver.GetCurUA().GetAllGrps();
			if (groupNum != null && pttGrps != null) {
				PttGrp grpByID = pttGrps.GetGrpByID(groupNum);
				if (grpByID != null) {
					ua.SetCurGrp(grpByID);
					sentGroupChangeStatus(true);
				}

			} else {
				sentGroupChangeStatus(false);
			}

		} else if (intent.getAction().equals(ACTION_GROUP_STATUS_GET)) {
			System.out.println("------ACTION_GROUP_STATUS_GET-----");
			// 获取当前讲话者和自身状态
			sentStatus();
		} else if (intent.getAction().equals(ACTION_GROUP_MEMBER_GET)) {
			// 获取当前组成员列表
			System.out.println("-------ACTION_GROUP_MEMBER_GET-------");
			sentGroupMember();

		} else if (intent.getAction().equals(ACTION_PTT)) {
			System.out.println("---------------");
			// PTT按下弹起
			Bundle data = intent.getExtras();
			int status = data.getInt(PTT_STATUS);
			if (status == PTT_DOWN) {
				System.out.println("-------PTT_DOWN--------");
				GroupCallUtil.makeGroupCall(true, false);
			} else if (status == PTT_UP) {
				System.out.println("-------PTT_UP--------");
				GroupCallUtil.makeGroupCall(false, false);
			}
		} else if (intent.getAction().equalsIgnoreCase(ACTION_GROUP_STATUS)) {
			Bundle bundle = intent.getExtras();
			PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
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
			if (pttGrp != null) {
				setStatus(speaker, ShowPttStatus(pttGrp.state));
				sentStatus();
			}
		} else if (intent.getAction().equalsIgnoreCase(ACTION_SETTING)) {
			Intent intent1 = new Intent(context, SettingNew.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent1);
		} else if (intent.getAction().equals(ACTION_SMS_SENT)) {
			Bundle bundle = intent.getExtras();
			SMS_SENT_NUM = bundle.getString("number");
			SMS_SENT_BODY = bundle.getString("body");
			sendMessage(SMS_SENT_NUM, SMS_SENT_BODY);
		} else if (intent.getAction().equals(ACTION_SEND_TEXT_MESSAGE_SUCCEED)) {
			Intent sms_sent_success = new Intent(ACTIONG_SMS_SENT_SUCCESS);
			Bundle extras = new Bundle();
			extras.putBoolean("success", true);
			sms_sent_success.putExtras(extras);
			Receiver.mContext.sendBroadcast(sms_sent_success);
			System.out.println("----短信发送成功----");
		} else if (intent.getAction().equals(ACTION_SEND_TEXT_MESSAGE_FAIL)) {
			Intent sms_sent_fail = new Intent(ACTIONG_SMS_SENT_SUCCESS);
			Bundle extras = new Bundle();
			extras.putBoolean("success", false);
			sms_sent_fail.putExtras(extras);
			Receiver.mContext.sendBroadcast(sms_sent_fail);
			System.out.println("----短信发送失败----");
		} else if (intent.getAction().equals(ACTION_RECEIVE_TEXT_MESSAGE)) {
			System.out.println("--------收到一条短信-----");
			initMsg();
		} else if(intent.getAction().equals(ACTION_ALL_GET)){
			System.out.println("-----收到获取通讯录广播");
			sendGroupInfo();
		}
	}
	private void login(String username,String password,String port,String proxy){
		Intent intent1 = new Intent(Receiver.mContext, SplashActivity.class);
		// Intent intent1 = new Intent(context, LoginActivity.class);
		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Receiver.mContext.startActivity(intent1);

		if (username != null && password != null && proxy != null
				&& port != null) {

			System.out.println("-----11username:" + username
					+ ",password:" + password + ",proxy:" + proxy
					+ ",port:" + port);
			 String sharedPrefsFile = "com.zed3.sipua_preferences";
			 SharedPreferences sharedPreferences = Receiver.mContext
			 .getSharedPreferences(sharedPrefsFile,
			 Context.MODE_PRIVATE);
			Editor edit = sharedPreferences.edit();
			edit.putString(Settings.PREF_USERNAME, username);
			edit.putString(Settings.PREF_PASSWORD, password);
			edit.putString(Settings.PREF_SERVER, proxy);
			edit.putString(Settings.PREF_PORT, port);
			edit.commit();
		}
	}
	/**
	 * 将收到的短信发出去
	 */
	private void initMsg() {
		String number = "";
		String name = "";
		String body = "";
		SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(Receiver.mContext);
		Cursor mCursor = mSmsMmsDatabase.mQuery("message_talk", "status= " + 0
				+ " and type='sms' ", null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			if (mCursor.moveToLast()) {
				number = mCursor.getString(mCursor.getColumnIndex("address"));
				name = mCursor
						.getString(mCursor.getColumnIndex("contact_name"));
				body = mCursor.getString(mCursor.getColumnIndex("body"));
				Intent intent = new Intent(ACTIONG_SMS_GET);
				Bundle extras = new Bundle();
				extras.putString("number", number);// 发送信息的人的号码
				extras.putString("name", name);// 发送信息的人的名称
				extras.putString("body", body);// 发送的信息内容
				intent.putExtras(extras);
				Receiver.mContext.sendBroadcast(intent);
				System.out.println("----发送广播--com.zed3.sipua.sms_get");
				System.out.println("-----number =" + number + "--body=" + body);
			}
		}
		if (mCursor != null) {
			mCursor.close();
		}
		if (mSmsMmsDatabase != null) {
			mSmsMmsDatabase.close();
		}

	}

	private void sendMessage(String toValue, String bodyValue) {
		// TODO Auto-generated method stub
		String E_id = Receiver.GetCurUA().SendTextMessage(toValue, bodyValue);
		SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(Receiver.mContext);
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
	}

	// 获取当前系统时间
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
	
	private void sendGroupInfo(){
		Gson gson = new Gson();
		String str = gson.toJson(getGroupName());
		System.out.println("-------str:"+str);
		Intent intent = new Intent(ACTION_ALL_SENT);
		Bundle extras = new Bundle();
		extras.putString("jsonString", str);//result为建议的报文
		intent.putExtras(extras);
		Receiver.mContext.sendBroadcast(intent);
	}
	
	/**
	 * 获取对讲组列表
	 */
	private List<GroupInfo> getGroupName(){
		PttGrps pttGrps = Receiver.GetCurUA().GetAllGrps();
		GroupInfo groupInfo = null;
		List<GroupInfo> pttGrpList = new ArrayList<GroupInfo>();
		for(int i = 0; i < pttGrps.GetCount();i++){
			groupInfo = new GroupInfo();
			PttGrp pttGrp = pttGrps.GetGrpByIndex(i);
			
			groupInfo.groupMember = new ArrayList<GroupInfo.GroupMember>();
			ArrayList<GroupListInfo> arrayList = GroupListUtil
					.getGroupListsMap().get(pttGrp);
			if (arrayList != null) {
				for (int j = 0; j < arrayList.size(); j++) {
					GroupMember groupMember = new GroupMember();
					groupMember.memberNumber = arrayList.get(j).GrpNum;
					groupMember.memberName = arrayList.get(j).GrpName;
					groupInfo.groupMember.add(groupMember);
				}
			}
			groupInfo.groupNumber = pttGrp.grpID;
			groupInfo.groupName = pttGrp.grpName;
			pttGrpList.add(groupInfo);
		}
		return pttGrpList;
	}
	public void sentGroupName() {
		PttGrps pttGrps = Receiver.GetCurUA().GetAllGrps();
		int length = 0;
		if (pttGrps != null) {
			length = pttGrps.GetCount();
		}

		StringBuilder groupList = new StringBuilder();
		for (int i = 0; i < length; i++) {
			String grpName = pttGrps.GetGrpByIndex(i).grpName;
			String grpID = pttGrps.GetGrpByIndex(i).grpID;
			groupList.append(grpName);
			groupList.append(",");
			groupList.append(grpID);
			groupList.append(";");
		}
		if (groupList != null && groupList.toString().length() >= 1) {
			String groupListStr = groupList.substring(0, groupList.toString()
					.length() - 1);
			System.out.println("---------groupListStr:"+groupListStr);
			Intent groupName = new Intent(ACTION_GROUP_SENT);
			Bundle extras = new Bundle();
			extras.putString(GROUPNAME, groupListStr);
			groupName.putExtras(extras);
			Receiver.mContext.sendBroadcast(groupName);
		}

	}

	public void sentGroupMember() {
		PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
		if (pttGrp != null) {
			GroupListUtil.getDataCurrentGroupList();
			ArrayList<GroupListInfo> arrayList = GroupListUtil
					.getGroupListsMap().get(pttGrp);
			StringBuilder groupMemberList = new StringBuilder();
			if (arrayList != null) {
				for (int i = 0; i < arrayList.size(); i++) {
					groupMemberList.append(arrayList.get(i).toString());
				}
			}
			String groupMemberListStr = null;
			if (groupMemberList != null && groupMemberList.length() >= 1) {
				groupMemberListStr = groupMemberList.substring(0,
						groupMemberList.length() - 1);
			}
			System.out.println("------组成员列表--" + groupMemberListStr);
			Intent intent = new Intent(ACTION_GROUP_MEMBER_SENT);
			Bundle extras = new Bundle();
			extras.putString(GROUP_MEMBER, groupMemberListStr);
			intent.putExtras(extras);
			Receiver.mContext.sendBroadcast(intent);
		}
	}

	public void sentStatus() {
		Intent intent = new Intent(ACTION_GROUP_STATUS_SENT);
		Bundle extras = new Bundle();
		extras.putString(CurrentSpeaker, currentSpeaker);
		extras.putString(STATUS, myStatus);
		intent.putExtras(extras);
		Receiver.mContext.sendBroadcast(intent);
	}

	public static void setStatus(String currenSpeaker1, String myStatus1) {
		currentSpeaker = currenSpeaker1;
		myStatus = myStatus1;
	}

	public String ShowPttStatus(PttGrp.E_Grp_State pttState) {
		switch (pttState) {
		// houyuchun modify 20120620 begin
		case GRP_STATE_SHOUDOWN:
			return Receiver.mContext.getResources().getString(
					R.string.status_close);
		case GRP_STATE_IDLE:
			return Receiver.mContext.getResources().getString(
					R.string.status_free);
		case GRP_STATE_TALKING:
			return Receiver.mContext.getResources().getString(
					R.string.status_speaking);
		case GRP_STATE_LISTENING:
			return Receiver.mContext.getResources().getString(
					R.string.status_listening);
		case GRP_STATE_QUEUE:
			return Receiver.mContext.getResources().getString(
					R.string.status_waiting);
		}
		return Receiver.mContext.getResources()
				.getString(R.string.status_error);
	}

	public void sentGroupChangeStatus(Boolean result) {

		Intent groupChangeStatus = new Intent(ACTION_GROUP_CHANGE_SUCCESS);
		Bundle extras = new Bundle();
		extras.putBoolean(GROUP_CHANGE_STATUS, result);
		groupChangeStatus.putExtras(extras);
		Receiver.mContext.sendBroadcast(groupChangeStatus);
	}

}
