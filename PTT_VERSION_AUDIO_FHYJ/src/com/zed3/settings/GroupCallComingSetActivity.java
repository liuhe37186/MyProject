package com.zed3.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
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

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent.GrpCallSetupType;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.SwitchButton;

/**
 * 对讲来电处理设置
 * @author zed
 *
 */
public class GroupCallComingSetActivity extends BaseActivity implements
		OnClickListener {
	LinearLayout high, same, low, btn_left,pttkey, restore;
	TextView high_summary, same_summary, low_summary, ptt_summary, restore_summary;
	private final String sharedPrefsFile = "com.zed3.sipua_preferences";
	SharedPreferences mypre = null;
	String keyVal = "";
	SwitchButton restore_switcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_groupcallcoming);
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setText(R.string.setting_intercom_call);
		ImageButton back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		high = (LinearLayout) findViewById(R.id.high);
		high.setOnClickListener(this);
		same = (LinearLayout) findViewById(R.id.same);
		same.setOnClickListener(this);
		low = (LinearLayout) findViewById(R.id.low);
		low.setOnClickListener(this);
		pttkey = (LinearLayout) findViewById(R.id.pttkey);
		pttkey.setOnClickListener(this);
		restore = (LinearLayout) findViewById(R.id.restore);
		restore.setOnClickListener(this);
		
		restore_switcher = (SwitchButton) findViewById(R.id.restore_switcher);

		high_summary = (TextView) findViewById(R.id.high_summary);
		same_summary = (TextView) findViewById(R.id.same_summary);
		low_summary = (TextView) findViewById(R.id.low_summary);
		mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
        ptt_summary = (TextView) findViewById(R.id.pttsummary);
        restore_summary = (TextView) findViewById(R.id.restore_summary);
		updateSunmary();
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
		
		restore_switcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
				
				commit(Settings.RESTORE_AFTER_OTHER_GROUP, isChecked);
				updateSunmary();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.high:
			int pos = findWhich(Settings.HIGH_PRI_KEY,1);
			createDialog(R.string.setting_intercom_1, pos).show();
			break;
		case R.id.same:
			int pos1 = findWhich(Settings.SAME_PRI_KEY,0);
			createDialog(R.string.setting_intercom_2, pos1).show();
			break;
		case R.id.low:
			int pos2 = findWhich(Settings.LOW_PRI_KEY,2);
			createDialog(R.string.setting_intercom_3, pos2).show();
			break;
		case R.id.pttkey:
			showDialog_Layout();
			break;
		case R.id.restore:
			restore_switcher.toggle();
			break;
		}
	}

	// 显示基于Layout的AlertDialog
	private void showDialog_Layout() {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View textEntryView = inflater
				.inflate(R.layout.dialoglayout, null);
		final EditText edtInput = (EditText) textEntryView
				.findViewById(R.id.edtInput);
		edtInput.setText(keyVal);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon22);
		builder.setTitle(R.string.key_set);
		builder.setView(textEntryView);
		builder.setPositiveButton(getResources().getString(R.string.key), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mypre = getSharedPreferences(sharedPrefsFile,
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = mypre.edit();
				if (!edtInput.getText().toString().equals(""))
					editor.putString("pttkey", edtInput.getText().toString());
				editor.commit();
				ptt_summary.setText(getResources().getString(R.string.key) + edtInput.getText().toString());
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// setTitle("");

			}
		});
		builder.show();
	}

	private int findWhich(String key,int val) {
		return mypre.getInt(key, val);
	}

	private Dialog createDialog(final int title, int pos) {
		return new AlertDialog.Builder(GroupCallComingSetActivity.this)
				.setTitle(title)
				.setSingleChoiceItems(R.array.name_list, pos,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (title == R.string.setting_intercom_1) {
									commit(Settings.HIGH_PRI_KEY, whichButton);
								} else if (title == R.string.setting_intercom_2) {
									commit(Settings.SAME_PRI_KEY, whichButton);
								} else if (title == R.string.setting_intercom_3) {
									commit(Settings.LOW_PRI_KEY, whichButton);
								}
								updateSunmary();
								updateGrpCallConfig();
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

	private void updateGrpCallConfig() {
		Receiver.GetCurUA().SetGrpCallConfig(
				getPriority(mypre.getInt(Settings.HIGH_PRI_KEY, 1)),
				getPriority(mypre.getInt(Settings.SAME_PRI_KEY, 0)),
				getPriority(mypre.getInt(Settings.LOW_PRI_KEY, 2)));

	}

	private GrpCallSetupType getPriority(int priority) {
		if (priority == 0)
			return GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
		else if (priority == 1)
			return GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT;
		else
			return GrpCallSetupType.GRPCALLSETUPTYPE_REJECT;
	}

	private void commit(String key, int value) {
		Editor edit = mypre.edit();
		edit.putInt(key, value);
		edit.commit();
	}
	
	private void commit(String key, boolean value) {
		Editor edit = mypre.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	private void updateSunmary() {
		int highvalue = mypre.getInt(Settings.HIGH_PRI_KEY, 1);
		high_summary
				.setText(getResources().getStringArray(R.array.name_list)[highvalue]);
		int samevalue = mypre.getInt(Settings.SAME_PRI_KEY, 0);
		same_summary
				.setText(getResources().getStringArray(R.array.name_list)[samevalue]);
		int lowvalue = mypre.getInt(Settings.LOW_PRI_KEY, 2);
		low_summary
				.setText(getResources().getStringArray(R.array.name_list)[lowvalue]);
		
		keyVal = mypre.getString("pttkey", "140");
		ptt_summary.setText(getResources().getString(R.string.key) + keyVal);
		boolean isRestore = mypre.getBoolean(Settings.RESTORE_AFTER_OTHER_GROUP, true);
		if (isRestore) {
			restore_switcher.setChecked(true);
			restore_summary.setText(R.string.rate_suspension_2);
		} else {
			restore_switcher.setChecked(false);
			restore_summary.setText(R.string.rate_suspension_1);
		}
	}
}
