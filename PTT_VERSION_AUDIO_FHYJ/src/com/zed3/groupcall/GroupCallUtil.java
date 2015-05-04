package com.zed3.groupcall;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Message;
import android.util.Log;

import com.zed3.bluetooth.MyPhoneStateListener;
import com.zed3.log.MyLog;
import com.zed3.media.TipSoundPlayer;
import com.zed3.media.TipSoundPlayer.Sound;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.baiduMap.LocationOverlayDemo;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;

public class GroupCallUtil {
	private static Lock lock = new ReentrantLock();
	private static String tag = "GroupCallUtil";
	private static UserAgent mUserAgent;
	private static Object pttGrps;
	public static boolean mIsPttDown;
	
	public static final int STATE_IDLE = 0;
	public static final int STATE_LISTENING = 1;
	public static final int STATE_TALKING = 2;
	public static final int STATE_QUEUE = 3;
	public static final int STATE_SHUTDOWN = 4;
	public static final int STATE_INITIATING = 4;
	
	public static final String STATE_IDLE_STR = "STATE_IDLE";
	public static final String STATE_LISTENING_STR = "STATE_LISTENING";
	public static final String STATE_TALKING_STR = "STATE_TALKING";
	public static final String STATE_QUEUE_STR = "STATE_QUEUE";
	public static final String STATE_SHUTDOWN_STR = "STATE_SHUTDOWN";
	
	private static int mGroupCallState = STATE_SHUTDOWN;
	
//	public static boolean PressPTT(boolean status) {
//
////		UserAgent ua = Receiver.GetCurUA();
////		if (ua != null) {
////			return ua.OnPttKey(status);
////		}
//		//通过调用makeGroupCall（），更新UI更全面。
//		makeGroupCall(status, true);
//		return false;
//	}
	
	/**
	 * makeGroupCall
	 * @param down
	 * @param needChangeUi
	 */
	public static void makeGroupCall(boolean down,boolean needChangeUi) {
		// TODO Auto-generated method stub
		try {
			lock.lock();
			MyLog.e(tag, "makeGroupCall("+down+")");
			//sim卡电话来电中、去电中或通话中，不处理话权申请  modify by mou 2014-07-29
			if (down && MyPhoneStateListener.getInstance().isInCall()) {
				MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
				Log.i(tag,"MyPhoneStateListener.getInstance().isInCall() is true ignore ");
				return;
			}
			//检查网络
			boolean netWorkWorking = NetChecker.check(SipUAApp.mContext, true);
			if (down && !netWorkWorking) {
				MyLog.e(tag, "makeGroupCall("+down+") NetChecker.check() false");
				return;
			}
			if (needChangeUi) {
				changeUI(down);
			}
			TipSoundPlayer.getInstance().play(down?Sound.PTT_DOWN:Sound.PTT_UP);
			if (CallUtil.isInCall()) {
				MyLog.e(tag, "makeGroupCall("+down+") isInCall() true");
			}
			//need not check here,modify by oumogang 2014-04-09
//	        if (mUserAgent == null) {
//	        	mUserAgent = Receiver.GetCurUA();
//			}
			if (!netWorkWorking) {
				MyLog.e(tag, "makeGroupCall("+down+") NetChecker.check() false");
				return;
			}
	        mUserAgent = Receiver.GetCurUA();
			if (mUserAgent != null) {
				mUserAgent.OnPttKey(down);
			}else {
				MyLog.e(tag, "makeGroupCall("+down+") pressPTT("+down+") ,ua = null");
			}
			mIsPttDown = down;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			//release lock for other thread
			//add by oumogang 2014-05-07
			lock.unlock();
		}
	}
	
