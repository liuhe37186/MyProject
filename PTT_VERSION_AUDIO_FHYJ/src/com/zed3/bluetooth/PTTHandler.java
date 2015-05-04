package com.zed3.bluetooth;

import android.os.Message;
import android.util.Log;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.baiduMap.LocationOverlayDemo;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;



public class PTTHandler{
	
	
	private static final String tag = "PTTHandler";
	private static boolean mDown;
	public static void pressPTT(boolean down){
		mDown = down;
		TalkBackNew.isPttPressing = down;
		LocationOverlayDemo.isPttPressing = down;
		/*if(listener != null){
			Log.i(tag, "pressPTT("+down+"),listener != null,listener.pressPTT("+down+")");
			listener.pressPTT(down);
		}
		else */
		if (TalkBackNew.isResume) {
//			Log.i(tag, "pressPTT("+down+"),listener == null TalkBackNew.isResume");
//			TalkBackNew.mContext.pressPTT(down);
			//更新界面  
			TalkBackNew instance = TalkBackNew.getInstance();
			if (instance != null) {
				Message msg = instance.pttPressHandler.obtainMessage();
				msg.what = down? 1:0;
				instance.pttPressHandler.sendMessage(msg);
				//设置波形和发送信令
				TalkBackNew.lineListener = down ?instance:null;
				if (!TalkBackNew.checkHasCurrentGrp(SipUAApp.mContext)) {
					return;
				}
			}
		}
		else if (LocationOverlayDemo.isResume) {
//			Log.i(tag, "pressPTT("+down+"),listener == null LocationOverlayDemo.isResume");
//			LocationOverlayDemo.mContext.pressPTT(down);
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
//		else {
//			Log.i(tag, "pressPTT("+down+"),listener == null");
//			UserAgent ua = Receiver.GetCurUA();
//			ua.OnPttKey(down);
//		}
		
		GroupCallUtil.makeGroupCall(down,true);
	}
	private static PTTListener listener;
	public static void setPTTListener(PTTListener listener) {
		Log.i(tag, "setPTTListener()...");
		PTTHandler.listener = listener;
	}
}
