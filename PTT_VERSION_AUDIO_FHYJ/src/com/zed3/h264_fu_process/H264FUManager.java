package com.zed3.h264_fu_process;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zoolu.tools.MyLog;

import android.util.Log;

import com.zed3.net.RtpPacket;

public class H264FUManager {
	/** 分片处理过程的缓冲区，集合； */
	// private List<FU> fuList;//理解为帧集合（完整的帧framelist）不是包集合， fu属于硬切，强制将包切割成1400
	private FU sigFu;

	private FU sigFU2;
	// public List<FU> fuList4Eyebeam;// 软切 eyebeam合片，效果好
	private FU sigFuEyebeam;

	private static H264FUManager instance;
	/** 集合元素，迭代元素 */
	private String tag = "H264FUManager";
	//
	// private FU it;
	// private Iterator<FU> iterator2;

	private long fuTimeStamp = 0, startBit = 0, endBit = 0, eyeTimeStamp = 0;
	private int fuSeqNum = 0, fuPayloadLen = 0, eyeSeqNum = 0,
			eyePayloadLen = 0;
	// public boolean IDR = false;
	static {
		instance = new H264FUManager();
	}

	private H264FUManager() {
		// fuList = new LinkedList<FU>();
		sigFu = new FU();
		sigFU2 = new FU();
		// fuList4Eyebeam = new LinkedList<FU>();
		sigFuEyebeam = new FU();
	}

	public static H264FUManager getInstance() {
		return instance;
	}

	/** 在每次视频通话结束后,清空集合； */
	public void clearFus() {
		// IDR = false;//
		// if (fuList.size() > 0)
		// fuList.clear();
		if (sigFu != null) {
			sigFu.init(0);
		}
		if (sigFuEyebeam != null) {
			sigFuEyebeam.init(0);
		}
		// if (fuList4Eyebeam.size() > 0)
		// fuList4Eyebeam.clear();
	}

	/**
	 * 该方法为对H264的FU分片进行组合（针对type为28的包进行组片，其余类型包直接对RTP负载解码即可） rtppack ：rtp数据包 返回值
	 * ： FU结构体指针
	 */

