package com.zed3.bluetooth;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zed3.bluetooth.SppMessageStorage.SppMessage;

public class SppMessageSender extends Thread {

	private SppMessageStorage mStorage;
	private boolean isRunning = true;
	private String tag = "SppMessageSender";
	private Handler reSendHandler;
	private SppMessage lastSendMessage;
	private long lastSendMessageTime;

	public SppMessageSender(SppMessageStorage sppMessageStorage) {
		// TODO Auto-generated constructor stub
		this.mStorage = sppMessageStorage;
	}

	public void startSending() {
		// TODO Auto-generated method stub
		isRunning = true;
	}
	public void stopSending() {
		// TODO Auto-generated method stub
		isRunning = false;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Define handler
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Looper.prepare();
				// TODO Auto-generated method stub
				reSendHandler = new Handler() {
					// @Override
					public void handleMessage(android.os.Message msg) {
						if (!isRunning)
							return;
						try {
							sleep(SppMessage.RESEND_SYCLE);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String msgStr = lastSendMessage.getMessage();
						int sendCount = lastSendMessage.getSendCount();
						if (!lastSendMessage.isAvailable()) {
							Log.i(tag, "handleMessage() message.isAvailable() is false   return");
							return;
						}else if( lastSendMessage.getTime() != lastSendMessageTime) {
							Log.i(tag, "handleMessage() lastSendMessage.getTime() != lastSendMessageTime   return");
							return;
						}else if (sendCount==SppMessage.MAX_SEND_COUNT) {
							Log.i(tag, "handleMessage() sendCount==SppMessage.MAX_SEND_COUNT  return ");
							return;
						}
						
						if (!lastSendMessage.needResend()) {
							Log.i(tag, "handleMessage() lastSendMessage.needResend() is false  return ");
							return;
						}
						ZMBluetoothManager.getInstance().send(msgStr);
						sendCount += 1;
						lastSendMessage.setSendCount(sendCount);
						Message handlerMessage = reSendHandler.obtainMessage();
						reSendHandler.sendMessageDelayed(handlerMessage, SppMessage.RESEND_SYCLE);
						Log.i(tag, "handleMessage() lastSendMessage.needResend() is true   reSendHandler.sendMessageDelayed(handlerMessage, "+SppMessage.RESEND_SYCLE+");");
						
					}
				};
				Looper.loop();	
			}
		}).start();
		
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender   start sending");
		Log.i(tag, "SppMessageSender   start sending");
		while (isRunning) {
			SppMessage message = mStorage.get();
			if (message != null) {
				String msg = message.getMessage();
				if (!message.isAvailable()) {
					Log.i(tag, "while (isRunning) , message.isAvailable() is false   continue");
					continue;
				}
				ZMBluetoothManager.getInstance().send(msg);
				try {
					Thread.sleep(/*20*//*10*//*30*/50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					stopSending();
					ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender  InterruptedException  stop sending");
					Log.i(tag, "SppMessageSender  InterruptedException  stop sending");
					e.printStackTrace();
				}
				if (!message.needResend()) {
					Log.i(tag, "while (isRunning) , lastSendMessage.needResend() is false  continue ");
					continue;
				}else {
					lastSendMessage = message;
					lastSendMessageTime = lastSendMessage.getTime();
					lastSendMessage.setSendCount(1);
					Message handlerMessage = reSendHandler.obtainMessage();
					reSendHandler.sendMessageDelayed(handlerMessage, SppMessage.RESEND_SYCLE);
					Log.i(tag, "while (isRunning) , message.needResend() is true   reSendHandler.sendMessageDelayed(handlerMessage, "+SppMessage.RESEND_SYCLE+");");
				}
			}else {
				ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender  mStorage.get() return null");
				Log.i(tag, "SppMessageSender  mStorage.get() return null");
			}
		}
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender   stop sending");
		Log.i(tag, "SppMessageSender   stop sending");
		
		if (reSendHandler != null && reSendHandler.getLooper()!= null) {
			reSendHandler.getLooper().quit();
		}
	}

}
