package com.zed3.settings;

import org.zoolu.tools.MyLog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.location.MemoryMg;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;

public class GpsSetActivity extends BaseActivity implements OnClickListener {
	LinearLayout postion_set, postion_settime, postion_uploadtime, btn_left;
	TextView locatemodetxt, settimetxt, uploadtimetxt;
	private static SharedPreferences mSharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_gpsset);

		mSharedPreferences = getSharedPreferences(Settings.sharedPrefsFile,
				MODE_PRIVATE);
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setText(R.string.setting_position);

		postion_set = (LinearLayout) findViewById(R.id.postion_set);
		postion_set.setOnClickListener(this);

		postion_settime = (LinearLayout) findViewById(R.id.postion_settime);
		postion_settime.setOnClickListener(this);

		postion_uploadtime = (LinearLayout) findViewById(R.id.postion_uploadtime);
		postion_uploadtime.setOnClickListener(this);

		locatemodetxt = (TextView) findViewById(R.id.locatemodetxt);
		settimetxt = (TextView) findViewById(R.id.settimetxt);
		uploadtimetxt = (TextView) findViewById(R.id.uploadtimetxt);

		TextView tv_show = (TextView) findViewById(R.id.t_leftbtn);
		tv_show.setText(R.string.advanced);
		btn_left = (LinearLayout) findViewById(R.id.btn_leftbtn);
		btn_left.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
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
		
		if(DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN){
			if (DeviceInfo.GPS_REMOTE == 0) {
				postion_set.setVisibility(View.GONE);
				findViewById(R.id.position_set_line).setVisibility(View.GONE);
			}else if(DeviceInfo.GPS_REMOTE == 1){
				postion_set.setClickable(false);
				postion_set.setBackgroundColor(Color.GRAY);
				postion_set.setVisibility(View.VISIBLE);
				locatemodetxt.setText(R.string.setting_position_4);
			}else if(DeviceInfo.GPS_REMOTE == 2){
				//DPMP������Ϊ�������ϱ�ʱ��ȡ����config.ini����
				getLocalConfigofGps();
			}else if(DeviceInfo.GPS_REMOTE == 3){
				postion_set.setClickable(false);
				postion_set.setBackgroundColor(Color.GRAY);
				postion_set.setVisibility(View.VISIBLE);
				locatemodetxt.setText(R.string.setting_position_5);
			}else if(DeviceInfo.GPS_REMOTE == 4){
				postion_set.setClickable(false);
				postion_set.setBackgroundColor(Color.GRAY);
				postion_set.setVisibility(View.VISIBLE);
				locatemodetxt.setText(R.string.setting_position_3);
			}
		}else{
			if (DeviceInfo.CONFIG_GPS == 0) {
				postion_set.setVisibility(View.GONE);
				findViewById(R.id.position_set_line).setVisibility(View.GONE);
			}else{
				postion_set.setVisibility(View.VISIBLE);
				findViewById(R.id.position_set_line).setVisibility(View.VISIBLE);
				//�ֶ��汾��ȡ����config.ini����
				getLocalConfigofGps();
			}
		}
		updateSunmary();
		// ����Gps���� delete by liangzhang 2014-09-09
		// if (DeviceInfo.GPS_REMOTE == 1) {
		// postion_set.setVisibility(View.GONE);
		// }
	}
	
	/**
	 * ��ȡ��������config.ini��gps��ֵ����Ӧ���浽���������е�ѡ�� 
	 * ѡ���������£� 0��GPS��λ 1���ٶ����ܶ�λ 2���ٶ�GPS��λ 3���Ӳ���λ 
	 */
	private void getLocalConfigofGps() {
		int config_mode = 3;// ��¼config.ini�����õ�gpsģʽ
		if (DeviceInfo.CONFIG_GPS == 1) {
			config_mode = 0;
		} else if (DeviceInfo.CONFIG_GPS == 2) {
			config_mode = 1;
		} else if (DeviceInfo.CONFIG_GPS == 3) {
			config_mode = 2;
		} else if (DeviceInfo.CONFIG_GPS == 4) {
			config_mode = 3;
		}
		Editor editor = mSharedPreferences.edit();
		int current_mode = mSharedPreferences.getInt(Settings.PREF_LOCATEMODE,
				Settings.DEFAULT_PREF_LOCATEMODE);
		if (current_mode == config_mode) {
			editor.putInt(Settings.PREF_LOCATEMODE, config_mode);
		} else {
			editor.putInt(Settings.PREF_LOCATEMODE, current_mode);
		}
		editor.commit();
	}

	private void updateSunmary() {
		// TODO Auto-generated method stub
		// ��λģʽ
		int pos = findWhich(Settings.PREF_LOCATEMODE,
				Settings.DEFAULT_PREF_LOCATEMODE);
		// if (pos == 0)
		// locatemodetxt.setText("���ܶ�λ");
		// else
		if (pos == 0)
			locatemodetxt.setText(R.string.setting_position_3);
		else if (pos == 1)
			locatemodetxt.setText(R.string.setting_position_4);
		else if (pos == 2)
			locatemodetxt.setText(R.string.setting_position_5);
		else if (pos == 3) {
			locatemodetxt.setText(R.string.setting_position_6);
		}
		//
		int pos1 = findWhich(Settings.PREF_LOCSETTIME,
				Settings.DEFAULT_PREF_LOCSETTIME);
		if (pos1 == 0)
			settimetxt.setText("5S");
		else if (pos1 == 1)
			settimetxt.setText("15S");
		else if (pos1 == 2)
			settimetxt.setText("30S");
		else if (pos1 == 3)
			settimetxt.setText("80S");

		//
		int pos2 = findWhich(Settings.PREF_LOCUPLOADTIME,
				Settings.DEFAULT_PREF_LOCUPLOADTIME);
		if (pos2 == 0)
			uploadtimetxt.setText("5S");
		else if (pos2 == 1)
			uploadtimetxt.setText("15S");
		else if (pos2 == 2)
			uploadtimetxt.setText("30S");
		else if (pos2 == 3)
			uploadtimetxt.setText("80S");

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.postion_set:
			// Ĭ�ϴӲ���λ
			// ����һ��Dialog��ʾ�򣬹�ѡ��λģʽ
			int pos = findWhich(Settings.PREF_LOCATEMODE,
					Settings.DEFAULT_PREF_LOCATEMODE);
			createDialog(R.string.setting_position_2, pos).show();

			break;
		case R.id.postion_settime:// gps����ʱ��
			int pos1 = findWhich(Settings.PREF_LOCSETTIME,
					Settings.DEFAULT_PREF_LOCSETTIME);
			createSetTimeDialog(pos1).show();
			break;

		case R.id.postion_uploadtime:// gps�ϴ�ʱ��
			int pos2 = findWhich(Settings.PREF_LOCUPLOADTIME,
					Settings.DEFAULT_PREF_LOCUPLOADTIME);
			createUploadTimeDialog(pos2).show();
			break;
		}
	}

	private int findWhich(String key, int defaultvalue) {
		return mSharedPreferences.getInt(key, defaultvalue);
	}

	// ��λ����
	private Dialog createDialog(final int title, int pos) {
		return new AlertDialog.Builder(GpsSetActivity.this)
				.setTitle(title)
				.setSingleChoiceItems(R.array.locateModle_txt_list, pos,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// if (DeviceInfo.CONFIG_GPS == 1
								// && whichButton == 3) {
								// Toast.makeText(GpsSetActivity.this,
								// "������Ч��", Toast.LENGTH_SHORT).show();
								// dialog.dismiss();
								// return;
								// }
								// �����ϱ�ģʽ
								MemoryMg.getInstance().GpsLocationModel = whichButton;
								// �����ϱ�ģʽ
								commit(Settings.PREF_LOCATEMODE, whichButton);

									// �Ӳ���λ �ر�gps��λ
									// ��
									Receiver.GetCurUA().GPSCloseLock();
									if(whichButton != 3){
									// ��
										Receiver.GetCurUA().GPSOpenLock();
									}
								updateSunmary();

								MyLog.e("GpsSetActivity", "whichButton is:"
										+ whichButton);

								dialog.dismiss();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).create();
	}

	private Dialog createSetTimeDialog(int pos) {
		return new AlertDialog.Builder(GpsSetActivity.this)
				.setTitle(R.string.setting_position_7)
				.setSingleChoiceItems(R.array.gps_txt_settime, pos,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (MemoryMg.getInstance().GpsLocationModel < 3
										&& MemoryMg.getInstance().GpsLocationModel > -1)
									// ��GPS
									Receiver.GetCurUA().GPSCloseLock();
								// GPS��λ���
								MemoryMg.getInstance().GpsSetTimeModel = whichButton;
								//
								commit(Settings.PREF_LOCSETTIME, whichButton);

								updateSunmary();

								MyLog.e("GpsSetActivity", "whichButton is:"
										+ whichButton);

								dialog.dismiss();
								if (MemoryMg.getInstance().GpsLocationModel < 3
										&& MemoryMg.getInstance().GpsLocationModel > -1)
									// ��GPS�߳�
									Receiver.GetCurUA().GPSOpenLock();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).create();
	}

	private Dialog createUploadTimeDialog(int pos) {
		return new AlertDialog.Builder(GpsSetActivity.this)
				.setTitle(R.string.setting_position_8)
				.setSingleChoiceItems(R.array.gps_txt_settime, pos,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (MemoryMg.getInstance().GpsLocationModel < 3
										&& MemoryMg.getInstance().GpsLocationModel > -1)
									// ��GPS
									Receiver.GetCurUA().GPSCloseLock();

								// GPS�ϱ����
								MemoryMg.getInstance().GpsUploadTimeModel = whichButton;
								//
								commit(Settings.PREF_LOCUPLOADTIME, whichButton);

								updateSunmary();

								MyLog.e("GpsSetActivity", "whichButton is:"
										+ whichButton);

								dialog.dismiss();

								if (MemoryMg.getInstance().GpsLocationModel < 3
										&& MemoryMg.getInstance().GpsLocationModel > -1)
									// ��GPS�߳�
									Receiver.GetCurUA().GPSOpenLock();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).create();
	}

	private void commit(String key, int value) {
		Editor edit = mSharedPreferences.edit();
		edit.putInt(key, value);
		edit.commit();
	}

}
