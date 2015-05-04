package com.zed3.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.zed3.sipua.R;

/**
 * this class use to show some simple dialog.
 * @author oumogang
 */

public class DialogUtil {

	
	private static DialogUtil instance;

	static{
		instance = new DialogUtil();
	}
	private DialogUtil() {
	}
	
	public static DialogUtil getInstance() {
		return instance;
	}
	
	public static abstract class DialogCallBack {
		public abstract void onPositiveButtonClick();
		public abstract void onNegativeButtonClick();
	}

	/**
	 * show dialog to tell user some message;
	 * @param context activity context
	 * @param title about what
	 * @param message message to show 
	 * @param check I know
	 */
	public synchronized static void showCheckDialog(Context context, String title,
			String message, String check) {
		// TODO Auto-generated method stub
		if (!(context instanceof Activity)) {
			Toast.makeText(context,message,0).show();
			return;
		}

		AlertDialog.Builder dialog =new AlertDialog.Builder(context);// 定义一个弹出框对象
		dialog.setTitle(title);//标题
		dialog.setMessage(message);
		dialog.setPositiveButton(check, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// mBtAdapter.cancelDiscovery();
				// seachButton.setText("重新搜索");
				//
				// Bluetooth.serviceOrCilent=ServerOrCilent.CILENT;
				// Bluetooth.needRestartServerOrCilent = true;
				// Bluetooth.mTabHost.setCurrentTab(1);
			}
		});
		// StopDialog.setNegativeButton("取消",new
		// DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// Bluetooth.BlueToothAddress = null;
		// }
		// });
		dialog.show();
		dialog.setCancelable(false);
	}

	/**
	 * show dialog to ask user to select yes or no;
	 * 
	 * @param context activity context
	 * @param title about what
	 * @param message message to show
	 * @param check I know
	 * @param callBack see {@DialogCallBack}
	 * @see #showSelectDialog
	 */
	public synchronized static void showSelectDialog(Context context,
			String title,String message, String check,
			final DialogCallBack callBack) {
		// TODO Auto-generated method stub
		if (!(context instanceof Activity)) {
			Toast.makeText(context,message,0).show();
			return;
		}
        //modify by wlei 2014-10-18
//		AlertDialog.Builder dialog =new AlertDialog.Builder(context);// 定义一个弹出框对象
//		dialog.setTitle(title);//标题
//		//add by wlei 2014-9-29 修改自动换行时，英文单词分开的问题
//		View vv = LayoutInflater.from(context).inflate(R.layout.dialog_item, null);
//		TextView textView = (TextView) vv.findViewById(R.id.message);
//		textView.setText(DialogMessageTool.getString(20,message));
//		dialog.setView(vv);
////		dialog.setMessage(DialogMessageTool.getString(message));
////		dialog.setMessage(message);
//		dialog.setPositiveButton(check, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				callBack.onPositiveButtonClick();
//			}
//		});
//		dialog.setNegativeButton(
//				context.getResources().getString(R.string.cancel),
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						callBack.onNegativeButtonClick();
//					}
//				});
//		dialog.show();
//		dialog.setCancelable(false);
		final CustomDialog dialog = new CustomDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setPositiveButton(check, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				callBack.onPositiveButtonClick();
			}
		});
		dialog.setNegativeButton(context.getResources().getString(R.string.cancel), new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				callBack.onNegativeButtonClick();
			}
		});
		dialog.setCancelable(false);
	}

	public synchronized static ProgressDialog showProcessDailog(
			Context context, String message) {
		// TODO Auto-generated method stub
		if (!(context instanceof Activity)) {
			Toast.makeText(context, message, 0).show();
			return null;
		}
		ProgressDialog pd = new ProgressDialog(context);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				// exitApp();
			}
		});
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage(message);
		pd.show();
		return pd;
	}

	public synchronized static void dismissProcessDailog(
			ProgressDialog processDailog) {
		// TODO Auto-generated method stub
		if (processDailog != null && processDailog.isShowing()) {
			processDailog.dismiss();
		}
	}

}
