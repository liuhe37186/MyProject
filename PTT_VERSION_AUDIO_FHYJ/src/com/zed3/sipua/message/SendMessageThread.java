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

	// ����TAG
	private static final String TAG = "SendMessageThread";
	private Context mContext;
	private Socket tcp_socket;
	// ip ��������ַ
	private String ip;
	// port �˿� 
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
			// ���������			
			OutputStream outStream = tcp_socket.getOutputStream();
			// ���������д����
			// Э��涨��ǰ44�ֽ�Ϊ��֤����,��������ʽ��������
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
	
	// ��ʼ��socket
	private void initSendSocket(final String ip, final int port) {
		// ��ʼ��socket
		try {
			tcp_socket = new Socket(ip, port);
			MyLog.e(TAG, "initSendSocket ");
		} catch(Exception e) {
			MyLog.e(TAG, "initSendSocket error:");
			e.printStackTrace();
		}
	}
}