	private static void changeUI(boolean down) {
		// TODO Auto-generated method stub
		TalkBackNew.isPttPressing = down;
		LocationOverlayDemo.isPttPressing = down;
		if (TalkBackNew.isResume) {
			//更新界面  
			TalkBackNew instance = TalkBackNew.getInstance();
			if (instance != null) {
				Message msg = instance.pttPressHandler.obtainMessage();
				msg.what = down? 1:0;
				instance.pttPressHandler.sendMessage(msg);
				//设置波形和发送信令
//				TalkBackNew.lineListener = down ?instance:null;
				if (!TalkBackNew.checkHasCurrentGrp(SipUAApp.mContext)) {
					return;
				}
			}
		}
		else if (LocationOverlayDemo.isResume) {
			//更新界面  
			LocationOverlayDemo instance = LocationOverlayDemo.getInstance();
			if (instance != null) {
				Message msg = instance.pttPressHandler.obtainMessage();
				msg.what = down? 1:0;
				instance.pttPressHandler.sendMessage(msg);
				if (!LocationOverlayDemo.checkHasCurrentGrp(SipUAApp.mContext)) {
					return;
				}
			}
		}
	}
	public static PttGrp switchCurrentGroup(int position) {
		// TODO Auto-generated method stub
		if (!NetChecker.check(SipUAApp.mContext, true)) {
			MyLog.e(tag, "switchCurrentGroup() NetChecker.check() false");
			return null;
		}
		// if(Receiver.GetCurUA().GetCurGrp()!=pttGrps.GetGrpByIndex(position)){
		// Receiver.GetCurUA().SetCurGrp(pttGrps.GetGrpByIndex(position));
		// }
		/**
		 * android.os.NetworkOnMainThreadException should't use main
		 * thread to send message,modify by oumogang 2014-03-07
		 */
		final UserAgent ua = Receiver.GetCurUA();
		if (mUserAgent == null) {
        	mUserAgent = Receiver.GetCurUA();
		}
		pttGrps = mUserAgent.GetAllGrps();
		final PttGrp grpByIndex = ((PttGrps) pttGrps).GetGrpByIndex(position);
		PttGrp curGrp = mUserAgent.GetCurGrp();
		if (curGrp != null && grpByIndex != null
				&& curGrp != grpByIndex) {
			ua.SetCurGrp(grpByIndex);
			return grpByIndex;
		}else {
			return null;
		}
	}

	public static int getGroupCallState() {
		// TODO Auto-generated method stub
		return mGroupCallState;
	}
	public static void setGroupCallState(int state) {
		// TODO Auto-generated method stub
		mGroupCallState = state;
	}
	
	public static String getGroupCallStateStr(int state) {
		// TODO Auto-generated method stub
		String stateStr = "";
		switch (state) {
		case STATE_IDLE:
			stateStr = "STATE_IDLE";
			break;
		case STATE_LISTENING:
			stateStr = "STATE_LISTENING";
			break;
		case STATE_QUEUE:
			stateStr = "STATE_QUEUE";
			break;
		case STATE_SHUTDOWN:
			stateStr = "STATE_SHUTDOWN";
			break;
		case STATE_TALKING:
			stateStr = "STATE_TALKING";
			break;
		default:
			stateStr = "unkown state";
			break;
		}
		return stateStr;
	} 
	
	private static String talkGroup;
	public static void setTalkGrp(String grp) {
		talkGroup = grp;
	}

	public static String getTalkGrp() {
		return talkGroup;
	}

	private static String ActionMode;

	public static void setActionMode(String action) {
		ActionMode = action;
	}

	public static String getActionMode() {
		return ActionMode;
	}
	
	
	/**
	 * @author Administrator 
	 * @param isBroadcast 是否广播触发，如果是才更新UI
	 **/
//	public  static void pressPTT(boolean pressed,boolean isBroadcast) { 
////		if (PressPTT(pressed) && TalkBackNew.isResume && isBroadcast){
////			//您不在任何对讲组中
////				TalkBackNew.setPttBackground(pressed);
////		}
//		//通过调用makeGroupCall（），更新UI更全面。
//		makeGroupCall(pressed, true);
//	}
	
}
