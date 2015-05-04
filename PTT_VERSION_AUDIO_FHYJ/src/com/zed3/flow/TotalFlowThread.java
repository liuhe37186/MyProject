package com.zed3.flow;

import org.zoolu.tools.MyLog;

import com.zed3.location.MemoryMg;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

public class TotalFlowThread extends Thread {
	private final String TAG = "TotalFlowThread";
	//private final String sharedPrefsFile = "com.zed3.sipua_preferences";
	private int TagNum = 5;

	private Context context;
	boolean Flag = false;
	private int Total_Data = 0;
	private int Total_DataPTT = 0;
	private int Total_DataVideo = 0;
	boolean is3GFlag = false;
	// 3G
	private double Total_3GData = 0;
	private double Total_3GDataPTT = 0;
	private double Total_3GDataVideo = 0;
	
	// 3G 200
	private double Upload_3GData = 0;
	private double subData = 0;
	
	// 3G
	private double Total_3GData_old = 0;
	private double Total_3GDataPTT_old = 0;
	private double Total_3GDataVideo_old = 0;
	
	int count = 0;
	UserAgent ua = null;
	boolean wifiFlag = false,alarmFlag=false;
	double a=0;
	
	public TotalFlowThread(Context context) {
		MyLog.e(TAG, "TotalFlowThread Start");
		
		this.context = context;
		Flag = true;
		alarmFlag=false;
		count=0;
		ua = Receiver.GetCurUA();
	}

	public void StopFlow() {
		Flag = false;
		String str = "StopFlow()";
		MyLog.e(TAG, str);
		
		mHandle.sendMessage(mHandle.obtainMessage(1, 0, 0));
	}
	
