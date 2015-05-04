package com.zed3.flow;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;

public class FlowRefreshService extends Service {
	TextView sip_send;
	TextView sip_receive;
	TextView gps_send;
	TextView gps_receive;
	TextView voice_send;
	TextView voice_receive;
	TextView video_send;
	TextView video_receive;
	TextView total_rate;
	TextView video_lost_count;
	TextView total_flow;
	Timer timer = null;

	TimerTask tt;

	Handler mHandle = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				if (isEasy == 0) {
					isEasy++;
					break;
				}
				MyLog.e("Flow===>", "fresh UI UI UI==>>>");
				MyLog.e("Flow===>", FlowStatistics.Voice_Send_Data + "");
				MyLog.e("Flow===>", FlowStatistics.Voice_Send_Data + "");		
				sip_send.setText(FlowStatistics.Sip_Send + "k/s");
				sip_receive.setText(FlowStatistics.Sip_Receive + "k/s");
				gps_send.setText(FlowStatistics.Gps_Send + "k/s");
				gps_receive.setText(FlowStatistics.Gps_Receive + "k/s");
				voice_send.setText(FlowStatistics.Voice_Send + "k/s");
				voice_receive.setText(FlowStatistics.Voice_Receive + "k/s");
				video_send.setText(FlowStatistics.Video_Send + "k/s");
				video_receive.setText(FlowStatistics.Video_Receive + "k/s");
				total_rate.setText(FlowStatistics.Total + "k/s");
				video_lost_count.setText(FlowStatistics.Video_Packet_Lost + "");
				total_flow.setText(FlowStatistics.Total_Flow + "k");
				MyLog.e("Build.MODEL==��", Build.MODEL);
//				if (Build.MODEL.contains("Coolpad")
//						|| Build.MODEL.contains("HUAWEI")) {
//					try {
//						if (!isAddView_)
//							wm.removeView(view_);
//							wm.addView(view_, wmParams_);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						Log.e("Coolpad or HUAWEI error", e.toString());
//						e.printStackTrace();
//					}
//				}GQTӢ�� 2014-9-4 �����Ϊy511�Ϳ����ֻ�����������ʱ����������
				break;
			}
		}

	};
	public static Boolean isAddView_ = true;
	WindowManager.LayoutParams wmParams;
	WindowManager.LayoutParams wmParams_;
	WindowManager wm;

	private int isEasy = 0;

	private int Sip_Send_Data = 0;

	private int Sip_Receive_Data = 0;

	private int Gps_Send_Data = 0;

	private int Gps_Receive_Data = 0;

	private int Voice_Send_Data = 0;

	private int Voice_Receive_Data = 0;
	private int Video_Send_Data = 0;

	private int Video_Receive_Data = 0;

	private int Total_Data = 0;

	private int Total_Data_old = 0;
	// ��С��������View
	View view;
	View view_;

	private boolean isMoved = false;// add by liangzhang 2014-08-22 ���view�Ƿ��ƶ�
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		view_ = View.inflate(this, R.layout.flow_details_message, null);
		// ��ʼ����ֵ7����¼����ֵ��TextView
		sip_send = (TextView) view_.findViewById(R.id.sip_send);
		sip_receive = (TextView) view_.findViewById(R.id.sip_receive);
		gps_send = (TextView) view_.findViewById(R.id.gps_send);
		gps_receive = (TextView) view_.findViewById(R.id.gps_receive);
		voice_send = (TextView) view_.findViewById(R.id.voice_send);
		voice_receive = (TextView) view_.findViewById(R.id.voice_receive);
		video_send = (TextView) view_.findViewById(R.id.video_send);
		video_receive = (TextView) view_.findViewById(R.id.video_receive);
		total_rate = (TextView) view_.findViewById(R.id.total_rate);
		video_lost_count = (TextView) view_.findViewById(R.id.video_lost_count);
		total_flow = (TextView) view_.findViewById(R.id.total_flow);
		initView();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		wm.removeView(view);
		if (!isAddView_)
			wm.removeView(view_);
		 isAddView_ = true;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		super.onDestroy();
	}

	private void initView() {
		// TODO Auto-generated method stub

		view = LayoutInflater.from(getApplicationContext()).inflate(
				R.layout.window, null);
		// ��ȡWindowManager
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		wmParams = new WindowManager.LayoutParams();
		// �������ṩ���û���������������Ӧ�ó����Ϸ���������״̬������
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		// �������κΰ����¼�
		wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.x = 250;
		wmParams.y = 50;//GQTӢ�İ� ������ٵ���������������ص�ʱ���������Ϳ��ؾ���Ͻ������׳���BUG������رյ�ʱ�򣬵㵽����������
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = PixelFormat.RGBA_8888;

		wmParams_ = new WindowManager.LayoutParams();
		// �������ṩ���û���������������Ӧ�ó����Ϸ���������״̬������
		wmParams_.type = WindowManager.LayoutParams.TYPE_PHONE;
		// �������κΰ����¼�
		wmParams_.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams_.x = -100;
		wmParams_.y = 0;
		wmParams_.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams_.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams_.format = PixelFormat.RGBA_8888;

		wm.addView(view, wmParams);
		// modify by liangzhang 2014-08-22 �޸��ƶ�GQT LOGOʱ��������������������
		// �޸ĺ�Ч��Ϊ�����LOGO�������������ƶ�ʱ����������ʾ
		view.setOnTouchListener(new OnTouchListener() {
			int lastX, lastY,dx,dy;
			int paramX, paramY;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					System.out.println("--------------" + "ACTION_DOWN");
					isMoved = false;
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					dx = 0;
					dy = 0;//add by wlei 2014-9-30 �����Ϊu9508���ƶ�����ͼ��֮���޷���/�ر������������ڵ�����
					paramX = wmParams.x;
					paramY = wmParams.y;
					break;
				case MotionEvent.ACTION_MOVE:
					System.out.println("--------------" + "ACTION_MOVE");
//					isMoved = true;
					dx = (int) event.getRawX() - lastX;
					dy = (int) event.getRawY() - lastY;
					wmParams.x = paramX + dx;
					wmParams.y = paramY + dy;
					// ����������λ��
					wm.updateViewLayout(v, wmParams);

					break;
				case MotionEvent.ACTION_UP:
					System.out.println("--------------" + "ACTION_UP");
					if(Math.abs(dx)>=10||Math.abs(dy)>=10){//GQT Ӣ�İ� 2014-9-10 �Ӹ��ж������������Щ�ֻ�̫���������޷���������¼�
						isMoved = true;
					}
					// delete by liangzhang 2014-08-22
					break;
				}
				return isMoved;
			}
		});
		
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("--------------" + "onClick");
				if (isAddView_) {
					isAddView_ = false;
					try {
						wm.addView(view_, wmParams_);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e("Coolpad or HUAWEI error", e.toString());
						e.printStackTrace();
					}
					if (timer == null) {
						timer = new Timer();
						TimerTask tTask = new TimerTask() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Total_Data = FlowStatistics.Sip_Send_Data
										+ FlowStatistics.Sip_Receive_Data
										+ FlowStatistics.Gps_Receive_Data
										+ FlowStatistics.Gps_Send_Data
										+ FlowStatistics.Voice_Receive_Data
										+ FlowStatistics.Voice_Send_Data
										+ FlowStatistics.Video_Receive_Data
										+ FlowStatistics.Video_Send_Data;

								// 1 sip�������ݵ��ٶȼ���
								if (FlowStatistics.Sip_Send_Data
										- Sip_Send_Data > 0) {

									FlowStatistics.Sip_Send = calculate(FlowStatistics.Sip_Send_Data
											- Sip_Send_Data)
											+ "";
									Sip_Send_Data = FlowStatistics.Sip_Send_Data;
								} else {
									FlowStatistics.Sip_Send = "0.00";
									Sip_Send_Data = FlowStatistics.Sip_Send_Data;
								}
								// 2 sip�������ݵ��ٶȼ���
								if (FlowStatistics.Sip_Receive_Data
										- Sip_Receive_Data > 0) {

									FlowStatistics.Sip_Receive = calculate(FlowStatistics.Sip_Receive_Data
											- Sip_Receive_Data)
											+ "";
									Sip_Receive_Data = FlowStatistics.Sip_Receive_Data;
								} else {
									FlowStatistics.Sip_Receive = "0.00";
									Sip_Receive_Data = FlowStatistics.Sip_Receive_Data;
								}
								// 3 gps�������ݵ��ٶȼ���
								if (FlowStatistics.Gps_Receive_Data
										- Gps_Receive_Data > 0) {

									FlowStatistics.Gps_Receive = calculate(FlowStatistics.Gps_Receive_Data
											- Gps_Receive_Data)
											+ "";
									Gps_Receive_Data = FlowStatistics.Gps_Receive_Data;
								} else {
									FlowStatistics.Gps_Receive = "0.00";
									Gps_Receive_Data = FlowStatistics.Gps_Receive_Data;
								}
								// 4 gps�������ݵ��ٶȼ���
								if (FlowStatistics.Gps_Send_Data
										- Gps_Send_Data > 0) {

									FlowStatistics.Gps_Send = calculate(FlowStatistics.Gps_Send_Data
											- Gps_Send_Data)
											+ "";
									Gps_Send_Data = FlowStatistics.Gps_Send_Data;
								} else {
									FlowStatistics.Gps_Send = "0.00";
									Gps_Send_Data = FlowStatistics.Gps_Send_Data;
								}
								// 5 �����������ݵ��ٶȼ���
								if (FlowStatistics.Voice_Receive_Data
										- Voice_Receive_Data > 0) {

									FlowStatistics.Voice_Receive = calculate(FlowStatistics.Voice_Receive_Data
											- Voice_Receive_Data)
											+ "";
									Voice_Receive_Data = FlowStatistics.Voice_Receive_Data;
								} else {
									FlowStatistics.Voice_Receive = "0.00";
									Voice_Receive_Data = FlowStatistics.Voice_Receive_Data;
								}
								// 6 �����������ݵ��ٶȼ���
								if (FlowStatistics.Voice_Send_Data
										- Voice_Send_Data > 0) {

									FlowStatistics.Voice_Send = calculate(FlowStatistics.Voice_Send_Data
											- Voice_Send_Data)
											+ "";
									Voice_Send_Data = FlowStatistics.Voice_Send_Data;
								} else {
									FlowStatistics.Voice_Send = "0.00";
									Voice_Send_Data = FlowStatistics.Voice_Send_Data;
								}
								// 7��Ƶ�������ݵ��ٶȼ���
								if (FlowStatistics.Video_Send_Data
										- Video_Send_Data > 0) {
									FlowStatistics.Video_Send = calculate(FlowStatistics.Video_Send_Data
											- Video_Send_Data)
											+ "";
									Video_Send_Data = FlowStatistics.Video_Send_Data;
								} else {
									FlowStatistics.Video_Send = "0.00";
									Video_Send_Data = FlowStatistics.Video_Send_Data;
								}

								// 8��Ƶ�������ݵ��ٶȼ���
								if (FlowStatistics.Video_Receive_Data
										- Video_Receive_Data > 0) {
									FlowStatistics.Video_Receive = calculate(FlowStatistics.Video_Receive_Data
											- Video_Receive_Data)
											+ "";
									Video_Receive_Data = FlowStatistics.Video_Receive_Data;
								} else {
									FlowStatistics.Video_Receive = "0.00";
									Video_Receive_Data = FlowStatistics.Video_Receive_Data;
								}

								// �����ܵ����������ٶ�
								if (Total_Data - Total_Data_old > 0) {
									FlowStatistics.Total = calculate(Total_Data
											- Total_Data_old)
											+ "";
									Total_Data_old = Total_Data;
								} else {
									FlowStatistics.Total = "0.00";
									Total_Data_old = Total_Data;
								}
								// ����������
								FlowStatistics.Total_Flow = calculateTotal(Total_Data);

								Message ms = new Message();
								ms.what = 1;
								mHandle.sendMessage(ms);

							}
						};
						timer.schedule(tTask, 0, 3000);
					}
				} else {
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					if (!isAddView_)
						try {
							wm.removeView(view_);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Log.e("Coolpad or HUAWEI error", e.toString());
							e.printStackTrace();
						}
					isAddView_ = true;
				}
			}
		});

		view_.setOnTouchListener(new OnTouchListener() {

			int lastX, lastY;
			int paramX, paramY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = wmParams_.x;
					paramY = wmParams_.y;
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					wmParams_.x = paramX + dx;
					wmParams_.y = paramY + dy;
					// ����������λ��
					wm.updateViewLayout(v, wmParams_);
					break;
				case MotionEvent.ACTION_UP:
					// if (lastX == (int) event.getRawX()
					// && lastY == (int) event.getRawY()) {
					// }
					break;
				}
				return true;
			}

		});
	}

	public double calculate(int data) {
		// TODO Auto- method stub
		// TODO Auto-generated method stub
		// if(data!=0){
		Double db = (double) data;
		double x = db / 1024 / 3;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
		// }

	}

	public double calculateTotal(int data) {
		Double db = (double) data;
		double x = db / 1024;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}

}
