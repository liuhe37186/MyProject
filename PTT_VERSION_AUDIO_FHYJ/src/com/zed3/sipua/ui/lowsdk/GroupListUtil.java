package com.zed3.sipua.ui.lowsdk;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zoolu.tools.GroupListInfo;
import org.zoolu.tools.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.zed3.log.Logger;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.contact.AddContactDialog;
import com.zed3.sipua.ui.contact.ContactUtil;

public class GroupListUtil {

	public static Context mContext;
	public static HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap;
	public static ArrayList<PttGrp> mGroups;
	private static IntentFilter intentfilter2;
	protected static int receiveCount;
	protected static boolean needLog = false;
	private static long thisTimeGroupRefresh;
	private static long lastTimeGroupRefresh;
	private static boolean isGroupListLoading;
	
	private static UserAgent ua;
	private static PttGrps mPttGrps; 
	static{
		mContext = SipUAApp.mContext;
		mGroups = new ArrayList<PttGrp>();
		mGroups_buffur = new ArrayList<PttGrp>();
		mGroupListsMap = new HashMap<PttGrp, ArrayList<GroupListInfo>>();
	}
	public static String getUserName(String number) {
		// TODO Auto-generated method stub
		if (mGroups.size()!= 0 && mGroupListsMap.size() != 0) {
			for (int i = 0; i < mGroups.size(); i++) {
				PttGrp pttGrp = mGroups.get(i);
				if (pttGrp != null) {
					ArrayList<GroupListInfo> arrayList = mGroupListsMap.get(pttGrp);
					if (arrayList != null) {
						for (int j = 0; j < arrayList.size(); j++) {
							GroupListInfo groupListInfo = arrayList.get(j);
							if (groupListInfo != null) {
								if (number.equals(groupListInfo.GrpNum)) {
									return groupListInfo.GrpName;
								}
							}
						}
					}
				}
			}
		}else {
			getData4GroupList();
		}
		return null;
	}
	
	public static boolean getData4GroupList() {
	
		NetChecker.check(SipUAApp.mContext, true);
//		needSendRegetMessage = true;
		ua = Receiver.GetCurUA();
		if (ua == null) {
			// 没有用户,清空前用户组列表信息
			removeDataOfGroupList();
			return false;
		}
		mPttGrps = ua.GetAllGrps();
		if (mPttGrps.GetCount() == 0) {
			// 不在任何组,清空前用户组列表信息
			removeDataOfGroupList();
			return false;
		}
		if (mPttGrps != null) {
			// add by oumogang 2013-05-10
			// 重复出现4009组，先情况缓存；
			isGroupListLoading = true;
			groupListHandler.removeMessages(2);
			for (int i = 0; i < mPttGrps.GetCount(); i++) {
				PttGrp pttGrp = mPttGrps.GetGrpByIndex(i);
				groupListHandler.sendMessage(groupListHandler.obtainMessage(2,
						/* pttGrps.GetGrpByIndex(0) */pttGrp.grpID));
			}
			return true;
		}
		return false;
	}
	
	private static boolean checkTime(long time) {
		// TODO Auto-generated method stub
		thisTimeGroupRefresh = System.currentTimeMillis();
		if (lastTimeGroupRefresh == 0) {
			lastTimeGroupRefresh = thisTimeGroupRefresh;
			return true;
		}
		if ((thisTimeGroupRefresh - lastTimeGroupRefresh) < time) {
			// Toast.makeText(mContext, "亲，刷新有度啊！", 0).show();
//			lastTimeGroupRefresh = thisTimeGroupRefresh;
			return false;
		} else {
			lastTimeGroupRefresh = thisTimeGroupRefresh;
			return true;
		}
	}
	//modify by oumogang 2013-05-27
	public static void removeDataOfGroupList() {
		// TODO Auto-generated method stub
		mGroupListsMap.clear();
		mGroups.clear();
		mContext.sendBroadcast(new Intent(Contants.ACTION_GROUPLIST_CLEAR_OVER));
	}

