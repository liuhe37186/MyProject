package com.zed3.window;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.Build;
import android.view.WindowManager.LayoutParams;

import com.zed3.utils.LogUtil;
/**
 * control keyguard and other things about window. add by mou 2014-12-14
 * @author oumogang
 *
 */
public class MyWindowManager {
	private static KeyguardLock mKeyguardLock;
	private static final String TAG = "MyWindowManager";
	private MyWindowManager(){
	}
	private static final class InstanceCreater {
		public static MyWindowManager sInstance = new MyWindowManager();
	}
	public static MyWindowManager getInstance() {
		// TODO Auto-generated method stub
		return InstanceCreater.sInstance;
	}
	private void makeLog(String tag, String logMsg) {
		// TODO Auto-generated method stub
		LogUtil.makeLog(tag, logMsg);
	}
	/**
	 * unlock window 。 add by mou 2014-12-12
	 * @param activity
	 */
	public synchronized void disableKeyguard(Activity activity) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("disableKeyguard(by "+activity.getClass()+")");
		if (Build.MODEL.contains("Coolpad 7296")) {
			// 解决Coolpad 7296语音来电不解锁的问题 modified by liangzhang 2014-11-25
			activity.getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
			builder.append(" add window flag FLAG_DISMISS_KEYGUARD");
		} 
		else if (Build.VERSION.SDK_INT >= 14) {
			activity.getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			builder.append(" add window flag FLAG_SHOW_WHEN_LOCKED");
		}
		else {
			KeyguardManager keyGuardManager = (KeyguardManager) activity
					.getSystemService(Context.KEYGUARD_SERVICE);
			mKeyguardLock = keyGuardManager.newKeyguardLock(activity.getClass().toString());
			if (keyGuardManager.inKeyguardRestrictedInputMode()) {
				mKeyguardLock.disableKeyguard();
				builder.append(" disableKeyguard");
			}
		}
		makeLog(TAG, builder.toString());
	}
	/**
	 * relock window 。add by mou 2014-12-12
	 * @param activity
	 */
	public synchronized void reenableKeyguard(Activity activity) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("reenableKeyguard(by "+activity.getClass()+")");
		if( mKeyguardLock!= null){
			mKeyguardLock.reenableKeyguard();
			builder.append(" reenableKeyguard()");
		}else {
			builder.append(" mKeyguardLock is null ignore");
		}
		makeLog(TAG, builder.toString());
	}
}
