package com.zed3.audio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.DialogMessageTool;

public class CustomDialog extends Activity implements OnClickListener {

	private static final String tag = "CustomDialog";
	//
	private static final int STATE_AUDIO_RECORD_STARTPLAYING_ERROR = 0;
	private static final int STATE_AUDIO_RECORD_STOP_ERROR = 1;
	private static final String CONTROL_STATE = "control_state";
	private static final String CONTROL_DEVICE_NAME = "control_device_name";
	private TextView mTitleTV;
	private TextView mMsgTV;
	private TextView mCancelTV;
	private TextView mCommitTV;
	private int mState = -1;
	private String mDeviceName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zmbluetooth_control_dialog);
		mTitleTV = (TextView) findViewById(R.id.title_tv);
		mMsgTV = (TextView) findViewById(R.id.msg_tv);
		mCancelTV = (TextView) findViewById(R.id.cancel_tv);
		mCommitTV = (TextView) findViewById(R.id.ok_tv);

		mCancelTV.setOnClickListener(this);
		mCommitTV.setOnClickListener(this);

		Intent intent = getIntent();
		mState = intent.getIntExtra(CONTROL_STATE, -1);
		mDeviceName = intent.getStringExtra(CONTROL_DEVICE_NAME);
		switch (mState) {
		case STATE_AUDIO_RECORD_STARTPLAYING_ERROR:
			initTextViews(
					getResources().getString(R.string.au_anomaly),
					getResources().getString(R.string.au_anomaly_notify_1)
							+ getResources().getString(
									R.string.au_anomaly_notify_2),
					getResources().getString(R.string.cancel), getResources()
							.getString(R.string.ok_know));
			break;
		case STATE_AUDIO_RECORD_STOP_ERROR:
			initTextViews(getResources().getString(R.string.bl_notify_1),
					getResources().getString(R.string.bl_notify_2),
					getResources().getString(R.string.cancel), getResources()
							.getString(R.string.bl_notify_ok));
			break;
		default:
			finish();
			break;
		}

	}

	private void initTextViews(String title, String msg, String cancel,
			String commit) {
		// TODO Auto-generated method stub
		mTitleTV.setText(title);
		// 解决提示对话框，在英文的时候，英文单词换行分割的问题。 add by lwang 2014-10-28
		int width = (int)(this.getResources().getDisplayMetrics().density*296+0.5f);
		msg = DialogMessageTool.getString(width, mMsgTV.getTextSize(), msg);
		mMsgTV.setText(msg);
		mCancelTV.setText(cancel);
		mCommitTV.setText(commit);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mState == -1) {
			Log.e(tag, "unknow state error");
			finish();
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ok_tv:
			switch (mState) {
			//enable bluetooth
			case STATE_AUDIO_RECORD_STARTPLAYING_ERROR:

				break;
			//select bluetooth
			case STATE_AUDIO_RECORD_STOP_ERROR:
				break;

			default:
				Log.e(tag, "unknow state error");
				break;
			}
			finish();
			break;
		case R.id.cancel_tv:
			switch (mState) {
			case STATE_AUDIO_RECORD_STARTPLAYING_ERROR:
				finish();
				break;
			//select bluetooth
			case STATE_AUDIO_RECORD_STOP_ERROR:
				finish();
				break;

			default:
				Log.e(tag, "unknow state error");
				finish();
				break;
			}

			finish();
			break;

		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent;
		// TODO Auto-generated method stub
		switch (requestCode) {
		//enable bluetooth
		case STATE_AUDIO_RECORD_STARTPLAYING_ERROR:
			finish();
			break;
		//select bluetooth
		case STATE_AUDIO_RECORD_STOP_ERROR:
			finish();
			break;

		default:
			finish();
			Log.e(tag, "unknow state error");
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static void askUserToConnectBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext, CustomDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE,STATE_AUDIO_RECORD_STOP_ERROR);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToCheckAudio() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext, CustomDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE,STATE_AUDIO_RECORD_STARTPLAYING_ERROR);
		SipUAApp.mContext.startActivity(intent);
	}

}
