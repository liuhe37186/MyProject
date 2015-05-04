/**
 * houyuchun create 20120507 
 * ReceiveMessageThread: the thread for receive message
 */

package com.zed3.sipua.message;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.zoolu.tools.MyLog;

import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.zed3.media.TipSoundPlayer;
import com.zed3.media.TipSoundPlayer.Sound;
import com.zed3.sipua.message.PhotoTransferReceiveActivity.PhotoReceiveMessage;
import com.zed3.sipua.ui.Receiver;

public class ReceiveMessageThread extends Thread {

	// 定义TAG
	private static final String TAG = "ReceiveMessageThread";
	private static final int TCP_RECONNECTION = 0;
	private static final int TCP_CONNECTION_ERROR = 1;
	private Socket receive_socket;
	private Context mContext;
	private boolean conn_state = false;

//	List<byte[]> bytelist;
	private int size;
	private String str_header = null;
	private boolean _isclose = false;
	// 计数器
	private String ip;
	private int port;
	private int count = 0;
	
	private String E_id = null;
	private String check_id = null;
	private String recipient_num = null;
	private String report_attr = null;
	
	public ReceiveMessageThread(String ip, int port, Context context, int size, String E_id, String check_id, boolean isclose, String recipient_num, String report_attr){
		this.ip = ip;
		this.port = port;
		this.mContext = context;
		this.size = size;
	    this.E_id = E_id;
	    this.check_id = check_id;
	    this.recipient_num = recipient_num;
	    this.report_attr = report_attr;
		_isclose = isclose;
		str_header = E_id + check_id; 
		initReceiveSocket(ip, port);
	}
	
	//guojunfeng 
	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		InputStream inputStream = null;
		OutputStream outStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			// 接收方接收数据前，首先发送44个字节的验证码给服务器
			outStream = receive_socket.getOutputStream();
			outStream.write(str_header.getBytes());
			outStream.flush();
			// 获得彩信文件流
			inputStream = receive_socket.getInputStream();
			MyLog.i(TAG, "begin receive message from socket");
			byte[] bytes = readInputStream(inputStream);			
			// parse mms bytes to mms file
			if(bytes.length==size){
				MessageParse parse = new MessageParse(mContext, E_id, recipient_num);
				if(parse.parseMmsInfoFromTxt(bytes)==1)
				{
					
					Message message = parse.saveMmsInfoToInbox();
					if(message!=null){
						String body = "请打开文件";
						int type = 0;
						Object result = message.obj;
						if(result!=null){
							if(result instanceof PhotoReceiveMessage) {
								PhotoReceiveMessage prm = (PhotoReceiveMessage) result;
								body = prm.mBody;
								type = 1;
							}
						}
						// save the mms info to database
						// houyuchun add 20120512 begin 
						// 发送通知有新信息
						Intent intent = new Intent();
						// houyuchun add 20120523 begin 
						intent.putExtra("E_id", E_id);
						intent.putExtra("recipient_num", recipient_num);
						intent.putExtra("contentType", parse.getContentType());
						intent.putExtra("report_attr", report_attr);
						intent.putExtra("body", body);
						intent.putExtra("type", type);
						
						TipSoundPlayer.getInstance().play(Sound.MESSAGE_ACCEPT);
						
						// houyuchun add 20120523 end 
						intent.setAction(PhotoTransferReceiveActivity.ACTION_RECEIVE_MMS);
						Receiver.mContext.sendBroadcast(intent);
						
					}
				}
				
			}
		} catch(Exception e) {
			MyLog.e(TAG, "receive message thread error:");
			e.printStackTrace();
		} finally {
			try {
			
				if(inputStream != null) {
					inputStream.close();
				}
				if(outStream != null) {
					outStream.close();
				}
				if(fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		super.run();
	}	
	
	// 初始化socket
	private void initReceiveSocket(final String ip, final int port) {
		try {
			receive_socket = new Socket(ip, port);
			conn_state = true;
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "initReceiveSocket error:");
			e.printStackTrace();
		}
	}
	
	// houyuchun add 20120524 begin
	private byte[] readInputStream(InputStream inputStream) {
		ByteArrayOutputStream byteArrayOutputStream = null;
		byte[] bytes = new byte[1024];
		int length = -1;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			while((length = inputStream.read(bytes)) != -1) {
				byteArrayOutputStream.write(bytes, 0, length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(byteArrayOutputStream != null) {
					byteArrayOutputStream.close();
				}
                if(inputStream != null) {
    				inputStream.close();
                }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				MyLog.e(TAG, "readInputStream error");
				e.printStackTrace();
				return null;
			}
		}
		return byteArrayOutputStream.toByteArray();
	}
	// houyuchun add 20120524 end
	
	// close the current Thread
	private void closeThread() {
		_isclose = false;
		try {
			receive_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
