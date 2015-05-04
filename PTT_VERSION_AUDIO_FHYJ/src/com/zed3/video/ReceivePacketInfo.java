package com.zed3.video;

public class ReceivePacketInfo {
	private int seqNum;
	private byte[] data;
	private long timeStamp;
	private int length;
	public ReceivePacketInfo(int seqNum,byte[] data,long timeStamp){
		this.seqNum = seqNum;
		this.data = data;
		this.setTimeStamp(timeStamp);
	}
	public ReceivePacketInfo(int seqNum,byte[] data,long timeStamp,int length){
		this.seqNum = seqNum;
		this.data = data.clone();
		this.setTimeStamp(timeStamp);
		this.setLength(length);
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
}
