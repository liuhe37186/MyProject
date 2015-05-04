package com.zed3.bluetooth;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.zed3.dialog.DialogUtil;
import com.zed3.dialog.DialogUtil.DialogCallBack;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

public class ZMBluetoothSelectActivity extends Activity {
	/** Called when the activity is first created. */

	private ListView mListView;
	private ArrayList<SiriListItem> list;
	private Button seachButton, serviceButton;
	ChatListAdapter mAdapter;
	static ZMBluetoothSelectActivity mContext;
	private static final int STATE_ENABLE_BLUETOOTH = 0;
	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

	@Override
	public void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.
		if (!mBtAdapter.isEnabled()) {
			 //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			//startActivityForResult(enableIntent, 3);

			DialogUtil.showSelectDialog(ZMBluetoothSelectActivity.this,
					getResources().getString(R.string.information),
					getResources().getString(R.string.bl_off_notify_message),
					getResources().getString(R.string.settings),
					new DialogCallBack() {

						@Override
						public void onPositiveButtonClick() {
							// TODO Auto-generated method stub
							//Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
							// startActivityForResult(enableIntent, 3);
							Intent enableIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
							startActivityForResult(enableIntent, 4);
							enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
							startActivityForResult(enableIntent, 3);

						}

						@Override
						public void onNegativeButtonClick() {
							// TODO Auto-generated method stub

						}
					});
		}
		dismissDialogs();
	}

	private void dismissDialogs() {
		// TODO Auto-generated method stub
		dismissDialog((AlertDialog) alertDialog);
	}

	private void dismissDialog(AlertDialog alertDialog) {
		// TODO Auto-generated method stub
		if (alertDialog != null && alertDialog.isShowing()) {
			alertDialog.dismiss();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices);
		mContext = this;
		init();

		// if (list.size()==1) {
		// }
		// handler.sendMessageDelayed(handler.obtainMessage(), /*1000*/500);

		list.clear();
		mAdapter.notifyDataSetChanged();

		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				String name = device.getName();
				if (checkIsZM(name)) {
					list.add(new SiriListItem(device.getName() + "("
							+ getStateStr(device) + ")" + "\n"
							+ device.getAddress(), true));
					// list.add(new SiriListItem(device.getName() + "\n" +
					// device.getAddress(), true));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
			}
		} else {
			list.add(new SiriListItem("No devices have been paired", true));
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(list.size() - 1);
		}
		/* 开始搜索 */
		mBtAdapter.startDiscovery();
		seachButton.setText(R.string.stop_search);
	}

	private void init() {
		list = new ArrayList<SiriListItem>();
		mAdapter = new ChatListAdapter(this, list);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setFastScrollEnabled(true);
		mListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, discoveryFilter);

		// Register for broadcasts when discovery has finished
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, foundFilter);

		// // Get a set of currently paired devices
		// Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		// // If there are paired devices, add each one to the ArrayAdapter
		// if (pairedDevices.size() > 0) {
		// for (BluetoothDevice device : pairedDevices) {
		// String name = device.getName();
		// if (checkIsZM(name)) {
		// list.add(new
		// SiriListItem(device.getName()+"("+getStateStr(device)+")" + "\n" +
		// device.getAddress(), true));
		// mAdapter.notifyDataSetChanged();
		// mListView.setSelection(list.size() - 1);
		// }
		// }
		// } else {
		// list.add(new SiriListItem("没有设备已经配对", true));
		// mAdapter.notifyDataSetChanged();
		// mListView.setSelection(list.size() - 1);
		// }

		seachButton = (Button) findViewById(R.id.start_seach);
		seachButton.setOnClickListener(seachButtonClickListener);

		serviceButton = (Button) findViewById(R.id.start_service);
		serviceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// Bluetooth.serviceOrCilent=ServerOrCilent.SERVICE;
				// Bluetooth.needRestartServerOrCilent = true;
				// Bluetooth.mTabHost.setCurrentTab(1);
				Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intent, STATE_ENABLE_BLUETOOTH);
				// intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				startActivityForResult(intent, 3);
			}
		});

	}

	private boolean checkIsZM(String name) {
		// TODO Auto-generated method stub
		if (name != null && name.startsWith("ZM") && name.length() == 8) {
			return true;
		}
		return false;
	}

	private String getStateStr(BluetoothDevice device) {
		// TODO Auto-generated method stub
		String stateStr = " unknow";
		switch (device.getBondState()) {
		case BluetoothDevice.BOND_NONE:

			// stateStr = " is not bonded";
			stateStr = SipUAApp.mContext.getResources().getString(
					R.string.bl_hm);
			break;
		case BluetoothDevice.BOND_BONDING:
			// stateStr = " is bonding";
			stateStr = SipUAApp.mContext.getResources().getString(
					R.string.bl_hm);
			break;
		case BluetoothDevice.BOND_BONDED:

			stateStr = SipUAApp.mContext.getResources().getString(
					R.string.bl_hm);
			// stateStr = " is bonded ";
			break;

		default:
			break;
		}
		return stateStr;
	}

	private String getConnectStateStr(BluetoothDevice device) {
		// TODO Auto-generated method stub
		String stateStr = " unknow";
		switch (device.getBondState()) {
		case BluetoothDevice.BOND_NONE:
			stateStr = " is not bonded";
			break;
		case BluetoothDevice.BOND_BONDING:
			stateStr = " is bonding";

			break;
		case BluetoothDevice.BOND_BONDED:

			stateStr = " is bonded ";
			break;

		default:
			break;
		}
		return stateStr;
	}

	private OnClickListener seachButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub

			if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
				Intent intent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				startActivityForResult(intent, 3);
				return;
			}
			if (mBtAdapter.isDiscovering()) {
				mBtAdapter.cancelDiscovery();
				seachButton.setText(R.string.re_search);
			} else {
				list.clear();
				mAdapter.notifyDataSetChanged();

				Set<BluetoothDevice> pairedDevices = mBtAdapter
						.getBondedDevices();
				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device : pairedDevices) {
						String name = device.getName();
						if (checkIsZM(name)) {
							list.add(new SiriListItem(device.getName() + "("
									+ getStateStr(device) + ")" + "\n"
									+ device.getAddress(), true));
							// list.add(new SiriListItem(device.getName() + "\n"
							// + device.getAddress(), true));
							mAdapter.notifyDataSetChanged();
							mListView.setSelection(list.size() - 1);
						}
					}
				} else {
					list.add(new SiriListItem("No devices have been paired",
							true));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
				/* 开始搜索 */
				mBtAdapter.startDiscovery();
				seachButton.setText(R.string.stop_search);
			}
		}
	};

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect

			SiriListItem item = list.get(arg2);
			String info = item.message;
			final String address = info.substring(info.length() - 17);
			// Bluetooth.BlueToothAddress = address;

			AlertDialog.Builder StopDialog = new AlertDialog.Builder(mContext);// 定义一个弹出框对象
			StopDialog.setTitle(R.string.connect);// 标题
			StopDialog.setMessage(item.message);
			StopDialog.setPositiveButton(
					getResources().getString(R.string.connect),
					new DialogInterface.OnClickListener() {
						private BluetoothDevice device;

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mBtAdapter.cancelDiscovery();
							seachButton.setText(R.string.re_search);

							device = BluetoothAdapter.getDefaultAdapter()
									.getRemoteDevice(address);
							if (device != null) {
								ZMBluetoothManager.getInstance().connectSPP(
										device);
							} else {
								Toast.makeText(getApplicationContext(),
										"device is null error", 0).show();
							}
							// Bluetooth.serviceOrCilent=ServerOrCilent.CILENT;
							// Bluetooth.needRestartServerOrCilent = true;
							// Bluetooth.mTabHost.setCurrentTab(1);
						}

					});
			StopDialog.setNegativeButton(
					getResources().getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Bluetooth.BlueToothAddress = null;
						}
					});
			StopDialog.show();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					String name = device.getName();
					if (checkIsZM(name)) {
						list.add(new SiriListItem(device.getName() + "("
								+ getStateStr(device) + ")" + "\n"
								+ device.getAddress(), false));
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(list.size() - 1);
					}
				}

				// device.ACTION_ACL_DISCONNECTED
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				if (mListView.getCount() == 0) {
					list.add(new SiriListItem(getResources().getString(
							R.string.no_bl_notify), false));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
				seachButton.setText(R.string.re_search);

				connectionCurrentDevice();
			}
		}
	};
	protected Dialog alertDialog;

	public class SiriListItem {
		String message;
		boolean isSiri;

		public SiriListItem(String msg, boolean siri) {
			message = msg;
			isSiri = siri;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}
		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (true) {
				return super.onKeyDown(keyCode, event);
			}
			if (alertDialog == null) {
				AlertDialog.Builder StopDialog = new AlertDialog.Builder(
						mContext);// 定义一个弹出框对象
				StopDialog.setTitle(R.string.information);// 标题
				StopDialog.setMessage(getResources().getString(
						R.string.exist_notify));
				StopDialog.setPositiveButton(
						getResources().getString(R.string.back_desk),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								alertDialog.dismiss();
								// Bluetooth.startHomeActivity(mContext);
							}
						});
				StopDialog.setNegativeButton(
						getResources().getString(R.string.exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								alertDialog.dismiss();
								finish();
							}
						});
				showDialog(StopDialog, false);
			} else {
				alertDialog.show();
			}
			return false;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showDialog(Builder stopDialog, boolean cancelable) {
		// TODO Auto-generated method stub
		alertDialog = stopDialog.show();
		alertDialog.setCancelable(cancelable);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		// If there are paired devices, add each one to the ArrayAdapter

		list.clear();
		mAdapter.notifyDataSetChanged();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				String name = device.getName();
				if (checkIsZM(name)) {
					list.add(new SiriListItem(device.getName() + "("
							+ getStateStr(device) + ")" + "\n"
							+ device.getAddress(), true));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
			}
		} else {
			list.add(new SiriListItem(getResources().getString(
					R.string.no_eq_notify), true));
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(list.size() - 1);
		}

		// chatActivity instance = chatActivity.getInstance();
		// if (instance != null) {
		// int state = mBtAdapter.getState();
		// switch (state) {
		// case BluetoothAdapter.STATE_CONNECTED:
		// instance.sendLinkDetectedMessage("STATE_CONNECTED", 0);
		// break;
		// case BluetoothAdapter.STATE_CONNECTING:
		// instance.sendLinkDetectedMessage("STATE_CONNECTING", 0);
		//
		// break;
		// case BluetoothAdapter.STATE_ON:
		// instance.sendLinkDetectedMessage("STATE_ON", 0);
		//
		// break;
		// case BluetoothAdapter.STATE_OFF:
		// instance.sendLinkDetectedMessage("STATE_OFF", 0);
		//
		// break;
		// case BluetoothAdapter.STATE_DISCONNECTED:
		// instance.sendLinkDetectedMessage("STATE_DISCONNECTED", 0);
		//
		// break;
		// case BluetoothAdapter.STATE_DISCONNECTING:
		// instance.sendLinkDetectedMessage("STATE_DISCONNECTING", 0);
		//
		// break;
		// case BluetoothAdapter.STATE_TURNING_ON:
		// instance.sendLinkDetectedMessage("STATE_TURNING_ON", 0);
		//
		// break;
		// case BluetoothAdapter.STATE_TURNING_OFF:
		// instance.sendLinkDetectedMessage("STATE_TURNING_OFF", 0);
		//
		// break;
		//
		// default:
		// break;
		// }
		//
		// BluetoothDevice device =
		// mBtAdapter.getRemoteDevice(Bluetooth.BlueToothAddress);
		// device.getBondState();
		// }

		super.onResume();
	}

	// Handler handler = new Handler(){
	// public void handleMessage(android.os.Message msg) {
	// connectionCurrentDevice();
	// };
	// };

	protected void connectionCurrentDevice() {
		// TODO Auto-generated method stub
		if (list.size() > 1) {
			DialogUtil.showCheckDialog(
					ZMBluetoothSelectActivity.this,
					getResources().getString(R.string.information),
					getResources().getString(R.string.information_1)
							+ list.size()
							+ getResources().getString(R.string.information_2),
					getResources().getString(R.string.ok_know));
			return;
		} else if (list.size() == 0) {
			return;
		}
		SiriListItem item = list.get(0);
		String info = item.message;
		if (!checkIsZM(info.substring(0, 8))) {
			DialogUtil.showCheckDialog(ZMBluetoothSelectActivity.this,
					getResources().getString(R.string.information),
					getResources().getString(R.string.no_paried),
					getResources().getString(R.string.ok_know));
			return;
		}
		String address = info.substring(info.length() - 17);
		// Bluetooth.BlueToothAddress = address;

		// AlertDialog.Builder StopDialog =new
		// AlertDialog.Builder(mContext);//定义一个弹出框对象
		// StopDialog.setTitle("连接");//标题
		// StopDialog.setMessage(item.message);
		// StopDialog.setPositiveButton("连接", new
		// DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// // TODO Auto-generated method stub
		// mBtAdapter.cancelDiscovery();
		// seachButton.setText("重新搜索");
		//
		// Bluetooth.serviceOrCilent=ServerOrCilent.CILENT;
		// Bluetooth.needRestartServerOrCilent = true;
		// Bluetooth.mTabHost.setCurrentTab(1);
		// }
		// });
		// StopDialog.setNegativeButton("取消",new
		// DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// Bluetooth.BlueToothAddress = null;
		// }
		// });
		// StopDialog.show();
		// Bluetooth.serviceOrCilent=ServerOrCilent.CILENT;
		// Bluetooth.needRestartServerOrCilent = true;
		// Bluetooth.mTabHost.setCurrentTab(1);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// switch (requestCode) {
		// case 4:
		// if (!mBtAdapter.isEnabled()) {
		// // Intent enableIntent = new
		// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		// // startActivityForResult(enableIntent, 3);
		//
		// DialogUtil.showSelectDialog(ZMBluetoothSelectActivity.this, "提示",
		// "蓝牙未打开，是否打开并设置连接蓝牙？", "设置", new DialogCallBack() {
		//
		// @Override
		// public void onPositiveButtonClick() {
		// // TODO Auto-generated method stub
		// Intent enableIntent = new
		// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		// startActivityForResult(enableIntent, 3);
		// enableIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
		// startActivityForResult(enableIntent, 4);
		// }
		//
		// @Override
		// public void onNegativeButtonClick() {
		// // TODO Auto-generated method stub
		// }
		// });
		// }else {
		// Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		// list.clear();
		// for (BluetoothDevice device : pairedDevices) {
		// String name = device.getName();
		// if (checkIsZM(name)) {
		// list.add(new
		// SiriListItem(device.getName()+"("+getStateStr(device)+")" + "\n" +
		// device.getAddress(), true));
		// }
		// }
		// if (list.size() == 0) {
		// DialogUtil.showSelectDialog(ZMBluetoothSelectActivity.this, "提示",
		// "蓝牙已打开，未绑定蓝牙手咪设备，是否重新设置？", "设置", new DialogCallBack() {
		//
		// @Override
		// public void onPositiveButtonClick() {
		// // TODO Auto-generated method stub
		// // Intent enableIntent = new
		// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		// // startActivityForResult(enableIntent, 3);
		// Intent enableIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
		// startActivityForResult(enableIntent, 4);
		// }
		// @Override
		// public void onNegativeButtonClick() {
		// // TODO Auto-generated method stub
		// DialogUtil.showCheckDialog(ZMBluetoothSelectActivity.this, "警告",
		// "蓝牙手咪未连接手机，无法使用蓝牙手咪控制手机。", "知道了");
		// }
		// });
		// }else if(list.size() > 1) {
		// DialogUtil.showSelectDialog(ZMBluetoothSelectActivity.this, "提示",
		// "已配对"+list.size()+"部蓝牙手咪，请保留一部蓝牙手咪，并取消其他蓝牙手咪的配对。", "去设置", new
		// DialogCallBack() {
		//
		// @Override
		// public void onPositiveButtonClick() {
		// // TODO Auto-generated method stub
		// // Intent enableIntent = new
		// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		// // startActivityForResult(enableIntent, 3);
		// Intent enableIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
		// startActivityForResult(enableIntent, 4);
		// }
		// @Override
		// public void onNegativeButtonClick() {
		// // TODO Auto-generated method stub
		// DialogUtil.showCheckDialog(ZMBluetoothSelectActivity.this, "警告",
		// "蓝牙手咪未连接手机，无法使用蓝牙手咪控制手机。", "知道了");
		// }
		// });
		// }else {
		// connectionCurrentDevice();
		// }
		// }
		//
		//
		//
		// break;
		//
		// default:
		// break;
		// }

		super.onActivityResult(requestCode, resultCode, data);
	}

	public static void askUserToSelectBluetooth() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(SipUAApp.mContext,ZMBluetoothSelectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.putExtra(CONTROL_STATE,STATE_RECONNECT_ZM_BLUETOOTH );
		SipUAApp.mContext.startActivity(intent);
	}

	public static Activity getInstance() {
		// TODO Auto-generated method stub
		return mContext;
	}
}