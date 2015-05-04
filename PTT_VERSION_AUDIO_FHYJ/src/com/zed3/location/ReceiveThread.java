package com.zed3.location;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zed3.flow.FlowStatistics;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.Zed3Log;

/*
 * �ȴ�����
 */
public class ReceiveThread extends Thread {

	private final static int EXITSYSTEM = 0;// �˳�ϵͳ
	private final static int CYCLEUP = 2;// �����ϴ�
	private final static int NORMALSTATE = 3;// ���ڷ��Ϳհ�
	private final static int MESSAGE_CHECK_NETWORK = 1001; // �������
	private final static int SAVEVALUE=4;
	private int TIMEOUT = 500;
	int flow = 0;
	private boolean returnFlag = true;
	DatagramSocket socket;
	Context context;
	public static boolean flag = true;
	int cyclex = 0;
	int occurType = 0, occurCircle = -1, delType = -1;// �ٷ��������� �ٷ�����ѭ�� ɾ������
	public static List<GpsInfo> infoList;
	boolean tipFlag = false;
	private long UnixTime = 0L;
	private long RealTime = 0L;
	private long LocalTime = 0L;
	private GpsListener regListener = null;
	int count = 0;
	Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					Zed3Log.debug("testgps","ReceiverThread#handleMessage message what = " + msg.what);
					try {
						switch (msg.what) {

						case CYCLEUP:// ����GPS�ն������ϴ�GPS����
							cyclex = (Integer) msg.obj;

							if (cyclex != 0) {
								// �ϱ�gps  
								if (MemoryMg.getInstance().GpsLockState) {
//									//�Զ�ģʽ---����
//									if (MemoryMg.getInstance().GpsLocationModel == 0) {
//										// ---�Զ��л��ϱ�ģʽ
//										if (MemoryMg.getInstance().isGpsLocation)
//											info = GpsManage.getInstance(context)
//													.GetValueGpsStr();
//										else
//											info = SipUAApp.gpsInfo;
//									}
//									// gpsģʽ
//									if (MemoryMg.getInstance().GpsLocationModel == 0) {
////										GpsManage.getInstance(context).GetValueGpsStr();
//										infoList = GpsTools.getInfo();
//										MyLog.i("ReceiverThread", "ReceiverThread��ǰ��λ��ʽΪGPSģʽ");
////										Toast.makeText(context, "��ǰ��λ��ʽΪGPSģʽ", Toast.LENGTH_LONG).show();								
//									}
//									// �ٶ�ģʽ
//									else if (MemoryMg.getInstance().GpsLocationModel == 1) {
//										infoList = GpsTools.getInfo();
//										MyLog.i("ReceiverThread", "ReceiverThread��ǰ��λ��ʽΪ�ٶ�ģʽ");
////										Toast.makeText(context, "��ǰ��λ��ʽΪ�ٶ�ģʽ", Toast.LENGTH_LONG).show();
//									}//�ٶ�GPS
//									else if (MemoryMg.getInstance().GpsLocationModel == 2) {
//										infoList = GpsTools.getInfo();
//										MyLog.i("ReceiverThread", "ReceiverThread��ǰ��λ��ʽΪ�ٶ�GPSģʽ");
////										Toast.makeText(context, "��ǰ��λ��ʽΪ�ٶ�GPSģʽ", Toast.LENGTH_LONG).show();
//									}
									infoList = GpsTools.getInfo();
									UploadGPSInfo(infoList);
								}
								MyLog.e("GPSCycle", "gps cycle is run");
								// �������ȴ�һ��ʱ���������gps����
								handler.sendMessageDelayed(// ���ݲ��� ���ʱ��
										handler.obtainMessage(CYCLEUP, cyclex), cyclex);
							} else
								MyLog.i("gpsReceiveThread", "stop message");

							break;
						case NORMALSTATE:// ÿ��45�� �����������һ���հ�����˲���ѭ��
							// ���Ϳհ�4���ֽ�
							MyLog.i("send null packet", "gps send null packet");
							SendGPS(GetEmptyByte());
							handler.sendMessageDelayed(
									handler.obtainMessage(NORMALSTATE), 45000);
							break;
//						case EXITSYSTEM:// �˳�ϵͳ
//							DestoryAll();
//							break;
						case SAVEVALUE:
							int sec = (Integer) msg.obj;
							int model = 0;
							if (sec == 5)
								model = 0;
							else if (sec == 15)
								model = 1;
							else if (sec == 30)
								model = 2;
							else// 80S
								model = 3;
							
							SharedPreferences  mSharedPreferences = context.getSharedPreferences(Settings.sharedPrefsFile,
									Activity.MODE_PRIVATE);
							Editor edit = mSharedPreferences.edit();
							edit.putInt(Settings.PREF_LOCUPLOADTIME, model);
							edit.commit();
								
							break;
						}
					} catch (Exception e) {
						Log.e("", "", e);
					}
				}

			};
	
		
	
	
	public ReceiveThread(DatagramSocket socket, Context context) {
		this.socket = socket;
		this.context = context;
	}
	
	
	/**
	 * gps��¼�ص�
	 */
	public void setGpsListener(GpsListener regListener) {
		this.regListener = regListener;
	}
	
	private Looper myLooper = null;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.i("xxxx","ReceiverThread#run enter");
		Looper.prepare();
		
		myLooper = Looper.myLooper();
		
		onLooperPrepared();
		
		Looper.loop();
		
		Log.i("xxxx","ReceiverThread#run exit");
		
	}
	

	private void onLooperPrepared() {
		Log.i("xxxx","ReceiverThread#onLooperPrepared enter");
		startRun();
		Log.i("xxxx","ReceiverThread#onLooperPrepared exit");
	}


	private void startRun() {
		Log.i("xxxx","ReceiverThread#startRun enter");
		try {
			//
//			String arr = "", str = "";
			int cycle = 0,upTime=0;
			int extType = 0;//pdu��չ����
			byte[] buffer = new byte[1024];
			DatagramPacket packet = null;
			
			while (GetReturnFlag()) {
				packet = new DatagramPacket(buffer, buffer.length);
				Log.i("xxxx","ReceiverThread#receive enter");
				if(socket != null){
					try {
						socket.setSoTimeout(TIMEOUT);
						socket.receive(packet);
					} catch (Exception e) {
						if((e != null) && !(e instanceof SocketTimeoutException)){
							e.printStackTrace();
						}
						packet = null;
						/**
						 * ��GPSʱloginFlagΪFALSE��GPS��¼�ɹ���loginFlag��ΪTRUE
						 * ���GPS��¼��ʱ���ߵ�������·���һ����½��ֱ����¼�ɹ�
						 */
						if(!GPSPacket.loginFlag){
							count++;
							if(count > 20){
								GpsTools.UploadGPSByTerminal(129, 0, 0, 0, 0, 0, 0, GpsTools.getE_id());
								Log.i("xxxx", "ReceiveThread#GPSPacket.loginFlag"+GPSPacket.loginFlag);
								count = 0;
							}
							continue;
						}
						
					}
				}
				Log.i("xxxx","ReceiverThread#receive exit");
				
				if(packet == null){
					continue;
				}
				
				//����
				if(packet.getLength()+42>60){
					flow = packet.getLength()+42;
				}else{
					flow = 60;
				}
				FlowStatistics.Gps_Receive_Data = FlowStatistics.Gps_Receive_Data+flow;
				
				
				// ��ȡ��չ����
				extType = GpsTools.BitToPDUExtendType(buffer);
				MyLog.e("gpsReceiveThread", "msgtype:" + extType);
				
				// ���Ȼ���Ӧ ����/�޸��ϴ�����
				if (extType == 5) {
					Zed3Log.debug("testgps", "ReceiveThread#startRun extType=5");
					//��ȡ���Ȼ���Ӧ���ն˺����ID
					if (MemoryMg.getInstance().TerminalNum.equals(GpsTools
							.BitToUploadCycleID(buffer))) {
						// ��С�ϱ�������ڴӷ���˻�ȡ������ֱ�Ӷ�ȡpctool
						cycle = ((buffer[1] & 0xFE) >> 1);
						MyLog.e("gps cycle up cycle:", cycle + "**");

						if (cycle == 127)// ˵�������û���ϱ��������Ҫ��ȡ����PCTool���ϱ����ʱ��
						{
							upTime = GpsTools
									.GetLocationTimeValByModel(MemoryMg
											.getInstance().GpsUploadTimeModel);
							if (upTime > 0)
								// ��ȡpctool����ϱ�ʱ����
								cycle = ChangeTimeToLip(upTime);
							else
								cycle = 4;
							
							SetGPSReCycle(cycle);			

						} else {//������Ҫ���ֻ��˰��մ��ݵ�ʱ�����ϱ�
							SetGPSReCycle(cycle);
						}
					}
				}//����/�޸Ĵ�������[���Ȼ�]
				else if (extType == 6) {
					// �����������塾δ��ɡ�����19(����������)���ȣ�9 ����λbit�����ͣ�
					// ��ȡ���ͣ�12 �����ͣ�21 ����ȷ��λ��(����)��255 ���� occurType  2�ǽ�������
					// ��ȡѭ����0 һ��/ 1 ѭ�� occurCircle
					this.BitToSetOrUpdateOccurID(buffer);
					
				}//ɾ����������[���Ȼ�]
				else if(extType==7)
				{
					//��ȡ���Ȼ���Ӧ���ն˺����ID
					//���ͣ�12 �����ͣ�21 ����ȷ��λ��(����ʧ��)(����)��255 ����  2�ǽ�������
					this.BitToDelOccurID(buffer);
					
				}//���Ȼ�Ҫ��   �������� || ���رա�  �ն�GPS�ϴ�
				else if(extType==10)
				{
					//��ȡ���Ȼ���Ӧ���ն˺����ID
					if (MemoryMg.getInstance().TerminalNum.equals(GpsTools
							.BitToOpenCloseGPSID(buffer))) {
						if (buffer[1] == 1) {// ������־
							// ���뿪��gps+�ٶ�   ����������Ȼ����Ϳ������
							GpsTools.OpenGPSByServer(context);
							
						} else {
							// ����ر�gps+�ٶ�
							GpsTools.CloseGPSByServer(context);
						
						}
					}
				}
				// 
				else if (extType == 4) {
					
					// ���Ȼ���Ӧͨ��Ӧ�𣬲����ǵ�¼
					int result = GpsTools.BitToSuccess(buffer);
//					System.out.println("---------buffer.size--"+buffer.length);
//					for(int i=0;i<36;i++){
//						System.out.println("----buffer----"+i+"----="+buffer[i]);
//					}
					int type = GpsTools.BitToExtralType(buffer);
					String UnixE_id = GpsTools.BitToE_id(buffer);
					if(type == 18){
						UnixTime = GpsTools.BitToUnixTime(buffer,4);
						RealTime = GpsTools.getCurrentRealTime();
						LocalTime = GpsTools.getCurrentLocalTime1();
//						GpsTools.saveUnixTime(UnixTime);
//						GpsTools.saveLocalTime(LocalTime);
//						GpsTools.saveRealTime(RealTime);
						GpsTools.saveTime(UnixTime, LocalTime, RealTime);
						System.out.println("gps--ReceiverThread--unixtime--="+UnixTime);
						System.out.println("gps--ReceiverThread--LocalTime--="+LocalTime);
						System.out.println("gps--ReceiverThread---RealTime ="+RealTime);
					}
					int uploadType = GpsTools.BitToUploadType(buffer);
					MyLog.e("GpsRecv", "Recv Server Respond:" + result + "");
					if (result == 0){
						regListener.LoginResult(0);
						regListener.UploadResult(0, uploadType,UnixE_id);
					}
					else{
						
						regListener.LoginResult(1);
						regListener.UploadResult(1, uploadType,UnixE_id);
					}
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i("xxxx","ReceiverThread#run exception = " + e.getMessage());
		}
	}


	private int ChangeTimeToLip(int second) {
		int cycle = 0;
		if (second == 1)
			cycle = 0;// 1s
		else if (second == 2)
			cycle = 1;// 2s
		else if (second > 2 && second <= 5)
			cycle = 2;// 5s
		else if (second > 5 && second <= 10)
			cycle = 3;// 10s
		else if (second > 10 && second <= 15)
			cycle = 4;// 15s
		else if (second > 15 && second <= 30)
			cycle = 5;// 30s
		else if (second > 30 && second <= 40)
			cycle = 6;// 40s
		else if (second > 40 && second <= 80)
			cycle = 7;
		else
			cycle = 4;

		return cycle;
	}

	private void SetGPSReCycle(int cycle) {
		int second = 0;
		if (cycle >= 0) {
			if (cycle <= 6) {
				if (cycle == 0)
					second = 1;// 1s
				else if (cycle == 1)
					second = 2;// 2s
				else if (cycle == 2)
					second = 5;// 5s
				else if (cycle == 3)
					second = 10;// 10s
				else if (cycle == 4)
					second = 15;// 15s
				else if (cycle == 5)
					second = 30;// 30s
				else if (cycle == 6)
					second = 40;// 40s
			} else {
				if (cycle > 6 && cycle <= 29) {
					second = (cycle + 1) * 10;
				} else if (cycle >= 30 && cycle <= 59) {
					second = (int) (5.5 + 0.5 * (cycle - 30)) * 60;
				} else if (cycle >= 60 && cycle <= 127) {
					second = (21 + 1 * (cycle - 60)) * 60;
				} else// Ĭ��1�����ϴ�һ��
				{
					second = 60;
				}
			}
			// /����/�޸��ϴ����ڽ��--�ɹ� flagΪ�������
			SendGPS(GpsTools.ReplyUploadCycleByte(CheckGpsEnabled(),
					MemoryMg.getInstance().TerminalNum, 0, cycle));

			// ��second���浽ȫ�ֱ�������ȥ
			// MemoryMg.getInstance().gpsSecond = second;

			second = second * 1000;// ת���ɺ���
			tipFlag = true;
			
			// �ȹر�gps
			//GpsManage.getInstance(context).CloseGPS();

			if (handler.hasMessages(CYCLEUP)) {
				handler.removeMessages(CYCLEUP);
			}
			handler.sendMessage(handler.obtainMessage(CYCLEUP, second));
			
			//�����ϱ�ʱ�䵽sharedpreference�����ļ�
			handler.sendMessage(handler.obtainMessage(SAVEVALUE, second / 1000));
			
		} else// ����/�޸��ϴ����ڽ��---ʧ�ܡ��նˡ� �������0~255
		{
			SendGPS(GpsTools.ReplyUploadCycleByte(CheckGpsEnabled(),
					MemoryMg.getInstance().TerminalNum, 1, 0));
		}
	}

	// ��ȡ����/�޸Ĵ�������[���Ȼ�] �ն�ID
	public void BitToSetOrUpdateOccurID(byte[] b) {
		// ��ȡ����
		int len = (((b[1] & 0x01) << 7) | (b[2] & 0xF8) >> 3);
		int i = (len - 4) / 8;
		int j = (len - 4) % 8;

		String str = "";
		int m = 0;
		for (m = 0; m < i; m++) {
			str += ((b[3 + m] & 0x78) >> 3) + "";
			str += ((b[3 + m] & 0x07) << 1 | (b[3 + m + 1] & 0x80) >> 7) + "";
		}

		if (j != 0) {// ����
			str += ((b[3 + i] & 0x78) >> 3) + "";
			// ��������ֽ�����ֱ��ǣ�[3+m+2] �� [3+m+3]
			occurType = (b[3 + m + 2] & 0xFF);
			occurCircle = ((b[3 + m + 3] & 0x80) >> 7);

		} else// ż��
		{
			// ��������ֽ�����ֱ��ǣ�[3+m+1] �� [3+m+2]
			occurType = ((b[3 + m + 1] & 0x0F) << 4 | (b[3 + m + 2] & 0xF0) >> 4);
			occurCircle = ((b[3 + m + 2] & 0x08) >> 3);
		}

		// ��ȡ���Ȼ���Ӧ���ն˺����ID
		if (MemoryMg.getInstance().TerminalNum.equals(str)) {
			if (occurType == 12) {
				// MemoryMg.getInstance().IsBatteryLower=true;
				MyLog.e("GPS ", "���õ�ص�����");//
			} else if (occurType == 21) {
				// MemoryMg.getInstance().IsGPSLoaction=true;
				MyLog.e("GPS ", "���� ����");//
			} else if (occurType == 255) {
				// MemoryMg.getInstance().IsLand=true;
				MyLog.e("GPS ", "���� ���ظ澯");// �ָ�ֵΪ254
			} else if (occurType == 2) {
				// MemoryMg.getInstance().IsUrgentCall=true;
				MyLog.e("GPS ", "��������");
			}
			// �������Ҫ�����˷���Ӧ�𡣡�����
			SendGPS(GpsTools.ReplyServerSetOrUpdateOccur(
					MemoryMg.getInstance().TerminalNum, occurCircle, occurType));
		}

		MyLog.e(">>>>>SetOrUpdateOccur", occurType + " " + occurCircle
				+ "  name:" + str);
	}

	// ��ȡ ɾ���������� �ն�ID
	public void BitToDelOccurID(byte[] b) {
		// ��ȡ����28
		int len = (((b[1] & 0x07) << 3) | (b[2] & 0xE0) >> 5);
		int i = (len - 4) / 8;
		int j = (len - 4) % 8;

		String str = "";
		int m = 0;
		for (m = 0; m < i; m++) {
			str += (((b[2 + m] & 0x01) << 3) | ((b[2 + m + 1] & 0xE0) >> 5))
					+ "";
			str += ((b[2 + m + 1] & 0x1E) >> 1) + "";
		}

		if (j != 0) {// ����
			str += (((b[2 + i] & 0x01) << 3) | ((b[2 + i + 1] & 0xE0) >> 5))
					+ "";

			// ��������ֽ�����ֱ��ǣ�[2+m+1] �� [2+m+2]
			occurType = ((b[2 + m + 1] & 0x01) << 7 | (b[2 + m + 2] & 0xFE) >> 1);
			delType = ((b[2 + m + 1] & 0x02) >> 2);
		} else// ż��
		{
			// ��������ֽ�����ֱ��ǣ�[2+m+2] �� [2+m+3]
			occurType = ((b[2 + m + 2] & 0x1F) << 3 | (b[2 + m + 3] & 0xE0) >> 5);
			delType = ((b[2 + m + 2] & 0x20) >> 5);
		}

		if (MemoryMg.getInstance().TerminalNum.equals(str)) {
			// if (occurType == 12)
			// MemoryMg.getInstance().IsBatteryLower = false;
			// else if (occurType == 21)
			// MemoryMg.getInstance().IsGPSLoaction = false;
			// else if (occurType == 255)
			// MemoryMg.getInstance().IsLand = false;
			// else if (occurType == 2)
			// MemoryMg.getInstance().IsUrgentCall = false;
			// �������Ҫ�����˷���Ӧ�𡣡�����
			SendGPS(GpsTools.ReplyServerDelOccur(
					MemoryMg.getInstance().TerminalNum, delType, occurType));
		}
		MyLog.e(">>>>>DelOccur", occurType + " " + delType + "  name:" + str);
	}

	// �ⲿ���� �ն���������handler
	public void StartHandler() {
		// ��ԭ���������
		if (handler.hasMessages(NORMALSTATE)) {
			handler.removeMessages(NORMALSTATE);
		}
		// �ն� ������ ���հ�
		handler.sendMessage(handler.obtainMessage(NORMALSTATE));
	}

	// �˳�
	public void ExitSys(boolean isCloseGps) {

		// ����հ� ����
		if (handler.hasMessages(NORMALSTATE)) {
			handler.removeMessages(NORMALSTATE);
		}
		// ��ʱ�ϴ�gps��ȥ��
		if (handler.hasMessages(CYCLEUP)) {
			handler.removeMessages(CYCLEUP);
		}
		DestoryAll(isCloseGps);
		
		//handler.sendMessage(handler.obtainMessage(EXITSYSTEM));
	}

	// ��ȡ��ʶ
	public boolean GetReturnFlag() {
		return returnFlag;
	}

	// ֹͣ�߳�
	public void StopRunning() {
		if(myLooper != null){
			try{
				myLooper.quit();
			}finally {
				myLooper = null;
			}
		}
		returnFlag = false;
		handler = null;
	}

//	// ע�� ��ʶ
//	public String GetRegisterOK() {
//		return MemoryMg.getInstance().RegisterOk;
//	}
//	// ע�� ��ʶ
//	public void SetRegisterOK(String str) {
//		MemoryMg.getInstance().RegisterOk = str;
//	}

	// private double test_x=116.46;//����
	// private double test_y=39.92;//γ��


	private void UploadGPSInfo(List<GpsInfo> infoList){
		if (infoList != null && infoList.size() > 0) {
			
			GpsTools.UploadGPSByTerminal(129,infoList);
//			for(int i = 0;i < infoList.size();i++){
//				GpsTools.UploadGPSByTerminal(129, infoList.get(i).gps_x,
//						infoList.get(i).gps_y, infoList.get(i).gps_speed,
//						infoList.get(i).gps_height, infoList.get(i).gps_direction,infoList.get(i).UnixTime,infoList.get(i).E_id);// ��ʱ�ϱ�
//				MyLog.i("GPS upload by seconds:" + cyclex, "----x:"
//						+ infoList.get(i).gps_x + "y:" + infoList.get(i).gps_y
//						+ "speed:" + infoList.get(i).gps_speed + "height:"
//						+ infoList.get(i).gps_height + " direction:"
//						+ infoList.get(i).gps_direction+" UnixTime:"+infoList.get(i).UnixTime+" E_id:"+infoList.get(i).E_id);
//			}
		} else {
			if (!tipFlag) {
				// Toast.makeText(context,R.string.gps_Starfailure,Toast.LENGTH_SHORT).show();
				MyLog.e("GPS upload by seconds: fail"
						+ cyclex, "null");
				// ����ʧ��
				GpsTools.UploadGPSByTerminal(21, 0, 0, 0,
						0, 0,0,"");
			} else
				tipFlag = false;
		} // ���� γ��
	}
	
	// ���gps�Ƿ���
	private int CheckGpsEnabled() {
		boolean gpsEnabled = android.provider.Settings.Secure.isLocationProviderEnabled(
				context.getContentResolver(), LocationManager.GPS_PROVIDER);
		if (gpsEnabled)
			return 1;// ����
		else
			return 0;
	}

	// �հ�
	private byte[] GetEmptyByte() {
		byte[] buf = new byte[4];
		String str = "\r\n\r\n";
		byte[] temp = str.getBytes();
		System.arraycopy(temp, 0, buf, 0, temp.length);
		return buf;
	}

	// ����gps
	public void SendGPS(byte[] bytestr) {
		// GpsTools.CheckNetWork()
		try {
			SendThread send = new SendThread(
					MemoryMg.getInstance().getSocket(),
					InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
			// ������Ҫ�ĳ�byte[]�ֽ���
			send.SetContent(bytestr);
			send.start();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// ��¼ע��
//	public void SendLoginUdp() {
//		String usernum = MemoryMg.getInstance().TerminalNum;
//
//		//info = GpsManage.getInstance(context).GetValueGpsStr();
//		info = SipUAApp.gpsInfo;
//		try {
//			SendThread send = new SendThread(socket,
//					InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
//			if (info != null)
//				send.SetContent(GpsTools.LoginByte(usernum, info.gps_x,
//						info.gps_y, info.gps_speed));
//			else {
//
//				send.SetContent(GpsTools.LoginByte(usernum, 0, 0, 0));
//			}
//			send.start();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	

	// �˳�����
	public void DestoryAll(boolean isCloseGps) {
		System.out.println("--------ֹͣ�߳�------" + isCloseGps);
		// ֹͣ�߳�
		StopRunning();
		
		if(isCloseGps) {
			// ֹͣgps
			GpsManage.getInstance(context).CloseGPS();
		}
		
		// �ر�socket
		if (socket != null) {
			try {
				socket.close();
				socket = null;
				MyLog.e("socket", "---socket close");
			} catch (Exception e) {
				MyLog.e("socket", "stop sc error.");
			}
		}
		MyLog.e("GPSPacket", "GPS DestoryAll==");
	}
	

}
