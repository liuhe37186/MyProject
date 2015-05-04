package com.zed3.flow;

import com.zed3.location.MemoryMg;
import com.zed3.sipua.R;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.ui.CameraCall;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TotalFlowView extends Activity {
	private final String sharedPrefsFile = "com.zed3.sipua_preferences";

	TextView alarmtip = null;
	TextView detailtip = null;
	ImageView imgviewbtn = null;
	ImageView tooltipbtn = null;
	ImageView flowiconx=null;
	TextView alarmnumtxt = null;
	LinearLayout alarmlinear = null;
	// ptt
	TextView ptttotal = null;
	TextView pttlast = null;
	// video
	TextView videototal = null;
	TextView videolast = null;
	
	LinearLayout videoLinear=null;
	ImageButton backbtn=null;
	
	boolean flag = false, alarmFlag = false;
	SharedPreferences mypre = null;
	double useTotal = 0, lastTotal = 0, useMin = 0, lasttime = 0;
	double a = 0, b = 0, c = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.totalflowview);
		flowiconx=(ImageView)findViewById(R.id.flowiconx);
		// ������
		alarmtip = (TextView) findViewById(R.id.alarmtip);
		// ��ʹ xM,ʣ��x%,ʣ���վ�x����
		detailtip = (TextView) findViewById(R.id.detailtip);
		backbtn=(ImageButton) findViewById(R.id.back_button);
		backbtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TotalFlowView.this.finish();
			}
		});
		// ��������ϵ
		imgviewbtn = (ImageView) findViewById(R.id.imgviewbtn);
		imgviewbtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ImageView pic = (ImageView) v;
				mypre = getSharedPreferences(sharedPrefsFile,
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = mypre.edit();

				if (flag == false) {// ��
					pic.setImageResource(R.drawable.off);
					flag = true;
					editor.putBoolean("flowtooltip", false);
					MemoryMg.getInstance().isProgressBarTip=false;
				} else {
					pic.setImageResource(R.drawable.on);// ��
					flag = false;
					editor.putBoolean("flowtooltip", true);
					MemoryMg.getInstance().isProgressBarTip=true;
				}
				editor.commit();
			}
		});
		// ����Ԥ��
		tooltipbtn = (ImageView) findViewById(R.id.tooltipbtn);
		tooltipbtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ImageView pic = (ImageView) v;
				mypre = getSharedPreferences(sharedPrefsFile,
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = mypre.edit();

				if (alarmFlag == false) {
					pic.setImageResource(R.drawable.off);
					alarmFlag = true;
					editor.putBoolean("flowalarmout", false);
					
					//Ԥ��ֵҲҪ����
					MemoryMg.getInstance().User_3GFlowOut = 0;
					editor.putString("3gflowoutval", "0");
					
					alarmlinear.setVisibility(View.GONE);
				} else {// ��
					pic.setImageResource(R.drawable.on);
					alarmFlag = false;
					editor.putBoolean("flowalarmout", true);

					alarmlinear.setVisibility(View.VISIBLE);
				}
				editor.commit();
			}
		});

		// ��ʣ������Ԥ��
		alarmnumtxt = (TextView) findViewById(R.id.alarmnumtxt);

		// ����ʣ�౨����ʾ��
		alarmlinear = (LinearLayout) findViewById(R.id.alarmlinear);
		alarmlinear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				startActivity(new Intent(TotalFlowView.this, FlowAlarmSet.class));
				
			}
		});

		// ptt�ײ�������
		ptttotal = (TextView) findViewById(R.id.ptttotal);
		// �����ײ�ʣ��
		pttlast = (TextView) findViewById(R.id.pttlast);
		// ��Ƶ
		videototal = (TextView) findViewById(R.id.videototal);
		// video�����ײ�ʣ��
		videolast = (TextView) findViewById(R.id.videolast);
		
		videoLinear=(LinearLayout)findViewById(R.id.videolinear);
		
		InitView();
	}
	
	
	
	protected void onResume() {
		super.onResume();
		//��������������ֵ
		if (MemoryMg.getInstance().User_3GFlowOut == 0)
			alarmnumtxt.setText("δ����");
		else
			alarmnumtxt.setText(MemoryMg.getInstance().User_3GFlowOut + "M");

	};
	//
	private void InitView() {
		
		if ( !DeviceInfo.CONFIG_SUPPORT_VIDEO )
			videoLinear.setVisibility(View.INVISIBLE);

		// ����������
		mypre = getSharedPreferences(sharedPrefsFile, Activity.MODE_PRIVATE);
		// ����������
		boolean isalarm = mypre.getBoolean("flowtooltip", true);//
		if (isalarm) {
			imgviewbtn.setImageResource(R.drawable.on);
			flag = false;
		} else {
			flag = true;
			imgviewbtn.setImageResource(R.drawable.off);
		}
		// ����Ԥ������
		boolean isalarmout = mypre.getBoolean("flowalarmout", true);//
		if (isalarmout) {
			tooltipbtn.setImageResource(R.drawable.on);
			alarmFlag = false;
			alarmlinear.setVisibility(View.VISIBLE);
			
		} else {
			alarmFlag = true;
			tooltipbtn.setImageResource(R.drawable.off);
			alarmlinear.setVisibility(View.GONE);
		}
		
		// ptt�ײ��������ܶ�
		ptttotal.setText(calculateTotal(MemoryMg.getInstance().User_3GTotalPTT)
				+ "M");

		// video�ײ��������ܶ�
		videototal
				.setText(calculateTotal(MemoryMg.getInstance().User_3GTotalVideo)
						+ "M");

		if (mHandle.hasMessages(1))
			mHandle.removeMessages(1);
		mHandle.sendEmptyMessage(1);

	}



	Handler mHandle = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				try {
					lasttime = calculatePercent(
							MemoryMg.getInstance().User_3GRelTotal,
							MemoryMg.getInstance().User_3GTotal);
					// ��ʹ xM,ʣ��x%
					detailtip
							.setText("��ʹ��"
									+ calculateTotal(MemoryMg.getInstance().User_3GRelTotal)
									+ "M,ʣ��" + (100 - lasttime * 100) + "%");

					pttlast.setText(calculateTotal(MemoryMg.getInstance().User_3GTotalPTT
							- MemoryMg.getInstance().User_3GRelTotalPTT)
							+ "M");

					if (MemoryMg.getInstance().User_3GTotalVideo == 0)
						videolast.setText("0.0M");
					else {
						videolast.setTextColor(Color.RED);
						videolast
								.setText(calculateTotal(MemoryMg.getInstance().User_3GTotalVideo
										- MemoryMg.getInstance().User_3GRelTotalVideo)
										+ "M");
					}
					SetFontColor();
					// 8��һ��
					mHandle.sendMessageDelayed(mHandle.obtainMessage(1), 8000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private void SetFontColor() {
		// ===
		a = calculatePercent(MemoryMg.getInstance().User_3GRelTotal,
				MemoryMg.getInstance().User_3GTotal);
		
		if (a < 0.6 && a >= 0) {
			flowiconx.setImageResource(R.drawable.flowicon);
			
			alarmtip.setTextColor(Color.GREEN);
			alarmtip.setText("����ʣ���������㣬�����ʹ��");
		} else if (a < 0.9 && a >= 0.6) {
			flowiconx.setImageResource(R.drawable.flowiconyellow);
			alarmtip.setTextColor(Color.YELLOW);
			alarmtip.setText("����ʣ�������ѹ��룬���ʡʹ��");
		} else if (a >= 0.9) {
			flowiconx.setImageResource(R.drawable.flowiconred);
			alarmtip.setTextColor(Color.RED);
			alarmtip.setText("��������ʹ���Ѿ��ӽ��ײ���ֵ�������ײ���ֵ�����Ĺ�������");
		}

	}

	public double calculateTotal(double db) {
		double x = db / 1024d / 1024d;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}

	public double calculatePercent(double a, double b) {
		double x = a / b;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mHandle.hasMessages(1))
			mHandle.removeMessages(1);

	}

}
