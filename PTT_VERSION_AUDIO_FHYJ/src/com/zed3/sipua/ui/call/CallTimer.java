package com.zed3.sipua.ui.call;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class CallTimer {

	private TextView mTextView;
	private MyTimerTask task;
	private Timer timer;
	private Handler handler;

	public CallTimer(Handler handler) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		timer = new Timer();
		task = new MyTimerTask(handler,0,0,0);
	}

	public void start() {
		// TODO Auto-generated method stub
		timer.scheduleAtFixedRate(task, 0, 1000);
	}
	public void reStart() {
		// TODO Auto-generated method stub
		task.reSet(0,0,0);
	}
	public void stop() {
		// TODO Auto-generated method stub
		timer.cancel();
	}
	public void reSet(){
		task.reSet(0, 0, 0);
	}
	public void reSet(int hh, int mm, int ss){
		task.reSet(hh, mm, ss);
	}
	
	class MyTimerTask extends TimerTask{
		int HH = 0;
		int MM = 0;
		int SS = 0;
		String HHSTR ;
		String MMSTR ;
		String SSSTR ;
		String TIME = "00:00:00";
		String ZERO = "0";
		String COLON = ":";
		private TextView mTextView;
		private Handler handler;
		
		public MyTimerTask(Handler handler) {
			// TODO Auto-generated constructor stub
			this.handler = handler;
		}
		
		public MyTimerTask(Handler handler, int hh, int mm, int ss) {
			// TODO Auto-generated constructor stub
			this.handler = handler;
			HH = hh;
			MM = mm;
			SS = ss;
		}
		
		private void reSet(int hh, int mm, int ss) {
			// TODO Auto-generated method stub
			HH = hh;
			MM = mm;
			SS = ss;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//秒 SS
			if (SS>59) { //00:59
				//分 MM
				if (SS>3559) {//00:59:59
					
				}else {
					//
				}
				//时 HH
				
			}else {
				//00:33
				TIME = ZERO+ZERO+COLON+ZERO+SS;
						
			}
			
			
			//分
			if (SS == 60) {
				SS = 0;
				MM++;
			}
			if (SS == 0) {
				SSSTR = ZERO+ZERO;;
			}else if (SS > 0 && SS < 9) {
				SSSTR = ZERO+SS;
			}else if (SS > 9 && SS < 60 ) {
				SSSTR = ""+SS;
			}
			
			//分
			if (MM == 60) {
				MM = 0;
				HH++;
			}
			if (MM == 0) {
				MMSTR = ZERO+ZERO;;
			}else if (MM > 0 && MM < 9) {
				MMSTR = ZERO+MM;
			}else if (MM > 9 && MM < 60 ) {
				MMSTR = ""+MM;
			}
			
			//时
			if (HH == 60) {
				HH = 0;
			}
			if (HH == 0) {
				HHSTR = "";
			}else if (HH > 0 && HH < 9) {
				HHSTR = ZERO+HH;
			}else if (HH > 9 && HH < 60 ) {
				HHSTR = ""+HH;
			}
			//生成字符串
			if (HH == 0) {
				TIME = MMSTR + COLON +SSSTR;
			}else {
				TIME = HHSTR + COLON + MMSTR + COLON +SSSTR;
			}
			//加一秒
			
//			mTextView.setText(TIME);
			Message msg = handler.obtainMessage();
			msg.obj = TIME;
			handler.sendMessage(msg);
			
			SS++;
		}
		
	}
	

}
