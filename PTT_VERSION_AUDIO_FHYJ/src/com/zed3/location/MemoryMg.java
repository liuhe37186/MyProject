package com.zed3.location;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MemoryMg {

	private static MemoryMg instance;
	//ptime for ptt
	public static int PTIME = 20;
	//Э�̺��ptimeֵ
	public static int SdpPtime=0;
	
	// socket
	private DatagramSocket socket;
	// �ն˺��� ���û��� �����ļ���ȡ
	public String TerminalNum = "";
	// ���� md5����
	public String Password = "";
	// �Ƿ��¼�ɹ� ����������ֵ���������� 0��ok ����ʧ��
	//public String RegisterOk = "";
	//
	public String IsLock = "";
	// �����ϴ���cycle
	public int cycle = 0;
	// ��ַ
	public String IPAddress = "";
	// �˿ں�
	public int IPPort = /*0*/5070;
    //���ݺ���
	public String CallNum="";
	//������Ϣ��¼seq���
	public String LastSeq = "";
	//sharedpreference�ļ����¼�
	public boolean IsChangeListener=true;
	
	//�����ǰٶȶ�λ����GPS��λ
	public boolean isGpsLocation = false;
	
	public boolean GpsLockState=false;
	// gps��λģʽ
	public int GpsLocationModel = -1;
	// gps��λ���ģʽ
	public int GpsSetTimeModel = 1;
	// gps�ϱ����ģʽ
	public int GpsUploadTimeModel = 1;
	
	// gps����ʧ����ʾ
	public boolean GPSSatelliteFailureTip = false;
	//gvsת��ֱ�������
	public String GvsTransSize="";
	//����ͷ��֧�ֵ���Ƶ�ֱ���
	public String SupportVideoSizeStr="";
	//��Ƶת���Ƿ�Я��isSendOnly
	public boolean isSendOnly = false;
	//sdp h264s
	public boolean isSdpH264s = false;
	//������⿪��
	public boolean isAudioVAD = false;
	//MIC���ѿ���
	public boolean isMicWakeUp = false;
	// �ƶ��绰����  0:�ƶ��绰 1:voip�绰
	public int PhoneType = 1;

	//--------------------------------
	// �û������ײ�������
	public double User_3GTotal = 0;
	public double User_3GTotalPTT = 0;
	public double User_3GTotalVideo = 0;
	
	
	// �û���������������
	public double User_3GLocalTotal = 0;
	public double User_3GLocalTotalPTT = 0;
	public double User_3GLocalTotalVideo = 0;
	// �û������ϴ�ʱ���
	public String User_3GLocalTime = "";

	
	// �û��������ݿ�����������
	public double User_3GDBLocalTotal = 0;
	public double User_3GDBLocalTotalPTT = 0;
	public double User_3GDBLocalTotalVideo = 0;
	
	// �û��������ݿ��ϴ�ʱ���
	public String User_3GDBLocalTime = "";	
	
	// ����õ�����ʵ����
	public double User_3GRelTotal=0;
	public double User_3GRelTotalPTT=0;
	public double User_3GRelTotalVideo=0;
	
	// ����Ԥ��ֵ
	public double User_3GFlowOut = 0;
	// ����������
	public boolean isProgressBarTip = false;
	//�洢GPS��Ϣ���ݱ���
	public static final String TABLE_NAME = "gps_info";
	public static final String TABLE_NAME_COPY = "gps_info_copy";
	//--------------------------------
	public static MemoryMg getInstance() {
		if (instance == null)
			instance = new MemoryMg();
		return instance;
	}
    //
	public DatagramSocket getSocket() {
		if (socket == null || socket.isClosed()) {
			try {
				socket = new DatagramSocket();
				//socket = new DatagramSocket(GpsTools.Port);

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return socket;
	}

}
