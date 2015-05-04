package com.zed3.sipua.ui.anta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.ContactManager;
import com.zed3.sipua.ui.lowsdk.ContactPerson;
import com.zed3.toast.MyToast;
import com.zed3.utils.Zed3Log;

public class AntaCallUtil {
	private static final String NUMBER = "info";
	private static final String ANTA_LIST = "ANTA_USER_LIST";
	static ArrayList<Map<String, Object>> userListData;
	public static boolean isAntaCall = false;
	private static Editor edit;
	private static SharedPreferences defaultSharedPreferences;
	private static List<Map<String, Object>> mContacts;
	private static boolean mIsGroupBroadcast;
	public static String mCreateTime(){return getCreateTime();};
	static{
		mContacts = new ArrayList<Map<String, Object>>();
		userListData = new ArrayList<Map<String, Object>>();
	}
	public static List<Map<String, Object>> getUsers() {
		// TODO Auto-generated method stub
		readData();
		return userListData;
	}
	private static List<Map<String, Object>> readData() {
		// TODO Auto-generated method stub
		if (defaultSharedPreferences == null) {
			defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext);
		}
		String string = defaultSharedPreferences.getString(ANTA_LIST, "");
		if (string.equals("")) {
			userListData.clear();
			return userListData;
		}else {
			userListData.clear();
			String[] numberStrings = getNumberArray(string);
			getNumbers(numberStrings);
			return userListData;
		}
	}
	private static void getNumbers(String[] numberStrings) {
		// TODO Auto-generated method stub
		for (int i = 0; i < numberStrings.length; i++) {
			String userNumber = numberStrings[i];
			String userName = ContactUtil.getUserName(numberStrings[i]);
			if (userName != null) {
				if (userName.equals(Settings.getUserName())) {
					break;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("title", userName);
				map.put("info", userNumber);
				userListData.add(map);
			}
		}
	}
	private static String[] getNumberArray(String string) {
		// TODO Auto-generated method stub
		String[] nubers = string.split(" ", 32);
		return nubers;
	}
	public static boolean add(Map<String, Object> map) {
		// TODO Auto-generated method stub
		String newNumber = (String) map.get(NUMBER);
		for (int i = 0; i < userListData.size(); i++) {
			if (newNumber.equals(userListData.get(i).get(NUMBER))) {
				return false;
			}
		}
		userListData.add(userListData.size(),map);
		return true;
	}
	public static Map<String, Object> remove(int mUserListIndex) {
		// TODO Auto-generated method stub
		if (mUserListIndex>-1&&mUserListIndex<userListData.size()) {
			return userListData.remove(mUserListIndex);
		}
		return null;
	}
	public static boolean checkExist(Map<String, Object> contactListClickedItem) {
		// TODO Auto-generated method stub
		String number = (String) contactListClickedItem.get(NUMBER);
		String mNumber;
		for (int i = 0; i < userListData.size(); i++) {
			if (((String)userListData.get(i).get(NUMBER)).equals(number)) {
				return true;
			}
		}
		return false; 
	}
	public static List<Map<String, Object>> removeAddedContact(
			List<Map<String, Object>> mData) {
		// TODO Auto-generated method stub
		if (mData == null) {
			return null;
		}
		String userName = Settings.getUserName();
		for (int j = 0; j < mData.size(); j++) {
			String numberString = (String)mData.get(j).get(NUMBER);
			if (numberString.equals(userName)) {
				mData.remove(j);
				break;
			}
		}
		String mNumber;
		int contactSize = mData.size();
		for (int i = 0; i < userListData.size(); i++) {
			mNumber = (String)userListData.get(i).get(NUMBER);
			for (int j = 0; j < contactSize; j++) {
				String numberString = (String)mData.get(j).get(NUMBER);
				if (numberString.equals(mNumber)) {
					mData.remove(j);
					contactSize--;
					break;
				}
			}
		}
		return mData;
	}
	public static void makeAntaCall(boolean isGroupBroadcast) {
		// TODO Auto-generated method stub
//		UserAgent ua = Receiver.GetCurUA();
		isAntaCall = true;
		Receiver.engine(SipUAApp.mContext).isMakeVideoCall = /*false*/0;
		String myNumber = /*MemoryMg.getInstance().TerminalNum*/Settings.getUserName();
		
		String numbers = getNumbers();
		if (numbers != null&&!"".equals(numbers)) {
//			Receiver.engine(SipUAApp.mContext).antaCall1(myNumber,numbers, true,isGroupBroadcast);
//			CallUtil.initNameAndNumber(numbers, isGroupBroadcast?"广播":"会议");
			makeAntaCall(isGroupBroadcast, numbers);
		}
	}
	
	//makeAntaCall by numbers ，add by oumogang 2013-09-03 
	public static void makeAntaCall(boolean isGroupBroadcast,String numbers) {
		// TODO Auto-generated method stub
		//sim卡电话来电中、去电中或通话中，voip禁止去电，拒接来电；  add by mou 2014-10-08
		if (CallUtil.checkGsmCallInCall()) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
			return;
		}
		//add by oumogang 2013-11-29
		setIsGroupBroadcast(isGroupBroadcast);
		if (numbers == null) {
//			MyToast.showToast(true, SipUAApp.mContext, "antacall null numbers exception");
//			return;
			throw new RuntimeException("antacall null numbers exception");
		}
		if ("".equals(numbers)) {
//			MyToast.showToast(true, SipUAApp.mContext, "antacall numbers no number exception");
//			return;
			throw new RuntimeException("antacall numbers no number exception");
		}
		// 是否注册成功
		if (!Receiver.mSipdroidEngine.isRegistered()) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.notfast_1);
			return;
		}

		if (Receiver.call_state == UserAgent.UA_STATE_INCALL
				|| Receiver.call_state == UserAgent.UA_STATE_OUTGOING_CALL) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.vedio_calling_notify);
			return;
		}
		