	/**
	 * 该方法为对H264的FU分片进行组合（针对type为28的包进行组片，其余类型包直接对RTP负载解码即可）
	 * 
	 * @param rtppack
	 *            rtp数据包
	 * 
	 * @return FU
	 * 
	 */
	// FU *H264FUManager2::ProcessFU(RtpPacket *rtppack)
	public FU processFU(RtpPacket rtppack) {
		MyLog.e("processFU", "begin:");
		try {
			byte[] pPayload = rtppack.getPayload();
			// unsigned int startBit = pPayload[1] & 0x80;
			startBit = pPayload[1] & 0x80;
			// unsigned int endBit = pPayload[1] & 0x40;
			endBit = pPayload[1] & 0x40;
			// FU * pfuGet = NULL;
			FU pfuGet = null;
			fuTimeStamp = rtppack.getTimestamp();
			fuSeqNum = rtppack.getSequenceNumber();
			fuPayloadLen = rtppack.getPayloadLength();

			Log.i(tag, "startBit:endBit---" + startBit + ":" + endBit
					+ " seqnum:" + fuSeqNum);

			// 如果为第一个分片
			if (startBit != 0/* true */) {
				if (sigFu.packetState == FU.PACKET_STATE_COMPLETE)
					sigFu.init(fuSeqNum);
				else if ((sigFu.packetState == FU.PACKET_STATE_CONTINUE) && (sigFu.timeStamp < fuTimeStamp)) {
					fuCopy(sigFu, sigFU2);
					sigFU2.lostCount += CalcDiffOfTwoSequence((int)(sigFU2.seqNumReconstruct + 2),fuSeqNum);
					sigFU2.totalCount = CalcDiffOfTwoSequence((int)(sigFU2.firstSeqNum),fuSeqNum);
					pfuGet = sigFU2;
					if(pfuGet.getDataLen() == 0){
						MyLog.e("fu_test", "p 1");
					}
					sigFu.init(fuSeqNum);
				}
				// }
				// ////////////end//////////////

				// Log.i(tag, "第一个分片 fuList.size():" + fuList.size()
				// + "pPayload.length:" + pPayload.length);
				// 新创建一个FU结构体，存储分片组合后的H264帧
				/*
				 * FU *pfu = new FU; pfu->timeStamp = rtppack->hdr().timeStamp;
				 * pfu->seqNumOrig = rtppack->hdr().seqNum;
				 * pfu->seqNumReconstruct = pfu->seqNumOrig;
				 */

				// FU pfu = new FU();

				sigFu.timeStamp = fuTimeStamp/* hdr().timeStamp */;
				sigFu.seqNumOrig = fuSeqNum;
				sigFu.seqNumReconstruct = sigFu.seqNumOrig;

				// 合并H264 nal头，并将第一片存入FU中
				// char nalhr;
				byte nalhr;
				// nalhr = ((pPayload[0] & 0xE0) + (pPayload[1] & 0x1F));
				nalhr = (byte) ((pPayload[0] & 0xE0) + (pPayload[1] & 0x1F));// 合并H264
																				// nal头，并将第一片存入FU中

				// pfu->data.append(&nalhr, 1);
				sigFu.data[0] = nalhr;
				sigFu.len += 1;// pfu.len默认值为0
				// edit by hdf
				System.arraycopy(pPayload, 2, sigFu.data, (int) sigFu.len,
						rtppack.getPayloadLength() - 2);
				sigFu.packetState = FU.PACKET_STATE_CONTINUE;
				Log.i(tag, "pfu.len:" + sigFu.len);
				// pfu->len += (rtppack->payloadLength()) - 1;
				sigFu.len += fuPayloadLen - 2;// 为什么是-1，而不是-2？第一片

				// 将FU放到一个集合中
				// fuList.add(pfu);//fulist是完整帧集合
				// Log.i(tag, "fuList.size():" + fuList.size());

				// MyLog.e("processFU",
				// "first fu:"+(System.currentTimeMillis()-a));
			}
			// 中间片或最后一片
			else {
				// Log.i(tag, "中间片或最后一片:fuList.size():" + fuList.size());
				// 遍历集合的所有FU
				// list<FU *>::iterator it = m_listFuSet.begin();
				// while (it != m_listFuSet.end())
				// Iterator iterator;
				// Iterator<FU> iterator = fuList.iterator();
				// while (iterator.hasNext()) {
				// fu = (FU) iterator.next();
				// 如果找到FU的时间戳与新收到的FU分片的时间戳一致
				/* else---hou end--- */if (sigFu.timeStamp == fuTimeStamp) {
					// 如果新收到的分片的序列号 == 上一个分片的序列号 + 1
					// if (((*it)->seqNumReconstruct + 1) ==
					// rtppack->hdr().seqNum)//分片时间戳一样但seqnum都是累加1的
					if ((sigFu.seqNumReconstruct + 1) != fuSeqNum && fuSeqNum != 0) {
						sigFu.lostCount += CalcDiffOfTwoSequence((int)(sigFu.seqNumReconstruct + 1),fuSeqNum);
					}
						System.arraycopy(pPayload, 2, sigFu.data,
								(int) sigFu.len, fuPayloadLen - 2);

						// (*it)->seqNumReconstruct = rtppack->hdr().seqNum;
						sigFu.seqNumReconstruct = fuSeqNum;
						// (*it)->len += rtppack->payloadLength() - 2;
						sigFu.len += fuPayloadLen - 2;

						Log.i(tag, "same seqnum:" + sigFu.seqNumReconstruct);

						if ((endBit != 0) || (rtppack.hasMarker())) {// 最后一个fu包
						// if ((sigFu.data[0] & 0x1f) ==
						// 5){//fu.data为上面arraycopy以后的完整的一帧 |若为IDR帧
						// H264FUManager.getInstance().IDR = true;
						// }

//							sigFu.isComplete = true;// 是否是完整的包
							sigFu.packetState = FU.PACKET_STATE_COMPLETE;
							// if (H264FUManager.getInstance().IDR == true)
							sigFu.totalCount =CalcDiffOfTwoSequence((int)(sigFu.firstSeqNum),fuSeqNum + 1) ;
							pfuGet = sigFu;
							if(pfuGet.getDataLen() == 0){
								MyLog.e("fu_test", "p 2");
							}
							// else
							// MyLog.e("cameracall",
							// "FU process P delete seqnum:"
							// + sigFu.seqNumReconstruct);
						}
					// break;??????
				} else {
					if(sigFu.packetState == FU.PACKET_STATE_CONTINUE){
						fuCopy(sigFu, sigFU2);
						sigFU2.lostCount += CalcDiffOfTwoSequence((int)(sigFU2.seqNumReconstruct + 2),fuSeqNum);
						sigFU2.totalCount = CalcDiffOfTwoSequence(sigFU2.firstSeqNum,fuSeqNum);
						pfuGet = sigFU2;
						if(pfuGet.getDataLen() == 0){
							MyLog.e("fu_test", "p 3");
						}
					}
					if(fuSeqNum > 0){
						sigFu.init(fuSeqNum-1);
					}else{
						sigFu.init(fuSeqNum);
					}
					
					sigFu.timeStamp = fuTimeStamp/* hdr().timeStamp */;
					sigFu.seqNumOrig = fuSeqNum;
					sigFu.seqNumReconstruct = sigFu.seqNumOrig;
					sigFu.lostCount = 1;

					// 合并H264 nal头，并将第一片存入FU中
					// char nalhr;
					byte nalhr;
					// nalhr = ((pPayload[0] & 0xE0) + (pPayload[1] & 0x1F));
					nalhr = (byte) ((pPayload[0] & 0xE0) + (pPayload[1] & 0x1F));// 合并H264
																					// nal头，并将第一片存入FU中

					// pfu->data.append(&nalhr, 1);
					sigFu.data[0] = nalhr;
					sigFu.len += 1;// pfu.len默认值为0
					// edit by hdf
					System.arraycopy(pPayload, 2, sigFu.data, (int) sigFu.len,
							rtppack.getPayloadLength() - 2);
					sigFu.packetState = FU.PACKET_STATE_CONTINUE;
					Log.i(tag, "pfu.len:" + sigFu.len);
					// pfu->len += (rtppack->payloadLength()) - 1;
					sigFu.len += fuPayloadLen - 2;// 为什么是-1，而不是-2？第一片

					// do nothing
					Log.i(tag, "do nothing");
				}

				// }// end while
			}
			if (rtppack.hasMarker()) {// 最后一个fu包
//				sigFu.isComplete = true;// 是否是完整的包
				sigFu.packetState = FU.PACKET_STATE_COMPLETE;
				sigFu.totalCount = CalcDiffOfTwoSequence((int)(sigFu.firstSeqNum),fuSeqNum + 1);
				pfuGet = sigFu;
				if(pfuGet.getDataLen() == 0){
					MyLog.e("fu_test", "p 4");
				}
			}
			return pfuGet;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// H264 eyebeam专用
	public FU processFU4Eyebeam(RtpPacket rtppack) {
		try {
			byte[] pPayload = rtppack.getPayload()/* payloadBuffer() */;
			long startBit = pPayload[1] & 0x80;
			boolean endBit = rtppack.hasMarker();

			eyeTimeStamp = rtppack.getTimestamp();
			eyeSeqNum = rtppack.getSequenceNumber();
			eyePayloadLen = rtppack.getPayloadLength();

			FU pfuGet = null;
			byte[] eyeBeamhead ={0,0,0,1};
			// start
			if (startBit != 0) {
				if (/*sigFuEyebeam.isComplete == true*/sigFuEyebeam.packetState == FU.PACKET_STATE_COMPLETE)
					sigFuEyebeam.init(eyeSeqNum);
				else if (sigFuEyebeam.timeStamp < eyeTimeStamp) {
					fuCopy(sigFuEyebeam, sigFU2);
					sigFU2.lostCount += CalcDiffOfTwoSequence((int)(sigFU2.seqNumReconstruct + 2),eyeSeqNum);
					sigFU2.totalCount = CalcDiffOfTwoSequence(sigFU2.firstSeqNum,eyeSeqNum);
					pfuGet = sigFU2;
					sigFuEyebeam.init(eyeSeqNum);
				}
				// FU pfu = new FU();
				// pfu->timeStamp = rtppack->hdr().timeStamp;
				sigFuEyebeam.timeStamp = eyeTimeStamp;

				// pfu->seqNumOrig = rtppack->hdr().seqNum;
				sigFuEyebeam.seqNumOrig = eyeSeqNum;

				// pfu->seqNumReconstruct = pfu->seqNumOrig;
				sigFuEyebeam.seqNumReconstruct = sigFuEyebeam.seqNumOrig;

				// Not need H264 start code,because input filter will add
				// Data copy
				// pfu->data.append((char *)pPayload, rtppack->payloadLength());
				System.arraycopy(pPayload, 0, sigFuEyebeam.data,
						(int) sigFuEyebeam.len, eyePayloadLen);
				// pfu->len += rtppack->payloadLength();
				sigFuEyebeam.len += eyePayloadLen;
				sigFuEyebeam.packetState = FU.PACKET_STATE_CONTINUE;

				MyLog.e(tag, "startBit != 0 pfu len:" + sigFuEyebeam.len
						+ " seqnum:" + sigFuEyebeam.seqNumOrig);

				// m_listFuSet.push_back(pfu);
				// fuList4Eyebeam.add(pfu);

			}
			// end and middle
			else {
				// list<FU *>::iterator it = m_listFuSet.begin();
				// iterator2 = fuList4Eyebeam.iterator();
				// while (iterator2.hasNext()) {
				// it = (FU) iterator2.next();
				MyLog.e(tag, "it.timeStamp--> " + sigFuEyebeam.timeStamp
						+ " it.seqnum--->" + sigFuEyebeam.seqNumOrig);

				if (sigFuEyebeam.timeStamp != eyeTimeStamp) {
					// iterator2.remove();
					// sigFuEyebeam.init();
					// MyLog.e(tag, "fuList4Eyebeam remove()--->size:"
					// + fuList4Eyebeam.size() + " it.seqnum--->"
					// + it.seqNumOrig);
					// if (sigFuEyebeam.isComplete == true)
					// sigFuEyebeam.init();
					// else if (sigFuEyebeam.timeStamp < eyeTimeStamp) {
					if(sigFuEyebeam.packetState == FU.PACKET_STATE_CONTINUE){
					fuCopy(sigFuEyebeam, sigFU2);
					sigFU2.lostCount += CalcDiffOfTwoSequence((int)(sigFU2.seqNumReconstruct + 2),eyeSeqNum);
					sigFU2.totalCount = CalcDiffOfTwoSequence(sigFU2.firstSeqNum,eyeSeqNum);
					pfuGet = sigFU2;
					}
					if(eyeSeqNum > 0){
						sigFuEyebeam.init(eyeSeqNum-1);
					}else{
						sigFuEyebeam.init(eyeSeqNum);
					}
					sigFuEyebeam.lostCount = 1;
					// }
					// FU pfu = new FU();
					// pfu->timeStamp = rtppack->hdr().timeStamp;
					sigFuEyebeam.timeStamp = eyeTimeStamp;

					// pfu->seqNumOrig = rtppack->hdr().seqNum;
					sigFuEyebeam.seqNumOrig = eyeSeqNum;

					// pfu->seqNumReconstruct = pfu->seqNumOrig;
					sigFuEyebeam.seqNumReconstruct = sigFuEyebeam.seqNumOrig;

					// Not need H264 start code,because input filter will add
					// Data copy
					// pfu->data.append((char *)pPayload,
					// rtppack->payloadLength());
					System.arraycopy(pPayload, 0, sigFuEyebeam.data,
							(int) sigFuEyebeam.len, eyePayloadLen);
					sigFuEyebeam.packetState = FU.PACKET_STATE_CONTINUE;
					// pfu->len += rtppack->payloadLength();
					sigFuEyebeam.len += eyePayloadLen;

					MyLog.e(tag, "startBit != 0 pfu len:" + sigFuEyebeam.len
							+ " seqnum:" + sigFuEyebeam.seqNumOrig);

					// m_listFuSet.push_back(pfu);
					// fuList4Eyebeam.add(pfu);
					// continue;
				} else if (sigFuEyebeam.timeStamp == eyeTimeStamp) {
					MyLog.e(tag, "it.seqNumReconstruct --->"
							+ sigFuEyebeam.seqNumReconstruct
							+ " rtppack.getSequenceNumber()--->" + eyeSeqNum);

					if ((sigFuEyebeam.seqNumReconstruct + 1) != eyeSeqNum && eyeSeqNum != 0){
						sigFuEyebeam.lostCount += CalcDiffOfTwoSequence((int)(sigFuEyebeam.seqNumReconstruct + 1),eyeSeqNum);
					} 
						// (*it)->data.append(3, 0x00);

						// 屏蔽
						// byte[] src1 = new byte[] { 0, 0, 0 };
						// // System.arraycopy(src1, 0, sigFuEyebeam.data, (int)
						// sigFuEyebeam.len, 3);
						// sigFuEyebeam.len += 3;
						// // (*it)->data.append(1, 0x01);
						// byte[] src2 = new byte[] { 1 };
						// System.arraycopy(src2, 0, sigFuEyebeam.data, (int)
						// sigFuEyebeam.len, 1);
						// // (*it)->len += 4;
						// sigFuEyebeam.len += 1;
						System.arraycopy(eyeBeamhead, 0/* 1 */, sigFuEyebeam.data,
								(int) sigFuEyebeam.len,// 是从最后一位的下一位开始保存。
								4/*-1*/);
						sigFuEyebeam.len += 4;
						System.arraycopy(pPayload, 0/* 1 */, sigFuEyebeam.data,
								(int) sigFuEyebeam.len,// 是从最后一位的下一位开始保存。
								eyePayloadLen/*-1*/);
						sigFuEyebeam.seqNumReconstruct = eyeSeqNum;
						sigFuEyebeam.len += (eyePayloadLen /*-1*/);
						if (endBit) {
//							sigFuEyebeam.isComplete = true;
							sigFuEyebeam.packetState = FU.PACKET_STATE_COMPLETE;
							sigFuEyebeam.totalCount = CalcDiffOfTwoSequence(sigFuEyebeam.firstSeqNum,eyeSeqNum + 1);
						}
				} else {
					// do nothing
				}

				// }// end while
			}
			if (rtppack.hasMarker()) {
//				sigFuEyebeam.isComplete = true;
				sigFuEyebeam.packetState = FU.PACKET_STATE_COMPLETE;
				sigFuEyebeam.totalCount = CalcDiffOfTwoSequence(sigFuEyebeam.firstSeqNum,eyeSeqNum + 1);
				pfuGet = sigFuEyebeam;
			}
			return pfuGet;
		} catch (Exception e) {
			MyLog.e("processFU4Eyebeam error", e.toString());
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @param f1 src
	 * @param f2 dst
	 * */
	void fuCopy(FU f1,FU f2){
		if(f1 == null || f2 == null) return;
		f2.packetState =f1.packetState;
		f2.len = f1.len;
		f2.lostCount = f1.lostCount;
		f2.totalCount = f1.totalCount;
		f2.firstSeqNum = f1.firstSeqNum;
		f2.seqNumOrig = f1.seqNumOrig;
		f2.seqNumReconstruct = f1.seqNumReconstruct;
		f2.timeStamp = f1.timeStamp;
		f2.type = f1.type;
		System.arraycopy(f1.data, 0, f2.data, 0, f1.data.length);
	}
	int CalcDiffOfTwoSequence(int FirstSeq,int LastSeq)
	{
		int result=0;
		if(FirstSeq<0||FirstSeq>0xFFFF)
			FirstSeq=0xFFFF;
		if(LastSeq<0||LastSeq>0xFFFF)
			LastSeq=0xFFFF;
		if(LastSeq>=FirstSeq)
			{
			result=LastSeq-FirstSeq;
			}
		else
			{
			result=(0xFFFF-FirstSeq)+LastSeq;
			}
		return result;
	}
}
