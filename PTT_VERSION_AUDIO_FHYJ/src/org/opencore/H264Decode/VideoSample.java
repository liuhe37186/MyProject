package org.opencore.H264Decode;

public class VideoSample {
	public byte[] data = null;
	public int timestamp = 0;

	public VideoSample(byte[] paramArrayOfByte, int paramInt) {
		this.data = paramArrayOfByte;
		this.timestamp = paramInt;
	}
}