//		UserAgent ua = Receiver.GetCurUA();
		isAntaCall = true;
		Receiver.engine(SipUAApp.mContext).isMakeVideoCall = /*false*/0;
		String myNumber = /*MemoryMg.getInstance().TerminalNum*/Settings.getUserName();
		if (numbers != null&&!"".equals(numbers)) {
			Receiver.engine(SipUAApp.mContext).antaCall1(myNumber,numbers, true,isGroupBroadcast);
//			CallUtil.initNameAndNumber(numbers, isGroupBroadcast?"广播":"会议");
			CallUtil.initNameAndNumber(numbers, SipUAApp.mContext.getString(R.string.host_me));
//			mCreateTime = getCreateTime();
		}
	}
	//getCreateTime add by oumogang 2014-01-24
	private static String getCreateTime() {
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat(
					" yyyy/MM/dd HH:mm:ss ");
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// 获取当前时
		return formatter.format(curDate);
	}
	private static String getNumbers() {
		// TODO Auto-generated method stub
		String numbers = "";
		String mNumber;
		for (int i = 0; i < userListData.size(); i++) {
			if (i>32) {
				break;
			}
			mNumber = (String)userListData.get(i).get(NUMBER);
			numbers+=" "+mNumber;
		}
		return numbers.trim();
	}
	public static String saveUserList() {
		// TODO Auto-generated method stub
		String numbers = getNumbers();
		if (edit == null) {
			edit = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).edit();
		}
		edit.putString(ANTA_LIST, numbers);
		edit.commit();
		return numbers;
	}
	public static boolean findAndRemoveCurrentUserFromList() {
		// TODO Auto-generated method stub
		String userName = Settings.getUserName();
		for (int j = 0; j < userListData.size(); j++) {
			String numberString = (String)userListData.get(j).get(NUMBER);
			if (numberString.equals(userName)) {
				userListData.remove(j);
				return true;
			}
		}
		return false;
	}
	public static List<Map<String, Object>> getContacts() {
		// TODO Auto-generated method stub
		List<Map<String, Object>>  contactMapList = new ArrayList<Map<String,Object>>();
//		mContacts.clear();
		mContacts = ContactUtil.copyUsers(mContacts);
//		if (mContacts != null) {
//			mContacts = removeAddedContact(mContacts);
//		}
		ContactManager cm = new ContactManager(SipUAApp.mContext);
		List<ContactPerson>  contactList = cm.query(ContactManager.QUERY_TYPE_ALL);
		if(contactList != null){
			for(ContactPerson cp:contactList){
				// modify by liangzhang 2014-08-06 修改进入会议后联系人列表中有已登录用户选项的bug
				if (cp.getContact_num().equals(Settings.getUserName()))
					continue;
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("info", cp.getContact_num());
				if(TextUtils.isEmpty(cp.getContact_name())){
					map.put("title", cp.getContact_num());
				}else{
					map.put("title", cp.getContact_name());
				}
				map.put("img", R.drawable.icon_contact);
				contactMapList.add(map);
			}
		}
		return contactMapList/*mContacts*/;
	}
	//add by oumogang 2013-11-29
		public static void reInit() {
			Zed3Log.debug("testcrash", "AntaCall#reInit() enter");
			// TODO Auto-generated method stub
			setIsGroupBroadcast(false);
			isAntaCall = false;
		}
	//add by oumogang 2013-11-29
		public static boolean isIsGroupBroadcast() {
			return mIsGroupBroadcast;
		}
		//add by oumogang 2013-11-29
		public static void setIsGroupBroadcast(boolean mIsGroupBroadcast) {
			AntaCallUtil.mIsGroupBroadcast = mIsGroupBroadcast;
		}
	
	

}
