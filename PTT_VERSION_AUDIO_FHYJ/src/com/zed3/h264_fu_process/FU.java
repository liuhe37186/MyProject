package com.zed3.h264_fu_process;

public class FU {
	public static final int PACKET_STATE_INIT = 0;
	public static final int PACKET_STATE_CONTINUE = 1;
	public static final int PACKET_STATE_COMPLETE = 2;
	// FU_TYPE type;
	FuType type;
	// string data;//������
	//adb shell �鿴�ֻ��ڴ���� cat /proc/meminfo
	byte[] data = new byte[1024 * 1800];// edit by hdf: 64--->128(720p����)-->180
	// //֡��ʱ���
	/** ֡��ʱ��� */
	// unsigned int timeStamp;
	long timeStamp;
	// unsigned int seqNumOrig;
	long seqNumOrig;
	// //��ǰ��Ƭ�����к�
	// unsigned int seqNumReconstruct;
	long seqNumReconstruct;
	// unsigned int len;
	long len;
	//integrity
//	boolean isComplete;//�Ƿ��������İ�
	int packetState = PACKET_STATE_INIT; 
	
	public int lostCount;
	public int totalCount;
	public int firstSeqNum;
	// _fu()
	// {
	// type = FU_TYPE_INVALID;
	// timeStamp = 0;
	// seqNumOrig = 0;
	// seqNumReconstruct = 0;
	// len = 0;
	// if (len > 0)
	// data.erase(0, len);
	// }
	public FU() {
		// type = FU_TYPE_INVALID;
		type = FuType.FU_TYPE_INVALID;
		// timeStamp = 0;
		timeStamp = 0;
		// seqNumOrig = 0;
		seqNumOrig = 0;
		// seqNumReconstruct = 0;
		seqNumReconstruct = 0;
		firstSeqNum = 0;
		totalCount = 0;
		lostCount =0;
		// len = 0;
		len = 0;
//		isComplete = true;
		packetState = FU.PACKET_STATE_INIT;
		// if (len > 0)
		// data.erase(0, len);
		if (len > 0) {
			// data.erase(0, len);//���   ???????���������ô����
			for (int i = 0; i < data.length; i++) {
				data[i] = 0;
			}
			
		}
	}
	public void init(int seqNum){
		type = FuType.FU_TYPE_INVALID;
		timeStamp = 0;
		seqNumOrig = 0;
		seqNumReconstruct = 0;
		lostCount =0;
		firstSeqNum = seqNum;
		totalCount = 0;
		len = 0;
//		isComplete = true;
		packetState = FU.PACKET_STATE_INIT;
		if (len > 0) {
			for (int i = 0; i < data.length; i++) {
				data[i] = 0;
			}
		}
	}

	// const char *GetData()
	// {
	// return data.c_str();
	// }
	public byte[] getData() {
		// return data.c_str();
		return data;
	}
	
	// int GetDataLen()
	public long getDataLen() {
		return len;
	}
}
