/**
 * houyuchun create 20120507 
 * SendMessageThread: the thread for send multi message
 */

package com.zed3.sipua.message;

import java.io.OutputStream;
import java.net.Socket;

import org.zoolu.tools.MyLog;

import android.content.Context;

import com.zed3.utils.Zed3Log;

public class SendMessageThread extends Thread {

	// 定义TAG
	private static final String TAG = "SendMessageThread";
	private Context mContext;
	private Socket tcp_socket;
	// ip 服务器地址
	private String ip;
	// port 端口 
	private int port;
	private byte[] send_byte;
	
	private String dataId = null;
	
	public SendMessageThread(String ip, int port, Context context, byte[] bytes, String e_id) {
		this.ip = ip;
		this.dataId = e_id;
		this.port = port;
		this.mContext = context;
		this.send_byte = bytes;
//		initSendSocket(ip,port);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			initSendSocket(ip,port);
			Zed3Log.debug("mmsTrace", "SendMessageThread#run enter data id = " + dataId);
			// 创建输出流			
			OutputStream outStream = tcp_socket.getOutputStream();
			// 向服务器端写数据
			// 协议规定，前44字节为验证数据,接着是正式数据内容
			outStream.write(send_byte);
			outStream.flush();
			onSendCompleted();
			MyLog.e(TAG, "begin send data by socket");
		} catch(Exception e) {			
			MyLog.e(TAG, "send message thread error:");
			e.printStackTrace();
			onSendError();
		} 
		super.run();
	}		
	
	private void onSendCompleted(){
		Zed3Log.debug("mmsTrace", "SendMessageThread#onSendCompleted enter");
		MessageSender.updateMmsState(dataId,MessageSender.PHOTO_UPLOAD_STATE_FINISHED);
	}
	
	private void onSendError() {
		Zed3Log.debug("mmsTrace", "SendMessageThread#onSendError enter");
		MessageSender.updateMmsState(dataId,MessageSender.PHOTO_UPLOAD_STATE_FAILED);
	}
	
	// 初始化socket
	private void initSendSocket(final String ip, final int port) {
		// 初始化socket
		try {
			tcp_socket = new Socket(ip, port);
			MyLog.e(TAG, "initSendSocket ");
		} catch(Exception e) {
			MyLog.e(TAG, "initSendSocket error:");
			e.printStackTrace();
		}
	}
}
