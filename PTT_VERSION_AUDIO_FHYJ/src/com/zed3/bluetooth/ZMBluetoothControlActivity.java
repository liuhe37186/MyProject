package com.zed3.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.zed3.audio.AudioUtil;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.utils.Tools;

public class ZMBluetoothControlActivity extends Activity implements
		OnClickListener, OnSppConnectStateChangedListener,
		OnBluetoothAdapterStateChangedListener,
		OnBluetoothConnectStateChangedListener {

	private static final String tag = "ZMBluetoothControlActivity";
	//
	private static final int STATE_ENABLE_BLUETOOTH = 0;
	private static final int STATE_CONNECT_AUDIO = 1;
	private static final int STATE_CONNECT_ZM_AUDIO = 2;
	private static final int STATE_CONNECT_SPP = 3;
	private static final int STATE_CHECK_ZM_BLUETOOTH = 4;
	private static final int STATE_RECONNECT_ZM_BLUETOOTH = 5;
	private static final int STATE_DISABLE_BLUETOOTH = 6;
	private static final int STATE_SELECT_HEADSET_BLUETOOTH = 7;
	private static final String CONTROL_STATE = "control_state";
	private static final String CONTROL_DEVICE_NAME = "control_device_name";
	private static Activity mContext;
	private TextView mTitleTV;
	private TextView mMsgTV;
	private TextView mCancelTV;
	private TextView mCommitTV;
	public static int mState = -1;
	private String mDeviceName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		ZMBluetoothManager.getInstance().setSppConnectStateListener(this);
		BluetoothSCOStateReceiver.setOnBluetoothAdapterStateChangedListener(this);
		BluetoothSCOStateReceiver.setOnBluetoothConnectStateChangedListener(this);
		setContentView(R.layout.zmbluetooth_control_dialog);
		mTitleTV = (TextView) findViewById(R.id.title_tv);
		mMsgTV = (TextView) findViewById(R.id.msg_tv);
		mCancelTV = (TextView) findViewById(R.id.cancel_tv);
		mCommitTV = (TextView) findViewById(R.id.ok_tv);

		mCancelTV.setOnClickListener(this);
		mCommitTV.setOnClickListener(this);

		ZMBluetoothManager.getInstance().isDeviceSupportBluetooth();
		ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled();

		Intent intent = getIntent();
		mState = intent.getIntExtra(CONTROL_STATE, -1);
		mDeviceName = intent.getStringExtra(CONTROL_DEVICE_NAME);
		switch (mState) {
		case STATE_ENABLE_BLUETOOTH:
			initTextViews(getResources().getString(R.string.bl_status_1),
					getResources().getString(R.string.bl_off_notfiy),
					getResources().getString(R.string.dis_hm), getResources()
							.getString(R.string.open_bl));
			break;
		case STATE_CONNECT_AUDIO:
			initTextViews(getResources().getString(R.string.dis_bl_hm),
					getResources().getString(R.string.bl_notify_2),
					getResources().getString(R.string.cancel), getResources()
							.getString(R.string.bl_notify_ok));
			break;
		case STATE_CONNECT_ZM_AUDIO:
			if (mDeviceName != null && !mDeviceName.equals("")) {
				initTextViews(getResources().getString(R.string.dis_bl_hm),
						getResources().getString(R.string.blueTooth_1) + mDeviceName
								+ getResources().getString(R.string.blueTooth_2),
						getResources().getString(R.string.keep), getResources()
								.getString(R.string.settings));
			} else {
				initTextViews(getResources().getString(R.string.dis_bl_hm),
						getResources().getString(R.string.blueTooth_3),
						getResources().getString(R.string.keep), getResources()
								.getString(R.string.settings));
			}
			break;
		case STATE_CHECK_ZM_BLUETOOTH:
			initTextViews(getResources().getString(R.string.bl_port_notify),
					getResources().getString(R.string.hm_notify),
					getResources().getString(R.string.cancel), getResources()
							.getString(R.string.try_again));
			break;
		case STATE_RECONNECT_ZM_BLUETOOTH:
			initTextViews(getResources().getString(R.string.dising_hm),
					getResources().getString(R.string.dis_notify),
					getResources().getString(R.string.cancel), getResources()
							.getString(R.string.try_again));
			break;
		case STATE_CONNECT_SPP:
			// initTextViews("蓝牙语音未连接","正在连接手咪","取消","现在连接");
			break;
		case STATE_DISABLE_BLUETOOTH:
			initTextViews(getResources().getString(R.string.turn_off_bl), "",
					getResources().getString(R.string.cancel), getResources()
							.getString(R.string.ok));
			break;
		case STATE_SELECT_HEADSET_BLUETOOTH:
			initTextViews(getResources().getString(R.string.turn_on_bl),
					getResources().getString(R.string.bl_off_notify),
					getResources().getString(R.string.cancel), getResources()
							.getString(R.string.ok));
			break;
		default:
			break;
		}

	}

	private void initTextViews(String title, String msg, String cancel,
			String commit) {
		// TODO Auto-generated method stub
		mTitleTV.setText(title);
		mMsgTV.setText(msg);
		mCancelTV.setText(cancel);
		mCommitTV.setText(commit);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		ZMBluetoothManager.getInstance().removeSppConnectStateListener(this);
		BluetoothSCOStateReceiver.reMoveOnBluetoothAdapterStateChangedListener(this);
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
			case STATE_ENABLE_BLUETOOTH:
				ZMBluetoothManager.getInstance().enableAdapter();
				Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, STATE_ENABLE_BLUETOOTH);
