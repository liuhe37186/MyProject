package com.zed3.sipua;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zoolu.tools.MyLog;

import android.media.MediaCodec;
import android.view.SurfaceView;

import com.video.utils.H264Dec;
import com.video.utils.IVideoSizeChange;
import com.zed3.flow.FlowStatistics;
import com.zed3.h264_fu_process.FU;
import com.zed3.h264_fu_process.H264FUManager;
import com.zed3.jni.VideoUtils;
import com.zed3.net.RtpPacket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.NetChangedReceiver;
import com.zed3.utils.Tools;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.ReceivePacketCompare;
import com.zed3.video.ReceivePacketInfo;
/*
 * 
 */
public class VideoUdpThread {
	public static final int MAX_HANDLE_VIDEO_PLAY_INTERVAL=20;//ms ������Ƶ���ŵ����ʱ������
	public static final int SO_TIMEOUT = 1000;
	long lastTokenTimeMS = -1;//�͸���Ƶ����ģ������ʱ�䣬��λ�Ǻ��룻
	long lastTokenFrameTS = -1;//�͸���Ƶ����ģ��������ʱ����
	public static int VideoPlaySpeedFactor=1;//��Ƶ�����������ӣ�ȱʡΪ1���������ۻ��ӳٳ�ԽMaxVideoJitterbufferDelayʱ���Ϊ2����Ƶ2���ٲ��ţ�
	long lastSystemTime = 0;
	List<ReceivePacketInfo> mList;
	int count = 0;
	boolean needCallTryConfig = false;
	boolean isIFrameDiscard = false;
	boolean isPFrameDiscard = false;
	int sFlow = 0;// �������㷢������
	int rFlow = 0;// ���������������
	private String TAG = "VideoUdpThread";
	// private int videoWidth = 0, videoHeight = 0; // 320/*176*/ 240/*144*/
	private DialogListener dialogListener;// �����ӿ�

	private DatagramSocket mdsSocket = null;
	private DatagramPacket dpPacket;

	private byte[] spsFrame = null,ppsFrame = null;
	private boolean Flag = false, markflag = false;
	private int recCount = 0,fuByteLen = 0, pixWidth = 0,
			pixHeight = 0, recLost = 0;
	private byte[] parambyte = null, picbyte = null, rtpbuffer = null,
			newbuf = null, recbuffer = null;// ����֡
	private short seqNum = 0;
	private byte[] timeSpanByte, tempb;// ʱ����ֽ�
	private int parVal = 0;
	private long seqTime = 3600;

	int[] lastArray= new int[2];
	SurfaceView videoView;
	private int[] withAndHight;

	//
	RtpPacket rtpPacket = null;
	private H264FUManager h264FuManager = null;
	H264Dec h264Dec = null;
	IVideoSizeChange mListener;
	Thread t2;
	public VideoUdpThread(SurfaceView videoView,IVideoSizeChange listener) {
		this.mListener = listener;
//		this.videoView = videoView;
		h264Dec = new H264Dec(videoView);
		this.Flag = true;
		h264FuManager = H264FUManager.getInstance();

		try {
			mdsSocket = new /*SipdroidSocket(0)*/DatagramSocket();
			mdsSocket.setSoTimeout(SO_TIMEOUT);
			// ���ܻ���Ӵ�Ϊ64k
			mdsSocket.setReceiveBufferSize(1024 * 512);
			mdsSocket.setSendBufferSize(1024 * 512);

		} catch (Exception e) {
			MyLog.e(TAG + "mdsSocket time out", e.toString());
		}
		mList = new ArrayList<ReceivePacketInfo>();

		// �߳�ͬ��
		TimeOutSyncBufferQueue tSyncQueue = new TimeOutSyncBufferQueue();
		SyncBufferQueue sync = new SyncBufferQueue();
		Thread t1 = new Thread(new Producer(tSyncQueue));//�����߳�
//		Thread t2 = null;
		Thread videoProcess = new Thread(new VideoProcess(tSyncQueue,sync));//��Ƶ�����߳�
		videoProcess.start();
		t2 = new Thread(new Consumer(sync));//�����߳�
//		if(!MemoryMg.getInstance().isSendOnly){
		t2.start();
		t1.start();
//		}
		//onDestory()����Ҫ��t1,t2 InterruptedException
		lastSystemTime = System.currentTimeMillis();
	}

	/**
	 * �ṩ���ⲿ����Ϣ��������
	 * 
	 * @param DialogListener
	 *            ��Ϣ�����ӿڶ���
	 */
	public void setDialogListener(DialogListener dialogListener) {
		this.dialogListener = dialogListener;
	}

	// ������Ƶ����
	class Producer implements Runnable {
		TimeOutSyncBufferQueue tSync = null;

