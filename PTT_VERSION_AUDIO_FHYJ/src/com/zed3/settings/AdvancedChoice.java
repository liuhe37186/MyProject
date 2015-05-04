package com.zed3.settings;

import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.UdpTransport;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.dialog.DialogUtil;
import com.zed3.dialog.DialogUtil.DialogCallBack;
import com.zed3.location.MemoryMg;
import com.zed3.log.CrashHandler;
import com.zed3.log.MyLog;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.SettingVideoSize;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.SwitchButton;

public class AdvancedChoice extends BaseActivity implements OnClickListener {
	LinearLayout audio_set, video_set, groupcall_set, position_set,
			encrypt_set, flow_set, log_set, bgdate_show, gpsOnOff,
			registertime_btn, bluetooth_set, wakeup_onoff,language,screenWakeupPeriod;
	SwitchButton log_ctrl, flow_ctrl, gps_ctrl, encrypt_onoff, bluetooth_onoff,
			wakeup_swt;

	TextView logsummary, flow_summary, gpssummary, locateModetxt,
			msgencry_summary, registertime_summary, bluetooth_summary,
			wakeupsummary,currentLanguage,currentScreenWakeupPeriodInfo;
	private SharedPreferences mSharedPreferences;
	int languageId = 0;//GQT英文版 选择语言的类型
	int flag = 0;
	int screenWakeupPeriodIndex = 0;
	int count = 0;
	// long flowtime=0;
	int regTime;
	LinearLayout btn_left;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_adchoice);
		count = 0;
		// mSharedPreferences add by oumogang 2013-07-11
		// this activity need one SharedPreferences for itself;
		mSharedPreferences = getSharedPreferences(Settings.sharedPrefsFile,
				MODE_PRIVATE);
		languageId = mSharedPreferences.getInt("languageId", 0);
		final String[] llist = getResources().getStringArray(
				R.array.screen_wakeup_period_List);
		screenWakeupPeriodIndex = mSharedPreferences.getInt(MyPowerManager.KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX, MyPowerManager.SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX);
		currentScreenWakeupPeriodInfo = (TextView) findViewById(R.id.current_screen_wakeup_period_info);
		currentScreenWakeupPeriodInfo.setText(llist[screenWakeupPeriodIndex]);
		flag = languageId;
		currentLanguage = (TextView) findViewById(R.id.currentLanguage);
		switch (languageId) {
		case 0:
			currentLanguage.setText(R.string.language_d);
			break;
		case 1:
			currentLanguage.setText(R.string.language_c);
			break;
		case 2:
			currentLanguage.setText(R.string.language_e);
			break;
		default:
			break;
		}
		//GQT英文版 实现应用内部切换语言
		language = (LinearLayout) findViewById(R.id.language);
		language.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String[] llist = getResources().getStringArray(R.array.languageList);				
				AlertDialog.Builder builder = new AlertDialog.Builder(AdvancedChoice.this);
				builder.setSingleChoiceItems(llist, mSharedPreferences.getInt("languageId", 0), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						languageId = which;	
					}
				}).setTitle(R.string.select_language)
				.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(languageId == flag){
							return ;
						} 
						MainActivity.getInstance().finish();//将主界面结束，方便重新调用主界面的onCreate（）
						mSharedPreferences.edit().putInt("languageId", languageId).commit();
