package com.zed3.sipua.welcome;

public class DeviceInfo {
	public static String IMEI= "";
	public static String IMSI= "";
	public static String SIMID = "";
	public static String TELNUM ="";
	public static String MACADDRESS = "";
	public static String UDID = "";
	public static String PHONENUM ="";
	public static String SIMNUM = "";
	public static String AutoVNoName = "";
	public static boolean isSameSimCard = false;
	public static boolean isSameHandset = false;
	
	public static boolean isEmergency;
	
	public static String svpnumber = "";
	public static String defaultrecnum = "";
	public static String http_port = "";
	public static String https_port = "";
	
	public static String CONFIG_UPDATE_URL = "";//���·�������ַ
	public static String CONFIG_CONFIG_URL = "";//���÷�������ַ
	public static boolean CONFIG_SUPPORT_AUTOLOGIN = true ;//�Զ���¼
	public static boolean CONFIG_SUPPORT_UNICOM_PASSWORD = false;//��ͨ����
	public static boolean CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS = false;//��ͨ����
	public static boolean CONFIG_SUPPORT_VIDEO = true;//��Ƶͨ��
	public static boolean CONFIG_SUPPORT_AUDIO = true;//����ͨ��
	public static int CONFIG_AUDIO_MODE =1;//0:�����ƶ��绰  1:VOIP�绰
	public static boolean CONFIG_SUPPORT_AUTORUN = false;//��������
	public static boolean CONFIG_CHECK_UPGRADE = true; //���������
	public static boolean CONFIG_SUPPORT_ENCRYPT = false;//�������
	public static boolean CONFIG_SUPPORT_PTTMAP = false;//��ͼģʽ
	public static int CONFIG_GPS = 4;// 0:���߱��ϱ����� 1:ǿ��GPS��λ�ϱ� 2:ǿ�ưٶ����ܶ�λ
										// 3��ǿ�ưٶ�GPS��λ 4��Ĭ�ϴӲ���λ
	public static boolean CONFIG_SUPPORT_AUDIO_CONFERENCE = false; //��������
	public static boolean CONFIG_SUPPORT_PICTURE_UPLOAD = true;//ͼƬ�Ĵ�
	public static boolean CONFIG_SUPPORT_IM = true;//����Ϣ
	public static boolean CONFIG_SUPPORT_EMERGENYCALL = true;//һ���澯
	public static boolean CONFIG_SUPPORT_HOMEKEY_BLOCK = false;//home����
	public static boolean CONFIG_SUPPORT_VAD = false;//������� 
	public static boolean CONFIG_SUPPORT_LOG = false;//LOG֧��
	public static boolean CONFIG_SUPPORT_RATE_MONITOR = true;//����������
	public static boolean CONFIG_SUPPORT_REGISTER_INTERNAL = true;//ע����
	public static boolean CONFIG_SUPPORT_AEC = true;//��������
	public static boolean CONFIG_SUPPORT_NS = true;//����
	public static boolean CONFIG_SUPPORT_BLUETOOTH = false;//����֧��
	public static boolean ISAlarmShowing;
	
	//remote   get value from server:
	public static int GPS_REMOTE = -1;
	public static boolean ENCRYPT_REMOTE = false;
	public static boolean AUTORUN_REMOTE = false;
	//phone state
		public static boolean isSupportHWChange = false;
}
