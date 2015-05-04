package com.zed3.settings;


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.SettingNew;


public class AboutActivity extends BaseActivity {
	LinearLayout btn_left;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_about);
		TextView tv = (TextView)findViewById(R.id.title);
		TextView versionNameTV = (TextView)findViewById(R.id.version_name);
		tv.setText(R.string.setting_about);
		ImageButton back = (ImageButton)findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionNameTV.setText(packageInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TextView tv_show =(TextView)findViewById(R.id.t_leftbtn);
		tv_show.setText(R.string.settings);
		btn_left = (LinearLayout) findViewById(R.id.btn_leftbtn);
		btn_left.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
//				MainActivity.getInstance().startIntent(SettingNew.class);
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
}
