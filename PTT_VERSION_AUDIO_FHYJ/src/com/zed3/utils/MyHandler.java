package com.zed3.utils;

import android.os.Handler;

import com.zed3.media.RtpStreamSender_group;



public class MyHandler{
	
	private static Handler myHandler;
	public static void sendMessage(int volume){
		if(RtpStreamSender_group.mPTTPause) return;
		if(myHandler != null){
			myHandler.sendMessage(myHandler.obtainMessage(1,volume,0));
		}
	}
	public static void sendReceiveMessage(int volume){
		if(!RtpStreamSender_group.mPTTPause) return;
		if(myHandler != null){
			myHandler.sendMessage(myHandler.obtainMessage(2,volume,0));
		}
	}
	public static void setHandler(Handler hd){
		myHandler = hd;
	}
}
