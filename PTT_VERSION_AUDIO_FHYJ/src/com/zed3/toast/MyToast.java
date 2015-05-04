package com.zed3.toast;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.Tools;

public class MyToast {
	private static long lastTime;
	private static Toast toastStart;

	/**
	 * 当需要在后台toast的时候调用
	 *  @param needToast
	 *            是否需要显示toast
	 * @param id
	 *            stringid ,eg. R.string.xxx
	 * */
	public static void showToastInBg(boolean needToast,Context mContext,int id){
		if(needToast){
			makeText(mContext, getToastView(mContext, mContext.getResources().getString(id)));
		}
	}
	/**
	 * @param needToast
	 *            是否需要显示toast
	 * @param id
	 *            stringid ,eg. R.string.xxx
	 * **/
	public static void showToast(boolean needToast, Context mContext, int id) {
		if(mContext == null) mContext = SipUAApp.mContext;
		showToast(needToast, mContext, mContext.getResources().getString(id));
	}

	/**
	 * @param needToast
	 *            是否需要显示toast
	 * @param string
	 *            要显示的文本内容
	 * **/
	public static void showToast(boolean needToast, Context mContext,
			String string) {
		if(mContext == null) mContext = SipUAApp.mContext;
		if (needToast && !Tools.isRunBackGroud(mContext)) {
			makeText(mContext, getToastView(mContext, string));
		}
	}

	// 文本
	private static View getToastView(Context context, String text) {
		View toastRoot = LayoutInflater.from(context).inflate(R.layout.toast,
				null);
		TextView message = (TextView) toastRoot.findViewById(R.id.message);
		message.setText(text);
		return toastRoot;
	}

	// modify by oumogang 2013-05-16
	// 避免toast堆积
	private static void makeText(Context context, View v) {
		// if (checkTime()) {
//		 mHandler.removeCallbacks(r);

		if (toastStart == null) {
			toastStart = new Toast(context);
			// toastStart.setGravity(Gravity.CENTER_VERTICAL, 0, 10);
			toastStart.setDuration(Toast.LENGTH_LONG);
			toastStart.setView(v);
		} else {
			toastStart.setView(v);
		}
//		mHandler.postDelayed(r, 2000);

		toastStart.show();
		// }
	}
	 private static Handler mHandler = new Handler();
	    private static Runnable r = new Runnable() {
	        public void run() {
	        	toastStart.cancel();
	        }
	    };


	// //add by oumogang 2013-05-16
	// //避免toast堆积
	// private static boolean checkTime() {
	// long thisTime = System.currentTimeMillis();
	// // TODO Auto-generated method stub
	// if (lastTime == 0) {//第一次
	// lastTime = thisTime;
	// return true;
	// }else if (thisTime - lastTime > 3000) {//间隔3秒以上
	// lastTime = thisTime;
	// return true;
	// }
	// return false;
	// }

}