//              intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//              intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//              startActivityForResult(intent, 3);

				break;
			// select bluetooth
			case STATE_CONNECT_AUDIO:
				intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, STATE_CONNECT_AUDIO);
				break;
			case STATE_CONNECT_ZM_AUDIO:
				intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, STATE_CONNECT_SPP);
				break;
			case STATE_CONNECT_SPP:
				intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, STATE_CONNECT_SPP);
				break;
			case STATE_CHECK_ZM_BLUETOOTH:
				// if
				// (ZMBluetoothManager.getInstance().getSPPConnectedDevice()==null)
				// {
				// }else {
				// ZMBluetoothManager.getInstance().connectSPP(getApplicationContext(),
				// ZMBluetoothManager.getInstance().getSPPConnectedDevice());
				// }

				ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
				finish();
				break;
			case STATE_RECONNECT_ZM_BLUETOOTH:
				// if
				// (ZMBluetoothManager.getInstance().getSPPConnectedDevice()==null)
				// {
				// }else {
				// ZMBluetoothManager.getInstance().connectSPP(getApplicationContext(),
				// ZMBluetoothManager.getInstance().getSPPConnectedDevice());
				// }
				// 有可能语音也已经断开，需要重新检测和连接。
				// if (ZMBluetoothManager.getInstance().mLastSPPConnectDevice !=
				// null) {
				// ZMBluetoothManager.getInstance().connectSPP(ZMBluetoothManager.getInstance().mLastSPPConnectDevice);
				// }else {
				// ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
				// }
				ZMBluetoothManager.getInstance().disConnectZMBluetooth(
						SipUAApp.mContext);
				ZMBluetoothManager.getInstance().connectZMBluetooth(
						SipUAApp.mContext);
				finish();
				break;
			case STATE_DISABLE_BLUETOOTH:
				// intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				// startActivityForResult(intent, STATE_DISABLE_BLUETOOTH);
				ZMBluetoothManager.getInstance().disableAdapter();
				break;
			case STATE_SELECT_HEADSET_BLUETOOTH:
				ZMBluetoothManager.getInstance().enableAdapter();
				intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, STATE_SELECT_HEADSET_BLUETOOTH);
				break;

			default:
				Log.e(tag, "unknow state error");
				break;
			}
			break;
		case R.id.cancel_tv:
			switch (mState) {
			// enable bluetooth
			case STATE_ENABLE_BLUETOOTH:
//				if (TalkBackNew.getInstance() != null) {
//					TalkBackNew.getInstance().reInitBluetoothButton(false);
//					if (TalkBackNew.mLastAudioMode != 0) {
//						TalkBackNew.getInstance().reInitModeButtons(TalkBackNew.mLastAudioMode);
//					}else {
//						TalkBackNew.getInstance().reInitModeButtons(AudioUtil.MODE_SPEAKER);
//					}
//				}else {
//					TalkBackNew.mIsBTServiceStarted = false;
//					TalkBackNew.mAudioMode = AudioUtil.MODE_SPEAKER;
//				}
				ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(false);
				ZMBluetoothManager.getInstance().disableAdapter();
				finish();
				break;
			//select bluetooth
			case STATE_CONNECT_AUDIO:
				if (TalkBackNew.getInstance() != null) {
					TalkBackNew.getInstance().reInitBluetoothButton(false);
				}else {
					TalkBackNew.mIsBTServiceStarted = false;
				}
				ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(false);
				ZMBluetoothManager.getInstance().disableAdapter();
				finish();
				break;
			case STATE_CONNECT_ZM_AUDIO:
				if (TalkBackNew.getInstance() != null) {
					TalkBackNew.getInstance().reInitBluetoothButton(false);
				}else {
					TalkBackNew.mIsBTServiceStarted = false;
				}
				ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(false);
				// need not disable bluetooth,user can use bluetooth for call.
				// ZMBluetoothManager.getInstance().disableAdapter();

				finish();
				break;
			case STATE_CONNECT_SPP:
				if (TalkBackNew.getInstance() != null) {
					TalkBackNew.getInstance().reInitBluetoothButton(false);
				}else {
					TalkBackNew.mIsBTServiceStarted = false;
				}
				ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(false);
				ZMBluetoothManager.getInstance().disableAdapter();
				finish();
				break;
			case STATE_CHECK_ZM_BLUETOOTH:
				if (TalkBackNew.getInstance() != null) {
					TalkBackNew.getInstance().reInitBluetoothButton(false);
				}else {
					TalkBackNew.mIsBTServiceStarted = false;
				}
				ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(false);
				ZMBluetoothManager.getInstance().disableAdapter();
				finish();
				break;
			case STATE_RECONNECT_ZM_BLUETOOTH:
				if (TalkBackNew.getInstance() != null) {
					TalkBackNew.getInstance().reInitBluetoothButton(false);
				}else {
					TalkBackNew.mIsBTServiceStarted = false;
				}
				ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(false);
				ZMBluetoothManager.getInstance().disableAdapter();
				finish();
				break;
			case STATE_DISABLE_BLUETOOTH:
				// intent = new
				// Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				// startActivityForResult(intent, STATE_DISABLE_BLUETOOTH);
				// ZMBluetoothManager.getInstance().disableAdapter();
				finish();
				break;
			case STATE_SELECT_HEADSET_BLUETOOTH:
//				if (TalkBackNew.getInstance() != null) {
//					TalkBackNew.getInstance().reInitModeButtons(TalkBackNew.mLastAudioMode);
//				}else {
//					TalkBackNew.mAudioMode = TalkBackNew.mLastAudioMode;
//				}
//				AudioUtil.getInstance().setCustomMode(AudioUtil.TYPE_GROUPCALL,TalkBackNew.mLastAudioMode);
//				AudioUtil.getInstance().setAudioConnectMode(TalkBackNew.mLastAudioMode);
//				ZMBluetoothManager.getInstance().disableAdapter();
//				finish();

				break;

			default:
				Log.e(tag, "unknow state error");
				finish();
				break;
			}
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
		case STATE_ENABLE_BLUETOOTH:
			finish();
			if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
				Log.i(tag, "系统蓝牙未打开");
				askUserToEnableBluetooth();
			}else {
				Log.i(tag, "系统蓝牙已打开");
				ZMBluetoothManager.getInstance().connectZMBluetooth(getApplicationContext());
			}
			break;
		//select bluetooth
		case STATE_CONNECT_AUDIO:
			finish();
			//ZMBluetoothManager.getInstance().connectSCO(SipUAApp.mContext);
			ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
			break;
		case STATE_CONNECT_ZM_AUDIO:
			finish();
			ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
			break;
		case STATE_CONNECT_SPP:
			ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
			finish();
			break;
		case STATE_CHECK_ZM_BLUETOOTH:
			ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
			finish();
			break;
		case STATE_DISABLE_BLUETOOTH:
			if (ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
				askUserToDisableBluetooth();
			}else {
				finish();
			}
			break;
		case STATE_SELECT_HEADSET_BLUETOOTH:
//			if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
//				Toast.makeText(getApplicationContext(), getResources().getString(R.string.bl_off_notify_1), 0).show();
//				if (TalkBackNew.getInstance() != null) {
//					TalkBackNew.getInstance().reInitModeButtons(TalkBackNew.mLastAudioMode);
//				}else {
//					TalkBackNew.mAudioMode = TalkBackNew.mLastAudioMode;
//				}
//				AudioUtil.getInstance().setCustomMode(AudioUtil.TYPE_GROUPCALL,TalkBackNew.mLastAudioMode);
//				AudioUtil.getInstance().setAudioConnectMode(TalkBackNew.mLastAudioMode);
//			}else {
//				if (TalkBackNew.getInstance() != null) {
//					TalkBackNew.getInstance().reInitModeButtons(AudioUtil.MODE_BLUETOOTH);
//					TalkBackNew.getInstance().setAudioMode(AudioUtil.MODE_BLUETOOTH);
//				}else {
//					TalkBackNew.mAudioMode = AudioUtil.MODE_BLUETOOTH;
//				}
//				AudioUtil.getInstance().setCustomMode(AudioUtil.TYPE_GROUPCALL,AudioUtil.MODE_BLUETOOTH);
//				AudioUtil.getInstance().setAudioConnectMode(AudioUtil.MODE_BLUETOOTH);
//			}
			finish();
			break;

		default:
			Log.e(tag, "unknow state error");
			finish();
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static void askUserToSelectHeadSetBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE,STATE_SELECT_HEADSET_BLUETOOTH);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToCheckZMBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE,STATE_CHECK_ZM_BLUETOOTH);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToConnectZMBluetooth(BluetoothDevice device) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE,STATE_CONNECT_ZM_AUDIO);
		intent.putExtra(CONTROL_DEVICE_NAME,device.getName());
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToConnectBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE, STATE_CONNECT_AUDIO);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToEnableBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE, STATE_ENABLE_BLUETOOTH);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToDisableBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE, STATE_DISABLE_BLUETOOTH);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToReConnectZMBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(CONTROL_STATE, STATE_RECONNECT_ZM_BLUETOOTH);
		SipUAApp.mContext.startActivity(intent);
	}

	public static Activity getInstance() {
		// TODO Auto-generated method stub
		return mContext;
	}
	@Override
	public void onDeviceConnectFailed(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onDeviceConnected(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(),getResources().getString(R.string.bl_hm_connected) + device.getDeviceName(), 0).show();

		finish();
	}
	@Override
	public void onDeviceDisconnected(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onDeviceFound(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onStateTurnningOn() {
		// TODO Auto-generated method stub

	}
	@Override
	public void onStateOn() {
		// TODO Auto-generated method stub
		if (mState == STATE_ENABLE_BLUETOOTH) {
			finish();
		}
//		if (mState == STATE_SELECT_HEADSET_BLUETOOTH) {
//			if (TalkBackNew.getInstance() != null) {
//				TalkBackNew.getInstance().startBluetoothAudioMode();
//			}
//		}
	}
	@Override
	public void onStateTurnningOff() {
		// TODO Auto-generated method stub

	}
	@Override
	public void onStateOff() {
		// TODO Auto-generated method stub

	}
	@Override
	public void onDeviceConnecting(BluetoothDevice device) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onDeviceConnected(BluetoothDevice device) {
		// TODO Auto-generated method stub
		// Tools.bringtoFront(SipUAApp.mContext);
		// finish();
		// ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
	}
	@Override
	public void onDeviceDisConnecting(BluetoothDevice device) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onDeviceDisConnected(BluetoothDevice device) {
		// TODO Auto-generated method stub
		finish();
	}
	@Override
	public void onDeviceConnectting(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onDeviceDisconnectting(BluetoothIBridgeDevice device) {
		// TODO Auto-generated method stub

	}

}
