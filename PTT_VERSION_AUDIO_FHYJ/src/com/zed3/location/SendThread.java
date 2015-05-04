package com.zed3.location;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import org.zoolu.tools.MyLog;

import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.zed3.flow.FlowStatistics;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.Zed3Log;

/*
 * 发送消息
 */
public class SendThread extends Thread {
	int flow=0;
	private InetAddress m_server;
	private DatagramSocket m_socket;
	private int m_port;
	private byte[] m_content;
	private DatagramPacket packet;
	int count = 0;
	List<GpsInfo> infoList;
	int sendType = 0;
	GpsInfo gpsInfo;

	public SendThread(DatagramSocket socket, InetAddress server, int port) {
		this.m_server = server;
		this.m_port = port;
		this.m_socket = socket;
	}
	public SendThread(DatagramSocket socket, InetAddress server, int port,int sendType,List<GpsInfo> list) {
		this.m_server = server;
		this.m_port = port;
		this.m_socket = socket;
		infoList = list;
		this.sendType =sendType;  
	}

	// 传输内容
	public void SetContent(byte[] content) {
		this.m_content = content;
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		sendGPSInfo();
	}

	//add by oumogang 2013-06-03
	private void sendBroadCast4Send(InetAddress m_server2, int m_port2) {
		// TODO Auto-generated method stub
		if (Settings.needSendLocateBroadcast) {
			Intent intent = new Intent(Contants.ACTION_LOCATE_UPLOAD_SENDED);
			String hostAddress = m_server2.getHostAddress();
			intent.putExtra(Contants.KEY_LOCATE_UPLOAD_SEND_ADDRESS, hostAddress);
			intent.putExtra(Contants.KEY_LOCATE_UPLOAD_SEND_PORT, m_port2+"");
			SipUAApp.mContext.sendBroadcast(intent);
		}
	}

	private void sendGPSInfo(){
		if(infoList != null){
			for(int i = 0;i < infoList.size();i++){
					gpsInfo = infoList.get(i);
					Zed3Log.debug("testgps", "SendThread#sendGPSInfo sendGPSInfo = " + gpsInfo);
					SetContent(GpsTools.GpsByte(MemoryMg.getInstance().TerminalNum,
						sendType, gpsInfo.gps_x,
						gpsInfo.gps_y, gpsInfo.gps_speed,
						gpsInfo.gps_height, gpsInfo.gps_direction,gpsInfo.UnixTime,gpsInfo.E_id));
					sendPackage();
				}
		}else{
			sendPackage();
		}
	}
	
	private void sendPackage(){
		MyLog.e("GPSSend", "transfer content length:" + m_content.length);
		byte[] message = m_content;
		int len = message.length;
//		DatagramPacket packet = new DatagramPacket(message, len, m_server,m_port);
		Zed3Log.debug("testgps", "SendThread#sendPackage gpsPacket = " + m_server + " , port = " + m_port);
		if (packet == null) {
			packet = new DatagramPacket(message, len, m_server,m_port);
		}
		packet.setAddress(m_server);
		packet.setPort(m_port);
		packet.setData(message);
		packet.setLength(len);
		MyLog.e("GPSSend", "transfer server & port:" + m_server+" &" +m_port);
		//sendBroadCast4Send   add by oumogang 2013-06-03
		sendBroadCast4Send(m_server,m_port);
		//post time out runner( Runnable )
		
		try {
			m_socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("xxxx", "SendThread#sendPackage exception");
			e.printStackTrace();
		}
		if(packet.getLength()+42>60){
			flow = packet.getLength();
		}else{
			flow = 60;
		}
		FlowStatistics.Gps_Send_Data = flow+FlowStatistics.Gps_Send_Data;
	}
	

}