//						//更新配置文件，更改应用的语言
						Resources resources = getResources();//获取资源对象
						Configuration config = resources.getConfiguration();//获取配置对象
						DisplayMetrics dm = resources.getDisplayMetrics();//获取屏幕分辨率
						switch (languageId ) {
						case 0:
							config.locale = config.locale.getDefault();//跟随系统
							currentLanguage.setText(R.string.language_d);
							break;
						case 1:
							config.locale = config.locale.SIMPLIFIED_CHINESE;//简体中文
							currentLanguage.setText(R.string.language_c);
							break;
						case 2:
							config.locale = config.locale.ENGLISH;//英文
							currentLanguage.setText(R.string.language_e);
							break;
						default:
							break;
						}
						resources.updateConfiguration(config, dm);//更新资源文件的数据，更新应用的语言,用于改变通知栏的信息
						dialog.dismiss();
						//重新创建主界面
						Intent i = new Intent(AdvancedChoice.this,MainActivity.class);
						AdvancedChoice.this.startActivity(i);
						//发送广播，改变通知栏中的状态
						AdvancedChoice.this.sendBroadcast(new Intent("SettingLanguage"));
						AdvancedChoice.this.finish();
					}
				}).create().show();
			}
		});
		// 定时亮屏设置
		screenWakeupPeriod = (LinearLayout) findViewById(R.id.screen_wakeup_set);
		screenWakeupPeriod.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final String[] llist = getResources().getStringArray(
						R.array.screen_wakeup_period_List);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						AdvancedChoice.this);
				AlertDialog dialog = builder.setSingleChoiceItems(llist,
						mSharedPreferences.getInt(MyPowerManager.KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX, MyPowerManager.SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								if (which == screenWakeupPeriodIndex) {
									return;
								}
								screenWakeupPeriodIndex = which;
								mSharedPreferences
								.edit()
								.putInt(MyPowerManager.KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX,
										screenWakeupPeriodIndex).commit();
								MyPowerManager powerManager = MyPowerManager.getInstance();
								powerManager.setScreenWakeupPeriod(powerManager.getScreenWakeupPeriodFromArray(screenWakeupPeriodIndex));
								currentScreenWakeupPeriodInfo.setText(llist[screenWakeupPeriodIndex]);
								dialog.dismiss();
							}
						})
						.setTitle(R.string.select_screen_wakeup_period)
						.create();
				dialog.show();
			}
		});
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setText(R.string.advanced_option);
		ImageButton back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// init linearlayout
		audio_set = (LinearLayout) findViewById(R.id.audio_set);
		if (DeviceInfo.CONFIG_SUPPORT_AUDIO)
			audio_set.setVisibility(View.VISIBLE);
		else
			audio_set.setVisibility(View.GONE);

		audio_set.setOnClickListener(this);
		video_set = (LinearLayout) findViewById(R.id.video_set);
		if (DeviceInfo.CONFIG_SUPPORT_VIDEO) {
			video_set.setVisibility(View.VISIBLE);
		} else
			video_set.setVisibility(View.GONE);

		video_set.setOnClickListener(this);
		groupcall_set = (LinearLayout) findViewById(R.id.groupcallcoming_set);
		groupcall_set.setOnClickListener(this);
		position_set = (LinearLayout) findViewById(R.id.postion_set);
		// modify by liangzhang 2014-09-09 与开关设计文档不符，已修改为文档设计模式
		if (DeviceInfo.CONFIG_GPS == 0 || DeviceInfo.GPS_REMOTE == 0) {// 0:没有上报功能
			position_set.setVisibility(View.GONE);
			findViewById(R.id.postion_set_line).setVisibility(View.GONE);
		} else {
			position_set.setOnClickListener(this);
		}
		locateModetxt = (TextView) findViewById(R.id.locatemodetxt);

		encrypt_set = (LinearLayout) findViewById(R.id.msgencry_set);
		//modify by liangzhang 2014-09-01 修改修改config.ini文件中encrypt的值不隐藏信令加密选项的问题
		if (!DeviceInfo.ENCRYPT_REMOTE) {
			if (!DeviceInfo.CONFIG_SUPPORT_ENCRYPT) {
				encrypt_set.setVisibility(View.GONE);
				findViewById(R.id.msgencry_set_line).setVisibility(View.GONE);
			} else {
				encrypt_set.setOnClickListener(this);
			}
			UdpTransport.needEncrypt = mSharedPreferences.getBoolean(Settings.PREF_MSG_ENCRYPT, false);
		} else {
			encrypt_set.setVisibility(View.GONE);
			findViewById(R.id.msgencry_set_line).setVisibility(View.GONE);
			UdpTransport.needEncrypt = true;
		}
		bgdate_show = (LinearLayout) findViewById(R.id.bgdate_show);
		bgdate_show.setOnClickListener(this);
		flow_set = (LinearLayout) findViewById(R.id.flowOnOff);
		if (!DeviceInfo.CONFIG_SUPPORT_RATE_MONITOR) {
			flow_set.setVisibility(View.GONE);
			findViewById(R.id.flowOnOff_line).setVisibility(View.GONE);
		} else {
			flow_set.setOnClickListener(this);
		}
		log_set = (LinearLayout) findViewById(R.id.logOnOff);
		if (!DeviceInfo.CONFIG_SUPPORT_LOG) {
			log_set.setVisibility(View.GONE);
			findViewById(R.id.log_onoff_line).setVisibility(View.GONE);
		} else {
			log_set.setOnClickListener(this);
		}

		gpsOnOff = (LinearLayout) findViewById(R.id.gpsOnOff);
		gpsOnOff.setOnClickListener(this);

		log_ctrl = (SwitchButton) findViewById(R.id.log_ctrl);

		// log_ctrl.SetOnChangedListener(new OnSlipChangedListener() {
		//
		// @Override
		// public void OnChanged(boolean CheckState) {
		// // TODO Auto-generated method stub
		// boolean flag = mSharedPreferences.getBoolean("logOnOffKey", false);
		// commit("logOnOffKey",!flag);
		// if(!flag){
		// CrashHandler csh = CrashHandler.getInstance();
		// csh.init(AdvancedChoice.this, true);
		// }else{
		// CrashHandler.EndLog();
		// }
		// updateSummary();
		// }
		// });
		TextView tv_show = (TextView) findViewById(R.id.t_leftbtn);
		tv_show.setText(R.string.settings);
		btn_left = (LinearLayout) findViewById(R.id.btn_leftbtn);
		btn_left.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				// MainActivity.getInstance().startIntent(SettingNew.class);
			}
		});
		btn_left.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView tv = (TextView) findViewById(R.id.t_leftbtn);
				TextView tv_left = (TextView) findViewById(R.id.left_icon);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// btn_home.setBackgroundResource(R.color.red);
					tv.setTextColor(Color.WHITE);
					btn_left.setBackgroundResource(R.color.btn_click_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_press);
					break;
				case MotionEvent.ACTION_UP:
					// btn_home.setBackgroundResource(R.color.font_color3);
					tv.setTextColor(getResources()
							.getColor(R.color.font_color3));
					btn_left.setBackgroundResource(R.color.whole_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_release);
					break;
				}
				return false;
			}
		});

		gps_ctrl = (SwitchButton) findViewById(R.id.gps_ctrl);

		// gps_ctrl.SetOnChangedListener(new OnSlipChangedListener() {
		//
		// @Override
		// public void OnChanged(boolean CheckState) {
		// // TODO Auto-generated method stub
		//
		// boolean flag = mSharedPreferences.getBoolean("gpsOnOffKey", true);
		// commit("gpsOnOffKey",!flag);
		// if(!flag){
		// //开
		// Receiver.GetCurUA().GPSOpenLock();
		// }else{
		// //关
		// Receiver.GetCurUA().GPSCloseLock();
		// }
		// updateSummary();
		//
		// }
		// });
		logsummary = (TextView) findViewById(R.id.logsummary);
		gpssummary = (TextView) findViewById(R.id.gpssummary);

		flow_ctrl = (SwitchButton) findViewById(R.id.flow_ctrl);

		// flow_ctrl.SetOnChangedListener(new OnSlipChangedListener() {
		//
		// @Override
		// public void OnChanged(boolean CheckState) {
		// // TODO Auto-generated method stub
		// MyLog.e("advancedchoice", System.currentTimeMillis()+"");
		// // if(flowtime==0)
		// // {
		// // flowtime=System.currentTimeMillis();
		// // }
		// // else
		// // {
		// // if(System.currentTimeMillis()-flowtime<5000)
		// // {
		// // Toast.makeText(AdvancedChoice.this, "轻轻点",
		// Toast.LENGTH_SHORT).show();
		// // return;
		// // }
		// // flowtime=0;
		// // }
		// boolean flag =mSharedPreferences.getBoolean("flowOnOffKey", false);
		// commit("flowOnOffKey",!flag);
		// Intent intent = new Intent();
		// intent.setFlags(Service.START_NOT_STICKY);
		// intent = new Intent();
		// intent.setAction("com.zed3.flow.FlowRefreshService");
		//
		// if(!flag){
		// startService(intent);
		// }else{
		// stopService(intent);
		// }
		// updateSummary();
		// }
		// });
		encrypt_onoff = (SwitchButton) findViewById(R.id.encrypt_onoff);

		// encrypt_onoff.SetOnChangedListener(new OnSlipChangedListener() {
		//
		// @Override
		// public void OnChanged(boolean CheckState) {
		// // TODO Auto-generated method stub
		// boolean flag
		// =mSharedPreferences.getBoolean(Settings.PREF_MSG_ENCRYPT, false);
		// commit(Settings.PREF_MSG_ENCRYPT,!flag);
		// updateSummary();
		//
		// }
		// });
		msgencry_summary = (TextView) findViewById(R.id.msgencry_summary);
		flow_summary = (TextView) findViewById(R.id.flowonoff);

		registertime_summary = (TextView) findViewById(R.id.registertime_summary);
		// 注册间隔
		registertime_btn = (LinearLayout) findViewById(R.id.registertime_btn);
		if (!DeviceInfo.CONFIG_SUPPORT_REGISTER_INTERNAL) {
			registertime_btn.setVisibility(View.GONE);
			findViewById(R.id.register_internal).setVisibility(View.GONE);
		} else {
			registertime_btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Dialog
					regTime = mSharedPreferences.getInt(
							Settings.PREF_REGTIME_EXPIRES, 1800);
					showDialog_Layout();
				}
			});
		}
		// 蓝牙开关
		bluetooth_summary = (TextView) findViewById(R.id.bluetooth_summary);
		bluetooth_onoff = (SwitchButton) findViewById(R.id.bluetooth_onoff);
		// 唤醒休眠
		wakeupsummary = (TextView) findViewById(R.id.wakeupsummary);
		wakeup_swt = (SwitchButton) findViewById(R.id.wakeup_swt);

		updateSummary();
		setOnCheckedChangeListener();
	}

	/**
	 * set OnCheckedChangeListener on activity create,not onResume. add by
	 * oumogang 2014-03-12
	 */
	private void setOnCheckedChangeListener() {
		// TODO Auto-generated method stub
		flow_ctrl.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				MyLog.e("advancedchoice", System.currentTimeMillis() + "");
				boolean flag = mSharedPreferences.getBoolean("flowOnOffKey",
						false);
				commit("flowOnOffKey", !flag);
				Intent intent = new Intent();
				intent.setFlags(Service.START_NOT_STICKY);
				intent = new Intent();
				intent.setAction("com.zed3.flow.FlowRefreshService");

				if (!flag) {
					startService(intent);
				} else {
					stopService(intent);
				}
				updateSummary();
			}
		});
		encrypt_onoff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				boolean flag = mSharedPreferences.getBoolean(
						Settings.PREF_MSG_ENCRYPT, false);
				commit(Settings.PREF_MSG_ENCRYPT, !flag);
				updateSummary();
				UdpTransport.needEncrypt = mSharedPreferences.getBoolean(Settings.PREF_MSG_ENCRYPT,false);
				if (Receiver.mSipdroidEngine == null) {// modify by hu 2014/3/18
					Receiver.engine(AdvancedChoice.this);
				} else {
					//regtier instead of startEngine . modify by mou 2014-11-03
//					Receiver.mSipdroidEngine.StartEngine();
					Receiver.mSipdroidEngine.register(true);
				}
			}
		});
		gps_ctrl.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				boolean flag = mSharedPreferences.getBoolean("gpsOnOffKey",
						true);
				commit("gpsOnOffKey", !flag);
				if (!flag) {
					// 开
					Receiver.GetCurUA().GPSOpenLock();
				} else {
					// 关
					Receiver.GetCurUA().GPSCloseLock();
				}
				updateSummary();
			}
		});
		log_ctrl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				boolean flag = mSharedPreferences.getBoolean("logOnOffKey",
						false);
				commit("logOnOffKey", !flag);
				if (!flag) {
					CrashHandler csh = CrashHandler.getInstance();
					csh.init(AdvancedChoice.this, true);
				} else {
					CrashHandler.EndLog();
				}
				updateSummary();
			}
		});

		// 蓝牙开关
		bluetooth_onoff
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					boolean mIsCloseByUser = true;
					boolean mIsOpenByUser = true;

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						commit(Settings.PREF_BLUETOOTH_ONOFF, isChecked);
						Settings.mNeedBlueTooth = isChecked;

						if (isChecked) {
							if (mIsOpenByUser) {
								// ZMBluetoothManager.getInstance().init(SipUAApp.mContext);
								ZMBluetoothManager.getInstance()
										.connectZMBluetooth(SipUAApp.mContext);
							}
							updateSummary();

						} else {
							if (ZMBluetoothManager.getInstance()
									.getSPPConnectedDevices().size() > 0) {

								DialogUtil.showSelectDialog(
										AdvancedChoice.this,
										getResources().getString(
												R.string.close_hm),
										getResources().getString(
												R.string.close_hm_notify),
										getResources().getString(
												R.string.disconnect),
										new DialogCallBack() {

											@Override
											public void onPositiveButtonClick() {
												// TODO Auto-generated method
												// stub
												ZMBluetoothManager
														.getInstance().mNeedAskUserToReconnectSpp = false;
												ZMBluetoothManager
														.getInstance()
														.disConnectZMBluetooth(
																SipUAApp.mContext);
												ZMBluetoothManager
														.getInstance()
														.exit(SipUAApp.mContext);
												if (ZMBluetoothManager
														.getInstance()
														.isBluetoothAdapterEnabled()) {
													ZMBluetoothManager
															.getInstance()
															.askUserToDisableBluetooth();
												}
												mIsOpenByUser = true;
												updateSummary();
											}

											@Override
											public void onNegativeButtonClick() {
												// TODO Auto-generated method
												// stub
												commit(Settings.PREF_BLUETOOTH_ONOFF,
														true);
												Settings.mNeedBlueTooth = true;
												mIsOpenByUser = false;
												bluetooth_onoff
														.setChecked(true);
												updateSummary();
											}
										});
							} else {
								updateSummary();
							}

						}

					}
				});
		// 唤醒
		wakeup_swt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				boolean flag = mSharedPreferences.getBoolean(
						Settings.PREF_MICWAKEUP_ONOFF, true);
				commit(Settings.PREF_MICWAKEUP_ONOFF, !flag);
				if (!flag) {

					MemoryMg.getInstance().isMicWakeUp = true;
				} else {

					MemoryMg.getInstance().isMicWakeUp = false;
				}
				updateSummary();
			}
		});

	}

	// 显示基于Layout的AlertDialog
	private void showDialog_Layout() {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View textEntryView = inflater
				.inflate(R.layout.dialoglayout, null);
		final EditText edtInput = (EditText) textEntryView
				.findViewById(R.id.edtInput);
		edtInput.setInputType(InputType.TYPE_CLASS_NUMBER);

		edtInput.setText(regTime + "");
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon22);
		builder.setTitle(R.string.setting_register_dialog_title);
		builder.setView(textEntryView);
		builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String str = edtInput.getText().toString();
				int val = 0;
				if (str.length() > 0) {
					try {
						val = Integer.parseInt(str);
					    if (val < 60) {
							Toast.makeText(AdvancedChoice.this, getResources().getString(R.string.setting_register_notify),
									Toast.LENGTH_SHORT).show();
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}

					Editor edit = mSharedPreferences.edit();
					edit.putInt(Settings.PREF_REGTIME_EXPIRES, val);
					edit.commit();
					registertime_summary.setText(val + AdvancedChoice.this.getResources().getString(R.string.second));

					SipStack.default_expires = val;
					// 重新注册
					if (Receiver.mSipdroidEngine == null) {// modify by hu
															// 2014/1/22
						Receiver.engine(AdvancedChoice.this);
					} else {
						//regtier instead of startEngine . modify by mou 2014-11-03
//						Receiver.mSipdroidEngine.StartEngine();
						Receiver.mSipdroidEngine.register(true);
					}

				}
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// setTitle("");

			}
		});
		builder.show();
	}

	@Override
	protected void onResume() {
		updateSummary();
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.audio_set: {
			Intent intent = new Intent();
			intent.setClass(AdvancedChoice.this, AudioSetActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		}
		case R.id.video_set: {
			Intent intent = new Intent();
			intent.setClass(AdvancedChoice.this, SettingVideoSize.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		}
		case R.id.groupcallcoming_set: {
			Intent intent = new Intent();
			intent.setClass(AdvancedChoice.this,
					GroupCallComingSetActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		}
		case R.id.postion_set: {
			Intent intent = new Intent();
			intent.setClass(AdvancedChoice.this, GpsSetActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			// // 默认从不定位
			// // 弹出一个Dialog提示框，供选择定位模式
			// int pos = findWhich(Settings.PREF_LOCATEMODE,
			// Settings.DEFAULT_PREF_LOCATEMODE);
			// createDialog(R.string.locateModle_dialogTitle, pos).show();

			break;
		}
		// case R.id.msgencry_set: {
		// boolean flag
		// =mSharedPreferences.getBoolean(Settings.PREF_MSG_ENCRYPT, false);
		// commit(Settings.PREF_MSG_ENCRYPT,!flag);
		// updateSummary();
		// break;
		// }
		// case R.id.flowOnOff: {
		// boolean flag =mSharedPreferences.getBoolean("flowOnOffKey", false);
		// commit("flowOnOffKey",!flag);
		// Intent intent = new Intent();
		// intent.setFlags(Service.START_NOT_STICKY);
		// intent = new Intent();
		// intent.setAction("com.zed3.flow.FlowRefreshService");
		// if(!flag){
		// startService(intent);
		// }else{
		// stopService(intent);
		// }
		// updateSummary();
		// break;
		// }
		// case R.id.logOnOff: {
		// boolean flag = mSharedPreferences.getBoolean("logOnOffKey", false);
		// commit("logOnOffKey",!flag);
		// if(!flag){
		// CrashHandler csh = CrashHandler.getInstance();
		// csh.init(this, true);
		// }else{
		// CrashHandler.EndLog();
		// }
		// updateSummary();
		// break;
		// }
		// case R.id.gpsOnOff: {
		// boolean flag = mSharedPreferences.getBoolean("gpsOnOffKey", true);
		// commit("gpsOnOffKey",!flag);
		//
		// if(!flag){
		// //开
		// Receiver.GetCurUA().GPSOpenLock();
		// }else{
		// //关
		// Receiver.GetCurUA().GPSCloseLock();
		// }
		// updateSummary();
		// // 计时器4秒
		// gpsOnOff.setEnabled(false);
		//
		// if(handler.hasMessages(1))
		// handler.removeMessages(1);
		// handler.sendEmptyMessage(1);
		//
		// break;
		// }
		case R.id.bgdate_show: {
			// 显示后台数据
			// startActivity(new Intent(this,DataViewsActivity.class));
			// Intent intent = new Intent();
			// intent.setClass(AdvancedChoice.this, DataViewsActivity.class);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// startActivity(intent);
			break;
		}
		}
	}

	private void updateSummary() {
		boolean flag = mSharedPreferences.getBoolean("logOnOffKey", false);
		if (flag) {
			// log_ctrl.setImageResource(R.drawable.on);
			log_ctrl.setChecked(true);
			logsummary.setText(R.string.rate_suspension_2);
		} else {
			log_ctrl.setChecked(false);
			// log_ctrl.setImageResource(R.drawable.off);
			logsummary.setText(R.string.rate_suspension_1);
		}
		// gps开关 hdf
		boolean flag2 = mSharedPreferences.getBoolean("gpsOnOffKey", true);
		if (flag2) {
			// gps_ctrl.setImageResource(R.drawable.on);
			gps_ctrl.setChecked(true);
			gpssummary.setText(R.string.rate_suspension_2);
		} else {
			gps_ctrl.setChecked(false);
			// gps_ctrl.setImageResource(R.drawable.off);
			gpssummary.setText(R.string.rate_suspension_1);
		}

		boolean flag3 = mSharedPreferences.getBoolean("flowOnOffKey", false);
		if (flag3) {
			// flow_ctrl.setImageResource(R.drawable.on);
			flow_ctrl.setChecked(true);
			flow_summary.setText(R.string.rate_suspension_2);
		} else {
			flow_ctrl.setChecked(false);
			// flow_ctrl.setImageResource(R.drawable.off);
			flow_summary.setText(R.string.rate_suspension_1);
		}
		// 信令加密
		boolean flag4 = mSharedPreferences.getBoolean(
				Settings.PREF_MSG_ENCRYPT, false);
		if (flag4) {
			MyLog.e("AdvancedChoice", "PREF_MSG_ENCRYPT true");
			encrypt_onoff.setChecked(true);
			msgencry_summary.setText(R.string.encryption_2);
		} else {
			MyLog.e("AdvancedChoice", "PREF_MSG_ENCRYPT false");
			encrypt_onoff.setChecked(false);
			msgencry_summary.setText(R.string.encryption_1);
		}
		// 蓝牙
		boolean flag5 = mSharedPreferences.getBoolean(
				Settings.PREF_BLUETOOTH_ONOFF, false);
		if (flag5) {
			bluetooth_onoff.setChecked(true);
			bluetooth_summary.setText(R.string.rate_suspension_2);
		} else {
			bluetooth_onoff.setChecked(false);
			bluetooth_summary.setText(R.string.rate_suspension_1);
		}
		// 唤醒
		boolean flag6 = mSharedPreferences.getBoolean(
				Settings.PREF_MICWAKEUP_ONOFF, true);
		if (flag6) {
			wakeup_swt.setChecked(true);
			wakeupsummary.setText(R.string.rate_suspension_2);
		} else {
			wakeup_swt.setChecked(false);
			wakeupsummary.setText(R.string.rate_suspension_1);
		}
		// 注册间隔
		regTime = mSharedPreferences
				.getInt(Settings.PREF_REGTIME_EXPIRES, 1800);
		registertime_summary.setText(regTime +getResources().getString(R.string.second));
	}

	private void commit(String key, boolean value) {
		Editor edit = mSharedPreferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// if(handler.hasMessages(1))
		// handler.removeMessages(1);
	}
}
