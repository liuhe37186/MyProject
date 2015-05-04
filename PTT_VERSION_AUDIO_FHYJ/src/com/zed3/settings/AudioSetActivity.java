package com.zed3.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.audio.AudioSettings;
import com.zed3.codecs.AmrNB;
import com.zed3.codecs.Codec;
import com.zed3.codecs.CodecBase;
import com.zed3.codecs.Codecs;
import com.zed3.codecs.EncodeRate;
import com.zed3.location.MemoryMg;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.SwitchButton;
import com.zed3.video.DeviceVideoInfo;

public class AudioSetActivity extends BaseActivity implements OnClickListener {
	LinearLayout arm_rate, ptime_set,audiovad,btn_left,phone_type;
	TextView rateValue, ptimeValue,audiovadvalue,phonetype_value;
	private SharedPreferences mSharedPreferences;
	SwitchButton aec_swtich;
	public static int curCodecNum = 114;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_audioset);
		mSharedPreferences = getSharedPreferences(Settings.sharedPrefsFile,
				MODE_PRIVATE);
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setText(R.string.setting_voice_call);
		arm_rate = (LinearLayout) findViewById(R.id.arm_rate);
		arm_rate.setOnClickListener(this);
		ptime_set = (LinearLayout) findViewById(R.id.ptime);
		ptime_set.setOnClickListener(this);
		audiovad =(LinearLayout)findViewById(R.id.audiovad);
		if(DeviceInfo.CONFIG_SUPPORT_VAD){
			audiovad.setOnClickListener(this);
		}else{
			String vadValues = mSharedPreferences.getString(Settings.AUDIO_VADCHK,
					Settings.DEFAULT_VAD_MODE);
			if(!vadValues.equals(Settings.DEFAULT_VAD_MODE)){
				commit2(Settings.AUDIO_VADCHK,Settings.DEFAULT_VAD_MODE);
			}
			audiovad.setVisibility(View.GONE);
			findViewById(R.id.audiovad_line).setVisibility(View.GONE);
		}
		phone_type = (LinearLayout) findViewById(R.id.phone_type);
		phone_type.setOnClickListener(this);
		
		
		rateValue = (TextView) findViewById(R.id.rate_value);
		ptimeValue = (TextView) findViewById(R.id.ptimevalue);
		audiovadvalue =(TextView)findViewById(R.id.audiovadvalue);
		phonetype_value =(TextView)findViewById(R.id.phonetype_value);
		//
		aec_swtich =(SwitchButton)findViewById(R.id.aec_switch);
		boolean flag_color_correct = mSharedPreferences.getBoolean(
				DeviceVideoInfo.AUDIO_AEC_SWITCH,
				DeviceVideoInfo.DEFAULT_AUDIO_AEC_SWITCH);
		if (flag_color_correct) {
			// log_ctrl.setImageResource(R.drawable.on);
			aec_swtich.setChecked(true);
		} else {
			aec_swtich.setChecked(false);
		}
		aec_swtich.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				boolean flag = mSharedPreferences.getBoolean(
						DeviceVideoInfo.AUDIO_AEC_SWITCH,
						DeviceVideoInfo.DEFAULT_AUDIO_AEC_SWITCH);
				commit(DeviceVideoInfo.AUDIO_AEC_SWITCH, !flag);
				AudioSettings.isAECOpen = !flag;
			}
		});
		updateSunmary();
		TextView tv_show =(TextView)findViewById(R.id.t_leftbtn);
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
				TextView tv =(TextView)findViewById(R.id.t_leftbtn);
				TextView tv_left =(TextView)findViewById(R.id.left_icon);
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
//					btn_home.setBackgroundResource(R.color.red);
					tv.setTextColor(Color.WHITE);
					btn_left.setBackgroundResource(R.color.btn_click_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_press);
					break;
				case MotionEvent.ACTION_UP:
