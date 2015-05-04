package com.zed3.sipua.ui;

import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zed3.location.MemoryMg;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.contant.Contants;
import com.zed3.toast.MyToast;
import com.zed3.utils.SwitchButton;
import com.zed3.utils.Tools;
import com.zed3.video.DeviceVideoInfo;

public class SettingVideoSize extends BaseActivity {
	public boolean isFront;
	EditText et_iframe,et_netrate,et_frame;
	
	private final String sharedPrefsFile = "com.zed3.sipua_preferences";

	private RadioGroup videocodinggroup = null;
	private RadioButton videoh264, videoh264s;
	//
	private RadioGroup maingroup = null;
	private RadioButton frontcamera, backcamera;
	//
	private RadioGroup frontgroup = null;
	private RadioGroup backgroup = null;
	private RadioGroup screengroup = null;
	private RadioGroup rg_allowloast = null;
	private RadioButton rb_one,rb_two,rb_three,rb_four,rb_five;
	private RadioButton rad_ver,rad_hor,rad_rotate;
	private RadioButton /*frontqcif,*/frontd1, frontcif, frontqvga, frontvga,
	front384288, front480320;
	private RadioButton /* backqcif,*/back720p, backd1, backcif, backqvga, 
	backvga, back384288, back480320;
	//
	private RadioGroup gvsgroup = null;
	private RadioButton gvsqcif, gvscif, gvs4cif, gvsqvga, nogvs;
	LinearLayout btn_left;
	SharedPreferences mypre = null;
	CheckBox chklock = null;
	private RadioGroup videoviewshow = null;
	private RadioButton videoviewshow1, videoviewshow2;
	// EditText h264sRate=null;
	SharedPreferences mSharedPreferences;
	SwitchButton fullscreen_ctrl,color_correct;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.settingvideosize);
		TextView title;
		title = (TextView) findViewById(R.id.title);
		title.setText(R.string.phonevideo_setting);
		ImageButton back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mSharedPreferences = getSharedPreferences(Settings.sharedPrefsFile,
				MODE_PRIVATE);
		color_correct = (SwitchButton) findViewById(R.id.color_correct);
		boolean flag_color_correct = mSharedPreferences.getBoolean(
				DeviceVideoInfo.VIDEO_COLOR_CORRECT,
				DeviceVideoInfo.DEFAULT_VIDEO_COLOR_CORRECT);
		if (flag_color_correct) {
			// log_ctrl.setImageResource(R.drawable.on);
			color_correct.setChecked(true);
		} else {
			color_correct.setChecked(false);
		}
		color_correct.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				boolean flag = mSharedPreferences.getBoolean(
						DeviceVideoInfo.VIDEO_COLOR_CORRECT,
						DeviceVideoInfo.DEFAULT_VIDEO_COLOR_CORRECT);
				commit(DeviceVideoInfo.VIDEO_COLOR_CORRECT, !flag);
				DeviceVideoInfo.color_correct = !flag;
			}
		});
		
		fullscreen_ctrl = (SwitchButton)findViewById(R.id.fullscreen_ctrl);
		boolean flag_fullscreen = mSharedPreferences.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_FULLSCREEN, DeviceVideoInfo.DEFAULT_VIDEO_SUPPORT_FULLSCREEN);
		if (flag_fullscreen) {
			// log_ctrl.setImageResource(R.drawable.on);
			fullscreen_ctrl.setChecked(true);
		}else{
			fullscreen_ctrl.setChecked(false);
		}
		fullscreen_ctrl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				boolean flag = mSharedPreferences.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_FULLSCREEN, DeviceVideoInfo.DEFAULT_VIDEO_SUPPORT_FULLSCREEN);
				commit(DeviceVideoInfo.VIDEO_SUPPORT_FULLSCREEN, !flag);
				DeviceVideoInfo.supportFullScreen = !flag;
			}
		});
		setDefaultValue(this);
		screengroup =(RadioGroup)findViewById(R.id.screengroup);
		rad_ver = (RadioButton)findViewById(R.id.ver_screen);
		rad_hor =(RadioButton)findViewById(R.id.hor_screen);
		rad_rotate =(RadioButton)findViewById(R.id.rotate_screen);
		if(DeviceVideoInfo.screen_type.equals("ver")){
			screengroup.check(rad_ver.getId());
		}else if(DeviceVideoInfo.screen_type.equals("hor")){
			screengroup.check(rad_hor.getId());
		}else{
			screengroup.check(rad_rotate.getId());
		}
		screengroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String ori = "ver";
				if(checkedId == rad_ver.getId()){
					ori = "ver";
					DeviceVideoInfo.isHorizontal = false;
					DeviceVideoInfo.supportRotate = false;
					DeviceVideoInfo.onlyCameraRotate = true;
				}else if(checkedId == rad_hor.getId()){
					ori = "hor";
					DeviceVideoInfo.isHorizontal = true;
					DeviceVideoInfo.supportRotate = false;
					DeviceVideoInfo.onlyCameraRotate = true;
				}else if(checkedId == rad_rotate.getId()){
					ori = "rotate";
					DeviceVideoInfo.isHorizontal = false;
					DeviceVideoInfo.supportRotate = true;
					DeviceVideoInfo.onlyCameraRotate = false;
				}
				DeviceVideoInfo.screen_type = ori;
				commit(DeviceVideoInfo.SCREEN_TYPE, ori);
			}
		});
		videocodinggroup = (RadioGroup) findViewById(R.id.videocodinggroup);
		videoh264 = (RadioButton) findViewById(R.id.videoh264);
		videoh264s = (RadioButton) findViewById(R.id.videoh264s);
		videocodinggroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						mypre = getSharedPreferences(sharedPrefsFile,
								Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = mypre.edit();
						// h264
						if (checkedId == videoh264.getId()) {
							editor.putString("videocode", "0");
						}
						// h264s
						if (checkedId == videoh264s.getId()) {
							editor.putString("videocode", "1");
						}
						editor.commit();
					}
				});
		
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
		// 摄像头
		maingroup = (RadioGroup) findViewById(R.id.maingroup);
		frontcamera = (RadioButton) findViewById(R.id.frontcamera);
		backcamera = (RadioButton) findViewById(R.id.backcamera);
		videoviewshow= (RadioGroup) findViewById(R.id.videoviewshow);
		videoviewshow1= (RadioButton) findViewById(R.id.videoviewshow1);
		videoviewshow2= (RadioButton) findViewById(R.id.videoviewshow2);
		
		videoviewshow.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mypre = getSharedPreferences(sharedPrefsFile,
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = mypre.edit();
				if(checkedId == videoviewshow1.getId()){
					editor.putInt("videoshowtype", 1);
				}else if(checkedId == videoviewshow2.getId()){
					editor.putInt("videoshowtype", 2);
				}
				editor.commit();
			}
		});
		maingroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						mypre = getSharedPreferences(sharedPrefsFile,
								Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = mypre.edit();
						// 前置
						if (checkedId == frontcamera.getId()) {
							backgroup.setVisibility(View.GONE);
							frontgroup.setVisibility(View.VISIBLE);
							editor.putString("usevideokey", "1");
							isFront = true;
						}
						// 后置
						if (checkedId == backcamera.getId()) {
							frontgroup.setVisibility(View.GONE);
							backgroup.setVisibility(View.VISIBLE);
							editor.putString("usevideokey", "0");
							isFront = false;
						}
						updateVedioframe(getCurVideoKey());
						editor.commit();
					}
				});

		// 前置分辨率
		frontgroup = (RadioGroup) findViewById(R.id.frontgroup);