	private static ArrayList<PttGrp> mGroups_buffur;
	protected static boolean isSingleGroup;
	private static BroadcastReceiver groupListReceiver = new BroadcastReceiver() {

		String tag = "groupListReceiver";
		@Override
		public void onReceive(Context mContext, Intent intent) {
			// TODO Auto-generated method stub
			receiveCount++;
			String d = intent.getAction();
			if (intent.getAction().equalsIgnoreCase(Contants.ACTION_GETSTATUS_MESSAGE)) {
				Bundle bundle = intent.getExtras();
				String body = bundle.getString("statusbody");
				MyLog.e("guojunfeng1028", body);
				Logger.i(needLog, tag, "receive string："+body);
				if (body != null) {
					// 接收数据要到广播里去接
					ArrayList<GroupListInfo> parseListInfo = ParseListInfo(body);
					PttGrp group = getGroup(body);
					ArrayList<GroupListInfo> arrayList = mGroupListsMap.get(group);
					arrayList = parseListInfo;
					mGroupListsMap.put(group, parseListInfo);
					//不存在重复；
//					for (int i = 0; i < mGroups_buffur.size(); i++) {
//						PttGrp pttGrp = mGroups_buffur.get(i);
//						if (pttGrp.grpID.equals(group.grpID)) {
//							return;
//						}
//					}
					
					if (isSingleGroup) {
						isSingleGroup = false;
						groupListHandler.sendMessage(groupListHandler.obtainMessage(3,
								"ok"));
						return;
					}
					//保证唯一
					int groupIndex = getGroupIndex(group);
					if (groupIndex == -1) {//没这个组
						return;
					}else {
						if (groupIndex == mGroups_buffur.size()) {
							mGroups_buffur.add(groupIndex,group);
						}else {//错位的都放弃；
							if (needSendRegetMessage) {
								Message msg = groupListHandler.obtainMessage();
								msg.what = 4;
								groupListHandler.sendMessageDelayed( msg, 3000);
								needSendRegetMessage = false;
							}
							Logger.i(needLog, tag, "groupIndex != mGroups_buffur.size(),"+groupIndex+"/"+mGroups_buffur.size());
							return;
						}
					}
					int a = mPttGrps.GetCount();
					int b = mGroups_buffur.size();
					if (mPttGrps.GetCount() == mGroups_buffur.size()) {
						// modify by oumogang 2013-05-22
						// 同步，避免 mGroups 空指针；
						synchronized (GroupListUtil.class) {
							mGroups.clear();
							for (int i = 0; i < mGroups_buffur.size(); i++) {
								mGroups.add(mGroups_buffur.get(i));
							}
						}
						mGroups_buffur.clear();
//						mGroupListAdapter.notifyDataSetChanged();
//						hideLoadingProgress();
						groupListHandler.sendMessage(groupListHandler.obtainMessage(3,
								"ok"));
						isGroupListLoading = false;
						receiveCount = 0;
					}
				}
			}
		}

		private int getGroupIndex(PttGrp group) {
			// TODO Auto-generated method stub
			if(mPttGrps!=null){
				for (int i = 0; i < mPttGrps.GetCount(); i++) {
					PttGrp grou = mPttGrps.GetGrpByIndex(i);
					if (group!=null&&grou!=null&&(group.grpID).equals(grou.grpID)) {
						return i;
					}
				}
			}
			return -1;
		}

		private PttGrp getGroup(String info)  {
			// TODO Auto-generated method stub
			// 3ghandset: getstatus
			// 4009(4000,4000,3;4001,灵妖,1;4002,灵碍,1;4003,灵参,3;4004,灵寺,0;4005,灵舞,0;4006,灵溜,0)
			String[] split = info.split(" ");
			if(split.length<3){
				return null;
			}
			String infoString = split[2];
			int end = infoString.indexOf("(");
			String groupNumber = infoString.substring(0, end);
			if (mPttGrps == null) {
				return null;
			}
			mPttGrps.GetCount();
			for (int i = 0; i < mPttGrps.GetCount(); i++) {
				PttGrp group = mPttGrps.GetGrpByIndex(i);
				if (groupNumber.equals(group.grpID)) {
					return group;
				}
			}
			return null;
		}
		// 获取adpater里的数据源
		public ArrayList<GroupListInfo> ParseListInfo(String info) {
			// 3ghandset: getstatus
			// 4009(4000,4000,3;4001,灵妖,1;4002,灵碍,1;4003,灵参,3;4004,灵寺,0;4005,灵舞,0;4006,灵溜,0)
			ArrayList<GroupListInfo> arrlist = new ArrayList<GroupListInfo>();
			try {
				// 判断后面是否有消息体
				if (info.length() > 21) {
					int begin = info.indexOf("(");
					int end = info.lastIndexOf(")");
					info = info.substring(begin + 1, end);
					GroupListInfo grp = null;
					if (info.indexOf(";") > 0) {
						String[] g3_mGroups = info.split(";");
						for (String g3_group : g3_mGroups) {
							grp = this.parseGrpAttributes(g3_group);
							if (grp == null)
								return null;
							arrlist.add(grp);
						}
					} else {
						grp = this.parseGrpAttributes(info);
						if (grp == null)
							return null;
						arrlist.add(grp);
					}

				} else {

					return null;
				}
			} catch (Exception e) {
				return null;
			}

			return arrlist;
		}
		
		private GroupListInfo parseGrpAttributes(String imp_group) {
			String[] attributes = imp_group.split(",");
			if (attributes.length != 3)
				return null;
			GroupListInfo grp = new GroupListInfo();
			grp.GrpNum = attributes[0];
			grp.GrpName = attributes[1];
			grp.GrpState = GetState(attributes[2]);
			Logger.i(needLog, tag, "member："+grp.GrpNum+"--"+grp.GrpName+"--"+grp.GrpState);
			return grp;
		}
		
		//
		private String GetState(String state) {
			try {
				String str = "";
				switch (Integer.valueOf(state)) {
				case 0:
					str = SipUAApp.mContext.getResources().getString(R.string.the_status_1);
					break;
				case 1:
					str = SipUAApp.mContext.getResources().getString(R.string.the_status_2);
					break;
				case 2:
					str = SipUAApp.mContext.getResources().getString(R.string.the_status_3);
					break;
				case 3:
					str = SipUAApp.mContext.getResources().getString(R.string.the_status_4);
					break;

				}
				return str;
			} catch (Exception e) {
				return "";
			}
		}
	};
	protected static boolean needSendRegetMessage;
	// 获取下一组成员
	private static Handler groupListHandler = new Handler() {

		private boolean isReceiverInited;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == 2) {
				// Log.e("tagrefesh", "handler on...");
				String idStr = (String) msg.obj;
				UserAgent ua = Receiver.GetCurUA();
				ua.PttGetGroupList(idStr);// 只管发送
			}
			else if (msg.what == 3) {//加载完成
				mContext.sendBroadcast(new Intent(Contants.ACTION_GROUPLIST_UPDATE_OVER));
				if (Settings.needUpdate2Contacts) {
					int addCount = addGroupList2Contacts();
				}
			}
			else if (msg.what == 4) {//重新获取
				needSendRegetMessage = true;
				getData4GroupList();
			}
		}
	};
	public static List<PttGrp> getGroups() {
		// TODO Auto-generated method stub
		return mGroups;
	}

	static List<Map<String, Object>> mContacts;
	public static int addGroupList2Contacts() {
		// TODO Auto-generated method stub
		ContactManager cm = new ContactManager(SipUAApp.mContext);
		
		boolean hasAdded = false;
		int count = 0;
		boolean needSaveContacts = false;
		if(ua == null || ua.GetAllGrps() == null) return 0;
		PttGrps ptts = ua.GetAllGrps();
		for (int i = 0; i </* mGroups.size()*/ptts.GetCount(); i++) {
			PttGrp pttGrp =/* mGroups.get(i)*/ptts.GetGrpByIndex(i);
			ArrayList<GroupListInfo> arrayList = mGroupListsMap.get(pttGrp);
			if (arrayList != null && arrayList.size()!=0) {
				for (int j = 0; j < arrayList.size(); j++) {
					GroupListInfo groupListInfo = arrayList.get(j);
//					try {
						if (groupListInfo != null) {
							
//							String contact_name = cm.queryNameByNum(groupListInfo.GrpNum);
							boolean flag = cm.queryNumExsit(groupListInfo.GrpNum);
							if(!flag){
								ContactPerson cp = new ContactPerson();
								cp.setContact_num(groupListInfo.GrpNum);
								if(TextUtils.isEmpty(groupListInfo.GrpName)){
									cp.setContact_name(groupListInfo.GrpNum);
								}else{
									cp.setContact_name(groupListInfo.GrpName);
								}
								cm.insertContact(cp);
								count++;
//								hasAdded = ContactUtil.add(groupListInfo.GrpName, groupListInfo.GrpNum);
//								if (hasAdded) {
//									needSaveContacts = true;
//									count++;
//								}
							}
						}
						/*} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
					}*/
				}
			}
			
		}
//		if (needSaveContacts) {
//			try {
//				ContactUtil.saveData();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		return count;
	}

	public static HashMap<PttGrp, ArrayList<GroupListInfo>> getGroupListsMap() {
		// TODO Auto-generated method stub
		if (mGroupListsMap.size() == 0) {
			getData4GroupList();
		}
		return mGroupListsMap;
	}

	public static void unRegisterReceiver() {
		// TODO Auto-generated method stub
		mContext.unregisterReceiver(groupListReceiver);
	}

	public static void registerReceiver() {
		// TODO Auto-generated method stub
		if (intentfilter2 == null) {
			intentfilter2 = new IntentFilter();
			intentfilter2.addAction(Contants.ACTION_GETSTATUS_MESSAGE);
		}
		mContext.registerReceiver(groupListReceiver,intentfilter2);
	}
	public static boolean getDataCurrentGroupList() {
		
		NetChecker.check(SipUAApp.mContext, true);
//		needSendRegetMessage = true;
		ua = Receiver.GetCurUA();
		if (ua == null) {
			// 没有用户,清空前用户组列表信息
			removeDataOfGroupList();
			return false;
		}
		mPttGrps = ua.GetAllGrps();
		if (mPttGrps.GetCount() == 0) {
			// 不在任何组,清空前用户组列表信息
			removeDataOfGroupList();
			return false;
		}
		if (mPttGrps != null) {
			// add by oumogang 2013-05-10
			// 重复出现4009组，先情况缓存；
			isGroupListLoading = true;
			groupListHandler.removeMessages(2);
			PttGrp pttGrp = ua.GetCurGrp();
			isSingleGroup = true;
			groupListHandler.sendMessage(groupListHandler.obtainMessage(2,
						/* pttGrps.GetGrpByIndex(0) */pttGrp.grpID));
			return true;
		}
		return false;
	}
	
	
}