//					btn_home.setBackgroundResource(R.color.font_color3);
					tv.setTextColor(getResources().getColor(R.color.font_color3));
					btn_left.setBackgroundResource(R.color.whole_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_release);
					break;
				}
				return false;
			}
		});
	}

	private void updateSunmary() {
		String modeValue = mSharedPreferences.getString(Settings.AMR_MODE,
				Settings.DEFAULT_AMR_MODE);
		// TODO edit by zdx
		rateValue.setText(modeValue + ("Auto".equals(modeValue)?"":"kbit/s"));
		String ptimeValues = mSharedPreferences.getString(Settings.PTIME_MODE,
				Settings.DEFAULT_PTIME_MODE);
		ptimeValue.setText(ptimeValues + "ms");
		//
		String vadValues = mSharedPreferences.getString(Settings.AUDIO_VADCHK,
				Settings.DEFAULT_VAD_MODE);
		audiovadvalue.setText(vadValues.equals("0") ? R.string.setting_detection_1:R.string.setting_detection_2);
		//通话类型
		String phonetypeValues = mSharedPreferences.getString(Settings.PHONE_MODE,
				Settings.DEFAULT_PHONE_MODE);
		phonetype_value.setText(phonetypeValues.equals("0") ? R.string.vc_type_1:R.string.vc_type_2);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.arm_rate:

			String modeValue = mSharedPreferences.getString(Settings.AMR_MODE,
					Settings.DEFAULT_AMR_MODE);
			int pos = findWhich(modeValue,
					getResources().getStringArray(R.array.amrMode_txt_list));
			createDialog(R.string.setting_ARM_title,
					R.array.amrMode_txt_list, pos).show();
			break;
		case R.id.ptime:
			String val = mSharedPreferences.getString(Settings.PTIME_MODE,
					Settings.DEFAULT_PTIME_MODE);
			int pos1 = findWhich(val,
					getResources().getStringArray(R.array.ptime_name_list));
			createDialog(R.string.setting_PIME_title,
					R.array.ptime_name_list, pos1).show();
			break;
		case R.id.audiovad:
			String vad = mSharedPreferences.getString(Settings.AUDIO_VADCHK,
					Settings.DEFAULT_VAD_MODE);
			
			int pos2 = findWhich((vad.equals("0") ? getResources().getString(R.string.setting_detection_1):getResources().getString(R.string.setting_detection_2)), getResources().getStringArray(R.array.gpstools_txt_list));
			createDialog(R.string.setting_PIME_detection, R.array.gpstools_txt_list,pos2).show();
			break;
		case R.id.phone_type:
			String phoneValue = mSharedPreferences.getString(Settings.PHONE_MODE,
					Settings.DEFAULT_PHONE_MODE);
			int phonepos = findWhich(phoneValue,
					getResources().getStringArray(R.array.phonetype_val_list));
			
			createDialog(R.string.vc_type,
					R.array.phonetype_list, phonepos).show();
			break;
		}
	}

	private int findWhich(String value, String[] array) {
		int which = 0;
		if (array.length > 0) {
			for (int i = 0; i < array.length; i++) {
				if (array[i].contains(value))
					return i;
			}
		}
		return which;
	}

	private Dialog createDialog(final int title, final int array, int pos) {
		return new AlertDialog.Builder(AudioSetActivity.this)
				.setTitle(title)
				.setSingleChoiceItems(array, pos,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (title == R.string.setting_ARM_title) {
									commit(Settings.AMR_MODE, getResources()
											.getStringArray(array)[whichButton]);
									String modeValue = mSharedPreferences
											.getString(Settings.AMR_MODE,
													Settings.DEFAULT_AMR_MODE);
									EncodeRate.Mode rate = AmrNB.getModeFromString(modeValue);
									CodecBase codecbase;
									for (Codec codec : Codecs.codecs) {
										codecbase = (CodecBase) codec;
										if ("AMR".equals(codecbase.name())) {
											((AmrNB) codecbase).setRate(rate);
										}
									}
								} else if (title == R.string.setting_PIME_title) {
									commit(Settings.PTIME_MODE, getResources()
											.getStringArray(array)[whichButton]);
									String val = mSharedPreferences.getString(
											Settings.PTIME_MODE,
											Settings.DEFAULT_PTIME_MODE);//
									if (val.equals("20")) {
										MemoryMg.PTIME = 20;
									} else if (val.equals("100")) {
										MemoryMg.PTIME = 100;
									} else if (val.equals("200")) {
										MemoryMg.PTIME = 200;
									}
								}
								else if(title == R.string.setting_PIME_detection){
									commit2(Settings.AUDIO_VADCHK,whichButton+"");
								}
								else if(title==R.string.vc_type)//通话类型
								{
									//用户设置后不再读取配置 add by liangzhang 2014-12-29
									mSharedPreferences.edit().putBoolean("isFirstLogin", false).commit();
									commit(Settings.PHONE_MODE,whichButton+"");
									MemoryMg.getInstance().PhoneType = whichButton;
								}
								updateSunmary();
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

	private void commit(String key, String value) {
		value = value.replace("kbit/s", "").replace("ms", "");
		Editor edit = mSharedPreferences.edit();
		edit.putString(key, value);
		edit.commit();
	}
	private void commit2(String key,String value){
		Editor edit = mSharedPreferences.edit();
		edit.putString(key, value);
		if (value.equals("0")){
//			edit.putString(Settings.PTIME_MODE, "100");
//			MemoryMg.PTIME = 100;
			MemoryMg.getInstance().isAudioVAD=false;
//			//设置属性为可编辑
//			arm_rate.setEnabled(true);
//			ptime_set.setEnabled(true);
		}
		else{
//			edit.putString(Settings.PTIME_MODE, "20");
//			MemoryMg.PTIME = 20;
			MemoryMg.getInstance().isAudioVAD=true;
//			//一定保证是4.75
//			edit.putString(Settings.AMR_MODE, "4.75");
//			//设置属性为不可编辑
//			arm_rate.setEnabled(false);
//			ptime_set.setEnabled(false);
		}
		edit.commit();
	}
	private void commit(String key, boolean value) {
		Editor edit = mSharedPreferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}
}