//		frontqcif =(RadioButton)findViewById(R.id.frontqcif);//7
		frontqvga = (RadioButton) findViewById(R.id.frontqvga);// 5
		frontcif = (RadioButton) findViewById(R.id.frontcif);// 4
		frontd1 = (RadioButton) findViewById(R.id.frontd1);// 3
		frontvga = (RadioButton) findViewById(R.id.frontvga);// 6
		front384288 = (RadioButton) findViewById(R.id.front384288);//8
		front480320 = (RadioButton) findViewById(R.id.front480320);//9
		frontgroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						mypre = getSharedPreferences(sharedPrefsFile,
								Activity.MODE_PRIVATE);
						String key = "6";
						SharedPreferences.Editor editor = mypre.edit();

						if (checkedId == frontd1.getId()) {
							editor.putString("videoresolutionkey", "3");
							key = "3";
						} else if (checkedId == frontcif.getId()) {
							editor.putString("videoresolutionkey", "4");
							key = "4";
						} else if (checkedId == frontqvga.getId()) {
							editor.putString("videoresolutionkey", "5");
							key = "5";
						} else if (checkedId == frontvga.getId()) {
							editor.putString("videoresolutionkey", "6");
							key = "6";
						}/*else if(checkedId == frontqcif.getId()){
							editor.putString("videoresolutionkey", "7");
							key = "7";
						}*/else if (checkedId == front384288.getId()) {// 384*288
							editor.putString("videoresolutionkey", "8");
							key = "8";
						} else if (checkedId == front480320.getId()) {// 480*320
							editor.putString("videoresolutionkey", "9");
							key = "9";
						}

						editor.commit();
						updateVedioframe(key);
					}
				});

		// 后置分辨率
		backgroup = (RadioGroup) findViewById(R.id.backgroup);
