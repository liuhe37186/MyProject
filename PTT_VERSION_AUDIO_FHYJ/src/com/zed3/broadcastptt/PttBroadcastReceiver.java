package com.zed3.broadcastptt;

import org.zoolu.tools.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;

public class PttBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		String str = intent.toString();
		int d = intent.getIntExtra("PTT_STATUS", 0);
		if (Receiver.mSipdroidEngine == null || !Receiver.mSipdroidEngine.isRegistered()) {
			return;
		}
		if (d == 1 /*&&  timeSpace > 500*/) {
			MyLog.e("hTag", "receive down pttkey");
			if (!NetChecker.check(context, true)) {
				return;
			}
			if (!TalkBackNew.checkHasCurrentGrp(context)) {
				return;
			}
			TalkBackNew.isPttPressing = true;
			if (TalkBackNew.isResume) {
				TalkBackNew.lineListener = TalkBackNew.mtContext;
			}
			GroupCallUtil.makeGroupCall(true,true);
			TalkBackNew.isPttPressing = true;
		} else if (d == 0) {
			MyLog.e("hTag", "receive up pttkey");
			if(TalkBackNew.isPttPressing){
			if (!NetChecker.check(context, true)) {
				return;
			}
			if (!TalkBackNew.checkHasCurrentGrp(context)) {
				return;
			}
			TalkBackNew.isPttPressing = false;
			if (TalkBackNew.isResume) {
				TalkBackNew.lineListener = null;
			}
			GroupCallUtil.makeGroupCall(false,true);
			}
		}
	}

}
