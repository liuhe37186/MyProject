package com.zed3.flow;

public class FlowStatistics {
	//����Ϊ������SIP���ֵ��ٶ�
	public  static  String Voice_Receive  ;

	public static  String Voice_Send  ;
	
	public static String Sip_Receive  ;
	
	public static String Sip_Send ;
	
	public static String Gps_Send;
	
	public static String Gps_Receive;
	
	public static String Video_Send;
	public static String Video_Receive;
	public static String Total;
	//�����Ĳ���
	public static int Sip_Send_Data=0;
	
	public static int Sip_Receive_Data=0;
	
	public static int Gps_Send_Data=0;
	
	public static int Gps_Receive_Data=0;
	
	public static int Voice_Send_Data=0;
	
	public static int Voice_Receive_Data=0;
	
	public static int Video_Send_Data=0;
	//������
	public static int Video_Receive_Data=0;
	
	public static int Video_Packet_Lost=0;
	
	public static double Total_Flow=0;
	
	public static int DownLoad_APK=0;
}
