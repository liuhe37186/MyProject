package com.ffmpeg;

public class H264Decode {
	static {
		System.loadLibrary("ffmpeg");
	}

	public static native int DecodeOneFrame(int paramInt1,
			byte[] paramArrayOfByte, int paramInt2, int paramInt3);

	public static native int Destory(int paramInt);

	public static native int GetHeight(int paramInt);

	public static native int GetPixel(int paramInt, int[] paramArrayOfInt);

	public static native int GetWidth(int paramInt);

	public static native int GetYUVPixels(int paramInt,
			byte[] paramArrayOfByte1, byte[] paramArrayOfByte2,
			byte[] paramArrayOfByte3);

	public static native int Initialize(int paramInt);
	
	
}
