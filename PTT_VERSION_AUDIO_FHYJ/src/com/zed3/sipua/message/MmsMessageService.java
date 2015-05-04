/**
 * houyuchun create 20120507 
 * MmsMessageService: 用来负责初始化socket,启动发送或接受彩信线程
 */

package com.zed3.sipua.message;

import org.zoolu.tools.MyLog;

import android.text.TextUtils;

import com.zed3.sipua.ui.Receiver;

public class MmsMessageService {

	// 定义TAG
	private static final String TAG = "MmsMessageService";
//	private Socket tcpSocket;
	// connection 服务器IP和端口信息 ，格式：IP/Port
	private String connection = null;
	private int flag = -1;
	// 彩信大小
	private int size = 0;
	// str_header 用来与服务器同步验证 ，包括：offlineDataId + offlinedataCheckId
	private String str_header = null;
	// houyuchun add 20120524 begin 
	private String E_id = null;
	private String check_id = null;
	private String recipient_num = null;
	private String report_attr = null;
	// houyuchun add 20120524 end 	
	
	public MmsMessageService(String conn_info, int flag, int size, String E_id, String check_id, String recipient_num, String report_attr) {
		this.connection = conn_info;
		this.flag = flag;
		this.size = size;
		this.E_id = E_id;
		this.check_id = check_id;
		str_header = E_id + check_id;
		this.recipient_num = recipient_num;
		this.report_attr = report_attr;
	}
	
	// 初始化Socket
	public void initSocket() {	
		String[] conn_info = connection.split("/");
		// 服务器ip
		String ip = conn_info[0];
		// 端口
		int port = Integer.parseInt(conn_info[1]);
		// flag，发送彩信/接收彩信标识  0：发送  1：接收
		if(flag == 0) {
			// 启动发送彩信线程
			SendMessageThread send_thread = new SendMessageThread(ip, port, Receiver.mContext, getSendByte(),TextUtils.isEmpty(E_id) ? MessageSender.getSendDataId() : E_id);
			send_thread.start();
		} else if(flag == 1) {
            // 接收方接收数据前，首先发送44个字节的验证码给服务器
			// houyuchun add 20120524 begin
			ReceiveMessageThread receive_thread = new ReceiveMessageThread(ip, port, Receiver.mContext, size, E_id, check_id, true, recipient_num, report_attr);
			// houyuchun add 20120524 end
			receive_thread.start();
		} else {
			MyLog.e(TAG, "initSocket error, flag = "+ flag);
		}
	}
	
	// getSendByte(): 获得需要发送内容的byte
	private byte[] getSendByte() {
		byte[] header = str_header.getBytes();
		byte[] content = new MessageSender(Receiver.mContext).getMmsTxtByte();
		int length = header.length + content.length;
		byte[] bytes = new byte[length];
		System.arraycopy(header, 0, bytes, 0, header.length);
		System.arraycopy(content, 0, bytes, header.length, content.length);
		return bytes;
	}
}