	Handler mHandle = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				if (msg.arg1 == 1) {
					AddNotify(1, "您的流量使用已经接近套餐限值，超过套餐限值将消耗国内流量");
					alarmFlag = true;
					// 还要发广播通知 视频 和 对讲页面 弹流量警告Dialog
					context.sendBroadcast(new Intent(ua.ACTION_3GFlow_ALARM));
						
				} else
					AddNotify(0, "");
				
			}
		}
	};
	
	private void AddNotify(int flag, String msg) {
		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (flag == 0)
		{
			manager.cancel(101);
			return;
		}
		// 创建一个Notification
		Notification notification = new Notification();
		// 设置显示在手机最上边的状态栏的图标
		notification.icon = R.drawable.icon22;
		// 添加声音提示
		notification.defaults = Notification.DEFAULT_SOUND;
		// audioStreamType的值必须AudioManager中的值，代表着响铃的模式
		notification.audioStreamType = android.media.AudioManager.ADJUST_LOWER;
		Intent intent = new Intent(context, TotalFlowView.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_ONE_SHOT);
		// 点击状态栏的图标出现的提示信息设置
		notification.setLatestEventInfo(context, "流量提示", msg, pendingIntent);
		manager.notify(101, notification);
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while (Flag) {
			try {
				// 程序处于运行中，月份切换，数据统计流量清零，并同步到服务器
				ResetData();
				
				Total_DataPTT = FlowStatistics.Voice_Receive_Data
						+ FlowStatistics.Voice_Send_Data;
				
				Total_DataVideo = FlowStatistics.Video_Receive_Data
						+ FlowStatistics.Video_Send_Data;
				
				Total_Data = FlowStatistics.Sip_Send_Data
						+ FlowStatistics.Sip_Receive_Data
						+ FlowStatistics.Gps_Receive_Data
						+ FlowStatistics.Gps_Send_Data
						+ FlowStatistics.DownLoad_APK
						+ Total_DataPTT
						+ Total_DataVideo;
				MyLog.e(TAG, FlowStatistics.DownLoad_APK+" UpdateVersionService");
				// // 计算总流量转换成kb
				// 计算3G总流量
				if (CheckNetWork()) 
				{
					wifiFlag = false;
					
					if (Total_3GData_old == 0) {
						// 根据文档获取结果值 如果切换网络的时候走注册和停止这里就没问题
						Total_3GData = MemoryMg.getInstance().User_3GRelTotal;
						Total_3GDataPTT = MemoryMg.getInstance().User_3GRelTotalPTT;
						Total_3GDataVideo = MemoryMg.getInstance().User_3GRelTotalVideo;

						Total_3GData_old = Total_Data;
						Total_3GDataPTT_old = Total_DataPTT;
						Total_3GDataVideo_old = Total_DataVideo;
						
					} else {
						subData = Total_Data - Total_3GData_old;
						Upload_3GData += subData;
						
						Total_3GData += subData;
						Total_3GDataPTT += (Total_DataPTT - Total_3GDataPTT_old);
						Total_3GDataVideo += (Total_DataVideo - Total_3GDataVideo_old);
								
						Total_3GData_old = Total_Data;
						Total_3GDataPTT_old = Total_DataPTT;
						Total_3GDataVideo_old = Total_DataVideo;
						
					}

					if (alarmFlag == false
							&& MemoryMg.getInstance().User_3GFlowOut > 0) {
						if (/*
							 * calculatePercent(
							 * MemoryMg.getInstance().User_3GRelTotal,
							 * MemoryMg.getInstance().User_3GFlowOut * 1024 *
							 * 1024) >= 1
							 */
						MemoryMg.getInstance().User_3GRelTotal >= MemoryMg
								.getInstance().User_3GFlowOut * 1024 * 1024) {

							mHandle.sendMessage(mHandle
									.obtainMessage(1, 1, 100));
						}
					}
					
					// 计数器累加了60秒的次数则写入本地存储
					if (count >= 60 / TagNum) {
						MyLog.e(TAG, Total_3GData + Total_3GDataPTT + Total_3GDataVideo + "save as db");
						count = 0;
						// 写入本地文件
						ua.NetFlowPreferenceEdit(Total_3GData + "",Total_3GDataPTT + "",Total_3GDataVideo + "",
								ua.GetCurrentMouth(false));
					}
					// 大于200k
					if (calculateTotal(Upload_3GData) > 200) {
						Upload_3GData = 0;
						MyLog.e(TAG, Total_3GData + Total_3GDataPTT + Total_3GDataVideo + "save as network");
						// 以下操作放到线程里去完成
						// 和上一次比较大于200k的时候需要发送到服务器
						ua.Upload3GTotal(Total_3GData + "",Total_3GDataPTT + "",Total_3GDataVideo + "");
					}

					count++;
					// 每次都需要赋值
					MemoryMg.getInstance().User_3GRelTotal = Total_3GData;
					MemoryMg.getInstance().User_3GRelTotalPTT = Total_3GDataPTT;
					MemoryMg.getInstance().User_3GRelTotalVideo = Total_3GDataVideo;
					
					
				} 
				else// wifi
				{
					if (wifiFlag == false) {
						wifiFlag = true;
						// 切换网络的时候也将流量保存到本地文本？
						// 写入本地文件 次数挺多 啊 ，wifi的时候，需要做个flag吧,只写一次。
						ua.NetFlowPreferenceEdit(
								MemoryMg.getInstance().User_3GRelTotal + "",
								MemoryMg.getInstance().User_3GRelTotalPTT + "",
								MemoryMg.getInstance().User_3GRelTotalVideo + "",
								ua.GetCurrentMouth(false));
					}
					Total_3GData_old = 0;
					Total_3GDataPTT_old = 0;
					Total_3GDataVideo_old = 0;
					
					Total_3GData = 0;
					Total_3GDataPTT = 0;
					Total_3GDataVideo = 0;
					
					Upload_3GData = 0;//
					count = 0;
				}

				Thread.sleep(TagNum * 1000);

			} catch (Exception e) {
				MyLog.e(TAG, e.toString());
				e.printStackTrace();
			}
		}
		
	}
	
	public double calculatePercent(double a, double b) {
		double x = a / b;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}
	
	private void ResetData() {
		// 程序处于运行中，月份切换，数据统计流量清零，并同步到服务器
		if (!ua.GetCurrentMouth(true).equals(
				MemoryMg.getInstance().User_3GDBLocalTime.substring(0, 7))) {
			MyLog.e(TAG, "ResetData");

			MemoryMg.getInstance().User_3GDBLocalTime = ua.GetCurrentMouth(false);
			
			Total_3GData_old = 0;
			Total_3GDataPTT_old = 0;
			Total_3GDataVideo_old = 0;
			
			Total_3GData = 0;
			Total_3GDataPTT = 0;
			Total_3GDataVideo = 0;
			
			Upload_3GData = 0;
			count = 0;

			MemoryMg.getInstance().User_3GRelTotal = 0;
			MemoryMg.getInstance().User_3GRelTotalPTT = 0;
			MemoryMg.getInstance().User_3GRelTotalVideo = 0;

			FlowStatistics.Sip_Send_Data = 0;
			FlowStatistics.Sip_Receive_Data = 0;
			FlowStatistics.Gps_Receive_Data = 0;
			FlowStatistics.Gps_Send_Data = 0;
			FlowStatistics.Voice_Receive_Data = 0;
			FlowStatistics.Voice_Send_Data = 0;
			FlowStatistics.Video_Receive_Data = 0;
			FlowStatistics.Video_Send_Data = 0;
			FlowStatistics.DownLoad_APK=0;
			
			// 写入本地文件
			ua.NetFlowPreferenceEdit("0","0","0", ua.GetCurrentMouth(false));
			// 写入服务器
			ua.Upload3GTotal("0","0","0");

		}
	}

	private boolean CheckNetWork() {

		// 先判断一次手机网络是否可用
		if (NetChecker.isNetworkAvailable(context)) {
			// 区分是wifi还是3G
			if (NetChecker.isWifi(context)) {
				is3GFlag = false;
				MyLog.e(TAG, "mobile NetWork is run but wifi");
			} else if (NetChecker.is3G(context)) {

				is3GFlag = true;
				MyLog.e(TAG, "mobile NetWork is run and is 3G");
			} else {
				is3GFlag = false;
				MyLog.e(TAG, "mobile NetWork is run but availbale");
			}

		} else {
			is3GFlag = false;
			MyLog.e(TAG, "mobile NetWork is not run");
		}
		return is3GFlag;
	}

	public double calculateTotal(int data) {
		Double db = (double) data;
		double x = db / 1024d;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}

	public double calculateTotal(double db) {
		double x = db / 1024d;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}
	

}
