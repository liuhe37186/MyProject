package com.zed3.dialog;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.utils.DialogMessageTool;

public class CustomDialog  {
	/**
	 * 自定义对话框，指定对话框的宽和高
	 */
	
	private Context mContext;
	private Window window;
	private TextView mTitle;
	private TextView mMessage;
	private Button mBtn_ok;
	private Button mBtn_cancel;
	private AlertDialog mDialog;
	
	
	public CustomDialog(Context context){
		mContext = context;
	    mDialog = new AlertDialog.Builder(context).create();
	    mDialog.show();
		window = mDialog.getWindow();
		window.setContentView(R.layout.dialog_item);
		mTitle = (TextView) window.findViewById(R.id.title);
		mMessage = (TextView) window.findViewById(R.id.message);
		mBtn_ok = (Button) window.findViewById(R.id.ok);
		mBtn_cancel = (Button) window.findViewById(R.id.cancel);
	}
	
	public void setTitle(String title){
		mTitle.setText(title);
	}
	public void setTitle(int id){
		mTitle.setText(id);
	}
	
	public void setMessage(String message){
		int width = (int)(mContext.getResources().getDisplayMetrics().density*284+0.5f);
		String m = DialogMessageTool.getString(width,mMessage.getTextSize(), message);
		mMessage.setText(m);
	}
	
	public void setPositiveButton(String text,OnClickListener onClickListener){
		mBtn_ok.setText(text);
		mBtn_ok.setOnClickListener(onClickListener);
	}
	public void setNegativeButton(String text,OnClickListener onClickListener){
		mBtn_cancel.setText(text);
		mBtn_cancel.setOnClickListener(onClickListener);
	}
	
	public void setCancelable(boolean flag){
		mDialog.setCancelable(flag);
	}
	public void dismiss(){
		mDialog.dismiss();
	}

}