//		backqcif =(RadioButton)findViewById(R.id.backqcif);//7
		backqvga = (RadioButton) findViewById(R.id.backqvga);// 5
		backcif = (RadioButton) findViewById(R.id.backcif);// 4
		backd1 = (RadioButton) findViewById(R.id.backd1);// 3
		backvga = (RadioButton) findViewById(R.id.backvga);// 6

		 back720p = (RadioButton) findViewById(R.id.back720p);//2
		 back384288 = (RadioButton) findViewById(R.id.back384288);// 8
		back480320 = (RadioButton) findViewById(R.id.back480320);// 9
		backgroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						mypre = getSharedPreferences(sharedPrefsFile,
								Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = mypre.edit();
						String key = "6";
						if (checkedId == backqvga.getId()) {
							editor.putString("videoresolutionkey0", "5");
							key = "5";
						} else if (checkedId == backcif.getId()) {
							editor.putString("videoresolutionkey0", "4");
							key = "4";
						} else if (checkedId == backd1.getId()) {
							editor.putString("videoresolutionkey0", "3");
							key = "3";
						} else if (checkedId == backvga.getId()) {
							editor.putString("videoresolutionkey0", "6");
							key = "6";
						}
						 else if (checkedId == back720p.getId()) {
						 editor.putString("videoresolutionkey0", "2");
						 key = "2";
						 }/*else if (checkedId == backqcif.getId()) {
							 editor.putString("videoresolutionkey0", "7");
							 key = "7";
							 }*/
						 else if (checkedId == back384288.getId()) {// 384*288
								editor.putString("videoresolutionkey0", "8");
								key = "8";
							} else if (checkedId == back480320.getId()) {// 480*320
								editor.putString("videoresolutionkey0", "9");
								key = "9";
							}
						editor.commit();
						updateVedioframe(key);
					}
				});

		// gvs转码设置
		gvsgroup = (RadioGroup) findViewById(R.id.gvsgroup);

		gvsqcif = (RadioButton) findViewById(R.id.gvsqcif);
		gvscif = (RadioButton) findViewById(R.id.gvscif);
		gvs4cif = (RadioButton) findViewById(R.id.gvs4cif);
		nogvs = (RadioButton) findViewById(R.id.gvs720);

		gvsgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				mypre = getSharedPreferences(sharedPrefsFile,
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = mypre.edit();

				if (checkedId == gvsqcif.getId()) {// QCIF
					editor.putString("gvstransvideosizekey", "3");
					MemoryMg.getInstance().GvsTransSize = "3";
				} else if (checkedId == gvs4cif.getId()) {// 4CIF
					MemoryMg.getInstance().GvsTransSize = "4";
					editor.putString("gvstransvideosizekey", "4");
				} else if (checkedId == gvscif.getId()) {// CIF
					MemoryMg.getInstance().GvsTransSize = "5";
					editor.putString("gvstransvideosizekey", "5");
				} else// 720p
				{
					MemoryMg.getInstance().GvsTransSize = "6";
					editor.putString("gvstransvideosizekey", "6");
				}
				editor.commit();

			}
		});

		// chklock = (CheckBox) findViewById(R.id.chklock);
		// chklock.setOnCheckedChangeListener(new
		// CompoundButton.OnCheckedChangeListener() {
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		// // TODO Auto-generated method stub
		// if (isChecked)
		// //
		// MemoryMg.getInstance().chklock = true;
		// else
		// //
		// MemoryMg.getInstance().chklock = false;
		// }
		// });

		// temp
		// h264sRate=(EditText)findViewById(R.id.h264srate);
		et_frame =(EditText)findViewById(R.id.frame_rate);
		et_iframe =(EditText)findViewById(R.id.iframerate);
		et_netrate =(EditText)findViewById(R.id.net_rate);
		et_frame.clearFocus();
		et_iframe.clearFocus();
		et_netrate.clearFocus();
		InitRadioButton();
		updateVedioframe(getCurVideoKey());
	}

	// 初始化
	private void InitRadioButton() {
		
		mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
		// videocode
		String videocode = mypre.getString("videocode", "0");
		if (videocode.equals("0"))// h264
			videocodinggroup.check(R.id.videoh264);
		else
			videocodinggroup.check(R.id.videoh264s);
		
		int videoviewtype = mypre.getInt("videoshowtype", 1);
		if(videoviewtype == 1){
			videoviewshow.check(R.id.videoviewshow1);
		}else{
			videoviewshow.check(R.id.videoviewshow2);
		}
		//
		String videokey = mypre.getString("usevideokey", "0");//
		if (videokey.equals("0"))// 后置
		{
			frontgroup.setVisibility(View.GONE);
			backgroup.setVisibility(View.VISIBLE);

			maingroup.check(R.id.backcamera);
		}
		if (videokey.equals("1"))// 前置
		{
			backgroup.setVisibility(View.GONE);
			frontgroup.setVisibility(View.VISIBLE);

			maingroup.check(R.id.frontcamera);
		}
		// 前置
		String pixkey = mypre.getString("videoresolutionkey", "5");//
		// 根据摄像头所支持的分辨率设置选项的显示与隐藏
		String temp = MemoryMg.getInstance().SupportVideoSizeStr;
		MyLog.e("SupportVideoSizeStr", temp);

		if (temp.length() > 0) {

			String[] arr = temp.split(",");
			if (arr != null) {
//				frontqcif.setVisibility(View.GONE);
//				backqcif.setVisibility(View.GONE);
				frontqvga.setVisibility(View.GONE);
				backqvga.setVisibility(View.GONE);
				frontcif.setVisibility(View.GONE);
				backcif.setVisibility(View.GONE);
				frontd1.setVisibility(View.GONE);
				backd1.setVisibility(View.GONE);
				backvga.setVisibility(View.GONE);
				 back720p.setVisibility(View.GONE);
				 front384288.setVisibility(View.GONE);
					front480320.setVisibility(View.GONE);
					back384288.setVisibility(View.GONE);
					back480320.setVisibility(View.GONE);
				for (String t : arr) {
					/*if(t.equals("176*144")){
						frontqcif.setVisibility(View.VISIBLE);
						backqcif.setVisibility(View.VISIBLE);
					}
					else*/ if (t.equals("320*240")) {
						frontqvga.setVisibility(View.VISIBLE);
						backqvga.setVisibility(View.VISIBLE);
					} else if (t.equals("352*288")) {
						frontcif.setVisibility(View.VISIBLE);
						backcif.setVisibility(View.VISIBLE);
					} /*else if (t.equals("720*480")) {//D1
						frontd1.setVisibility(View.VISIBLE);
						backd1.setVisibility(View.VISIBLE);
					}*/ else if (t.equals("640*480")) {
						backvga.setVisibility(View.VISIBLE);
						frontvga.setVisibility(View.VISIBLE);
					}
					 else if (t.equals("1280*720")) {
					 back720p.setVisibility(View.VISIBLE);
					 }else if (t.equals("384*288")) {
							back384288.setVisibility(View.VISIBLE);
							front384288.setVisibility(View.VISIBLE);
						} else if (t.equals("480*320")) {
							back480320.setVisibility(View.VISIBLE);
							front480320.setVisibility(View.VISIBLE);
						}
				}
			}

		}
		// 前置
		if (pixkey.equals("5")) {
			frontgroup.check(R.id.frontqvga);
		} else if (pixkey.equals("4")) {
			frontgroup.check(R.id.frontcif);
		} else if (pixkey.equals("3")) {
			frontgroup.check(R.id.frontd1);
		} else if (pixkey.equals("6")) {
			frontgroup.check(R.id.frontvga);
		}else if (pixkey.equals("2")) {//720p
		 backgroup.check(R.id.back720p);
		 }else if(pixkey.equals("7")){
			 frontgroup.check(R.id.frontqcif);
		 }else if(pixkey.equals("8")){
			 frontgroup.check(R.id.front384288);
		 }else if(pixkey.equals("9")){
			 frontgroup.check(R.id.front480320);
		 }else{
			 frontgroup.check(R.id.frontvga);
		 }
		// 后置
		pixkey = mypre.getString("videoresolutionkey0", "5");//
		if (pixkey.equals("5")) {
			backgroup.check(R.id.backqvga);
		} else if (pixkey.equals("4")) {
			backgroup.check(R.id.backcif);
		} else if (pixkey.equals("3")) {
			backgroup.check(R.id.backd1);
		} else if (pixkey.equals("6")) {
			backgroup.check(R.id.backvga);
		}else if(pixkey.equals("7")){
			backgroup.check(R.id.backqcif);
		}else if(pixkey.equals("8")){
			backgroup.check(R.id.back384288);
		}else if(pixkey.equals("9")){
			backgroup.check(R.id.back480320);
		}else if (pixkey.equals("2")) {
		 backgroup.check(R.id.back720p);
		 }
		else
			backgroup.check(R.id.backvga);

		// ------------------------转码---------------------------------------
		//
		String gvspixkey = mypre.getString("gvstransvideosizekey", "5");//
		if (gvspixkey.equals("3")) {// qcif
			gvsgroup.check(R.id.gvsqcif);
		} else if (gvspixkey.equals("4")) {// 4cif
			gvsgroup.check(R.id.gvs4cif);
		} else if (gvspixkey.equals("5")) {// cif
			gvsgroup.check(R.id.gvscif);
		} else
			// 720
			gvsgroup.check(R.id.gvs720);

		// if (MemoryMg.getInstance().chklock)
		// chklock.setChecked(true);
		// else
		// chklock.setChecked(false);
		// String ratetxt = mypre.getString("h264srate", "120");//
		// h264sRate.setText(ratetxt);
		rg_allowloast = (RadioGroup)findViewById(R.id.rg_packetlost);
		rg_allowloast.check(findId());//calc 
		rg_allowloast.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int level = DeviceVideoInfo.DEFAULT_PACKET_LOST_LEVEL;
				switch(checkedId){
				case  R.id.one:
					level = 1;
					break;
				case R.id.two:
					level = 2;
					break;
				case R.id.three:
					level = 3;
					break;
				case R.id.four:
					level = 4;
					break;
				case R.id.five:
					level = 5;
					break;
				}
				DeviceVideoInfo.lostLevel = level;
				commit(DeviceVideoInfo.PACKET_LOST_LEVEL,level);
			}
		});;
	}

	private int findId() {
		int level = mSharedPreferences.getInt(DeviceVideoInfo.PACKET_LOST_LEVEL, DeviceVideoInfo.DEFAULT_PACKET_LOST_LEVEL);
		int id = R.id.one;
		switch(level){
		case 1:
			id = R.id.one;
			break;
		case 2:
			id = R.id.two;
			break;
		case 3:
			id = R.id.three;
			break;
		case 4:
			id = R.id.four;
			break;
		case 5:
			id = R.id.five;
			break;
		}
		return id;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//
		// mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
		// SharedPreferences.Editor editor = mypre.edit();
		// editor.putString("h264srate", h264sRate.getText().toString());
		// editor.commit();
		
				
	}
	public void onSave(View v){
		if(TextUtils.isEmpty(et_iframe.getText())){
			MyToast.showToast(true, SettingVideoSize.this, "I帧间隔不能为空");
			return;
		}
		if(TextUtils.isEmpty(et_netrate.getText())){
			MyToast.showToast(true, SettingVideoSize.this, "码率不能为空");
			return;
		}
		if(TextUtils.isEmpty(et_frame.getText())){
			MyToast.showToast(true, SettingVideoSize.this, "帧率不能为空");
			return;
		}
		String result = et_iframe.getText()+","+et_frame.getText()+","+et_netrate.getText();
		if(mypre != null){
			boolean is3G = NetChecker.is3G(SettingVideoSize.this);
			String add = is3G?"3g":"wifi";
			SharedPreferences.Editor editor = mypre.edit();
			String key = getCurVideoKey();
			editor.putString(getCurVideoSize(key)+add, result);
			editor.commit();
		}
	}
	public static String getDefaultValueWifi(String key){
		String value = "";
		if("2".equals(key)){
//			value ="1,10,800";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,800";
			}
		}else if("3".equals(key)){
//			value ="1,10,300";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,300";
			}
		}else if("4".equals(key)){
//			value ="1,10,300";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,300";
			}
		}else if("5".equals(key)){
//			value ="1,20,200";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,20,200";
			}
		}else if("6".equals(key)){
//			value ="1,10,500";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,500";
			}
		}else if("7".equals(key)){
//			value="1,10,200";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,200";
			}
		}else {
			value = "1,10,300";
		}
		return value;
	}
	public static String getDefaultValue3G(String key){
		String value = "";
		if("2".equals(key)){
//			value ="1,15,800";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,15,800";
			}
		}else if("3".equals(key)){
//			value ="1,10,300";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,300";
			}
		}else if("4".equals(key)){
//			value ="1,10,300";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,300";
			}
		}else if("5".equals(key)){
//			value ="1,10,200";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,200";
			}
		}else if("6".equals(key)){
//			value ="1,10,500";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,300";
			}
		}else if("7".equals(key)){
//			value="1,10,200";
			//HUAWEI G7-TL00
			if (Tools.matchDevice(Contants.MODEL_HUAWEI_G7)) {
				value ="1,15,4000";
			}else {
				value ="1,10,200";
			}
		}else {
			value = "1,10,300";
		}
		return value;
	}
	public void updateVedioframe(String key){
//		String defaultvalue = getDefaultValue(key);
		String result = getCurVideoSize(key);
		boolean is3G = NetChecker.is3G(SettingVideoSize.this);
		String add = is3G?"3g":"wifi";
		String[] info = mypre.getString(result+add, /*defaultvalue*/"5,10,300").split(",");
		if(info.length == 3){
			et_iframe.setText(info[0]);
			et_frame.setText(info[1]);
			et_netrate.setText(info[2]);
		}else{
//			MyToast.showToast(true, SettingVideoSize.this, "更新失败");
		}
	}
	public static String getCurVideoSize(String key){//320*240
		String result = "";
		if("2".equals(key)){//720
			result = "1080*720";
		}else if("3".equals(key)){
			result = "720*480";
		}else if("4".equals(key)){
			result = "352*288";
		}else if("5".equals(key)){
			result = "320*240";
		}else if("6".equals(key)){
			result = "640*480";
		}else if("7".equals(key)){
			result = "176*144";
		}else if ("8".equals(key)) {
			result = "384*288";
		} else if ("9".equals(key)) {
			result = "480*320";
		}
		return result;
	}
	public static void setDefaultValue(Context ctx){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = sp.edit();
		boolean is3G = NetChecker.is3G(ctx);
		String add = is3G?"3g":"wifi";
		for(int i =2;i<10;i++){
			String size = getCurVideoSize(i+"");
			String result = sp.getString(size+add, "aaa");
			String value = NetChecker.is3G(ctx)?getDefaultValue3G
					(i+""):getDefaultValueWifi
					(i+"");
			if(result.equals("aaa")|| result.equals(size)){
				edit.putString(getCurVideoSize(i+"")+add, value);
				edit.commit();
			}
		}
	}
	String getCurVideoKey(){
		String dd ="";
		if(isFront){
			if(mypre != null){
				dd = mypre.getString("videoresolutionkey", "6");
			}
		}else {
			if(mypre != null){
				dd = mypre.getString("videoresolutionkey0", "6");
			}
		}
		return dd;
	}
	/**
	 * @param String like as cif,qcif,vga
	 * @return as 176*144
	 * */
	public static String getResolution(String tag){
		String result = "";
		if("cif".equals(tag)){
			result ="352*288";
		}else if("qcif".equals(tag)){
			result ="176*144";
		}else if("720p".equals(tag)){
			result ="1280*720";
		}else if("qvga".equals(tag)){
			result ="320*240";
		}else if("vga".equals(tag)){
			result = "640*480";
		}else if ("384*288".equals(tag)) {
			result = "384*288";
		} else if ("480*320".equals(tag)) {
			result = "480*320";
		}
		return  result;
	}
	private void commit(String key, boolean value) {
		Editor edit = mSharedPreferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}
	private void commit(String key, String value) {
		Editor edit = mSharedPreferences.edit();
		edit.putString(key, value);
		edit.commit();
	}
	private void commit(String key, int value) {
		Editor edit = mSharedPreferences.edit();
		edit.putInt(key, value);
		edit.commit();
	}
}