		int len = 0;
		long tempStamp = 0,lastTimeStamp = -1;
		byte[] pBuffer = null;
		int timeOutCount = 0;
		public Producer(TimeOutSyncBufferQueue tSync) {
			this.tSync = tSync;
			MyLog.e(TAG, "producer runnable");
		}
		@Override
		public void run() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			byte[] buffer = new byte[1024 * 10];
			DatagramPacket packet = null;
			while (Flag) {
				try {
					packet = new DatagramPacket(buffer, buffer.length);// SDK4.0
					mdsSocket.receive(packet);
					len = packet.getLength();
					if(len > 0){
						timeOutCount = 0;
					}
					FlowStatistics.Video_Receive_Data += len;
					if (len > 12) {
						pBuffer = new byte[len];
						System.arraycopy(buffer, 0, pBuffer, 0, len);
						tSync.push(pBuffer);
					}
				} catch (Exception e) {
					e.printStackTrace();
					timeOutCount ++;
					if(timeOutCount > 30){
						CallUtil.rejectCall();
						timeOutCount = 0;
					}
				}
			}
		}

	}

	// ��Ƶ�����߳�
	class VideoProcess implements Runnable{
		long firstTimeStamp,firstTimeMS;
		boolean firstTime = true;
		int lastTokenPackageSeq = -1;//�͸���Ƶ����ģ����������ţ�
		List<ReceivePacketInfo> receivePackageList = null;
		TimeOutSyncBufferQueue tSync;
		SyncBufferQueue sync;
		int count = 0;
		int DelaySum = 0;
		public VideoProcess(TimeOutSyncBufferQueue tSync,SyncBufferQueue sync){
			this.tSync = tSync;
			this.sync = sync;
			receivePackageList = new ArrayList<ReceivePacketInfo>();
		}
		@Override
		public void run() {
			android.os.Process
			.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			long queueDelayTime=0;
//			int midpoint=DeviceVideoInfo.MinVideoJitterbufferDelay+(DeviceVideoInfo.MaxVideoJitterbufferDelay-DeviceVideoInfo.MinVideoJitterbufferDelay)/2;
			while(Flag){
				try {
					byte[] buffer = tSync.pop();
					if(buffer != null){
						int len = buffer.length;
						int seqNum = getSequenceNumber(buffer, len);
						long timeStamp = getTimestamp(buffer, len);
						//sort
						ReceivePacketInfo receiPackage = new ReceivePacketInfo(seqNum, buffer, timeStamp);
						receivePackageList.add(receiPackage);
						Collections.sort(receivePackageList, new ReceivePacketCompare());
					}
					while(receivePackageList.size() > 0){
						if(IsNowSeqLarger(receivePackageList.get(0).getSeqNum(),lastTokenPackageSeq)){
							break;
						}
						receivePackageList.remove(0);
					}
					if(receivePackageList.size() < 3) continue;
					queueDelayTime=calcIntervalBetweenTwoTS(receivePackageList.get(receivePackageList.size()-1).getTimeStamp(),receivePackageList.get(0).getTimeStamp());
					if(queueDelayTime > DeviceVideoInfo.MaxVideoJitterbufferDelay){
						VideoPlaySpeedFactor = 2;
					}else{
						if((queueDelayTime < DeviceVideoInfo.MidVideoJitterbufferDelay/*midpoint*/) && (VideoPlaySpeedFactor != 1)){
							VideoPlaySpeedFactor = 1;
						}
					}
					if(queueDelayTime > DeviceVideoInfo.MinVideoJitterbufferDelay){

						if(isThisFramePlayTime(receivePackageList.get(0).getTimeStamp(),lastTokenFrameTS)){
							if(firstTime){
								firstTimeMS = System.currentTimeMillis();
								firstTimeStamp = receivePackageList.get(0).getTimeStamp();
								firstTime = false;
							}else{
								count++;
								DelaySum += queueDelayTime;
								if(count == 20){
									count = 0;
									DelaySum = 0;
								}
							}
							ReceivePacketInfo firstPacket = receivePackageList.get(0);
							sync.push(firstPacket.getData());
							receivePackageList.remove(0);
							lastTokenPackageSeq = firstPacket.getSeqNum();
							while(receivePackageList.size() > 0){
								if(receivePackageList.get(0).getTimeStamp() != firstPacket.getTimeStamp()){
									break;
								}
								if(IsNowSeqLarger(receivePackageList.get(0).getSeqNum(),lastTokenPackageSeq)){
									lastTokenPackageSeq = receivePackageList.get(0).getSeqNum();
									sync.push(receivePackageList.get(0).getData());
									receivePackageList.remove(0);
								}else{
									MyLog.e(TAG, "Error!,thread2 receive same seq! curSeqNum="+receivePackageList.get(0).getSeqNum()+",last = "+lastTokenPackageSeq);
								}
							}
							setAndAdjustLastTokenTimeMS(firstPacket.getTimeStamp());
							lastTokenFrameTS = firstPacket.getTimeStamp();
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	// ��ʾ��Ƶ����
	class Consumer implements Runnable {
		int frameType = 0;
		SyncBufferQueue s;
		byte[] buffer = null;
		byte[] recpspbuffer = null;
		int w = 0, h = 0, k = 0, len = 0, conCount = 0, seqNumLog = 0;
		byte[] head = { 0, 0, 0, 1 };
		long a = 0, b = 0;
		int temp = 0;
		// RandomAccessFile raf = null;
		byte[] tempBuffer = null;
		FU fuByte = null;
		public Consumer(SyncBufferQueue s) {
			this.s = s;
			MyLog.e(TAG, "producer Consumer");

			// try {
			// File file = new File("/sdcard/videoc.h264");
			// raf = new RandomAccessFile(file, "rw");
			// } catch (Exception ex) {
			// Log.v("System.out", ex.toString());
			// }
		}

		@Override
		public void run() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			while (Flag) {
				try {

					buffer = s.pop();
					len = buffer.length;
					seqNumLog = getSequenceNumber(buffer, len);

					a = System.currentTimeMillis();
//					MyLog.e("video timestamp", "begin...seqNum:" + seqNumLog);

//					MyLog.e(TAG, "producer Consumer buffer count:"
//							+ (conCount++));

					if ((buffer[12] & 0x1F) != 28) {// ��FU��NAL ����28����fu

						if ((buffer[12] & 0x1F) == 7
								|| (buffer[12] & 0x1F) == 8
								|| (buffer[12] & 0x1F) == 6) {// sps pps

							recbuffer = new byte[len - 12];
							System.arraycopy(buffer, 12, recbuffer, 0, len - 12);
//							MyLog.e(TAG, "RTP is not 28 but 1");
						}
						// edit by zzhan begin С��1400�ֽڵ�NAL��(eyebeam��fu����)
						else if (hasMarker(buffer, len) == true
								&& (buffer[13] & 0x80) == 0x80) {
								recbuffer = new byte[len - 12];
								System.arraycopy(buffer, 12, recbuffer, 0,
										len - 12);
//								MyLog.e(TAG, "RTP is not 28 but 2");
						} else {
//							MyLog.e(TAG, "RTP is not 28 but FU4Eyebeam");
							// mark:0 start:0x80
							// ......
							// mark:1 start:������0x80
							// -------eyebeam��Ƭ-------------------
							rtpPacket = new RtpPacket(buffer, len, 0);
							fuByte = h264FuManager
									.processFU4Eyebeam(rtpPacket);
							// Ϊnull�������ں�Ƭ����Ϊ�������ֱ�ӷ���������
							if (fuByte == null) {
								MyLog.i(TAG, "not 28 eyebeam fuByte == null");
								continue;
							}
							// ��ȡ�����ĺ�֡���ݣ��������д�����������64k
							fuByteLen = (int) (fuByte.getDataLen());
							recbuffer = new byte[fuByteLen];
							System.arraycopy(fuByte.getData(), 0, recbuffer, 0,
									fuByteLen);
							if(fuByte.lostCount > 0){
								recbuffer[0] |= 0x80;
							}
							// ----------------------------
						}// edit by zzhan end

//						MyLog.e("video timestamp",
//								"No proces:" + (System.currentTimeMillis() - a));
						a = System.currentTimeMillis();

						/*
						 * Delete by zzhan 2013-02-22 else if
						 * ((hasMarker(buffer, len) == false && (buffer[13] &
						 * 0x80) == 0x80) || (hasMarker(buffer, len) == true &&
						 * (buffer[13] & 0x80) != 0x80)) {// ����eyebeam����Ƶ��
						 * MyLog.e(TAG, "RTP is not 28 but FU4Eyebeam"); //
						 * mark:0 start:0x80 // ...... // mark:1 start:������0x80
						 * // -------eyebeam��Ƭ------------------- rtpPacket =
						 * new RtpPacket(buffer, len, 0); FU fuByte =
						 * h264FuManager .processFU4Eyebeam(rtpPacket); //
						 * Ϊnull�������ں�Ƭ����Ϊ�������ֱ�ӷ��������� if (fuByte == null) {
						 * MyLog.i(TAG, "not 28 eyebeam fuByte == null");
						 * continue; } // ��ȡ�����ĺ�֡���ݣ��������д�����������64k fuByteLen =
						 * (int) (fuByte.getDataLen()); recbuffer = new
						 * byte[fuByteLen]; System.arraycopy(fuByte.getData(),
						 * 0, recbuffer, 0, fuByteLen); //
						 * ---------------------------- } else { recbuffer = new
						 * byte[len - 12]; System.arraycopy(buffer, 12,
						 * recbuffer, 0, len - 12); MyLog.e(TAG,
						 * "RTP is not 28 but 2"); }
						 */

					} else {// FU
						MyLog.i(TAG, "Rtp is 28 seqNum:" + seqNumLog);
						// + " timestamp:"+ getTimestamp(buffer, len)
						// b=System.currentTimeMillis();

						rtpPacket = new RtpPacket(buffer, len, 0);
						// MyLog.e("video timestamp", "RtpPacket time..." +
						// (System.currentTimeMillis() - b));

						fuByte = h264FuManager.processFU(rtpPacket);
						// MyLog.e("video timestamp", "processFU time..." +
						// (System.currentTimeMillis() - b));

						// Ϊnull�������ں�Ƭ����Ϊ�������ֱ�ӷ���������
						if (fuByte == null) {
							MyLog.i(TAG, "28 fuByte == null");

//							MyLog.e("video timestamp",
//									"processFU..."
//											+ (System.currentTimeMillis() - a));
//							a = System.currentTimeMillis();
							continue;
						}

						// ��ȡ�����ĺ�֡���ݣ��������д�����������64k
						fuByteLen = (int) (fuByte.getDataLen());
						recbuffer = new byte[fuByteLen];
						System.arraycopy(fuByte.getData(), 0, recbuffer, 0,
								fuByteLen);
						if(fuByte.lostCount > 0){
							recbuffer[0] |= 0x80;
						}
//						MyLog.e("video timestamp", "processFU Success:"
//								+ (System.currentTimeMillis() - a));
						a = System.currentTimeMillis();
					}
					// H264����
					// �յ���Ƶ������Ҫ���룬����Ҫ���h264 Ȼ�����videoview
					if (recCount == 0) {
						// ��һ����Ϊ����֡
						if ((buffer[12] & 0x1F) != 7) {

							continue;
						} else {
//							MyLog.e(TAG, "recv seqnum parameter:" + seqNumLog);
							recCount = 1;
							// add by hdf20130108
							recpspbuffer = new byte[len - 13];// ��Ϊ��ȡ��Ƶ�ֱ������
							System.arraycopy(recbuffer, 1, recpspbuffer, 0,
									len - 13);

							// begin get width height from jni
							withAndHight = VideoUtils
									.getWithAndHight(recpspbuffer);
							if (withAndHight != null) {
								// �����Ⱥ͸߶�
								pixWidth = withAndHight[0];
								pixHeight = withAndHight[1];

//								MyLog.e(TAG, "receive parameter frame"
//										+ pixWidth + " " + pixHeight);
								// ȡ���ȴ�����...
//								dialogListener.DialogCancel(1);
								// �жϽ������Ƿ�֧�ָ÷ֱ���
								// if(isSupport(pixWidth + "*" + pixHeight))
								// {
								// MyLog.e(TAG, "this resolution not support");
								// VideoUdpThread.this.Flag = false;
								// //�ص�������仭��
								// dialogListener.DialogCancel(0);
								// return;
								// }
								// ȡ���ȴ�����...
//								dialogListener.DialogCancel(1);
								if (pixHeight > 720 && pixWidth >720){
									MyLog.e(TAG, "this resolution not support");
									VideoUdpThread.this.Flag = false;
									dialogListener.DialogCancel(0);
									return;
								}
								tempBuffer = new byte[temp];
								h264Dec.createCodec();
								h264Dec.tryConfig(pixWidth, pixHeight,null,null);
//								if(mListener != null){
//									mListener.sizeChanged(pixWidth, pixHeight);
//								}
//								// ��ʼ��������**
//								int i = vDec.a(pixWidth, pixHeight, temp);
//								if (i == -1) {
//									Log.e(TAG, "H264 init fail...");
//									return;
//								}
								// ǧ������˸��ġ�����������������������

							} else
								MyLog.e(TAG,
										"get width height from jni is null");
							// end
						}
					}else {
						//�жϷֱ���
						if((buffer[12] & 0x1F) == 7){
							recpspbuffer = new byte[len - 13];// ��Ϊ��ȡ��Ƶ�ֱ������
							System.arraycopy(recbuffer, 1, recpspbuffer, 0,
									len - 13);
//							spsArray = new byte[recpspbuffer.length+4];
//							System.arraycopy(head, 0, spsArray, 0, 4);
//							System.arraycopy(recbuffer, 0, spsArray, 4, recpspbuffer.length);
							// begin get width height from jni
							withAndHight = VideoUtils
									.getWithAndHight(recpspbuffer);
//							continue;
						}/*else if((buffer[12] & 0x1F) == 8){
							recpspbuffer = new byte[len - 12];// ��Ϊ��ȡ��Ƶ�ֱ������
							System.arraycopy(recbuffer, 0, recpspbuffer, 0,
									len - 12);
//							System.arraycopy(head, 0, ppsArray, 0, 4);
//							System.arraycopy(recbuffer, 0, ppsArray, 4, recpspbuffer.length);
						}*/
//						if(lastArray!= null && withAndHight!= null && (lastArray[0]!=withAndHight[0] || lastArray[1]!=withAndHight[1])){
//							pixWidth = withAndHight[0];
//							pixHeight = withAndHight[1];
//							h264Dec.reConfig(pixWidth, pixHeight,spsArray,ppsArray);
//						}

					}
					
					if (pixWidth != 0 && pixHeight != 0)
					// ���벢��ʾ��videoview��
					{
						tempb = new byte[recbuffer.length + 4];
						System.arraycopy(recbuffer, 0, tempb, 4,
								recbuffer.length);

						System.arraycopy(head, 0, tempb, 0, 4);
						if(tempb.length > 4){
							if((tempb[4]&0x1f)== 7 || (tempb[4]&0x1f)== 8 /*|| (tempb[4]&0x1f)== 5*/){
								if((tempb[4]&0x1f)== 7){
									spsFrame = tempb.clone();
									isIFrameDiscard = false;
									isPFrameDiscard = false;
									continue;
								}else if((tempb[4]&0x1f)== 8){
									ppsFrame = tempb.clone();
									isIFrameDiscard = false;
									isPFrameDiscard = false;
									tempb= new byte[/*iFrame.length+*/spsFrame.length+ppsFrame.length];
									System.arraycopy(spsFrame, 0, tempb, 0,
											spsFrame.length);
									System.arraycopy(ppsFrame, 0, tempb, spsFrame.length,
											ppsFrame.length);
//									continue;
									
									if(needCallTryConfig || (lastArray!= null && withAndHight!= null && (lastArray[0]!=withAndHight[0] || lastArray[1]!=withAndHight[1]))){
//										MyLog.e("video_dec", "reset h264dec1.. "+(++count) +"lastW="+lastArray[0]+", lastH="+lastArray[1]+"  w,h="+withAndHight[0]+","+withAndHight[1]);
										pixWidth = withAndHight[0];
										pixHeight = withAndHight[1];
										lastArray[0] = pixWidth;
										lastArray[1] = pixHeight;
//										MyLog.e("video_dec", "reset h264dec.. "+(++count) +"pixW="+pixWidth+", pixH="+pixHeight);
										h264Dec.reConfig(pixWidth, pixHeight,spsFrame,ppsFrame);
										if(mListener != null){
											mListener.sizeChanged(pixWidth, pixHeight);
										}
										needCallTryConfig = false;
									}
									frameType = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
								}//else {
//									iFrame = tempb.clone();
//									tempb= new byte[/*iFrame.length+*/spsFrame.length+ppsFrame.length];
//									System.arraycopy(spsFrame, 0, tempb, 0,
//											spsFrame.length);
//									System.arraycopy(ppsFrame, 0, tempb, spsFrame.length,
//											ppsFrame.length);
//									System.arraycopy(iFrame, 0, tempb, spsFrame.length+ppsFrame.length,
//											iFrame.length);
//								}
							}else if((tempb[4]&0x1f)== 6){
								frameType = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
							}else{
								frameType =0;
							}
						}
						// ���յ���Ƶ������ɱ����ļ�
						// raf.write(tempb);

//						MyLog.e(TAG, "test_remote_send " + tempb.length + " w:"
//								+ pixWidth + " h:" + pixHeight
//								+ " decode count:" + (decodeAllPacket++));
//
//						// b=System.currentTimeMillis();
//						// MyLog.e("video timestamp", "seqNumLog:"+ seqNumLog
//						// +" from pop to processFU timestamp:"+(b-a));
//
//						MyLog.e("video timestamp",
//								"before Decode..."
//										+ (System.currentTimeMillis() - a));
//						a = System.currentTimeMillis();

						// ����
//						k = vDec.a(tempb, tempb.length, tempBuffer);
//
//						if (k != -1) {
//							MyLog.e(TAG, "DecodeOneFrame ok k:" + k);
//
//							videoView.updateBitmap(pixWidth, pixHeight,
//									tempBuffer);
//
//						} else {
//							MyLog.e(TAG, "DecodeOneFrame fail k:" + k);
//						}
						
						if(tempb == null || tempb.length == 0) continue;
						if(fuByte!= null){
							if(fuByte.lostCount >= fuByte.totalCount){
								MyLog.e(TAG+"1", "isNeedDiscard():total:"+fuByte.totalCount+",lost:"+fuByte.lostCount);
							}
						}
						if((tempb[4] & 0x1f) == 5){
							if(fuByte!= null && isNeedDiscard(true,fuByte.lostCount,fuByte.totalCount)){
								continue;
							}
						} else if ((tempb[4] & 0x1f) == 1) {
							if (fuByte != null) {
								if (isNeedDiscard(false, fuByte.lostCount,
										fuByte.totalCount)) {
									continue;
								}
							} else {
								if (isNeedDiscard(false, 0, 10)) {
									MyLog.e("discard", "called");
									continue;
								}

							}
						}
						h264Dec.PlayDecode(tempb,frameType);
					}

				} catch (Exception e) {
					MyLog.e(TAG + "3", e.toString());
					e.printStackTrace();
				}

			}// end while

			h264Dec.releaseCodec();
			// release
//			if (videoView != null)
//				vDec.b();

			// if (raf != null)
			// try {
			// raf.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

		}

	}

	// ��ϸ����
	// public void UpdateVideoView(byte[] data, int w, int h) {
	// try {
	// NativeH264Decoder.DecodeAndConvert(data, this.bmpdata);
	// Bitmap bmp = Bitmap.createBitmap(this.bmpdata, w, h,
	// Bitmap.Config.RGB_565);
	//
	// // ����ͼ��???????
	// // videoView.setBitmap(bmp);
	// // ͼ����ת
	// videoView.setBitmap(bmp, w, h);
	// } catch (Exception e) {
	// MyLog.e(TAG,"UpdateVideoView error "+e.toString());
	// }
	// }
	// Thread thread = new Thread(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// android.os.Process
	// .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	// // ���롣����
	// ////InitNativeH264Decoder();
	//
	// byte[] buffer = new byte[1024 * 10];
	// DatagramPacket packet = new DatagramPacket(buffer,
	// buffer.length);
	//
	// while (Flag) {
	// try {
	// mdsSocket.receive(packet);
	// len = packet.getLength();
	//
	// seqNumLog = getSequenceNumber(buffer, len);
	// MyLog.e(TAG, "recv seqnum:" + seqNumLog + " size:"+ len);
	//
	// if (len > 12) {
	//
	// if ((buffer[12] & 0x1F) != 28) {
	//
	// if ((buffer[12] & 0x1F) == 7
	// || (buffer[12] & 0x1F) == 8
	// || (buffer[12] & 0x1F) == 6) {
	//
	// recbuffer = new byte[len - 12];
	// System.arraycopy(buffer, 12, recbuffer, 0,
	// len - 12);
	// // }
	// // // ���markΪ1 �� buffer�ĵ�һ���ֽ�&ox80Ϊ1
	// // else if (hasMarker(buffer, len) == true
	// // && (buffer[13] & 0x80) == 1) {
	// // MyLog.e(TAG, "RTP is not 28 but normal");
	// // // mark:1 start:1
	// //
	// // // �����Ϊ28��ȥ�� 12��ͷ�ֽڣ� RTP������
	// // recbuffer = new byte[len - 12];
	// // System.arraycopy(buffer, 12, recbuffer, 0,
	// // len - 12);
	// } else
	// if ((hasMarker(buffer, len) == false && (buffer[13] & 0x80) == 1)
	// || (hasMarker(buffer, len) == true && (buffer[13] & 0x80) != 1)) {//
	// ����eyebeam����Ƶ��
	// MyLog.e(TAG, "RTP is not 28 but FU4Eyebeam");
	// // mark:0 start:1
	// // ......
	// // mark:1 start:������1
	// // -------eyebeam��Ƭ-------------------
	// rtpPacket = new RtpPacket(buffer, len, 0);
	// FU fuByte = h264FuManager
	// .processFU4Eyebeam(rtpPacket);
	// // Ϊnull�������ں�Ƭ����Ϊ�������ֱ�ӷ���������
	// if (fuByte == null) {
	// MyLog.i(TAG,
	// "not 28 eyebeam fuByte == null");
	// continue;
	// }
	// // ��ȡ�����ĺ�֡���ݣ��������д�����������64k
	// fuByteLen = (int) (fuByte.getDataLen());
	// recbuffer = new byte[fuByteLen];
	// System.arraycopy(fuByte.getData(), 0,
	// recbuffer, 0, fuByteLen);
	// // ----------------------------
	// } else {
	// recbuffer = new byte[len - 12];
	// System.arraycopy(buffer, 12, recbuffer, 0,
	// len - 12);
	// }
	//
	// } else {// H264��Ƭ
	// MyLog.i(TAG,
	// "Rtp is 28 seqNum:" + seqNumLog
	// + " timestamp:"
	// + getTimestamp(buffer, len));
	//
	// rtpPacket = new RtpPacket(buffer, len, 0);
	//
	// FU fuByte = h264FuManager.processFU(rtpPacket);
	// // Ϊnull�������ں�Ƭ����Ϊ�������ֱ�ӷ���������
	// if (fuByte == null) {
	// MyLog.i(TAG, "28 fuByte == null");
	// continue;
	// }
	// //��ȡ�����ĺ�֡���ݣ��������д�����������64k
	// fuByteLen=(int) (fuByte.getDataLen());
	// recbuffer = new byte[fuByteLen];
	// System.arraycopy(fuByte.getData(), 0, recbuffer, 0,fuByteLen);
	//
	// }
	//
	// //H264����
	// // �յ���Ƶ������Ҫ���룬����Ҫ���h264 Ȼ�����videoview
	// if (NativeH264Encoder.getLastEncodeStatus() == 0) {
	// if (recCount == 0) {
	// // ��һ����Ϊ����֡
	// if ((buffer[12] & 0x1F) != 7) {
	//
	// continue;
	// } else {
	// MyLog.e(TAG, "recv seqnum parameter:"
	// + seqNumLog);
	// recCount = 1;
	// //add by hdf20130108
	// byte[] recpspbuffer = new byte[len-13];
	// System.arraycopy(recbuffer, 1, recpspbuffer, 0,
	// len - 13);
	//
	// //begin get width height from jni
	// withAndHight = VideoUtils
	// .getWithAndHight(recpspbuffer);//
	//
	// if (withAndHight != null) {
	// // �����Ⱥ͸߶�
	// pixWidth = withAndHight[0];
	// pixHeight = withAndHight[1];
	//
	// MyLog.e(TAG,
	// "receive parameter frame"
	// + pixWidth + " "
	// + pixHeight);
	// //��ʼ��������
	// InitNativeH264Decoder(pixWidth,pixHeight);
	//
	// } else
	// MyLog.e(TAG,
	// "get width height from jni is null");
	//
	//
	// //end
	// }
	// }
	//
	// if (pixWidth != 0 && pixHeight != 0)
	// // ���벢��ʾ��videoview��
	// UpdateVideoView(recbuffer, pixWidth, pixHeight);
	//
	// MyLog.e("***test_remote_recv", "video size:"
	// + len + "  seqnum :" + seqNumLog);
	// }// end if
	// }
	// } catch (Exception e) {
	// MyLog.e(TAG + "1", e.toString());
	// e.printStackTrace();
	// }
	// }// end while
	//
	// } catch (Exception e) {
	// MyLog.e(TAG + "2", e.toString());
	// }
	// }
	// });
	
	// H264S
	public void VideoPacketSend(byte[] buffer, int len, int marker,
			boolean h264s) {
		try {
			if (len <= 0)
				return;

			markflag = false;
			if (marker == 1)
				markflag = true;

			// �������H264�����RTP 12Ϊͷ
			newbuf = new byte[len + 12];
			System.arraycopy(buffer, 0, newbuf, 12, len);

			RtpPacket dt_packet = new RtpPacket(newbuf, 0);
			if (UserAgent.camera_PayLoadType.length() > 0)
				dt_packet.setPayloadType(Integer
						.parseInt(UserAgent.camera_PayLoadType));

			dt_packet.setPayloadLength(len);
			dt_packet.setTimestamp(seqTime);
			dt_packet.setMarker(markflag);// ����NAL ����Ҫ��Ƭ Ϊtrue
			// ��װRTP����ֽ�����
			rtpbuffer = dt_packet.getPacket();
			len = len + 12;

			seqNum++;

			sendNewByte(ShorttoByte(rtpbuffer, seqNum), len);

//			MyLog.e(TAG, "***seqnum:" + seqNum);
			// ʱ����ۼ�
			if (markflag == true)
				seqTime += 3600;

//			if (seqNum % 10 == 0) {
//				Thread.sleep(10);
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �ⲿ����H264
	public void VideoPacketToH264(byte[] buffer, int len, int marker) {
		try {
			if (len <= 0)
				return;

			markflag = false;
			if (marker == 1)
				markflag = true;
			if(seqNum == 3600){
				lastSystemTime = System.currentTimeMillis();
			}
			// �������H264�����RTP 12Ϊͷ
			newbuf = new byte[len + 12];
			System.arraycopy(buffer, 0, newbuf, 12, len);

			RtpPacket dt_packet = new RtpPacket(newbuf, 0);
			if (UserAgent.camera_PayLoadType.length() > 0)
				dt_packet.setPayloadType(Integer
						.parseInt(UserAgent.camera_PayLoadType));

			dt_packet.setPayloadLength(len);
			dt_packet.setTimestamp(seqTime);
			dt_packet.setMarker(markflag);// ����NAL ����Ҫ��Ƭ Ϊtrue
			// ��װRTP����ֽ�����
			rtpbuffer = dt_packet.getPacket();
			len = len + 12;

			// ==================begin============================
			parVal = (rtpbuffer[12] & 0x1F);
			if (parVal == 7) {// sps �жϲ� �������֡
				parambyte = new byte[len];
				System.arraycopy(rtpbuffer, 0, parambyte, 0, len);
				// �����ͳ�ȥ,ֻ�Ǳ�������
				MyLog.e("test_local_saveparameter", "save sps parameter frame"
						+ seqNum);

				return;
			}
			// ����pps֡
			if (parVal == 8) {
				picbyte = new byte[len];
				System.arraycopy(rtpbuffer, 0, picbyte, 0, len);
				// �����ͳ�ȥ,ֻ�Ǳ�������
				MyLog.e("test_local_saveparameter", "save pps parameter frame"
						+ seqNum);

				return;
			}
			seqNum++;
			// �ж��Ƿ�fu��ʽ 28��fu��ʽ
			if ((rtpbuffer[12] & 0x1F) == 28) {

				// bΪ5 ��I֡
				if ((rtpbuffer[13] & 0x1F) == 5) {
					// ����I֡��ʱ���
					timeSpanByte = new byte[4];
					System.arraycopy(rtpbuffer, 4, timeSpanByte, 0, 4);

					// sΪ1 ˵���ǵ�һ����
					if ((rtpbuffer[13] >> 7 & 0x01) == 1) {
						// ���ͱ���Ĳ���֡
						if (parambyte != null) {

							// ��ǰ�濽����I֡ʱ�����ֵ������֡ʱ���λ��
							// ����4~7
							MyLog.e("test_local_sendparameter",
									"send parameter frame" + seqNum);
							System.arraycopy(timeSpanByte, 0, parambyte, 4, 4);

							// ����Ҫ���ݱ���Ĳ���֡��ʵ�ʳ���
							sendNewByte(ShorttoByte(parambyte, seqNum++),
									parambyte.length);
						}
						// p֡
						if (picbyte != null) {
							System.arraycopy(timeSpanByte, 0, picbyte, 4, 4);
							// ����Ҫ���ݱ���Ĳ���֡��ʵ�ʳ���
							sendNewByte(ShorttoByte(picbyte, seqNum++),
									picbyte.length);
						}

						//
						sendNewByte(ShorttoByte(rtpbuffer, seqNum), len);

					} else// s��Ϊ1 ˵���������İ�
					{
						sendNewByte(ShorttoByte(rtpbuffer, seqNum), len);
					}
				} else// b��Ϊ5 ��P֡
				{
					sendNewByte(ShorttoByte(rtpbuffer, seqNum), len);
				}

			} else// ������28���ǵ�����NAL��ʽ
			{
				// aΪ5 ��I֡
				if ((rtpbuffer[12] & 0x1F) == 5) {
					// ��I֡��ʱ�������������ֵ������֡ʱ���λ�� ������4~7
					timeSpanByte = new byte[4];
					System.arraycopy(rtpbuffer, 4, timeSpanByte, 0, 4);

					if (parambyte != null) {
						// ��ǰ�濽����I֡ʱ�����ֵ������֡ʱ���λ�� ����4~7
						System.arraycopy(timeSpanByte, 0, parambyte, 4, 4);
						// ����Ҫ���ݱ���Ĳ���֡��ʵ�ʳ���
						sendNewByte(ShorttoByte(parambyte, seqNum++),
								parambyte.length);
					}
					// p֡
					if (picbyte != null) {
						System.arraycopy(timeSpanByte, 0, picbyte, 4, 4);
						// ����Ҫ���ݱ���Ĳ���֡��ʵ�ʳ���
						sendNewByte(ShorttoByte(picbyte, seqNum++),
								picbyte.length);
					}

					sendNewByte(ShorttoByte(rtpbuffer, seqNum), len);
				}
				// a��Ϊ5 ��P֡
				else {
					sendNewByte(ShorttoByte(rtpbuffer, seqNum), len);
				}

			}
			// ===================end============================
//			MyLog.e(TAG, "***seqnum:" + seqNum);
			// ʱ����ۼ�
			if (markflag == true){
				seqTime += (System.currentTimeMillis() - lastSystemTime)*90;
				lastSystemTime = System.currentTimeMillis();
			}
//				seqTime += 3600;

//			if (seqNum % 10 == 0) {
//				Thread.sleep(10);
//			}
		} catch (Exception e) {
//			MyLog.e(TAG, "VideoPacketToH264 " + e.toString());
		}

	}

	public void sendNewByte(byte[] buffer, int len) {
		try {

			if (!UserAgent.Camera_URL.equals("")
					&& !UserAgent.Camera_VideoPort.equals("")) {
				// ��������h264��Ƶ������ת����������
				dpPacket = new DatagramPacket(buffer, len,
						InetAddress.getByName(UserAgent.Camera_URL),
						Integer.parseInt(UserAgent.Camera_VideoPort));
				if(mdsSocket != null){
					mdsSocket.send(dpPacket);
				}
//				mdsSocket.send(dpPacket);
				FlowStatistics.Video_Send_Data += len;

				// ����ʱ��
//				MyLog.e("test_local_send", "video size:" + len + " seq:"
//						+ getSequenceNumber(buffer, len));

			} else {
				MyLog.e(TAG, "��Ƶ�˿ڵ�ַΪ��");
			}
		} catch (Exception e) {
			e.printStackTrace();
//			try {
//				mdsSocket.close();
//				mdsSocket = null;
//				mdsSocket = new DatagramSocket();
//				// ���ܻ���Ӵ�Ϊ64k
////				mdsSocket.setReceiveBufferSize(1024 * 512);
////				mdsSocket.setSendBufferSize(1024 * 512);
//			} catch (SocketException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		}
	}

	/** Gets the sequence number */
	private synchronized int getSequenceNumber(byte[] buffer, int packet_len) {
		if (packet_len >= 12)
			return getInt(buffer, 2, 4);
		else
			return 0; // broken packet
	}

	/** Gets the timestamp */
	public long getTimestamp(byte[] buffer, int packet_len) {
		if (packet_len >= 12)
			return getLong(buffer, 4, 8);
		else
			return 0;
	}

	/** Gets Int value */
	private static int getInt(byte[] data, int begin, int end) {
		return (int) getLong(data, begin, end);
	}

	private static long getLong(byte[] data, int begin, int end) {
		long n = 0;
		for (; begin < end; begin++) {
			n <<= 8;
			n += data[begin] & 0xFF;
		}
		return n;
	}

	// ���short�㷨 ok
	private byte[] ShorttoByte(byte[] buffer, short n) {
		buffer[3] = (byte) (n & 0xff);
		buffer[2] = (byte) (n >> 8 & 0xff);
		return buffer;
	}

	/** Whether has marker (M) */
	private boolean hasMarker(byte[] buffer, int packet_len) {
		if (packet_len >= 12)
			return getBit(buffer[1], 7);
		else
			return false; // broken packet
	}

	/** Gets bit value edit..... */
	private static boolean getBit(byte b, int bit) {
		// return (b >> bit) == 1;
		return (b & 0x80) == 128;
	}

	// �ر�
	public void CloseUdpSocket() {
		this.Flag = false;
		if(t2 != null){
			t2.interrupt();
		}
		MyLog.e(TAG, "recv lost all packet:" + recLost);
		FlowStatistics.Video_Packet_Lost = 0;
		// �ȴ��߳�1�������˳��̣߳�100����ȴ��߳��˳���---���Ƽ���Ӧ���߳������˳�
		// try {
		// if(thread!=null)
		// thread.join(100);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
//		if(h264Dec != null){
//			h264Dec.releaseCodec();
//		}
		if (h264FuManager != null) {
			h264FuManager.clearFus();
		}

		if (mdsSocket != null) {
			MyLog.e(TAG, "mdsSocket Socket Close");
			mdsSocket.close();
			mdsSocket = null;
		}
	}
	public void resetDecode(){
//		recCount = 0;
		needCallTryConfig = true;
	}
	boolean isNeedDiscard(boolean isIframe, int lostNum,int packetNumInFrame){
		if(lostNum >0){
			MyLog.e(TAG+"1", "total:"+packetNumInFrame+",lost:"+lostNum);
			if(withAndHight != null && withAndHight.length == 2){
//				Tools.writeVideoFileToSD(withAndHight[0]+"*"+withAndHight[1]+":total:"+packetNumInFrame+",lost:"+lostNum+",isIFrame ="+isIframe+"\n");
			}
		}
		switch(DeviceVideoInfo.lostLevel){
		case 1:
			if(isIframe){
				if(lostNum >0){
					isIFrameDiscard = true;
					isPFrameDiscard = true;
					return true;
				}
			}else{
				if(isIFrameDiscard || isPFrameDiscard){
					isPFrameDiscard = true;
					return true;
				}
				if(lostNum > 0){
					isPFrameDiscard = true;
					return true;
				}
			}
			break;
		case 2:
			if(isIframe){
				isIFrameDiscard = false;
				return false;
			}else{
				if(isPFrameDiscard){
					isPFrameDiscard = true;
					return true;
				}
				if(lostNum > 0){
					isPFrameDiscard = true;
					return true;
				}
			}
			break;
		case 3:
			if(isIframe){
				if(packetNumInFrame*0.1 < lostNum){
					isIFrameDiscard = true;
					return true;
				}
			}else{
				if(isIFrameDiscard ||isPFrameDiscard){
					isPFrameDiscard = true;
					return true;
				}
				if(packetNumInFrame*0.1 < lostNum){
					isPFrameDiscard = true;
					return true;
				}
			}
			break;
		case 4:
			if(isIframe){
				if(packetNumInFrame*0.4 < lostNum){
					isIFrameDiscard = true;
					return true;
				}
			}else{
				if(isIFrameDiscard ||isPFrameDiscard){
					isPFrameDiscard = true;
					return true;
				}
				if(packetNumInFrame*0.4 < lostNum){
					isPFrameDiscard = true;
					return true;
				}
			}
			break;
		case 5:
			break;
		}
		return false;
	}

	public boolean IsNowSeqLarger(long nowSeq, long lastSeq) {
		if (nowSeq > lastSeq) {
			if ((nowSeq - lastSeq) < 0x7fff)
				return true;
			else
				return false;
		} else if (nowSeq < lastSeq) {
			if ((lastSeq - nowSeq) < 0x7fff)
				return false;
			else
				return true;
		} else
			return false;
	}
	boolean isExpectSeqNum(long nowSeq,long lastSeq){
		if(lastSeq+1 == nowSeq){
			return true;
		}
		if(lastSeq == 0xffff && nowSeq == 0){
			return true;
		}
		return false;
	}
	boolean isDelayOverLimitTime(long tailTS,long lastTS){
		int diff = 0;
		if(tailTS >= lastTS){
			diff=(int)(tailTS - lastTS)/90;
			if(diff > DeviceVideoInfo.MaxVideoJitterbufferDelay){
				return true;
			}else{
				return false;
			}
		}else{
			if((lastTS - tailTS) > 0x7fffffff){
				diff=(int)((0x100000000l-lastTS+tailTS)/90);
				if(diff >  DeviceVideoInfo.MaxVideoJitterbufferDelay)
					return true;
				else
					return false;
			}else{
				MyLog.e(TAG, "error happened ! error value");
				return true;
			}
		}
	}
	boolean isDelayBeyondMinTime(long tailTS,long lastTS){
		int diff = 0;
		if(tailTS >= lastTS){
			diff=(int)(tailTS - lastTS)/90;
			if(diff >  DeviceVideoInfo.MinVideoJitterbufferDelay){
				return true;
			}else{
				return false;
			}
		}else{
			if((lastTS - tailTS) > 0x7fffffff){
				diff=(int)((0x100000000l-lastTS+tailTS)/90);
				if(diff >  DeviceVideoInfo.MinVideoJitterbufferDelay)
					return true;
				else
					return false;
			}else{
				MyLog.e(TAG, "error happened ! error value");
				return true;
			}
		}
	}
	/*
	�ж�֡�Ƿ񵽲���ʱ������
	Input�� 
	long frameTS������֡��ʱ���� 
	Output��
		 true���ѵ�����ʱ�䣻
		 false��δ������ʱ�䣻
	*/
	boolean isThisFramePlayTime(long frameTS,long lastTokenFrameTS)
	{
		if(lastTokenFrameTS == -1) return true;
		long nowTimeMS = System.currentTimeMillis();
		long intervalTS = calcIntervalBetweenTwoTS(frameTS, lastTokenFrameTS);
		long intervalTime = nowTimeMS - lastTokenTimeMS;
		
		if (intervalTime >= (intervalTS/VideoPlaySpeedFactor))
			return true;
		else{
			return false;
		}
	}
	long calcIntervalBetweenTwoTS(long nowTS, long lastTS) {
		long diff;
		if (nowTS >= lastTS) {
			// ����ĳ���90���ǿ��ǵ�H264��90000/s�����ʣ�ÿ���������90����
			diff = (nowTS - lastTS) / 90;
			return diff;
		} else {
			// ����Ĳ�ֵ��0x7fffffff�Ƚϣ��ǿ���ʱ����32bitsѭ�����⣻
			if ((lastTS - nowTS) > 0x7fffffff) {
				diff = (0x100000000L  - lastTS + nowTS) / 90;
				return diff;
			} else {
				MyLog.e(TAG, "error happened ! error value");
				// ����Ӧ����Զ������룻������룬�����д��󣬴�ӡlog�ļ���
				return 0;
			}
		}
	}
	/*
	�������Ż����²���֡ʱ�䣺
	Input�� 
	long frameTS������֡��ʱ����
	Output��
		 void��
	*/
	void setAndAdjustLastTokenTimeMS(long frameTS)
	{
		if(lastTokenTimeMS == -1){
			lastTokenTimeMS = System.currentTimeMillis();
			return;
		}
		long nowTimeMS = System.currentTimeMillis();
		long intervalTS = calcIntervalBetweenTwoTS(frameTS, lastTokenFrameTS);
		long intervalTime = nowTimeMS - lastTokenTimeMS;
		if (intervalTime < (intervalTS/VideoPlaySpeedFactor)){
			//����Ӧ����Զ������룻������룬�����д��󣬴�ӡlog�ļ���
			return;
		};
		long diff= intervalTime - (intervalTS/VideoPlaySpeedFactor);
		if(diff > 2*MAX_HANDLE_VIDEO_PLAY_INTERVAL)
		{
			//��������������ĵط������ˣ�����ʱ����Ӧ�ô����̴߳�������
//			diff = 2*MAX_HANDLE_VIDEO_PLAY_INTERVAL;
		}
		lastTokenTimeMS= nowTimeMS - diff;//��ֱ��ʹ�õ�ǰʱ�䣬���Ǽ�ȥ���֡���ŵ�����ʱ�䣬��Ϊ�˱�������ʱ��������ۻ���
	}
}
